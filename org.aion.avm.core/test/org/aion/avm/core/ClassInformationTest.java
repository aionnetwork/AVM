package org.aion.avm.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.junit.Test;

public class ClassInformationTest {

    /**
     * A post-rename class info that has no super classes listed will be given IObject if it is
     * an interface or shadow Object otherwise.
     */
    @Test
    public void testPostRenameClassInfoWithNoSupers() {
        // Check a regular class.
        ClassInformation info = ClassInformation.preRenameInfoFor(false, "class", null, null);
        ClassInformation renamedInfo = info.toPostRenameClassInfo();
        assertEquals(CommonType.SHADOW_OBJECT.dotName, renamedInfo.superClassDotName);
        assertEquals(0, renamedInfo.getInterfaces().length);

        // Check an interface.
        info = ClassInformation.preRenameInfoFor(true, "class", null, null);
        renamedInfo = info.toPostRenameClassInfo();
        assertNull(renamedInfo.superClassDotName);
        assertEquals(1, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.I_OBJECT.dotName, renamedInfo.getInterfaces()[0]);
    }

    /**
     * A mix of pre- and post-rename super classes. The post-rename supers should not get renamed.
     */
    @Test
    public void testRenamingPreRenameClassInfoWithPreAndPostRenameNames() {
        String self = "self";
        String postRenameSuper1 = CommonType.SHADOW_ENUM.dotName;
        String postRenameSuper2 = PackageConstants.kArrayWrapperDotPrefix + "array";
        String postRenameSuper3 = PackageConstants.kUserDotPrefix + "user";
        String postRenameSuper4 = PackageConstants.kInternalDotPrefix + "internal";
        String postRenameSuper5 = PackageConstants.kShadowApiDotPrefix + "api";
        String preRename1 = "preRename";
        String preRename2 = "java.lang.Number";
        String preRename3 = PackageConstants.kPublicApiDotPrefix + "user";

        String[] interfaces = new String[]{ postRenameSuper2, postRenameSuper3, postRenameSuper4, postRenameSuper5, preRename1, preRename2, preRename3 };
        ClassInformation info = ClassInformation.preRenameInfoFor(false, self, postRenameSuper1, interfaces);
        ClassInformation renamedInfo = info.toPostRenameClassInfo();

        assertEquals(PackageConstants.kUserDotPrefix + self, renamedInfo.dotName);
        assertEquals(postRenameSuper1, renamedInfo.superClassDotName);
        assertEquals(interfaces.length, renamedInfo.getInterfaces().length);

        // We expect the pre-rename names to be renamed as follows.
        String preRename1renamed = PackageConstants.kUserDotPrefix + preRename1;
        String preRename2renamed = PackageConstants.kShadowDotPrefix + preRename2;
        String preRename3renamed = PackageConstants.kShadowApiDotPrefix + preRename3;

        String[] expectedInterfaces = new String[]{ postRenameSuper2, postRenameSuper3, postRenameSuper4, postRenameSuper5, preRename1renamed, preRename2renamed, preRename3renamed };
        assertArrayEquals(expectedInterfaces, renamedInfo.getInterfaces());
    }

    @Test
    public void testRenamingPreRenameClassWithJavaLangObjectSuper() {
        ClassInformation info = ClassInformation.preRenameInfoFor(false, "self", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation renamedInfo = info.toPostRenameClassInfo();

        assertEquals(0, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, renamedInfo.superClassDotName);
    }

    @Test
    public void testRenamingPreRenameInterfaceWithJavaLangObjectSuper() {
        ClassInformation info = ClassInformation.preRenameInfoFor(true, "self", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation renamedInfo = info.toPostRenameClassInfo();

        assertNull(renamedInfo.superClassDotName);
        assertEquals(1, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.I_OBJECT.dotName, renamedInfo.getInterfaces()[0]);
    }

    /**
     * java.lang.Object has no post-rename equivalent. It is a proper post-rename type.
     */
    @Test(expected = RuntimeAssertionError.class)
    public void testRenamingJavaLangObjectClassInfo() {
        ClassInformation info = ClassInformation.preRenameInfoFor(false, CommonType.JAVA_LANG_OBJECT.dotName, null, null);
        info.toPostRenameClassInfo();
    }

    @Test
    public void testInterfacesIsNeverNull() {
        ClassInformation info = ClassInformation.postRenameInfoFor(false, "class", null, null);
        assertNotNull(info.getInterfaces());
        assertEquals(0, info.getInterfaces().length);
    }

    @Test
    public void testClassInfoImmutability() {
        String[] superInterfaces = new String[]{ "A", "B" };
        String[] superInterfacesCopy = Arrays.copyOf(superInterfaces, superInterfaces.length);

        ClassInformation info = ClassInformation.preRenameInfoFor(false, "self", "super", superInterfacesCopy);
        assertArrayEquals(superInterfaces, info.getInterfaces());

        // Modifying input array does nothing.
        superInterfacesCopy[0] = "C";
        assertArrayEquals(superInterfaces, info.getInterfaces());

        // Modifying returned interface array does nothing.
        info.getInterfaces()[0] = "D";
        assertArrayEquals(superInterfaces, info.getInterfaces());
    }

}
