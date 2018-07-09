package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testWallet.Abi;
import org.aion.avm.core.testWallet.ByteArrayHelpers;
import org.aion.avm.core.testWallet.ByteArrayWrapper;
import org.aion.avm.core.testWallet.BytesKey;
import org.aion.avm.core.testWallet.Daylimit;
import org.aion.avm.core.testWallet.EventLogger;
import org.aion.avm.core.testWallet.Multiowned;
import org.aion.avm.core.testWallet.Operation;
import org.aion.avm.core.testWallet.RequireFailedException;
import org.aion.avm.core.testWallet.Wallet;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApiImpl;
import org.aion.kernel.Transaction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Our current thinking is that we will use a JUnit launcher for the proof-of-concept demonstration.  This is that entry-point.
 * See issue-124 for more of the background.
 */
public class ProofOfConceptTest {
    private static AvmSharedClassLoader sharedClassLoader;
    private static KernelApiImpl cb;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
        cb = new KernelApiImpl();
    }

    // For now, we will just reuse the from, to, and block for each call (in the future, this will change).
    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_000;

    private byte[] buildTestWalletJar() {
        return JarBuilder.buildJarForMainAndClasses(Wallet.class
                , Multiowned.class
                , AionMap.class
                , AionSet.class
                , AionList.class
                , ByteArrayWrapper.class
                , Operation.class
                , ByteArrayHelpers.class
                , BytesKey.class
                , RequireFailedException.class
                , Daylimit.class
                , EventLogger.class
                , Abi.class
        );
    }

    /**
     * Tests that a deploy call will store the code for the Wallet JAR.
     * This means that it transformed it correctly and nothing was missing.
     */
    @Test
    public void testDeployWritesCode() {
        byte[] testWalletJar = buildTestWalletJar();
        AvmImpl createAvm = new AvmImpl(sharedClassLoader);
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0, testWalletJar, energyLimit);
        AvmResult createResult = createAvm.run(createTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, createResult.code);
        Assert.assertNotNull(cb.getTransformedCode(to));
    }
}
