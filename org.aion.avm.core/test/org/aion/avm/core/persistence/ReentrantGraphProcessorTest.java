package org.aion.avm.core.persistence;

import java.util.Collections;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.NullFeeProcessor;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.OutOfEnergyException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Based on the ReflectionStructureCodecTest, this accomplishes similar verifications but based on memory-memory serialization/deserialization
 * rather than back-ending onto a serialized binary encoding.
 */
public class ReentrantGraphProcessorTest {
    // We don't verify fees at this time so just use the "null" utility processor.
    private static NullFeeProcessor FEE_PROCESSOR = new NullFeeProcessor();

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    /**
     * Change a class and verify that the changes stay on commit.
     */
    @Test
    public void commitClassChange() {
        ReflectionStructureCodecTarget originalInstance = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = originalInstance;
        ReentrantGraphProcessor codec = new ReentrantGraphProcessor(new ReflectedFieldCache(), FEE_PROCESSOR, Collections.singletonList(ReflectionStructureCodecTarget.class));
        
        // Capture the state of the static.
        codec.captureAndReplaceStaticState();
        
        // Modify the one we have.
        ReflectionStructureCodecTarget.s_five = 42;
        ReflectionStructureCodecTarget.s_eight = 42.0d;
        ReflectionStructureCodecTarget changedInstance = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget.s_nine = changedInstance;
        
        // Commit those changes.
        codec.commitGraphToStoredFieldsAndRestore();
        
        // Verify that these are the changes we see.
        Assert.assertEquals(42, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(42.0d, ReflectionStructureCodecTarget.s_eight, 0.1);
        Assert.assertTrue(changedInstance == ReflectionStructureCodecTarget.s_nine);
    }

    /**
     * Change a class and verify that the changes revert on revert.
     */
    @Test
    public void revertClassChange() {
        ReflectionStructureCodecTarget originalInstance = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = originalInstance;
        ReentrantGraphProcessor codec = new ReentrantGraphProcessor(new ReflectedFieldCache(), FEE_PROCESSOR, Collections.singletonList(ReflectionStructureCodecTarget.class));
        
        // Capture the state of the static.
        codec.captureAndReplaceStaticState();
        
        // Modify the one we have.
        ReflectionStructureCodecTarget.s_five = 42;
        ReflectionStructureCodecTarget.s_eight = 42.0d;
        ReflectionStructureCodecTarget changedInstance = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget.s_nine = changedInstance;
        
        // Revert those changes.
        codec.revertToStoredFields();
        
        // Verify that we see the original data.
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_eight, 0.1);
        Assert.assertTrue(originalInstance == ReflectionStructureCodecTarget.s_nine);
    }

