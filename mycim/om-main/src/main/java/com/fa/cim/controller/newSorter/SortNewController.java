package com.fa.cim.controller.newSorter;


import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.newSorter.ISortNewController;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.frameworks.pprocess.proxy.impl.SorterActionReportProxy;
import com.fa.cim.frameworks.pprocess.proxy.impl.SorterActionRequestProxy;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.newSorter.ISortNewService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@RequestMapping("/v1/sort")
@Listenable
public class SortNewController implements ISortNewController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISortNewService sortNewService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @ResponseBody
    @RequestMapping(value = "/wafer_sorter_action_register/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORT_ACTION_REGISTER_REQ)
    public Response waferSorterActionRegisterReq(@RequestBody Params.WaferSorterActionRegisterReqParams params) {
        final String transactionID = TransactionIDEnum.WAFER_SORT_ACTION_REGISTER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("action code management");
        }
        sortNewService.sxWaferSorterActionRegisterReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_priority_modify/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_PRIORITY_CHANGE_REQ)
    public Response sjPriorityChangeReq(@RequestBody Params.SortJobPriorityChangeReqParam params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_PRIORITY_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("change sort job Priority level");
        }
        sortNewService.sxSortJobPriorityChangeReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_CANCEL_REQ)
    public Response sjCancelReq(@RequestBody Params.SJCancelReqParm params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - cancel sortJob");
        }
        sortNewService.sxSJCancelReq(objCommon, params, params.getNotifyEAPFlag());
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_status_chg/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_STATUS_CHANGE_RPT)
    public Response sjStatusChgRpt(@Validated @RequestBody  Params.SJStatusChgRptParams params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - change sort job status");
        }
        sortNewService.sxSJStatusChgRpt(objCommon, params, false);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_JOB_CREATE_REQ)
    @EnablePostProcess
    public Response sjCreateReq(@Validated @RequestBody Params.SJCreateReqParams params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.
                cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);
        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        String sjID = sortNewService.sxSJCreateReq(objCommon, params);
        return Response.createSucc(transactionID, sjID);
    }


    @ResponseBody
    @RequestMapping(value = "/action/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_ACTION_REQ)
    @EnablePostProcess(proxy = SorterActionRequestProxy.class)
    public Response sorterActionReq(@RequestBody Info.SortJobInfo params) {
        final String transactionID = TransactionIDEnum.SORT_ACTION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params
                .AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);
        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        sortNewService.sxSorterActionReq(objCommon, params);
        if (log.isDebugEnabled()) {
            log.debug("step3 - return result");
        }
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/action/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_ACTION_RPT)
    @EnablePostProcess(proxy = SorterActionReportProxy.class)
    public Response sorterActionRpt(@RequestBody  Info.SortJobInfo params) {
        final String transactionID = TransactionIDEnum.SORT_ACTION_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)

        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<ObjectIdentifier> lotIDs = null;
        try {
            lotIDs = sortNewService.sxSorterActionRpt(objCommon, params);
        } catch (ServiceException ex){
            if (Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidActionCode())
                    ||Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidParameter())
                    ||Validations.isEquals(ex.getCode(),retCodeConfigEx.getInvalidSorterstatus())
                    ||Validations.isEquals(ex.getCode(),retCodeConfigEx.getEapReportedError())
                    ||Validations.isEquals(ex.getCode(),retCodeConfigEx.getSlotNoMismatchSlotmapEqpctnpst())
                    ||Validations.isEquals(ex.getCode(),retCodeConfigEx.getInvalidSorterJobid())
                    || Validations.isEquals(ex.getCode(),retCodeConfigEx.getWafersorterSlotmapCompareError())){
                return Response.createSucc(transactionID, lotIDs);
            }
            if (log.isErrorEnabled()) {
                log.error(ex.getMessage());
            }
            throw ex;
        }

        return Response.createSucc(transactionID, lotIDs);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_slotmap_compare/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_DATA_COMPARE_REQ)
    public Response onlineSorterSlotmapCompareReq(@RequestBody Params.SJListInqParams params) {
        final String transactionID = TransactionIDEnum.WAFER_SORTER_DATA_COMPARE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        Info.WaferSorterCompareCassette result = sortNewService.sxOnlineSorterSlotmapCompareReq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_slotmap_adjust/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ)
    public Response onlineSorterSlotmapAdjustReq(@RequestBody Params.OnlineSorterSlotmapAdjustReqParam params) {
        final String transactionID = TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        sortNewService.sxOnlineSorterSlotmapAdjustReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/action/start", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ACTION_START_REQ)
    public Response sortActionStart(@RequestBody Params.SJCreateReqParams params) {
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ACTION_START_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto
                .Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService
                .checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        sortNewService.sortActionStart(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/sj_condition_check/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SJ_CONDITION_CHECK_REQ)
    public Response sjConditionCheckReq(@RequestBody Params.SortJobCheckConditionReqInParam params) {
        final String transactionID = TransactionIDEnum.SJ_CONDITION_CHECK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(params.getStrSorterComponentJobListAttributesSequence())) {
            for (Info.ComponentJob sorterComponentJobListAttributes : params.getStrSorterComponentJobListAttributesSequence()) {
                if (CimArrayUtils.isNotEmpty(sorterComponentJobListAttributes.getWaferList())) {
                    for (Info.WaferSorterSlotMap waferSorterSlotMap : sorterComponentJobListAttributes.getWaferList()) {
                        lotIDLists.add(waferSorterSlotMap.getLotID());
                    }
                }
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService
                .checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - Main Process");
        }
        sortNewService.sxSJConditionCheckReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }


    @ResponseBody
    @RequestMapping(value = "/wafer_slotmap_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SLOTMAP_CHANGE_REQ)
    public Response waferSlotmapChangeReq(@RequestBody Params.WaferSlotMapChange params) {
        log.info(" WaferSlotmapChangeReqParams :{}", params);
        final String transactionID = TransactionIDEnum.WAFER_SLOTMAP_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getSortJobInfo().getEquipmentID());

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        sortNewService.sxWaferSlotmapChangeReq(objCommon,params.getSortJobInfo(),params.getBNotifyToTCS());

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_exchange/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_EXCHANGE_REQ)
    public Response carrierExchangeReq(@RequestBody Params.CarrierExchangeParams params) {
        log.debug("carrierExchangeReq(): invoke carrierExchangeReq");
        String txId = TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //Step2 - cassette_lotIDList_GetDR, cassetteLotIDList will used in txAccessControlCheckInq
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        Infos.LotListInCassetteInfo lotIDs = null;
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            lotIDs = cassetteMethod.cassetteLotIDListGetDR(null, params.getSortJobInfo().getComponentJob().getOriginalCassetteID());
        }

        //Step3 - txAccessControlCheckInq
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getSortJobInfo().getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(CimObjectUtils.isEmpty(lotIDs) ? null : lotIDs.getLotIDList());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step4 - txCarrierExchangeReq
        sortNewService.sxCarrierExchangeReq(objCommon, params.getSortJobInfo());
        return Response.createSucc(txId, null);
    }


}