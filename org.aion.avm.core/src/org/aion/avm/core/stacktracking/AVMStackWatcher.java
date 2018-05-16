package org.aion.avm.core.stacktracking;

import java.lang.Thread;

public class AVMStackWatcher {

    /* StackWacher policy
    *  POLICY_DEPTH will keep JVM stack within depth of maxStackDepth.
    *  POLICY_SIZE  will keep JVM stack within size of maxStackSize.
    *  (POLICY_DEPTH | POLICY_SIZE) will enforce both policy
    */
    public static final int POLICY_DEPTH = 1 << 0;
    public static final int POLICY_SIZE  = 1 << 1;

    private static boolean checkDepth = false;
    private static boolean checkSize  = false;

    private static int maxStackDepth = 1000;
    private static int maxStackSize  = 100000;

    private static int curDepth = 0;
    private static int curSize  = 0;

    public static void setPolicy(int policy){
        checkDepth = (policy & POLICY_DEPTH) == 1;
        checkSize  = (policy & POLICY_SIZE)  == 2;
    }

    public static void setMaxStackDepth(int l){
        maxStackDepth = l;
    }

    public static int getMaxStackDepth(){
        return maxStackDepth;
    }

    public static void setMaxStackSize(int l){
        maxStackSize = l;
    }

    public static int getMaxStackSize(){
        return maxStackSize;
    }

    public static void recalibrateStackSize(){
        curSize = 0;
    }

    public static void recalibrateStackDepth(){
        curDepth = Thread.currentThread().getStackTrace().length - 2;
    }

    public static void abortCurrentContract(){
        //throw new AVMStackError("AVM stack overflow")
    }

    public static void enterMethod(){
        if (checkDepth && (curDepth++ > maxStackDepth)){
            abortCurrentContract();
        }
    }

    public static void exitMethod(){
        if (checkDepth && (curDepth-- < 0)){
            abortCurrentContract();
        }
    }

    public static void enterCatchBlock(){
        if (checkDepth){
            recalibrateStackDepth();
        }

        if (checkSize){
            recalibrateStackSize();
        }
    }

}
