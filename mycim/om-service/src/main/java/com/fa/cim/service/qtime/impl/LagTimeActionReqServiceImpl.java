package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.qtime.ILagTimeActionReqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/7        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/11/7 16:07
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class LagTimeActionReqServiceImpl implements ILagTimeActionReqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;


    @Autowired
    private ILotService lotService;

    /**     
     * description:
     *     Update lot's process lag time information.
     *     (Case-1) action : SP_ProcessLagTime_Action_Set
     *        This action is specified to set lot's processLagTime information.
     *        When OpeComp is claimed for trigger operation of processLagTime,
     *        this tx is called with SP_ProcessLagTime_Action_Set. in this case,
     *        txHoldLotReq() is also called with "PLTH" (ProcessLagTimeHold).
     *
     *     (Case-2) action : SP_ProcessLagTime_Action_Clear
     *        This action is specified to clear lot's processLagTime information.
     *        When process lag time is expired, this tx is called by Watchdog with
     *        SP_ProcessLagTime_Action_Clear. In this case, txHoldLotReleaseReq()
     *        is also called with "PLTR" (ProcLagTimeHoldRelease).
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/8 11:07
     * @param objCommon - 
     * @param params -  
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    @Override
    public void sxProcessLagTimeUpdate(Infos.ObjCommon objCommon, Params.LagTimeActionReqParams params) {
        ObjectIdentifier lotID = params.getLotID();
        //【step1】check lot interFab transfer state
        log.debug("【step1】check lot interFab transfer state");
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), lotID.getValue(), interFabXferState));

        //【step2】action:SP_ProcessLagTime_Action_Set
        log.debug("【step2】action:SP_ProcessLagTime_Action_Set");
        if (CimStringUtils.equals(BizConstant.SP_PROCESSLAGTIME_ACTION_SET, params.getAction())) {
            log.debug("【in-param】action = SP_PROCESSLAGTIME_ACTION_SET");
            /**********************************************************************************************************/
            /*【step2-1】set process lag time info                                                                    */
            /*   - get process lag time info                                                                          */
            /*   - set process lag time info                                                                          */
            /**********************************************************************************************************/
            log.debug("【step2-1】set process lag time info");
            //【step2-1-1】get process lag time info
            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = processMethod.processProcessLagTimeGet(objCommon, lotID);

            //【step2-2】set process lag time information to lot
            log.debug("【step2-2】set process lag time information to lot");
            lotMethod.lotProcessLagTimeSet(objCommon, lotID, lagTimeGetOutRetCode.getProcessLagTimeStamp().toString());

            Double expriedTimeDuration = lagTimeGetOutRetCode.getExpriedTimeDuration();
            if (null != expriedTimeDuration && expriedTimeDuration > 0D) {
                log.debug("expired time duration > 0...");
                //prepare for txHoldLotReq's Input paramter.
                User user = new User();
                ObjectIdentifier holdUserID = new ObjectIdentifier(BizConstant.SP_PPTSVCMGR_PERSON);
                user.setUserID(holdUserID);
                Infos.ObjCommon tmpObjCommonIn = new Infos.ObjCommon();
                tmpObjCommonIn.setUser(user);
                tmpObjCommonIn.setTransactionID(objCommon.getTransactionID());
                tmpObjCommonIn.setTimeStamp(objCommon.getTimeStamp());

                ObjectIdentifier holdReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLD);
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                ObjectIdentifier dummyOI = ObjectIdentifier.emptyIdentifier();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                lotHoldReq.setHoldReasonCodeID(holdReasonCodeID);
                lotHoldReq.setHoldUserID(holdUserID);
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(dummyOI);
                lotHoldReq.setOperationNumber("");
                lotHoldReq.setRelatedLotID(dummyOI);
                lotHoldReq.setClaimMemo(params.getClaimMemo());
                holdReqList.add(lotHoldReq);
                //【step2-2-1】call sxHoldLotReq
                try {
                    lotService.sxHoldLotReq(tmpObjCommonIn, lotID, holdReqList);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getExistSameHold(), e.getCode())){
                        throw e;
                    }
                }
            }
        } else {
            //【step3】action:SP_ProcessLagTime_Action_Clear
            log.debug("【step3】action:SP_ProcessLagTime_Action_Clear");
            log.debug("【in-param】action = SP_PROCESSLAGTIME_ACTION_CLEAR");
            //【step3-1】clear lot's process lag time information
            log.debug("【step3-1】clear lot's process lag time information");
            lotMethod.lotProcessLagTimeSet(objCommon, lotID, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
            //-----------------------------------------------------------
            //   Prepare for txHoldLotReleaseReq's Input Parameter
            //-----------------------------------------------------------
            ObjectIdentifier holdReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLD);
            ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLDRELEASE);
            ObjectIdentifier holdUserID = new ObjectIdentifier(BizConstant.SP_PPTSVCMGR_PERSON);
            List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            ObjectIdentifier dummyOI = ObjectIdentifier.emptyIdentifier();
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldReasonCodeID(holdReasonCodeID);
            lotHoldReq.setHoldUserID(holdUserID);
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(dummyOI);
            lotHoldReq.setOperationNumber("");
            lotHoldReq.setRelatedLotID(dummyOI);
            holdReqList.add(lotHoldReq);
            //【step3-2】release "PLTH" hold record by txHoldLotReleaseReq
            log.debug("【step3-2】release \"PLTH\" hold record by txHoldLotReleaseReq");
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setReleaseReasonCodeID(releaseReasonCodeID);
            holdLotReleaseReqParams.setLotID(lotID);
            holdLotReleaseReqParams.setHoldReqList(holdReqList);
            try{
                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
            }catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getLotNotHeld(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getNotExistHold(), e.getCode()) ) {
                    throw e;
                }
            }
        }
    }
}