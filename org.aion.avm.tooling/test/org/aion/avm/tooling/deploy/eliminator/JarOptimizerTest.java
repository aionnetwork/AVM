package org.aion.avm.tooling.deploy.eliminator;

import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.tooling.deploy.eliminator.resources.jarOptimizer.*;
import org.aion.avm.utilities.JarBuilder;
import org.aion.avm.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

public class JarOptimizerTest {

    @Test
    public void testInnerClassRemoval() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(Main.class, new HashMap<>(),
                InnerMethodAccess.class, InnerFieldAccess.class, InnerClassUnreachable.class, Generic.class);
        JarOptimizer jarOptimizer = new JarOptimizer(false);
        byte[] optimizedJar = jarOptimizer.optimize(jar);

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(optimizedJar), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.DOT_NAME);
        Assert.assertEquals(9, classMap.size());

        Assert.assertTrue(classMap.containsKey(Main.class.getName()));
        Assert.assertTrue(classMap.containsKey(InnerMethodAccess.InnerClass.class.getName()));
        Assert.assertTrue(classMap.containsKey(InnerFieldAccess.class.getName()));
        Assert.assertTrue(classMap.containsKey(InnerFieldAccess.InnerClass.class.getName()));
        Assert.assertTrue(classMap.containsKey(InnerClassUnreachable.class.getName()));
        // This is included only because method signature is preserved
        Assert.assertTrue(classMap.containsKey(InnerClassUnreachable.InnerClassArg.class.getName()));
        Assert.assertTrue(classMap.containsKey(Generic.class.getName()));
        Assert.assertTrue(classMap.containsKey(Generic.StaticInnerClassGeneric.class.getName()));
        Assert.assertTrue(classMap.containsKey(Generic.InnerClassGeneric.class.getName()));

        SingleLoader loader = new SingleLoader();
        for(Map.Entry<String, byte[]> e: classMap.entrySet()){
            loader.loadClassFromByteCode(e.getKey(), e.getValue());
        }
    }

    public class SingleLoader extends ClassLoader {

        Class<?> loadClassFromByteCode(String name, byte[] bytecode) {
            Class<?> clazz = this.defineClass(name, bytecode, 0, bytecode.length);
            return clazz;
        }
    }
}
