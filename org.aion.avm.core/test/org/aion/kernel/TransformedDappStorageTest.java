package org.aion.kernel;

import org.aion.avm.core.util.Helpers;
import org.aion.avm.api.Address;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransformedDappStorageTest {

    @Test
    public void testByteArrayWrapper (){
        byte[] byteArray1 = Helpers.randomBytes(Address.LENGTH);
        byte[] byteArray2 = byteArray1.clone();

        TransformedDappStorage codeStorage = new TransformedDappStorage();
        codeStorage.storeCode(byteArray1, TransformedDappStorage.CodeVersion.VERSION_1_0, new byte[] {0x0});
        assertEquals(TransformedDappStorage.CodeVersion.VERSION_1_0, codeStorage.getCodeVersion(byteArray2));
    }

}