package org.aion.avm.core;

import org.aion.avm.core.types.Forest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Roman Katerinenko
 */
public class ForestTest {
    @Test
    public void checkAddingTheFirstPair() {
        final var forest = new Forest<String, byte[]>();
        final var nameA = "A";
        final var nameB = "B";
        final var a = newNode(nameA);
        final var b = newNode(nameB);
        forest.add(a, b);
        assertEquals(1, forest.getRoots().size());
        final var actualA = forest.getNodeById(nameA);
        final var actualB = forest.getNodeById(nameB);
        assertEquals(a, actualA);
        assertNull(actualA.getParent());
        assertEquals(actualA, actualB.getParent());
        assertEquals(1, actualA.getChildren().size());
        assertEquals(0, actualB.getChildren().size());
        assertEquals(actualA.getChildren().iterator().next(), actualB);
        assertEquals(forest.getRoots().iterator().next(), actualA);
    }

    @Test
    public void checkReparanting() {
        final var forest = new Forest<String, byte[]>();
        final var nameA0 = "A0";
        final var nameA = "A";
        final var nameB = "B";
        final var a0 = newNode(nameA0);
        final var a = newNode(nameA);
        final var b = newNode(nameB);
        forest.add(a, b);
        forest.add(a0, a);
        assertEquals(1, forest.getRoots().size());
        final var actualA0 = forest.getNodeById(nameA0);
        final var actualA = forest.getNodeById(nameA);
        final var actualB = forest.getNodeById(nameB);
        assertEquals(forest.getRoots().iterator().next(), actualA0);
        assertEquals(a0, actualA0);
        assertEquals(a, actualA);
        assertEquals(b, actualB);
        assertEquals(a.getParent(), a0);
    }

    @Test
    public void checkReparanting2() {
        final var forest = new Forest<String, byte[]>();
        final var a = newNode("A");
        final var ab = newNode("AB");
        final var abc = newNode("ABC");
        final var abcd = newNode("ABCD");
        forest.add(a, ab);
        forest.add(abc, abcd);
        assertEquals(2, forest.getRoots().size());
        forest.add(ab, abc);
        assertEquals(1, forest.getRoots().size());
    }

    @Test
    public void checkAddingDisjointTree() {
        final var nameA = "A";
        final var nameB = "B";
        final var nameC = "C";
        final var nameD = "D";
        final var a = newNode(nameA);
        final var b = newNode(nameB);
        final var c = newNode(nameC);
        final var d = newNode(nameD);
        final var forest = new Forest<String, byte[]>();
        forest.add(a, b);
        forest.add(c, d);
        final var actualA = forest.getNodeById(nameA);
        final var actualB = forest.getNodeById(nameB);
        final var actualC = forest.getNodeById(nameC);
        final var actualD = forest.getNodeById(nameD);
        assertEquals(actualB.getParent(), actualA);
        assertEquals(actualD.getParent(), actualC);
        assertEquals(2, forest.getRoots().size());
        assertNull(actualA.getParent());
        assertNull(actualC.getParent());
    }

    @Test
    public void checkAddingTwoChildren() {
        final var a = newNode("A");
        final var b = newNode("B");
        final var c = newNode("C");
        final var forest = new Forest<String, byte[]>();
        forest.add(a, b);
        forest.add(a, c);
        assertEquals(1, forest.getRoots().size());
        assertEquals(2, a.getChildren().size());
        assertEquals(b.getParent(), a);
        assertEquals(c.getParent(), a);
    }

    @Test
    public void checkWrongInput() {
        final var nameA = "A";
        final var a = newNode(nameA);
        final var b = newNode(nameA);
        final var forest = new Forest<String, byte[]>();
        try {
            forest.add(a, b);
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testVisitorVisitsAllNodes() {
        final var a = newNode("A");
        final var ab = newNode("AB");
        final var abc = newNode("ABC");
        final var abd = newNode("ABD");
        final var e = newNode("E");
        final var ef = newNode("EF");
        final var forest = new Forest<String, byte[]>();
        forest.add(a, ab);
        forest.add(ab, abc);
        forest.add(ab, abd);
        forest.add(e, ef);
        final var visitor = new TestVisitor();
        forest.walkPreOrder(visitor);
        String[] expectedPaths = {"A" + "AB" + "ABC" + "ABD",
                "E" + "EF"
        };
        checkPathsEqual(expectedPaths, visitor.getPathsFromRoot());
        Collection<String> actualRoots = visitor.getVisitedRoots();
        assertEquals(2, actualRoots.size());
        assertTrue(actualRoots.contains("A"));
        assertTrue(actualRoots.contains("E"));
    }

    private static void checkPathsEqual(String[] expected, Collection<String> actual) {
        assertEquals(expected.length, actual.size());
        for (String str : expected) {
            Assert.assertTrue(actual.contains(str));
        }
    }

    private static Forest.Node<String, byte[]> newNode(String id) {
        return new Forest.Node<>(id, null);
    }

    private static class TestVisitor implements Forest.Visitor<String, byte[]> {
        private final Collection<String> visitedRoots = new ArrayList<>();
        private final Collection<String> pathsFromRoot = new ArrayList<>();

        private StringBuilder curPath;

        @Override
        public void onVisitRoot(Forest.Node<String, byte[]> root) {
            visitedRoots.add(root.getId());
            if (curPath != null) {
                pathsFromRoot.add(curPath.toString());
            }
            curPath = new StringBuilder();
            curPath.append(root.getId());
        }

        @Override
        public void onVisitNotRootNode(Forest.Node<String, byte[]> node) {
            curPath.append(node.getId());
        }

        @Override
        public void afterAllNodesVisited() {
            if (curPath != null) {
                pathsFromRoot.add(curPath.toString());
            }
        }

        public Collection<String> getVisitedRoots() {
            return visitedRoots;
        }

        public Collection<String> getPathsFromRoot() {
            return pathsFromRoot;
        }
    }
}