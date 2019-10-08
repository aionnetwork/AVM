package org.aion.avm.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import org.aion.avm.utilities.Utilities;
import org.aion.avm.utilities.analyze.ClassFileInfoBuilder;


/**
 * Collects information based on deployed classes against the running instance and can dump that information to a PrintStream, later.
 * The data is structured as a statically formed histogram so that the storage overhead required is constant, no matter the number
 * of samples provided.
 */
public class HistogramDataCollector {
    /*
     * These limits represent the static buckets we will use to organize the samples for each value being measured.
     * We check to see if the sample is _in_ the bucket at a given index by checking that it is <= the corresponding limit.
     */
    // JAR-level data.
    private static final int[] LIMIT_jarSize = new int[]        {16_000, 32_000, 64_000,  128_000, 256_000, 512_000, 1_024_000 };
    private static final int[] LIMIT_classCount = new int[]     {0,      2,      4,       8,       16,      32,      64        };

    // Class-level data.
    private static final int[] LIMIT_classSize = new int[]      {256,    512,    1024,    2048,    4096,    8192,    16384     };
    private static final int[] LIMIT_cpEntryCount = new int[]   {0,      64,     128,     256,     512,     1024,    2048      };
    private static final int[] LIMIT_methodCount = new int[]    {0,      2,      4,       8,       16,      32,      64        };
    private static final int[] LIMIT_fieldCount = new int[]     {0,      2,      4,       8,       16,      32,      64        };

    // Method-level data.
    private static final int[] LIMIT_maxStack = new int[]       {0,      1,      2,       4,       8,       16,      32        };
    private static final int[] LIMIT_maxLocals = new int[]      {0,      1,      2,       4,       8,       16,      32        };
    private static final int[] LIMIT_codeSize = new int[]       {64,     128,    256,     512,     1024,    2048,    4096      };
    private static final int[] LIMIT_exceptionCount = new int[] {0,      1,      2,       4,       8,       16,      32        };


    private final PrintStream deploymentDataHistorgramOutput;
    private final Object lock;

    // Regarding how we store the actual histogram samples:  we store 1 for each entry in the limits (for <= the limit), 1 for the number over the limit, and 1 for the max value.
    // JAR-level.
    private int readJarCount;
    private int corruptedJarCount;
    private int[] jarSize = new int[LIMIT_jarSize.length + 2];
    private int[] classCount = new int[LIMIT_classCount.length + 2];

    // Class-level.
    private int[] classSize = new int[LIMIT_classSize.length + 2];
    private int[] cpEntryCount = new int[LIMIT_cpEntryCount.length + 2];
    private int[] methodCount = new int[LIMIT_methodCount.length + 2];
    private int[] fieldCount = new int[LIMIT_fieldCount.length + 2];

    // Method-level.
    private int[] maxStack = new int[LIMIT_maxStack.length + 2];
    private int[] maxLocals = new int[LIMIT_maxLocals.length + 2];
    private int[] codeSize = new int[LIMIT_codeSize.length + 2];
    private int[] exceptionCount = new int[LIMIT_exceptionCount.length + 2];


    public HistogramDataCollector(PrintStream deploymentDataHistorgramOutput) {
        this.deploymentDataHistorgramOutput = deploymentDataHistorgramOutput;
        this.lock = new Object();
    }

