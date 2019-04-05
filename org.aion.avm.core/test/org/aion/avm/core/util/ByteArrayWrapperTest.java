package org.aion.avm.core.util;

import avm.Address;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteArrayWrapperTest {

    @Test
    public void testByteArrayWrapper() {
        byte[] byteArray1 = Helpers.randomBytes(Address.LENGTH);
        byte[] byteArray2 = byteArray1.clone();

        assertEquals(new ByteArrayWrapper(byteArray1), new ByteArrayWrapper(byteArray2));
        assertEquals(new ByteArrayWrapper(byteArray1).hashCode(), new ByteArrayWrapper(byteArray2).hashCode());
    }

}