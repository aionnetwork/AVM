package org.aion.avm.shadow.java.lang;

import java.io.IOException;
import org.aion.avm.internal.IObject;

public interface Appendable extends IObject {

    Appendable avm_append(CharSequence csq) throws IOException;

    Appendable avm_append(CharSequence csq, int start, int end) throws IOException;

    Appendable avm_append(char c) throws IOException;
}
