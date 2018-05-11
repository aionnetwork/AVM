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

    public String[] buildStringArray(int length) {
        return new String[length];
    }

    public String[][][] buildMultiStringArray3(int d1, int d2, int d3) {
        return new String[d1][d2][d3];
    }

    public Object buildLongArray2(int d1, int d2) {
        return new long[d1][d2];
    }
}
