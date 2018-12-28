package org.aion.avm.core.util;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;


/**
 * Implements the IInstrumentation interface for tests which need to create runtime objects or otherwise interact with the parts of the system
 * which assume that there is an IInstrumentation installed.
 * It automatically installs itself as the helper and provides utilities to install and remove itself from IInstrumentation.currentContractHelper.
 * Additionally, it provides some common static helpers for common cases of its use.
 */
public class TestingHelper implements IInstrumentation {
    public static Address buildAddress(byte[] raw) {
        TestingHelper helper = new TestingHelper(false);
        Address data = new Address(raw);
        helper.remove();
        return data;
    }
    public static Object decodeResult(TransactionResult result) {
        return decodeResultRaw(result.getReturnData());
    }
    public static Object decodeResultRaw(byte[] returnData) {
        Object data = null;
        if (null != returnData) {
            TestingHelper helper = new TestingHelper(false);
            data = ABIDecoder.decodeOneObject(returnData);
            helper.remove();
        }
        return data;
    }

    /**
     * A special entry-point used only the test wallet when running the constract, inline.  This allows the helper to be setup for constant initialization.
     * 
     * @param invocation The invocation to run under the helper.
     */
    public static void runUnderBoostrapHelper(Runnable invocation) {
        TestingHelper helper = new TestingHelper(true);
        try {
            invocation.run();
        } finally {
            helper.remove();
        }
    }


    private final boolean isBootstrapOnly;
    private final int constantHashCode;

    private TestingHelper(boolean isBootstrapOnly) {
        this.isBootstrapOnly = isBootstrapOnly;
        // If this is a helper created for bootstrap purposes, use the "placeholder hash code" we rely on for constants.
        // Otherwise, use something else so we know we aren't accidentally being used for constant init.
        this.constantHashCode = isBootstrapOnly ? Integer.MIN_VALUE : -1;
        install();
    }

    private void install() {
        InstrumentationHelpers.attachThread(this);
    }
    private void remove() {
        InstrumentationHelpers.detachThread(this);
    }

    @Override
    public void chargeEnergy(long cost) throws OutOfEnergyException {
        // Free!
    }

    @Override
    public long energyLeft() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }

    @Override
    public <T> Class<T> wrapAsClass(java.lang.Class<T> input) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }

    @Override
    public int getNextHashCodeAndIncrement() {
        return this.constantHashCode;
    }

    @Override
    public void bootstrapOnly() {
        if (!this.isBootstrapOnly) {
            throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
        }
    }

    @Override
    public void setAbortState() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void clearAbortState() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void enterNewFrame(ClassLoader contractLoader, long energyLeft, int nextHashCode) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void exitCurrentFrame() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public int getCurStackSize() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public int getCurStackDepth() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void enterMethod(int frameSize) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void exitMethod(int frameSize) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void enterCatchBlock(int depth, int size) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public int peekNextHashCode() {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
    @Override
    public void forceNextHashCode(int nextHashCode) {
        throw RuntimeAssertionError.unreachable("Shouldn't be called in the testing code");
    }
}
