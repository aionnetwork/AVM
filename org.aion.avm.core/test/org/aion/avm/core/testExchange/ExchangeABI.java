package org.aion.avm.core.testExchange;

import org.aion.avm.api.Address;
import org.aion.avm.core.testWallet.ByteArrayHelpers;


public class ExchangeABI {

    public static final byte kToken_totalSupply = 0x1;
    public static final byte kToken_balanceOf = 0x2;
    public static final byte kToken_allowance = 0x3;
    public static final byte kToken_transfer = 0x4;
    public static final byte kToken_approve = 0x5;
    public static final byte kToken_transferFrom = 0x6;
    public static final byte kToken_openAccount = 0x7;
    public static final byte kToken_mint = 0x8;

    public static final byte kExchange_listCoin = 0x1;
    public static final byte kExchange_postBid = 0x2;
    public static final byte kExchange_postAsk = 0x3;
    public static final byte kExchange_requestTransfer = 0x4;
    public static final byte kExchange_processExchangeTransaction = 0x5;

    public static Decoder buildDecoder(byte[] input) {
        return new Decoder(input);
    }

    public static Encoder buildEncoder(byte[] onto) {
        return new Encoder(onto);
    }


    public static class Decoder {
        private final byte[] input;
        private int cursor;

        private Decoder(byte[] input) {
            this.input = input;
        }

        public byte decodeByte() {
            byte result = this.input[this.cursor];
            this.cursor += 1;
            return result;
        }

        public int decodeInt() {
            byte[] slice = ByteArrayHelpers.arraySlice(this.input, this.cursor, 4);
            this.cursor += 4;
            return ByteArrayHelpers.decodeInt(slice);
        }

        public long decodeLong() {
            byte[] slice = ByteArrayHelpers.arraySlice(this.input, this.cursor, 8);
            this.cursor += 8;
            return ByteArrayHelpers.decodeLong(slice);
        }

        public Address decodeAddress() {
            byte[] slice = ByteArrayHelpers.arraySlice(this.input, this.cursor, Address.LENGTH);
            this.cursor += Address.LENGTH;
            return new Address(slice);
        }

        public String decodeString(int l) {
            byte[] slice = ByteArrayHelpers.arraySlice(this.input, this.cursor, l);
            this.cursor += l;
            return new String(slice);
        }

        public byte[] decodeRemainder() {
            int length = this.input.length - this.cursor;
            byte[] slice = ByteArrayHelpers.arraySlice(this.input, this.cursor, length);
            this.cursor += length;
            return slice;
        }
    }


    public static class Encoder {
        private final byte[] onto;
        private int cursor;

        private Encoder(byte[] onto) {
            this.onto = onto;
        }

        public Encoder encodeByte(byte arg) {
            this.onto[this.cursor] = arg;
            this.cursor += 1;
            return this;
        }

        public Encoder encodeInt(int arg) {
            byte[] slice = ByteArrayHelpers.encodeInt(arg);
            System.arraycopy(slice, 0, this.onto, this.cursor, slice.length);
            this.cursor += slice.length;
            return this;
        }

        public Encoder encodeLong(long arg) {
            byte[] slice = ByteArrayHelpers.encodeLong(arg);
            System.arraycopy(slice, 0, this.onto, this.cursor, slice.length);
            this.cursor += slice.length;
            return this;
        }

        public Encoder encodeAddress(Address arg) {
            byte[] slice = arg.unwrap();
            System.arraycopy(slice, 0, this.onto, this.cursor, slice.length);
            this.cursor += slice.length;
            return this;
        }

        public Encoder encodeString(String s){
            byte[] slice = s.getBytes();
            System.arraycopy(slice, 0, this.onto, this.cursor, slice.length);
            this.cursor += slice.length;
            return this;
        }

        public Encoder encodeRemainder(byte[] arg) {
            System.arraycopy(arg, 0, this.onto, this.cursor, arg.length);
            this.cursor += arg.length;
            return this;
        }
    }
}
