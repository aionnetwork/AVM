package org.aion.avm.core.persistence;

import org.aion.avm.core.ClassToolchain;
import org.objectweb.asm.Opcodes;


/**
 * This visitor is responsible for reshaping the contract code such that our "automatic graph" persistence design can be applied.
 * Specifically, this means the following transformations:
 * 1)  Add an empty constructor, if there isn't one, already (just calling superclass).
 * 2)  Remove "final" from all fields (at least instance fields - we may be able to treat static fields differently).
 * 3)  Generate specialized "get()/set()" methods for all instance fields, with the same visibility.
 * 4)  Convert all external (not within the same object) PUTFIELD/GETFIELD calls with calls to these generated methods.
 * 5)  Inject a local instance call to "lazyLoad()", at the beginning of each method (maybe we can make this only public),
 *     including the generated field methods.
 * Note that this transformation doesn't depend on the persistence model being applied.  So long as "lazyLoad()" is a safe no-op,
 * there is no harm in enabling this without the corresponding persistence logic.
 * This should probably be put late in the pipeline since these transformations are substantial, and could change energy and stack
 * accounting in pretty large ways for what are essentially our own implementation details.
 */
public class AutomaticGraphVisitor extends ClassToolchain.ToolChainClassVisitor {
    public AutomaticGraphVisitor() {
        super(Opcodes.ASM6);
    }
}
