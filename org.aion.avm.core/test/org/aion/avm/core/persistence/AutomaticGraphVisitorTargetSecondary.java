package org.aion.avm.core.persistence;


/**
 * Used within AutomaticGraphVisitorTest
 * This primary exists to test that transformed calls from AutomaticGraphVisitorTargetPrimary
 * hit the correct marks.
 */
public class AutomaticGraphVisitorTargetSecondary {
    public int value;
    
    public AutomaticGraphVisitorTargetSecondary(int initial) {
        // This non-empty constructor is to verify what happens when we create an empty one, later.
        this.value = initial;
    }
    
    public void setValue(int newValue) {
        this.value = newValue;
    }
}
