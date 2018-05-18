package org.aion.avm.core.arraywrapping;

public class TestResource {

    public int increaseFirstElement() {
        int[] arr = new int[1];

        arr.hashCode();

        return arr[0];
    }
}
