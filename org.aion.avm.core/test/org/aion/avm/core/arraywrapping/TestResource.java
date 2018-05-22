package org.aion.avm.core.arraywrapping;

public class TestResource {

    public int increaseFirstElement() {
        int[] arr = new int[20];

        arr.hashCode();

        boolean x = arr instanceof Object;

        return arr[0];
    }

    public boolean testBasic(){
        boolean res = true;
        int[] arr = new int[20];
        int b = arr[2];
        arr[2] = 4;
        return res;
    }

    public boolean testBooleanArray(){
        boolean res = true;
        int i = 0;

        //newarray for byte
        boolean[] a = new boolean[2];
        boolean[] b = new boolean[64];
        boolean[] c = new boolean[1024];

        //BASTORE
        for (i = 0; i < 1024; i++){
            c[i] = ((i % 2) == 0);
        }

        //BALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == ((i % 2) == 0));
        }
        return res;
    }

    public boolean testByteArray(){
        boolean res = true;
        int i = 0;

        //newarray for byte
        byte[] a = new byte[2];
        byte[] b = new byte[64];
        byte[] c = new byte[1024];

        //BASTORE
        for (i = 0; i < 1024; i++){
            c[i] = (byte)(i & 0xff);
        }

        //BALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (byte)(i & 0xff));
        }
        return res;
    }

    public boolean testCharArray(){
        boolean res = true;
        int i = 0;

        //newarray for char
        char[] a = new char[2];
        char[] b = new char[64];
        char[] c = new char[1024];

        //CASTORE
        for (i = 0; i < 1024; i++){
            c[i] = (char)(i & 0xffff);;
        }

        //CALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (char)(i & 0xffff));
        }
        return res;
    }

    public boolean testDoubleArray(){
        boolean res = true;
        int i = 0;

        //newarray for double
        double[] a = new double[2];
        double[] b = new double[64];
        double[] c = new double[1024];

        //DASTORE
        for (i = 0; i < 1024; i++){
            c[i] = ((double) i) / 3;
        }

        //DALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (((double) i) / 3));
        }
        return res;
    }

    public boolean testFloatArray(){
        boolean res = true;
        int i = 0;

        //newarray for float
        float[] a = new float[2];
        float[] b = new float[64];
        float[] c = new float[1024];

        //FASTORE
        for (i = 0; i < 1024; i++){
            c[i] = ((float) i) / 3;
        }

        //FALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (((float) i) / 3));
        }
        return res;
    }

    public boolean testIntArray(){
        boolean res = true;
        int i = 0;

        //newarray for int
        int[] a = new int[2];
        int[] b = new int[64];
        int[] c = new int[1024];

        //IASTORE
        for (i = 0; i < 1024; i++){
            c[i] = i;
        }

        //IALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == i);
        }
        return res;
    }

    public boolean testLongArray(){
        boolean res = true;
        int i = 0;

        //newarray for long
        long[] a = new long[2];
        long[] b = new long[64];
        long[] c = new long[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            c[i] = (long) i;
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (long) i);
        }
        return res;
    }

    public boolean testShortArray(){
        boolean res = true;
        int i = 0;

        //newarray for long
        short[] a = new short[2];
        short[] b = new short[64];
        short[] c = new short[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            c[i] = (short) i;
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            res = res && (c[i] == (short) i);
        }
        return res;
    }

}
