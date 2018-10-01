package org.aion.avm.core.persistence;


/**
 * The Extent is the lowest-level representation of object data which still has the logical distinction of data versus objects.
 * This means that it is the ideal interchange format between the graph structure serializer and higher-local object instance serializers.
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
}
