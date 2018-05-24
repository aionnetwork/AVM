package org.aion.avm.internal;


/**
 * While most of the package constants (prefixes for our various namespaces, etc) are only used directly within the core module,
 * sometimes the Helper needs to know about them, as they represent part of the agreement between these 2 modules (for
 * instantiation, etc).
 */
public class PackageConstants {
    public static final String kTopLevelDotPrefix = "org.aion.avm.";
    public static final String kExceptionWrapperDotPrefix = "org.aion.avm.exceptionwrapper.";

}
