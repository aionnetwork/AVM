package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIDecoder;

import avm.Address;
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
        deleteSelfAndReturnValue(false);
        deleteSelfAndReturnValue(true);
        deleteSelfAndReturnValue(true);
    }

    private void deleteSelfAndReturnValue(boolean generateBlock) {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteAndReturn", deployer);
        int result = callDAppInteger(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_RETURN, result);

        if(generateBlock) avmRule.kernel.generateBlock();

        failToCall(target);
    }

    @Test
    public void deleteSelfThenCallAnotherAndReturnValue() {
        deleteSelfThenCallAnotherAndReturnValue(false);
        deleteSelfThenCallAnotherAndReturnValue(true);
        deleteSelfThenCallAnotherAndReturnValue(true);
    }

    private void deleteSelfThenCallAnotherAndReturnValue(boolean generateBlock) {
        Address bystander = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteCallAndReturn", deployer, bystander);
        int result = callDAppInteger(target, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, result);

        if(generateBlock) avmRule.kernel.generateBlock();

        failToCall(target);
    }

    @Test
    public void deleteSelfDuringDeploy() {
        // Provide the deployer address as an argument, it will pay to them.
        Address target = deployCommonResource(deployer.toByteArray());
        
        // The response should be real, but also impossible to call.
        Assert.assertTrue(null != target);
        failToCall(target);
    }

    @Test
    public void deleteSelfThenDeployNewCopyAndReturnItsAddress() {
        Address target = deployCommonResource(new byte[0]);
        
        // Call the callSelfForNull entry-point and it should return null to us.
        byte[] argData = ABIUtil.encodeMethodArguments("deleteDeployAndReturnAddress", deployer, makeDeploymentData(new byte[0], SelfDestructSmallResource.class));
        Address newTarget = callDAppAddress(target, argData);
        Assert.assertNotNull(newTarget);
        
        // Verify that we cannot call the original target.
        argData = ABIUtil.encodeMethodArguments("justReturn");
        failToCall(target);
    }

    @Test
    public void deleteSelfAndReturnBalance() {
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        
        long start = avmRule.kernel.getBalance(org.aion.vm.api.types.Address.wrap(target.toByteArray())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteAndReturnBalance", deployer);
        long result = callDAppLong(target, argData);
        Assert.assertEquals(0L, result);
        failToCall(target);
    }

    @Test
    public void deleteAndReturnBalanceFromAnother() {
        Address bystander = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        
        long start = avmRule.kernel.getBalance(org.aion.vm.api.types.Address.wrap(target.toByteArray())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteAndReturnBalanceFromAnother", deployer, bystander);
        long result = callDAppLong(target, argData);
        Assert.assertEquals(0L, result);
        failToCall(target);
    }

    @Test
    public void deleteAndFailToCallSelf() {
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteAndFailToCallSelf", deployer);
        int result = callDAppInteger(target, argData);
        Assert.assertEquals(SelfDestructResource.DELETE_AND_FAIL_TO_CALL_SELF, result);
        failToCall(target);
    }

    @Test
    public void callToDeleteSuccess() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIUtil.encodeMethodArguments("callToDeleteSuccess", deployer, target);
        int result = callDAppInteger(accomplice, argData);
        Assert.assertEquals(SelfDestructResource.CALL_TO_DELETE_SUCCESS, result);
        failToCall(target);
    }

    @Test
    public void callToDeleteFailure() {
        Address accomplice = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        byte[] argData = ABIUtil.encodeMethodArguments("callToDeleteFailure", deployer, target);
        int result = callDAppInteger(accomplice, argData);
        Assert.assertEquals(SelfDestructResource.CALL_TO_DELETE_FAIL, result);
        
        // Verify that this is still alive.
        argData = ABIUtil.encodeMethodArguments("justReturn");
        result = callDAppInteger(target, argData);
        Assert.assertEquals(SelfDestructResource.JUST_RETURN, result);
    }

    @Test
    public void deleteAndReturnBeneficiaryBalance() {
        Address beneficiary = deployCommonResource(new byte[0]);
        Address target = deployCommonResource(new byte[0]);
        
        // Give it some money, so we can check this later.
        sendMoney(target, new BigInteger("128"));
        long start = avmRule.kernel.getBalance(org.aion.vm.api.types.Address.wrap(target.toByteArray())).longValueExact();
        Assert.assertEquals(128L, start);
        
        byte[] argData = ABIUtil.encodeMethodArguments("deleteAndReturnBeneficiaryBalance", beneficiary);
        long fromInside = callDAppLong(target, argData);
        Assert.assertEquals(128L, fromInside);
        failToCall(target);
        
        // Check that we can see the balance having moved.
        long end = avmRule.kernel.getBalance(org.aion.vm.api.types.Address.wrap(beneficiary.toByteArray())).longValueExact();
        Assert.assertEquals(128L, end);
    }


    private Address deployCommonResource(byte[] deployArgs) {
        byte[] txData = makeDeploymentData(deployArgs, SelfDestructResource.class);

        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        return new Address(result1.getReturnData());
    }

    private int callDAppInteger(Address dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(dAppAddress, argData);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private long callDAppLong(Address dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(dAppAddress, argData);
        return new ABIDecoder(result).decodeOneLong();
    }

    private Address callDAppAddress(Address dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(dAppAddress, argData);
        return new ABIDecoder(result).decodeOneAddress();
    }

    private byte[] callDAppSuccess(Address dAppAddress, byte[] argData) {
        TransactionResult result = avmRule.call(deployer, dAppAddress, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return result.getReturnData();
    }

    private void failToCall(Address dAppAddress) {
        TransactionResult result = avmRule.call(deployer, dAppAddress, BigInteger.ZERO, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        // Sending a call to nobody is a success, since the data doesn't need to go anywhere.
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        // That said, our tests will always return something on a real call so check that this is nothing.
        Assert.assertEquals(null, result.getReturnData());
    }

    private byte[] makeDeploymentData(byte[] deployArgs, Class<?> classToDeploy) {
        return avmRule.getDappBytes(classToDeploy, deployArgs);
    }

    private void sendMoney(Address target, BigInteger value) {
        TransactionResult result = avmRule.call(deployer, target, value, new byte[0], ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
