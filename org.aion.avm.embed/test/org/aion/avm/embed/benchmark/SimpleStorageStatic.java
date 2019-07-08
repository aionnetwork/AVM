package org.aion.avm.embed.benchmark;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;


/**
 * Tests flat reading and writing of storage (not particularly big or deep).
 */
public class SimpleStorageStatic {
    private static final int myInt = 703;
    private static final String myString = "Benchmark Testing";
    private static final int[] myInt1DArray = new int[]{0,1,2,3,4,5};
    private static final AionMap<Integer, String> myMap = new AionMap<>();

    static {
        myMap.put(0, "zero");
        myMap.put(1, "one");
        myMap.put(2, "two");
        myMap.put(3, "three");
        myMap.put(4, "four");
    }

    @Callable
    public static void putStorage(String key, String value){
        Blockchain.putStorage(convertToFittingKey(key), value.getBytes());
    }

    @Callable
    public static String getStorage(String key){
        byte[] payload = Blockchain.getStorage(convertToFittingKey(key));
        return (null != payload)
                ? new String(payload)
                : null;
    }

    @Callable
    public static int getMyInt() {
        return myInt;
    }

    @Callable
    public static String getMyString () {
        return myString;
    }

    @Callable
    public static int[] getMyInt1DArray(){
        return myInt1DArray;
    }

    @Callable
    public static void putMap(int key, String value){
        myMap.put(key, value);
    }

    @Callable
    public static String getMap(int key) {
        return myMap.get(key);
    }


    private static byte[] convertToFittingKey(String string) {
        // The key needs to be 32-bytes so either truncate or 0x0-pad the bytes from the string.
        byte[] key = new byte[32];
        byte[] raw = string.getBytes();
        int length = StrictMath.min(key.length, raw.length);
        System.arraycopy(raw, 0, key, 0, length);
        return key;
    }
}
