package org.aion.avm.core.instrument;

import java.util.HashMap;

import org.objectweb.asm.Opcodes;

/**
 * The bytecode fee schedule as designed on wiki page
 * See {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Java-Bytecode-fee-schedule}
 */
public class BytecodeFeeScheduler {

    /**
     * The bytecode Energy levels as designed on wiki page
     * See {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Java-Bytecode-fee-schedule}
     */
    public enum BytecodeEnergyLevels {
        ZERO        (0),
        BASE        (2),
        VERYLOW     (3),
        LOW         (5),
        MID         (8),
        HIGH        (10),
        VERYHIGH    (13),
        MACCESS     (20),
        FLOWCONTROL (40),
        CREATION    (40),
        MEMORY      (3);

        private final int val;
        BytecodeEnergyLevels(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    /**
     * The bytecode fee info, including the Energy levels, alpha, delta and the static fee.
     */
    private class BytecodeFeeInfo {
        BytecodeEnergyLevels    nrgLvl;
        int                     delta;    // number of the items removed from the stack
        int                     alpha;    // number of the additional items placed on the stack
        int                     fee;      // the static fee of this bytecode, generally including the computation cost and stack memory cost, assuming that the heap memory cost is added dynamically.

        private BytecodeFeeInfo (BytecodeEnergyLevels nrgLvl,
                                 int delta,
                                 int alpha,
                                 int fee) {
            this.nrgLvl     = nrgLvl;
            this.delta      = delta;
            this.alpha      = alpha;
            this.fee        = fee;
        }

        /**
         * return the Energy level.
         */
        public BytecodeEnergyLevels getNrgLvl() {
            return nrgLvl;
        }

        /**
         * return the Delta.
         */
        public int getDelta() {
            return delta;
        }

        /**
         * return the Alpha.
         */
        public int getAlpha() {
            return alpha;
        }

        /**
         * return the fee.
         */
        public int getFee() {
            return fee;
        }

        private void setFee(int fee) {
            this.fee = fee;
        }
    }

    /**
    * A hashmap that stores the fee info for each bytecode.
    */
    private HashMap<Integer, BytecodeFeeInfo> feeScheduleMap;

    /**
     * Constructor.
     */
    public BytecodeFeeScheduler() {
    }

    /**
     * Initialize the fee schedule Hashmap. Add the fee info for each bytecode and calculate the static fee.
     * The bytecode Energy levels, alpha and delta are listed on wiki page
     * See {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Java-Bytecode-fee-schedule}
     */
    public void initialize() {
        feeScheduleMap = new HashMap<>()
        {{
            put(Opcodes.NOP,         new BytecodeFeeInfo(BytecodeEnergyLevels.ZERO,    0, 0, 0));
            put(Opcodes.ACONST_NULL, new BytecodeFeeInfo(BytecodeEnergyLevels.VERYLOW, 0, 1, 0));
        }};

        // calculate the static fee for each bytecode.
        for (int op : feeScheduleMap.keySet()) {
            BytecodeFeeInfo feeInfo = feeScheduleMap.get(op);
            int fee = feeInfo.getNrgLvl().getVal()
                      + BytecodeEnergyLevels.MEMORY.getVal() * Math.max((feeInfo.getAlpha() - feeInfo.getDelta()), 0);

            feeInfo.setFee(fee);
        }
    }

    /**
     * return the bytecode fee.
     */
    public int getFee(int op) {
        if (feeScheduleMap.containsKey(op)) {
            return feeScheduleMap.get(op).getFee();
        }
        else {
            return -1; // deal with this error code later
        }
    }
}
