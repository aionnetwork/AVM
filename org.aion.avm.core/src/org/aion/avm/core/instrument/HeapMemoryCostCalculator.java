package org.aion.avm.core.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.List;

/**
 * Heap memory is allocated at the new object creation. This class provides a map of every class' instance size.
 * Every time an object is created by the "new" instruction, a piece of heap memory of this size is allocated.
 * The accordingly memory usage cost is then charged on the Energy meter.
 *
 * The hashmap stores one instance's heap allocation size of every classes, including,
 *   1. runtime and shadowing classes, of which the instance sizes are fixed;
 *   2. user's classes, of which the instance sizes are calculated at deployment.
 *
 * Every instance has a copy of the class fields allocated in the heap.
 * The class fields include the ones declared in this class and its all superclasses.
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
     * A hashmap that stores the instance size of every classes.
     * Key - class name
     * Value - the heapMemoryInfo of the class
     */
    private HashMap<String, Integer> classHeapSizeMap;

    /**
     * Constructor
     */
    public HeapMemoryCostCalculator() {
        classHeapSizeMap = new HashMap<>();
    }

    /**
     * Calculate the instance size of one class and record it in the "classHeapSizeMap".
     * @param classBytes input class bytecode stream.
     *
     * Note, this method is called from the top to bottom of the class inheritance hierarchy. Such that, it can
     * be assumed that the parent classes' heap size is already in the map.
     */
    public void calcInstanceSizeOfOneClass(byte[] classBytes) {
        if (classHeapSizeMap == null) {
            throw new IllegalStateException("HeapMemoryCostCalculator does not have the classHeapSizeMap.");
        }

        // read in, build the classNode
        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(classBytes);
        cr.accept(classNode, 0);

        // read the class name; check if it is already in the classHeapInfoMap
        if (classHeapSizeMap.containsKey(classNode.name)) {
            return;
        }

        // calculate it if not in the classHeapInfoMap
        int heapSize = 0;

        // get the parent classes, copy the fieldsMap
        if (classHeapSizeMap.containsKey(classNode.superName)) {
            heapSize += classHeapSizeMap.get(classNode.superName);
        }
        else {
            throw new IllegalStateException("A parent class is not processed by HeapMemoryCostCalculator.");
        }

        // read the declared fields in the current class, add the size of each.
        List<FieldNode> fieldNodes = classNode.fields;
        for (FieldNode fieldNode : fieldNodes) {
            switch (fieldNode.desc.charAt(0)) {
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
                    // Array field; class object creation only allocates a ref in the heap;
                    // and later the bytecode "NEWARRAY / ANEWARRAY" allocates the memory for each element.
                    heapSize += FieldTypeSizeInBits.OBJECTREF.getVal();
                    break;
                }
                default: {
                    throw new IllegalStateException("field has an invalid d");
                }
            }
        }

        // convert the size to number of bytes and add to classHeapSizeMap
        classHeapSizeMap.put(classNode.name, heapSize / 8);
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
