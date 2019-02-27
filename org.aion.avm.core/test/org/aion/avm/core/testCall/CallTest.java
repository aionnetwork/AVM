package org.aion.avm.core.testCall;

import org.aion.avm.shadowapi.org.aion.avm.api.Address;
import org.aion.avm.shadowapi.org.aion.avm.api.Result;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.blockchainruntime.TestingBlockchainRuntime;
import org.aion.avm.core.SuspendedInstrumentation;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.shadow.java.math.BigInteger;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallTest {

    private boolean callbackReceived = false;
    private boolean preserveDebuggability = false;


    @Test
    public void testCallIsHandled() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        byte[] from = new byte[Address.LENGTH];
        byte[] to = new byte[Address.LENGTH];
        long energyLimit = 5000000;

        SimpleAvm avm = new SimpleAvm(energyLimit, this.preserveDebuggability, Caller.class);
        avm.attachBlockchainRuntime(new TestingBlockchainRuntime(new EmptyCapabilities()) {
            @Override
            public Result avm_call(Address targetAddress, BigInteger value, ByteArray data, long energyLimit) {
                callbackReceived = true;

                assertEquals(new Address(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}), targetAddress);
                assertEquals(BigInteger.avm_TWO, value);
                assertEquals(new ByteArray("hello".getBytes()), data);
                assertEquals(10000, energyLimit);

                return new Result(true, new ByteArray("world".getBytes()));
            }
        }.withCaller(from).withAddress(to).withEnergyLimit(energyLimit));

        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(Caller.class.getName(), this.preserveDebuggability);
        Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("main")).invoke(null);

        assertTrue(callbackReceived);
        assertEquals(new ByteArray("world".getBytes()), ret);
        avm.shutdown();
    }

    @Test
    public void testSynchronousCall() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        byte[] from = new byte[Address.LENGTH];
        byte[] to = new byte[Address.LENGTH];
        long energyLimit = 5000000;

        SimpleAvm avm = new SimpleAvm(energyLimit, this.preserveDebuggability, Caller.class);
        avm.attachBlockchainRuntime(new TestingBlockchainRuntime(new EmptyCapabilities()) {
            @Override
            public Result avm_call(Address a, BigInteger v, ByteArray d, long e) {
                // We want to suspend the outer IInstrumentation for the sub-call (they are supposed to be distinct).
                SuspendedInstrumentation suspended = new SuspendedInstrumentation();
                
                callbackReceived = true;

                SimpleAvm avm2 = null;
                try {
                    avm2 = new SimpleAvm(e, CallTest.this.preserveDebuggability, Callee.class);
                    avm2.attachBlockchainRuntime(new TestingBlockchainRuntime(new EmptyCapabilities()).withCaller(to).withAddress(a.unwrap()).withData(d.getUnderlying()));
                    Class<?> clazz = avm2.getClassLoader().loadUserClassByOriginalName(Callee.class.getName(), CallTest.this.preserveDebuggability);
                    Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("main")).invoke(null);

                    // TODO: refund remaining energy

                    // TODO: how to restore the Helper state after call

                    // we have to re-wrap the byte array rather than returning the previous one.
                    return new Result(true, new ByteArray(((ByteArray)ret).getUnderlying()));

                } catch (Exception ex) {
                    ex.printStackTrace();

                    // TODO: how to interpret failure
                    return null;
                } finally {
                    avm2.shutdown();
                    suspended.resume();
                }
            }
        }.withCaller(from).withAddress(to).withEnergyLimit(energyLimit));

        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(Caller.class.getName(), this.preserveDebuggability);
        Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("main")).invoke(null);

        assertTrue(callbackReceived);
        assertEquals(new ByteArray("helloworld".getBytes()), ret);
        avm.shutdown();
    }
}
