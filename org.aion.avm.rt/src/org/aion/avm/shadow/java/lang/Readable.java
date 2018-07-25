package org.aion.avm.shadow.java.lang;

import java.io.IOException;

public interface Readable {
    public int avm_read(org.aion.avm.shadow.java.nio.CharBuffer cb) throws IOException;
}
