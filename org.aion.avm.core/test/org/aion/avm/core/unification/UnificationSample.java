package org.aion.avm.core.unification;

import avm.Result;
import org.aion.avm.userlib.AionBuffer;

public class UnificationSample {

    public enum UnificationEnum { VALUE1, VALUE2 }

    public static byte[] main() {
        return null;
    }

    public static void something(boolean flag) {
        Number number = Integer.valueOf(1);
        String string = new String("");
        Result result = new Result(true, null);
        AionBuffer buffer = AionBuffer.allocate(10);
        Enum enumeration = UnificationEnum.VALUE1;
        RuntimeException jclException = new NullPointerException();
        UnificationException userException = new UnificationException();
        UnificationClass userClass = new UnificationClass();
        UnificationAbstractClass userAbstractClass = new UnificationClass();
        UnificationInterface userInterface = new UnificationClass();

        string = (flag ? number : result).toString();
    }

    public static class UnificationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public interface UnificationInterface {
        public void somethingElse();
    }

    public static abstract class UnificationAbstractClass implements UnificationInterface {}

    public static class UnificationClass extends UnificationAbstractClass {

        @Override
        public void somethingElse() {}

    }
}
