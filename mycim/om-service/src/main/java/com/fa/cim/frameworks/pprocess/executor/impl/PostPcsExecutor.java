package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PostProcessTaskHandler(available = AvailablePhase.CHAINED_OR_JOINED)
public class PostPcsExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlScriptService processControlScriptService;

    public PostPcsExecutor(ILotMethod lotMethod, IProcessControlScriptService processControlScriptService) {
        this.lotMethod = lotMethod;
        this.processControlScriptService = processControlScriptService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        ObjectIdentifier lotID = param.getEntityID();
        Infos.ObjCommon objCommon = param.getObjCommon();
        String lotState = lotMethod.lotStateGet(objCommon, lotID);

        if (!CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.error(this);
        } else {
            log.debug("lot_state == Active. - N/A -");

            Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
            scriptParams.setEquipmentId(param.getEquipmentID());
            scriptParams.setLotId(lotID);
            scriptParams.setPhase(BizConstant.SP_BRSCRIPT_POST);
            scriptParams.setUser(objCommon.getUser());
            log.debug(">>>>>> Execute Post Script");
            processControlScriptService.sxProcessControlScriptRunReq(objCommon, scriptParams);
            return PostProcessTask.success();
        }
    }
}
