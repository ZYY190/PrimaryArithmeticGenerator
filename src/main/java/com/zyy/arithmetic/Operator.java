package com.zyy.arithmetic;

import java.util.function.BinaryOperator;

/**
 * 四则运算符及其优先级。
 */
public enum Operator {
    ADD("+", 1, true, Fraction::add),
    SUBTRACT("−", 1, false, Fraction::subtract),
    MULTIPLY("×", 2, true, Fraction::multiply),
    DIVIDE("÷", 2, false, Fraction::divide);

    private final String symbol;
    private final int precedence;
    private final boolean commutative;
    private final BinaryOperator<Fraction> operation;

    Operator(String symbol, int precedence, boolean commutative, BinaryOperator<Fraction> operation) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.commutative = commutative;
        this.operation = operation;
    }

    public String symbol() {
        return symbol;
    }

    public int precedence() {
        return precedence;
    }

    public boolean isCommutative() {
        return commutative;
    }

    public Fraction apply(Fraction left, Fraction right) {
        return operation.apply(left, right);
    }

    public static Operator fromSymbol(String symbol) {
        return switch (symbol) {
            case "+" -> ADD;
            case "-", "−" -> SUBTRACT;
            case "*", "×", "x", "X" -> MULTIPLY;
            case "/", "÷" -> DIVIDE;
            default -> throw new IllegalArgumentException("未知运算符: " + symbol);
        };
    }
}
