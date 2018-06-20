package org.aion.avm.core.testWallet;

import org.aion.avm.api.IEventLogger;


public class EventLogger {
    private final IEventLogger logSink;

    public EventLogger(IEventLogger logSink) {
        this.logSink = logSink;
    }

    public void revoke() {
        this.logSink.revoke();
    }

    public void ownerChanged() {
        this.logSink.ownerChanged();
    }

    public void ownerAdded() {
        this.logSink.ownerAdded();
    }

    public void ownerRemoved() {
        this.logSink.ownerRemoved();
    }

    public void requirementChanged() {
        this.logSink.requirementChanged();
    }

    public void deposit() {
        this.logSink.deposit();
    }

    public void transactionUnderLimit() {
        this.logSink.transactionUnderLimit();
    }

    public void confirmationNeeded() {
        this.logSink.confirmationNeeded();
    }
}
