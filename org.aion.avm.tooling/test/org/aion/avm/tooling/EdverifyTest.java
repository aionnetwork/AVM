package org.aion.avm.tooling;

import net.i2p.crypto.eddsa.Utils;
import avm.Address;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

public class EdverifyTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static byte[] publicKeyBytes = Utils.hexToBytes("8c11e9a4772bb651660a5a5e412be38d33f26b2de0487115d472a6c8bf60aa19");
    private byte[] testMessage = "test message".getBytes();
    private byte[] messageSignature = Utils.hexToBytes("0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61fb73777830ba3e92e4aa1832d41ec00c6e1d3bdd193e779cc2d51cb10d908c0a");

    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;

    private static Address deployer = avmRule.getPreminedAccount();
    private static Address dappAddress;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes(EdverifyTestTargetClass.class, null);
        dappAddress = avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testVerifyCorrectness(){
        byte[] txData = ABIUtil.encodeMethodArguments("callEdverify", testMessage, messageSignature, publicKeyBytes);

        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertTrue(txResult.transactionStatus.isSuccess());
        Assert.assertTrue(new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());
    }

    @Test
    public void testVerifyFailIncorrectSignature(){
        byte[] incorrectSignature = messageSignature;
        incorrectSignature[0] = (byte) (int)(incorrectSignature[0] + 1);

        byte[] txData = ABIUtil.encodeMethodArguments("callEdverify", testMessage, incorrectSignature, publicKeyBytes);

        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertTrue(txResult.transactionStatus.isSuccess());
        Assert.assertFalse(new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());
    }

    @Test
    public void testVerifyFailIncorrectPublicKey(){
        byte[] incorrectPublicKey = publicKeyBytes;
        incorrectPublicKey[1] = (byte) (int)(incorrectPublicKey[1] + 1);

        byte[] txData = ABIUtil.encodeMethodArguments("callEdverify", testMessage, messageSignature, incorrectPublicKey);

        TransactionResult txResult = avmRule.call(deployer, dappAddress, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();

        Assert.assertTrue(txResult.transactionStatus.isSuccess());
        Assert.assertFalse(new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());
    }
}
