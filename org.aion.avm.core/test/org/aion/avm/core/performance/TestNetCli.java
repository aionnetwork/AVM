package org.aion.avm.core.performance;

import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aion.avm.userlib.abi.ABIEncoder;

public class TestNetCli {
    private static final int maxValueForParam = 10000;
    private static final int heavyLevel = 1;
    private static final int allocSize = 1000000;//(1 * (1 << 20));

    public void run(String password, String dappPath, String preminedAccount, int accountNum, int threadNum) {
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);

        System.out.println("Test starting: " + LocalDateTime.now());
        System.out.println("Starting creating accounts: " + LocalDateTime.now());
        ArrayList<String> accounts = createAccounts(accountNum, password, pool);

        System.out.println("Starting transferring: " + LocalDateTime.now());
        String amount = "1000000000000000000";
        ArrayList<String> transferReceipts = transfers(preminedAccount, password, amount, accounts, pool);

        System.out.println("Starting checking transferring receipts: " + LocalDateTime.now());
        checkReceiptsStatus(transferReceipts, pool, "Transfer");

        System.out.println("Starting deploying dapps: " + LocalDateTime.now());
        ArrayList<String> deployReceipts = deployDapps(accounts, password, dappPath, pool);

        System.out.println("Starting getting dapp addresses: " + LocalDateTime.now());
        ArrayList<String> dapps = getAllDappAddresses(deployReceipts, pool);

        System.out.println("Starting calling dapps: " + LocalDateTime.now());
        ArrayList<String> callReceipts = callDapps(accountNum, accounts, password, dapps, pool);

        System.out.println("Starting checking calling receipts: " + LocalDateTime.now());
        checkReceiptsStatus(callReceipts, pool, "Call");
        System.out.println("Test finished: " + LocalDateTime.now());

