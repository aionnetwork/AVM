package org.aion.avm.core.persistence;


/**
 * Used within AutomaticGraphVisitorTest
 * Note that this is the main entry-point we call from the tests to verify interactions with
 * AutomaticGraphVisitorTargetSecondary work properly.
 */
public class AutomaticGraphVisitorTargetPrimary {
    public final int value;
    
    public AutomaticGraphVisitorTargetPrimary() {
        // We have an empty constructor to demonstrate that we don't break it.
        this.value = 42;
    }
    
    public static AutomaticGraphVisitorTargetSecondary createSecondary(int initial, int changed) {
        AutomaticGraphVisitorTargetSecondary target = new AutomaticGraphVisitorTargetSecondary(initial);
        target.value = changed;
        return target;
    }
    
    public static void changeAgain(AutomaticGraphVisitorTargetSecondary target, int changed) {
        target.value = changed;
    }
}
