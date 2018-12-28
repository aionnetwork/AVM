package org.aion.avm.internal;


/**
 * Used by ABIEncoder and ABIDecoder to convert between internal/external perspectives on ABI types.
 * An implementation of this is statically loaded into ABIEncoder so that the core ABI implementation can access
 * these external types in a high-level way while remaining internally generic.
 * 
 * Note that this is split into "standard" and "shadow" type terminology:
 * -standard:  The Java classes of the ABI-supported types
 * -shadow:  An implementation-defined aliasing space for these types
 * 
 * Additionally, shadow types are split between "concrete" and "binding":
 * -concrete:  The actual type of a concrete instance of this type
 * -binding:  The optionally more generic version of this type which is suitable for method type binding
 */
public interface IABISupport {
    /**
     * Converts a method name into the name it has in the shadow space.
     * 
     * @param original The original method name.
     * @return The shadow version of the method name.
     */
    String convertToShadowMethodName(String original);

    /**
     * Converts the object from a standard instance to a shadow instance.
     * 
     * @param standardValue The object as a standard type.
     * @return The shadow object.
     */
    Object convertToShadowValue(Object standardValue);

    /**
     * Converts the object from a shadow instance to a standard instance.
     * 
     * @param shadowValue The object as a shadow type.
     * @return The standard object.
     */
    Object convertToStandardValue(Object shadowValue);

    /**
     * Converts the given standard type into its concrete shadow type.
     * This is used for looking up instantiation types, for example.
     * 
     * @param standardType The standard type.
     * @return The concrete shadow type.
     */
    Class<?> convertToConcreteShadowType(Class<?> standardType);

    /**
     * Converts the given standard type into its binding shadow type.
     * This is used for looking up method parameter types, for example.
     * 
     * @param standardType The standard type.
     * @return The binding shadow type.
     */
    Class<?> convertToBindingShadowType(Class<?> standardType);

    /**
     * Converts the given concrete shadow type to its standard type.
     * 
     * @param shadowType The concrete shadow type.
     * @return The standard type.
     */
    Class<?> convertConcreteShadowToStandardType(Class<?> shadowType);

    /**
     * Converts the given binding shadow type to the corresponding concrete shadow type.
     * 
     * @param bindingShadowType The binding shadow type.
     * @return The concrete shadow type.
     */
    Class<?> mapFromBindingTypeToConcreteType(Class<?> bindingShadowType);
}
