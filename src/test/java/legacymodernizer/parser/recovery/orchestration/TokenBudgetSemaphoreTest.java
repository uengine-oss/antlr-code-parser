package legacymodernizer.parser.recovery.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class TokenBudgetSemaphoreTest {

    @Test
    void capacityIsCharactersInFlightNotThreadCount() throws Exception {
        TokenBudgetSemaphore semaphore = new TokenBudgetSemaphore(1_000);
        assertTrue(semaphore.tryAcquire(600, Duration.ofMillis(50)));
        assertTrue(semaphore.tryAcquire(300, Duration.ofMillis(50)));
        assertFalse(semaphore.tryAcquire(200, Duration.ofMillis(50)),
                "900 in flight + 200 exceeds the 1000-char budget");
        semaphore.release(600);
        assertTrue(semaphore.tryAcquire(200, Duration.ofMillis(50)));
        assertEquals(500, semaphore.inFlightChars());
    }

    @Test
    void oversizedRequestIsClampedSoItCanStillRunAlone() throws Exception {
        TokenBudgetSemaphore semaphore = new TokenBudgetSemaphore(100);
        assertTrue(semaphore.tryAcquire(10_000, Duration.ofMillis(50)));
        assertFalse(semaphore.tryAcquire(1, Duration.ofMillis(50)),
                "a clamped oversized request still occupies the whole budget");
        semaphore.release(10_000);
        assertTrue(semaphore.tryAcquire(1, Duration.ofMillis(50)));
    }

    @Test
    void blockedWaitersProceedAfterReleaseAndBudgetIsNeverExceeded() throws Exception {
        TokenBudgetSemaphore semaphore = new TokenBudgetSemaphore(500);
        AtomicLong peak = new AtomicLong();
        AtomicInteger completed = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(8);
        for (int worker = 0; worker < 8; worker++) {
            new Thread(() -> {
                try {
                    assertTrue(semaphore.tryAcquire(200, Duration.ofSeconds(5)));
                    peak.accumulateAndGet(semaphore.inFlightChars(), Math::max);
                    Thread.sleep(20);
                    semaphore.release(200);
                    completed.incrementAndGet();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(8, completed.get());
        assertTrue(peak.get() <= 500, "budget exceeded: " + peak.get());
        assertEquals(0, semaphore.inFlightChars());
    }

    @Test
    void rejectsNonPositiveBudget() {
        assertThrows(IllegalArgumentException.class, () -> new TokenBudgetSemaphore(0));
    }
}
