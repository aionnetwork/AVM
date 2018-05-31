package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayWrappingBytecodeFactory {

    static private HashMap<java.lang.String, java.lang.String> arrayWrapperMap = new HashMap<>();

    static{
        arrayWrapperMap.put("[I", "Lorg/aion/avm/arraywrapper/IntArray");
        arrayWrapperMap.put("[B", "Lorg/aion/avm/arraywrapper/ByteArray");
        arrayWrapperMap.put("[Z", "Lorg/aion/avm/arraywrapper/ByteArray");
        arrayWrapperMap.put("[C", "Lorg/aion/avm/arraywrapper/CharArray");
        arrayWrapperMap.put("[F", "Lorg/aion/avm/arraywrapper/FloatArray");
        arrayWrapperMap.put("[S", "Lorg/aion/avm/arraywrapper/ShortArray");
        arrayWrapperMap.put("[J", "Lorg/aion/avm/arraywrapper/LongArray");
        arrayWrapperMap.put("[D", "Lorg/aion/avm/arraywrapper/DoubleArray");
        arrayWrapperMap.put("[Ljava/lang/Object", "Lorg/aion/avm/arraywrapper/ObjectArray");
    }

    static java.lang.String updateMethodDesc(java.lang.String desc) {
        //\[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
        StringBuilder sb = new StringBuilder();
        java.lang.String wrappedDesc;

        int beginIndex = desc.indexOf('(');
        int endIndex = desc.lastIndexOf(')');

        // Method descriptor has to contain () pair
        if(beginIndex == -1 || endIndex == -1) {
            System.err.println(beginIndex);
            System.err.println(endIndex);
            throw new RuntimeException();
        }
        sb.append(desc, 0, beginIndex);
        sb.append('(');

        // Parse param
        java.lang.String para = desc.substring(beginIndex + 1, endIndex);
        Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
        Matcher paraMatcher = pattern.matcher(para);

        while(paraMatcher.find())
        {
            wrappedDesc = getWrapperName(paraMatcher.group());
            if (wrappedDesc.startsWith("L")){
                wrappedDesc = wrappedDesc + ";";
            }
            sb.append(wrappedDesc);
        }
        sb.append(')');

        // Parse return type if there is any
        if (endIndex < (desc.length() - 1)){
            java.lang.String ret = desc.substring(endIndex + 1);
            if (ret.equals("V")){
                sb.append(ret);
            }
            Matcher retMatcher = pattern.matcher(ret);
            if (retMatcher.find()){
                wrappedDesc = getWrapperName(retMatcher.group());
                if (wrappedDesc.startsWith("L")){
                    wrappedDesc = wrappedDesc + ";";
                }
                sb.append(wrappedDesc);
            }
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    // Return the wrapper descriptor of an array
    static java.lang.String getWrapperName(java.lang.String desc){
        if (desc.endsWith(";")){
            desc = desc.substring(0, desc.length() - 1);
        }

        java.lang.String ret;
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (arrayWrapperMap.containsKey(desc)){
            ret = arrayWrapperMap.get(desc);
        }else{
            arrayWrapperMap.put(desc, newWrapperName(desc));
            ret = arrayWrapperMap.get(desc);
        }

        //System.out.println("Wrapper name : " + ret);
        return ret;
    }

    //TODO:: is this enough?
    private static java.lang.String newWrapperName(java.lang.String desc){
        //System.out.println(desc);
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
    private static java.lang.String getRefWrapperName(java.lang.String desc){
        return desc.replace('[', '$');
    }


    // Return the element descriptor of an array
    public static java.lang.String getElementDesc(java.lang.String desc){
        return desc.substring(1);
    }

    //TODO: Ugly
    // Return the element descriptor of an array
    // 1D Primitive array will not be called with this method since there will be no aaload
    static java.lang.String getElementType(java.lang.String desc){
        return desc.substring(2, desc.length() - 1);
    }
}
