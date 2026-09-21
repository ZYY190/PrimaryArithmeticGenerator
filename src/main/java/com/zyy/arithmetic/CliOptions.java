package com.zyy.arithmetic;

import java.nio.file.Path;

/**
 * 命令行参数解析结果。
 */
public record CliOptions(Mode mode, int count, int range, Path exerciseFile, Path answerFile) {
    public enum Mode {
        GENERATE,
        GRADE,
        HELP
    }

    public static CliOptions parse(String[] args) {
        if (args.length == 0) {
            return new CliOptions(Mode.HELP, 0, 0, null, null);
        }
        Integer count = null;
        Integer range = null;
        Path exerciseFile = null;
        Path answerFile = null;

        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            switch (argument) {
                case "-h", "--help" -> {
                    return new CliOptions(Mode.HELP, 0, 0, null, null);
                }
                case "-n" -> count = readPositiveInt(args, ++i, "-n");
                case "-r" -> range = readPositiveInt(args, ++i, "-r");
                case "-e" -> exerciseFile = Path.of(readValue(args, ++i, "-e"));
                case "-a" -> answerFile = Path.of(readValue(args, ++i, "-a"));
                default -> throw new IllegalArgumentException("未知参数: " + argument);
            }
        }

        boolean grading = exerciseFile != null || answerFile != null;
        if (grading) {
            if (exerciseFile == null || answerFile == null) {
                throw new IllegalArgumentException("判分模式必须同时提供 -e 和 -a 参数");
            }
            if (count != null || range != null) {
                throw new IllegalArgumentException("判分模式不能同时使用 -n 或 -r");
            }
            return new CliOptions(Mode.GRADE, 0, 0, exerciseFile, answerFile);
        }

        if (count == null || range == null) {
            throw new IllegalArgumentException("生成模式必须同时提供 -n 和 -r 参数");
        }
        return new CliOptions(Mode.GENERATE, count, range, null, null);
    }

    private static int readPositiveInt(String[] args, int index, String option) {
        String value = readValue(args, index, option);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new IllegalArgumentException(option + " 必须是不小于 1 的整数");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(option + " 需要整数，实际为: " + value);
        }
    }

    private static String readValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " 缺少参数值");
        }
        return args[index];
    }

    public static String helpText() {
        return """
               小学四则运算自动出题程序
                
               用法:
                 Myapp.cmd -n <题目数量> -r <范围>
                 Myapp.cmd -e <exercisefile>.txt -a <answerfile>.txt
                 Myapp.cmd -h
                
               参数:
                 -n   生成题目的数量，必须为正整数
                 -r   题目中数值的范围，生成的数值小于该值，必须为正整数
                 -e   题目文件路径
                 -a   答案文件路径
                 -h   显示本帮助信息
                
               示例:
                 Myapp.cmd -n 10 -r 10
                 Myapp.cmd -e Exercises.txt -a Answers.txt
               """;
    }
}
