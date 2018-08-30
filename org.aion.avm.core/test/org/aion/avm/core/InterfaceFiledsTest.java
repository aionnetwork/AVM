package org.aion.avm.core;

import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.InterfaceFieldMappingVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.types.GeneratedClassConsumer;
import org.aion.avm.core.util.Helpers;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterfaceFiledsTest {

    @Test
    public void testVisitor() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Set<String> userInterfaceSlashNames = Set.of(Helpers.fulllyQualifiedNameToInternalName(OuterInteface.class.getName()));
        String javaLangObjectSlashName = "java/lang/Object";

        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(OuterInteface.class.getName().replaceAll("\\.", "/") + ".class");
        byte[] transformed = new ClassToolchain.Builder(bytecode, 0)
                .addNextVisitor(new InterfaceFieldMappingVisitor(consumer, userInterfaceSlashNames, javaLangObjectSlashName))
                .addWriter(new ClassWriter(0))
                .build()
                .runAndGetBytecode();
        classes.put(OuterInteface.class.getName(), transformed);

        AvmSharedClassLoader loader = new AvmSharedClassLoader(classes);
        Class<?> clazz1 = loader.loadClass(OuterInteface.class.getName());
        assertTrue(clazz1.getFields().length == 0);

        Class<?> clazz2 = loader.loadClass(OuterInteface.class.getName() + "$FIELDS");

        Field a = clazz2.getField("a");
        a.setAccessible(true);
        assertEquals(1, a.get(null));

        Field b = clazz2.getField("b");
        b.setAccessible(true);
        assertEquals("abc", b.get(null));

        Field c = clazz2.getField("c");
        c.setAccessible(true);
        assertEquals(Object.class, c.get(null).getClass());
    }

    @Test
    public void testInnnerInterfaceStaticFields() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(1_000_000L, InterfaceTestResource.class, InterfaceTestResource.InnerInterface.class);
        AvmClassLoader classLoader = avm.getClassLoader();

        classLoader.loadUserClassByOriginalName(InterfaceTestResource.InnerInterface.class.getName());
        avm.shutdown();
    }

    @Test
    public void testOuterInterfaceStaticFields() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(1_000_000L, OuterInteface.class);
        AvmClassLoader classLoader = avm.getClassLoader();

        classLoader.loadUserClassByOriginalName(OuterInteface.class.getName());
        avm.shutdown();
    }

    @Test
    public void testAccess() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SimpleAvm avm = new SimpleAvm(1_000_000L, InterfaceTestResource.class, InterfaceTestResource.InnerInterface.class);
        AvmClassLoader classLoader = avm.getClassLoader();

        Class<?> clazz = classLoader.loadUserClassByOriginalName(InterfaceTestResource.class.getName());
        Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("f")).invoke(null);
        assertEquals(6, ret);

        avm.shutdown();
    }
}
