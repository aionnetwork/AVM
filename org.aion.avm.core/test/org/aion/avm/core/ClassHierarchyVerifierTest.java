package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.ClassHierarchyVerifier;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.core.types.HierarchyVerificationResult;
import org.junit.Test;

public class ClassHierarchyVerifierTest {
    private static ClassHierarchyVerifier verifier = new ClassHierarchyVerifier();

    @Test
    public void testWhenHierarchyIsProperlyConstructed() {
        ClassHierarchy hierarchy = produceCompleteClassHierarchy();
        assertTrue(verifier.verifyHierarchy(hierarchy).success);
    }

    @Test(expected = NullPointerException.class)
    public void testWhenHierarchyIsNull() {
        verifier.verifyHierarchy(null);
    }

    @Test
    public void testWhenHierarchyContainsGhostNode() {
        // We give the hierarchy a class that talks about another class 'B', but we never give it B,
        // so B is left hanging around as a ghost node.
        ClassInformation info = ClassInformation.postRenameInfoFor(false, "A", "B", new String[]{ CommonType.I_OBJECT.dotName });

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(info);

        HierarchyVerificationResult result = verifier.verifyHierarchy(hierarchy);
        assertFalse(result.success);
        assertTrue(result.foundGhost);
        assertEquals(info.superClassDotName, result.nodeName);
    }

    @Test
    public void testWhenHierarchyContainsInterfaceWithConcreteSuperclass() {
        ClassInformation info1 = ClassInformation.postRenameInfoFor(false, "A", CommonType.SHADOW_OBJECT.dotName, null);
        ClassInformation info2 = ClassInformation.postRenameInfoFor(true, "B", info1.dotName, null);

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(info1);
        hierarchy.add(info2);

        HierarchyVerificationResult result = verifier.verifyHierarchy(hierarchy);
        assertFalse(result.success);
        assertTrue(result.foundInterfaceWithConcreteSuper);
        assertEquals(info2.dotName, result.nodeName);
    }

    @Test
    public void testWhenHierarchyContainsClassWithMultipleConcreteSuperclasses() {
        ClassInformation info1 = ClassInformation.postRenameInfoFor(false, "A", CommonType.SHADOW_OBJECT.dotName, null);
        ClassInformation info2 = ClassInformation.postRenameInfoFor(false, "B", CommonType.SHADOW_OBJECT.dotName, null);
        ClassInformation info3 = ClassInformation.postRenameInfoFor(false, "C", info1.dotName, new String[]{ info2.dotName });

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(info1);
        hierarchy.add(info2);
        hierarchy.add(info3);

        HierarchyVerificationResult result = verifier.verifyHierarchy(hierarchy);
        assertFalse(result.success);
        assertTrue(result.foundMultipleNonInterfaceSupers);
        assertEquals(info3.dotName, result.nodeName);
    }

    @Test
    public void testWhenHierarchyContainsUnreachableNodes() {
        // B will be a ghost node, but since it's never made real it will never descend from any
        // other node. And A only descends from B, so both A and B are cut off from the hierarchy.
        ClassInformation info = ClassInformation.postRenameInfoFor(false, "A", "B", null);

        ClassHierarchy hierarchy = new ClassHierarchyBuilder().build();
        hierarchy.add(info);

        HierarchyVerificationResult result = verifier.verifyHierarchy(hierarchy);
        assertFalse(result.success);
        assertTrue(result.foundUnreachableNodes);
        assertEquals(2, result.numberOfUnreachableNodes);
    }

    private ClassHierarchy produceCompleteClassHierarchy() {
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

}
