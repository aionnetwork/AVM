package org.aion.avm.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.aion.avm.NameStyle;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.ClassInformationRenamer;
import org.aion.avm.core.types.CommonType;
import i.PackageConstants;
import i.RuntimeAssertionError;
import org.junit.Test;

public class ClassInformationTest {

    /**
     * A post-rename class info that has no super classes listed will be given IObject if it is
     * an interface or shadow Object otherwise.
     */
    @Test
    public void testPostRenameClassInfoWithNoSupers() {
        ClassRenamer classRenamer = newClassRenamer(Collections.singleton("class"));

        // Check a regular class.
        ClassInformation info = ClassInformation.preRenameInfoFor(false, "class", null, null);
        ClassInformation renamedInfo = ClassInformationRenamer.toPostRenameClassInfo(classRenamer, info);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, renamedInfo.superClassDotName);
        assertEquals(0, renamedInfo.getInterfaces().length);

        // Check an interface.
        info = ClassInformation.preRenameInfoFor(true, "class", null, null);
        renamedInfo = ClassInformationRenamer.toPostRenameClassInfo(classRenamer, info);
        assertNull(renamedInfo.superClassDotName);
        assertEquals(1, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.I_OBJECT.dotName, renamedInfo.getInterfaces()[0]);
    }

    @Test
    public void testRenamingPreRenameClassWithJavaLangObjectSuper() {
        ClassRenamer classRenamer = newClassRenamer(Collections.singleton("self"));

        ClassInformation info = ClassInformation.preRenameInfoFor(false, "self", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation renamedInfo = ClassInformationRenamer.toPostRenameClassInfo(classRenamer, info);

        assertEquals(0, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, renamedInfo.superClassDotName);
    }

    @Test
    public void testRenamingPreRenameInterfaceWithJavaLangObjectSuper() {
        ClassRenamer classRenamer = newClassRenamer(Collections.singleton("self"));

        ClassInformation info = ClassInformation.preRenameInfoFor(true, "self", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation renamedInfo = ClassInformationRenamer.toPostRenameClassInfo(classRenamer, info);

        assertNull(renamedInfo.superClassDotName);
        assertEquals(1, renamedInfo.getInterfaces().length);
        assertEquals(CommonType.I_OBJECT.dotName, renamedInfo.getInterfaces()[0]);
    }

    /**
     * java.lang.Object has no post-rename equivalent. It is a proper post-rename type.
     */
    @Test(expected = RuntimeAssertionError.class)
    public void testRenamingJavaLangObjectClassInfo() {
        ClassRenamer classRenamer = newClassRenamer(Collections.emptySet());
        ClassInformation info = ClassInformation.preRenameInfoFor(false, CommonType.JAVA_LANG_OBJECT.dotName, null, null);
        ClassInformationRenamer.toPostRenameClassInfo(classRenamer, info);
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

    private static ClassRenamer newClassRenamer(Set<String> userDefinedClasses) {
        return new ClassRenamerBuilder(NameStyle.DOT_NAME, false)
            .loadPreRenameUserDefinedClasses(userDefinedClasses)
            .loadPostRenameJclExceptionClasses(fetchPostRenameJclExceptions())
            .build();
    }

    private static Set<String> fetchPostRenameJclExceptions() {
        Set<String> exceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                exceptions.add(type.dotName);
            }
        }
        return exceptions;
    }
}
