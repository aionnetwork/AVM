package org.aion.data;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;


public class DirectoryBackedDataStoreTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSmallAccount() throws Exception {
        File storage = folder.newFolder();
        DirectoryBackedDataStore store = new DirectoryBackedDataStore(storage);
        
        byte[] address1 = Helpers.randomBytes(32);
        byte[] code1 = Helpers.randomBytes(100);
        BigInteger balance1 = BigInteger.ONE;
        long nonce1 = 2L;
        byte[] key1 = Helpers.randomBytes(32);
        byte[] value1 = Helpers.randomBytes(100);
        IAccountStore account1 = store.createAccount(address1);
        account1.setCode(code1);
        account1.setBalance(BigInteger.ONE);
        account1.setNonce(2L);
        account1.setData(key1, value1);
        
        account1 = store.openAccount(address1);
        Assert.assertTrue(Arrays.equals(code1, account1.getCode()));
        Assert.assertEquals(balance1, account1.getBalance());
        Assert.assertEquals(nonce1, account1.getNonce());
        Assert.assertTrue(Arrays.equals(value1, account1.getData(key1)));
        
        // Overwrite a key to see that we can observe the update.
        byte[] value1_2 = Helpers.randomBytes(100);
        account1.setData(key1, value1_2);
        Assert.assertTrue(Arrays.equals(value1_2, account1.getData(key1)));
        
        // Add another key and see that we can read all of these from the account testing method.
        byte[] key2 = Helpers.randomBytes(32);
        byte[] value2 = Helpers.randomBytes(50);
        account1.setData(key2, value2);
        Map<ByteArrayWrapper, byte[]> testing = account1.getStorageEntries();
        Assert.assertEquals(2, testing.size());
        Assert.assertTrue(Arrays.equals(value1_2, testing.get(new ByteArrayWrapper(key1))));
        Assert.assertTrue(Arrays.equals(value2, testing.get(new ByteArrayWrapper(key2))));
        
        // Check the directory layout contains places for this.
        File[] underTop = storage.listFiles();
        // This is the 1 account we created.
        Assert.assertEquals(1, underTop.length);
        
        File[] underAccount = underTop[0].listFiles();
        // We should see 5, here
        Assert.assertEquals(5, underAccount.length);
        
        // Make sure that the cleanup works correctly.
        store.deleteAccount(address1);
        Assert.assertEquals(0, storage.listFiles().length);
    }
}
