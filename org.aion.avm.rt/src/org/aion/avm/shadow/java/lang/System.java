package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.Array;
import org.aion.avm.internal.IObject;

public final class System extends Object{
    private System() {
    }

    public static void avm_arraycopy(IObject src,  int  srcPos,
                                     IObject dest, int destPos,
                                     int length)
    {
        if (!((src instanceof Array) && (dest instanceof Array))){
            throw new ArrayStoreException();
        }else{
            java.lang.Object asrc = ((Array) src).getUnderlyingAsObject();
            java.lang.Object adst = ((Array) dest).getUnderlyingAsObject();
            java.lang.System.arraycopy(asrc, srcPos, adst, destPos, length);
            ((Array) dest).setUnderlyingAsObject(adst);
        }
    }
}
