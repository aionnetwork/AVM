package org.aion.avm.core;


/**
 * Counters and timer data written by the owning AvmThread.
 * Mutable access to this structure is granted to all users since it is meant to be fast to access and has no internal
 * consistency requirements (although external consumers should take care to only make consistency assumptions when
 * the thread is not running).
 * Note the average times might degrade after a while, if the total number of operations significantly increases.
 */
public class AvmThreadStats {
    public int transactionsProcessed;
    public long nanosRunning;
    public long nanosSleeping;

    public long transformationAvgTimeNanos;
    public long transformationMaxTimeNanos;
    public int transformationCount;

    public long retransformationAvgTimeNanos;
    public long retransformationMaxTimeNanos;
    public int retransformationCount;

    // The number of concurrent resource access requests which resulted in acquire/wait/abort.  Note that a wait request will result in either an acquire or abort.
    public int concurrentResource_acquired;
    public int concurrentResource_waited;
    public int concurrentResource_aborted;

    /**
     * updates the transformation count, max and average transformation times
     *
     * @param transformationTime new transformation time
     */
    public void addTransformationTimeToStats(long transformationTime) {
        transformationMaxTimeNanos = Long.max(transformationTime, transformationMaxTimeNanos);
        transformationCount++;
        transformationAvgTimeNanos += ((transformationTime - transformationAvgTimeNanos) / transformationCount);
    }

    /**
     * updates re-transfromation count, max and average re-transformation time
     *
     * @param retransformationTime new re-transformation time
     */
    public void addRetransformationTimeToStats(long retransformationTime) {
        retransformationMaxTimeNanos = Long.max(retransformationTime, retransformationMaxTimeNanos);
        retransformationCount++;
        retransformationAvgTimeNanos += ((retransformationTime - retransformationAvgTimeNanos) / retransformationCount);
    }

    public void clear() {
        this.transactionsProcessed = 0;
        this.nanosRunning = 0;
        this.nanosSleeping = 0;
        this.transformationMaxTimeNanos = 0;
        this.transformationAvgTimeNanos = 0;
        this.transformationCount = 0;
        this.retransformationAvgTimeNanos = 0;
        this.retransformationCount = 0;
        this.retransformationMaxTimeNanos = 0;
        this.concurrentResource_acquired = 0;
        this.concurrentResource_waited = 0;
        this.concurrentResource_aborted = 0;
    }
}
