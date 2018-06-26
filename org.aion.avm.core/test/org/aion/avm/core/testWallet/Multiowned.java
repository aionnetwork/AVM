package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;


/**
 * Note that the original implementation has a very "C++ feel", with the way that a lot of independent mappings are used
 * to represent different views on the data and the underlying array of Addresses is flat and reused, with a fixed limit.
 * In our implementation, we will use common Java collections to accomplish much of this (we might add a limit, later,
 * but the core implementation doesn't require it).
 */
public class Multiowned {
    // The owners.
    private static AionSet<Address> owners;
    private static AionList<Address> ownersByJoinOrder;
    // The operations which we currently know about.
    private static AionMap<Operation, PendingState> ongoingOperations;
    // The number of owners which must confirm the operation before it is run.
    private static int numberRequired;

    public static void init(Address sender, Address[] requestedOwners, int votesRequiredPerOperation) {
        Multiowned.owners = new AionSet<>();
        Multiowned.ownersByJoinOrder = new AionList<>();
        Multiowned.owners.add(sender);
        Multiowned.ownersByJoinOrder.add(sender);
        for (Address owner : requestedOwners) {
            Multiowned.owners.add(owner);
            Multiowned.ownersByJoinOrder.add(owner);
        }
        Multiowned.ongoingOperations = new AionMap<>();
        Multiowned.numberRequired = votesRequiredPerOperation;
    }

    // PUBLIC INTERFACE
    public static void revoke() {
        Address sender = BlockchainRuntime.getSender();
        // Make sure that they are an owner.
        if (Multiowned.owners.contains(sender)) {
            // See if we know about this operation.
            Operation operation = Operation.fromMessage();
            PendingState state = Multiowned.ongoingOperations.get(operation);
            if (null != state) {
                // Remove them from the confirmed set.
                state.confirmedOwners.remove(sender);
                EventLogger.revoke(sender, operation);
            }
        }
    }

    // PUBLIC INTERFACE
    public static void changeOwner(Address from, Address to) {
        // (modifier)
        onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        // Make sure that the to isn't already an owner and the from is.
        if (!isOwner(to) && isOwner(from)) {
            // Remove the one owner and add the other.
            Multiowned.owners.remove(from);
            Multiowned.owners.add(to);
            Multiowned.ownersByJoinOrder.remove(from);
            Multiowned.ownersByJoinOrder.add(to);
            
            // Pending states will now be inconsistent so clear them.
            Multiowned.ongoingOperations.clear();
            EventLogger.ownerChanged(from, to);
        }
    }

    // PUBLIC INTERFACE
    public static void addOwner(Address owner) {
        // (modifier)
        onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        // Make sure that this owner isn't already in the set.
        if (!isOwner(owner)) {
            // Remove the one owner and add the other.
            Multiowned.owners.add(owner);
            Multiowned.ownersByJoinOrder.add(owner);
            
            // Pending states will now be inconsistent so clear them.
            Multiowned.ongoingOperations.clear();
            EventLogger.ownerAdded(owner);
        }
    }

    // PUBLIC INTERFACE
    public static void removeOwner(Address owner) {
        // (modifier)
        onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        // Make sure that this owner is  in the set.
        if (isOwner(owner)) {
            // Remove the one owner and add the other.
            Multiowned.owners.remove(owner);
            Multiowned.ownersByJoinOrder.remove(owner);
            
            // Pending states will now be inconsistent so clear them.
            Multiowned.ongoingOperations.clear();
            EventLogger.ownerRemoved(owner);
            
            // (Note that it is possible that we have now removed so many owners that numberRequired no longer exist - the Solidity example didn't avoid this)
        }
    }

    // PUBLIC INTERFACE
    public static void changeRequirement(int newRequired) {
        // (modifier)
        onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        // Change the requirement.
        Multiowned.numberRequired = newRequired;
        
        // Pending states will now be inconsistent so clear them.
        Multiowned.ongoingOperations.clear();
        EventLogger.requirementChanged(newRequired);
    }

    // PUBLIC INTERFACE
    // Gets the 0-indexed owner, in order of when they became owners.
    // This is just supported because the Solidity example had it but I am not sure how it would be used.
    public static Address getOwner(int ownerIndex) {
        Address result = null;
        
        if (ownerIndex < Multiowned.ownersByJoinOrder.size()) {
            result = Multiowned.ownersByJoinOrder.get(ownerIndex);
        }
        return result;
    }

    // This is just supported because the Solidity example had it but it wasn't marked "external" and had no callers.
    public static boolean hasConfirmed(Operation operation, Address owner) {
        boolean didConfirm = false;
        
        // Make sure they are an owner.
        if (Multiowned.owners.contains(owner)) {
            // Check to see if this is a real operation.
            PendingState pending = Multiowned.ongoingOperations.get(operation);
            if (null != pending) {
                didConfirm = pending.confirmedOwners.contains(owner);
            }
        }
        return didConfirm;
    }


    // MODIFIER - public for composition
    public static void onlyOwner(Address sender) {
        if (!isOwner(sender)) {
            // We want to bail out with a failed "require".
            throw new RequireFailedException();
        }
    }

    /**
     * This is called in the case where the activity requires consensus of some owners.
     * This will confirm the operation for the sender but only return if they were the last required
     * to confirm (also, erasing the PendingState record, in that case).  RequireFailedException is
     * thrown in other cases.
     * 
     * @param sender The current message sender.
     * @param operation The operation hash.
     */
    // MODIFIER - public for composition
    public static void onlyManyOwners(Address sender, Operation operation) {
        boolean shouldRunRoutine = confirmAndCheck(sender, operation);
        if (!shouldRunRoutine) {
            // We want to bail out with a failed "require".
            throw new RequireFailedException();
        }
    }

    private static boolean isOwner(Address address) {
        return Multiowned.owners.contains(address);
    }

    private static boolean confirmAndCheck(Address sender, Operation operation) {
        boolean shouldRunRoutine = false;
        
        // They must be an owner.
        if (Multiowned.owners.contains(sender)) {
            // We lazily create these the first time they are requested so see if there is one.
            PendingState state = Multiowned.ongoingOperations.get(operation);
            if (null == state) {
                state = new PendingState();
                Multiowned.ongoingOperations.put(operation, state);
            }
            
            // Add us to the set of those who have confirmed.
            EventLogger.confirmation(sender, operation);
            state.confirmedOwners.add(sender);
            
            // If we were the last required, return true and clean up.
            if (state.confirmedOwners.size() == Multiowned.numberRequired) {
                shouldRunRoutine = true;
                Multiowned.ongoingOperations.remove(operation);
            }
        }
        return shouldRunRoutine;
    }


    // (this is public just for easy referencing in the Deployer's loader logic).
    public static class PendingState {
        public final AionSet<Address> confirmedOwners = new AionSet<>();
    }
}
