package org.aion.cli;

import java.io.File;

import org.aion.avm.api.Address;
import org.aion.avm.core.ShadowCoverageTarget;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class AvmCLIIntegrationTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void usage() {
        TestEnvironment env = new TestEnvironment("Usage: AvmCLI [options] [command] [command options]");
        // We expect this to count as a failure so extract the string.
        String errorMessage = null;
        try {
            AvmCLI.testingMain(env, new String[0]);
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
        }
        Assert.assertEquals("Usage: AvmCLI [options] [command] [command options]", errorMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressArgumentInvalid() {
        String[] args = new String[]{"call", "0xFFFF", "--method", "method", "--args", "-A", "0x112233"};
        // We should fail on the next line.
        ArgumentParser.parseArgs(args);
    }

    @Test
    public void testAddressArgument() {
        String[] args = new String[]{"call", "0xFFFF", "--method", "method", "--args", "-A", "0x1122334455667788112233445566778811223344556677881122334455667788"};
        ArgumentParser.Invocation invocation = ArgumentParser.parseArgs(args);
        // Verify that the argument is an Address.
        Assert.assertTrue(invocation.commands.get(0).args.get(0) instanceof Address);
    }

    @Test
    public void exploreShadowCoverageTarget() throws Exception {
        // Create the JAR and write it to a location we can parse from the command-line.
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        File temp = this.folder.newFile();
        Helpers.writeBytesToFile(jar, temp.getAbsolutePath());

        // Create the testing environment to look for the successful deployment.
        TestEnvironment deployEnv = new TestEnvironment("Result status: SUCCESS");
        AvmCLI.testingMain(deployEnv, new String[] {"deploy", temp.getAbsolutePath()});
        Assert.assertTrue(deployEnv.didScrapeString);
        String dappAddress = deployEnv.capturedAddress;
        Assert.assertNotNull(dappAddress);

        // Now, issue a call.
        TestEnvironment callEnv = new TestEnvironment("Result status: SUCCESS");
        AvmCLI.testingMain(callEnv, new String[] {"call", dappAddress, "--method", "populate_JavaLang"});
        Assert.assertTrue(callEnv.didScrapeString);

        // Now, check the storage.
        // (note that this NPE is just something in an instance field, as an example of deep data).
        TestEnvironment exploreEnv = new TestEnvironment("NullPointerException(27):");
        AvmCLI.testingMain(exploreEnv, new String[] {"explore", dappAddress});
        Assert.assertTrue(exploreEnv.didScrapeString);
    }

    @Test
    public void callWithAddressArgument() throws Exception {
        // Create the JAR and write it to a location we can parse from the command-line.
        byte[] jar = JarBuilder.buildJarForMainAndClasses(LengthOfAddressTarget.class);
        File temp = this.folder.newFile();
        Helpers.writeBytesToFile(jar, temp.getAbsolutePath());
        
        // Create the testing environment to look for the successful deployment.
        TestEnvironment deployEnv = new TestEnvironment("Result status: SUCCESS");
        AvmCLI.testingMain(deployEnv, new String[] {"deploy", temp.getAbsolutePath()});
        Assert.assertTrue(deployEnv.didScrapeString);
        String dappAddress = deployEnv.capturedAddress;
        Assert.assertNotNull(dappAddress);
        
        // Now, issue a call.
        // (this value requires decoding since it is ABI-encoded but means:  "int(32)").
        TestEnvironment callEnv = new TestEnvironment("Return value : 0500000020");
        AvmCLI.testingMain(callEnv, new String[] {"call", dappAddress, "--method", "getAddressLength", "--args", "-A", dappAddress});
        Assert.assertTrue(callEnv.didScrapeString);
    }

    @Test
    public void callSimpleStackDemo() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(SimpleStackDemo.class);
        File temp = this.folder.newFile();
        Helpers.writeBytesToFile(jar, temp.getAbsolutePath());
        
        // Create the testing environment to look for the successful deployment.
        TestEnvironment deployEnv = new TestEnvironment("Result status: SUCCESS");
        AvmCLI.testingMain(deployEnv, new String[] {"deploy", temp.getAbsolutePath()});
        Assert.assertTrue(deployEnv.didScrapeString);
        String dappAddress = deployEnv.capturedAddress;
        Assert.assertNotNull(dappAddress);
        
        // Note that we are running this in-process, so we can't scoop the stdout.  Therefore, we just verify it was correct.
        // (this is only here until the other command-line is working).
        TestEnvironment callEnv = new TestEnvironment("Return value : void");
        AvmCLI.testingMain(callEnv, new String[] {"call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test1"});
        Assert.assertTrue(callEnv.didScrapeString);
        AvmCLI.testingMain(callEnv, new String[] {"call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test2"});
        Assert.assertTrue(callEnv.didScrapeString);
        AvmCLI.testingMain(callEnv, new String[] {"call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test3"});
        Assert.assertTrue(callEnv.didScrapeString);
    }

    @Test
    public void parseFailOnExtraArgs() {
        String[] args = new String[]{"call", "0xFFFF", "--method", "method", "--args", "-A", "0x1122334455667788112233445566778811223344556677881122334455667788", "bogus_arg"};
        // Make sure that we see the complaint about the unknown arg.
        String message = null;
        try {
            ArgumentParser.parseArgs(args);
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        }
        Assert.assertEquals("Unknown argument: bogus_arg", message);
    }

    @Test
    public void multiSimpleStackDemo() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(SimpleStackDemo.class);
        File temp = this.folder.newFile();
        Helpers.writeBytesToFile(jar, temp.getAbsolutePath());
        
        // Create the testing environment to look for the successful deployment.
        TestEnvironment deployEnv = new TestEnvironment("Running block with 1 transactions");
        AvmCLI.testingMain(deployEnv, new String[] {
                "deploy", temp.getAbsolutePath(),
        });
        Assert.assertTrue(deployEnv.didScrapeString);
        String dappAddress = deployEnv.capturedAddress;
        Assert.assertNotNull(dappAddress);
        
        // Note that we are running this in-process, so we can't scoop the stdout.  Therefore, we just verify it was correct.
        // (this is only here until the other command-line is working).
        TestEnvironment callEnv = new TestEnvironment("Running block with 2 transactions");
        AvmCLI.testingMain(callEnv, new String[] {
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test1_1",
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test1_2",
        });
        Assert.assertTrue(callEnv.didScrapeString);
        callEnv = new TestEnvironment("Running block with 1 transactions");
        AvmCLI.testingMain(callEnv, new String[] {
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test2_1",
        });
        Assert.assertTrue(callEnv.didScrapeString);
        callEnv = new TestEnvironment("Running block with 3 transactions");
        AvmCLI.testingMain(callEnv, new String[] {
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test3_1",
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test3_2",
                "call", dappAddress, "--method", "addNewTuple", "--args", "-T", "test3_3",
        });
        Assert.assertTrue(callEnv.didScrapeString);
    }

    @Test
    public void parseFailOnInvalidBatch() {
        String[] args = new String[] {
                "call", "0xFFFF", "--method", "addNewTuple", "--args", "-T", "test1_1",
                "explore", "0xFFFF",
        };
        // Make sure that we see the complaint about the use of batching on explore.
        String message = null;
        try {
            ArgumentParser.parseArgs(args);
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        }
        Assert.assertEquals("EXPLORE cannot be in a batch of commands", message);
        args = new String[] {
                "open", "--address", "0xFFFF",
                "call", "0xFFFF", "--method", "addNewTuple", "--args", "-T", "test1_1",
        };
        // Make sure that we see the complaint about the use of batching on open.
        message = null;
        try {
            ArgumentParser.parseArgs(args);
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        }
        Assert.assertEquals("OPEN cannot be in a batch of commands", message);
    }


    private static class TestEnvironment implements IEnvironment {
        public final String requiredScrape;
        public String capturedAddress;
        public boolean didScrapeString;

        public TestEnvironment(String requiredScrape) {
            this.requiredScrape = requiredScrape;
        }

        @Override
        public RuntimeException fail(String message) {
            throw new RuntimeException(message);
        }
        @Override
        public void noteRelevantAddress(String address) {
            this.capturedAddress = address;
        }
        @Override
        public void logLine(String line) {
            if (null != this.requiredScrape) {
                if (line.contains(this.requiredScrape)) {
                    this.didScrapeString = true;
                }
            }
        }

        @Override
        public void dumpThrowable(Throwable throwable) {

        }
    }
}
