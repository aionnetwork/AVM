package org.aion.avm.kernel;

import java.math.BigInteger;
import org.aion.avm.core.IExternalState;
import org.aion.types.AionAddress;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TestingState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestingStateTest {

    @Test
    public void testPremine() {
        IExternalState kernel = new TestingState();
        AionAddress address = Helpers.randomAddress();
        BigInteger delta = BigInteger.valueOf(10);
        kernel.adjustBalance(TestingState.PREMINED_ADDRESS, delta.negate());
        kernel.adjustBalance(address, delta);

        assertEquals(TestingState.PREMINED_AMOUNT.subtract(delta), kernel.getBalance(TestingState.PREMINED_ADDRESS));
        assertEquals(delta, kernel.getBalance(address));
    }
}
