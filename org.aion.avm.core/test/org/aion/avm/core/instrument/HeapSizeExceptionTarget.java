package org.aion.avm.core.instrument;

public class HeapSizeExceptionTarget {
    public static class UserDefinedException extends Exception {
        private static final long serialVersionUID = 1L;
        public final String additionalMessage;
        public UserDefinedException(String message, String additionalMessage) {
            super(message);
            this.additionalMessage = additionalMessage;
        }
    }
}
