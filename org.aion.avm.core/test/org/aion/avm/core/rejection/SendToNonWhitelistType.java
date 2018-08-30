package org.aion.avm.core.rejection;



/**
 * A basic DApp which should fail to load because it defines a method which is initially defined (in the hierarchy) in a JCL class not in the
 * shadow implementation.
 */
public class SendToNonWhitelistType {
    public static void failingCase() {
        ((java.util.Scanner)null).close();
    }
}
