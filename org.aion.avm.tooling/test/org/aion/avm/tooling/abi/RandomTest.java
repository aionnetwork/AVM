package org.aion.avm.tooling.abi;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.math.BigInteger;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.objectweb.asm.Opcodes.*;

public class RandomTest {
    @Rule
    public AvmRule avmRule = new AvmRule(true);
    private ABICompiler compiler = new ABICompiler();

    private static final long ENERGY_LIMIT = 50_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private Address installTestDApp(byte[] jar) {
        compiler.compile(jar);
        byte[] txData = new CodeAndArguments(compiler.getJarFileBytes(), new byte[0]).encodeToBytes();
        // Deploy.
        TransactionResult createResult =
                avmRule.deploy(
                        avmRule.getPreminedAccount(),
                        BigInteger.ZERO,
                        txData,
                        ENERGY_LIMIT,
                        ENERGY_PRICE)
                        .getTransactionResult();
        assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private Object callStatic(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, arguments);
        TransactionResult result =
                avmRule.call(
                        avmRule.getPreminedAccount(),
                        dapp,
                        BigInteger.ZERO,
                        argData,
                        ENERGY_LIMIT,
                        ENERGY_PRICE)
                        .getTransactionResult();
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertNotNull(result.getReturnData());
        return ABIDecoder.decodeOneObject(result.getReturnData());
    }

