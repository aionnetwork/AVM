package org.aion.avm.core.testWallet;

import org.aion.avm.core.NodeEnvironment;
import org.junit.Assert;
import org.junit.Test;


/**
 * JUnit wrapper over the high-level entry-points in the Deployer class (so we know if we break it).
 */
public class DeployerTest {
    @Test
    public void runDirect() throws Throwable {
        Assert.assertNotNull(NodeEnvironment.singleton);
        Deployer.callableInvokeDirect();
    }

    @Test
    public void runTransformed() throws Throwable {
        Assert.assertNotNull(NodeEnvironment.singleton);
        Deployer.callableInvokeTransformed();
    }
}
