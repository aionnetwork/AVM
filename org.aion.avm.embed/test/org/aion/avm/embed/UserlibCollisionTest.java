package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.userlib.CodeAndArguments;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.objectweb.asm.Opcodes.*;

public class UserlibCollisionTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    @Test
    public void testJavaLangPackageContract() {
        boolean correctExceptionThrown = false;
        try {
            // having a package starting with .java fails during class loading
            SingleLoader loader = new SingleLoader();
            loader.loadClassFromByteCode("java.lang.MyClass", getJavaLangPackageClassBytes());
        } catch (SecurityException e) {
            correctExceptionThrown = true;
        }

        Assert.assertTrue(correctExceptionThrown);
    }

    @Test
    public void testJavaLangPackageContractDuringDeployment() {

        byte[] jar = buildJarForClassNameAndBytecode("java.lang.MyClass", getJavaLangPackageClassBytes());

        JarOptimizer jarOptimizer = new JarOptimizer(false);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        byte[] txData = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();

        Address from = avmRule.getPreminedAccount();

        AvmRule.ResultWrapper resultWrapper = avmRule.deploy(from, BigInteger.ZERO, txData);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isFailed());
    }

    @Test
    public void testAvmPackageContract() {
        ;
        JarOptimizer jarOptimizer = new JarOptimizer(false);
        byte[] jar = JarBuilder.buildJarForExplicitClassNameAndBytecode("avm.Main", getAvmPackageClassBytes());
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        byte[] txData = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();

        Address from = avmRule.getPreminedAccount();

        AvmRule.ResultWrapper resultWrapper = avmRule.deploy(from, BigInteger.ZERO, txData);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isFailed());
    }

    private static byte[] getJavaLangPackageClassBytes() {

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "java/lang/MyClass", null, "java/lang/Object", null);

        classWriter.visitSource("MyClass.java", null);
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

    private static byte[] getAvmPackageClassBytes() {

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "avm/Main", null, "java/lang/Object", null);

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

    public static byte[] buildJarForClassNameAndBytecode(String mainClassName, byte[] mainClassBytes) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();

        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        JarOutputStream stream = null;
        try {
            stream = new JarOutputStream(byteStream, manifest);
        } catch (IOException e) {
            throw unexpected(e);
        }
        JarOutputStream jarStream = stream;
        Set<String> entriesInJar = new HashSet<>();

        try {
            String internalName = Helpers.fulllyQualifiedNameToInternalName(mainClassName);
            assertTrue(!entriesInJar.contains(internalName));
            JarEntry entry = new JarEntry(internalName + ".class");
            jarStream.putNextEntry(entry);
            jarStream.write(mainClassBytes);
            jarStream.closeEntry();
            entriesInJar.add(internalName);

            jarStream.finish();
            jarStream.close();
            byteStream.close();
        } catch (IOException e) {
            throw unexpected(e);
        }

        return byteStream.toByteArray();
    }

    private static void assertTrue(boolean flag) {
        // We use a private helper to manage the assertions since the JDK default disables them.
        if (!flag) {
            throw new AssertionError("Case must be true");
        }
    }

    private static AssertionError unexpected(IOException e) throws AssertionError {
        throw new AssertionError("Unexpected exception", e);
    }


    public class SingleLoader extends ClassLoader {
        public Class<?> loadClassFromByteCode(String name, byte[] bytecode) {
            Class<?> clazz = this.defineClass(name, bytecode, 0, bytecode.length);
            Assert.assertNotNull(clazz);
            Assert.assertEquals(this, clazz.getClassLoader());
            return clazz;
        }
    }
}
