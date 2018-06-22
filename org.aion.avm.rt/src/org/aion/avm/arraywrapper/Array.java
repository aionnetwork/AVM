package org.aion.avm.arraywrapper;
import org.aion.avm.shadow.java.lang.Cloneable;
import org.aion.avm.shadow.java.lang.Object;

public abstract class Array extends Object implements Cloneable{
    public abstract int length();
}
