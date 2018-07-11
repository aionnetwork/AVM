package org.aion.avm.core.persistence;

import org.aion.avm.core.ClassToolchain;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * This visitor is responsible for reshaping the contract code such that our "automatic graph" persistence design can be applied.
 * Specifically, this means the following transformations:
 * 1)  Add an empty constructor, if there isn't one, already (just calling superclass).
 * 2)  Remove "final" from all fields (at least instance fields - we may be able to treat static fields differently).
 * 3)  Prepend all PUTFIELD/GETFIELD instructions with a call to "lazyLoad()" on the receiver object
 * 
 * Note that this transformation doesn't depend on the persistence model being applied.  So long as "lazyLoad()" is a safe no-op,
 * there is no harm in enabling this without the corresponding persistence logic.
 * This should probably be put late in the pipeline since these transformations are substantial, and could change energy and stack
 * accounting in pretty large ways for what are essentially our own implementation details.
 */
public class AutomaticGraphVisitor extends ClassToolchain.ToolChainClassVisitor {
    private static final String CLINIT_NAME = "<clinit>";
    private static final String INIT_NAME = "<init>";
    private static final String ZERO_ARG_DESCRIPTOR = "()V";

    private boolean isInterface;
    private String superClassName;
    private boolean didDefineEmptyConstructor;

    public AutomaticGraphVisitor() {
        super(Opcodes.ASM6);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Note that we don'tw ant to change interfaces - clearly, they have no constructors.
        this.isInterface = (0 != (Opcodes.ACC_INTERFACE & access));
        // We just want to extract the superclass name.
        this.superClassName = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // Filter out the "final" from all fields.
        // (note that we may way to skip this, for statics, and exclude them from the serialization system).
        int newAccess = (~Opcodes.ACC_FINAL) & access; 
        return super.visitField(newAccess, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // We are just snooping on these to see if the zero-arg constructor is defined.
        if (INIT_NAME.equals(name) && ZERO_ARG_DESCRIPTOR.equals(descriptor)) {
            this.didDefineEmptyConstructor = true;
        }
        // If this is the <clinit>, we don't want to inject the lazyLoad calls (nothing visible there could be a stub).
        return CLINIT_NAME.equals(name)
                ? super.visitMethod(access, name, descriptor, signature, exceptions)
                : new LazyLoadingMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
    }

    @Override
    public void visitEnd() {
        // If the user didn't define one of these, we need to generate one, here.
        if (!this.isInterface && !this.didDefineEmptyConstructor) {
            // This logic is similar to StubGenerator.
            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, INIT_NAME, ZERO_ARG_DESCRIPTOR, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, this.superClassName, INIT_NAME, ZERO_ARG_DESCRIPTOR, false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        super.visitEnd();
    }
}
