package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.service.durable.IDurableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>DurableLagTimeExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:39    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:39
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class DurableLagTimeExecutor implements PostProcessExecutor {

    private final IDurableMethod durableMethod;
    private final IDurableService durableService;

    @Autowired
    public DurableLagTimeExecutor(IDurableMethod durableMethod, IDurableService durableService) {
        this.durableMethod = durableMethod;
        this.durableService = durableService;
    }

    /**
     * Durable process lag time
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier durableID = param.getEntityID();

        String durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
        if (ObjectIdentifier.isNotEmpty(durableID)) {
            durableCategory = durableMethod.durableDurableCategoryGet(objCommon, durableID);
        }

        Infos.DurableOnRouteStateGetIn strDurableOnRouteStateGetin = new Infos.DurableOnRouteStateGetIn();
        strDurableOnRouteStateGetin.setDurableCategory(durableCategory);
        strDurableOnRouteStateGetin.setDurableID(durableID);
        String strDurableOnRouteStateGetout = durableMethod.durableOnRouteStateGet(objCommon, strDurableOnRouteStateGetin);
        if (CimStringUtils.equals(strDurableOnRouteStateGetout, BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("durable onRoute state = Active.");
            Params.DurableProcessLagTimeUpdateReqInParm strDurableProcessLagTimeUpdateReqInParm =
                    new Params.DurableProcessLagTimeUpdateReqInParm();
            strDurableProcessLagTimeUpdateReqInParm.setDurableCategory(durableCategory);
            strDurableProcessLagTimeUpdateReqInParm.setDurableID(durableID);
            strDurableProcessLagTimeUpdateReqInParm.setAction(BizConstant.SP_PROCESSLAGTIME_ACTION_SET);
            //-----------------------------------
            // Call sxDrbLagTimeActionReq
            //-----------------------------------
            if (log.isDebugEnabled()) log.debug("call sxDrbLagTimeActionReq()...");
            durableService.sxDrbLagTimeActionReq(objCommon, objCommon.getUser(), strDurableProcessLagTimeUpdateReqInParm);
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("durable onRoute state != Active.");
            return PostProcessTask.success();
        }
    }

}
