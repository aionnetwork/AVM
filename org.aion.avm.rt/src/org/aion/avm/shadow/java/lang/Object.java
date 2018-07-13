package org.aion.avm.shadow.java.lang;

import java.util.function.Consumer;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;


/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object implements IObject {
    private int hashCode;
    public long instanceId;
    // We hold on to this deserializer until we need to load the instance (this is cleared after lazyLoad() completes).
    private IDeserializer deserializer;

    public Object() {
        this.hashCode = IHelper.currentContractHelper.get().externalGetNextHashCode();
        this.instanceId = 0l;
    }

    // Special constructor only invoked when instantiating this as an instance stub.
    public Object(IDeserializer deserializer, long instanceId) {
        this.instanceId = instanceId;
        this.deserializer = deserializer;
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

    /**
     * The call which causes this instance to become loaded.
     * Note that this is final since the protected "deserializeSelf" should be over-ridden.
     */
    public final void lazyLoad() {
        if (null != this.deserializer) {
            // Tell the deserializer to invoke the deserialization pipeline (which may call back to us).
            this.deserializer.startDeserializeInstance(this, this.instanceId);
            this.deserializer = null;
        }
    }

    public void deserializeSelf(IObjectDeserializer deserializer) {
        // We only operate on our hashCode.
        this.hashCode = deserializer.readInt();
        
        // TODO:  In the future, this is where we need to hook into the automatic deserialization mechanism.
    }

    public void serializeSelf(IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        // We only operate on our hashCode.
        serializer.writeInt(this.hashCode);
        
        // TODO:  In the future, this is where we need to hook into the automatic serialization mechanism.
    }
}
