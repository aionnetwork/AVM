package org.aion.avm.java.lang;

import org.aion.avm.internal.Helper;


/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object {
    private final int hashCode;

    public Object() {
        this.hashCode = Helper.getNextHashCode();
    }

    Class<?> avm_getClass() {
        return null;
    }

    public int avm_hashCode() {
        return this.hashCode;
    }

    public boolean avm_equals(Object obj) {
        return false;
    }

    protected Object avm_clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

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
