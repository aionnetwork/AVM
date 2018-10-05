package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object implements IObject {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private int hashCode;

    public IPersistenceToken persistenceToken;

    // We hold on to this deserializer until we need to load the instance (this is cleared after lazyLoad() completes).
    private IDeserializer deserializer;

    public Object() {
        this.hashCode = IHelper.currentContractHelper.get().externalGetNextHashCode();
        this.persistenceToken = null;
    }

    // Special constructor only invoked when instantiating this as an instance stub.
    public Object(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        this.persistenceToken = persistenceToken;
        this.deserializer = deserializer;
    }

    @Override
    public Class<?> avm_getClass() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_getClass);
        return IHelper.currentContractHelper.get().externalWrapAsClass(this.getClass());
    }

    @Override
    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_hashCode);
        lazyLoad();
        return this.hashCode;
    }

    @Override
    public boolean avm_equals(IObject obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_equals);
        // By default, we are only instance-equal.
        return (this == obj);
    }

    protected IObject avm_clone() throws CloneNotSupportedException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_clone);
        throw new CloneNotSupportedException();
    }

    @Override
    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_toString);
        return null;
    }

    public final void avm_notify() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_notify);
    }

    public final void avm_notifyAll() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_notifyAll);
    }

    public final void avm_wait() throws InterruptedException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_wait);
    }

    public final void avm_wait​(long timeout) throws InterruptedException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_wait_1);
    }

    public final void avm_wait​(long timeout, int nanos) throws InterruptedException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_wait_2);
    }

    protected void avm_finalize() throws java.lang.Throwable {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Object_avm_finalize);
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
            // Clear the deserializer instance variable before we perform the deserialization (since it might want to install a new one).
            IDeserializer existingDeserializer = this.deserializer;
            this.deserializer = null;
            // Tell the deserializer to invoke the deserialization pipeline (which may call back to us).
            existingDeserializer.startDeserializeInstance(this, this.persistenceToken);
        }
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        // We only operate on our hashCode.
        this.hashCode = deserializer.readInt();
        
        // NOTE:  It would probably be a better design to special-case the handling of the hashCode, in the automatic implementation,
        // since this otherwise means that we have a "real" implementation which we pretend is not "real" so we can automatically
        // deserialize our sub-class.  This should improve performance, though.
        deserializer.beginDeserializingAutomatically(this, firstRealImplementation);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        // We only operate on our hashCode.
        serializer.writeInt(this.hashCode);
        
        // NOTE:  It would probably be a better design to special-case the handling of the hashCode, in the automatic implementation,
        // since this otherwise means that we have a "real" implementation which we pretend is not "real" so we can automatically
        // serialize our sub-class.  This should improve performance, though.
        serializer.beginSerializingAutomatically(this, firstRealImplementation);
    }
}
