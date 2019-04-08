package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aion.avm.internal.ClassPersistenceToken;
import org.aion.avm.internal.ConstantPersistenceToken;
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
    private final Class<?>[] classes;
    
    // We need bidirectional identity maps:
    // -callee->caller for deserializing a callee object - it needs to lookup the caller source (although this could be managed by a field in the object).
    // -caller->callee for uniquing instance stubs (they don't have IDs but are looked up by instance, directly).
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> calleeToCallerMap;
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> callerToCalleeMap;
    
    // We only hold the deserializerField because we need to check if it is null when traversing the graph for objects to serialize.
    private final Field deserializerField;
    private final Field persistenceTokenField;
    
    private HeapRepresentation previousStatics;
    
    // We scan all the objects we loaded as roots since we don't want to hide changes to them if reachable via other paths (issue-249).
    private final List<org.aion.avm.shadow.java.lang.Object> loadedObjectInstances;
    
    // ISuspendableInstanceLoader state.
    private boolean isActiveInstanceLoader;

    private final NotLoadedDeserializer initialDeserializer;
    private final PreLoadedDeserializer preLoadedDeserializer;
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, Integer> objectSizesLoadedForCallee;

    public ReentrantGraphProcessor(ConstructorCache constructorCache, ReflectedFieldCache fieldCache, IStorageFeeProcessor feeProcessor, Class<?>[] classes) {
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
        this.preLoadedDeserializer = new PreLoadedDeserializer();
        this.objectSizesLoadedForCallee = new IdentityHashMap<>();
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
            HeapRepresentation staticRepresentation = internalCaptureAndReplaceStaticState(true);
            this.feeProcessor.readStaticDataFromHeap(staticRepresentation.getBillableSize());
            RuntimeAssertionError.assertTrue(null == this.previousStatics);
            this.previousStatics = staticRepresentation;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // Any failures at this point are either failure mis-configuration or serious bugs in our implementation.
            RuntimeAssertionError.unexpected(e);
        }
    }

    private HeapRepresentation internalCaptureAndReplaceStaticState(boolean shouldUpdateInstances) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // We will save out and build new stubs for references in the same pass.
        HeapRepresentationCodec.Encoder encoder = new HeapRepresentationCodec.Encoder();
        // Note that we need to measure the "serialized" size of the statics in order to provide consistent IStorageFeeProcessor billing (treating this as a "read from storage").
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                        boolean val = field.getBoolean(null);
                        encoder.encodeByte(val ? (byte)0x1 : (byte)0x0);
                    } else if (byte.class == type) {
                        byte val = field.getByte(null);
                        encoder.encodeByte(val);
                    } else if (short.class == type) {
                        short val = field.getShort(null);
                        encoder.encodeShort(val);
                    } else if (char.class == type) {
                        char val = field.getChar(null);
                        encoder.encodeChar(val);
                    } else if (int.class == type) {
                        int val = field.getInt(null);
                        encoder.encodeInt(val);
                    } else if (float.class == type) {
                        float val = field.getFloat(null);
                        encoder.encodeInt(Float.floatToIntBits(val));
                    } else if (long.class == type) {
                        long val = field.getLong(null);
                        encoder.encodeLong(val);
                    } else if (double.class == type) {
                        double val = field.getDouble(null);
                        encoder.encodeLong(Double.doubleToLongBits(val));
                    } else {
                        // This should be a shadow object.
                        org.aion.avm.shadow.java.lang.Object contents = (org.aion.avm.shadow.java.lang.Object)field.get(null);
                        encoder.encodeReference(contents);
                        if (shouldUpdateInstances && (null != contents)) {
                            // We now want to replace this object with a stub which knows how to deserialize itself from contents.
                            org.aion.avm.shadow.java.lang.Object stub = internalGetCalleeStubForCaller(contents);
                            field.set(null, stub);
                        }
                    }
                }
            }
        }
        return encoder.toHeapRepresentation();
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
        HeapRepresentationCodec.Decoder decoder = new HeapRepresentationCodec.Decoder(this.previousStatics);
        
        for (Class<?> clazz : this.classes) {
            for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
                // We are only capturing class statics.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (boolean.class == type) {
                        boolean val = ((byte)0x1 == decoder.decodeByte());
                        field.setBoolean(null, val);
                    } else if (byte.class == type) {
                        byte val = decoder.decodeByte();
                        field.setByte(null, val);
                    } else if (short.class == type) {
                        short val = decoder.decodeShort();
                        field.setShort(null, val);
                    } else if (char.class == type) {
                        char val = decoder.decodeChar();
                        field.setChar(null, val);
                    } else if (int.class == type) {
                        int val = decoder.decodeInt();
                        field.setInt(null, val);
                    } else if (float.class == type) {
                        float val = Float.intBitsToFloat(decoder.decodeInt());
                        field.setFloat(null, val);
                    } else if (long.class == type) {
                        long val = decoder.decodeLong();
                        field.setLong(null, val);
                    } else if (double.class == type) {
                        double val = Double.longBitsToDouble(decoder.decodeLong());
                        field.setDouble(null, val);
                    } else {
                        // This should be a shadow object.
                        org.aion.avm.shadow.java.lang.Object val = decoder.decodeReference();
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
            writeBackInstanceToCallerSpace(calleeSpaceToProcess);
        }
        
        // Write-back anything we loaded for a callee but didn't touch, ourselves.
        // Verify that we already processed on the instances we have loaded.
        RuntimeAssertionError.assertTrue(this.loadedObjectInstances.isEmpty());
        for (org.aion.avm.shadow.java.lang.Object toWrite: this.objectSizesLoadedForCallee.keySet()) {
            // Note that these aren't in the initial "dry run" since they don't contribute to the billing system - if we are going to commit, write these back for free.
            writeBackInstanceToCallerSpace(toWrite);
        }
        this.objectSizesLoadedForCallee.clear();
    }

    private void writeBackInstanceToCallerSpace(org.aion.avm.shadow.java.lang.Object calleeSpaceToProcess) {
        // This is always a callee-space object but we usually want the caller-space instance.
        org.aion.avm.shadow.java.lang.Object callerSpaceCounterpart = this.calleeToCallerMap.get(calleeSpaceToProcess);
        // Note that the IDeserializer of the callerSpaceCounterpart is usually null but it might be a "pre-loaded" variant.
        
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
        loopback.switchToDecode();
        
        if (null != callerSpaceCounterpart) {
            // Deserialize into the caller-space object.
            callerSpaceCounterpart.deserializeSelf(null, loopback);
        } else {
            // This means that the callee object is being stitched into the caller graph as a new object.
            // We want need to update any object references which may point back at older caller objects.
            calleeSpaceToProcess.deserializeSelf(null, loopback);
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
        
        // Just walk through the reference fields of the statics to find objects for commit processing.
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
                        // See if there is a caller version.
                        // NOTE:  We use mapCalleeToCallerAndEnqueueForCommitProcessing since the dry run is where we build the object graph.
                        selectiveEnqueueCalleeSpaceForCommitProcessing(calleeObjectsToScan, callee);
                    }
                }
            }
        }
        // Statics are "written" as a single unit.
        // Check if the static actually changed.
        HeapRepresentation endStatics = null;
        try {
            endStatics = internalCaptureAndReplaceStaticState(false);
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // Any failures at this point are either failure mis-configuration or serious bugs in our implementation.
            RuntimeAssertionError.unexpected(e);
        }
        if (!doRepresentationsMatch(this.previousStatics, endStatics)) {
            this.feeProcessor.writeUpdateStaticDataToHeap(endStatics.getBillableSize());
        }
        
        // Treat any of the instances we loaded as potential roots.
        for (org.aion.avm.shadow.java.lang.Object calleeSpaceRoot : this.loadedObjectInstances) {
            selectiveEnqueueCalleeSpaceForCommitProcessing(calleeObjectsToScan, calleeSpaceRoot);
        }
        // Clear this so we can assert we are done with it when finishing.
        this.loadedObjectInstances.clear();
        
        // Note only is this pass measuring size, but it is also finding the graph of objects to write-back, on commit, so provide that mapping function.
        Consumer<org.aion.avm.shadow.java.lang.Object> calleeSpaceInstanceProcessor = (calleeRef) -> selectiveEnqueueCalleeSpaceForCommitProcessing(calleeObjectsToScan, calleeRef);
        
        // We have accounted for the statics and found the initial roots so follow those roots to account for all reachable instances.
        Queue<org.aion.avm.shadow.java.lang.Object> calleeSpaceObjectsToCopyBack = new LinkedList<>();
        while (!calleeObjectsToScan.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object calleeSpaceToScan = calleeObjectsToScan.remove();
            
            // Determine if this is a new instance or an update.
            // TODO:  Verify that we are not seeing a "new instance" which was already billed as new in a callee frame.  If this becomes reachable
            // in this frame, but was billed in the callee frame, we will probably misinterpret it as a new instance, again.
            // The only way to solve this problem is likely to communicate these new instances (and potentially their serialized form) back to the
            // caller so it knows to avoid billing them as new instances if the reentrant call commits (note that knowing whether or not this
            // already-billed new instance requires an updated write-back will depend on communicating the serialized state, too).
            
            // Anything we found in this queue MUST be a ReentrantCallerReferenceToken (modified) or null (new).
            ReentrantCallerReferenceToken objectToken = (ReentrantCallerReferenceToken) safeExtractPersistenceToken(calleeSpaceToScan);
            boolean isNewInstance = (null == objectToken);
            
            int instanceByteSize = measureByteSizeOfInstance(calleeSpaceToScan, calleeSpaceInstanceProcessor);
            // Write each instance, one at a time.
            if (isNewInstance) {
                this.feeProcessor.writeFirstOneInstanceToHeap(instanceByteSize);
            } else {
                // This is an existing instance so we want to see if we actually want to write-back or not.
                if (didObjectChange(objectToken.callerSpaceOriginal, calleeSpaceToScan)) {
                    this.feeProcessor.writeUpdateOneInstanceToHeap(instanceByteSize);
                }
            }
            calleeSpaceObjectsToCopyBack.add(calleeSpaceToScan);
        }
        
        // Clear the DONE_MARKER, since we want to make sure no deserializeSelf calls are made to objects while one of these markers is in the graph.
        for (org.aion.avm.shadow.java.lang.Object calleeSpaceToClear : calleeSpaceObjectsToCopyBack) {
            this.deserializerField.set(calleeSpaceToClear, null);
        }
        return calleeSpaceObjectsToCopyBack;
    }

    private boolean didObjectChange(org.aion.avm.shadow.java.lang.Object callerSpaceOriginal, org.aion.avm.shadow.java.lang.Object calleeSpaceCopy) {
        // Serialize both of these with a loopback so we can compare each element.
        LoopbackCodec callerLoopback = new LoopbackCodec(this, null, null);
        callerSpaceOriginal.serializeSelf(null, callerLoopback);
        HeapRepresentation callerData = callerLoopback.takeOwnershipOfData();
        LoopbackCodec calleeLoopback = new LoopbackCodec(this, null, null);
        calleeSpaceCopy.serializeSelf(null, calleeLoopback);
        HeapRepresentation calleeData = calleeLoopback.takeOwnershipOfData();
        
        // If the queues don't match, the instance changed.
        return !doRepresentationsMatch(callerData, calleeData);
    }

    private boolean doRepresentationsMatch(HeapRepresentation callerRepresentation, HeapRepresentation calleeRepresentation) {
        // For now, we still need to walk the internals of these.
        Object[] callerData = callerRepresentation.buildInternalsArray();
        Object[] calleeData = calleeRepresentation.buildInternalsArray();
        
        // These MUST be the same size since they are the same types.
        RuntimeAssertionError.assertTrue(callerData.length == calleeData.length);
        
        // Walk the lists together and compare corresponding elements:
        // -primitives compared directly as primitives.
        // -references are instance-compared AFTER the callee-space references are mapped back to the caller-space (any non-null instance which can't map is obviously a change).
        boolean queuesDoMatch = true;
        int index = 0;
        while (queuesDoMatch && (index < callerData.length)) {
            Object callerElt = callerData[index];
            Object calleeElt = calleeData[index];
            
            // Handle all the boxed primitives from LoopbackCodec and our own auto methods.
            if (callerElt instanceof Boolean) {
                queuesDoMatch = (((Boolean)callerElt).booleanValue() == ((Boolean)calleeElt).booleanValue());
            } else if (callerElt instanceof Byte) {
                queuesDoMatch = (((Byte)callerElt).byteValue() == ((Byte)calleeElt).byteValue());
            } else if (callerElt instanceof Short) {
                queuesDoMatch = (((Short)callerElt).shortValue() == ((Short)calleeElt).shortValue());
            } else if (callerElt instanceof Character) {
                queuesDoMatch = (((Character)callerElt).charValue() == ((Character)calleeElt).charValue());
            } else if (callerElt instanceof Integer) {
                queuesDoMatch = (((Integer)callerElt).intValue() == ((Integer)calleeElt).intValue());
            } else if (callerElt instanceof Float) {
                queuesDoMatch = (((Float)callerElt).floatValue() == ((Float)calleeElt).floatValue());
            } else if (callerElt instanceof Long) {
                queuesDoMatch = (((Long)callerElt).longValue() == ((Long)calleeElt).longValue());
            } else if (callerElt instanceof Double) {
                queuesDoMatch = (((Double)callerElt).doubleValue() == ((Double)calleeElt).doubleValue());
            } else {
                // Note that this might not be a type that gets copied so just check the direct match, first.
                if (callerElt != calleeElt) {
                    // There is a difference so try the mapping and, as long as the mapping didn't fail, compare again.
                    org.aion.avm.shadow.java.lang.Object callerSpaceRef = this.calleeToCallerMap.get(calleeElt);
                    // This only matches if the updated reference matches and we didn't fail (can't convert a non-null to null and match).
                    queuesDoMatch = ((null != callerSpaceRef) && (callerElt == callerSpaceRef));
                } else {
                    // Still match.
                    queuesDoMatch = true;
                }
            }
            index += 1;
        }
        return queuesDoMatch;
    }

    /**
     * Called by the special IDeserializer instance we use for deserializing callee-space objects from caller-space objects.
     * 
     * @param calleeSpace The object in the callee space which must be written.
     * @param callerSpaceOriginal The original object from the caller space.
     * @return The billing-relevant instance size.
     */
    private int populateCalleeSpaceObject(org.aion.avm.shadow.java.lang.Object calleeSpace, org.aion.avm.shadow.java.lang.Object callerSpaceOriginal) {
        // Note that the IDeserializer for callerSpaceOriginal is usually null but may actually be a "pre-loaded" deserializer.
        
        // Account for the size of this instance.
        // TODO:  Find a way to capture the size of this instance without serializing twice.
        int instanceBytes = measureByteSizeOfInstance(callerSpaceOriginal, (ignored) -> {});
        
        // We want to use the same codec logic which exists in all shadow objects (since the shadow and API classes do special things here which we don't want to duplicate).
        // In this case, we want to provide an object deserialization helper which can create a callee-space instance stub.
        Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper = (callerField) -> internalGetCalleeStubForCaller(callerField);

        LoopbackCodec loopback = new LoopbackCodec(this, this, deserializeHelper);
        // Serialize the original.
        callerSpaceOriginal.serializeSelf(null, loopback);
        loopback.switchToDecode();
        // Deserialize this data into the new instance.
        calleeSpace.deserializeSelf(null, loopback);
        return instanceBytes;
    }

    @Override
    public void partiallyAutoSerialize(HeapRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            hierarchyAutoSerialize(encoder, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partiallyAutoDeserialize(HeapRepresentationCodec.Decoder decoder, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            hierarchyAutoDeserialize(decoder, deserializeHelper, instance.getClass(), instance, firstManualClass);
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


    private void hierarchyAutoSerialize(HeapRepresentationCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
            hierarchyAutoSerialize(encoder, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            hierarchyAutoSerialize(encoder, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                autoSerializeDeclaredFields(encoder, clazz, object);
            }
        }
    }

    private void hierarchyAutoDeserialize(HeapRepresentationCodec.Decoder decoder, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
            hierarchyAutoDeserialize(decoder, deserializeHelper, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            hierarchyAutoDeserialize(decoder, deserializeHelper, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                autoDeserializeDeclaredFields(decoder, deserializeHelper, clazz, object);
            }
        }
    }

    private void autoSerializeDeclaredFields(HeapRepresentationCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (0x0 == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = field.getBoolean(object);
                    encoder.encodeByte(val ? (byte)0x1 : (byte)0x0);
                } else if (byte.class == type) {
                    byte val = field.getByte(object);
                    encoder.encodeByte(val);
                } else if (short.class == type) {
                    short val = field.getShort(object);
                    encoder.encodeShort(val);
                } else if (char.class == type) {
                    char val = field.getChar(object);
                    encoder.encodeChar(val);
                } else if (int.class == type) {
                    int val = field.getInt(object);
                    encoder.encodeInt(val);
                } else if (float.class == type) {
                    float val = field.getFloat(object);
                    encoder.encodeInt(Float.floatToIntBits(val));
                } else if (long.class == type) {
                    long val = field.getLong(object);
                    encoder.encodeLong(val);
                } else if (double.class == type) {
                    double val = field.getDouble(object);
                    encoder.encodeLong(Double.doubleToLongBits(val));
                } else {
                    org.aion.avm.shadow.java.lang.Object ref = (org.aion.avm.shadow.java.lang.Object) field.get(object);
                    encoder.encodeReference(ref);
                }
            }
        } 
    }

    private void autoDeserializeDeclaredFields(HeapRepresentationCodec.Decoder decoder, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (0x0 == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = ((byte)0x1 == decoder.decodeByte());
                    field.setBoolean(object, val);
                } else if (byte.class == type) {
                    byte val = decoder.decodeByte();
                    field.setByte(object, val);
                } else if (short.class == type) {
                    short val = decoder.decodeShort();
                    field.setShort(object, val);
                } else if (char.class == type) {
                    char val = decoder.decodeChar();
                    field.setChar(object, val);
                } else if (int.class == type) {
                    int val = decoder.decodeInt();
                    field.setInt(object, val);
                } else if (float.class == type) {
                    float val = Float.intBitsToFloat(decoder.decodeInt());
                    field.setFloat(object, val);
                } else if (long.class == type) {
                    long val = decoder.decodeLong();
                    field.setLong(object, val);
                } else if (double.class == type) {
                    double val = Double.longBitsToDouble(decoder.decodeLong());
                    field.setDouble(object, val);
                } else {
                    org.aion.avm.shadow.java.lang.Object val = decoder.decodeReference();
                    org.aion.avm.shadow.java.lang.Object mapped = deserializeHelper.apply(val);
                    field.set(object, mapped);
                }
            }
        } 
    }

    private org.aion.avm.shadow.java.lang.Object internalGetCalleeStubForCaller(org.aion.avm.shadow.java.lang.Object caller) {
        // Before anything, check to see if this is something we even _want_ to copy from the caller space or if it is immutable.
        org.aion.avm.shadow.java.lang.Object callee = null;
        if (objectUsesReentrantCopy(caller)) {
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

    /**
     * Called when scanning object references in the callee space (typically for instance size measurement, etc).
     * Enqueues the given calleeSpace object for commit processing so long as it is a copied type (not constant/class) and hasn't already been enqueued for processing.
     * 
     * @param calleeObjectsToProcess The consumer of objects the function decides to enqueue for commit processing.
     * @param calleeSpace The callee-space object reference to check.
     */
    private void selectiveEnqueueCalleeSpaceForCommitProcessing(Queue<org.aion.avm.shadow.java.lang.Object> calleeObjectsToProcess, org.aion.avm.shadow.java.lang.Object calleeSpace) {
        if (objectUsesReentrantCopy(calleeSpace)) {
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
    }

    private int measureByteSizeOfInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> calleeSpaceInstanceProcessor) {
        // We will use the loopback codec to serialize the object into a list we can operate on, directly, to infer size and interpret stubs.
        LoopbackCodec loopback = new LoopbackCodec(this, null, null);
        // Serialize the callee-space object.
        instance.serializeSelf(null, loopback);
        // Now, take ownership of the captured data and process it.
        HeapRepresentation representation = loopback.takeOwnershipOfData();
        
        // TODO: Replace this with another method of accessing object references.
        Object[] internals = representation.buildInternalsArray();
        for (Object elt : internals) {
            if (elt instanceof Boolean) {
            } else if (elt instanceof Byte) {
            } else if (elt instanceof Short) {
            } else if (elt instanceof Character) {
            } else if (elt instanceof Integer) {
            } else if (elt instanceof Float) {
            } else if (elt instanceof Long) {
            } else if (elt instanceof Double) {
            } else {
                // This better be a shadow object.
                RuntimeAssertionError.assertTrue((null == elt) || (elt instanceof org.aion.avm.shadow.java.lang.Object));
                // We need to apply the function we were given to this reference.
                org.aion.avm.shadow.java.lang.Object callee = (org.aion.avm.shadow.java.lang.Object)elt;
                calleeSpaceInstanceProcessor.accept(callee);
            }
        }
        return representation.getBillableSize();
    }

    /**
     * Used to determine if reentrant (memory-memory) calls are supposed to create an instance stub of the given object or if both spaces can
     * refer to it, directly.
     * This is important for cases like "Class" or constants where there is no real notion of how to make a "duplicate copy".
     * 
     * @param instance The object instance to check.
     * @return True if a copy should be made, false if the instance should be shared.
     */
    private boolean objectUsesReentrantCopy(org.aion.avm.shadow.java.lang.Object instance) {
        boolean shouldCopy = false;
        if (null == instance) {
            // Copy null doesn't make sense.
            shouldCopy = false;
        } else {
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(instance);
            if (persistenceToken instanceof ClassPersistenceToken) {
                // Classes don't get copied.
                shouldCopy = false;
            } else if (persistenceToken instanceof ConstantPersistenceToken) {
                // Constants don't get copied.
                shouldCopy = false;
            } else {
                // This last case is for all the cases which we _should_ copy:
                // -null (new object, not yet assigned - normal instance)
                // -reentrant caller reference token (this was copied, from a parent call, so we should also copy it)
                // -is a normal instanceId token
                // Note that we don't expect any other cases here so we can just assert these and return true.
                RuntimeAssertionError.assertTrue((null == persistenceToken)
                        || (persistenceToken instanceof ReentrantCallerReferenceToken)
                        || (persistenceToken instanceof NodePersistenceToken)
                );
                shouldCopy = true;
            }
        }
        return shouldCopy;
    }

    private IPersistenceToken safeExtractPersistenceToken(org.aion.avm.shadow.java.lang.Object instance) {
        try {
            return (IPersistenceToken)this.persistenceTokenField.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any failure related to this would have happened much earlier.
            throw RuntimeAssertionError.unexpected(e);
        }
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
            
            // Populate the object with the data from the caller instance.
            int instanceBytes = populateCalleeSpaceObject(instance, callerSpaceOriginal);
            
            // Check to see if we are currently active or if this deserialization was triggered by a callee while we are dormant.
            if (ReentrantGraphProcessor.this.isActiveInstanceLoader) {
                // We are on top so we directly loaded this in response to this DApp running.
                
                // This means that we need to bill them for it.
                ReentrantGraphProcessor.this.feeProcessor.readOneInstanceFromHeap(instanceBytes);
                
                // Save this instance into our root set to scan for re-save, when done (issue-249: fixes hidden changes being skipped).
                ReentrantGraphProcessor.this.loadedObjectInstances.add(instance);
            } else {
                // Someone else is actually loading this and we are a caller of theirs so we have to pretend this didn't happen.
                
                // First, record the billing fee we _would_ have charged them (this map is also used for the "free write-back" set).
                ReentrantGraphProcessor.this.objectSizesLoadedForCallee.put(instance, instanceBytes);
                
                // Install our pre-loaded deserializer so we can bill them for this, later.
                try {
                    ReentrantGraphProcessor.this.deserializerField.set(instance, ReentrantGraphProcessor.this.preLoadedDeserializer);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Failures here would have happened during startup.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        }
    }


    /**
     * The deserializer installed in an object instance which was fully loaded, but not while this instance loader was active.
     * This kind of deserializer is special since it knows how to delay the cost of loading, if not yet referenced, as well as
     * handling the logic of how this should be written-back.
     * Note that this isn't static since it still depends on the state/capabilities of the outer class instance.
     */
    private class PreLoadedDeserializer implements IDeserializer {
        @Override
        public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
            // All the objects we are creating to deserialize in the callee space have ReentrantCallerReferenceToken as the persistenceToken.
            RuntimeAssertionError.assertTrue(persistenceToken instanceof ReentrantCallerReferenceToken);
            
            // See if we are the active loader.
            if (ReentrantGraphProcessor.this.isActiveInstanceLoader) {
                // We are on top so we can now bill them for this load (we can check the billing instance map which we populated during the load).
                int instanceBytes = ReentrantGraphProcessor.this.objectSizesLoadedForCallee.remove(instance);
                ReentrantGraphProcessor.this.feeProcessor.readOneInstanceFromHeap(instanceBytes);
                
                // This also needs to be moved to the root set to scan for re-save (the issue-249 case).
                // It is no longer required in the billing map.
                ReentrantGraphProcessor.this.loadedObjectInstances.add(instance);
            } else {
                // We still aren't active but we already did the load so just re-install the deserializer.
                // NOTE:  This assumes that the derializer is cleared _before_ calling this, not after.
                try {
                    ReentrantGraphProcessor.this.deserializerField.set(instance, this);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Failures here would have happened during startup.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        }
    }
}
