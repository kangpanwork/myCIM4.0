package com.fa.cim.frameworks.dto.pp;

import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * current context of the post process execution
 *
 * @author Yuri
 */
@Getter
public class PostProcessContext {

    private final String taskId;
    private final Infos.ObjCommon objCommon;
    private final boolean lockHoldEnabled = StandardProperties.OM_PP_LOCK_HOLD_FLAG.isTrue();
    private final AtomicInteger operationChangedCount = new AtomicInteger(0);
    private final List<PostProcessTrace> trace = new ArrayList<>();
    private final Map<String, Infos.LotHoldReq> lotHoldRecords = new HashMap<>();

    @Setter
    private boolean successFlag;

    @Setter
    private Object mainGrgument;

    @Setter
    private Object mainResult;


    @Setter
    private PostProcessResult.Register register;

    public PostProcessContext(Infos.ObjCommon objCommon, String taskId) {
        this.taskId = taskId;
        this.objCommon = objCommon;
        this.register = null;
    }
}
