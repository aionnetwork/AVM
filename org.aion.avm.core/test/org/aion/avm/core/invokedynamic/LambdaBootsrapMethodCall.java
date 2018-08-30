package org.aion.avm.core.invokedynamic;

public class LambdaBootsrapMethodCall {
    public LambdaBootsrapMethodCall() {
        try {
            java.lang.invoke.LambdaMetafactory.metafactory(null, null, null, null, null, null);
        } catch (Exception e) {
        }
    }
}