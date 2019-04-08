package org.aion.avm.shadow.java.lang;

import org.aion.avm.ClassNameExtractor;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * The shadow implementation of the {@link java.lang.Object}.
 */
public class Object extends java.lang.Object implements IObject {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    private int hashCode;

    public IPersistenceToken persistenceToken;

    // We hold on to this deserializer until we need to load the instance (this is cleared after lazyLoad() completes).
    private IDeserializer deserializer;

    public Object() {
        this.hashCode = IInstrumentation.attachedThreadInstrumentation.get().getNextHashCodeAndIncrement();
        this.persistenceToken = null;
    }

    // Special constructor only invoked when instantiating this as an instance stub.
    public Object(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        this.persistenceToken = persistenceToken;
        this.deserializer = deserializer;
    }

    // We created this special allocator for cases where we don't want to change the hashcode (Classes, for example).
    protected Object(java.lang.Void ignore, java.lang.Void ignore2, int hashCode) {
        this.hashCode = hashCode;
    }
 
    public void updateHashCodeForConstant(int hashCode) {
        // Note that this can only be called for constants and their hash codes are initialized by a special IInstrumentation, under NodeEnvironment, which gives them
        // this MIN_VALUE hash code so we can detect this case.
        RuntimeAssertionError.assertTrue(java.lang.Integer.MIN_VALUE == this.hashCode);
        this.hashCode = hashCode;
    }

    /**
     * This exists purely for serialization of INode instances for the delta hash.
     * 
     * @return The identity hash of the instance.
     */
    public int getIdentityHashCode() {
        return this.hashCode;
    }

    @Override
    public Class<?> avm_getClass() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Object_avm_getClass);
        return IInstrumentation.attachedThreadInstrumentation.get().wrapAsClass(this.getClass());
    }

    @Override
    public int avm_hashCode() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Object_avm_hashCode);
        return internalHashcode();
    }

    @Override
    public boolean avm_equals(IObject obj) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Object_avm_equals);
        // By default, we are only instance-equal.
        return (this == obj);
    }

    protected IObject avm_clone() throws CloneNotSupportedException {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Object_avm_clone);
        throw new CloneNotSupportedException();
    }

    @Override
    public String avm_toString() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Object_avm_toString);
        //using the public facing method since the user can override this
        return new String(ClassNameExtractor.getOriginalClassName(getClass().getName()) + "@" + java.lang.Integer.toHexString(avm_hashCode()));
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

    public int internalHashcode(){
        lazyLoad();
        return this.hashCode;
    }
}
