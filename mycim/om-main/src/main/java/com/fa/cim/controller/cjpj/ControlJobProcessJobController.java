package com.fa.cim.controller.cjpj;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.interfaces.controlJobProcessJob.IControlJobProcessJobController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
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
 * 2019/7/31          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/31 10:36
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IControlJobProcessJobController.class, confirmableKey = "ControlJobProcessJobConfirm", cancellableKey = "ControlJobProcessJobCancel")
@RequestMapping("/cjpj")
@Listenable
public class ControlJobProcessJobController implements IControlJobProcessJobController {

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IControlJobMethod controlJobMethod;
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IControlJobProcessJobService controlJobProcessJobService;

    @ResponseBody
    @RequestMapping(value = "/cj_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONTROL_JOB_MANAGE_REQ)
    public Response cjStatusChangeReq(@RequestBody Params.CJStatusChangeReqParams params) {

        //【step0】init params
        if (log.isDebugEnabled()){
            log.debug("step1 - check input Param");
        }
        final String transactionID = TransactionIDEnum.CONTROL_JOB_MANAGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //【step0】check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.CONTROL_JOB_MANAGE_REQ, user);

        //【step2】call checkPrivilegeAndGetObjCommon(...)
        if (log.isDebugEnabled()){
            log.debug("step2 - checkPrivilegeAndGetObjCommon");
        }
        List<ObjectIdentifier> lotIDs = null;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        //  tmpPrivilegeCheckCJValue = "1";
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue)
                && CimStringUtils.equals(tmpPrivilegeCheckCJValue, BizConstant.VALUE_ONE)) {
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setEquipmentID(params.getControlJobCreateRequest().getEquipmentID());
        accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);


        //【step3】call sxCJStatusChangeReqService
        if (log.isDebugEnabled()){
            log.debug("step3 - sxCJStatusChangeReqService");
        }
        Results.CJStatusChangeReqResult result = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon,
                params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/pj_info/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_JOB_INFO_RPT)
    public Response pjInfoRpt(@RequestBody Params.PJInfoRptParams params) {
        final String transactionID = TransactionIDEnum.PROCESS_JOB_INFO_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        Validations.check(user == null || ObjectIdentifier.isEmpty(controlJobID) || ObjectIdentifier.isEmpty(equipmentID), "user or equipmentID or controlJobID is null");

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Infos.ObjCommon objCommon = checkPrivilegeAndGetObjCommonForWLC(user, controlJobID, equipmentID);

        log.info("【step3】call sxPJInfoRpt()");
        controlJobProcessJobService.sxPJInfoRpt(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/pj_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROCESS_JOB_MANAGE_REQ)
    public Response pjStatusChangeReq(@RequestBody Params.PJStatusChangeReqParams params) {
        final String transactionID = TransactionIDEnum.PROCESS_JOB_MANAGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Infos.PJStatusChangeReqInParm strPJStatusChangeReqInParm = params.getStrPJStatusChangeReqInParm();
        ObjectIdentifier equipmentID = strPJStatusChangeReqInParm.getEquipmentID();
        ObjectIdentifier controlJobID = strPJStatusChangeReqInParm.getControlJobID();
        Validations.check(user == null
                || CimObjectUtils.isEmpty(strPJStatusChangeReqInParm)
                || ObjectIdentifier.isEmpty(equipmentID)
                || ObjectIdentifier.isEmpty(controlJobID),
                "user or strPJStatusChangeReqInParm or strPJStatusChangeReqInParm.equipmentID is null");

        if (log.isDebugEnabled()){
            log.debug("call checkPrivilegeAndGetObjCommonForWLC(...) and get schedule from calendar");
        }
        Infos.ObjCommon objCommon = checkPrivilegeAndGetObjCommonForWLC(user, controlJobID, equipmentID);

        if (log.isDebugEnabled()){
            log.debug("call sxPJStatusChangeReq()");
        }
        controlJobProcessJobService.sxPJStatusChangeReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/pj_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_JOB_STATUS_CHANGE_RPT)
    public Response pjStatusChangeRpt(@RequestBody Params.PJStatusChangeRptInParm params) {
        log.info("PJStatusChangeRptInParm : {}", params);
        final String transactionID = TransactionIDEnum.PROCESS_JOB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Asserts.check(null != user, "the check request is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //step2 -  call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            //Step2 - controlJob_lotIDList_GetDR
            log.info("Call controlJob_lotIDList_GetDR");
            lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        controlJobProcessJobService.sxPJStatusChangeRpt(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/pj_map_info/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_JOB_MAP_INFO_RPT)
    public Response processJobMapInfoRpt(@RequestBody Params.ProcessJobMapInfoRptParam processJobMapInfoRptParam) {
        final String transactionID = TransactionIDEnum.PROCESS_JOB_MAP_INFO_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = processJobMapInfoRptParam.getUser();
        ObjectIdentifier equipmentID = processJobMapInfoRptParam.getProcessJobMapInfoRptInParm().getEquipmentID();
        ObjectIdentifier controlJobID = processJobMapInfoRptParam.getProcessJobMapInfoRptInParm().getControlJobID();
        Validations.check(user == null || ObjectIdentifier.isEmpty(controlJobID) || ObjectIdentifier.isEmpty(equipmentID), "user or equipmentID or controlJobID is null");
        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Infos.ObjCommon objCommon = checkPrivilegeAndGetObjCommonForWLC(user, controlJobID, equipmentID);
        log.info("【step3】call sxPJInfoRpt()");
        controlJobProcessJobService.sxProcessJobMapInfoRpt(objCommon, processJobMapInfoRptParam.getProcessJobMapInfoRptInParm());
        return Response.createSucc(transactionID);
    }

    /**
     * description: check privilege for pjInfoRpt, pjStatusChangeReq
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param user
     * @param equipmentID -
     * @return com.fa.cim.dto.Infos.ObjCommon
     * @author Nyx
     * @date 2019/7/12 16:52
     */
    private Infos.ObjCommon checkPrivilegeAndGetObjCommonForWLC(User user, ObjectIdentifier controlJobID, ObjectIdentifier equipmentID) {
        log.info("【step1】get schedule from calendar");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(ThreadContextHolder.getTransactionId(), user);
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            List<ObjectIdentifier> controlJobLotIDList = controlJobMethod.controlJobLotIDListGetDR(objCommon, controlJobID);
            accessControlCheckInqParams.setLotIDLists(controlJobLotIDList);
        }
        log.info("【step2】call txAccessControlCheckInq(...)");
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        return objCommon;
    }
}
