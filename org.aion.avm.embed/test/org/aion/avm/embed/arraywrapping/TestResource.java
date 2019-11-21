package org.aion.avm.embed.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

import avm.Blockchain;

public class TestResource {

    public interface X {}
    public interface Y {}
    public interface Z extends X, Y{}

    public static class A implements Z{}
    public static class B extends A {}
    public static class C extends B {}

    private static Object[] oa;
    private static int[] oi;
    public static int[] ois;
    public static String[][][] fieldMDString;
    public static int[][][] fieldMDInt;

    public boolean testBasic(){
        boolean res = true;
        int[] arr = new int[20];
        int b = arr[2];
        Blockchain.require(0 == b);
        arr[2] = 4;
        return res;
    }

    @Callable
    public static boolean testSignature(){
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
        char[][][]    l = new char[42][1][1];
        return a == testMixedSignature(a, b, c, d, e, f, g, h, i, j, k, l);
    }

    public static boolean[] testMixedSignature(boolean[] in1, byte[] in2, char[] in3,
                                        double[] in4, float[] in5, int[] in6,
                                        long[] in7, short[] in8, Object[] in9,
                                        String[] in10, String[][] in11, char[][][] in12)
    {
        return in1;
    }

    @Callable
    public static boolean[] testBooleanSignature(boolean[] in){
        return in;
    }

    @Callable
    public static byte[] testByteSignature(byte[] in){
        return in;
    }

    @Callable
    public static char[] testCharSignature(char[] in){
        return in;
    }

    @Callable
    public static double[] testDoubleSignature(double[] in){
        return in;
    }

    @Callable
    public static float[] testFloatSignature(float[] in){
        return in;
    }

    @Callable
    public static int[] testIntSignature(int[] in){
        return in;
    }

    @Callable
    public static long[] testLongSignature(long[] in){
        return in;
    }

    @Callable
    public static short[] testShortSignature(short[] in){
        return in;
    }

    @Callable
    public static int[][] testInt2DArray(){ return new int[][] {{1, 2}, {3, 4}}; }