        pool.shutdown();
    }

    private ArrayList<String> createAccounts(int accountNum, String password, ExecutorService pool) {
        ArrayList<String> accounts = new ArrayList<>();

        String header = "{\"jsonrpc\":\"2.0\",\"method\":\"personal_newAccount\",\"params\":[\"";
        String footer = "\"]}";
        String data = header + password + footer;

        ArrayList<Future<String>> results = new ArrayList<>();
        for(int i = 0; i < accountNum; ++i) {
            final int n = i;
            Callable<String> createAccountTask = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("%s: %d-creating account ...\n", threadName, n);
                String response = post(data);
                assert (null != response);
                System.out.printf("%s: %d-response:%s\n", threadName, n, response);
                String receiptHash = extractReceiptHash(response);
                assert (null != receiptHash);
                return receiptHash;
            };
            results.add(pool.submit(createAccountTask));
        }

        for (Future<String> result : results) {
            try {
                accounts.add(result.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        return accounts;
    }

    private ArrayList<String> transfers(String preminedAccount, String password, String amount, ArrayList<String> accounts, ExecutorService pool) {
        ArrayList<String> transferReceipts = new ArrayList<>();
        System.out.printf("Unlocking account %s ...\n", preminedAccount);
        String responseUnlock = unlockAccount(preminedAccount, password);
        System.out.println("Response:" + responseUnlock);
        boolean isSucceed = extractUnlockResult(responseUnlock);
        assert (isSucceed);
        System.out.printf("Unlocking account %s successfully!\n", preminedAccount);

        int nonce = getNonce(preminedAccount);

        ArrayList<Future<String>> results = new ArrayList<>();
        for(int i = 0; i < accounts.size(); ++i) {
            final String recipient = accounts.get(i);
            results.add(pool.submit(new TransferTask(preminedAccount, recipient, amount, Integer.toString(nonce), i)));
            nonce++;
        }

        for (Future<String> result : results) {
            try {
                transferReceipts.add(result.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        return transferReceipts;
    }

    private void checkReceiptsStatus(ArrayList<String> receipts, ExecutorService pool, String description) {
        CountDownLatch countDownLatch = new CountDownLatch(receipts.size());
        for(int i = 0; i < receipts.size(); ++i) {
            pool.submit(new GetReceiptStatusTask(description, receipts.get(i), countDownLatch, i));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String getReceipt(String receiptHash) {
        String header="{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":[";
        String quote="'";
        String footer="']}";
        String data = header + quote + receiptHash + footer;
        return post(data);
    }

    private void unlockAllAccounts(ArrayList<String> accounts, String password, ExecutorService pool) {
        final CountDownLatch countDownLatch = new CountDownLatch(accounts.size());
        for (int i = 0; i < accounts.size(); ++i) {
            final String account = accounts.get(i);
            final int n = i;
            Runnable unlockAccountTask = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("%s: %d-unlocking account %s ...\n", threadName, n, account);
                String response = unlockAccount(account, password);
                assert (response != null);
                System.out.printf("%s: %d-response:%s\n", threadName, n, response);
                boolean isSucceed = extractUnlockResult(response);
                assert (isSucceed);
                System.out.printf("%s: %d-unlocking account %s successfully!\n", threadName, n, account);
                countDownLatch.countDown();
            };
            pool.submit(unlockAccountTask);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private boolean extractUnlockResult(String response) {
        return response.contains("\"result\":true");
    }

    private String unlockAccount(String account, String password) {
        String header="{\"jsonrpc\":\"2.0\",\"method\":\"personal_unlockAccount\",\"params\":[\"";
        String space="\", \"";
        String footer="\", \"600\"]}";
        String data = header + account + space + password + footer;

        return post(data);
    }

    private int getNonce(String preminedAccount) {
        String data = "{\"jsonrpc\":\"2.0\"," +
                "\"method\":\"eth_getTransactionCount\"," +
                "\"params\":[\"" + preminedAccount + "\",\"latest\"]," +
                "\"id\":1}";
        return Integer.parseInt(extractNonce(post(data)).substring(2), 16);
    }

    private static String extractNonce(String response) {
        String hash = null;

        // String to be scanned to find the pattern.
        String pattern = "\"result\":\"(0x[0-9a-f]+)\",";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(response);
        if (m.find()) {
            hash = m.group(1);
        }
        return hash;
    }

    private static String post(String postParams) {
        String responseString = null;
        try {
            System.out.println("Request:" + postParams);
            URL obj = new URL("http://127.0.0.1:8545");
            HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            postConnection.setDoOutput(true);
            OutputStream os = postConnection.getOutputStream();
            os.write(postParams.getBytes());
            os.flush();
            os.close();
            int responseCode = postConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        postConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                responseString = response.toString();
            } else {
                System.out.println("Post restful request failed!");
            }
        }catch (Exception e) {
            System.out.println(e.toString());
            System.exit(1);
        }
        return responseString;
    }

    private static String extractReceiptHash(String response) {
        String hash = null;

        // String to be scanned to find the pattern.
        String pattern = "\"result\":\"(0x[0-9a-f]{64})";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(response);
        if (m.find()) {
            hash = m.group(1);
        }
        return hash;
    }

    private ArrayList<String> deployDapps(ArrayList<String> accounts, String password, String dappPath, ExecutorService pool) {
        ArrayList<String> deployReceipts = new ArrayList<>();
        try {
            Path path = Paths.get(dappPath);
            byte[] jar = Files.readAllBytes(path);
            byte[] args = ABIEncoder.encodeOneIntegerArray(new int[] { heavyLevel, allocSize });
            final String codeArguments = Helpers.bytesToHexString(new CodeAndArguments(jar, args).encodeToBytes());

            ArrayList<Future<String>> results = new ArrayList<>();
            for(int i = 0; i < accounts.size(); ++i) {
                final String account = accounts.get(i);
                final int n = i;
                Callable<String> deployTask = () -> {
                    String threadName = Thread.currentThread().getName();
                    System.out.printf("%s: %d-unlocking account %s ...\n", threadName, n, account);
                    String responseUnlock = unlockAccount(account, password);
                    assert (responseUnlock != null);
                    System.out.printf("%s: %d-response:%s\n", threadName, n, responseUnlock);
                    boolean isSucceed = extractUnlockResult(responseUnlock);
                    assert (isSucceed);
                    System.out.printf("%s: %d-unlocking account %s successfully!\n", threadName, n, account);

                    System.out.printf("%s: %d-deploying dapp for account %s ...\n", threadName, n, account);
                    String response = deployDapp(account, codeArguments);
                    assert (response != null);
                    System.out.printf("%s: %d-response:%s\n", threadName, n, response);
                    String receiptHash = extractReceiptHash(response);
                    assert (receiptHash != null);
                    return receiptHash;
                };
                results.add(pool.submit(deployTask));
            }

            for (Future<String> result : results) {
                try {
                    deployReceipts.add(result.get());
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(1);
        }
        return deployReceipts;
    }

    private String deployDapp(String deployer, String codeArguments) {
        String header="{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\": \"";
        String gas="\", \"gas\": \"5000000\", \"gasPrice\": \"100000000000\", \"type\": \"0xf\", \"data\": \"";
        String footer="\"}]}";
        String data = header + deployer + gas + codeArguments + footer;
        return post(data);
    }

    private ArrayList<String> getAllDappAddresses(ArrayList<String> deployReceipts, ExecutorService pool) {
        ArrayList<String> dapps = new ArrayList<>();
        ArrayList<Future<String>> results = new ArrayList<>();
        for(int i = 0; i < deployReceipts.size(); ++i) {
            final String receiptHash = deployReceipts.get(i);
            final int n = i;
            Callable<String> GetDappAddressTask = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("%s: %d-getting dapp address from transaction %s ...\n", threadName, n, receiptHash);
                String dappAddress;
                while((dappAddress = getDappAddress(receiptHash)) == null){
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                System.out.printf("%s: %d-getting dapp address %s successfully!\n", threadName, n, dappAddress);
                return dappAddress;
            };
            results.add(pool.submit(GetDappAddressTask));
        }

        for (Future<String> result : results) {
            try {
                dapps.add(result.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return dapps;
    }

    private String getDappAddress(String receiptHash) {
        String response = getReceipt(receiptHash);
        System.out.println("Response:" + response);
        return extractContractAddr(response);
    }

    private String extractContractAddr(String response) {
        String address = null;

        // String to be scanned to find the pattern.
        String pattern = "\"contractAddress\":\"(0x[0-9a-f]{64})";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(response);
        if (m.find()) {
            address = m.group(1);
        }
        return address;
    }

    private ArrayList<String> callDapps(int accountNum, ArrayList<String> accounts, String password, ArrayList<String> dapps, ExecutorService pool) {
        ArrayList<String> callReceipts = new ArrayList<>();
        final String callEncoding = Helpers.bytesToHexString(ABIUtil.encodeMethodArguments("cpuHeavy"));
        ArrayList<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < accountNum; ++i) {
            final String account = accounts.get(i);
            final String dapp = dapps.get(i);
            final int n = i;
            Callable<String> callTask = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("%s: %d-unlocking account %s ...\n", threadName, n, account);
                String responseUnlock = unlockAccount(account, password);
                assert (responseUnlock != null);
                System.out.printf("%s: %d-response:%s\n", threadName, n, responseUnlock);
                boolean isSucceed = extractUnlockResult(responseUnlock);
                assert (isSucceed);
                System.out.printf("%s: %d-unlocking account %s successfully!\n", threadName, n, account);

                System.out.printf("%s: %d-account %s is calling dapp %s ...\n", threadName, n, account, dapp);
                String response = callDapp(account, dapp, callEncoding);
                System.out.printf("%s: %d-response:%s\n", threadName, n, response);
                String receiptHash = extractReceiptHash(response);
                assert (!receiptHash.equals(""));
                return receiptHash;
            };
            results.add(pool.submit(callTask));
        }

        for (Future<String> result : results) {
            try {
                callReceipts.add(result.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return callReceipts;
    }

    private String callDapp(String sender, String dapp, String methodEncoding) {
        String header="{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\": \"";
        String to="\", \"to\": \"";
        String gas="\", \"gas\": \"2000000\", \"gasPrice\": \"100000000000\", \"data\": \"";
        String footer="\"}]}";
        String data = header + sender + to + dapp + gas + methodEncoding + footer;
        return post(data);
    }

    static private void usage() {
        System.out.println("Usage: <main class> [options]\n" +
                "  Options:\n" +
                "    <password> <dapp path> <pre-mined account address> <account number> <dapp number> <thread number>\n");
    }

    static public void main(String[] args) {
        if(args.length != 6) {
            System.out.println("Invalid parameters!");
            usage();
            System.exit(1);
        }
        try {
            String password = args[0];

            String dappPath = args[1];

            String preminedAccount = args[2];

            int accountNum = Integer.parseInt(args[3]);
            if (accountNum <= 0 || accountNum > maxValueForParam) {
                throw new Exception("Parameter <accountsNum> should be in [1, " + maxValueForParam + "] (found " + accountNum +")");
            }


            int dappNum = Integer.parseInt(args[4]);
            if (dappNum <= 0 || dappNum > maxValueForParam) {
                throw new Exception("Parameter <dappsNum> should be in [1, " + maxValueForParam + "] (found " + dappNum +")");
            }

            int threadNum = Integer.parseInt(args[5]);
            if (threadNum <= 0 || threadNum > maxValueForParam) {
                throw new Exception("Parameter <threadsNum> should be in [1, " + maxValueForParam + "] (found " + threadNum +")");
            }

            System.out.printf("Parameters: %s, %s, %s, %d, %d, %d.\n", password, dappPath, preminedAccount, accountNum, dappNum, threadNum);

            TestNetCli testNetCli = new TestNetCli();
            testNetCli.run(password, dappPath, preminedAccount, accountNum, threadNum);

            System.out.println("*****Test finished!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number " + e.getMessage());
            usage();
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            usage();
            System.exit(1);
        }
    }

    private static class TransferTask implements Callable<String>{
        private final String preminedAccount;
        private final String recipient;
        private final String amount;
        private final String nonce;
        private final int xth;

        private TransferTask(String preminedAccount, String recipient, String amount, String nonce, int xth) {
            this.preminedAccount = preminedAccount;
            this.recipient = recipient;
            this.amount = amount;
            this.nonce = nonce;
            this.xth = xth;
        }

        public String call() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("%s: %d-transfering %s from %s to %s with nonce %s ...\n", threadName, xth, amount, preminedAccount, recipient, nonce);
            String response = transfer(preminedAccount, recipient, amount, nonce);
            System.out.printf("%s: %d-response:%s\n", threadName, xth, response);
            assert (response != null);
            String receiptHash = extractReceiptHash(response);
            assert (receiptHash != null);
            return receiptHash;
        }

        private String transfer(String sender, String recipient, String amount, String nonce) {
            String header = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\": \"";
            String to = "\", \"to\": \"";
            String gas = "\", \"gas\": \"2000000\", \"gasPrice\": \"100000000000\", \"value\": \"";
            String nonceStr = "\", \"nonce\":\"";
            String footer="\"}]}";
            String data = header + sender + to + recipient + gas + amount + nonceStr + nonce + footer;
            return post(data);
        }
    }

    private static class GetReceiptStatusTask implements Runnable{
        private final String description;
        private final String receiptHash;
        private CountDownLatch countDownLatch;
        private int xth;
        private GetReceiptStatusTask(String description, String receiptHash, CountDownLatch countDownLatch, int xth) {
            this.description = description;
            this.receiptHash = receiptHash;
            this.xth = xth;
            this.countDownLatch = countDownLatch;
        }

        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("%s: %d-checking %s receipt status of %s ...\n", threadName, xth, description, receiptHash);
            while (!getReceiptStatus(receiptHash)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("%s: %d-%s Receipt %s succeeds!\n", threadName, xth, description, receiptHash);
            countDownLatch.countDown();
        }

        private boolean getReceiptStatus(String receiptHash) {
            String response = getReceipt(receiptHash);
            System.out.println("Response:" + response);
            return extractReceiptStatus(response);
        }

        private boolean extractReceiptStatus(String response) {
            return response.contains("\"status\":\"0x1\"");
        }
    }
}
