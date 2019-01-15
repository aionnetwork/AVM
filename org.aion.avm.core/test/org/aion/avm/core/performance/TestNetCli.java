package org.aion.avm.core.performance;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.CodeAndArguments;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestNetCli {
    private static final int maxValueForParam = 10000;
    private static final int heavyLevel = 1;
    private static final int allocSize = 1000000;//(1 * (1 << 20));

    public void run(String password, String dappPath, String preminedAccount, int accountNum) {

        ArrayList<String> accounts = createAccounts(accountNum, password);

        String amount = "1000000000000000000";
        ArrayList<String> transferReceipts = transfers(preminedAccount, password, amount, accounts);

        checkReceiptsStatus(transferReceipts);

        unlockAllAccounts(accounts, password);

        ArrayList<String> deployReceipts = deployDapps(accounts, dappPath);

        ArrayList<String> dapps = getAllDappAddresses(deployReceipts);

        ArrayList<String> callReceipts = callDapps(accountNum, accounts, password, dapps);

        checkReceiptsStatus(callReceipts);

        System.out.println("*****Test finished!");
    }

    private ArrayList<String> createAccounts(int accountNum, String password) {
        ArrayList<String> accounts = new ArrayList<>();

        String header = "{\"jsonrpc\":\"2.0\",\"method\":\"personal_newAccount\",\"params\":[\"";
        String footer = "\"]}";
        String data = header + password + footer;

        for(int i = 0; i < accountNum; ++i) {
            System.out.printf("%d: Creating account...\n", i);
            String response = post(data);
            assert (null != response);
            System.out.println("Response:" + response);
            String receiptHash = extractReceiptHash(response);
            assert (null != receiptHash);
            accounts.add(receiptHash);
        }
        return accounts;
    }

    private ArrayList<String> transfers(String preminedAccount, String password, String amount, ArrayList<String> accounts) {
        ArrayList<String> transferReceipts = new ArrayList<>();
        System.out.println("Unlocking " + preminedAccount);
        String responseUnlock = unlockAccount(preminedAccount, password);
        System.out.println("Response:" + responseUnlock);
        boolean isSucceed = extractUnlockResult(responseUnlock);
        assert (isSucceed);
        System.out.printf("Unlocking account %s successfully!\n", preminedAccount);

        for(int i = 0; i < accounts.size(); ++i) {
            String recipient = accounts.get(i);
            System.out.printf("%d: Transfering %s from %s to %s ...\n", i, amount, preminedAccount, recipient);
            String response = transfer(preminedAccount, recipient, amount);
            System.out.println("Response:" + response);
            assert (response != null);
            String receiptHash = extractReceiptHash(response);
            assert (receiptHash != null);
            transferReceipts.add(receiptHash);
        }
        return transferReceipts;
    }

    private String transfer(String sender, String recipient, String amount) {
        String header = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\": \"";
        String to = "\", \"to\": \"";
        String gas = "\", \"gas\": \"2000000\", \"gasPrice\": \"100000000000\", \"value\": \"";
        String footer="\"}]}";
        String data = header + sender + to + recipient + gas + amount + footer;
        return post(data);
    }


    private void checkReceiptsStatus(ArrayList<String> receipts) {
        for(int i = 0; i < receipts.size(); ++i) {
            String hash = receipts.get(i);
            System.out.printf("%d: Checking receipt status of %s ...\n", i, hash);
            while(!getReceiptStatus(hash)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("Receipt %s succeeds!\n", hash);
        }
    }

    private boolean getReceiptStatus(String receiptHash) {
        String response = getReceipt(receiptHash);
        System.out.println("Response:" + response);
        return extractReceiptStatus(response);
    }

    private String getReceipt(String receiptHash) {
        String header="{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":[";
        String quote="'";
        String footer="']}";
        String data = header + quote + receiptHash + footer;
        return post(data);
    }

    private boolean extractReceiptStatus(String response) {
        return response.contains("\"status\":\"0x1\"");
    }

    private void unlockAllAccounts(ArrayList<String> accounts, String password) {
        for (int i = 0; i < accounts.size(); ++i) {
            System.out.printf("%d: Unlocking %s\n", i , accounts.get(i));
            String response = unlockAccount(accounts.get(i), password);
            assert (response != null);
            System.out.println("Response:" + response);
            boolean isSucceed = extractUnlockResult(response);
            assert (isSucceed);
            System.out.printf("Unlocking account %s successfully!\n", accounts.get(i));
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

    private String post(String postParams) {
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

    private String extractReceiptHash(String response) {
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

    private ArrayList<String> deployDapps(ArrayList<String> accounts, String dappPath) {
        ArrayList<String> deployReceipts = new ArrayList<>();
        try {
            Path path = Paths.get(dappPath);
            byte[] jar = Files.readAllBytes(path);
            byte[] args = ABIEncoder.encodeOneObject(new int[] { heavyLevel, allocSize });
            String codeArguments = Helpers.bytesToHexString(new CodeAndArguments(jar, args).encodeToBytes());
            // Deploy dapps
            for(int i = 0; i < accounts.size(); ++i) {
                System.out.printf("%d: Deploying dapp for account %s  ...\n", i, accounts.get(i));
                String response = deployDapp(accounts.get(i), codeArguments);
                assert (response != null);
                System.out.println("Response:" + response);
                String receiptHash = extractReceiptHash(response);
                assert (receiptHash != null);
                deployReceipts.add(receiptHash);
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

    private ArrayList<String> getAllDappAddresses(ArrayList<String> deployReceipts) {
        ArrayList<String> dapps = new ArrayList<>();
        for(int i = 0; i < deployReceipts.size(); ++i) {
            String receiptHash = deployReceipts.get(i);
            System.out.printf("%d: Getting dapp address from transaction %s ...\n", i, receiptHash);
            String dappAddress;
            while((dappAddress = getDappAddress(receiptHash)) == null){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dapps.add(dappAddress);
            System.out.printf("Getting dapp address %s successfully!\n", dappAddress);
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

    private ArrayList<String> callDapps(int accountNum, ArrayList<String> accounts, String password, ArrayList<String> dapps) {
        ArrayList<String> callReceipts = new ArrayList<>();
        String callEncoding = Helpers.bytesToHexString(ABIEncoder.encodeMethodArguments("cpuHeavy"));
        for(int i = 0; i < accountNum; ++i) {
            System.out.printf("%d: Account %s is calling dapp %s ...\n", i, accounts.get(i), dapps.get(i));
            String response = callDapp(accounts.get(i), dapps.get(i), callEncoding);
            System.out.println("Response:" + response);
            String receiptHash = extractReceiptHash(response);
            assert (!receiptHash.equals(""));
            callReceipts.add(receiptHash);
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
        System.out.printf("Usage: <main class> [options]\n" +
                "  Options:\n" +
                "    <password> <dapp path> <pre-mined account address> <account number> <dapp number> <thread number>");
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
            testNetCli.run(password, dappPath, preminedAccount, accountNum);
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
}
