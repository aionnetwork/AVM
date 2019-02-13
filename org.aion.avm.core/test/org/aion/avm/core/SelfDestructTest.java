package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
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
    private Address deployer = avmRule.getPreminedAccount();

    @Test
    public void callMissingDApp() {
        failToCall(deployer);
    }

    @Test
    public void deleteSelfAndReturnValue() {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturn", deployer);
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_RETURN, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void deleteSelfThenCallAnotherAndReturnValue() {
        Address bystander = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteCallAndReturn", deployer, bystander);
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void deleteSelfDuringDeploy() {
        // Provide the deployer address as an argument, it will pay to them.
        Address target = deployCommonResource(deployer.unwrap());
        
        // The response should be real, but also impossible to call.
        Assert.assertTrue(null != target);
        failToCall(target);
    }

    @Test
    public void deleteSelfThenDeployNewCopyAndReturnItsAddress() {
        Address target = deployCommonResource(new byte[0]);
        
        // Call the callSelfForNull entry-point and it should return null to us.
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteDeployAndReturnAddress", deployer, makeDeploymentData(new byte[0]));
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
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturnBalance", deployer);
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
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndReturnBalanceFromAnother", deployer, bystander);
        long result = ((Long)callDApp(target, argData)).longValue();
        Assert.assertEquals(0L, result);
        failToCall(target);
    }

    @Test
    public void deleteAndFailToCallSelf() {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("deleteAndFailToCallSelf", deployer);
        Object resultObject = callDApp(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_FAIL_TO_CALL_SELF, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void callToDeleteSuccess() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("callToDeleteSuccess", deployer, target);
        Object resultObject = callDApp(accomplice, argData);
        Assert.assertEquals(SelfDestructResource.CALL_TO_DELETE_SUCCESS, ((Integer)resultObject).intValue());
        failToCall(target);
    }

    @Test
    public void callToDeleteFailure() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIEncoder.encodeMethodArguments("callToDeleteFailure", deployer, target);
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
        return new Address(result1.getReturnData());
    }

    private Object callDApp(Address dAppAddress, byte[] argData) {
        TransactionResult result = avmRule.call(deployer, dAppAddress, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ABIDecoder.decodeOneObject(result.getReturnData());
    }

    private void failToCall(Address dAppAddress) {
        TransactionResult result = avmRule.call(deployer, dAppAddress, BigInteger.ZERO, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
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
        TransactionResult result = avmRule.call(deployer, target, value, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
