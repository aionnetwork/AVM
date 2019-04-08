package org.aion.avm.shadowapi.avm;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * The address has a very specific meaning, within the environment, so we wrap a ByteArray to produce this more specific type.
 * 
 * This is likely to change a lot as we build more DApp tests (see issue-76 for more details on how we might want to evolve this).
 * There is a good chance that we will convert this into an interface so that our implementation can provide a richer interface to
 * our AVM code than we want to support for the contract.
 */
public class Address extends Object {
    // Runtime-facing implementation.
    public static final int avm_LENGTH = 32;

    private ByteArray underlying;

    /**
     * The constructor which user code can call, directly, to create an Address object.
     * This will remain until/unless we decide to make a factory which creates these from within the runtime.
     * 
     * @param raw The raw bytes representing the address.
     */
    public Address(ByteArray raw) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Address_avm_constructor);
        if (raw == null || raw.length() != avm_LENGTH) {
            throw new IllegalArgumentException();
        }

        this.underlying = raw;
    }

    /**
     * Similarly, this method will probably be removed or otherwise hidden.
     * 
     * @return The raw bytes underneath the address.
     */
    public ByteArray avm_unwrap() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Address_avm_unwrap);
        lazyLoad();
        return this.underlying;
    }

    @Override
    public int avm_hashCode() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Address_avm_hashCode);
        lazyLoad();
        // Just a really basic implementation.
        int code = 0;
        for (byte elt : this.underlying.getUnderlying()) {
            code += (int)elt;
        }
        return code;
    }

    @Override
    public boolean avm_equals(IObject obj) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Address_avm_equals);

        boolean isEqual = this == obj;
        if (!isEqual && (obj instanceof Address)) {
            Address other = (Address)obj;
            lazyLoad();
            other.lazyLoad();
            if (this.underlying.length() == other.underlying.length()) {
                isEqual = true;
                byte[] us = this.underlying.getUnderlying();
                byte[] them = other.underlying.getUnderlying();
                for (int i = 0; isEqual && (i < us.length); ++i) {
                    isEqual = (us[i] == them[i]);
                }
            }
        }
        return isEqual;
    }

    @Override
    public String avm_toString() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Address_avm_toString);
        lazyLoad();
        return toHexString(this.underlying.getUnderlying());
    }

    private static String toHexString(byte[] bytes) {
        int length = bytes.length;

        char[] hexChars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(new CharArray(hexChars));
    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    // Compiler-facing implementation.
    public static final int LENGTH = avm_LENGTH;

    /**
     * Note that this constructor is only here to support our tests while we decide whether or not to expose the constructor
     * of construct the class this way.
     * 
     * @param raw The raw bytes representing the address.
     */
    public Address(byte[] raw) {
        this(new ByteArray(raw));
    }

    /**
     * Similarly, this method will probably be removed or otherwise hidden.
     * 
     * @return The raw bytes underneath the address.
     */
    public byte[] unwrap() {
        lazyLoad();
        return this.underlying.getUnderlying();
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        boolean isEqual = this == obj;
        if (!isEqual && (obj instanceof Address)) {
            Address other = (Address) obj;
            lazyLoad();
            other.lazyLoad();
            if (this.underlying.length() == other.underlying.length()) {
                isEqual = true;
                for (int i = 0; isEqual && (i < other.underlying.length()); ++i) {
                    isEqual = (this.underlying.get(i) == other.underlying.get(i));
                }
            }
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int code = 0;
        lazyLoad();
        for (byte elt : this.underlying.getUnderlying()) {
            code += (int)elt;
        }
        return code;
    }

    // Support for deserialization
    public Address(Void ignore, int readIndex) {
        super(ignore, readIndex);
    }
}
