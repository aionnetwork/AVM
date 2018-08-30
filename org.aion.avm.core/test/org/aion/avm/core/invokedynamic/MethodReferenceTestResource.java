package org.aion.avm.core.invokedynamic;

public class MethodReferenceTestResource {
    public static final Integer VALUE = 10;


    public MethodReferenceTestResource() {
        methodReferenceAcceptor(MethodReferenceTestResource::function);
    }

    public Object methodReferenceAcceptor(java.util.function.Function<Object, Object> function) {
        return function.apply(null);
    }

    public static Object function(Object object) {
        return VALUE;
    }
}