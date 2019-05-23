package org.aion.avm.tooling.blockchainruntime;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.math.BigInteger;

import static org.aion.avm.tooling.UserlibCollisionTest.buildJarForClassNameAndBytecode;
import static org.objectweb.asm.Opcodes.*;

public class CreateTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private long energyLimit = 5_000_000L;
    private long energyPrice = 1;
    private Address dappAddr;

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

