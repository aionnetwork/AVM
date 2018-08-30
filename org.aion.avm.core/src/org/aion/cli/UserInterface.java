package org.aion.cli;

public interface UserInterface {

    /**
     * Deploy Dapp
     *
     * @param storagePath  The path to the persistent storage
     * @param jarPath      The path to the contract jar
     * @param sender       The address of the sender
     */
    void deploy(String storagePath, String jarPath, byte[] sender);

    /**
     * Execute transaction
     *
     * @param storagePath  The path to the persistent storage
     * @param contract     The address of the contract
     * @param sender       The address of the sender
     * @param method       The name of the method
     * @param arg          The argument of the method
     */
    void call(String storagePath, byte[] contract, byte[] sender, String method, String[] arg);

    /**
     * Open new account
     *
     * @param storagePath  The path to the persistent storage
     * @param toOpen       The address of the new account
     */
    void openAccount(String storagePath, byte[] toOpen);

}
