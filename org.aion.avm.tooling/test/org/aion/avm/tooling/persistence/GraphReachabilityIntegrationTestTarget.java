package org.aion.avm.tooling.persistence;

import java.math.BigInteger;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * The test class loaded by GraphReachabilityIntegrationTest.
 */
public class GraphReachabilityIntegrationTestTarget {
    private static GraphReachabilityIntegrationTestTarget rootLeft;
    private static GraphReachabilityIntegrationTestTarget rootRight;
    
    private int value;
    private GraphReachabilityIntegrationTestTarget next;
    
    public GraphReachabilityIntegrationTestTarget(int value) {
        this.value = value;
    }

    /**
     * Create the graph which would otherwise demonstrate problems:
     * R -> (A, B)
     * A -> (C, 0)
     * B -> (D, 1)
     * C -> (E, 2)
     * D -> (E, 3)
     * E -> (null, 4)
     * (E is reachable through 2 totally distinct paths)
     */
    @Callable
    public static void setup249() {
        GraphReachabilityIntegrationTestTarget a = new GraphReachabilityIntegrationTestTarget(0);
        GraphReachabilityIntegrationTestTarget b = new GraphReachabilityIntegrationTestTarget(1);
        GraphReachabilityIntegrationTestTarget c = new GraphReachabilityIntegrationTestTarget(2);
        GraphReachabilityIntegrationTestTarget d = new GraphReachabilityIntegrationTestTarget(3);
        GraphReachabilityIntegrationTestTarget e = new GraphReachabilityIntegrationTestTarget(4);
        rootLeft = a;
        rootRight = b;
        a.next = c;
        b.next = d;
        c.next = e;
        d.next = e;
    }
    
    /**
     * Check that the value is what is provided, in both directions (unless the left is broken, since that is part of the test).
     */
    @Callable
    public static void check249(int value) {
        // Check the value.
        int left = rootRight.next.next.value;
        assert value == left;
        
        if (null != rootLeft.next.next) {
            // Check the instances match.
            assert rootRight.next.next == rootLeft.next.next;
        }
    }
    
    /**
     * Modify the graph, on the left, via a reentrant call, then verify the modification, on the right.
     */
    @Callable
    public static void run249_reentrant_notLoaded() {
        reentrantCallModify();
        
        // Now verify on the right.
        int changed = rootRight.next.next.value;
        assert 5 == changed;
    }
    
    /**
     * Check the state of the graph, on the right, modify the graph, on the left, via a reentrant call,
     * then verify the modification, on the right.
     */
    @Callable
    public static void run249_reentrant_loaded() {
        int initial = rootRight.next.next.value;
        assert 4 == initial;
        
        reentrantCallModify();
        
        // Now verify on the right.
        int changed = rootRight.next.next.value;
        assert 5 == changed;
    }
    
    /**
     * Make the reentrant call to create the new instance.
     */
    @Callable
    public static void runNewInstance_reentrant() {
        // Make the call to change it.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("modifyNewInstance");
        long energyLimit = 500000;
        Result result = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
        assert result.isSuccess();
    }
    
    /**
     * Make the reentrant call to runNewInstance_reentrant.
     */
    @Callable
    public static void runNewInstance_reentrant2() {
        // Make the call to change it.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("runNewInstance_reentrant");
        long energyLimit = 500000;
        Result result = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
        assert result.isSuccess();
    }
    
    /**
     * Checks the new instance, on the right, and returns its value.
     */
    @Callable
    public static int checkNewInstance() {
        return rootRight.next.next.next.value;
    }
    
    /**
     * Modify the E value to 5, on the left.
     */
    @Callable
    public static void modify249() {
        // Make sure it is its initial value.
        int initial = rootLeft.next.next.value;
        assert 4 == initial;
        
        // Change it.
        rootLeft.next.next.value = 5;
        
        // Hide the change by breaking the connection (the change should still be visible by other paths, but a simple reachability won't find it).
        rootLeft.next.next = null;
    }
    
    /**
     * Add a link with the value 5 to E, on the left, then sever that connection.
     */
    @Callable
    public static void modifyNewInstance() {
        // Make sure it is its initial value.
        rootLeft.next.next.next = new GraphReachabilityIntegrationTestTarget(5);
        rootLeft.next = null;
    }
    
    private static void reentrantCallModify() {
        // Make the call to change it.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("modify249");
        long energyLimit = 500000;
        Result result = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
        assert result.isSuccess();
    }
}