    @Test
    public void testArgumentsWithFixedTypesRandomValues() {
        RandomArgumentsGenerator argsGenerator = new RandomArgumentsGenerator();
        argsGenerator.addFixedTypesAndRandomValues(18);

        String className = "RandomClass";

        String methodName = argsGenerator.getRandomString(9);

//        System.out.println(String.format("%s.%s%s --- %s arguments", className , methodName,
//                        RandomTest.getMethodDescriptor(String.join("", argsGenerator.getArgDescriptors())), argsGenerator.getNumberOfArgs()));

        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode(className, createClass(methodName, argsGenerator), new HashMap<>());

        Address dapp = installTestDApp(jar);

        byte[] result = (byte[]) callStatic(dapp, methodName, argsGenerator.getVarArgs());
        byte[] expected = ABIUtil.encodeMethodArguments("",argsGenerator.getVarArgs());
        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void testArgumentsWithRandomTypesRandomValue() {
        int times = 100;
        for(int i = 0; i < times; ++i) {
            testRandom();
        }
    }

    private void testRandom() {
        RandomArgumentsGenerator argsGenerator = new RandomArgumentsGenerator();
        argsGenerator.addRandomTypeAndRandomValues(50);

        String className = "RandomClass";

        String methodName = argsGenerator.getRandomString(9);

//        System.out.println(String.format("%s.%s%s --- %s arguments", className , methodName,
//                RandomTest.getMethodDescriptor(String.join("", argsGenerator.getArgDescriptors())), argsGenerator.getNumberOfArgs()));

        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode(className, createClass(methodName, argsGenerator), new HashMap<>());

        Address dapp = installTestDApp(jar);

        byte[] result = (byte[]) callStatic(dapp, methodName, argsGenerator.getVarArgs());
        byte[] expected = ABIUtil.encodeMethodArguments("",argsGenerator.getVarArgs());
        Assert.assertArrayEquals(expected, result);
    }

    /*
    An example of generated class:
    public class RandomClass {
        @Callable
        public static byte[] nWGIW8DCM(byte v1, double[] v2, char[] v3, boolean v4, int[] v5, float v6, short v7, float[] v8, String v9, char v10) {
            return ABIUtil.encodeMethodArguments("", new Object[]{v1, v2, v3, v4, v5, v6, v7, v8, v9, v10});
        }
    }
    */
    private byte[] createClass(String methodName, RandomArgumentsGenerator argsGenerator) {
        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(V10, ACC_PUBLIC + ACC_SUPER, "RandomClass", null, "java/lang/Object", null);

        // Create method. It's descriptor is random.
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC,
                methodName,
                RandomTest.getMethodDescriptor(String.join("", argsGenerator.getArgDescriptors())),
                null,
                null);
        AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation("Lorg/aion/avm/tooling/abi/Callable;", false);
        annotationVisitor.visitEnd();
        methodVisitor.visitCode();

        // Load the first parameter of ABIUtil.encodeMethodArguments.
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLdcInsn("");

        // Create an Object[] with the same size as the arguments.
        // new Object[]{v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11 ...}
        methodVisitor.visitIntInsn(BIPUSH, argsGenerator.getArgTypes().length);
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        int index = 0;
        for(int i = 0; i < argsGenerator.getArgDescriptors().length; ++i, ++index) {
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitIntInsn(BIPUSH, i);
            // Load one local variable
            loadLocalVariableToStack(methodVisitor, argsGenerator.getArgTypes()[i], index);
            // LONG is stored in two local variables. Thus, we need to increment index by one more.
            // The same for DOUBLE.
            if(argsGenerator.getArgTypes()[i] == Type.LONG || argsGenerator.getArgTypes()[i] == Type.DOUBLE) index++;
            // Cast primitive types to their corresponding Object types.
            castArgumentType(methodVisitor, argsGenerator.getArgTypes()[i]);
            // Store this object generated from the method argument to Object[].
            methodVisitor.visitInsn(AASTORE);
        }

        // Finally,
        // return ABIUtil.encodeMethodArguments("", new Object[]{v1, v2, v3, v4, v5, ...})
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/aion/avm/userlib/abi/ABIEncoder", "encodeMethodArguments", "(Ljava/lang/String;[Ljava/lang/Object;)[B", false);
        methodVisitor.visitInsn(ARETURN);

        Label label1 = new Label();
        methodVisitor.visitLabel(label1);

        // Create local variables for the method arguments.
        index = 0;
        for(int i = 0; i < argsGenerator.getArgDescriptors().length; ++i, ++index) {
            //System.out.println(String.format("%s %s %s", argsGenerator.getArgTypes()[i], getTypeDescriptor(argsGenerator.getArgTypes()[i]), argsGenerator.getVarArgs()[i]));
            methodVisitor.visitLocalVariable("v" + (i + 1), getTypeDescriptor(argsGenerator.getArgTypes()[i]), null, label0, label1, index);
            if(argsGenerator.getArgTypes()[i] == Type.LONG || argsGenerator.getArgTypes()[i] == Type.DOUBLE) index++;
        }

        methodVisitor.visitMaxs(6, index);
        methodVisitor.visitEnd();

        return classWriter.toByteArray();
    }

    private void loadLocalVariableToStack(MethodVisitor methodVisitor, Type type, int index) {
        switch (type) {
            case BYTE:
            case BOOLEAN:
            case CHAR:
            case SHORT:
            case INT:
                methodVisitor.visitVarInsn(ILOAD, index);
                break;
            case LONG:
                methodVisitor.visitVarInsn(LLOAD, index);
                break;
            case FLOAT:
                methodVisitor.visitVarInsn(FLOAD, index);
                break;
            case DOUBLE:
                methodVisitor.visitVarInsn(DLOAD, index);
                break;
            default:
                //other
                methodVisitor.visitVarInsn(ALOAD, index);
                break;
        }
    }

    private void castArgumentType(MethodVisitor methodVisitor, Type type) {
        switch (type) {
            case BYTE:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case BOOLEAN:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case CHAR:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case SHORT:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case INT:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case LONG:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case FLOAT:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case DOUBLE:
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
            default:
                //other
                break;
        }
    }

    private static String getMethodDescriptor(String argumentsDescriptor) {
        return String.format("(%s)[B", argumentsDescriptor);
    }

    private String getTypeDescriptor(Type type) {
        String t = "";
        switch (type) {
            case BYTE:
                t = "B";
                break;
            case BOOLEAN:
                t = "Z";
                break;
            case CHAR:
                t = "C";
                break;
            case SHORT:
                t = "S";
                break;
            case INT:
                t = "I";
                break;
            case LONG:
                t = "J";
                break;
            case FLOAT:
                t = "F";
                break;
            case DOUBLE:
                t = "D";
                break;
            case BYTE_ARRAY:
                t = "[B";
                break;
            case BOOL_ARRAY:
                t = "[Z";
                break;
            case CHAR_ARRAY:
                t = "[C";
                break;
            case SHORT_ARRAY:
                t = "[S";
                break;
            case INT_ARRAY:
                t = "[I";
                break;
            case LONG_ARRAY:
                t = "[J";
                break;
            case FLOAT_ARRAY:
                t = "[F";
                break;
            case DOUBLE_ARRAY:
                t = "[D";
                break;
            case STRING:
                t = "Ljava/lang/String;";
                break;
            case INT_ARRAY_2D:
                t = "[[I";
                break;
        }
        return t;
    }
}
