package examples;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;


/**
 * A simple example for the Beta release.
 * Exposes 2 methods:
 * 1) put(String, String)
 * 2) get(String)
 * 
 * These methods operate on the state of a shared static map, producing debug log and event log output based on these actions.
 */
public class BetaMapEvents {
    /**
     * AVM doesn't expose all the java.util containers, provides its own set:
     * -AionMap
     * -AionList
     * -AionSet
     * Since this map is a static, it will be part of the graph saved to persistent storage between transactions.
     */
    private static final AionMap<String, String> map;

    /**
     * AVM only runs the static initializer (<clinit> method) on deployment.
     * Additional data passed to the deployment can be accessed here, too.
     */
    static {
        map = new AionMap<>();
        BlockchainRuntime.println("Deployed BetaMapEvents");
    }

    /**
     * This static main() MUST be provided in the main class of a DApp.  This is where every non-deploy transaction begins.
     * 
     * @return The data to return (only relevant if this called by another DApp - this will be returned to the caller).
     */
    public static byte[] main() {
        // The ABI is technically optional.  Any interpretation of the incoming data is permitted but the ABI is what we use, internally.
        byte[] inputData = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithClass(BetaMapEvents.class, inputData);
    }

    public static void put(String key, String value) {
        String oldValue = map.put(key, value);
        BlockchainRuntime.println("PUT(\"" + key + "\", \"" + value + "\") -> " + oldValue);
        BlockchainRuntime.log("PUT".getBytes(), key.getBytes(), value.getBytes());
        if (null != oldValue) {
            BlockchainRuntime.log("REPLACE".getBytes(), key.getBytes(), oldValue.getBytes());
        }
    }

    public static void get(String key) {
        String value = map.get(key);
        BlockchainRuntime.println("GET(\"" + key + "\") -> " + value);
        String reportingValue = (null != value)
                ? value
                : "null";
        BlockchainRuntime.log("GET".getBytes(), key.getBytes(), reportingValue.getBytes());
    }
}
