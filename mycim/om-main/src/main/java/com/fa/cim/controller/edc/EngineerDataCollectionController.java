package com.fa.cim.controller.edc;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.engineerDataCollection.IEngineerDataCollectionController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.edc.IEngineerDataCollectionService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fa.cim.common.constant.BizConstant.SP_SUBSYSTEMID_MM;
import static com.fa.cim.common.constant.BizConstant.SP_SYSTEMMSGCODE_DCSIFERR;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 14:09
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IEngineerDataCollectionController.class, confirmableKey = "EngineerDataCollectionConfirm", cancellableKey = "EngineerDataCollectionCancel")
@RequestMapping("/edc")
@Listenable
public class EngineerDataCollectionController implements IEngineerDataCollectionController {
    @Autowired
    private IEngineerDataCollectionService engineerDataCollectionService;
    @Autowired
    private IUtilsComp utilsCompService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IControlJobMethod controlJobMethod;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private PostController postController;
    @Autowired
    private IPostService postService;
    @Autowired
    private ISystemService systemService;
    @ResponseBody
    @RequestMapping(value = "/edc_with_spec_check_action/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_ACTION_REQ)
    public Response edcWithSpecCheckActionReq(@RequestBody Params.EDCWithSpecCheckActionReqParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.COLLECTED_DATA_ACTION_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        //abstract LotIDs for PrivilegeCheck
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        if (!CimArrayUtils.isEmpty(params.getStartCassetteList())) {
            params.getStartCassetteList().forEach(startCassette -> {
                if (CimArrayUtils.isEmpty(startCassette.getLotInCassetteList())) {
                    return;
                }
                startCassette.getLotInCassetteList().forEach(lotInCassette -> {
                    if (!CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                        return;
                    }
                    privilegeCheckParams.getLotIDLists().add(lotInCassette.getLotID());
                });
            });
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txEDCWithSpecCheckActionReq
        Infos.EDCWithSpecCheckActionReqInParm strEDCWithSpecCheckActionReqInParm = new Infos.EDCWithSpecCheckActionReqInParm();
        strEDCWithSpecCheckActionReqInParm.setEquipmentID(params.getEquipmentID());
        strEDCWithSpecCheckActionReqInParm.setControlJobID(params.getControlJobID());
        strEDCWithSpecCheckActionReqInParm.setStrStartCassette(params.getStartCassetteList());
        Results.EDCWithSpecCheckActionReqResult result = engineerDataCollectionService.sxEDCWithSpecCheckActionReq(objCommon, strEDCWithSpecCheckActionReqInParm, params.getClaimMemo());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/spec_check/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_SPEC_CHECK_REQ)
    public Response specCheckReq(@RequestBody Params.SpecCheckReqParams params) {
        String txId = TransactionIDEnum.DATA_SPEC_CHECK_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("userID can not be null"), txId);
        }
        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        //Step3 - txSpecCheckReq
        Results.SpecCheckReqResult result = engineerDataCollectionService.sxSpecCheckReq(objCommon, params.getEquipmentID(), params.getControlJobID(), params.getStartCassetteList());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_transit_data/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.TEMP_DATA_RPT)
    public Response edcTransitDataRpt(@RequestBody Params.EDCTransitDataRptParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.TEMP_DATA_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        //abstract LotIDs for PrivilegeCheck
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        List<Infos.StartCassette> startCassetteList = params.getStartCassetteList();
        if (!CimArrayUtils.isEmpty(startCassetteList)) {
            startCassetteList.forEach(startCassette -> {
                if (CimArrayUtils.isEmpty(startCassette.getLotInCassetteList())) {
                    return;
                }
                startCassette.getLotInCassetteList().forEach(lotInCassette -> {
                    if (!CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                        return;
                    }
                    privilegeCheckParams.getLotIDLists().add(lotInCassette.getLotID());
                });
            });
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txEDCTransitDataRpt
        ObjectIdentifier result = engineerDataCollectionService.sxEDCTransitDataRpt(objCommon, params.getEquipmentID(),
                params.getPortGroupID(), params.getControlJobID(), params.getStartCassetteList());

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/spc_check/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SPC_CHECK_REQ)
    public Response spcCheckReq(@RequestBody Params.SPCCheckReqParams spcCheckReqParams) {
        User user = spcCheckReqParams.getUser();
        Asserts.check(null != user, "the check request is null...");
        String txId = TransactionIDEnum.SPC_CHECK_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        /*-----------------------------------------------------------------------*/
        /*   abstract LotIDs for PrivilegeCheck                                  */
        /*-----------------------------------------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        int lenStartCassette = CimArrayUtils.getSize(spcCheckReqParams.getStrStartCassette());
        for (int i = 0; i < lenStartCassette; i++){
            List<Infos.LotInCassette> lotInCassetteList = spcCheckReqParams.getStrStartCassette().get(i).getLotInCassetteList();
            int lenLotInCassette = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lenLotInCassette; j++){
                if (CimBooleanUtils.isTrue(lotInCassetteList.get(j).getMoveInFlag())){
                    lotIDs.add(lotInCassetteList.get(j).getLotID());
                }
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setEquipmentID(spcCheckReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        Results.SPCCheckReqResult spcCheckReqResult = engineerDataCollectionService.sxSPCCheckReq(objCommon, spcCheckReqParams);
        return Response.createSucc(txId, spcCheckReqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/spc_do_action/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SPC_ACTION_EXECUTE_REQ)
    @Override
    public Response spcDoActionReq(@RequestBody Params.SPCDoActionReqParams spcDoActionReqParams) {
        User user = spcDoActionReqParams.getUser();
        Asserts.check(null != user, "the check request is null...");
        String txId = TransactionIDEnum.SPC_ACTION_EXECUTE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        /*-----------------------------------------------------------------------*/
        /*   LotIDs for PrivilegeCheck                                  */
        /*-----------------------------------------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<Infos.BankMove> bankMoveList = spcDoActionReqParams.getBankMoveList();
        List<Infos.ReworkBranch> reworkBranchList = spcDoActionReqParams.getReworkBranchList();
        Optional.ofNullable(bankMoveList).ifPresent(bankMoves -> {
            bankMoveList.forEach(bankMove -> {
                if (!ObjectIdentifier.isEmptyWithValue(bankMove.getLotID())){
                    lotIDs.add(bankMove.getLotID());
                }
            });
        });
        Optional.ofNullable(reworkBranchList).ifPresent(reworkBranches -> {
            reworkBranchList.forEach(reworkBranch -> {
                if (!ObjectIdentifier.isEmptyWithValue(reworkBranch.getLotID())){
                    lotIDs.add(reworkBranch.getLotID());
                }
            });
        });
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        engineerDataCollectionService.sxSPCDoActionReq(objCommon, bankMoveList, spcDoActionReqParams.getMailSendList(), reworkBranchList);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_with_spec_check_action_by_pj/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_ACTION_BY_PJ_REQ)
    public Response edcWithSpecCheckActionByPJReq(@RequestBody Params.EDCWithSpecCheckActionByPJReqParams edcWithSpecCheckActionByPJReqParams) {
        User user = edcWithSpecCheckActionByPJReqParams.getUser();
        String txID = TransactionIDEnum.COLLECTED_DATA_ACTION_BY_PJ_REQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(edcWithSpecCheckActionByPJReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setEquipmentID(edcWithSpecCheckActionByPJReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //step3 - txEDCWithSpecCheckActionByPJReq
        Results.EDCWithSpecCheckActionByPJReqResult result = engineerDataCollectionService.sxEDCWithSpecCheckActionByPJReq(objCommon, edcWithSpecCheckActionByPJReqParams);
        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_by_pj/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT)
    public Response edcByPJRpt(@RequestBody Params.EDCByPJRptInParms params) {
        //======Pre Process======
        String txId = TransactionIDEnum.COLLECTED_DATA_BY_PJ_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - calendar_GetCurrentTimeDR
        Infos.ObjCommon objCommon = utilsCompService.setObjCommon(txId, user);

        //Step2 - txAccessControlCheckInq
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(params.getEquipmentID());
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals("1", tmpPrivilegeCheckCJValue)) {
            List<ObjectIdentifier> lotIdListOut = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
            privilegeCheckParams.setLotIDLists(lotIdListOut);
        }

        accessInqService.sxAccessControlCheckInq(objCommon, privilegeCheckParams);

        //Step3 - txEDCByPJRpt
        Results.EDCByPJRptResult result = engineerDataCollectionService.sxEDCByPJRpt(objCommon, params);

        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult = new Results.PostTaskRegisterReqResult();

        log.info("txEDCByPJRpt :rc == RC_OK");

        if (0 < CimArrayUtils.getSize(result.getLotIDs())) {
            log.info("0 < retVal->lotIDSeq.length()");
            int nLotIDSeqLen = CimArrayUtils.getSize(result.getLotIDs());
            for (int i = 0; i < nLotIDSeqLen; i++) {
                log.info("Registration of Post-Processing for Lot,{},{}", i, result.getLotIDs().get(i).getValue());
            }

            Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            strPostProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
            strPostProcessRegistrationParm.setControlJobID(params.getControlJobID());
            strPostProcessRegistrationParm.setLotIDs(result.getLotIDs());
            log.info("Registration of Post-Processing={}", strPostProcessRegistrationParm.getLotIDs().get(0).getValue());
            //Post-Processing Registration for Lot
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setPatternID(null);
            postTaskRegisterReqParams.setKey(null);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
            postTaskRegisterReqParams.setClaimMemo("");
            strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

        }

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.info("txEDCByPJRpt and txPostTaskRegisterReq__100 :rc == RC_OK");

        //Post Process Execution for Lot
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(user);
        postTaskExecuteReqParams.setKey(strPostTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        log.info("TxPostTaskExecuteReq__100 :rc == RC_OK");
        return Response.createSucc(txId, result);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strDChubDataSendCompleteRptInParam
     * @return com.fa.cim.common.support.Response
     * @throws
     * @author Ho
     * @date 2019/8/6 17:04
     */
    @ResponseBody
    @RequestMapping(value = "/dchub_data_send_complete/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_SEND_COMPLETE_RPT)
    public Response dchubDataSendCompleteRpt(@RequestBody Params.DChubDataSendCompleteRptInParam strDChubDataSendCompleteRptInParam) {
        List<Infos.EventParameter> strEventParameter = null;
        Infos.ObjCommon strObjCommonIn;

        //Step1 - calendar_GetCurrentTimeDR

        String txID = TransactionIDEnum.DATA_SEND_COMPLETE_RPT.getValue();

        ThreadContextHolder.setTransactionId(txID);

        int nLen = CimArrayUtils.getSize(strEventParameter);
        strEventParameter = new ArrayList<>();
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen).setParameterName("EqpId");
        strEventParameter.get(nLen).setParameterValue(strDChubDataSendCompleteRptInParam.getEquipmentID().getValue());
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen).setParameterName("ControlJobId");
        strEventParameter.get(nLen).setParameterValue(strDChubDataSendCompleteRptInParam.getControlJobID().getValue());

        RetCode<Object> strAccessControlCheckInqResult;
        ObjectIdentifier dummy = null;

        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams params = new Params.AccessControlCheckInqParams();
        params.setUser(strDChubDataSendCompleteRptInParam.getUser());
        params.setEquipmentID(strDChubDataSendCompleteRptInParam.getEquipmentID());

        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(txID, params.getUser(), params);

        try {
            engineerDataCollectionService.sxDChubDataSendCompleteRpt(strObjCommonIn,
                    strDChubDataSendCompleteRptInParam);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfigEx.getNoResponseDcs(), ex.getCode())
                    || Validations.isEquals(retCodeConfigEx.getDcsServerBindFail(), ex.getCode())
                    || Validations.isEquals(retCodeConfig.getExtServiceNilObj(), ex.getCode())) {
                List<ObjectIdentifier> lotIDs = controlJobMethod.controlJobLotIDListGetDR(strObjCommonIn, strDChubDataSendCompleteRptInParam.getControlJobID());

                StringBuilder lotIDStr = new StringBuilder();
                int lotLen = CimArrayUtils.getSize(lotIDs);
                for (int lotCnt = 0; lotCnt < lotLen; lotCnt++) {
                    log.info("" + "lotCnt/lotLen" + lotCnt + lotLen);
                    if (lotCnt > 0) {
                        lotIDStr.append(", ");
                    }
                    lotIDStr.append(lotIDs.get(lotCnt).getValue());
                }

                String msg = "";
                msg += "";
                msg += "<<< DCS OperationCompleted Error >>>";
                msg += "";
                msg += "Eqpipment ID         : ";
                msg += (String) strDChubDataSendCompleteRptInParam.getEquipmentID().getValue();
                msg += "-----------------------------------------";
                msg += "Transaction ID       : ";
            /*msg+=strControlJobLotIDListGetDROut.getTransactionID() ;
            msg+="Return Code          : " ;
            msg+=strControlJobLotIDListGetDROut.getReturnCode() ;
            msg+="Message ID           : " ;
            msg+=strControlJobLotIDListGetDROut.getMessageID() ;
            msg+="Message Text         : " ;
            msg+=strControlJobLotIDListGetDROut.getMessageText() ;
            msg+="Reason Text          : " ;
            msg+=strControlJobLotIDListGetDROut.getReasonText() ;*/
                msg += "-----------------------------------------";
                msg += "Lot IDs              : ";
                msg += lotIDStr;

                Results.AlertMessageRptResult strAlertMessageRptResult;
                dummy = null;

                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(SP_SYSTEMMSGCODE_DCSIFERR);
                alertMessageRptParams.setSystemMessageText(msg);
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setEquipmentID(dummy);
                alertMessageRptParams.setEquipmentStatus("");
                alertMessageRptParams.setStockerID(dummy);
                alertMessageRptParams.setStockerStatus("");
                alertMessageRptParams.setAGVID(dummy);
                alertMessageRptParams.setAGVStatus("");
                alertMessageRptParams.setLotID(dummy);
                alertMessageRptParams.setLotStatus("");
                alertMessageRptParams.setRouteID(dummy);
                alertMessageRptParams.setOperationID(dummy);
                alertMessageRptParams.setOperationNumber("");
                alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp()));
                alertMessageRptParams.setClaimMemo("");

