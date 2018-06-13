package org.aion.avm.core.instrument;


/**
 * This interface is used by the ClassMeteringTest to verify that empty methods don't break metering.
 */
public interface TestInterface {
    public void one();
    public int two(String arg);
}
