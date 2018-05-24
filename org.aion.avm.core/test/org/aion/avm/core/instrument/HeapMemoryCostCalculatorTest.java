package org.aion.avm.core.instrument;

import org.aion.avm.core.Forest;
import org.junit.Test;

import static org.junit.Assert.*;

public class HeapMemoryCostCalculatorTest {

    @Test
    public void testCalcInstanceSizeOfOneClass() {
        final var HeapCalc = new HeapMemoryCostCalculator();
        final var forest = new Forest<String, byte[]>();
        final var nameA = "A";
        final var nameB = "B";
        final var a = newNode(nameA);
        final var b = newNode(nameB);
        forest.add(a, b);


    }

    @Test
    public void testCalcClassesInstanceSize() {
        final var HeapCalc = new HeapMemoryCostCalculator();
    }


    private static Forest.Node<String, byte[]> newNode(String id) {
        return new Forest.Node<>(id, null);
    }

}