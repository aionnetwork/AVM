package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.signature.*;
import org.objectweb.asm.util.*;
import java.util.*;
import java.util.regex.*;

public class ArrayWrappingBytecodeFactory {

    static private HashMap<String, String> arrayWrapperMap = new HashMap<String, String>();

    static{
        arrayWrapperMap.put("[I", "Lorg/aion/avm/arraywrapper/IntArray;");
        arrayWrapperMap.put("[B", "Lorg/aion/avm/arraywrapper/ByteArray;");
        arrayWrapperMap.put("[Z", "Lorg/aion/avm/arraywrapper/ByteArray;");
        arrayWrapperMap.put("[C", "Lorg/aion/avm/arraywrapper/CharArray;");
        arrayWrapperMap.put("[F", "Lorg/aion/avm/arraywrapper/FloatArray;");
        arrayWrapperMap.put("[S", "Lorg/aion/avm/arraywrapper/ShortArray;");
        arrayWrapperMap.put("[J", "Lorg/aion/avm/arraywrapper/LongArray;");
        arrayWrapperMap.put("[D", "Lorg/aion/avm/arraywrapper/DoubleArray;");
        arrayWrapperMap.put("[Ljava/lang/Object;", "Lorg/aion/avm/arraywrapper/ObjectArray;");
    }

    public static String updateMethodDesc(String desc) {
        //\[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
        StringBuilder sb = new StringBuilder();
        String wrappedDesc;

        int beginIndex = desc.indexOf('(');
        int endIndex = desc.lastIndexOf(')');

        // Method descriptor has to contain () pair
        if((beginIndex == -1 && endIndex != -1) || (beginIndex != -1 && endIndex == -1) || (beginIndex == -1 && endIndex == -1)) {
            System.err.println(beginIndex);
            System.err.println(endIndex);
            throw new RuntimeException();
        }
        sb.append(desc.substring(0, beginIndex));
        sb.append('(');

        // Parse param
        String para = desc.substring(beginIndex + 1, endIndex);
        Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
        Matcher paraMatcher = pattern.matcher(para);

        while(paraMatcher.find())
        {
            wrappedDesc = getWrapperDesc(paraMatcher.group());
            sb.append(wrappedDesc);
        }
        sb.append(')');

        // Parse return type is there is any
        if (endIndex < (desc.length() - 1)){
            String ret = desc.substring(endIndex + 1);
            if (ret.equals("V")){
                sb.append(ret);
            }
            Matcher retMatcher = pattern.matcher(ret);
            if (retMatcher.find()){
                wrappedDesc = getWrapperDesc(retMatcher.group());
                sb.append(wrappedDesc);
            }
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    // Return the wrapper descriptor of an array
    public static String getWrapperDesc(String desc){
        String ret;
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (arrayWrapperMap.containsKey(desc)){
            ret = arrayWrapperMap.get(desc);
        }else{
            arrayWrapperMap.put(desc, genWrapperName(desc));
            ret = arrayWrapperMap.get(desc);
        }
        return ret;
    }

    //TODO:: is this enough?
    public static String genWrapperName(String desc){
        System.out.println(desc);
        StringBuilder sb = new StringBuilder();
        sb.append("Lorg/aion/avm/arraywrapper/");

        //Check if the desc is a ref array
        if((desc.charAt(1) == 'L') || (desc.charAt(1) == '[')){
            sb.append(getRefWrapperName(desc));
        }else{
            Assert.unreachable("genWrapperName :" + desc);
        }

        return sb.toString();
    }

    // Return the element descriptor of an array
    public static String getRefWrapperName(String desc){
        String ret = desc.replace('[', '$');
        return ret;
    }

    // Return the element descriptor of an array
    public static String getElementDesc(String desc){
        String ret = desc.substring(1);
        return ret;
    }

    // Return the element descriptor of an array
    // 1D Primitive array will not be called with this method since there will be no aaload
    public static String getElementType(String desc){
        String ret = desc.substring(2, desc.length() - 1);
        return ret;
    }
}
