package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.rt.Address;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class StringConstantVisitorTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }


    private Class<?> clazz;
    private Class<?> clazzNoStatic;

    @Before
    public void setup() throws Exception {
        String targetTestName = StringConstantVisitorTestTarget.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(targetTestName.replaceAll("\\.", "/") + ".class");
        String targetNoStaticName = StringConstantVisitorTestTargetNoStatic.class.getName();
        byte[] targetNoStaticBytes = Helpers.loadRequiredResourceAsBytes(targetNoStaticName.replaceAll("\\.", "/") + ".class");
        
        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new StringConstantVisitor())
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(targetTestName, transformer.apply(targetTestBytes));
        classes.put(targetNoStaticName, transformer.apply(targetNoStaticBytes));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
        this.clazz = loader.loadClass(targetTestName);
        this.clazzNoStatic = loader.loadClass(targetNoStaticName);
    }

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    @Test
    public void testLoadStringConstant() throws Exception {
        Object obj = this.clazz.getConstructor().newInstance();
        
        // Get the constant via the method.
        Method method = this.clazz.getMethod("returnStaticStringConstant");
        Object ret = method.invoke(obj);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, ret);
        
        // Get the constant directly from the static field.
        Object direct = this.clazz.getField("kStringConstant").get(null);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, direct);
        
        // They should also be the same instance.
        Assert.assertTrue(ret == direct);
    }

    @Test
    public void testLoadStringConstantNoStatic() throws Exception {
        Object obj = this.clazzNoStatic.getConstructor().newInstance();
        
        // Get the constant via the method.
        Method method = this.clazzNoStatic.getMethod("returnStaticStringConstant");
        Object ret = method.invoke(obj);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, ret);
        
        // Get the constant directly from the static field.
        Object direct = this.clazzNoStatic.getField("kStringConstant").get(null);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, direct);
        
        // They should also be the same instance.
        Assert.assertTrue(ret == direct);
    }
}
