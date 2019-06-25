package org.aion.avm.embed.collection;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AionCollectionInterfaceContract {

    public static List<Integer> targetList;

    public static Set<Integer> targetSet;

    public static Map<Integer, Integer> targetMap;

    static{
        targetList = new AionList<>();
        targetSet = new AionSet<>();
        targetMap = new AionMap<>();
    }

    @Callable
    public static void testList(){
        targetList.add(Integer.valueOf(1));
        targetList.add(Integer.valueOf(1));
        targetList.add(Integer.valueOf(1));

        targetList.size();
        targetList.isEmpty();
        targetList.contains(Integer.valueOf(1));
        targetList.toArray();
        targetList.add(Integer.valueOf(1));
        targetList.remove(Integer.valueOf(1));
        targetList.get(0);
        targetList.set(0, Integer.valueOf(2));
        targetList.add(0, Integer.valueOf(3));
        targetList.remove(Integer.valueOf(1));
        targetList.indexOf(Integer.valueOf(1));
        targetList.lastIndexOf(Integer.valueOf(1));
    }

    @Callable
    public static void testSet(){
        targetSet.add(Integer.valueOf(1));
        targetSet.add(Integer.valueOf(1));
        targetSet.add(Integer.valueOf(1));

        targetSet.size();
        targetSet.isEmpty();
        targetSet.contains(Integer.valueOf(1));
        targetSet.add(Integer.valueOf(1));
        targetSet.remove(Integer.valueOf(1));
    }

    @Callable
    public static void testMap(){
        targetMap.put(Integer.valueOf(1), Integer.valueOf(1));
        targetMap.put(Integer.valueOf(2), Integer.valueOf(1));
        targetMap.put(Integer.valueOf(3), Integer.valueOf(1));

        targetMap.size();
        targetMap.isEmpty();
        targetMap.containsKey(Integer.valueOf(1));
        targetMap.containsValue(Integer.valueOf(1));

        targetMap.get(Integer.valueOf(1));
        targetMap.put(Integer.valueOf(1), Integer.valueOf(1));
        targetMap.remove(Integer.valueOf(1));
    }
}
