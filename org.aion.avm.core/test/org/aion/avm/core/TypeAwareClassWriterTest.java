package org.aion.avm.core;

import java.util.HashSet;
import java.util.Set;

import org.aion.avm.core.exceptionwrapping.ExceptionWrapperNameMapper;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the internal logic of TypeAwareClassWriter.
 * Note that this class is not directly unit-testable so we created a testing subclass, in order to get access to the relevant protected method.
 *
 * Note that the {@link org.aion.avm.core.unification.CommonSuperClassTest} test suite is really
 * testing out the class writer but in an integration-style manner, not a unit test. However, it
 * does cover the class writer fairly exhaustively.
 */
public class TypeAwareClassWriterTest {
    private static boolean preserveDebuggability = false;

    @Test
    public void testJdkOnly_basic() throws Exception {
        TestClass clazz = new TestClass();
        String common = clazz.testing_getCommonSuperClass("java/lang/String", "java/lang/Throwable");
        Assert.assertEquals("java/lang/Object", common);
    }

    @Test
    public void testJdkOnly_exceptions() throws Exception {
        TestClass clazz = new TestClass();
        String common = clazz.testing_getCommonSuperClass("java/lang/OutOfMemoryError", "java/lang/Error");
        Assert.assertEquals("java/lang/Error", common);
    }

    @Test
    public void testWrappers_generated() throws Exception {
        TestClass clazz = new TestClass();
        String common = clazz.testing_getCommonSuperClass(ExceptionWrapperNameMapper.slashWrapperNameForClassName(PackageConstants.kShadowSlashPrefix + "java/lang/AssertionError"), ExceptionWrapperNameMapper.slashWrapperNameForClassName(PackageConstants.kShadowSlashPrefix + "java/lang/Error"));

        // We expect unification under java.lang.Throwable because as a simplifying heuristic we return
        // java.lang.Throwable as the super class of two exception wrappers.
        Assert.assertEquals(Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_THROWABLE.dotName), common);
    }

    @Test
    public void testWrappers_generatedAndreal() throws Exception {
        TestClass clazz = new TestClass();
        String common = clazz.testing_getCommonSuperClass(ExceptionWrapperNameMapper.slashWrapperNameForClassName(PackageConstants.kShadowSlashPrefix + "java/lang/AssertionError"), "java/lang/AssertionError");
        Assert.assertEquals("java/lang/Throwable", common);
    }

    @Test
    public void testShadows_both() throws Exception {
        TestClass clazz = new TestClass();
        String common = clazz.testing_getCommonSuperClass(PackageConstants.kShadowSlashPrefix + "java/lang/AssertionError", PackageConstants.kShadowSlashPrefix + "java/lang/TypeNotPresentException");
        Assert.assertEquals(PackageConstants.kShadowSlashPrefix + "java/lang/Throwable", common);
    }

    @Test
    public void testGeneratedOnly() throws Exception {
        HierarchyTreeBuilder builder = new HierarchyTreeBuilder();
        builder.addClass("A", "java.lang.Object", false, null);
        builder.addClass("B", "A", false, null);
        builder.addClass("C", "B", false, null);
        builder.addClass("B2", "A", false, null);

        Set<ClassInformation> classesToAdd = new HashSet<>();
        classesToAdd.add(ClassInformation
            .preRenameInfoFor(false, "A", CommonType.JAVA_LANG_OBJECT.dotName, null));
        classesToAdd.add(ClassInformation.preRenameInfoFor(false, "B", "A", null));
        classesToAdd.add(ClassInformation.preRenameInfoFor(false, "C", "B", null));
        classesToAdd.add(ClassInformation.preRenameInfoFor(false, "B2", "A", null));

        ClassHierarchy classHierarchy = new ClassHierarchyBuilder()
            .addPreRenameUserDefinedClasses(classesToAdd, preserveDebuggability)
            .build();

        TestClass clazz = new TestClass(classHierarchy);

        // Don't rename if debug mode.
        String prefix = (preserveDebuggability) ? "" : PackageConstants.kUserSlashPrefix;

        String common = clazz.testing_getCommonSuperClass(prefix + "B", prefix + "C");
        Assert.assertEquals(prefix + "B", common);
        common = clazz.testing_getCommonSuperClass(prefix + "B", prefix + "B2");
        Assert.assertEquals(prefix + "A", common);
    }


    private static class TestClass extends TypeAwareClassWriter {

        public TestClass(ClassHierarchy classHierarchy) {
            super(0, classHierarchy, preserveDebuggability);
        }

        public TestClass() {
            super(0, getPreLoadedClassHierarchy(), preserveDebuggability);
        }

        public String testing_getCommonSuperClass(String type1, String type2) {
            return this.getCommonSuperClass(type1, type2);
        }

        private static ClassHierarchy getPreLoadedClassHierarchy() {
            return new ClassHierarchyBuilder()
                .addShadowJcl()
                .addPostRenameJclExceptions()
                .addHandwrittenArrayWrappers()
                .build();
        }
    }
}
