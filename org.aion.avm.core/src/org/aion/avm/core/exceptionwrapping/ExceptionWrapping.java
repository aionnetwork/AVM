package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.core.util.ClassHierarchyForest;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class ExceptionWrapping extends ClassVisitor {

    private ClassHierarchyForest classHierarchy;
    private Map<String, byte[]> generatedClasses;

    public ExceptionWrapping(ClassVisitor visitor, ClassHierarchyForest classHierarchy, Map<String, byte[]> generatedClasses) {
        super(Opcodes.ASM6, visitor);

        this.classHierarchy = classHierarchy;
        this.generatedClasses = generatedClasses;
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
