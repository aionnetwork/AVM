package org.aion.avm.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.aion.avm.core.util.Helpers;
import org.aion.vm.api.types.Address;
import org.junit.Test;

public class AddressTest {

    @Test(expected = NullPointerException.class)
    public void testUnderlyingByteArrayIsNull() {
        new Address((byte[])null);
    }

    @Test(expected = NullPointerException.class)
    public void testWrappingNullByteArray() {
        Address.wrap((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayIsLengthZero() {
        new Address(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingZeroLengthByteArray() {
        Address.wrap(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLessThanRequiredSize() {
        new Address(Helpers.randomBytes(Address.SIZE - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooShortByteArray() {
        Address.wrap(Helpers.randomBytes(Address.SIZE - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnderlyingByteArrayLargerThanRequiredSize() {
        new Address(Helpers.randomBytes(Address.SIZE + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrappingTooLargeByteArray() {
        Address.wrap(Helpers.randomBytes(Address.SIZE + 1));
    }

    @Test
    public void testIsZero() {
        byte[] zeroBytes = new byte[Address.SIZE];
        assertTrue(new Address(zeroBytes).isZeroAddress());
        zeroBytes[0] = 0x1;
        assertFalse(new Address(zeroBytes).isZeroAddress());
    }

    @Test
    public void testEquivalenceOfConstructorAndWrap() {
        byte[] underlying = Helpers.randomBytes(Address.SIZE);
        Address addressByConstructor = new Address(underlying);
        Address addressByWrap = Address.wrap(underlying);
        assertEquals(addressByConstructor, addressByWrap);
    }

    @Test
    public void testEquivalenceOfToBytes() {
        byte[] underlying = Helpers.randomBytes(Address.SIZE);
        Address address = new Address(underlying);
        Address addressFromToBytes = Address.wrap(address.toBytes());
        assertEquals(address, addressFromToBytes);
    }

    @Test
    public void testEqualsOnTwoUnequalAddresses() {
        byte[] underlying1 = Helpers.randomBytes(Address.SIZE);
        byte[] underlying2 = Arrays.copyOf(underlying1, underlying1.length);
        underlying2[0] = (byte) (((int) underlying2[0]) + 1);
        Address address1 = new Address(underlying1);
        Address address2 = new Address(underlying2);
        assertNotEquals(address1, address2);
    }

}
