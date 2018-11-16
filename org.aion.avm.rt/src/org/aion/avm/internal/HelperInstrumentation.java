package org.aion.avm.internal;


/**
 * This exists purely as a stopgap for issue-308:  an implementation of IInstrumentation which purely bounces back into the IHelper thread local.
 * The entire point of this is to allow API usage to transition before we completely change core behaviour.
 */
public class HelperInstrumentation implements IInstrumentation {
    @SuppressWarnings("unchecked")
    @Override
    public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(java.lang.Class<T> input) {
        return (org.aion.avm.shadow.java.lang.Class<T>) IHelper.currentContractHelper.get().externalWrapAsClass(input);
    }

    @Override
    public org.aion.avm.shadow.java.lang.String wrapAsString(java.lang.String input) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public void chargeEnergy(long cost) throws OutOfEnergyException {
        IHelper.currentContractHelper.get().externalChargeEnergy(cost);
    }

    @Override
    public long energyLeft() {
        return IHelper.currentContractHelper.get().externalGetEnergyRemaining();
    }

    @Override
    public int getNextHashCodeAndIncrement() {
        return IHelper.currentContractHelper.get().externalGetNextHashCodeAndIncrement();
    }

    @Override
    public void setAbortState() {
        IHelper.currentContractHelper.get().externalSetAbortState();
    }

    @Override
    public int getCurStackSize() {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public int getCurStackDepth() {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public void enterMethod(int frameSize) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public void exitMethod(int frameSize) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public void enterCatchBlock(int depth, int size) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public int peekNextHashCode() {
        return IHelper.currentContractHelper.get().externalPeekNextHashCode();
    }

    @Override
    public void forceNextHashCode(int nextHashCode) {
        throw RuntimeAssertionError.unimplemented("Not part of IHelper interface");
    }

    @Override
    public void bootstrapOnly() {
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }
}
