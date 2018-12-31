package legacy_examples.twoclasses;


public class C1 {
    private class InnerClass {
    }

    private static class NestedClass {
    }

    interface NestedInterface {

    }

    public C2 getC2() {
        return new C2();
    }
}
