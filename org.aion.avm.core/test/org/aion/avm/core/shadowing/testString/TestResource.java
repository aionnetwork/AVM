package org.aion.avm.core.shadowing.testString;

public class TestResource {

    public Object[] singleString() {
        // to hold various results for verifying
        Object[] results = new Object[16];
        int i = 0;

        String str1 = new String("abc");
        results[i++] = str1.hashCode();
        results[i++] = str1.length();
        results[i++] = str1.contains("c");
        results[i++] = str1.contains("d");
        results[i++] = str1.equals("abc");
        results[i++] = str1.equals("def");
        results[i++] = str1.startsWith("a");
        results[i++] = str1.startsWith("b");
        results[i++] = str1.isEmpty();
        results[i++] = str1.charAt(0);
        results[i++] = str1.getBytes();
        results[i++] = str1.toLowerCase();
        results[i++] = str1.toUpperCase();
        results[i++] = str1.indexOf("b");
        results[i++] = str1.indexOf("d");

        return results;
    }
}
