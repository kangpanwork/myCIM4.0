package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimFimmTransDO;
import com.fa.cim.entity.nonruntime.postprocess.CimPostProcessDO;
import com.fa.cim.entity.nonruntime.postprocess.CimPostProcessInfoDO;
import com.fa.cim.entity.nonruntime.postprocess.CimPostProcessPatternDO;
import com.fa.cim.entity.nonruntime.postprocess.postProcessFilter.CimExternalPostProcessProductFilterDO;
import com.fa.cim.entity.nonruntime.postprocess.postProcessFilter.CimPostProcessFilterDO;
import com.fa.cim.entity.nonruntime.postprocess.postProcessFilter.CimPostProcessFilterValueDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPostProcessMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.Durable;
import com.rits.cloning.Cloner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/29       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2019/4/29 17:43
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class PostProcessMethod implements IPostProcessMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private Cloner cloner;

    @Autowired
    private IDurableMethod durableMethod;

    @Override
    public List<Infos.PostProcessActionInfo> postProcessQueueGetDR(Infos.ObjCommon objCommon, String key, Long prevSeqNo, List<Long> seqNoList, String status, Integer syncFlag, String targetType, ObjectIdentifier objectID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        List<Infos.PostProcessActionInfo> actionInfoSequence = new ArrayList<>();
        log.info("{} key", key);

        if (seqNoList.size() > 0) {
            //-----------------------------------------------------------------------------------
            //  Get Entry
            //   If seqNoList exists, a record is aquired using the specified seqNos one by one.
            //-----------------------------------------------------------------------------------
            log.info("Get Entry");
            for (Long seqNo : seqNoList) {
                CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                cimPostProcessExam.setDkey(key);
                cimPostProcessExam.setSeqNo(CimNumberUtils.intValue(seqNo));
                List<CimPostProcessDO> postProcesss = cimJpaRepository.findAll(Example.of(cimPostProcessExam));
                for (CimPostProcessDO processs : postProcesss) {
                    actionInfoSequence.add(setPostProcessActionInfo(processs));
                }
            }
            Validations.check(CimObjectUtils.isEmpty(actionInfoSequence), retCodeConfig.getNotFoundEntry());
        } else {
            CimPostProcessDO postProcess = null;
            if (!CimObjectUtils.isEmpty(targetType) && !ObjectIdentifier.isEmpty(objectID) && !ObjectIdentifier.equalsWithValue(objectID, "")) {
                log.info("Get Entry 2");
                if (CimStringUtils.equals(targetType, BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)) {
                    String postProcessLotID = ObjectIdentifier.fetchValue(objectID);
                    CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                    cimPostProcessExam.setDkey(key);
                    cimPostProcessExam.setTargetType(BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT);
                    cimPostProcessExam.setLotID(postProcessLotID);
                    List<CimPostProcessDO> postProcessList = cimJpaRepository.findAll(Example.of(cimPostProcessExam)).stream()
                            .sorted().collect(Collectors.toList());
                    if (!CimArrayUtils.isEmpty(postProcessList)) {
                        postProcess = postProcessList.get(0);
                    }
                } else if (CimStringUtils.equals(targetType, BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)) {
                    String postProcessCastID = ObjectIdentifier.fetchValue(objectID);
                    CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                    cimPostProcessExam.setDkey(key);
                    cimPostProcessExam.setTargetType(BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST);
                    cimPostProcessExam.setCassetteID(postProcessCastID);
                    List<CimPostProcessDO> postProcessList = cimJpaRepository.findAll(Example.of(cimPostProcessExam)).stream()
                            .sorted().collect(Collectors.toList());
                    if (!CimArrayUtils.isEmpty(postProcessList)) {
                        postProcess = postProcessList.get(0);
                    }
                } else {
                    //Currently other target type is not supported.
                    Validations.check(true, retCodeConfig.getInvalidParameter());
                }
            } else {
                //-----------------------------------------------------------------------------------
                //  Get Entry
                //   If seqNoList doesn't exist, one record is aquired using the specified prevSeqNo.
                //-----------------------------------------------------------------------------------
                log.info("Get Next Entry");
                log.info("{} hPREV_SEQ_NO", prevSeqNo);

                //-------------------------------------------------
                // Status expresses the condition of Queue.
                // Waiting, Reserved .... etc.
                //-------------------------------------------------
                if (CimObjectUtils.isEmpty(status)) {
                    //If "syncFlag" value is "-1", the record  is searched without sync_flag. ( minus value -> not use )
                    if (syncFlag < 0) {
                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query("SELECT * FROM OQPPRC WHERE LINK_KEY = ?1 AND IDX_NO > ?2 ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else if (BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL == syncFlag || BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL == syncFlag) {
                        // Synchronous execution item
                        log.info("Synchronous execution");
                        String postProcessSyncFlag1 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                        String postProcessSyncFlag2 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL);

                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query("SELECT * FROM OQPPRC WHERE LINK_KEY = ?1 AND IDX_NO > ?2 AND (SYNC_FLAG = ?3 OR SYNC_FLAG = ?4) ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo, postProcessSyncFlag1, postProcessSyncFlag2);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else if (BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL == syncFlag || BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL == syncFlag) {
                        // Synchronous execution item
                        log.info("Synchronous execution");
                        String postProcessSyncFlag1 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL);
                        String postProcessSyncFlag2 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL);
                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query("SELECT * FROM OQPPRC WHERE LINK_KEY = ?1 AND IDX_NO > ?2 AND (SYNC_FLAG = ?3 OR SYNC_FLAG = ?4) ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo, postProcessSyncFlag1, postProcessSyncFlag2);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else {
                        //Invalid syncFlag is specified
                        Validations.check(true, retCodeConfig.getInvalidParameter());
                    }
                } else {
                    String hFQPOSTPROC_STATUS = status;
                    log.info("{} hFQPOSTPROC_STATUS", hFQPOSTPROC_STATUS);

                    //If "syncFlag" value is "-1", the record  is searched without sync_flag. ( minus value -> not use )
                    if (syncFlag < 0) {
                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query(
                                "SELECT * FROM OQPPRC " +
                                          "WHERE  LINK_KEY = ?1 " +
                                          "   AND IDX_NO > ?2 " +
                                          "   AND PPRC_STATUS = ?3 " +
                                          "ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo, hFQPOSTPROC_STATUS);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else if (BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL == syncFlag || BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL == syncFlag) {
                        // Synchronous execution item
                        log.info("Synchronous execution");
                        String postProcessSyncFlag1 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL);
                        String postProcessSyncFlag2 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL);

                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query("SELECT * FROM OQPPRC WHERE LINK_KEY = ?1 AND IDX_NO > ?2 AND PPRC_STATUS = ?3 AND (SYNC_FLAG = ?4 OR SYNC_FLAG = ?5) ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo, hFQPOSTPROC_STATUS, postProcessSyncFlag1, postProcessSyncFlag2);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else if (BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL == syncFlag || BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL == syncFlag) {
                        // Synchronous execution item
                        log.info("Synchronous execution");
                        String postProcessSyncFlag1 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL);
                        String postProcessSyncFlag2 = String.valueOf(BizConstant.SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL);

                        List<CimPostProcessDO> postProcessList = cimJpaRepository.query("SELECT * FROM OQPPRC WHERE LINK_KEY = ?1 AND IDX_NO > ?2 AND PPRC_STATUS = ?3 AND (SYNC_FLAG = ?4 OR SYNC_FLAG = ?5) ORDER BY IDX_NO", CimPostProcessDO.class, key, prevSeqNo, hFQPOSTPROC_STATUS, postProcessSyncFlag1, postProcessSyncFlag2);
                        if (!CimObjectUtils.isEmpty(postProcessList)) {
                            postProcess = postProcessList.get(0);
                        }
                    } else {
                        //Invalid syncFlag is specified
                        Validations.check(true, retCodeConfig.getInvalidParameter());
                    }
                }
            }
            Validations.check(CimObjectUtils.isEmpty(postProcess), retCodeConfig.getNotFoundEntry());
            if (!CimObjectUtils.isEmpty(postProcess)) {
                actionInfoSequence.add(setPostProcessActionInfo(postProcess));
            }
        }

        for (Infos.PostProcessActionInfo postProcessActionInfo : actionInfoSequence) {
            //--------------------------------------------------------------------------
            // Make a execCondition string. Parse execCondition string
            // For example,
            // A string is "2+3".   ==> execConditionSeq[0]  <-- 2
            //                      ==> execConditionSeq[1]  <-- 3
            //--------------------------------------------------------------------------
            List<Long> execConditionList = new ArrayList<>();
            String buf = postProcessActionInfo.getExecCondition();
            if (!CimObjectUtils.isEmpty(buf)) {
                String[] bufs = buf.split("\\+");
                for (String s : bufs) {
                    execConditionList.add(Long.valueOf(s));
                }
            }
            postProcessActionInfo.setExecConditionList(execConditionList);
        }

        return actionInfoSequence;
    }

    @Override
    public void postProcessAdditionalInfoDeleteDR(Infos.ObjCommon objCommon, String dKey) {
        // InParameter Trace
        log.info("{} # dKey ", dKey);

        /******************/
        /*   Initialise   */
        /******************/
        //----- Check parent OQPPRC record -----//
        CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
        cimPostProcessExam.setDkey(dKey);
        List<CimPostProcessDO> postProcessList = cimJpaRepository.findAll(Example.of(cimPostProcessExam));
        Validations.check(!CimObjectUtils.isEmpty(postProcessList), retCodeConfig.getPostprocQueueExist());

        log.info("Start Info Deleting...");
        //----- Delete OQPPRC_INFO -----//
        CimPostProcessInfoDO cimPostProcessInfoExam = new CimPostProcessInfoDO();
        cimPostProcessInfoExam.setDkey(dKey);
        List<CimPostProcessInfoDO> cimPostProcessInfoDOList = cimJpaRepository.findAll(Example.of(cimPostProcessInfoExam));
        if (!CimArrayUtils.isEmpty(cimPostProcessInfoDOList)) {
            for (CimPostProcessInfoDO cimPostProcessInfoDO : cimPostProcessInfoDOList) {
                cimJpaRepository.delete(cimPostProcessInfoDO);
            }
        }
    }

    @Override
    public void postProcessAdditionalInfoInsertDR(Infos.ObjCommon objCommon, List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList) {
        for (int i = 0; i < postProcessAdditionalInfoList.size(); i++) {
            Infos.PostProcessAdditionalInfo postProcessAdditionalInfo = postProcessAdditionalInfoList.get(i);
            CimPostProcessInfoDO cimPostProcessInfoDO = new CimPostProcessInfoDO();
            cimPostProcessInfoDO.setDkey(postProcessAdditionalInfo.getDKey());
            cimPostProcessInfoDO.setSequenceNumber(postProcessAdditionalInfo.getSequenceNumber());
            cimPostProcessInfoDO.setName(postProcessAdditionalInfo.getName());
            cimPostProcessInfoDO.setValue(postProcessAdditionalInfo.getValue());
            cimJpaRepository.save(cimPostProcessInfoDO);
        }
    }

    @Override
    public void postProcessAdditionalInfoInsertUpdateDR(Infos.ObjCommon objCommon, List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList) {
        for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfoList) {
            CimPostProcessInfoDO cimPostProcessInfoExam = new CimPostProcessInfoDO();
            cimPostProcessInfoExam.setDkey(postProcessAdditionalInfo.getDKey());
            cimPostProcessInfoExam.setSequenceNumber(postProcessAdditionalInfo.getSequenceNumber());
            cimPostProcessInfoExam.setName(postProcessAdditionalInfo.getName());
            List<CimPostProcessInfoDO> cimPostProcessInfoDOList = cimJpaRepository.findAll(Example.of(cimPostProcessInfoExam));
            //--------------------------------
            //   Update OQPPRC_INFO table
            //--------------------------------
            if (!CimArrayUtils.isEmpty(cimPostProcessInfoDOList)) {
                for (CimPostProcessInfoDO cimPostProcessInfoDO : cimPostProcessInfoDOList) {
                    cimPostProcessInfoDO.setValue(postProcessAdditionalInfo.getValue());
                    cimJpaRepository.save(cimPostProcessInfoDO);
                }
            } else {
                CimPostProcessInfoDO cimPostProcessInfoDO = new CimPostProcessInfoDO();
                cimPostProcessInfoDO.setDkey(postProcessAdditionalInfo.getDKey());
                cimPostProcessInfoDO.setSequenceNumber(postProcessAdditionalInfo.getSequenceNumber());
                cimPostProcessInfoDO.setName(postProcessAdditionalInfo.getName());
                cimPostProcessInfoDO.setValue(postProcessAdditionalInfo.getValue());
                cimJpaRepository.save(cimPostProcessInfoDO);
            }
        }
    }

    @Override
    public List<Infos.PostProcessActionInfo> postProcessQueueUpdateDR(Infos.ObjCommon objCommon, String actionCode, List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq) {
        if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADD)) {
            //-------------------------
            // Add Queue
            //-------------------------
            String dKey = null;
            if (!CimObjectUtils.isEmpty(strPostProcessActionInfoSeq)) {
                for (Infos.PostProcessActionInfo actionInfo : strPostProcessActionInfoSeq) {
                    //---------------------------
                    // Preparing Host variable
                    //---------------------------
                    // execCondition
                    String execCondition = actionInfo.getExecCondition();
                    if (CimObjectUtils.isEmpty(execCondition)) {
                        List<Long> execConditionList = actionInfo.getExecConditionList();
                        StringBuffer sb = new StringBuffer();
                        for (int i1 = 0; i1 < execConditionList.size(); i1++) {
                            sb.append(execConditionList.get(i1));
                            if (i1 < execConditionList.size() - 1) {
                                sb.append("+");
                            }
                        }
                        execCondition = sb.toString();
                    }
                    dKey = actionInfo.getDKey();
                    CimPostProcessDO postProcess = new CimPostProcessDO();
                    postProcess.setDkey(actionInfo.getDKey());
                    postProcess.setSeqNo(actionInfo.getSequenceNumber());
                    postProcess.setExecCondition(execCondition);
                    postProcess.setWatchDogName(actionInfo.getWatchDogName());
                    postProcess.setPostProcessID(actionInfo.getPostProcessID());
                    postProcess.setSyncFlag(actionInfo.getSyncFlag());
                    postProcess.setTransactionID(actionInfo.getTransationID());
                    postProcess.setTargetType(actionInfo.getTargetType());
                    Infos.PostProcessTargetObject postProcessTargetObject = actionInfo.getPostProcessTargetObject();
                    ObjectIdentifier lotID = postProcessTargetObject.getLotID();
                    if (!ObjectIdentifier.isEmpty(lotID)) {
                        postProcess.setLotID(lotID.getValue());
                        postProcess.setLotObj(lotID.getReferenceKey());
                    }
                    ObjectIdentifier equipmentID = postProcessTargetObject.getEquipmentID();
                    if (!ObjectIdentifier.isEmpty(equipmentID)) {
                        postProcess.setEquipmentID(equipmentID.getValue());
                        postProcess.setEquipmentObj(equipmentID.getReferenceKey());
                    }
                    ObjectIdentifier controlJobID = postProcessTargetObject.getControlJobID();
                    if (!ObjectIdentifier.isEmpty(controlJobID)) {
                        postProcess.setControlJobID(controlJobID.getValue());
                        postProcess.setControlJobObj(controlJobID.getReferenceKey());
                    }
                    ObjectIdentifier cassetteID = postProcessTargetObject.getCassetteID();
                    if (!ObjectIdentifier.isEmpty(cassetteID)) {
                        postProcess.setCassetteID(cassetteID.getValue());
                        postProcess.setCassetteObj(cassetteID.getReferenceKey());
                    }
                    postProcess.setCommitFlag(actionInfo.getCommitFlag());
                    postProcess.setStatus(actionInfo.getStatus());
                    postProcess.setSplitCount(actionInfo.getSplitCount());
                    postProcess.setErrorAction(actionInfo.getErrorAction());
                    postProcess.setCreateTime(Timestamp.valueOf(actionInfo.getCreateTime()));
                    postProcess.setUpdateTime(Timestamp.valueOf(actionInfo.getUpdateTime()));
                    postProcess.setClaimUserID(actionInfo.getClaimUserID());
                    postProcess.setClaimTime(Timestamp.valueOf(actionInfo.getClaimTime()));
                    postProcess.setClaimMemo(actionInfo.getClaimMemo());
                    postProcess.setExtEventID(actionInfo.getExtEventID());
                    cimJpaRepository.save(postProcess);
                    log.info(" Successfully added the queue....");
                }
            }
            //-----------------------------------------------------
            // Add original dKey if post process is on chained mode
            //-----------------------------------------------------
            int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
            String strTriggerDKey = null;
            if (ppChainMode == 1) {
                strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                if (CimStringUtils.isEmpty(strTriggerDKey)) {
                    ppChainMode = 0;
                }
            }
            if (1 == ppChainMode) {
                List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList = new ArrayList<>();
                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo1 = new Infos.PostProcessAdditionalInfo();
                postProcessAdditionalInfoList.add(postProcessAdditionalInfo1);
                postProcessAdditionalInfo1.setDKey(dKey);
                postProcessAdditionalInfo1.setSequenceNumber(0);
                postProcessAdditionalInfo1.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                postProcessAdditionalInfo1.setValue(strTriggerDKey);
                Infos.PostProcessAdditionalInfo postProcessAdditionalInfo2 = new Infos.PostProcessAdditionalInfo();
                postProcessAdditionalInfoList.add(postProcessAdditionalInfo2);
                postProcessAdditionalInfo2.setDKey(dKey);
                postProcessAdditionalInfo2.setSequenceNumber(0);
                postProcessAdditionalInfo2.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT);
                String strChainExecCnt = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT);
                postProcessAdditionalInfo2.setValue(strChainExecCnt);
                this.postProcessAdditionalInfoInsertDR(objCommon, postProcessAdditionalInfoList);
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG, "1");
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE)) {
            //-------------------------
            // Delete Queue
            //-------------------------
            int lenActionInfo = CimArrayUtils.getSize(strPostProcessActionInfoSeq);
            for (int i = 0; i < lenActionInfo; i++) {
                Infos.PostProcessActionInfo postProcessActionInfo = strPostProcessActionInfoSeq.get(i);
                //--------------------------
                // Preparing Host variable
                //--------------------------
                String dkey = postProcessActionInfo.getDKey();
                if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER)) {
                    //------------------------------
                    // Check Lot interFabXferState
                    //------------------------------
                    String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                    if (CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)
                            || CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                        throw new ServiceException(retCodeConfig.getInterfabInvalidXferstate(), interFabXferState);
                    }
                }
                if (strPostProcessActionInfoSeq.get(i).getSequenceNumber() != -1) {
                    //-------------------------------------------------------------------------------
                    // When sequence number is specified, the record is deleted.
                    //-------------------------------------------------------------------------------
                    Integer sequenceNumber = strPostProcessActionInfoSeq.get(i).getSequenceNumber();
                    CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                    cimPostProcessExam.setDkey(dkey);
                    cimPostProcessExam.setSeqNo(sequenceNumber);
                    cimJpaRepository.delete(Example.of(cimPostProcessExam));
                } else if (strPostProcessActionInfoSeq.get(i).getSequenceNumber() == -1) {
                    //-------------------------------------------------------------------------------
                    // When sequence number is not specified, all is deleted in the same dKey.
                    //-------------------------------------------------------------------------------
                    CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                    cimPostProcessExam.setDkey(dkey);
                    cimJpaRepository.delete(Example.of(cimPostProcessExam));
                } else {
                    log.info("invalid parameter .");
                    Validations.check(true, retCodeConfig.getInvalidParameter());
                }
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT)) {
            //-------------------------
            // Delete Queue
            //-------------------------
            int lenActionInfo = CimArrayUtils.getSize(strPostProcessActionInfoSeq);
            for (int i = 0; i < lenActionInfo; i++) {
                Infos.PostProcessActionInfo postProcessActionInfo = strPostProcessActionInfoSeq.get(i);
                if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER)) {
                    //------------------------------
                    // Check Lot interFabXferState
                    //------------------------------
                    String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                    if (CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)
                            || CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                        throw new ServiceException(retCodeConfig.getInterfabInvalidXferstate(), interFabXferState);
                    }
                }
                CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                cimPostProcessExam.setDkey(postProcessActionInfo.getDKey());
                cimPostProcessExam.setTargetType(postProcessActionInfo.getTargetType());
                cimPostProcessExam.setLotID(ObjectIdentifier.fetchValue(postProcessActionInfo.getPostProcessTargetObject().getLotID()));
                cimPostProcessExam.setLotObj(ObjectIdentifier.fetchReferenceKey(postProcessActionInfo.getPostProcessTargetObject().getLotID()));
                cimJpaRepository.delete(Example.of(cimPostProcessExam));
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHCAST)) {
            //-------------------------
            // Delete Queue
            //-------------------------
            int lenActionInfo = CimArrayUtils.getSize(strPostProcessActionInfoSeq);
            for (int i = 0; i < lenActionInfo; i++) {
                Infos.PostProcessActionInfo postProcessActionInfo = strPostProcessActionInfoSeq.get(i);
                ObjectIdentifier cassetteID = postProcessActionInfo.getPostProcessTargetObject().getCassetteID();
                CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                cimPostProcessExam.setDkey(postProcessActionInfo.getDKey());
                cimPostProcessExam.setTargetType(postProcessActionInfo.getTargetType());
                cimPostProcessExam.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
                cimPostProcessExam.setCassetteObj(ObjectIdentifier.fetchReferenceKey(cassetteID));
                cimJpaRepository.delete(Example.of(cimPostProcessExam));
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATE)) {
            //-------------------------
            // Update Queue
            //-------------------------
            // Currently, there is no function to update the queue.
            // This sentence is for maintenance or customize.
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATEFORSTATUS)) {
            int lenActionInfo = CimArrayUtils.getSize(strPostProcessActionInfoSeq);
            for (int i = 0; i < lenActionInfo; i++) {
                Infos.PostProcessActionInfo postProcessActionInfo = strPostProcessActionInfoSeq.get(i);
                CimPostProcessDO cimPostProcessExam = new CimPostProcessDO();
                cimPostProcessExam.setDkey(postProcessActionInfo.getDKey());
                cimPostProcessExam.setSeqNo(postProcessActionInfo.getSequenceNumber());
                List<CimPostProcessDO> cimPostProcessDOList = cimJpaRepository.findAll(Example.of(cimPostProcessExam));
                if (!CimArrayUtils.isEmpty(cimPostProcessDOList)) {
                    for (CimPostProcessDO cimPostProcessDO : cimPostProcessDOList) {
                        cimPostProcessDO.setStatus(postProcessActionInfo.getStatus());
                        cimJpaRepository.save(cimPostProcessDO);
                    }
                }
            }
        } else {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        return null;
    }

    @Override
    public Outputs.PostProcessQueueMakeOut postProcessQueueMake(Infos.ObjCommon objCommon, String txID, String patternID, String key, Integer seqNo, Infos.PostProcessRegistrationParam processRegistrationParm, String claimMemo) {
        Outputs.PostProcessQueueMakeOut postProcessQueueMakeOut = new Outputs.PostProcessQueueMakeOut();
        ObjectIdentifier equipmentID = processRegistrationParm.getEquipmentID();
        //------------------------------------------------------------
        //Get Post Process Pattern Information by PatternID / TX_ID
        //------------------------------------------------------------
        log.info("Get Post Process Pattern Information.");
        List<Infos.HashedInfo> strSearchInfoSeq = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            log.info("{} strPostProcessRegistrationParm.equipmentID", equipmentID);
            strSearchInfoSeq.add(new Infos.HashedInfo(BizConstant.SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTID, equipmentID.getValue()));
        }
        String ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getValue();
        log.info("{} OM_PP_CHAIN_FLAG", ppChainMode);
        if (CimStringUtils.equals("1", ppChainMode)) {
            log.info("ppChainMode=1, get SP_ThreadSpecificData_Key_TriggerDKey");
            String strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
            if (CimStringUtils.isEmpty(strTriggerDKey)) {
                ppChainMode = "0";
            }
        }
        if (CimStringUtils.equals("1", ppChainMode)) {
            log.info("{} ppChainMode=1, set transactionID", objCommon.getTransactionID());
            strSearchInfoSeq.add(new Infos.HashedInfo(BizConstant.SP_POSTPROCESS_SEARCH_KEY_TRIGGERTXID, objCommon.getTransactionID()));
        }
        List<Infos.PostProcessConfigInfo> strPatternInfoSeq = this.postProcessConfigGetPatternInfoDR(objCommon, txID, patternID, strSearchInfoSeq);
        int nPatternLen = CimArrayUtils.getSize(strPatternInfoSeq);
        //--------------------------------------------------
        // Set target Items.
        //--------------------------------------------------
        List<ObjectIdentifier> lotIDs = processRegistrationParm.getLotIDs();
        ObjectIdentifier controlJobID = processRegistrationParm.getControlJobID();
        //List<ObjectIdentifier> cassetteIDs = processRegistrationParm.getCassetteIDs();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(processRegistrationParm.getCassetteIDs())) {
            //-----------------------------------
            // Omit already collected cassette
            //-----------------------------------
            int castLen = CimArrayUtils.getSize(cassetteIDs);
            List<ObjectIdentifier> tmpCassetteIDs = new ArrayList<>();
            int count = 0;
            for (int i = 0; i < castLen; i++) {
                ObjectIdentifier cassetteID = cassetteIDs.get(i);
                boolean sameCastFoundFlag = false;
                //-----------------------------------
                //  Verify Cassette Object
                //-----------------------------------
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                tmpCassetteIDs.add(new ObjectIdentifier(aCassette.getIdentifier(), aCassette.getPrimaryKey()));

                if (!CimObjectUtils.isEmpty(tmpCassetteIDs)) {
                    for (int j = 0; j < count; j++) {
                        if (ObjectIdentifier.equalsWithValue(tmpCassetteIDs.get(i), cassetteIDs.get(j))) {
                            sameCastFoundFlag = true;
                            break;
                        }
                    }

                    if (!sameCastFoundFlag) {
                        cassetteIDs.set(count++, tmpCassetteIDs.get(i));
                    }
                }
            }
        } else {
            boolean needCastFlag = false;
            //------------------------------------------------------------
            // Check target type.
            // If SP_PostProcess_TargetType_CAST, get cassette from lot.
            //------------------------------------------------------------
            for (int i = 0; i < nPatternLen; i++) {
                if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)) {
                    needCastFlag = true;
                    break;
                }
            }

            if (needCastFlag) {
                log.info("SP_PostProcess_TargetType_CAST was found, but input cassetteID is null. So set cassette from Lot.");
                int lotLen = CimArrayUtils.getSize(lotIDs);
                int count = 0;
                for (int i = 0; i < lotLen; i++) {
                    //---------------------------
                    // Get Cassette from Lot
                    //---------------------------
                    ObjectIdentifier getLotCassette = null;
                    try {
                        getLotCassette = lotMethod.lotCassetteGet(objCommon, lotIDs.get(i));
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                            continue;
                        } else {
                            log.error("getLotCassette() != ok");
                            throw e;
                        }
                    }

                    ObjectIdentifier objGetLotCassetteOut = getLotCassette;

                    //-----------------------------------
                    // Omit already collected cassette
                    //-----------------------------------
                    boolean sameCastFoundFlag = false;
                    for (int j = 0; j < count; j++) {
                        if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), objGetLotCassetteOut)) {
                            sameCastFoundFlag = true;
                        }
                    }

                    if (!sameCastFoundFlag) {
                        cassetteIDs.add(objGetLotCassetteOut);
                    }
                }
            }
        }

        List<Integer> cassetteIndexTable = new ArrayList<>();
        int castLen = CimArrayUtils.getSize(cassetteIDs);
        for (int castCnt = 0; castCnt < castLen; castCnt++) {
            cassetteIndexTable.add(castCnt);
        }
        if (nPatternLen == 0) {
            postProcessQueueMakeOut.setDKey(key);
            log.info("Pattern information is not found.");
            return postProcessQueueMakeOut;
        }

        String dKey = key;
        if (CimObjectUtils.isEmpty(key)) {
            //-------------------------------------------------
            //Generate dKey
            //-------------------------------------------------
            dKey = CimDateUtils.getCurrentDateTimeByPattern("yyyy-MM-dd-HH.mm.ss.SSSS") + "+" + objCommon.getUser().getUserID().getValue();
        }

        if (seqNo <= 0) {
            seqNo = 1;
        }

        //-------------------------------------------------
        //Preparing Action Informations
        //-------------------------------------------------
        List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();
        List<Integer> tmpLotIndexTable = new ArrayList<>();
        List<Integer> tmpABSLotIndexTable = new ArrayList<>();

        List<ObjectIdentifier> tmpLotIDs = new ArrayList<>();
        List<ObjectIdentifier> tmpABSLotIDs = new ArrayList<>();

        List<ObjectIdentifier> tmpCastIDs = new ArrayList<>();
        List<ObjectIdentifier> tmpABSCastIDs = new ArrayList<>();

        for (int i = 0; i < CimArrayUtils.getSize(lotIDs); i++) {
            //-------------------------------------------------
            //Check lot state
            //-------------------------------------------------
            ObjectIdentifier lotID = lotIDs.get(i);
            String lotState = lotMethod.lotStateGet(objCommon, lotID);

            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                //Active ... OK
            } else {
                log.info("{} LotState != 'Active'. lotID:", lotID);
                continue;
            }

            //-------------------------------------------------
            //Check Process State "Waiting"
            //-------------------------------------------------
            String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);

            if (CimStringUtils.equals(txID, "OEDCR002"))    //TxEDCByPJRpt
            {
                log.info("txID = OEDCR002");
                if (CimStringUtils.equals(theLotProcessState, CIMStateConst.SP_LOT_PROCESS_STATE_PROCESSING)) {
                    ;//Processing ... OK
                } else {
                    log.info("{} Lot Process Status is not Processing.", lotID);
                    continue;
                }
            } else {
                log.info("txID != OEDCR002");
                if (CimStringUtils.equals(theLotProcessState, CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING)) {
                    ;//Waiting ... OK
                } else {
                    log.info("{} Lot Process Status is not Waiting.", lotID);
                    continue;
                }
            }

            //---------------------------
            // Get Cassette from Lot
            //---------------------------
            ObjectIdentifier getLotCassette = null;
            boolean castFoundFlag = false;
            try {
                getLotCassette = lotMethod.lotCassetteGet(objCommon, lotID);
                castFoundFlag = true;
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                    castFoundFlag = false;
                    //Do nothing...
                } else {
                    log.info("lot_cassette_Get() returned error.");
                    throw e;
                }
            }

            tmpABSCastIDs.add(getLotCassette);
            tmpABSLotIndexTable.add(i);
            tmpABSLotIDs.add(lotID);

            //-------------------------------------------------
            //Check lot hold state
            //-------------------------------------------------
            String getLotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);

            if (CimStringUtils.equals(getLotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                log.info("{} LotHoldState == 'ONHOLD'. lotID:", lotID);
                continue; //OnHold ... NG (Skip)
            } else {
                ;//!= OnHold ... OK
            }

            //-------------------------------------------------
            //  Check Lot's InPostProcessFlag
            //-------------------------------------------------
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagGet = lotMethod.lotInPostProcessFlagGet(objCommon, lotIDs.get(i));
            Boolean inPostProcessFlagOfLot = lotInPostProcessFlagGet.getInPostProcessFlagOfLot();

            if (CimBooleanUtils.isTrue(inPostProcessFlagOfLot)) {
                log.info("{} inPostProcessFlagOfLot == true. lotID:", lotIDs.get(i));
                continue; //Lot is in post process ... NG (Skip)
            } else {
                ;//Lot is not in post process ... OK
            }

            if (castFoundFlag) {
                tmpCastIDs.add(getLotCassette);
            }

            tmpLotIndexTable.add(i);
            tmpLotIDs.add(lotIDs.get(i));
        }
        //-----------------------------------
        //  Verify exec condition
        //-----------------------------------
        for (int patnCnt = 0; patnCnt < nPatternLen; patnCnt++) {
            Infos.PostProcessConfigInfo postProcessConfigInfo = strPatternInfoSeq.get(patnCnt);
            int execConditionLen = CimArrayUtils.getSize(postProcessConfigInfo.getExecConditionSeq());
            for (int execCnt = 0; execCnt < execConditionLen; execCnt++) {
                Integer ConditionValue = postProcessConfigInfo.getExecConditionSeq().get(execCnt);
                if (0 >= ConditionValue ||
                        Objects.equals(ConditionValue, postProcessConfigInfo.getSeqNo())) {
                    log.info("{} ### Invalid execCondition was found. Set execCondition null. execCondition = ", ConditionValue);
                    Validations.check(true, new OmCode(retCodeConfig.getInvalidExecCondition(), ConditionValue.toString()));
                }
                boolean foundFlag = false;
                for (int patnCnt2 = 0; patnCnt2 < nPatternLen; patnCnt2++) {
                    Infos.PostProcessConfigInfo postProcessConfigInfo2 = strPatternInfoSeq.get(patnCnt);
                    int exCon = CimObjectUtils.isEmpty(postProcessConfigInfo2.getExecConditionSeq()) ? 0 : postProcessConfigInfo2.getExecConditionSeq().get(execCnt);
                    if (exCon == strPatternInfoSeq.get(patnCnt2).getSeqNo()) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    log.info("{} ### Invalid execCondition was found. Set execCondition null. execCondition = ", ConditionValue);
                    Validations.check(true, new OmCode(retCodeConfig.getInvalidExecCondition(), ConditionValue.toString()));
                }
            }
        }

        Integer nPatternMax = strPatternInfoSeq.get(nPatternLen - 1).getSeqNo();
        List<List<Integer>> seqNumberTable = new ArrayList<>();

        boolean bAPCInterface = false; /*for APC I/F*/
        boolean bAPCRunCapabilityDelete = true; /*for APC I/F*/


        log.info("{} Max Seq# = ", nPatternMax);
        log.info("{} strPatternInfoSeq.size()", nPatternLen);

        for (int i = 0; i < nPatternLen; i++) {
            Infos.PostProcessConfigInfo postProcessConfigInfo = strPatternInfoSeq.get(i);
            Integer syncFlag = postProcessConfigInfo.getSyncFlag();
            String tmpStatus;
            if (Objects.equals(Integer.valueOf(SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL), syncFlag)
                    || Objects.equals(Integer.valueOf(SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL), syncFlag))   //Asynchronous+Parallel
            {
                log.info("Asynchronous execution.");
                tmpStatus = BizConstant.SP_DURABLE_PROCSTATE_WAITING;
            } else {
                log.info("Synchronous execution.");
                tmpStatus = BizConstant.SP_POSTPROCESS_STATE_RESERVED;
            }
            //-------------------------------------------------
            //Preparing Q-item (1)
            //-------------------------------------------------
            Infos.PostProcessActionInfo qitem = new Infos.PostProcessActionInfo();
            qitem.setExecConditionList(new ArrayList<>());
            qitem.setDKey(dKey);
            qitem.setTransationID(objCommon.getTransactionID());
            qitem.setClaimUserID(objCommon.getUser().getUserID().getValue());
            qitem.setClaimTime(CimDateUtils.getCurrentDateTimeWithDefault());
            qitem.setCreateTime(CimDateUtils.getCurrentDateTimeWithDefault());
            qitem.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
            qitem.setClaimShopDate(objCommon.getTimeStamp().getReportShopDate());

            Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
            postProcessTargetObject.setEquipmentID(equipmentID);
            postProcessTargetObject.setControlJobID(controlJobID);
            qitem.setPostProcessTargetObject(postProcessTargetObject);
            //-------------------------------------------------
            //Preparing Q-item (2)
            //-------------------------------------------------
            qitem.setWatchDogName(BizConstant.SP_POSTPROCESS_WATCHDOGNAME_DEFAULT);
            qitem.setTargetType(strPatternInfoSeq.get(i).getTargetType());
            qitem.setPostProcessID(strPatternInfoSeq.get(i).getPostProcID());
            qitem.setSyncFlag(strPatternInfoSeq.get(i).getSyncFlag());
            qitem.setCommitFlag(true);
            qitem.setStatus(tmpStatus);
            qitem.setSplitCount(0);
            qitem.setErrorAction(strPatternInfoSeq.get(i).getErrorAction());
            if (CimStringUtils.equals(strPatternInfoSeq.get(i).getPostProcID(), BizConstant.SP_POSTPROCESS_ACTIONID_EXTERNALPOSTPROCESSEXECREQ)) {
                qitem.setExtEventID(strPatternInfoSeq.get(i).getExtEventID());
            }

            //--------------------------------------------------------------------------------------
            // targetType = LOT ( without Hold/Inactive LotIDs )
            //--------------------------------------------------------------------------------------
            if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)) {
                log.info("targetType == SP_PostProcess_TargetType_LOT.");
                //check parameter
                if (CimObjectUtils.isEmpty(lotIDs)) {
                    seqNumberTable.clear();
                    Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "lotIDs", patternID));
                }
                List<Integer> list = new ArrayList<>();
                for (int j = 0; j < lotIDs.size(); j++) {
                    list.add(j, 0);
                }
                seqNumberTable.add(list);

                //for APC I/F
                if (CimStringUtils.equals(strPatternInfoSeq.get(i).getPostProcID(), BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION)) {
                    bAPCInterface = true;
                    if (!CimObjectUtils.isEmpty(tmpLotIDs)) {
                        bAPCRunCapabilityDelete = false;
                    }
                }

                //registration by every lot( PostProc1[LOT-A, LOT-B, LOT-C], PostProc2[LOT-A, LOT-B, LOT-C], PostProc3[LOT-A, LOT-B, LOT-C] )
                for (int j = 0; j < tmpLotIDs.size(); j++) {
                    //-------------------------------------------------
                    //exec condition
                    //-------------------------------------------------
                    List<Long> conditionsl = new ArrayList<>();
                    qitem.setExecConditionList(conditionsl);
                    List<Integer> execConditionSeq = strPatternInfoSeq.get(i).getExecConditionSeq();
                    if (!CimObjectUtils.isEmpty(execConditionSeq)) {
                        for (int k = 0; k < CimArrayUtils.getSize(execConditionSeq); k++) {
                            int tmpSeqNo = execConditionSeq.get(k);
                            if (tmpSeqNo <= 0 && nPatternMax < tmpSeqNo) {
                                seqNumberTable.clear();
                                Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "execCondition", patternID));
                            }

                            if (seqNumberTable.get(tmpSeqNo - 1).size() > 1) {
                                //----------------------------------------------------------------------------
                                // Check if the target type is CAST
                                //----------------------------------------------------------------------------
                                if (CimStringUtils.equals(strPatternInfoSeq.get(tmpSeqNo - 1).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)) {
                                    conditionsl.set(k, 0l);
                                    //-----------------------------------------------------------------------------------------------------
                                    // "target type Cast" means this pattern for LOT have to wait the cassette post exec includes this Lot.
                                    //-----------------------------------------------------------------------------------------------------
                                    boolean foundFlag = false;
                                    for (int allCastCnt = 0; allCastCnt < cassetteIDs.size(); allCastCnt++) {
                                        for (int lotCastCnt = 0; lotCastCnt < tmpCastIDs.size(); lotCastCnt++) {
                                            if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(allCastCnt), tmpCastIDs.get(lotCastCnt)) &&
                                                    ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), tmpLotIDs.get(lotCastCnt))) {
                                                qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpLotIndexTable.get(allCastCnt))));
                                                foundFlag = true;
                                                break;
                                            }
                                        }
                                        if (foundFlag) break;
                                    }
                                }
                                //----------------------------------------------------------------------------
                                // Else the target type is LOT or LOT_ABSOLUTE.
                                // Wait for same lot's post process only.
                                //----------------------------------------------------------------------------
                                else {
                                    //neyo : the sorce code is: get(1 + tmpLotIndexTable.get(j)))),but can not pass ;change to :get(0 + tmpLotIndexTable.get(j))))
                                    qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(0 + tmpLotIndexTable.get(j))));
                                }

                            } else {
                                qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(0)));
                            }
                        }
                    }

                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    //neyo: the sorce code set(1 + tmpLotIndexTable.get(j), sequenceNumber) ,but can not pass ;change to :set(0 + tmpLotIndexTable.get(j), sequenceNumber)
                    seqNumberTable.get(strPatternInfoSeq.get(i).getSeqNo() - 1).set(0 + tmpLotIndexTable.get(j), seqNo);

                    //-------------------------------------------------
                    //Preparing Q-item 3
                    //-------------------------------------------------
                    qitem.getPostProcessTargetObject().setLotID(tmpLotIDs.get(j));
                    qitem.getPostProcessTargetObject().setCassetteID(null);
                    qitem.setSequenceNumber(seqNo++);

                    //deep clone the qitem
                    Infos.PostProcessActionInfo qitemNew = cloner.deepClone(qitem);
                    actionInfoSeq.add(qitemNew);
                }
            }
            //--------------------------------------------------------------------------------------
            // targetType = LOT-ABSOLUTE ( All LotIDs )
            //--------------------------------------------------------------------------------------
            else if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT_ABSOLUTE)) {
                log.info("targetType == SP_PostProcess_TargetType_LOT_ABSOLUTE.");
                //check parameter
                if (lotIDs.size() == 0) {
                    seqNumberTable.clear();
                    Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "lotIDs", patternID));
                }

                //replace targetType -> "LOT"
                qitem.setTargetType(BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT);
                List<Integer> ins = new ArrayList<>();
                ins.add(0);
                seqNumberTable.add(strPatternInfoSeq.get(i).getSeqNo() - 1, ins);

                for (int j = 0; j < lotIDs.size(); j++) {
                    ins.add(j, 0);
                    seqNumberTable.add(strPatternInfoSeq.get(i).getSeqNo() - 1, ins);
                }

                //for APC I/F
                if (CimStringUtils.equals(strPatternInfoSeq.get(i).getPostProcID(), BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION)) {
                    bAPCInterface = true;
                    if (tmpABSLotIDs.size() > 0) {
                        bAPCRunCapabilityDelete = false;
                    }
                }

                //registration by every lot( PostProc1[LOT-A, LOT-B, LOT-C], PostProc2[LOT-A, LOT-B, LOT-C], PostProc3[LOT-A, LOT-B, LOT-C] )
                for (int j = 0; j < tmpABSLotIDs.size(); j++) {
                    //-------------------------------------------------
                    //exec condition
                    //-------------------------------------------------
                    List<Integer> execConditionSeq = strPatternInfoSeq.get(i).getExecConditionSeq();
                    if (!CimObjectUtils.isEmpty(execConditionSeq)) {
                        for (int k = 0; k < execConditionSeq.size(); k++) {
                            int tmpSeqNo = execConditionSeq.get(k);
                            if (tmpSeqNo <= 0 && nPatternMax < tmpSeqNo) {
                                seqNumberTable.clear();
                                Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "execCondition", patternID));
                            }

                            if (seqNumberTable.get(tmpSeqNo - 1).size() > 1) {
                                //----------------------------------------------------------------------------
                                // Check if the target type is CAST
                                //----------------------------------------------------------------------------
                                if (CimStringUtils.equals(strPatternInfoSeq.get(tmpSeqNo - 1).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)) {
                                    qitem.getExecConditionList().add(k, 0l);
                                    //----------------------------------------------------------------------------------------------------
                                    // "target type Cast" means this pattern for LOT have to wait the cassette post exec includs this Lot.
                                    //----------------------------------------------------------------------------------------------------
                                    boolean foundFlag = false;
                                    for (int allCastCnt = 0; allCastCnt < cassetteIDs.size(); allCastCnt++) {
                                        for (int lotCastCnt = 0; lotCastCnt < tmpABSCastIDs.size(); lotCastCnt++) {
                                            if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(allCastCnt), tmpABSCastIDs.get(lotCastCnt)) &&
                                                    ObjectIdentifier.equalsWithValue(tmpABSLotIDs.get(j), tmpABSLotIDs.get(lotCastCnt))) {
                                                qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpLotIndexTable.get(allCastCnt))));
                                                foundFlag = true;
                                                break;
                                            }
                                        }
                                        if (foundFlag) break;
                                    }
                                }
                                //----------------------------------------------------------------------------
                                // Else the target type is LOT or LOT_ABSOLUTE.
                                // Wait for same lot's post process only.
                                //----------------------------------------------------------------------------
                                else {
                                    qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpABSLotIndexTable.get(j))));
                                }
                            } else {
                                qitem.getExecConditionList().add(k, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(0)));
                            }
                        }
                    }


                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    seqNumberTable.get(strPatternInfoSeq.get(i).getSeqNo() - 1).set(1 + tmpABSLotIndexTable.get(j), seqNo);

                    //-------------------------------------------------
                    //Preparing Q-item 3
                    //-------------------------------------------------
                    qitem.getPostProcessTargetObject().setLotID(tmpABSLotIDs.get(j));
                    qitem.getPostProcessTargetObject().setCassetteID(null);
                    qitem.setSequenceNumber(seqNo++);

                    //deep clone the qitem
                    Infos.PostProcessActionInfo qitemNew = cloner.deepClone(qitem);
                    actionInfoSeq.add(qitemNew);
                }
            }
            //--------------------------------------------------------------------------------------
            // targetType = EQP
            //--------------------------------------------------------------------------------------
            else if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_EQP)) {
                log.info("targetType == SP_PostProcess_TargetType_EQP.");
                if (ObjectIdentifier.isEmpty(equipmentID)) {
                    seqNumberTable.clear();
                    Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "equipmentID", patternID));
                }

                List<Integer> ins = new ArrayList<>();
                ins.add(0);
                seqNumberTable.add(0, ins);
                //-------------------------------------------------
                //exec condition
                //-------------------------------------------------
                List<Integer> execConditionSeq = strPatternInfoSeq.get(i).getExecConditionSeq();
                if (!CimObjectUtils.isEmpty(execConditionSeq)) {
                    for (int j = 0; j < execConditionSeq.size(); j++) {
                        int tmpSeqNo = execConditionSeq.get(j);

                        int len = qitem.getExecConditionList().size();
                        int len2 = seqNumberTable.get(tmpSeqNo - 1).size();

                        int index1 = 0;
                        for (int k = 0; k < len2; k++) {
                            qitem.getExecConditionList().add(len + index1, 0l);
                            if (seqNumberTable.get(tmpSeqNo - 1).get(k) != 0) {
                                qitem.getExecConditionList().add(len + index1++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(k)));
                            }
                        }
                    }
                }

                //-------------------------------------------------
                //update seqNumberTable
                //-------------------------------------------------
                seqNumberTable.get(strPatternInfoSeq.get(i).getSeqNo() - 1).set(0, seqNo);

                //-------------------------------------------------
                //Preparing Q-item 3
                //-------------------------------------------------
                qitem.getPostProcessTargetObject().setCassetteID(null);
                qitem.getPostProcessTargetObject().setLotID(null);
                qitem.setSequenceNumber(seqNo++);

                //deep clone the qitem
                Infos.PostProcessActionInfo qitemNew = cloner.deepClone(qitem);
                actionInfoSeq.add(qitemNew);
            }
            //--------------------------------------------------------------------------------------
            // targetType = EQP+CONTROLJOB
            //--------------------------------------------------------------------------------------
            else if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_EQPANDCJ)) {
                log.info("targetType == SP_PostProcess_TargetType_EQPandCJ.");
                if (ObjectIdentifier.isEmpty(controlJobID)) {
                    seqNumberTable.clear();
                    Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "controlJobID", patternID));
                }
                Validations.check(ObjectIdentifier.isEmpty(equipmentID), new OmCode(retCodeConfig.getPostprocPatternMismatch(), "equipmentID", patternID));

                List<Integer> ins = new ArrayList<>();
                ins.add(0);
                seqNumberTable.add(0, ins);
                //-------------------------------------------------
                //exec condition
                //-------------------------------------------------
                List<Integer> execConditionSeq = strPatternInfoSeq.get(i).getExecConditionSeq();
                if (!CimObjectUtils.isEmpty(execConditionSeq)) {
                    for (int j = 0; j < execConditionSeq.size(); j++) {
                        int tmpSeqNo = execConditionSeq.get(j);

                        int len = CimArrayUtils.getSize(qitem.getExecConditionList());
                        int len2 = CimArrayUtils.getSize(seqNumberTable.get(tmpSeqNo - 1));

                        int index1 = 0;
                        for (int k = 0; k < len2; k++) {
                            qitem.getExecConditionList().add(len + index1, 0l);
                            if (seqNumberTable.get(tmpSeqNo - 1).get(k) != 0) {
                                qitem.getExecConditionList().add(len + index1++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(k)));
                            }
                        }
                    }
                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    seqNumberTable.get(strPatternInfoSeq.get(i).getSeqNo() - 1).set(0, seqNo);
                }


                //-------------------------------------------------
                //Preparing Q-item 3
                //-------------------------------------------------
                qitem.getPostProcessTargetObject().setCassetteID(null);
                qitem.getPostProcessTargetObject().setLotID(null);
                qitem.setSequenceNumber(seqNo++);

                //deep clone the qitem
                Infos.PostProcessActionInfo qitemNew = cloner.deepClone(qitem);
                actionInfoSeq.add(qitemNew);
            }
