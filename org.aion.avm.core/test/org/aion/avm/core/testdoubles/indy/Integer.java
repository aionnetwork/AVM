package org.aion.avm.core.testdoubles.indy;

/**
 * @author Roman Katerinenko
 */
public class Integer extends org.aion.avm.core.testdoubles.indy.Object {
    private int val;
    private boolean avm_valueOfWasCalled;

    public static org.aion.avm.core.testdoubles.indy.Integer avm_valueOf(int intVal) {
        org.aion.avm.core.testdoubles.indy.Integer result = new org.aion.avm.core.testdoubles.indy.Integer();
        result.val = intVal;
        result.avm_valueOfWasCalled = true;
        return result;
    }

    public int avm_intValue(){
        return val;
    }
}