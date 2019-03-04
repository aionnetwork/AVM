package org.aion.avm.tooling.abi;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.StringJoiner;

public class ABICompilerMethodVisitor extends MethodVisitor {
    private int access;
    private String methodName;
    private String methodDescriptor;
    private boolean isCallable = false;
    private boolean isFallback = false;

    public boolean isCallable() {
        return isCallable;
    }

    // Should only be called on public static methods
    public String getSignature() {
        String signature = "";

        StringJoiner arguments = new StringJoiner(", ");
        for (Type type : Type.getArgumentTypes(this.methodDescriptor)) {
            arguments.add(type.getClassName());
        }
        String returnType = Type.getReturnType(this.methodDescriptor).getClassName();
        signature = ("public ")
                + ("static ")
                + returnType + " "
                + this.methodName + "("
                + arguments.toString()
                + ")";
        return signature;
    }

    public ABICompilerMethodVisitor(int access, String methodName, String methodDescriptor, MethodVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.access = access;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        boolean isPublic = (this.access & Opcodes.ACC_PUBLIC) != 0;
        boolean isStatic = (this.access & Opcodes.ACC_STATIC) != 0;
        if(Type.getType(descriptor).getClassName().equals(Callable.class.getName())) {
            if (!isPublic) {
                throw new AnnotationException("@Callable methods must be public", methodName);
            }
            if (!isStatic) {
                throw new AnnotationException("@Callable methods must be public", methodName);
            }
            isCallable = true;
            return null;
        } else if (Type.getType(descriptor).getClassName().equals(Fallback.class.getName())) {
            if (!isStatic) {
                throw new AnnotationException("Fallback function must be static", methodName);
            }
            if (Type.getReturnType(methodDescriptor) != Type.VOID_TYPE) {
                throw new AnnotationException(
                    "Function annotated @Fallback must have void return type", methodName);
            }
            if (Type.getArgumentTypes(methodDescriptor).length != 0) {
                throw new AnnotationException(
                    "Function annotated @Fallback cannot take arguments", methodName);
            }
            isFallback = true;
            return null;
        }
        else {
            return super.visitAnnotation(descriptor, visible);
        }
    }

    public boolean isFallback() {
        return isFallback;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDescriptor() {
        return methodDescriptor;
    }
}