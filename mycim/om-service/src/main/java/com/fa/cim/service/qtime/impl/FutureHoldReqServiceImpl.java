package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.method.ICodeMethod;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.service.qtime.IFutureHoldReqService;
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
 * 2018/7/18         ********              Nyx             create file
 * 2019/9/30        ######              Neko                Refactoring
 *
 * @author Nyx
 * @since 2018/7/18 17:28
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
//@Transactional(rollbackFor = Exception.class)
@Slf4j
public class FutureHoldReqServiceImpl implements IFutureHoldReqService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICodeMethod codeComp;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Override
    public void sxFutureHoldReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params) {

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier codeDataID = params.getReasonCodeID();

        // [step1] - Lock objects to be updated [object_Lock]
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        // [step2] -Check lot interFabXferState [lot_interFabXferState_Get]
        log.info("[FutureHoldReq - step2] : Get and Check lotState (must not be Finished)");
        String interFabXferState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, interFabXferState), retCodeConfig.getInvalidLotStat());

        /*-----------------------------------------------------------*/
        /*  [step3] Check PosCode                                           */
        /*-----------------------------------------------------------*/
        log.info("[FutureHoldReq - step3] : Check PosCode");
        List<ObjectIdentifier> codeDataIDs = new ArrayList<>();
        codeDataIDs.add(codeDataID);
        try{
            codeComp.codeCheckExistanceDR(objCommon, CIMStateConst.CIM_LOT_HOLD_TYPE_FUTUREHOLD, codeDataIDs);
        }catch (ServiceException e) {
            String transactionID = objCommon.getTransactionID();
            if(transactionID.equals("OPRCW001") || transactionID.equals("TXPCC029")){
                throw e;
            }else {
                codeComp.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, codeDataIDs);
            }
        }
        // [step4] - TODO-Corecode： is not implemented completely yet: Check lot interFabXferState [lot_interFabXferState_Get]
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String strLotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        Validations.check(CimStringUtils.equals(strLotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq());

        // [step5] - Create a list into PosProcessOperation of a lot
        log.info("[FutureHoldReq - step5] : Create a list into PosProcessOperation of a lot");
        Infos.FutureHoldHistory entryOut = lotMethod.lotFutureHoldRequestsMakeEntry(objCommon, params);


        // [step6] Call lotFutureHoldEvent_Make [lotFutureHoldEvent_Make]
        log.info("Call lotFutureHoldEvent_Make");
        Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams = new Inputs.LotFutureHoldEventMakeParams();
        lotFutureHoldEventMakeParams.setTransactionID(TransactionIDEnum.ENHANCED_FUTURE_HOLD_REQ.getValue());
        lotFutureHoldEventMakeParams.setLotID(params.getLotID());
        lotFutureHoldEventMakeParams.setEntryType(BizConstant.SP_ENTRYTYPE_ENTRY);
        lotFutureHoldEventMakeParams.setFutureHoldHistory(entryOut);
        lotFutureHoldEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotFutureHoldEventMakeParams.setReleaseReasonCode(new ObjectIdentifier());
        eventMethod.lotFutureHoldEventMake(objCommon, lotFutureHoldEventMakeParams);
    }
}
