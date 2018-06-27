package org.aion.avm.arraywrapper;
import org.aion.avm.shadow.java.lang.Cloneable;
import org.aion.avm.shadow.java.lang.Object;

public abstract class Array extends Object implements Cloneable{
    public abstract java.lang.Object getUnderlyingAsObject();

    public abstract void setUnderlyingAsObject(java.lang.Object u);

    public abstract int length();
}
