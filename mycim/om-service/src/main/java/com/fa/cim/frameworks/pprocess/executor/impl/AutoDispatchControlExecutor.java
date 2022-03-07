package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IAutoDispatchControlMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.service.dispatch.IDispatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>AutoDispatchControlExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 14:33    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 14:33
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class AutoDispatchControlExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final GenericCoreFactory coreFactory;
    private final IAutoDispatchControlMethod autoDispatchControlMethod;
    private final IDispatchService dispatchService;

    @Autowired
    public AutoDispatchControlExecutor(ILotMethod lotMethod,
                                       GenericCoreFactory coreFactory,
                                       IAutoDispatchControlMethod autoDispatchControlMethod,
                                       IDispatchService dispatchService) {
        this.lotMethod = lotMethod;
        this.coreFactory = coreFactory;
        this.autoDispatchControlMethod = autoDispatchControlMethod;
        this.dispatchService = dispatchService;
    }

    /**
     * Auto dispatch control
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            //---------------------------------------------
            // call lotPreviousOperationInfoGet
            //---------------------------------------------
            CimLot cimLot = coreFactory.getBO(CimLot.class, lotID);
            CimProcessOperation processOperation = cimLot.getResponsibleProcessOperation();
            CimProcessDefinition process = processOperation.getMainProcessDefinition();
            ObjectIdentifier routeID = ObjectIdentifier.build(process.getIdentifier(), process.getPrimaryKey());
            String operationNumber = processOperation.getOperationNumber();

            //--------------------------------------------
            //  Get Auto Dispatch Control Information
            //--------------------------------------------
            Inputs.ObjAutoDispatchControlInfoGetDRIn dispCtrlIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
            dispCtrlIn.setLotID(lotID);
            dispCtrlIn.setRouteID(routeID);
            dispCtrlIn.setOperationNumber(operationNumber);

            //---------------------------------------------
            // call autoDispatchControlInfoGetDR
            //---------------------------------------------
            List<Infos.LotAutoDispatchControlInfo> dispCtrlInfos = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon,
                    dispCtrlIn);

            if (CimArrayUtils.isNotEmpty(dispCtrlInfos)) {
                Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo = dispCtrlInfos.get(0);
                if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), dispCtrlIn.getRouteID())
                        && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), dispCtrlIn.getOperationNumber())
                        && lotAutoDispatchControlInfo.isSingleTriggerFlag()) {
                    Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams = new Params.AutoDispatchConfigModifyReqParams();
                    List<Infos.LotAutoDispatchControlUpdateInfo> lotAutoDispatchControlUpdateInfoList = new ArrayList<>();
                    autoDispatchConfigModifyReqParams.setLotAutoDispatchControlUpdateInfoList(lotAutoDispatchControlUpdateInfoList);
                    Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo = new Infos.LotAutoDispatchControlUpdateInfo();
                    lotAutoDispatchControlUpdateInfoList.add(lotAutoDispatchControlUpdateInfo);
                    lotAutoDispatchControlUpdateInfo.setLotID(lotID);
                    List<Infos.AutoDispatchControlUpdateInfo> dispCtrlList = new ArrayList<>();
                    lotAutoDispatchControlUpdateInfo.setAutoDispatchControlUpdateInfoList(dispCtrlList);
                    Infos.AutoDispatchControlUpdateInfo dispCtrlInfo = new Infos.AutoDispatchControlUpdateInfo();
                    dispCtrlList.add(dispCtrlInfo);
                    dispCtrlInfo.setUpdateMode(BizConstant.SP_AUTODISPATCHCONTROL_AUTODELETE);
                    dispCtrlInfo.setRouteID(lotAutoDispatchControlInfo.getRouteID());
                    dispCtrlInfo.setOperationNumber(lotAutoDispatchControlInfo.getOperationNumber());
                    dispCtrlInfo.setSingleTriggerFlag(lotAutoDispatchControlInfo.isSingleTriggerFlag());
                    dispCtrlInfo.setDescription(lotAutoDispatchControlInfo.getDescription());
                    autoDispatchConfigModifyReqParams.setClaimMemo("AutoDispatchControlExecutor");

                    //---------------------------------------------
                    // call sxAutoDispatchConfigModifyReq
                    //---------------------------------------------
                    if (log.isDebugEnabled()) log.debug("call sxAutoDispatchConfigModifyReq()...");
                    dispatchService.sxAutoDispatchConfigModifyReq(objCommon, autoDispatchConfigModifyReqParams);
                }
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
