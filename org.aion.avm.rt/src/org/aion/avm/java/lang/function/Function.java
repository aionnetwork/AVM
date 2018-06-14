package org.aion.avm.java.lang.function;

/**
 * @author Roman Katerinenko
 */
public interface Function extends org.aion.avm.internal.IObject {
    org.aion.avm.internal.IObject avm_apply(org.aion.avm.internal.IObject t);
}