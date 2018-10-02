package org.aion.avm.core.persistence;

import org.aion.avm.internal.IPersistenceToken;


/**
 * This implementation of the IPersistenceToken is used by the reentrant invocation case.
 * 
 * All it does is contain a pointer to the object in the caller's frame.
 */
public class ReentrantCallerReferenceToken implements IPersistenceToken {
    public final org.aion.avm.shadow.java.lang.Object callerSpaceOriginal;
    
    public ReentrantCallerReferenceToken(org.aion.avm.shadow.java.lang.Object callerSpaceOriginal) {
        this.callerSpaceOriginal = callerSpaceOriginal;
    }
}
