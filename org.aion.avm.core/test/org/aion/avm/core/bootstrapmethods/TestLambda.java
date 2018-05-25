package org.aion.avm.core.bootstrapmethods;

import java.util.stream.Stream;

class TestLambda {
    public static void main(String[] args) {
       Runnable r = ()-> {
           System.out.println("1");
           System.out.println("2");
           System.out.println("3");
       };
       r.run();
    }
}
