package com.fa.cim.controller.post;

import com.fa.cim.common.constant.BizConstant;
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
import com.fa.cim.controller.interfaces.post.IPostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IPostProcessMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 9:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IPostController.class, confirmableKey = "PostConfirm", cancellableKey = "PostCancel")
@RequestMapping("/post")
@Listenable
public class PostController implements IPostController {
    @Autowired
    private IPostService postService;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private IDurableService durableService;

    @ResponseBody
    @RequestMapping(value = "/post_task_execute/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.POST_PROCESS_EXEC_REQ)
    public Response postTaskExecuteReq(@RequestBody Params.PostTaskExecuteReqParams postTaskExecuteReqParams) {
        // init params
        final String transactionID = TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Results.PostTaskExecuteReqResult retVal = new Results.PostTaskExecuteReqResult();

        // check input params
        log.info("【step1】get schedule from calendar");
        log.info("【step2】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, postTaskExecuteReqParams.getUser(), new Params.AccessControlCheckInqParams(true));

        log.info("【step3】call sxDOCLotActionReq");
        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
        //------------------------------------
        // txPostTaskExecuteReq__100
        //------------------------------------
        Results.PostTaskExecuteReqResult result = null;
        int seqNo = postTaskExecuteReqParams.getPreviousSequenceNumber();
        boolean bLUWFlag = false;
        boolean loopFlag = true;
        int errSeqNo = -1;
        List<String> relatedQueueKeys = new ArrayList<>();
        int nQKeyLen = 0;
        int rc_save = 0;
        String retValMessage = null;
        int retValCode = 0;
        while (loopFlag) {
            //------------------------------------------------------
            // Check commitFlag
            // When commitFlag is 1, TX_COMMIT is performed.
            //------------------------------------------------------
            if (bLUWFlag == false) {
                bLUWFlag = true;
            }
            postTaskExecuteReqParams.setKeyTimeStamp(null);
            postTaskExecuteReqParams.setClaimMemo("");
            try {
                result = postService.sxPostTaskExecuteReq(objCommon, postTaskExecuteReqParams);
                seqNo = result.getLastSequenceNumber();
                // DKey of a related lot is keeped.
                if (!CimStringUtils.isEmpty(result.getRelatedQueueKey())) {
                    retVal.setRelatedQueueKey(result.getRelatedQueueKey());
                }
                // SPC/SpecCheckResult is keeped.
                int nLen1 = CimArrayUtils.getSize(result.getStrLotSpecSPCCheckResultSeq());
                if (nLen1 > 0) {
                    List<Infos.OpeCompLot> strLotSpecSPCCheckResultSeq = retVal.getStrLotSpecSPCCheckResultSeq();
                    if (CimArrayUtils.isEmpty(strLotSpecSPCCheckResultSeq)) {
                        strLotSpecSPCCheckResultSeq = new ArrayList<>();
                        retVal.setStrLotSpecSPCCheckResultSeq(strLotSpecSPCCheckResultSeq);
                    }
                    strLotSpecSPCCheckResultSeq.addAll(result.getStrLotSpecSPCCheckResultSeq());
                }

                //-----------------------------------------------
                // Check whether any new D-Key is created by this action
                //-----------------------------------------------
                if (ppChainMode == 1) {
                    String strChainedFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG);
                    if (CimStringUtils.equals(strChainedFlag, "1")) {
                        List<String> relatedQueueKeysResult = null;
                        try {
                            relatedQueueKeysResult = postProcessMethod.postProcessRelatedQueueKeyGetDR(objCommon, postTaskExecuteReqParams.getKey());
                        } catch (ServiceException e) {
                            //UOW will be controlled by while-loop
                            //even if error happens in obj, this will not lead to data inconsistency
                        }
                        int nTmpKeyLen = CimArrayUtils.getSize(relatedQueueKeysResult);
                        for (int k = 0; k < nTmpKeyLen; k++) {
                            relatedQueueKeys.add(relatedQueueKeysResult.get(k));
                            nQKeyLen++;
                        }
                        //clear SP_ThreadSpecificData_PostProc_ChainedFlag
                        ThreadContextHolder.removeDataString(BizConstant.SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG);
                    }
                }

                //---------------------------------------------------------
                // To return RC_OK is that Post-Processing Execution end.
                //---------------------------------------------------------
                loopFlag = false;
                log.info("Last entry execution is completed.");
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getPostprocNextEntryWithCommit(), e.getCode())) {
                    //-----------------------------------------------------------------------------------
                    // The next execution needs to perform with TX_COMMIT. ( COMMIT_FLAG is TRUE.)
                    //-----------------------------------------------------------------------------------
                    bLUWFlag = false;
                    log.info("continue to execute the next one. COMMIT_FLAG is FALSE.");
                } else if (Validations.isEquals(retCodeConfig.getPostprocNextEntryWithoutCommit(), e.getCode())) {
                    //-----------------------------------------------------------------------------------
                    // The next execution doesn't need to perform with TX_COMMIT. (COMMIT_FLAG is FALSE.)
                    //-----------------------------------------------------------------------------------
                    log.info("continue to execute the next one. COMMIT_FLAG is FALSE.");
                } else if (Validations.isEquals(retCodeConfig.getExtpostprocExecuted(), e.getCode())) {
                    //-----------------------------------------------------------------------------------
                    // Post-processing is suspended with TX_COMMIT for the external post-processing.
                    //-----------------------------------------------------------------------------------
                    bLUWFlag = false;
                    log.info("Post-processing is suspended for the external post-processing.");
                } else if (Validations.isEquals(retCodeConfig.getInterfabLotxferExecuted(), e.getCode())) {
                    //-----------------------------------------------------------------------------------
                    // Post-processing is suspended with TX_COMMIT for InterFab Transfer.
                    //-----------------------------------------------------------------------------------
                    bLUWFlag = false;
                    loopFlag = false;
                    log.info("Post-processing is suspended for the InterFab Transfer.");
                } else if (Validations.isEquals(retCodeConfig.getPostprocError(), e.getCode())) {
                    //---------------------------------------------------------------------------
                    // Error.
                    // errorAction is performed according to errorAction of the queue.
                    // - SP_PostProcess_ErrAction_None        : No action. The queue remains.
                    // - SP_PostProcess_ErrAction_RemoveQueue : The queue is deleted.
                    //---------------------------------------------------------------------------
                    bLUWFlag = false;
                    loopFlag = false;
                    Results.PostTaskExecuteReqResult tmpPostTaskExecuteReqResult = e.getData(Results.PostTaskExecuteReqResult.class);
                    List<Infos.PostProcessActionInfo> postProcessActionInfoList = tmpPostTaskExecuteReqResult.getPostProcessActionInfoList();
                    if (!CimArrayUtils.isEmpty(postProcessActionInfoList)) {
                        switch (postProcessActionInfoList.get(0).getErrorAction()) {
                            case BizConstant.SP_POSTPROCESS_ERRACTION_NONE:
                                log.info("Error Action is None. Post Process Watchdog will call this transaction for the queue.");
                                if (errSeqNo == -1) {
                                    errSeqNo = seqNo;
                                    retValMessage = e.getMessage();
                                    retValCode = e.getCode();
                                }
                                break;
                            case BizConstant.SP_POSTPROCESS_ERRACTION_REMOVEQUEUE:
                                log.info("Error Action is deletion of the queue.");
                                List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = new ArrayList<>();
                                Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                                strPostProcessActionInfoSeq.add(postProcessActionInfo);
                                postProcessActionInfo.setDKey(postTaskExecuteReqParams.getKey());
                                postProcessActionInfo.setSequenceNumber(seqNo);
                                List<Infos.PostProcessAdditionalInfo> dummySeq = new ArrayList<>();
                                try {
                                    List<Infos.PostProcessActionInfo> postProcessActionUpdateInfoList = postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq, dummySeq, "");
                                } catch (ServiceException ex) {
                                    continue;
                                }
                                // Keep only first error seqNo.
                                if (errSeqNo == -1) {
                                    errSeqNo = seqNo;
                                    retValCode = e.getCode();
                                    retValMessage = e.getMessage();
                                }
                                break;
                            default:
                                //-------------------------------------------------------------
                                // Other Error
                                //-------------------------------------------------------------
                                if (errSeqNo == -1) {
                                    errSeqNo = seqNo;
                                    retValMessage = e.getMessage();
                                    retValCode = e.getCode();
                                }
                                break;
                        }
                    }
                } else {
                    //-------------------------------------------------------------------------------------------
                    // OTHER Error ( RC_POSTPROC_INVALID_LOT_PROCSTAT, RC_POSTPROC_UNKNOWN_PROC_ID     ...etc
                    //-------------------------------------------------------------------------------------------
                    loopFlag = false;
                    if (errSeqNo == -1) {
                        errSeqNo = seqNo;
                        retValMessage = e.getMessage();
                        retValCode = e.getCode();
                    }
                    if (Validations.isEquals(retCodeConfig.getPostprocDeleteByOther(), e.getCode())) {
                        //save the rc for TxPostEndForExtRpt check
                        log.info("rc == RC_POSTPROC_DELETE_BY_OTHER, save it");
                        rc_save = e.getCode();
                    }
                }
            }
        }
        retVal.setLastSequenceNumber(seqNo);
        //------------------------------
        // Execute followup keys
        //------------------------------
        String errKey = postTaskExecuteReqParams.getKey();
        int idx = 0;
        while (idx < nQKeyLen) {
            loopFlag = true;
            bLUWFlag = false;
            seqNo = -1;
            if (errSeqNo == -1) {
                //error is not raised yet, keep the current dkey as errKey
                errKey = relatedQueueKeys.get(idx);
            }
            while (loopFlag) {
                //------------------------------------------------------
                // Check commitFlag
                // When commitFlag is 1, TX_COMMIT is performed.
                //------------------------------------------------------
                if (!bLUWFlag) {
                    bLUWFlag = true;
                }
                Params.PostTaskExecuteReqParams postTaskExecuteReqParams2 = new Params.PostTaskExecuteReqParams();
                postTaskExecuteReqParams2.setKey(relatedQueueKeys.get(idx));
                postTaskExecuteReqParams2.setSyncFlag(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                postTaskExecuteReqParams2.setPreviousSequenceNumber(seqNo);
                Results.PostTaskExecuteReqResult postTaskExecuteReqResult = null;
                try {
                    postTaskExecuteReqResult = postService.sxPostTaskExecuteReq(objCommon, postTaskExecuteReqParams2);
                    //-----------------------------------------------
                    // Keep executed LotID, seqNo of the queue.
                    //-----------------------------------------------
                    seqNo = postTaskExecuteReqResult.getLastSequenceNumber();
                    //-----------------------------------------------
                    // Check whether any new D-Key is created by this action
                    //-----------------------------------------------
                    String strChainedFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG);
                    if (CimStringUtils.equals(strChainedFlag, "1")) {
                        List<String> relatedQueueKeysResult2 = null;
                        try {
                            relatedQueueKeysResult2 = postProcessMethod.postProcessRelatedQueueKeyGetDR(objCommon, relatedQueueKeys.get(idx));
                        } catch (ServiceException e) {

                        }
                        int nTmpKeyLen = CimArrayUtils.getSize(relatedQueueKeysResult2);
                        for (int k = 0; k < nTmpKeyLen; k++) {
                            relatedQueueKeys.add(relatedQueueKeysResult2.get(k));
                            nQKeyLen++;
                        }
                        //clear SP_ThreadSpecificData_PostProc_ChainedFlag
                        ThreadContextHolder.removeDataString(BizConstant.SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG);
                    }
                    //---------------------------------------------------------
                    // To return RC_OK is that Post-Processing Execution end.
                    //---------------------------------------------------------
                    loopFlag = false;
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getPostprocNextEntryWithCommit(), e.getCode())) {
                        //-----------------------------------------------------------------------------------
                        // The next execution needs to perform with TX_COMMIT. ( COMMIT_FLAG is TRUE.)
                        //-----------------------------------------------------------------------------------
                        bLUWFlag = false;
                    } else if (Validations.isEquals(retCodeConfig.getPostprocNextEntryWithoutCommit(), e.getCode())) {
                        //-----------------------------------------------------------------------------------
                        // The next execution doesn't need to perform with TX_COMMIT. (COMMIT_FLAG is FALSE.)
                        //-----------------------------------------------------------------------------------
                        // continue to execute the next one...
                    } else if (Validations.isEquals(retCodeConfig.getExtpostprocExecuted(), e.getCode())) {
                        bLUWFlag = false;
                        loopFlag = false;
                    } else if (Validations.isEquals(retCodeConfig.getInterfabLotxferExecuted(), e.getCode())) {
                        //-----------------------------------------------------------------------------------
                        // Post-processing is suspended with TX_COMMIT for InterFab Transfer.
                        //-----------------------------------------------------------------------------------
                        bLUWFlag = false;
                        loopFlag = false;
                    } else if (Validations.isEquals(retCodeConfig.getPostprocError(), e.getCode())) {
                        //---------------------------------------------------------------------------
                        // Error.
                        // errorAction is performed according to errorAction of the queue.
                        // - SP_PostProcess_ErrAction_None        : No action. The queue remains.
                        // - SP_PostProcess_ErrAction_RemoveQueue : The queue is deleted.
                        //---------------------------------------------------------------------------
                        bLUWFlag = false;
                        loopFlag = false;
                        Results.PostTaskExecuteReqResult tmpPostTaskExecuteReqResult = e.getData(Results.PostTaskExecuteReqResult.class);
                        List<Infos.PostProcessActionInfo> postProcessActionInfoList = tmpPostTaskExecuteReqResult.getPostProcessActionInfoList();
                        if (!CimArrayUtils.isEmpty(postProcessActionInfoList)) {
                            switch (postProcessActionInfoList.get(0).getErrorAction()) {
                                case BizConstant.SP_POSTPROCESS_ERRACTION_NONE:
                                    log.info("Error Action is None. Post Process Watchdog will call this transaction for the queue.");
                                    if (errSeqNo == -1) {
                                        errSeqNo = seqNo;
                                        retValMessage = e.getMessage();
                                        retValCode = e.getCode();
                                    }
                                    break;
                                case BizConstant.SP_POSTPROCESS_ERRACTION_REMOVEQUEUE:
                                    log.info("Error Action is deletion of the queue.");
                                    List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = new ArrayList<>();
                                    Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                                    strPostProcessActionInfoSeq.add(postProcessActionInfo);
                                    postProcessActionInfo.setDKey(relatedQueueKeys.get(idx));
                                    postProcessActionInfo.setSequenceNumber(seqNo);
                                    List<Infos.PostProcessAdditionalInfo> dummySeq = new ArrayList<>();
                                    try {
                                        List<Infos.PostProcessActionInfo> postProcessActionUpdateInfoList = postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq, dummySeq, "");
                                    } catch (ServiceException ex) {
                                        continue;
                                    }
                                    // Keep only first error seqNo.
                                    if (errSeqNo == -1) {
                                        errSeqNo = seqNo;
                                        retValMessage = e.getMessage();
                                        retValCode = e.getCode();
                                    }
                                    break;
                                default:
                                    //-------------------------------------------------------------
                                    // Other Error
                                    //-------------------------------------------------------------
                                    if (errSeqNo == -1) {
                                        errSeqNo = seqNo;
                                        retValMessage = e.getMessage();
                                        retValCode = e.getCode();
                                    }
                                    break;
                            }
                        }
                    } else {
                        //-------------------------------------------------------------------------------------------
                        // Other Error ( RC_POSTPROC_INVALID_LOT_PROCSTAT, RC_POSTPROC_UNKNOWN_PROC_ID     ...etc
                        //-------------------------------------------------------------------------------------------
                        loopFlag = false;
                        if (errSeqNo == -1) {
                            errSeqNo = seqNo;
                            retValMessage = e.getMessage();
                            retValCode = e.getCode();
                        }
                    }
                }
            }
            idx++;
        }
        //-------------------------------------------------------------
        // Check Error.
        //-------------------------------------------------------------
        OmCode retOmCode = retCodeConfig.getSucc();
        if (errSeqNo != -1) {
            //Error.
            log.info("The d_key is {}", errKey);
            log.info("The seq_no is {}", errSeqNo);
            String messageText = retValMessage;
            String returnCode = String.valueOf(retValCode);
            String errSeqNo_var = String.valueOf(errSeqNo);
            if (Validations.isEquals(rc_save, retCodeConfig.getPostprocDeleteByOther()) && CimStringUtils.equals(postTaskExecuteReqParams.getUser().getFunctionID(), "OPOSR001")) {
                retOmCode = new OmCode(retCodeConfig.getPostprocDeleteByOther(), errKey, errSeqNo_var);
            }
            //------------------------------------------------
            // Error information of the post process is saved
            //------------------------------------------------
            List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = new ArrayList<>();
            Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
            strPostProcessActionInfoSeq.add(postProcessActionInfo);
            postProcessActionInfo.setDKey(errKey);
            postProcessActionInfo.setSequenceNumber(errSeqNo);
            List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
            Infos.PostProcessAdditionalInfo postProcessAdditionalInfo1 = new Infos.PostProcessAdditionalInfo();
            strPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo1);
            postProcessAdditionalInfo1.setDKey(errKey);
            postProcessAdditionalInfo1.setSequenceNumber(errSeqNo);
            postProcessAdditionalInfo1.setName(BizConstant.SP_POSTPROCESSADDITIONALINFO_ERRORCODE);
            postProcessAdditionalInfo1.setValue(returnCode);
            Infos.PostProcessAdditionalInfo postProcessAdditionalInfo2 = new Infos.PostProcessAdditionalInfo();
            strPostProcessAdditionalInfoSeq.add(postProcessAdditionalInfo2);
            postProcessAdditionalInfo2.setDKey(errKey);
            postProcessAdditionalInfo2.setSequenceNumber(errSeqNo);
            postProcessAdditionalInfo2.setName(BizConstant.SP_POSTPROCESSADDITIONALINFO_ERRORMSG);
            postProcessAdditionalInfo2.setValue(messageText);
            List<Infos.PostProcessActionInfo> postProcessActionUpdateInfoList = postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDUPDATEADDITIONALINFO,
                    strPostProcessActionInfoSeq, strPostProcessAdditionalInfoSeq, "");
        }
        return Response.createSuccWithOmCode(transactionID, retOmCode, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/post_task_register/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ)
    public Response postTaskRegisterReq(@RequestBody Params.PostTaskRegisterReqParams postTaskRegisterReqParams) {
        final TransactionIDEnum transactionId = TransactionIDEnum.POST_PROCESS_ACTION_REGIST_REQ;
        // step0 - check input params

        //step1 - done: get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), postTaskRegisterReqParams.getUser(), new Params.AccessControlCheckInqParams(true));

        //step3 - done: call PostTaskRegisterReq
        Results.PostTaskRegisterReqResult result = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(transactionId.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/post_action_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.POST_PROCESS_ACTION_UPDATE)
    public Response postActionModifyReq(@RequestBody Params.PostActionModifyReqParams postActionModifyReqParams) {
        String actionCode = postActionModifyReqParams.getActionCode();
        final TransactionIDEnum transactionId = TransactionIDEnum.POST_PROCESS_ACTION_UPDATE;
        ThreadContextHolder.setTransactionId(transactionId.getValue());
        //step1 - call txAccessControlCheckInq(...)
        List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = postActionModifyReqParams.getStrPostProcessActionInfoSeq();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        int postProcNum = CimArrayUtils.getSize(strPostProcessActionInfoSeq);
        if (postProcNum > 0) {
            for (int i = 0; i < postProcNum; i++) {
                lotIDs.add(strPostProcessActionInfoSeq.get(i).getPostProcessTargetObject().getLotID());
            }
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), postActionModifyReqParams.getUser(), accessControlCheckInqParams);

        //step2 - done: call PostTaskRegisterReq
        List<Infos.PostProcessActionInfo> postProcessActionInfoList = postService.sxPostActionModifyReq(objCommon, actionCode, strPostProcessActionInfoSeq, postActionModifyReqParams.getStrPostProcessAdditionalInfoSeq(), postActionModifyReqParams.getClaimMemo());
        if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE)
                || CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT)) {
            if (!CimArrayUtils.isEmpty(strPostProcessActionInfoSeq)) {
                postProcessActionInfoList = postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEADDITIONALINFO, strPostProcessActionInfoSeq, postActionModifyReqParams.getStrPostProcessAdditionalInfoSeq(), postActionModifyReqParams.getClaimMemo());
            }
        }
        return Response.createSucc(transactionId.getValue(), postProcessActionInfoList);
    }

    @ResponseBody
    @RequestMapping(value = "/post_filter_create_for_ext/req", method = RequestMethod.POST)
    @Override
    public Response postFilterCreateForExtReq(@RequestBody Params.PostFilterCreateForExtReqParams params) {
        final TransactionIDEnum transactionId = TransactionIDEnum.EXTERNAL_POST_PROCESS_FILTER_REGIST_REQ;
        ThreadContextHolder.setTransactionId(transactionId);

        Infos.PostFilterCreateForExtReqInParm postFilterCreateForExtReqInParm = params.getPostFilterCreateForExtReqInParm();
        ObjectIdentifier productSpec = CimStringUtils.equals(BizConstant.SP_POSTPROCESS_OBJECTTYPE_PRODUCTSPEC, postFilterCreateForExtReqInParm.getObjectType()) ? postFilterCreateForExtReqInParm.getObjectID() : null;

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setProductIDList(Arrays.asList(productSpec));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), params.getUser(), accessControlCheckInqParams);

        //step3 - done: call PostTaskRegisterReq
        postService.sxPostFilterCreateForExtReq(objCommon, postFilterCreateForExtReqInParm, params.getClaimMemo());

        return Response.createSucc(transactionId.getValue());
    }

    @ResponseBody
    @RequestMapping(value = "/post_filter_remove_for_ext/req", method = RequestMethod.POST)
    @Override
    public Response postFilterRemoveForExtReq(@RequestBody Params.PostFilterRemoveForExtReqParams params) {
        final TransactionIDEnum transactionId = TransactionIDEnum.EXTERNAL_POST_PROCESS_FILTER_REGIST_REQ;
        ThreadContextHolder.setTransactionId(transactionId);

        //step2 - call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), params.getUser(), new Params.AccessControlCheckInqParams(true));

        //step3 - done: call PostTaskRegisterReq
        postService.sxPostFilterRemoveForExtReq(objCommon, params.getExternalPostProcessFilterInfos(), params.getClaimMemo());

        return Response.createSucc(transactionId.getValue());
    }

    /**
     * Call postProcess for SubRouteBranchReq, BranchReq, branchWithHoldReleaseReq, waferLotStartReq, skipReq
     *
     * @param objCommon
     * @param postTaskRegisterReqParams
     * @author Nyx
     */
    public void execPostProcess(Infos.ObjCommon objCommon, Params.PostTaskRegisterReqParams postTaskRegisterReqParams) {
        //-----------------------------
        // Resister post action.
        //-----------------------------
        log.info("Resister post action.");
        Results.PostTaskRegisterReqResult sxPostTaskRegisterReq = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);


        //-------------------------------------
        // Post-Processing Execution Section
        //-------------------------------------
        log.info("Post-Processing Execution Section");
        this.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(), sxPostTaskRegisterReq.getDKey()
                , 1, 0, null, ""));
    }


    @ResponseBody
    @RequestMapping(value = "/post_force_remove_for_carrier_lot/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.POST_PROCESS_FORCE_DELETE_REQ)
    public Response lotCassettePostProcessForceDeleteReq(@RequestBody Params.StrLotCassettePostProcessForceDeleteReqInParams params) {
        final TransactionIDEnum transactionId = TransactionIDEnum.POST_PROCESS_FORCE_DELETE_REQ;
        ThreadContextHolder.setTransactionId(transactionId);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), params.getUser(), accessControlCheckInqParams);

        //step3 - done: call PostTaskRegisterReq
        durableService.sxLotCassettePostProcessForceDeleteReq(objCommon, params);

        return Response.createSucc(transactionId.getValue());
    }

}