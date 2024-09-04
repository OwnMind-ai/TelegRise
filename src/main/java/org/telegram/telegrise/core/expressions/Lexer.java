package org.telegram.telegrise.core.expressions;

import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.expressions.tokens.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class Lexer {
    private final static String WHITESPACES = " \n\t\r";
    private final CharsStream charsStream;
    private Token current;

    public Lexer(CharsStream charsStream) {
        this.charsStream = charsStream;
    }

    public Token next() throws ReferenceParsingException {
        Token previous = this.current;
        this.current = null;
        return previous == null ? this.readNext() : previous;
    }

    public Token peek() throws ReferenceParsingException {
        if (charsStream.eof())
            return null;

        if (this.current == null) {
            this.current = this.readNext();
        }

        return this.current;
    }

    private Token readNext() throws ReferenceParsingException {
        this.skipWhitespaces();
        if (this.charsStream.eof()) return null;

        Optional<OperatorToken> operatorCandidate = this.readOperator();
        if (operatorCandidate.isPresent()) {
            return operatorCandidate.get();
        }

        Optional<PunctuationToken> punctuationCandidate = this.readPunctuation();
        if (punctuationCandidate.isPresent()) {
            return punctuationCandidate.get();
        }

        Optional<KeywordToken> keywordToken = this.readKeyword();
        if (keywordToken.isPresent()) {
            return keywordToken.get();
        }

        Optional<ValueToken> valueToken = this.readValue();
        if (valueToken.isPresent())
            return valueToken.get();

        Optional<Token> referenceToken = this.readReference();
        if (referenceToken.isPresent())
            return referenceToken.get();

        throw new ReferenceParsingException(ErrorCodes.UNDEFINED_TOKEN, this.charsStream.getPosition());
    }

    private Optional<ValueToken> readValue() {
        char c = this.charsStream.peek();

        if (c == '"' || c == '\'') {
            charsStream.next();
            String value = this.readWhile(s -> !s.equals(String.valueOf(c)), true);
            charsStream.next();
            return Optional.of(new ValueToken(c == '"' ? value : value.charAt(0),
                    c == '"' ? String.class : Character.class));
        } else if (Character.isDigit(c)) {
            String value = this.readWhile(s -> Character.isDigit(s.charAt(0)));
            if (this.charsStream.peek() == '.') {
                value += this.charsStream.next();
                value += this.readWhile(s -> Character.isDigit(s.charAt(0)));
                return Optional.of(new ValueToken(Double.parseDouble(value), Double.class));
            }

            return Optional.of(new ValueToken(Long.parseLong(value), Long.class));
        }

        return Stream.of("true", "false").filter(s -> this.charsStream.peek(5).startsWith(s))
                .map(s -> new ValueToken(Boolean.parseBoolean(s), Boolean.class))
                .findFirst();
    }

    private Optional<KeywordToken> readKeyword() {
        String candidateString = this.charsStream.peek(Syntax.MAX_KEYWORDS_LENGTH + 1).toUpperCase();

        Optional<KeywordToken> result = Syntax.KEYWORDS.stream()
                .map(String::toUpperCase)
                .filter(prefix -> candidateString.startsWith(prefix + " "))
                .map(KeywordToken::new)
                .findFirst();

        result.ifPresent(keywordToken -> this.charsStream.next(keywordToken.getKeyword().length()));

        return result;
    }

    private Optional<Token> readReference() {
        return Optional.ofNullable(new ReferenceReader().read());
    }

    private Optional<PunctuationToken> readPunctuation() {
        String s = String.valueOf(this.charsStream.peek());
        if (s.equals(Syntax.PARENTHESES_START) || s.equals(Syntax.PARENTHESES_END)) {
            return Optional.of(new PunctuationToken(String.valueOf(this.charsStream.next())));
        }

        return Optional.empty();
    }

    private Optional<OperatorToken> readOperator() {
        String candidateString = this.charsStream.peek(Syntax.MAX_OPERATORS_LENGTH + 1).toUpperCase();

        Optional<OperatorToken> result = Syntax.OPERATORS.stream()
                .map(String::toUpperCase)
                .filter(prefix -> candidateString.startsWith(prefix + " "))
                .map(OperatorToken::new)
                .findFirst();

        result.ifPresent(operatorToken -> this.charsStream.next(operatorToken.getOperator().length()));

        return result;
    }

    private void skipWhitespaces() {
        this.readWhile(WHITESPACES::contains);
    }

    private String readWhile(Predicate<String> predicate) {
        return readWhile(predicate, false);
    }

    private String readWhile(Predicate<String> predicate, boolean skipEscape) {
        StringBuilder text = new StringBuilder();

        while (!this.charsStream.eof() && ((skipEscape && this.charsStream.peek() == '\\')
                || predicate.test(String.valueOf(this.charsStream.peek()))))
        {
            if (skipEscape && this.charsStream.peek() == '\\')
                text.append(this.charsStream.next());

            text.append(this.charsStream.next());
        }

        return text.toString();
    }

    public int getPosition(){
        return this.charsStream.getPosition();
    }

    private class ReferenceReader{
        private final List<PrimitiveToken> params = new LinkedList<>();

        private ReferenceReader() {
        }

        public Token read(){
            String className = null;
            if (!Syntax.METHOD_REFERENCE_START.equals(String.valueOf(charsStream.peek()))){
                className = readWhile(Pattern.compile("[.\\w_]").asPredicate());
            }

            // Skip '#'
            if (!Syntax.METHOD_REFERENCE_START.equals(String.valueOf(charsStream.next()))) return null;

            String methodName = readWhile(Pattern.compile("[\\w_]").asPredicate());

            if (Syntax.PARENTHESES_START.equals(String.valueOf(charsStream.peek()))){
                boolean success = this.readParameters();

                if (!success){
                    return null;
                }
            }

            return new MethodReferenceToken(className, methodName, params.isEmpty() ? null : params);
        }

        private boolean readParameters() {
            charsStream.next(); // Skip '('

            int opened = 1;
            int closed = 0;

            StringBuilder parameterValue = new StringBuilder();
            while (!charsStream.eof()){
                skipWhitespaces();
                char c = charsStream.peek();

                if (c == '"') {
                    parameterValue.append(charsStream.next());
                    parameterValue.append(readWhile(s -> !"\"".equals(s), true));
                    parameterValue.append(charsStream.next());  // Adds '"'
                } else if (c == '\''){
                    parameterValue.append(charsStream.next());
                    parameterValue.append(readWhile(s -> !"'".equals(s), true));
                    parameterValue.append(charsStream.next());  // Adds '\''
                } else if (c == ',') {
                    this.addParameter(parameterValue.toString());
                    parameterValue = new StringBuilder();
                    charsStream.next();
                } else if (c == '(' || c == ')'){
                    if (c == '('){
                        opened++;
                    } else {
                        closed++;
                    }

                    parameterValue.append(charsStream.next());
                    if (opened == closed) {
                        String last = parameterValue.toString();
                        this.addParameter(last.substring(0, last.length() - 1));  // Removes redundant ")" at the end
                        break;
                    }
                } else {
                    parameterValue.append(charsStream.next());
                }
            }

            return opened == closed;
        }

        private void addParameter(String parameter){
            if (parameter.isEmpty()) return;

            if (parameter.startsWith("\"") && parameter.endsWith("\"")
                    && parameter.replace("\\\"", "").split("\"").length <= 2){  // Ensures that there is only one string and not an expression like "a" + "b"
                this.params.add(new ValueToken(parameter.substring(1, parameter.length() - 1), String.class));
            } else if (parameter.startsWith("'") && parameter.endsWith("'")){
                this.params.add(new ValueToken(parameter.substring(1, parameter.length() - 1), Character.class));
            } else if (parameter.matches("\\d+")){
                this.params.add(new ValueToken(Long.parseLong(parameter), Long.class));
            } else if (parameter.matches("\\d+\\.\\d+")){
                this.params.add(new ValueToken(Double.parseDouble(parameter), Double.class));
            } else if (List.of("false", "true").contains(parameter)){
                this.params.add(new ValueToken(parameter.equals("true"), Boolean.class));
            } else {
                this.params.add(new RawToken(parameter));
            }
        }
    }
}
