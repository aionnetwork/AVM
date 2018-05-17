package org.aion.avm.core.arraywrapping;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayWrapping extends ClassVisitor {

    private Logger logger = LoggerFactory.getLogger(ArrayWrapping.class);

    private String helperClass;

    public ArrayWrapping(ClassVisitor visitor, String helperClass) {
        super(Opcodes.ASM6, visitor);

        this.helperClass = helperClass;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        logger.info("Method: access = {}, name = {}, descriptor = {}, signature = {}, exceptions = {}", access, name, descriptor, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {

        };
    }
}
