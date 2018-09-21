package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.Cloneable;
import org.aion.avm.shadow.java.lang.Object;


public abstract class Array extends Object implements Cloneable, IArray {
    // Initial creation.
    public Array() {
    }

    // Deserializer support.
    public Array(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public abstract java.lang.Object getUnderlyingAsObject();

    public abstract void setUnderlyingAsObject(java.lang.Object u);

    public abstract java.lang.Object getAsObject(int idx);

    public abstract int length();

    public abstract IObject avm_clone();

    static protected void chargeEnergy(long cost){
        IHelper.currentContractHelper.get().externalChargeEnergy(cost);
    }
}
