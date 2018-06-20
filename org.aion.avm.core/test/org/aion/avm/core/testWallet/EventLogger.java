package org.aion.avm.core.testWallet;

import org.aion.avm.api.BlockchainRuntime;


/**
 * For now, we just use the method name as the indexable argument to the log, with no payload.
 * In the future, we may change how we expose event logging.
 */
public class EventLogger {
    public static final String kConfirmation = "confirmation";
    public static final String kRevoke = "revoke";
    public static final String kOwnerChanged = "ownerChanged";
    public static final String kOwnerAdded = "ownerAdded";
    public static final String kOwnerRemoved = "ownerRemoved";
    public static final String kRequirementChanged = "requirementChanged";
    public static final String kDeposit = "deposit";
    public static final String kSingleTransact = "singleTransact";
    public static final String kMultiTransact = "multiTransact";
    public static final String kConfirmationNeeded = "confirmationNeeded";

    private final BlockchainRuntime logSink;

    public EventLogger(BlockchainRuntime logSink) {
        this.logSink = logSink;
    }

    public void confirmation() {
        this.logSink.log(kConfirmation.getBytes(), null);
    }

    public void revoke() {
        this.logSink.log(kRevoke.getBytes(), null);
    }

    public void ownerChanged() {
        this.logSink.log(kOwnerChanged.getBytes(), null);
    }

    public void ownerAdded() {
        this.logSink.log(kOwnerAdded.getBytes(), null);
    }

    public void ownerRemoved() {
        this.logSink.log(kOwnerRemoved.getBytes(), null);
    }

    public void requirementChanged() {
        this.logSink.log(kRequirementChanged.getBytes(), null);
    }

    public void deposit() {
        this.logSink.log(kDeposit.getBytes(), null);
    }

    public void singleTransact() {
        this.logSink.log(kSingleTransact.getBytes(), null);
    }

    public void multiTransact() {
        this.logSink.log(kMultiTransact.getBytes(), null);
    }

    public void confirmationNeeded() {
        this.logSink.log(kConfirmationNeeded.getBytes(), null);
    }
}
