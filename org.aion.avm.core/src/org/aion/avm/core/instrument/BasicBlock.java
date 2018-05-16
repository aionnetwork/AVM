package org.aion.avm.core.instrument;

import java.util.Collections;
import java.util.List;


/**
 * Describes a single basic block within a method.
 * Note that only the opcodeSequence and allocatedTypes are meant to be immutable.
 * The variable energyCost is mutable, deliberately, to allow for mutation requests.
 */
public class BasicBlock {
    public final List<Integer> opcodeSequence;
    public final List<String> allocatedTypes;
    private long energyCost;

    public BasicBlock(List<Integer> opcodes, List<String> allocatedTypes) {
        this.opcodeSequence = Collections.unmodifiableList(opcodes);
        this.allocatedTypes = Collections.unmodifiableList(allocatedTypes);
    }

    /**
     * Sets the cost of the block, so that the accounting idiom will be prepended when the block is next serialized.
     * @param energyCost The energy cost.
     */
    public void setEnergyCost(long energyCost) {
        this.energyCost = energyCost;
    }

    /**
     * Called when serializing the block to determine if the accounting idiom should be prepended.
     * @return The energy cost of the block.
     */
    public long getEnergyCost() {
        return this.energyCost;
    }
}
