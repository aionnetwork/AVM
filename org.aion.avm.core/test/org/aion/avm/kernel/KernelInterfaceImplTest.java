package org.aion.avm.kernel;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class KernelInterfaceImplTest {

    @Test
    public void testPremine() {
        KernelInterface kernel = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        BigInteger value = BigInteger.TEN;
        kernel.adjustBalance(KernelInterfaceImpl.PREMINED_ADDRESS, value.negate());
        kernel.adjustBalance(address, value);

        assertEquals(KernelInterfaceImpl.PREMINED_AMOUNT.subtract(value), kernel.getBalance(KernelInterfaceImpl.PREMINED_ADDRESS));
        assertEquals(value, kernel.getBalance(address));
    }
}
