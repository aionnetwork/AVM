package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.core.ClassHierarchyForest;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ExceptionWrapping extends ClassVisitor {
    private static final String kWrapperClassLibraryPrefix = "org/aion/avm/exceptionwrapper/";

    private final String runtimeClassName;
    private ClassHierarchyForest classHierarchy;
    private Map<String, byte[]> generatedClasses;

    public ExceptionWrapping(ClassVisitor visitor, String runtimeClassName, ClassHierarchyForest classHierarchy, Map<String, byte[]> generatedClasses) {
        super(Opcodes.ASM6, visitor);

        this.runtimeClassName = runtimeClassName;
        this.classHierarchy = classHierarchy;
        this.generatedClasses = generatedClasses;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            // We are going to collect the target labels of the exception handlers, so we can inject our instrumentation.
            private final Set<Label> catchTargets = new HashSet<>();
            // When we match a label as a target, we set this flag to inject our call-out before the first bytecode.
            private boolean isTargetFrame = false;
            
            @Override
            public void visitLabel(Label label) {
                super.visitLabel(label);
                
                // Be careful that we could have multiple labels at the same bytecode so saturate to true on match.
                if (this.catchTargets.contains(label)) {
                    this.isTargetFrame = true;
                }
            }
            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                super.visitFrame(type, nLocal, local, nStack, stack);
                
                // If this is the target frame, then this is the last callback before the first bytecode.
                // This means, once we call the super visitFrame, we can now inject the call-out.
                if (this.isTargetFrame) {
                    // We need to build the call to the unwrap helper, followed by the checkcast.
                    // (we know that there is exactly one element on the stack, since this is the beginning of a catch block.
                    String castType = (String) stack[0];
                    String methodName = "unwrapThrowable";
                    
                    // SHADOW NOTES:
                    // Note that this java/lang/Throwable MUST not be rewritten as another type since that is literally what is on the operand
                    // stack at the beginning of an exception handler.
                    // On the other hand, the returned java/lang/Object _should_ be rewritten as the shadow type, but we are safe if it isn't.
                    String methodDescriptor = "(Ljava/lang/Throwable;)Lorg/aion/avm/java/lang/Object;";
                    
                    // The call-out will actually return the base object class in our environment so we need to cast.
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, ExceptionWrapping.this.runtimeClassName, methodName, methodDescriptor, false);
                    
                    // The cast will give us exactly what the previous path thought was the common class of any multi-catch options.
                    // (without this, the verifier will complain about the operand stack containing the wrong type when used).
                    super.visitTypeInsn(Opcodes.CHECKCAST, castType);
                }
                this.isTargetFrame = false;
            }
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                // Our special-handling is only for non-finally blocks.
                if (null != type) {
                    if (type.startsWith("java/lang/")) {
                        // If this was trying to catch anything in "java/lang/*", then duplicate it for our wrapper type.
                        super.visitTryCatchBlock(start, end, handler, type);
                        String wrapperType = kWrapperClassLibraryPrefix + type;
                        super.visitTryCatchBlock(start, end, handler, wrapperType);
                    } else {
                        // This is user-defined (or should have been stripped, earlier) so replace it with the appropriate wrapper type.
                        String wrapperType = kWrapperClassLibraryPrefix + type;
                        super.visitTryCatchBlock(start, end, handler, wrapperType);
                    }
                } else {
                    // Finally, just pass through.
                    super.visitTryCatchBlock(start, end, handler, type);
                }
                
                // We also need to record the handler label so we know it is a catch block, in visitLabel, above.
                this.catchTargets.add(handler);
            }
            @Override
            public void visitInsn(int opcode) {
                // If this is athrow, we need to wrap whatever we had with an actual exception (after shadowing, nothing we get here will be an
                // actual exception - even java/lang exceptions will be exposed to the user's code as some sort of stub object from our domain).
                // This must happen BEFORE the athrow opcode is emitted, for obvious reasons.
                if (Opcodes.ATHROW == opcode) {
                    String methodName = "wrapAsThrowable";
                    
                    // SHADOW NOTES:
                    // Note that this java/lang/Throwable MUST NOT be rewritten as another type since that is literally what must be on the
                    // operand stack when we call athrow.
                    // On the other hand, the java/lang/Object _should_ be rewritten as the shadow type, but we are safe if it isn't.
                    String methodDescriptor = "(Lorg/aion/avm/java/lang/Object;)Ljava/lang/Throwable;";
                    
                    // The call-out will actually return the base object class in our environment so we need to cast.
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, ExceptionWrapping.this.runtimeClassName, methodName, methodDescriptor, false);
                }
                super.visitInsn(opcode);
            }
        };
    }
}
