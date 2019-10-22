package org.aion.avm.core.stacktracking;

import i.RuntimeAssertionError;
import java.util.List;
import java.util.stream.Collectors;
import org.aion.avm.core.stacktracking.InitializationInterpreter.InitializationValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

/**
 * The purpose of this class is to capture each BasicValue and to wrap it into an InitializationValue,
 * which holds a pointer to a 'reference'. We use this pointer in a later stage to follow all
 * uninitializedThis references through the stack and local variables.
 */
public final class InitializationInterpreter extends Interpreter<InitializationValue> {
    private final BasicInterpreter underlyingInterpreter;

    public InitializationInterpreter() {
        super(Opcodes.ASM6);
        this.underlyingInterpreter = new BasicInterpreter();
    }

    @Override
    public InitializationValue newValue(Type type) {
        BasicValue value = this.underlyingInterpreter.newValue(type);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue newParameterValue(boolean isInstanceMethod, int local, Type type) {
        BasicValue value = this.underlyingInterpreter
            .newParameterValue(isInstanceMethod, local, type);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue newReturnTypeValue(Type type) {
        BasicValue value = this.underlyingInterpreter.newReturnTypeValue(type);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue newEmptyValue(int local) {
        BasicValue value = this.underlyingInterpreter.newEmptyValue(local);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue newOperation(AbstractInsnNode abstractInsnNode) throws AnalyzerException {
        BasicValue value = this.underlyingInterpreter.newOperation(abstractInsnNode);
        RuntimeAssertionError.assertTrue(value != null);
        return new InitializationValue(value);
    }

    @Override
    public InitializationValue copyOperation(AbstractInsnNode abstractInsnNode, InitializationValue incomingValue) throws AnalyzerException {
        BasicValue value = this.underlyingInterpreter.copyOperation(abstractInsnNode, incomingValue.underlying);
        RuntimeAssertionError.assertTrue(value != null);
        return new InitializationValue(value);
    }

    @Override
    public InitializationValue unaryOperation(AbstractInsnNode abstractInsnNode, InitializationValue incomingValue) throws AnalyzerException {
        BasicValue value = this.underlyingInterpreter.unaryOperation(abstractInsnNode, incomingValue.underlying);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue binaryOperation(AbstractInsnNode abstractInsnNode, InitializationValue incomingValue1, InitializationValue incomingValue2) throws AnalyzerException {
        BasicValue value = this.underlyingInterpreter.binaryOperation(abstractInsnNode, incomingValue1.underlying, incomingValue2.underlying);
        return (value == null) ? null : new InitializationValue(value);
    }

    @Override
    public InitializationValue ternaryOperation(AbstractInsnNode abstractInsnNode, InitializationValue incomingValue1, InitializationValue incomingValue2, InitializationValue incomingValue3) throws AnalyzerException {
        BasicValue value = this.underlyingInterpreter.ternaryOperation(abstractInsnNode, incomingValue1.underlying, incomingValue2.underlying, incomingValue3.underlying);
        RuntimeAssertionError.assertTrue(value != null);
        return new InitializationValue(value);
    }

    @Override
    public InitializationValue naryOperation(AbstractInsnNode abstractInsnNode, List<? extends InitializationValue> list) throws AnalyzerException {
        List<BasicValue> basics = list.stream().map((value) -> value.underlying).collect(Collectors.toList());
        BasicValue value = this.underlyingInterpreter.naryOperation(abstractInsnNode, basics);
        return new InitializationValue(value);
    }

    @Override
    public void returnOperation(AbstractInsnNode abstractInsnNode, InitializationValue incomingValue1, InitializationValue incomingValue2) throws AnalyzerException {
        this.underlyingInterpreter.returnOperation(abstractInsnNode, incomingValue1.underlying, incomingValue2.underlying);
    }

    @Override
    public InitializationValue merge(InitializationValue incomingValue1, InitializationValue incomingValue2) {
        BasicValue value = this.underlyingInterpreter.merge(incomingValue1.underlying, incomingValue2.underlying);
        return (value == null) ? null : new InitializationValue(value);
    }


    /**
     * A wrapper over {@link BasicValue} that holds a reference. This is meant to track uninitializedThis
     * references across the stack and local variable table by using instance equality on the
     * reference field.
     *
     * If reference is null, then this stack slot or local variable does not hold an uninitializedThis.
     */
    public static class InitializationValue implements Value {
        public final BasicValue underlying;
        public InitializationRefernece refernece;

        public InitializationValue(BasicValue underlying) {
            this.underlying = underlying;
        }

        public boolean isLongOrDouble() {
            if (this.underlying != null) {
                if (!this.underlying.isReference()) {
                    String desc = this.underlying.getType().getDescriptor();
                    return (desc.equals("J") || desc.equals("D"));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public int getSize() {
            return this.underlying.getSize();
        }

        @Override
        public String toString() {
            return "{ ref = " + (this.refernece == null ? "unassigned" : this.refernece) + " }";
        }

        @Override
        public boolean equals(Object other) {
            if (!(this.getClass().equals(other.getClass()))) {
                return false;
            } else if (this == other) {
                return true;
            }

            if (this.underlying == null) {
                return ((InitializationValue) other).underlying == null;
            } else {
                return this.underlying.equals(((InitializationValue) other).underlying);
            }
        }

        @Override
        public int hashCode() {
            return this.underlying.hashCode();
        }
    }

    /**
     * This class does not define equality because the only kind of equality that should interest it
     * is instance equality.
     */
    public static class InitializationRefernece {
        public final String classReference;

        public InitializationRefernece(String classReference) {
            this.classReference = classReference;
        }

        @Override
        public String toString() {
            return this.classReference;
        }
    }
}
