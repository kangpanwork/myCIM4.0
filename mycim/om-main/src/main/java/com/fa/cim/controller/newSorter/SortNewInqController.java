package com.fa.cim.controller.newSorter;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.newSorter.ISortNewInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.newSorter.ISortNewInqService;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.Results;
import com.fa.cim.sorter.SorterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/sort")
@Listenable
public class SortNewInqController implements ISortNewInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISortNewInqService sortNewInqService;

    @ResponseBody
    @RequestMapping(value = "/online_sorter_action_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ACTION_LIST_INQ)
    public Response onlineSorterActionSelectionInq(@RequestBody Params.OnlineSorterActionSelectionInqParams params) {
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ACTION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - get sorter action by equipment");
        }
        Results.OnlineSorterActionSelectionInqResult result = sortNewInqService.sxOnlineSorterActionSelectionInq(objCommon, params);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/sj_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_JOB_LIST_INQ)
    public Response SJListInq(@RequestBody Params.SJListInqParams params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - get sortJob list");
        }
        List<Info.SortJobListAttributes> sortJobListAttributes = sortNewInqService.sxSJListInq(objCommon, params);
        return Response.createSucc(transactionID, sortJobListAttributes);
    }


    @ResponseBody
    @PostMapping(value = "/action/inq")
    @Override
    @CimMapping(TransactionIDEnum.SORT_ACTION_INQ)
    public Response sorterActionInq(@RequestBody Params.SorterActionInqParams params) {

        final String transactionID = TransactionIDEnum.SORT_ACTION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);
        // Post Process
        return Response.createSucc(transactionID, sortNewInqService
                .sorterActionInq(objCommon, params, SorterType.Status.Created.getValue()));
    }

    @ResponseBody
    @RequestMapping(value = "/job_history/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_JOB_HISTORY_INQ)
    public Response getSortJobHistoryInq(@Validated @RequestBody Params.SortJobHistoryParams params) {
        final String transactionID = TransactionIDEnum.SORT_JOB_HISTORY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);
        return Response.createSucc(transactionID, sortNewInqService.getSortJobHistory(objCommon, params));
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_and_lot/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CARRIER_AND_LOT_INQ)
    public Response carrierAndLotHisInq(@RequestBody Params.OnlineSorterActionSelectionInqParams params) {
        final String transactionID = TransactionIDEnum.CARRIER_AND_LOT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step1 - Authority certification");
        }
        com.fa.cim.dto.Params.AccessControlCheckInqParams accessControlCheckInqParams = new com.fa.cim.dto.Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step2 - get carrier and lot list");
        }
        Info.CarrierAndLotHistory carrierAndLotHistory = sortNewInqService.sxCarrierAndLotHisInq(objCommon, params.getEquipmentID());
        return Response.createSucc(transactionID, carrierAndLotHistory);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/scanbar_code", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SORT_SCANBAR_CODE_INQ)
    public Response scanbarCodeInq(@Validated @RequestBody  Params.ScanBarCodeInqParams params) {
        final String transactionID = TransactionIDEnum.SORT_SCANBAR_CODE_INQ.getValue();
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
        sortNewInqService.scanbarCodeInq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

}