package org.aion.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public void putCode(byte[] address, VersionedCode code) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.putCode(address, code);
        };
        write.accept(writeCache);
        writeLog.add(write);
    }

    @Override
    public VersionedCode getCode(byte[] address) {
        VersionedCode result = null;
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
    public void createAccount(byte[] address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.createAccount(address);
        };
        write.accept(writeCache);
        writeLog.add(write);
        this.deletedAccountProjection.remove(new ByteArrayWrapper(address));
        // Say that we have this cached so we don't go back to any old version in the parent (even though it is unlikely we will create over delete).
        this.cachedAccountBalances.add(new ByteArrayWrapper(address));
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
    public boolean isExists(byte[] address) {
        boolean result = false;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            result = this.writeCache.isExists(address);
            if (!result) {
                result = this.parent.isExists(address);
            }
        }
        return result;
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
            long balance = this.parent.getBalance(address);
            this.writeCache.adjustBalance(address, balance);
            this.cachedAccountBalances.add(new ByteArrayWrapper(address));
        }
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

    @Override
    public Map<ByteArrayWrapper, byte[]> getStorageEntries(byte[] address) {
        Map<ByteArrayWrapper, byte[]> workingCopy = null;
        if (!this.deletedAccountProjection.contains(new ByteArrayWrapper(address))) {
            workingCopy = new HashMap<>(this.parent.getStorageEntries(address));
            Map<ByteArrayWrapper, byte[]> overlay = this.writeCache.getStorageEntries(address);
            if (null != overlay) {
                for (Map.Entry<ByteArrayWrapper, byte[]> overlayElement : overlay.entrySet()) {
                    workingCopy.put(overlayElement.getKey(), overlayElement.getValue());
                }
            }
        }
        return workingCopy;
    }
}
