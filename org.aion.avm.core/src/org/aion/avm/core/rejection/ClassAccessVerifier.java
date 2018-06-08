package org.aion.avm.core.rejection;

import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.DescriptorParser;


/**
 * Common utilities which verify that a referenced class is accessible from the contract (either defined by the contract or in "java/lang").
 */
public class ClassAccessVerifier {
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
        if (!classWhiteList.isInWhiteList(className)) {
            RejectedClassException.nonWhiteListedClass(className);
        }
    }
}
