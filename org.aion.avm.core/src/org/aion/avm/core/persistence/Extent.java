package org.aion.avm.core.persistence;

import java.util.Arrays;


/**
 * The Extent is the lowest-level representation of object data which still has the logical distinction of data versus objects.
 * This means that it is the ideal interchange format between the graph structure serializer and higher-local object instance serializers.
 * 
 * The hashCode(), equals(Object), and toString() were implemented since these needed to be compared to check if an instance changed and the
 * others seemed simple enough.
 */
public class Extent {
    public final byte[] data;
    public final INode[] references;

    public Extent(byte[] data, INode[] references) {
        this.data = data;
        this.references = references;
    }

    public int getBillableSize() {
        int primitiveSizeInBytes = this.data.length;
        int referenceAbstractSizeInBytes = ByteSizes.REFERENCE * this.references.length;
        return primitiveSizeInBytes + referenceAbstractSizeInBytes;
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(this.data);
        hash ^= this.references.length;
        for (INode ref : this.references) {
            if (null != ref) {
                // Note that this assumes the INode has a reasonable hashCode implementation, which is probably only true because they are interned.
                hash ^= ref.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (obj instanceof Extent);
        
        if (isEqual) {
            Extent other = (Extent) obj;
            if (Arrays.equals(this.data, other.data) && (this.references.length == other.references.length)) {
                int index = 0;
                while (isEqual && (index < this.references.length)) {
                    // NOTE:  This implementation is based on the assumption that INode instances for the same object are, themselves, the same objects.
                    // This is true since we require that the IObjectGraphStore do this interning.
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
        return "Extent(" + this.references.length + " references, " + this.data.length + " primitive bytes)";
    }
}
