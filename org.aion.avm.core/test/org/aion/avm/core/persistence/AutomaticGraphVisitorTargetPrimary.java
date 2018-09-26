package org.aion.avm.core.persistence;


import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

/**
 * Used within AutomaticGraphVisitorTest
 * Note that this is the main entry-point we call from the tests to verify interactions with
 * AutomaticGraphVisitorTargetSecondary work properly.
 */
public class AutomaticGraphVisitorTargetPrimary {
    public int value;
    
    public AutomaticGraphVisitorTargetPrimary() {
        // We have an empty constructor to demonstrate that we don't break it.
        this.value = 42;
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new AutomaticGraphVisitorTargetPrimary(), BlockchainRuntime.getData());
    }

    public int getValue() {
        return value;
    }

    public int setValue(int value) {
        this.value = value;
        return this.value;
    }
    
    /*public static AutomaticGraphVisitorTargetSecondary createSecondary(int initial, int changed) {
        AutomaticGraphVisitorTargetSecondary target = new AutomaticGraphVisitorTargetSecondary(initial);
        target.value = changed;
        return target;
    }*/
    
    public static void changeAgain(AutomaticGraphVisitorTargetSecondary target, int changed) {
        target.value = changed;
    }
}
