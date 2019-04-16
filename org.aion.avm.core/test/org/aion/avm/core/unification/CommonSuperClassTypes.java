package org.aion.avm.core.unification;


/**
 * issue-362: Defines the types used by the CommonSuperClassTest (specifically, the generated CommonSuperClassTarget class).
 */
public class CommonSuperClassTypes {
    public static interface RootA {
        String getRootA();
    }

    public static interface RootB {
        String getRootB();
    }

    public static interface SubRootA1 extends RootA {}

    public static interface SubRootA2 extends RootA {}

    public static interface SubSubRootA1 extends SubRootA1 {}

    public static class SubSubRootA1Child implements SubSubRootA1 {

        @Override
        public String getRootA() {
            return null;
        }
    }

    public static class SubRootA1Child implements SubRootA1 {

        @Override
        public String getRootA() {
            return null;
        }
    }

    public static class SubRootA2Child implements SubRootA2 {

        @Override
        public String getRootA() {
            return null;
        }
    }

    public static interface ChildA extends RootA, RootB {
        String getChildA();
    }

    public static interface ChildB extends RootA, RootB {
        String getChildB();
    }

    public static class ConcreteChildA implements ChildA {

        @Override
        public String getRootA() {
            return "Child A -> getRootA";
        }

        @Override
        public String getRootB() {
            return "Child B -> getRootB";
        }

        @Override
        public String getChildA() {
            return "Child A -> getChildA";
        }
    }

    public static class ConcreteChildB implements ChildB {

        @Override
        public String getRootA() {
            return "Child B -> getRootA";
        }

        @Override
        public String getRootB() {
            return "Child B -> getRootB";
        }

        @Override
        public String getChildB() {
            return "Child B -> getChildB";
        }
    }

    public static enum EnumA1 implements RootA, RootB {
        ME;

        @Override
        public String getRootA() {
            return "A";
        }
        @Override
        public String getRootB() {
            return "B";
        }
    }

    public static enum EnumA2 implements RootA {
        ME;

        @Override
        public String getRootA() {
            return "A";
        }
    }

    public static enum EnumB implements RootA, RootB {
        ME;

        @Override
        public String getRootA() {
            return "A";
        }
        @Override
        public String getRootB() {
            return "B";
        }
    }

    public static abstract class ClassRoot {
        public abstract String getClassRoot();
    }

    public static abstract class ClassChild extends ClassRoot {
    }
}
