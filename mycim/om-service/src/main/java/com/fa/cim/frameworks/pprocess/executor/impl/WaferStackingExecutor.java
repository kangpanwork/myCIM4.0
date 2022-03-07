package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IBondingGroupMethod;
import com.fa.cim.service.bond.IBondService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <p>WaferStackingExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 13:54    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 13:54
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class WaferStackingExecutor implements PostProcessExecutor {

    private final IBondingGroupMethod bondingGroupMethod;
    private final IBondService bondService;

    @Autowired
    public WaferStackingExecutor(IBondingGroupMethod bondingGroupMethod, IBondService bondService) {
        this.bondingGroupMethod = bondingGroupMethod;
        this.bondService = bondService;
    }

    /**
     * Wafer stacking executor
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier equipmentID = param.getEquipmentID();
        final ObjectIdentifier controlJobID = param.getControlJobID();

        //--------------------------------------------------------
        //   call bondingGroupInfoByEqpGetDR
        //--------------------------------------------------------
        if (log.isDebugEnabled()) log.debug("call bondingGroupInfoByEqpGetDR()...");
        Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut =
                bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID, controlJobID, false);
        List<Infos.BondingGroupInfo> bondingGroupInfoList = objBondingGroupInfoByEqpGetDROut.getBondingGroupInfoList();

        //--------------------------------------------------------
        //   call bondingGroupInfoByEqpGetDR for each Bonding Group
        //--------------------------------------------------------
        Optional.ofNullable(bondingGroupInfoList).ifPresent(list -> list.forEach(bondingGroupInfo -> {
            if (log.isDebugEnabled()) log.debug("call sxWaferStackingReq()...");
            Params.WaferStackingReqInParams waferStackingReqInParams = new Params.WaferStackingReqInParams();
            waferStackingReqInParams.setBondingGroupID(bondingGroupInfo.getBondingGroupID());
            bondService.sxWaferStackingReq(objCommon, waferStackingReqInParams);
        }));

        return PostProcessTask.success();
    }
}
