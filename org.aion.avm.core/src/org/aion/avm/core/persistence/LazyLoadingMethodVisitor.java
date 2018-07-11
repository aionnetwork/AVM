package org.aion.avm.core.persistence;

import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Walks the method code, replace prepending a call to "lazyLoad()" on any GETFIELD/PUTFIELD bytecodes.
 * Note that there is a special-case:
 * -"<clinit>" - no re-writing is done here since nothing visible at this point could be a stub
 * 
 * Constructor calls are handled as any other method, even though many of the writes will be to the new object,
 * since in-depth flow analysis would be required to determine the receiver instance.
 * Such flow analysis-based optimizations could be applied later, and the constructor would only add a small
 * assumption (object in local 0 is already loaded).
 * 
 * NOTE:  An alternative to this design is to generate special get/set methods on the receiver object, which
 * would make the call to lazyLoad(), and then just change the GETFIELD/PUTFIELD to call those methods.  This
 * design would make the size of the change to the caller method much smaller but substantially increases the
 * complexity of the callee class (and requires much larger method generation logic).
 */
public class LazyLoadingMethodVisitor extends MethodVisitor {
    private static final String SHADOW_OBJECT_NAME = Helpers.fulllyQualifiedNameToInternalName(org.aion.avm.shadow.java.lang.Object.class.getName());
    private static final String LAZY_LOAD_NAME = "lazyLoad";
    private static final String LAZY_LOAD_DESCRIPTOR = "()V";

    public LazyLoadingMethodVisitor(MethodVisitor visitor) {
        super(Opcodes.ASM6, visitor);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        // If this is a PUTFIELD or GETFIELD, we want to call "lazyLoad()":
        // -PUTIFELD:  DUP2, POP, INVOKEVIRTUAL
        // -GETIFELD:  DUP, INVOKEVIRTUAL
        if (Opcodes.PUTFIELD == opcode) {
            // We need to see how big this type is since double and long need a far more complex dance.
            if ((1 == descriptor.length()) && ((DescriptorParser.LONG == descriptor.charAt(0)) || (DescriptorParser.DOUBLE == descriptor.charAt(0)))) {
                // Here, the stack looks like: ... OBJECT, VAR1, VAR2 (top)
                // Where we need:  ... OBJECT, VAR1, VAR2, OBJECT (top)
                // This is multiple stages:
                // DUP2_X1: ... VAR1, VAR2, OBJECT, VAR1, VAR2 (top)
                this.visitInsn(Opcodes.DUP2_X1);
                // POP2: ... VAR1, VAR2, OBJECT (top)
                this.visitInsn(Opcodes.POP2);
                // DUP: ... VAR1, VAR2, OBJECT, OBJECT (top)
                this.visitInsn(Opcodes.DUP);
                // INOKE: ... VAR1, VAR2, OBJECT (top)
                this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SHADOW_OBJECT_NAME, LAZY_LOAD_NAME, LAZY_LOAD_DESCRIPTOR, false);
                // DUP_X2: ... OBJECT, VAR1, VAR2, OBJECT (top)
                this.visitInsn(Opcodes.DUP_X2);
                // POP: ... OBJECT, VAR1, VAR2 (top)
                this.visitInsn(Opcodes.POP);
            } else {
                // Here, the stack looks like: ... OBJECT, VAR, (top)
                // Where we need:  ... OBJECT, VAR, OBJECT (top)
                // Stages:
                // DUP2: ... OBJECT, VAR, OBJECT, VAR (top)
                this.visitInsn(Opcodes.DUP2);
                // POP: ... OBJECT, VAR, OBJECT (top)
                this.visitInsn(Opcodes.POP);
                // INOKE: ... OBJECT, VAR (top)
                this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SHADOW_OBJECT_NAME, LAZY_LOAD_NAME, LAZY_LOAD_DESCRIPTOR, false);
            }
        } else if (Opcodes.GETFIELD == opcode) {
            // Here, the stack looks like: ... OBJECT, (top)
            // Where we need:  ... OBJECT, OBJECT (top)
            this.visitInsn(Opcodes.DUP);
            this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SHADOW_OBJECT_NAME, LAZY_LOAD_NAME, LAZY_LOAD_DESCRIPTOR, false);
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }
}
