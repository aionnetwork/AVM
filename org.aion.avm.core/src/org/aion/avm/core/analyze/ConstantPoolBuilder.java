package org.aion.avm.core.analyze;

import java.util.HashMap;
import java.util.Map;

import i.RuntimeAssertionError;

public class ConstantPoolBuilder {
    public static final Object lock = new Object();
    public static int classCount;
    public static int failCount;
    public static final int[] LIMIT_constantCount = new int[] {0, 64, 128, 256, 512, 1024};
    public static int[] constantCount = new int[LIMIT_constantCount.length + 2];
    public static final int[] LIMIT_methodCount = new int[] {0, 16, 32, 64, 128, 256};
    public static int[] methodCount = new int[LIMIT_methodCount.length + 2];
    public static final int[] LIMIT_interfaceCount = new int[] {0, 1, 2, 4, 8, 16};
    public static int[] interfaceCount = new int[LIMIT_interfaceCount.length + 2];
    public static final int[] LIMIT_fieldCount = new int[] {0, 2, 4, 8, 16, 32, 64};
    public static int[] fieldCount = new int[LIMIT_fieldCount.length + 2];
    public static final int[] LIMIT_codeSize = new int[] {512, 1024, 2048, 4096, 8192, 16384};
    public static int[] codeSize = new int[LIMIT_codeSize.length + 2];
    public static final int[] LIMIT_exceptionCount = new int[] {0, 2, 4, 8, 16, 32};
    public static int[] exceptionCount = new int[LIMIT_exceptionCount.length + 2];
    public static final int[] LIMIT_classSize = new int[] {512, 1024, 2048, 4096, 8192, 16384};
    public static int[] classSize = new int[LIMIT_classSize.length + 2];
    public static final int[] LIMIT_jarSize = new int[] {32_000, 64_000, 125_000, 250_000, 500_000, 1_000_000};
    public static int[] jarSize = new int[LIMIT_jarSize.length + 2];
    
    public static ClassConstantSizeInfo getConstantPoolInfo(byte[] classFile) {
        try {
            return internalPoolInfo(classFile);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            failCount += 1;
            return null;
        }
    }
    
    private static ClassConstantSizeInfo internalPoolInfo(byte[] classFile) {
        // https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-4.html
        
        Map<String, Integer> constantTypeCount = new HashMap<>();
        int totalUtf8Length = 0;
        int codeIndex = -1;

        ByteReader reader = new ByteReader(classFile);
        // magic
        reader.readU4();
        //minorVersion
        reader.readU2();
        // majorVersion
        reader.readU2();

        int constantPoolCount = reader.readU2() - 1;
        for (int i = 0; i < constantPoolCount; i++) {
            int tag = reader.readU1();
            ConstantType constantType = ConstantType.forTag(tag);
            try {
                switch (constantType) {
                    case CONSTANT_CLASS:
                    case CONSTANT_METHOD_TYPE:
                    case CONSTANT_STRING:
                        reader.readU2();
                        break;
                    case CONSTANT_DOUBLE:
                    case CONSTANT_LONG:
                        reader.readU4();
                        reader.readU4();
                        // the next usable item in the pool is located at index n+2
                        i++;
                        break;
                    case CONSTANT_FIELDREF:
                    case CONSTANT_METHODREF:
                    case CONSTANT_NAME_AND_TYPE:
                    case CONSTANT_INVOKE_DYNAMIC:
                    case CONSTANT_INTERFACE_METHODREF:
                        reader.readU2();
                        reader.readU2();
                        break;
                    case CONSTANT_INTEGER:
                    case CONSTANT_FLOAT:
                        reader.readU4();
                        break;
                    case CONSTANT_UTF8:
                        int length = reader.readU2();
                        totalUtf8Length += length;
                        byte[] raw = reader.readNBytes(length);
                        // We need to read the buffer to advance, but we are also looking for "Code".
                        if ("Code".equals(new String(raw))) {
                            codeIndex = i;
                        }
                        break;
                    case CONSTANT_METHOD_HANDLE:
                        reader.readU1();
                        reader.readU2();
                        break;
                }
            } catch (Exception e) {
                throw new AssertionError("Could not find constant pool tag " + tag);
            }
            constantTypeCount.put(constantType.name, constantTypeCount.getOrDefault(constantType.name, 0) + 1);
        }
//        RuntimeAssertionError.assertTrue(-1 != codeIndex);
        
        //access_flags
        reader.readU2();
        //this_class
        reader.readU2();
        //super_class
        reader.readU2();
        
        //interfaces_count
        int interfaceCount = reader.readU2();
        for (int i = 0; i < interfaceCount; ++i) {
            //interfaces[interfaces_count]
            reader.readU2();
        }
        
        //fields_count
        int fieldCount = reader.readU2();
        for (int i = 0; i < fieldCount; ++i) {
            //fields[fields_count]
            readFieldInfo(reader);
        }
        //methods_count
        int methodCount = reader.readU2();
        int implementedMethodCount = 0;
        for (int i = 0; i < methodCount; ++i) {
            //methods[methods_count]
            // Note that constant pool entries are 1-indexed, so add 1 to the code index as that is how it will be referenced.
            boolean isImplemented = readMethodInfo(reader, codeIndex + 1);
            if (isImplemented) {
                implementedMethodCount += 1;
            }
        }
        //attributes_count
        int attributeCount = reader.readU2();
        for (int i = 0; i < attributeCount; ++i) {
            //attributes[attributes_count]
            readAttributeInfo(reader, -1);
        }
        synchronized (lock) {
            ConstantPoolBuilder.classCount += 1;
            addCount(ConstantPoolBuilder.LIMIT_constantCount, ConstantPoolBuilder.constantCount, constantPoolCount);
            addCount(ConstantPoolBuilder.LIMIT_interfaceCount, ConstantPoolBuilder.interfaceCount, interfaceCount);
            addCount(ConstantPoolBuilder.LIMIT_fieldCount, ConstantPoolBuilder.fieldCount, fieldCount);
            // FYI: excludes abstract.
            addCount(ConstantPoolBuilder.LIMIT_methodCount, ConstantPoolBuilder.methodCount, implementedMethodCount);
        }
        
        return new ClassConstantSizeInfo(classFile.length, constantTypeCount, totalUtf8Length, reader.position());
    }


