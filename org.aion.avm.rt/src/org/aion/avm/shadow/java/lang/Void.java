package org.aion.avm.shadow.java.lang;

public final class Void extends Object{

    public static final Class<Void> avm_TYPE = new Class(java.lang.Void.TYPE);

    /*
     * The Void class cannot be instantiated.
     */
    private Void() {
    }
}
