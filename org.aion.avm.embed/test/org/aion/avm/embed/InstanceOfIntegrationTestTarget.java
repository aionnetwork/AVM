package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

/**
 * The test class loaded by InstanceOfIntegrationTest.
 * The point of this test is to check the behaviour of the "instanceof" operator, before and after transformation, to make sure it is the same.
 */
public class InstanceOfIntegrationTestTarget {

    @Callable
    public static boolean checkSelf() {
        InstanceOfIntegrationTestTarget target = new InstanceOfIntegrationTestTarget();
        return (target instanceof InstanceOfIntegrationTestTarget);
    }

    @Callable
    public static boolean checkNull() {
        return (null instanceof Object);
    }

    @Callable
    public static boolean checkSubTrue() {
        Sub sub = new Sub();
        return (sub instanceof InstanceOfIntegrationTestTarget);
    }

    @Callable
    public static boolean checkSubFalse() {
        InstanceOfIntegrationTestTarget target = new InstanceOfIntegrationTestTarget();
        return (target instanceof Sub);
    }

    @Callable
    public static boolean checkSubObject() {
        Sub sub = new Sub();
        return (sub instanceof Object);
    }

    @Callable
    public static boolean checkSubA() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceA);
    }

    @Callable
    public static boolean checkSubB() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceB);
    }

    @Callable
    public static boolean checkSubC() {
        Sub sub = new Sub();
        return (sub instanceof InterfaceC);
    }

    @Callable
    public static boolean checkBOfA() {
        ClassB b = new ClassB();
        return (b instanceof InterfaceA);
    }

    @Callable
    public static boolean checkAddHocAObject() {
        InterfaceA a = new InterfaceA() {};
        return (a instanceof Object);
    }

    @Callable
    public static boolean subArrayIsObject() {
        Sub[] sub = new Sub[0];
        return (sub instanceof Object);
    }

    @Callable
    public static boolean subArrayIsObjectArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof Object[]);
    }

    @Callable
    public static boolean targetIsTargetArray() {
        InstanceOfIntegrationTestTarget[] target = new InstanceOfIntegrationTestTarget[0];
        return (target instanceof InstanceOfIntegrationTestTarget[]);
    }

    @Callable
    public static boolean subArrayIsTargetArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InstanceOfIntegrationTestTarget[]);
    }

    @Callable
    public static boolean subArrayIsCArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InterfaceC[]);
    }

    @Callable
    public static boolean subArrayIsAArray() {
        Sub[] sub = new Sub[0];
        return (sub instanceof InterfaceA[]);
    }

    @Callable
    public static boolean bArrayIsAArray() {
        ClassB[] b = new ClassB[0];
        return (b instanceof InterfaceA[]);
    }

    @Callable
    public static boolean bArrayIsBArray() {
        ClassB[] b = new ClassB[0];
        return (b instanceof InterfaceB[]);
    }

    @Callable
    public static boolean intCArrayIsBArray() {
        InterfaceC[] c = new InterfaceC[0];
        return (c instanceof InterfaceB[]);
    }

    // Ensures method overloading compiles and works fine
    @Callable
    public static boolean call_getSub() {
        Sub[] sub = new Sub[]{new Sub()};
        InterfaceA inner = getSub(sub);
        return (inner instanceof Sub);
    }

    // Ensures method overloading compiles and works fine
    @Callable
    public static boolean call_getSub2() {
        Sub[] sub = new Sub[]{new Sub(), new Sub()};
        InterfaceA inner = getSub(sub);
        return (inner instanceof Sub);
    }

    // Ensures multiple array params compiles and works fine
    @Callable
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
