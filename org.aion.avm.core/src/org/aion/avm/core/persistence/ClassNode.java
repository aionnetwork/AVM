package org.aion.avm.core.persistence;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.Object;


/**
 * The data required to describe a Class reference, in the serialization layer.
 */
public class ClassNode implements INode {
    public final String className;

    public ClassNode(String className) {
        this.className = className;
    }

    @Override
    public Object getObjectInstance() {
        try {
            Class<?> jdkClass = Class.forName(this.className);
            return IHelper.currentContractHelper.get().externalWrapAsClass(jdkClass);
        } catch (ClassNotFoundException e) {
            // If this happens, how did we save it in the first place?
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public int getIdentityHashCode() {
        // The hash code of a class is just the hashcode of its name as a string.
        return this.className.hashCode();
    }
}
