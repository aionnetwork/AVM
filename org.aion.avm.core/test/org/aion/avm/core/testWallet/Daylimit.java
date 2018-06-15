package org.aion.avm.core.testWallet;

import org.aion.avm.rt.IFutureRuntime;


/**
 * In the original, daylimit "inherited" from multiowned but the Solidity concept of "inheritance" probably makes more sense as strict composition
 * so we will just depend on someone creating this object with a pre-configured Multiowned instance.
 */
public class Daylimit {
    private static final long kSecondsPerDay = 60 * 60 * 24;

    private final Multiowned owners;
    private long dailyLimit;
    private long lastDay;
    private long spentToday = 0;

    public Daylimit(Multiowned owners, long value, long nowInDays) {
        this.owners = owners;
        this.dailyLimit = value;
        this.lastDay = nowInDays;
    }

    // PUBLIC INTERFACE
    public void setDailyLimit(IFutureRuntime runtime, long value) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.avm_getSender(), Operation.from(runtime));
        
        this.dailyLimit = value;
    }

    // PUBLIC INTERFACE
    public void resetSpentToday(IFutureRuntime runtime) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.avm_getSender(), Operation.from(runtime));
        
        this.spentToday = 0;
    }


    // checks to see if there is at least `_value` left from the daily limit today. if there is, subtracts it and
    // returns true. otherwise just returns false.
    // public for composition.
    public boolean underLimit(IFutureRuntime runtime, long value) {
        // (modifier)
        this.owners.onlyOwner(runtime.avm_getSender());
        
        // reset the spend limit if we're on a different day to last time.
        long nowInDays = runtime.getBlockEpochSeconds() / kSecondsPerDay;
        if (nowInDays > this.lastDay) {
            this.spentToday = 0;
            this.lastDay = nowInDays;
        }
        
        // check to see if there's enough left - if so, subtract and return true.
        // overflow protection                    // dailyLimit check
        boolean didChange = false;
        if (((this.spentToday + value) >= this.spentToday)
                && ((this.spentToday + value) < this.dailyLimit)
           ) {
            this.spentToday += value;
            didChange = true;
        }
        return didChange;
    }
}
