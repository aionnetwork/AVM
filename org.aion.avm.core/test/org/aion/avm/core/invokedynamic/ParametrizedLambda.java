package org.aion.avm.core.invokedynamic;

/**
 * @author Roman Katerinenko
 */
public class ParametrizedLambda {

    public Double test() {
        java.util.function.Function
                <java.lang.Integer, java.lang.Double> fun = (i) -> java.lang.Double.valueOf(10 + i);
        return fun.apply(20);
    }
}