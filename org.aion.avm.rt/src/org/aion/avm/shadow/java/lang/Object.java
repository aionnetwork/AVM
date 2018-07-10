package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object implements IObject {
    private final int hashCode;
    // Note that isLoaded and instanceId are not used yet but here to test ReflectionStructureCodec.
    @SuppressWarnings("unused")
    private boolean isLoaded;
    @SuppressWarnings("unused")
    private long instanceId;

    public Object() {
        this.hashCode = IHelper.currentContractHelper.get().externalGetNextHashCode();
        this.isLoaded = true;
        this.instanceId = 0l;
    }

    @Override
    public Class<?> avm_getClass() {
        return null;
    }

    @Override
    public int avm_hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean avm_equals(IObject obj) {
        return false;
    }

    protected IObject avm_clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String avm_toString() {
        return null;
    }

    public final void avm_notify() {

    }

    public final void avm_notifyAll() {

    }

    public final void avm_wait() throws InterruptedException {

    }

    public final void avm_wait​(long timeout) throws InterruptedException {

    }

    public final void avm_wait​(long timeout, int nanos) throws InterruptedException {

    }

    protected void avm_finalize() throws java.lang.Throwable {

    }

    @Override
    public int hashCode() {
        // NOTE:  This is not called in normal operation but is called in cases where we run a contract without transformation.
        return avm_hashCode();
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        // NOTE:  This is not called in normal operation but is called in cases where we run a contract without transformation.
        return (obj instanceof IObject)
                ? avm_equals((IObject)obj)
                : false;
    }
}