                systemService.sxAlertMessageRpt(strObjCommonIn, alertMessageRptParams);
            }
        }


        return Response.createSucc(txID, null);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strEDCDataUpdateForLotReqInParm
     * @return com.fa.cim.common.support.Response
     * @throws
     * @author Ho
     * @date 2019/8/20 13:14
     */
    @ResponseBody
    @RequestMapping(value = "/edc_data_update_for_lot/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DATA_UPDATE_FORLOT_REQ)
    public Response edcDataUpdateForLotReq(@RequestBody Params.EDCDataUpdateForLotReqInParm strEDCDataUpdateForLotReqInParm) {

        List<Infos.EventParameter> strEventParameter;
        Infos.ObjCommon strObjCommonIn;

        String txID = TransactionIDEnum.DATA_UPDATE_FORLOT_REQ.getValue();

        ThreadContextHolder.setTransactionId(txID);

        // Step1 - calendar_GetCurrentTimeDR
        strEventParameter = new ArrayList<>();
        int nLen = CimArrayUtils.getSize(strEventParameter);
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen).setParameterName("LotId");
        strEventParameter.get(nLen++).setParameterValue(strEDCDataUpdateForLotReqInParm.getLotID().getValue());
        strEventParameter.add(new Infos.EventParameter());
        strEventParameter.get(nLen).setParameterName("CtrljobId");
        strEventParameter.get(nLen++).setParameterValue(strEDCDataUpdateForLotReqInParm.getControlJobID().getValue());

        ObjectIdentifier dummyID = new ObjectIdentifier();
        dummyID.setValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>();


        List<ObjectIdentifier> lotIDs;
        lotIDs = new ArrayList<>();
        lotIDs.add(strEDCDataUpdateForLotReqInParm.getLotID());

        Params.AccessControlCheckInqParams strAccessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        strAccessControlCheckInqParams.setEquipmentID(dummyID);
        strAccessControlCheckInqParams.setStockerID(dummyID);
        strAccessControlCheckInqParams.setProductIDList(dummyIDs);
        strAccessControlCheckInqParams.setRouteIDList(dummyIDs);
        strAccessControlCheckInqParams.setLotIDLists(lotIDs);
        strAccessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);

        // step2 - txAccessControlCheckInq
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(txID,
                strEDCDataUpdateForLotReqInParm.getUser(),
                strAccessControlCheckInqParams);

        // step3 - txEDCDataUpdateForLotReq
        engineerDataCollectionService.sxEDCDataUpdateForLotReq(
                strObjCommonIn,
                strEDCDataUpdateForLotReqInParm,
                strEDCDataUpdateForLotReqInParm.getClaimMemo());

        return Response.createSucc(txID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/edc_with_spec_check_action_by_post_task/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EDCWITH_SPEC_CHECK_ACTION_BY_POST_TASK_REQ)
    public Response edcWithSpecCheckActionByPostTaskReq(Params.EDCWithSpecCheckActionByPostTaskReqParams params) {
        String txID = TransactionIDEnum.EDCWITH_SPEC_CHECK_ACTION_BY_POST_TASK_REQ.getValue();
        //step1 - set current tx id
        ThreadContextHolder.setTransactionId(txID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("userID can not be null"), txID);
        }
        //step2 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, user, accessControlCheckInqParams);

        //Step3 - sxEDCWithSpecCheckActionByPostTaskReq
        Results.CollectedDataActionByPostProcReqResult result = engineerDataCollectionService.sxEDCWithSpecCheckActionByPostTaskReq(objCommon,params);
        return Response.createSucc(txID, result);
    }
}
