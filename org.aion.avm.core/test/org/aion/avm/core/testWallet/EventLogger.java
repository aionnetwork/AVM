package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;


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

    public static void confirmation(IBlockchainRuntime logSink, Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        logSink.log(kConfirmation.getBytes(), data);
    }

    public static void revoke(IBlockchainRuntime logSink, Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        logSink.log(kRevoke.getBytes(), data);
    }

    public static void ownerChanged(IBlockchainRuntime logSink, Address oldOwner, Address newOwner) {
        byte[] oldBytes = oldOwner.unwrap();
        byte[] newBytes = newOwner.unwrap();
        byte[] data = ByteArrayHelpers.concatenate(oldBytes, newBytes);
        logSink.log(kOwnerChanged.getBytes(), data);
    }

    public static void ownerAdded(IBlockchainRuntime logSink, Address newOwner) {
        byte[] data = newOwner.unwrap();
        logSink.log(kOwnerAdded.getBytes(), data);
    }

    public static void ownerRemoved(IBlockchainRuntime logSink, Address oldOwner) {
        byte[] data = oldOwner.unwrap();
        logSink.log(kOwnerRemoved.getBytes(), data);
    }

    public static void requirementChanged(IBlockchainRuntime logSink, int newRequired) {
        byte[] data = ByteArrayHelpers.encodeInt(newRequired);
        logSink.log(kRequirementChanged.getBytes(), data);
    }

    public static void deposit(IBlockchainRuntime logSink, Address from, long value) {
        byte[] fromBytes = from.unwrap();
        byte[] data = ByteArrayHelpers.appendLong(fromBytes, value);
        logSink.log(kDeposit.getBytes(), data);
    }

    public static void singleTransact(IBlockchainRuntime logSink, Address owner, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] ownerValue = ByteArrayHelpers.appendLong(ownerBytes, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        logSink.log(kSingleTransact.getBytes(), finalData);
    }

    public static void multiTransact(IBlockchainRuntime logSink, Address owner, Operation operation, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] ownerOperation = ByteArrayHelpers.concatenate(ownerBytes, operationBytes);
        byte[] ownerOperationValue = ByteArrayHelpers.appendLong(ownerOperation, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerOperationValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        logSink.log(kMultiTransact.getBytes(), finalData);
    }

    public static void confirmationNeeded(IBlockchainRuntime logSink, Operation operation, Address initiator, long value, Address to, byte[] data) {
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] initBytes = initiator.unwrap();
        byte[] operationInit = ByteArrayHelpers.concatenate(operationBytes, initBytes);
        byte[] operationInitValue = ByteArrayHelpers.appendLong(operationInit, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(operationInitValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        logSink.log(kConfirmationNeeded.getBytes(), finalData);
    }
}
