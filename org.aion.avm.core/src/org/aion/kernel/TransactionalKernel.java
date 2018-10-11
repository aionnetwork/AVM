package org.aion.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.aion.avm.core.util.ByteArrayWrapper;


/**
 * A transactional implementation of the KernelInterface which only writes back to its "parent" on commit.
 * 
 * This uses a relatively extensible pattern for its implementation, building a transaction log rather than its own actual direct implementation.
 * This means that changes to the interface should mostly just translate into a new kind of transaction log entry, in this implementation.
 * Special attention needs to be paid to read-and-write operations (such as adjustBalance()) and anything involving deletes.
 */
public class TransactionalKernel implements KernelInterface {
    private final KernelInterface parent;
    private final KernelInterfaceImpl writeCache;
    private final List<Consumer<KernelInterface>> writeLog;
    private final Set<ByteArrayWrapper> deletedAccountProjection;
    private final Set<ByteArrayWrapper> cachedAccountBalances;

    public TransactionalKernel(KernelInterface parent) {
        this.parent = parent;
        this.writeCache = new KernelInterfaceImpl();
        this.writeLog = new ArrayList<>();
        this.deletedAccountProjection = new HashSet<>();
        this.cachedAccountBalances = new HashSet<>();
    }

    /**
     * Causes the changes enqueued in the receiver to be written back to the parent.
     * After this call, uses of the receiver are undefined.
     */
    public void commit() {
        for (Consumer<KernelInterface> mutation : this.writeLog) {
            mutation.accept(this.parent);
        }
    }

    @Override
    public void putCode(byte[] address, byte[] code) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.putCode(address, code);
        };
        write.accept(writeCache);
        writeLog.add(write);
    }

    @Override
    public byte[] getCode(byte[] address) {
        byte[] result = null;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            result = this.writeCache.getCode(address);
            if (null == result) {
                result = this.parent.getCode(address);
            }
        }
        return result;
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.putStorage(address, key, value);
        };
        write.accept(writeCache);
        writeLog.add(write);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        byte[] result = null;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            result = this.writeCache.getStorage(address, key);
            if (null == result) {
                result = this.parent.getStorage(address, key);
            }
        }
        return result;
    }

    @Override
    public void deleteAccount(byte[] address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.deleteAccount(address);
        };
        write.accept(writeCache);
        writeLog.add(write);
        this.deletedAccountProjection.add(new ByteArrayWrapper(address));
        this.cachedAccountBalances.remove(new ByteArrayWrapper(address));
    }

    @Override
    public long getBalance(byte[] address) {
        long result = 0L;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            result = this.writeCache.getBalance(address);
            if (0 == result) {
                result = this.parent.getBalance(address);
            }
        }
        return result;
    }

    @Override
    public void adjustBalance(byte[] address, long delta) {
        // This is a read-then-write operation so we need to make sure that there is an entry in our cache, first, before we can apply the mutation.
        if (!this.cachedAccountBalances.contains(new ByteArrayWrapper(address))) {
            // We can only re-cache this if we didn't already delete it.
            // If it was deleted, we need to fake the lazy creation and start it at zero.
            if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
                long balance = this.parent.getBalance(address);
                this.writeCache.adjustBalance(address, balance);
            } else {
                this.writeCache.adjustBalance(address, 0L);
            }
            this.cachedAccountBalances.add(new ByteArrayWrapper(address));
        }
        // If this was previously deleted, fake the lazy re-creation.
        this.deletedAccountProjection.remove(new ByteArrayWrapper(address));
        
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.adjustBalance(address, delta);
        };
        write.accept(writeCache);
        writeLog.add(write);
    }

    @Override
    public long getNonce(byte[] address) {
        long result = 0L;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            result = this.writeCache.getNonce(address);
            if (0 == result) {
                result = this.parent.getNonce(address);
            }
        }
        return result;
    }

    @Override
    public void incrementNonce(byte[] address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.incrementNonce(address);
        };
        write.accept(writeCache);
        writeLog.add(write);
    }

}
