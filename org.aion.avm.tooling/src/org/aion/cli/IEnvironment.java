package org.aion.cli;


/**
 * Describes the environment where the CLI entry-point is being invoked.
 * This is purely to allow top-level testing of the static entry-point, itself (including command line parsing).
 */
public interface IEnvironment {
    /**
     * Called when something fatal has happened which should terminate the CLI.
     * 
     * @param message An optional description of the fatal error.
     * @return This never actually returns but states it will return RuntimeException so the caller can throw, to communicate this to the compiler.
     */
    public RuntimeException fail(String message);

    /**
     * Called by various routines which create or reference an address which couldn't be deterministically determined by the caller.
     * This is so that tests which want to create a random address and then interact with it can be told the address without scraping output.
     * 
     * @param address The relevant address, as a hex string.
     */
    public void noteRelevantAddress(String address);

    /**
     * Called to log something to STDOUT.
     * 
     * @param line A line of output (could be multiple lines but can safely be written with a newline at the end).
     */
    public void logLine(String line);

    /**
     * Dump a throwable to console.
     *
     * @param throwable the throwable object
     */
    public void dumpThrowable(Throwable throwable);
}
