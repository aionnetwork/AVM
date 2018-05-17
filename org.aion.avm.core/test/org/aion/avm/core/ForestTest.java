package org.aion.avm.core;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

    private static Forest.Node<String, byte[]> newNode(String id) {
        return new Forest.Node<>(id, null);
    }
}