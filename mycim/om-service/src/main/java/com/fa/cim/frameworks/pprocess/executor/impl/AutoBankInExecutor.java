package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.service.bank.IBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * perform auto bank in for lot
 *
 * @author Yuri
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class AutoBankInExecutor implements PostProcessExecutor {

    private final IBankService bankService;

    @Autowired
    public AutoBankInExecutor(IBankService bankService) {
        this.bankService = bankService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (param.getEntityType() != EntityType.Lot) {
            if (log.isDebugEnabled()) {
                log.debug("Type is not Lot, do not perform this action <AutoBankIn>");
            }
            return PostProcessTask.success();
        }

        //-----------------------------------------------------------------
        // Call txBankInByPostProcReq
        //-----------------------------------------------------------------
        bankService.sxBankInByPostProcessReq(param.getObjCommon(), param.getEntityID(), "");

        return PostProcessTask.success();
    }

}
