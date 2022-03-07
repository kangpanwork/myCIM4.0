package com.fa.cim.controller.sort;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.sort.ISortController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.sort.ISortService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ISortController.class, confirmableKey = "SortConfirm", cancellableKey = "SortCancel")
@RequestMapping("/sort")
@Listenable
public class SortController implements ISortController {
    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postController;

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ISortService sortService;

    @ResponseBody
    @RequestMapping(value = "/wafer_slotmap_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SLOTMAP_CHANGE_REQ)
    public Response waferSlotmapChangeReq(@RequestBody Params.WaferSlotmapChangeReqParams params) {
        log.info(" WaferSlotmapChangeReqParams :{}", params);
        final String transactionID = TransactionIDEnum.WAFER_SLOTMAP_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        sortService.sxWaferSlotmapChangeReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_action_execute /req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ON_EQP_REQ)
    public Response onlineSorterActionExecuteReq(@RequestBody Params.OnlineSorterActionExecuteReqParams params) {
        log.info("PJStatusChangeRptInParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ON_EQP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        sortService.sxOnlineSorterActionExecuteReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT)
    public Response onlineSorterRpt(@RequestBody Params.OnlineSorterRptParams params) {
        log.info("OnlineSorterRptParams : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        try {
            sortService.sxOnlineSorterRpt(objCommon, params);
        } catch (ServiceException ex){
            if (Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidActionCode())||
                    Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidParameter())||
                    Validations.isEquals(ex.getCode(),retCodeConfigEx.getInvalidSorterstatus())||
                    Validations.isEquals(ex.getCode(),retCodeConfigEx.getWafersorterSlotmapCompareError())){
                log.debug("{} {} {}","PPTServiceManager_i:: txWaferSorterOnEqpRpt", "rc =",ex.getMessage());
                return Response.createSucc(transactionID, null);
            }
            log.debug("{} {}","PPTServiceManager_i:: txWaferSorterOnEqpRpt", "rc != RC_OK");
            throw ex;
        }

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_slotmap_compare/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_DATA_COMPARE_REQ)
    public Response onlineSorterSlotmapCompareReq(@RequestBody Params.OnlineSorterSlotmapCompareReqParams params) {
        log.info("OnlineSorterSlotmapCompareReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_DATA_COMPARE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 - txOnlineSorterSlotmapCompareReq
        List<Infos.WaferSorterCompareCassette> result = sortService.sxOnlineSorterSlotmapCompareReq(objCommon, params.getPortGroup(), params.getEquipmentID(),params.getActionCode());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_sorter_action_register/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CHANGE_WAFER_SORTER_ACTION_REQ)
    public Response waferSorterActionRegisterReq(@RequestBody Params.WaferSorterActionRegisterReqParams params) {
        log.info("WaferSorterActionRegisterReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.CHANGE_WAFER_SORTER_ACTION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txWaferSorterActionRegisterReq

        sortService.sxWaferSorterActionRegisterReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_slotmap_adjust/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ)
    public Response onlineSorterSlotmapAdjustReq(@RequestBody Params.OnlineSorterSlotmapAdjustReqParam params) {
        log.info("OnlineSorterSlotmapAdjustReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterSlotmapAdjustReq
        sortService.sxOnlineSorterSlotmapAdjustReq(objCommon, params);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/online_sorter_action_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ON_EQP_CANCEL_REQ)
    public Response onlineSorterActionCancelReq(@RequestBody Params.OnlineSorterActionCancelReqParm params) {
        log.info("OnlineSorterActionCancelReqParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ON_EQP_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txWaferSorterActionRegisterReq
        sortService.sxOnlineSorterActionCancelReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_exchange/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_EXCHANGE_REQ)
    public Response carrierExchangeReq(@RequestBody Params.CarrierExchangeReqParams params) {
        log.debug("carrierExchangeReq(): invoke carrierExchangeReq");
        String txId = TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //Step2 - cassette_lotIDList_GetDR, cassetteLotIDList will used in txAccessControlCheckInq
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        Infos.LotListInCassetteInfo lotIDs = null;
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            for (Infos.WaferTransfer waferTransfer : params.getWaferXferList()) {
                lotIDs = cassetteMethod.cassetteLotIDListGetDR(null, waferTransfer.getOriginalCassetteID());
            }
        }

        //Step3 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(CimObjectUtils.isEmpty(lotIDs) ? null : lotIDs.getLotIDList());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step4 - txCarrierExchangeReq
        sortService.sxCarrierExchangeReq(objCommon, params.getEquipmentID(), params.getWaferXferList(), params.getClaimMemo());
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/sj_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_CREATE_REQ)
    public Response sjCreateReqOld(@RequestBody Params.SJCreateReqParams params) {
        log.info("SJCreateReqParams : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  sxSJCreateReq
        ObjectIdentifier sjID = sortService.sxSJCreateReq(objCommon, params);

        List<ObjectIdentifier> tmpCassetteIDs = new ArrayList<>();
        for(int i = 0; i< CimArrayUtils.getSize(params.getStrSorterComponentJobListAttributesSequence()); i++) {
            tmpCassetteIDs.add(params.getStrSorterComponentJobListAttributesSequence().get(i).getOriginalCarrierID());
            tmpCassetteIDs.add(params.getStrSorterComponentJobListAttributesSequence().get(i).getDestinationCarrierID());
        }

        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for(int i = 0; i< CimArrayUtils.getSize(params.getStrSorterComponentJobListAttributesSequence()); i++) {
            Boolean findFlag = false;
            for(int j = i + 1; j < CimArrayUtils.getSize(cassetteIDs); j++) {
                if(ObjectIdentifier.equalsWithValue(tmpCassetteIDs.get(i), tmpCassetteIDs.get(j) ) ) {
                    findFlag = true;
                    break;
                }
            }
            if(CimBooleanUtils.isFalse(findFlag)) {
                cassetteIDs.add(tmpCassetteIDs.get(i));
            }
        }
        //-----------------------------------------------
        // Call txPostTaskRegisterReq__100
        //-----------------------------------------------
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParam.setCassetteIDs(cassetteIDs);
        postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
        postTaskRegisterReqParams.setUser(params.getUser());

        //step4 - txPostTaskRegisterReq__100
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

        log.info("txSJCreateReq and txPostTaskRegisterReq__100 : rc == RC_OK");
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        //--------------------------------------
        // Call PostTaskExecuteReq
        //--------------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);

        //step5 - TxPostTaskExecuteReq__100
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(transactionID, sjID);
    }

    @Override
    @ResponseBody
    @PostMapping(value = "/sj_create/req")
    @CimMapping(TransactionIDEnum.SORT_JOB_CREATE_REQ)
    @EnablePostProcess
    public Response sjCreateReq(@RequestBody Params.SJCreateReqParams params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  sxSJCreateReq
        ObjectIdentifier sjID = sortService.sxSJCreateReq(objCommon, params);

        return Response.createSucc(transactionID, sjID);
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_start/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_START_REQ)
    public Response sjStartReq(@RequestBody Params.SJStartReqParams params) {
        log.info("SJStartReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_START_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Infos.SJStartReqInParm sjStartReqInParm = params.getSjStartReqInParm();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(sjStartReqInParm.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  sxSJCreateReq
        sortService.sxSJStartReq(objCommon, sjStartReqInParm, params.getClaimMemo());

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_condition_check/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SJ_CONDITION_CHECK_REQ)
    public Response sjConditionCheckReq(@RequestBody Params.SortJobCheckConditionReqInParam params) {
        log.info("SJConditionCheckReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.SJ_CONDITION_CHECK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(params.getStrSorterComponentJobListAttributesSequence())) {
            for (Infos.SorterComponentJobListAttributes sorterComponentJobListAttributes : params.getStrSorterComponentJobListAttributesSequence()) {
                if (CimArrayUtils.isNotEmpty(sorterComponentJobListAttributes.getWaferSorterSlotMapList())) {
                    for (Infos.WaferSorterSlotMap waferSorterSlotMap : sorterComponentJobListAttributes.getWaferSorterSlotMapList()) {
                        lotIDLists.add(waferSorterSlotMap.getLotID());
                    }
                }
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  sxSJConditionCheckReq
        sortService.sxSJConditionCheckReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_CANCEL_REQ)
    public Response sjCancelReq(@RequestBody Params.SJCancelReqParm params) {
        log.info("sjCancelReqInParm : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  sxSJCancelReq
        sortService.sxSJCancelReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_priority_modify/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_PRIORITY_CHANGE_REQ)
    public Response sjPriorityChangeReq(@RequestBody Params.SortJobPriorityChangeReqParam params) {
        log.info("SortJobPriorityChangeReqParam : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_PRIORITY_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txSortJobPriorityChangeReq
        sortService.sxSortJobPriorityChangeReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_status_chg/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_STATUS_CHANGE_RPT)
    public Response sjStatusChgRpt(@RequestBody  Params.SJStatusChgRptParams params) {
        log.info("SJStatusChgRptParams : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txSortJobPriorityChangeReq
        sortService.sxSJStatusChgRpt(objCommon, params);

        return Response.createSucc(transactionID, null);
    }
}