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

        SimpleAvm avm = new SimpleAvm(energyLimit, Call.class);
        avm.attachBlockchainRuntime(new SimpleRuntime(from, to, energyLimit) {
            @Override
            public ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray data, long energyLimit) {
                callbackReceived = true;

                assertEquals(new Address(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}), targetAddress);
                assertEquals(new ByteArray(new byte[]{0, 0, 0, 2}), value);
                assertEquals(new ByteArray("hello".getBytes()), data);
                assertEquals(10000, energyLimit);

                return new ByteArray("world".getBytes());
            }
        });

        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(Call.class.getName());
        Object instance = clazz.getConstructor().newInstance();
        Object ret = clazz.getMethod(UserClassMappingVisitor.mapMethodName("foo")).invoke(instance);

        assertTrue(callbackReceived);
        assertEquals(new ByteArray("world".getBytes()), ret);
    }
}
