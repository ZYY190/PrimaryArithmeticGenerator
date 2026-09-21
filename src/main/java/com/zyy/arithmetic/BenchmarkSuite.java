package com.zyy.arithmetic;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 在同一个 JVM 中执行多规模基准测试，输出中位数结果。
 */
public final class BenchmarkSuite {
    private BenchmarkSuite() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length >= 1 ? Path.of(args[0]) : Path.of("performance-runs.csv");
        int range = args.length >= 2 ? Integer.parseInt(args[1]) : 100;
        int[] counts = {1_000, 5_000, 10_000, 50_000, 100_000};

        new ArithmeticGenerator(range, 20260922L).generate(10_000);

        StringBuilder csv = new StringBuilder("count,range,median_ms,min_ms,ms_per_question,memory_delta_mb\n");
        for (int count : counts) {
            long[] timings = new long[3];
            long memoryDelta = 0;
            for (int repeat = 0; repeat < timings.length; repeat++) {
                Runtime runtime = Runtime.getRuntime();
                long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
                long start = System.nanoTime();
                ArithmeticGenerator generator = new ArithmeticGenerator(range, 20260922L + count + repeat);
                List<Question> questions = generator.generate(count);
                if (questions.size() != count) {
                    throw new IllegalStateException("生成数量不正确");
                }
                timings[repeat] = (System.nanoTime() - start) / 1_000_000;
                memoryDelta = runtime.totalMemory() - runtime.freeMemory() - beforeMemory;
            }
            long[] sorted = timings.clone();
            Arrays.sort(sorted);
            long median = sorted[1];
            long min = sorted[0];
            csv.append(count).append(',')
                    .append(range).append(',')
                    .append(median).append(',')
                    .append(min).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.6f", median / (double) count)).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.3f", memoryDelta / 1024.0 / 1024.0)).append('\n');
        }
        Files.writeString(output, csv.toString(), StandardCharsets.UTF_8);
        System.out.println(csv);
        System.out.println("CSV: " + output.toAbsolutePath());
    }
}
