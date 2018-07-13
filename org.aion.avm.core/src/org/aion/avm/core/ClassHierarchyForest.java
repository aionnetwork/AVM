package org.aion.avm.core;

import org.aion.avm.core.dappreading.LoadedJar;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper which maintain the class inheritance relations.
 *
 * There is one hierarchy forest struct per each DApp; and the forest may include multiple trees.
 * The hierarchy forest is to record all the inheritance relationships of the DApp's classes, but not the ones of the runtime
 * or java.lang.* ones. However, some DApp classes can have a parent class that is one of runtime or java.lang.*. For these
 * classes, it is still needed to record their parents in this hierarchy.
 * Because of that, after the hierarchy of a DApp is built, it should contain one or several trees; each tree has a root
 * node representing a class of the runtime or java.lang.*; and besides the root node, all other node in the tree should
 * represent a DApp class.
 */
public final class ClassHierarchyForest extends Forest<String, byte[]> {

    private final LoadedJar loadedJar;

    private String curParentName;
    private boolean isInterface;

    public Map<String, byte[]> toFlatMapWithoutRoots() {
        final var collector = new FlatMapCollector(getNodesCount());
        walkPreOrder(collector);
        return collector.getMap();
    }

    public static ClassHierarchyForest createForestFrom(LoadedJar loadedJar) throws IOException {
        final var forest = new ClassHierarchyForest(loadedJar);
        forest.createForestInternal();
        return forest;
    }

    private ClassHierarchyForest(LoadedJar loadedJar) {
        this.loadedJar = loadedJar;
    }

    private void createForestInternal() throws IOException {
        Map<String, byte[]> classNameToBytes = this.loadedJar.classBytesByQualifiedNames;
        for (Map.Entry<String, byte[]> entry : classNameToBytes.entrySet()) {
            final byte[] klass = entry.getValue();
            analyzeClass(klass);
            if (!isInterface) {
                final var parentNode = new Node<>(curParentName, classNameToBytes.get(curParentName));
                final var childNode = new Node<>(entry.getKey(), klass);
                add(parentNode, childNode);
            }else{
                // Interface will be added into forest as child of Object
                final var parentNode = new Node<>(Object.class.getName(), classNameToBytes.get(Object.class.getName()));
                final var childNode = new Node<>(entry.getKey(), klass);
                add(parentNode, childNode);
            }
        }
    }

    private void analyzeClass(byte[] klass) {
        ClassReader reader = new ClassReader(klass);
        final var codeVisitor = new CodeVisitor();
        reader.accept(codeVisitor, ClassReader.SKIP_FRAMES);
        curParentName = codeVisitor.getParentQualifiedName();
        isInterface = codeVisitor.isInterface();
    }

    private static final class CodeVisitor extends ClassVisitor {
        private String parentQualifiedName;
        private boolean isInterface;

        private CodeVisitor() {
            super(Opcodes.ASM6);
        }

        // todo check nested parent
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            // todo parent may be null if the class is java.lang.Object. Add unit test for such a case
            parentQualifiedName = toQualifiedName(superName);
            isInterface = Opcodes.ACC_INTERFACE == (access & Opcodes.ACC_INTERFACE);
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            super.visitAttribute(attribute);
        }

        private boolean isInterface() {
            return isInterface;
        }

        private String getParentQualifiedName() {
            return parentQualifiedName;
        }

        private static String toQualifiedName(String internalClassName) {
            return internalClassName.replaceAll("/", ".");
        }
    }

    private static final class FlatMapCollector extends VisitorAdapter<String, byte[]> {
        private final Map<String, byte[]> map;

        private FlatMapCollector(int size) {
            map = new HashMap<>(size);
        }

        @Override
        public void onVisitNotRootNode(Node<String, byte[]> node) {
            map.put(node.getId(), node.getContent());
        }

        private Map<String, byte[]> getMap() {
            return Collections.unmodifiableMap(map);
        }
    }
}