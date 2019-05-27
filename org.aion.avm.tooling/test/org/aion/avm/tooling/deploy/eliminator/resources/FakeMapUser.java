package org.aion.avm.tooling.deploy.eliminator.resources;

import java.util.Map;

public class FakeMapUser {

    public static byte[] main() {
        createMap();
        return null;
    }

    private static void createMap() {
        Map map = new FakeMap();
        int size = map.size();
        map.remove("abc");
    }
}
