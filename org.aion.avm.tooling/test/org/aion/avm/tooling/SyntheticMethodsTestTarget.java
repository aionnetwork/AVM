package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;

import java.math.BigInteger;

public class SyntheticMethodsTestTarget{

    private static int COMPARISION_RESULT = 100;
    public static final int DEFAULT_VALUE = 100;
    private static TargetInterface currentTargetClass;

    private static Gen<Integer> intGen;
    private static GenSub intGenSub;
    private static Gen<Integer> subCopy;

    @Callable
    public static void compareSomething(int typeInput){
        if(typeInput == 1) {
            compare(BigInteger.ONE, BigInteger.ZERO, "BigInteger"); // should be 1
        } else if(typeInput == 2) {
            compare('a', 'a', "Character"); // should be 0
        } else if(typeInput == 3) {
            compare("5", "6", "String"); // should be -1
        } else {
            compare(null, null, "null"); // should be 100
        }
    }

    @Callable
    public static int getCompareResult(){
        return COMPARISION_RESULT;
    }

    /**
     * Assume obj1 and obj2 are both of type @param objectsType
     * compareTo() calls on objects transforms to the shadow class in avm avm_compareTo
     * set COMPARISION_RESULT on default to indicate @objectsType not supported
     *
     * returns an int [-1, 0, or 1]
     */
    private static void compare(Object obj1, Object obj2, String objectsType){
        switch (objectsType) {
            case "BigInteger":
                COMPARISION_RESULT = ((BigInteger) obj1).compareTo((BigInteger) obj2);
                break;
            case "Character":
                COMPARISION_RESULT = ((Character) obj1).compareTo((Character) obj2);
                break;
            case "String":
                COMPARISION_RESULT = ((String) obj1).compareTo((String) obj2);
                break;
            default:
                COMPARISION_RESULT = 100;
        }
    }

    @Callable
    public static void pickTarget(int targetNum){
        if (targetNum == 1){
            currentTargetClass = new TargetClassImplOne();
            currentTargetClass.play();
        } else if(targetNum ==2){
            currentTargetClass = new TargetClassImplTwo();
            currentTargetClass.play();
        }
    }

    @Callable
    public static String getName(){
        return currentTargetClass.getName();
    }

    /**
     * A demo on overriding generic method.
     */
    @Callable
    public static void setGenerics(int input1, int input2){
        intGen = new Gen<>(input1);
        intGenSub = new GenSub(input2);
        subCopy = intGenSub;
    }

    @Callable
    public static int getIntGen(){
        return intGen.getObject(1);
    }

    @Callable
    public static int getIntGenSub(){
        return intGenSub.getObject(1);
    }

    @Callable
    public static int getSubCopy(){
        return subCopy.getObject(1);
    }

    /**
     * Generating layered synthetic method cases
     *
     * TargetAbstractClass implements the TargetInterface interface
     * TargetClassImplOne extends TargetAbstractClass and overrides methods in parent class
     * TargetClassImplTwo extends TargetAbstractClass and uses methods in parent class
     *
     */
    private interface TargetInterface{

        void play();

        boolean addPlayer(Address playerAddress);

        String getName();
    }

    private static abstract class TargetAbstractClass implements TargetInterface{

        protected String name;
        protected int playNum;
        protected Object specialObject;

        private AionMap<Address, Integer> playersList;

        public TargetAbstractClass(){
            name = "TargetAbstractClass";
            this.specialObject = new Object();
        }

        @Override
        public void play() {
            playNum = 0;
        }

        @Override
        public boolean addPlayer(Address playerAddress) {
            if (!playersList.containsKey(playerAddress)){
                playersList.put(playerAddress, 0);
                return true;
            }
            return false;
        }
        
        @Override
        public String getName(){
            return this.name;
        }

        public Object getSpecialObject() {
            return specialObject;
        }

        // set specialObject
        public <E> void GenericMethod(E input){
            this.specialObject = input;
        }
    }

    private static class TargetClassImplOne extends TargetAbstractClass {

        TargetClassImplOne(){
            super.name = "TargetClassImplOne";
        }

        @Override
        public void play() {
            super.playNum = 1;
        }

        @Override
        public String getName() {
            return super.name;
        }

        @Override
        public Object getSpecialObject() {
            return super.specialObject;
        }

        // set specialObject
        public <E> void GenericMethod(int input){
            this.specialObject = input;
        }
    }

    private static class TargetClassImplTwo extends TargetAbstractClass {

        TargetClassImplTwo(){
            super(); // uses parent name
        }

        @Override
        public void play() {
            super.playNum = 2;
        }

        public String getName() {
            return super.name; // returns parent name
        }
    }

    // A simple generic class
    static class Gen<T> {
        T obj; // declare an object of type T

        Gen(T o) {
            obj = o;
        }

        // generic method
        T getObject(T arg) {
            return obj;
        }
    }

    // A subclass of Gen that overrides getObject().
    static class GenSub extends Gen<Integer> {

        GenSub(Integer obj) {
            super(obj);
        }

        // Override generic method.
        Integer getObject(Integer arg) {
            return obj;
        }
    }
}


