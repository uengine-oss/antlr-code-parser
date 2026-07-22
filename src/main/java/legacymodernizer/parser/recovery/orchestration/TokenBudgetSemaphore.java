package legacymodernizer.parser.recovery.orchestration;

import java.time.Duration;

/**
 * Weighted concurrency guard for Agent requests (spec 012 FR-053): capacity is prompt
 * characters in flight, not thread count, so one huge request and many small ones are
 * throttled by the same budget. Waiters are served with plain monitor fairness; a request
 * larger than the whole budget is clamped so it can still run (alone).
 */
public final class TokenBudgetSemaphore {

    private final long budgetChars;
    private long inFlightChars;

    public TokenBudgetSemaphore(long budgetChars) {
        if (budgetChars <= 0) throw new IllegalArgumentException("TOKEN_BUDGET_NOT_POSITIVE");
        this.budgetChars = budgetChars;
    }

    /** Blocks until the weight fits the remaining budget or the timeout expires. */
    public boolean tryAcquire(long weightChars, Duration timeout) throws InterruptedException {
        long weight = clamp(weightChars);
        long deadline = System.nanoTime() + timeout.toNanos();
        synchronized (this) {
            while (inFlightChars + weight > budgetChars) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) return false;
                wait(Math.max(1, remaining / 1_000_000));
            }
            inFlightChars += weight;
            return true;
        }
    }

    public void release(long weightChars) {
        long weight = clamp(weightChars);
        synchronized (this) {
            inFlightChars = Math.max(0, inFlightChars - weight);
            notifyAll();
        }
    }

    public synchronized long inFlightChars() {
        return inFlightChars;
    }

    private long clamp(long weightChars) {
        return Math.min(budgetChars, Math.max(1, weightChars));
    }
}
