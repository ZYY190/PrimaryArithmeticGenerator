package com.zyy.arithmetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 负责生成满足约束且无重复的表达式。
 */
public final class ArithmeticGenerator {
    private static final int MIN_OPERATORS = 1;
    private static final int MAX_OPERATORS = 3;
    private static final int MAX_ATTEMPTS_PER_QUESTION = 20_000;

    private final int range;
    private final Random random;
    private final Set<String> seenCanonicalKeys = new HashSet<>();

    public ArithmeticGenerator(int range) {
        this(range, System.nanoTime());
    }

    public ArithmeticGenerator(int range, long seed) {
        if (range < 1) {
            throw new IllegalArgumentException("范围必须是不小于 1 的自然数");
        }
        this.range = range;
        this.random = new Random(seed);
    }

    public int range() {
        return range;
    }

    public List<Question> generate(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("题目数量必须大于 0");
        }
        List<Question> questions = new ArrayList<>(count);
        for (int index = 1; index <= count; index++) {
            Expression expression = generateUniqueExpression();
            questions.add(new Question(index, expression, expression.value()));
        }
        return questions;
    }

    private Expression generateUniqueExpression() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_QUESTION; attempt++) {
            int operatorCount = MIN_OPERATORS + random.nextInt(MAX_OPERATORS - MIN_OPERATORS + 1);
            Expression expression = generateTree(operatorCount);
            if (expression == null) {
                continue;
            }
            String key = expression.canonicalKey();
            if (seenCanonicalKeys.add(key)) {
                return expression;
            }
        }
        throw new IllegalStateException("无法生成足够多的不重复题目，请增大 -r 或减少 -n");
    }

    private Expression generateTree(int operatorCount) {
        if (operatorCount == 0) {
            return new Expression.NumberNode(generateAtom());
        }
        int leftOperatorCount = random.nextInt(operatorCount);
        int rightOperatorCount = operatorCount - 1 - leftOperatorCount;
        Operator operator = randomOperator();
        Expression left = generateTree(leftOperatorCount);
        Expression right = generateTree(rightOperatorCount);
        if (left == null || right == null) {
            return null;
        }
        return constrain(operator, left, right);
    }

    private Expression constrain(Operator operator, Expression left, Expression right) {
        Fraction leftValue = left.value();
        Fraction rightValue = right.value();
        return switch (operator) {
            case ADD, MULTIPLY -> new Expression.BinaryNode(operator, left, right);
            case SUBTRACT -> {
                if (leftValue.compareTo(rightValue) >= 0) {
                    yield new Expression.BinaryNode(operator, left, right);
                }
                yield new Expression.BinaryNode(operator, right, left);
            }
            case DIVIDE -> {
                if (rightValue.isZero() || leftValue.isZero() || leftValue.equals(rightValue)) {
                    yield null;
                }
                if (leftValue.compareTo(rightValue) > 0) {
                    Expression temporary = left;
                    left = right;
                    right = temporary;
                }
                yield new Expression.BinaryNode(operator, left, right);
            }
        };
    }

    private Operator randomOperator() {
        return Operator.values()[random.nextInt(Operator.values().length)];
    }

    private Fraction generateAtom() {
        if (range == 1) {
            return Fraction.ZERO;
        }
        if (range == 2 || random.nextBoolean()) {
            return Fraction.integer(random.nextInt(range));
        }

        int denominator = 2 + random.nextInt(range - 2);
        int numerator = 1 + random.nextInt(denominator - 1);
        int whole = random.nextInt(range - 1);
        if (whole == 0) {
            return Fraction.of(numerator, denominator);
        }
        return Fraction.of((long) whole * denominator + numerator, denominator);
    }
}