//D8000028 add start
            //--------------------------------------------------------------------------------------
            // targetType = CAST
            //--------------------------------------------------------------------------------------
            else if (CimStringUtils.equals(strPatternInfoSeq.get(i).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)) {
                log.info("targetType == SP_PostProcess_TargetType_CAST.");
                //------------------
                // Initialization
                //------------------
                List<Integer> list = new ArrayList<>();
                for (int j = 0; j < cassetteIDs.size(); j++) {
                    Integer no = strPatternInfoSeq.get(i).getSeqNo();
                    list.add(j, 0);
                    //seqNumberTable.get(strPatternInfoSeq.get(i).getSequenceNumber() - 1).set(j, 0);
                }
                seqNumberTable.add(list);
                for (int j = 0; j < cassetteIDs.size(); j++) {
                    //-------------------------------------------------
                    // Set exec condition
                    //-------------------------------------------------
                    int totalExecCount = 0;
                    List<Integer> execConditionSeq = strPatternInfoSeq.get(i).getExecConditionSeq();
                    if (!CimObjectUtils.isEmpty(execConditionSeq)) {
                        for (int k = 0; k < execConditionSeq.size(); k++) {
                            int tmpSeqNo = execConditionSeq.get(k);
                            if (tmpSeqNo <= 0 && nPatternMax < tmpSeqNo) {
                                seqNumberTable.clear();
                                Validations.check(true, new OmCode(retCodeConfig.getPostprocPatternMismatch(), "execCondition", patternID));
                            }

                            //----------------------------------------------------------------------------
                            // Check collected Queue table seqno.
                            // If the length is 2 or more, the target is LOT or LOT_ABSOLUTE or CAST
                            //----------------------------------------------------------------------------
                            if (seqNumberTable.get(tmpSeqNo - 1).size() > 1) {
                                //----------------------------------------------------------------------------
                                // Check if the target type is Lot
                                //----------------------------------------------------------------------------
                                if (CimStringUtils.equals(strPatternInfoSeq.get(tmpSeqNo - 1).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)) {
                                    qitem.getExecConditionList().add(totalExecCount, 0l);
                                    //-------------------------------------------------------------------------------------------------
                                    // "target type Lot" means this pattern for CAST have to wait the all including Lot Post Process.
                                    //-------------------------------------------------------------------------------------------------
                                    for (int lotCastCnt = 0; lotCastCnt < tmpCastIDs.size(); lotCastCnt++) {
                                        if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), tmpCastIDs.get(lotCastCnt))) {
                                            qitem.getExecConditionList().add(totalExecCount++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(tmpLotIndexTable.get(lotCastCnt))));
                                        }
                                    }
                                }
                                //----------------------------------------------------------------------------
                                // Check if the target type is Lot_ABSOLUTE
                                //----------------------------------------------------------------------------
                                else if (CimStringUtils.equals(strPatternInfoSeq.get(tmpSeqNo - 1).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT_ABSOLUTE)) {
                                    qitem.getExecConditionList().add(totalExecCount, 0l);
                                    //------------------------------------------------------------------------------------------------------
                                    // "target type ABS_LOT" means this pattern for CAST have to wait the all including Lot Post Process.
                                    //------------------------------------------------------------------------------------------------------
                                    for (int lotCastCnt = 0; lotCastCnt < tmpABSCastIDs.size(); lotCastCnt++) {
                                        if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), tmpABSCastIDs.get(lotCastCnt))) {
                                            qitem.getExecConditionList().add(totalExecCount++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpABSLotIndexTable.get(lotCastCnt))));
                                        }
                                    }
                                }
                                //----------------------------------------------------------------------------
                                // Else the target type is CAST
                                // Wait for same cassette's post process only.
                                //----------------------------------------------------------------------------
                                else {
                                    qitem.getExecConditionList().add(totalExecCount++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(1 + cassetteIndexTable.get(j))));
                                }
                            }
                            //----------------------------------------------------------------------------
                            // The Queue seqno is less than 2. It means the target is EQP or CJ
                            //----------------------------------------------------------------------------
                            else {
                                qitem.getExecConditionList().add(totalExecCount++, Long.valueOf(seqNumberTable.get(tmpSeqNo - 1).get(0)));
                            }
                        }
                    }


                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    seqNumberTable.get(strPatternInfoSeq.get(i).getSeqNo() - 1).set(cassetteIndexTable.get(j), seqNo);

                    //-------------------------------------------------
                    //Preparing Q-item 3
                    //-------------------------------------------------
                    qitem.getPostProcessTargetObject().setLotID(null);
                    qitem.getPostProcessTargetObject().setCassetteID(cassetteIDs.get(j));
                    qitem.setSequenceNumber(seqNo++);

                    //deep clone the qitem
                    Infos.PostProcessActionInfo qitemNew = cloner.deepClone(qitem);
                    actionInfoSeq.add(qitemNew);
                }
            }
