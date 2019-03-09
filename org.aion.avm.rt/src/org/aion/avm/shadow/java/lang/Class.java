package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class Class<T> extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public String avm_getName() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_getName);
        // Note that we actively try not to give the same instance of the name wrapper back (since the user could see implementation details of our
        // contract life-cycle or the underlying JVM/ClassLoader.
        return new org.aion.avm.shadow.java.lang.String(v.getName());
    }

    public String avm_toString() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_toString);
        return null;
    }

    public IObject avm_cast(IObject obj) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_cast);
        return (IObject)this.v.cast(obj);
    }

    public java.lang.Class<T> getRealClass(){return this.v;}

    @SuppressWarnings("unchecked")
    public Class<T> avm_getSuperclass() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_getSuperclass);
        // Note that we need to return null if the underlying is the shadow object root.
        Class<T> toReturn = null;
        if (org.aion.avm.shadow.java.lang.Object.class != this.v) {
            toReturn = (Class<T>) IInstrumentation.attachedThreadInstrumentation.get().wrapAsClass(this.v.getSuperclass());
        }
        return toReturn;
    }

    public boolean avm_desiredAssertionStatus() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_desiredAssertionStatus);
        // Note that we currently handle assertions as always-enabled.
        // Internally, these will result in throwing AssertionError which, unless caught by the user's code, results in a FAILED_EXCEPTION status.
        // See issue-72 for more details on our thought process and future interpretations of this we may want to entertain.
        return true;
    }

    //=======================================================
    // Methods below are used by Enum
    //========================================================
    Map<String, T> enumConstantDirectory() {
        Map<String, T> directory = enumConstantDirectory;
        if (directory == null) {
            ObjectArray universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                        avm_getName() + " is not an enum type");
            directory = new HashMap<>(2 * universe.length());
            for (int i = 0; i < universe.length(); i++){
                @SuppressWarnings("unchecked")
                T constant = (T) universe.get(i);
                directory.put(((Enum<?>)constant).avm_name(), constant);
            }
            enumConstantDirectory = directory;
        }
        return directory;
    }
    private transient volatile Map<String, T> enumConstantDirectory;



    ObjectArray getEnumConstantsShared() {
        ObjectArray constants = enumConstants;
        if (constants == null) {
            try {
                Method m = v.getDeclaredMethod("avm_values");
                java.lang.Object value = m.invoke(null);

                ObjectArray temporaryConstants = (ObjectArray) value;
                enumConstants = constants = temporaryConstants;
            }
            // These can happen when users concoct enum-like classes
            // that don't comply with the enum spec.
            catch (NoSuchMethodException |
                    IllegalAccessException |
                    InvocationTargetException ex) {
                java.lang.System.out.println(ex.toString());
                return null; }
        }
        return constants;
    }
    private transient volatile ObjectArray enumConstants;


    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Class(java.lang.Class<T> v) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Class_avm_constructor);
        this.v = v;
    }

    private final java.lang.Class<T> v;

    @Override
    public java.lang.String toString() {
        return this.v.toString();
    }
}
