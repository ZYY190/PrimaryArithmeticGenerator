package com.zyy.arithmetic;

import java.util.Objects;

/**
 * 表达式抽象。NumberNode 和 BinaryNode 共同组成表达式树。
 */
public sealed interface Expression permits Expression.NumberNode, Expression.BinaryNode {
    Fraction value();

    int operatorCount();

    String toDisplay();

    String canonicalKey();

    default String toQuestion() {
        return toDisplay() + " =";
    }

    record NumberNode(Fraction value) implements Expression {
        public NumberNode {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public int operatorCount() {
            return 0;
        }

        @Override
        public String toDisplay() {
            return value.toDisplay();
        }

        @Override
        public String canonicalKey() {
            return "N:" + value.canonical();
        }
    }

    final class BinaryNode implements Expression {
        private final Operator operator;
        private final Expression left;
        private final Expression right;
        private final Fraction value;

        public BinaryNode(Operator operator, Expression left, Expression right) {
            this.operator = Objects.requireNonNull(operator, "operator");
            this.left = Objects.requireNonNull(left, "left");
            this.right = Objects.requireNonNull(right, "right");
            this.value = operator.apply(left.value(), right.value());
        }

        public Operator operator() {
            return operator;
        }

        public Expression left() {
            return left;
        }

        public Expression right() {
            return right;
        }

        @Override
        public Fraction value() {
            return value;
        }

        @Override
        public int operatorCount() {
            return 1 + left.operatorCount() + right.operatorCount();
        }

        @Override
        public String toDisplay() {
            String leftText = formatChild(left, false);
            String rightText = formatChild(right, true);
            return leftText + " " + operator.symbol() + " " + rightText;
        }

        private String formatChild(Expression child, boolean rightSide) {
            if (!(child instanceof BinaryNode binary)) {
                return child.toDisplay();
            }
            boolean parentheses = binary.operator.precedence() < operator.precedence()
                    || (rightSide && binary.operator.precedence() == operator.precedence());
            String text = binary.toDisplay();
            return parentheses ? "(" + text + ")" : text;
        }

        @Override
        public String canonicalKey() {
            String leftKey = left.canonicalKey();
            String rightKey = right.canonicalKey();
            if (operator.isCommutative() && leftKey.compareTo(rightKey) > 0) {
                String temporary = leftKey;
                leftKey = rightKey;
                rightKey = temporary;
            }
            return "(" + operator.name() + ":" + leftKey + "," + rightKey + ")";
        }

        @Override
        public String toString() {
            return toDisplay();
        }
    }
}
