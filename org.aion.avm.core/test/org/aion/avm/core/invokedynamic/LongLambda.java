package org.aion.avm.core.invokedynamic;

/**
 * @author Roman Katerinenko
 */
public final class LongLambda {

    private static Double result;

    public Double test() {
        Runnable r = () -> {
            final var str = new String("str");
            final var intArr = new int[]{100};
            intArr[0] += 0;
            final var objArr = new Object[]{null};
            objArr[0] = str;
            LongLambda.result = (double) intArr[0];
        };
        r.run();
        return result;
    }
}