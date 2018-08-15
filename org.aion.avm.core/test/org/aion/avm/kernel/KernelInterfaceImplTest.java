package org.aion.avm.kernel;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KernelInterfaceImplTest {

    @Test
    public void testPremine() {
        KernelInterface kernel = new KernelInterfaceImpl();
        byte[] address = Helpers.randomBytes(32);
        long delta = 10;
        kernel.adjustBalance(KernelInterfaceImpl.PREMINED_ADDRESS, -delta);
        kernel.adjustBalance(address, delta);

        assertEquals(KernelInterfaceImpl.PREMINED_AMOUNT - delta, kernel.getBalance(KernelInterfaceImpl.PREMINED_ADDRESS));
        assertEquals(delta, kernel.getBalance(address));
    }
}
