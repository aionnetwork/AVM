package org.aion.avm.shadow.java.util.function;

import org.aion.avm.internal.IObject;

/**
 * @author Roman Katerinenko
 */
public interface Function extends IObject {

    org.aion.avm.internal.IObject avm_apply(org.aion.avm.internal.IObject t);

}