package org.aion.avm.rt;


/**
 * The address has a very specific meaning, within the environment, so we wrap a byte[] to produce this more specific type.
 * Note that we currently can't wrap a "ByteArray", which otherwise seems like the obvious approach, since that descends from our
 * shadow Object, meaning it can only be allocated from within the child context.
 * 
 * This is likely to change a lot as we build more DApp tests (see issue-76 for more details on how we might want to evolve this).
 * There is a good chance that we will convert this into an interface so that our implementation can provide a richer interface to
 * our AVM code than we want to support for the contract.
 */
public class Address {

    public static final int LENGTH = 32;

    private final byte[] underlying;

    /**
     * Note that this constructor is only here to support our tests while we decide whether or not to expose the constructor
     * of construct the class this way.
     * 
     * @param raw The raw bytes representing the address.
     */
    public Address(byte[] raw) {
        if (raw == null || raw.length != LENGTH) {
            throw new IllegalArgumentException();
        }

        this.underlying = raw;
    }

    /**
     * Similarly, this method will probably be removed or otherwise hidden.
     * 
     * @return The raw bytes underneath the address.
     */
    public byte[] unwrap() {
        return this.underlying;
    }
}
