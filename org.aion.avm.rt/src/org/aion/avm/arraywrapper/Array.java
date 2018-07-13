package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Cloneable;
import org.aion.avm.shadow.java.lang.Object;

public abstract class Array extends Object implements Cloneable{
    // Initial creation.
    public Array() {
    }

    // Deserializer support.
    public Array(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public abstract java.lang.Object getUnderlyingAsObject();

    public abstract void setUnderlyingAsObject(java.lang.Object u);

    public abstract int length();

    public abstract IObject avm_clone();
}
