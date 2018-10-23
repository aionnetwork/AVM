package org.aion.avm.kernel;

import java.math.BigInteger;
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
        BigInteger delta = BigInteger.valueOf(10);
        kernel.adjustBalance(KernelInterfaceImpl.PREMINED_ADDRESS, delta.negate());
        kernel.adjustBalance(address, delta);

        assertEquals(KernelInterfaceImpl.PREMINED_AMOUNT.subtract(delta), kernel.getBalance(KernelInterfaceImpl.PREMINED_ADDRESS));
        assertEquals(delta, kernel.getBalance(address));
    }
}
