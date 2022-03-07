package com.fa.cim.controller.automonitor;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorController;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorInqController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.automonitor.IAutoMonitorService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/13      ********             Neko                create file
 *
 * @author Neko
 * @since 2019/11/13 18:00
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IAutoMonitorInqController.class, confirmableKey = "AutoMonitorConfirm", cancellableKey = "AutoMonitorCancel")
@RequestMapping("/amon")
@Listenable
public class AutoMonitorController implements IAutoMonitorController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postTaskExecuteReqController;

    @Autowired
    private IAutoMonitorService autoMonitorService;

    @ResponseBody
    @RequestMapping(value = "/am_job_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_JOB_STATUS_CHANGE_RPT)
    @Override
    public Response amJobStatusChangeRpt(@RequestBody Params.AMJobStatusChangeRptInParm param) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.EQP_MONITOR_JOB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        String result = autoMonitorService.sxAMJobStatusChangeRpt(objCommon, param);
        // -------------------------------------
        // Post-Processing Registration Section [Only when retVal.monitorJobStatus=Ready]
        //-------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_READY, result)) {
            Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
            processActionRegistReqParams.setTransactionID(transactionID);
            processActionRegistReqParams.setPatternID(null);
            processActionRegistReqParams.setKey(null);
            processActionRegistReqParams.setSequenceNumber(-1);
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setEquipmentID(param.getEquipmentID());
            processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);

            //----------------------------------
            // Post-Processing Execution Section
            //----------------------------------

            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams.setUser(param.getUser());
            postTaskExecuteReqParams.setKey(postTaskRegisterReqResult.getDKey());
            postTaskExecuteReqParams.setSyncFlag(1);
            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
            postTaskExecuteReqParams.setKeyTimeStamp(null);
            postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams);
        }

        Response response = Response.createSucc(transactionID, null);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/old/am_job_lot_reserve/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ)
    public Response amJobLotReserveReqOld(@RequestBody Params.AMJobLotReserveReqInParams param) {
        final String transaction = TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ.getValue();
        log.info("AMJobLotReserveReqInParams : {}", param);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        List<ObjectIdentifier> lots = null;
        int mapSize = CimArrayUtils.getSize(param.getStrProductLotMap());
        for (int i = 0; i < mapSize; i++) {
            lots = param.getStrProductLotMap().get(i).getEqpMonitorLotIDs();
        }
        accessControlCheckInqParams.setLotIDLists(lots);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transaction, param.getUser(), accessControlCheckInqParams);

        /*---------------------------*/
        /*   Main Process            */
        /*---------------------------*/
        Results.AMJobLotReserveReqResult retVal = autoMonitorService.sxAMJobLotReserveReq(objCommon, param);

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
        processActionRegistReqParams.setTransactionID(transaction);
        processActionRegistReqParams.setSequenceNumber(-1);
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParam.setEquipmentID(param.getEquipmentID());
        processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);


        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(param.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqController.postTaskExecuteReq(postTaskExecuteReqParams);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transaction, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/am_job_lot_reserve/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ)
    @EnablePostProcess
    @Override
    public Response amJobLotReserveReq(@RequestBody Params.AMJobLotReserveReqInParams param) {
        final String transaction = TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ.getValue();
        log.info("AMJobLotReserveReqInParams : {}", param);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        List<ObjectIdentifier> lots = null;
        int mapSize = CimArrayUtils.getSize(param.getStrProductLotMap());
        for (int i = 0; i < mapSize; i++) {
            lots = param.getStrProductLotMap().get(i).getEqpMonitorLotIDs();
        }
        accessControlCheckInqParams.setLotIDLists(lots);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transaction, param.getUser(),
                accessControlCheckInqParams);

        /*---------------------------*/
        /*   Main Process            */
        /*---------------------------*/
        Results.AMJobLotReserveReqResult retVal = autoMonitorService.sxAMJobLotReserveReq(objCommon, param);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transaction, retVal);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/am_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_STATUS_CHANGE_RPT)
    @Override
    public Response amStatusChangeRpt(@RequestBody Params.AMStatusChangeRptInParm param) {
        log.info("AMStatusChangeRptInParm : {}", param);
        final String transactionID = TransactionIDEnum.EQP_MONITOR_STATUS_CHANGE_RPT.getValue();
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.AMStatusChangeRptResult result = autoMonitorService.sxAMStatusChangeRpt(objCommon, param);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/am_set/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_UPDATE_RPT)
    @Override
    public Response amSetReq(@RequestBody Params.AMSetReqInParm param) {
        // init params
        final String txId = TransactionIDEnum.EQP_MONITOR_UPDATE_RPT.getValue();

        log.info("AMSetReqInParm : {}", param);
        User user = param.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        autoMonitorService.sxAMSetReq(objCommon, param);

        Response response = Response.createSucc(txId, null);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/am_schedule_chg/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_SCHEDULE_UPDATE_REQ)
    @Override
    public Response amScheduleChgReq(@RequestBody Params.EqpMonitorScheduleUpdateInParm param) {
        // init params
        final String txId = TransactionIDEnum.EQP_MONITOR_SCHEDULE_UPDATE_REQ.getValue();

        log.info("AMSetReqInParm : {}", param);
        User user = param.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        autoMonitorService.sxAMScheduleChgReq(objCommon, param);

        Response response = Response.createSucc(txId, null);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/am_recover/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_RESET_REQ)
    @Override
    public Response amRecoverReq(@RequestBody Params.AMRecoverReqParams param) {
        // init params
        final String txId = TransactionIDEnum.EQP_MONITOR_RESET_REQ.getValue();

        log.info("AMSetReqInParm : {}", param);
        User user = param.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(param.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        autoMonitorService.sxAMRecoverReq(objCommon, param);

        Response response = Response.createSucc(txId, null);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/am_wafer_usage_update/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_WAFER_USED_COUNT_UPDATE_REQ)
    @Override
    public Response eqpMonitorWaferUsedCountUpdateReq(@RequestBody Params.EqpMonitorUsedCountUpdateReqInParam eqpMonitorUsedCountUpdateReqInParam) {
        // init params
        final String txId = TransactionIDEnum.EQP_MONITOR_WAFER_USED_COUNT_UPDATE_REQ.getValue();

        log.info("eqpMonitorUsedCountUpdateReqInParam : {}", eqpMonitorUsedCountUpdateReqInParam);
        User user = eqpMonitorUsedCountUpdateReqInParam.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(eqpMonitorUsedCountUpdateReqInParam.getStrEqpLotMonitorWaferUsedCountSeq().get(0).getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        autoMonitorService.sxEqpMonitorWaferUsedCountUpdateReq(objCommon, eqpMonitorUsedCountUpdateReqInParam);

        Response response = Response.createSucc(txId, null);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }
}
