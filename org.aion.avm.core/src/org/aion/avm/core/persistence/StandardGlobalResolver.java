package org.aion.avm.core.persistence;

import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.InternedClasses;
import org.aion.avm.internal.RuntimeAssertionError;


public class StandardGlobalResolver implements IGlobalResolver {
    private final InternedClasses internedClassMap;
    private final ClassLoader classLoader;

    public StandardGlobalResolver(InternedClasses internedClassMap, ClassLoader classLoader) {
        this.internedClassMap = internedClassMap;
        this.classLoader = classLoader;
    }

    @Override
    public String getAsInternalClassName(Object target) {
        return (target instanceof org.aion.avm.shadow.java.lang.Class)
                ? ((org.aion.avm.shadow.java.lang.Class<?>)target).getRealClass().getName()
                : null;
    }

    @Override
    public Object getClassObjectForInternalName(String internalClassName) {
        try {
            Class<?> underlyingClass = this.classLoader.loadClass(internalClassName);
            org.aion.avm.shadow.java.lang.Class<?> internedClass = this.internedClassMap.get(underlyingClass);
            return internedClass;
        } catch (ClassNotFoundException e) {
            // This can only fail if we were given the wrong loader.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public int getAsConstant(Object target) {
        // TODO (AKI-97): Replace this with an inline constant readIndex value.
        int constant = 0;
        for (Map.Entry<Integer, org.aion.avm.shadow.java.lang.Object> elt : NodeEnvironment.singleton.getConstantMap().entrySet()) {
            if (elt.getValue() == target) {
                constant = elt.getKey();
                break;
            }
        }
        return constant;
    }

    @Override
    public Object getConstantForIdentifier(int constantIdentifier) {
        return NodeEnvironment.singleton.getConstantMap().get(constantIdentifier);
    }
}
