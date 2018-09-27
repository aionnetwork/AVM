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
        int sizeInBytes = this.data.length;
        for (INode node : this.references) {
            sizeInBytes += Extent.getBillableSizeOfReference(node);
        }
        return sizeInBytes;
    }

    /**
     * Returns the billable size of a reference to the object referenced by node.  Note that this is not the billable size of that "object", just referring to it.
     * TODO:  In the future, this needs to be replaced with a fixed cost, no matter the referent type.
     * 
     * @param node The object being referenced.
     * @return The size, in bytes, which should be used in the cost calculation.
     */
    public static int getBillableSizeOfReference(INode node) {
        int sizeInBytes = 0;
        // See issue-147 for more information regarding this interpretation:
        // - null: (int)0.
        // - -1: (int)-1, (long) instanceId (of constant - negative).
        // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
        // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
        // Reason for order of evaluation:
        // - null goes first, since it is easy to detect on either side (and probably a common case).
        // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
        // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
        // - normal references go last (includes those with 0 or >0 instanceIds).
        if (null == node) {
            // Just encoding the null stub constant as an int.
            sizeInBytes += ByteSizes.INT;
        } else {
            sizeInBytes += node.getBillableReferenceSize();
        }
        return sizeInBytes;
    }
}
