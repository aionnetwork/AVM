package org.aion.avm.core;

import org.aion.avm.utilities.Utilities;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class PrivateInterfaceMethodTest {

    @Test
    public void testPrivateMethod() {
        Set<String> methods = new HashSet<>();

        byte[] bytes = Utilities.loadRequiredResourceAsBytes(PrivateInterfaceMethod.class.getName().replaceAll("\\.", "/") + ".class");
        ClassReader cr = new ClassReader(bytes);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                methods.add(name);
                return mv;
            }
        };
        cr.accept(cv, 0);


        assertTrue(methods.contains("init"));
    }
}
