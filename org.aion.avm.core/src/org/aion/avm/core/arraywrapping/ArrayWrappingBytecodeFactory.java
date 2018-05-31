package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayWrappingBytecodeFactory {

    static private HashMap<java.lang.String, java.lang.String> arrayWrapperMap = new HashMap<>();

    static{
        arrayWrapperMap.put("[I", "org/aion/avm/arraywrapper/IntArray");
        arrayWrapperMap.put("[B", "org/aion/avm/arraywrapper/ByteArray");
        arrayWrapperMap.put("[Z", "org/aion/avm/arraywrapper/ByteArray");
        arrayWrapperMap.put("[C", "org/aion/avm/arraywrapper/CharArray");
        arrayWrapperMap.put("[F", "org/aion/avm/arraywrapper/FloatArray");
        arrayWrapperMap.put("[S", "org/aion/avm/arraywrapper/ShortArray");
        arrayWrapperMap.put("[J", "org/aion/avm/arraywrapper/LongArray");
        arrayWrapperMap.put("[D", "org/aion/avm/arraywrapper/DoubleArray");
        arrayWrapperMap.put("[Ljava/lang/Object", "org/aion/avm/arraywrapper/ObjectArray");
    }

    static java.lang.String updateMethodDesc(java.lang.String desc) {
        //\[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
        StringBuilder sb = new StringBuilder();
        java.lang.String wrapperName;
        java.lang.String cur;

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
            cur = paraMatcher.group();
            if(cur.startsWith("[")) {
                cur = "L" + getWrapperName(cur) + ";";
            }
            sb.append(cur);
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
                cur = retMatcher.group();
                if(cur.startsWith("[")) {
                    cur = "L" + getWrapperName(cur) + ";";
                }
                sb.append(cur);
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

    // Return the wrapper descriptor of an array
    static java.lang.String getWrapperNameFromElement(java.lang.String desc){
        String wDesc = "[" + desc;
        //System.out.println("Wrapper name : " + ret);
        return getWrapperName(wDesc);
    }

    //TODO:: is this enough?
    private static java.lang.String newWrapperName(java.lang.String desc){
        //System.out.println(desc);
        StringBuilder sb = new StringBuilder();
        sb.append("org/aion/avm/arraywrapper/");

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
