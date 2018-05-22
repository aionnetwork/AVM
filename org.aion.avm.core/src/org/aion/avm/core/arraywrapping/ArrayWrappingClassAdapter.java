package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ArrayWrappingClassAdapter extends ClassVisitor {

    private Logger logger = LoggerFactory.getLogger(ArrayWrappingClassAdapter.class);

    private String helperClass;

    public ArrayWrappingClassAdapter(ClassVisitor visitor) {
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

        //logger.info("Method: access = {}, name = {}, descriptor = {}, signature = {}, exceptions = {}", access, name, descriptor, signature, exceptions);

        return new ArrayWrappingMethodAdapter(mv, access, name, descriptor);
    }
}
