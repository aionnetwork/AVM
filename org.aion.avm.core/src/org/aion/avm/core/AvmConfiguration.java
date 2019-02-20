package org.aion.avm.core;


/**
 * A class to describe how to configure an AVM instance, when requesting that it be created.
 * The overall strategy with this class is just to make it into a plain-old-data struct, with each field set to a "default" value
 * which can be overwritten by a caller.
 */
public class AvmConfiguration {
    /**
     * Decides if debug data and names need to be preserved during deployment transformation.
     * Note that this must be set to false as a requirement of the security model but that prohibits local debugging.  Hence, it
     * should only be enabled for embedded use-cases during development, never in actual deployment in a node.
     * Some security violations will change into fatal assertion errors, instead of being rejected, if this is enabled.
     */
    public boolean preserveDebuggability;
    /**
     * If set to true, will log details of uncaught contract exceptions to stderr.
     * Enabling this is useful for local debugging cases.
     */
    public boolean enableVerboseContractErrors;

    public AvmConfiguration() {
        // By default, we MUST reparent user code and discard debug data!  This is part of the security model so it should only be enabled to enable local contract debugging.
        this.preserveDebuggability = false;
        // By default, none of our verbose options are enabled.
        this.enableVerboseContractErrors = false;
    }
}
