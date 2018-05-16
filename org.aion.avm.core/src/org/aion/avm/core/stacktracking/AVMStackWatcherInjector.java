package org.aion.avm.core.stacktracking;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AVMStackWatcherInjector extends MethodVisitor {

    public AVMStackWatcherInjector(MethodVisitor out) {
        super(Opcodes.ASM6, out);

        // TODO: Junhan, refactor this class using method visitor
    }
}
