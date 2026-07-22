package legacymodernizer.parser.recovery.repair;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.orchestration.TokenBudgetSemaphore;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * Spec 012 SC-008: measures real Agent request latency/throughput against the live GPU
 * endpoint at rising concurrency, gated by the token-budget semaphore. Enabled only when
 * {@code parser.live.agent.api.base} and {@code parser.live.agent.model} are supplied.
 */
class GpuConcurrencyBenchmarkTest {

    private static final int[] CONCURRENCY = {1, 2, 4, 8, 16, 32};
    private static final int REQUESTS_PER_LEVEL =
            Integer.getInteger("parser.bench.requests.per.level", 16);

    @Test
    void measuresLatencyAndThroughputPerConcurrencyLevel() throws Exception {
        String apiBase = System.getProperty("parser.live.agent.api.base");
        String model = System.getProperty("parser.live.agent.model");
        String apiKey = System.getProperty("parser.live.agent.api.key", "");
        Assumptions.assumeTrue(apiBase != null && !apiBase.isBlank()
                && model != null && !model.isBlank());

        StructuredRepairAgent agent = new StructuredRepairAgent(true,
                URI.create(apiBase.replaceAll("/$", "") + "/chat/completions"), apiKey, model,
                Duration.ofSeconds(120), 512, null, false, 1, HttpClient.newHttpClient(),
                new ObjectMapper(),
                Files.readString(Path.of("src/main/resources/recovery/repair-agent-system-prompt.txt")));
        TokenBudgetSemaphore budget = new TokenBudgetSemaphore(
                Long.getLong("parser.repair.agent.budget.chars", 200_000L));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("apiBase", apiBase);
        report.put("model", model);
        report.put("requestsPerLevel", REQUESTS_PER_LEVEL);
        List<Map<String, Object>> levels = new ArrayList<>();
        for (int concurrency : CONCURRENCY) {
            levels.add(runLevel(agent, budget, concurrency));
        }
        report.put("levels", levels);
        Path output = Path.of("target", "corpus-reports", "gpu-concurrency-benchmark.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(report), StandardCharsets.UTF_8);
        System.out.println("BENCH written " + output);
    }

    private Map<String, Object> runLevel(StructuredRepairAgent agent,
                                         TokenBudgetSemaphore budget, int concurrency)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        List<String> failures = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(REQUESTS_PER_LEVEL);
        long started = System.nanoTime();
        for (int request = 0; request < REQUESTS_PER_LEVEL; request++) {
            final int index = request;
            pool.submit(() -> {
                FailureEnvelope envelope = envelope(index);
                long weight = envelope.sourceExcerpt().length();
                long callStart = System.nanoTime();
                try {
                    if (!budget.tryAcquire(weight, Duration.ofMinutes(5))) {
                        failures.add("BUDGET_TIMEOUT");
                        return;
                    }
                    try {
                        agent.propose(envelope);
                    } finally {
                        budget.release(weight);
                    }
                    latencies.add((System.nanoTime() - callStart) / 1_000_000);
                } catch (RepairAgentException error) {
                    // Rejected proposals still measure a full round trip.
                    latencies.add((System.nanoTime() - callStart) / 1_000_000);
                    failures.add(error.getMessage());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        done.await(15, TimeUnit.MINUTES);
        pool.shutdownNow();
        long wallMillis = (System.nanoTime() - started) / 1_000_000;
        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        Map<String, Object> level = new LinkedHashMap<>();
        level.put("concurrency", concurrency);
        level.put("completed", latencies.size());
        level.put("failures", failures.size());
        level.put("wallMillis", wallMillis);
        level.put("throughputPerSecond", latencies.isEmpty() ? 0
                : Math.round(latencies.size() * 1000.0 / wallMillis * 100) / 100.0);
        level.put("latencyP50Millis", percentile(sorted, 50));
        level.put("latencyP95Millis", percentile(sorted, 95));
        System.out.println("BENCH concurrency=" + concurrency + " " + level);
        return level;
    }

    private static long percentile(List<Long> sorted, int percent) {
        if (sorted.isEmpty()) return -1;
        return sorted.get(Math.min(sorted.size() - 1, sorted.size() * percent / 100));
    }

    /** Realistic ~1.5K-char Oracle slice; index varies the content to defeat response caching. */
    private static FailureEnvelope envelope(int index) {
        StringBuilder body = new StringBuilder("  SELECT T").append(index)
                .append(".ID INTO v_id FROM APP_TABLE_").append(index).append(" AS T")
                .append(index).append(";\n");
        for (int line = 0; line < 40; line++) {
            body.append("  v_value_").append(index).append('_').append(line)
                    .append(" := v_value_").append(index).append('_').append(line)
                    .append(" + ").append(line).append(";\n");
        }
        String excerpt = body.toString();
        int alias = excerpt.indexOf("AS T");
        return new FailureEnvelope("2.0.0", "a".repeat(64), "oracle", "grammar-1",
                Hashes.sha256(("file" + index).getBytes(StandardCharsets.UTF_8)),
                Hashes.sha256(excerpt.getBytes(StandardCharsets.UTF_8)),
                "bench-unit-" + index, "PROCEDURE", 0, excerpt.length(), 1, 42,
                "L1", 0, excerpt.length(), 0, excerpt.length(),
                Hashes.sha256(excerpt.getBytes(StandardCharsets.UTF_8)), "",
                List.of(new DiagnosticEvidence("PARSER", "ANTLR_PARSER_SYNTAX",
                        "no viable alternative at input 'AS T" + index + "'", 1, alias,
                        alias, alias + 2, "AS", "{ID}", List.of("table_ref"),
                        excerpt.substring(Math.max(0, alias - 40),
                                Math.min(excerpt.length(), alias + 40)))),
                new CoverageEvidence(1, 0, List.of("bench"), false), List.of(),
                "unit=bench-unit-" + index + "; kind=PROCEDURE; name=bench", excerpt,
                List.of(0), List.of(new SourceTokenEvidence(alias, alias + 2, "AS")),
                new RepairConstraints(0, excerpt.length(), 64, 4, 3,
                        List.of("AST", "NODE_JSON", "FULL_UNIT_REWRITE")));
    }
}
