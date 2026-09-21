package com.zyy.arithmetic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 输入输出文件服务。
 */
public final class FileService {
    private FileService() {
    }

    public static void writeExercises(Path file, List<Question> questions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Question question : questions) {
                writer.write(question.exerciseText());
                writer.newLine();
            }
        }
    }

    public static void writeAnswers(Path file, List<Question> questions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Question question : questions) {
                writer.write(question.answerText());
                writer.newLine();
            }
        }
    }

    public static void writeGrade(Path file, GradeResult result) throws IOException {
        Files.writeString(file, result.toDisplay() + System.lineSeparator(), StandardCharsets.UTF_8);
    }
}
