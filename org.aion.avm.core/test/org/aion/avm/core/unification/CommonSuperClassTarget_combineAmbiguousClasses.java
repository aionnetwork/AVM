package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineAmbiguousClasses {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        CommonSuperClassTypes.ConcreteChildA childA = new CommonSuperClassTypes.ConcreteChildA();
        CommonSuperClassTypes.ConcreteChildB childB = new CommonSuperClassTypes.ConcreteChildB();

        String answer = combineAmbiguous1(true, childA, childB);
        if (!answer.equals(childA.getRootA())) {
            throw new IllegalStateException("Wrong answer: " + answer);
        }

        answer = combineAmbiguous1(false, childA, childB);
        if (!answer.equals(childB.getRootA())) {
            throw new IllegalStateException("Wrong answer: " + answer);
        }

        CommonSuperClassTypes.ChildA a = (CommonSuperClassTypes.ChildA) combineAmbiguous2(true, childA, childB);
        CommonSuperClassTypes.ChildB b = (CommonSuperClassTypes.ChildB) combineAmbiguous2(false, childA, childB);

        if (!a.getRootA().equals(childA.getRootA())) {
            throw new IllegalStateException();
        }

        if (!b.getRootA().equals(childB.getRootA())) {
            throw new IllegalStateException();
        }

        return null;
    }

    public static String combineAmbiguous1(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return (flag ? a : b).getRootA();
    }

    public static Object combineAmbiguous2(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return flag ? a : b;
    }
}
