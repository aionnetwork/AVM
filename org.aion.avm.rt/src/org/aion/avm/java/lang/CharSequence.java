package org.aion.avm.java.lang;

public interface CharSequence {

    int avm_length();

    char avm_charAt(int index);

    CharSequence avm_subSequence(int start, int end);

    String avm_toString();
}