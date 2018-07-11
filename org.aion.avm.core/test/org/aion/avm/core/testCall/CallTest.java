package org.aion.avm.core.testCall;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallTest {

    private boolean callbackReceived = false;

    @Test
    public void testCallIsHandled() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        byte[] from = new byte[Address.LENGTH];
        byte[] to = new byte[Address.LENGTH];
        long energyLimit = 5000000;

        SimpleAvm avm = new SimpleAvm(energyLimit, Caller.class);
        avm.attachBlockchainRuntime(new SimpleRuntime(from, to, energyLimit) {
            @Override
            public ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
                callbackReceived = true;

                assertEquals(new Address(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}), targetAddress);
                assertEquals(2, value);
                assertEquals(new ByteArray("hello".getBytes()), data);
                assertEquals(10000, energyLimit);

                return new ByteArray("world".getBytes());
            }
        });

        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(Caller.class.getName());
        Object ret = clazz.getMethod(UserClassMappingVisitor.mapMethodName("main")).invoke(null);

        assertTrue(callbackReceived);
        assertEquals(new ByteArray("world".getBytes()), ret);
    }

    @Test
    public void testSynchronousCall() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        byte[] from = new byte[Address.LENGTH];
        byte[] to = new byte[Address.LENGTH];
        long energyLimit = 5000000;

        SimpleAvm avm = new SimpleAvm(energyLimit, Caller.class);
        avm.attachBlockchainRuntime(new SimpleRuntime(from, to, energyLimit) {
            @Override
            public ByteArray avm_call(Address a, long v, ByteArray d, long e) {
                callbackReceived = true;

                try {
                    SimpleAvm avm2 = new SimpleAvm(e, Callee.class);
                    avm2.attachBlockchainRuntime(new SimpleRuntime(to, a.unwrap(), e, d.getUnderlying()));
                    Class<?> clazz = avm2.getClassLoader().loadUserClassByOriginalName(Callee.class.getName());
                    Object ret = clazz.getMethod(UserClassMappingVisitor.mapMethodName("main")).invoke(null);

                    // TODO: refund remaining energy

                    // TODO: how to restore the Helper state after call

                    // we have to re-wrap the byte array rather than returning the previous one.
                    return new ByteArray(((ByteArray)ret).getUnderlying());

                } catch (Exception ex) {
                    ex.printStackTrace();

                    // TODO: how to interpret failure
                    return null;
                }
            }
        });

        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(Caller.class.getName());
        Object ret = clazz.getMethod(UserClassMappingVisitor.mapMethodName("main")).invoke(null);

        assertTrue(callbackReceived);
        assertEquals(new ByteArray("helloworld".getBytes()), ret);
    }
}