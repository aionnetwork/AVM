package org.aion.avm.core.rejection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.Forest;
import org.aion.avm.core.HierarchyTreeBuilder;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;


public class RejectionClassVisitorTest {
    @Test
    public void testFiltering() throws Exception {
        String className = FilteringResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .addClass(className + "$A", "java.lang.Throwable", null)
                .addClass(className + "$B", className + "$A", null)
                .asMutableForest();
        ClassWhiteList classWhiteList = ClassWhiteList.buildFromClassHierarchy(classHierarchy);
        // We want to prove we can strip out everything so don't use any special parsing options for this visitor.
        byte[] filteredBytes = new ClassToolchain.Builder(raw, 0)
                .addNextVisitor(new RejectionClassVisitor(classWhiteList))
                .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                .build()
                .runAndGetBytecode();
        
        // Now, generate the ClassNode instances for each of these.
        ClassNode inputNode = new ClassNode(Opcodes.ASM6);
        new ClassReader(raw).accept(inputNode, 0);
        ClassNode outputNode = new ClassNode(Opcodes.ASM6);
        new ClassReader(filteredBytes).accept(outputNode, 0);
        
        compareClasses(inputNode, outputNode);
    }

    @Test
    public void testRejection_control() throws Exception {
        // This is just to verify that the test classes we are using were made from a test that actually _does_ load.
        String path = "test/resources/TestClassTemplate_control.class";
        // This is the untouched input so it should work.
        byte[] filteredBytes = commonFilterClass(path);
        Assert.assertNotNull(filteredBytes);
    }

    @Test(expected=RejectedClassException.class)
    public void testRejection_version() throws Exception {
        // Load the bytes we saved (normal TestClassTemplate but with Java9 version).
        // Verify that this fails by throwing.
        commonFilterClass("test/resources/TestClassTemplate_V9.class");
    }

    @Test(expected=RejectedClassException.class)
    public void testRejection_jsr() throws Exception {
        // Load the bytes we saved (normal TestClassTemplate but with an innocuous JSR/RET combo injected into the stream).
        // ASM snippet to add this (uses local 4 which may need to be changed but this otherwise shouldn't disrupt flow):
        /*
    {
        Label jsr = new Label();
        Label after = new Label();
        methodVisitor.visitJumpInsn(Opcodes.JSR, jsr);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, after);
        methodVisitor.visitLabel(jsr);
        methodVisitor.visitVarInsn(ASTORE, 4);
        methodVisitor.visitVarInsn(RET, 4);
        methodVisitor.visitLabel(after);
    }
         */
        // NOTE:  Code generated as above will fail verification unless built with a class file version before JDK7.
        // Beyond that, it _will_ run, but only with -Xverify:none.
        // (this test uses JDK10 as the version to ensure that we aren't tripped by the version check but actually see the blacklisted opcode)
        
        // Verify that this fails by throwing.
        commonFilterClass("test/resources/TestClassTemplate_jsr.class");
    }

    // TODO:  We probably want to handle these RuntimeExceptions from ASM in a more general way.  For now, we just get whatever it failed on.
    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRejection_corrupt() throws Exception {
        // Load the bytes we saved (normal TestClassTemplate I over-wrote a random byte in the bytecode).
        // (interestingly, I over-wrote the goto offset in a path the test doesn't execute so this runs with -Xverify:none)
        // Verify that this fails by throwing.
        commonFilterClass("test/resources/TestClassTemplate_corrupt.class");
    }

    @Test(expected=RejectedClassException.class)
    public void testRejection_deniedField() throws Exception {
        // Load the bytes we saved (normal TestClassTemplate but we include a java/util/Set static field).
        // Verify that this fails by throwing.
        commonFilterClass("test/resources/TestClassTemplate_deniedField.class");
    }

    @Test(expected=RejectedClassException.class)
    public void testRejection_deniedMethod() throws Exception {
        // Load the bytes we saved (normal TestClassTemplate we try to call System.out near the end).
        // Verify that this fails by throwing.
        commonFilterClass("test/resources/TestClassTemplate_deniedMethod.class");
    }

