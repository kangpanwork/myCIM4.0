package com.fa.cim.controller.flowbatch;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.flowBatch.IFlowBatchInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.flowbatch.IFlowBatchInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:14
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/flowb")
@Listenable
public class FlowBatchInqController implements IFlowBatchInqController {
    @Autowired
    private IFlowBatchInqService flowBatchInqService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;

    @ResponseBody
    @RequestMapping(value = "/floating_batch_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FLOATING_BATCH_INQ)
    public Response floatingBatchListInq(@RequestBody Params.FloatingBatchListInqParams params) {
        String transactionID = TransactionIDEnum.FLOATING_BATCH_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 txFloatingBatchListInq
        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        Results.FloatingBatchListInqResult result = null;
        try {
            result = flowBatchInqService.sxFloatingBatchListInq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundFlowBatch(), e.getCode()) && CimNumberUtils.intValue(returnOK) == 1) {
                result = new Results.FloatingBatchListInqResult();
            } else {
                throw e;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/flow_batch_lot_selection/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FLOW_BATCH_CANDIDATE_INQ)
    public Response flowBatchLotSelectionInq(@RequestBody Params.FlowBatchLotSelectionInqParam params) {
        final String transactionID = TransactionIDEnum.FLOW_BATCH_CANDIDATE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        // step3  - txFlowBatchLotSelectionInq__090
        Results.FlowBatchLotSelectionInqResult retVal = null;
        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            retVal = flowBatchInqService.sxFlowBatchLotSelectionInq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundFlowbatchCandLot(), e.getCode()) && CimNumberUtils.intValue(returnOK) == 1) {
                retVal = new Results.FlowBatchLotSelectionInqResult();
            } else {
                throw e;
            }
        }
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/flow_batch_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FLOW_BATCH_INQ)
    public Response flowBatchInfoInq(@RequestBody Params.FlowBatchInfoInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FLOW_BATCH_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxReworkWholeLotCancel
        Results.FlowBatchInfoInqResult result = flowBatchInqService.sxFlowBatchInfoInq(objCommon, params.getFlowBatchID(), params.getLotID(), params.getEquipmentID());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "flow_batch_stray_lots_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FLOW_BATCH_LOST_LOTS_INQ)
    public Response flowBatchStrayLotsListInq(@RequestBody Params.FlowBatchStrayLotsListInqParams params) {
        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.FLOW_BATCH_LOST_LOTS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step-4:txFlowBatchStrayLotsListInq;
        log.debug("【Step-4】call-txFlowBatchStrayLotsListInq(...)");
        Results.FlowBatchStrayLotsListInqResult result = flowBatchInqService.sxFlowBatchStrayLotsListInq(objCommon);
        // Step-5:Post Process(Generate Output Results/Set Transaction ID);
        return Response.createSucc(transactionID, result);
    }
}