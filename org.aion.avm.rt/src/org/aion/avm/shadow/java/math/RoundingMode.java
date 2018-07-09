package org.aion.avm.shadow.java.math;

import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.shadow.java.lang.Enum;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Class;

import java.math.BigDecimal;

public class RoundingMode extends org.aion.avm.shadow.java.lang.Enum<RoundingMode>{
    public static final RoundingMode avm_UP;
    public static final RoundingMode avm_DOWN;
    public static final RoundingMode avm_CEILING;
    public static final RoundingMode avm_FLOOR;
    public static final RoundingMode avm_HALF_UP;
    public static final RoundingMode avm_HALF_DOWN;
    public static final RoundingMode avm_HALF_EVEN;
    public static final RoundingMode avm_UNNECESSARY;

    final int avm_oldMode;

    private static final ObjectArray avm_$VALUES;

    private RoundingMode(org.aion.avm.shadow.java.lang.String s, int a, int b){
        super(s, a);
        avm_oldMode = b;
    }

//    public static ObjectArray avm_values(){
//        return (ObjectArray) avm_$VALUES.clone();
//    }

    public static RoundingMode avm_valueOf(String request){
        return (RoundingMode) Enum.avm_valueOf(new Class(RoundingMode.class), request);
    }

    public static RoundingMode avm_valueOf(int idx){
        if (idx > 7 || idx < 0){
            throw new IllegalArgumentException("argument out of range");
        }else{
            return (RoundingMode) avm_$VALUES.get(idx);
        }
    }

    static {
        avm_UP = new RoundingMode(new String("UP"), 0, BigDecimal.ROUND_UP);
        avm_DOWN = new RoundingMode(new String("DOWN"), 1, BigDecimal.ROUND_DOWN);
        avm_CEILING = new RoundingMode(new String("CEILING"), 2, BigDecimal.ROUND_CEILING);
        avm_FLOOR = new RoundingMode(new String("FLOOR"), 3, BigDecimal.ROUND_FLOOR);
        avm_HALF_UP = new RoundingMode(new String("HALF_UP"), 4, BigDecimal.ROUND_HALF_UP);
        avm_HALF_DOWN = new RoundingMode(new String("HALF_DOWN"), 5, BigDecimal.ROUND_HALF_DOWN);
        avm_HALF_EVEN = new RoundingMode(new String("HALF_EVEN"), 6, BigDecimal.ROUND_HALF_EVEN);
        avm_UNNECESSARY = new RoundingMode(new String("UNNECESSARY"), 7, BigDecimal.ROUND_UNNECESSARY);

        avm_$VALUES = ObjectArray.initArray(8);
        avm_$VALUES.set(0, avm_UP);
        avm_$VALUES.set(1, avm_DOWN);
        avm_$VALUES.set(2, avm_CEILING);
        avm_$VALUES.set(3, avm_FLOOR);
        avm_$VALUES.set(4, avm_HALF_UP);
        avm_$VALUES.set(5, avm_HALF_DOWN);
        avm_$VALUES.set(6, avm_HALF_EVEN);
        avm_$VALUES.set(7, avm_UNNECESSARY);
    }
}