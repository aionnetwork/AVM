package org.aion.avm.kernel;

import java.math.BigInteger;
import java.util.Arrays;

import org.aion.avm.core.IExternalState;
import org.aion.kernel.TransactionalState;
import org.aion.types.AionAddress;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TestingState;
import org.junit.Assert;
import org.junit.Test;


public class TransactionalStateTest {
    @Test
    public void testCommitDataOntoEmpty() {
        IExternalState base = new TestingState();
        TransactionalState transaction = new TransactionalState(base);
        AionAddress address = Helpers.randomAddress();
        // Code cannot be empty.
        transaction.setTransformedCode(address, new byte[1]);
        Assert.assertEquals(1, transaction.getTransformedCode(address).length);
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);
        transaction.putStorage(address, key, value);
        Assert.assertTrue(Arrays.equals(value, transaction.getStorage(address, key)));

        AionAddress account1 = Helpers.randomAddress();
        transaction.createAccount(account1);
        transaction.adjustBalance(account1, BigInteger.valueOf(50L));
        Assert.assertEquals(BigInteger.valueOf(50L), transaction.getBalance(account1));
        
        // Prove nothing is committed.
        Assert.assertNull(base.getTransformedCode(address));
        Assert.assertNull(base.getStorage(address, key));
        Assert.assertEquals(BigInteger.ZERO, base.getBalance(account1));
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(1, base.getTransformedCode(address).length);
        Assert.assertTrue(Arrays.equals(value, base.getStorage(address, key)));
        Assert.assertEquals(BigInteger.valueOf(50L), base.getBalance(account1));
    }

    @Test
    public void testCommitDataOntoPartial() {
        IExternalState base = new TestingState();
        AionAddress address = Helpers.randomAddress();
        byte[] key1 = Helpers.randomBytes(32);
        byte[] value1_1 = Helpers.randomBytes(32);
        base.putStorage(address, key1, value1_1);
        Assert.assertTrue(Arrays.equals(value1_1, base.getStorage(address, key1)));
        
        TransactionalState transaction = new TransactionalState(base);
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
        IExternalState base = new TestingState();
        AionAddress address = Helpers.randomAddress();
        base.createAccount(address);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalState transaction = new TransactionalState(base);
        Assert.assertEquals(BigInteger.ONE, transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.TEN);
        Assert.assertEquals(BigInteger.valueOf(11L), transaction.getBalance(address));
        transaction.adjustBalance(address, BigInteger.valueOf(5).negate());
        AionAddress address2 = Helpers.randomAddress();
        transaction.adjustBalance(address2, BigInteger.ONE);
        
        // Now, commit and prove it is all written back.
        transaction.commit();
        Assert.assertEquals(BigInteger.valueOf(6L), base.getBalance(address));
        Assert.assertEquals(BigInteger.ONE, base.getBalance(address2));
    }

    @Test
    public void testCommitDelete() {
        IExternalState base = new TestingState();
        AionAddress address = Helpers.randomAddress();
        base.createAccount(address);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalState transaction = new TransactionalState(base);
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
        IExternalState base = new TestingState();
        AionAddress address = Helpers.randomAddress();
        base.createAccount(address);
        base.adjustBalance(address, BigInteger.ONE);
        
        TransactionalState transaction = new TransactionalState(base);
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

    @Test
    public void testIncrementNonceAndCommit() {
        // Create an account with a 1 nonce and leave one uninitialized (will be lazily created as zero).
        IExternalState base = new TestingState();
        AionAddress nonceOne = Helpers.randomAddress();
        AionAddress nonceZero = Helpers.randomAddress();
        base.createAccount(nonceOne);
        base.incrementNonce(nonceOne);
        
        // Increment both of them.
        TransactionalState transaction = new TransactionalState(base);
        transaction.incrementNonce(nonceOne);
        transaction.incrementNonce(nonceZero);
        
        // Make sure that these are correct in the transaction cache.
        Assert.assertEquals(BigInteger.TWO, transaction.getNonce(nonceOne));
        Assert.assertEquals(BigInteger.ONE, transaction.getNonce(nonceZero));
        
        // Now, commit and observe the nonces are correctly incremented to 2 and 1, respectively.
        transaction.commit();
        Assert.assertEquals(BigInteger.TWO, base.getNonce(nonceOne));
        Assert.assertEquals(BigInteger.ONE, base.getNonce(nonceZero));
    }
}
