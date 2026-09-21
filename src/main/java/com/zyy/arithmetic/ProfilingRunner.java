package com.zyy.arithmetic;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 基于线程栈采样的轻量级热点分析器。
 *
 * <p>JFR 在部分受控环境中不可用，本类使用 1 毫秒采样间隔读取主线程栈顶方法，
 * 输出实际工作负载中的热点方法排序。</p>
 */
public final class ProfilingRunner {
    private ProfilingRunner() {
    }

    public static void main(String[] args) throws Exception {
        int count = args.length >= 1 ? Integer.parseInt(args[0]) : 200_000;
        int range = args.length >= 2 ? Integer.parseInt(args[2 - 1]) : 100;
        Path output = args.length >= 3 ? Path.of(args[2]) : Path.of("profile-methods.csv");
        if (count < 1 || range < 1) {
            throw new IllegalArgumentException("count 和 range 必须为正数");
        }

        Thread target = Thread.currentThread();
        Map<String, LongAdder> counters = new ConcurrentHashMap<>();
        SamplingThread sampler = new SamplingThread(target, counters);
        sampler.start();

        long start = System.nanoTime();
        List<Question> questions = new ArithmeticGenerator(range, 20260922L).generate(count);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        sampler.stopAndJoin();
        long totalSamples = counters.values().stream().mapToLong(LongAdder::sum).sum();
        List<Map.Entry<String, LongAdder>> entries = new ArrayList<>(counters.entrySet());
        entries.sort(Map.Entry.<String, LongAdder>comparingByValue(
                Comparator.comparingLong(LongAdder::sum)).reversed());

        StringBuilder csv = new StringBuilder("rank,method,samples,percent\n");
        int rank = 1;
        for (Map.Entry<String, LongAdder> entry : entries) {
            long samples = entry.getValue().sum();
            double percent = totalSamples == 0 ? 0.0 : samples * 100.0 / totalSamples;
            csv.append(rank++).append(',')
                    .append(entry.getKey()).append(',')
                    .append(samples).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.2f", percent)).append('\n');
        }
        Files.writeString(output, csv.toString(), StandardCharsets.UTF_8);
        System.out.println("Profiled " + questions.size() + " questions in " + elapsedMillis + " ms");
        System.out.println("Samples: " + totalSamples);
        System.out.println("CSV: " + output.toAbsolutePath());
    }

    private static final class SamplingThread extends Thread {
        private final Thread target;
        private final Map<String, LongAdder> counters;
        private volatile boolean running = true;

        private SamplingThread(Thread target, Map<String, LongAdder> counters) {
            super("arithmetic-sampler");
            this.target = target;
            this.counters = counters;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                StackTraceElement[] stack = target.getStackTrace();
                if (stack.length > 0) {
                    StackTraceElement frame = stack[0];
                    String method = frame.getClassName() + "." + frame.getMethodName();
                    counters.computeIfAbsent(method, ignored -> new LongAdder()).increment();
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private void stopAndJoin() throws InterruptedException {
            running = false;
            join();
        }
    }
}
