package com.fa.cim.controller.sort;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.sort.ISortInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.sort.ISortInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/2          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/8/2 11:26
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/sort")
@Listenable
public class SortInqController implements ISortInqController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISortInqService sortInqService;

    @ResponseBody
    @RequestMapping(value = "/online_sorter_scrap_wafer/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_SCRAP_WAFER_INQ)
    public Response onlineSorterScrapWaferInq(@RequestBody Params.OnlineSorterScrapWaferInqParams params) {
        log.info("OnlineSorterScrapWaferInqInParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_SCRAP_WAFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterScrapWaferInq
        List<Infos.LotWaferMap> result = sortInqService.sxOnlineSorterScrapWaferInq(objCommon, params);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_action_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_ACTION_LIST_INQ)
    public Response onlineSorterActionSelectionInq(@RequestBody Params.OnlineSorterActionSelectionInqParams params) {
        log.info("OnlineSorterActionSelectionInqInParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_ACTION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterActionSelectionInq
        Results.OnlineSorterActionSelectionInqResult result = sortInqService.sxOnlineSorterActionSelectionInq(objCommon, params);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/online_sorter_action_status/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WAFER_SORTER_DATA_INQ)
    public Response OnlineSorterActionStatusInq(@RequestBody Params.OnlineSorterActionStatusInqParm params) {
        log.info("OnlineSorterActionStatusInqParm : {}", params);
        final String transactionID = TransactionIDEnum.WAFER_SORTER_DATA_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterActionSelectionInq
        List<Infos.WaferSorterSlotMap> result = sortInqService.sxOnlineSorterActionStatusInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/sj_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_JOB_LIST_INQ)
    public Response SJListInq(@RequestBody Params.SJListInqParams params) {
        log.info("SJListInqParams : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterActionSelectionInq
        List<Infos.SortJobListAttributes> sortJobListAttributes = sortInqService.sxSJListInq(objCommon, params);
        return Response.createSucc(transactionID, sortJobListAttributes);
    }

    @ResponseBody
    @RequestMapping(value = "/sj_status/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SORT_JOB_STATUS_LIST_INQ)
    public Response SJStatusInq(@RequestBody Params.SJStatusInqParams params) {
        log.info("SJStatusInqParams : {}", params);
        final String transactionID = TransactionIDEnum.SORT_JOB_STATUS_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        //【step3】 -  txOnlineSorterActionSelectionInq
        Results.SJStatusInqResult sjStatusInqResult = sortInqService.sxSJStatusInq(objCommon, params.getSorterJobID());

        // Post Process
        return Response.createSucc(transactionID, sjStatusInqResult);
    }

    @ResponseBody
    @PostMapping(value = "/sj_info_for_lot_start/inq")
    @Override
    public Response sjInfoForAutoStartInq(@RequestBody com.fa.cim.fam.Params.SjInfoForAutoStartInqParams params) {
        return Response.createSucc(null, sortInqService.sxSJInfoForAutoLotStartInq(new Infos.ObjCommon()));
    }

    @ResponseBody
    @RequestMapping(value = "/get_category_by_lot/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.GET_CATEGORY_BY_LOT_INQ)
    public Response reqCategoryGetByLot(@RequestBody Params.ReqCategoryGetByLotParams params) {
        final String transactionID = TransactionIDEnum.GET_CATEGORY_BY_LOT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        Results.reqCategoryGetByLotResult result = new Results.reqCategoryGetByLotResult();
        //【step3】 -  txOnlineSorterActionSelectionInq
        String castCategory = sortInqService.reqCategoryGetByLot(objCommon, params.getLotID());
        result.setCarrierCategory(castCategory);
        // Post Process
        return Response.createSucc(transactionID, result);
    }
}