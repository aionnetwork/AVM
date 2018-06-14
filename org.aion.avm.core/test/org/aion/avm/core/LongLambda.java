package org.aion.avm.core;

/**
 * @author Roman Katerinenko
 */
public final class LongLambda {
    static Double result;

    public Double test() {
        Runnable r = () -> {
            System.out.println("");
            final var max = Math.max(1, 23);
            final var str = new String("str");
            final var obj = new Object();
            final var intArr = new int[]{10};
            intArr[0] = max;
            final var objArr = new Object[]{null};
            objArr[0] = str;
            try {
                throw new IllegalStateException("Test exception");
            }catch (IllegalStateException e){
                // do nothing
            }
            result = 100.;
        };
        r.run();
        return result;
    }
}