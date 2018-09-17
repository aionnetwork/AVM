package org.aion.avm.core.classgeneration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


/**
 * Contains some of the common constants and code-generation idioms used in various tests and/or across the system, in general.
 */
public class CommonGenerators {
    // There doesn't appear to be any way to enumerate these classes in the existing class loader (even though they are part of java.lang)
    // so we will list the names of all the classes we need and assemble them that way.
    // We should at least be able to use the original Throwable's classloader to look up the subclasses (again, since they are in java.lang).
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

            "java.util.NoSuchElementException",
            "java.nio.BufferUnderflowException",
            "java.nio.BufferOverflowException"
    };

    // We don't generate the shadows for these ones since we have hand-written them (but wrappers are still required).
    public static final Set<String> kHandWrittenExceptionClassNames = Set.of(new String[] {
            "java.lang.Error",
            "java.lang.AssertionError",
            "java.lang.Exception",
            "java.lang.RuntimeException",
            "java.lang.EnumConstantNotPresentException",
            "java.lang.TypeNotPresentException",

            "java.util.NoSuchElementException",
    });

    // We generate "legacy-style exception" shadows for these ones (and wrappers are still required).
    public static final Set<String> kLegacyExceptionClassNames = Set.of(new String[] {
            "java.lang.ExceptionInInitializerError",
            "java.lang.ClassNotFoundException",
    });

    public static final Set<String> kShadowEnumClassNames = Set.of(new String[] {
            PackageConstants.kShadowDotPrefix + "java.math.RoundingMode",
    });

    // Record the parent class of each generated class. This information is needed by the heap size calculation.
    // Both class names are in the shadowed version.
    public static Map<String, String> parentClassMap;

    public static Map<String, byte[]> generateShadowJDK() {
        Map<String, byte[]> shadowJDK = new HashMap<>();

        Map<String, byte[]> shadowException = generateShadowException();
        //Map<String, byte[]> shadowEnum = generateShadowEnum();

        //shadowJDK.putAll(shadowEnum);
        shadowJDK.putAll(shadowException);

        return shadowJDK;
    }

    public static Map<String, byte[]> generateShadowException() {
        Map<String, byte[]> generatedClasses = new HashMap<>();
        parentClassMap = new HashMap<>();
        for (String className : kExceptionClassNames) {
            // We need to look this up to find the superclass.
            String superclassName = null;
            try {
                superclassName = Class.forName(className).getSuperclass().getName();
            } catch (ClassNotFoundException e) {
                // We are operating on built-in exception classes so, if these are missing, there is something wrong with the JDK.
                throw RuntimeAssertionError.unexpected(e);
            }
            
            // Generate the shadow.
            if (!kHandWrittenExceptionClassNames.contains(className)) {
                // Note that we are currently listing the shadow "java.lang." directly, so strip off the redundant "java.lang."
                // (this might change in the future).
                String shadowName = PackageConstants.kShadowDotPrefix + className;
                String shadowSuperName = PackageConstants.kShadowDotPrefix + superclassName;
                byte[] shadowBytes = null;
                if (kLegacyExceptionClassNames.contains(className)) {
                    // "Legacy" exception.
                    shadowBytes = generateLegacyExceptionClass(shadowName, shadowSuperName);
                } else {
                    // "Standard" exception.
                    shadowBytes = generateExceptionClass(shadowName, shadowSuperName);
                }
                
                generatedClasses.put(shadowName, shadowBytes);

                parentClassMap.put(shadowName, shadowSuperName);
            }
            
            // Generate the wrapper.
            String wrapperName = PackageConstants.kExceptionWrapperDotPrefix + className;
            String wrapperSuperName = PackageConstants.kExceptionWrapperDotPrefix + superclassName;
            byte[] wrapperBytes = generateWrapperClass(wrapperName, wrapperSuperName);
            generatedClasses.put(wrapperName, wrapperBytes);
        }
        return generatedClasses;
    }

    public static Map<String, byte[]> generateShadowEnum(){
        Map<String, byte[]> generatedClasses = new HashMap<>();

        for (String name : kShadowEnumClassNames){
            byte[] cnt = Helpers.loadRequiredResourceAsBytes(name.replaceAll("\\.", "/") + ".class");

            PreRenameClassAccessRules emptyUserRuleRuleSet = new PreRenameClassAccessRules(Collections.emptySet(), Collections.emptySet());
            byte[] bytecode = new ClassToolchain.Builder(cnt, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(emptyUserRuleRuleSet)))
                    .addNextVisitor(new ClassShadowing(PackageConstants.kInternalSlashPrefix + "Helper"))
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                    .build()
                    .runAndGetBytecode();
            bytecode = new ClassToolchain.Builder(bytecode, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                    .build()
                    .runAndGetBytecode();

            generatedClasses.put(name, bytecode);
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
