package org.aion.avm.embed.blockchainruntime;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.userlib.CodeAndArguments;
import org.junit.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.math.BigInteger;

import static org.aion.avm.embed.UserlibCollisionTest.buildJarForClassNameAndBytecode;
import static org.objectweb.asm.Opcodes.*;

public class CreateTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;

    /**
     * this test demonstrates the maximum number of times you can deploy an empty contract using blockchain.create
     * since blockchain.create cost does not include the basic transaction cost, it'll be much cheaper to deploy multiple contracts
     */
    @Test
    public void createContract() {
        byte[] createTargetJar = avmRule.getDappBytes(CreateTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, createTargetJar, energyLimit, energyPrice).getDappAddress();

        byte[] jar = buildJarForClassNameAndBytecode("a.Main", getByteCodeForEmptyClass());

        JarOptimizer jarOptimizer = new JarOptimizer(false);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        byte[] txData = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();

        call("createContracts", txData);
    }

    @Test
    public void createContractWithoutSuperName() {
        byte[] jar = buildJarForClassNameAndBytecode("b.Main", getByteCodeForClassWithoutSuperName());

        JarOptimizer jarOptimizer = new JarOptimizer(false);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        byte[] txData = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();

        AvmRule.ResultWrapper result = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isFailed());
    }

    private void call(String methodName, Object... objects) {
        byte[] txData = ABIUtil.encodeMethodArguments(methodName, objects);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
    }

    private byte[] getByteCodeForEmptyClass() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "a/Main", null, "java/lang/Object", null);

        classWriter.visitSource("Main.java", null);
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private byte[] getByteCodeForClassWithoutSuperName() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "b/Main", null, null, null);

        classWriter.visitSource("Main.java", null);
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}