    @Callable
    public static boolean testBooleanArray(){
        boolean res = true;
        int i = 0;

        //newarray for byte
        boolean[] a = new boolean[2];
        boolean[] b = new boolean[64];
        boolean[] c = new boolean[1024];

        //BASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = ((i % 2) == 0);
                }
                b[i] = ((i % 2) == 0);
            }
            c[i] = ((i % 2) == 0);
        }

        //BALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == ((i % 2) == 0));
                }
                res = res && (b[i] == ((i % 2) == 0));
            }
            res = res && (c[i] == ((i % 2) == 0));
        }
        return res;
    }

    @Callable
    public static boolean testByteArray(){
        boolean res = true;
        int i = 0;

        //newarray for byte
        byte[] a = new byte[2];
        byte[] b = new byte[64];
        byte[] c = new byte[1024];

        //BASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = (byte)(i & 0xff);
                }
                b[i] = (byte)(i & 0xff);
            }
            c[i] = (byte)(i & 0xff);
        }

        //BALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (byte)(i & 0xff));
                }
                res = res && (b[i] == (byte)(i & 0xff));
            }
            res = res && (c[i] == (byte)(i & 0xff));
        }
        return res;
    }

    @Callable
    public static boolean testCharArray(){
        boolean res = true;
        int i = 0;

        //newarray for char
        char[] a = new char[2];
        char[] b = new char[64];
        char[] c = new char[1024];

        //CASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = (char)(i & 0xffff);
                }
                b[i] = (char)(i & 0xffff);
            }
            c[i] = (char)(i & 0xffff);
        }

        //CALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (char)(i & 0xffff));
                }
                res = res && (b[i] == (char)(i & 0xffff));
            }
            res = res && (c[i] == (char)(i & 0xffff));
        }
        return res;
    }

    @Callable
    public static boolean testDoubleArray(){
        boolean res = true;
        int i = 0;

        //newarray for double
        double[] a = new double[2];
        double[] b = new double[64];
        double[] c = new double[1024];

        //DASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = ((double) i) / 3;
                }
                b[i] = ((double) i) / 3;
            }
            c[i] = ((double) i) / 3;
        }

        //DALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (((double) i) / 3));
                }
                res = res && (b[i] == (((double) i) / 3));
            }
            res = res && (c[i] == (((double) i) / 3));
        }
        return res;
    }

    @Callable
    public static boolean testFloatArray(){
        boolean res = true;
        int i = 0;

        //newarray for float
        float[] a = new float[2];
        float[] b = new float[64];
        float[] c = new float[1024];

        //FASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = ((float) i) / 3;
                }
                b[i] = ((float) i) / 3;
            }
            c[i] = ((float) i) / 3;
        }

        //FALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (((float) i) / 3));
                }
                res = res && (b[i] == (((float) i) / 3));
            }
            res = res && (c[i] == (((float) i) / 3));
        }
        return res;
    }

    @Callable
    public static boolean testIntArray(){
        boolean res = true;
        int i = 0;

        //newarray for int
        int[] a = new int[2];
        int[] b = new int[64];
        int[] c = new int[1024];

        //IASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = i;
                }
                b[i] = i;
            }
            c[i] = i;
        }

        //IALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == i);
                }
                res = res && (b[i] == i);
            }
            res = res && (c[i] == i);
        }
        return res;
    }

    @Callable
    public static boolean testLongArray(){
        boolean res = true;
        int i = 0;

        //newarray for long
        long[] a = new long[2];
        long[] b = new long[64];
        long[] c = new long[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = (long) i;
                }
                b[i] = (long) i;
            }
            c[i] = (long) i;
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (long) i);
                }
                res = res && (b[i] == (long) i);
            }
            res = res && (c[i] == (long) i);
        }
        return res;
    }

    @Callable
    public static boolean testShortArray(){
        boolean res = true;
        int i = 0;

        //newarray for long
        short[] a = new short[2];
        short[] b = new short[64];
        short[] c = new short[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = (short) i;
                }
                b[i] = (short) i;
            }
            c[i] = (short) i;
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    res = res && (a[i] == (short) i);
                }
                res = res && (b[i] == (short) i);
            }
            res = res && (c[i] == (short) i);
        }
        return res;
    }

    @Callable
    public static boolean testObjectArray(){
        boolean res = true;
        int count = 0;
        int i = 0;

        //newarray for long
        Object[] a = new Object[2];
        Object[] b = new Object[64];
        Object[] c = new Object[1024];

        //LASTORE
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    a[i] = new Object();
                }
                b[i] = new Object();
            }
            c[i] = new Object();
        }

        //LALOAD
        for (i = 0; i < 1024; i++){
            if (i < b.length) {
                if (i < a.length) {
                    count = count + a[i].hashCode();
                }
                count = count + b[i].hashCode();
            }
            count = count + c[i].hashCode();
        }

        res = (count != 0);

        return res;
    }

    @Callable
     public static boolean testStringArray(){
         boolean res = true;
         int count = 0;
         int i = 0;

         //newarray for long
         String[] a = new String[2];
         String[] b = new String[64];
         String[] c = new String[1024];

         //LASTORE
         for (i = 0; i < 1024; i++){
             if (i < b.length) {
                 if (i < a.length) {
                     a[i] = new String(Integer.toString(i));
                 }
                 b[i] = new String(Integer.toString(i));
             }
             c[i] = new String(Integer.toString(i));
         }

         //LALOAD
         for (i = 0; i < 1024; i++){
             if (i < b.length) {
                 if (i < a.length) {
                     count = count + a[i].hashCode();
                 }
                 count = count + b[i].hashCode();
             }
             count = count + c[i].hashCode();
         }


         res = (count != 0);

         return res;
     }


    @Callable
    public static boolean testVarargs(){
        int a = varargsHelper(1);
        int b = varargsHelper(1,2,3,4,5,6,7,8,9,10);
        int c = varargsHelper();

        boolean res = ((a == 1) && (b == 55) && (c == 0));
        return res;
    }

    private static int varargsHelper(int ...a){
        int c = 0;
        for (int i:a){
            c = c + i;
        }
        return c;
    }

    @Callable
    public static boolean testTypeChecking(){
        int[] a = new int[10];
        Object b = (Object) a;
        int[] c = (int[]) b;

        return (c instanceof int[]) && (c instanceof java.lang.Object);
    }

    @Callable
    public static boolean testClassField(){
        oa = new Object[50];
        oa[20] = new Object();
        Object a = oa[20];
        
        oi = new int[50];
        oi[20] = 1;
        int b = oi[20];

        ois = new int[50];
        ois[20] = 1;
        int c = ois[20];

        fieldMDInt = new int[10][10][10];
        fieldMDInt[5][5][5] = 10;
        int d = fieldMDInt[5][5][5];

        fieldMDString = new String[10][10][10];
        fieldMDString[5][5][5] = "Bomb";
        String e = fieldMDString[5][5][5];

        return (null != a) && (oa instanceof Object[]) && (b == 1) && (oi instanceof int[]) && (c == 1) && (ois instanceof int[]) && (d == 10) && (e.equals("Bomb"));
    }

    @Callable
    public static boolean testMultiInt(){
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

    @Callable
    public static boolean testMultiByte(){
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

    @Callable
    public static boolean testMultiChar(){
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

    @Callable
    public static boolean testMultiDouble(){
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

    @Callable
    public static boolean testMultiFloat(){
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

    @Callable
    public static boolean testMultiLong(){
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

    @Callable
    public static boolean testMultiRef(){
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

    @Callable
    public static boolean testHierarachy(){
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

    @Callable
    public static boolean testArrayEnergy(){
        int[][][] s = new int[10][10][10];
        Blockchain.require(10 == s.length);
        return true;
    }

    @Callable
    public static boolean testIncompleteArrayIni(){
        int[][][] s;
        s = new int[10][10][];
        Blockchain.require(10 == s.length);
        s = new int[10][][];
        Blockchain.require(10 == s.length);

        String[][][] ss;
        ss = new String[10][10][];
        Blockchain.require(10 == ss.length);
        ss = new String[10][][];
        Blockchain.require(10 == ss.length);

        return true;
    }

    @Callable
    public static boolean testInterfaceArray(){
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

    @Callable
    public static boolean testArrayClone(){
        byte[] ba = new byte[10];
        byte[] bcp = ba.clone();
        Blockchain.require(ba.length == bcp.length);

        char[] ca = new char[11];
        char[] ccp = ca.clone();
        Blockchain.require(ca.length == ccp.length);

        double[] da = new double[12];
        double[] dcp = da.clone();
        Blockchain.require(da.length == dcp.length);

        float[] fa = new float[13];
        float[] fcp = fa.clone();
        Blockchain.require(fa.length == fcp.length);

        int[] ia = new int[14];
        int[] icp = ia.clone();
        Blockchain.require(ia.length == icp.length);

        long[] ja = new long[15];
        long[] jcp = ja.clone();
        Blockchain.require(ja.length == jcp.length);

        String[] osa = new String[16];
        String[] oscp = osa.clone();
        Blockchain.require(osa.length == oscp.length);

        String[][] osa2 = new String[17][18];
        String[][] osa2cp = osa2.clone();
        Blockchain.require(osa2.length == osa2cp.length);

        short[] sa = new short[19];
        short[] scp = sa.clone();
        Blockchain.require(sa.length == scp.length);

        byte[][][] bamd = new byte[20][21][22];
        byte[][][] bmdcp = bamd.clone();
        Blockchain.require(bamd.length == bmdcp.length);
        byte[][] bamdcp2 = bamd[1].clone();
        Blockchain.require(bamd[1].length == bamdcp2.length);

        String[][][] osamd = new String[23][24][25];
        String[][][] osmdcp = osamd.clone();
        Blockchain.require(osamd.length == osmdcp.length);
        String[][] osmdcp2 = osamd[1].clone();
        Blockchain.require(osamd[1].length == osmdcp2.length);

        return true;
    }

}
