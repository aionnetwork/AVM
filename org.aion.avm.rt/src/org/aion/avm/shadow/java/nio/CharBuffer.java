package org.aion.avm.shadow.java.nio;

public abstract class CharBuffer extends Buffer {

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    CharBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}
