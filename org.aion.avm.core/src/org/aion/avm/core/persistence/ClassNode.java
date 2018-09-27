package org.aion.avm.core.persistence;

import java.nio.charset.StandardCharsets;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.Object;


/**
 * The data required to describe a Class reference, in the serialization layer.
 */
public class ClassNode implements INode {
    public final String className;

    public ClassNode(String className) {
        this.className = className;
    }

    @Override
    public Object getObjectInstance() {
        try {
            Class<?> jdkClass = Class.forName(this.className);
            return IHelper.currentContractHelper.get().externalWrapAsClass(jdkClass);
        } catch (ClassNotFoundException e) {
            // If this happens, how did we save it in the first place?
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public int getBillableReferenceSize() {
        // Encode the class stub constant as an int.
        int constantSize = ByteSizes.INT;
        
        // Get the class name.
        byte[] utf8Name = this.className.getBytes(StandardCharsets.UTF_8);
        
        // Write the length and the bytes.
        int nameSize = ByteSizes.INT + utf8Name.length;
        return constantSize + nameSize;
    }
}
