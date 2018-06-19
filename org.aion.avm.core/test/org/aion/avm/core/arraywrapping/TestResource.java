package org.aion.avm.core.arraywrapping;

import org.aion.avm.arraywrapper.ByteArray;

public class TestResource {

    public interface X {}
    public interface Y {}
    public interface Z extends X, Y{}

    public static class A implements Z{}
    public static class B extends A {}
    public static class C extends B {}

    private Object[] oa;
    private int[] oi;
    public static int[] ois;
    public String[][][] fieldMDString;
    public int[][][] fieldMDInt;

    public boolean testBasic(){
        boolean res = true;
        int[] arr = new int[20];
        int b = arr[2];
        arr[2] = 4;
        return res;
    }

    public boolean testSignature(){
        boolean[]   a = new boolean[42];
        byte[]      b = new byte[42];
        char[]      c = new char[42];
        double[]    d = new double[42];
        float[]     e = new float[42];
        int[]       f = new int[42];
        long[]      g = new long[42];
        short[]     h = new short[42];
        Object[]    i = new Object[42];
        String[]    j = new String[42];
        String[][]          k = new String[42][1];
        String[][][][][]    l = new String[42][1][1][1][1];
        return a == this.testMixedSignature(a, b, c, d, e, f, g, h, i, j, k, l);
    }

    public boolean[] testMixedSignature(boolean[] in1, byte[] in2, char[] in3,
                                        double[] in4, float[] in5, int[] in6,
                                        long[] in7, short[] in8, Object[] in9,
                                        String[] in10, String[][] in11, String[][][][][] in12)
    {
        return in1;
    }

    public boolean[] testBooleanSignature(boolean[] in){
        return in;
    }

    public byte[] testByteSignature(byte[] in){
        return in;
    }

    public char[] testCharSignature(char[] in){
        return in;
    }

    public double[] testDoubleSignature(double[] in){
        return in;
    }

    public float[] testFloatSignature(float[] in){
        return in;
    }

    public int[] testIntSignature(int[] in){
        return in;
    }

    public long[] testLongSignature(long[] in){
        return in;
    }

    public Object[] testObjectSignature(Object[] in){
         return in;
    }

    public short[] testShortSignature(short[] in){
        return in;
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

    public boolean testObjectArray(){
        boolean res = true;
        int count = 0;
        int i = 0;

        //newarray for long
        Object[] a = new Object[2];
        Object[] b = new Object[64];
        Object[] c = new Object[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            c[i] = new Object();
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            count = count + c[i].hashCode();
        }

        res = (count != 0);

        return res;
    }

     public boolean testStringArray(){
         boolean res = true;
         int count = 0;
         int i = 0;

         //newarray for long
         String[] a = new String[2];
         String[] b = new String[64];
         String[] c = new String[1024];

         //LASTORE
         for (i = 0; i < 1024; i++){
             c[i] = new String(Integer.toString(i));
         }

         //LALOAD
         for (i = 0; i < 1024; i++){
             //count = count + c[i].indexOf('1');
             count = count + c[i].hashCode();
         }


         res = (count != 0);

         return res;
     }


    public boolean testVarargs(){
        int a = varargsHelper(1);
        int b = varargsHelper(1,2,3,4,5,6,7,8,9,10);
        int c = varargsHelper();

        boolean res = ((a == 1) && (b == 55) && (c == 0));
        return res;
    }

    private int varargsHelper(int ...a){
        int c = 0;
        for (int i:a){
            c = c + i;
        }
        return c;
    }

    public boolean testTypeChecking(){
        int[] a = new int[10];
        Object b = (Object) a;
        int[] c = (int[]) b;

        return (c instanceof int[]) && (c instanceof java.lang.Object);
    }

    public boolean testClassField(){
        oi = new int[50];
        oi[20] = 1;
        int a = oi[20];

        ois = new int[50];
        ois[20] = 1;
        int b = ois[20];

        fieldMDInt = new int[10][10][10];
        fieldMDInt[5][5][5] = 10;
        int c = fieldMDInt[5][5][5];

        fieldMDString = new String[10][10][10];
        fieldMDString[5][5][5] = "Bomb";
        String d = fieldMDString[5][5][5];

        return (a == 1) && (oi instanceof int[]) && (b == 1) && (ois instanceof int[]) && (c == 10) && (d.equals("Bomb"));
    }

    public boolean testMultiInt(){
        boolean ret = true;

        int[][][] i3 = new int[3][3][3];
        i3[1][1][1] = 1;
        i3[2][2][2] = 8;

        ret = ret && (i3[1][1][1] == 1);
        ret = ret && (i3[2][2][2] == 8);

        int[][] i2 = new int[3][3];
        i2[2][2] = 7;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 7);

        return ret;
    }

    public boolean testMultiByte(){
        boolean ret = true;

        byte[][][] i3 = new byte[3][3][3];
        i3[1][1][1] = 0xA;
        i3[2][2][2] = 0XB;

        ret = ret && (i3[1][1][1] == 0xA);
        ret = ret && (i3[2][2][2] == 0XB);

        byte[][] i2 = new byte[3][3];
        i2[2][2] = 0XC;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 0XC);

