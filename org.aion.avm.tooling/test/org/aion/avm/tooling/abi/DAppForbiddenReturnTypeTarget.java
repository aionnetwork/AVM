package org.aion.avm.tooling.abi;

public class DAppForbiddenReturnTypeTarget {

    @Callable()
    public static Integer badReturn() {
        return 5;
    }
}

