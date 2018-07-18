package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testICO.IAionToken;
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
import org.aion.avm.core.testICO.PepeCoin;
import org.aion.avm.core.testICO.MemeCoin;
import org.aion.avm.core.testICO.ICOAbi;
import org.aion.avm.core.testICO.ICOController;
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
    private static KernelApiImpl cb;

    @BeforeClass
    public static void setupClass() {
        cb = new KernelApiImpl();
    }

    // For now, we will just reuse the from, to, and block for each call (in the future, this will change).
    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_000;

    private byte[] minter = Helpers.randomBytes(Address.LENGTH);
    private byte[] coinbaseAddress = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr2 = Helpers.randomBytes(Address.LENGTH);

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
        AvmImpl createAvm = new AvmImpl();
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0, testWalletJar, energyLimit);
        AvmResult createResult = createAvm.run(createTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, createResult.code);
        Assert.assertNotNull(cb.getTransformedCode(to));
    }

    /**
     * Tests that we can run init on the deployed code, albeit as a second transaction (since we haven't yet decided how to invoke init on deploy).
     */
    @Test
    public void testDeployAndCallInit() {
        // Constructor args.
        byte[] extra1 = Helpers.randomBytes(Address.LENGTH);
        byte[] extra2 = Helpers.randomBytes(Address.LENGTH);
        int requiredVotes = 2;
        long dailyLimit = 5000;

        byte[] testWalletJar = buildTestWalletJar();
        AvmImpl createAvm = new AvmImpl();
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0, testWalletJar, energyLimit);
        AvmResult createResult = createAvm.run(createTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, createResult.code);

        // contract address is stored in return data
        byte[] contractAddress = createResult.returnData;

        AvmImpl initAvm = new AvmImpl();
        byte[] initArgs = encodeInit(extra1, extra2, requiredVotes, dailyLimit);
        Transaction initTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, 0, initArgs, energyLimit);
        AvmResult initResult = initAvm.run(initTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, initResult.code);
    }


    /**
     * Note that this is copied from CallEncoder to allow us to create the input without needing to instantiate Address objects.
     */
    public static byte[] encodeInit(byte[] extra1, byte[] extra2, int requiredVotes, long dailyLimit) {
        byte[] onto = new byte[1 + Integer.BYTES + Address.LENGTH + Address.LENGTH + Integer.BYTES + Long.BYTES];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        // We are encoding the Addresses as a 2-element array, so describe it that way to the encoder.
        encoder
            .encodeByte(Abi.kWallet_init)
            .encodeInt(2)
            .encodeRemainder(extra1)
            .encodeRemainder(extra2)
            .encodeInt(requiredVotes)
            .encodeLong(dailyLimit);
        return onto;
    }


    private byte[] buildTestICOJar() {
        return JarBuilder.buildJarForMainAndClasses(ICOController.class,
                IAionToken.class,
                PepeCoin.class,
                MemeCoin.class,
                ICOAbi.class,
                AionMap.class,
                ByteArrayHelpers.class
        );
    }

    private byte[] initCoinbase(){
        byte[] testICOJar = buildTestICOJar();
        AvmImpl createAvm = new AvmImpl();
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, minter, coinbaseAddress, 0, testICOJar, energyLimit);
        AvmResult createResult = createAvm.run(createTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, createResult.code);
        return createResult.returnData;
    }


    private AvmResult callTotalSupply() {
        AvmImpl callAvm = new AvmImpl();
        byte[] args = new byte[1];
        ICOAbi.Encoder encoder = ICOAbi.buildEncoder(args);
        encoder.encodeByte(ICOAbi.kICO_totalSupply);

        Transaction callTransaction = new Transaction(Transaction.Type.CALL, usr2, coinbaseAddress, 0, args, energyLimit);
        AvmResult callResult = callAvm.run(callTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, callResult.code);
        return callResult;
    }

    private AvmResult callBalanceOf(byte[] toQuery) {
        AvmImpl callAvm = new AvmImpl();

        byte[] args = new byte[1 + Address.LENGTH];
        ICOAbi.Encoder encoder = ICOAbi.buildEncoder(args);
        encoder.encodeByte(ICOAbi.kICO_balanceOf);
        encoder.encodeAddress(new Address(toQuery));

        Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, coinbaseAddress, 0, args, energyLimit);
        AvmResult callResult = callAvm.run(callTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, callResult.code);
        return callResult;
    }

    private AvmResult callOpenAccount(byte[] toOpen) {
        AvmImpl callAvm = new AvmImpl();

        byte[] args = new byte[1 + Address.LENGTH];
        ICOAbi.Encoder encoder = ICOAbi.buildEncoder(args);
        encoder.encodeByte(ICOAbi.kICO_openAccount);
        encoder.encodeAddress(new Address(toOpen));

        Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, coinbaseAddress, 0, args, energyLimit);
        AvmResult callResult = callAvm.run(callTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, callResult.code);
        return callResult;
    }

    private AvmResult callMint(byte[] receiver) {
        AvmImpl callAvm = new AvmImpl();

        byte[] args = new byte[1 + Address.LENGTH + Long.BYTES];
        ICOAbi.Encoder encoder = ICOAbi.buildEncoder(args);
        encoder.encodeByte(ICOAbi.kICO_mint);
        encoder.encodeAddress(new Address(receiver));
        encoder.encodeLong(5000L);

        Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, coinbaseAddress, 0, args, energyLimit);
        AvmResult callResult = callAvm.run(callTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, callResult.code);
        return callResult;
    }

    private AvmResult callTransfer(byte[] sender, byte[] receiver, long amount) {
        AvmImpl callAvm = new AvmImpl();

        byte[] args = new byte[1 + Address.LENGTH + Long.BYTES];
        ICOAbi.Encoder encoder = ICOAbi.buildEncoder(args);
        encoder.encodeByte(ICOAbi.kICO_transfer);
        encoder.encodeAddress(new Address(receiver));
        encoder.encodeLong(amount);

        Transaction callTransaction = new Transaction(Transaction.Type.CALL, sender, coinbaseAddress, 0, args, energyLimit);
        AvmResult callResult = callAvm.run(callTransaction, block, cb);
        Assert.assertEquals(AvmResult.Code.SUCCESS, callResult.code);
        return callResult;
    }

    @Test
    public void testERC20(){

        AvmResult res = null;

        initCoinbase();

        res = callTotalSupply();

        Assert.assertEquals(PepeCoin.TOTAL_SUPPLY, ByteArrayHelpers.decodeLong(res.returnData));

        res = callBalanceOf(usr1);

        Assert.assertEquals(-1L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callOpenAccount(usr1);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callBalanceOf(usr1);

        Assert.assertEquals(0L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callOpenAccount(usr1);

        Assert.assertEquals(false, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callBalanceOf(usr2);

        Assert.assertEquals(-1L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callOpenAccount(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callOpenAccount(usr2);

        Assert.assertEquals(false, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callMint(usr1);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callBalanceOf(usr1);

        Assert.assertEquals(5000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callMint(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callMint(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callBalanceOf(usr2);

        Assert.assertEquals(10000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callTransfer(usr1, usr2, 2000L);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = callBalanceOf(usr1);

        Assert.assertEquals(3000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = callBalanceOf(usr2);

        Assert.assertEquals(12000L, ByteArrayHelpers.decodeLong(res.returnData));

    }

}
