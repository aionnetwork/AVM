package org.aion.avm.internal;


/**
 * Passed to serializeSelf() so that the receiver can abstractly serialize itself.
 * Note that there is no identification of data elements, other than the order they are written.
 */
public interface IObjectSerializer {
    void writeInt(int value);
}
