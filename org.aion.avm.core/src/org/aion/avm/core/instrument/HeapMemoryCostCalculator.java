package org.aion.avm.core.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.List;

/**
 * Heap memory is allocated at the new object creation. This class provides a map of every class' instance size.
 * Every time an object is created by the "new" instruction, a piece of heap memory of this size is allocated.
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
    public enum FieldTypeSizeInBits {
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

        FieldTypeSizeInBits(long val) {
            this.val = val;
        }

        public long getVal() {
            return val;
        }
    }

    /**
     * A data structure that stores the heap size and the fields info of one class.
     * To decide the heap size, the derived class need to compare its declared fields to the inherited fields that are
     * from the parent class. For this purpose, all the declared and inherited fields are recorded in this data struct.
     */
    public class HeapMemoryInfo {
        /**
         * The instance size of a class. In bytes.
         */
        private long heapSize = 0; // in bytes

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
        private HashMap<String, Type> fieldsMap;

        /**
         * Constructor.
         */
        private HeapMemoryInfo() {
            fieldsMap = new HashMap<>();
        }

        /**
         * Set the heapSize value of the class.
         * @param heapSize the heapSize value.
         */
        public void setHeapSize(long heapSize) {
            if (heapSize >= 0) {
                this.heapSize = heapSize;
            }
            else {
                throw new IllegalArgumentException("The heapSize of a class cannot be negative.");
            }
        }

        /**
         * Get the heapSize value of the class.
         * @return the heapSize of the class.
         */
        public long getHeapSize() {
            return heapSize;
        }

        /**
         * Add one field to the fields list of the class.
         * @param fieldName A String of the field's name.
         * @param fieldType A String of the field's descriptor.
         */
        public void addToFieldsMap(String fieldName, Type fieldType) {
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
     * Key - class name
     * Value - the heapMemoryInfo of the class
     */
    private HashMap<String, HeapMemoryInfo> classHeapInfoMap;

    /**
     * Constructor
     */
    public HeapMemoryCostCalculator() {
        classHeapInfoMap = new HashMap<>();
    }

    /**
     *
     */
    private void copyFields(HeapMemoryInfo parentClassInfo, HeapMemoryInfo childClassInfo) {
        if (parentClassInfo == null || childClassInfo == null
                || parentClassInfo.fieldsMap == null || childClassInfo.fieldsMap == null) {
            throw new IllegalStateException("copyFields without the parent or child's fieldsMap set up.");
        }

        for (String fieldName : parentClassInfo.fieldsMap.keySet()) {
            childClassInfo.fieldsMap.put(fieldName, parentClassInfo.fieldsMap.get(fieldName));
        }
    }

    /**
     * Calculate the instance size of one class and record it in the "classHeapInfoMap", as long as its fields.
     * @param classBytes input class bytecode stream.
     *
     * Note, this method is called from the top to bottom of the class inheritance hierarchy tree. Such that it can
     * be assumed that the parent classes' heapMemoryInfo is already in the map.
     */
    public void calcInstanceSizeOfOneClass(byte[] classBytes) {
        if (classHeapInfoMap == null) {
            throw new IllegalStateException("HeapMemoryCostCalculator does not have the classHeapInfoMap.");
        }

        // read in, build the classNode
        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(classBytes);
        cr.accept(classNode, 0);

        // read the class name; check if it is already in the classHeapInfoMap
        if (classHeapInfoMap.containsKey(classNode.name)) {
            return;
        }

        // calculate it if not in the classHeapInfoMap
        HeapMemoryInfo curHeapInfo = new HeapMemoryInfo();

        // get the parent classes, copy the fieldsMap
        if (classHeapInfoMap.containsKey(classNode.superName)) {
            HeapMemoryInfo parentHeapInfo = classHeapInfoMap.get(classNode.superName);
            copyFields(parentHeapInfo, curHeapInfo);
        }
        else {
            throw new IllegalStateException("A parent class is not processed by HeapMemoryCostCalculator.");
        }

        // read the declared fields in the current class, check the duplicates, hiding, etc, and update the fieldMap
        List<FieldNode> fieldNodes = classNode.fields;
        for (FieldNode fieldNode : fieldNodes) {
            /**
             * 1. parent does not have it, add to the fieldMap;
             * 2. parent has it but with a different descriptor. The parent one is hid in this case. Replace it with the new one in fieldMap.
             * 3. parent has it and the descriptor is also the same. It is a duplicated declaration. Do nothing to the fieldMap.
             */
            if (!(curHeapInfo.fieldsMap.containsKey(fieldNode.name)) ||
                    (curHeapInfo.fieldsMap.get(fieldNode.name) != Type.getType(fieldNode.desc))) {
                curHeapInfo.addToFieldsMap(fieldNode.name, Type.getType(fieldNode.desc));
            }
        }

        // walk the fields and sum up the size
        long heapSize = 0;
        for (String fieldName : curHeapInfo.fieldsMap.keySet()) {
            String fieldDesc = curHeapInfo.fieldsMap.get(fieldName).getDescriptor();
            switch (fieldDesc.charAt(0)) {
                case 'Z' : {
                    heapSize += FieldTypeSizeInBits.BOOLEAN.getVal();
                    break;
                }
                case 'B': {
                    heapSize += FieldTypeSizeInBits.BYTE.getVal();
                    break;
                }
                case 'C': {
                    heapSize += FieldTypeSizeInBits.CHAR.getVal();
                    break;
                }
                case 'S': {
                    heapSize += FieldTypeSizeInBits.SHORT.getVal();
                    break;
                }
                case 'I': {
                    heapSize += FieldTypeSizeInBits.INT.getVal();
                    break;
                }
                case 'J': {
                    heapSize += FieldTypeSizeInBits.LONG.getVal();
                    break;
                }
                case 'F': {
                    heapSize += FieldTypeSizeInBits.FLOAT.getVal();
                    break;
                }
                case 'D': {
                    heapSize += FieldTypeSizeInBits.DOUBLE.getVal();
                    break;
                }
                case 'L': {
                    heapSize += FieldTypeSizeInBits.OBJECTREF.getVal();
                    break;
                }
                case '[': {
                    heapSize += FieldTypeSizeInBits.OBJECTREF.getVal();
                    break;
                }
                default: {
                    throw new IllegalStateException("field has an invalid d");
                }
            }

            // convert to byte counts and record it
            curHeapInfo.setHeapSize(heapSize /= 8);
        }
    }

    /**
     * Calculate the instance sizes of classes and record them in the "classHeapInfoMap", as long as their fields.
     *
     * This method is applied with all the classes that may be instantiated during the execution of a smart contract,
     * 1. when JVM starts, apply this method to the java.lang.* classes;
     * 2. when runtime starts, apply this method to the runtime classes;
     * 3. at the deployment of a smart contract, apply this method to all the classes of the contract.
     */
    public void calcClassesInstanceSize() {


        InheritanceHierarchyBuilder.buildHierarchy();

        // To-do - pop from the stack and call calcInstanceSizeOfOneClass
        byte[] classBytes = null;
        calcInstanceSizeOfOneClass(classBytes);
    }


    /**
     * A helper class that builds the hierarchy tree of the classes.
     */
    private static class InheritanceHierarchyBuilder {

        /**
         * build a stack of classes, in which the top ones should not be derived from the bottom ones.
         */
        private static void buildHierarchy() {

        }
    }

}
