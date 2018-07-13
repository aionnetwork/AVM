package org.aion.avm.internal;


/**
 * Passed to deserializeSelf() so that the receiver can abstractly deserialize itself.
 * Note that there is no identification of data elements, other than the order they are read.
 */
public interface IObjectDeserializer {
    int readInt();
}
