package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Similar to the ReflectionStructureCodec but only used for reentrant DApp invocation.
 * Specifically, knows how to hold an old version of the data in class statics and create a new object graph which lazily loads from the old
 * one.
 * Note that instance relationships are very important for this component.  Specifically, an instance in the caller can NEVER be replaced by
 * a new callee instance (since it might be referenced from the stack, for example).  This also means that an association between
 * callee-instances and caller-instances must be maintained (either within the instances, themselves, or in a side identity map) so that
 * changes can be written-back.
 * 
 * See issue-167 for more information.
 * TODO:  Investigate possible ways to generalize all field walkers found here and in ReflectionStructureCodec.
 */
public class ReentrantGraphProcessor implements LoopbackCodec.AutomaticSerializer, LoopbackCodec.AutomaticDeserializer, ISuspendableInstanceLoader {
    /**
     * We apply the DONE_MARKER to a callee object when we add it to a queue to process for possible write-back to the caller.
     * This is used to mark the object so we don't add it to the queue more than once.
     * We only mark the callee objects since we want a consistent convention.
     * NOTE:  No references to DONE_MARKER should be reachable in the graph when any user code is being run.
     */
    private static IDeserializer DONE_MARKER = new GraphWalkingMarker();

    // NOTE:  This fieldCache is passed in from outside so we can modify it for later use (it is used for multiple instances of this).
    private final ConstructorCache constructorCache;
    private final ReflectedFieldCache fieldCache;
    private final IStorageFeeProcessor feeProcessor;
    private final List<Class<?>> classes;
    
    // We need bidirectional identity maps:
    // -callee->caller for deserializing a callee object - it needs to lookup the caller source (although this could be managed by a field in the object).
    // -caller->callee for uniquing instance stubs (they don't have IDs but are looked up by instance, directly).
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> calleeToCallerMap;
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> callerToCalleeMap;
    
    // We only hold the deserializerField because we need to check if it is null when traversing the graph for objects to serialize.
    private final Field deserializerField;
    private final Field persistenceTokenField;
    
    // (mostly non-final just to prove that the state machine is being used correctly).
    private Queue<Object> previousStatics;
    
    // We scan all the objects we loaded as roots since we don't want to hide changes to them if reachable via other paths (issue-249).
    private final List<org.aion.avm.shadow.java.lang.Object> loadedObjectInstances;
    
    // ISuspendableInstanceLoader state.
    private boolean isActiveInstanceLoader;

    private final NotLoadedDeserializer initialDeserializer;

