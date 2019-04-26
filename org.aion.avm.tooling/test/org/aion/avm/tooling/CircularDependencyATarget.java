package org.aion.avm.tooling;

import org.aion.avm.tooling.abi.Callable;

public class CircularDependencyATarget {
    int value;

    CircularDependencyBTarget bTarget = new CircularDependencyBTarget(20);

    public CircularDependencyATarget(int v) {
        value = v;
    }

    @Callable
    public static int createBAndReturn(int val) {
        CircularDependencyBTarget b = new CircularDependencyBTarget(val);
        return b.getValue();
    }

    @Callable
    public static int getValue() {
        return new CircularDependencyBTarget(5).getValue();
    }
}