        return ret;
    }

    public boolean testMultiChar(){
        boolean ret = true;

        char[][][] i3 = new char[3][3][3];
        i3[1][1][1] = 'A';
        i3[2][2][2] = 'B';

        ret = ret && (i3[1][1][1] == 'A');
        ret = ret && (i3[2][2][2] == 'B');

        char[][] i2 = new char[3][3];
        i2[2][2] = 'C';
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 'C');

        return ret;
    }

    public boolean testMultiDouble(){
        boolean ret = true;

        double[][][] i3 = new double[3][3][3];
        i3[1][1][1] = 1 / 3;
        i3[2][2][2] = 2 / 3;

        ret = ret && (i3[1][1][1] == 1 / 3);
        ret = ret && (i3[2][2][2] == 2 / 3);

        double[][] i2 = new double[3][3];
        i2[2][2] = 1 / 3;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 1 / 3);

        return ret;
    }

    public boolean testMultiFloat(){
        boolean ret = true;

        float[][][] i3 = new float[3][3][3];
        i3[1][1][1] = 1 / 3;
        i3[2][2][2] = 2 / 3;

        ret = ret && (i3[1][1][1] == 1 / 3);
        ret = ret && (i3[2][2][2] == 2 / 3);

        float[][] i2 = new float[3][3];
        i2[2][2] = 1 / 3;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 1 / 3);

        return ret;
    }

    public boolean testMultiLong(){
        boolean ret = true;

        long[][][] i3 = new long[3][3][3];
        i3[1][1][1] = 111111111;
        i3[2][2][2] = 222222222;

        ret = ret && (i3[1][1][1] == 111111111);
        ret = ret && (i3[2][2][2] == 222222222);

        long[][] i2 = new long[3][3];
        i2[2][2] = 333333333;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 333333333);

        return ret;
    }

    public boolean testMultiShort(){
        boolean ret = true;

        short[][][] i3 = new short[3][3][3];
        i3[1][1][1] = 1;
        i3[2][2][2] = 2;

        ret = ret && (i3[1][1][1] == 1);
        ret = ret && (i3[2][2][2] == 2);

        short[][] i2 = new short[3][3];
        i2[2][2] = 3;
        i3[2] = i2;
        ret = ret && (i3[2][2][2] == 3);

        return ret;
    }

    public boolean testMultiRef(){
        boolean ret = true;

        String[][][] s3 = new String[3][3][3];
        s3[1][1][1] = "Hello";
        s3[2][2][2] = "World";

        ret = ret && s3[1][1][1].startsWith("Hello");
        ret = ret && s3[2][2][2].startsWith("World");

        String[][] s2 = new String[3][3];
        s2[2][2] = "Canada";
        s3[2] = s2;
        ret = ret && s3[2][2][2].startsWith("Canada");

        return ret;
    }

    public boolean testHierarachy(){
        boolean ret = true;

        A[][][] a = new A[5][5][5];
        a[1][1][1] = new A();

        B[][][] b = new B[5][5][5];
        b[1][1][1] = new B();

        C[][][] c = new C[5][5][5];
        c[1][1][1] = new C();

        ret = ret && (c instanceof A[][][]) && (c instanceof B[][][]) && (b instanceof A[][][]);
        ret = ret && (!(a instanceof C[][][])) && (!(a instanceof B[][][])) && (!(b instanceof C[][][]));

        a = b;
        a = c;
        b = c;

        a[1] = b[2];
        b[1] = c[1];

        a[1][1] = b[2][2];
        b[1][1] = c[1][1];
        b[1][1] = c[1][1];

        int[][][] d = new int[5][5][5];
        d[1][1][1] = 1;

        int[][][] e = new int[5][5][5];
        e[1][1][1] = 2;

        d[1] = e[2];
        d[1][1] = e[2][2];

        return ret;
    }

    public boolean testArrayEnergy(){
        int[][][][] s = new int[10][10][10][10];
        return true;
    }

    public boolean testIncompleteArrayIni(){
        int[][][][] s;
        s = new int[10][10][10][];
        s = new int[10][10][][];
        s = new int[10][][][];

        String[][][][] ss;
        ss = new String[10][10][10][];
        ss = new String[10][10][][];
        ss = new String[10][][][];

        return true;
    }

    public boolean testInterfaceArray(){
        X[][][] xxx = new X[5][5][5];
        Y[][][] yyy = new Y[5][5][5];
        Z[][][] zzz = new Z[5][5][5];
        A[][][] aaa = new A[5][5][5];
        B[][][] bbb = new B[5][5][5];
        C[][][] ccc = new C[5][5][5];

        xxx[0][0][0] = new A();
        xxx[0][0][1] = new B();
        xxx[0][0][2] = new C();

        yyy[0][0][0] = new A();
        yyy[0][0][1] = new B();
        yyy[0][0][2] = new C();

        zzz[0][0][0] = new A();
        zzz[0][0][1] = new B();
        zzz[0][0][2] = new C();

        xxx = aaa;
        yyy = bbb;
        zzz = ccc;

        return true;
    }

    public boolean testArrayClone(){
        byte[] ba = new byte[10];
        byte[] cp = ba.clone();
        return true;
    }

}
