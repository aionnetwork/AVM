package org.aion.avm.core.persistence;


public class TargetRoot {
    public static TargetRoot root;
    public final int readIndex;
    public int counter;
    public TargetRoot next;
    
    public TargetRoot() {
        this.readIndex = -1;
    }
    public TargetRoot(Void ignore, int readIndex) {
        this.readIndex = readIndex;
    }
    public void serializeSelf(Class<?> stopBefore, ByteBufferObjectSerializer serializer) {
        serializer.writeInt(this.counter);
        serializer.writeObject(this.next);
        serializer.automaticallySerializeToRoot((null == stopBefore) ? TargetRoot.class : stopBefore, this);
    }
    
    public void deserializeSelf(Class<?> stopBefore, ByteBufferObjectDeserializer deserializer) {
        this.counter = deserializer.readInt();
        this.next = (TargetRoot) deserializer.readObject();
        deserializer.automaticallyDeserializeFromRoot((null == stopBefore) ? TargetRoot.class : stopBefore, this);
    }
}
