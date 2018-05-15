package org.aion.avm.core.instrument;

import java.util.HashMap;

/**
 * Heap memory is allocated at the new object creation. This class provides a map of every class' instance size.
 * At every time an object is created by the "new" instruction, a piece of heap memory of this size is allocated.
 * The accordingly memory usage cost is then charged on the Energy meter.
 *
 * The hashmap stores the instance size of every classes, including,
 *   1. runtime and shadowing classes, of which the instance sizes are fixed;
 *   2. user's classes, of which the instance sizes are calculated at loading time.
 *
 * Note that the instance size is the allocation in the heap for one instance, aka, the class field size.
 * The class field can be declared in the current class, or inherited from the parent class.
 *
 * JVM implementation may distinguish between small and large objects and allocate the small ones in "thread local
 * areas (TLAs)" that is reserved from the heap and given to the Java thread (see JRockit JVM spec). Here we don't consider
 * this variance in JVM implementation, aka, the heap allocation size is counted linearly with tha actual object size.
 */
public class HeapMemoryCostCalculator {

    /**
     * Enum - class field size based on the descriptor / type.
     * Size in bits.
     */
    public enum FieldTypeSize {
        BYTE        (8),
        CHAR        (16),
        SHORT       (16),
        INT         (32),
        LONG        (64),
        FLOAT       (32),
        DOUBLE      (64),
        BOOLEAN     (1),
        OBJECTREF   (64);

        private final long val;

        FieldTypeSize(long val) {
            this.val = val;
        }

        public long getVal() {
            return val;
        }
    }

    private class HeapMemoryInfo {
        /**
         * The instance size of a class. In bytes.
         */
        long heapSize = 0; // in bytes

        /**
         * A Hashmap that stores the class' fields, including the inherited ones.
         * key - field's name
         * value - field's descriptor / type
         *
         * This is needed for the sub-class to decide if more memory is required for its declared fields.
         * 1. If its name is different from any one of its parent class, this is a new field;
         * 2. If the parent has one field that has the same name and the same descriptor, this is not a new field;
         * 3. If the parent has one field that has the same name but different descriptor, that one is hided by the
         *    current one, so the memory size needs to re-calculated -- minus the old one's size and add the new one's.
         */
        private HashMap<String, String> fieldsMap;

        private HeapMemoryInfo() {
            fieldsMap = new HashMap<>();
        }

        private void setHeapSize(long heapSize) {
            if (heapSize >= 0) {
                this.heapSize = heapSize;
            }
            else {
                throw new IllegalArgumentException("The heapSize of a class cannot be negative.");
            }
        }

        // size can be positive or negative.
        private void modifyHeapSize(long size) {
            this.heapSize += size;
        }

        private void addToFieldsMap(String fieldName, String fieldType) {
            if (fieldsMap == null) {
                throw new IllegalStateException("HeapMemoryInfo does not have the fieldsMap.");
            }
            else {
                fieldsMap.put(fieldName, fieldType);
            }
        }
    }

    /**
     * A hashmap that stores the instance size of every classes.
     */
    private HashMap<String, HeapMemoryInfo> classInstanceSizeMap;

    public HeapMemoryCostCalculator() {
        classInstanceSizeMap = new HashMap<>();
    }

    /**
     * Calculate the instance size of one class and record it in the "classInstanceSizeMap", as long as its fields.
     * @param classBytes input class bytecode stream.
     *
     * Note, this method is called from the top to bottom of the class inheritance hierarchy tree. Such that it can
     * be assumed that the parent classes' heapMemoryInfo is already in the hashmap.
     */
    public void calcInstanceSizeOfOneClass(byte[] classBytes) {
        if (classInstanceSizeMap == null) {
            throw new IllegalStateException("HeapMemoryCostCalculator does not have the classInstanceSizeMap.");
        }

        // read the class name; check if it is already in the classInstanceSizeMap

        // calculate it if not in the classInstanceSizeMap

        HeapMemoryInfo heapMemoryInfo = new HeapMemoryInfo();

        // get the parent classes, combine the fields and copy to heapMemoryInfo.fieldsMap (check the hiding, dups, etc)

        // walk the fields and sum up the size
        //heapMemoryInfo.fieldsMap.get("a");

    }


}
