package org.aion.avm.shadowapi.avm;

import java.lang.invoke.MethodHandle;

import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RevertException;


public final class InternalFunction extends org.aion.avm.shadow.java.lang.Object implements org.aion.avm.shadow.java.util.function.Function {
    public static InternalFunction createFunction(MethodHandle target) {
        return new InternalFunction(target);
    }


    private final MethodHandle target;

    private InternalFunction(MethodHandle target) {
        // We call the hidden super-class so this doesn't update our hash code.
        super(null, null, 0);
        this.target = target;
    }

    // Deserializer support.
    // TODO(AKI-131): Implement serialization support for lambdas.
    public InternalFunction(java.lang.Void ignore, int readIndex) {
        super(ignore, readIndex);
        this.target = null;
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        // TODO(AKI-131): Implement serialization support for lambdas.
        throw new OutOfEnergyException();
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        // TODO(AKI-131): Implement serialization support for lambdas.
        throw new OutOfEnergyException();
    }

    @Override
    public org.aion.avm.internal.IObject avm_apply(org.aion.avm.internal.IObject input) {
        try {
            // NOTE:  We can't use "invokeExact" since the method we are calling is usually more precise than IObject->IObject.
            return (org.aion.avm.internal.IObject) target.invoke(input);
        } catch (Throwable e) {
            // We will treat a failure here as something fatal.
            e.printStackTrace();
            throw new RevertException();
        }
    }

    public org.aion.avm.shadow.java.util.function.Function self() {
        return this;
    }
}