    @Test(expected=RejectedClassException.class)
    public void testRejection_finalize() throws Exception {
        String className = RejectFinalizeResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        // We expect this to fail since we have a finalize() method, which isn't allowed.
        commonFilterBytes(raw);
    }


    private static void compareClasses(ClassNode inputNode, ClassNode outputNode) {
        // Access is unchanged.
        Assert.assertEquals(inputNode.access, outputNode.access);
        
        // Attributes aren't in either.
        Assert.assertNull(inputNode.attrs);
        Assert.assertNull(outputNode.attrs);
        
        // We expect the same number of fields but we need a deep comparison.
        List<FieldNode> inputFields = inputNode.fields;
        List<FieldNode> outputFields = outputNode.fields;
        Assert.assertEquals(inputFields.size(), outputFields.size());
        
        for (int i = 0; i < inputFields.size(); ++i) {
            compareFields(inputFields.get(i), outputFields.get(i));
        }
        
        // Inner classes are unchanged.
        List<InnerClassNode> inputInnerClasses = inputNode.innerClasses;
        List<InnerClassNode> outputInnerClasses = outputNode.innerClasses;
        Assert.assertEquals(inputInnerClasses.size(), outputInnerClasses.size());
        
        for (int i = 0; i < inputInnerClasses.size(); ++i) {
            // Names are unchanged.
            Assert.assertEquals(inputInnerClasses.get(i).name, outputInnerClasses.get(i).name);
        }
        
        // Interfaces are unchanged.
        List<String> inputInterfaces = inputNode.interfaces;
        List<String> outputInterfaces = outputNode.interfaces;
        Assert.assertEquals(inputInterfaces.size(), outputInterfaces.size());
        
        for (int i = 0; i < inputInterfaces.size(); ++i) {
            Assert.assertEquals(inputInterfaces.get(i), outputInterfaces.get(i));
        }
        
        // Neither use any invisible annotations.
        Assert.assertNull(inputNode.invisibleAnnotations);
        Assert.assertNull(outputNode.invisibleAnnotations);
        Assert.assertNull(inputNode.invisibleTypeAnnotations);
        Assert.assertNull(outputNode.invisibleTypeAnnotations);
        
        // We expect the same number of methods but we need a deep comparison.
        List<MethodNode> inputMethods = inputNode.methods;
        List<MethodNode> outputMethods = outputNode.methods;
        Assert.assertEquals(inputMethods.size(), outputMethods.size());
        
        for (int i = 0; i < inputMethods.size(); ++i) {
            compareMethods(inputMethods.get(i), outputMethods.get(i));
        }
        
        Assert.assertEquals(inputNode.module, outputNode.module);
        
        // There are now no annotations (some of these had them, before).
        Assert.assertNull(outputNode.visibleAnnotations);
        Assert.assertNull(outputNode.visibleTypeAnnotations);
        
        Assert.assertEquals(inputNode.name, outputNode.name);
        Assert.assertEquals(inputNode.outerClass, outputNode.outerClass);
        Assert.assertEquals(inputNode.outerMethod, outputNode.outerMethod);
        Assert.assertEquals(inputNode.outerMethodDesc, outputNode.outerMethodDesc);
        Assert.assertEquals(inputNode.signature, outputNode.signature);
        Assert.assertEquals(inputNode.sourceDebug, outputNode.sourceDebug);
        
        // We expect the sourceFile to be removed.
        Assert.assertNull(outputNode.sourceFile);
        
        Assert.assertEquals(inputNode.superName, outputNode.superName);
    }

    private static void compareFields(FieldNode inputField, FieldNode outputField) {
        // Access is unchanged.
        Assert.assertEquals(inputField.access, outputField.access);
        
        // Attributes aren't in either.
        Assert.assertNull(inputField.attrs);
        Assert.assertNull(outputField.attrs);
        
        // Descriptor is unchanged.
        Assert.assertEquals(inputField.desc, outputField.desc);
        
        // Neither use any invisible annotations.
        Assert.assertNull(inputField.invisibleAnnotations);
        Assert.assertNull(outputField.invisibleAnnotations);
        Assert.assertNull(inputField.invisibleTypeAnnotations);
        Assert.assertNull(outputField.invisibleTypeAnnotations);
        
        // Name is unchanged.
        Assert.assertEquals(inputField.name, outputField.name);
        
        // Signature is now null.
        Assert.assertNull(outputField.signature);
        
        // Value is unchanged.
        Assert.assertEquals(inputField.value, outputField.value);
        
        // There are now no annotations (some of these had them, before).
        Assert.assertNull(outputField.visibleAnnotations);
        Assert.assertNull(outputField.visibleTypeAnnotations);
    }

