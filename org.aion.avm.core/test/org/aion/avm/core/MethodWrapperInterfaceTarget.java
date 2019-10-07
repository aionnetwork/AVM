package org.aion.avm.core;

import java.io.Serializable;
import java.math.BigInteger;

public interface MethodWrapperInterfaceTarget {

    public void interfaceReturnVoid();

    public void interfaceReturnVoidWithParams(Object o);

    public double interfaceReturnPrimitive();

    public float interfaceReturnPrimitiveWithParams(int i, char c, String s);

    public boolean[] interfaceReturnArray();

    public Number[][] interfaceReturnArrayWithParams(BigInteger b, short s);

    public StringBuilder interfaceReturnObject();

    public Serializable interfaceReturnObjectWithParams(Exception e, char[] c, int[] i) throws IllegalArgumentException;
}
