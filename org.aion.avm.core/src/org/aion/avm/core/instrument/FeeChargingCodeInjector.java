package org.aion.avm.core.instrument;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class FeeChargingCodeInjector extends MethodVisitor {

    public FeeChargingCodeInjector(MethodVisitor visitor) {
        super(Opcodes.ASM6, visitor);

        // TODO: Nancy, refactor this class using method visitor
    }

    private BytecodeFeeScheduler bytecodeFeeScheduler = null;

    /**
     * Constructor.
     */
    public FeeChargingCodeInjector() {
        super(Opcodes.ASM6, null); // FIXME

        bytecodeFeeScheduler = new BytecodeFeeScheduler();
        bytecodeFeeScheduler.initialize();
    }

    /**
     * Walks the opcodes in a given block, returning the total fee they will cost the block.
     * @param block A code block.
     * @return The block fee.
     */
    public long calculateBlockFee(BasicBlock block) {
        long blockFee = 0;

        // Sum up the bytecode fee in the code block
        for (Integer opcode : block.opcodeSequence) {
            blockFee += bytecodeFeeScheduler.getFee(opcode);
        }

        return blockFee;
    }
}
