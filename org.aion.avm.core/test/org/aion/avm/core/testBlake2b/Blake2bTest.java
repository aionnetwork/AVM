package org.aion.avm.core.testBlake2b;

import java.math.BigInteger;

import org.aion.avm.core.AvmTransactionUtil;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class Blake2bTest {

    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;

    private AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private AionAddress dappAddress;

    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainAndClasses(Main.class, Blake2b.class);
        byte[] arguments = null;
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(this.kernel, new Transaction[] {tx})[0].get();
        System.out.println(txResult);

        dappAddress = new AionAddress(txResult.getReturnData());
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

        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[0], energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(this.kernel, new Transaction[] {tx})[0].get();
        System.out.println(txResult);

        assertArrayEquals(hash, txResult.getReturnData());
    }
}
