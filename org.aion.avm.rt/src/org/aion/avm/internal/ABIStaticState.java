package org.aion.avm.internal;

/**
 * Contains the IABISupport instance shared by all the static invocation paths and provides mechanisms for tests and startup to manage it.
 * Note that the reason why this is needed is that the ABI is invoked from DApp code, as a static, so we either need per-DApp class copies
 * (which would be overkill) or some kind of static singleton (and the external support interface seemed the obvious answer).
 *
 */
public final class ABIStaticState {
    private static IABISupport SUPPORT_SINGLETON;

    /**
     * MUST be called to initialize the encoder's ability to create higher-dimension arrays before it is used.
     * This must only be called once.
     * Ideally, this is called by the same mechanism which is forcing the initialization of the shadow class library.
     * @param arrayFactory The factory to install (must NOT be null).
     */
    public static void initializeSupport(IABISupport arrayFactory) {
        // Verify that we were only called once and that we weren't given null.
        RuntimeAssertionError.assertTrue(null == SUPPORT_SINGLETON);
        RuntimeAssertionError.assertTrue(null != arrayFactory);
        SUPPORT_SINGLETON = arrayFactory;
    }

    /**
     * Exposed for certain testing cases where we actually do need to re-initialize this for some explicit case.
     */
    public static IABISupport testingSecondaryInitialization(IABISupport newFactory) {
        RuntimeAssertionError.assertTrue(null != newFactory);
        IABISupport previous = SUPPORT_SINGLETON;
        SUPPORT_SINGLETON = newFactory;
        return previous;
    }

    public static IABISupport getSupport() {
        // Nobody can use this if it hasn't yet been initialized.
        RuntimeAssertionError.assertTrue(null != SUPPORT_SINGLETON);
        return SUPPORT_SINGLETON;
    }
}
