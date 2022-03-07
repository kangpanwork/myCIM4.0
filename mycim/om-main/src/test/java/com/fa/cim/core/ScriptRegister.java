package com.fa.cim.core;

import com.fa.cim.pcs.engine.ScriptEntityFactory;

import java.util.HashMap;
import java.util.Map;

public class ScriptRegister {

    private static Map<String,Class<?>> classMap=new HashMap<>();

    static {
        try {
            register("$Factory", ScriptEntityFactory.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(String name,Class<?> cls) throws Exception {
        if (classMap.get(name)!=null){
            throw new Exception(String.format("已经注册[%s=%s]", name,cls.getName()));
        }
        classMap.put(name,cls);
    }

    public static Class<?> fetch(String name){
        return classMap.get(name);
    }


}
