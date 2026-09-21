package com.zyy.arithmetic;

import java.util.Objects;

/**
 * 一道题目及其标准答案。
 */
public record Question(int index, Expression expression, Fraction answer) {
    public Question {
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(answer, "answer");
    }

    public String exerciseText() {
        return index + ". " + expression.toQuestion();
    }

    public String answerText() {
        return index + ". " + answer.toDisplay();
    }
}
