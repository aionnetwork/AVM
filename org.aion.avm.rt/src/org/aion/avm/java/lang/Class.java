package org.aion.avm.java.lang;

import org.aion.avm.internal.IHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class Class<T> extends Object {
    private final java.lang.Class<T> underlying;

    public Class(java.lang.Class<T> underlying) {
        this.underlying = underlying;
    }

    public String avm_getName() {
        // Note that the class name is a constant so use the wrapper which will intern the instance.
        return IHelper.currentContractHelper.get().externalWrapAsString(underlying.getName());
    }

    public String avm_toString() {
        return null;
    }

    public java.lang.Class<T> getRealClass(){return this.underlying;}

    public Class<T> getSuperclass(){return new Class(this.underlying.getSuperclass());}

    //=======================================================
    // Methods below are used by org.aion.avm.java.lang.Enum
    //========================================================
    Map<String, T> enumConstantDirectory() {
        Map<String, T> directory = enumConstantDirectory;
        if (directory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                        avm_getName() + " is not an enum type");
            directory = new HashMap<>(2 * universe.length);
            for (T constant : universe) {
                directory.put(((Enum<?>)constant).avm_name(), constant);
            }
            enumConstantDirectory = directory;
        }
        return directory;
    }
    private transient volatile Map<String, T> enumConstantDirectory;



    T[] getEnumConstantsShared() {
        T[] constants = enumConstants;
        if (constants == null) {
            try {
                Field f = underlying.getDeclaredField("$VALUES");
                f.setAccessible(true);

                java.lang.Object value = f.get(underlying);
                T[] temporaryConstants = (T[]) value;
                enumConstants = constants = temporaryConstants;
            }
            // These can happen when users concoct enum-like classes
            // that don't comply with the enum spec.
            catch (NoSuchFieldException |
                    IllegalAccessException ex) {
                System.out.println(ex.toString());
                return null; }
        }
        return constants;
    }
    private transient volatile T[] enumConstants;


    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================



    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}
