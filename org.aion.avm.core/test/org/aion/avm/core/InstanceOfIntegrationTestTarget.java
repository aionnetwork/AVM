package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by InstanceOfIntegrationTest.
 * The point of this test is to check the behaviour of the "instanceof" operator, before and after transformation, to make sure it is the same.
 */
public class InstanceOfIntegrationTestTarget {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(InstanceOfIntegrationTestTarget.class, BlockchainRuntime.getData());
    }
    
    public static boolean checkSelf() {
        InstanceOfIntegrationTestTarget target = new InstanceOfIntegrationTestTarget();
        return (target instanceof InstanceOfIntegrationTestTarget);
    }
    
    public static boolean checkNull() {
        return (null instanceof Object);
    }
    
    public static boolean checkSubTrue() {
        Sub sub = new Sub();
        return (sub instanceof InstanceOfIntegrationTestTarget);
    }
    
    public static boolean checkSubFalse() {
        InstanceOfIntegrationTestTarget target = new InstanceOfIntegrationTestTarget();
        return (target instanceof Sub);
    }
    
    public static boolean checkSubObject() {
        Sub sub = new Sub();
        return (sub instanceof Object);
    }
    
    public static boolean checkSubA() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceA);
    }
    
    public static boolean checkSubB() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceB);
    }
    
    public static boolean checkSubC() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceC);
    }
    
    public static boolean checkBOfA() {
        ClassB b = new ClassB();
        return (b instanceof InterfaceA);
    }
    
    public static boolean checkAddHocAObject() {
        InterfaceA a = new InterfaceA() {};
        return (a instanceof Object);
    }
    
    public static boolean subArrayIsObject() {
        Sub[] sub = new Sub[0];
        return (sub instanceof Object);
    }
    
    public static boolean subArrayIsObjectArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof Object[]);
    }
    
    public static boolean subArrayIsTargetArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InstanceOfIntegrationTestTarget[]);
    }
    
    public static boolean subArrayIsCArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InterfaceC[]);
    }
    
    public static boolean subArrayIsAArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InterfaceA[]);
    }
    
    public static boolean bArrayIsAArray() {
        ClassB[] b = new ClassB[0];
        return (b instanceof InterfaceA[]);
    }
    
    public static boolean bArrayIsBArray() {
        ClassB[] b = new ClassB[0];
        return (b instanceof InterfaceB[]);
    }
    
    public static boolean intCArrayIsBArray() {
        InterfaceC[] c = new InterfaceC[0];
        return (c instanceof InterfaceB[]);
    }
    
    
    private static class Sub extends InstanceOfIntegrationTestTarget implements InterfaceC {
    }
    
    private static class ClassB implements InterfaceB {
    }
    
    private interface InterfaceA {
    }
    
    private interface InterfaceB {
    }
    
    private interface InterfaceC extends InterfaceA, InterfaceB {
    }
}
