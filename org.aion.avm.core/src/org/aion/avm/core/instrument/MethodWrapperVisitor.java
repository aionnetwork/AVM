package org.aion.avm.core.instrument;

import i.Helper;
import i.RuntimeAssertionError;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Responsible for creating a wrapper method per each method in a class. The original method, which
 * has already been renamed at this point to avm_X becomes avm2_X and the wrapper method becomes
 * avm_X. This is to simplify things so we can avoid special-casing logic around things like calls
 * to toString() etc. that require the avm_ prefix not the avm2_ prefix.
 *
 * NOTE: this stage of the pipeline comes after the method prefix is added. This is because the
 * method prefix gets added to the method itself as well as any places where it is invoked/referenced.
 * To simplify the renaming, we take advantage of the fact that all references are renamed for us
 * appropriately, and then we simply rename the methods only, and then add in our wrapper methods
 * which have the originally prefixed name so that now everything delegates directly into the
 * wrapper methods.
 */
public final class MethodWrapperVisitor extends ClassToolchain.ToolChainClassVisitor {
    public static final String WRAPPER_PREFIX = NamespaceMapper.METHOD_PREFIX;
    public static final String METHOD_PREFIX = "avm2_";

    private String className;
    private boolean isInterface;
    private Set<MethodInfo> methodsToWrap;

    public MethodWrapperVisitor() {
        super(Opcodes.ASM6);
        this.methodsToWrap = new HashSet<>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        this.isInterface = Modifier.isInterface(access);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Adds all non-abstract methods to a list of methods to be wrapped and all constructors to a
     * list of constructors to be wrapped.
     *
     * This method renames any methods to be wrapped so that they have the {@value METHOD_PREFIX}
     * prefix.
     *
     * This method modifies the descriptor of constructors to be wrapped so that they take a void
     * parameter as their last argument.
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!name.equals("<init>") && !name.equals("<clinit>") && !Modifier.isAbstract(access)) {
            // Add any non-abstract methods to the list of methods to be wrapped.
            String prefixStrippedName = NamespaceMapper.stripMethodPrefix(name);
            this.methodsToWrap.add(new MethodInfo(access, prefixStrippedName, descriptor, signature, exceptions));
            return super.visitMethod(access, METHOD_PREFIX + prefixStrippedName, descriptor, signature, exceptions);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    /**
     * Vists the end of the class and adds all of the wrapper methods.
     */
    @Override
    public void visitEnd() {

        //TODO -- we need to generate all our static factory methods that will call into their respective constructors here.

        // Create a method wrapper for every non-abstract method we've visited.
        for (MethodInfo methodInfo : this.methodsToWrap) {
            int access = methodInfo.access;
            RuntimeAssertionError.assertTrue(!Modifier.isAbstract(access));

            MethodVisitor mv = super.visitMethod(access,WRAPPER_PREFIX + methodInfo.prefixStrippedName, methodInfo.descriptor, methodInfo.signature, methodInfo.exceptions);

            boolean isStaticMethod = Modifier.isStatic(methodInfo.access);
            Integer[] paramTypes = getMethodParameterLoadInstructions(methodInfo.descriptor);
            int returnOpcode = getMethodReturnTypeInstruction(methodInfo.descriptor);
            int[] maxStackAndLocals = computeMaxStackAndMaxLocals(isStaticMethod, paramTypes, returnOpcode);

            // We instrument one new local variable (an int that represents maxStack + maxLocals)
            maxStackAndLocals[1] = maxStackAndLocals[1] + 1;

            // Invoke the runtime helper's enterMethod to track stack depth and size.
            mv.visitLdcInsn(maxStackAndLocals[0] + maxStackAndLocals[1]);   // Load the sum of maxStack + maxLocals
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Helper.RUNTIME_HELPER_NAME, "enterMethod", "(I)V", false);

            // Wrap all of the logic in this wrapper method inside a try-catch block (really this is a try-finally).
            Label startOfTryBlock = new Label();
            Label endOfTryBlock = new Label();
            Label startOfCatchBlock = new Label();
            Label endOfCatchBlock = new Label();

            mv.visitTryCatchBlock(startOfTryBlock, endOfTryBlock, startOfCatchBlock, null);
            mv.visitLabel(startOfTryBlock);

            // If this is a non-static method then load the first variable in the table, which is the 'this' reference.
            int index = 0;
            if (!isStaticMethod) {
                mv.visitVarInsn(Opcodes.ALOAD, index);
                index++;
            }

            // Load every parameter that was passed into this wrapper method to be passed into the original method.
            // Note in the case of long and double we need to increment our index by 2 since these are loading two words.
            for (int paramType : paramTypes) {
                mv.visitVarInsn(paramType, index);
                index += ((paramType == Opcodes.LLOAD || paramType == Opcodes.DLOAD) ? 2 : 1);
            }

            // The following invocation rules are obeyed:
            // All static methods use invokestatic (even if defined in an interface)
            // All interface instance methods use invokeinterface.
            // All private instance methods use invokespecial.
            // Everything else we see here is invokevirtual.
            int invoke = isStaticMethod ? Opcodes.INVOKESTATIC : -1;
            invoke = (!isStaticMethod && this.isInterface) ? Opcodes.INVOKEINTERFACE : invoke;
            invoke = (!isStaticMethod && Modifier.isPrivate(methodInfo.access)) ? Opcodes.INVOKESPECIAL : invoke;
            invoke = (invoke == -1) ? Opcodes.INVOKEVIRTUAL : invoke;
            mv.visitMethodInsn(invoke, this.className, METHOD_PREFIX + methodInfo.prefixStrippedName, methodInfo.descriptor, this.isInterface);

            // Call into the runtime helper's exitMethod before we return.
            mv.visitLdcInsn(maxStackAndLocals[0] + maxStackAndLocals[1]);   // Load the sum of maxStack + maxLocals
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Helper.RUNTIME_HELPER_NAME, "exitMethod", "(I)V", false);
            mv.visitInsn(returnOpcode);

            // End the try-catch block, call into the runtime helper's exitMethod and propagate the exception.
            mv.visitLabel(endOfTryBlock);
            mv.visitLabel(startOfCatchBlock);
            mv.visitLdcInsn(maxStackAndLocals[0] + maxStackAndLocals[1]);   // Load the sum of maxStack + maxLocals
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Helper.RUNTIME_HELPER_NAME, "exitMethod", "(I)V", false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(endOfCatchBlock);

            mv.visitMaxs(maxStackAndLocals[0], maxStackAndLocals[1]);

            mv.visitEnd();
        }

