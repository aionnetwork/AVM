package org.aion.avm.core.invokedynamic;

import java.lang.invoke.StringConcatFactory;

public class StringConcatFactoryMethodCall {
    public StringConcatFactoryMethodCall() {
        try {
            StringConcatFactory.makeConcat(null, null, null);
        } catch (Exception e) {
        }
    }
}