package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the JCL and API types which can be sub-classed.
 * Also ensures that they can be persisted and reloaded correctly.
 */
public class SubclassPersistenceIntegrationTest {
    private static final Block BLOCK = new Block(new byte[32], 1L, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
    }

    @Test
    public void testUserSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_user");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_user");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testEnumSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_enum");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_enum");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testExceptionSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_exception");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_exception");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testObjectSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_object");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_object");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testRuntimeExceptionSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_runtimeException");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_runtimeException");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testThrowableSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_throwable");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_throwable");
        Assert.assertEquals(startValue, endValue);
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(testClass);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        Transaction create = new Transaction(Transaction.Type.CREATE, KernelInterfaceImpl.PREMINED_ADDRESS, Helpers.address(2), this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = this.avm.run(new TransactionContextImpl(create, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        return TestingHelper.buildAddress(createResult.getReturnData());
    }

    private int callStaticReturnInteger(Address dapp, String methodName) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = new Transaction(Transaction.Type.CALL, KernelInterfaceImpl.PREMINED_ADDRESS, dapp.unwrap(), this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = this.avm.run(new TransactionContextImpl(call, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }
}
