package org.aion.avm.shadow.java.util.function;

/**
 * @author Roman Katerinenko
 */
public interface Function {

    org.aion.avm.internal.IObject avm_apply(org.aion.avm.internal.IObject t);

}