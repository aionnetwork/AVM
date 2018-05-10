package org.aion.avm.core.shadowing;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassShadowingTest {

    private static byte[] readClassFile(String name) throws IOException {
        InputStream in = ClassShadowing.class.getResourceAsStream("/" + name.replaceAll("\\.", File.separator) + ".class");
        return in.readAllBytes();
    }

    @Test
    public void testReplaceJavaLang() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("java/lang/Object", "org/aion/avm/java/lang/Object");
        map.put("java/lang/Math", "org/aion/avm/java/lang/Math");

        byte[] source = readClassFile("org.aion.avm.core.shadowing.TestResource");
        byte[] result = ClassShadowing.replaceInvoke(source, map);

        System.out.println(source.length);
        System.out.println(result.length);
    }
}
