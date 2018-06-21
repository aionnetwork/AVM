package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
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

    public void confirmation(Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        this.logSink.log(kConfirmation.getBytes(), data);
    }

    public void revoke(Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        this.logSink.log(kRevoke.getBytes(), data);
    }

    public void ownerChanged(Address oldOwner, Address newOwner) {
        byte[] oldBytes = oldOwner.unwrap();
        byte[] newBytes = newOwner.unwrap();
        byte[] data = ByteArrayHelpers.concatenate(oldBytes, newBytes);
        this.logSink.log(kOwnerChanged.getBytes(), data);
    }

    public void ownerAdded(Address newOwner) {
        byte[] data = newOwner.unwrap();
        this.logSink.log(kOwnerAdded.getBytes(), data);
    }

    public void ownerRemoved(Address oldOwner) {
        byte[] data = oldOwner.unwrap();
        this.logSink.log(kOwnerRemoved.getBytes(), data);
    }

    public void requirementChanged(int newRequired) {
        byte[] data = ByteArrayHelpers.serializeInt(newRequired);
        this.logSink.log(kRequirementChanged.getBytes(), data);
    }

    public void deposit(Address from, long value) {
        byte[] fromBytes = from.unwrap();
        byte[] data = ByteArrayHelpers.appendLong(fromBytes, value);
        this.logSink.log(kDeposit.getBytes(), data);
    }

    public void singleTransact(Address owner, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] ownerValue = ByteArrayHelpers.appendLong(ownerBytes, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        this.logSink.log(kSingleTransact.getBytes(), finalData);
    }

    public void multiTransact(Address owner, Operation operation, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] ownerOperation = ByteArrayHelpers.concatenate(ownerBytes, operationBytes);
        byte[] ownerOperationValue = ByteArrayHelpers.appendLong(ownerOperation, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerOperationValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        this.logSink.log(kMultiTransact.getBytes(), finalData);
    }

    public void confirmationNeeded(Operation operation, Address initiator, long value, Address to, byte[] data) {
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] initBytes = initiator.unwrap();
        byte[] operationInit = ByteArrayHelpers.concatenate(operationBytes, initBytes);
        byte[] operationInitValue = ByteArrayHelpers.appendLong(operationInit, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(operationInitValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        this.logSink.log(kConfirmationNeeded.getBytes(), finalData);
    }
}
