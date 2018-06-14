package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;

/**
 * This transformer has no dependencies on other transformers.
 *
 * @author Roman Katerinenko
 */
public class InvokedynamicShadower extends ClassToolchain.ToolChainClassVisitor {
    private final Replacer replacer;

    public InvokedynamicShadower(String runtimeClassName, String shadowPackage, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        this.replacer = new Replacer(shadowPackage, classWhiteList);
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
        return new IndyMethodVisitor(mv);
    }

    private final class IndyMethodVisitor extends MethodVisitor {
        private IndyMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM6, methodVisitor);
        }

        @Override
        public void visitInvokeDynamicInsn(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            String methodOwner = bootstrapMethodHandle.getOwner();
            if (isStringConcatIndy(origMethodName, methodOwner)) {
                handleStringConcatIndy(methodDescriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            } else if (isLambdaIndy(methodOwner)) {
                handleLambdaIndy(origMethodName, methodDescriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            } else {
                throw new IllegalStateException("Unsupported invokedymanic: boostrap:" + origMethodName + " owner:" + methodOwner);
            }
        }

        private boolean isStringConcatIndy(String origMethodName, String owner) {
            return "makeConcatWithConstants".equals(origMethodName)
                    && "java/lang/invoke/StringConcatFactory".equals(owner);
        }

        private boolean isLambdaIndy(String owner) {
            return "java/lang/invoke/LambdaMetafactory".equals(owner);
        }

        private void handleLambdaIndy(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            final String newMethodName = replacer.replaceMethodName(bootstrapMethodHandle.getOwner(), origMethodName);
            final String newMethodDescriptor = replacer.replaceMethodDescriptor(methodDescriptor);
            final Handle newHandle = newLambdaHandleFrom(bootstrapMethodHandle, false);
            final Object[] newBootstrapMethodArgs = newShadowLambdaArgsFrom(bootstrapMethodArguments);
            super.visitInvokeDynamicInsn(newMethodName, newMethodDescriptor, newHandle, newBootstrapMethodArgs);
        }

        private void handleStringConcatIndy(String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            final String newMethodName = "concat";  /** {@link org.aion.avm.core.testdoubles.indy.invoke.StringConcatFactory#concat} */
            final String newMethodDescriptor = replacer.replaceMethodDescriptor(methodDescriptor);
            final Handle newHandle = newLambdaHandleFrom(bootstrapMethodHandle, false);
            super.visitInvokeDynamicInsn(newMethodName, newMethodDescriptor, newHandle, bootstrapMethodArguments);
        }

        private Handle newLambdaHandleFrom(Handle origHandle, boolean shadowMethodDescriptor) {
            final String owner = origHandle.getOwner();
            final String newOwner = replacer.replaceType(owner, true);
            final String newMethodName = replacer.replaceMethodName(owner, origHandle.getName());
            final String newMethodDescriptor = shadowMethodDescriptor ? replacer.replaceMethodDescriptor(origHandle.getDesc()) : origHandle.getDesc();
            return new Handle(origHandle.getTag(), newOwner, newMethodName, newMethodDescriptor, origHandle.isInterface());
        }

        private Object[] newShadowLambdaArgsFrom(Object[] origArgs) {
            final var newArgs = new ArrayList<>(origArgs.length);
            for (final Object origArg : origArgs) {
                final Object newArg;
                if (origArg instanceof Type) {
                    newArg = newMethodTypeFrom((Type) origArg);
                } else if (origArg instanceof Handle) {
                    newArg = newLambdaHandleFrom((Handle) origArg, true);
                } else {
                    newArg = origArg;
                }
                newArgs.add(newArg);
            }
            return newArgs.toArray();
        }

        private org.objectweb.asm.Type newMethodTypeFrom(org.objectweb.asm.Type origType) {
            return org.objectweb.asm.Type.getMethodType(replacer.replaceMethodDescriptor(origType.getDescriptor()));
        }
    }
}