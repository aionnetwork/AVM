package org.aion.avm.core.stacktracking;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StackTracking extends ClassVisitor {

    public StackTracking(ClassVisitor visitor) {
        super(Opcodes.ASM6, visitor);
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            // TODO: Junhan, move the stackwatcher to this package, and plug into this method somehow.
        };
    }
}