    private static void compareMethods(MethodNode inputMethod, MethodNode outputMethod) {
        Assert.assertEquals(inputMethod.access, outputMethod.access);
        Assert.assertEquals(inputMethod.desc, outputMethod.desc);
        Assert.assertEquals(inputMethod.invisibleAnnotableParameterCount, outputMethod.invisibleAnnotableParameterCount);
        Assert.assertEquals(inputMethod.maxLocals, outputMethod.maxLocals);
        Assert.assertEquals(inputMethod.maxStack, outputMethod.maxStack);
        Assert.assertEquals(inputMethod.name, outputMethod.name);
        
        // Signature is now null.
        Assert.assertNull(outputMethod.signature);
        
        Assert.assertEquals(inputMethod.annotationDefault, outputMethod.annotationDefault);
        Assert.assertEquals(inputMethod.attrs, outputMethod.attrs);
        
        List<String> inputExceptions = inputMethod.exceptions;
        List<String> outputExceptions = outputMethod.exceptions;
        Assert.assertEquals(inputExceptions.size(), outputExceptions.size());
        
        for (int i = 0; i < inputExceptions.size(); ++i) {
            Assert.assertEquals(inputExceptions.get(i), outputExceptions.get(i));
        }
        
        // Neither use any invisible annotations.
        Assert.assertNull(inputMethod.invisibleAnnotations);
        Assert.assertNull(outputMethod.invisibleAnnotations);
        Assert.assertNull(inputMethod.invisibleLocalVariableAnnotations);
        Assert.assertNull(outputMethod.invisibleLocalVariableAnnotations);
        Assert.assertNull(inputMethod.invisibleParameterAnnotations);
        Assert.assertNull(outputMethod.invisibleParameterAnnotations);
        Assert.assertNull(inputMethod.invisibleTypeAnnotations);
        Assert.assertNull(outputMethod.invisibleTypeAnnotations);
        
        // The debug data has been filtered so we should see no local variable data.
        Assert.assertEquals(0, outputMethod.localVariables.size());
        
        Assert.assertEquals(inputMethod.parameters, outputMethod.parameters);
        
        // We expect the same number of try-catch blocks, none with annotations in the output.
        Assert.assertEquals(inputMethod.tryCatchBlocks.size(), outputMethod.tryCatchBlocks.size());
        
        for (TryCatchBlockNode block : outputMethod.tryCatchBlocks) {
            Assert.assertNull(block.visibleTypeAnnotations);
        }
        
        // There are now no annotations (some of these had them, before).
        Assert.assertNull(outputMethod.visibleAnnotations);
        Assert.assertEquals(0, outputMethod.visibleAnnotableParameterCount);
        Assert.assertNull(outputMethod.visibleParameterAnnotations);
        Assert.assertNull(outputMethod.visibleLocalVariableAnnotations);
        Assert.assertNull(outputMethod.visibleTypeAnnotations);
        
        Assert.assertNull(outputMethod.parameters);
    }

    private byte[] commonFilterClass(String path) throws IOException {
        byte[] testBytes = Files.readAllBytes(Paths.get(path));
        return commonFilterBytes(testBytes);
    }

    private byte[] commonFilterBytes(byte[] testBytes) throws IOException {
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass("TestClassTemplate", "java.lang.Object", testBytes)
                .asMutableForest();
        ClassWhiteList classWhiteList = ClassWhiteList.buildFromClassHierarchy(classHierarchy);
        byte[] filteredBytes = new ClassToolchain.Builder(testBytes, 0)
                .addNextVisitor(new RejectionClassVisitor(classWhiteList))
                .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                .build()
                .runAndGetBytecode();
        return filteredBytes;
    }
}
