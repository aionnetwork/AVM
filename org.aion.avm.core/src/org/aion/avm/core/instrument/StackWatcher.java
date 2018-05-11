package org.aion.avm.core.instrument;

public class StackWatcher {

    /* StackWacher policy
    *  POLICY_DEPTH will keep JVM stack within depth of limit_stack_depth.
    *  POLICY_SIZE  will keep JVM stack within size of limit_stact_size.
    *  (POLICY_DEPTH | POLICY_SIZE) will enforce both policy
    */
    public static final int POLICY_DEPTH = 1 << 0;
    public static final int POLICY_SIZE  = 1 << 1;

    private boolean checkDepth = false;
    private boolean checkSize  = false;

    private int limit_stack_depth = 1000;
    private int limit_stack_size  = 100000;

    private int cur_depth = 0;
    private int cur_size  = 0;

    public StackWatcher(int policy){
        this.checkDepth = (policy & POLICY_DEPTH) == 1;
        this.checkSize  = (policy & POLICY_SIZE)  == 2;
    }

    public void setStackDepthLimit(int l){
        this.limit_stack_depth = l;
    }

    public int getStackDepthLimit(){
        return this.limit_stack_depth;
    }

    public void setStackSizeLimit(int l){
        this.limit_stack_size = l;
    }

    public int getStackSizeLimit(){
        return this.limit_stack_size;
    }

    private int calculateFrameSize(){
        return 0;
    }

    public void abortCurrentContract(){

    }

    public void executeMethod(){

    }

}
