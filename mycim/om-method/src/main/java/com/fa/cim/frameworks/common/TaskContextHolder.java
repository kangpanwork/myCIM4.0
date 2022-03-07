package com.fa.cim.frameworks.common;

import com.fa.cim.dto.Infos;

public class TaskContextHolder {

    private final static ThreadLocal<Infos.ObjCommon> LOCAL_OBJ_COMMON = new ThreadLocal<>();
    private final static ThreadLocal<String> LOCAL_TASK_ID = new ThreadLocal<>();

    public static Infos.ObjCommon getObjCommon() {
        return LOCAL_OBJ_COMMON.get();
    }

    public static String getTaskId() {
        return LOCAL_TASK_ID.get();
    }

    public static void init(Infos.ObjCommon objCommon, String taskId) {
        LOCAL_OBJ_COMMON.set(objCommon);
        LOCAL_TASK_ID.set(taskId);
    }

    public static void setObjCommon(Infos.ObjCommon objCommon) {
        LOCAL_OBJ_COMMON.set(objCommon);
    }

    public static void setTaskId(String taskId) {
        LOCAL_TASK_ID.set(taskId);
    }

    public static void clear() {
        LOCAL_TASK_ID.remove();
        LOCAL_OBJ_COMMON.remove();
    }

}
