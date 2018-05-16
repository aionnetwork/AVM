package org.aion.avm.core.instrument;

import org.aion.avm.core.util.ClassHierarchyForest;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class ClassMetering extends ClassVisitor {
    private final String runtimeClassName;
    private ClassHierarchyForest classHierarchy;
    private Map<String, Integer> objectSizes;

    public ClassMetering(ClassVisitor visitor, String runtimeClassName, ClassHierarchyForest classHierarchy, Map<String, Integer> objectSizes) {
        super(Opcodes.ASM6, visitor);

        this.runtimeClassName = runtimeClassName;
        this.classHierarchy = classHierarchy;
        this.objectSizes = objectSizes;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            // TODO: Jeff
        };
    }
}
