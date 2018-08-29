package org.aion.avm.core.rejection;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.internal.RuntimeAssertionError;
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

    // The names of the classes that the user defined in their JAR (note:  this does NOT include interfaces).
    private final PreRenameClassAccessRules preRenameClassAccessRules;

    public RejectionClassVisitor(PreRenameClassAccessRules preRenameClassAccessRules) {
        super(Opcodes.ASM6);
        this.preRenameClassAccessRules = preRenameClassAccessRules;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Make sure that this is the version we can understand.
        if (SUPPORTED_CLASS_VERSION != version) {
            RejectedClassException.unsupportedClassVersion(version);
        }
        if (!this.preRenameClassAccessRules.canUserSubclass(superName)) {
            RejectedClassException.restrictedSuperclass(name, superName);
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
        throw RuntimeAssertionError.unimplemented("TODO:  Determine if/how to handle module definitions");
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
        // Note that the "value" field is only used for statics and can't be an object other than a String so we are safe with that.
        
        // Null the signature, since we don't use it and don't want to make sure it is safe.
        FieldVisitor fieldVisitor = super.visitField(access, name, descriptor, null, value);
        return new RejectionFieldVisitor(fieldVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // Check that they aren't trying to override a forbidden method.
        // finalize() is forbidden - should we check only the empty args descriptor or all methods with this name?
        if ("finalize".equals(name)) {
            RejectedClassException.forbiddenMethodOverride(name);
        }
        
        // Check that this method isn't synchronized, since we don't allow monitor operations.
        if (0 != (Opcodes.ACC_SYNCHRONIZED & access)) {
            RejectedClassException.invalidMethodFlag(name, "ACC_SYNCHRONIZED");
        }
        
        // Null the signature, since we don't use it and don't want to make sure it is safe.
        MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
        return new RejectionMethodVisitor(mv);
    }
}
