package org.aion.avm.embed.arraywrapping;

import avm.Address;
import org.aion.avm.tooling.abi.Callable;

public class ArrayEnergyTarget {
    @Callable
    public static void initArray1Dim(){
        boolean[]   a = new boolean[100];
        byte[]      b = new byte[100];
        char[]      c = new char[100];
        double[]    d = new double[100];
        float[]     e = new float[100];
        int[]       f = new int[100];
        long[]      g = new long[100];
        short[]     h = new short[100];
        Object[]    i = new Object[100];
        Address[]   j = new Address[100];
        String[]    k = new String[100];
    }

    @Callable
    public static void initArrayMultiDim(){
        boolean[][]   a = new boolean[100][];
        byte[][]      b = new byte[100][1];
        char[][]      c = new char[100][1];
        double[][]    d = new double[100][1];
        float[][]     e = new float[100][1];
        int[][]       f = new int[100][1];
        long[][]      g = new long[100][];
        short[][]     h = new short[100][];
        Object[][]    i = new Object[100][];
        Address[][]   j = new Address[100][];
        String[][]    k = new String[100][];
        String[][][]  l = new String[100][1][];
        char[][][]    m = new char[100][1][1];
        int[][][]     n = new int[100][][];
    }

    @Callable
    public static void cloneMultiDim(){
        boolean[][]   a = new boolean[10][];
        byte[][]      b = new byte[10][1];
        char[][]      c = new char[10][1];
        double[][]    d = new double[10][1];
        float[][]     e = new float[10][1];
        int[] []      f = new int[10][1];
        long[][]      g = new long[10][];
        short[][]     h = new short[10][];
        Object[][]    i = new Object[10][];
        Address[][]   j = new Address[10][];
        String[][]    k = new String[10][];
        String[][][]  l = new String[10][1][];
        char[][][]    m = new char[10][1][1];
        int[][][]     n = new int[10][][];
        a.clone();
        b.clone();
        c.clone();
        d.clone();
        e.clone();
        f.clone();
        g.clone();
        h.clone();
        i.clone();
        k.clone();
        j.clone();
        l.clone();
        m.clone();
        n.clone();
    }
}
