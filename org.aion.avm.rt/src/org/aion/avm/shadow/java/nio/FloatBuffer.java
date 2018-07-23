package org.aion.avm.shadow.java.nio;

public abstract class FloatBuffer extends Buffer {

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    FloatBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
