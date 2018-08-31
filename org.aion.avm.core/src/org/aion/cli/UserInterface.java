package org.aion.cli;

public interface UserInterface {

    /**
     * Deploy Dapp
     *
     * @param env          The execution environment
     * @param storagePath  The path to the persistent storage
     * @param jarPath      The path to the contract jar
     * @param sender       The address of the sender
     * @param energyLimit  The energy cap
     */
    void deploy(IEnvironment env, String storagePath, String jarPath, byte[] sender, long energyLimit);

    /**
     * Execute transaction
     *
     * @param env          The execution environment
     * @param storagePath  The path to the persistent storage
     * @param contract     The address of the contract
     * @param sender       The address of the sender
     * @param method       The name of the method
     * @param arg          The argument of the method
     * @param energyLimit  The energy cap
     */
    void call(IEnvironment env, String storagePath, byte[] contract, byte[] sender, String method, String[] arg, long energyLimit);

    /**
     * Open new account
     *
     * @param env          The execution environment
     * @param storagePath  The path to the persistent storage
     * @param toOpen       The address of the new account
     */
    void openAccount(IEnvironment env, String storagePath, byte[] toOpen);

    /**
     * Print the object graph for a given DApp.
     *
     * @param env          The execution environment
     * @param storagePath  The path to the persistent storage
     * @param dappAddress  The address of the DApp to explore.
     */
    void exploreStorage(IEnvironment env, String storagePath, byte[] dappAddress);

}
