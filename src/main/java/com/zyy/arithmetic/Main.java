package com.zyy.arithmetic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 命令行程序入口。
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, Path.of("").toAbsolutePath());
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args, Path workingDirectory) {
        try {
            CliOptions options = CliOptions.parse(args);
            return switch (options.mode()) {
                case HELP -> {
                    System.out.println(CliOptions.helpText());
                    yield 0;
                }
                case GENERATE -> generate(options, workingDirectory);
                case GRADE -> grade(options, workingDirectory);
            };
        } catch (IllegalArgumentException exception) {
            System.err.println("参数错误: " + exception.getMessage());
            System.err.println();
            System.err.println(CliOptions.helpText());
            return 2;
        } catch (IOException exception) {
            System.err.println("文件处理失败: " + exception.getMessage());
            return 3;
        } catch (RuntimeException exception) {
            System.err.println("运行失败: " + exception.getMessage());
            return 4;
        }
    }

    private static int generate(CliOptions options, Path workingDirectory) throws IOException {
        ArithmeticGenerator generator = new ArithmeticGenerator(options.range());
        List<Question> questions = generator.generate(options.count());
        Path exerciseFile = workingDirectory.resolve("Exercises.txt");
        Path answerFile = workingDirectory.resolve("Answers.txt");
        FileService.writeExercises(exerciseFile, questions);
        FileService.writeAnswers(answerFile, questions);
        System.out.println("已生成 " + questions.size() + " 道题目。");
        System.out.println("题目: " + exerciseFile.toAbsolutePath());
        System.out.println("答案: " + answerFile.toAbsolutePath());
        return 0;
    }

    private static int grade(CliOptions options, Path workingDirectory) throws IOException {
        Path exerciseFile = resolve(workingDirectory, options.exerciseFile());
        Path answerFile = resolve(workingDirectory, options.answerFile());
        GradeResult result = new Grader().grade(exerciseFile, answerFile);
        Path gradeFile = workingDirectory.resolve("Grade.txt");
        FileService.writeGrade(gradeFile, result);
        System.out.println(result.toDisplay());
        System.out.println("统计结果: " + gradeFile.toAbsolutePath());
        return result.wrong().isEmpty() ? 0 : 1;
    }

    private static Path resolve(Path workingDirectory, Path path) {
        return path.isAbsolute() ? path : workingDirectory.resolve(path);
    }
}
