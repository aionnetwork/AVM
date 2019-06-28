package org.aion.avm.tooling.shadowing.testClass;

import avm.Address;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.tooling.deploy.eliminator.UnreachableMethodRemover;
import org.aion.avm.tooling.deploy.renamer.Renamer;
import org.aion.avm.userlib.CodeAndArguments;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassNameTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() throws Exception{
        // class and method renaming is not used
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = new JarOptimizer(true).optimize(compiler.getJarFileBytes());
        optimizedDappBytes = UnreachableMethodRemover.optimize(optimizedDappBytes);
        byte[] data = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void getClassName1DIntArray() {
        int[] a = new int[10];
        assertEquals(a.getClass().getName(), callStatic("getClassName1DIntArray"));
    }

    @Test
    public void getClassName2DBooleanArray() {
        boolean[][] b = new boolean[10][];
        assertEquals(b.getClass().getName(), callStatic("getClassName2DBooleanArray"));
    }

    @Test
    public void getClassName1DObjectArray() {
        Object[] o = new Object[10];
        assertEquals(o.getClass().getName(), callStatic("getClassName1DObjectArray"));
    }

    @Test
    public void getClassName3DObjectArray() {
        Object[][][] o = new Object[10][][];
        assertEquals(o.getClass().getName(), callStatic("getClassName3DObjectArray"));
    }

    @Test
    public void getClassName1DUserDefinedArray() {
        TestResource.Child[] c = new TestResource.Child[10];
        assertEquals(c.getClass().getName(), callStatic("getClassName1DUserDefinedArray"));
        TestResource.Child child = new TestResource.Child();
        assertEquals(child.getClass().getName(), callStatic("getClassNameUserDefined"));
    }

    @Test
    public void getObjectToString(){
        assertEquals(TestResource.Child.class.getName() + "@14", callStatic("getObjectToString"));
    }

    @Test
    public void getClassNameThrowableArray() {
        Throwable[] t = new Throwable[10];
        assertEquals(t.getClass().getName(), callStatic("getClassNameThrowableArray"));
    }

    @Test
    public void getClassNameAssertionErrorArray() {
        AssertionError[] t = new AssertionError[10];
        assertEquals(t.getClass().getName(), callStatic("getClassNameAssertionErrorArray"));
    }

    @Test
    public void getClassName1DStringArray() {
        String[] t = new String[10];
        assertEquals(t.getClass().getName(), callStatic("getClassName1DStringArray"));
    }

    @Test
    public void getClassName2DStringArray() {
        String[][] t = new String[10][];
        assertEquals(t.getClass().getName(), callStatic("getClassName2DStringArray"));
    }

    @Test
    public void getClassNameAddressArray() {
        Address[] a = new Address[10];
        assertEquals(a.getClass().getName(), callStatic("getClassNameAddressArray"));
    }

    @Test
    public void getClassNameThrowable() {
        Throwable t = new Throwable();
        assertEquals(t.getClass().getName(), callStatic("getClassNameThrowable"));
    }

    @Test
    public void getClassNameBoolean() {
        Boolean b = true;
        assertEquals(b.getClass().getName(), callStatic("getClassNameBoolean"));
    }

    @Test
    public void getClassNameArrayParam() {
        int[] a = new int[5];
        assertEquals(a.getClass().getName(), callStatic("getClassName1DArrayParam", a));

        int[][] b = new int[10][10];
        assertEquals(b.getClass().getName(),callStatic("getClassNameUserDefined2", new Object[]{b}));
    }

    @Test
    public void getInterfaceName(){
        assertEquals(TestResource.MyInterface.class.getName(), callStatic("getInterfaceName"));
    }

    @Test
    public void getInterfaceArrayName(){
        TestResource.MyInterface[] o = new TestResource.MyInterface[10];
        assertEquals(o.getClass().getName(), callStatic("getInterfaceArrayName"));
    }

    @Test
    public void getObjectImplementInterfaceName(){
        TestResource.MyChild[] o = new TestResource.MyChild[10];
        assertEquals(o.getClass().getName(), callStatic("getObjectImplementInterfaceName"));
    }

    private String callStatic(String methodName, Object... args) {

        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        return (String) avmRule.call(sender, contract, value, data, 2_000_000, 1).getDecodedReturnData();
    }

}
