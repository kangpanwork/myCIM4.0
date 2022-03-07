package com.fa.cim.controller.tms;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.transferManagementSystem.ITransferManagementSystemInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.tms.ITransferManagementSystemInqService;
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
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:16
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Listenable
public class TransferManagementSystemInqController implements ITransferManagementSystemInqController {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private ITransferManagementSystemInqService transferManagementSystemInqService;

    @ResponseBody
    @RequestMapping(value = "/where_next_stocker/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WHERE_NEXT_INTER_BAY_INQ)
    public Response whereNextStockerInq(@RequestBody Params.WhereNextStockerInqParams params) {
        final String transactionID = TransactionIDEnum.WHERE_NEXT_INTER_BAY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - call txWhereNextStockerInq
        Results.WhereNextStockerInqResult result = transferManagementSystemInqService.sxWhereNextStockerInq(objCommon, params.getLotID(), params.getCassetteID());


        return Response.createSucc(transactionID, result);

    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_job_detail_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CASSETTE_XFER_JOB_DETAIL_INQ)
    public Response carrierTransferJobDetailInfoInq(@RequestBody Params.CarrierTransferJobDetailInfoInqParam params) {
        final String transactionID = TransactionIDEnum.LOT_CASSETTE_XFER_JOB_DETAIL_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step-3】:txCarrierTransferJobDetailInfoInq
        log.debug("【Step-3】txCarrierTransferJobDetailInfoInq(...)");
        Results.LotCassetteXferJobDetailResult result = transferManagementSystemInqService.sxCarrierTransferJobDetailInfoInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_job_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CASSETTE_XFER_JOB_INQ)
    public Response carrierTransferJobInfoInq(@RequestBody Params.CarrierTransferJobInfoInqParam params) {
        //【Step-0】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.LOT_CASSETTE_XFER_JOB_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step-3】:txCarrierTransferJobInfoInq;
        log.debug("【Step-3】txCarrierTransferJobInfoInq(...)");
        Results.CarrierTransferJobInfoInqResult result = transferManagementSystemInqService.sxCarrierTransferJobInfoInq(objCommon, params);
        String returnOK = null;
        try {
            returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        } catch (ServiceException e) {
            if ((e.getCode().equals(retCodeConfig.getTmsRecordNotFound()) && ("1".equals(returnOK)))) {
                log.info("Reset RC_OK");
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STOCKER_INFO_INQ)
    public Response stockerInfoInq(@RequestBody Params.StockerInfoInqInParams params) {

        final String transactionID = TransactionIDEnum.STOCKER_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setStockerID(params.getMachineID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txStockerInfoInq
        Results.StockerInfoInqResult result = transferManagementSystemInqService.sxStockerInfoInq(objCommon, params.getMachineID(), params.isDetailFlag());

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STOCKER_LIST_INQ)
    public Response stockerListInq(@RequestBody Params.StockerListInqInParams params) {

        final String transactionID = TransactionIDEnum.STOCKER_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txStockerListInq
        Results.StockerListInqResult result = null;

        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            result = transferManagementSystemInqService.sxStockerListInq(objCommon, params.getStockerType(), params.isAvailFlag());
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundStkType(), ex.getCode())) {
                if (!CimStringUtils.equals(returnOK, "1")){
                    throw ex;
                } else {
                    result = ex.getData(Results.StockerListInqResult.class);
                }
            }
        }

        return Response.createSucc(transactionID, result);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/3/17 13:26
     */
    @ResponseBody
    @RequestMapping(value = "/where_next_uts_carrier/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.WHERE_NEXT_UTS_CARRIER_INQ)
    public Response cxWhereNextOHBCarrierInq(@RequestBody Params.WhereNextOHBCarrierInqInParm strWhereNextOHBCarrierInqInParm) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Infos.ObjCommon strObjCommonIn ;
        String txID=TransactionIDEnum.WHERE_NEXT_UTS_CARRIER_INQ.getValue();

        // TODO: Event Log Put

        //-------------------------------------
        // calendar_GetCurrentTimeDR
        //-------------------------------------

        // step-1 - calendar_GetCurrentTimeDR

        Params.AccessControlCheckInqParams accessControlCheckInqParams=new Params.AccessControlCheckInqParams();

        //-------------------------------------
        // txAccessControlCheckInq
        //-------------------------------------

        // step-2 - calendar_GetCurrentTimeDR
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(txID,strWhereNextOHBCarrierInqInParm.getUser(),accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        //------------------------------------
        // txWhereNextOHBCarrierInq
        //------------------------------------

        // step-3 - txWhereNextOHBCarrierInq

        ObjectIdentifier retVal = transferManagementSystemInqService.sxWhereNextOHBCarrierInq(
                strObjCommonIn,
                strWhereNextOHBCarrierInqInParm);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/

        return Response.createSucc(retVal) ;
    }

    @ResponseBody
    @RequestMapping(value = "/all_eqp_for_auto_transfer/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ALL_AVAILABLE_EQP_INQ)
    public Response allEqpForAutoTransferInq(@RequestBody Params.AllAvailableEqpParams allAvailableEqpParams) {

        final TransactionIDEnum transactionId = TransactionIDEnum.ALL_AVAILABLE_EQP_INQ;
        ThreadContextHolder.setTransactionId(transactionId.getValue());

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), allAvailableEqpParams.getUser(), accessControlCheckInqParams);

        //step3 - txAllEqpForAutoTransferInq
        List<ObjectIdentifier> result = transferManagementSystemInqService.sxAllEqpForAutoTransferInq(objCommon);

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_for_auto_transfer/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.AVAILABLE_EQP_INQ)
    public Response eqpForAutoTransferInq(@RequestBody Params.EqpForAutoTransferInqParams eqpForAutoTransferInqParams) {

        String transactionId = TransactionIDEnum.AVAILABLE_EQP_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionId);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(eqpForAutoTransferInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId, eqpForAutoTransferInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txEqpForAutoTransferInq
        List<ObjectIdentifier> equipmentIDs = transferManagementSystemInqService.sxEqpForAutoTransferInq(objCommon, eqpForAutoTransferInqParams.getEquipmentID(), eqpForAutoTransferInqParams.getLotID(),
                eqpForAutoTransferInqParams.getCassetteID(), eqpForAutoTransferInqParams.getDurableID());

        return Response.createSuccWithOmCode(transactionId, CimArrayUtils.isEmpty(equipmentIDs) ? new OmCode(retCodeConfig.getSucc().getCode(), retCodeConfig.getNotFoundEntry().getMessage()) : retCodeConfig.getSucc(), equipmentIDs);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_for_auto_transfer/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.AVAILABLE_STOCKER_INQ)
    public Response stockerForAutoTransferInq(@RequestBody Params.StockerForAutoTransferInqParams stockerForAutoTransferInqParams) {

        final String transactionID = TransactionIDEnum.AVAILABLE_STOCKER_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, stockerForAutoTransferInqParams.getUser(), accessControlCheckInqParams);

        //step3 - txStockerForAutoTransferInq
        List<Infos.AvailableStocker> result = null;

        String returnOK = StandardProperties.OM_RC_WHEN_NO_DATA_FOR_LIST_INQ.getValue();
        try {
            result = transferManagementSystemInqService.sxStockerForAutoTransferInq(objCommon);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundAvailStk(), ex.getCode()) && CimStringUtils.equals(returnOK, "1")) {
            }
        }

        Results.StockerForAutoTransferInqResult stockerForAutoTransferInqResult = new Results.StockerForAutoTransferInqResult();
        stockerForAutoTransferInqResult.setStrAvailableStocker(result);
        return Response.createSucc(objCommon.getTransactionID(), stockerForAutoTransferInqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/durable_where_next_stocker/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_WHERE_NEXT_INTER_BAY_INQ)
    public Response durableWhereNextStockerInq(@RequestBody Params.DurableWhereNextStockerInqParams params) {
        final String transactionID = TransactionIDEnum.DURABLE_WHERE_NEXT_INTER_BAY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - call sxDurableWhereNextStockerInq
        Results.DurableWhereNextStockerInqResult result = transferManagementSystemInqService.sxDurableWhereNextStockerInq(objCommon, params.getDurableID());

        return Response.createSucc(transactionID, result);
    }
}