package org.aion.avm.core.miscvisitors.interfaceVisitor;

public class SampleObj {
    int count = 1;

    @Override
    public String toString() {
        count *= 1000;
        return "SampleObj{" +
                "count=" + count +
                '}';
    }
}
