package org.aion.avm.kernel;

import java.math.BigInteger;
import java.util.Arrays;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionalKernel;
import org.junit.Assert;
import org.junit.Test;


public class TransactionalKernelTest {
    @Test
    public void testCommitDataOntoEmpty() {
        KernelInterface base = new KernelInterfaceImpl();
        TransactionalKernel transaction = new TransactionalKernel(base);
        byte[] address = Helpers.randomBytes(32);
        transaction.putCode(address, new byte[0]);
        Assert.assertEquals(0, transaction.getCode(address).length);
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);
        transaction.putStorage(address, key, value);
        Assert.assertTrue(Arrays.equals(value, transaction.getStorage(address, key)));
        
        byte[] account1 = Helpers.randomBytes(32);
        transaction.adjustBalance(account1, BigInteger.valueOf(50L));
        Assert.assertEquals(BigInteger.valueOf(50L), transaction.getBalance(account1));
        
        // Prove nothing is committed.
        Assert.assertNull(base.getCode(address));
        Assert.assertNull(base.getStorage(address, key));
        Assert.assertEquals(BigInteger.ZERO, base.getBalance(account1));
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(0, base.getCode(address).length);
        Assert.assertTrue(Arrays.equals(value, base.getStorage(address, key)));
        Assert.assertEquals(BigInteger.valueOf(50L), base.getBalance(account1));
    }

    @Test
    public void testCommitDataOntoPartial() {
        KernelInterface base = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        byte[] key1 = Helpers.randomBytes(32);
        byte[] value1_1 = Helpers.randomBytes(32);
        base.putStorage(address, key1, value1_1);
        Assert.assertTrue(Arrays.equals(value1_1, base.getStorage(address, key1)));
        
        TransactionalKernel transaction = new TransactionalKernel(base);
        byte[] key2 = Helpers.randomBytes(32);
        byte[] value2_1 = Helpers.randomBytes(32);
        byte[] value1_2 = Helpers.randomBytes(32);
        transaction.putStorage(address, key1, value1_2);
        transaction.putStorage(address, key2, value2_1);
        Assert.assertTrue(Arrays.equals(value1_1, base.getStorage(address, key1)));
        Assert.assertNull(base.getStorage(address, key2));
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertTrue(Arrays.equals(value1_2, base.getStorage(address, key1)));
        Assert.assertTrue(Arrays.equals(value2_1, base.getStorage(address, key2)));
    }

    @Test
    public void testCommitAdjustment() {
        KernelInterface base = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalKernel transaction = new TransactionalKernel(base);
        Assert.assertEquals(BigInteger.ONE, transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.TEN);
        Assert.assertEquals(BigInteger.valueOf(11L), transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.valueOf(5).negate());
        byte[] address2 = Helpers.randomBytes(32);
        transaction.adjustBalance(address2, BigInteger.ONE);
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(BigInteger.valueOf(6L), base.getBalance(address));
        Assert.assertEquals(BigInteger.ONE, base.getBalance(address2));
    }

    @Test
    public void testCommitDelete() {
        KernelInterface base = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalKernel transaction = new TransactionalKernel(base);
        Assert.assertEquals(BigInteger.ONE, transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.TEN);
        Assert.assertEquals(BigInteger.valueOf(11L), transaction.getBalance(address));
        transaction.deleteAccount(address);
        Assert.assertEquals(BigInteger.ONE, base.getBalance(address));
        Assert.assertEquals(BigInteger.ZERO, transaction.getBalance(address));
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(BigInteger.ZERO, base.getBalance(address));
    }

    @Test
    public void testCommitDeleteRecreate() {
        // This probably can't happen, in reality, but this test at least shows it is possible.
        KernelInterface base = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalKernel transaction = new TransactionalKernel(base);
        Assert.assertEquals(BigInteger.ONE, transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.TEN);
        Assert.assertEquals(BigInteger.valueOf(11L), transaction.getBalance(address));
        transaction.deleteAccount(address);
        Assert.assertEquals(BigInteger.ONE, base.getBalance(address));
        Assert.assertEquals(BigInteger.ZERO, transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.TWO);
        Assert.assertEquals(BigInteger.TWO, transaction.getBalance(address));
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(BigInteger.TWO, base.getBalance(address));
    }
}
