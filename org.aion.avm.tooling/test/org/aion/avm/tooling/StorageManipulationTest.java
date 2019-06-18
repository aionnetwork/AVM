package org.aion.avm.tooling;

import static org.junit.Assert.assertTrue;

import avm.Address;
import java.math.BigInteger;
import org.aion.types.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

public class StorageManipulationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testManipulatePutStorageKey() {
        byte[] jar = avmRule.getDappBytes(StorageManipulationTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());

        avmRule.kernel.generateBlock();
        Address contract = new Address(result.copyOfTransactionOutput().orElseThrow());;
        byte[] data = ABIUtil.encodeMethodArguments("manipulatePutStorageKey");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testManipulatePutStorageValue() {
        byte[] jar = avmRule.getDappBytes(StorageManipulationTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());

        avmRule.kernel.generateBlock();
        Address contract = new Address(result.copyOfTransactionOutput().orElseThrow());;
        byte[] data = ABIUtil.encodeMethodArguments("manipulatePutStorageValue");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testManipulateGetStorageValue() {
        byte[] jar = avmRule.getDappBytes(StorageManipulationTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());

        avmRule.kernel.generateBlock();
        Address contract = new Address(result.copyOfTransactionOutput().orElseThrow());;
        byte[] data = ABIUtil.encodeMethodArguments("manipulateGetStorageValue");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testManipulateStorageInReentrantCall() {
        byte[] jar = avmRule.getDappBytes(StorageManipulationTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());

        avmRule.kernel.generateBlock();
        Address contract = new Address(result.copyOfTransactionOutput().orElseThrow());;
        byte[] data = ABIUtil.encodeMethodArguments("manipulateStorageInReentrantCall");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }
}
