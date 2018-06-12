package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;


public class ClassWhiteListTest {
    @Test
    public void testJdkType() {
        ClassWhiteList list = ClassWhiteList.buildForEmptyContract();
        String className = "java/lang/Object";
        Assert.assertTrue(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }

    @Test
    public void testJdkSubType() {
        // NOTE:  Classes in sub-packages should probably be rejected (the white-list needs to be made more complex).
        ClassWhiteList list = ClassWhiteList.buildForEmptyContract();
        String className = "java/lang/ref/SoftReference";
        Assert.assertTrue(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }

    @Test
    public void testUtilType() {
        ClassWhiteList list = ClassWhiteList.buildForEmptyContract();
        String className = "java/util/HashSet";
        Assert.assertFalse(list.isJdkClass(className));
        Assert.assertFalse(list.isInWhiteList(className));
    }

    @Test
    public void testContractType() {
        String exceptionClassDotName = "my.contract.exception";
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(exceptionClassDotName, "java.lang.Throwable", null)
                .asMutableForest();
        String className = Helpers.fulllyQualifiedNameToInternalName(exceptionClassDotName);
        ClassWhiteList list = ClassWhiteList.buildFromClassHierarchy(classHierarchy);
        Assert.assertFalse(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }
}
