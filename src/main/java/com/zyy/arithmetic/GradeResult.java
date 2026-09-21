package com.zyy.arithmetic;

import java.util.List;

/**
 * 判分统计结果。
 */
public record GradeResult(List<Integer> correct, List<Integer> wrong) {
    public GradeResult {
        correct = List.copyOf(correct);
        wrong = List.copyOf(wrong);
    }

    public String toDisplay() {
        return "Correct: " + correct.size() + " " + formatNumbers(correct) + System.lineSeparator()
                + "Wrong: " + wrong.size() + " " + formatNumbers(wrong);
    }

    private static String formatNumbers(List<Integer> numbers) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(numbers.get(i));
        }
        return builder.append(")").toString();
    }
}
