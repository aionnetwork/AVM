package org.aion.avm.core.rejection;

import java.text.Annotation;


/**
 * Written to demonstrate the bug in issue-305:  attempting to subclass something not in the shadow JCL should safely fail due to a rejection.
 */
public class RejectNonShadowJclSubclassError extends Annotation {
    public RejectNonShadowJclSubclassError(Object value) {
        super(value);
    }

    public static byte[] main() {
        return new byte[] { 0x0 };
    }
}
