package org.aion.avm.core.testBlake2b;

public class Main {
    public byte[] main() {
        Blake2b mac = Blake2b.Mac.newInstance("key".getBytes());
        return mac.digest("input".getBytes());
    }
}
