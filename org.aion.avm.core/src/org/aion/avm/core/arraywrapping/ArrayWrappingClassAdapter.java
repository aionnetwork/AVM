package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.signature.*;
import org.objectweb.asm.util.*;


public class ArrayWrappingClassAdapter extends ClassVisitor {

    public ArrayWrappingClassAdapter(ClassVisitor visitor) {
        super(Opcodes.ASM6, visitor);
    }

    @Override
    public FieldVisitor visitField(int access,
            java.lang.String name,
            java.lang.String descriptor,
            java.lang.String signature,
            java.lang.Object value)
    {
        String desc = ArrayWrappingBytecodeFactory.getWrapperName(descriptor);

        if (desc.startsWith("L")){
            desc = desc + ";";
        }

        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions)
    {

        String desc = descriptor;
        desc = ArrayWrappingBytecodeFactory.updateMethodDesc(descriptor);

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        return new ArrayWrappingMethodAdapter(mv, access, name, desc);
    }

}
