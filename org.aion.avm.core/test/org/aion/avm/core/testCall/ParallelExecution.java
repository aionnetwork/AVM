package org.aion.avm.core.testCall;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.types.InternalTransaction;
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
 * Parallel execution PoC.
 */
public class ParallelExecution {
    private static final Logger logger = LoggerFactory.getLogger(ParallelExecution.class);

    private static final int NUM_THREADS = 4;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    private List<Transaction> transactions;
    private State initState;
    private int parallelism;
    private int startFrom;

    public ParallelExecution(List<Transaction> transactions, State initState, int parallelism) {
        this.transactions = transactions;
        this.initState = initState;
        this.parallelism = parallelism;
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
            for (int i = 0; i < parallelism && startFrom + i < transactions.size(); i++) {
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
            logger.debug("Transaction: " + tx);

            TransactionResult result = new TransactionResult();
            result.track = track;
            result.internalTransactions = new ArrayList<>();

            // Execute the transaction
            SimpleAvm avm = new SimpleAvm(tx.getEnergyLimit(), Contract.class);
            avm.attachBlockchainRuntime(new SimpleRuntime(tx.getFrom(), tx.getTo(), tx.getEnergyLimit(), tx.getData()) {
                // TODO: runtime should be based on the state
                @Override
                public ByteArray avm_call(Address targetAddress, long value, ByteArray payload, long energyToSend) {
                    InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CALL, tx.getTo(), targetAddress.unwrap(), value, payload.getUnderlying(), energyToSend, tx.getEnergyPrice());
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
            avm.shutdown();

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

    public static void simpleCall() {
        Transaction tx1 = new Transaction(Transaction.Type.CALL, Helpers.address(1), Helpers.address(2), 0, Helpers.address(3), 1000000, 1);
        Transaction tx2 = new Transaction(Transaction.Type.CALL, Helpers.address(3), Helpers.address(4), 0, Helpers.address(1), 1000000, 1);
        Transaction tx3 = new Transaction(Transaction.Type.CALL, Helpers.address(3), Helpers.address(5), 0, new byte[0], 1000000, 1);

        ParallelExecution exec = new ParallelExecution(List.of(tx1, tx2, tx3), new State(null), NUM_THREADS);
        exec.execute();

        threadPool.shutdown();
    }

    public static void randomCall(int numAccounts, int numTransactions, int numThreads) {
        threadPool.shutdown();
        threadPool = Executors.newFixedThreadPool(numThreads);

        List<Transaction> transactions = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numTransactions; i++) {
            int from = r.nextInt(numAccounts);
            int to = r.nextInt(numAccounts);
            int callee = r.nextInt(numAccounts);

            Transaction tx = new Transaction(Transaction.Type.CALL, Helpers.address(from), Helpers.address(to), 0, Helpers.address(callee), 1000000, 1);
            transactions.add(tx);
        }

        long t1 = System.currentTimeMillis();
        ParallelExecution exec = new ParallelExecution(transactions, new State(null), numThreads);
        exec.execute();
        long t2 = System.currentTimeMillis();
        logger.info("# of accounts = {}, # of threads = {}, # of txs = {}, Time elapsed: {}ms",
                numAccounts, numTransactions, numThreads, t2 - t1);

        threadPool.shutdown();
    }

    public static void main(String[] args) {
        // simple case, for testing
        logger.info("**** SIMPLE_TEST ****");
        simpleCall();

        // scalability test
        logger.info("**** SCALABILITY_TEST ****");
        randomCall(100, 10000, NUM_THREADS); // warm up
        logger.info("----");
        randomCall(200, 1000, 1);
        randomCall(200, 1000, 2);
        randomCall(200, 1000, 4);
        randomCall(200, 1000, 8);
        logger.info("----");
        randomCall(400, 1000, 1);
        randomCall(400, 1000, 2);
        randomCall(400, 1000, 4);
        randomCall(400, 1000, 8);
        logger.info("----");
        randomCall(800, 1000, 1);
        randomCall(800, 1000, 2);
        randomCall(800, 1000, 4);
        randomCall(800, 1000, 8);
    }
}
