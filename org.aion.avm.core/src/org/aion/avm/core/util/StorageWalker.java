package org.aion.avm.core.util;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.aion.avm.core.types.ImmortalDappModule;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.persistence.ReflectedFieldCache;
import org.aion.avm.core.persistence.ReflectionStructureCodec;
import org.aion.avm.core.persistence.StorageKeys;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.PackageConstants;
import org.aion.kernel.KernelInterface;


/**
 * This is a tool to examine the storage graph for a given DApp.
 * All this requires is a TransactionContext to access the storage and the address of the DApp in question and it will walk
 * the entire storage, from the class static roots, and dump a human-readable representation to an output stream.
 * Note that the output is produced as a queue, not a stack, so the representation is completely flat (not attempting to
 * indent or otherwise render the graph).  Each line is a single class or object instance.
 */
public class StorageWalker {
    /**
     * Called to walk the storage of dappAddress, accessed via context, and print a description of the object graph to output.
     * 
     * @param output Where the output will be written.
     * @param kernel The storage abstraction used to read both the code and data.
     * @param dappAddress The address of the application.
     * @throws IOException A problem reading the application code.
     * @throws NoSuchFieldException A problem interpreting the data.
     * @throws SecurityException A problem interpreting the data.
     * @throws IllegalArgumentException A problem interpreting the data.
     * @throws IllegalAccessException A problem interpreting the data.
     */
    public static void walkAllStaticsForDapp(PrintStream output, KernelInterface kernel, byte[] dappAddress) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        byte[] immortalDappJar = kernel.getCode(dappAddress).getCode();
        ImmortalDappModule app = ImmortalDappModule.readFromJar(immortalDappJar);
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        List<Class<?>> alphabeticalContractClasses = Helpers.getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet());
        doReadEntireStorage(output, classLoader, kernel, dappAddress, alphabeticalContractClasses);
    }


    private static void doReadEntireStorage(PrintStream output, ClassLoader loader, KernelInterface kernel, byte[] address, List<Class<?>> classes) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // These objects are filled/used by the populator.
        Set<Long> processed = new HashSet<>();
        Queue<org.aion.avm.shadow.java.lang.Object> instanceQueue = new LinkedList<>();
        
        // Create the populator which describes and outputs what it sees.
        ReflectionStructureCodec.IFieldPopulator populator =  new ReflectionStructureCodec.IFieldPopulator() {
            @Override
            public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": boolean(" + val + "), ");
            }
            @Override
            public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": double(" + val + "), ");
            }
            @Override
            public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": long(" + val + "), ");
            }
            @Override
            public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": float(" + val + "), ");
            }
            @Override
            public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": int(" + val + "), ");
            }
            @Override
            public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": char(" + val + "), ");
            }
            @Override
            public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": short(" + val + "), ");
            }
            @Override
            public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": byte(" + val + "), ");
            }
            @Override
            public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) throws IllegalArgumentException, IllegalAccessException {
                output.print(field.getName() + ": ref(" + val + "), ");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object createRegularInstance(String className, long instanceId) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                // Note that we can't decode all object instances (most shadows and array wrappers, for example), but we will determine that on the reading side.
                // For now, just make sure we enqueue each instance only once.
                if (!processed.contains(instanceId)) {
                    processed.add(instanceId);
                    Constructor<?> con = Class.forName(className, false, loader).getConstructor(IDeserializer.class, long.class);
                    con.setAccessible(true);
                    org.aion.avm.shadow.java.lang.Object stub = (org.aion.avm.shadow.java.lang.Object)con.newInstance(null, instanceId);
                    instanceQueue.add(stub);
                }
                return new org.aion.avm.shadow.java.lang.String("instance(" + shortenClassName(className) + ", " + instanceId + ")");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object createClass(String className) throws ClassNotFoundException {
                return new org.aion.avm.shadow.java.lang.String("class(" + shortenClassName(className) + ")");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object createConstant(long instanceId) {
                return new org.aion.avm.shadow.java.lang.String("constant(" + instanceId + ")");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object createNull() {
                return new org.aion.avm.shadow.java.lang.String("null");
            }
        };
        
        // Create the codec back-ended on the populator.
        // (note that it requires a fieldCache but we don't attempt to reuse this, in our case).
        ReflectedFieldCache fieldCache = new ReflectedFieldCache();
        NullFeeProcessor feeProcessor = new NullFeeProcessor();
        ReflectionStructureCodec codec = new ReflectionStructureCodec(fieldCache, populator, feeProcessor, kernel, address, 0);
        
        // Extract the raw data for the class statics.
        byte[] staticData = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        StreamingPrimitiveCodec.Decoder staticDecoder = StreamingPrimitiveCodec.buildDecoder(staticData);
        for (Class<?> clazz : classes) {
            output.print("Class(" + shortenClassName(clazz.getName()) + "): ");
            codec.deserializeClass(staticDecoder, clazz);
            output.println();
        }
        
        // Walk each instance in the queue (potentially adding more as we go), writing each to the storage.
        Field instanceIdField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("instanceId");
        instanceIdField.setAccessible(true);
        while (!instanceQueue.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object instance = instanceQueue.poll();
            String className = instance.getClass().getName();
            long instanceId = instanceIdField.getLong(instance);
            output.print(shortenClassName(className) + "("+ instanceId + "): ");
            
            // We need to look into a few special-cases here:
            // -we decode String but no other shadow instances.
            boolean isStringCase = className.equals(org.aion.avm.shadow.java.lang.String.class.getName());
            // -we decode ObjectArray, but no other array wrappers.
            boolean isObjectArrayCase = className.equals(org.aion.avm.arraywrapper.ObjectArray.class.getName());
            // -we decode all user-defined objects.
            boolean isCommonUserDefinedCase = (!className.startsWith(PackageConstants.kShadowDotPrefix) && !className.startsWith(PackageConstants.kArrayWrapperDotPrefix));
            if (isStringCase || isObjectArrayCase || isCommonUserDefinedCase) {
                // We are going to process this instance so load its data and create its decoder.
                byte[] instanceData = kernel.getStorage(address, StorageKeys.forInstance(instanceId));
                StreamingPrimitiveCodec.Decoder instanceDecoder = StreamingPrimitiveCodec.buildDecoder(instanceData);
                
                // We need to special-case the hashCode (normally handled by the shadow Object implementation).
                output.print("hashCode: int(" + instanceDecoder.decodeInt() + "), ");
                
                // From here, we diverge to handle each case, specially.
                if (isStringCase) {
                    // Decode UTF-8.
                    int length = instanceDecoder.decodeInt();
                    byte[] bytes = new byte[length];
                    instanceDecoder.decodeBytesInto(bytes);
                    output.print("string: \"" + new String(bytes, StandardCharsets.UTF_8) + "\"");
                } else  if (isObjectArrayCase) {
                    // Get the length and then a list of instance stubs.
                    int length = instanceDecoder.decodeInt();
                    output.print(length + "[");
                    for (int i = 0; i < length; ++i) {
                        org.aion.avm.shadow.java.lang.Object stub = codec.decodeStub(instanceDecoder);
                        output.print("ref(" + stub + "), ");
                    }
                    output.print("]");
                } else {
                    // Call the automatic deserializer.
                    codec.partialAutomaticDeserializeInstance(instanceDecoder, instance, null);
                }
            } else {
                // We might do something with this type, in the future, but not right now.
                output.print("(ignored)");
            }
            output.println();
        }
    }

    private static String shortenClassName(String className) {
        int indexOfLastDot = className.lastIndexOf(".");
        return (-1 != indexOfLastDot)
                ? className.substring(indexOfLastDot + 1)
                : className;
        
    }
}
