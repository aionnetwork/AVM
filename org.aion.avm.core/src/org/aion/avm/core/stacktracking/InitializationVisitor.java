package org.aion.avm.core.stacktracking;

import i.RuntimeAssertionError;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.instrument.MethodWrapperVisitor;
import org.aion.avm.core.stacktracking.InitializationInterpreter.InitializationRefernece;
import org.aion.avm.core.stacktracking.InitializationInterpreter.InitializationValue;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

public final class InitializationVisitor extends ClassToolchain.ToolChainClassVisitor {
    private String className;

    public InitializationVisitor() {
        super(Opcodes.ASM6);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        return new MethodNode(Opcodes.ASM6, access, name, descriptor, signature, exceptions) {
            @Override
            public void visitEnd() {
                super.visitEnd();

                // We only need to instrument the original non-abstract, non-clinit/init methods that the user provided here.
                // We can safely ignore our generated wrapper methods since we are hunting down initializations in user-provided code.
                if (!this.name.equals("<clinit>") && !this.name.equals("<init>") && !Modifier.isAbstract(this.access) && !this.name.startsWith(MethodWrapperVisitor.WRAPPER_PREFIX)) {

                    try {
                        InitializationInterpreter interpreter = new InitializationInterpreter();
                        Analyzer<InitializationValue> analyzer = new Analyzer<>(interpreter);
                        Frame<InitializationInterpreter.InitializationValue>[] frames = analyzer.analyze(InitializationVisitor.this.className, this);

                        RuntimeAssertionError.assertTrue(this.instructions.size() == frames.length);

                        System.err.println();
                        List<InstructionModificationType> instructionModificationTypes = new ArrayList<>();

                        for (int i = 0; i < this.instructions.size(); i++) {
                            AbstractInsnNode insn = this.instructions.get(i);
                            Frame<InitializationValue> frameAtThisInsn = frames[i];
                            Frame<InitializationValue> frameAfterThisInsn = (i == this.instructions.size() - 1) ? null : frames[i+1];

                            if (insn.getOpcode() == Opcodes.NEW) {

                                //build a new ref graph --> probably right in place in the frame Value, wonder if that same Value instance is used throughout!
                                RuntimeAssertionError.assertTrue(frameAfterThisInsn != null);
                                InitializationRefernece ref = new InitializationRefernece(((TypeInsnNode) insn).desc);
                                frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 1).refernece = ref;
                                instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());

                            } else if (insn.getOpcode() == Opcodes.DUP) {

                                //add to reference graph
                                InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // Then this corresponds to a NEW, this is a DUP we want to track.
                                if (ref != null) {
                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 1).refernece = ref;
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {

                                // We are only concerned with constructor calls.
                                MethodInsnNode methodInsn = ((MethodInsnNode) insn);
                                if (methodInsn.name.equals("<init>")) {
                                    // Verify that we have a tracked uninitializedThis ref on the stack where we expect it.
                                    // If it's not tracked then something went wrong visiting a previous step! We don't actually need to do anything with it.
                                    int uninitThisRefDepth = countNumParametersInDescriptor(methodInsn.desc) + 1;
                                    InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - uninitThisRefDepth).refernece;
                                    RuntimeAssertionError.assertTrue(ref != null);

                                    // Now check the locals and the stack and verify only a single reference to ref exists and it is on top of the stack.
                                    // Note we use instance equality here because we want to be sure we have the exact same reference in our hands, not a reference to the same class, etc.
                                    for (int j = 0; j < frameAfterThisInsn.getLocals(); j++) {
                                        RuntimeAssertionError.assertTrue(ref != frameAfterThisInsn.getLocal(j).refernece);
                                    }
                                    for (int j = 0; j < frameAfterThisInsn.getStackSize() - 1; j++) {
                                        RuntimeAssertionError.assertTrue(ref != frameAfterThisInsn.getStack(j).refernece);
                                    }
                                    RuntimeAssertionError.assertTrue(ref == frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 1).refernece);

                                    // We replace the invokespecial with a call into a static factory method in the owner class that will call into the
                                    // appropriate constructor.
                                    instructionModificationTypes.add(InstructionModificationType.newStaticFactoryInitReplacementCandidateInsn(methodInsn.owner, methodInsn.desc));
                                }

                            } else if (insn.getOpcode() == Opcodes.ALOAD) {

                                int varIndex = ((VarInsnNode) insn).var;
                                InitializationRefernece ref = frameAtThisInsn.getLocal(varIndex).refernece;

                                // If not null then we are loading an uninitializedThis onto the stack
                                if (ref != null) {
                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize()).refernece = ref;
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.ASTORE) {

                                int varIndex = ((VarInsnNode) insn).var;
                                InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // If not null then we are storing an uninitializedThis into a local
                                if (ref != null) {
                                    frameAfterThisInsn.getLocal(varIndex).refernece = ref;
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.ATHROW) {

                                // This can be ignored. You can never call ATHROW on an uninitializedThis, and once it has been
                                // initialized this is a valid instruction.
                                instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());

                            } else if (insn.getOpcode() == Opcodes.DUP_X1) {

                                InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // If not null then we are dup_x1'ing an uninitializedThis
                                if (ref != null) {

                                    if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                        // This is now a requirement: you cannot use dup_x1 to split a long or double in half.
                                        RuntimeAssertionError.unreachable("Invalid use of dup_x1");
                                    } else {
                                        frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 3).refernece = ref;
                                    }
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.DUP_X2) {

                                InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // If not null then we are dup_x2'ing an uninitializedThis
                                if (ref != null) {

                                    InitializationValue val = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2);
                                    if (val.isLongOrDouble()) {
                                        frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 3).refernece = ref;
                                    } else {
                                        frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 4).refernece = ref;
                                    }
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.DUP2) {

                                InitializationRefernece ref1 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).refernece;
                                InitializationRefernece ref2 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // If either is not null then we are possibly dup2'ing at least one uninitializedThis
                                if (ref1 != null || ref2 != null) {

                                    boolean actedOnRef = false;
                                    if (ref2 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                            // This is now a requirement: you cannot use dup2 to split a long or double in half.
                                            RuntimeAssertionError.unreachable("Invalid use of dup2");
                                        } else {
                                            frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 3).refernece = ref2;
                                            actedOnRef = true;
                                        }
                                    }
                                    if (ref1 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).isLongOrDouble()) {
                                            // Then we have nothing to do, since the dup2 is acting on a long or double.
                                        } else {
                                            frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 4).refernece = ref1;
                                            actedOnRef = true;
                                        }
                                    }

                                    // If both items on the stack were 'this' references, we can remove the dup2 instruction.
                                    // If only one of them was, then we replace it with a dup since the 'this' won't be on the stack anymore.
                                    // Otherwise we leave it alone.
                                    if (actedOnRef && (ref1 != null) && (ref2 != null)) {
                                        instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                    } else if (actedOnRef && ((ref1 != null) || (ref2 != null))) {
                                        instructionModificationTypes.add(InstructionModificationType.newDupReplacementCandidateInsn());
                                    } else {
                                        instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                    }

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.DUP2_X1) {

                                InitializationRefernece ref1 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;
                                InitializationRefernece ref2 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).refernece;

                                // If either is not null then we are possibly dup2_x1'ing at least one uninitializedThis
                                if (ref1 != null || ref2 != null) {

                                    boolean actedOnRef = false;
                                    if (ref1 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                            // We have something like: [long, long_2nd, REF]
                                            // This is now a requirement: you cannot use dup2_x1 to split a long or double in half.
                                            RuntimeAssertionError.unreachable("Invalid use of dup2_x1");
                                        } else {
                                            if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 3).isLongOrDouble()) {
                                                // We have something like: [long, long_2nd, REF, REF]
                                                frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 5).refernece = ref1;
                                                actedOnRef = true;
                                            } else {
                                                if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 4).isLongOrDouble()) {
                                                    // We have something like: [long, long_2nd, REF, REF, REF]
                                                    // This is now a requirement: you cannot use dup2_x1 to split a long or double in half.
                                                    RuntimeAssertionError.unreachable("Invalid use of dup2_x1");
                                                } else {
                                                    // We have something like: [REF, REF, REF, REF]
                                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 5).refernece = ref1;
                                                    actedOnRef = true;
                                                }
                                            }
                                        }
                                    }
                                    if (ref2 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).isLongOrDouble()) {
                                            // Then we have nothing to do, since the dup2_x1 is acting on a long or double.
                                        } else {
                                            if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 3).isLongOrDouble()) {
                                                // We have something like: [long, long_2nd, REF, REF]
                                                frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 6).refernece = ref2;
                                                actedOnRef = true;
                                            } else {
                                                // We have something like: [REF, REF, REF]
                                                if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 4).isLongOrDouble()) {
                                                    // We have something like: [long, long_2nd, REF, REF, REF]
                                                } else {

                                                }

                                                frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 5).refernece = ref2;
                                                actedOnRef = true;
                                            }
                                        }
                                    }

                                    // If both items on the stack were 'this' references, we can remove the dup2_x1 instruction.
                                    // If only one of them was, then we replace it with a dup_x1 since the 'this' won't be on the stack anymore.
                                    // Otherwise we leave it alone.
                                    if (actedOnRef && (ref1 != null) && (ref2 != null)) {
                                        instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                    } else if (actedOnRef && ((ref1 != null) || (ref2 != null))) {
                                        instructionModificationTypes.add(InstructionModificationType.newDupX1ReplacementCandidateInsn());
                                    } else {
                                        instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                    }

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.DUP2_X2) {

                                InitializationRefernece ref1 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;
                                InitializationRefernece ref2 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).refernece;

                                // If either is not null then we are possibly dup2_x2'ing at least one uninitializedThis
                                boolean actedOnRef = false;
                                if (ref1 != null || ref2 != null) {

                                    if (ref1 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                            // This is now a requirement: you cannot use dup2_x2 to split a long or double in half.
                                            RuntimeAssertionError.unreachable("Invalid use of dup2_x2");
                                        } else {
                                            if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 3).isLongOrDouble()) {
                                                // we have something like: [long, long_2nd, REF, REF] on the stack (ASM treats long/double as a single slot)
                                                frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 4).refernece = ref1;
                                                actedOnRef = true;
                                            } else {
                                                if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 4).isLongOrDouble()) {
                                                    // we have something like: [long, long_2nd, REF, REF, REF]
                                                    // This is now a requirement: you cannot use dup2_x2 to split a long or double in half.
                                                    RuntimeAssertionError.unreachable("Invalid use of dup2_x2");
                                                } else {
                                                    // we have something like: [REF, REF, REF, REF]
                                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 5).refernece = ref1;
                                                    actedOnRef = true;
                                                }
                                            }
                                        }
                                    }
                                    if (ref2 != null) {
                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).isLongOrDouble()) {
                                            // Then we have nothing to do, since the dup2_x2 is acting on a long or double.
                                        } else {
                                            if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 3).isLongOrDouble()) {
                                                if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 4).isLongOrDouble()) {
                                                    // We have something like: [long, long_2nd, long, long_2nd, REF, REF]
                                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 4).refernece = ref2;
                                                    actedOnRef = true;
                                                } else {
                                                    // We have something like: [REF, long, long_2nd, REF, REF]
                                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 5).refernece = ref2;
                                                    actedOnRef = true;
                                                }
                                            } else {
                                                if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 4).isLongOrDouble()) {
                                                    // We have something like: [REF, REF, REF, REF]
                                                    frameAfterThisInsn.getStack(frameAfterThisInsn.getStackSize() - 6).refernece = ref2;
                                                    actedOnRef = true;
                                                } else {
                                                    // We have something like: [long, long_2nd, REF, REF, REF]
                                                    // This is now a requirement: you cannot use dup2_x2 to split a long or double in half.
                                                    RuntimeAssertionError.unreachable("Invalid use of dup2_x2");
                                                }
                                            }
                                        }
                                    }

                                    // If both items on the stack were 'this' references, we can remove the dup2_x2 instruction.
                                    // If only one of them was, then we replace it with a dup_x2 since the 'this' won't be on the stack anymore.
                                    // Otherwise we leave it alone.
                                    if (actedOnRef && (ref1 != null) && (ref2 != null)) {
                                        instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                    } else if (actedOnRef && ((ref1 != null) || (ref2 != null))) {
                                        instructionModificationTypes.add(InstructionModificationType.newDupX2ReplacementCandidateInsn());
                                    } else {
                                        instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                    }

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.LDC) {

                                //TODO -- confirm this...

                                // Note that LDC can actually load an instance onto the stack if that instance is a string literal.
                                // However, it cannot load an uninitializedThis, so by the we reach this instruction we don't have to worry about it.
                                instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());

                            } else if (insn.getOpcode() == Opcodes.POP) {

                                InitializationRefernece ref = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;

                                // Then we are actually popping the uninitialized/initialized this from the stack.
                                if (ref != null) {
                                    // We don't actually have anything to do here but remember to remove this instruction.
                                    instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.POP2) {

                                InitializationRefernece ref1 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;
                                InitializationRefernece ref2 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).refernece;

                                // Then we are possibly popping the uninitialized/initialized this from the stack.
                                if (ref1 != null || ref2 != null) {

                                    boolean actedOnRef = false;
                                    if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).isLongOrDouble()) {
                                        // Then 'this' never actually gets popped here.
                                    } else {

                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                            // This is now a requirement: you cannot use pop2 to split a long or double in half.
                                            RuntimeAssertionError.unreachable("Invalid use of pop2");
                                        }

                                        // If the top value was not a long/double then we know ref1 and ref2 will be popped and at least one
                                        // of them is 'this'.
                                        actedOnRef = true;
                                    }

                                    // If both items on the stack were 'this' references, we can remove the pop2 instruction.
                                    // If only one of them was, then we replace it with a pop since the 'this' won't be on the stack anymore.
                                    // Otherwise we leave it alone.
                                    if (actedOnRef && (ref1 != null) && (ref2 != null)) {
                                        instructionModificationTypes.add(InstructionModificationType.newRemovalCandidateInsn());
                                    } else if (actedOnRef && ((ref1 != null) || (ref2 != null))) {
                                        instructionModificationTypes.add(InstructionModificationType.newPopReplacementCandidateInsn());
                                    } else {
                                        instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                    }

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else if (insn.getOpcode() == Opcodes.SWAP) {

                                // Note we can safely remove the SWAP opcode since 'this' will not be on the stack
                                // so there is nothing to swap anymore.

                                InitializationRefernece ref1 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).refernece;
                                InitializationRefernece ref2 = frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).refernece;

                                // Then we are possibly swapping the uninitialized/initialized this with another value on the stack.
                                if (ref1 != null || ref2 != null) {

                                    boolean actedOnRef = false;
                                    if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 1).isLongOrDouble()) {
                                        // Then 'this' never actually gets swapped.
                                    } else {

                                        if (frameAtThisInsn.getStack(frameAtThisInsn.getStackSize() - 2).isLongOrDouble()) {
                                            // This is now a requirement: you cannot use swap to split a long or double in half.
                                            RuntimeAssertionError.unreachable("Invalid use of swap");
                                        }

                                        // If the top value was not a long/double then we know ref1 and ref2 will be swapped and one
                                        // of them is 'this'. We have nothing to do here but remove the instruction.
                                        actedOnRef = true;
                                    }
                                    instructionModificationTypes.add(actedOnRef ? InstructionModificationType.newRemovalCandidateInsn() : InstructionModificationType.newUnmodifiedInsn());

                                } else {
                                    instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                                }

                            } else {
                                instructionModificationTypes.add(InstructionModificationType.newUnmodifiedInsn());
                            }
                        }

                        // Second pass: now we iterate over each instruction and we look at the corresponding InstructionModificationType
                        // to figure out what to do with it. Note that once the uninitializedThis becomes instantiated we do not modify the
                        // instructions. We only perform modifications while the reference is an uninitializedThis. We use a set to keep
                        // track of when a reference has been initialized so that we can ignore any instructions done to it.
                        Set<InitializationRefernece> initializedReferences = new HashSet<>();

                        //TODO

                    } catch (AnalyzerException e) {
                        System.err.println(InitializationVisitor.this.className + " " + this.name + " " + this.desc);
                        throw RuntimeAssertionError.unexpected(e);
                    }

                }

                accept(methodVisitor);
            }
        };
    }

    private static int countNumParametersInDescriptor(String descriptor) {
        int count = 0;

        // params is X, where a methodDescriptor is of the format (X)Y
        String params = descriptor.substring(1, descriptor.lastIndexOf(')'));

        if (!params.isEmpty()) {
            int index = 0;
            while (index < params.length()) {

                char nextParam = params.charAt(index);
                if (nextParam == 'I' || nextParam == 'B' || nextParam == 'C' || nextParam == 'S' || nextParam == 'Z' || nextParam == 'J' || nextParam == 'D' || nextParam == 'F') {
                    count++;
                    index++;
                } else if (nextParam == '[') {
                    count++;
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
                    count++;
                    index = getIndexOfNextParamFromStartOfObject(params, index);
                } else {
                    RuntimeAssertionError.unreachable("Unknown type: " + params.charAt(index));
                }
            }
        }

        return count;
    }

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
