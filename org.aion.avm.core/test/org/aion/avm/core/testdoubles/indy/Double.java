package org.aion.avm.core.testdoubles.indy;

/**
 * @author Roman Katerinenko
 */
public class Double extends org.aion.avm.java.lang.Object {
    private double val;

    public boolean avm_valueOfWasCalled;

    public static org.aion.avm.core.testdoubles.indy.Double avm_valueOf(double origVal) {
        final var newVal = new org.aion.avm.core.testdoubles.indy.Double();
        newVal.val = origVal;
        newVal.avm_valueOfWasCalled = true;
        return newVal;
    }

    public double avm_doubleValue() {
        return val;
    }
}