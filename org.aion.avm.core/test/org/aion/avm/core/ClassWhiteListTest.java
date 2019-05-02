package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import i.PackageConstants;
import org.junit.Assert;
import org.junit.Test;


public class ClassWhiteListTest {
    @Test
    public void testJdkType() {
        ClassWhiteList list = new ClassWhiteList();
        String className = PackageConstants.kShadowSlashPrefix + "java/lang/Object";
        Assert.assertTrue(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }

    @Test
    public void testJdkSubType() {
        // NOTE:  Classes in sub-packages should probably be rejected (the white-list needs to be made more complex).
        ClassWhiteList list = new ClassWhiteList();
        String className = PackageConstants.kShadowSlashPrefix + "java/lang/ref/SoftReference";
        Assert.assertTrue(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }

    @Test
    public void testUtilType() {
        ClassWhiteList list = new ClassWhiteList();
        String className = "java/util/HashSet";
        Assert.assertFalse(list.isJdkClass(className));
        Assert.assertFalse(list.isInWhiteList(className));
    }

    @Test
    public void testContractType() {
        String exceptionClassDotName = PackageConstants.kUserDotPrefix + "my.contract.exception";
        String className = Helpers.fulllyQualifiedNameToInternalName(exceptionClassDotName);
        ClassWhiteList list = new ClassWhiteList();
        Assert.assertFalse(list.isJdkClass(className));
        Assert.assertTrue(list.isInWhiteList(className));
    }
}
