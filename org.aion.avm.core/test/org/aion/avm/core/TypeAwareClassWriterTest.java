package org.aion.avm.core;

import java.util.Collections;
import java.util.Set;

import org.aion.avm.core.exceptionwrapping.ExceptionWrapperNameMapper;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.internal.PackageConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the internal logic of TypeAwareClassWriter.
 * Note that this class is not directly unit-testable so we created a testing subclass, in order to get access to the relevant protected method.
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
        Assert.assertEquals(ExceptionWrapperNameMapper.slashWrapperNameForClassName(PackageConstants.kShadowSlashPrefix + "java/lang/Error"), common);
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
        TestClass clazz = new TestClass(Set.of("A", "B", "C", "B2"), builder.asMutableForest());
        String common = clazz.testing_getCommonSuperClass(PackageConstants.kUserSlashPrefix + "B", PackageConstants.kUserSlashPrefix + "C");
        Assert.assertEquals(PackageConstants.kUserSlashPrefix + "B", common);
        common = clazz.testing_getCommonSuperClass(PackageConstants.kUserSlashPrefix + "B", PackageConstants.kUserSlashPrefix + "B2");
        Assert.assertEquals(PackageConstants.kUserSlashPrefix + "A", common);
    }


    private static class TestClass extends TypeAwareClassWriter {
        public TestClass(Set<String> userDefinedClassNames, Forest<String, ClassInfo> classHierarchy) {
            super(0, new ParentPointers(userDefinedClassNames, classHierarchy, preserveDebuggability));
        }
        public TestClass() {
            super(0, new ParentPointers(Collections.emptySet(), new HierarchyTreeBuilder().asMutableForest(), preserveDebuggability));
        }
        public String testing_getCommonSuperClass(String type1, String type2) {
            return this.getCommonSuperClass(type1, type2);
        }
    }
}
