package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.internal.PackageConstants;

import java.util.stream.Stream;

class Replacer {
    private static final String JAVA_LANG = "java/lang/";
    private static final String JAVA_UTIL = "java/util/";
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";
    private static final String AVM_INTERNAL_IOBJECT = PackageConstants.kInternalSlashPrefix + "IObject";

    private final String shadowPackage;
    private final ClassWhiteList whiteList;

    Replacer(String shadowPackage, ClassWhiteList whiteList) {
        this.shadowPackage = shadowPackage;
        this.whiteList = whiteList;
    }

    /**
     * Update the class reference if the type is a white-listed JDK class which starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @param allowInterfaceReplacement If true, we will use IObject instead of our shadow Object when replacing java/lang/Object
     * @return
     */
    protected String replaceType(String type, boolean allowInterfaceReplacement) {
        // Note that this assumes we have an agreement with the ClassWhiteList regarding what the JAVA_LANG prefix is
        // but this is unavoidable since it is a high-level interface and we are doing low-level string replacement.
        boolean shouldReplacePrefix = whiteList.isJdkClass(type);
        if (shouldReplacePrefix) {
            // This assertion verifies that these agree (in the future, we probably want to source them from the same place and avoid the direct string manipulation, here).
            // (technically, the white-list check is more restrictive than this since it can know about sub-packages while this doesn't).
            Assert.assertTrue(type.startsWith(JAVA_LANG) || type.startsWith(JAVA_UTIL));
        }

        // Handle the 3 relevant cases, independently.
        boolean isTypeJavaLangObject = JAVA_LANG_OBJECT.equals(type);
        if (allowInterfaceReplacement && isTypeJavaLangObject) {
            return AVM_INTERNAL_IOBJECT;
        } else if (isTypeJavaLangObject) {
            return PackageConstants.kShadowJavaLangSlashPrefix + type.substring(JAVA_LANG.length());
        } else if (shouldReplacePrefix) {
            return Stream.of(JAVA_LANG, JAVA_UTIL)
                    .filter(type::startsWith)
                    .findFirst()
                    .map(s -> shadowPackage + type.substring(s.length()))
                    .orElse(type);
        } else {
            return type;
        }
    }

    String replaceMethodDescriptor(String methodDescriptor) {
        StringBuilder sb = DescriptorParser.parse(methodDescriptor, new DescriptorParser.Callbacks<>() {
            @Override
            public StringBuilder readObject(int arrayDimensions, String type, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.OBJECT_START);
                userData.append(replaceType(type, true));
                userData.append(DescriptorParser.OBJECT_END);
                return userData;
            }

            @Override
            public StringBuilder readBoolean(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.BOOLEAN);
                return userData;
            }

            @Override
            public StringBuilder readShort(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.SHORT);
                return userData;
            }

            @Override
            public StringBuilder readLong(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.LONG);
                return userData;
            }

            @Override
            public StringBuilder readInteger(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.INTEGER);
                return userData;
            }

            @Override
            public StringBuilder readFloat(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.FLOAT);
                return userData;
            }

            @Override
            public StringBuilder readDouble(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.DOUBLE);
                return userData;
            }

            @Override
            public StringBuilder readChar(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.CHAR);
                return userData;
            }

            @Override
            public StringBuilder readByte(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
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

            private void populateArray(StringBuilder builder, int dimensions) {
                for (int i = 0; i < dimensions; ++i) {
                    builder.append(DescriptorParser.ARRAY);
                }
            }
        }, new StringBuilder());

        return sb.toString();
    }
}