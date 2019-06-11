package org.aion.avm.kernel;

import java.math.BigInteger;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TestingKernel;
import org.aion.vm.api.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestingKernelTest {

    @Test
    public void testPremine() {
        KernelInterface kernel = new TestingKernel();
        Address address = Helpers.randomAddress();
        BigInteger delta = BigInteger.valueOf(10);
        kernel.adjustBalance(TestingKernel.PREMINED_ADDRESS, delta.negate());
        kernel.adjustBalance(address, delta);

        assertEquals(TestingKernel.PREMINED_AMOUNT.subtract(delta), kernel.getBalance(TestingKernel.PREMINED_ADDRESS));
        assertEquals(delta, kernel.getBalance(address));
    }
}
