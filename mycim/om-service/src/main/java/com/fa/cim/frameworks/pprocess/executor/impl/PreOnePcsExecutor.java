package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class PreOnePcsExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlScriptService processControlScriptService;
    private final GenericCoreFactory coreFactory;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public PreOnePcsExecutor(ILotMethod lotMethod, IProcessControlScriptService processControlScriptService,
                             GenericCoreFactory coreFactory, RetCodeConfig retCodeConfig) {
        this.lotMethod = lotMethod;
        this.processControlScriptService = processControlScriptService;
        this.coreFactory = coreFactory;
        this.retCodeConfig = retCodeConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        ObjectIdentifier lotID = param.getEntityID();
        Infos.ObjCommon objCommon = param.getObjCommon();
        String lotState = lotMethod.lotStateGet(objCommon, lotID);

        if (!CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) {
                log.debug("lot_state != Active. - N/A -");
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("lot_state == Active. - N/A -");
            }

            Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
            scriptParams.setEquipmentId(param.getEquipmentID());
            scriptParams.setLotId(lotID);
            scriptParams.setPhase(BizConstant.SP_BRSCRIPT_PRE1);
            scriptParams.setUser(objCommon.getUser());
            if (log.isDebugEnabled()) {
                log.debug(">>>>>> Execute Pre - 1 Script");
            }
            try {
                CimLot cimLot = coreFactory.getBO(CimLot.class, lotID);
                int maxRunScriptCount = StandardProperties.OM_PP_MAX_OPE_COUNT.getIntValue();
                for (int i = 0; i < maxRunScriptCount; i++) {
                    String preOpeNo = cimLot.getProcessOperation().getOperationNumber();
                    processControlScriptService.sxProcessControlScriptRunReq(objCommon, scriptParams);
                    String postOpeNo = cimLot.getProcessOperation().getOperationNumber();
                    if (preOpeNo.equals(postOpeNo)) {
                        break;
                    }
                }

            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundScript(), e.getCode())) {
                    // do nothing.
                    if (log.isDebugEnabled()) {
                        log.debug(">>> Not Found Script in PostProcess. Do nothing...");
                    }
                } else {
                    throw e;
                }
            }
            return PostProcessTask.success();
        }
    }
}
