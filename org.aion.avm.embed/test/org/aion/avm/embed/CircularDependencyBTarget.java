package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

import avm.Blockchain;

public class CircularDependencyBTarget {
    int value;
    CircularDependencyATarget aTarget;

    public CircularDependencyBTarget(int v) {
        value = v;
    }

    @Callable
    public static int createAAndReturn(int val) {
        CircularDependencyATarget b = new CircularDependencyATarget(val);
        Blockchain.require(null != b);
        return CircularDependencyATarget.getValue();
    }

    int getValue() {
        return value;
    }
}
