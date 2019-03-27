package org.aion.avm.tooling.abi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aion.avm.api.Address;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class ABICompilerClassVisitor extends ClassVisitor {
    private boolean hasMainMethod = false;
    private String className;
    private String fallbackMethodName = "";
    private List<ABICompilerMethodVisitor> methodVisitors = new ArrayList<>();
    private List<ABICompilerMethodVisitor> callableMethodVisitors = new ArrayList<>();
    private List<String> callableSignatures = new ArrayList<>();

    public ABICompilerClassVisitor(ClassWriter cw) {
        super(Opcodes.ASM6, cw);
    }

    public List<String> getCallableSignatures() {
        return callableSignatures;
    }

    public List<ABICompilerMethodVisitor> getCallableMethodVisitors() {
        return callableMethodVisitors;
    }

    private void postProcess() {
        boolean foundFallback = false;

        // We have to make a second pass to create the list of callables
        Set<String> callableNames = new HashSet<String>();
        for (ABICompilerMethodVisitor mv : methodVisitors) {
            if (mv.isCallable()) {
                callableSignatures.add(mv.getPublicStaticMethodSignature());
                callableMethodVisitors.add(mv);
                if(callableNames.contains(mv.getMethodName())) {
                    throw new ABICompilerException("Multiple @Callable methods with the same name", mv.getMethodName());
                } else {
                    callableNames.add(mv.getMethodName());
                }
            }
            if (mv.isFallback()) {
                if(!foundFallback) {
                    fallbackMethodName = mv.getMethodName();
                    foundFallback = true;
                }
                else {
                    throw new ABICompilerException("Only one function can be marked @Fallback", mv.getMethodName());
                }
            }
        }
    }

    @Override
    public void visit(int version, int access, java.lang.String name, java.lang.String signature, java.lang.String superName, java.lang.String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("main") && ((access & Opcodes.ACC_PUBLIC) != 0)) {
            hasMainMethod = true;
        }
        ABICompilerMethodVisitor mv = new ABICompilerMethodVisitor(access, name, descriptor,
                super.visitMethod(access, name, descriptor, signature, exceptions));
            methodVisitors.add(mv);
        return mv;
    }

    @Override
    public void visitEnd() {
        postProcess();
        if (!hasMainMethod) {
            addMainMethod();
        }
        super.visitEnd();
    }

    private boolean hasFallback() {
        return !fallbackMethodName.isEmpty();
    }

    private void addMainMethod() {
        // write function signature
        MethodVisitor methodVisitor =
            super.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "()[B", null, null);
        methodVisitor.visitCode();

        // set inputBytes = BlockchainRuntime.getData();
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/BlockchainRuntime", "getData", "()[B", false);
        methodVisitor.visitVarInsn(ASTORE, 0);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);

        // set methodName = ABIDecoder.decodeMethodName(inputBytes);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIDecoder", "decodeMethodName", "([B)Ljava/lang/String;", false);
        methodVisitor.visitVarInsn(ASTORE, 1);
        Label label2 = new Label();

        // if methodName is null, call fallback(), or return empty byte array

        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitJumpInsn(IFNONNULL, label2);
        if(hasFallback()) {
            methodVisitor.visitMethodInsn(
                INVOKESTATIC, className, fallbackMethodName, "()V", false);
        }
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(label2);
        // set argValues = ABIDecoder.decodeArguments(BlockchainRuntime.getData());

        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIDecoder", "decodeArguments", "([B)[Ljava/lang/Object;", false);
        methodVisitor.visitVarInsn(ASTORE, 2);

        Label latestLabel = new Label();
        Label firstLabel = latestLabel;

        for (ABICompilerMethodVisitor callableMethod : this.getCallableMethodVisitors()) {

            // latestLabel is the goto label of the preceding if condition
            methodVisitor.visitLabel(latestLabel);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitLdcInsn(callableMethod.getMethodName());
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            latestLabel = new Label();
            methodVisitor.visitJumpInsn(IFEQ, latestLabel);

            // load the various arguments as indicated by the function signature, casting them as needed
            Type[] argTypes = Type.getArgumentTypes(callableMethod.getDescriptor());

            for (int i = 0; i < argTypes.length; i++) {
                methodVisitor.visitVarInsn(ALOAD, 2);
                methodVisitor.visitIntInsn(BIPUSH, i);
                methodVisitor.visitInsn(AALOAD);
                castArgumentType(methodVisitor, argTypes[i]);
            }

            // if void return type, invoke function and return empty byte array,
            // else return ABIEncoder.encodeOneObject(<methodName>(<arguments>));
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, callableMethod.getMethodName(), callableMethod.getDescriptor(), false);
            Type returnType = Type.getReturnType(callableMethod.getDescriptor());
            if (returnType != Type.VOID_TYPE) {
                if (returnType == Type.BYTE_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneByte","(B)[B", false);
                } else if (returnType == Type.BOOLEAN_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneBoolean","(Z)[B", false);
                } else if (returnType == Type.CHAR_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneCharacter","(C)[B", false);
                } else if (returnType == Type.SHORT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneShort","(S)[B", false);
                } else if (returnType == Type.INT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneInteger","(I)[B", false);
                } else if (returnType == Type.LONG_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneLong","(J)[B", false);
                } else if (returnType == Type.FLOAT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneFloat","(F)[B", false);
                } else if (returnType == Type.DOUBLE_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneDouble","(D)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.BYTE_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneByteArray","([B)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.BOOLEAN_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneBooleanArray", "([Z)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.CHAR_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneCharacterArray", "([C)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.SHORT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneShortArray", "([S)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.INT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneIntegerArray", "([I)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.LONG_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneLongArray", "([J)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.FLOAT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneFloatArray", "([F)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.DOUBLE_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneDoubleArray", "([D)[B", false);
                } else if (isString(returnType)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneString", "(Ljava/lang/String;)[B", false);
                } else if (isAddress(returnType)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneAddress", "(Lorg/aion/avm/api/Address;)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.BYTE_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DByteArray", "([[B)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.BOOLEAN_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DBooleanArray", "([[Z)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.CHAR_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DCharacterArray", "([[C)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.SHORT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DShortArray", "([[S)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.INT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DIntegerArray", "([[I)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.FLOAT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DFloatArray", "([[F)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.LONG_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DLongArray", "([[J)[B", false);
                } else if (isArrayOfTypeAndDimensions(returnType, Type.DOUBLE_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOne2DDoubleArray", "([[D)[B", false);
                } else if (returnType.getSort() == Type.ARRAY && returnType.getDimensions() == 1 && isString(returnType.getElementType())) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneStringArray", "([Ljava/lang/String;)[B", false);
                } else if (returnType.getSort() == Type.ARRAY && returnType.getDimensions() == 1 && isAddress(returnType.getElementType())) {
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeOneAddressArray", "([Lorg/aion/avm/api/Address;)[B", false);
                }
            } else {
                methodVisitor.visitInsn(ICONST_0);
                methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
            }
            methodVisitor.visitInsn(ARETURN);
        }

        // this latestLabel is the catch-all else, we call the fallback() if it exists,
        // else we revert the transaction
        methodVisitor.visitLabel(latestLabel);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"[B", "java/lang/String", "[Ljava/lang/Object;"}, 0, null);
        if (hasFallback()) {
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC, className, fallbackMethodName, "()V", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
            methodVisitor.visitInsn(ARETURN);
        } else {
            methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/BlockchainRuntime", "revert", "()V", false);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ARETURN);
        }

        Label lastLabel = new Label();
        methodVisitor.visitLabel(lastLabel);
        methodVisitor.visitLocalVariable("inputBytes", "[B", null, label1, lastLabel, 0);
        methodVisitor.visitLocalVariable("methodName", "Ljava/lang/String;", null, label2, lastLabel, 1);
        methodVisitor.visitLocalVariable("argValues", "[Ljava/lang/Object;", null, firstLabel, lastLabel, 2);
        methodVisitor.visitMaxs(2, 3);
        methodVisitor.visitEnd();
    }

    private void castArgumentType(MethodVisitor mv, Type t) {
        switch (t.getSort()) {
            case Type.BOOLEAN:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case Type.INT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case Type.BYTE:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                break;
            case Type.CHAR:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                break;
            case Type.SHORT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                break;
            case Type.LONG:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case Type.DOUBLE:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case Type.FLOAT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case Type.OBJECT:
            case Type.ARRAY:
                mv.visitTypeInsn(CHECKCAST, t.getInternalName());
                break;
        }
    }

    private boolean isArrayOfTypeAndDimensions(Type arrayType, Type expectedElementType, int expectedDimensions) {
        return arrayType.getSort() == Type.ARRAY && arrayType.getDimensions() == expectedDimensions && arrayType.getElementType() == expectedElementType;
    }

    private boolean isString(Type t) {
        return t.getClassName().equals(String.class.getName());
    }

    private boolean isAddress(Type t) {
        return t.getClassName().equals(Address.class.getName());
    }

    public boolean addedMainMethod() {
        return !hasMainMethod;
    }
}
