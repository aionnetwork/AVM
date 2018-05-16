package org.aion.avm.core.instrument;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

public class FeeChargingCodeInjector extends MethodVisitor {

    public FeeChargingCodeInjector(MethodVisitor visitor) {
        super(Opcodes.ASM6, visitor);

        // TODO: Nancy, refactor this class using method visitor
    }

    private String runtimeClassName = null;
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
     * Called by the runtime to register the Energy meter class name, which has a static method to charge the Energy.
     * @param energyMeterClassName
     */
    public void registerRuntimeEnergyMeter(String energyMeterClassName) {
        runtimeClassName = energyMeterClassName;
    }

    /**
     * injectCodeIntoOneMethod()
     * @param methodBytecode Original bytecode stream of one method.
     * @return Instrumented bytecode stream of the method, with the fee charging bytecode added to every code block.
     */
    public byte[] injectCodeIntoOneMethod(byte[] methodBytecode) {
        Map<String, List<BasicBlock>> methodBlocks = ClassRewriter.parseMethodBlocks(methodBytecode);
        for (List<BasicBlock> list : methodBlocks.values()) {
            for (BasicBlock block : list) {
                long blockCost = calculateBlockFee(block);

                block.setEnergyCost(blockCost);
            }
        }

        // Re-write the class adding the instrumental code.
        return ClassRewriter.rewriteBlocksInClass(runtimeClassName, methodBytecode, methodBlocks);
    }

    /**
     * Called by injectCodeIntoOneMethod() to calculate the fee of one code block.
     * @param block A code block.
     * @return The block fee.
     */
    private long calculateBlockFee(BasicBlock block) {
        long blockFee = 0;

        // Sum up the bytecode fee in the code block
        for (Integer opcode : block.opcodeSequence) {
            blockFee += bytecodeFeeScheduler.getFee(opcode);
        }

        return blockFee;
    }
}
