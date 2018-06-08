package org.aion.avm.core.rejection;


public class RejectFinalizeResource {
    @Override
    protected void finalize() throws Throwable {
        // This should fail to load, just because we have this here.
    }
}
