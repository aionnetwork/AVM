package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.signature.*;
import org.objectweb.asm.util.*;
import java.util.*;
import java.util.regex.*;

public class ArrayWrappingBytecodeFactory {

    static private HashMap<String, String> awMap = new HashMap<String, String>();

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
            wrappedDesc = wrapTypeDesc(paraMatcher.group());
            sb.append(wrappedDesc);
        }
        sb.append(')');

        // Parse return type is there is any
        if (endIndex < (desc.length() - 1)){
            String ret = desc.substring(endIndex + 1);
            Matcher retMatcher = pattern.matcher(ret);
            if (retMatcher.find()){
                wrappedDesc = wrapTypeDesc(retMatcher.group());
                sb.append(wrappedDesc);
            }
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static String wrapTypeDesc(String desc){
        String ret;
        // We dont wrap non array in this pass
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (awMap.containsKey(desc)){
            ret = awMap.get(desc);
        }else{
            awMap.put(desc, genArrayName(desc));
            ret = awMap.get(desc);
        }
        return ret;
    }

    //TODO:: is this enough?
    public static String genArrayName(String desc){
        StringBuilder sb = new StringBuilder();
        sb.append("Lorg.aion.avm.arraywrapper.");
        sb.append(desc.replace('[', '$'));
        sb.append(";");
        return sb.toString();
    }
}
