package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.ClassToolchain;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ArrayWrappingClassAdapterRef extends ClassToolchain.ToolChainClassVisitor {

    public String className;

    public ArrayWrappingClassAdapterRef() {
        super(Opcodes.ASM6);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        return new ArrayWrappingMethodAdapterRef(access, name, descriptor, signature, exceptions, mv, className);
    }
}