package org.aion.avm.core.persistence;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;


public class TargetRoot {
    public static TargetRoot root;
    public final int readIndex;
    public int counter;
    public TargetRoot next;
    
    public TargetRoot() {
        this.readIndex = -1;
    }
    // Temporarily use IDeserializer and IPersistenceToken to reduce the scope of this commit.
    public TargetRoot(IDeserializer ignore, IPersistenceToken readIndex) {
        this.readIndex = readIndex.readIndex;
    }
    public void serializeSelf(Class<?> stopBefore, IObjectSerializer serializer) {
        serializer.writeInt(this.counter);
        serializer.writeObject(this.next);
        serializer.automaticallySerializeToRoot((null == stopBefore) ? TargetRoot.class : stopBefore, this);
    }
    
    public void deserializeSelf(Class<?> stopBefore, IObjectDeserializer deserializer) {
        this.counter = deserializer.readInt();
        this.next = (TargetRoot) deserializer.readObject();
        deserializer.automaticallyDeserializeFromRoot((null == stopBefore) ? TargetRoot.class : stopBefore, this);
    }
}
