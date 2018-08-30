package org.aion.avm.core.rejection;


public class RejectStaticStringCall {
    public String callStaticString() {
        return String.join(".", "elt", "one", "two");
    }
}
