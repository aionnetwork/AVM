package org.aion.avm.java.lang;

public class Character extends Object {

    private char c;

    public Character(char c) {
        this.c = c;
    }

    public static Character avm_valueOf(char c) {
        return new Character(c);
    }


    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Character && this.c == ((Character) obj).c;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Character.toString(this.c);
    }
}