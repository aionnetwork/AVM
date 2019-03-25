package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests various cases around the instanceof opcode.
 */
public class InstanceOfIntegrationTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;
    private static Address deployer = avmRule.getPreminedAccount();
    private static Address dappAddress;



    @BeforeClass
    public static void setupClass() throws Exception {
        TransactionResult createResult = avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytes(InstanceOfIntegrationTestTarget.class, new byte[0]), ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        dappAddress = new Address(createResult.getReturnData());
    }

    @Test
    public void checkSelf() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSelf(), callStaticBoolean("checkSelf"));
    }

    @Test
    public void checkNull() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkNull(), callStaticBoolean("checkNull"));
    }

    @Test
    public void checkSubTrue() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubTrue(), callStaticBoolean("checkSubTrue"));
    }

    @Test
    public void checkSubFalse() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubFalse(), callStaticBoolean("checkSubFalse"));
    }

    @Test
    public void checkSubObject() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubObject(), callStaticBoolean("checkSubObject"));
    }

    @Test
    public void checkSubA() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubA(), callStaticBoolean("checkSubA"));
    }

    @Test
    public void checkSubB() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubB(), callStaticBoolean("checkSubB"));
    }

    @Test
    public void checkSubC() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkSubC(), callStaticBoolean("checkSubC"));
    }

    @Test
    public void checkBOfA() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkBOfA(), callStaticBoolean("checkBOfA"));
    }

    @Test
    public void checkAddHocAObject() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkAddHocAObject(), callStaticBoolean("checkAddHocAObject"));
    }

    @Test
    public void subArrayIsObject() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.subArrayIsObject(), callStaticBoolean("subArrayIsObject"));
    }

    @Test
    public void subArrayIsObjectArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.subArrayIsObjectArray(), callStaticBoolean("subArrayIsObjectArray"));
    }

    @Test
    public void targetIsTargetArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.targetIsTargetArray(), callStaticBoolean("targetIsTargetArray"));
    }

    @Test
    public void subArrayIsTargetArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.subArrayIsTargetArray(), callStaticBoolean("subArrayIsTargetArray"));
    }

    @Test
    public void subArrayIsCArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.subArrayIsCArray(), callStaticBoolean("subArrayIsCArray"));
    }

    @Test
    public void subArrayIsAArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.subArrayIsAArray(), callStaticBoolean("subArrayIsAArray"));
    }

    @Test
    public void bArrayIsAArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.bArrayIsAArray(), callStaticBoolean("bArrayIsAArray"));
    }

    @Test
    public void bArrayIsBArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.bArrayIsBArray(), callStaticBoolean("bArrayIsBArray"));
    }

    @Test
    public void intCArrayIsBArray() throws Exception {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.intCArrayIsBArray(), callStaticBoolean("intCArrayIsBArray"));
    }

    @Test
    public void call_getSub() {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.call_getSub(), callStaticBoolean("call_getSub"));
    }

    @Test
    public void call_getSub2() {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.call_getSub2(), callStaticBoolean("call_getSub2"));
    }

    @Test
    public void checkArrayParams() {
        Assert.assertEquals(InstanceOfIntegrationTestTarget.checkArrayParams(), callStaticBoolean("checkArrayParams"));
    }

    private boolean callStaticBoolean(String methodName) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        TransactionResult result = avmRule.call(deployer, dappAddress, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Boolean) ABIDecoder.decodeOneObject(result.getReturnData())).booleanValue();
    }
}
