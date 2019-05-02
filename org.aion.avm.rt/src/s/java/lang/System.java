package s.java.lang;

import org.aion.avm.arraywrapper.Array;
import i.IInstrumentation;
import i.IObject;
import org.aion.avm.RuntimeMethodFeeSchedule;


public final class System extends Object{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    private System() {
    }

    public static void avm_arraycopy(IObject src,  int  srcPos,
                                     IObject dest, int destPos,
                                     int length)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.System_avm_arraycopy + length);
        if(src == null || dest == null){
            throw new NullPointerException();
        } else if (!((src instanceof Array) && (dest instanceof Array))){
            throw new ArrayStoreException();
        }else{
            java.lang.Object asrc = ((Array) src).getUnderlyingAsObject();
            java.lang.Object adst = ((Array) dest).getUnderlyingAsObject();
            java.lang.System.arraycopy(asrc, srcPos, adst, destPos, length);
            ((Array) dest).setUnderlyingAsObject(adst);
        }
    }
}
