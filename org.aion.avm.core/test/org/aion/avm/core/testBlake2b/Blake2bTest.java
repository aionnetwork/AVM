package org.aion.avm.core.testBlake2b;

import java.math.BigInteger;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.StandardCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class Blake2bTest {

    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddress;

    private KernelInterfaceImpl kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainAndClasses(Main.class, Blake2b.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        TransactionResult txResult = avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();
        System.out.println(txResult);

        dappAddress = AvmAddress.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBlake2b() {
        Blake2b mac = Blake2b.Mac.newInstance("key".getBytes());
        byte[] hash = mac.digest("input".getBytes());

        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[0], energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        TransactionResult txResult = avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();
        System.out.println(txResult);

        assertArrayEquals(hash, txResult.getReturnData());
    }
}
