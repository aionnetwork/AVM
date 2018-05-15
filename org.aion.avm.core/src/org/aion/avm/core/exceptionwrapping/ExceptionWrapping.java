package org.aion.avm.core.exceptionwrapping;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ExceptionWrapping extends ClassVisitor {

    public ExceptionWrapping(ClassVisitor visitor) {
        super(Opcodes.ASM6, visitor);
    }
}
