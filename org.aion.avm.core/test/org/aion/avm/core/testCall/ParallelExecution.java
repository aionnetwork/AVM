package org.aion.avm.core.testCall;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the world initState.
 */
class State {
    private State parent;

    public State(State parent) {
        this.parent = parent;
    }

    public State track() {
        return new State(this);
    }

    public void commit() {
    }

    public void rollback() {
    }
}

/**
 * Represents an internal transaction.
 */
class InternalTransaction extends Transaction {
    private Transaction parent;

    public InternalTransaction(Type type, byte[] from, byte[] to, byte[] value, byte[] data, long energyLimit, Transaction parent) {
        super(type, from, to, value, data, energyLimit);
        this.parent = parent;
    }
}

/**
 * Parallel execution PoC.
 */
public class ParallelExecution {
    private static final Logger logger = LoggerFactory.getLogger(ParallelExecution.class);

    private static final int NUM_THREADS = 4; // TODO: tune this parameter
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
    private static final AtomicInteger counter = new AtomicInteger(0);

    private List<Transaction> transactions;
    private State initState;
    private int startFrom;

    public ParallelExecution(List<Transaction> transactions, State initState) {
        this.transactions = transactions;
        this.initState = initState;
        this.startFrom = 0;
    }

    /**
     * Executes all transaction in parallel.
     *
     * @return
     */
    public List<TransactionResult> execute() {
        State state = initState;
        List<TransactionResult> results = new ArrayList<>();

        while (startFrom < transactions.size()) {
            // set up workers
            List<Callable<TransactionResult>> callables = new ArrayList<>();
            for (int i = 0; i < NUM_THREADS && startFrom + i < transactions.size(); i++) {
                State track = state.track();
                callables.add(new Worker(transactions.get(startFrom + i), track));
            }

            try {
                // invoke all
                List<Future<TransactionResult>> futures = threadPool.invokeAll(callables);

                // detect conflicts
                Set<String> accounts = new HashSet<>();
                for (Future<TransactionResult> f : futures) {
                    Transaction tx = transactions.get(startFrom);
                    TransactionResult r = f.get();

                    Set<String> set = new HashSet<>();
                    set.add(Helpers.toHexString(tx.getFrom()));
                    set.add(Helpers.toHexString(tx.getTo()));
                    for (InternalTransaction it : r.internalTransactions) {
                        set.add(Helpers.toHexString(it.getFrom()));
                        set.add(Helpers.toHexString(it.getTo()));
                    }

                    if (set.stream().anyMatch(k -> accounts.contains(k))) {
                        break;
                    }
                    r.track.commit();
                    startFrom++;
                    accounts.addAll(set);
                }
            } catch (InterruptedException e) {
                logger.info("Interrupted!");
                return null;
            } catch (ExecutionException e) {
                Assert.unexpected(e);
            }
        }

        return results;
    }


    /**
     * Callable worker for transaction execution.
     */
    private static class Worker implements Callable<TransactionResult> {

        private Transaction tx;
        private State track;

        public Worker(Transaction tx, State track) {
            this.tx = tx;
            this.track = track;
        }

        @Override
        public TransactionResult call() throws Exception {
            TransactionResult result = new TransactionResult();
            result.track = track;
            result.internalTransactions = new ArrayList<>();

            // record stats
            logger.debug("Transaction: " + tx);
            counter.incrementAndGet();

            // Execute the transaction
            SimpleAvm avm = new SimpleAvm(tx.getEnergyLimit(), Contract.class);
            avm.attachBlockchainRuntime(new SimpleRuntime(tx.getFrom(), tx.getTo(), tx.getEnergyLimit(), tx.getData()) {
                // TODO: runtime should be based on the state
                @Override
                public ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray payload, long energyToSend) {
                    InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CALL, tx.getTo(), targetAddress.unwrap(), value.getUnderlying(), payload.getUnderlying(), energyToSend, tx);
                    result.internalTransactions.add(internalTx);
                    logger.debug("Internal transaction: " + internalTx);

                    return new ByteArray(new byte[0]);
                }
            });
            try {
                Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(Contract.class.getName());
                ByteArray ret = (ByteArray) clazz.getMethod(UserClassMappingVisitor.mapMethodName("main")).invoke(null);
                result.returnData = ret.getUnderlying();
            } catch (Exception e) {
                // revert changes on failure
                result.track.rollback();
                result.internalTransactions.clear();

                logger.info("Transaction failed", e);
            }

            return result;
        }
    }


    /**
     * Represents the receipt of transaction.
     */
    private static class TransactionResult {

        byte[] returnData;

        List<InternalTransaction> internalTransactions;

        State track;
    }

    //============
    // TEST
    //============

    private static byte[] address(int n) {
        byte[] arr = new byte[32];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) n;
        }
        return arr;
    }

    public static void main(String[] args) {
        int numAccounts = 32;
        int numTransactions = 100;

        List<Transaction> transactions = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numTransactions; i++) {
            int from = r.nextInt(numAccounts);
            int to = r.nextInt(numAccounts);
            int callee = r.nextInt(numAccounts);

            Transaction tx = new Transaction(Transaction.Type.CALL, address(from), address(to), new byte[0], address(callee), 1000000);
            transactions.add(tx);
        }

        ParallelExecution exec = new ParallelExecution(transactions, new State(null));
        exec.execute();

        logger.info("Number of executions: " + counter.get());
        threadPool.shutdown();
    }

    public static void main1(String[] args) {
        Transaction tx1 = new Transaction(Transaction.Type.CALL, address(1), address(2), new byte[0], address(3), 1000000);
        Transaction tx2 = new Transaction(Transaction.Type.CALL, address(3), address(4), new byte[0], address(1), 1000000);
        Transaction tx3 = new Transaction(Transaction.Type.CALL, address(3), address(5), new byte[0], new byte[0], 1000000);

        ParallelExecution exec = new ParallelExecution(List.of(tx1, tx2, tx3), new State(null));
        exec.execute();

        logger.info("Number of executions: " + counter.get());
        threadPool.shutdown();
    }
}
