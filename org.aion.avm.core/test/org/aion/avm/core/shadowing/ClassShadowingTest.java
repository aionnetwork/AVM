package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.PackageConstants;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;


public class ClassShadowingTest {
    @Test
    public void testNewObject() throws Exception {
        SimpleAvm avm = new SimpleAvm(1_000_000L, TestObjectCreation.class);
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestObjectCreation.class.getName());

        Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("accessObject")).invoke(null);
        Assert.assertEquals(Integer.valueOf(1), ret);
        avm.shutdown();
    }

    @Test
    public void testReplaceJavaLang() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = TestResource.class.getName();
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(createTestingMapper(className)))
                        .addNextVisitor(new ConstantVisitor())
                        .addNextVisitor(new ClassShadowing(PackageConstants.kShadowSlashPrefix))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(PackageConstants.kUserDotPrefix + className, transformer.apply(bytecode));
        Map<String, byte[]> classesAndHelper = Helpers.mapIncludingHelperBytecode(classes, Helpers.loadDefaultHelperBytecode());
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classesAndHelper);

        TestingInstrumentation instrumentation = new TestingInstrumentation(new CommonInstrumentation());
        InstrumentationHelpers.attachThread(instrumentation);
        IRuntimeSetup runtime = Helpers.getSetupForLoader(loader);
        InstrumentationHelpers.pushNewStackFrame(runtime, loader, 1_000_000L, 1);
        Class<?> clazz = loader.loadUserClassByOriginalName(className);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("abs"), int.class);
        Object ret = method.invoke(obj, -10);
        Assert.assertEquals(10, ret);

        // Note that all string constants are wrapped in <clinit>, so we should see that, but classes are lazily wrapped so that is still 0.
        Assert.assertEquals(1, instrumentation.countWrappedStrings);
        Assert.assertEquals(0, instrumentation.countWrappedClasses);

        // We can rely on our test-facing toString methods to look into what we got back.
        Object wrappedClass = clazz.getMethod(NamespaceMapper.mapMethodName("returnClass")).invoke(obj);
        Assert.assertEquals("class org.aion.avm.shadow.java.lang.String", wrappedClass.toString());
        Object wrappedString = clazz.getMethod(NamespaceMapper.mapMethodName("returnString")).invoke(obj);
        Assert.assertEquals("hello", wrappedString.toString());

        // Verify that we see wrapped instances.
        Assert.assertEquals(1, instrumentation.countWrappedStrings);
        Assert.assertEquals(1, instrumentation.countWrappedClasses);

        InstrumentationHelpers.popExistingStackFrame(runtime);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = TestResource2.class.getName();
        String mappedClassName = PackageConstants.kUserDotPrefix + className;
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0) /* DO NOT SKIP ANYTHING */
                        .addNextVisitor(new UserClassMappingVisitor(createTestingMapper(className)))
                        .addNextVisitor(new ConstantVisitor())
                        .addNextVisitor(new ClassShadowing(PackageConstants.kShadowSlashPrefix))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();

        Map<String, byte[]> classes = new HashMap<>();
        classes.put(mappedClassName, transformer.apply(bytecode));

        Map<String, byte[]> classesAndHelper = Helpers.mapIncludingHelperBytecode(classes, Helpers.loadDefaultHelperBytecode());
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classesAndHelper);

        CommonInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        IRuntimeSetup runtime = Helpers.getSetupForLoader(loader);
        InstrumentationHelpers.pushNewStackFrame(runtime, loader, 1_000_000L, 1);

        Class<?> clazz = loader.loadClass(mappedClassName);
        Object obj = clazz.getConstructor().newInstance();

        Method method2 = clazz.getMethod(NamespaceMapper.mapMethodName("localVariable"));
        Object ret2 = method2.invoke(obj);
        Assert.assertEquals(Integer.valueOf(3), ret2);

        InstrumentationHelpers.popExistingStackFrame(runtime);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void testInterfaceHandling() throws Exception {
        String className = TestResourceInterface.class.getName();
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        String innerClassName = className + "$1";
        byte[] innerBytecode = Helpers.loadRequiredResourceAsBytes(innerClassName.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(createTestingMapper(className, innerClassName)))
                        .addNextVisitor(new ConstantVisitor())
                        .addNextVisitor(new ClassShadowing(PackageConstants.kShadowSlashPrefix))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        byte[] transformed = transformer.apply(bytecode);
        classes.put(PackageConstants.kUserDotPrefix + className, transformed);
        classes.put(PackageConstants.kUserDotPrefix + innerClassName, transformer.apply(innerBytecode));

        Map<String, byte[]> classesAndHelper = Helpers.mapIncludingHelperBytecode(classes, Helpers.loadDefaultHelperBytecode());
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classesAndHelper);

        CommonInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        IRuntimeSetup runtime = Helpers.getSetupForLoader(loader);
        InstrumentationHelpers.pushNewStackFrame(runtime, loader, 1_000_000L, 1);
        Class<?> clazz = loader.loadUserClassByOriginalName(className);

        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("getStringForNull"));
        Object ret = method.invoke(null);
        // Note that we can't yet override methods in our contracts so the toString returns false, from Object.
        Assert.assertEquals(null, ret);

        InstrumentationHelpers.popExistingStackFrame(runtime);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void testEnumHandling() throws Exception {
        SimpleAvm avm = new SimpleAvm(1000000L, TestResourceEnum.class);
        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(TestResourceEnum.class.getName());
        
        // Try the normal constructor (private, so set accessible).
        Constructor<?> one = clazz.getDeclaredConstructor(org.aion.avm.shadow.java.lang.String.class, int.class, org.aion.avm.shadow.java.lang.String.class);
        one.setAccessible(true);
        Object instance = one.newInstance(new org.aion.avm.shadow.java.lang.String("TEST"), 99, new org.aion.avm.shadow.java.lang.String("TEST"));
        Assert.assertNotNull(instance);
        
        // Try the deserialization constructor.
        Object stub = clazz.getConstructor(IDeserializer.class, IPersistenceToken.class).newInstance(null, null);
        Assert.assertNotNull(stub);
        avm.shutdown();
    }

    @Test
    public void testEnumHandling_internal() throws Exception {
        SimpleAvm avm = new SimpleAvm(1000000L, TestContainer.class, TestContainer.InternalEnum.class);
        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(TestContainer.InternalEnum.class.getName());
        
        // Try the normal constructor (private, so set accessible).
        Constructor<?> one = clazz.getDeclaredConstructor(org.aion.avm.shadow.java.lang.String.class, int.class, org.aion.avm.shadow.java.lang.String.class);
        one.setAccessible(true);
        Object instance = one.newInstance(new org.aion.avm.shadow.java.lang.String("TEST"), 99, new org.aion.avm.shadow.java.lang.String("TEST"));
        Assert.assertNotNull(instance);
        
        // Try the deserialization constructor.
        Object stub = clazz.getConstructor(IDeserializer.class, IPersistenceToken.class).newInstance(null, null);
        Assert.assertNotNull(stub);
        avm.shutdown();
    }

    /**
     * Shadow Object is only instance-equal, no other kind of equality is defined.
     */
    @Test
    public void testShadowObjectEquality() throws Exception {
        SimpleAvm avm = new SimpleAvm(1_000_000L, TestObjectCreation.class);
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestObjectCreation.class.getName());
        Method createInstance = clazz.getMethod(NamespaceMapper.mapMethodName("createInstance"));
        Method isEqual = clazz.getMethod(NamespaceMapper.mapMethodName("isEqual"), org.aion.avm.internal.IObject.class, org.aion.avm.internal.IObject.class);

        Object one = createInstance.invoke(null);
        Object two = createInstance.invoke(null);
        Assert.assertEquals(Boolean.TRUE, isEqual.invoke(null, one, one));
        Assert.assertEquals(Boolean.FALSE, isEqual.invoke(null, one, two));
        avm.shutdown();
    }


    private NamespaceMapper createTestingMapper(String... userDotNameClasses) {
        // WARNING:  We are providing the class set as both the "classes only" and "classes plus interfaces" sets.
        // This works for this test but, in general, is not correct.
        Set<String> userClassDotNameSet = Set.of(userDotNameClasses);
        return new NamespaceMapper(new PreRenameClassAccessRules(userClassDotNameSet, userClassDotNameSet));
    }


    public static class TestingInstrumentation implements IInstrumentation {
        private final IInstrumentation realImplementation;
        public int countWrappedClasses;
        public int countWrappedStrings;
        
        public TestingInstrumentation(IInstrumentation realImplementation) {
            this.realImplementation = realImplementation;
        }
        @Override
        public void enterNewFrame(ClassLoader contractLoader, long energyLeft, int nextHashCode) {
            this.realImplementation.enterNewFrame(contractLoader, energyLeft, nextHashCode);
        }
        @Override
        public void exitCurrentFrame() {
            this.realImplementation.exitCurrentFrame();
        }
        @Override
        public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
            this.countWrappedClasses += 1;
            return this.realImplementation.wrapAsClass(input);
        }
        @Override
        public org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            this.countWrappedStrings += 1;
            return this.realImplementation.wrapAsString(input);
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable input) {
            return this.realImplementation.unwrapThrowable(input);
        }
        @Override
        public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object input) {
            return this.realImplementation.wrapAsThrowable(input);
        }
        @Override
        public void chargeEnergy(long cost) throws OutOfEnergyException {
            this.realImplementation.chargeEnergy(cost);
        }
        @Override
        public long energyLeft() {
            return this.realImplementation.energyLeft();
        }
        @Override
        public int getNextHashCodeAndIncrement() {
            return this.realImplementation.getNextHashCodeAndIncrement();
        }
        @Override
        public void setAbortState() {
            this.realImplementation.setAbortState();
        }
        @Override
        public int getCurStackSize() {
            return this.realImplementation.getCurStackSize();
        }
        @Override
        public int getCurStackDepth() {
            return this.realImplementation.getCurStackDepth();
        }
        @Override
        public void enterMethod(int frameSize) {
            this.realImplementation.enterMethod(frameSize);
        }
        @Override
        public void exitMethod(int frameSize) {
            this.realImplementation.exitMethod(frameSize);
        }
        @Override
        public void enterCatchBlock(int depth, int size) {
            this.realImplementation.enterCatchBlock(depth, size);
        }
        @Override
        public int peekNextHashCode() {
            return this.realImplementation.peekNextHashCode();
        }
        @Override
        public void forceNextHashCode(int nextHashCode) {
            this.realImplementation.forceNextHashCode(nextHashCode);
        }
        @Override
        public void bootstrapOnly() {
            this.realImplementation.bootstrapOnly();
        }
    }
}
