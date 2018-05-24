package org.aion.avm.core.classgeneration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aion.avm.internal.PackageConstants;


/**
 * Contains some of the common constants and code-generation idioms used in various tests and/or across the system, in general.
 */
public class CommonGenerators {
    public static final String kShadowClassLibraryPrefix = PackageConstants.kTopLevelDotPrefix;
    public static final String kWrapperClassLibraryPrefix = PackageConstants.kExceptionWrapperDotPrefix;
    public static final String kSlashWrapperClassLibraryPrefix = kWrapperClassLibraryPrefix.replaceAll("\\.", "/");

    // There doesn't appear to be any way to enumerate these classes in the existing class loader (even though they are part of java.lang)
    // so we will list the names of all the classes we need and assemble them that way.
    // We should at least be able to use the original Throwable's classloader to look up the subclasses (again, since they are in java.lang).
    // TODO:  A few of these actually need to be hand-coded (see issue-39).
    public static final String[] kExceptionClassNames = new String[] {
            "java.lang.Error",
            "java.lang.AssertionError",
            "java.lang.LinkageError",
            "java.lang.BootstrapMethodError",
            "java.lang.ClassCircularityError",
            "java.lang.ClassFormatError",
            "java.lang.UnsupportedClassVersionError",
            "java.lang.ExceptionInInitializerError",
            "java.lang.IncompatibleClassChangeError",
            "java.lang.AbstractMethodError",
            "java.lang.IllegalAccessError",
            "java.lang.InstantiationError",
            "java.lang.NoSuchFieldError",
            "java.lang.NoSuchMethodError",
            "java.lang.NoClassDefFoundError",
            "java.lang.UnsatisfiedLinkError",
            "java.lang.VerifyError",
            "java.lang.ThreadDeath",
            "java.lang.VirtualMachineError",
            "java.lang.InternalError",
            "java.lang.OutOfMemoryError",
            "java.lang.StackOverflowError",
            "java.lang.UnknownError",
            "java.lang.Exception",
            "java.lang.CloneNotSupportedException",
            "java.lang.InterruptedException",
            "java.lang.ReflectiveOperationException",
            "java.lang.ClassNotFoundException",
            "java.lang.IllegalAccessException",
            "java.lang.InstantiationException",
            "java.lang.NoSuchFieldException",
            "java.lang.NoSuchMethodException",
            "java.lang.RuntimeException",
            "java.lang.ArithmeticException",
            "java.lang.ArrayStoreException",
            "java.lang.ClassCastException",
            "java.lang.EnumConstantNotPresentException",
            "java.lang.IllegalArgumentException",
            "java.lang.IllegalThreadStateException",
            "java.lang.NumberFormatException",
            "java.lang.IllegalCallerException",
            "java.lang.IllegalMonitorStateException",
            "java.lang.IllegalStateException",
            "java.lang.IndexOutOfBoundsException",
            "java.lang.ArrayIndexOutOfBoundsException",
            "java.lang.StringIndexOutOfBoundsException",
            "java.lang.LayerInstantiationException",
            "java.lang.NegativeArraySizeException",
            "java.lang.NullPointerException",
            "java.lang.SecurityException",
            "java.lang.TypeNotPresentException",
            "java.lang.UnsupportedOperationException",
    };

    // We don't generate the shadows for these ones since we have hand-written them (but wrappers are still required).
    public static final Set<String> kHandWrittenExceptionClassNames = Set.of(new String[] {
            "java.lang.Exception",
            "java.lang.RuntimeException",
            "java.lang.EnumConstantNotPresentException",
            "java.lang.TypeNotPresentException",
    });

    // We generate "legacy-style exception" shadows for these ones (and wrappers are still required).
    public static final Set<String> kLegacyExceptionClassNames = Set.of(new String[] {
            "java.lang.ExceptionInInitializerError",
            "java.lang.ClassNotFoundException",
    });

    public static Map<String, byte[]> generateExceptionShadowsAndWrappers() throws Exception {
        Map<String, byte[]> generatedClasses = new HashMap<>();
        for (String className : kExceptionClassNames) {
            // We need to look this up to find the superclass.
            String superclassName = Class.forName(className).getSuperclass().getName();
            
            // Generate the shadow.
            if (!kHandWrittenExceptionClassNames.contains(className)) {
                String shadowName = kShadowClassLibraryPrefix + className;
                String shadowSuperName = kShadowClassLibraryPrefix + superclassName;
                byte[] shadowBytes = null;
                if (kLegacyExceptionClassNames.contains(className)) {
                    // "Legacy" exception.
                    shadowBytes = generateLegacyExceptionClass(shadowName, shadowSuperName);
                } else {
                    // "Standard" exception.
                    shadowBytes = generateExceptionClass(shadowName, shadowSuperName);
                }
                
                generatedClasses.put(shadowName, shadowBytes);
            }
            
            // Generate the wrapper.
            String wrapperName = kWrapperClassLibraryPrefix + className;
            String wrapperSuperName = kWrapperClassLibraryPrefix + superclassName;
            byte[] wrapperBytes = generateWrapperClass(wrapperName, wrapperSuperName);
            generatedClasses.put(wrapperName, wrapperBytes);
        }
        return generatedClasses;
    }

    private static byte[] generateWrapperClass(String mappedName, String mappedSuperName) {
        String slashName = mappedName.replaceAll("\\.", "/");
        String superSlashName = mappedSuperName.replaceAll("\\.", "/");
        return StubGenerator.generateWrapperClass(slashName, superSlashName);
    }

    private static byte[] generateExceptionClass(String mappedName, String mappedSuperName) {
        String slashName = mappedName.replaceAll("\\.", "/");
        String superSlashName = mappedSuperName.replaceAll("\\.", "/");
        return StubGenerator.generateExceptionClass(slashName, superSlashName);
    }

    private static byte[] generateLegacyExceptionClass(String mappedName, String mappedSuperName) {
        String slashName = mappedName.replaceAll("\\.", "/");
        String superSlashName = mappedSuperName.replaceAll("\\.", "/");
        return StubGenerator.generateLegacyExceptionClass(slashName, superSlashName);
    }
}