        super.visitEnd();
    }

    /**
     * Returns an array of length 2 such that the int at index 0 is the max stack value and the int
     * at index 1 is the max local value.
     *
     * This is computed based on the provided load instructions (used to load parameters onto the stack)
     * and the return instruction.
     *
     * The isStaticInvoke refers to whether or not the invoke opcode is invokestatic (specifically
     * the invoke opcode used to invoke the original method that the wrapper method wraps).
     *
     * @param isStaticInvoke Whether the original method invoke opcode is invokestatic or not.
     * @param loadInstructions The load instructions.
     * @param returnInstruction The return instruction.
     * @return the max stack and local values.
     */
    public static int[] computeMaxStackAndMaxLocals(boolean isStaticInvoke, Integer[] loadInstructions, int returnInstruction) {
        int[] maxStackAndLocals = new int[2];

        int maxStack = 0;
        int maxLocals = 0;

        if (!isStaticInvoke) {
            maxStack++;
            maxLocals++;
        }

        for (int loadInstruction : loadInstructions) {
            if (loadInstruction == Opcodes.ILOAD || loadInstruction == Opcodes.FLOAD || loadInstruction == Opcodes.ALOAD) {
                maxStack++;
                maxLocals++;
            } else if (loadInstruction == Opcodes.DLOAD || loadInstruction == Opcodes.LLOAD) {
                maxStack += 2;
                maxLocals += 2;
            }
        }

        // If the stack is too small, adjust it based on the return instruction.
        if (maxStack == 1) {
            if (returnInstruction == Opcodes.LRETURN || returnInstruction == Opcodes.DRETURN) {
                maxStack = 2;
            }
        } else if (maxStack == 0) {
            if (returnInstruction == Opcodes.LRETURN || returnInstruction == Opcodes.DRETURN) {
                maxStack = 2;
            } else if (returnInstruction != Opcodes.RETURN) {
                maxStack = 1;
            }
        }

        maxStackAndLocals[0] = maxStack;
        maxStackAndLocals[1] = maxLocals;
        return maxStackAndLocals;
    }

    /**
     * Returns the return instruction corresponding to the return type of the method with the given
     * descriptor.
     *
     * The return instructions are one of: RETURN, IRETURN, LRETURN, DRETURN, FRETURN, ARETURN.
     *
     * @param methodDescriptor The method descriptor.
     * @return the return instruction for the return type.
     */
    public static int getMethodReturnTypeInstruction(String methodDescriptor) {
        char returnType = methodDescriptor.substring(methodDescriptor.lastIndexOf(')') + 1).charAt(0);

        if (returnType == 'V') {
            return Opcodes.RETURN;
        } else if (returnType == 'I' || returnType == 'S' || returnType == 'C' || returnType == 'B' || returnType == 'Z') {
            return Opcodes.IRETURN;
        } else if (returnType == 'J') {
            return Opcodes.LRETURN;
        } else if (returnType == 'D') {
            return Opcodes.DRETURN;
        } else if (returnType == 'F') {
            return Opcodes.FRETURN;
        } else if (returnType == '[' || returnType == 'L') {
            return Opcodes.ARETURN;
        }

        RuntimeAssertionError.unreachable("Unexpected return type: " + returnType);
        return -1;
    }

    /**
     * Returns an array of LOAD instructions corresponding to the appropriate load calls required
     * to load each of the parameters in the given method descriptor.
     *
     * These load instructions are one of: ILOAD, LLOAD, DLOAD, FLOAD, ALOAD.
     *
     * @param methodDescriptor The method descriptor.
     * @return the load instructions for the parameters.
     */
    public static Integer[] getMethodParameterLoadInstructions(String methodDescriptor) {
        List<Integer> types = new ArrayList<>();

        // params is X, where a methodDescriptor is of the format (X)Y
        String params = methodDescriptor.substring(1, methodDescriptor.lastIndexOf(')'));

        if (params.isEmpty()) {
            return new Integer[0];
        } else {

            int index = 0;
            while (index < params.length()) {

                char nextParam = params.charAt(index);
                if (nextParam == 'I' || nextParam == 'B' || nextParam == 'C' || nextParam == 'S' || nextParam == 'Z') {
                    types.add(Opcodes.ILOAD);
                    index++;
                } else if (nextParam == 'J') {
                    types.add(Opcodes.LLOAD);
                    index++;
                } else if (nextParam == 'D') {
                    types.add(Opcodes.DLOAD);
                    index++;
                } else if (nextParam == 'F') {
                    types.add(Opcodes.FLOAD);
                    index++;
                } else if (nextParam == '[') {

                    types.add(Opcodes.ALOAD);
                    index++;

                    // First skip over all the array dimensionality signifiers.
                    nextParam = params.charAt(index);
                    while (nextParam == '[') {
                        index++;
                        nextParam = params.charAt(index);
                    }

                    // Skip over the rest of the array name.
                    if (nextParam == 'L') {
                        // If we have an object array then skip over the object to the next entry.
                        index = getIndexOfNextParamFromStartOfObject(params, index);
                    } else {
                        // We must have a primitive array, so we skip the next signifier to end up at the next entry.
                        index++;
                    }

                } else if (nextParam == 'L') {
                    types.add(Opcodes.ALOAD);
                    index = getIndexOfNextParamFromStartOfObject(params, index);
                } else {
                    RuntimeAssertionError.unreachable("Unknown type: " + params.charAt(index));
                }
            }

            return types.toArray(new Integer[0]);
        }

    }

    /**
     * For any descriptor of the form (P1..PN)R this method returns (P1..PN, V)R where the newly
     * added V parameter at the end of the argument list is of type void.
     *
     * @param originalDescriptor The original constructor descriptor.
     * @return the new descriptor for the constructor.
     */
    public static String deriveNewWrappedConstructorDescriptor(String originalDescriptor) {
        String voidType = "Ls/java/lang/Void;";
        int indexOfClosingBracket = originalDescriptor.lastIndexOf(')');
        return originalDescriptor.substring(0, indexOfClosingBracket) + voidType + originalDescriptor.substring(indexOfClosingBracket);
    }

    /**
     * Returns the index into params of the start of the next entry, where we assume that startIndex
     * is the start of an object entry that begins with L and ends with ;
     *
     * If this is the last entry in params then this method returns params.length().
     *
     * @param params The parameters of a method descriptor.
     * @param startIndex The start index of an object descriptor.
     * @return the start index of the next entry.
     */
    private static int getIndexOfNextParamFromStartOfObject(String params, int startIndex) {
        int index = startIndex;
        char nextChar = params.charAt(index);

        // Walk every character until we hit the end of the object param, which is given by ;
        while (nextChar != ';') {
            index++;
            nextChar = params.charAt(index);
        }

        // Increment once more before returning so that we step over the ;
        return ++index;
    }
}
