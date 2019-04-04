package org.aion.avm.tooling;

import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

/**
 * To more accurately benchmark the amount of time(resource) used for each crypto method calls,
 * we will use the following mechanism for each test:
 * - Make a few calls to method warm up the system.
 * - Make several individual calls to a single method and record the time in ns.
 *      - Average these times to get a relatively accurate setup time.
 * - Make large number of calls to a single method and benchmark the result.
 *      - Remove the setup time before averaging the time used for each call.
 *
 * Note:
 * - This test suite does explicitly cover correctness.
 * - All counters are set to 1 to avoid excess time used when running test over the whole AVM.
 * - In general, time/call will decrease logarithmically.
 *
 * Please be aware that these benchmark will have some inaccuracies, if you would like a even more
 * precise benchmark consider the following:
 * - using a profiler software such as visualvm
 * - record the times in DAppExecutor class, example [surrounding: "byte[] ret = dapp.callMain();"]
 *      long st, et;
 *      st = System.nanoTime();
 *      byte[] ret = dapp.callMain();
 *      et = System.nanoTime();
 *      System.out.println(et-st + "");
 *
 * Our summary of from the benchmark:
 * - the 3 hash functions have relatively similar speed (within 5% difference on average;
 * - edverify calls are much slower, can take anywhere between 50 to 100 times longer.
 * - length of the message has negligible effect on resources used to hash, but we should still
 * consider adding a little extra fees corresponding to msg length.
 */

public class CryptoUtilMethodFeeBenchmarkTest {

    private long energyLimit = 100_000_000_000L;
    private long energyPrice = 1L;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private org.aion.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private org.aion.types.Address dappAddress;

    private TestingKernel kernel;
    private AvmImpl avm;

    private byte[] hashMessage = "benchmark testing".getBytes();
    private byte[] hashMessageLong = "long benchmark testing 0123456789abcdef 0123456789abcdef 0123456789abcdef 0123456789abcdef 0123456789abcdef".getBytes();
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final String blake2bMethodName = "callBlake2b";
    private static final String shaMethodName = "callSha";
    private static final String keccakMethodName = "callKeccak";
    private static final String edverifyMethodName = "callEdverify";

    /**
     * todo: when benchmarking, modify some of these values see more accurate results.
     * Suggest:
     * - WARMUP_COUNT >= 1000
     * - LOOP_COUNT >= 10000
     * - AVG_COUNT = 1
     * - LIST_OF_STRING_COUNT = 10000
     * - FACTOR = 100, must be a factor of LIST_OF_STRING_COUNT
     * - should focus on relative time of each method call as different systems will yield different numbers.
     */
    private static final int WARMUP_COUNT = 2;
    private static final int LOOP_COUNT = 10;
    private static final int AVG_COUNT = 1;
    private static final int LIST_OF_STRING_COUNT = 5;
    private static final int FACTOR = 1;

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClassesAndUserlib(CryptoUtilMethodFeeBenchmarkTestTargetClass.class);

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new TestingKernel();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        dappAddress = org.aion.types.Address.wrap(avm.run(this.kernel, new Transaction[] {tx})[0].get().getReturnData());
        Assert.assertNotNull(dappAddress);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    /**
     * For the basic hash api benchmarks (testBlake2b, testSha, testKeccak), they should be ran
     * individually 1 by 1. Running them in sequence WILL have some effect on the results.
     */
    @Test
    public void testBlake2b(){
        // warm up
        for (int i = 0; i < WARMUP_COUNT; i++) {
            getAvgCallTime(blake2bMethodName, AVG_COUNT, hashMessage);
        }

        // call method and record result, measure average call time
        long recordSum = 0;
        for (int i = 0; i < LOOP_COUNT; i++){
            recordSum = recordSum + getCallTime(blake2bMethodName, hashMessage, 1);
        }
        System.out.println("Average time per api call for blake2b hashing: " + recordSum/LOOP_COUNT + "ns");
    }

