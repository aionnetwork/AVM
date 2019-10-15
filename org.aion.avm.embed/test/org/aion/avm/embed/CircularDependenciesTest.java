 package org.aion.avm.embed;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.objectweb.asm.Opcodes.*;

import avm.Address;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.AionAddress;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class CircularDependenciesTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static AionAddress DEPLOYER;
    private static Address DEPLOYER_API;
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1;

    AionAddress contract;

    @BeforeClass
    public static void setup() {
        DEPLOYER_API = avmRule.getPreminedAccount();
        DEPLOYER = new AionAddress(DEPLOYER_API.toByteArray());
    }

    private TransactionResult callContract(String method, Object... parameters) {
        return callContract(DEPLOYER, method, parameters);
    }

    private TransactionResult callContract(AionAddress sender, String method, Object... parameters) {
        byte[] callData = ABIUtil.encodeMethodArguments(method, parameters);
        Address contractAddress = new Address(contract.toByteArray());
        Address senderAddress = new Address(sender.toByteArray());
        AvmRule.ResultWrapper result = avmRule.call(senderAddress, contractAddress, BigInteger.ZERO, callData, ENERGY_LIMIT, ENERGY_PRICE);
        assertTrue(result.getReceiptStatus().isSuccess());
        return result.getTransactionResult();

    }

    /**
     * Note: this does not test a circularity in the type relationships themselves, rather circular
     * object referencing, which is OK.
     */
    @Test
    public void testCircularDependency() {
        byte[] jarBytes = avmRule.getDappBytes(CircularDependencyATarget.class, null, CircularDependencyBTarget.class);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, jarBytes, ENERGY_LIMIT, ENERGY_PRICE);
        assertTrue(result.getReceiptStatus().isSuccess());
        contract = new AionAddress(result.getDappAddress().toByteArray());

        callContract("getValue");
    }

    /**
     * Tests a circularity in the type relationships themselves: A is child of & parent of B.
     */
    @Test
    public void testCircularTypesInterfacesDependency() throws IOException {
        byte[] interfaceA = Files.readAllBytes(Paths.get("test/org/aion/avm/embed/CircularInterfaceTypesATarget.class"));
        byte[] interfaceB = Files.readAllBytes(Paths.get("test/org/aion/avm/embed/CircularInterfaceTypesBTarget.class"));
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("CircularClassTypesATarget", interfaceA);
        classMap.put("CircularInterfaceTypesBTarget", interfaceB);
        byte[] jar = UserlibJarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(
            SelfDestructSmallResource.class, classMap);
        CodeAndArguments codeAndArguments = new CodeAndArguments(jar, null);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, codeAndArguments.encodeToBytes(), ENERGY_LIMIT, ENERGY_PRICE);
        assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, result.getReceiptStatus().causeOfError);
    }

    /**
     * Tests a circularity in the type relationships themselves: A is child of & parent of B.
     */
    @Test
    public void testCircularTypesClassesDependency() throws IOException {
        byte[] classA = Files.readAllBytes(Paths.get("test/org/aion/avm/embed/CircularClassTypesATarget.class"));
        byte[] classB = Files.readAllBytes(Paths.get("test/org/aion/avm/embed/CircularClassTypesBTarget.class"));
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("CircularClassTypesATarget", classA);
        classMap.put("CircularInterfaceTypesBTarget", classB);
        byte[] jar = UserlibJarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(
            SelfDestructSmallResource.class, classMap);
        CodeAndArguments codeAndArguments = new CodeAndArguments(jar, null);
        AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, codeAndArguments.encodeToBytes(), ENERGY_LIMIT, ENERGY_PRICE);
        assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, result.getReceiptStatus().causeOfError);
    }

     /**
      * Tests the case where a class has a dependency to its inner class.
      */
     @Test
     public void testCircularDependencyToNestedParent() {
         Map<String, byte[]> classMap = new HashMap<>();
         classMap.put("b.Child$Parent", getByteCodeForNestedParent());
         byte[] jar = UserlibJarBuilder.buildJarForExplicitClassNamesAndBytecode(
                 "b.Child", getByteCodeForOuterChild(),  classMap);

         byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

         AvmRule.ResultWrapper result = avmRule.deploy(DEPLOYER_API, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
         Assert.assertTrue(result.getReceiptStatus().isSuccess());

         result = avmRule.call(DEPLOYER_API, result.getDappAddress(), BigInteger.ZERO, new byte[0]);
         Assert.assertTrue(result.getReceiptStatus().isSuccess());
     }

     private byte[] getByteCodeForNestedParent() {

         ClassWriter classWriter = new ClassWriter(0);
         MethodVisitor methodVisitor;

         classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "b/Child$Parent", null, "java/lang/Object", null);
         classWriter.visitSource("Child.java", null);
         classWriter.visitInnerClass("b/Child$Parent", "b/Child", "Parent", ACC_PUBLIC | ACC_STATIC);

         {
             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
             methodVisitor.visitCode();
             Label label0 = new Label();
             methodVisitor.visitLabel(label0);
             methodVisitor.visitLineNumber(5, label0);
             methodVisitor.visitVarInsn(ALOAD, 0);
             methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
             methodVisitor.visitInsn(RETURN);
             methodVisitor.visitMaxs(1, 1);
             methodVisitor.visitEnd();
         }
         {
         methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "()[B", null, null);
         methodVisitor.visitCode();
         Label label0 = new Label();
         methodVisitor.visitLabel(label0);
         methodVisitor.visitLineNumber(7, label0);
         methodVisitor.visitInsn(ICONST_2);
         methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitInsn(ICONST_0);
         methodVisitor.visitInsn(ICONST_3);
         methodVisitor.visitInsn(BASTORE);
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitInsn(ICONST_1);
         methodVisitor.visitInsn(ICONST_4);
         methodVisitor.visitInsn(BASTORE);
         methodVisitor.visitInsn(ARETURN);
         methodVisitor.visitMaxs(4, 0);
         methodVisitor.visitEnd();
        }
         classWriter.visitEnd();

         return classWriter.toByteArray();
     }

     private byte[] getByteCodeForOuterChild() {


         ClassWriter classWriter = new ClassWriter(0);
         MethodVisitor methodVisitor;
         classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "b/Child", null, "b/Child$Parent", null);

         classWriter.visitSource("Child.java", null);

         classWriter.visitInnerClass("b/Child$Parent", "b/Child", "Parent", ACC_PUBLIC | ACC_STATIC);
         {
             methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
             methodVisitor.visitCode();
             Label label0 = new Label();
             methodVisitor.visitLabel(label0);
             methodVisitor.visitLineNumber(3, label0);
             methodVisitor.visitVarInsn(ALOAD, 0);
             methodVisitor.visitMethodInsn(INVOKESPECIAL, "b/Child$Parent", "<init>", "()V", false);
             methodVisitor.visitInsn(RETURN);
             methodVisitor.visitMaxs(1, 1);
             methodVisitor.visitEnd();
         }
         {
         methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "()[B", null, null);
         methodVisitor.visitCode();
         Label label0 = new Label();
         methodVisitor.visitLabel(label0);
         methodVisitor.visitLineNumber(7, label0);
         methodVisitor.visitInsn(ICONST_2);
         methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitInsn(ICONST_0);
         methodVisitor.visitInsn(ICONST_1);
         methodVisitor.visitInsn(BASTORE);
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitInsn(ICONST_1);
         methodVisitor.visitInsn(ICONST_2);
         methodVisitor.visitInsn(BASTORE);
         methodVisitor.visitInsn(ARETURN);
         methodVisitor.visitMaxs(4, 0);
         methodVisitor.visitEnd();
        }
         classWriter.visitEnd();

         return classWriter.toByteArray();
     }
}
