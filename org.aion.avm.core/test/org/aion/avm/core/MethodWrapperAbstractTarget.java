package org.aion.avm.core;

import java.math.BigInteger;

public abstract class MethodWrapperAbstractTarget {

    public abstract void abstractVoidReturn();

    public abstract void abstractVoidReturnWithParams(byte b, char c);

    public abstract long abstractPrimitiveReturn() throws NullPointerException;

    public abstract int abstractPrimitiveReturnWithParams(String s, int[] i);

    public abstract byte[] abstractArrayReturn();

    public abstract String[][] abstractArrayReturnWithParams(char c, byte[] b, Object o, String s, int i);

    public abstract Object abstractObjectReturn();

    public abstract Number abstractObjectReturnWithParams(BigInteger b, Integer i, Double d);

    public long methodWithImplementation() {
        return 0;
    }

    public static long methodWithImplementation(int i, Exception e) {
        return 7;
    }
}
