package org.aion.avm.userlib;


/**
 * Used to test the behaviour of hash collisions in some of out tests.
 * The specified hash is used for the hashCode() while both the hash and the value are used for equality.
 * Note that the toString() also returns a deterministic answer, based only on hash and value.
 */
public class TestElement {
    public final int hash;
    public final int value;

    public TestElement(int hash, int value) {
        this.hash = hash;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (this == obj);
        if (!isEqual && (obj instanceof TestElement)) {
            TestElement other = (TestElement)obj;
            isEqual = (this.hash == other.hash)
                    && (this.value == other.value)
                    ;
        }
        return isEqual;
    }

    @Override
    public String toString() {
        return "TestElement(" + this.hash + ", " + this.value + ")";
    }
}