    @Test
    public void testSha(){
        // warm up
        for (int i = 0; i < WARMUP_COUNT; i++) {
            getAvgCallTime(shaMethodName, AVG_COUNT, hashMessage);
        }

        // call method and record result, measure average call time
        long recordSum = 0;
        for (int i = 0; i < LOOP_COUNT; i++){
            recordSum = recordSum + getCallTime(shaMethodName, hashMessage, 1);
        }
        System.out.println("Average time per api call for sha hashing: " + recordSum/LOOP_COUNT + "ns");
    }

    @Test
    public void testKeccak(){
        // warm up
        for (int i = 0; i < WARMUP_COUNT; i++) {
            getAvgCallTime(keccakMethodName, AVG_COUNT, hashMessage);
        }

        // call method and record result, measure average call time
        long recordSum = 0;
        for (int i = 0; i < LOOP_COUNT; i++){
            recordSum = recordSum + getCallTime(keccakMethodName, hashMessage, 1);
        }
        System.out.println("Average time per api call for keccak hashing: " + recordSum/LOOP_COUNT + "ns");
    }

    /**
     * Compare making looping calls from outside of dapp
     */
    @Test
    public void testAll3HashFunctionsAndCompare(){
        long blake2bSum = 0;
        long shaSum = 0;
        long keccakSum = 0;

        byte[] msg = hashMessageLong; // change this message to see variance in result

        for (int i = 0; i < LOOP_COUNT; i++){
            if (i < WARMUP_COUNT){
                getCallTime(blake2bMethodName, msg, 1);
                getCallTime(shaMethodName, msg, 1);
                getCallTime(keccakMethodName, msg, 1);
            } else {
                blake2bSum = blake2bSum + getCallTime(blake2bMethodName, msg, 1);
                shaSum = shaSum + getCallTime(shaMethodName, msg, 1);
                keccakSum = keccakSum + getCallTime(keccakMethodName, msg, 1);
            }
        }

        long blake2bTimePerCall = blake2bSum / (LOOP_COUNT - WARMUP_COUNT);
        long shaTimePerCall = shaSum / (LOOP_COUNT - WARMUP_COUNT);
        long keccakTimePerCall = keccakSum / (LOOP_COUNT - WARMUP_COUNT);

        System.out.println("blake2b avg: " + blake2bTimePerCall);
        System.out.println("sha avg: " + shaTimePerCall + ", is " + String.format("%.3f", (double)shaTimePerCall/blake2bTimePerCall) + " times speed comparing to blake2b");
        System.out.println("keccak avg: " + keccakTimePerCall + ", is " + String.format("%.3f", (double)keccakTimePerCall/blake2bTimePerCall) + " times speed comparing to blake2b");
    }

    /**
     * Compare making looping calls from within the dapp
     */
    @Test
    public void testAll3HashFunctionsAndCompare2(){
        //following values should be increased for a more accurate result
        // recommended values are 1000, 1000000
        int warmUp = WARMUP_COUNT;
        int loopCount = LOOP_COUNT;
        byte[] msg = hashMessageLong; // change this message to see variance in result

        // warm up blake2b, then make multiple calls within the dapp
        for (int i = 0; i < warmUp; i++) {
            getAvgCallTime(blake2bMethodName, AVG_COUNT, hashMessage);
        }
        long blake2bTime = getCallTime(blake2bMethodName, msg, loopCount);
        System.out.println("blake2b avg: " + blake2bTime);

        // warm up sha, then make multiple calls within the dapp
        for (int i = 0; i < warmUp; i++) {
            getAvgCallTime(shaMethodName, AVG_COUNT, hashMessage);
        }
        long shaSumTime = getCallTime(shaMethodName, msg, loopCount);
        System.out.println("sha avg: " + shaSumTime + ", is " + String.format("%.3f", (double)shaSumTime/blake2bTime) + " times speed comparing to blake2b");

        // warm up keccak, then make multiple calls within the dapp
        for (int i = 0; i < warmUp; i++) {
            getAvgCallTime(keccakMethodName, AVG_COUNT, hashMessage);
        }
        long keccakTime = getCallTime(keccakMethodName, msg, loopCount);
        System.out.println("keccak avg: " + keccakTime + ", is " + String.format("%.3f", (double)keccakTime/blake2bTime) + " times speed comparing to blake2b");
    }

