package org.aion.avm.core.arraywrapping;

public class TestResource {

    public int increaseFirstElement() {
        int[] arr = new int[20];

        arr.hashCode();

        boolean x = arr instanceof Object;

        return arr[0];
    }
}