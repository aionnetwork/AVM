package org.aion.avm.shadowapi.avm;

import java.lang.invoke.MethodHandle;

import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RevertException;


public final class InternalRunnable extends org.aion.avm.shadow.java.lang.Object implements org.aion.avm.shadow.java.lang.Runnable {
    public static InternalRunnable createRunnable(MethodHandle target) {
        return new InternalRunnable(target);
    }


    private final MethodHandle target;

    private InternalRunnable(MethodHandle target) {
        // We call the hidden super-class so this doesn't update our hash code.
        super(null, null, 0);
        this.target = target;
    }

    // Deserializer support.
    // TODO(AKI-131): Implement serialization support for lambdas.
    public InternalRunnable(java.lang.Void ignore, int readIndex) {
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
    public void avm_run() {
        try {
            target.invokeExact();
        } catch (Throwable e) {
            // We will treat a failure here as something fatal.
            e.printStackTrace();
            throw new RevertException();
        }
    }

    public org.aion.avm.shadow.java.lang.Runnable self() {
        return this;
    }
}
