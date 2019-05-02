package i;


/**
 * While most of the package constants (prefixes for our various namespaces, etc) are only used directly within the core module,
 * sometimes the Helper needs to know about them, as they represent part of the agreement between these 2 modules (for
 * instantiation, etc).
 */
public class PackageConstants {
    public static final String kShadowDotPrefix = "s.";
    public static final String kShadowApiDotPrefix = "p.";
    public static final String kExceptionWrapperDotPrefix = "org.aion.avm.exceptionwrapper.";
    public static final String kArrayWrapperDotPrefix = "org.aion.avm.arraywrapper.";
    public static final String kArrayWrapperUnifyingDotPrefix = "org.aion.avm.arraywrapper.interface.";
    public static final String kInternalDotPrefix = "i.";
    public static final String kUserDotPrefix = "org.aion.avm.user.";
    public static final String kPublicApiDotPrefix = "avm.";

    public static final String kShadowSlashPrefix = "s/";
    public static final String kShadowApiSlashPrefix = "p/";
    public static final String kExceptionWrapperSlashPrefix = "org/aion/avm/exceptionwrapper/";
    public static final String kArrayWrapperSlashPrefix = "org/aion/avm/arraywrapper/";
    public static final String kArrayWrapperUnifyingSlashPrefix = "org/aion/avm/arraywrapper/interface/";
    public static final String kInternalSlashPrefix = "i/";
    public static final String kUserSlashPrefix = "org/aion/avm/user/";
    public static final String kPublicApiSlashPrefix = "avm/";

    public static final String kConstantClassName = "C";
}
