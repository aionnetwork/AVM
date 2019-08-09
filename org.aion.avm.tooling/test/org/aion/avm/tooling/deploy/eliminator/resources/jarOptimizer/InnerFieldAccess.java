package org.aion.avm.tooling.deploy.eliminator.resources.jarOptimizer;

public class InnerFieldAccess {

    public int getCount(){
        InnerClass innerClass = new InnerClass();
        return innerClass.count;
    }

    public class InnerClass {
        public int count = 0;
    }
}
