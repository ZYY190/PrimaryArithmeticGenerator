package com.zyy.arithmetic;

import java.math.BigInteger;

/**
 * 递归下降解析器，支持自然数、真分数、带分数、括号和四则运算符。
 */
public final class ExpressionParser {
    private final String input;
    private int position;

    private ExpressionParser(String input) {
        this.input = input;
    }

    public static Expression parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        ExpressionParser parser = new ExpressionParser(input.replace("\uFEFF", ""));
        Expression expression = parser.parseExpression();
        parser.skipSpaces();
        if (!parser.atEnd()) {
            throw parser.error("表达式末尾存在无法识别的内容");
        }
        return expression;
    }

    private Expression parseExpression() {
        Expression expression = parseTerm();
        while (true) {
            skipSpaces();
            if (match('+')) {
                expression = new Expression.BinaryNode(Operator.ADD, expression, parseTerm());
            } else if (match('-') || match('−')) {
                expression = new Expression.BinaryNode(Operator.SUBTRACT, expression, parseTerm());
            } else {
                return expression;
            }
        }
    }

    private Expression parseTerm() {
        Expression expression = parseFactor();
        while (true) {
            skipSpaces();
            if (match('*') || match('×')) {
                expression = new Expression.BinaryNode(Operator.MULTIPLY, expression, parseFactor());
            } else if (match('/') || match('÷')) {
                expression = new Expression.BinaryNode(Operator.DIVIDE, expression, parseFactor());
            } else {
                return expression;
            }
        }
    }

    private Expression parseFactor() {
        skipSpaces();
        if (match('(')) {
            Expression expression = parseExpression();
            skipSpaces();
            if (!match(')')) {
                throw error("缺少右括号");
            }
            return expression;
        }
        if (atEnd() || !Character.isDigit(peek())) {
            throw error("需要数字或左括号");
        }
        return new Expression.NumberNode(parseNumber());
    }

    private Fraction parseNumber() {
        BigInteger first = readInteger();
        skipSpaces();
        if (match('’') || match('\'')) {
            BigInteger whole = first;
            skipSpaces();
            if (atEnd() || !Character.isDigit(peek())) {
                throw error("带分数缺少分子");
            }
            BigInteger numerator = readInteger();
            skipSpaces();
            if (!match('/')) {
                throw error("带分数缺少分数线");
            }
            BigInteger denominator = readInteger();
            return Fraction.of(whole.multiply(denominator).add(numerator), denominator);
        }
        if (match('/')) {
            BigInteger denominator = readInteger();
            return Fraction.of(first, denominator);
        }
        return Fraction.of(first, BigInteger.ONE);
    }

    private BigInteger readInteger() {
        skipSpaces();
        if (atEnd() || !Character.isDigit(peek())) {
            throw error("需要自然数");
        }
        int start = position;
        while (!atEnd() && Character.isDigit(peek())) {
            position++;
        }
        return new BigInteger(input.substring(start, position));
    }

    private void skipSpaces() {
        while (!atEnd() && Character.isWhitespace(peek())) {
            position++;
        }
    }

    private boolean match(char expected) {
        if (!atEnd() && input.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private char peek() {
        return input.charAt(position);
    }

    private boolean atEnd() {
        return position >= input.length();
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + "，位置 " + position + "，输入：" + input);
    }
}

