package org.aion.avm.core.instrument;

import java.util.List;
import java.util.Map;

public class FeeChargingCodeInjector {


    private String runtimeClassName = null;

    public void registerRuntimeEnergyMeter(String energyMeterClassName) {
        runtimeClassName = energyMeterClassName;
    }

    /**
     * injectCodeIntoOneMethod()
     * @param methodBytecode Original bytecode stream of one method.
     * @return Instrumented bytecode stream of the method, with the fee charging bytecode added to every code block.
     */
    public byte[] injectCodeIntoOneMethod(byte[] methodBytecode) {

        Map<String, List<ClassRewriter.BasicBlock>> methodBlocks = ClassRewriter.parseMethodBlocks(methodBytecode);
        for (List<ClassRewriter.BasicBlock> list : methodBlocks.values()) {
            for (ClassRewriter.BasicBlock block : list) {
                long blockCost = calculateBlockFee(block);

                block.setEnergyCost(blockCost);
            }
        }

        // Re-write the class adding the instrumental code.
        return ClassRewriter.rewriteBlocksInClass(runtimeClassName, methodBytecode, methodBlocks);
    }

    private long calculateBlockFee(ClassRewriter.BasicBlock block) {
        long blockFee = 0;

        // Sum up the bytecode fee in the code block

        return blockFee;
    }


}
