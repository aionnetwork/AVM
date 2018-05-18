package org.aion.avm.core.classgeneration;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Used to generate simple "stub" classes, dynamically.
 * These classes only have a single constructor: takes a single Object argument and passes it to the superclass constructor.
 * These classes are useful for cases where we don't need special behaviour, but we do need a type (exception catching, parametric polymorphism, etc).
 */
public class StubGenerator {
    private static final int CLASS_VERSION = 54;
    private static final String INIT_NAME = "<init>";
    private static final String ONE_ARG_DESCRIPTOR = "(Ljava/lang/Object;)V";
    
    /**
     * Generates and returns the bytecode for a stub class (only has a 1-Object init method).
     * Note that class names here are always in the slash style:  "java/lang/Object".
     * 
     * @param name The name of the class to generate.
     * @param superName The name of the superclass.
     * @return The bytecode for the new class.
     */
    public static byte[] generateClass(String name, String superName) {
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // This class only exists to be a type - the superclasses always do everything.
        // (common access for all classes we generate - public and "super", meaning post-1.1 invokestatic).
        int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
        // We ignore generics, so null signature.
        String signature = null;
        // We implement no interfaces.
        String[] interfaces = new String[0];
        out.visit(CLASS_VERSION, access, name, signature, superName, interfaces);
        
        // Generate the singular constructor.
        MethodVisitor methodVisitor = out.visitMethod(Opcodes.ACC_PUBLIC, INIT_NAME, ONE_ARG_DESCRIPTOR, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, INIT_NAME, ONE_ARG_DESCRIPTOR, false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
        
        // Finish this and dump the bytes.
        out.visitEnd();
        return out.toByteArray();
    }
}
