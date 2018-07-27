package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;

public abstract class LongBuffer extends Buffer {

    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    LongBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
