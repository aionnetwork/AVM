package org.aion.avm.core.rejection;

import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;


/**
 * Common utilities which verify that a referenced class is accessible from the contract (either defined by the contract or in "java/lang").
 */
public class ClassAccessVerifier {
    /**
     * While most cases of type names are strictly names or descriptors but this helper is used in cases where either may appear.
     * 
     * @param classWhiteList
     * @param descriptorOrName
     */
    public static void checkOptionallyDecorated(ClassWhiteList classWhiteList, String descriptorOrName) {
        if (descriptorOrName.startsWith("[")) {
            ClassAccessVerifier.checkDescriptor(classWhiteList, descriptorOrName);
        } else {
            ClassAccessVerifier.checkClassAccessible(classWhiteList, descriptorOrName);
        }
    }

    /**
     * The logic of the descriptor verification through the rejection components is the same so stored here.
     * In short, if anything not allowed is referenced, this will throw RejectedClassException.
     * @param classWhiteList
     * @param descriptor
     */
    public static void checkDescriptor(ClassWhiteList classWhiteList, String descriptor) {
        DescriptorParser.parse(descriptor, new DescriptorParser.Callbacks<Void>() {
            @Override
            public Void argumentStart(Void userData) {
                return null;
            }
            @Override
            public Void argumentEnd(Void userData) {
                return null;
            }
            @Override
            public Void readObject(int arrayDimensions, String type, Void userData) {
                checkClassAccessible(classWhiteList, type);
                return null;
            }
            @Override
            public Void readVoid(Void userData) {
                return null;
            }
            @Override
            public Void readBoolean(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readShort(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readLong(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readInteger(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readFloat(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readDouble(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readChar(int arrayDimensions, Void userData) {
                return null;
            }
            @Override
            public Void readByte(int arrayDimensions, Void userData) {
                return null;
            }
        }, null);
    }

    public static void checkClassAccessible(ClassWhiteList classWhiteList, String className) {
        // Note that we don't expect characters normally reserved for a descriptor encoding to be seen here (it is the caller's responsibility to parse that).
        Assert.assertTrue(!className.contains("["));
        Assert.assertTrue(!className.contains(";"));
        
        if (!classWhiteList.isInWhiteList(className)) {
            RejectedClassException.nonWhiteListedClass(className);
        }
    }
}
