package org.aion.avm.core.util;

import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.internal.IABISupport;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Provides generated array instantiation for the ABIEncoder, in the runtime package.
 * Note that this knows about the name/shape of generated array classes so we might want to fold this
 * support into a class which already has those assumptions.
 */
public class GeneratedClassesFactory implements IABISupport {
    private final AvmSharedClassLoader classLoader;

    public GeneratedClassesFactory(AvmSharedClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String convertToShadowMethodName(String original) {
        return "avm_" + original;
    }

    @Override
    public Object convertToStandardValue(Object shadowValue) {
        String shadowClassName = shadowValue.getClass().getName();
        return ShadowTypeBridge.FROM_CONCRETE_SHADOW_CLASS_NAME.get(shadowClassName).convertToStandardValue((IObject)shadowValue);
    }

    @Override
    public Object convertToShadowValue(Object standardValue) {
        Object shadowValue = null;
        if (null != standardValue) {
            Class<?> standardClass = standardValue.getClass();
            try {
                shadowValue = ShadowTypeBridge.FROM_STANDARD_CLASS.get(standardClass).convertToConcreteShadowValue(this.classLoader, standardValue);
            } catch (Exception e) {
                // This means that we didn't correctly filter the type at some other level (or that the installation is just missing shadow JCL components).
                throw RuntimeAssertionError.unexpected(e);
            }
        }
        return shadowValue;
    }

    @Override
    public Class<?> convertConcreteShadowToStandardType(Class<?> shadowType) {
        String shadowClassName = shadowType.getName();
        return ShadowTypeBridge.FROM_CONCRETE_SHADOW_CLASS_NAME.get(shadowClassName).standardClass;
    }

    @Override
    public Class<?> convertToConcreteShadowType(Class<?> standardType) {
        String shadowClassName = ShadowTypeBridge.FROM_STANDARD_CLASS.get(standardType).concreteShadowClassName;
        try {
            return this.classLoader.loadClass(shadowClassName);
        } catch (ClassNotFoundException e) {
            // This means that we didn't correctly filter the type at some other level (or that the installation is just missing shadow JCL components).
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public Class<?> convertToBindingShadowType(Class<?> standardType) {
        String shadowClassName = ShadowTypeBridge.FROM_STANDARD_CLASS.get(standardType).bindingShadowClassName;
        try {
            return this.classLoader.loadClass(shadowClassName);
        } catch (ClassNotFoundException e) {
            // This means that we didn't correctly filter the type at some other level (or that the installation is just missing shadow JCL components).
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public Class<?> mapFromBindingTypeToConcreteType(Class<?> bindingShadowType) {
        String shadowClassName = ShadowTypeBridge.FROM_BINDING_SHADOW_CLASS_NAME.get(bindingShadowType.getName()).concreteShadowClassName;
        try {
            return this.classLoader.loadClass(shadowClassName);
        } catch (ClassNotFoundException e) {
            // This means that we didn't correctly filter the type at some other level (or that the installation is just missing shadow JCL components).
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}
