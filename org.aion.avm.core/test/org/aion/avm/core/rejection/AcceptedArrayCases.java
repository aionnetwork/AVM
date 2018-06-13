package org.aion.avm.core.rejection;


public class AcceptedArrayCases {
    // This should be fine since this object type is in the white-list, so that extends to any arrays of it.
    public String[] acceptedStrings;
    
    // This should be fine since arrays of primitives are also always acceptable.
    public byte[] acceptedPrimitives;
    
    // "clone()" is special in that it is actually owned by the array type, not strictly inherited from Object.
    public static String[] stringClone(String[] input) {
        return input.clone();
    }
    
    // "clone()" is special in that it is actually owned by the array type, not strictly inherited from Object.
    public static byte[] primitiveClone(byte[] input) {
        return input.clone();
    }
}
