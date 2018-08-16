package org.aion.avm.core.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;


public class StackThisTrackerTest {
    private static final int LOCAL_COUNT = 0;

    @Test
    public void testEmptyStack() throws Exception {
        @SuppressWarnings("unchecked")
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = new Frame[] {
                new Frame<ConstructorThisInterpreter.ThisValue>(LOCAL_COUNT, 0)
        };
        StackThisTracker tracker = new StackThisTracker(frames);
        Assert.assertFalse(tracker.isThisTargetOfGet(0));
        Assert.assertFalse(tracker.isThisTargetOfPut(0));
        Assert.assertEquals(frames.length, tracker.getFrameCount());
    }

    @Test
    public void testPartialStack() throws Exception {
        Frame<ConstructorThisInterpreter.ThisValue> frame = new Frame<>(LOCAL_COUNT, 1);
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        @SuppressWarnings("unchecked")
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = new Frame[] {
                frame
        };
        StackThisTracker tracker = new StackThisTracker(frames);
        Assert.assertFalse(tracker.isThisTargetOfGet(0));
        Assert.assertFalse(tracker.isThisTargetOfPut(0));
    }
    @Test
    public void testGet() throws Exception {
        Frame<ConstructorThisInterpreter.ThisValue> frame = new Frame<>(LOCAL_COUNT, 4);
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createThis(BasicValue.REFERENCE_VALUE));
        @SuppressWarnings("unchecked")
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = new Frame[] {
                frame
        };
        StackThisTracker tracker = new StackThisTracker(frames);
        Assert.assertTrue(tracker.isThisTargetOfGet(0));
        Assert.assertFalse(tracker.isThisTargetOfPut(0));
    }
    @Test
    public void testPut() throws Exception {
        Frame<ConstructorThisInterpreter.ThisValue> frame = new Frame<>(LOCAL_COUNT, 4);
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createThis(BasicValue.REFERENCE_VALUE));
        frame.push(ConstructorThisInterpreter.ThisValue.createNotThis(BasicValue.REFERENCE_VALUE));
        @SuppressWarnings("unchecked")
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = new Frame[] {
                frame
        };
        StackThisTracker tracker = new StackThisTracker(frames);
        Assert.assertFalse(tracker.isThisTargetOfGet(0));
        Assert.assertTrue(tracker.isThisTargetOfPut(0));
    }
}
