package org.aion.avm.java.lang;

/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object {

    public Object() {

    }

    // getClass() is skipped

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object obj) {
        return false;
    }

    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return null;
    }

    // notify(), notifyAll(), wait() and wait(long) are skipped

    // finalize() is skipped
}
