package org.aion.avm.core.shadowing;

public class TestObjectCreation {

    public static int accessObject() {
        return new Object().hashCode();
    }
}
