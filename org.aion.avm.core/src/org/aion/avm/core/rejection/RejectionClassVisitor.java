package org.aion.avm.core.rejection;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;


/**
 * Does a simple read-only pass over the loaded class, ensuring it isn't doing anything it isn't allowed to do:
 * -uses bytecode in blacklist
 * -references class not in whitelist
 * -overrides methods which we will not support as the user may expect
 * 
 * When a violation is detected, throws the RejectedClassException.
 */
public class RejectionClassVisitor extends ClassToolchain.ToolChainClassVisitor {
    // This will probably change, in the future, but we currently will only parse Java10 (version 54) classes.
    private static final int SUPPORTED_CLASS_VERSION = 54;

    private final ClassWhiteList classWhiteList;

    public RejectionClassVisitor(ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        
        this.classWhiteList = classWhiteList;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Make sure that this is the version we can understand.
        if (SUPPORTED_CLASS_VERSION != version) {
            RejectedClassException.unsupportedClassVersion(version);
        }
        
        // Check the superName.
        // The superName either needs to be provided by the user contract or be in "java/lang/".
        ClassAccessVerifier.checkClassAccessible(this.classWhiteList, superName);
        
        // Check the interfaces.
        // These rules are the same as the superclass.
        for (String interfaceName : interfaces) {
            ClassAccessVerifier.checkClassAccessible(this.classWhiteList, interfaceName);
        }
        
        // Null the signature, since we don't use it and don't want to make sure it is safe.
        super.visit(version, access, name, null, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        // Filter this.
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        Assert.unimplemented("TODO:  Determine if/how to handle module definitions");
        return super.visitModule(name, access, version);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        // "Non-standard attributes" are not supported, so filter them.
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // Check the descriptor.
        ClassAccessVerifier.checkDescriptor(this.classWhiteList, descriptor);
        
        // Note that the "value" field is only used for statics and can't be an object other than a String so we are safe with that.
        
        // Null the signature, since we don't use it and don't want to make sure it is safe.
        FieldVisitor fieldVisitor = super.visitField(access, name, descriptor, null, value);
        return new RejectionFieldVisitor(fieldVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // Check the descriptor.
        ClassAccessVerifier.checkDescriptor(this.classWhiteList, descriptor);
        // Check the exceptions.
        if (null != exceptions) {
            for (String exceptionName : exceptions) {
                ClassAccessVerifier.checkClassAccessible(this.classWhiteList, exceptionName);
            }
        }
        
        // Null the signature, since we don't use it and don't want to make sure it is safe.
        MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
        return new RejectionMethodVisitor(mv);
    }
}
