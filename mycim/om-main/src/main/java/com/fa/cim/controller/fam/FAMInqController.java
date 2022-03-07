package com.fa.cim.controller.fam;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.fam.IFAMInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.fam.Outputs;
import com.fa.cim.fam.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.fam.IFAMInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date defect# person comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/4/21 ******** zh create file
 *
 * @author: zh
 * @date: 2021/4/21 16:47
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/fam")
@Listenable
public class FAMInqController implements IFAMInqController {

    @Autowired
    private IFAMInqService famInqService;
    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @RequestMapping(value = "/sort_job_history_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FAM_SORTJOB_HISTORY_INQ)
    public Response sortJobHistoryListInq(@RequestBody Params.SortJobHistoryParams sortJobHistoryParams) {
        //【step0】init params
//        final String transactionID = TransactionIDEnum.SORT_JOB_HISTORY_INQ.getValue();
//        ThreadContextHolder.setTransactionId(transactionID);
//
//        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
//        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, sortJobHistoryParams.getUser(), accessControlCheckInqParams);

        Boolean hasSJHistory = famInqService.sxHasSJHistoryListInq(sortJobHistoryParams);
        return Response.createSucc(hasSJHistory);
    }

    @ResponseBody
    @RequestMapping(value = "/auto_splits/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.FAM_AUTO_SPLITS_INQ)
    @Override
    public Response autoSplitsInq(@RequestBody com.fa.cim.fam.Params.AutoSplitsInqParams params) {
        Results.AutoSplitResult autoSplitResult = new Results.AutoSplitResult();
        List<Outputs.AutoSplitOut> autoSplitOuts = famInqService.autoSplits();
        autoSplitResult.setAutoSplitOuts(autoSplitOuts);
        return Response.createSucc(autoSplitResult);
    }

    @ResponseBody
    @RequestMapping(value = "/ava_carrier/inq")
    @Override
    public Response availableCarrierInq(@RequestBody com.fa.cim.fam.Params.AvailableCarrierInqParams params) {
        Infos.ObjCommon objCommon = new Infos.ObjCommon();
        List<Infos.AvailableCarrierOut> strings = famInqService.sxAvailableCarrierListForLotStartInq(objCommon, params);
        return Response.createSucc(null, strings);
    }

    @ResponseBody
    @RequestMapping(value = "/auto_merges/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.FAM_AUTO_MERGES_INQ)
    @Override
    public Response autoMergesInq(@RequestBody com.fa.cim.fam.Params.AutoMergesInqParams params) {
        Results.AutoMergesResult autoMergesResult = new Results.AutoMergesResult();
        List<Outputs.AutoMergeOut> autoMergeOuts = famInqService.autoMerges();
        autoMergesResult.setAutoSplitOuts(autoMergeOuts);
        return Response.createSucc(autoMergesResult);
    }
}