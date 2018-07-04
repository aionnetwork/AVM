package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.rejection.RejectedClassException;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * The user classes can come in associated with essentially any package name but we want to be able to identity them, in later pipeline stages,
 * and ensure that they aren't trying to invade one of our spaces, so we will re-map them all into PackageConstants.kUserDotPrefix, here.
 *
 * The following mechanical transformation has been applied:
 * 1) User-defined code are moved to `org.aion.avm.user`;
 * 2) All method declarations and references has been prepended with `avm_`;
 * 3) All fields declarations and references has been prepended with `avm_`;
 * 4) TODO: `java.base` types has been replaced with shadow `java.base`, i.e. `org.aion.avm.shadow.**`.
 *
 * NOTE: String & class constant wrapping is in separate class visitor
 */
public class UserClassMappingVisitor extends ClassToolchain.ToolChainClassVisitor {

    private static final String FIELD_PREFIX = "avm_";
    private static final String METHOD_PREFIX = "avm_";

    private static final String JAVA_LANG = "java/lang/";
    private static final String JAVA_MATH = "java/math/";
    private static final String JAVA_UTIL_FUNCTION = "java/util/function";
    private static final String ORG_AION_AVM_API = "org/aion/avm/api/";

    private final Set<String> userDefinedClassSlashNames;

    private final String shadowPackageSlash;

    public UserClassMappingVisitor(Set<String> userDefinedClassDotNames, String shadowPackageSlash) {
        super(Opcodes.ASM6);
        
        // Note that the input is given in .-style names, so convert these for our uses.
        this.userDefinedClassSlashNames = userDefinedClassDotNames.stream().map((dotStyle) -> Helpers.fulllyQualifiedNameToInternalName(dotStyle)).collect(Collectors.toSet());
        this.shadowPackageSlash = shadowPackageSlash;
    }

