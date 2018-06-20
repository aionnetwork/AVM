package org.aion.avm.api;


/**
 * The concept of an "event" within Solidity and EVM, in general, appears to just be a logging system.
 * For the purposes of this test, we will just create an interface which maps to the original testWallet.sol event definitions.
 * This is also great for our tests since we can use the event interface to observe when certain actions are taken by the internals.
 * In our actual environment, we still need to determine what kind of event concept is required.
 * 
 * TODO:  Refactor this into a general solution as part of issue-103.
 */
public interface IEventLogger {
    public void revoke();
    public void ownerChanged();
    public void ownerAdded();
    public void ownerRemoved();
    public void requirementChanged();
    public void deposit();
    public void transactionUnderLimit();
    public void confirmationNeeded();

    public default void avm_revoke() { revoke(); }
    public default void avm_ownerChanged() { ownerChanged(); }
    public default void avm_ownerAdded() { ownerAdded(); }
    public default void avm_ownerRemoved() { ownerRemoved(); }
    public default void avm_requirementChanged() { requirementChanged(); }
    public default void avm_deposit() { deposit(); }
    public default void avm_transactionUnderLimit() { transactionUnderLimit(); }
    public default void avm_confirmationNeeded() { confirmationNeeded(); }
}
