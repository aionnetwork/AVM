package org.aion.avm.java.lang;

/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object {

    public Object() {

    }

    Class<?> avm_getClass() {
        return null;
    }

    public int avm_hashCode() {
        return 0;
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
