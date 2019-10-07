package org.aion.avm.core;

import i.PackageConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.instrument.MethodWrapperVisitor;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

//TODO -- move these tests to the stacktracking folder

public class MethodWrapperTest {
    private SimpleAvm avm;
    private Class<?> mainClass;
    private Class<?> innerClass;
    private Class<?> innerEnum;
    private Class<?> abstractClass;
    private Class<?> interfaceClass;
    private Class<?> innerInterface;
    private Class<?> innerInterfaceWithDefaultMethod;
    private Class<?> innerConstructorClass;

    private static boolean preserveDebuggability = false;

    @Before
    public void setup() throws Exception {
        this.avm = new SimpleAvm(1000000L, preserveDebuggability,
            MethodWrapperTarget.class,
            MethodWrapperTarget.E.class,
            MethodWrapperTarget.InnerClass.class,
            MethodWrapperAbstractTarget.class,
            MethodWrapperInterfaceTarget.class,
            MethodWrapperTarget.InnerAbstract.class,
            MethodWrapperTarget.InnerAbstractImpl.class,
            MethodWrapperTarget.InnerInterface.class,
            MethodWrapperTarget.InnerInterfaceImpl.class,
            MethodWrapperTarget.InnerInterfaceWithDefaultMethod.class,
            MethodWrapperTarget.InnerInterfaceWithDefaultMethodImpl.class,
            MethodWrapperTarget.InnerConstructorClass.class,
            ABIDecoder.class,
            ABIEncoder.class,
            ABIException.class
        );
        AvmClassLoader loader = avm.getClassLoader();

        this.mainClass = loader.loadUserClassByOriginalName(MethodWrapperTarget.class.getName(), preserveDebuggability);
        this.innerClass = loader.loadUserClassByOriginalName(MethodWrapperTarget.InnerClass.class.getName(), preserveDebuggability);
        this.innerEnum = loader.loadUserClassByOriginalName(MethodWrapperTarget.E.class.getName(), preserveDebuggability);
        this.abstractClass = loader.loadUserClassByOriginalName(MethodWrapperAbstractTarget.class.getName(), preserveDebuggability);
        this.interfaceClass = loader.loadUserClassByOriginalName(MethodWrapperInterfaceTarget.class.getName(), preserveDebuggability);
        this.innerInterface = loader.loadUserClassByOriginalName(MethodWrapperTarget.InnerInterface.class.getName(), preserveDebuggability);
        this.innerInterfaceWithDefaultMethod = loader.loadUserClassByOriginalName(MethodWrapperTarget.InnerInterfaceWithDefaultMethod.class.getName(), preserveDebuggability);
        this.innerConstructorClass = loader.loadUserClassByOriginalName(MethodWrapperTarget.InnerConstructorClass.class.getName(), preserveDebuggability);
        Assert.assertEquals(loader, this.mainClass.getClassLoader());

        forceConstantsToLoad(loader);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    /**
     * Verifies that invoking the original main method and the wrapper main method produces the same result.
     */
    @Test
    public void testInvokeOriginalAndWrapperMainMethods() throws Exception {
        Object classInstance = this.mainClass.getConstructor(s.java.lang.String.class).newInstance(new s.java.lang.String(""));

        Method originalMain = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "main");
        a.ByteArray output = (a.ByteArray) originalMain.invoke(classInstance);
        byte[] bytes = output.getUnderlying();
        Assert.assertEquals("testMain", new ABIDecoder(bytes).decodeOneString());

        Method wrapperMain = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "main");
        output = (a.ByteArray) wrapperMain.invoke(classInstance);
        bytes = output.getUnderlying();
        Assert.assertEquals("testMain", new ABIDecoder(bytes).decodeOneString());
    }

    /**
     * Verifies that every method in the main class has a wrapper method that differs only by prefix.
     */
    @Test
    @Ignore
    public void testWrapperMethodsOnMainClass() throws Exception {
        // Check that the constructor and its wrapper both exist!
        this.mainClass.getConstructor(s.java.lang.String.class);
        this.mainClass.getConstructor(s.java.lang.String.class, s.java.lang.Void.class);

        // Now check methods and their wrappers...
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "main");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "main");

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticLong");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticLong");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnLong");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnLong");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticLongWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticLongWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnLongWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, i.IObject.class, byte.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnLongWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, i.IObject.class, byte.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticInt");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticInt");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnInt");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnInt");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticIntWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticIntWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnIntWithParams", int.class, s.java.lang.String.class, this.innerClass, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnIntWithParams", int.class, s.java.lang.String.class, this.innerClass, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticChar");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticChar");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnChar");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnChar");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticCharWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticCharWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnCharWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnCharWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticByte");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticByte");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnByte");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnByte");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticByteWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticByteWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnByteWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnByteWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticShort");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticShort");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnShort");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnShort");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticShortWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticShortWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnShortWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, this.innerClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnShortWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, this.innerClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticBoolean");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticBoolean");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnBoolean");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnBoolean");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticBooleanWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticBooleanWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnBooleanWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Exception.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnBooleanWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Exception.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticDouble");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticDouble");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnDouble");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnDouble");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticDoubleWithParams", byte.class, a.CharArray.class, this.innerClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticDoubleWithParams", byte.class, a.CharArray.class, this.innerClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnDoubleWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnDoubleWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticFloat");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticFloat");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnFloat");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnFloat");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticFloatWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticFloatWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnFloatWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnFloatWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticVoid");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticVoid");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnVoid");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnVoid");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticVoidWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticVoidWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnVoidWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnVoidWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticArray");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticArray");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnArray");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnArray");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticArrayWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticArrayWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnArrayWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnArrayWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticObject");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticObject");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnObject");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnObject");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticObjectWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticObjectWithParams", byte.class, a.CharArray.class, i.IObject.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnObjectWithParams", int.class, this.innerClass, s.java.lang.String.class, s.java.lang.Enum.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnObjectWithParams", int.class, this.innerClass, s.java.lang.String.class, s.java.lang.Enum.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "finalMethod", this.innerEnum);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "finalMethod", this.innerEnum);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithTryCatch", boolean.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithTryCatch", boolean.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "recurse", int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "recurse", int.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callInnerAbstractImpl", int.class, long.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callInnerAbstractImpl", int.class, long.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callInnerInterfaceImpl", this.innerInterface, s.java.lang.String.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callInnerInterfaceImpl", this.innerInterface, s.java.lang.String.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callAndThrow");
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callAndThrow");

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callDefaultMethod", this.innerInterfaceWithDefaultMethod, s.java.lang.String.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callDefaultMethod", this.innerInterfaceWithDefaultMethod, s.java.lang.String.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth1", int.class, int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth1", int.class, int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth2", int.class, int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth2", int.class, int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth3", int.class, int.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth3", int.class, int.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callConstructors", s.java.lang.String.class, s.java.lang.String.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callConstructors", s.java.lang.String.class, s.java.lang.String.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticVoidReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticVoidReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticVoidReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticVoidReturnWithParams", this.abstractClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticPrimitiveReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticPrimitiveReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticPrimitiveReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticPrimitiveReturnWithParams", this.abstractClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticArrayReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticArrayReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticArrayReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticArrayReturnWithParams", this.abstractClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticObjectReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticObjectReturn", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticObjectReturnWithParams", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticObjectReturnWithParams", this.abstractClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithImplementation", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithImplementation", this.abstractClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "staticMethodWithImplementation", int.class, s.java.lang.Exception.class);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "staticMethodWithImplementation", int.class, s.java.lang.Exception.class);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceVoidReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceVoidReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticVoidReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticVoidReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceVoidReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceVoidReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticVoidReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticVoidReturnWithParams", this.interfaceClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfacePrimitiveReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfacePrimitiveReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticPrimitiveReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticPrimitiveReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfacePrimitiveReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfacePrimitiveReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticPrimitiveReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticPrimitiveReturnWithParams", this.interfaceClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceArrayReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceArrayReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticArrayReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticArrayReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceArrayReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceArrayReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticArrayReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticArrayReturnWithParams", this.interfaceClass);

        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceObjectReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceObjectReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticObjectReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticObjectReturn", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceObjectReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceObjectReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticObjectReturnWithParams", this.interfaceClass);
        this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticObjectReturnWithParams", this.interfaceClass);
    }

    /**
     * Verifies that every method in the inner class has a wrapper method that differs only by prefix.
     */
    @Test
    public void testWrapperMethodsOnInnerClass() throws Exception {
        this.innerClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "innerMethod", s.java.lang.String.class, a.BooleanArray.class);
        this.innerClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "innerMethod", s.java.lang.String.class, a.BooleanArray.class);
    }

    /**
     * Verifies that only non-abstract methods have a wrapper method that differs by a prefix.
     */
    @Test
    public void testWrapperMethodsOnAbstractClass() throws Exception {
        int noSuchMethodCount = 0;

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturn");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturn");

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturnWithParams", byte.class, char.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturnWithParams", byte.class, char.class);

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturn");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturn");

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturnWithParams", s.java.lang.String.class, a.IntArray.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturnWithParams", s.java.lang.String.class, a.IntArray.class);

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturn");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturn");

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturnWithParams", char.class, a.ByteArray.class, i.IObject.class, s.java.lang.String.class, int.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturnWithParams", char.class, a.ByteArray.class, i.IObject.class, s.java.lang.String.class, int.class);

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturn");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturn");

        try {
            this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturnWithParams", s.java.math.BigInteger.class, s.java.lang.Integer.class, s.java.lang.Double.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturnWithParams", s.java.math.BigInteger.class, s.java.lang.Integer.class, s.java.lang.Double.class);

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithImplementation");
        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithImplementation");

        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithImplementation", int.class, s.java.lang.Exception.class);
        this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithImplementation", int.class, s.java.lang.Exception.class);

        // We expect all of the above try-catch blocks to fail out and increment the counter, they are all abstract methods.
        Assert.assertEquals(8, noSuchMethodCount);
    }

    /**
     * Verifies that every method in the interface class does not have a wrapper method, because
     * every method in this class is abstract, except for any default methods with implementation.
     */
    @Test
    public void testWrapperMethodsOnInterfaceClass() throws Exception {
        int noSuchMethodCount = 0;

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnVoid");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnVoid");

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnVoidWithParams", i.IObject.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnVoidWithParams", i.IObject.class);

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnPrimitive");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnPrimitive");

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnPrimitiveWithParams", int.class, char.class, s.java.lang.String.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnPrimitiveWithParams", int.class, char.class, s.java.lang.String.class);

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnArray");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnArray");

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnArrayWithParams", s.java.math.BigInteger.class, short.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnArrayWithParams", s.java.math.BigInteger.class, short.class);

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnObject");
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnObject");

        try {
            this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceReturnObjectWithParams", s.java.lang.Exception.class, a.CharArray.class, a.IntArray.class);
        } catch (NoSuchMethodException e) {
            noSuchMethodCount++;
        }

        this.interfaceClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceReturnObjectWithParams", s.java.lang.Exception.class, a.CharArray.class, a.IntArray.class);

        Method original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callDefaultMethod", this.innerInterfaceWithDefaultMethod, s.java.lang.String.class);
        Method wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callDefaultMethod", this.innerInterfaceWithDefaultMethod, s.java.lang.String.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        // We expect all of the above try-catch blocks to fail out and increment the counter, they are all abstract methods.
        Assert.assertEquals(8, noSuchMethodCount);
    }

    /**
     * Verifies that the wrapper method is identical to the original method in terms of:
     * 1. access modifiers
     * 2. parameter types
     * 3. return types
     * 4. declared exceptions
     */
    @Test
    public void testMainClassWrapperMethodSimilarity() throws Exception {
        Method original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "main");
        Method wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "main");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticLong");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticLong");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnLong");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnLong");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticLongWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticLongWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnLongWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, i.IObject.class, byte.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnLongWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.String.class, i.IObject.class, byte.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticInt");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticInt");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnInt");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnInt");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticIntWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticIntWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnIntWithParams", int.class, s.java.lang.String.class, this.innerClass, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnIntWithParams", int.class, s.java.lang.String.class, this.innerClass, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticChar");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticChar");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnChar");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnChar");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticCharWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticCharWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnCharWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnCharWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticByte");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticByte");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnByte");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnByte");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticByteWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticByteWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnByteWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnByteWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticShort");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticShort");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnShort");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnShort");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticShortWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticShortWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnShortWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, this.innerClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnShortWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, this.innerClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticBoolean");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticBoolean");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnBoolean");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnBoolean");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticBooleanWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticBooleanWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnBooleanWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Exception.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnBooleanWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Exception.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticDouble");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticDouble");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnDouble");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnDouble");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticDoubleWithParams", byte.class, a.CharArray.class, this.innerClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticDoubleWithParams", byte.class, a.CharArray.class, this.innerClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnDoubleWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnDoubleWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticFloat");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticFloat");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnFloat");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnFloat");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticFloatWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticFloatWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnFloatWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnFloatWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticVoid");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticVoid");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnVoid");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnVoid");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticVoidWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticVoidWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnVoidWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnVoidWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticArray");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticArray");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnArray");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnArray");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticArrayWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticArrayWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnArrayWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnArrayWithParams", int.class, s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticObject");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticObject");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnObject");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnObject");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnStaticObjectWithParams", byte.class, a.CharArray.class, i.IObject.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnStaticObjectWithParams", byte.class, a.CharArray.class, i.IObject.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "returnObjectWithParams", int.class, this.innerClass, s.java.lang.String.class, s.java.lang.Enum.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "returnObjectWithParams", int.class, this.innerClass, s.java.lang.String.class, s.java.lang.Enum.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "finalMethod", this.innerEnum);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "finalMethod", this.innerEnum);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithTryCatch", boolean.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithTryCatch", boolean.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "recurse", int.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "recurse", int.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth1", int.class, int.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth1", int.class, int.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth2", int.class, int.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth2", int.class, int.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callChainDepth3", int.class, int.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callChainDepth3", int.class, int.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callConstructors", s.java.lang.String.class, s.java.lang.String.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callConstructors", s.java.lang.String.class, s.java.lang.String.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callInnerAbstractImpl", int.class, long.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callInnerAbstractImpl", int.class, long.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callInnerInterfaceImpl", this.innerInterface, s.java.lang.String.class);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callInnerInterfaceImpl", this.innerInterface, s.java.lang.String.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "callAndThrow");
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "callAndThrow");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticVoidReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticVoidReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractVoidReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractVoidReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticVoidReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticVoidReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticPrimitiveReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticPrimitiveReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractPrimitiveReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractPrimitiveReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticPrimitiveReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticPrimitiveReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticArrayReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticArrayReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractArrayReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractArrayReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticArrayReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticArrayReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticObjectReturn", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticObjectReturn", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractObjectReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractObjectReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "abstractStaticObjectReturnWithParams", this.abstractClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "abstractStaticObjectReturnWithParams", this.abstractClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceVoidReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceVoidReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticVoidReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticVoidReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceVoidReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceVoidReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticVoidReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticVoidReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfacePrimitiveReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfacePrimitiveReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticPrimitiveReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticPrimitiveReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfacePrimitiveReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfacePrimitiveReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticPrimitiveReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticPrimitiveReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceArrayReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceArrayReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticArrayReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticArrayReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceArrayReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceArrayReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticArrayReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticArrayReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceObjectReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceObjectReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticObjectReturn", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticObjectReturn", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceObjectReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceObjectReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "interfaceStaticObjectReturnWithParams", this.interfaceClass);
        wrapper = this.mainClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "interfaceStaticObjectReturnWithParams", this.interfaceClass);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());
    }

    /**
     * Verifies that the wrapper method is identical to the original method in terms of:
     * 1. access modifiers
     * 2. parameter types
     * 3. return types
     * 4. declared exceptions
     */
    @Test
    public void testInnerClassWrapperMethodSimilarity() throws Exception {
        Method original = this.innerClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "innerMethod", s.java.lang.String.class, a.BooleanArray.class);
        Method wrapper = this.innerClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "innerMethod", s.java.lang.String.class, a.BooleanArray.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());
    }

    /**
     * Verifies that the wrapper method is identical to the original method in terms of:
     * 1. access modifiers
     * 2. parameter types
     * 3. return types
     * 4. declared exceptions
     *
     * Note that we only have 2 non-abstract methods in this class for which this is even applicable.
     */
    @Test
    public void testAbstractClassWrapperMethodSimilarity() throws Exception {
        Method original = this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithImplementation");
        Method wrapper = this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithImplementation");
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        original = this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.METHOD_PREFIX + "methodWithImplementation", int.class, s.java.lang.Exception.class);
        wrapper = this.abstractClass.getDeclaredMethod(MethodWrapperVisitor.WRAPPER_PREFIX + "methodWithImplementation", int.class, s.java.lang.Exception.class);
        Assert.assertArrayEquals(original.getParameterTypes(), wrapper.getParameterTypes());
        Assert.assertEquals(original.getReturnType(), wrapper.getReturnType());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());
    }

    /**
     * Verifies that the constructors in the InnerConstructorClass have their wrappers instrumented.
     */
    @Test
    @Ignore
    public void testWrapperConstructors() throws Exception {
        // An exception will be thrown if any of these constructors does not exist.
        this.innerConstructorClass.getConstructor();
        this.innerConstructorClass.getConstructor(s.java.lang.Void.class);

        this.innerConstructorClass.getConstructor(s.java.lang.String.class);
        this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.Void.class);

        this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.String.class);
        this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Void.class);
    }

    /**
     * Verifies that the wrapper constructor is identical to the original method in terms of:
     *
     * 1. access modifiers
     * 2. parameter types (with the exception of a single void parameter at the end of the original)
     * 3. return types
     * 4. declared exceptions
     */
    @Test
    @Ignore
    public void testInnerConstructorClassConstructorSimilarity() throws Exception {
        Constructor wrapper = this.innerConstructorClass.getConstructor();
        Constructor original = this.innerConstructorClass.getConstructor(s.java.lang.Void.class);
        Assert.assertArrayEquals(removeFinalEntry(original.getParameterTypes()), wrapper.getParameterTypes());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        wrapper = this.innerConstructorClass.getConstructor(s.java.lang.String.class);
        original = this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.Void.class);
        Assert.assertArrayEquals(removeFinalEntry(original.getParameterTypes()), wrapper.getParameterTypes());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());

        wrapper = this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.String.class);
        original = this.innerConstructorClass.getConstructor(s.java.lang.String.class, s.java.lang.String.class, s.java.lang.Void.class);
        Assert.assertArrayEquals(removeFinalEntry(original.getParameterTypes()), wrapper.getParameterTypes());
        Assert.assertArrayEquals(original.getExceptionTypes(), wrapper.getExceptionTypes());
        Assert.assertEquals(original.getModifiers(), wrapper.getModifiers());
    }

    private static Class<?>[] removeFinalEntry(Class<?>[] array) {
        return Arrays.copyOf(array, array.length - 1);
    }

    private void forceConstantsToLoad(AvmClassLoader loader) throws ClassNotFoundException {
        // We can force the <clinit> to run if we use Class.forName().
        boolean initialize = true;
        Class.forName(PackageConstants.kConstantClassName, initialize, loader);
    }
}
