package org.aion.avm.core.util;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.aion.avm.core.types.ImmortalDappModule;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.ConstructorCache;
import org.aion.avm.core.persistence.SerializedRepresentation;
import org.aion.avm.core.persistence.SerializedRepresentationCodec;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.IRegularNode;
import org.aion.avm.core.persistence.NodePersistenceToken;
import org.aion.avm.core.persistence.ReflectedFieldCache;
import org.aion.avm.core.persistence.ReflectionStructureCodec;
import org.aion.avm.core.persistence.keyvalue.KeyValueNode;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterface;


/**
 * This is a tool to examine the storage graph for a given DApp.
 * All this requires is a TransactionContext to access the storage and the address of the DApp in question and it will walk
 * the entire storage, from the class static roots, and dump a human-readable representation to an output stream.
 * Note that the output is produced as a queue, not a stack, so the representation is completely flat (not attempting to
 * indent or otherwise render the graph).  Each line is a single class or object instance.
 * 
 * NOTE:  This is currently tightly coupled to the key-value object graph implementation since it actually wants to show the
 * links in their native form (instanceIds).
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
        byte[] immortalDappJar = kernel.getCode(dappAddress);
        ImmortalDappModule app = ImmortalDappModule.readFromJar(immortalDappJar);
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes, Helpers.loadDefaultHelperBytecode());
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        List<Class<?>> alphabeticalContractClasses = Helpers.getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet());
        
        // We need to install a fake Helper since we create artificial shadow String objects to communicate some information back.
        // (We are using ReflectionStructureCodec.IFieldPopulator to store human-readable reference descriptions into fields).
        final AvmClassLoader avmClassLoader = NodeEnvironment.singleton.createInvocationClassLoader(Collections.emptyMap());
        new Helper(avmClassLoader, 1_000_000L, 1);
        KeyValueObjectGraph objectGraph = new KeyValueObjectGraph(kernel, dappAddress);
        doReadEntireStorage(output, classLoader, objectGraph, alphabeticalContractClasses);
        Helper.clearTestingState();
    }


    private static void doReadEntireStorage(PrintStream output, ClassLoader loader, KeyValueObjectGraph objectGraph, List<Class<?>> classes) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // These objects are filled/used by the populator.
        Set<Long> processed = new HashSet<>();
        Queue<org.aion.avm.shadow.java.lang.Object> instanceQueue = new LinkedList<>();
        ConstructorCache constructorCache = new ConstructorCache(loader);
        
        // Create the populator which describes and outputs what it sees.
        ReflectionStructureCodec.IFieldPopulator populator =  new ReflectionStructureCodec.IFieldPopulator() {
            @Override
            public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) {
                output.println("\t" + field.getName() + ": boolean(" + val + ")");
            }
            @Override
            public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) {
                output.println("\t" + field.getName() + ": double(" + val + ")");
            }
            @Override
            public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) {
                output.println("\t" + field.getName() + ": long(" + val + ")");
            }
            @Override
            public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) {
                output.println("\t" + field.getName() + ": float(" + val + ")");
            }
            @Override
            public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) {
                output.println("\t" + field.getName() + ": int(" + val + ")");
            }
            @Override
            public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) {
                output.println("\t" + field.getName() + ": char(" + val + ")");
            }
            @Override
            public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) {
                output.println("\t" + field.getName() + ": short(" + val + ")");
            }
            @Override
            public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) {
                output.println("\t" + field.getName() + ": byte(" + val + ")");
            }
            @Override
            public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) {
                output.println("\t" + field.getName() + ": ref(" + val + ")");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object instantiateReference(INode rawNode) {
                // We handle these in differently descriptive way (note that this implementation is tightly coupled to the KeyValueObjectGraph).
                org.aion.avm.shadow.java.lang.Object result = null;
                if (rawNode instanceof KeyValueNode) {
                    KeyValueNode node = (KeyValueNode) rawNode;
                    String className = node.getInstanceClassName();
                    long instanceId = node.getInstanceId();
                    
                    // Note that we can't decode all object instances (most shadows and array wrappers, for example), but we will determine that on the reading side.
                    // For now, just make sure we enqueue each instance only once.
                    if (!processed.contains(instanceId)) {
                        processed.add(instanceId);
                        try {
                            Constructor<?> con = constructorCache.getConstructorForClassName(className);
                            org.aion.avm.shadow.java.lang.Object stub = (org.aion.avm.shadow.java.lang.Object)con.newInstance(null, new NodePersistenceToken(node, false));
                            instanceQueue.add(stub);
                        } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            // Errors not handled in this tool.
                            throw RuntimeAssertionError.unexpected(e);
                        }
                    }
                    result = new org.aion.avm.shadow.java.lang.String("instance(" + shortenClassName(className) + ", " + instanceId + ")");
                } else if (rawNode instanceof ClassNode) {
                    ClassNode node = (ClassNode) rawNode;
                    result = new org.aion.avm.shadow.java.lang.String("class(" + shortenClassName(node.className) + ")");
                } else if (rawNode instanceof ConstantNode) {
                    ConstantNode node = (ConstantNode) rawNode;
                    result = new org.aion.avm.shadow.java.lang.String("constant(" + node.constantHashCode + ")");
                } else {
                    // This better be null.
                    RuntimeAssertionError.assertTrue(null == rawNode);
                    result = new org.aion.avm.shadow.java.lang.String("null");
                }
                return result;
            }
        };
        
        // Create the codec back-ended on the populator.
        // (note that it requires a fieldCache but we don't attempt to reuse this, in our case).
        ReflectedFieldCache fieldCache = new ReflectedFieldCache();
        NullFeeProcessor feeProcessor = new NullFeeProcessor();
        ReflectionStructureCodec codec = new ReflectionStructureCodec(fieldCache, populator, feeProcessor, objectGraph);
        
        // Extract the raw data for the class statics.
        SerializedRepresentationCodec.Decoder staticDecoder = new SerializedRepresentationCodec.Decoder(objectGraph.getRoot());
        for (Class<?> clazz : classes) {
            output.println("Class(" + shortenClassName(clazz.getName()) + "): ");
            codec.deserializeClass(staticDecoder, clazz);
        }
        
        // Walk each instance in the queue (potentially adding more as we go), writing each to the storage.
        Field persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("persistenceToken");
        persistenceTokenField.setAccessible(true);
        while (!instanceQueue.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object instance = instanceQueue.poll();
            String className = instance.getClass().getName();
            // We know that these are INode instances.
            IRegularNode persistenceToken = ((NodePersistenceToken)persistenceTokenField.get(instance)).node;
            long instanceId = ((KeyValueNode) persistenceToken).getInstanceId();
            output.println(shortenClassName(className) + "("+ instanceId + "): ");
            
            // We need to look into a few special-cases here:
            // -we decode String but no other shadow instances.
            boolean isStringCase = className.equals(org.aion.avm.shadow.java.lang.String.class.getName());
            // -we decode ObjectArray, but no other array wrappers.
            boolean isObjectArrayCase = className.equals(org.aion.avm.arraywrapper.ObjectArray.class.getName());
            // -we decode all user-defined objects.
            boolean isCommonUserDefinedCase = (!className.startsWith(PackageConstants.kShadowDotPrefix) && !className.startsWith(PackageConstants.kArrayWrapperDotPrefix));
            // We are going to process this instance so load its data and create its decoder.
            SerializedRepresentationCodec.Decoder instanceDecoder = new SerializedRepresentationCodec.Decoder(persistenceToken.loadOriginalData());
            
            if (isStringCase || isObjectArrayCase || isCommonUserDefinedCase) {
                // We need to special-case the hashCode (normally handled by the shadow Object implementation).
                output.println("\thashCode: int(" + instanceDecoder.decodeInt() + "), ");
                
                // From here, we diverge to handle each case, specially.
                if (isStringCase) {
                    // Decode UTF-8.
                    int length = instanceDecoder.decodeInt();
                    byte[] bytes = new byte[length];
                    instanceDecoder.decodeBytesInto(bytes);
                    output.println("\tstring: \"" + new String(bytes, StandardCharsets.UTF_8) + "\"");
                } else  if (isObjectArrayCase) {
                    // Get the length and then a list of instance stubs.
                    int length = instanceDecoder.decodeInt();
                    output.println(length + "[");
                    for (int i = 0; i < length; ++i) {
                        org.aion.avm.shadow.java.lang.Object stub = codec.decodeStub(instanceDecoder.decodeReference());
                        output.println("\tref(" + stub + "), ");
                    }
                    output.println("]");
                } else {
                    // Call the automatic deserializer.
                    codec.partialAutomaticDeserializeInstance(instanceDecoder, instance, null);
                }
            } else {
                // We might do something with this type, in the future, but not right now.
                // Just write the references.
                SerializedRepresentation extent = persistenceToken.loadOriginalData();
                
                output.println("\t(opaque) refs: " + extent.references.length + "[");
                for (int i = 0; i < extent.references.length; ++i) {
                    org.aion.avm.shadow.java.lang.Object stub = codec.decodeStub(instanceDecoder.decodeReference());
                    output.println("\tref(" + stub + "), ");
                }
                output.println("]");
            }
        }
    }

    private static String shortenClassName(String className) {
        int indexOfLastDot = className.lastIndexOf(".");
        return (-1 != indexOfLastDot)
                ? className.substring(indexOfLastDot + 1)
                : className;
        
    }
}
