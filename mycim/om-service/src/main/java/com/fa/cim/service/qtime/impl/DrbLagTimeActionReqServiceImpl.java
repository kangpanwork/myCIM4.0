package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IProcessForDurableMethod;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.qtime.IDrbLagTimeActionReqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 *
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/28        ********             Miner               create file
 *
 * @author: Miner
 * @date: 2020/6/28 9:07
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.  
 */
@Slf4j
@OmService("drbLagTimeSen")
public class DrbLagTimeActionReqServiceImpl implements IDrbLagTimeActionReqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IDurableService durableService;


    @Autowired
    private IProcessForDurableMethod processMethod;

    @Override
    public Results.DurableProcessLagTimeUpdateReqResult sxDrbLagTimeActionReq(Infos.ObjCommon objCommonIn, User user, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {

        Results.DurableProcessLagTimeUpdateReqResult durableProcessLagTimeUpdateReqResult=new Results.DurableProcessLagTimeUpdateReqResult();
        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if(!CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)&&
                !CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)&&
                !CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {

            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }

        if(CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            //-----------------------------------------------------------
            // Check cassette interFabXferState
            //-----------------------------------------------------------
            String cassetteInterFabXferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommonIn, durableProcessLagTimeUpdateReqInParm.getDurableID());
            //-----------------------------------------------------------
            // "Transferring"
            //-----------------------------------------------------------

            Validations.check(CimStringUtils.equals(cassetteInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq());


        }
        //-----------------------------------------------------------
        //   Action : SP_ProcessLagTime_Action_Set
        //    1. Set processLagTime information to duralbe
        //    2. Make "DLTH" durable hold by txHoldDurableReq()
        //-----------------------------------------------------------

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getAction(), BizConstant.SP_PROCESSLAGTIME_ACTION_SET)) {
            log.info("", "in-parm's action = SP_PROCESSLAGTIME_ACTION_SET...");
            //-----------------------------------------------------------
            //   Get ProcessLagTime Information of previous operation
            //-----------------------------------------------------------
            log.info("", "Get ProcessLagTime Information of previous operation......");

            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = processMethod.processDurableProcessLagTimeGet(objCommonIn, durableProcessLagTimeUpdateReqInParm);
            //-----------------------------------------------------------
            //   Set ProcessLagTime Information to Durable
            //-----------------------------------------------------------
            processMethod.DurableProcessLagTimeSet(objCommonIn, durableProcessLagTimeUpdateReqInParm, lagTimeGetOutRetCode);

            if (lagTimeGetOutRetCode.getExpriedTimeDuration() > 0) {
                log.info("", "expiredTimeDuration > 0.........");
                //-----------------------------------------------------------
                //   Prepare for txHoldDurableReq's Input Parameter
                //-----------------------------------------------------------
                log.info("", "Prepare for txHoldDurableReq's Input Parameter...........");


                ObjectIdentifier holdReasonCodeID;
                ObjectIdentifier holdUserID;
                ObjectIdentifier dummyID = null;

                holdReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLD);
                holdUserID = ObjectIdentifier.buildWithValue(BizConstant.SP_PPTSVCMGR_PERSON);

                List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
                strDurableHoldList.add(new Infos.DurableHoldList());
                strDurableHoldList.get(0).setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
                strDurableHoldList.get(0).setHoldReasonCodeID(holdReasonCodeID);
                strDurableHoldList.get(0).setHoldUserID(holdUserID);
                strDurableHoldList.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                strDurableHoldList.get(0).setRouteID(dummyID);
                strDurableHoldList.get(0).setOperationNumber("");
                strDurableHoldList.get(0).setRelatedDurableID(dummyID);
                strDurableHoldList.get(0).setRelatedDurableCategory("");
                strDurableHoldList.get(0).setClaimMemo(durableProcessLagTimeUpdateReqInParm.getClaimMemo());

                Params.HoldDurableReqInParam strHoldDurableReqInParam = new Params.HoldDurableReqInParam();
                strHoldDurableReqInParam.setDurableCategory(durableProcessLagTimeUpdateReqInParm.getDurableCategory());
                strHoldDurableReqInParam.setDurableID(durableProcessLagTimeUpdateReqInParm.getDurableID());
                strHoldDurableReqInParam.setDurableHoldLists(strDurableHoldList);

                //-----------------------------------------------------------
                //   Call txHoldDurableReq()
                //-----------------------------------------------------------
                try {
                    durableService.sxHoldDrbReq(objCommonIn, strHoldDurableReqInParam, durableProcessLagTimeUpdateReqInParm.getClaimMemo());
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(ex.getCode(), retCodeConfig.getExistSameHold())) {
                        throw ex;
                    }
                }
            }
        }else {
            //-----------------------------------------------------------
            //   Clear ProcessLagTime Information of Durable
            //-----------------------------------------------------------
            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = new Outputs.ObjProcessLagTimeGetOut();
            lagTimeGetOutRetCode.setProcessLagTimeStamp(Timestamp.valueOf(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING));
            processMethod.DurableProcessLagTimeSet(objCommonIn,durableProcessLagTimeUpdateReqInParm,lagTimeGetOutRetCode);

            //-----------------------------------------------------------
            //   Prepare for txHoldDurableReleaseReq's Input Parameter
            //-----------------------------------------------------------
            log.info("{}", "Prepare for txHoldDurableReleaseReq's Input Parameter...");
            ObjectIdentifier holdReasonCodeID;
            ObjectIdentifier holdUserID;
            ObjectIdentifier releaseReasonCodeID;
            ObjectIdentifier dummyID=null;

            holdReasonCodeID    = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLD);
            holdUserID          = ObjectIdentifier.buildWithValue(BizConstant.SP_PPTSVCMGR_PERSON);
            releaseReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLDRELEASE);

            List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
            strDurableHoldList.add(new Infos.DurableHoldList());
            strDurableHoldList.get(0).setHoldType                      (BizConstant.SP_HOLDTYPE_DURABLEHOLD);
            strDurableHoldList.get(0).setHoldReasonCodeID              (holdReasonCodeID);
            strDurableHoldList.get(0).setHoldUserID                    (holdUserID);
            strDurableHoldList.get(0).setResponsibleOperationMark      (BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            strDurableHoldList.get(0).setRouteID                       (dummyID);
            strDurableHoldList.get(0).setOperationNumber               ("");
            strDurableHoldList.get(0).setRelatedDurableID              (dummyID);
            strDurableHoldList.get(0).setRelatedDurableCategory        ("");
            strDurableHoldList.get(0).setClaimMemo                     (durableProcessLagTimeUpdateReqInParm.getClaimMemo());

            Infos.HoldDurableReleaseReqInParam strHoldDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
            strHoldDurableReleaseReqInParam.setDurableCategory      (durableProcessLagTimeUpdateReqInParm.getDurableCategory());
            strHoldDurableReleaseReqInParam.setDurableID            (durableProcessLagTimeUpdateReqInParm.getDurableID());
            strHoldDurableReleaseReqInParam.setReleaseReasonCodeID  (releaseReasonCodeID);
            strHoldDurableReleaseReqInParam.setStrDurableHoldList   (strDurableHoldList);

            //-----------------------------------------------------------
            //   Call txHoldDurableReleaseReq()
            //-----------------------------------------------------------
            log.info("{}", "Call txHoldLotReleaseReq()...");
            try {
                durableService.sxHoldDrbReleaseReq(
                        objCommonIn,
                        strHoldDurableReleaseReqInParam,
                        durableProcessLagTimeUpdateReqInParm.getClaimMemo());
            } catch (ServiceException ex){
                if (!Validations.isEquals(ex.getCode(),retCodeConfigEx.getInvalidDurableStat())&&
                        !Validations.isEquals(ex.getCode(),retCodeConfig.getNotExistHold())) {
                    throw ex;
                }
            }

        }
        return durableProcessLagTimeUpdateReqResult;
    }
}