    private static void readFieldInfo(ByteReader reader) {
        // access_flags.
        reader.readU2();
        // name_index.
        reader.readU2();
        // descriptor_index.
        reader.readU2();
        // attributes_count.
        int attributeCount = reader.readU2();
        for (int i = 0; i < attributeCount; ++i) {
            readAttributeInfo(reader, -1);
        }
    }

    private static boolean readMethodInfo(ByteReader reader, int codeIndex) {
        boolean isImplemented = false;
        // access_flags.
        reader.readU2();
        // name_index.
        reader.readU2();
        // descriptor_index.
        reader.readU2();
        // attributes_count.
        int attributeCount = reader.readU2();
        for (int i = 0; i < attributeCount; ++i) {
            MethodCode code = readAttributeInfo(reader, codeIndex);
            if ((null != code) && (0 != code.codeLength)) {
                RuntimeAssertionError.assertTrue(!isImplemented);
                addCount(LIMIT_codeSize, codeSize, code.codeLength);
                addCount(LIMIT_exceptionCount, exceptionCount, code.exceptionTableSize);
                isImplemented = true;
            }
        }
        return isImplemented;
    }

    private static MethodCode readAttributeInfo(ByteReader reader, int codeAttributeNameIndex) {
        // return null or MethodCode if this is a code attribute.
        MethodCode code = null;
        // attribute_name_index.
        int attributeNameIndex = reader.readU2();
        // attribute_length.
        int attributeLength = reader.readU4();
        int start = reader.position();
        if (codeAttributeNameIndex == attributeNameIndex) {
            // Section 4.7.3 "Code_attribute".
            int max_stack = reader.readU2();
            int max_locals = reader.readU2();
            int code_length = reader.readU4();
            //u1 code[code_length];
            reader.readNBytes(code_length);
            int exception_table_length = reader.readU2();
            for (int i = 0; i < exception_table_length; ++i) {
                //start_pc
                reader.readU2();
                //end_pc
                reader.readU2();
                //handler_pc
                reader.readU2();
                //catch_type
                reader.readU2();
            }
            // attributes_count.
            int attributeCount = reader.readU2();
            for (int i = 0; i < attributeCount; ++i) {
                readAttributeInfo(reader, -1);
            }
            code = new MethodCode(max_stack, max_locals, code_length, exception_table_length);
            /*
            Code_attribute {
                u2 attribute_name_index;
                u4 attribute_length;
                u2 max_stack;
                u2 max_locals;
                u4 code_length;
                u1 code[code_length];
                u2 exception_table_length;
                {   u2 start_pc;
                    u2 end_pc;
                    u2 handler_pc;
                    u2 catch_type;
                } exception_table[exception_table_length];
                u2 attributes_count;
                attribute_info attributes[attributes_count];
            }
            */
        } else {
            // Skip the info.
            reader.readNBytes(attributeLength);
        }
        int end = reader.position();
        RuntimeAssertionError.assertTrue((end - start) == attributeLength);
        return code;
    }

    public static void addCount(int[] legend, int[] counters, int count) {
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

    static class ClassConstantSizeInfo {
        int bytecodeLength;
        Map<String, Integer> constantTypeCount;
        int totalUtf8Length;
        int totalConstantPoolSize;

        ClassConstantSizeInfo(int bytecodeLength, Map<String, Integer> constantTypeCount, int totalUtf8Length, int totalConstantPoolSize) {
            this.bytecodeLength = bytecodeLength;
            this.constantTypeCount = constantTypeCount;
            this.totalUtf8Length = totalUtf8Length;
            this.totalConstantPoolSize = totalConstantPoolSize;
        }
    }

    static class MethodCode {
        public final int maxStack;
        public final int maxLocals;
        public final int codeLength;
        public final int exceptionTableSize;
        
        public MethodCode(int maxStack, int maxLocals, int codeLength, int exceptionTableSize) {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
            this.codeLength = codeLength;
            this.exceptionTableSize = exceptionTableSize;
        }
    }
}
