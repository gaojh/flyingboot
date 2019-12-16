package com.github.gaojh.mvc.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author gaojianhua
 * @date 2019/12/11 4:35 下午
 */
public class SetupContext {

    private static TreeMap<Integer, SetupRunner> setupMap = new TreeMap<>();

    public static void addSetupRunner(Integer order, SetupRunner setupRunner) {
        setupMap.put(order, setupRunner);
    }

    public static List<SetupRunner> getSetupRunners() {
        Set<Integer> keys = setupMap.keySet();
        List<SetupRunner> list = new ArrayList<>();
        for (Integer idx : keys) {
            list.add(setupMap.get(idx));
        }
        return list;
    }
}
