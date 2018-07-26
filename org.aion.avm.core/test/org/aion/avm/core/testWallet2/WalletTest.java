package org.aion.avm.core.testWallet2;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.Block;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Ignore;
import org.junit.Test;

public class WalletTest {

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private byte[] deployer = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner2 = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner3 = Helpers.randomBytes(Address.LENGTH);
    private byte[] contract = null;
    private byte[] pendingTx = null;

    @Test
    @Ignore
    public void testWallet() throws InvalidTxDataException {
        //================
        // DEPLOY
        //================
        byte[] jar = JarBuilder.buildJarForMainAndClasses(Main.class, Wallet.class, Bytes32.class, AionSet.class, AionMap.class);
        byte[] arguments = new byte[0]; // TODO: encode the owners and k
        byte[] data = Helpers.encodeCodeAndData(jar, arguments);
        Transaction tx = new Transaction(Transaction.Type.CREATE, deployer, null, 0L, data, 2_000_000L);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = new AvmImpl().run(txContext);
        System.out.println(">> " + txResult);
        contract = txResult.getReturnData();

        //================
        // PROPOSE
        //================
        data = ABIEncoder.encodeMethodArguments("propose", new Address(new byte[Address.LENGTH]), 1L, "DATA".getBytes(), 1_000_000L);
        tx = new Transaction(Transaction.Type.CALL, deployer, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> " + txResult);
        pendingTx = txResult.getReturnData();

        //================
        // CONFIRM #1
        //================
        data = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner1, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> " + txResult);

        //================
        // CONFIRM #2
        //================
        data = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner2, contract, 0L, data, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> " + txResult);
    }
}
