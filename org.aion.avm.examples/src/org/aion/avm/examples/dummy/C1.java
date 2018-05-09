package org.aion.avm.examples.dummy;

/**
 * @author Roman Katerinenko
 */
public class C1 {
    private C2 c2;

    public C2 getC2() {
        c2 = new C2();
        c2.getInt();
        return c2;
    }
}