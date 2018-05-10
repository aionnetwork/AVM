package org.aion.avm.core.instrument;


/**
 * Note that this class is just used as a resource by the other tests in this package.
 */
public class TestResource {
    private final int fixedHashCode;

    public TestResource(int fixedHashCode) {
        this.fixedHashCode = fixedHashCode;
    }

    @Override
    public int hashCode() {
        // We will just return the fixed value, here, but change it in some tests, via ASM.
        return this.fixedHashCode;
    }
}
