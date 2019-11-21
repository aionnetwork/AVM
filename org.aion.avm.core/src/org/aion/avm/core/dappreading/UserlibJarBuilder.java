package org.aion.avm.core.dappreading;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.aion.avm.userlib.*;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.userlib.abi.ABIToken;
import org.aion.avm.utilities.JarBuilder;


/**
 * This just sits on top of the common JarBuilder, from the utilities module, stitching in the userlib where required by tests
 * in core and embed modules.
 * All the actual heavy-lifting is done in JarBuilder but, since it doesn't have visibility into the userlib module (as it is
 * in utilities), this class exists to add those classes, where required.
 */
public class UserlibJarBuilder {
    private static Class<?>[] userlibClasses = new Class[] {ABIDecoder.class, ABIEncoder.class,
        ABIStreamingEncoder.class, ABIException.class, ABIToken.class, AionBuffer.class, AionList.class, AionMap.class, AionSet.class, AionUtilities.class};

    /**
     * Creates the in-memory representation of a JAR with the given main class, other classes, and all classes in the Userlib.
     *
     * @param mainClass The main class to include and list in manifest (can be null).
     * @param otherClasses The other classes to include (main is already included).
     * @return The bytes representing this JAR.
     */
    public static byte[] buildJarForMainAndClassesAndUserlib(Class<?> mainClass, Class<?> ...otherClasses) {
        Class<?>[] combinedOtherClasses = Stream.of(otherClasses, userlibClasses).flatMap(Stream::of).toArray(Class<?>[]::new);
        return JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(mainClass, Collections.emptyMap(), combinedOtherClasses);
    }

    /**
     * Creates the in-memory representation of a JAR with the given main class, a map of class names to bytecode, other classes,
     * and all classes in the Userlib.
     *
     * @param mainClass The main class to include and list in manifest (can be null).
     * @param classMap A map of additional class names to bytecode.
     * @param otherClasses The other classes to include (main is already included).
     * @return The bytes representing this JAR.
     */
    public static byte[] buildJarForExplicitClassNamesAndBytecodeAndUserlib(Class<?> mainClass, Map<String, byte[]> classMap, Class<?> ...otherClasses) {
        Class<?>[] combinedOtherClasses = Stream.of(otherClasses, userlibClasses).flatMap(Stream::of).toArray(Class<?>[]::new);
        return JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(mainClass, classMap, combinedOtherClasses);
    }
}
