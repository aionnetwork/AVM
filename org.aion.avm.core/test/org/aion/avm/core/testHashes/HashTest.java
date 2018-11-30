package org.aion.avm.core.testHashes;

import java.math.BigInteger;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.HashUtils;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class HashTest {

    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddress;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    private byte[] hashMessage = "test".getBytes();
    private final String blake2bMethodName = "callBlake2b";
    private final String shaMethodName = "callSha";
    private final String keccakbMethodName = "callKeccak";

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClasses(HashTestTargetClass.class);

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddress = avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBlake2b() {
        String expected = "928b20366943e2afd11ebc0eae2e53a93bf177a4fcf35bcc64d503704e65e202";

        // Call blake2b
        byte[] txData = ABIEncoder.encodeMethodArguments(blake2bMethodName, hashMessage);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult.getStatusCode());
        Assert.assertEquals(true, (TestingHelper.decodeResult(txResult)));

        // Retrieve hash
        byte[] txData2 = ABIEncoder.encodeMethodArguments("getHashedVal");
        Transaction tx2 = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData2, energyLimit, energyPrice);
        TransactionContextImpl context2 = new TransactionContextImpl(tx2, block);

        TransactionResult txResult2 = avm.run(new TransactionContext[]{context2})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult2.getStatusCode());

        // check hash correctness
        byte[] hash = (byte[]) TestingHelper.decodeResult(txResult2);
        String decodedHash = Hex.toHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.blake2b(hashMessage), hash);
    }

    @Test
    public void testSha(){
        String expected = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

        // Call sha256
        byte[] txData = ABIEncoder.encodeMethodArguments(shaMethodName, hashMessage);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult.getStatusCode());
        Assert.assertEquals(true, (TestingHelper.decodeResult(txResult)));

        // Retrieve hash
        byte[] txData2 = ABIEncoder.encodeMethodArguments("getHashedVal");
        Transaction tx2 = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData2, energyLimit, energyPrice);
        TransactionContextImpl context2 = new TransactionContextImpl(tx2, block);

        TransactionResult txResult2 = avm.run(new TransactionContext[]{context2})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult2.getStatusCode());

        // check hash correctness
        byte[] hash = (byte[]) TestingHelper.decodeResult(txResult2);
        String decodedHash = Hex.toHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.sha256(hashMessage), hash);
    }

    @Test
    public void testKeccak(){
        String expected = "9c22ff5f21f0b81b113e63f7db6da94fedef11b2119b4088b89664fb9a3cb658";

        // Call Keccak256
        byte[] txData = ABIEncoder.encodeMethodArguments(keccakbMethodName, hashMessage);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult.getStatusCode());
        Assert.assertEquals(true, (TestingHelper.decodeResult(txResult)));

        // Retrieve hash
        byte[] txData2 = ABIEncoder.encodeMethodArguments("getHashedVal");
        Transaction tx2 = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData2, energyLimit, energyPrice);
        TransactionContextImpl context2 = new TransactionContextImpl(tx2, block);

        TransactionResult txResult2 = avm.run(new TransactionContext[]{context2})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, txResult2.getStatusCode());

        // check hash correctness
        byte[] hash = (byte[]) TestingHelper.decodeResult(txResult2);
        String decodedHash = Hex.toHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.keccak256(hashMessage), hash);
    }
}
