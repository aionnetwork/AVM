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

    public static interface ChildA extends RootA, RootB {
        String getChildA();
    }

    public static interface ChildB extends RootA, RootB {
        String getChildB();
    }
}