    public UserClassMappingVisitor(Set<String> userDefinedClassDotNames) {
        this(userDefinedClassDotNames, PackageConstants.kShadowSlashPrefix);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // We may need to re-map the name, superName, and interfaces.
        String newName = mapType(name);
        String newSuperName = mapType(superName);
        String[] newInterfaces = mapTypeArray(interfaces);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        super.visit(version, access, newName, null, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String newName = mapMethodName(name);
        String newDescriptor = mapDescriptor(descriptor);
        String[] newExceptions = mapTypeArray(exceptions);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, newDescriptor, null, newExceptions);
        
        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                String newOwner = mapType(owner);
                String newName = mapMethodName(name);
                String newDescriptor = mapDescriptor(descriptor);
                super.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            }
            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, mapType(type));
            }
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = mapType(owner);
                String newName = mapFieldName(name);
                String newDescriptor = mapDescriptor(descriptor);
                super.visitFieldInsn(opcode, newOwner, newName, newDescriptor);
            }
            @Override
            public void visitInvokeDynamicInsn(String methodName, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                String newName = mapMethodName(methodName);
                String newDescriptor = mapDescriptor(descriptor);

                // NOTE: method descriptor can't be replaced, based on Rom's comments
                Handle newBootstrapMethodHandle = mapHandle(bootstrapMethodHandle, false);
                
                Object newArgs[] = new Object[bootstrapMethodArguments.length];
                for (int i = 0; i < bootstrapMethodArguments.length; ++i) {
                    Object arg = bootstrapMethodArguments[i];
                    Object newArg = null;
                    if (arg instanceof Type) {
                        newArg = mapMethodType((Type) arg);
                    } else if (arg instanceof Handle) {
                        newArg = mapHandle((Handle) arg, true);
                    } else {
                        newArg = arg;
                    }
                    newArgs[i] = newArg;
                }
                
                super.visitInvokeDynamicInsn(newName, newDescriptor, newBootstrapMethodHandle, newArgs);
            }
            @Override
            public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                String newDescriptor = mapDescriptor(descriptor);
                super.visitMultiANewArrayInsn(newDescriptor, numDimensions);
            }
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                String newType = (null != type)
                        ? mapType(type)
                        : null;
                super.visitTryCatchBlock(start, end, handler, newType);
            }
            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                // We might need to adjust types in locals and stack slots.
                Object[] newLocals = new Object[local.length];
                for (int i = 0; i < local.length; ++i) {
                    if (local[i] instanceof String) {
                        newLocals[i] = mapType((String)local[i]);
                    } else {
                        newLocals[i] = local[i];
                    }
                }
                Object[] newStack = new Object[stack.length];
                for (int i = 0; i < stack.length; ++i) {
                    if (stack[i] instanceof String) {
                        newStack[i] = mapType((String)stack[i]);
                    } else {
                        newStack[i] = stack[i];
                    }
                }
                super.visitFrame(type, nLocal, newLocals, nStack, newStack);
            }

            @Override
            public void visitLdcInsn(final Object value) {
                Object valueToWrite = value;
                if (value instanceof Type) {
                    if(((Type) value).getSort() == Type.OBJECT){
                        valueToWrite = Type.getType(mapDescriptor(((Type) value).getDescriptor()));
                    }else if (((Type) value).getSort() == Type.ARRAY){
                        valueToWrite = Type.getType("L" + ArrayWrappingClassGenerator.getClassWrapper(mapDescriptor((((Type) value).getDescriptor()))) + ";");
                    }
                }
                super.visitLdcInsn(valueToWrite);
            }
        };
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String newName = mapFieldName(name);
        String newDescriptor = mapDescriptor(descriptor);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, newName, newDescriptor, null, value);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        String newOwner = mapType(owner);
        String newName = mapMethodName(name);
        String newDescriptor = descriptor == null ? null: mapDescriptor(descriptor);
        super.visitOuterClass(newOwner, newName, newDescriptor);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String newName = mapType(name);
        String newOuterName = (null != outerName)
                ? mapType(outerName)
                : null;
        String newInnerName = (null != innerName)
                ? innerName // mapType(innerName) TODO: disable because it's simple name, more investigation may be required
                : null;
        super.visitInnerClass(newName, newOuterName, newInnerName, access);
    }

    public static String mapFieldName(String name) {
        return FIELD_PREFIX  + name;
    }

    public static String mapMethodName(String name) {
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return name;
        }

        return METHOD_PREFIX + name;
    }

    private Type mapMethodType(Type type) {
        return Type.getMethodType(mapDescriptor(type.getDescriptor()));
    }

    private Handle mapHandle(Handle methodHandle, boolean mapMethodDescriptor) {
        String methodOwner = methodHandle.getOwner();
        String methodName = methodHandle.getName();
        String methodDescriptor = methodHandle.getDesc();

        String newMethodOwner = mapType(methodOwner);
        String newMethodName = mapMethodName(methodName);
        String newMethodDescriptor = mapMethodDescriptor ? mapDescriptor(methodDescriptor) : methodDescriptor;
        return new Handle(methodHandle.getTag(), newMethodOwner, newMethodName, newMethodDescriptor, methodHandle.isInterface());
    }

    /**
     * If the given descriptor contains class names is in the user-defined set, it is mapped to the new location.  Otherwise, it is returned, unchanged.
     * 
     * @param descriptor The incoming descriptor.
     * @return The possibly re-mapped descriptor.
     */
    private String mapDescriptor(String descriptor) {
        StringBuilder builder = DescriptorParser.parse(descriptor, new DescriptorParser.Callbacks<>() {
            @Override
            public StringBuilder readObject(int arrayDimensions, String type, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                String newType = mapType(type);
                userData.append(DescriptorParser.OBJECT_START);
                userData.append(newType);
                userData.append(DescriptorParser.OBJECT_END);
                return userData;
            }
            @Override
            public StringBuilder readBoolean(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.BOOLEAN);
                return userData;
            }
            @Override
            public StringBuilder readShort(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.SHORT);
                return userData;
            }
            @Override
            public StringBuilder readLong(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.LONG);
                return userData;
            }
            @Override
            public StringBuilder readInteger(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.INTEGER);
                return userData;
            }
            @Override
            public StringBuilder readFloat(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.FLOAT);
                return userData;
            }
            @Override
            public StringBuilder readDouble(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.DOUBLE);
                return userData;
            }
            @Override
            public StringBuilder readChar(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.CHAR);
                return userData;
            }
            @Override
            public StringBuilder readByte(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.BYTE);
                return userData;
            }
            @Override
            public StringBuilder argumentStart(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_START);
                return userData;
            }
            @Override
            public StringBuilder argumentEnd(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_END);
                return userData;
            }
            @Override
            public StringBuilder readVoid(StringBuilder userData) {
                userData.append(DescriptorParser.VOID);
                return userData;
            }
            private void writeArrayDimensions(StringBuilder builder, int dimensions) {
                for (int i = 0; i < dimensions; ++i) {
                    builder.append(DescriptorParser.ARRAY);
                }
            }
        }, new StringBuilder());
        
        return builder.toString();
    }

    private String[] mapTypeArray(String[] types) {
        String[] newNames = null;
        if (null != types) {
            newNames = new String[types.length];
            for (int i = 0; i < types.length; ++i) {
                newNames[i] = mapType(types[i]);
            }
        }
        return newNames;
    }

    /**
     * If the given type name is in the user-defined set, it is mapped to the new location.  Otherwise, it is returned, unchanged.
     * NOTE:  This method operates on /-style names.
     * 
     * @param type The incoming type name.
     * @return The possibly re-mapped type.
     */
    private String mapType(String type) {
        Assert.assertTrue(-1 == type.indexOf("."));
        
        String newType = null;

        if (type.startsWith("[")){
            newType = mapDescriptor(type);
        }else {
            if (this.userDefinedClassSlashNames.contains(type)) {
                return PackageConstants.kUserSlashPrefix + type;

            } else if (type.startsWith(JAVA_LANG) || type.startsWith(JAVA_UTIL_FUNCTION)) {
                return shadowPackageSlash + type;

            } else if (type.startsWith(ORG_AION_AVM_API) || type.startsWith("org/aion/avm/shadow/")) {
                return type;

            } else {
                RejectedClassException.nonWhiteListedClass(type);
            }
        }

        return newType;
    }

    // FOR TEST PURPOSE ONLY

    public String testMapDescriptor(String descriptor) {
        return mapDescriptor(descriptor);
    }

    public String testMapType(String type) {
        return mapType(type);
    }
}
