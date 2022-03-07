package com.fa.cim.pcs.engine;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;

public class ScriptThreadHolder {

    private static final ThreadLocal<String> PHASE = new ThreadLocal<>();
    private static final ThreadLocal<Infos.ObjCommon> OBJ_COMMON = new ThreadLocal<>();
    private static final ThreadLocal<ObjectIdentifier> USER_ID = new ThreadLocal<>();


    private ScriptThreadHolder() {
    }

    public static String getPhase () {
        return PHASE.get();
    }

    public static Infos.ObjCommon getObjCommon() {
        return OBJ_COMMON.get();
    }

    public static ObjectIdentifier getUserID () {
        return USER_ID.get();
    }


    public static void init(Infos.ObjCommon objCommon, String phase, String scriptName) {
        PHASE.set(phase);
        User user = objCommon.getUser();
        USER_ID.set(user.getUserID());
        Infos.ObjCommon duplicate = objCommon.duplicate();
        String tmpUserId = String.format("[PCS Action] %s.%s", phase, scriptName);
        duplicate.getUser().setUserID(ObjectIdentifier.build(tmpUserId, user.getUserID().getReferenceKey()));
        OBJ_COMMON.set(duplicate);
    }

    static void clear() {
        PHASE.remove();
        OBJ_COMMON.remove();
        USER_ID.remove();
    }
}
