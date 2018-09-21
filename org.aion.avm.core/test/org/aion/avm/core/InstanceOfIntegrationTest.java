package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests various cases around the instanceof opcode.
 * NOTE:  The ignored cases will be resolved before issue-82 is resolved.
 */
public class InstanceOfIntegrationTest {
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private static Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private static byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private static Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);
    private static Address dappAddress;

    @BeforeClass
    public static void setup() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(InstanceOfIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        Transaction create = new Transaction(Transaction.Type.CREATE, deployer, null, kernel.getNonce(deployer), 0, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = avm.run(new TransactionContextImpl(create, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        dappAddress = TestingHelper.buildAddress(createResult.getReturnData());
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
        Transaction call = new Transaction(Transaction.Type.CALL, deployer, dappAddress.unwrap(), kernel.getNonce(deployer), 0, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return ((Boolean)TestingHelper.decodeResult(result)).booleanValue();
    }
}
