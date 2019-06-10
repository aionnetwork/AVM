package org.aion.avm.tooling.testHashes;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.tooling.hash.HashUtils;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

public class HashTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;

    private static Address deployer = avmRule.getPreminedAccount();
    private static Address dappAddress;

    private byte[] hashMessage = "test".getBytes();
    private final String blake2bMethodName = "callBlake2b";
    private final String shaMethodName = "callSha";
    private final String keccakbMethodName = "callKeccak";

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes(HashTestTargetClass.class, null);
        dappAddress = avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testBlake2b() {
        String expected = "928b20366943e2afd11ebc0eae2e53a93bf177a4fcf35bcc64d503704e65e202";

        // Call blake2b
        byte[] txData = ABIUtil.encodeMethodArguments(blake2bMethodName, hashMessage);
        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertEquals(true, new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        // Retrieve hash
        byte[] txData2 = ABIUtil.encodeMethodArguments("getHashedVal");
        TransactionResult txResult2 = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData2, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult2.getResultCode());

        // check hash correctness
        byte[] hash = new ABIDecoder(txResult2.getReturnData()).decodeOneByteArray();
        String decodedHash = Helpers.bytesToHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.blake2b(hashMessage), hash);
    }

    @Test
    public void testSha(){
        String expected = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

        // Call sha256
        byte[] txData = ABIUtil.encodeMethodArguments(shaMethodName, hashMessage);
        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertEquals(true, new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        // Retrieve hash
        byte[] txData2 = ABIUtil.encodeMethodArguments("getHashedVal");
        TransactionResult txResult2 = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData2, energyLimit, energyPrice).getTransactionResult();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult2.getResultCode());

        // check hash correctness
        byte[] hash = new ABIDecoder(txResult2.getReturnData()).decodeOneByteArray();
        String decodedHash = Helpers.bytesToHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.sha256(hashMessage), hash);
    }

    @Test
    public void testKeccak(){
        String expected = "9c22ff5f21f0b81b113e63f7db6da94fedef11b2119b4088b89664fb9a3cb658";

        // Call Keccak256
        byte[] txData = ABIUtil.encodeMethodArguments(keccakbMethodName, hashMessage);
        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertEquals(true, new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        // Retrieve hash
        byte[] txData2 = ABIUtil.encodeMethodArguments("getHashedVal");
        TransactionResult txResult2 = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData2, energyLimit, energyPrice).getTransactionResult();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult2.getResultCode());

        // check hash correctness
        byte[] hash = new ABIDecoder(txResult2.getReturnData()).decodeOneByteArray();
        String decodedHash = Helpers.bytesToHexString(hash);

        // check decoded
        Assert.assertEquals(expected, decodedHash);

        // check hash
        Assert.assertArrayEquals(HashUtils.keccak256(hashMessage), hash);
    }
}
