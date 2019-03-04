package org.aion.avm.tooling.abi;

public class AnnotationException extends RuntimeException {
    public AnnotationException(String exceptionString, String methodName) {
        super("Exception in method " + methodName + ": " + exceptionString);
    }
}
