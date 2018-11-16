package org.aion.avm.internal;


/**
 * Helpers related to how we attach or otherwise interact with IInstrumentation.
 */
public class InstrumentationHelpers {
    public static void attachThread(IInstrumentation instrumentation) {
        RuntimeAssertionError.assertTrue(null == IInstrumentation.attachedThreadInstrumentation.get());
        IInstrumentation.attachedThreadInstrumentation.set(instrumentation);
    }
    public static void detachThread(IInstrumentation instrumentation) {
        RuntimeAssertionError.assertTrue(instrumentation == IInstrumentation.attachedThreadInstrumentation.get());
        IInstrumentation.attachedThreadInstrumentation.remove();
    }
}
