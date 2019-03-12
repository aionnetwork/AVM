package org.aion.avm.tooling.abi;

import org.aion.avm.api.Address;
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
    public String getPublicStaticMethodSignature() {
        String signature = "";

        StringJoiner arguments = new StringJoiner(", ");
        for (Type type : Type.getArgumentTypes(this.methodDescriptor)) {
            arguments.add(shortenClassName(type.getClassName()));
        }
        String returnType = Type.getReturnType(this.methodDescriptor).getClassName();
        signature = ("public ")
                + ("static ")
                + shortenClassName(returnType) + " "
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
                throw new ABICompilerException("@Callable methods must be public", methodName);
            }
            if (!isStatic) {
                throw new ABICompilerException("@Callable methods must be public", methodName);
            }
            checkArgumentsAndReturnType();
            isCallable = true;
            return null;
        } else if (Type.getType(descriptor).getClassName().equals(Fallback.class.getName())) {
            if (!isStatic) {
                throw new ABICompilerException("Fallback function must be static", methodName);
            }
            if (Type.getReturnType(methodDescriptor) != Type.VOID_TYPE) {
                throw new ABICompilerException(
                    "Function annotated @Fallback must have void return type", methodName);
            }
            if (Type.getArgumentTypes(methodDescriptor).length != 0) {
                throw new ABICompilerException(
                    "Function annotated @Fallback cannot take arguments", methodName);
            }
            isFallback = true;
            return null;
        }
        else {
            return super.visitAnnotation(descriptor, visible);
        }
    }

    private void checkArgumentsAndReturnType() {
        for (Type type : Type.getArgumentTypes(this.methodDescriptor)) {
            if(!isAllowedType(type)) {
                throw new ABICompilerException(
                    type.getClassName() + " is not an allowed parameter type", methodName);
            }
        }
        Type returnType = Type.getReturnType(methodDescriptor);
        if(!isAllowedType(returnType) && returnType != Type.VOID_TYPE) {
            throw new ABICompilerException(
                returnType.getClassName() + " is not an allowed return type", methodName);
        }
    }

    private boolean isAllowedType(Type type) {
        if(isPrimitiveType(type) || isAllowedObject(type)) {
            return true;
        }
        if (type.getSort() == Type.ARRAY) {
            switch(type.getDimensions()) {
                case 1:
                    return isAllowedType(type.getElementType());
                case 2:
                    // We do not allow 2-dimensional arrays of Strings and Addresses
                    return isPrimitiveType(type.getElementType());
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean isPrimitiveType(Type type) {
        switch (type.getSort()) {
            case Type.BYTE:
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                return true;
            default:
                return false;
        }
    }

    private boolean isAllowedObject(Type type) {
        return type.getClassName().equals(String.class.getName())
            || type.getClassName().equals(Address.class.getName());
    }

    private String shortenClassName(String s) {
        if(s.contains(".")) {
            return s.substring(s.lastIndexOf('.') + 1);
        } else {
            return s;
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