package org.aion.avm.core.instrument;

import org.aion.avm.core.util.ClassHierarchyForest;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class ClassMetering extends ClassVisitor {

    private ClassHierarchyForest classHierarchy;
    private Map<String, Integer> objectSizes;

    public ClassMetering(ClassVisitor visitor, ClassHierarchyForest classHierarchy, Map<String, Integer> objectSizes) {
        super(Opcodes.ASM6, visitor);

        this.classHierarchy = classHierarchy;
        this.objectSizes = objectSizes;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        super.visitMethod(access, name, descriptor, signature, exceptions);

        // TODO: Jeff, move the stackwatcher to this package, and plug into this method somehow.
        // Feel free to change the package/class name.
        // Be sure to properly delegate visits to upstream MethodVisitor

        return null;
    }
}
