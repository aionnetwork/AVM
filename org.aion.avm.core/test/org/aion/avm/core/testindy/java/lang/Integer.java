package org.aion.avm.core.testindy.java.lang;

/**
 * @author Roman Katerinenko
 */
public class Integer extends s.java.lang.Object {
    private int val;
    private boolean avm_valueOfWasCalled;

    public static Integer avm_valueOf(int intVal) {
        Integer result = new Integer();
        result.val = intVal;
        result.avm_valueOfWasCalled = true;
        return result;
    }

    public int avm_intValue(){
        return val;
    }
}