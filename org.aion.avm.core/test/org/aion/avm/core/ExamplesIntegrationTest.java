package org.aion.avm.core;

import examples.BetaMapEvents;
import examples.HelloWorld;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.userlib.AionMap;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Various tests to prove that our examples we build into our packages basically work.
 */
public class ExamplesIntegrationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Test
    public void test_BetaMapEvents() throws Exception {
        byte[] txData = avmRule.getDappBytes(BetaMapEvents.class, new byte[0], AionMap.class);
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TransactionResult createResult = avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // We will just invoke a basic sequence of "PUT", "PUT", "GET" to make sure that we can call the main entry-points and execute the main paths.
        callStatic(contractAddr, "put", "key1", "value1");
        callStatic(contractAddr, "put", "key1", "value2");
        callStatic(contractAddr, "get", "key1");
    }

    @Test
    public void test_HelloWorld() throws Exception {
        byte[] txData = avmRule.getDappBytes(HelloWorld.class, new byte[0]);

        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TransactionResult createResult = avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // We only want to check that we can call it without issue (it only produces STDOUT).
        callStatic(contractAddr, "sayHello");
    }


    private void callStatic(Address contractAddr, String methodName, Object... args) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, args);
        TransactionResult result = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO, argData, energyLimit, 1l).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
