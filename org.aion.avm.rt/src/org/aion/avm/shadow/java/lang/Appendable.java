package org.aion.avm.shadow.java.lang;

import java.io.IOException;

public interface Appendable {

    Appendable avm_append(CharSequence csq) throws IOException;

    Appendable avm_append(CharSequence csq, int start, int end) throws IOException;

    Appendable avm_append(char c) throws IOException;
}