    public void collectDataFromJarBytes(byte[] jarBytes) {
        ClassFileInfoBuilder.ClassFileInfo[] classes = null;
        try {
            JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);
            Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.SLASH_NAME);
            classes = new ClassFileInfoBuilder.ClassFileInfo[classMap.size()];
            int index = 0;
            for (Map.Entry<String, byte[]> classEntry : classMap.entrySet()) {
                // We call the direct variant since we expect failure to read the class as a corrupt JAR.
                classes[index] = ClassFileInfoBuilder.getDirectClassFileInfo(classEntry.getValue());
                index += 1;
            }
        } catch (IOException e) {
            // This is in-memory so IOException is not possible.
            throw new RuntimeException(e);
        } catch (Throwable t) {
            // If something else happened, it probably means the JAR is corrupt.
            classes = null;
        }
        
        // Now that we have all the data, lock and account.
        synchronized (this.lock) {
            this.readJarCount += 1;
            if (null != classes) {
                // We only collect measurements for valid jars.
                // Collect the per-JAR data.
                addCount(LIMIT_jarSize, this.jarSize, jarBytes.length);
                addCount(LIMIT_classCount, this.classCount, classes.length);
                for (ClassFileInfoBuilder.ClassFileInfo info : classes) {
                    // Collect the per-class data.
                    addCount(LIMIT_classSize, this.classSize, info.classFileLength);
                    addCount(LIMIT_cpEntryCount, this.cpEntryCount, info.constantPoolEntryCount);
                    List<ClassFileInfoBuilder.MethodCode> definedMethods = info.definedMethods;
                    addCount(LIMIT_methodCount, this.methodCount, definedMethods.size());
                    addCount(LIMIT_fieldCount, this.fieldCount, info.instanceFieldCount);
                    for (ClassFileInfoBuilder.MethodCode method : definedMethods) {
                        // Collect the per-method data.
                        addCount(LIMIT_maxStack, this.maxStack, method.maxStack);
                        addCount(LIMIT_maxLocals, this.maxLocals, method.maxLocals);
                        addCount(LIMIT_codeSize, this.codeSize, method.codeLength);
                        addCount(LIMIT_exceptionCount, this.exceptionCount, method.exceptionTableSize);
                    }
                }
            } else {
                this.corruptedJarCount += 1;
            }
        }
    }

    public void dumpData() {
        this.deploymentDataHistorgramOutput.println("*****   REPORT   *****");
        this.deploymentDataHistorgramOutput.println("JARs: " + this.readJarCount + " processed, " + this.corruptedJarCount + " corrupted");
        printSequence("JAR Size        ", LIMIT_jarSize, this.jarSize);
        printSequence("Class Count     ", LIMIT_classCount, this.classCount);
        this.deploymentDataHistorgramOutput.println();
        printSequence("Class Size      ", LIMIT_classSize, this.classSize);
        printSequence("CP Entry Count  ", LIMIT_cpEntryCount, this.cpEntryCount);
        printSequence("Method Count    ", LIMIT_methodCount, this.methodCount);
        printSequence("Field Count     ", LIMIT_fieldCount, this.fieldCount);
        this.deploymentDataHistorgramOutput.println();
        printSequence("Max Stack       ", LIMIT_maxStack, this.maxStack);
        printSequence("Max Locals      ", LIMIT_maxLocals, this.maxLocals);
        printSequence("Code Size       ", LIMIT_codeSize, this.codeSize);
        printSequence("Exception Count ", LIMIT_exceptionCount, this.exceptionCount);
        this.deploymentDataHistorgramOutput.println("*****    END    *****");
    }


    private void addCount(int[] legend, int[] counters, int count) {
        boolean found = false;
        for (int i = 0; !found && (i < legend.length); ++i) {
            if (count <= legend[i]) {
                counters[i] += 1;
                found = true;
            }
        }
        if (!found) {
            // Populate the overflow.
            counters[legend.length] += 1;
        }
        // Populate the max.
        counters[counters.length-1] = Math.max(count, counters[counters.length-1]);
    }

    private void printSequence(String name, int[] limits, int[] counts) {
        this.deploymentDataHistorgramOutput.print("\t" + name);
        for (int limit : limits) {
            this.deploymentDataHistorgramOutput.print("\t<=" + limit);
        }
        this.deploymentDataHistorgramOutput.println("\tOVER\tMAX");
        this.deploymentDataHistorgramOutput.print("\t");
        for (int i = 0; i < name.length(); ++i) {
            this.deploymentDataHistorgramOutput.print(" ");
        }
        for (int count : counts) {
            this.deploymentDataHistorgramOutput.print("\t" + count);
        }
        this.deploymentDataHistorgramOutput.println();
    } 
}
