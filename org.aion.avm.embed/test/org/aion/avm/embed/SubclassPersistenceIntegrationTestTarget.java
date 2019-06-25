package org.aion.avm.embed;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;


/**
 * The test class loaded by SubclassPersistenceIntegrationTest.
 * It defines some inner classes as sub-classes of various whitelisted classes in the JCL/API.
 * The purpose of the test is to make sure that those can be used and persist/restore correctly.
 */
public class SubclassPersistenceIntegrationTestTarget {
    private static SubUser user;
    private static SubEnum anEnum;
    private static SubException exception;
    private static SubObject object;
    private static SubRuntimeException runtimeException;
    private static SubThrowable throwable;

    @Callable
    public static int setup_user() {
        // We just need some kind of random number.
        user = new SubUser((int)Blockchain.getBlockTimestamp());
        return user.number2;
    }

    @Callable
    public static int check_user() {
        return user.number2;
    }

    @Callable
    public static int setup_enum() {
        anEnum = SubEnum.ONE;
        return anEnum.hashCode();
    }

    @Callable
    public static int check_enum() {
        return anEnum.hashCode();
    }

    @Callable
    public static int setup_exception() {
        // We just need some kind of random number.
        exception = new SubException((int)Blockchain.getBlockTimestamp() + 1);
        return exception.number;
    }

    @Callable
    public static int check_exception() {
        return exception.number;
    }

    @Callable
    public static int setup_object() {
        // We just need some kind of random number.
        object = new SubObject((int)Blockchain.getBlockTimestamp() + 2);
        return object.number;
    }

    @Callable
    public static int check_object() {
        return object.number;
    }

    @Callable
    public static int setup_runtimeException() {
        // We just need some kind of random number.
        runtimeException = new SubRuntimeException((int)Blockchain.getBlockTimestamp() + 3);
        return runtimeException.number;
    }

    @Callable
    public static int check_runtimeException() {
        return runtimeException.number;
    }

    @Callable
    public static int setup_throwable() {
        // We just need some kind of random number.
        throwable = new SubThrowable((int)Blockchain.getBlockTimestamp() + 4);
        return throwable.number;
    }

    @Callable
    public static int check_throwable() {
        return throwable.number;
    }


    private static class SubUser extends SubObject {
        public final int number2;
        public SubUser(int number) {
            super(number);
            this.number2 = number + 1;
        }
    }

    private static enum SubEnum {
        ONE;
    }

    private static class SubException extends Exception {
        private static final long serialVersionUID = 1L;
        public final int number;
        public SubException(int number) {
            super();
            this.number = number;
        }
    }

    private static class SubObject {
        public final int number;
        public SubObject(int number) {
            this.number = number;
        }
    }

    private static class SubRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public final int number;
        public SubRuntimeException(int number) {
            super();
            this.number = number;
        }
    }

    private static class SubThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public final int number;
        public SubThrowable(int number) {
            super();
            this.number = number;
        }
    }
}
