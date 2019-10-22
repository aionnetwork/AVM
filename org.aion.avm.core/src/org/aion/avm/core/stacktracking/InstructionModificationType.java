package org.aion.avm.core.stacktracking;

import org.objectweb.asm.Opcodes;

/**
 * An InstructionModificationType corresponds to how an existing instruction should be modified.
 *
 * This class does not hold any information about what instruction this object relates to, any
 * relationships between these objects and the corresponding instructions should be maintained by
 * the caller.
 *
 * Note that the replacementOpcode will be specified for any replacement candidate instruction, and
 * otherwise will be a negative number.
 *
 * Note that the ownerClass and methodDescriptor fields will be specified for an invokespecial
 * replacement candidate only, and otherwise will be null.
 */
public final class InstructionModificationType {
    private enum Modification { UNMODIFIED, REMOVE_CANDIDATE, REPLACE_CANDIDATE }

    public final Modification modification;
    public final int replacementOpcode;
    public final String ownerClass;
    public final String methodDescriptor;

    private InstructionModificationType(Modification modification, int replacementOpcode, String ownerClass, String methodDescriptor) {
        this.modification = modification;
        this.replacementOpcode = replacementOpcode;
        this.ownerClass = ownerClass;
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * Returns a new modification type that declares the instruction not be modified.
     *
     * @return a new modification type that declares the instruction not be modified.
     */
    public static InstructionModificationType newUnmodifiedInsn() {
        return new InstructionModificationType(Modification.UNMODIFIED, -1, null, null);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be removed.
     *
     * If there is any conditional logic upon which this removal is decided, that logic is defined
     * in the caller, not here.
     *
     * @return a new modification type that declares that the instruction is a candidate to be removed.
     */
    public static InstructionModificationType newRemovalCandidateInsn() {
        return new InstructionModificationType(Modification.REMOVE_CANDIDATE, -1, null, null);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be replaced
     * by a call to a static factory method that knows how to produce the object being initialized.
     *
     * This instruction must only replace an invokespecial call to <init>! The constructor is defined
     * in the ownerClass and its descriptor is given by constructorDescriptor. The static factory
     * method that knows how to delegate to this constructor will have the same descriptor as the
     * constructor (except the return type, of course, will be the ownerClass).
     *
     * If there is any conditional logic upon which this replacement is decided, that logic is defined
     * in the caller, not here.
     *
     * @param constructorClass The class in which the constructor is defined.
     * @param constructorDescriptor The constructor method descriptor.
     * @return a new modification type that declares that the instruction is a candidate to be replaced by a static factory initialization call.
     */
    public static InstructionModificationType newStaticFactoryInitReplacementCandidateInsn(String constructorClass, String constructorDescriptor) {
        return new InstructionModificationType(Modification.REPLACE_CANDIDATE, Opcodes.INVOKESTATIC, constructorClass, constructorDescriptor);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be replaced
     * by a dup opcode.
     *
     * If there is any conditional logic upon which this replacement is decided, that logic is defined
     * in the caller, not here.
     *
     * @return a new modification type that declares that the instruction is a candidate to be replaced by a dup opcode.
     */
    public static InstructionModificationType newDupReplacementCandidateInsn() {
        return new InstructionModificationType(Modification.REPLACE_CANDIDATE, Opcodes.DUP, null, null);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be replaced
     * by a dup_x1 opcode.
     *
     * If there is any conditional logic upon which this replacement is decided, that logic is defined
     * in the caller, not here.
     *
     * @return a new modification type that declares that the instruction is a candidate to be replaced by a dup_x1 opcode.
     */
    public static InstructionModificationType newDupX1ReplacementCandidateInsn() {
        return new InstructionModificationType(Modification.REPLACE_CANDIDATE, Opcodes.DUP_X1, null, null);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be replaced
     * by a dup_x2 opcode.
     *
     * If there is any conditional logic upon which this replacement is decided, that logic is defined
     * in the caller, not here.
     *
     * @return a new modification type that declares that the instruction is a candidate to be replaced by a dup_x2 opcode.
     */
    public static InstructionModificationType newDupX2ReplacementCandidateInsn() {
        return new InstructionModificationType(Modification.REPLACE_CANDIDATE, Opcodes.DUP_X2, null, null);
    }

    /**
     * Returns a new modification type that declares that the instruction is a candidate to be replaced
     * by a pop opcode.
     *
     * If there is any conditional logic upon which this replacement is decided, that logic is defined
     * in the caller, not here.
     *
     * @return a new modification type that declares that the instruction is a candidate to be replaced by a pop opcode.
     */
    public static InstructionModificationType newPopReplacementCandidateInsn() {
        return new InstructionModificationType(Modification.REPLACE_CANDIDATE, Opcodes.POP, null, null);
    }
}
