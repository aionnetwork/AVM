package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IObject;

public interface CharSequence extends IObject {

    int avm_length();

    char avm_charAt(int index);

    CharSequence avm_subSequence(int start, int end);

    String avm_toString();
}