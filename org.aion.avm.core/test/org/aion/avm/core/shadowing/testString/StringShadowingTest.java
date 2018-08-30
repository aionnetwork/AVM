package org.aion.avm.core.shadowing.testString;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.shadow.java.lang.Boolean;
import org.aion.avm.shadow.java.lang.Character;
import org.aion.avm.shadow.java.lang.Integer;
import org.aion.avm.shadow.java.lang.String;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringShadowingTest {

    @Test
    public void testSingleString() throws Exception {
        SimpleAvm avm = new SimpleAvm(1000000L, TestResource.class);

        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
        Object obj = clazz.getConstructor().newInstance();
        ObjectArray results = (ObjectArray) clazz.getMethod(NamespaceMapper.mapMethodName("singleString")).invoke(obj);

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
        avm.shutdown();
    }
}
