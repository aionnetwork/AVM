package org.aion.avm.java.lang;

import org.aion.avm.internal.IObject;

/**
 * @author Roman Katerinenko
 */
public interface Runnable extends IObject {
    void avm_run();
}