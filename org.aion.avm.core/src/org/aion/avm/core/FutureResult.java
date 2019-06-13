package org.aion.avm.core;

import org.aion.kernel.AvmTransactionResult;

/**
 * A simple alternative to {@link java.util.concurrent.Future} that provides only a blocking
 * {@code get()} method, which blocks until a transaction result is ready to be consumed.
 */
public final class FutureResult {
    private final HandoffMonitor handoffMonitor;
    private final int index;
    private AvmTransactionResult cachedResult;

    public FutureResult(HandoffMonitor handoffMonitor, int index) {
        this.handoffMonitor = handoffMonitor;
        this.index = index;
    }

    /**
     * Returns a transaction result, blocking if no result is ready to be consumed yet.
     *
     * @return a transaction result.
     */
    public AvmTransactionResult get() {
        if (null == this.cachedResult) {
            this.cachedResult = this.handoffMonitor.blockingConsumeResult(this.index);
        }
        return this.cachedResult;
    }
}
