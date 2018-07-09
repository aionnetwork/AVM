package org.aion.avm.core.testWallet;

import org.aion.avm.api.BlockchainRuntime;


/**
 * In the original, daylimit "inherited" from multiowned but the Solidity concept of "inheritance" probably makes more sense as strict composition
 * so we will just depend on someone creating this object with a pre-configured Multiowned instance.
 */
public class Daylimit {
    private static final long kSecondsPerDay = 60 * 60 * 24;

    private static long dailyLimit;
    private static long lastDay;
    private static long spentToday = 0;

    // "Constructor"
    public static void init(long value, long nowInDays) {
        Daylimit.dailyLimit = value;
        Daylimit.lastDay = nowInDays;
    }

    // PUBLIC INTERFACE
    public static void setDailyLimit(long value) {
        // (modifier)
        Multiowned.onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        Daylimit.dailyLimit = value;
    }

    // PUBLIC INTERFACE
    public static void resetSpentToday() {
        // (modifier)
        Multiowned.onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        Daylimit.spentToday = 0;
    }


    // checks to see if there is at least `_value` left from the daily limit today. if there is, subtracts it and
    // returns true. otherwise just returns false.
    // public for composition.
    public static boolean underLimit(long value) {
        // (modifier)
        Multiowned.onlyOwner(BlockchainRuntime.getSender());
        
        // reset the spend limit if we're on a different day to last time.
        long nowInDays = BlockchainRuntime.getBlockEpochSeconds() / kSecondsPerDay;
        if (nowInDays > Daylimit.lastDay) {
            Daylimit.spentToday = 0;
            Daylimit.lastDay = nowInDays;
        }
        
        // check to see if there's enough left - if so, subtract and return true.
        // overflow protection                    // dailyLimit check
        boolean didChange = false;
        if (((Daylimit.spentToday + value) >= Daylimit.spentToday)
                && ((Daylimit.spentToday + value) < Daylimit.dailyLimit)
           ) {
            Daylimit.spentToday += value;
            didChange = true;
        }
        return didChange;
    }
}
