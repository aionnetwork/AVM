package org.aion.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.StorageWalker;
import org.aion.avm.internal.Helper;
import org.aion.kernel.*;
import com.beust.jcommander.Parameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvmCLI implements UserInterface{
    static String DEFAULT_STORAGE = "./storage";

    static String DEFAULT_SENDER_STRING = Helpers.bytesToHexString(KernelInterfaceImpl.PREMINED_ADDRESS);

    static long DEFAULT_ENERGY_LIMIT = 100_000_000L;

    static Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private JCommander jc;

    private CLIFlag flag;
    private CommandOpen open;
    private CommandDeploy deploy;
    private CommandCall call;
    private CommandExplore explore;

    private AvmCLI(){
        flag = new CLIFlag();
        open = new CommandOpen();
        deploy = new CommandDeploy();
        call = new CommandCall();
        explore = new CommandExplore();
        jc = JCommander.newBuilder()
                .addObject(flag)
                .addCommand("open", open)
                .addCommand("deploy", deploy)
                .addCommand("call", call)
                .addCommand("explore", explore)
                .build();

        jc.setProgramName("AvmCLI");
    }

    public class CLIFlag {

        @Parameter(names = {"-st", "--storage"}, description = "Specify the storage directory")
        private String storage = DEFAULT_STORAGE;

        @Parameter(names = {"-h", "--help"}, description = "Show usage of AVMCLI")
        private boolean usage = false;
    }


    @Parameters(commandDescription = "Open new account")
    public class CommandOpen{

        @Parameter(names = {"-a","--address"}, description = "The address to open")
        private String address = "Random generated address";
    }

    @Parameters(commandDescription = "Deploy Dapp")
    public class CommandDeploy{

        @Parameter(description = "DappJar", required = true)
        private String contract = null;

        @Parameter(names = { "-sd" ,"--sender"}, description = "The sender of the request")
        private String sender = DEFAULT_SENDER_STRING;
    }

    @Parameters(commandDescription = "Explore storage")
    public class CommandExplore{
        @Parameter(description = "DappAddress", required = true)
        private String contract = null;
    }

    @Parameters(commandDescription = "Call Dapp")
    public class CommandCall{

        @Parameter(description = "DappAddress", required = true)
        private String contract = null;

        @Parameter(names = {"-sd", "--sender"}, description = "The sender of the request")
        private String sender = DEFAULT_SENDER_STRING;

        @Parameter(names = {"-e", "--energy-limit"}, description = "The energy limit of the request")
        private long energyLimit = DEFAULT_ENERGY_LIMIT;

        @Parameter(names = {"-m", "--method"}, description = "The requested method")
        private String methodName = "";

        @Parameter(names = {"-a", "--args"}, description = "The requested arguments. " +
                "User provided arguments have the format of (Type Value)*. " +
                "The following type are supported " +
                "-I int, -J long, -S short, -C char, -F float, -D double, -B byte, -Z boolean, -A address. "+
                "For example, option \"-a -I 1 -C c -Z true\" will form arguments of [(int) 1, (char) 'c', (boolean) true].",
                variableArity = true)
        public List<String> arguments = new ArrayList<>();

    }

    @Override
    public void deploy(IEnvironment env, String storagePath, String jarPath, byte[] sender, long energyLimit) {

        reportDeployRequest(env, storagePath, jarPath, sender);

        if (sender.length != Address.LENGTH){
            throw env.fail("deploy : Invalid sender address");
        }

        File storageFile = new File(storagePath);

        KernelInterfaceImpl kernel = new KernelInterfaceImpl(storageFile);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        Path path = Paths.get(jarPath);
        byte[] jar;
        try {
            jar = Files.readAllBytes(path);
        }catch (IOException e){
            throw env.fail("deploy : Invalid location of Dapp jar");
        }

        Transaction createTransaction = Transaction.create(sender, kernel.getNonce(sender), 0, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, 1L);

        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);

        reportDeployResult(env, createResult);
    }

    public void reportDeployRequest(IEnvironment env, String storagePath, String jarPath, byte[] sender) {
        lineSeparator(env);
        env.logLine("DApp deployment request");
        env.logLine("Storage      : " + storagePath);
        env.logLine("Dapp Jar     : " + jarPath);
        env.logLine("Sender       : " + Helpers.bytesToHexString(sender));
    }

    public void reportDeployResult(IEnvironment env, TransactionResult createResult){
        String dappAddress = Helpers.bytesToHexString(createResult.getReturnData());
        env.noteRelevantAddress(dappAddress);
        
        lineSeparator(env);
        env.logLine("DApp deployment status");
        env.logLine("Result status: " + createResult.getStatusCode().name());
        env.logLine("Dapp Address : " + dappAddress);
        env.logLine("Energy cost  : " + createResult.getEnergyUsed());
    }

    @Override
    public void call(IEnvironment env, String storagePath, byte[] contract, byte[] sender, String method, String[] args, long energyLimit) {
        reportCallRequest(env, storagePath, contract, sender, method, args);

        if (contract.length != Address.LENGTH){
            throw env.fail("call : Invalid Dapp address ");
        }

        if (sender.length != Address.LENGTH){
            throw env.fail("call : Invalid sender address");
        }

        byte[] arguments = ABIEncoder.encodeMethodArguments(method, parseArgs(args));

        File storageFile = new File(storagePath);

        KernelInterfaceImpl kernel = new KernelInterfaceImpl(storageFile);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), 0L, arguments, energyLimit, 1L);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        TransactionResult callResult = avm.run(callContext);

        reportCallResult(env, callResult);
    }

    private void reportCallRequest(IEnvironment env, String storagePath, byte[] contract, byte[] sender, String method, String[] args){
        lineSeparator(env);
        env.logLine("DApp call request");
        env.logLine("Storage      : " + storagePath);
        env.logLine("Dapp Address : " + Helpers.bytesToHexString(contract));
        env.logLine("Sender       : " + Helpers.bytesToHexString(sender));
        env.logLine("Method       : " + method);
        env.logLine("Arguments    : ");
        for (int i = 0; i < args.length; i += 2){
            env.logLine("             : " + args[i] + " " + args[i + 1]);
        }
    }

    private void reportCallResult(IEnvironment env, TransactionResult callResult){
        lineSeparator(env);
        env.logLine("DApp call result");
        env.logLine("Result status: " + callResult.getStatusCode().name());
        env.logLine("Return value : " + Helpers.bytesToHexString(callResult.getReturnData()));
        env.logLine("Energy cost  : " + callResult.getEnergyUsed());

        if (callResult.getStatusCode() == TransactionResult.Code.FAILED_EXCEPTION) {
            env.dumpThrowable(callResult.getUncaughtException());
        }
    }

    // open for testing
    public static Object[] parseArgs(String[] args){

        Object[] argArray = new Object[args.length / 2];

        for (int i = 0; i < args.length; i += 2){
            switch (args[i]){
                case "-I":
                    argArray[i / 2] = Integer.valueOf(args[i + 1]);
                    break;
                case "-S":
                    argArray[i / 2] = Short.valueOf(args[i + 1]);
                    break;
                case "-L":
                    argArray[i / 2] = Long.valueOf(args[i + 1]);
                    break;
                case "-F":
                    argArray[i / 2] = Float.valueOf(args[i + 1]);
                    break;
                case "-D":
                    argArray[i / 2] = Double.valueOf(args[i + 1]);
                    break;
                case "-C":
                    argArray[i / 2] = Character.valueOf(args[i + 1].charAt(0));
                    break;
                case "-B":
                    argArray[i / 2] = Helpers.hexStringToBytes(args[i + 1])[0];
                    break;
                case "-Z":
                    argArray[i / 2] = Boolean.valueOf(args[i + 1]);
                    break;
                case "-A": {
                    // We want to parse an Address but we first need to read the hex to bytes.
                    if (!args[i + 1].matches("(0x)?[A-Fa-f0-9]{64}")) {
                        throw new IllegalArgumentException("Invalid address: " + args[i + 1]);
                    }
                    // To create an Address instance, we need to temporarily install a Helper (for base class instantiation).
                    AvmClassLoader avmClassLoader = NodeEnvironment.singleton.createInvocationClassLoader(Collections.emptyMap());
                    new Helper(avmClassLoader, 1_000_000L, 1);
                    argArray[i / 2] = new Address(Helpers.hexStringToBytes(args[i + 1]));
                    Helper.clearTestingState();
                    break;
                }
            }
        }

        return argArray;
    }

    private void lineSeparator(IEnvironment env){
        env.logLine("*******************************************************************************************");
    }

    @Override
    public void openAccount(IEnvironment env, String storagePath, byte[] toOpen){
        lineSeparator(env);

        if (toOpen.length != Address.LENGTH){
            throw env.fail("open : Invalid address to open");
        }

        env.logLine("Creating Account " + Helpers.bytesToHexString(toOpen));

        File storageFile = new File(storagePath);
        KernelInterfaceImpl kernel = new KernelInterfaceImpl(storageFile);

        kernel.createAccount(toOpen);
        kernel.adjustBalance(toOpen, 100000000000L);

        env.logLine("Account Balance : " + kernel.getBalance(toOpen));
    }

    @Override
    public void exploreStorage(IEnvironment env, String storagePath, byte[] dappAddress) {
        // Create the PrintStream abstraction that walkAllStaticsForDapp expects.
        // (ideally, we would incrementally filter this but that could be a later improvement - current Dapps are very small).
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(stream);
        
        // Create the directory-backed kernel.
        KernelInterfaceImpl kernel = new KernelInterfaceImpl(new File(storagePath));
        
        // Walk everything, treating unexpected exceptions as fatal.
        try {
            StorageWalker.walkAllStaticsForDapp(printer, kernel, dappAddress);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
            // This tool can fail out if something goes wrong.
            throw env.fail(e.getMessage());
        }
        
        // Flush all the captured data and feed it to the env.
        printer.flush();
        String raw = new String(stream.toByteArray());
        env.logLine(raw);
    }

    public static void testingMain(IEnvironment env, String[] args) {
        internalMain(env, args);
    }

    static public void main(String[] args){
        IEnvironment env = new IEnvironment() {
            @Override
            public RuntimeException fail(String message) {
                if (null != message) {
                    System.err.println(message);
                }
                System.exit(1);
                throw new RuntimeException();
            }
            @Override
            public void noteRelevantAddress(String address) {
                // This implementation doesn't care.
            }
            @Override
            public void logLine(String line) {
                System.out.println(line);
            }

            @Override
            public void dumpThrowable(Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        internalMain(env, args);
    }

    private static void internalMain(IEnvironment env, String[] args) {
        // We handle all the parsing and dispatch through a special instance of ourselves (although this should probably be split into a few concerns).
        AvmCLI instance = new AvmCLI();
        try {
            instance.jc.parse(args);
        }catch (ParameterException e){
            callUsage(env, instance);
            throw env.fail(e.getMessage());
        }

        if (instance.flag.usage){
            callUsage(env, instance);
        }

        String parserCommand = instance.jc.getParsedCommand();
        if (null != parserCommand) {
            // Before we run any command, make sure that the specified storage directory exists.
            // (we want the underlying storage engine to remain very passive so it should always expect that the directory was created for it).
            verifyStorageExists(env, instance.flag.storage);
            
            if (parserCommand.equals("open")) {
                if (instance.open.address.equals("Random generated address")) {
                    instance.openAccount(env, instance.flag.storage, Helpers.randomBytes(Address.LENGTH));
                } else {
                    instance.openAccount(env, instance.flag.storage, Helpers.hexStringToBytes(instance.open.address));
                }
            }

            if (parserCommand.equals("deploy")) {
                instance.deploy(env, instance.flag.storage, instance.deploy.contract, Helpers.hexStringToBytes(instance.deploy.sender), instance.call.energyLimit);
            }

            if (parserCommand.equals("call")) {
                instance.call(env, instance.flag.storage, Helpers.hexStringToBytes(instance.call.contract),
                        Helpers.hexStringToBytes(instance.call.sender), instance.call.methodName,
                        instance.call.arguments.toArray(new String[0]), instance.call.energyLimit);
            }

            if (parserCommand.equals("explore")) {
                instance.exploreStorage(env, instance.flag.storage, Helpers.hexStringToBytes(instance.explore.contract));
            }
        } else {
            // If we specify nothing, print the usage.
            callUsage(env, instance);
        }
    }

    private static void verifyStorageExists(IEnvironment env, String storageRoot) {
        File directory = new File(storageRoot);
        if (!directory.isDirectory()) {
            boolean didCreate = directory.mkdirs();
            // Is this the best way to handle this failure?
            if (!didCreate) {
                // System.exit isn't ideal but we are very near the top of the entry-point so this shouldn't cause confusion.
                throw env.fail("Failed to create storage root: \"" + storageRoot + "\"");
            }
        }
    }

    private static void callUsage(IEnvironment env, AvmCLI instance) {
        StringBuilder builder = new StringBuilder();
        instance.jc.usage(builder);
        env.logLine(builder.toString());
    }
}
