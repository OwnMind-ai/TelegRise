package org.telegrise.telegrise.core.expressions;

import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.expressions.tokens.*;

import java.util.List;

import static org.telegrise.telegrise.core.expressions.ErrorCodes.ILLEGAL_IF_ARGUMENT;
import static org.telegrise.telegrise.core.expressions.ErrorCodes.MISSING_DO_STATEMENT;
import static org.telegrise.telegrise.core.expressions.Syntax.*;

public class Parser {
    private static final PunctuationToken EXPRESSION_START_TOKEN = new PunctuationToken(PARENTHESES_START);
    private static final KeywordToken IF_START_TOKEN = new KeywordToken(IF);
    private static final KeywordToken IF_DO_TOKEN = new KeywordToken(DO);
    private static final KeywordToken IF_ELSE_TOKEN = new KeywordToken(ELSE);

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Token parse() throws ReferenceParsingException {
        return this.buildExpressionTree(this.parseToken(), 0);
    }

    private Token parseToken() throws ReferenceParsingException {
        Token token = this.lexer.next();

        if (EXPRESSION_START_TOKEN.equals(token)){
            Token expression = this.parse();
            this.lexer.next(); // Skip expression end
            return expression;
        }

        if (IF_START_TOKEN.equals(token)){
            return parseIfToken();
        }

        if (token != null && List.of(TokenTypes.REFERENCE, TokenTypes.GENERATOR, TokenTypes.VALUE).contains(token.getTokenType()))
            return token;

        throw new ReferenceParsingException(ErrorCodes.UNDEFINED_TOKEN, this.lexer.getPosition());
    }

    private Token parseIfToken() throws ReferenceParsingException {
        IfToken result = new IfToken();
        result.setPredicate(this.parse());

        if (this.isPassiveToken(result.getPredicate()))
            throw new ReferenceParsingException(ILLEGAL_IF_ARGUMENT, this.lexer.getPosition());

        if (!IF_DO_TOKEN.equals(this.lexer.next())) {
            throw new ReferenceParsingException(MISSING_DO_STATEMENT, this.lexer.getPosition());
        }

        result.setDoAction(this.parse());

        if (this.isPassiveToken(result.getDoAction()))
            throw new ReferenceParsingException(ILLEGAL_IF_ARGUMENT, this.lexer.getPosition());

        if (IF_ELSE_TOKEN.equals(this.lexer.peek())){
            this.lexer.next();

            result.setElseAction(this.parse());

            if (this.isPassiveToken(result.getElseAction()))
                throw new ReferenceParsingException(ILLEGAL_IF_ARGUMENT, this.lexer.getPosition());
        }

        return result;
    }

    private boolean isPassiveToken(Token token){
        return !List.of(TokenTypes.REFERENCE, TokenTypes.EXPRESSION, TokenTypes.REFERENCE, TokenTypes.VALUE, TokenTypes.IF_CONSTRUCTION).contains(token.getTokenType());
    }

    private Token buildExpressionTree(Token previousToken, int currentPrecedence) throws ReferenceParsingException {
        Token token = this.lexer.peek();

        if (token != null && token.getTokenType() == TokenTypes.OPERATOR){
            OperatorToken operatorToken = (OperatorToken) token;

            int nextPrecedence = this.getPrecedence(operatorToken.getOperator());
            if (currentPrecedence < nextPrecedence){
                this.lexer.next();

                return this.buildExpressionTree(
                        new ExpressionToken(previousToken, this.buildExpressionTree(this.parseToken(), nextPrecedence), operatorToken),
                        currentPrecedence
                );
            }
        }

        return previousToken;
    }

    private int getPrecedence(@NotNull String operator) throws ReferenceParsingException {
        // ALWAYS > 0
        return switch (operator.toUpperCase()) {
            case PARALLEL_SEPARATOR -> 1;
            case LIST_SEPARATOR -> 2;
            case OR_OPERATOR -> 4;
            case AND_OPERATOR -> 5;
            case GREATER_OPERATOR, GREATER_OR_EQUALS_OPERATOR, LESS_OPERATOR, LESS_OR_EQUALS_OPERATOR -> 6;
            case EQUALS_OPERATOR, NOT_EQUALS_OPERATOR -> 7;
            case CHAIN_SEPARATOR -> 11;
            default ->
                    throw new ReferenceParsingException(ErrorCodes.UNDEFINED_OPERATOR, this.lexer.getPosition() - operator.length());
        };
    }
}
