package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.signature.*;
import org.objectweb.asm.util.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.*;


public class ArrayWrappingClassAdapterRef extends ClassNode {

    ClassVisitor cv;

    public ArrayWrappingClassAdapterRef(ClassVisitor visitor) {
        super(Opcodes.ASM6);
        cv = visitor;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String mname,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, mname, descriptor, signature, exceptions);

        return new ArrayWrappingMethodAdapterRef(access, name, descriptor, signature, exceptions, mv);
    }

    @Override
    public void visitEnd(){
            accept(cv);
    }

}
