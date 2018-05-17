package org.aion.avm.core.arraywrapping;

public class TestResource {

    public byte increaseFirstElement(byte[] xx) {
        if (xx.length > 0) {
            return xx[0]++;
        }

        return 0;
    }
}
