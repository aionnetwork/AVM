package org.aion.avm.embed.shadowing.testStringBufferBuilder;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.*;

import java.math.BigInteger;

public class StringBufferTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes (StringBufferResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void stringBufferInsertNull(){
        callStatic("stringBufferInsertNull");
    }

    @Test
    public void stringBufferAppendNull(){
        callStatic("stringBufferAppendNull");
    }

    @Test
    public void insertCharacterSequence(){
        callStatic("insertCharacterSequence");
    }

    @Test
    public void appendStringBuffer(){
        callStatic("appendStringBuffer");
    }

    @Test
    public void insertMaxValue(){
        callStatic("insertMaxValue");
    }

    @Test
    public void indexOfNull(){
        callStatic("indexOfNull");
    }

    @Test
    public void indexOf(){
        callStatic("indexOf");
    }

    @Test
    public void appendDelete(){
        callStatic("appendDelete");
    }

    @Test
    public void setLength(){
        callStatic("setLength");
    }

    @Test
    public void stringBufferAddObject(){
        callStatic("stringBufferAddObject");
    }

    @Test
    public void stringBufferConstructor(){
        callStatic("stringBufferConstructor");
    }

    @Test
    public void stringBufferInvalidConstructor(){
        callStatic("stringBufferInvalidConstructor");
    }

    @Test
    public void stringBufferInvalidAppend(){
        callStatic("stringBufferInvalidAppend");
    }

    @Test
    public void stringBufferAppend(){
        callStatic("stringBufferAppend");
    }

    @Test
    public void stringBufferInsert(){
        callStatic("stringBufferInsert");
    }

    private void callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, data, 2_000_000, 1);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
    }
}
