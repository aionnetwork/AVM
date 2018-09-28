package org.aion.avm.core.shadowing.testNio;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

import java.nio.*;

public class TestResource {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new TestResource(), BlockchainRuntime.getData());
    }

    public boolean testByteBuffer(){

        // Create an empty ByteBuffer with a 10 byte capacity
        ByteBuffer bbuf = ByteBuffer.allocate(10);

        // Get the buffer's capacity
        int capacity = bbuf.capacity(); // 10

        // Use the absolute put(int, byte).
        // This method does not affect the position.
        bbuf.put(0, (byte)0xFF); // position=0

        // Set the position
        bbuf.position(5);

        // Use the relative put(byte)
        bbuf.put((byte)0xFF);

        // Get the new position
        int pos = bbuf.position(); // 6

        // Get remaining byte count
        int rem = bbuf.remaining(); // 4

        // Set the limit
        bbuf.limit(7); // remaining=1

        // This convenience method sets the position to 0
        bbuf.rewind(); // remaining=7

        return true;
    }

    public boolean testCharBuffer(){
        CharBuffer cbuf = CharBuffer.allocate(10);

        int capacity = cbuf.capacity(); // 10

        cbuf.put(0, 'a'); // position=0

        cbuf.position(5);

        cbuf.put('b');

        int pos = cbuf.position(); // 6

        int rem = cbuf.remaining(); // 4

        cbuf.limit(7); // remaining=1

        cbuf.rewind(); // remaining=7

        cbuf.toString();

        return true;
    }

    public boolean testDoubleBuffer(){
        DoubleBuffer dbuf = DoubleBuffer.allocate(10);

        int capacity = dbuf.capacity(); // 10

        dbuf.put(0, 10.9d); // position=0

        dbuf.position(5);

        dbuf.put(10.99d);

        int pos = dbuf.position(); // 6

        int rem = dbuf.remaining(); // 4

        dbuf.limit(7); // remaining=1

        dbuf.rewind(); // remaining=7

        dbuf.toString();

        return true;
    }

    public boolean testFloatBuffer(){
        FloatBuffer fbuf = FloatBuffer.allocate(10);

        int capacity = fbuf.capacity(); // 10

        fbuf.put(0, 10.9f); // position=0

        fbuf.position(5);

        fbuf.put(10.99f);

        int pos = fbuf.position(); // 6

        int rem = fbuf.remaining(); // 4

        fbuf.limit(7); // remaining=1

        fbuf.rewind(); // remaining=7

        fbuf.toString();

        return true;
    }

    public boolean testIntBuffer(){
        IntBuffer ibuf = IntBuffer.allocate(10);

        int capacity = ibuf.capacity(); // 10

        ibuf.put(0, 10); // position=0

        ibuf.position(5);

        ibuf.put(9);

        int pos = ibuf.position(); // 6

        int rem = ibuf.remaining(); // 4

        ibuf.limit(7); // remaining=1

        ibuf.rewind(); // remaining=7

        ibuf.toString();

        return true;
    }

    public boolean testLongBuffer(){
        LongBuffer lbuf = LongBuffer.allocate(10);

        int capacity = lbuf.capacity(); // 10

        lbuf.put(0, 1000L); // position=0

        lbuf.position(5);

        lbuf.put(100L);

        int pos = lbuf.position(); // 6

        int rem = lbuf.remaining(); // 4

        lbuf.limit(7); // remaining=1

        lbuf.rewind(); // remaining=7

        lbuf.toString();

        return true;
    }

    public boolean testShortBuffer(){
        ShortBuffer sbuf = ShortBuffer.allocate(10);

        int capacity = sbuf.capacity(); // 10

        sbuf.put(0, (short)10); // position=0

        sbuf.position(5);

        sbuf.put((short)9);

        int pos = sbuf.position(); // 6

        int rem = sbuf.remaining(); // 4

        sbuf.limit(7); // remaining=1

        sbuf.rewind(); // remaining=7

        sbuf.toString();

        return true;
    }

}
