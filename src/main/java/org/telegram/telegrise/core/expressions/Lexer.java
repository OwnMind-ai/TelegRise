package org.telegram.telegrise.core.expressions;

import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.expressions.tokens.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

        Optional<Token> referenceToken = this.readReference();
        if (referenceToken.isPresent())
            return referenceToken.get();

        throw new ReferenceParsingException(ErrorCodes.UNDEFINED_TOKEN, this.charsStream.getPosition());
    }

    private Optional<KeywordToken> readKeyword() {
        String candidateString = this.charsStream.peek(Syntax.MAX_KEYWORDS_LENGTH).toUpperCase();

        Optional<KeywordToken> result = Syntax.KEYWORDS.stream()
                .map(String::toUpperCase)
                .filter(candidateString::startsWith)
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
        String candidateString = this.charsStream.peek(Syntax.MAX_OPERATORS_LENGTH).toUpperCase();

        Optional<OperatorToken> result = Syntax.OPERATORS.stream()
                .map(String::toUpperCase)
                .filter(candidateString::startsWith)
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
        private final List<String> params = new LinkedList<>();

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

            StringBuilder parameter = new StringBuilder();
            while (!charsStream.eof()){
                skipWhitespaces();
                char c = charsStream.peek();

                if (c == '"') {
                    parameter.append(charsStream.next());
                    parameter.append(readWhile(s -> !"\"".equals(s), true));
                    parameter.append(charsStream.next());  // Adds '"'
                } else if (c == '\''){
                    parameter.append(charsStream.next());
                    parameter.append(readWhile(s -> !"'".equals(s), true));
                    parameter.append(charsStream.next());  // Adds '\''
                } else if (c == ',') {
                    this.params.add(parameter.toString());
                    parameter = new StringBuilder();
                    charsStream.next();
                } else if (c == '(' || c == ')'){
                    if (c == '('){
                        opened++;
                    } else {
                        closed++;
                    }

                    parameter.append(charsStream.next());
                    if (opened == closed) {
                        String last = parameter.toString();
                        this.params.add(last.substring(0, last.length() - 1));  // Removes redundant ')' at the end
                        break;
                    }
                } else {
                    parameter.append(charsStream.next());
                }
            }

            return opened == closed;
        }
    }
}
