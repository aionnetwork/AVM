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
