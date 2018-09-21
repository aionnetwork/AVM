package org.aion.avm.core.testWallet;

import org.junit.Ignore;
import org.junit.Test;


/**
 * JUnit wrapper over the high-level entry-points in the Deployer class (so we know if we break it).
 */
public class DeployerTest {
    @Test
    public void runDirect() throws Throwable {
        Deployer.callableInvokeDirect();
    }

    @Test
    public void runTransformed() throws Throwable {
        Deployer.callableInvokeTransformed();
    }
}
