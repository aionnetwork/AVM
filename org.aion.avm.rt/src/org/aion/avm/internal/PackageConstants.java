package org.aion.avm.internal;


/**
 * While most of the package constants (prefixes for our various namespaces, etc) are only used directly within the core module,
 * sometimes the Helper needs to know about them, as they represent part of the agreement between these 2 modules (for
 * instantiation, etc).
 */
public class PackageConstants {
    public static final String kShadowDotPrefix = "org.aion.avm.shadow.";
    public static final String kShadowApiDotPrefix = "org.aion.avm.shadowapi.";
    public static final String kExceptionWrapperDotPrefix = "org.aion.avm.exceptionwrapper.";
    public static final String kArrayWrapperDotPrefix = "org.aion.avm.arraywrapper.";
    public static final String kArrayWrapperUnifyingDotPrefix = "org.aion.avm.arraywrapper.interface.";
    public static final String kInternalDotPrefix = "org.aion.avm.internal.";
    public static final String kUserDotPrefix = "org.aion.avm.user.";
    public static final String kPublicApiDotPrefix = "avm.";

    public static final String kShadowSlashPrefix = "org/aion/avm/shadow/";
    public static final String kShadowApiSlashPrefix = "org/aion/avm/shadowapi/";
    public static final String kExceptionWrapperSlashPrefix = "org/aion/avm/exceptionwrapper/";
    public static final String kArrayWrapperSlashPrefix = "org/aion/avm/arraywrapper/";
    public static final String kArrayWrapperUnifyingSlashPrefix = "org/aion/avm/arraywrapper/interface/";
    public static final String kInternalSlashPrefix = "org/aion/avm/internal/";
    public static final String kUserSlashPrefix = "org/aion/avm/user/";
    public static final String kPublicApiSlashPrefix = "avm/";

}
