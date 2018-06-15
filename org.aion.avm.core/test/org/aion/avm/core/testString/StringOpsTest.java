package org.aion.avm.core.testString;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.java.lang.Boolean;
import org.aion.avm.java.lang.Character;
import org.aion.avm.java.lang.Integer;
import org.aion.avm.java.lang.String;
import org.aion.avm.rt.Address;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringOpsTest {

    @Test
    public void testSingleString() throws Exception {
        SimpleRuntime rt = new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 10000);
        SimpleAvm avm = new SimpleAvm(rt, StringOps.class);

        Class<?> clazz = avm.getClassLoader().loadClass(StringOps.class.getName());
        Object obj = clazz.getConstructor().newInstance();
        ObjectArray results = (ObjectArray) clazz.getMethod("avm_singleString").invoke(obj);

        int i = 0;
        assertEquals(new Integer(96354), results.get(i++));
        assertEquals(new Integer(3), results.get(i++));
        assertEquals(Boolean.avm_TRUE, results.get(i++));
        assertEquals(Boolean.avm_FALSE, results.get(i++));
        assertEquals(Boolean.avm_TRUE, results.get(i++));
        assertEquals(Boolean.avm_FALSE, results.get(i++));
        assertEquals(Boolean.avm_TRUE, results.get(i++));
        assertEquals(Boolean.avm_FALSE, results.get(i++));
        assertEquals(Boolean.avm_FALSE, results.get(i++));
        assertEquals(new Character('a'), results.get(i++));
        assertEquals(new ByteArray(new byte[]{'a', 'b', 'c'}), results.get(i++));
        assertEquals(new String("abc"), results.get(i++));
        assertEquals(new String("ABC"), results.get(i++));
        assertEquals(new Integer(1), results.get(i++));
        assertEquals(new Integer(-1), results.get(i++));
        assertEquals(null, results.get(i++));
    }
}
