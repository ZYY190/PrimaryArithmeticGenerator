package com.zyy.arithmetic;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 用于生成性能分析数据的小型基准程序。
 */
public final class PerformanceRunner {
    private PerformanceRunner() {
    }

    public static void main(String[] args) throws Exception {
        int count = args.length >= 1 ? Integer.parseInt(args[0]) : 10_000;
        int range = args.length >= 2 ? Integer.parseInt(args[1]) : 100;
        Path output = args.length >= 3 ? Path.of(args[2]) : Path.of("performance.csv");

        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        long start = System.nanoTime();
        ArithmeticGenerator generator = new ArithmeticGenerator(range, 20260922L + count);
        List<Question> questions = generator.generate(count);
        long elapsedNanos = System.nanoTime() - start;
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long elapsedMillis = elapsedNanos / 1_000_000;
        double millisecondsPerQuestion = elapsedMillis / (double) count;

        String header = "count,range,total_ms,ms_per_question,memory_delta_mb\n";
        String row = count + "," + range + "," + elapsedMillis + ","
                + String.format(java.util.Locale.ROOT, "%.6f", millisecondsPerQuestion) + ","
                + String.format(java.util.Locale.ROOT, "%.3f", (afterMemory - beforeMemory) / 1024.0 / 1024.0) + "\n";
        Files.writeString(output, header + row, StandardCharsets.UTF_8);
        System.out.println("Generated " + questions.size() + " questions in " + elapsedMillis + " ms");
        System.out.println("CSV: " + output.toAbsolutePath());
    }
}
