package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.ConstantToken;
import org.aion.avm.internal.ShadowClassConstantId;

public final class Void extends Object{

    public static final Class<Void> avm_TYPE = new Class(java.lang.Void.TYPE, new ConstantToken(ShadowClassConstantId.Void_avm_TYPE));

    /*
     * The Void class cannot be instantiated.
     */
    private Void() {
    }
}
