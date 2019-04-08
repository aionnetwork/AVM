package org.aion.avm.core.persistence;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;


public class TargetLeaf extends TargetRoot {
    public static double D;
    public TargetRoot left;
    public TargetRoot right;
    public TargetLeaf() {
    }
    // Temporarily use IDeserializer and IPersistenceToken to reduce the scope of this commit.
    public TargetLeaf(IDeserializer ignore, IPersistenceToken readIndex) {
        super(ignore, readIndex);
    }
}
