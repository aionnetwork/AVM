package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class StringConstantVisitorTest {
    private Class<?> clazz;
    private Class<?> clazzNoStatic;

    @Before
    public void setup() throws Exception {
        String targetTestName = StringConstantVisitorTestTarget.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(targetTestName.replaceAll("\\.", "/") + ".class");
        String targetNoStaticName = StringConstantVisitorTestTargetNoStatic.class.getName();
        byte[] targetNoStaticBytes = Helpers.loadRequiredResourceAsBytes(targetNoStaticName.replaceAll("\\.", "/") + ".class");
        
        // WARNING:  We are providing the class set as both the "classes only" and "classes plus interfaces" sets.
        // This works for this test but, in general, is not correct.
        Set<String> userClassDotNameSet = Set.of(targetTestName, targetNoStaticName);
        PreRenameClassAccessRules classAccessRules = new PreRenameClassAccessRules(userClassDotNameSet, userClassDotNameSet);
        
        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(classAccessRules)))
                        .addNextVisitor(new ConstantVisitor())
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(PackageConstants.kUserDotPrefix + targetTestName, transformer.apply(targetTestBytes));
        classes.put(PackageConstants.kUserDotPrefix + targetNoStaticName, transformer.apply(targetNoStaticBytes));

        Map<String, byte[]> classAndHelper = Helpers.mapIncludingHelperBytecode(classes, Helpers.loadDefaultHelperBytecode());
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classAndHelper);
        // We don't really need the runtime but we do need the intern map initialized.
        loader.loadClass(Helper.RUNTIME_HELPER_NAME).getConstructor(ClassLoader.class, long.class, int.class).newInstance(loader, 1_000_000L, 1);
        
        this.clazz = loader.loadUserClassByOriginalName(targetTestName);
        this.clazzNoStatic = loader.loadUserClassByOriginalName(targetNoStaticName);
    }

    @After
    public void clearTestingState() {
        // NOTE:  We should use the actual instance we created but we only want to clear the thread local.
        Helper.clearTestingState();
    }

    @Test
    public void testLoadStringConstant() throws Exception {
        Object obj = this.clazz.getConstructor().newInstance();
        
        // Get the constant via the method.
        Method method = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnStaticStringConstant"));
        Object ret = method.invoke(obj);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, ret.toString());
        
        // Get the constant directly from the static field.
        Object direct = this.clazz.getField(NamespaceMapper.mapFieldName("kStringConstant")).get(null);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, direct.toString());
        
        // They should also be the same instance.
        Assert.assertTrue(ret == direct);
    }

    @Test
    public void testLoadStringConstantNoStatic() throws Exception {
        Object obj = this.clazzNoStatic.getConstructor().newInstance();
        
        // Get the constant via the method.
        Method method = this.clazzNoStatic.getMethod(NamespaceMapper.mapMethodName("returnStaticStringConstant"));
        Object ret = method.invoke(obj);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, ret.toString());
        
        // Get the constant directly from the static field.
        Object direct = this.clazzNoStatic.getField(NamespaceMapper.mapFieldName("kStringConstant")).get(null);
        Assert.assertEquals(StringConstantVisitorTestTarget.kStringConstant, direct.toString());
        
        // They should also be the same instance.
        Assert.assertTrue(ret == direct);
    }
}
