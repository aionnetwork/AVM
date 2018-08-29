package org.aion.cli;

import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



public class AvmCLI implements UserInterface{

    static public long ENERGY_LIMIT = 100_000_000_000L;

    private static final AvmCLI instance = new AvmCLI();

    static Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    public static AvmCLI getInstance(){
        return instance;
    }

    private AvmCLI(){
    }

    @Override
    public void deploy(String storagePath, String jarPath, byte[] sender) {

        System.out.println("****************************************************************************");
        System.out.println("Processing DApp deployment request");
        System.out.println("Storage      : " + storagePath);
        System.out.println("Dapp Jar     : " + jarPath);
        System.out.println("Sender       : " + Helpers.bytesToHexString(sender));

        File storageFile = new File(storagePath);

        KernelInterfaceImpl kernel = new KernelInterfaceImpl(storageFile);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        Path path = Paths.get(jarPath);
        byte[] jar;
        try {
            jar = Files.readAllBytes(path);
        }catch (IOException e){
            System.out.println("Error: Invalid location of Dapp jar");
            return;
        }

        byte[] to = Helpers.randomBytes(Address.LENGTH);
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, sender, to, kernel.getNonce(sender), 0,
                new CodeAndArguments(jar, null).encodeToBytes(), ENERGY_LIMIT, 1);

        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);
        System.out.println(createResult.toString());

        if (createResult.getStatusCode().isSuccess()) {

            System.out.println("****************************************************************************");
            System.out.println("DApp deployment succeed");
            System.out.println("Dapp Address : " + Helpers.bytesToHexString(createResult.getReturnData()));
            System.out.println("Energy cost  : " + createResult.getEnergyUsed());
        }

    }

    @Override
    public void execute(String storagePath, byte[] contract, byte[] sender, String method, String... args) {

    }

    public void openAccount(String storagePath, byte[] toOpen){
        System.out.println("****************************************************************************");
        System.out.println("Creating Account " + Helpers.bytesToHexString(toOpen));

        File storageFile = new File(storagePath);
        KernelInterfaceImpl kernel = new KernelInterfaceImpl(storageFile);

        kernel.createAccount(toOpen);
        kernel.adjustBalance(toOpen, 100000000000L);

        System.out.println("Account Balance : " + kernel.getBalance(toOpen));
    }

    static public void main(String[] args){

        String mode = args[0];

        if (mode.equals("-d")) {
            String storagePath = args[1];
            String jarPath = args[2];
            //String senderString = args[3];
            byte[] sender = KernelInterfaceImpl.PREMINED_ADDRESS;;

            instance.deploy(storagePath, jarPath, sender);
        }else if (mode.equals("-e")){

        }else if (mode.equals("-g")){
            String storagePath = args[1];
            instance.openAccount(storagePath, Helpers.randomBytes(Address.LENGTH));

        }else if (mode.equals("-a")){
            String storagePath = args[1];
            String senderString = args[2];
            byte[] sender = Helpers.hexStringToBytes(senderString);
            instance.openAccount(storagePath, sender);
        }

    }
}
