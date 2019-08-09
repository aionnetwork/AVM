package org.aion.avm.tooling.deploy.eliminator.resources.jarOptimizer;

import java.util.List;

public class InnerClassUnreachable {

    public void outerMethod(List<InnerClassArg> args) {

    }

    public class InnerClassArg {
        public void method() {
        }
    }

    public class InnerClassNotReferenced {
        public void method() {
        }
    }
}
