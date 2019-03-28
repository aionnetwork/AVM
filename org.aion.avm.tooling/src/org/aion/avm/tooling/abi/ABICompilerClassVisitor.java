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

        // ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData())

        methodVisitor.visitTypeInsn(NEW, "org/aion/avm/userlib/abi/ABIDecoder");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/BlockchainRuntime", "getData", "()[B", false);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/aion/avm/userlib/abi/ABIDecoder", "<init>", "([B)V", false);
        methodVisitor.visitVarInsn(ASTORE, 0);

        Label label1 = new Label();
        methodVisitor.visitLabel(label1);

        // set methodName = ABIDecoder.decodeMethodName(inputBytes);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeMethodName", "()Ljava/lang/String;", false);
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

        Label latestLabel = new Label();

        for (ABICompilerMethodVisitor callableMethod : this.getCallableMethodVisitors()) {

            // latestLabel is the goto label of the preceding if condition
            methodVisitor.visitLabel(latestLabel);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitLdcInsn(callableMethod.getMethodName());
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            latestLabel = new Label();
            methodVisitor.visitJumpInsn(IFEQ, latestLabel);

            // load the various arguments as indicated by the function signature
            Type[] argTypes = Type.getArgumentTypes(callableMethod.getDescriptor());

            if (argTypes.length > 0) {
                methodVisitor.visitVarInsn(ALOAD, 0);
            }

            for (int i = 0; i < argTypes.length; i++) {
                methodVisitor.visitInsn(DUP);
                if (argTypes[i] == Type.BYTE_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneByte", "()B", false);
                } else if (argTypes[i] == Type.BOOLEAN_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneBoolean", "()Z", false);
                } else if (argTypes[i] == Type.CHAR_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneCharacter", "()C", false);
                } else if (argTypes[i] == Type.SHORT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneShort", "()S", false);
                } else if (argTypes[i] == Type.INT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneInteger", "()I", false);
                } else if (argTypes[i] == Type.LONG_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneLong", "()J", false);
                } else if (argTypes[i] == Type.FLOAT_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneFloat", "()F", false);
                } else if (argTypes[i] == Type.DOUBLE_TYPE) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneDouble", "()D", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.BYTE_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneByteArray", "()[B", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.BOOLEAN_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneBooleanArray", "()[Z", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.CHAR_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneCharacterArray", "()[C", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.SHORT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneShortArray", "()[S", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.INT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneIntegerArray", "()[I", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.LONG_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneLongArray", "()[J", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.FLOAT_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneFloatArray", "()[F", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.DOUBLE_TYPE, 1)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneDoubleArray", "()[D", false);
                } else if (isString(argTypes[i])) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneString", "()Ljava/lang/String;", false);
                } else if (isAddress(argTypes[i])) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneAddress", "()Lorg/aion/avm/api/Address;", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.BYTE_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DByteArray", "()[[B", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.BOOLEAN_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DBooleanArray", "()[[Z", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.CHAR_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DCharacterArray", "()[[C", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.SHORT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DShortArray", "()[[S", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.INT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DIntegerArray", "()[[I", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.LONG_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DLongArray", "()[[J", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.FLOAT_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DFloatArray", "()[[F", false);
                } else if (isArrayOfTypeAndDimensions(argTypes[i], Type.DOUBLE_TYPE, 2)) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOne2DDoubleArray", "()[[D", false);
                } else if (argTypes[i].getSort() == Type.ARRAY && argTypes[i].getDimensions() == 1 && isString(argTypes[i].getElementType())) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneStringArray", "()[Ljava/lang/String;", false);
                } else if (argTypes[i].getSort() == Type.ARRAY && argTypes[i].getDimensions() == 1 && isAddress(argTypes[i].getElementType())) {
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/aion/avm/userlib/abi/ABIDecoder", "decodeOneAddressArray", "()[Lorg/aion/avm/api/Address;", false);
                } else {
                    throw new ABICompilerException("Need to decode an unsupported ABI type");
                }
                visitSwap(methodVisitor, argTypes[i]);
            }

            if (argTypes.length > 0) {
                methodVisitor.visitInsn(POP);
            }
            // if void return type, invoke function and return empty byte array,
            // else call ABIEncoder for the appropriate element type
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
        methodVisitor.visitLocalVariable("decoder", "Lorg/aion/avm/userlib/abi/ABIDecoder;", null, label1, lastLabel, 0);
        methodVisitor.visitLocalVariable("methodName", "Ljava/lang/String;", null, label2, lastLabel, 1);
        methodVisitor.visitMaxs(2, 3);
        methodVisitor.visitEnd();
    }

    private void visitSwap(MethodVisitor mv, Type topType) {
        if (topType.getSize() == 1) {
            mv.visitInsn(SWAP);
        } else {
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(POP2);
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
