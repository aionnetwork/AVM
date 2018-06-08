package org.aion.avm.core.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Abi {

    /**
     * Indicates an abi exception.
     */
    public static class AbiException extends Exception {
        public AbiException(String message) {
            super(message);
        }
    }

    /**
     * Represents a method invocation.
     */
    public static class MethodInvoke {
        /**
         * Method name
         */
        String name;

        /**
         * Return types
         */
        Class<?> returnType;

        /**
         * Parameter types
         */
        List<Class<?>> parameterTypes;

        /**
         * Arguments
         */
        List<Object> arguments;


        public MethodInvoke(String name, Class<?> returnType, List<Class<?>> parameterTypes, List<Object> arguments) {
            this.name = name;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
            this.arguments = arguments;
        }
    }


    /**
     * Encodes a method invoke into byte array.
     *
     * @param name       the name of the method
     * @param returnType the return type of the method
     * @param arguments  the arguments
     * @return
     */
    public static byte[] encode(String name, Class<?> returnType, Object... arguments) throws AbiException {
        ByteBuffer args = ByteBuffer.allocate(1024); // TODO: size

        StringBuilder descriptor = new StringBuilder();
        descriptor.append(name);
        descriptor.append("(");
        for (Object arg : arguments) {
            if (arg == null) {
                throw new AbiException("Null argument is not allowed");
            }

            Class<?> argType = arg.getClass();
            if (argType == Boolean.class) {
                args.put(((Boolean) arg) == true ? (byte) 1 : (byte) 0);
                descriptor.append(DescriptorParser.BOOLEAN);

            } else if (argType == Byte.class) {
                args.put((Byte) arg);
                descriptor.append(DescriptorParser.BYTE);

            } else if (argType == Character.class) {
                args.putChar((Character) arg);
                descriptor.append(DescriptorParser.CHAR);

            } else if (argType == Short.class) {
                args.putShort((Short) arg);
                descriptor.append(DescriptorParser.SHORT);

            } else if (argType == Integer.class) {
                args.putInt((Integer) arg);
                descriptor.append(DescriptorParser.INTEGER);

            } else if (argType == Long.class) {
                args.putLong((Long) arg);
                descriptor.append(DescriptorParser.LONG);

            } else if (argType == Float.class) {
                args.putFloat((Float) arg);
                descriptor.append(DescriptorParser.FLOAT);

            } else if (argType == Double.class) {
                args.putDouble((Double) arg);
                descriptor.append(DescriptorParser.DOUBLE);

            } else if (argType.isArray()) {
                // TODO: array

            } else {
                throw new AbiException("Unsupported parameter type: " + argType);
            }
        }
        descriptor.append(")");
        if (returnType == Void.class) {
            descriptor.append(DescriptorParser.VOID);

        } else if (returnType == Boolean.class) {
            descriptor.append(DescriptorParser.BOOLEAN);

        } else if (returnType == Byte.class) {
            descriptor.append(DescriptorParser.BYTE);

        } else if (returnType == Character.class) {
            descriptor.append(DescriptorParser.CHAR);

        } else if (returnType == Short.class) {
            descriptor.append(DescriptorParser.SHORT);

        } else if (returnType == Integer.class) {
            descriptor.append(DescriptorParser.INTEGER);

        } else if (returnType == Long.class) {
            descriptor.append(DescriptorParser.LONG);

        } else if (returnType == Float.class) {
            descriptor.append(DescriptorParser.FLOAT);

        } else if (returnType == Double.class) {
            descriptor.append(DescriptorParser.DOUBLE);

        } else if (returnType.isArray()) {
            // TODO: array

        } else {
            throw new AbiException("Unsupported return type: " + returnType);
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        writeBytes(buffer, descriptor.toString().getBytes());
        writeBytes(buffer, args.flip().array());

        return buffer.flip().array();
    }

    /**
     * Decodes a method invoke from it's encoded format.
     *
     * @param bytes
     * @return
     * @throws AbiException
     */
    public static MethodInvoke decode(byte[] bytes) throws AbiException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] descriptorEncoded = readBytes(buffer);
        byte[] argsEncoded = readBytes(buffer);

        // validate method descriptor
        String descriptor = new String(descriptorEncoded); // TODO: invalid UTF-8 string?
        if (descriptor.matches("[_$a-zA-Z0-9]{1,255}\\((\\[*BCDFIJSZ)*\\)V|(\\[*BCDFIJSZ)")) {
            throw new AbiException("Invalid method descriptor: " + descriptor);
        }

        String name = descriptor.substring(descriptor.indexOf('('), descriptor.lastIndexOf(')'));
        Class<?> returnType = null;
        List<Class<?>> parameterTypes = new ArrayList<>();
        List<Object> arguments = new ArrayList<>();

        DescriptorParser.parse(descriptor, new DescriptorParser.Callbacks<Object>() {
            // TODO: parse data base on method descriptor

            @Override
            public Object argumentStart(Object userData) {
                return null;
            }

            @Override
            public Object argumentEnd(Object userData) {
                return null;
            }

            @Override
            public Object readObject(int arrayDimensions, String type, Object userData) {
                return null;
            }

            @Override
            public Object readVoid(Object userData) {
                return null;
            }

            @Override
            public Object readBoolean(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readShort(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readLong(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readInteger(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readFloat(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readDouble(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readChar(int arrayDimensions, Object userData) {
                return null;
            }

            @Override
            public Object readByte(int arrayDimensions, Object userData) {
                return null;
            }
        }, null);


        return new MethodInvoke(name, returnType, parameterTypes, arguments);
    }

    private static byte[] readBytes(ByteBuffer buffer) throws AbiException {
        short size = buffer.getShort();
        if (size < 0) {
            throw new AbiException("Invalid byte array size: " + size);
        }

        byte[] bytes = new byte[size]; // 32k
        buffer.get(bytes);

        return bytes;
    }

    private static void writeBytes(ByteBuffer buffer, byte[] bytes) throws AbiException {
        if (bytes.length > Short.MAX_VALUE) {
            throw new AbiException("Too big byte array: size = " + bytes.length);
        }

        buffer.putShort((short) bytes.length);
        buffer.put(bytes);

        byte[][] x  = new byte[1][];
    }
}