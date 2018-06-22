package org.aion.avm.core.testindy.java.lang;

/**
 * @author Roman Katerinenko
 */
public class Double extends org.aion.avm.shadow.java.lang.Object {
    private double val;

    public boolean avm_valueOfWasCalled;

    public static Double avm_valueOf(double origVal) {
        final var newVal = new Double();
        newVal.val = origVal;
        newVal.avm_valueOfWasCalled = true;
        return newVal;
    }

    public double avm_doubleValue() {
        return val;
    }
}