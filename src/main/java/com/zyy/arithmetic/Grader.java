package com.zyy.arithmetic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 对题目文件和答案文件逐题判分。
 */
public final class Grader {
    private static final Pattern LINE_PREFIX = Pattern.compile("^\\s*\\d+\\.\\s*");

    public GradeResult grade(Path exerciseFile, Path answerFile) throws IOException {
        List<String> exercises = Files.readAllLines(exerciseFile, StandardCharsets.UTF_8);
        List<String> answers = Files.readAllLines(answerFile, StandardCharsets.UTF_8);
        if (exercises.size() != answers.size()) {
            throw new IOException("题目数与答案数不一致：Exercises=" + exercises.size()
                    + ", Answers=" + answers.size());
        }

        List<Integer> correct = new ArrayList<>();
        List<Integer> wrong = new ArrayList<>();
        for (int index = 0; index < exercises.size(); index++) {
            int number = index + 1;
            String exercise = stripPrefix(exercises.get(index)).trim();
            String answer = stripPrefix(answers.get(index)).trim();
            if (!exercise.endsWith("=")) {
                throw new IOException("第 " + number + " 题缺少等号: " + exercise);
            }
            String expressionText = exercise.substring(0, exercise.length() - 1).trim();
            Expression expression = ExpressionParser.parse(expressionText);
            Fraction expected = expression.value();
            Fraction actual = ExpressionParser.parse(answer).value();
            if (expected.equals(actual)) {
                correct.add(number);
            } else {
                wrong.add(number);
            }
        }
        return new GradeResult(correct, wrong);
    }

    private String stripPrefix(String line) {
        return LINE_PREFIX.matcher(line).replaceFirst("");
    }
}
