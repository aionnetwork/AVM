package org.aion.avm.tooling;

import org.aion.avm.tooling.abi.Callable;

public class CircularDependencyBTarget {
    int value;
    CircularDependencyATarget aTarget;

    public CircularDependencyBTarget(int v) {
        value = v;
    }

    @Callable
    public static int createAAndReturn(int val) {
        CircularDependencyATarget b = new CircularDependencyATarget(val);
        return b.getValue();
    }

    int getValue() {
        return value;
    }
}