    public ReentrantGraphProcessor(ConstructorCache constructorCache, ReflectedFieldCache fieldCache, IStorageFeeProcessor feeProcessor, List<Class<?>> classes) {
        this.constructorCache = constructorCache;
        this.fieldCache = fieldCache;
        this.feeProcessor = feeProcessor;
        this.classes = classes;
        
        this.calleeToCallerMap = new IdentityHashMap<>();
        this.callerToCalleeMap = new IdentityHashMap<>();
        
        try {
            this.deserializerField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("deserializer");
            this.deserializerField.setAccessible(true);
            this.persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("persistenceToken");
            this.persistenceTokenField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
        this.loadedObjectInstances = new LinkedList<>();
        this.isActiveInstanceLoader = true;
        this.initialDeserializer = new NotLoadedDeserializer();
    }

    /**
     * Called at the beginning of a reentrant call.
     * This copies the existing statics (including representing roots to the existing object graph they describe) to a back-buffer and replaces
     * all existing object references in the statics with instance stubs which back-end on the original versions.
     */
    public void captureAndReplaceStaticState() {
        // The internal mapping structures must be empty.
        RuntimeAssertionError.assertTrue(this.calleeToCallerMap.isEmpty());
        RuntimeAssertionError.assertTrue(this.callerToCalleeMap.isEmpty());
        
        try {
            internalCaptureAndReplaceStaticState();
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // Any failures at this point are either failure mis-configuration or serious bugs in our implementation.
            RuntimeAssertionError.unexpected(e);
        }
    }

    private void internalCaptureAndReplaceStaticState() throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // We will save out and build new stubs for references in the same pass.
        Queue<Object> inOrderData = new LinkedList<>();
        // Note that we need to measure the "serialized" size of the statics in order to provide consistent IStorageFeeProcessor billing (treating this as a "read from storage").
        int byteSize = 0;
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                        boolean val = field.getBoolean(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.BOOLEAN;
                    } else if (byte.class == type) {
                        byte val = field.getByte(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.BYTE;
                    } else if (short.class == type) {
                        short val = field.getShort(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.SHORT;
                    } else if (char.class == type) {
                        char val = field.getChar(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.CHAR;
                    } else if (int.class == type) {
                        int val = field.getInt(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.INT;
                    } else if (float.class == type) {
                        float val = field.getFloat(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.FLOAT;
                    } else if (long.class == type) {
                        long val = field.getLong(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.LONG;
                    } else if (double.class == type) {
                        double val = field.getDouble(null);
                        inOrderData.add(val);
                        byteSize += ByteSizes.DOUBLE;
                    } else {
                        // This should be a shadow object.
                        org.aion.avm.shadow.java.lang.Object contents = (org.aion.avm.shadow.java.lang.Object)field.get(null);
                        inOrderData.add(contents);
                        if (null != contents) {
                            // We now want to replace this object with a stub which knows how to deserialize itself from contents.
                            org.aion.avm.shadow.java.lang.Object stub = internalGetCalleeStubForCaller(contents);
                            field.set(null, stub);
                        }
                        // Use the fixed-size reference accounting.
                        byteSize += ByteSizes.REFERENCE;
                    }
                }
            }
        }
        this.feeProcessor.readStaticDataFromHeap(byteSize);
        RuntimeAssertionError.assertTrue(null == this.previousStatics);
        this.previousStatics = inOrderData;
    }

    /**
     * Called after a reentrant call finishes in an error state and must be reverted.
     * This discards the current graph and over-writes all class statics with the contents of the back-buffer.
     */
    public void revertToStoredFields() {
        try {
            internalRevertToStoredFields();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any failures at this point are either failure mis-configuration or serious bugs in our implementation.
            RuntimeAssertionError.unexpected(e);
        }
    }

    private void internalRevertToStoredFields() throws IllegalArgumentException, IllegalAccessException {
        // This is the simple case:  walk the previous statics and over-write them with the versions we stored, originally.
        RuntimeAssertionError.assertTrue(null != this.previousStatics);
        
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                        boolean val = (Boolean)this.previousStatics.remove();
                        field.setBoolean(null, val);
                    } else if (byte.class == type) {
                        byte val = (Byte)this.previousStatics.remove();
                        field.setByte(null, val);
                    } else if (short.class == type) {
                        short val = (Short)this.previousStatics.remove();
                        field.setShort(null, val);
                    } else if (char.class == type) {
                        char val = (Character)this.previousStatics.remove();
                        field.setChar(null, val);
                    } else if (int.class == type) {
                        int val = (Integer)this.previousStatics.remove();
                        field.setInt(null, val);
                    } else if (float.class == type) {
                        float val = (Float)this.previousStatics.remove();
                        field.setFloat(null, val);
                    } else if (long.class == type) {
                        long val = (Long)this.previousStatics.remove();
                        field.setLong(null, val);
                    } else if (double.class == type) {
                        double val = (Double)this.previousStatics.remove();
                        field.setDouble(null, val);
                    } else {
                        // This should be a shadow object.
                        org.aion.avm.shadow.java.lang.Object val = (org.aion.avm.shadow.java.lang.Object)this.previousStatics.remove();
                        field.set(null, val);
                    }
                }
            }
        }
        this.previousStatics = null;
    }

    /**
     * Called after a reentrant call finishes in a success state and should be committed.
     * This considers the current graph as "correct" but prefers the object instances in the caller's graph (rooted in the back-buffer).
     * This is because the caller could still have things like stack slots which point at these older instances so we have to treat that
     * graph as canonical.
     * This disagreement is rationalized by copying the contents of each of the callee's objects into the corresponding caller's
     * instances.
     * @throws OutOfEnergyException The commit _can_ fail due to energy exhaustion, so the caller must be aware of that.  The commit will be
     * aborted, in that case, but the caller will still need to call revert (since the statics aborted statics are still installed in
     * the classes).
     */
    public void commitGraphToStoredFieldsAndRestore() throws OutOfEnergyException {
        try {
            internalCommitGraphToStoredFieldsAndRestore();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any failures at this point are either failure mis-configuration or serious bugs in our implementation.
            RuntimeAssertionError.unexpected(e);
        }
    }

    private void internalCommitGraphToStoredFieldsAndRestore() throws IllegalArgumentException, IllegalAccessException {
        // This is the complicated case:  walk the previous statics, writing only the instances back but updating each of those instances written to
        // the graph with the callee version's contents, recursively.
        // In any cases where a reference was to be copied, choose the caller version unless there isn't one, in which case the callee version can be
        // copied (new object case).  In either case, the recursive update of the graph must be continued through this newly-attached object.
        RuntimeAssertionError.assertTrue(null != this.previousStatics);
        
        // We need to start with a dry run to see if the user can afford to write-back this data (since the commit reaches a point of no return as
        // soon as we start writing to the visible object graph).
        // We can build the list of objects to process while we interpret the size that would be written back (we must NOT write to caller objects).
        // The only writes that we need to make into the heap are those to write the DONE_MARKER but that is only done against callee-space objects,
        // meaning that a revert won't see them.
        // Once we bill for all of these objects, we can update the statics and then walk the list, without needing to rediscover the graph.
        // (this way, we avoid needing a second DONE_MARKER on these objects).
        Queue<org.aion.avm.shadow.java.lang.Object> calleeObjectsToProcess = doDryRunForCommit();
        // (if we got this far without hitting OutOfEnergy, we can proceed with the commit)
        
        // We discard the statics since the only information we need from them (the caller instances from which the callees are derived) is already
        // in our calleeToCallerMap.
        this.previousStatics = null;
        
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                    } else if (byte.class == type) {
                    } else if (short.class == type) {
                    } else if (char.class == type) {
                    } else if (int.class == type) {
                    } else if (float.class == type) {
                    } else if (long.class == type) {
                    } else if (double.class == type) {
                    } else {
                        // Load the field (it will be either a new object or the callee-space object which we need to replace with its caller-space).
                        org.aion.avm.shadow.java.lang.Object callee = (org.aion.avm.shadow.java.lang.Object)field.get(null);
                        if (null != callee) {
                            // See if there is a caller version.
                            org.aion.avm.shadow.java.lang.Object caller = this.calleeToCallerMap.get(callee);
                            // If there was a caller version, copy this back (otherwise, we will continue looking at the callee).
                            if (null != caller) {
                                field.set(null, caller);
                            }
                        }
                    }
                }
            }
        }
        
        // Now that the statics are processed, we can process the queue until it is empty (this will complete the graph).
        while (!calleeObjectsToProcess.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object calleeSpaceToProcess = calleeObjectsToProcess.remove();
            
            // This is always a callee-space object but we usually want the caller-space instance.
            org.aion.avm.shadow.java.lang.Object callerSpaceCounterpart = this.calleeToCallerMap.get(calleeSpaceToProcess);
            if (null != callerSpaceCounterpart) {
                // If there is a caller-space object, it MUST already be loaded by the time we get here.
                RuntimeAssertionError.assertTrue(null == this.deserializerField.get(callerSpaceCounterpart));
            }
            
            // We want to copy "back" to the caller space so serialize the callee and deserialize them into either the caller or callee (if there was no caller).
            // (the reason to serialize/deserialize the same object is to process the object references in a general way:  try to map back into the caller space).
            Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper = (calleeField) -> {
                org.aion.avm.shadow.java.lang.Object caller = null;
                // We only want to map this back if we are non-null and there actually is a corresponding caller-space object.
                if ((null != calleeField) && this.calleeToCallerMap.containsKey(calleeField)) {
                    caller = this.calleeToCallerMap.get(calleeField);
                } else {
                    // Otherwise, just return whatever we were given (leave the field unchanged).
                    caller = calleeField;
                }
                return caller;
            };
            LoopbackCodec loopback = new LoopbackCodec(this, this, deserializeHelper);
            // Serialize the callee-space object.
            calleeSpaceToProcess.serializeSelf(null, loopback);
            
            if (null != callerSpaceCounterpart) {
                // Deserialize into the caller-space object.
                callerSpaceCounterpart.deserializeSelf(null, loopback);
            } else {
                // This means that the callee object is being stitched into the caller graph as a new object.
                // We want need to update any object references which may point back at older caller objects.
                calleeSpaceToProcess.deserializeSelf(null, loopback);
            }
            // Prove that we didn't miss anything.
            loopback.verifyDone();
        }
    }

    /**
     * Runs a simulation of the commit, but doesn't actually touch anything.  This involves walking the static fields but also every instance in the
     * callee address space.  Since this implies graph discovery was done, setting DONE_MARKER is done within this function, on all reachable objects.
     * The reason why we do this is so that we can treat the commit as effectively atomic since we have pushed all failures up to this dry run phase.
     * This is important since we want to apply transactional semantics (commit/rollback the entire transaction) to in-memory data structures.  Since
     * we can't rely on an underlying storage engine to give us these semantics, we emulate them by using this method to prove that we won't fail the
     * commit, if we decide to start it.
     * This means that this method is allowed to fail for any reason which should logically cause the commit to fail (typically, this is for
     * write-back billing).
     * NOTE:  The DONE_MARKER is only set on any objects inside this method.  All of the objects in the returned queue have been cleaned of this.
     * 
     * @return The queue of all objects found which need to be committed back to the caller's graph.
     */
    private Queue<org.aion.avm.shadow.java.lang.Object> doDryRunForCommit() throws IllegalArgumentException, IllegalAccessException {
        // This is the complicated case:  walk the previous statics, writing only the instances back but updating each of those instances written to
        // the graph with the callee version's contents, recursively.
        // In any cases where a reference was to be copied, choose the caller version unless there isn't one, in which case the callee version can be
        // copied (new object case).  In either case, the recursive update of the graph must be continued through this newly-attached object.
        RuntimeAssertionError.assertTrue(null != this.previousStatics);
        Queue<org.aion.avm.shadow.java.lang.Object> calleeObjectsToScan = new LinkedList<>();
        
        // Note that we need to measure the "serialized" size of the statics, as a write on commit, in order to provide consistent IStorageFeeProcessor billing (treating this as a "write to storage").
        int staticByteSize = 0;
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                        staticByteSize += ByteSizes.BOOLEAN;
                    } else if (byte.class == type) {
                        staticByteSize += ByteSizes.BYTE;
                    } else if (short.class == type) {
                        staticByteSize += ByteSizes.SHORT;
                    } else if (char.class == type) {
                        staticByteSize += ByteSizes.CHAR;
                    } else if (int.class == type) {
                        staticByteSize += ByteSizes.INT;
                    } else if (float.class == type) {
                        staticByteSize += ByteSizes.FLOAT;
                    } else if (long.class == type) {
                        staticByteSize += ByteSizes.LONG;
                    } else if (double.class == type) {
                        staticByteSize += ByteSizes.DOUBLE;
                    } else {
                        // Load the field (it will be either a new object or the callee-space object which we need to replace with its caller-space).
                        org.aion.avm.shadow.java.lang.Object callee = (org.aion.avm.shadow.java.lang.Object)field.get(null);
                        // See if there is a caller version.
                        // NOTE:  We use mapCalleeToCallerAndEnqueueForCommitProcessing since the dry run is where we build the object graph.
                        mapCalleeToCallerAndEnqueueForCommitProcessing(calleeObjectsToScan, callee);
                        // Use the fixed-size reference accounting.
                        staticByteSize += ByteSizes.REFERENCE;
                    }
                }
            }
        }
        // Statics are "written" as a single unit.
        this.feeProcessor.writeStaticDataToHeap(staticByteSize);
        
        // Treat any of the instances we loaded as potential roots.
        for (org.aion.avm.shadow.java.lang.Object calleeSpaceRoot : this.loadedObjectInstances) {
            mapCalleeToCallerAndEnqueueForCommitProcessing(calleeObjectsToScan, calleeSpaceRoot);
        }
        
        // Note only is this pass measuring size, but it is also finding the graph of objects to write-back, on commit, so provide that mapping function.
        Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> calleeToCallerRefMappingFunction = (calleeRef) -> mapCalleeToCallerAndEnqueueForCommitProcessing(calleeObjectsToScan, calleeRef);
        
        // We have accounted for the statics and found the initial roots so follow those roots to account for all reachable instances.
        Queue<org.aion.avm.shadow.java.lang.Object> calleeSpaceObjectsToCopyBack = new LinkedList<>();
        while (!calleeObjectsToScan.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object calleeSpaceToScan = calleeObjectsToScan.remove();
            
            int instanceByteSize = measureByteSizeOfInstance(calleeSpaceToScan, calleeToCallerRefMappingFunction);
            // Write each instance, one at a time.
            this.feeProcessor.writeOneInstanceToHeap(instanceByteSize);
            calleeSpaceObjectsToCopyBack.add(calleeSpaceToScan);
        }
        
        // Clear the DONE_MARKER, since we want to make sure no deserializeSelf calls are made to objects while one of these markers is in the graph.
        for (org.aion.avm.shadow.java.lang.Object calleeSpaceToClear : calleeSpaceObjectsToCopyBack) {
            this.deserializerField.set(calleeSpaceToClear, null);
        }
        return calleeSpaceObjectsToCopyBack;
    }

    /**
     * Called by the special IDeserializer instance we use for deserializing callee-space objects from caller-space objects.
     * 
     * @param calleeSpace The object in the callee space which must be written.
     * @param callerSpaceOriginal The original object from the caller space.
     */
    private void populateCalleeSpaceObject(org.aion.avm.shadow.java.lang.Object calleeSpace, org.aion.avm.shadow.java.lang.Object callerSpaceOriginal) {
        // We assume that the caller object has been fully deserialized.
        try {
            RuntimeAssertionError.assertTrue(null == this.deserializerField.get(callerSpaceOriginal));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Reflection errors would happen much earlier.
            RuntimeAssertionError.unexpected(e);
        }
        
        // Account for the size of this instance.
        // TODO:  Find a way to capture the size of this instance without serializing twice.
        int instanceBytes = measureByteSizeOfInstance(callerSpaceOriginal, (ignored) -> null);
        this.feeProcessor.readOneInstanceFromHeap(instanceBytes);
        
        // We want to use the same codec logic which exists in all shadow objects (since the shadow and API classes do special things here which we don't want to duplicate).
        // In this case, we want to provide an object deserialization helper which can create a callee-space instance stub.
        Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper = (callerField) -> internalGetCalleeStubForCaller(callerField);

        LoopbackCodec loopback = new LoopbackCodec(this, this, deserializeHelper);
        // Serialize the original.
        callerSpaceOriginal.serializeSelf(null, loopback);
        // Deserialize this data into the new instance.
        calleeSpace.deserializeSelf(null, loopback);
        // Prove that we didn't miss anything.
        loopback.verifyDone();
    }

    @Override
    public void partiallyAutoSerialize(Queue<Object> dataQueue, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            hierarchyAutoSerialize(dataQueue, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partiallyAutoDeserialize(Queue<Object> dataQueue, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            hierarchyAutoDeserialize(dataQueue, deserializeHelper, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void loaderDidBecomeActive() {
        RuntimeAssertionError.assertTrue(!this.isActiveInstanceLoader);
        this.isActiveInstanceLoader = true;
    }

    @Override
    public void loaderDidBecomeInactive() {
        RuntimeAssertionError.assertTrue(this.isActiveInstanceLoader);
        this.isActiveInstanceLoader = false;
    }


    private void hierarchyAutoSerialize(Queue<Object> dataQueue, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but the next invocation can, so pass null as the manual class to them.
            hierarchyAutoSerialize(dataQueue, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            hierarchyAutoSerialize(dataQueue, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                autoSerializeDeclaredFields(dataQueue, clazz, object);
            }
        }
    }

    private void hierarchyAutoDeserialize(Queue<Object> dataQueue, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but the next invocation can, so pass null as the manual class to them.
            hierarchyAutoDeserialize(dataQueue, deserializeHelper, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            hierarchyAutoDeserialize(dataQueue, deserializeHelper, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                autoDeserializeDeclaredFields(dataQueue, deserializeHelper, clazz, object);
            }
        }
    }

    private void autoSerializeDeclaredFields(Queue<Object> dataQueue, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (0x0 == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = field.getBoolean(object);
                    dataQueue.add(val);
                } else if (byte.class == type) {
                    byte val = field.getByte(object);
                    dataQueue.add(val);
                } else if (short.class == type) {
                    short val = field.getShort(object);
                    dataQueue.add(val);
                } else if (char.class == type) {
                    char val = field.getChar(object);
                    dataQueue.add(val);
                } else if (int.class == type) {
                    int val = field.getInt(object);
                    dataQueue.add(val);
                } else if (float.class == type) {
                    float val = field.getFloat(object);
                    dataQueue.add(val);
                } else if (long.class == type) {
                    long val = field.getLong(object);
                    dataQueue.add(val);
                } else if (double.class == type) {
                    double val = field.getDouble(object);
                    dataQueue.add(val);
                } else {
                    org.aion.avm.shadow.java.lang.Object ref = (org.aion.avm.shadow.java.lang.Object) field.get(object);
                    dataQueue.add(ref);
                }
            }
        } 
    }

    private void autoDeserializeDeclaredFields(Queue<Object> dataQueue, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (0x0 == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = (Boolean)dataQueue.remove();
                    field.setBoolean(object, val);
                } else if (byte.class == type) {
                    byte val = (Byte)dataQueue.remove();
                    field.setByte(object, val);
                } else if (short.class == type) {
                    short val = (Short)dataQueue.remove();
                    field.setShort(object, val);
                } else if (char.class == type) {
                    char val = (Character)dataQueue.remove();
                    field.setChar(object, val);
                } else if (int.class == type) {
                    int val = (Integer)dataQueue.remove();
                    field.setInt(object, val);
                } else if (float.class == type) {
                    float val = (Float)dataQueue.remove();
                    field.setFloat(object, val);
                } else if (long.class == type) {
                    long val = (Long)dataQueue.remove();
                    field.setLong(object, val);
                } else if (double.class == type) {
                    double val = (Double)dataQueue.remove();
                    field.setDouble(object, val);
                } else {
                    org.aion.avm.shadow.java.lang.Object val = (org.aion.avm.shadow.java.lang.Object) dataQueue.remove();
                    org.aion.avm.shadow.java.lang.Object mapped = deserializeHelper.apply(val);
                    field.set(object, mapped);
                }
            }
        } 
    }

    private org.aion.avm.shadow.java.lang.Object internalGetCalleeStubForCaller(org.aion.avm.shadow.java.lang.Object caller) {
        // Before anything, check to see if this is something we even _want_ to copy from the caller space or if it is immutable.
        org.aion.avm.shadow.java.lang.Object callee = null;
        if (SerializedInstanceStub.objectUsesReentrantCopy(caller, this.persistenceTokenField)) {
            // First, see if we already have a stub for this caller.
            callee = this.callerToCalleeMap.get(caller);
            if (null == callee) {
                // Note that this instanceId will never be used, so we pass in SerializedInstanceStub.REENTRANT_CALLEE_INSTANCE_ID.  This is because we never replace caller instances with
                // callee instance.
                // This means that, since this object is the callee representation of a caller object, it will never end up in the caller's graph, hence
                // never serialized to the storage.  The only objects which can be added to the caller's graph are new objects (which don't have an
                // instanceId, either).
                try {
                    Constructor<?> constructor = this.constructorCache.getConstructorForClassName(caller.getClass().getName());
                    // We start out with the initial deserializer, replacing with preloaded only if we load this while inactive.
                    callee = (org.aion.avm.shadow.java.lang.Object) constructor.newInstance(this.initialDeserializer, new ReentrantCallerReferenceToken(caller));
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
                    // TODO:  These should probably come through a cache.
                    RuntimeAssertionError.unexpected(e);
                }
                
                // We also need to add this to the callee-caller mapping (in the future, this might be made into a field in the object).
                this.callerToCalleeMap.put(caller, callee);
                this.calleeToCallerMap.put(callee, caller);
            }
        } else {
            // We just want to continue pointing at the caller object.
            callee = caller;
        }
        return callee;
    }

    private org.aion.avm.shadow.java.lang.Object mapCalleeToCallerAndEnqueueForCommitProcessing(Queue<org.aion.avm.shadow.java.lang.Object> calleeObjectsToProcess, org.aion.avm.shadow.java.lang.Object calleeSpace) {
        org.aion.avm.shadow.java.lang.Object callerSpace = null;
        if (SerializedInstanceStub.objectUsesReentrantCopy(calleeSpace, this.persistenceTokenField)) {
            // We want to replace this with a reference to caller-space (unless this object is new - has no mapping).
            callerSpace = this.calleeToCallerMap.get(calleeSpace);
            
            // NOTE:  We can't store the DONE_MARKER in any caller-space objects since they might have a real deserializer.
            // This means that we need to actually enqueue the callee-space object and, during processing, determine if we need to
            // actually operate on the corresponding caller (essentially determinine which copy is in the output graph).
            try {
                if (null == this.deserializerField.get(calleeSpace)) {
                    // This is either new or was faulted so add this to our queue to process.
                    calleeObjectsToProcess.add(calleeSpace);
                    // Set the market so we don't double-add it.
                    this.deserializerField.set(calleeSpace, DONE_MARKER);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                RuntimeAssertionError.unexpected(e);
            }
        }
        return callerSpace;
    }

    private int measureByteSizeOfInstance(org.aion.avm.shadow.java.lang.Object instance, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> calleeToCallerRefMappingFunction) {
        // We will use the loopback codec to serialize the object into a list we can operate on, directly, to infer size and interpret stubs.
        LoopbackCodec loopback = new LoopbackCodec(this, null, null);
        // Serialize the callee-space object.
        instance.serializeSelf(null, loopback);
        // Now, take ownership of the captured data and process it.
        Queue<Object> dataQueue = loopback.takeOwnershipOfData();
        
        // We can process the data in this queue as the size (although this does assume we know how the LoopbackCodec is implemented but it
        // already shares its queue with use, for automatic serialization, so there is no avoiding that).
        int instanceByteSize = 0;
        while (!dataQueue.isEmpty()) {
            Object elt = dataQueue.remove();
            // Handle all the boxed primitives from LoopbackCodec and our own auto methods.
            if (elt instanceof Boolean) {
                instanceByteSize += ByteSizes.BOOLEAN;
            } else if (elt instanceof Byte) {
                instanceByteSize += ByteSizes.BYTE;
            } else if (elt instanceof Short) {
                instanceByteSize += ByteSizes.SHORT;
            } else if (elt instanceof Character) {
                instanceByteSize += ByteSizes.CHAR;
            } else if (elt instanceof Integer) {
                instanceByteSize += ByteSizes.INT;
            } else if (elt instanceof Float) {
                instanceByteSize += ByteSizes.FLOAT;
            } else if (elt instanceof Long) {
                instanceByteSize += ByteSizes.LONG;
            } else if (elt instanceof Double) {
                instanceByteSize += ByteSizes.DOUBLE;
            } else {
                // This better be a shadow object.
                RuntimeAssertionError.assertTrue((null == elt) || (elt instanceof org.aion.avm.shadow.java.lang.Object));
                // We need to apply the function we were given to this reference.
                org.aion.avm.shadow.java.lang.Object callee = (org.aion.avm.shadow.java.lang.Object)elt;
                calleeToCallerRefMappingFunction.apply(callee);
                // Use the fixed-size reference accounting.
                instanceByteSize += ByteSizes.REFERENCE;
            }
        }
        // Prove that we didn't miss anything.
        loopback.verifyDone();
        return instanceByteSize;
    }


    /**
     * The deserializer installed in a stub which has only been referenced, not yet loaded.
     * Note that this isn't static since it still depends on the state/capabilities of the outer class instance.
     */
    private class NotLoadedDeserializer implements IDeserializer {
        @Override
        public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
            // All the objects we are creating to deserialize in the callee space have ReentrantCallerReferenceToken as the persistenceToken.
            RuntimeAssertionError.assertTrue(persistenceToken instanceof ReentrantCallerReferenceToken);
            org.aion.avm.shadow.java.lang.Object callerSpaceOriginal = ((ReentrantCallerReferenceToken)persistenceToken).callerSpaceOriginal;
            
            // Make sure that it is loaded.
            callerSpaceOriginal.lazyLoad();
            
            populateCalleeSpaceObject(instance, callerSpaceOriginal);
            
            // Save this instance into our root set to scan for re-save, when done (issue-249: fixes hidden changes being skipped).
            ReentrantGraphProcessor.this.loadedObjectInstances.add(instance);
        }
    }
}
