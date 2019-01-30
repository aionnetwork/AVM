package org.aion.avm.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmAddress;
import org.aion.vm.api.interfaces.Address;
import org.junit.Test;

public class AddressTest {

    @Test(expected = NullPointerException.class)
    public void testUnderlyingByteArrayIsNull() {
        new AvmAddress(null);
    }

    @Test(expected = NullPointerException.class)
    public void testWrappingNullByteArray() {
        AvmAddress.wrap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayIsLengthZero() {
        new AvmAddress(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingZeroLengthByteArray() {
        AvmAddress.wrap(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLessThanRequiredSize() {
        new AvmAddress(Helpers.randomBytes(Address.SIZE - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooShortByteArray() {
        AvmAddress.wrap(Helpers.randomBytes(Address.SIZE - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLargerThanRequiredSize() {
        new AvmAddress(Helpers.randomBytes(Address.SIZE + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooLargeByteArray() {
        AvmAddress.wrap(Helpers.randomBytes(Address.SIZE + 1));
    }

    @Test
    public void testIsZero() {
        byte[] zeroBytes = new byte[Address.SIZE];
        assertTrue(new AvmAddress(zeroBytes).isZeroAddress());
        zeroBytes[0] = 0x1;
        assertFalse(new AvmAddress(zeroBytes).isZeroAddress());
    }

    @Test
    public void testEquivalenceOfConstructorAndWrap() {
        byte[] underlying = Helpers.randomBytes(Address.SIZE);
        Address addressByConstructor = new AvmAddress(underlying);
        Address addressByWrap = AvmAddress.wrap(underlying);
        assertEquals(addressByConstructor, addressByWrap);
    }

    @Test
    public void testEquivalenceOfToBytes() {
        byte[] underlying = Helpers.randomBytes(Address.SIZE);
        AvmAddress address = new AvmAddress(underlying);
        Address addressFromToBytes = AvmAddress.wrap(address.toBytes());
        assertEquals(address, addressFromToBytes);
    }

    @Test
    public void testEqualsOnTwoUnequalAddresses() {
        byte[] underlying1 = Helpers.randomBytes(Address.SIZE);
        byte[] underlying2 = Arrays.copyOf(underlying1, underlying1.length);
        underlying2[0] = (byte) (((int) underlying2[0]) + 1);
        Address address1 = new AvmAddress(underlying1);
        AvmAddress address2 = new AvmAddress(underlying2);
        assertNotEquals(address1, address2);
    }

}
