package org.aion.avm.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import org.aion.types.AionAddress;
import org.aion.avm.core.util.Helpers;
import org.junit.Test;

public class AddressTest {

    @Test(expected = NullPointerException.class)
    public void testUnderlyingByteArrayIsNull() {
        new AionAddress((byte[])null);
    }

    @Test(expected = NullPointerException.class)
    public void testWrappingNullByteArray() {
        new AionAddress((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayIsLengthZero() {
        new AionAddress(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingZeroLengthByteArray() {
        new AionAddress(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLessThanRequiredSize() {
        new AionAddress(Helpers.randomBytes(AionAddress.LENGTH - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooShortByteArray() {
        new AionAddress(Helpers.randomBytes(AionAddress.LENGTH - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLargerThanRequiredSize() {
        new AionAddress(Helpers.randomBytes(AionAddress.LENGTH + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooLargeByteArray() {
        new AionAddress(Helpers.randomBytes(AionAddress.LENGTH + 1));
    }

    @Test
    public void testIsZero() {
        byte[] zeroBytes = new byte[AionAddress.LENGTH];
        assertEquals(new AionAddress(zeroBytes), Helpers.ZERO_ADDRESS);
        zeroBytes[0] = 0x1;
        assertNotEquals(new AionAddress(zeroBytes), Helpers.ZERO_ADDRESS);
    }

    @Test
    public void testEquivalenceOfConstructorAndWrap() {
        byte[] underlying = Helpers.randomBytes(AionAddress.LENGTH);
        AionAddress addressByConstructor = new AionAddress(underlying);
        AionAddress addressByWrap = new AionAddress(underlying);
        assertEquals(addressByConstructor, addressByWrap);
    }

    @Test
    public void testEquivalenceOfToBytes() {
        byte[] underlying = Helpers.randomBytes(AionAddress.LENGTH);
        AionAddress address = new AionAddress(underlying);
        AionAddress addressFromToBytes = new AionAddress(address.toByteArray());
        assertEquals(address, addressFromToBytes);
    }

    @Test
    public void testEqualsOnTwoUnequalAddresses() {
        byte[] underlying1 = Helpers.randomBytes(AionAddress.LENGTH);
        byte[] underlying2 = Arrays.copyOf(underlying1, underlying1.length);
        underlying2[0] = (byte) (((int) underlying2[0]) + 1);
        AionAddress address1 = new AionAddress(underlying1);
        AionAddress address2 = new AionAddress(underlying2);
        assertNotEquals(address1, address2);
    }

}
