package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class SelfDestructTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static long ENERGY_LIMIT = 10_000_000L;
    private static long ENERGY_PRICE = 1L;
    private static org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Test
    public void callMissingDApp() {
        failToCall(TestingHelper.buildAddress(deployer.toBytes()));
    }

    @Test
    public void deleteSelfAndReturnValue() {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturn", TestingHelper.buildAddress(deployer.toBytes()));
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_RETURN, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void deleteSelfThenCallAnotherAndReturnValue() {
        Address bystander = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteCallAndReturn", TestingHelper.buildAddress(deployer.toBytes()), bystander);
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void deleteSelfDuringDeploy() {
        // Provide the deployer address as an argument, it will pay to them.
        Address target = deployCommonResource(deployer.toBytes());
        
        // The response should be real, but also impossible to call.
        Assert.assertTrue(null != target);
        failToCall(target);
    }

    @Test
    public void deleteSelfThenDeployNewCopyAndReturnItsAddress() {
        Address target = deployCommonResource(new byte[0]);
        
        // Call the callSelfForNull entry-point and it should return null to us.
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteDeployAndReturnAddress", TestingHelper.buildAddress(deployer.toBytes()), makeDeploymentData(new byte[0]));
        Object resultObject = callDApp(target, argData);
        Address newTarget = (Address)resultObject;
        
        // Verify that we can call this new DApp instance, but not the original target.
        argData = ABIEncoder.encodeMethodArguments("justReturn");
        resultObject = callDApp(newTarget, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void deleteSelfAndReturnBalance() {
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        
        long start = avmRule.kernel.getBalance(AvmAddress.wrap(target.unwrap())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturnBalance", TestingHelper.buildAddress(deployer.toBytes()));
        long result = ((Long)callDApp(target, argData)).longValue();
        Assert.assertEquals(0L, result);
        failToCall(target);
    }

    @Test
    public void deleteAndReturnBalanceFromAnother() {
        Address bystander = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        
        long start = avmRule.kernel.getBalance(AvmAddress.wrap(target.unwrap())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturnBalanceFromAnother", TestingHelper.buildAddress(deployer.toBytes()), bystander);
        long result = ((Long)callDApp(target, argData)).longValue();
        Assert.assertEquals(0L, result);
        failToCall(target);
    }

    @Test
    public void deleteAndFailToCallSelf() {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndFailToCallSelf", TestingHelper.buildAddress(deployer.toBytes()));
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_FAIL_TO_CALL_SELF, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void callToDeleteSuccess() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("callToDeleteSuccess", TestingHelper.buildAddress(deployer.toBytes()), target);
        Object resultObject = callDApp(accomplice, argData);
        Assert.assertEquals(SelfDestructResource.CALL_TO_DELETE_SUCCESS, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void callToDeleteFailure() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("callToDeleteFailure", TestingHelper.buildAddress(deployer.toBytes()), target);
        Object resultObject = callDApp(accomplice, argData);
        Assert.assertEquals(SelfDestructResource.CALL_TO_DELETE_FAIL, ((Integer)resultObject).intValue());
        
        // Verify that this is still alive.
        argData = ABIEncoder.encodeMethodArguments("justReturn");
        resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, ((Integer)resultObject).intValue());
    }

    @Test
    public void deleteAndReturnBeneficiaryBalance() {
        Address beneficiary = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        long start = avmRule.kernel.getBalance(AvmAddress.wrap(target.unwrap())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturnBeneficiaryBalance", beneficiary);
        Object resultObject = callDApp(target, argData);
        long fromInside = ((Long)resultObject).longValue();
        Assert.assertEquals(128L, fromInside);
        failToCall(target);
        
        // Check that we can see the balance having moved.
        long end = avmRule.kernel.getBalance(AvmAddress.wrap(beneficiary.unwrap())).longValueExact();
        Assert.assertEquals(128L, end);
    }


    private Address deployCommonResource(byte[] deployArgs) {
        byte[] txData = makeDeploymentData(deployArgs);

        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        return TestingHelper.buildAddress(result1.getReturnData());
    }

    private Object callDApp(Address dAppAddress, byte[] argData) {
        TransactionResult result = avmRule.call(deployer, AvmAddress.wrap(dAppAddress.unwrap()), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return TestingHelper.decodeResult(result);
    }

    private void failToCall(Address dAppAddress) {
        TransactionResult result = avmRule.call(deployer, AvmAddress.wrap(dAppAddress.unwrap()), BigInteger.ZERO, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        // Sending a call to nobody is a success, since the data doesn't need to go anywhere.
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        // That said, our tests will always return something on a real call so check that this is nothing.
        Assert.assertEquals(null, result.getReturnData());
    }

    private byte[] makeDeploymentData(byte[] deployArgs) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(SelfDestructResource.class);
        return new CodeAndArguments(jar, deployArgs).encodeToBytes();
    }

    private void sendMoney(Address target, BigInteger value) {
        TransactionResult result = avmRule.call(deployer, AvmAddress.wrap(target.unwrap()), value, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
