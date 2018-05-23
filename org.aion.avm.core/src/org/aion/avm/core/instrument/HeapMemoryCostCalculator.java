package org.aion.avm.core.instrument;

import org.aion.avm.core.ClassHierarchyForest;
import org.aion.avm.core.Forest;
import org.aion.avm.core.Forest.Node;
import org.aion.avm.core.util.Helpers;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heap memory is allocated at the new object creation. This class provides a map of every class' instance size.
 * Every time an object is created by the "new" instruction, a piece of heap memory of this size is allocated.
 * The accordingly memory usage cost is then charged on the Energy meter.
 *
 * The hashmap stores one instance's heap allocation size of every class.
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
     * A map that stores the instance size of every class.
     * Key - class name
     * Value - the instance/heap size of the class
     */
    private Map<String, Integer> classHeapSizeMap;

    /**
     * Constructor
     */
    public HeapMemoryCostCalculator() {
        classHeapSizeMap = new HashMap<>();
    }

    /**
     * return the map of the class names to their instance sizes
     * @return the hash map that stores the calculated instance sizes of the classes
     */
    public Map<String, Integer> getClassHeapSizeMap() {
        return classHeapSizeMap;
    }

    /**
     * A helper method that calculates the instance size of one class and record it in the "classHeapSizeMap".
     * @param classBytes input class bytecode stream.
     *
     * Note, this method is called from the top to bottom of the class inheritance hierarchy. Such that, it can
     * be assumed that the parent classes' heap size is already in the map.
     */
    private void calcInstanceSizeOfOneClass(byte[] classBytes) {
        if (classHeapSizeMap == null) {
            throw new IllegalStateException("HeapMemoryCostCalculator does not have the classHeapSizeMap.");
        }

        // read in, build the classNode
        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(classBytes);
        cr.accept(classNode, ClassReader.SKIP_DEBUG);

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

        // read the declared fields in the current class, add the size of each according to the FieldType
        List<FieldNode> fieldNodes = classNode.fields;
        for (FieldNode fieldNode : fieldNodes) {
            switch (fieldNode.desc.charAt(0)) {
                // FieldType -- BasicType, ObjectType, ArrayType
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
                    // ObjectType
                    heapSize += FieldTypeSizeInBits.OBJECTREF.getVal();
                    break;
                }
                case '[': {
                    // ArrayType; class object creation only allocates a ref in the heap;
                    // and later the bytecode "NEWARRAY / ANEWARRAY" allocates the memory for each element.
                    heapSize += FieldTypeSizeInBits.OBJECTREF.getVal();
                    break;
                }
                default: {
                    throw new IllegalStateException("field has an invalid FieldType");
                }
            }
        }

        // convert the size to number of bytes and add to classHeapSizeMap
        classHeapSizeMap.put(classNode.name, heapSize / 8);
    }

    /**
     * Calculate the instance sizes of classes and record them in the "classHeapInfoMap".
     * This method is called to calculate the heap size of classes that belong to one Dapp, at the deployment time.
     * @param classHierarchy the pre-constructed class hierarchy forest
     * @param runtimeObjectSizes the pre-constructed map of the runtime and java.lang.* classes to their instance size
     */
    public void calcClassesInstanceSize(ClassHierarchyForest classHierarchy, Map<String, Integer> runtimeObjectSizes) {
        // get the root nodes list of the class hierarchy
        Collection<Node<String, byte[]>> rootClasses = classHierarchy.getRoots();

        // calculate for each tree in the class hierarchy
        for (Node<String, byte[]> rootClass : rootClasses) {
            // rootClass is one of the runtime or java.lang.* classes and 'runtimeObjectSizes' map already has its size.
            // copy rootClass size to classHeapSizeMap
            final String splashName = Helpers.fulllyQualifiedNameToInternalName(rootClass.getId());
            classHeapSizeMap.put(splashName, runtimeObjectSizes.get(splashName));
        }
        final var visitor = new Forest.Visitor<String, byte[]>() {
            @Override
            public void onVisitRoot(Node<String, byte[]> root) {
            }

            @Override
            public void onVisitNotRootNode(Node<String, byte[]> node) {
                calcInstanceSizeOfOneClass(node.getContent());
            }

            @Override
            public void afterAllNodesVisited() {
            }
        };
        classHierarchy.walkPreOrder(visitor);
    }
}
