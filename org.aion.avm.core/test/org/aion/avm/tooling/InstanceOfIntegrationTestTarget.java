package org.aion.avm.tooling;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectArray;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.avm.shadow.java.lang.String;


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
    
    public static boolean targetIsTargetArray() {
        InstanceOfIntegrationTestTarget[] target = new InstanceOfIntegrationTestTarget[0];
        return (target instanceof InstanceOfIntegrationTestTarget[]);
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

    // Ensures method overloading compiles and works fine
    public static boolean call_getSub() {
        Sub[] sub = new Sub[]{new Sub()};
        InterfaceA inner = getSub(sub);
        return (inner instanceof Sub);
    }

    // Ensures method overloading compiles and works fine
    public static boolean call_getSub2() {
        Sub[] sub = new Sub[]{new Sub(), new Sub()};
        InterfaceA inner = getSub(sub);
        return (inner instanceof Sub);
    }

    // Ensures multiple array params compiles and works fine
    public static boolean checkArrayParams() {
        Sub[] array1 = new Sub[]{new Sub()};
        ClassC[] array2 = new ClassC[]{new ClassC()};
        return compareArrays(array1, array2);
    }

    private static InterfaceA getSub(InterfaceC[] array) {
        return array[0];
    }

    private static InterfaceA getSub(InterfaceD[] array) {
        return array[1];
    }

    private static boolean compareArrays(InterfaceC[] array1, InterfaceD[] array2) {
        return (array1[0] instanceof InstanceOfIntegrationTestTarget)
            && (array2[0] instanceof InstanceOfIntegrationTestTarget)
            && (array2[0] instanceof InterfaceC);
    }

    private static class Sub extends InstanceOfIntegrationTestTarget implements InterfaceC {
    }

    private static class ClassB implements InterfaceB {
    }

    private static class ClassC implements InterfaceD {
    }

    private interface InterfaceA {
    }

    private interface InterfaceB {
    }

    private interface InterfaceC extends InterfaceA, InterfaceB, InterfaceD {
    }

    private interface InterfaceD extends InterfaceA, InterfaceB {
    }
}
