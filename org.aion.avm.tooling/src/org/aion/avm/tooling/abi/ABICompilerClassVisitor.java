package org.aion.avm.tooling.abi;

import java.util.ArrayList;
import java.util.List;
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

        for (ABICompilerMethodVisitor mv : methodVisitors) {
            if (mv.isCallable()) {
                callableSignatures.add(mv.getPublicStaticMethodSignature());
                callableMethodVisitors.add(mv);
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
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/ABIDecoder", "decodeMethodName", "([B)Ljava/lang/String;", false);
        methodVisitor.visitVarInsn(ASTORE, 1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);

        // set argValues = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/BlockchainRuntime", "getData", "()[B", false);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/ABIDecoder", "decodeArguments", "([B)[Ljava/lang/Object;", false);
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

            // return ABIEncoder.encodeOneObject(<methodName>(<arguments>));
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, callableMethod.getMethodName(), callableMethod.getDescriptor(), false);
            castReturnType(methodVisitor, Type.getReturnType(callableMethod.getDescriptor()));
            methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/api/ABIEncoder", "encodeOneObject", "(Ljava/lang/Object;)[B", false);
            methodVisitor.visitInsn(ARETURN);
        }

        // this latestLabel is the catch-all else, we just return null
        methodVisitor.visitLabel(latestLabel);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"[B", "java/lang/String", "[Ljava/lang/Object;"}, 0, null);
        if (!fallbackMethodName.isEmpty()) {
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC, className, fallbackMethodName, "()V", false);
        }
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ARETURN);
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

    private void castReturnType(MethodVisitor mv, Type t) {
        switch (t.getSort()) {
            case Type.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case Type.INT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case Type.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case Type.CHAR:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case Type.SHORT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case Type.LONG:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case Type.DOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
            case Type.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case Type.OBJECT:
            case Type.ARRAY:
                break;
        }
    }
}
