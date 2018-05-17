package org.aion.avm.separatejars.twotestclasses;

import java.util.function.Function;

/**
 * @author Roman Katerinenko
 */
public class C2 {
    private static final C2 anonymous = new C2() {
    };

    private static final Function lambda = (obj) -> null;

    enum NestedEnum {

    }

    public void doSomething() {
    }
}