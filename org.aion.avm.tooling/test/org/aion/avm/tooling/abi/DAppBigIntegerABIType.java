package org.aion.avm.tooling.abi;

import java.math.BigInteger;

public class DAppBigIntegerABIType {
    @Callable
    public static BigInteger returnBigInteger(){ return BigInteger.ZERO; }
}
