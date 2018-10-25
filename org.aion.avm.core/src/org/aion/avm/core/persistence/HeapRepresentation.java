package org.aion.avm.core.persistence;


/**
 * The HeapRepresentation of an object instance (or collection of instances) is analogous to the SerializedRepresentation, but only used in the reentrant case.
 * This means that it supports similar usage patterns but is internally completely different (captures everything as just an object instance) and exposes a
 * slightly different interface (since it doesn't operate on INode).
 */
public class HeapRepresentation {
    public final Object[] primitives;
    public final org.aion.avm.shadow.java.lang.Object[] references;
    private final int billableSize;

    public HeapRepresentation(Object[] primitives, org.aion.avm.shadow.java.lang.Object[] references, int billableSize) {
        this.primitives = primitives;
        this.references = references;
        this.billableSize = billableSize;
    }

    public int getBillableSize() {
        return this.billableSize;
    }

    @Override
    public int hashCode() {
        // We might want something more interesting, in the future, but we don't heavily rely on this (need to decide what elements of nested data can be used).
        int hash = this.billableSize;
        hash ^= this.primitives.length;
        hash ^= this.references.length;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (obj instanceof HeapRepresentation);
        
        if (isEqual) {
            HeapRepresentation other = (HeapRepresentation) obj;
            if ((this.primitives.length == other.primitives.length) && (this.references.length == other.references.length)) {
                int index = 0;
                while (isEqual && (index < this.primitives.length)) {
                    // These are box types so their .equals should be reasonable.
                    isEqual = (this.primitives[index].equals(other.primitives[index]));
                    index += 1;
                }
                index = 0;
                while (isEqual && (index < this.references.length)) {
                    // We are just looking for direct references comparisons.
                    isEqual = (this.references[index] == other.references[index]);
                    index += 1;
                }
            } else {
                isEqual = false;
            }
        }
        return isEqual;
    }

    @Override
    public String toString() {
        return "HeapRepresentation(" + this.references.length + " references, " + this.primitives.length + " primitive elements)";
    }

    // TODO:  Remove this when we can return billable size and expose referenced objects.
    public Object[] buildInternalsArray() {
        Object[] internals = new Object[this.primitives.length + this.references.length];
        System.arraycopy(this.primitives, 0, internals, 0, this.primitives.length);
        System.arraycopy(this.references, 0, internals, this.primitives.length, this.references.length);
        return internals;
    }
}
