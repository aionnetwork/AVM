package org.aion.avm.core.rejection;


public class RejectFinalizeResource {
    @Override
    // finalize() is deprecated since JDK9, and we know that, but this suppresses the warning in our build.
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        // This should fail to load, just because we have this here.
    }
}