    /**
     * Verify that a commit reflects changes in an instance reachable from the static:
     * -populate an instance stored in the static
     * -verify that capturing the state replaces it with a new instance (blank stub)
     * -verify that calling lazyLoad() on this new instance populates it
     * -change some of that data
     * -commit the change
     * -verify that the instance is back to the original instance but that the new change is present
     */
    @Test
    public void commitInstanceChange() {
        ReflectionStructureCodecTarget originalRoot = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget originalLeaf = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget target = originalRoot;
        target.i_one = true;
        target.i_two = 5;
        target.i_three = 5;
        target.i_four = 5;
        target.i_five = 5;
        target.i_six = 5.0f;
        target.i_seven = 5;
        target.i_eight = 5.0d;
        target.i_nine = originalLeaf;
        ReflectionStructureCodecTarget.s_nine = originalRoot;
        
        ReentrantGraphProcessor codec = new ReentrantGraphProcessor(new ReflectedFieldCache(), FEE_PROCESSOR, Collections.singletonList(ReflectionStructureCodecTarget.class));
        
        // Capture the state of the static.
        codec.captureAndReplaceStaticState();
        
        // Verify the change and stub appearance.
        Assert.assertFalse(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertEquals(0, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(null, ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Call lazyLoad() and verify that the instance is populated.
        ReflectionStructureCodecTarget.s_nine.lazyLoad();
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertTrue(null != ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertTrue(originalLeaf != ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Change data in this now-loaded instance and commit the change.
        ReflectionStructureCodecTarget.s_nine.i_five = 42;
        ReflectionStructureCodecTarget.s_nine.i_eight = 42.0d;
        codec.commitGraphToStoredFieldsAndRestore();
        
        // Verify that the instances are back and the changed data has been flushed to them.
        Assert.assertTrue(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertTrue(originalLeaf == ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertEquals(42, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(42.0d, ReflectionStructureCodecTarget.s_nine.i_eight, 0.1);
    }

    /**
     * Verify that a revert reverts to an unchanged instance graph, even if an instance was modified:
     * -populate an instance stored in the static
     * -verify that capturing the state replaces it with a new instance (blank stub)
     * -verify that calling lazyLoad() on this new instance populates it
     * -change some of that data
     * -revert the change
     * -verify that the instance is back to the original instance with none of the changes reflected
     */
    @Test
    public void revertInstanceChange() {
        ReflectionStructureCodecTarget originalRoot = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget originalLeaf = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget target = originalRoot;
        target.i_one = true;
        target.i_two = 5;
        target.i_three = 5;
        target.i_four = 5;
        target.i_five = 5;
        target.i_six = 5.0f;
        target.i_seven = 5;
        target.i_eight = 5.0d;
        target.i_nine = originalLeaf;
        ReflectionStructureCodecTarget.s_nine = originalRoot;
        
        ReentrantGraphProcessor codec = new ReentrantGraphProcessor(new ReflectedFieldCache(), FEE_PROCESSOR, Collections.singletonList(ReflectionStructureCodecTarget.class));
        
        // Capture the state of the static.
        codec.captureAndReplaceStaticState();
        
        // Verify the change and stub appearance.
        Assert.assertFalse(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertEquals(0, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(null, ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Call lazyLoad() and verify that the instance is populated.
        ReflectionStructureCodecTarget.s_nine.lazyLoad();
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertTrue(null != ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertTrue(originalLeaf != ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Change data in this now-loaded instance and revert the change.
        ReflectionStructureCodecTarget.s_nine.i_five = 42;
        ReflectionStructureCodecTarget.s_nine.i_eight = 42.0d;
        codec.revertToStoredFields();
        
        // Verify that the instances are back and the changed data has been flushed to them.
        Assert.assertTrue(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertTrue(originalLeaf == ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_nine.i_eight, 0.1);
    }

    /**
     * A run that attempts to commit but runs out of energy right before the last instance is measured, causing a rollback.
     * Verify that none of the instance shape is changed.
     */
    @Test
    public void commitFailureDueToCost() {
        ReflectionStructureCodecTarget originalRoot = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget originalLeaf = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget target = originalRoot;
        target.i_one = true;
        target.i_two = 5;
        target.i_three = 5;
        target.i_four = 5;
        target.i_five = 5;
        target.i_six = 5.0f;
        target.i_seven = 5;
        target.i_eight = 5.0d;
        target.i_nine = originalLeaf;
        ReflectionStructureCodecTarget.s_nine = originalRoot;
        
        ReentrantGraphProcessor codec = new ReentrantGraphProcessor(new ReflectedFieldCache(), new FailOnInstanceWrite(), Collections.singletonList(ReflectionStructureCodecTarget.class));
        
        // Capture the state of the static.
        codec.captureAndReplaceStaticState();
        
        // Verify the change and stub appearance.
        Assert.assertFalse(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertEquals(0, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(null, ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Call lazyLoad() and verify that the instance is populated.
        ReflectionStructureCodecTarget.s_nine.lazyLoad();
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertTrue(null != ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertTrue(originalLeaf != ReflectionStructureCodecTarget.s_nine.i_nine);
        
        // Change data in this now-loaded instance and revert the change.
        ReflectionStructureCodecTarget.s_nine.i_five = 42;
        ReflectionStructureCodecTarget.s_nine.i_eight = 42.0d;
        boolean didCatch = false;
        try {
            codec.commitGraphToStoredFieldsAndRestore();
        } catch (OutOfEnergyException e) {
            // This is expected.
            didCatch = true;
            // We are expected to commute this into a revert.
            codec.revertToStoredFields();
        }
        Assert.assertTrue(didCatch);
        
        // Verify that the instances are back and the changed data has been flushed to them.
        Assert.assertTrue(originalRoot == ReflectionStructureCodecTarget.s_nine);
        Assert.assertTrue(originalLeaf == ReflectionStructureCodecTarget.s_nine.i_nine);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_nine.i_five);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_nine.i_eight, 0.1);
    }


    private static class FailOnInstanceWrite implements IStorageFeeProcessor {
        @Override
        public void readStaticDataFromStorage(int byteSize) {
        }
        @Override
        public void writeStaticDataToStorage(int byteSize) {
        }
        @Override
        public void readOneInstanceFromStorage(int byteSize) {
        }
        @Override
        public void writeOneInstanceToStorage(int byteSize) {
        }
        @Override
        public void readStaticDataFromHeap(int byteSize) {
        }
        @Override
        public void writeStaticDataToHeap(int byteSize) {
        }
        @Override
        public void readOneInstanceFromHeap(int byteSize) {
        }
        @Override
        public void writeOneInstanceToHeap(int byteSize) {
            // This is the place where we want to trigger a failure (may generalize this in the future).
            throw new OutOfEnergyException();
        }
    }
}
