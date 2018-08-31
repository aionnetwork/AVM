package org.aion.cli;

import org.junit.Assert;
import org.junit.Test;


public class AvmCLIIntegrationTest {
    @Test
    public void usage() {
        TestEnvironment env = new TestEnvironment("Usage: AvmCLI [options] [command] [command options]");
        AvmCLI.testingMain(env, new String[0]);
        Assert.assertTrue(env.didScrapeString);
        Assert.assertNull(env.capturedAddress);
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
    }
}
