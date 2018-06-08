package org.aion.avm.core;

public interface PrivateInterfaceMethod {

    void normalInterfaceMethod();

    default void interfaceMethodWithDefault() {  init(1); }

    default void anotherDefaultMethod() { init(2); }

    // This method is not part of the public API exposed by MyInterface
    private int init(int a) { return a * 2; }
}
