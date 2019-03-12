package org.aion.avm.tooling.abi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.aion.avm.core.dappreading.JarBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class MainTest {
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

        byte[] jar =
            JarBuilder
                .buildJarForMainAndClasses(TestDAppTarget.class);
        Path tempDir = Files.createTempDirectory("tempResources");
            DataOutputStream dout =
                new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
            dout.write(jar);
            dout.close();

        ABICompiler.main(new String[] {tempDir.toString() + "/dapp.jar"});
        Assert.assertEquals(
            ABICompiler.getVersionNumber()
                + "\norg.aion.avm.tooling.abi.TestDAppTarget"
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

        byte[] jar =
            JarBuilder
                .buildJarForMainAndClasses(ChattyCalculatorTarget.class, SilentCalculatorTarget.class);
        Path tempDir = Files.createTempDirectory("tempResources");
            DataOutputStream dout =
                new DataOutputStream(new FileOutputStream(tempDir.toString() + "/dapp.jar"));
            dout.write(jar);
            dout.close();


        ABICompiler.main(new String[] {tempDir.toString() + "/dapp.jar"});
        Assert.assertEquals(
            ABICompiler.getVersionNumber()
                + "\norg.aion.avm.tooling.abi.ChattyCalculatorTarget"
                + "\npublic static String amIGreater(int, int)\n",
            outContent.toString());
        File outputJar = new File(System.getProperty("user.dir") + "/outputJar.jar");
        boolean didDelete = outputJar.delete();
        Assert.assertTrue(didDelete);
    }
}
