package org.aion.avm.core.testWallet2;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.Block;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class WalletTest {

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private byte[] deployer = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner2 = Helpers.randomBytes(Address.LENGTH);
    private byte[] receiver = Helpers.randomBytes(Address.LENGTH);
    private byte[] contract = null;
    private byte[] pendingTx = null;

    private static Address createAddressInFakeContract(byte[] bytes) {
        // Create a fake runtime for encoding the arguments (since these are shadow objects - they can only be instantiated within the context of a contract).
        IHelper.currentContractHelper.set(new IHelper() {
            @Override
            public void externalChargeEnergy(long cost) {
                Assert.fail("Not in test");
            }
            @Override
            public void externalSetEnergy(long energy) {
                Assert.fail("Not in test");
            }
            @Override
            public long externalGetEnergyRemaining() {
                Assert.fail("Not in test");
                return 0;
            }
            @Override
            public Class<?> externalWrapAsClass(java.lang.Class<?> input) {
                Assert.fail("Not in test");
                return null;
            }
            @Override
            public int externalGetNextHashCode() {
                // Will be called.
                return 1;
            }
            @Override
            public void externalBootstrapOnly() {
                Assert.fail("Not in test");
            }});
        Address instance = new Address(bytes);
        IHelper.currentContractHelper.set(null);
        return instance;
    }

    @Test
    public void testWallet() throws InvalidTxDataException {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);

        //================
        // DEPLOY
        //================
        byte[] jar = JarBuilder.buildJarForMainAndClasses(Main.class, Wallet.class, Bytes32.class, AionSet.class, AionMap.class);
        //byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.testWallet.jar");
        byte[] arguments = ABIEncoder.encodeMethodArguments("", createAddressInFakeContract(owner1), createAddressInFakeContract(owner2), 2);
        Transaction tx = new Transaction(Transaction.Type.CREATE, deployer, null, 0L, Helpers.encodeCodeAndData(jar, arguments), 2_000_000L);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = new AvmImpl().run(txContext);
        System.out.println(">> Wallet Dapp is deployed. The address is " + Helpers.toHexString((byte[])txResult.getReturnData()));
        contract = txResult.getReturnData();

        //================
        // PROPOSE
        //================
        byte[] data = ABIEncoder.encodeMethodArguments("propose", new Address(receiver), 1L, "DATA".getBytes(), 1_000_000L);
        tx = new Transaction(Transaction.Type.CALL, deployer, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> Proposed a transaction with the ID " + Helpers.toHexString((byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData())));
        pendingTx = (byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData());

        //================
        // CONFIRM #1
        //================
        data = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner1, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        //================
        // CONFIRM #2
        //================
        data = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner2, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        //TODO- fix this -- BlockchainRuntime.call(pendingTx.to, pendingTx.value, pendingTx.data, pendingTx.energyLimit) fails because it cannot do. Need to deploy an ERC20 token Dapp, and route this call to it.
        //txResult = new AvmImpl().run(txContext);
        //System.out.println(">> " + ABIDecoder.decodeOneObject(txResult.getReturnData()));
    }
}
