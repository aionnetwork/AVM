package org.aion.avm.internal;


/**
 * The interface required to support the Helper injected class which provides the global callout points from within
 * the instrumented code.
 * NOTE:  This is currently a work-in-progress for issue-308, directly mirroring the refactored implementation from
 * within Helper.
 */
public interface IInstrumentation {
    <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input);
    org.aion.avm.shadow.java.lang.String wrapAsString(String input);
    org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t);
    Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg);
    void chargeEnergy(long cost) throws OutOfEnergyException;
    long energyLeft();
    void setEnergy(long e);
    int getNextHashCode();
    void setAbortState();
    
    int getCurStackSize();
    int getCurStackDepth();
    void enterMethod(int frameSize);
    void exitMethod(int frameSize);
    void enterCatchBlock(int depth, int size);
    
    void forceNextHashCode(int nextHashCode);
}
