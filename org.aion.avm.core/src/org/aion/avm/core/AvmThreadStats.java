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

    // We track min/max/avg of serialized graph size, which requires that we store the running total and running count.
    // (note that the graph is limited to 500k but the running total could be large - long should be sufficient since this is just for stats).
    public int serializedGraph_count;
    public int serializedGraph_min = Integer.MAX_VALUE;
    public int serializedGraph_max;
    public long serializedGraph_sum;
    public long serializedGraph_avgNanos;

    // Record our AVM-internal cache usage (note that these are counted for all transactions, not specifically sync or other explicit uses).
    public int cache_code_hit;
    public int cache_code_miss;
    // (a special-case where we didn't even consult the code cache since the code was found in the reentrant stack).
    public int cache_code_reentrant;
    public int cache_data_hit;
    public int cache_data_miss;
    // (a special-case where we wanted to use the data cache but couldn't due to it being a reentrant call).
    public int cache_data_reentrant;

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

    /**
     * Updates internal counters related to serialized object graph sizes.
     * 
     * @param graphSize The size of the serialized object graph, in bytes.
     * @param serializationTimeNanos Nanoseconds spent in serialization.
     */
    public void addSerializedGraphSizeToStats(int graphSize, long serializationTimeNanos) {
        this.serializedGraph_count += 1;
        if (graphSize < this.serializedGraph_min) {
            this.serializedGraph_min = graphSize;
        }
        if (graphSize > this.serializedGraph_max) {
            this.serializedGraph_max = graphSize;
        }
        this.serializedGraph_sum += (long)graphSize;
        // We may want to change this to a different averaging mechanism to avoid washing out these numbers but this pattern should
        // be fine for stats (and nanos precision).
        this.serializedGraph_avgNanos += ((serializationTimeNanos - this.serializedGraph_avgNanos) / this.serializedGraph_count);
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
        this.serializedGraph_count = 0;
        this.serializedGraph_min = Integer.MAX_VALUE;
        this.serializedGraph_max = 0;
        this.serializedGraph_sum = 0L;
        this.serializedGraph_avgNanos = 0L;
        this.cache_code_hit = 0;
        this.cache_code_miss = 0;
        this.cache_code_reentrant = 0;
        this.cache_data_hit = 0;
        this.cache_data_miss = 0;
        this.cache_data_reentrant = 0;
    }
}
