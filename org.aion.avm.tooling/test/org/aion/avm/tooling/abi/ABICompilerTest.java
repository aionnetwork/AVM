package org.aion.avm.tooling.abi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aion.avm.tooling.deploy.eliminator.TestUtil;
import org.aion.avm.tooling.util.JarBuilder;
import org.aion.avm.tooling.util.Utilities;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.abi.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ABICompilerTest {
    private static final String CHATTY_CALCULATOR_ABI = ABICompiler.getVersionNumberForABIFilePrint(ABICompiler.getDefaultVersionNumber())
        + "\n" + ChattyCalculatorTarget.class.getName()
        + "\nClinit: ()"
        + "\npublic static String amIGreater(int, int)\n";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testMainMethod() throws IOException {

        byte[] jar = TestUtil.serializeClassesAsJar(TestDAppTarget.class);
        Path tempDir = Files.createTempDirectory("tempResources");
        DataOutputStream dout =
            new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
        dout.write(jar);
        dout.close();

        ABICompiler.main(new String[]{tempDir.toString() + "/dapp.jar"});
        Assert.assertEquals(
                ABICompiler.getVersionNumberForABIFilePrint(ABICompiler.getDefaultVersionNumber())
                + "\n" + TestDAppTarget.class.getName()
                + "\nClinit: ()"
                + "\npublic static String returnHelloWorld()"
                + "\npublic static String returnGoodbyeWorld()"
                + "\npublic static String returnEcho(String)"
                + "\npublic static Address returnEchoAddress(Address)"
                + "\npublic static String returnAppended(String, String)"
                + "\npublic static String returnAppendedMultiTypes(String, String, boolean, int)"
                + "\npublic static int[] returnArrayOfInt(int, int, int)"
                + "\npublic static String[] returnArrayOfString(String, String, String)"
                + "\npublic static int[] returnArrayOfIntEcho(int[])"
                + "\npublic static int[][] returnArrayOfInt2D(int, int, int, int)"
                + "\npublic static int[][] returnArrayOfInt2DEcho(int[][])"
                + "\npublic static void doNothing()\n",
            outContent.toString());
        File outputJar = new File(System.getProperty("user.dir") + "/outputJar.jar");
        boolean didDelete = outputJar.delete();
        Assert.assertTrue(didDelete);
    }

    @Test
    public void testMainMethodCalculator() throws IOException {

        byte[] jar = TestUtil.serializeClassesAsJar(ChattyCalculatorTarget.class, SilentCalculatorTarget.class);
        Path tempDir = Files.createTempDirectory("tempResources");
        DataOutputStream dout =
            new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
        dout.write(jar);
        dout.close();

        ABICompiler.main(new String[]{tempDir.toString() + "/dapp.jar"});
        Assert.assertEquals(CHATTY_CALCULATOR_ABI,
            outContent.toString());
        File outputJar = new File(System.getProperty("user.dir") + "/outputJar.jar");
        boolean didDelete = outputJar.delete();
        Assert.assertTrue(didDelete);
    }

    @Test
    public void testEmbeddedUsesOfCalculator() {
        byte[] jar = TestUtil.serializeClassesAsJar(ChattyCalculatorTarget.class, SilentCalculatorTarget.class);
        
        ABICompiler embeddedCompiler = ABICompiler.compileJarBytes(jar);
        // Write the ABI to a stream we can explore.
        ByteArrayOutputStream abiStream = new ByteArrayOutputStream();
        embeddedCompiler.writeAbi(abiStream, ABICompiler.getDefaultVersionNumber());
        String abiString = new String(abiStream.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertEquals(CHATTY_CALCULATOR_ABI, abiString);
    }

    @Test
    public void testStaticInitializers() throws IOException {

        byte[] jar = TestUtil.serializeClassesAsJar(StaticInitializersTarget.class, SilentCalculatorTarget.class);
        Path tempDir = Files.createTempDirectory("tempResources");
        DataOutputStream dout =
            new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
        dout.write(jar);
        dout.close();

        ABICompiler.main(new String[]{tempDir.toString() + "/dapp.jar"});
        Assert.assertEquals(
                ABICompiler.getVersionNumberForABIFilePrint(ABICompiler.getDefaultVersionNumber())
                + "\n" + StaticInitializersTarget.class.getName()
                + "\nClinit: (int, String)"
                + "\npublic static int getInt()"
                + "\npublic static String getString()"
                + "\npublic static String amIGreater(int, int)\n",
            outContent.toString());
        File outputJar = new File(System.getProperty("user.dir") + "/outputJar.jar");
        boolean didDelete = outputJar.delete();
        Assert.assertTrue(didDelete);
    }

    @Test
    public void testGetMissingUserlibClasses() {
        byte[] jar = TestUtil.serializeClassesAsJar(ChattyCalculatorTarget.class, SilentCalculatorTarget.class, AionList.class, AionBuffer.class);

        Class[] expectedMissingClasses = new Class[] {ABIDecoder.class, ABIEncoder.class,
            ABIStreamingEncoder.class, ABIException.class, ABIToken.class, AionMap.class, AionSet.class};

        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        Class[] actualMissingClasses = ABICompiler.getMissingUserlibClasses(compiler.getClassMap());
        checkArrays(expectedMissingClasses, actualMissingClasses);
    }

    // Compilation should fail because of garbage AionList.class
    @Test(expected = ABICompilerException.class)
    public void testGetMissingUserlibClassesFail() {
        String qualifiedClassName = ChattyCalculatorTarget.class.getName();
        String internalName = Utilities.fulllyQualifiedNameToInternalName(qualifiedClassName);
        byte[] mainClassBytes = Utilities.loadRequiredResourceAsBytes(internalName + ".class");
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("org.aion.avm.userlib.AionList", new byte[10]);
        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode(qualifiedClassName, mainClassBytes, classMap);
        ABICompiler.compileJarBytes(jar);
    }

    @Test(expected = ABICompilerException.class)
    public void testBigIntegerVersion(){
        byte[] jar = TestUtil.serializeClassesAsJar(DAppBigIntegerABIType.class);
        ABICompiler.compileJarBytes(jar, 0);
    }

    @Test
    public void testBigInteger() throws IOException {
        String version = "1";

        byte[] jar = TestUtil.serializeClassesAsJar(DAppBigIntegerABIType.class);
        Path tempDir = Files.createTempDirectory("tempResources");
        DataOutputStream dout =
                new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
        dout.write(jar);
        dout.close();

        ABICompiler.main(new String[]{tempDir.toString() + "/dapp.jar", version});
        Assert.assertEquals(
                version
                        + "\n" + DAppBigIntegerABIType.class.getName()
                        + "\nClinit: ()"
                        + "\npublic static BigInteger returnBigInteger()"
                        + "\n",
                outContent.toString());
        File outputJar = new File(System.getProperty("user.dir") + "/outputJar.jar");
        boolean didDelete = outputJar.delete();
        Assert.assertTrue(didDelete);
    }

    private void checkArrays(Class[] expectedArray, Class[] actualArray) {
        List<Class> expectedList = Arrays.asList(expectedArray);
        List<Class> actualList = Arrays.asList(actualArray);
        Assert.assertTrue(actualList.containsAll(expectedList));
        Assert.assertTrue(expectedList.containsAll(actualList));
    }
}
