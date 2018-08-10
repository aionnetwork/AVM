package org.aion.avm.core;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.Assert;


/**
 * Contains the state of DApps currently running within the current logical thread (DApps calling DApps) to ensure that we can properly manage
 * the state when a call back into one of these is made (since reentrant calls are permitted and must inherit the state the DApp was left in).
 * NOTE:  This is only intended to be manipulated within a single callstack.  Sharing across unrelated call stacks will cause undefined behaviour.
 */
public class ReentrantDAppStack {
    private final Deque<ReentrantState> stack = new ArrayDeque<>();

    /**
     * Pushes the given state onto the stack.  Note that state will temporarily shadow any other states on the stack with the same address.
     * 
     * @param state The new state to push.
     */
    public void pushState(ReentrantState state) {
        Assert.assertNotNull(state);
        this.stack.push(state);
    }

    /**
     * Searches the stack (starting with the top) for a state with the given address, returning it (but not modifying the state of the stack)
     * if it is found.
     * 
     * @param address The address of the state we wish to find.
     * @return The first state found with the given address.
     */
    public ReentrantState tryShareState(byte[] address) {
        Assert.assertNotNull(address);
        ReentrantState foundState = null;
        for (ReentrantState state : this.stack) {
            if (Arrays.equals(state.address, address)) {
                foundState = state;
                break;
            }
        }
        return foundState;
    }

    /**
     * Pops the top state off the stack and returns it.  Returns null if the stack is empty.
     * 
     * @return The state which was previously on top of the stack (null if empty).
     */
    public ReentrantState popState() {
        return (this.stack.isEmpty())
                ? null
                : this.stack.pop();
    }


    public static class ReentrantState {
        public final byte[] address;
        public final LoadedDApp dApp;
        private ContractEnvironmentState environment;
        
        public ReentrantState(byte[] address, LoadedDApp dApp, ContractEnvironmentState environment) {
            this.address = address;
            this.dApp = dApp;
            this.environment = environment;
        }
        
        public ContractEnvironmentState getEnvironment() {
            return this.environment;
        }
        
        public void updateEnvironment(int nextHashCode) {
            this.environment = new ContractEnvironmentState(nextHashCode, this.environment.nextInstanceId);
        }
    }
}
