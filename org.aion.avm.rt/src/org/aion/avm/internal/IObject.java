package org.aion.avm.internal;

import org.aion.avm.java.lang.Class;
import org.aion.avm.java.lang.Object;
import org.aion.avm.java.lang.String;


/**
 * See issue-80 for a more in-depth rationale leading up to the creation of this class.
 * In short, this defines the parts of the "shadow java.lang.Object" interface which is callable by contract code.
 * This exists as the root of both the class hierarchy and the interface hierarchy in order to allow interfaces to
 * be assignable to something we can treat as the "root".
 */
public interface IObject {
    public Class<?> avm_getClass();

    public int avm_hashCode();

    public boolean avm_equals(Object obj);

    public String avm_toString();
}
