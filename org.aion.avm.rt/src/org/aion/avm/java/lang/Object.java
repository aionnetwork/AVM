package org.aion.avm.java.lang;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object implements IObject {
    private final int hashCode;

    public Object() {
        this.hashCode = IHelper.currentContractHelper.get().externalGetNextHashCode();
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
    public boolean avm_equals(Object obj) {
        return false;
    }

    protected Object avm_clone() throws CloneNotSupportedException {
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
}
