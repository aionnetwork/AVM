package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.DecoratedHierarchyNode;
import org.aion.avm.core.types.HierarchyGhostNode;
import org.aion.avm.core.types.HierarchyNode;
import org.aion.avm.internal.RuntimeAssertionError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HierarchyNodeTest {
    private HierarchyNode node;

    @Before
    public void setup() {
        node = HierarchyNode.from(ClassInformation.preRenameInfoFor(false, "self", null, null));
    }

    @After
    public void tearDown() {
        node = null;
    }

    @Test
    public void testAddParents() {
        HierarchyNode parent1 = newHierarchyNode("classA");
        HierarchyNode parent2 = newHierarchyNode("classB");

        Set<HierarchyNode> expectedParents = new HashSet<>();
        expectedParents.add(parent1);
        expectedParents.add(parent2);

        node.addParent(parent1);
        node.addParent(parent2);
        node.addParent(parent1);
        assertEquals(expectedParents, node.getParents());
    }

    @Test
    public void testAddChildren() {
        HierarchyNode parent1 = newHierarchyNode("classA");
        HierarchyNode parent2 = newHierarchyNode("classB");
        HierarchyNode parent3 = newHierarchyNode("classC");

        Set<HierarchyNode> expectedChildren = new HashSet<>();
        expectedChildren.add(parent1);
        expectedChildren.add(parent2);
        expectedChildren.add(parent3);

        node.addChild(parent1);
        node.addChild(parent2);
        node.addChild(parent3);
        assertEquals(expectedChildren, node.getChildren());
    }

    @Test
    public void testRemoveParents() {
        HierarchyNode parent1 = newHierarchyNode("classA");
        HierarchyNode parent2 = newHierarchyNode("classB");
        HierarchyNode parent3 = newHierarchyNode("classC");

        // Add 3 parents.
        Set<HierarchyNode> expectedParents = new HashSet<>();
        expectedParents.add(parent1);
        expectedParents.add(parent2);
        expectedParents.add(parent3);

        node.addParent(parent1);
        node.addParent(parent2);
        node.addParent(parent3);
        assertEquals(expectedParents, node.getParents());

        // Remove 2 of the parents.
        expectedParents.remove(parent1);
        expectedParents.remove(parent3);

        node.removeParent(parent1);
        node.removeParent(parent3);
        assertEquals(expectedParents, node.getParents());

        // Remove the last parent.
        node.removeParent(parent2);
        assertTrue(node.getParents().isEmpty());
    }

    @Test
    public void testAddGhostNodeParent() {
        node.addParent(newHierarchyGhostNode("ghost"));
        assertEquals(1, node.getParents().size());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testAddDecoratedNodeParent() {
        node.addParent(DecoratedHierarchyNode.decorate(newHierarchyGhostNode("ghost")));
    }

    @Test
    public void testAddGhostNodeChild() {
        node.addChild(newHierarchyGhostNode("ghost"));
        assertEquals(1, node.getChildren().size());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testAddDecoratedNodeChild() {
        node.addChild(DecoratedHierarchyNode.decorate(newHierarchyNode("node")));
    }

    @Test
    public void testDecoratedNodeAddAndRemoveParent() {
        DecoratedHierarchyNode decorated = DecoratedHierarchyNode.decorate(node);
        assertEquals(node, decorated.unwrap());

        // Verify that the decorated node and the node it wraps both change accordingly.
        decorated.addParent(newHierarchyNode("node"));
        assertEquals(1, decorated.getParents().size());
        assertEquals(decorated.getParents(), node.getParents());

        decorated.removeParent(newHierarchyNode("node"));
        assertTrue(decorated.getParents().isEmpty());
        assertEquals(decorated.getParents(), node.getParents());
    }

    @Test
    public void testDecoratedNodeAddChildren() {
        DecoratedHierarchyNode decorated = DecoratedHierarchyNode.decorate(node);
        assertEquals(node, decorated.unwrap());

        // Verify that the decorated node and the node it wraps both change accordingly.
        decorated.addChild(newHierarchyNode("node"));
        assertEquals(1, decorated.getChildren().size());
        assertEquals(decorated.getChildren(), node.getChildren());
    }

    private HierarchyNode newHierarchyNode(String name) {
        ClassInformation info = ClassInformation.preRenameInfoFor(false, name, null, null);
        return HierarchyNode.from(info);
    }

    private HierarchyGhostNode newHierarchyGhostNode(String name) {
        return new HierarchyGhostNode(name);
    }

}
