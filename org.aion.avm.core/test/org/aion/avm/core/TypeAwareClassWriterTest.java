package org.aion.avm.core;

import java.util.HashSet;
import java.util.Set;

import org.aion.avm.NameStyle;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapperNameMapper;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.CommonType;
import i.PackageConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the internal logic of TypeAwareClassWriter.
 * Note that this class is not directly unit-testable so we created a testing subclass, in order to get access to the relevant protected method.
 *
 * These are legacy tests on the type writer class. Integ tests on this are exploited in the
 * {@link org.aion.avm.core.unification.CommonSuperClassTest} test and unit tests are done in:
 * {@link ArraySuperResolverTest}, {@link ExceptionWrapperSuperResolverTest}, and
 * {@link PlainTypeSuperResolverTest}.
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
        Assert.assertEquals("e/s/java/lang/Error", common);
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

        Set<String> userClasses = new HashSet<>();
        for (ClassInformation classToAdd : classesToAdd) {
            userClasses.add(classToAdd.dotName);
        }

        Set<String> jclExceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                jclExceptions.add(type.dotName);
            }
        }

        ClassRenamer classRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
            .loadPreRenameUserDefinedClasses(userClasses)
            .loadPostRenameJclExceptionClasses(jclExceptions)
            .build();

        ClassHierarchy classHierarchy = new ClassHierarchyBuilder()
            .addPreRenameUserDefinedClasses(classRenamer, classesToAdd)
            .build();

        TestClass clazz = new TestClass(classHierarchy, classRenamer);

        // Don't rename if debug mode.
        String prefix = (preserveDebuggability) ? "" : PackageConstants.kUserSlashPrefix;

        String common = clazz.testing_getCommonSuperClass(prefix + "B", prefix + "C");
        Assert.assertEquals(prefix + "B", common);
        common = clazz.testing_getCommonSuperClass(prefix + "B", prefix + "B2");
        Assert.assertEquals(prefix + "A", common);
    }


    private static class TestClass extends TypeAwareClassWriter {

        public TestClass(ClassHierarchy classHierarchy, ClassRenamer classRenamer) {
            super(0, classHierarchy, classRenamer);
        }

        public TestClass() {
            super(0, getPreLoadedClassHierarchy(), getPreLoadedClassRenamer());
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

        private static ClassRenamer getPreLoadedClassRenamer() {
            Set<String> jclExceptions = new HashSet<>();
            for (CommonType type : CommonType.values()) {
                if (type.isShadowException) {
                    jclExceptions.add(type.dotName);
                }
            }

            return new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
                .loadPreRenameUserDefinedClasses(getPreLoadedClassHierarchy().getPreRenameUserDefinedClassesAndInterfaces())
                .loadPostRenameJclExceptionClasses(jclExceptions)
                .build();
        }
    }
}
