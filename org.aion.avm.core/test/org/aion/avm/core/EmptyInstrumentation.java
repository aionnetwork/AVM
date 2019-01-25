package org.aion.avm.core;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RuntimeAssertionError;

/**
 * An empty or nonsensical implementation of {@link IInstrumentation} for testing purposes. This
 * class is most useful when generating a new {@link org.aion.avm.api.Address} type for the ABI
 * encoder.
 */
public class EmptyInstrumentation implements IInstrumentation {

    @Override
    public void bootstrapOnly() {

    }

    @Override
    public void chargeEnergy(long arg0) throws OutOfEnergyException {

    }

    @Override
    public long energyLeft() {
        return 0;
    }

    @Override
    public void enterCatchBlock(int arg0, int arg1) {

    }

    @Override
    public void enterMethod(int arg0) {

    }

    @Override
    public void enterNewFrame(ClassLoader arg0, long arg1, int arg2) {

    }

    @Override
    public void exitCurrentFrame() {

    }

    @Override
    public void exitMethod(int arg0) {

    }

    @Override
    public void forceNextHashCode(int arg0) {

    }

    @Override
    public int getCurStackDepth() {
        return 0;
    }

    @Override
    public int getCurStackSize() {
        return 0;
    }

    @Override
    public int getNextHashCodeAndIncrement() {
        return 0;
    }

    @Override
    public int peekNextHashCode() {
        return 0;
    }

    @Override
    public void setAbortState() {

    }

    @Override
    public void clearAbortState() {

    }

    @Override
    public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable arg0) {
        return null;
    }

    @Override
    public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> arg0) {
        return null;
    }

    @Override
    public org.aion.avm.shadow.java.lang.String wrapAsString(String arg0) {
        return null;
    }

    @Override
    public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg0) {
        return null;
    }

    @Override
    public boolean isLoadedByCurrentClassLoader(java.lang.Class userClass) {
        throw RuntimeAssertionError.unreachable("Not expected in this test");
    }

}
