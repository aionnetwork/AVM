package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.ClassHierarchyVerifier;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.core.types.HierarchyVerificationResult;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.junit.Test;

public class ClassHierarchyTest {
    private static final int BASE_SIZE = 4;

    /**
     * An empty hierarchy always contains: java.lang.Object, IObject, shadow Object, and
     * java.lang.Throwable
     */
    @Test
    public void testEmptyHierarchy() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();

        assertEquals(BASE_SIZE, hierarchy.size());

        assertTrue(hierarchy.contains(CommonType.JAVA_LANG_OBJECT.dotName));
        assertTrue(hierarchy.contains(CommonType.I_OBJECT.dotName));
        assertTrue(hierarchy.contains(CommonType.SHADOW_OBJECT.dotName));
        assertTrue(hierarchy.contains(CommonType.JAVA_LANG_THROWABLE.dotName));
    }

    /**
     * The hierarhcy only accepts post-rename classes.
     */
    @Test(expected = RuntimeAssertionError.class)
    public void testAddingPreRenameClass() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(ClassInformation.preRenameInfoFor(false, "class", null, null));
    }

    /**
     * Hierarchy does not allow for classes to directly subclass java.lang.Object
     * This is for correctness: the hierarchy is post-rename and we should have require any classes
     * except for IObject and java.lang.Throwable to descend from the real Object.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddingDirectChildOfJavaLangObject() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(ClassInformation.postRenameInfoFor(false, "class", CommonType.JAVA_LANG_OBJECT.dotName, null));
    }

    /**
     * Hierarchy does not allow for the same class to be added twice when using the 'add' method.
     * This is to help ensure correctness.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddingSameClassTwiceUsingAdd() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();

        ClassInformation info = ClassInformation.postRenameInfoFor(false, "class", CommonType.SHADOW_OBJECT.dotName, null);
        hierarchy.add(info);
        hierarchy.add(info);
    }

    @Test
    public void testAddingSameClassTwiceUsingAddIfAbsent() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();

        ClassInformation info = ClassInformation.postRenameInfoFor(false, "class", CommonType.SHADOW_OBJECT.dotName, null);
        hierarchy.addIfAbsent(info);
        hierarchy.addIfAbsent(info);

        assertEquals(BASE_SIZE + 1, hierarchy.size());
        assertTrue(hierarchy.contains(info.dotName));
    }

    /**
     * This test is here to verify that, given N nodes inserted into the hierarchy, such that these
     * N nodes should produce a complete tree without any inconsistencies, that we can add these
     * nodes into the hierarchy in any of the N possible permutations.
     *
     * We select a set of M random permutations for simplicity and try each of these out.
     */
    @Test
    public void testVariousConstructionInputPermutations() {
        ClassInformation interface1 = ClassInformation.postRenameInfoFor(true, "int1", null, new String[]{ CommonType.I_OBJECT.dotName });
        ClassInformation interface2 = ClassInformation.postRenameInfoFor(true, "int2", null, new String[]{ interface1.dotName });
        ClassInformation class1 = ClassInformation.postRenameInfoFor(false, "class1",CommonType.SHADOW_OBJECT.dotName, null);
        ClassInformation class2 = ClassInformation.postRenameInfoFor(false, "class2", class1.dotName, new String[]{ interface1.dotName, interface2.dotName });

        List<ClassInformation> classes = toList(interface1, interface2, class1, class2);

        ClassHierarchyVerifier verifier = new ClassHierarchyVerifier();
        for (List<ClassInformation> permutation : randomPermutations(classes, 100)) {
            ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();

            for (ClassInformation classToAdd : permutation) {
                hierarchy.add(classToAdd);
            }

            // Verify the hierarchy is complete.
            HierarchyVerificationResult result = verifier.verifyHierarchy(hierarchy);
            if (!result.success) {
                System.err.println("Permutation that caused the failure: " + permutation);
                fail(result.getError());
            }
        }
    }

    /**
     * User-defined classes are submitted to the hierarchy as pre-rename classes, and this is because
     * the hierarchy must hold onto the list of all pre-renamed user-defined classes.
     *
     * However, these classes get renamed when added to the hierarchy, so that no pre-rename classes
     * ever get in.
     */
    @Test
    public void testAddingUserDefinedClassesWhenNotInDebugMode() {
        ClassInformation interface1 = ClassInformation.preRenameInfoFor(true, "int1", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation interface2 = ClassInformation.preRenameInfoFor(true, "int2", null, new String[]{ interface1.dotName });
        ClassInformation class1 = ClassInformation.preRenameInfoFor(false, "class1", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation class2 = ClassInformation.preRenameInfoFor(false, "class2", class1.dotName, new String[]{ interface1.dotName, interface2.dotName });

        Set<ClassInformation> classes = new HashSet<>(toList(interface1, interface2, class1, class2));

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.addPreRenameUserDefinedClasses(classes, false);

        // When we ask for the classes back we get the pre-rename classes.
        assertEquals(BASE_SIZE + 4, hierarchy.size());
        assertEquals(extractClassNamesToSet(classes), hierarchy.getPreRenameUserDefinedClassesAndInterfaces());
        assertEquals(extractClassNamesToSetNoInterfaces(classes), hierarchy.getPreRenameUserDefinedClassesOnly(false));

        // However, none of the pre-rename classes are actually nodes in the hierarchy.
        for (ClassInformation classInformation : classes) {
            assertFalse(hierarchy.contains(classInformation.dotName));
        }

        // But the post-rename version of each of these classes are in the hierarchy.
        for (ClassInformation classInformation : classes) {
            assertTrue(hierarchy.contains(PackageConstants.kUserDotPrefix + classInformation.dotName));
        }

        // Finally, let's just verify that all of the super classes got renamed.
        String renamedSuper = PackageConstants.kUserDotPrefix + class2.superClassDotName;
        assertEquals(renamedSuper, hierarchy.getConcreteSuperClassDotName(PackageConstants.kUserDotPrefix + class2.dotName));

        // class1 is subclassed under shadow object now.
        assertEquals(CommonType.SHADOW_OBJECT.dotName, hierarchy.getConcreteSuperClassDotName(PackageConstants.kUserDotPrefix + class1.dotName));

        // interface1 is subclassed under IObject, so it no longer has a direct super class.
        assertNull(hierarchy.getConcreteSuperClassDotName(PackageConstants.kUserDotPrefix + interface1.dotName));
    }

    /**
     * See explanation of the above test when tests this scenario when not in debug mode.
     *
     * The only difference is that in debug mode the classes do not get renamed -- their pre- and
     * post-rename names are identical. However, their super classes do get renamed if those supers
     * are not user-defined classes!
     */
    @Test
    public void testAddingUserDefinedClassesWhenInDebugMode() {
        ClassInformation interface1 = ClassInformation.preRenameInfoFor(true, "int1", CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation interface2 = ClassInformation.preRenameInfoFor(true, "int2", null, new String[]{ interface1.dotName });
        ClassInformation class1 = ClassInformation.preRenameInfoFor(false, "class1",CommonType.JAVA_LANG_OBJECT.dotName, null);
        ClassInformation class2 = ClassInformation.preRenameInfoFor(false, "class2", class1.dotName, new String[]{ interface1.dotName, interface2.dotName });

        Set<ClassInformation> classes = new HashSet<>(toList(interface1, interface2, class1, class2));

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.addPreRenameUserDefinedClasses(classes, true);

        // When we ask for the classes back we get the pre-rename classes.
        assertEquals(BASE_SIZE + 4, hierarchy.size());
        assertEquals(extractClassNamesToSet(classes), hierarchy.getPreRenameUserDefinedClassesAndInterfaces());
        assertEquals(extractClassNamesToSetNoInterfaces(classes), hierarchy.getPreRenameUserDefinedClassesOnly(true));

        // Since these classes do not get renamed, they are all in the hierarchy as-is.
        for (ClassInformation classInformation : classes) {
            assertTrue(hierarchy.contains(classInformation.dotName));
        }

        // Let's verify that non-user-defined classes are renamed, but the user-defined ones are not.
        assertEquals(class2.superClassDotName, hierarchy.getConcreteSuperClassDotName(class2.dotName));

        // class1 is subclassed under shadow object now since that is not a user-defined class.
        assertEquals(CommonType.SHADOW_OBJECT.dotName, hierarchy.getConcreteSuperClassDotName(class1.dotName));

        // interface1 is subclassed under IObject, so it no longer has a direct super class.
        assertNull(hierarchy.getConcreteSuperClassDotName(interface1.dotName));
    }

    @Test
    public void testTightestSuperOfSiblings() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals("A", hierarchy.getTightestCommonSuperClass("B", "C"));
    }

    @Test
    public void testTightestSuperOfChildAndParent() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals("A", hierarchy.getTightestCommonSuperClass("B", "A"));
    }

    @Test
    public void testTightestSuperOfChildAndDistantAncestor() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals("A", hierarchy.getTightestCommonSuperClass("E", "A"));
    }

    @Test
    public void testTightestSuperOfClassAndJavaLangObject() {
        String javaLangObject = CommonType.JAVA_LANG_OBJECT.dotName;

        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals(javaLangObject, hierarchy.getTightestCommonSuperClass("G", javaLangObject));
    }

    @Test
    public void testTightestSuperOfJavaLangObjectAndItself() {
        String javaLangObject = CommonType.JAVA_LANG_OBJECT.dotName;

        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals(javaLangObject, hierarchy.getTightestCommonSuperClass(javaLangObject, javaLangObject));
    }

    @Test
    public void testTightestSuperOfClassAndItself() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals("H", hierarchy.getTightestCommonSuperClass("H", "H"));
    }

    @Test
    public void testTightestSuperOfDistantCousins() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals("A", hierarchy.getTightestCommonSuperClass("E", "F"));
    }

    /**
     * NOTE: the tightest super class is ambiguous when there are multiple such candidates.
     *
     * Our current heuristic to handle this case is to return IObject, since this is always a safe
     * super class to choose in such a condition.
     */
    @Test
    public void testTightestSuperWhenAmbiguous() {
        ClassHierarchy hierarchy = produceHierarchyForTightestSuperTests();
        assertEquals(CommonType.I_OBJECT.dotName, hierarchy.getTightestCommonSuperClass("G", "H"));
    }

    @Test
    public void testDeepCopy() {
        ClassInformation interface1 = ClassInformation.postRenameInfoFor(true, "int1", null, new String[]{ CommonType.I_OBJECT.dotName });
        ClassInformation interface2 = ClassInformation.postRenameInfoFor(true, "int2", null, new String[]{ interface1.dotName });
        ClassInformation class1 = ClassInformation.postRenameInfoFor(false, "class1",CommonType.SHADOW_OBJECT.dotName, null);
        ClassInformation class2 = ClassInformation.postRenameInfoFor(false, "class2", class1.dotName, new String[]{ interface1.dotName, interface2.dotName });

        List<ClassInformation> classes = toList(interface1, interface2, class1, class2);
        ClassHierarchy original = new ClassHierarchyBuilder().build();

        for (ClassInformation classToAdd : classes) {
            original.add(classToAdd);
        }

        ClassInformation extra1 = ClassInformation.postRenameInfoFor(false, "extra1", class1.dotName, null);
        ClassInformation extra2 = ClassInformation.postRenameInfoFor(false, "extra2", extra1.dotName, null);

        // We make a deep copy of the hierarchy, then modify it (ie. add classes to it) and then verify
        // that the original did not change.
        ClassHierarchy copy = original.deepCopy();

        // First, verify the two hierarchies are the same.
        assertEquals(copy.size(), original.size());
        for (ClassInformation addedClass : classes) {
            assertTrue(original.contains(addedClass.dotName));
            assertTrue(copy.contains(addedClass.dotName));
        }

        // Now modify the copy.
        copy.add(extra1);
        copy.add(extra2);

        assertEquals(original.size() + 2, copy.size());
        for (ClassInformation addedClass : classes) {
            assertTrue(original.contains(addedClass.dotName));
            assertTrue(copy.contains(addedClass.dotName));
        }

        // Verify the extra classes are in the copy and not in the original.
        assertFalse(original.contains(extra1.dotName));
        assertTrue(copy.contains(extra1.dotName));

        assertFalse(original.contains(extra2.dotName));
        assertTrue(copy.contains(extra2.dotName));
    }

    /**
     * This hierarchy is of the following shape:
     *
     * java.lang.Object
     *   - java.lang.Throwable
     *   - IObject
     *     - shadow Object
     *     - A
     *       - B
     *         - D
     *           - E
     *         - G
     *         - H
     *       - C
     *         - G
     *         - H
     *         - F
     */
    private ClassHierarchy produceHierarchyForTightestSuperTests() {
        ClassInformation A = ClassInformation.postRenameInfoFor(true, "A", null, new String[]{ CommonType.I_OBJECT.dotName });
        ClassInformation B = ClassInformation.postRenameInfoFor(false, "B", null, new String[]{ A.dotName });
        ClassInformation C = ClassInformation.postRenameInfoFor(true, "C", null, new String[]{ A.dotName });
        ClassInformation D = ClassInformation.postRenameInfoFor(false, "D", B.dotName, null);
        ClassInformation E = ClassInformation.postRenameInfoFor(false, "E", D.dotName, null);
        ClassInformation F = ClassInformation.postRenameInfoFor(true, "F", null, new String[]{ C.dotName });
        ClassInformation G = ClassInformation.postRenameInfoFor(false, "G", B.dotName, new String[]{ C.dotName });
        ClassInformation H = ClassInformation.postRenameInfoFor(false, "H", B.dotName, new String[]{ C.dotName });
        List<ClassInformation> classes = toList(A, B, C, D, E, F, G, H);

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        for (ClassInformation classToAdd : classes) {
            hierarchy.add(classToAdd);
        }

        assertTrue(new ClassHierarchyVerifier().verifyHierarchy(hierarchy).success);
        return hierarchy;
    }

    private List<ClassInformation> toList(ClassInformation... infos) {
        return Arrays.asList(infos);
    }

    private Set<List<ClassInformation>> randomPermutations(List<ClassInformation> original, int num) {
        Set<List<ClassInformation>> permutations = new HashSet<>();

        List<ClassInformation> list = original;
        for (int i = 0; i < num; i++) {
            List<ClassInformation> copy = new ArrayList<>(list);
            Collections.shuffle(copy);
            permutations.add(copy);
            list = copy;
        }

        return permutations;
    }

    private Set<String> extractClassNamesToSet(Set<ClassInformation> classInformations) {
        Set<String> names = new HashSet<>();
        for (ClassInformation classInformation : classInformations) {
            names.add(classInformation.dotName);
        }
        return names;
    }

    private Set<String> extractClassNamesToSetNoInterfaces(Set<ClassInformation> classInformations) {
        Set<String> names = new HashSet<>();
        for (ClassInformation classInformation : classInformations) {
            if (!classInformation.isInterface) {
                names.add(classInformation.dotName);
            }
        }
        return names;
    }

}