//D8000028 add end
            else {
                log.info("{} Unknown targetType.", strPatternInfoSeq.get(i).getTargetType());
                seqNumberTable.clear();
                Validations.check(true, new OmCode(retCodeConfig.getPostprocUnknownTargettype(), strPatternInfoSeq.get(i).getTargetType()));
            }
        }


        seqNumberTable.clear();

        //for APC I/F
        if (bAPCInterface == true && bAPCRunCapabilityDelete == true) {
            if (!ObjectIdentifier.isEmpty(controlJobID)) {
                log.info("{} bAPCInterface == true && controlJobID == ", controlJobID);

                log.info("Delete APCRuntimeCapability information.");
                //TODO-NOTIMPL: APCRuntimeCapability_DeleteDR
            } else {
                log.info("bAPCInterface == true && controlJobID == null string");
            }
        }

        postProcessQueueMakeOut.setStrActionInfoSeq(actionInfoSeq);
        postProcessQueueMakeOut.setDKey(dKey);
        return postProcessQueueMakeOut;
    }

    @Override
    public List<Infos.PostProcessConfigInfo> postProcessConfigGetPatternInfoDR(Infos.ObjCommon objCommon, String txID, String patternID, List<Infos.HashedInfo> strSearchInfoSeq) {

        log.info("{} txID", txID);
        log.info("{} patternID", patternID);

        String fimmTranTx = null;
        String fimmTranPatternID = null;
        String fipostProcessPatternID = null;
        String fimmTranEventID = null;
        boolean fimmTranPatternSerch = false;

        //--------------------------
        // Check parameter.
        //--------------------------
        if (CimObjectUtils.isEmpty(patternID)) {
            if (CimObjectUtils.isEmpty(txID)) {
                Validations.check(CimObjectUtils.isEmpty(txID), retCodeConfig.getInvalidParameter());
            } else {
                //---------------------------------
                // Only specified TxID.
                // Get the pattern using the TxID.
                //---------------------------------
                fimmTranTx = txID;
                log.info("{} fimmTranTx", fimmTranTx);
                CimFimmTransDO cimFimmTransExam = new CimFimmTransDO();
                cimFimmTransExam.setTxID(fimmTranTx);
                List<CimFimmTransDO> fimmTransDO = cimJpaRepository.findAll(Example.of(cimFimmTransExam));
                if (!CimObjectUtils.isEmpty(fimmTransDO)) {
                    fimmTranPatternID = fimmTransDO.get(0).getPatternID();
                    fimmTranEventID = fimmTransDO.get(0).getExtEventID();
                    fimmTranPatternSerch = fimmTransDO.get(0).getPatternSearch();
                }
            }
            log.info("{} fimmTranPatternID", fimmTranPatternID);

            if (CimObjectUtils.isEmpty(fimmTranPatternID)) {
                log.info("fimmTranTx == \"\"");
                return Collections.emptyList();
            }

            fipostProcessPatternID = fimmTranPatternID;
        } else {
            fipostProcessPatternID = patternID;
            if (CimObjectUtils.isEmpty(txID)) {
                log.info("EXT_EVENT_ID is not acquired. Because, txID is empty.");
            } else {
                log.info("get EXT_EVENT_ID from OCTRX.");
                CimFimmTransDO cimFimmTransExam = new CimFimmTransDO();
                cimFimmTransExam.setTxID(fimmTranTx);
                List<CimFimmTransDO> fimmTransDO = cimJpaRepository.findAll(Example.of(cimFimmTransExam));
                if (!CimObjectUtils.isEmpty(fimmTransDO)) {
                    fimmTranEventID = fimmTransDO.get(0).getExtEventID();
                }
            }
        }

        //--------------------------
        // Additional Search for Post Process pattern.
        //--------------------------
        int nSearchInfoLen = CimArrayUtils.getSize(strSearchInfoSeq);

        if (fimmTranPatternSerch && !CimObjectUtils.isEmpty(txID)) {
            log.info("Tx ID is filled and fimmTranPatternSerch=1");
            if (0 < nSearchInfoLen) {
                //--------------------------
                // Collect Post Process search filter
                //--------------------------
                log.info("Perform additional pattern search");

                CimPostProcessFilterDO cimPostProcessFilterExam = new CimPostProcessFilterDO();
                cimPostProcessFilterExam.setTxID(txID);
                List<String> strPostProcFilterSeq = cimJpaRepository.findAll(Example.of(cimPostProcessFilterExam)).stream()
                        .sorted()
                        .map(CimPostProcessFilterDO::getFilterType)
                        .collect(Collectors.toList());
                int nFilterCnt = CimArrayUtils.getSize(strPostProcFilterSeq);

                boolean bPatternFoundFlag = false;
                String separatorChar = StandardProperties.OM_PP_QUERY_FILTER_DELIMITER.getValue();
                for (int i = 0; i < nFilterCnt; i++) {
                    //--------------------------
                    // Post Process pattern search
                    //--------------------------
                    log.info("loop for Filter", strPostProcFilterSeq.get(i));
                    // Separate search filter
                    List<String> strKeySeq = new ArrayList<>();
                    String filterType = strPostProcFilterSeq.get(i);
                    String[] result = filterType.split(separatorChar);
                    strKeySeq = Arrays.asList(result);
                    //--------------------------
                    // Collect search Value
                    //--------------------------
                    // Data which is used for the search
                    int nUsedInfoCnt = 0;
                    List<Infos.HashedInfo> strUsedSearchInfo = new ArrayList<>();

                    // Item which is needed for the search, but not collected
                    List<String> strCollectItemSeq = new ArrayList<>();
                    int nColItemCnt = 0;

                    nSearchInfoLen = CimArrayUtils.getSize(strSearchInfoSeq);
                    log.info("{} nSearchInfoLen", nSearchInfoLen);
                    int j;
                    int nKeyCnt = CimArrayUtils.getSize(strKeySeq);
                    for (j = 0; j < nKeyCnt; j++) {
                        log.info("{} {} Filter condition, j", strKeySeq.get(j), j);
                        boolean bFoundFlag = false;
                        for (int k = 0; k < nSearchInfoLen; k++) {
                            log.info("{} strTmpSearchInfoSeq, k", strSearchInfoSeq.get(k).getHashKey());
                            if (CimStringUtils.equals(strSearchInfoSeq.get(k).getHashKey(), strKeySeq.get(j))) {
                                log.info("{} Search information is already stored.", strSearchInfoSeq.get(k).getHashData());
                                strUsedSearchInfo.get(nUsedInfoCnt).setHashKey(strSearchInfoSeq.get(k).getHashKey());
                                strUsedSearchInfo.get(nUsedInfoCnt).setHashData(strSearchInfoSeq.get(k).getHashData());
                                nUsedInfoCnt++;
                                bFoundFlag = true;
                                break;
                            }
                        }
                        if (false == bFoundFlag) {
                            log.info("{} Search information isn't found for the filter condition.", strKeySeq.get(j));
                            strCollectItemSeq.set(nColItemCnt, strKeySeq.get(j));
                            nColItemCnt++;
                        }
                    }

                    if (0 < nColItemCnt) {
                        log.info("0 < nColItemCnt. Call postProcessPatternSearchCondition_Get.");
                        List<Infos.HashedInfo> postProcessPatternSearchConditionGet = this.postProcessPatternSearchConditionGet(objCommon, strCollectItemSeq, strSearchInfoSeq);

                        int nCollectedLen = CimArrayUtils.getSize(postProcessPatternSearchConditionGet);
                        log.info("{} nCollectedLen", nCollectedLen);

                        for (j = 0; j < nCollectedLen; j++) {
                            Infos.HashedInfo hashedInfo = postProcessPatternSearchConditionGet.get(j);
                            log.info("{} Loop for j", j);
                            strUsedSearchInfo.get(nUsedInfoCnt + j).setHashKey(hashedInfo.getHashKey());
                            strUsedSearchInfo.get(nUsedInfoCnt + j).setHashData(hashedInfo.getHashData());

                        }
                        nUsedInfoCnt = nUsedInfoCnt + j;
                        log.info("{} nUsedInfoCnt", nUsedInfoCnt);

                        nSearchInfoLen = CimArrayUtils.getSize(strSearchInfoSeq);
                        for (j = 0; j < nCollectedLen; j++) {
                            Infos.HashedInfo hashedInfo = postProcessPatternSearchConditionGet.get(j);
                            log.info("{} Loop for j", j);
                            strUsedSearchInfo.get(nUsedInfoCnt + j).setHashKey(hashedInfo.getHashKey());
                            strUsedSearchInfo.get(nUsedInfoCnt + j).setHashData(hashedInfo.getHashData());
                        }
                        nSearchInfoLen = nSearchInfoLen + j;
                    }

                    //--------------------------
                    // Create search key
                    //--------------------------
                    StringBuffer keyAllChar = new StringBuffer();
                    boolean bFirstFlag = true;
                    boolean bSkipFilter = false;
                    int l = 0;
                    for (j = 0; j < nKeyCnt; j++) {
                        log.info("{} {} Loop for j, strKeySeq[j]", j, strKeySeq.get(j));
                        boolean bFoundFlag = false;
                        for (l = 0; l < nUsedInfoCnt; l++) {
                            log.info("{} {} Loop for l. strUsedSearchInfo[l].hashKey", l, strUsedSearchInfo.get(l).getHashKey());
                            if (CimStringUtils.equals(strUsedSearchInfo.get(l).getHashKey(), strKeySeq.get(j))) {
                                log.info("{} Value for Key is found.", strKeySeq.get(j));
                                bFoundFlag = true;
                                if (bFirstFlag) {
                                    log.info("bFirstFlag is TRUE.");
                                    keyAllChar.append(String.format("%s", strUsedSearchInfo.get(l).getHashData()));
                                    bFirstFlag = false;
                                } else {
                                    log.info("bFirstFlag is false.");
                                    keyAllChar.append(String.format("%s", strUsedSearchInfo.get(l).getHashData()));
                                }
                                break;
                            }
                        }
                        if (false == bFoundFlag) {
                            log.info("{} Value for Key isn't found.", strKeySeq.get(j));
                            bSkipFilter = true;
                            break;
                        }
                    }

                    if (bSkipFilter) {
                        //Search information isn't collected. Skip Search for this filter
                        log.info("bSkipFilter is TRUE. Go to next search filter.");
                        continue;
                    }

                    //--------------------------
                    // Search with collected information
                    //--------------------------
                    CimPostProcessFilterValueDO cimPostProcessFilterValueExam = new CimPostProcessFilterValueDO();
                    cimPostProcessFilterValueExam.setTxID(txID);
                    cimPostProcessFilterValueExam.setFilterType(strPostProcFilterSeq.get(i));
                    cimPostProcessFilterValueExam.setFilterValue(keyAllChar.toString());
                    List<CimPostProcessFilterValueDO> filterValues = cimJpaRepository.findAll(Example.of(cimPostProcessFilterValueExam));
                    if (!CimObjectUtils.isEmpty(filterValues)) {
                        fipostProcessPatternID = filterValues.get(0).getPatternID();
                        bPatternFoundFlag = true;
                    } else {
                        log.info("Pattern is not found.");
                    }

                    if (bPatternFoundFlag) {
                        log.info("Pattern is found.");
                        break;
                    }
                }
            } else {
                log.info("The length strSearchInfo is 0. Skip additional pattern search");
            }
        } else {
            // If OCTRX.PATTERN_SEARCH=0. Skip pattern search
            log.info("Skip additional pattern search");
        }
        log.info("{} fipostProcessPatternID", fipostProcessPatternID);
        CimPostProcessPatternDO cimPostProcessPatternExam = new CimPostProcessPatternDO();
        cimPostProcessPatternExam.setPatternID(fipostProcessPatternID);
        String _fimmTranEventID = fimmTranEventID;
        List<Infos.PostProcessConfigInfo> patternInfoSequence = cimJpaRepository.findAll(Example.of(cimPostProcessPatternExam))
                .stream()
                .sorted(Comparator.comparing(CimPostProcessPatternDO::getSequenceNumber))
                .map(processPattern -> {
                    Infos.PostProcessConfigInfo postProcessConfigInfo = new Infos.PostProcessConfigInfo();
                    postProcessConfigInfo.setPatternID(processPattern.getPatternID());
                    postProcessConfigInfo.setSeqNo(processPattern.getSequenceNumber());
                    postProcessConfigInfo.setExecCondition(processPattern.getExecCondition());
                    postProcessConfigInfo.setTargetType(processPattern.getTargetType());
                    postProcessConfigInfo.setPostProcID(processPattern.getPostProcessID());
                    postProcessConfigInfo.setSyncFlag(processPattern.getSyncFlag());
                    postProcessConfigInfo.setErrorAction(processPattern.getErrorAction());
                    postProcessConfigInfo.setCreateTime(CimDateUtils.convertToSpecString(processPattern.getCreateTime()));
                    postProcessConfigInfo.setUpdateTime(CimDateUtils.convertToSpecString(processPattern.getUpdateTime()));
                    if (CimStringUtils.equals(postProcessConfigInfo.getPostProcID(), BizConstant.SP_POSTPROCESS_ACTIONID_EXTERNALPOSTPROCESSEXECREQ)) {
                        postProcessConfigInfo.setExtEventID(_fimmTranEventID);
                    }

                    //--------------------------------------------------------------------------
                    // Make a execCondition string. Parse execCondition string.
                    // For example,
                    // A string is "2+3".   ==> execConditionSeq[0]  <-- 2
                    //                      ==> execConditionSeq[1]  <-- 3
                    //--------------------------------------------------------------------------
                    List<Integer> execConditionSeq = new ArrayList<>();
                    postProcessConfigInfo.setExecConditionSeq(execConditionSeq);
                    String execCondition = processPattern.getExecCondition();
                    String[] exs = CimObjectUtils.isEmpty(execCondition) ? null : execCondition.split("\\+");
                    if (!CimObjectUtils.isEmpty(exs)) {
                        for (String ex : exs) {
                            execConditionSeq.add(Integer.valueOf(ex));
                        }
                    }
                    return postProcessConfigInfo;
                }).collect(Collectors.toList());
        Validations.check(CimObjectUtils.isEmpty(patternInfoSequence), retCodeConfig.getNotFoundEntry());
        return patternInfoSequence;
    }

    @Override
    public List<Infos.HashedInfo> postProcessPatternSearchConditionGet(Infos.ObjCommon objCommon, List<String> strCollectionInfoSeq, List<Infos.HashedInfo> strInputInfoSeq) {
        int nInputInfoLen = CimArrayUtils.getSize(strInputInfoSeq);
        int nItemLen = CimArrayUtils.getSize(strCollectionInfoSeq);

        //---------------------------//
        //  Display input parameter  //
        //---------------------------//
        // Collection Info

        List<Infos.HashedInfo> strCollectedSearchInfoSeq = new ArrayList<>();
        int nCnt = 0;

        // Define object identifier for famous base information
        ObjectIdentifier equipmentID = new ObjectIdentifier();

        //------------------------------------------------------------
        // Collect information
        //------------------------------------------------------------
        for (int i = 0; i < nItemLen; i++) {
            log.info("{} {} strCollectionInfoSeq[i]", i, strCollectionInfoSeq.get(i));
            if (CimStringUtils.equals(strCollectionInfoSeq.get(i), BizConstant.SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTTYPE)) {
                log.info("Get equipment type information.");
                //This "if statement" is reference for future expansion
                if (ObjectIdentifier.isEmpty(equipmentID)) {
                    log.info("equipmentID isn't set");
                    boolean bFoundFlag = false;
                    for (int j = 0; j < nInputInfoLen; j++) {
                        log.info("{} loop for j", j);
                        if (CimStringUtils.equals(strInputInfoSeq.get(j).getHashKey(), BizConstant.SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTID)
                                && !CimObjectUtils.isEmpty(strInputInfoSeq.get(j).getHashData())) {
                            log.info("{} EquipmnetID is found", strInputInfoSeq.get(j).getHashData());
                            equipmentID.setValue(strInputInfoSeq.get(j).getHashData());
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (!bFoundFlag) {
                        //Skip collecting this information
                        log.info("bFoundFlag is FALSE");
                        continue;
                    }
                }

                log.info("Call machine_type_Get()");
                Outputs.ObjMachineTypeGetOut machineTypeGet = equipmentMethod.machineTypeGet(objCommon, equipmentID);

                // Set Equipment Type to strCollectedSearchInfoSeq
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                strCollectedSearchInfoSeq.add(hashedInfo);
                hashedInfo.setHashKey(BizConstant.SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTTYPE);
                hashedInfo.setHashData(machineTypeGet.getEquipmentType());
                nCnt++;
            } else {
                log.info("Collection item is invalid.");
            }
        }
        return strCollectedSearchInfoSeq;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/13 18:09
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjPostProcessQueListDROut postProcessQueueListDR(Infos.ObjCommon objCommon, Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn) {
        Outputs.ObjPostProcessQueListDROut out = new Outputs.ObjPostProcessQueListDROut();

        String hvBuffer;
        String searchCondition;
        String hvTmpBuffer = "";
        String claimUserId = postProcessQueueListDRIn.getClaimUserId();
        Long seqNo = postProcessQueueListDRIn.getSeqNo();
        String key = postProcessQueueListDRIn.getKey();
        String postProcId = postProcessQueueListDRIn.getPostProcId();
        String txId = postProcessQueueListDRIn.getTxId();
        String targetType = postProcessQueueListDRIn.getTargetType();
        String watchDogName = postProcessQueueListDRIn.getWatchDogName();
        Long syncFlag = postProcessQueueListDRIn.getSyncFlag();
        String status = postProcessQueueListDRIn.getStatus();
        String startCreateTimeStamp = postProcessQueueListDRIn.getStartCreateTimeStamp();
        String endCreateTieStamp = postProcessQueueListDRIn.getEndCreateTieStamp();
        String startUpdateTimeStamp = postProcessQueueListDRIn.getStartUpdateTimeStamp();
        String endUpdateTimeStamp = postProcessQueueListDRIn.getEndUpdateTimeStamp();
        Long passedTime = postProcessQueueListDRIn.getPassedTime();
        Long maxCount = postProcessQueueListDRIn.getMaxCount();

        Infos.PostProcessTargetObject strPostProcessTargetObject = postProcessQueueListDRIn.getStrPostProcessTargetObject();
        ObjectIdentifier lotID = strPostProcessTargetObject.getLotID();
        ObjectIdentifier equipmentID = strPostProcessTargetObject.getEquipmentID();
        ObjectIdentifier controlJobID = strPostProcessTargetObject.getControlJobID();
        ObjectIdentifier cassetteID = strPostProcessTargetObject.getCassetteID();

        hvBuffer = "SELECT  LINK_KEY, " +
                "           IDX_NO, " +
                "           RUN_SEQ_CONDITION, " +
                "           SENTINEL_NAME, " +
                "           PPRC_ID, " +
                "           SYNC_FLAG, " +
                "           TRX_ID," +
                "           TARGET_OBJECT_TYPE, " +
                "           LOT_ID, " +
                "           LOT_RKEY, " +
                "           EQP_ID, " +
                "           EQP_RKEY, " +
                "           CJ_ID, " +
                "           CJ_RKEY, " +
                "           CARRIER_ID, " +
                "           CARRIER_RKEY, " +
                "           COMMIT_FLAG, " +
                "           PPRC_STATUS, " +
                "           SPLIT_COUNT, " +
                "           ERROR_ACTION, " +
                "           CREATE_TIME, " +
                "           UPDATE_TIME, " +
                "           TRX_USER_ID, " +
                "           TRX_TIME, " +
                "           TRX_MEMO, " +
                "           EXT_PPRC_ID " +
                "FROM       OQPPRC";

        // lotID
        if (!ObjectIdentifier.isEmptyWithValue(lotID)) {
            searchCondition = "";
            searchCondition += String.format(" NVL(LOT_ID, 0) LIKE '%s'", lotID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // equipmentID
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" NVL(EQP_ID, 0) LIKE '%s'", equipmentID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // controlJobID
        if (!ObjectIdentifier.isEmpty(controlJobID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" CJ_ID = '%s'", controlJobID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // cassetteID
        if (!ObjectIdentifier.isEmpty(cassetteID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" NVL(CARRIER_ID, 0) LIKE '%s'", cassetteID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // claimUserID
        if (CimStringUtils.length(claimUserId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TRX_USER_ID = '%s'", claimUserId);
            hvTmpBuffer += searchCondition;
        }
        // sequenceNumber
        if (seqNo > 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" IDX_NO = %d", seqNo);
            hvTmpBuffer += searchCondition;
        }
        // key
        if (CimStringUtils.length(key) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" LINK_KEY = '%s'", key);
            hvTmpBuffer += searchCondition;
        }
        // postProcID
        if (CimStringUtils.length(postProcId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" PPRC_ID = '%s'", postProcId);
            hvTmpBuffer += searchCondition;
        }
        // TxID
        if (CimStringUtils.length(txId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TRX_ID = '%s'", txId);
            hvTmpBuffer += searchCondition;
        }
        // targetType
        if (CimStringUtils.length(targetType) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TARGET_OBJECT_TYPE = '%s'", targetType);
            hvTmpBuffer += searchCondition;
        }
        // watchdogName
        if (CimStringUtils.length(watchDogName) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" SENTINEL_NAME = '%s'", watchDogName);
            hvTmpBuffer += searchCondition;
        }
        // syncFlag
        if (syncFlag > -1) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" SYNC_FLAG = %d", syncFlag);
            hvTmpBuffer += searchCondition;
        }
        // status
        if (CimStringUtils.length(status) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" PPRC_STATUS = '%s'", status);
            hvTmpBuffer += searchCondition;
        }
        // startCreateTimeStamp - endCreateTimeStamp
        if ((CimStringUtils.length(startCreateTimeStamp) != 0) &&
                CimStringUtils.length(endCreateTieStamp) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" '%s' <= CREATE_TIME ", startCreateTimeStamp);
            hvTmpBuffer += searchCondition;
            searchCondition = "";
            searchCondition += String.format(" AND CREATE_TIME <= '%s'", endCreateTieStamp);
            hvTmpBuffer += searchCondition;
        } else {
            if (CimStringUtils.length(startCreateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" '%s' <= CREATE_TIME ", startCreateTimeStamp);
                hvTmpBuffer += searchCondition;
            } else if (CimStringUtils.length(endCreateTieStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" AND CREATE_TIME <= '%s'", endCreateTieStamp);
                hvTmpBuffer += searchCondition;
            }
        }
        // startUpdateTimeStamp - endUpdateTimeStamp
        if ((CimStringUtils.length(startUpdateTimeStamp) != 0) &&
                CimStringUtils.length(endUpdateTimeStamp) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" '%s' <= UPDATE_TIME ", startUpdateTimeStamp);
            hvTmpBuffer += searchCondition;
            searchCondition = "";
            searchCondition += String.format(" AND UPDATE_TIME <= '%s'", endUpdateTimeStamp);
            hvTmpBuffer += searchCondition;
        } else {
            if (CimStringUtils.length(startUpdateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" '%s' <= UPDATE_TIME ", startUpdateTimeStamp);
                hvTmpBuffer += searchCondition;
            } else if (CimStringUtils.length(endUpdateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" AND UPDATE_TIME <= '%s'", endUpdateTimeStamp);
                hvTmpBuffer += searchCondition;
            }
        }
        // passed Time  1 - 9999min
        if (0 < passedTime && passedTime <= 9999) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" CURRENT TIMESTAMP >= CREATE_TIME + %d MINUTES", passedTime);
            hvTmpBuffer += searchCondition;
        }
        //-----------------------------
        // Deside Search Condition.
        //-----------------------------
        if (CimStringUtils.length(hvTmpBuffer) > 0) {
            hvBuffer += " WHERE ";
            hvBuffer += hvTmpBuffer;
        }
        // Order by
        hvBuffer += " ORDER BY LINK_KEY, IDX_NO";
        // maxCount todo
        List<CimPostProcessDO> query = cimJpaRepository.query(hvBuffer, CimPostProcessDO.class);
        //-----------------------------
        // Set return value.
        //-----------------------------
        List<Infos.PostProcessActionInfo> strActionInfoSeq = new ArrayList<>();
        for (CimPostProcessDO cimPostProcessDO : query) {
            Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
            strActionInfoSeq.add(postProcessActionInfo);
            postProcessActionInfo.setDKey(cimPostProcessDO.getDkey());
            postProcessActionInfo.setSequenceNumber(cimPostProcessDO.getSeqNo());
            postProcessActionInfo.setExecCondition(cimPostProcessDO.getExecCondition());
            postProcessActionInfo.setWatchDogName(cimPostProcessDO.getWatchDogName());
            postProcessActionInfo.setPostProcessID(cimPostProcessDO.getPostProcessID());
            postProcessActionInfo.setSyncFlag(cimPostProcessDO.getSyncFlag());
            postProcessActionInfo.setTransationID(cimPostProcessDO.getTransactionID());
            postProcessActionInfo.setTargetType(cimPostProcessDO.getTargetType());

            Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
            postProcessActionInfo.setPostProcessTargetObject(postProcessTargetObject);
            postProcessTargetObject.setLotID(ObjectIdentifier.build(cimPostProcessDO.getLotID(), cimPostProcessDO.getLotObj()));
            postProcessTargetObject.setEquipmentID(ObjectIdentifier.build(cimPostProcessDO.getEquipmentID(), cimPostProcessDO.getEquipmentObj()));
            postProcessTargetObject.setControlJobID(ObjectIdentifier.build(cimPostProcessDO.getControlJobID(), cimPostProcessDO.getControlJobObj()));
            postProcessTargetObject.setCassetteID(ObjectIdentifier.build(cimPostProcessDO.getCassetteID(), cimPostProcessDO.getCassetteObj()));
            postProcessActionInfo.setCommitFlag(cimPostProcessDO.getCommitFlag());
            postProcessActionInfo.setStatus(cimPostProcessDO.getStatus());
            postProcessActionInfo.setSplitCount(cimPostProcessDO.getSplitCount());
            postProcessActionInfo.setErrorAction(cimPostProcessDO.getErrorAction());
            postProcessActionInfo.setCreateTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getCreateTime()));
            postProcessActionInfo.setUpdateTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getUpdateTime()));
            postProcessActionInfo.setClaimUserID(cimPostProcessDO.getClaimUserID());
            postProcessActionInfo.setClaimTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getClaimTime()));
            postProcessActionInfo.setClaimMemo(cimPostProcessDO.getClaimMemo());
            postProcessActionInfo.setExtEventID(cimPostProcessDO.getExtEventID());
        }
        out.setStrActionInfoSeq(strActionInfoSeq);
        return out;
    }

    @Override
    public Outputs.ObjPostProcessPageQueListDROut postProcessQueueListDR(Infos.ObjCommon objCommon, Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn, SearchCondition searchConditions) {
        Outputs.ObjPostProcessPageQueListDROut out = new Outputs.ObjPostProcessPageQueListDROut();

        String hvBuffer;
        String searchCondition;
        String hvTmpBuffer = "";
        String claimUserId = postProcessQueueListDRIn.getClaimUserId();
        Long seqNo = postProcessQueueListDRIn.getSeqNo();
        String key = postProcessQueueListDRIn.getKey();
        String postProcId = postProcessQueueListDRIn.getPostProcId();
        String txId = postProcessQueueListDRIn.getTxId();
        String targetType = postProcessQueueListDRIn.getTargetType();
        String watchDogName = postProcessQueueListDRIn.getWatchDogName();
        Long syncFlag = postProcessQueueListDRIn.getSyncFlag();
        String status = postProcessQueueListDRIn.getStatus();
        String startCreateTimeStamp = postProcessQueueListDRIn.getStartCreateTimeStamp();
        String endCreateTieStamp = postProcessQueueListDRIn.getEndCreateTieStamp();
        String startUpdateTimeStamp = postProcessQueueListDRIn.getStartUpdateTimeStamp();
        String endUpdateTimeStamp = postProcessQueueListDRIn.getEndUpdateTimeStamp();
        Long passedTime = postProcessQueueListDRIn.getPassedTime();
        Long maxCount = postProcessQueueListDRIn.getMaxCount();

        Infos.PostProcessTargetObject strPostProcessTargetObject = postProcessQueueListDRIn.getStrPostProcessTargetObject();
        ObjectIdentifier lotID = strPostProcessTargetObject.getLotID();
        ObjectIdentifier equipmentID = strPostProcessTargetObject.getEquipmentID();
        ObjectIdentifier controlJobID = strPostProcessTargetObject.getControlJobID();
        ObjectIdentifier cassetteID = strPostProcessTargetObject.getCassetteID();

        hvBuffer = "SELECT  LINK_KEY, " +
                "           IDX_NO, " +
                "           RUN_SEQ_CONDITION, " +
                "           SENTINEL_NAME, " +
                "           PPRC_ID, " +
                "           SYNC_FLAG, " +
                "           TRX_ID," +
                "           TARGET_OBJECT_TYPE, " +
                "           LOT_ID, " +
                "           LOT_RKEY, " +
                "           EQP_ID, " +
                "           EQP_RKEY, " +
                "           CJ_ID, " +
                "           CJ_RKEY, " +
                "           CARRIER_ID, " +
                "           CARRIER_RKEY, " +
                "           COMMIT_FLAG, " +
                "           PPRC_STATUS, " +
                "           SPLIT_COUNT, " +
                "           ERROR_ACTION, " +
                "           CREATE_TIME, " +
                "           UPDATE_TIME, " +
                "           TRX_USER_ID, " +
                "           TRX_TIME, " +
                "           TRX_MEMO, " +
                "           EXT_PPRC_ID " +
                "FROM       OQPPRC";

        // lotID
        if (!ObjectIdentifier.isEmptyWithValue(lotID)) {
            searchCondition = "";
            searchCondition += String.format(" NVL(LOT_ID, 0) LIKE '%s'", lotID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // equipmentID
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" NVL(EQP_ID, 0) LIKE '%s'", equipmentID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // controlJobID
        if (!ObjectIdentifier.isEmpty(controlJobID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" CJ_ID = '%s'", controlJobID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // cassetteID
        if (!ObjectIdentifier.isEmpty(cassetteID)) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" NVL(CARRIER_ID, 0) LIKE '%s'", cassetteID.getValue());
            hvTmpBuffer += searchCondition;
        }
        // claimUserID
        if (CimStringUtils.length(claimUserId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TRX_USER_ID = '%s'", claimUserId);
            hvTmpBuffer += searchCondition;
        }
        // sequenceNumber
        if (seqNo > 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" IDX_NO = %d", seqNo);
            hvTmpBuffer += searchCondition;
        }
        // key
        if (CimStringUtils.length(key) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" LINK_KEY = '%s'", key);
            hvTmpBuffer += searchCondition;
        }
        // postProcID
        if (CimStringUtils.length(postProcId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" PPRC_ID = '%s'", postProcId);
            hvTmpBuffer += searchCondition;
        }
        // TxID
        if (CimStringUtils.length(txId) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TRX_ID = '%s'", txId);
            hvTmpBuffer += searchCondition;
        }
        // targetType
        if (CimStringUtils.length(targetType) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" TARGET_OBJECT_TYPE = '%s'", targetType);
            hvTmpBuffer += searchCondition;
        }
        // watchdogName
        if (CimStringUtils.length(watchDogName) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" SENTINEL_NAME = '%s'", watchDogName);
            hvTmpBuffer += searchCondition;
        }
        // syncFlag
        if (syncFlag > -1) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" SYNC_FLAG = %d", syncFlag);
            hvTmpBuffer += searchCondition;
        }
        // status
        if (CimStringUtils.length(status) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" PPRC_STATUS = '%s'", status);
            hvTmpBuffer += searchCondition;
        }
        // startCreateTimeStamp - endCreateTimeStamp
        if ((CimStringUtils.length(startCreateTimeStamp) != 0) &&
                CimStringUtils.length(endCreateTieStamp) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" '%s' <= CREATE_TIME ", startCreateTimeStamp);
            hvTmpBuffer += searchCondition;
            searchCondition = "";
            searchCondition += String.format(" AND CREATE_TIME <= '%s'", endCreateTieStamp);
            hvTmpBuffer += searchCondition;
        } else {
            if (CimStringUtils.length(startCreateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" '%s' <= CREATE_TIME ", startCreateTimeStamp);
                hvTmpBuffer += searchCondition;
            } else if (CimStringUtils.length(endCreateTieStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" AND CREATE_TIME <= '%s'", endCreateTieStamp);
                hvTmpBuffer += searchCondition;
            }
        }
        // startUpdateTimeStamp - endUpdateTimeStamp
        if ((CimStringUtils.length(startUpdateTimeStamp) != 0) &&
                CimStringUtils.length(endUpdateTimeStamp) != 0) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" '%s' <= UPDATE_TIME ", startUpdateTimeStamp);
            hvTmpBuffer += searchCondition;
            searchCondition = "";
            searchCondition += String.format(" AND UPDATE_TIME <= '%s'", endUpdateTimeStamp);
            hvTmpBuffer += searchCondition;
        } else {
            if (CimStringUtils.length(startUpdateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" '%s' <= UPDATE_TIME ", startUpdateTimeStamp);
                hvTmpBuffer += searchCondition;
            } else if (CimStringUtils.length(endUpdateTimeStamp) != 0) {
                if (CimStringUtils.length(hvTmpBuffer) > 0) {
                    hvTmpBuffer += " AND ";
                }
                searchCondition = "";
                searchCondition += String.format(" AND UPDATE_TIME <= '%s'", endUpdateTimeStamp);
                hvTmpBuffer += searchCondition;
            }
        }
        // passed Time  1 - 9999min
        if (0 < passedTime && passedTime <= 9999) {
            if (CimStringUtils.length(hvTmpBuffer) > 0) {
                hvTmpBuffer += " AND ";
            }
            searchCondition = "";
            searchCondition += String.format(" CURRENT TIMESTAMP >= CREATE_TIME + %d MINUTES", passedTime);
            hvTmpBuffer += searchCondition;
        }
        //-----------------------------
        // Deside Search Condition.
        //-----------------------------
        if (CimStringUtils.length(hvTmpBuffer) > 0) {
            hvBuffer += " WHERE ";
            hvBuffer += hvTmpBuffer;
        }
        // Order by
        hvBuffer += " ORDER BY LINK_KEY, IDX_NO";
        // maxCount todo
        Page<CimPostProcessDO> query = cimJpaRepository.query(hvBuffer, CimPostProcessDO.class,searchConditions);
        //-----------------------------
        // Set return value.
        //-----------------------------
        List<Infos.PostProcessActionInfo> strActionInfoSeq = new ArrayList<>();
        for (CimPostProcessDO cimPostProcessDO : query) {
            Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
            strActionInfoSeq.add(postProcessActionInfo);
            postProcessActionInfo.setDKey(cimPostProcessDO.getDkey());
            postProcessActionInfo.setSequenceNumber(cimPostProcessDO.getSeqNo());
            postProcessActionInfo.setExecCondition(cimPostProcessDO.getExecCondition());
            postProcessActionInfo.setWatchDogName(cimPostProcessDO.getWatchDogName());
            postProcessActionInfo.setPostProcessID(cimPostProcessDO.getPostProcessID());
            postProcessActionInfo.setSyncFlag(cimPostProcessDO.getSyncFlag());
            postProcessActionInfo.setTransationID(cimPostProcessDO.getTransactionID());
            postProcessActionInfo.setTargetType(cimPostProcessDO.getTargetType());

            Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
            postProcessActionInfo.setPostProcessTargetObject(postProcessTargetObject);
            postProcessTargetObject.setLotID(ObjectIdentifier.build(cimPostProcessDO.getLotID(), cimPostProcessDO.getLotObj()));
            postProcessTargetObject.setEquipmentID(ObjectIdentifier.build(cimPostProcessDO.getEquipmentID(), cimPostProcessDO.getEquipmentObj()));
            postProcessTargetObject.setControlJobID(ObjectIdentifier.build(cimPostProcessDO.getControlJobID(), cimPostProcessDO.getControlJobObj()));
            postProcessTargetObject.setCassetteID(ObjectIdentifier.build(cimPostProcessDO.getCassetteID(), cimPostProcessDO.getCassetteObj()));
            postProcessActionInfo.setCommitFlag(cimPostProcessDO.getCommitFlag());
            postProcessActionInfo.setStatus(cimPostProcessDO.getStatus());
            postProcessActionInfo.setSplitCount(cimPostProcessDO.getSplitCount());
            postProcessActionInfo.setErrorAction(cimPostProcessDO.getErrorAction());
            postProcessActionInfo.setCreateTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getCreateTime()));
            postProcessActionInfo.setUpdateTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getUpdateTime()));
            postProcessActionInfo.setClaimUserID(cimPostProcessDO.getClaimUserID());
            postProcessActionInfo.setClaimTime(CimDateUtils.convertToSpecString(cimPostProcessDO.getClaimTime()));
            postProcessActionInfo.setClaimMemo(cimPostProcessDO.getClaimMemo());
            postProcessActionInfo.setExtEventID(cimPostProcessDO.getExtEventID());
        }
        out.setStrActionInfoSeq(new PageImpl<>(strActionInfoSeq,query.getPageable(),query.getTotalElements()));
        return out;
    }

    private Infos.PostProcessActionInfo setPostProcessActionInfo(CimPostProcessDO processs) {
        Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
        postProcessActionInfo.setDKey(processs.getDkey());
        postProcessActionInfo.setSequenceNumber(processs.getSeqNo());
        postProcessActionInfo.setExecCondition(processs.getExecCondition());
        postProcessActionInfo.setWatchDogName(processs.getWatchDogName());
        postProcessActionInfo.setPostProcessID(processs.getPostProcessID());
        postProcessActionInfo.setSyncFlag(processs.getSyncFlag());
        postProcessActionInfo.setTransationID(processs.getTransactionID());
        postProcessActionInfo.setTargetType(processs.getTargetType());
        postProcessActionInfo.setCommitFlag(processs.getCommitFlag());
        postProcessActionInfo.setStatus(processs.getStatus());
        postProcessActionInfo.setSplitCount(processs.getSplitCount());
        postProcessActionInfo.setErrorAction(processs.getErrorAction());
        postProcessActionInfo.setCreateTime(String.valueOf(processs.getCreateTime()));
        postProcessActionInfo.setUpdateTime(String.valueOf(processs.getUpdateTime()));
        postProcessActionInfo.setClaimUserID(processs.getClaimUserID());
        postProcessActionInfo.setClaimTime(String.valueOf(processs.getClaimTime()));
        postProcessActionInfo.setClaimMemo(processs.getClaimMemo());
        postProcessActionInfo.setExtEventID(processs.getExtEventID());

        Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
        postProcessTargetObject.setLotID(new ObjectIdentifier(processs.getLotID(), processs.getLotObj()));
        postProcessTargetObject.setEquipmentID(new ObjectIdentifier(processs.getEquipmentID(), processs.getEquipmentObj()));
        postProcessTargetObject.setControlJobID(new ObjectIdentifier(processs.getControlJobID(), processs.getControlJobObj()));
        postProcessTargetObject.setCassetteID(new ObjectIdentifier(processs.getCassetteID(), processs.getCassetteObj()));
        postProcessActionInfo.setPostProcessTargetObject(postProcessTargetObject);

        return postProcessActionInfo;
    }

    @Override
    public Outputs.ObjPostProcessQueueParallelExecCheckOut postProcessQueueParallelExecCheck(Infos.ObjCommon objCommon, Inputs.PostProcessQueueParallelExecCheckIn postProcessQueueParallelExecCheckIn) {
        Outputs.ObjPostProcessQueueParallelExecCheckOut objPostProcessQueueParallelExecCheckOut = new Outputs.ObjPostProcessQueueParallelExecCheckOut();
        String dkey = postProcessQueueParallelExecCheckIn.getDKey();
        boolean bCountCheck = postProcessQueueParallelExecCheckIn.isLotCountCheckFlag();
        //---------------------------------
        // Initialize return value
        //---------------------------------
        objPostProcessQueueParallelExecCheckOut.setParallelExecFlag(false);
        int PostProcParallelFlag = StandardProperties.OM_PP_PARALLEL_ENABLE.getIntValue();
        if (PostProcParallelFlag == 0) {
            return objPostProcessQueueParallelExecCheckOut;
        }
        List<ObjectIdentifier> targetLots = new ArrayList<>();
        int nPostProcLen = CimArrayUtils.getSize(postProcessQueueParallelExecCheckIn.getPostProcessActionInfoList());
        Validations.check(CimStringUtils.isEmpty(dkey) && nPostProcLen == 0, retCodeConfig.getInvalidParameter());
        List<Infos.PostProcessActionInfo> tmpActionInfoList = null;
        if (nPostProcLen > 0) {
            tmpActionInfoList = postProcessQueueParallelExecCheckIn.getPostProcessActionInfoList();
        } else {
            Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
            postProcessQueueListDRIn.setKey(dkey);
            postProcessQueueListDRIn.setSeqNo((long) -1);
            postProcessQueueListDRIn.setSyncFlag((long) -1);
            postProcessQueueListDRIn.setPassedTime((long) -1);
            postProcessQueueListDRIn.setMaxCount((long) -1);
            Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROutRetCode = postProcessQueueListDR(objCommon, postProcessQueueListDRIn);
            tmpActionInfoList = objPostProcessQueListDROutRetCode.getStrActionInfoSeq();
        }
        boolean bSequentialSyncFlagFound = false;
        boolean bParallelSyncFlagFound = false;
        nPostProcLen = CimArrayUtils.getSize(tmpActionInfoList);
        if (nPostProcLen > 0) {
            for (Infos.PostProcessActionInfo postProcessActionInfo : tmpActionInfoList) {
                if (postProcessActionInfo.getSyncFlag() == BizConstant.SP_PostProcess_SyncFlag_Async_Sequential) {
                    bSequentialSyncFlagFound = true;
                    break;
                } else if (postProcessActionInfo.getSyncFlag() == BizConstant.SP_PostProcess_SyncFlag_Async_Parallel) {
                    bParallelSyncFlagFound = true;
                } else {
                    //Not target for synchronous execution
                    continue;
                }
                if (bCountCheck) {
                    if (!ObjectIdentifier.isEmptyWithValue(postProcessActionInfo.getPostProcessTargetObject().getLotID())) {
                        boolean bLotFound = false;
                        for (ObjectIdentifier targetLot : targetLots) {
                            if (ObjectIdentifier.equalsWithValue(postProcessActionInfo.getPostProcessTargetObject().getLotID(), targetLot)) {
                                bLotFound = true;
                                break;
                            }
                        }
                        if (!bLotFound) {
                            targetLots.add(postProcessActionInfo.getPostProcessTargetObject().getLotID());
                        }
                    }
                }
            }
        }
        if (bParallelSyncFlagFound && bSequentialSyncFlagFound) {
            if (bCountCheck) {
                int nLotThreshold = StandardProperties.OM_PP_PARALLEL_LOT_LIMIT.getIntValue();
                if (targetLots.size() > nLotThreshold) {
                    objPostProcessQueueParallelExecCheckOut.setParallelExecFlag(true);
                }
            } else {
                objPostProcessQueueParallelExecCheckOut.setParallelExecFlag(true);
            }
        }
        return objPostProcessQueueParallelExecCheckOut;
    }

    @Override
    public boolean postProcessLastFlagForCarrierGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID) {
        //--------------------------------------------------
        // COUNT remaning Queue for all lots in same carrier
        //--------------------------------------------------
        String sql = String.format("SELECT COUNT(T1.LOT_ID)\n" +
                "             FROM\n" +
                "                (SELECT C.LOT_ID,\n" +
                "                        MIN(C.IDX_NO) AS SEQ_NO\n" +
                "                 FROM   OMCARRIER A,\n" +
                "                        OMCARRIER_LOT B,\n" +
                "                        OQPPRC C\n" +
                "                 WHERE  A.CARRIER_ID        = '%s'\n" +
                "                   AND  A.ID = B.REFKEY\n" +
                "                   AND  B.LOT_ID         = C.LOT_ID\n" +
                "                 GROUP BY C.LOT_ID ) AS T1, OQPPRC D\n" +
                "             WHERE T1.LOT_ID = D.LOT_ID\n" +
                "               AND T1.SEQ_NO = D.IDX_NO\n" +
                "               AND D.PPRC_ID <> '%s'", carrierID.getValue(), BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER);
        long count = cimJpaRepository.count(sql);
        return count > 0;
    }

    @Override
    public List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoGetDR(Infos.ObjCommon objCommon, String dKey, long seqNo) {
        CimPostProcessInfoDO cimPostProcessInfoExam = new CimPostProcessInfoDO();
        cimPostProcessInfoExam.setDkey(dKey);
        if (seqNo != -1) {
            cimPostProcessInfoExam.setSequenceNumber(CimNumberUtils.intValue(seqNo));
        }
        List<CimPostProcessInfoDO> cimPostProcessInfoDOList = cimJpaRepository.findAll(Example.of(cimPostProcessInfoExam));
        return cimPostProcessInfoDOList.stream()
                .map(data -> {
                    Infos.PostProcessAdditionalInfo info = new Infos.PostProcessAdditionalInfo();
                    info.setDKey(dKey);
                    info.setSequenceNumber(data.getSequenceNumber());
                    info.setName(data.getName());
                    info.setValue(data.getValue());
                    return info;
                }).collect(Collectors.toList());
    }

    @Override
    public List<String> postProcessRelatedQueueKeyGetDR(Infos.ObjCommon objCommon, String key) {
        CimPostProcessInfoDO cimPostProcessInfoExam = new CimPostProcessInfoDO();
        cimPostProcessInfoExam.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
        cimPostProcessInfoExam.setValue(key);
        cimPostProcessInfoExam.setSequenceNumber(0);
        return cimJpaRepository.findAll(Example.of(cimPostProcessInfoExam)).stream()
                .map(CimPostProcessInfoDO::getDkey)
                .collect(Collectors.toList());
    }

    @Override
    public void postProcessFilterRegistCheckDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID) {
        long count = 0;
        if (CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_PRODUCTSPEC)) {
            count = cimJpaRepository.count("SELECT COUNT(*) FROM OMPRODINFO WHERE PROD_ID = ?1", ObjectIdentifier.fetchValue(objectID));
        } else if (CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_PRODUCTGROUP)) {
            count = cimJpaRepository.count("SELECT COUNT(*) FROM OMPRODFMLY WHERE PRODFMLY_ID = ?1", ObjectIdentifier.fetchValue(objectID));
        } else if (CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_TECHNOLOGY)) {
            count = cimJpaRepository.count("SELECT COUNT(*) FROM OMTECH WHERE TECH_ID = ?1", ObjectIdentifier.fetchValue(objectID));
        } else if (CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_LOT)) {
            count = cimJpaRepository.count("SELECT COUNT(*) FROM OMLOT WHERE LOT_ID = ?1", ObjectIdentifier.fetchValue(objectID));
        } else {
            Validations.check(true, retCodeConfigEx.getInvalidObjectType(), objectType);
        }
        Validations.check(count == 0, retCodeConfigEx.getNotFoundObjectId(), objectType);
    }

    @Override
    public void postProcessFilterInsertDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID) {
        CimExternalPostProcessProductFilterDO example = new CimExternalPostProcessProductFilterDO();
        example.setObjectID(ObjectIdentifier.fetchValue(objectID));
        example.setObjectType(objectType);
        CimExternalPostProcessProductFilterDO externalPostProcessProductFilter = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        Validations.check(Objects.nonNull(externalPostProcessProductFilter), retCodeConfigEx.getDuplicatePostprocFlt(), objectID, objectType);

        externalPostProcessProductFilter = new CimExternalPostProcessProductFilterDO();
        externalPostProcessProductFilter.setObjectID(ObjectIdentifier.fetchValue(objectID));
        externalPostProcessProductFilter.setObjectType(objectType);
        externalPostProcessProductFilter.setClaimUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        externalPostProcessProductFilter.setClaimTime(objCommon.getTimeStamp().getReportTimeStamp());
        cimJpaRepository.save(externalPostProcessProductFilter);
    }

    @Override
    public void postProcessFilterDeleteDR(Infos.ObjCommon objCommon, List<Infos.ExternalPostProcessFilterInfo> externalPostProcessFilterInfos) {
        externalPostProcessFilterInfos.forEach(info -> {
            CimExternalPostProcessProductFilterDO example = new CimExternalPostProcessProductFilterDO();
            example.setObjectID(ObjectIdentifier.fetchValue(info.getObjectID()));
            example.setObjectType(info.getObjectType());
            cimJpaRepository.delete(Example.of(example));
        });
    }

    @Override
    public List<Infos.ExternalPostProcessFilterInfo> postProcessFilterGetDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID, ObjectIdentifier userID) {
        CimExternalPostProcessProductFilterDO example = new CimExternalPostProcessProductFilterDO();
        if (CimStringUtils.isNotEmpty(objectType)) {
            example.setObjectType(objectType);
        }
        if (!ObjectIdentifier.isEmpty(objectID)) {
            example.setObjectID(ObjectIdentifier.fetchValue(objectID));
        }
        if (!ObjectIdentifier.isEmpty(userID)) {
            example.setClaimUserID(ObjectIdentifier.fetchValue(userID));
        }
        return cimJpaRepository.findAll(Example.of(example)).stream().map(filter -> {
            Infos.ExternalPostProcessFilterInfo filterInfo = new Infos.ExternalPostProcessFilterInfo();
            filterInfo.setObjectID(new ObjectIdentifier(filter.getObjectID()));
            filterInfo.setObjectType(filter.getObjectType());
            filterInfo.setClaimUserID(new ObjectIdentifier(filter.getClaimUserID()));
            filterInfo.setClaimTime(String.valueOf(filter.getClaimTime()));
            return filterInfo;
        }).collect(Collectors.toList());
    }


    @Override
    public Outputs.DurablePostProcessQueueMakeOut durablePostProcessQueueMake(Infos.ObjCommon objCommon, Inputs.DurablePostProcessQueueMakeIn paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        Outputs.DurablePostProcessQueueMakeOut retVal = new Outputs.DurablePostProcessQueueMakeOut();

        Infos.DurablePostProcessRegistrationParm registrationParm = paramIn.getStrDurablePostProcessRegistrationParm();
        assert null != registrationParm;

        //------------------------------------------------------------
        // Get Post Process Pattern Information by PatternID / TX_ID
        //------------------------------------------------------------
        log.info("Get Post Process Pattern Information.");
        List<Infos.HashedInfo> strSearchInfoSeq = new ArrayList<>();
        if (ObjectIdentifier.isNotEmptyWithValue(registrationParm.getEquipmentID())) {
            Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey(BizConstant.SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTID);
            hashedInfo.setHashData(ObjectIdentifier.fetchValue(registrationParm.getEquipmentID()));
            strSearchInfoSeq.add(hashedInfo);
        }
        long ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getLongValue();
        if (ppChainMode == 1) {
            String strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
            if (CimStringUtils.isEmpty(strTriggerDKey)) {
                ppChainMode = 0;
            }
        }
        if (ppChainMode == 1) {
            Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
            hashedInfo.setHashKey(BizConstant.SP_POSTPROCESS_SEARCH_KEY_TRIGGERTXID);
            hashedInfo.setHashData(objCommon.getTransactionID());
            strSearchInfoSeq.add(hashedInfo);
        }
        List<Infos.PostProcessConfigInfo> patternInfoSeq = this.postProcessConfigGetPatternInfoDR(objCommon,
                paramIn.getTxID(),
                paramIn.getPatternID(),
                strSearchInfoSeq);
        if (CimArrayUtils.isEmpty(patternInfoSeq)) {
            retVal.setDKey(paramIn.getKey());
            log.info("Pattern information is not found.");
            return retVal;
        }

        String dKey = paramIn.getKey();
        Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
        if (CimStringUtils.isEmpty(dKey)) {
            //-------------------------------------------------
            //Generate dKey
            //-------------------------------------------------
            dKey = String.format("%s+%s", CimDateUtils.convertToSpecString(currentTimeStamp), objCommon.getUser().getUserID().getValue());
        }

        AtomicLong seqNo = new AtomicLong(paramIn.getSeqNo());
        if (seqNo.get() <= 0) {
            seqNo.set(1);
        }

        //-------------------------------------------------
        //Preparing Action Informations
        //-------------------------------------------------
        List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();

        //-------------------------------------------------
        //Preparing Q-item (1)
        //-------------------------------------------------
        Infos.PostProcessActionInfo previousQItem = new Infos.PostProcessActionInfo();
        previousQItem.setDKey(dKey);
        previousQItem.setTransationID(objCommon.getUser().getFunctionID());
        previousQItem.setClaimUserID(objCommon.getUser().getUserID().getValue());
        previousQItem.setClaimTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
        previousQItem.setCreateTime(currentTimeStamp.toString());
        previousQItem.setUpdateTime(currentTimeStamp.toString());
        previousQItem.setClaimMemo(paramIn.getClaimMemo());
        previousQItem.setClaimShopDate(objCommon.getTimeStamp().getReportShopDate());

        Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
        postProcessTargetObject.setEquipmentID(registrationParm.getEquipmentID());
        postProcessTargetObject.setControlJobID(registrationParm.getDurableControlJobID());
        previousQItem.setPostProcessTargetObject(postProcessTargetObject);

        List<ObjectIdentifier> durableIDs = registrationParm.getDurableIDs();
        String durableCategory = registrationParm.getDurableCategory();

        List<ObjectIdentifier> tmpDurableIDs = new ArrayList<>();
        List<Integer> tmpDurableIndexTable = new ArrayList<>();
        List<ObjectIdentifier> tmpABSDurableIDs = new ArrayList<>();
        List<Integer> tmpABSDurableIndexTable = new ArrayList<>();
        int durableLen = durableIDs.size();
        for (int index = 0; index < durableLen; index++) {
            ObjectIdentifier durableID = durableIDs.get(index);
            Durable aDurable = null;
            switch (durableCategory) {
                case SP_DURABLECAT_CASSETTE:
                    aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                    break;
                case SP_DURABLECAT_RETICLE:
                    aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                    break;
            }
            Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());
            durableID.setValue(aDurable.getIdentifier());
            durableID.setReferenceKey(aDurable.getPrimaryKey());

            //-------------------------------------------------
            // Check durable state
            //-------------------------------------------------
            String durableState = durableMethod.durableStateGet(objCommon, durableCategory, durableID);
            if (!CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_AVAILABLE)
                    && !CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_NOTAVAILABLE)) {
                continue;
            }

            //-------------------------------------------------
            // Check Process State "Waiting"
            //-------------------------------------------------
            String durableProcessState = durableMethod.durableProcessStateGet(objCommon, durableCategory, durableID);
            if (!CimStringUtils.equals(durableProcessState, BizConstant.SP_DURABLE_PROCSTATE_WAITING)
                    && CimStringUtils.isNotEmpty(durableProcessState)) {
                continue;
            }

            tmpABSDurableIndexTable.add(index);
            tmpABSDurableIDs.add(durableID);

            //-------------------------------------------------
            //Check durable hold state
            //-------------------------------------------------
            String durableHoldState = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
            if (CimStringUtils.equals(durableHoldState, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                continue;
            }

            //-------------------------------------------------
            //  Check Lot's InPostProcessFlag
            //-------------------------------------------------
            Boolean isPostProcessFlagOn = durableMethod.durableInPostProcessFlagGet(objCommon, durableCategory, durableID);
            if (isPostProcessFlagOn) {
                continue;   //Durable is in post process ... NG (Skip)
            }
            tmpDurableIndexTable.add(index);
            tmpDurableIDs.add(durableID);
        }

        //-----------------------------------
        //  Verify exec condition
        //-----------------------------------
        Optional.of(patternInfoSeq).ifPresent(list -> list.forEach(postProcessConfigInfo -> {
            List<Integer> execConditionSeq = postProcessConfigInfo.getExecConditionSeq();
            Optional.ofNullable(execConditionSeq).ifPresent(conditionSeqs -> conditionSeqs.forEach(seq -> {
                if (0 >= seq || seq.equals(postProcessConfigInfo.getSeqNo())) {
                    log.error("### Invalid execCondition was found. Set execCondition null. execCondition = " + seq);
                    Validations.check(retCodeConfig.getInvalidExecCondition(), seq);
                }

                boolean foundFlag = false;
                for (Infos.PostProcessConfigInfo configInfo : patternInfoSeq) {
                    if (seq.equals(configInfo.getSeqNo())) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    log.error("### Invalid execCondition was found. Set execCondition null. execCondition = " + seq);
                    Validations.check(retCodeConfig.getInvalidExecCondition(), seq);
                }

            }));
        }));

        int nPatternMax = patternInfoSeq.get(patternInfoSeq.size() - 1).getSeqNo();
        List<List<Long>> seqNumberTable = new ArrayList<>(nPatternMax);
        for (int i = 0; i < nPatternMax; i++) {
            seqNumberTable.add(i, new ArrayList<>());
        }

        Optional.of(patternInfoSeq).ifPresent(list -> list.forEach(patternInfo -> {
            String tmpStatus = patternInfo.getSyncFlag() == 0 ? BizConstant.SP_POSTPROCESS_STATE_WAITING : BizConstant.SP_POSTPROCESS_STATE_RESERVED;

            //-------------------------------------------------
            //Preparing Q-item (2)
            //-------------------------------------------------
            previousQItem.setWatchDogName(BizConstant.SP_POSTPROCESS_WATCHDOGNAME_DEFAULT);
            previousQItem.setTargetType(patternInfo.getTargetType());
            previousQItem.setPostProcessID(patternInfo.getPostProcID());
            previousQItem.setSyncFlag(patternInfo.getSyncFlag());
            previousQItem.setCommitFlag(true);
            previousQItem.setStatus(tmpStatus);
            previousQItem.setSplitCount(0);
            previousQItem.setErrorAction(patternInfo.getErrorAction());

            previousQItem.setExecCondition("");

            // example: Durable x2 CRUP001, CRUP002
            //
            // Pattern  [1]={ TARGET_TYPE="CAST-ABSOLUTE", ID="DProcessLagTime",          EXEC_CONDITION=""  }
            //          [2]={ TARGET_TYPE="CAST-ABSOLUTE", ID="DScript",                  EXEC_CONDITION="1" }
            //          [3]={ TARGET_TYPE="CAST-ABSOLUTE", ID="DAutoBankIn",              EXEC_CONDITION="2" }
            //          [4]={ TARGET_TYPE="EQP",           ID="MessageQueuePut",          EXEC_CONDITION="3" }
            //
            // Queue    [1]={ LOT_ID="CRUP001",    ID="DProcessLagTime",          EXEC_CONDITION=""    }
            //          [2]={ LOT_ID="CRUP002",    ID="DProcessLagTime",          EXEC_CONDITION=""    }
            //          [3]={ LOT_ID="CRUP001",    ID="DScript",                  EXEC_CONDITION="1"   }
            //          [4]={ LOT_ID="CRUP002",    ID="DScript",                  EXEC_CONDITION="2"   }
            //          [5]={ LOT_ID="CRUP001",    ID="DAutoBankIn",              EXEC_CONDITION="3"   }
            //          [6]={ LOT_ID="CRUP002",    ID="DAutoBankIn",              EXEC_CONDITION="4"   }
            //          [7]={ EQP_ID="EQP-A",      ID="MessageQueuePut",          EXEC_CONDITION="5+6" }
            //
            // seqNumberTable[seqNo-1] = [1 -1] = [0,1,2]
            //                           [2 -1] = [0,3,4]
            //                           [3 -1] = [0,5,6]
            //                           [4 -1] = [7]

            String targetType = patternInfo.getTargetType();
            switch (targetType) {
                //--------------------------------------------------------------------------------------
                // targetType = Cast
                //--------------------------------------------------------------------------------------
                case SP_POSTPROCESS_TARGETTYPE_CAST:
                    log.info("targetType == SP_PostProcess_TargetType_CAST.");
                    if (durableLen == 0) {
                        Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                    }

                    // init seqNumberTable
                    for (int j = 0; j < durableLen + 1; j++) {
                        seqNumberTable.get(patternInfo.getSeqNo() - 1).add(0L);
                    }

                    int tmpDurableLength = tmpDurableIDs.size();
                    for (int j = 0; j < tmpDurableLength; j++) {
                        /*--------------------------------*/
                        /* Clone a new Object.            */
                        /*--------------------------------*/
                        Infos.PostProcessActionInfo newQItem = previousQItem.clone();
                        List<Long> execConditionList = new ArrayList<>();
                        newQItem.setExecConditionList(execConditionList);

                        ObjectIdentifier durableID = tmpDurableIDs.get(j);
                        //-------------------------------------------------
                        //exec condition
                        //-------------------------------------------------
                        int conditionSize = CimArrayUtils.getSize(patternInfo.getExecConditionSeq());
                        for (int k = 0; k < conditionSize; k++) {
                            Integer tmpSeqNo = patternInfo.getExecConditionSeq().get(k);
                            if (tmpSeqNo <= 0 && nPatternMax < tmpSeqNo) {
                                Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                            }

                            if (seqNumberTable.get(tmpSeqNo - 1).size() > 1) {
                                //Wait for same lot's post process only.
                                execConditionList.add(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpDurableIndexTable.get(j)));
                            } else {
                                //eqp / cj
                                execConditionList.add(seqNumberTable.get(tmpSeqNo - 1).get(0));
                            }
                        }

                        //-------------------------------------------------
                        //update seqNumberTable
                        //-------------------------------------------------
                        seqNumberTable.get(patternInfo.getSeqNo() - 1).set(1 + tmpDurableIndexTable.get(j), seqNo.get());

                        //-------------------------------------------------
                        //Preparing Q-item 3
                        //-------------------------------------------------
                        Infos.PostProcessTargetObject processTargetObject = new Infos.PostProcessTargetObject();
                        processTargetObject.setCassetteID(durableID);
                        newQItem.setPostProcessTargetObject(processTargetObject);
                        newQItem.setSequenceNumber(CimNumberUtils.intValue(seqNo.getAndIncrement()));

                        actionInfoSeq.add(newQItem);
                    }
                    break;
                //--------------------------------------------------------------------------------------
                // targetType = CAST-ABSOLUTE
                //--------------------------------------------------------------------------------------
                case SP_POSTPROCESS_TARGETTYPE_CAST_ABSOLUTE:
                    log.info("targetType == SP_PostProcess_TargetType_CAST_ABSOLUTE.");
                    if (durableLen == 0) {
                        Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                    }

                    //replace targetType -> "CAST"
                    previousQItem.setTargetType(BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST);

                    // init seqNumberTable
                    for (int j = 0; j < durableLen + 1; j++) {
                        seqNumberTable.get(patternInfo.getSeqNo() - 1).add(0L);
                    }

                    int size = tmpABSDurableIDs.size();
                    for (int j = 0; j < size; j++) {
                        /*--------------------------------*/
                        /* Clone a new Object.            */
                        /*--------------------------------*/
                        Infos.PostProcessActionInfo newQItem = previousQItem.clone();
                        List<Long> execConditionList = new ArrayList<>();
                        newQItem.setExecConditionList(execConditionList);

                        //-------------------------------------------------
                        //exec condition
                        //-------------------------------------------------
                        int conditionSize = CimArrayUtils.getSize(patternInfo.getExecConditionSeq());
                        for (int k = 0; k < conditionSize; k++) {
                            int tmpSeqNo = patternInfo.getExecConditionSeq().get(k);
                            if (tmpSeqNo <= 0 && tmpSeqNo > nPatternMax) {
                                Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                            }

                            if (seqNumberTable.get(tmpSeqNo - 1).size() > 1) {
                                //Wait for same lot's post process only.
                                execConditionList.add(seqNumberTable.get(tmpSeqNo - 1).get(1 + tmpABSDurableIndexTable.get(j)));
                            } else {
                                //eqp / cj
                                execConditionList.add(seqNumberTable.get(tmpSeqNo - 1).get(0));
                            }
                        }

                        //-------------------------------------------------
                        //update seqNumberTable
                        //-------------------------------------------------
                        seqNumberTable.get(patternInfo.getSeqNo() - 1).set(1 + tmpABSDurableIndexTable.get(j), seqNo.get());

                        //-------------------------------------------------
                        //Preparing Q-item 3
                        //-------------------------------------------------
                        Infos.PostProcessTargetObject processTargetObject = new Infos.PostProcessTargetObject();
                        processTargetObject.setCassetteID(tmpABSDurableIDs.get(j));
                        newQItem.setPostProcessTargetObject(processTargetObject);
                        newQItem.setSequenceNumber(CimNumberUtils.intValue(seqNo.getAndIncrement()));

                        actionInfoSeq.add(newQItem);
                    }
                    break;
                //--------------------------------------------------------------------------------------
                // targetType = EQP
                //--------------------------------------------------------------------------------------
                case SP_POSTPROCESS_TARGETTYPE_EQP:
                    if (ObjectIdentifier.isEmptyWithValue(paramIn.getStrDurablePostProcessRegistrationParm().getEquipmentID())) {
                        Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                    }

                    /*--------------------------------*/
                    /* Clone a new Object.            */
                    /*--------------------------------*/
                    Infos.PostProcessActionInfo newQItem = previousQItem.clone();
                    newQItem.setExecConditionList(new ArrayList<>());

                    //-------------------------------------------------
                    //exec condition
                    //-------------------------------------------------
                    int conditionSize = CimArrayUtils.getSize(patternInfo.getExecConditionSeq());
                    for (int j = 0; j < conditionSize; j++) {
                        int tmpSeqNo = patternInfo.getExecConditionSeq().get(j);
                        int len2 = seqNumberTable.get(tmpSeqNo - 1).size();

                        for (int k = 0; k < len2; k++) {
                            newQItem.getExecConditionList().add(0L);
                            if (seqNumberTable.get(tmpSeqNo - 1).get(k) != 0) {
                                newQItem.getExecConditionList().add(seqNumberTable.get(tmpSeqNo - 1).get(k));
                            }
                        }
                    }

                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    seqNumberTable.get(patternInfo.getSeqNo() - 1).add( seqNo.get());

                    //-------------------------------------------------
                    //Preparing Q-item (3)
                    //-------------------------------------------------
                    newQItem.setSequenceNumber(CimNumberUtils.intValue(seqNo.getAndIncrement()));
                    actionInfoSeq.add(newQItem);
                    break;
                //--------------------------------------------------------------------------------------
                // targetType = EQP+CONTROL JOB
                //--------------------------------------------------------------------------------------
                case SP_POSTPROCESS_TARGETTYPE_EQPANDCJ:
                    log.info("targetType == SP_PostProcess_TargetType_EQPandCJ.");
                    if (ObjectIdentifier.isEmptyWithValue(paramIn.getStrDurablePostProcessRegistrationParm().getDurableControlJobID())
                            || ObjectIdentifier.isEmptyWithValue(paramIn.getStrDurablePostProcessRegistrationParm().getEquipmentID())) {
                        Validations.check(retCodeConfig.getPostprocPatternMismatch(), "execCondition", paramIn.getPatternID());
                    }

                    /*--------------------------------*/
                    /* Clone a new Object.            */
                    /*--------------------------------*/
                    newQItem = previousQItem.clone();
                    newQItem.setExecConditionList(new ArrayList<>());

                    //-------------------------------------------------
                    //exec condition
                    //-------------------------------------------------
                    conditionSize = CimArrayUtils.getSize(patternInfo.getExecConditionSeq());
                    for (int j = 0; j < conditionSize; j++) {
                        int tmpSeqNo = patternInfo.getExecConditionSeq().get(j);
                        int len2 = seqNumberTable.get(tmpSeqNo - 1).size();

                        for (int k = 0; k < len2; k++) {
                            newQItem.getExecConditionList().add(0L);
                            if (seqNumberTable.get(tmpSeqNo - 1).get(k) != 0) {
                                newQItem.getExecConditionList().add(seqNumberTable.get(tmpSeqNo - 1).get(k));
                            }
                        }
                    }

                    //-------------------------------------------------
                    //update seqNumberTable
                    //-------------------------------------------------
                    seqNumberTable.get(patternInfo.getSeqNo() - 1).add(seqNo.get());

                    //-------------------------------------------------
                    //Preparing Q-item (3)
                    //-------------------------------------------------
                    newQItem.setSequenceNumber(CimNumberUtils.intValue(seqNo.getAndIncrement()));
                    actionInfoSeq.add(newQItem);
                    break;
                default:
                    log.error("Unknown targetType.");
                    Validations.check(retCodeConfig.getPostprocUnknownTargettype());
                    break;
            }
        }));

        retVal.setDKey(dKey);
        retVal.setStrActionInfoSeq(actionInfoSeq);

        return retVal;
    }
}
