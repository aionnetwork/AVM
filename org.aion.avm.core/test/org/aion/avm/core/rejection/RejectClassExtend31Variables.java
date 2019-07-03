package org.aion.avm.core.rejection;


/**
 * This will be rejected since it declares declares another instance variable after the super declared 31.
 */
public class RejectClassExtend31Variables extends RejectClass31Variables {
    public int sub1;

    public static byte[] main() {
        return new byte[] { 1 };
    }
}