    /**
     * Compare edverify to blake2b
     */
    @Test
    public void testEdverifyComparisionToBlake2bInDepth(){
        //following values should be increased for a more accurate result
        int warmUp = WARMUP_COUNT;
        int loopCount = LOOP_COUNT;
        byte[] msg = hashMessageLong; // change this message to see variance in result

        // warm up blake2b, then make multiple calls within the dapp
        for (int i = 0; i < warmUp; i++) {
            getAvgCallTime(blake2bMethodName, AVG_COUNT, hashMessage);
        }
        long blake2bTime = getCallTime(blake2bMethodName, msg, loopCount);
        System.out.println("blake2b avg: " + blake2bTime);

        // warm up blake2b, then make multiple calls within the dapp
        for (int i = 0; i < warmUp; i++) {
            getAvgCallTime(edverifyMethodName, AVG_COUNT, hashMessage);
        }
        long edverifyTime = getCallTime(edverifyMethodName, msg, loopCount);
        System.out.println("edverify avg: " + edverifyTime + ", which is " + String.format("%.3f", (double)edverifyTime/blake2bTime) + " times speed comparing to blake2b");
    }

    /**
     * benchmark how message length can affect time it takes to hash for each algorithm
     */
    @Test
    public void testBlake2bMessageLength(){
        String[] listOfMessage = generateListOfStrings(LIST_OF_STRING_COUNT);
        for (int i = 0; i < LIST_OF_STRING_COUNT; i = i + FACTOR){
            long time = getCallTime(blake2bMethodName, listOfMessage[i].getBytes(), 1);
            System.out.println("Signing using blake2b: msg length = " + i+1 + " time = " + time);
        }
    }

    @Test
    public void testShaMessageLength(){
        String[] listOfMessage = generateListOfStrings(LIST_OF_STRING_COUNT);
        for (int i = 0; i < LIST_OF_STRING_COUNT; i = i + FACTOR){
            long time = getCallTime(shaMethodName, listOfMessage[i].getBytes(), 1);
            System.out.println("Signing using sha: msg length = " + i+1 + " time = " + time);
        }
    }

    @Test
    public void testKeccakMessageLength(){
        String[] listOfMessage = generateListOfStrings(LIST_OF_STRING_COUNT);
        for (int i = 0; i < LIST_OF_STRING_COUNT; i = i + FACTOR){
            long time = getCallTime(keccakMethodName, listOfMessage[i].getBytes(), 1);
            System.out.println("Signing using keccak: msg length = " + i+1 + " time = " + time);
        }
    }


    /**
     * Helper methods for benchmark
     */

    private double getAvgCallTime(String methodName, int loopCount, byte[] message){
        long sum = 0;
        for(int i = 0; i < loopCount; i++){
            sum = sum + getCallTime(methodName, message, 1);
        }
        return sum / loopCount;
    }

    private long getCallTime(String methodName, byte[] message, int count){
        long st;
        long et;
        Transaction tx = setupTransaction(methodName,count, message);

        st = System.nanoTime();
        TransactionResult result = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        et = System.nanoTime();

        Assert.assertEquals(result.getResultCode(), Code.SUCCESS);

        return et - st;
    }

    private Transaction setupTransaction(String methodName, java.lang.Object... arguments){
        byte[] txData = ABIUtil.encodeMethodArguments(methodName, arguments);
        return Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
    }

    private String[] generateListOfStrings(int count){
        String[] listOfString = new String[count];
        for (int i = 0; i < count; i = i + FACTOR){
            listOfString[i] = generateString(i+1);
        }
        return listOfString;
    }

    private String generateString(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            double n = (Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt((int)n));
        }
        return builder.toString();
    }
}
