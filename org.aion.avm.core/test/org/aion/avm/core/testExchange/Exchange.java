package org.aion.avm.core.testExchange;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.testWallet.ByteArrayHelpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;

public class Exchange {

    private AionMap<String, Address> coinListing;

    private ExchangeTransaction toProcess;

    private Address owner;

    public Exchange(){
        owner = BlockchainRuntime.getSender();
        coinListing = new AionMap<>();
    }

    private boolean verifyContractAddress(String name, Address contract){
        return true;
    }

    public boolean listCoin(String name, Address contractAddr){
        if (coinListing.containsKey(name)){return false;}

        if (BlockchainRuntime.getSender().equals(owner) && verifyContractAddress(name, contractAddr)){
            coinListing.put(name, contractAddr);
            return true;
        }

        return false;
    }

    public boolean postBid(String coin, long amount, String offerCoin, long bidAmount){
        return true;
    }

    public boolean postAsk(String coin, long amount, String askCoin){
        return true;
    }

    public boolean requestTransfer(String coin, Address to, long amount){
        if (!coinListing.containsKey(coin)){return false;}
        Address coinContract = coinListing.get(coin);

        Address sender = BlockchainRuntime.getSender();

        byte[] args = new byte[1 + Address.LENGTH + Address.LENGTH];
        ExchangeABI.Encoder encoder = ExchangeABI.buildEncoder(args);
        encoder.encodeByte(ExchangeABI.kToken_allowance);
        encoder.encodeAddress(sender);
        encoder.encodeAddress(BlockchainRuntime.getAddress());

        byte[] result = BlockchainRuntime.call(coinContract, 0, args, 1000000L);

        if (ByteArrayHelpers.decodeLong(result) >= amount){
            toProcess = new ExchangeTransaction(coin, sender, to, amount);
            return true;
        }

        return false;
    }

    public boolean processExchangeTransaction(){

        if (!BlockchainRuntime.getSender().equals(owner)){return false;};



        if (null == toProcess){return false;}



        if (!coinListing.containsKey(toProcess.getCoin())){return false;}

        Address coinContract = coinListing.get(toProcess.getCoin());

        byte[] args = new byte[1 + Address.LENGTH + Address.LENGTH + Long.BYTES];
        ExchangeABI.Encoder encoder = ExchangeABI.buildEncoder(args);
        encoder.encodeByte(ExchangeABI.kToken_transferFrom);
        encoder.encodeAddress(toProcess.getFrom());
        encoder.encodeAddress(toProcess.getTo());
        encoder.encodeLong(toProcess.getAmount());

        byte[] result = BlockchainRuntime.call(coinContract, 0, args, 1000000L);

        if (ByteArrayHelpers.decodeBoolean(result)){
            toProcess = null;
        }

        return ByteArrayHelpers.decodeBoolean(result);
    }


}
