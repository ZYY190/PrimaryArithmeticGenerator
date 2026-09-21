package com.zyy.arithmetic;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 不依赖第三方库的自测试入口。
 */
public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        testFractionFormatting();
        testParser();
        testGeneratedConstraintsAndDuplicates();
        testTenThousandQuestions();
        testGrading();
        System.out.println("ALL TESTS PASSED");
    }

    private static void testFractionFormatting() {
        assertEquals("3/5", Fraction.of(3, 5).toDisplay(), "真分数格式");
        assertEquals("1’1/2", Fraction.of(3, 2).toDisplay(), "带分数格式");
        assertEquals("7/24", Fraction.of(1, 6).add(Fraction.of(1, 8)).toDisplay(), "分数加法");
        assertEquals("0", Fraction.of(2, 4).subtract(Fraction.of(1, 2)).toDisplay(), "零值格式");
    }

    private static void testParser() {
        Expression mixed = ExpressionParser.parse("2’3/8 + 1/8");
        assertEquals("2’1/2", mixed.value().toDisplay(), "带分数解析");
        Expression nested = ExpressionParser.parse("3 + (2 + 1)");
        assertEquals("6", nested.value().toDisplay(), "括号解析");
        assertEquals(ExpressionParser.parse("1 + 2 + 3").canonicalKey(),
                ExpressionParser.parse("3 + (2 + 1)").canonicalKey(), "交换律去重");
        assertNotEquals(ExpressionParser.parse("1 + 2 + 3").canonicalKey(),
                ExpressionParser.parse("3 + 2 + 1").canonicalKey(), "结合顺序区分");
    }

    private static void testGeneratedConstraintsAndDuplicates() {
        ArithmeticGenerator generator = new ArithmeticGenerator(20, 20260922L);
        List<Question> questions = generator.generate(500);
        Set<String> canonicalKeys = new HashSet<>();
        for (Question question : questions) {
            Expression expression = question.expression();
            assertTrue(expression.operatorCount() >= 1 && expression.operatorCount() <= 3,
                    "运算符数量应在 1 到 3 之间");
            validateExpression(expression);
            assertTrue(question.answer().isNonNegative(), "答案不能为负数");
            assertTrue(canonicalKeys.add(expression.canonicalKey()), "题目不应重复");
            Expression reparsed = ExpressionParser.parse(expression.toDisplay());
            assertEquals(expression.value(), reparsed.value(), "表达式格式应可重新解析");
        }
    }

    private static void testTenThousandQuestions() {
        long start = System.nanoTime();
        ArithmeticGenerator generator = new ArithmeticGenerator(100, 10000L);
        List<Question> questions = generator.generate(10_000);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertEquals(10_000, questions.size(), "应生成一万道题");
        assertTrue(elapsedMs < 120_000, "一万道题生成时间应低于 120 秒");
    }

    private static void testGrading() throws IOException {
        Path directory = Files.createTempDirectory("arithmetic-grade-");
        Path exercises = directory.resolve("Exercises.txt");
        Path answers = directory.resolve("Answers.txt");
        Files.writeString(exercises,
                "1. 1/2 + 1/3 =\n2. 2 × 3 =\n3. 5 − 1 =\n", StandardCharsets.UTF_8);
        Files.writeString(answers,
                "1. 5/6\n2. 5\n3. 4\n", StandardCharsets.UTF_8);
        GradeResult result = new Grader().grade(exercises, answers);
        assertEquals(List.of(1, 3), result.correct(), "正确题号");
        assertEquals(List.of(2), result.wrong(), "错误题号");
    }

    private static void validateExpression(Expression expression) {
        if (expression instanceof Expression.BinaryNode binary) {
            validateExpression(binary.left());
            validateExpression(binary.right());
            Fraction left = binary.left().value();
            Fraction right = binary.right().value();
            if (binary.operator() == Operator.SUBTRACT) {
                assertTrue(left.compareTo(right) >= 0, "减法中间结果不能为负数");
            }
            if (binary.operator() == Operator.DIVIDE) {
                assertTrue(right.isNonNegative() && !right.isZero(), "除数不能为 0");
                assertTrue(binary.value().isProper(), "除法结果应是真分数");
            }
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "，期望=" + expected + "，实际=" + actual);
        }
    }

    private static void assertNotEquals(Object first, Object second, String message) {
        if (first.equals(second)) {
            throw new AssertionError(message + "，值不应相同：" + first);
        }
    }
}
