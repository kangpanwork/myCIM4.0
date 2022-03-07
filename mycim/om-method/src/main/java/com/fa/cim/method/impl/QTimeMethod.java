package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.pos.CimPOSQrestDefaultActionDO;
import com.fa.cim.entity.runtime.pos.CimPOSQrestProductGroupActionDO;
import com.fa.cim.entity.runtime.pos.CimPOSQrestTechnologyActionDO;
import com.fa.cim.entity.runtime.pos.CimPOSTimeRestrictActionDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.qtime.CimQTimeActionDO;
import com.fa.cim.entity.runtime.qtime.CimQTimeDO;
import com.fa.cim.entity.runtime.qtime.MMQRestDO;
import com.fa.cim.entity.runtime.qtime.MMQTimeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.fa.cim.common.constant.BizConstant.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * QTimeCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/8        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/8 18:25
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class QTimeMethod  implements IQTimeMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private QTimeRestrictionManager qtimeRestrictionManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Override
    public List<Infos.QTimeActionRegisterInfo> qtimeReSetByOpeStartCancel(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        List<Infos.QTimeActionRegisterInfo> qTimeActionRegisterInfos = new ArrayList<>();
        int scLen = startCassetteList.size();
        int actionResetCnt = 0;
        for (int i = 0; i < scLen; i++) {
            int lcLen = startCassetteList.get(i).getLotInCassetteList().size();
            for (int j = 0; j < lcLen; j++) {
                //------------------------
                //   Omit Not-Start lot
                //------------------------
                boolean operationStartFlag = startCassetteList.get(i).getLotInCassetteList().get(j).getMoveInFlag();
                if (CimBooleanUtils.isFalse(operationStartFlag)) {
                    continue;
                }
                //--------------------
                //   Get lot Object
                //--------------------
                com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,startCassetteList.get(i).getLotInCassetteList().get(j).getLotID());
                //----------------------------------
                //   Get Current Operation Number
                //----------------------------------
                String currentOperationNumber = aLot.getOperationNumber();
                //Clear triggered wafer Level qtime and get Future Hold and Future Rework that registered by wafer Level Qtime.
                Outputs.QtimeClearByOpeStartCancelOut qtimeClearByOpeStartCancelout = this.qtimeClearByOpeStartCancel(objCommon, startCassetteList.get(i).getLotInCassetteList().get(j).getLotID());
                Infos.QTimeActionRegisterInfo qTimeActionRegisterInfo = new Infos.QTimeActionRegisterInfo();
                qTimeActionRegisterInfo.setLotID(startCassetteList.get(i).getLotInCassetteList().get(j).getLotID());
                qTimeActionRegisterInfo.setFutureHoldList(qtimeClearByOpeStartCancelout.getFutureHoldCancelList());
                qTimeActionRegisterInfo.setFutureReworkList(qtimeClearByOpeStartCancelout.getFutureReworkCancelList());
                qTimeActionRegisterInfos.add(qTimeActionRegisterInfo);

                //---------------------------------------------------
                //   Find qtime Restrictions for Current Operation
                //---------------------------------------------------
                List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeq = aLot.findQTimeRestrictionsFor( currentOperationNumber);
                int wFQTSLength = 0;
                List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> waferLevelQtimeSeq = null;
                if (CimStringUtils.equals(objCommon.getTransactionID(), "OEQPW012") || CimStringUtils.equals(objCommon.getTransactionID(), "OEQPW024")) {
                    waferLevelQtimeSeq = aLot.findQTimeRestrictionsForWaferLevelQTime(currentOperationNumber);
                    wFQTSLength = CimArrayUtils.getSize(waferLevelQtimeSeq);
                } else {
                    int waferLevelQtimeForWaferCount = aLot.getWaferLevelQTimeCount();
                    if (waferLevelQtimeForWaferCount > 0) {
                        waferLevelQtimeSeq = aLot.findQTimeRestrictionsForWaferLevelQTime( currentOperationNumber);
                        wFQTSLength = CimArrayUtils.getSize(waferLevelQtimeSeq);
                    }
                }
                int lotQTSLength = qtimeSeq.size();
                for (int kk = 0; kk < wFQTSLength; kk++) {
                    qtimeSeq.add(waferLevelQtimeSeq.get(kk));
                }
                //------------------------------------------
                //   Change WatchDogRequiredFlag to FALSE
                //------------------------------------------
                int qTSLength = qtimeSeq.size();
                for (int k = 0; k < qTSLength; k++) {
                    qtimeSeq.get(k).makeWatchDogRequired();
                    List<ProcessDTO.QTimeRestrictionAction> aQTimeRestrictionActionSeq = qtimeSeq.get(k).getQTimeRestrictionActions();
                    for (ProcessDTO.QTimeRestrictionAction qTimeAction : aQTimeRestrictionActionSeq) {
                        qTimeAction.setWatchdogRequired(true);
                    }
                    qtimeSeq.get(k).setQTimeRestrictionActions( aQTimeRestrictionActionSeq );
                    //----------------------------------------------------------------
                    // Make event
                    //----------------------------------------------------------------
                    ProcessDTO.QTimeRestrictionInfo qTimeRestrictionInfo = qtimeSeq.get(k).getQTimeRestrictionInfo();
                    Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                    strQtimeInfo.setWaferID(qTimeRestrictionInfo.getWaferID());
                    strQtimeInfo.setQTimeType(qTimeRestrictionInfo.getQTimeType());
                    strQtimeInfo.setPreTrigger(qTimeRestrictionInfo.getPreTrigger());
                    strQtimeInfo.setOriginalQTime(qTimeRestrictionInfo.getOriginalQTime());
                    strQtimeInfo.setProcessDefinitionLevel(qTimeRestrictionInfo.getProcessDefinitionLevel());
                    strQtimeInfo.setRestrictionTriggerRouteID(qTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    strQtimeInfo.setRestrictionTriggerOperationNumber(qTimeRestrictionInfo.getTriggerOperationNumber());
                    strQtimeInfo.setRestrictionTriggerBranchInfo(qTimeRestrictionInfo.getTriggerBranchInfo());
                    strQtimeInfo.setRestrictionTriggerReturnInfo(qTimeRestrictionInfo.getTriggerReturnInfo());
                    strQtimeInfo.setRestrictionTriggerTimeStamp(qTimeRestrictionInfo.getTriggerTimeStamp());
                    strQtimeInfo.setRestrictionTargetRouteID(qTimeRestrictionInfo.getTargetMainProcessDefinition());
                    strQtimeInfo.setRestrictionTargetOperationNumber(qTimeRestrictionInfo.getTargetOperationNumber());
                    strQtimeInfo.setRestrictionTargetBranchInfo(qTimeRestrictionInfo.getTargetBranchInfo());
                    strQtimeInfo.setRestrictionTargetReturnInfo(qTimeRestrictionInfo.getTargetReturnInfo());
                    strQtimeInfo.setRestrictionTargetTimeStamp(qTimeRestrictionInfo.getTargetTimeStamp());
                    strQtimeInfo.setPreviousTargetInfo(qTimeRestrictionInfo.getPreviousTargetInfo());
                    strQtimeInfo.setSpecificControl(qTimeRestrictionInfo.getControl());
                    if (qTimeRestrictionInfo.getWatchdogRequired() == true) {
                        strQtimeInfo.setWatchDogRequired("Y");
                    } else if (qTimeRestrictionInfo.getWatchdogRequired() == false) {
                        strQtimeInfo.setWatchDogRequired("N");
                    }
                    if (qTimeRestrictionInfo.getActionDone() == true) {
                        strQtimeInfo.setActionDoneFlag("Y");
                    } else if (qTimeRestrictionInfo.getActionDone() == false) {
                        strQtimeInfo.setActionDoneFlag("N");
                    }
                    strQtimeInfo.setManualCreated(qTimeRestrictionInfo.getManualCreated());
                    int actionLength = qTimeRestrictionInfo.getActions().size();
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    if (actionLength != 0) {
                        for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                            strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp(qTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction(qTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID(qTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID(qTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());  //DSN000100682
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber(qTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming(qTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID(qTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID(qTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField(qTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                        }
                    }
                }
            }
        }
        return qTimeActionRegisterInfos;
    }

    @Override
    public void qTimeCheckConditionForReplaceTarget(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {

        //【step1】get lot object
        log.debug("【step1】get lot object");
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        Validations.check(null == lot, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));

        //【step2】get target lot's process flow context.
        log.debug("【step2】get target lot's process flow context.");
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
        Validations.check(null == processFlowContext, new OmCode(retCodeConfig.getNotFoundPfx(), ""));

        //【step3】find qtime Restrictions for target lot
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qTimeRestrictionList = null;
        log.debug("【step3】find qtime Restrictions for target lot");
        if (CimStringUtils.equals("OEQPW012", objCommon.getTransactionID())
                || CimStringUtils.equals("OEQPW024", objCommon.getTransactionID())) {
            log.debug("TxID = %s", objCommon.getTransactionID());
            qTimeRestrictionList = lot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            log.debug("TxID = %s", objCommon.getTransactionID());
            int qTimeSeqForWaferCount = lot.getWaferLevelQTimeCount();
            if (qTimeSeqForWaferCount > 0){
                qTimeRestrictionList = lot.allQTimeRestrictionsWithWaferLevelQTime();
            } else {
                qTimeRestrictionList = lot.allQTimeRestrictions();
            }
        }
        int QTSLength = CimArrayUtils.getSize(qTimeRestrictionList);
        for (int i = 0; i < QTSLength; i++) {
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction qTimeRestriction = qTimeRestrictionList.get(i);
            Validations.check(null == qTimeRestriction, new OmCode(retCodeConfig.getNotFoundQtime(), lotID.getValue()));
            // check the lot is in qtime section or not
            log.debug("check the lot is in qtime section or not");
            boolean bIsInQTimeIntervalFlag = processFlowContext.isInQTimeInterval(qTimeRestriction);
            if (bIsInQTimeIntervalFlag){
                qTimeRestriction.setControl(BizConstant.SP_QRESTTIME_SPECIFICCONTROL_REPLACETARGET);
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lot
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/7/18 23:36
     */
    @Override
    public void checkMMQTime(com.fa.cim.newcore.bo.product.CimLot lot, boolean deleteReq) {
        List<MMQRestDO> qrests = cimJpaRepository.query("SELECT * FROM MM_QREST WHERE PROD_OBJ=?"
                , MMQRestDO.class, lot.getProductSpecificationID().getReferenceKey());
        if (CimArrayUtils.getSize(qrests) == 0) {
            return;
        }
        for (MMQRestDO qrest : qrests) {
            MMQTimeDO qtime = cimJpaRepository.queryOne("SELECT * FROM MM_QTIME WHERE LOT_OBJ=? AND QREST_OBJ=?",
                    MMQTimeDO.class, lot.getPrimaryKey(), qrest.getId());
            long st = System.currentTimeMillis();
            if (CimStringUtils.equals(lot.getOperationNumber(), qrest.getTriggerOperationNumber())) {
                if (qtime == null) {
                    qtime = new MMQTimeDO();
                }
                qtime.setLotID(lot.getIdentifier());
                qtime.setLotObj(lot.getPrimaryKey());
                qtime.setQrestObj(qrest.getId());
                long exptimes = CimLongUtils.longValue(qrest.getExptimes() == null ? 0 : qrest.getExptimes());
                qtime.setTriggerTime(new Timestamp(st));
                qtime.setTargetTime(new Timestamp(st + exptimes));
                cimJpaRepository.save(qtime);
                continue;
            }
            if (qtime == null) {
                continue;
            }
            if (CimStringUtils.equals(lot.getOperationNumber(), qrest.getTargetOperationNumber())) {
                long target = qtime.getTargetTime().getTime();
                if (CimBooleanUtils.isTrue(qrest.getMaxFlag()) && st > target) {
                    throw new ServiceException(new OmCode(12, qrest.getMaxmes()));
                }
                if (!CimBooleanUtils.isTrue(qrest.getMaxFlag()) && st < target) {
                    throw new ServiceException(new OmCode(12, qrest.getMinmes()));
                }
                if (deleteReq) {
                    cimJpaRepository.delete(qtime);
                }
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @throws
     * @author Ho
     * @date 2019/7/24 10:09
     */
    @Override
    public void checkMMQTime(com.fa.cim.newcore.bo.product.CimLot lot) {
        checkMMQTime(lot, false);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @throws
     * @author Ho
     * @date 2019/7/24 10:09
     */
    @Override
    public void checkMMQTime(ObjectIdentifier lotID, boolean deleteReq) {
        checkMMQTime(baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID), deleteReq);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strExpiredQrestTimeLotListGetDRIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.ExpiredQrestTimeLotListGetDROut>
     * @throws
     * @author Ho
     * @date 2019/4/23 10:40
     */
    @Override
    public Infos.ExpiredQrestTimeLotListGetDROut expiredQrestTimeLotListGetDR(Infos.ObjCommon strObjCommonIn, Infos.ExpiredQrestTimeLotListGetDRIn strExpiredQrestTimeLotListGetDRIn) {

        Long totalcount = 0L;

        Long fetchLimitCount = strExpiredQrestTimeLotListGetDRIn.getMaxRetrieveCount();
        if (fetchLimitCount <= 0 || fetchLimitCount > BizConstant.SP_MAXLIMITCOUNT_FOR_LISTINQ) {
            fetchLimitCount = -1L;
        }

        String hFRQTIME_ACTIONTARGET_TIME = CimDateUtils.getCurrentDateTimeByPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
        String hFRQTIME_ACTIONACTION = BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE;

        String sql = "SELECT COUNT( DISTINCT LOT_ID )\n" +
                "        FROM OMQT, OMQT_ACT\n" +
                "        WHERE OMQT_ACT.ACTION <> ?\n" +
                "        AND OMQT_ACT.TARGET_TIME <= ?\n" +
                "        AND OMQT.TIMER_FLAG       = 1\n" +
                "        AND OMQT.ACTION_COMPLETE_FLAG   = 0\n" +
                "        AND OMQT.ID     = OMQT_ACT.REFKEY";
        totalcount = cimJpaRepository.count(sql, hFRQTIME_ACTIONACTION, hFRQTIME_ACTIONTARGET_TIME);

        List<ObjectIdentifier> aList = new ArrayList<>();

        if (totalcount > 0) {
            sql = "SELECT DISTINCT(LOT_ID), LOT_RKEY\n" +
                    "            FROM OMQT, OMQT_ACT\n" +
                    "            WHERE OMQT_ACT.ACTION <> ?\n" +
                    "            AND OMQT_ACT.TARGET_TIME <= ?\n" +
                    "            AND OMQT.TIMER_FLAG       = 1\n" +
                    "            AND OMQT.ACTION_COMPLETE_FLAG   = 0\n" +
                    "            AND OMQT.ID     = OMQT_ACT.REFKEY";

            List<Object[]> objects = cimJpaRepository.query(sql, hFRQTIME_ACTIONACTION, hFRQTIME_ACTIONTARGET_TIME);

            int count = 0;
            for (Object[] object : objects) {
                String hFRQTIMELOT_ID = (String) object[0], hFRQTIMELOT_OBJ = (String) object[1];

                ObjectIdentifier anObjectIdentifier = ObjectIdentifier.build(hFRQTIMELOT_ID, hFRQTIMELOT_OBJ);
                aList.add(anObjectIdentifier);

                count++;
                if (fetchLimitCount != -1 && count >= fetchLimitCount) {
                    break;
                }
            }
        }

        List<ObjectIdentifier> aLotIDSeq = aList;
        Infos.ExpiredQrestTimeLotListGetDROut strExpiredQrestTimeLotListGetDROut = new Infos.ExpiredQrestTimeLotListGetDROut();
        strExpiredQrestTimeLotListGetDROut.setLotIDs(aLotIDSeq);
        return strExpiredQrestTimeLotListGetDROut;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                           Wind
     *
     * @param objCommon
     * @param qtimeDefinitionSelectionInqIn
     * @return RetCode<Outputs.ObjQTimeCandidateListGetOut>
     * @author Wind
     * @date 2018/10/30 14:38
     */
    @Override
    public List<Infos.QrestTimeInfo> qtimeCandidateListGet(Infos.ObjCommon objCommon, Inputs.QtimeDefinitionSelectionInqIn qtimeDefinitionSelectionInqIn) {
        ObjectIdentifier lotID = qtimeDefinitionSelectionInqIn.getLotID();

        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,qtimeDefinitionSelectionInqIn.getLotID());

        CimProductSpecification productSpecification = lot.getProductSpecification();
        String aProdID = null;
        if (productSpecification != null) {
            aProdID = productSpecification.getIdentifier();
        }
        com.fa.cim.newcore.bo.prodspec.CimProductGroup aPosProductGroup = null;
        if (productSpecification != null) {
            aPosProductGroup = productSpecification.getProductGroup();
        }
        String aProdGroupID = null;
        if (aPosProductGroup != null) {
            aProdGroupID = aPosProductGroup.getIdentifier();
        }

        //----------------------------------------------------------------------------------
        //  Get Technology
        //----------------------------------------------------------------------------------
        CimTechnology aTechnology = null;
        if (aPosProductGroup != null) {
            aTechnology = aPosProductGroup.getTechnology();
        }
        String aTechnologyID = null;
        if (aTechnology != null) {
            aTechnologyID = aTechnology.getIdentifier();
        }
        //----------------------------------------------------------------------------------
        //  Get process flow context
        //----------------------------------------------------------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
        Validations.check(processFlowContext == null, retCodeConfig.getNotFoundPfx());
        //----------------------------------------------------------------------------------
        //  Get process flow
        //----------------------------------------------------------------------------------
        ProcessFlow processFlow = lot.getProcessFlow();
        Validations.check(processFlow == null, retCodeConfig.getNotFoundProcessFlow());
        //----------------------------------------------------------------------------------
        //  Get main process flow
        //----------------------------------------------------------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessFlow mainProcessFlow = processFlowContext.getMainProcessFlow();
        Validations.check(mainProcessFlow == null, retCodeConfig.getNotFoundProcessFlow());
        //----------------------------------------------------------------------------------
        //  Get module process flow
        //----------------------------------------------------------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessFlow moduleProcessFlow = processFlowContext.getModuleProcessFlow();
        Validations.check(moduleProcessFlow == null, retCodeConfig.getNotFoundProcessFlow());

        /*----------------------------------*/
        /*   Get Current Operation Number   */
        /*----------------------------------*/
        String anOperationNumber = lot.getOperationNumber();
        //----------------------------------------------------------------
        //  Get candidate list of Q-Time definition in current route
        //----------------------------------------------------------------
        Inputs.ObjQtimeCandidateListInRouteGetDRIn objQtimeCandidateListInRouteGetDRIn = new Inputs.ObjQtimeCandidateListInRouteGetDRIn();

        objQtimeCandidateListInRouteGetDRIn.setLotID(lotID);
        objQtimeCandidateListInRouteGetDRIn.setProductID(aProdID);
        objQtimeCandidateListInRouteGetDRIn.setProductGroupID(aProdGroupID);
        objQtimeCandidateListInRouteGetDRIn.setTechnologyID(aTechnologyID);
        objQtimeCandidateListInRouteGetDRIn.setProcessFlow(processFlow.getPrimaryKey());
        objQtimeCandidateListInRouteGetDRIn.setMainProcessFlow(mainProcessFlow.getPrimaryKey());
        objQtimeCandidateListInRouteGetDRIn.setModuleProcessFlow(moduleProcessFlow.getPrimaryKey());
        objQtimeCandidateListInRouteGetDRIn.setOperationNumber(anOperationNumber);

        //step1 - qtime_candidateListInRoute_GetDR__180()
        List<Infos.QrestTimeInfo>  objQTimeCandidateListInRouteGetDROut = qtimeCandidateListInRouteGetDR(objCommon, objQtimeCandidateListInRouteGetDRIn);
        List<Infos.QrestTimeInfo> strQtimeInfoSeq = objQTimeCandidateListInRouteGetDROut;
        int aQtimeInfoCnt = strQtimeInfoSeq.size();
        //----------------------------------------------------------------------------------
        //  Get return operations
        //----------------------------------------------------------------------------------
        List<ProcessDTO.ReturnOperation> returnOperations = processFlowContext.allReturnOperations();
        if (returnOperations != null) {
            int aReturnOpeLen = returnOperations.size();
            for (int aReturnOpeNo = 0; aReturnOpeNo < aReturnOpeLen; aReturnOpeNo++) {
                //----------------------------------------------------------------
                //  Get candidate list of Q-Time definition in return route
                //----------------------------------------------------------------
                objQtimeCandidateListInRouteGetDRIn.setLotID(lotID);
                objQtimeCandidateListInRouteGetDRIn.setProductID(aProdID);
                objQtimeCandidateListInRouteGetDRIn.setProductGroupID(aProdGroupID);
                objQtimeCandidateListInRouteGetDRIn.setTechnologyID(aTechnologyID);

                objQtimeCandidateListInRouteGetDRIn.setProcessFlow(returnOperations.get(aReturnOpeNo).getProcessFlow());
                objQtimeCandidateListInRouteGetDRIn.setMainProcessFlow(returnOperations.get(aReturnOpeNo).getMainProcessFlow());
                objQtimeCandidateListInRouteGetDRIn.setModuleProcessFlow(returnOperations.get(aReturnOpeNo).getModuleProcessFlow());
                objQtimeCandidateListInRouteGetDRIn.setOperationNumber(returnOperations.get(aReturnOpeNo).getOperationNumber());

                //step2 - qtime_candidateListInRoute_GetDR__180
                objQTimeCandidateListInRouteGetDROut = qtimeCandidateListInRouteGetDR(objCommon, objQtimeCandidateListInRouteGetDRIn);

                int tmpQtimeInfoCnt = objQTimeCandidateListInRouteGetDROut.size();
                for (int tmpQtimeInfoNo = 0; tmpQtimeInfoNo < tmpQtimeInfoCnt; tmpQtimeInfoNo++) {
                    Infos.QrestTimeInfo qrestTimeInfo = objQTimeCandidateListInRouteGetDROut.get(tmpQtimeInfoNo);
                    strQtimeInfoSeq.add( qrestTimeInfo);
                }
                aQtimeInfoCnt += tmpQtimeInfoCnt;
            }
        }
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return strQtimeInfoSeq;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                          Wind
     *
     * @param objCommon
     * @param objQtimeCandidateListInRouteGetDRIn
     * @return RetCode<Outputs.ObjQTimeCandidateListInRouteGetDROut>
     * @author Wind
     * @date 2018/10/30 16:13
     */
    @Override
    public List<Infos.QrestTimeInfo> qtimeCandidateListInRouteGetDR(Infos.ObjCommon objCommon, Inputs.ObjQtimeCandidateListInRouteGetDRIn objQtimeCandidateListInRouteGetDRIn) {
        /*------------------*/
        /*    Initialize    */
        /*------------------*/

        ObjectIdentifier lotID = objQtimeCandidateListInRouteGetDRIn.getLotID();
        //----------------------------------
        //  Get systemkey of mainPF(Main_Mod)
        //----------------------------------
        String mainPfdTheSystemKey = objQtimeCandidateListInRouteGetDRIn.getMainProcessFlow();
        String modulePfdTheSystemKey = objQtimeCandidateListInRouteGetDRIn.getModuleProcessFlow();

        String operationNumber = objQtimeCandidateListInRouteGetDRIn.getOperationNumber();
        String moduleNo = BaseStaticMethod.convertOpeNoToModuleNo(operationNumber);
        String moduleOpeNo = BaseStaticMethod.convertOpeNoToModuleOpeNo(operationNumber);

        //------------------------------------------
        //   Get MainPDID from MainPF systemkey
        //------------------------------------------
        List<Object[]> mainPdIdAndMainPdObj = cimJpaRepository
                .query("select PRP_ID,PRP_RKEY from OMPRF where ID = ?1",mainPfdTheSystemKey);
        ObjectIdentifier mainPDOI = new ObjectIdentifier();
        if (!CimArrayUtils.isEmpty(mainPdIdAndMainPdObj)) {
            mainPDOI.setValue(CimObjectUtils.toString(mainPdIdAndMainPdObj.get(0)[0]));
            mainPDOI.setReferenceKey(CimObjectUtils.toString(mainPdIdAndMainPdObj.get(0)[1]));
        }

        //------------------------------------------
        //   Get Seqno from MainPF PD list
        //------------------------------------------
        Integer sequenceNumber = CimNumberUtils.intValue(
                CimStringUtils.firstToString(cimJpaRepository
                        .queryOne("select IDX_NO,REFKEY from OMPRF_ROUTESEQ where REFKEY=?1 " +
                                "and LINK_KEY = ?2 ",mainPfdTheSystemKey, moduleNo)));
        List<String> triggerModNoList = new ArrayList<>();
        List<String> dKeys = CimArrayUtils.firstConvertToString(cimJpaRepository
                .query("select LINK_KEY,REFKEY from OMPRF_ROUTESEQ where REFKEY = ? " +
                        "and IDX_NO < ?",mainPfdTheSystemKey, sequenceNumber));
        if (!CimArrayUtils.isEmpty(dKeys)) {
            triggerModNoList.addAll(dKeys);
        }

        String hFRPFd_theSystemKey = modulePfdTheSystemKey;
        String hFRPF_POSLISTd_key = moduleOpeNo;
        Integer hFRPF_POSLISTd_SeqNo = 0;

        String sql = "SELECT IDX_NO\n" +
                "        FROM   OMPRF_PRSSSEQ\n" +
                "        WHERE  REFKEY = ?\n" +
                "        AND    LINK_KEY = ?";

        List<Object[]> querys = cimJpaRepository.query(sql, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
        Validations.check(CimArrayUtils.isEmpty(querys), retCodeConfig.getNotFoundPos());
        hFRPF_POSLISTd_SeqNo = CimNumberUtils.intValue(querys.get(0)[0]);

        List<String> triggerModOpeNoList = new ArrayList<>();

        int triggerModOpeNoListCnt = 0;
        int triggerModOpeNoListLen = 10;

        sql = "SELECT LINK_KEY,REFKEY\n" +
                "        FROM   OMPRF_PRSSSEQ\n" +
                "        WHERE  REFKEY = ?\n" +
                "        AND    IDX_NO < ?";
        List<Object[]> query = cimJpaRepository.query(sql, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);

        for (Object[] obj : query) {
            hFRPF_POSLISTd_key = (String) obj[0];

            if (triggerModOpeNoListCnt >= triggerModOpeNoListLen) {
                triggerModOpeNoListLen += 10;
            }
            triggerModOpeNoList.add(hFRPF_POSLISTd_key);

            triggerModOpeNoListCnt++;
        }

        long qtimeDispatchPrecedeUseCustomFieldFlag =
                StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getLongValue();
        int totalcount;
        Map<String, Infos.QrestTimeInfo> candidateList = new HashMap<>();
        List<String> strings = CimArrayUtils.generateList(null, null, null, null);
        ObjectIdentifier blankID = new ObjectIdentifier();
        if (objQtimeCandidateListInRouteGetDRIn.getProductID() != null) {
            if (!CimArrayUtils.isEmpty(triggerModNoList)) {
                for (String triggerModNo : triggerModNoList) {
                    List<Object[]> posAndPosTimeResTrictList = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTPROD QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO LIKE ?2 " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.PROD_ID =?3",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(),
                                    triggerModNo + "%", objQtimeCandidateListInRouteGetDRIn.getProductID());
                    if (posAndPosTimeResTrictList != null) {
                        for (Object[] objects : posAndPosTimeResTrictList) {
                            //------------------------------------------------------
                            //   Convert moduleNo and moduleOpeNo from openo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are same,
                            //  check target openo is higher than input openo.
                            //---------------------------------------------------------
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                            }
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are not same,
                            //  check target moduleno is higher than input moduleno.
                            //---------------------------------------------------------
                            else {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_ROUTESEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                            }
                            if (totalcount == 0) {
                                continue;
                            }

                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);

                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }

                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();

                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.parseLong(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);
                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSTimeRestrictActionDO> posTimeRestrictActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPROD_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSTimeRestrictActionDO.class,
                                    objects[1].toString(), objects[2].toString());
                            Validations.check(CimArrayUtils.isEmpty(posTimeRestrictActions),
                                    new ErrorCode("pos time restrict action list is null !"));
                            for (CimPOSTimeRestrictActionDO posTimeRestrictAction : posTimeRestrictActions) {
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posTimeRestrictAction.getAction())) {
                                    continue;
                                }
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();

                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posTimeRestrictAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posTimeRestrictAction.getAction());
                                qTimeActionInfo.setReasonCodeID(
                                        ObjectIdentifier.build(posTimeRestrictAction.getReasonCode(),
                                                posTimeRestrictAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(
                                        posTimeRestrictAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(
                                        Boolean.parseBoolean(qTimeActionInfo
                                                .getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posTimeRestrictAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(
                                        ObjectIdentifier.build(posTimeRestrictAction.getMainPdID(),
                                                posTimeRestrictAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(
                                        ObjectIdentifier.build(posTimeRestrictAction.getMsgDefID(),
                                                posTimeRestrictAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posTimeRestrictAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");


                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }

            //--------------------------------------------------------------------
            //   Get qtime definitions of MainPOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String dKey : triggerModOpeNoList) {

                    StringBuffer posOPENumber = new StringBuffer();
                    posOPENumber.append(moduleNo);
                    posOPENumber.append(".");
                    posOPENumber.append(dKey);

                    List<Object[]> posAndPosTimeResTrict =
                            cimJpaRepository.query("SELECT OMPRSS.OPE_NO,\n" +
                                    "                            QREST.REFKEY,\n" +
                                    "                            QREST.LINK_KEY,\n" +
                                    "                            QREST.DURATION,\n" +
                                    "                            QREST.QT_TYPE, \n" +
                                    "                            QREST.TARGET_OPE_NO\n" +
                                    "                     FROM   OMPRSS, OMPRSS_QTPROD QREST\n" +
                                    "                     WHERE  OMPRSS.PRF_RKEY = ?\n" +
                                    "                     AND    OMPRSS.OPE_NO = ?\n" +
                                    "                     AND    OMPRSS.ID = QREST.REFKEY\n" +
                                    "                     AND    QREST.PROD_ID = ?",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString(),
                            objQtimeCandidateListInRouteGetDRIn.getProductID());
                    if (!CimArrayUtils.isEmpty(posAndPosTimeResTrict)) {
                        for (Object[] objects : posAndPosTimeResTrict) {
                            //------------------------------------------------------
                            //   Convert openo from moduleNo and moduleOpeNo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                            } else {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_ROUTESEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                            }
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            // Exist qtime definition
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.parseLong(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);
                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSTimeRestrictActionDO> posTimeRestrictActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPROD_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",
                                    CimPOSTimeRestrictActionDO.class,objects[1].toString(), objects[2].toString());
                            if (!CimArrayUtils.isEmpty(posTimeRestrictActions)) {
                                for (CimPOSTimeRestrictActionDO posTimeRestrictAction : posTimeRestrictActions) {
                                    // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                    if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                            && CimStringUtils.equals(
                                                    BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                            posTimeRestrictAction.getAction())) {
                                        continue;
                                    }
                                    Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();

                                    strQtimeActionInfo.add(qTimeActionInfo);
                                    qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                    qTimeActionInfo.setExpiredTimeDuration(
                                            CimNumberUtils.longValue(posTimeRestrictAction.getExpiredTime()));
                                    qTimeActionInfo.setQrestrictionAction(posTimeRestrictAction.getAction());
                                    qTimeActionInfo.setReasonCodeID(
                                            ObjectIdentifier.build(posTimeRestrictAction.getReasonCode(),
                                                    posTimeRestrictAction.getReasonCodeObj()));
                                    qTimeActionInfo.setActionOperationNumber(
                                            posTimeRestrictAction.getOperationNumber());
                                    qTimeActionInfo.setActionRouteID(
                                            Boolean.parseBoolean(qTimeActionInfo
                                                    .getActionOperationNumber()) ? mainPDOI : blankID);
                                    qTimeActionInfo.setFutureHoldTiming(posTimeRestrictAction.getTiming());
                                    qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                            .build(posTimeRestrictAction.getMainPdID(),
                                                    posTimeRestrictAction.getMainPdObj()));
                                    qTimeActionInfo.setMessageID(
                                            ObjectIdentifier.build(posTimeRestrictAction.getMsgDefID(),
                                                    posTimeRestrictAction.getMsgDefObj()));
                                    /*qTimeActionInfo.setCustomField(posTimeRestrictAction.getCustomField());*/
                                    qTimeActionInfo.setWatchDogRequired("");
                                    qTimeActionInfo.setActionDoneFlag("");
                                }
                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
            //--------------------------------------------------------------------
            //   Get qtime definitions of ModulePOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String triggerModOpeNo : triggerModOpeNoList) {
                    List<Object[]> posAndPosTimerStricts = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTPROD QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO = ?2 " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.PROD_ID =?3",
                            objQtimeCandidateListInRouteGetDRIn.getModuleProcessFlow(),
                                    triggerModOpeNo, objQtimeCandidateListInRouteGetDRIn.getProductID());
                    if (!CimArrayUtils.isEmpty(posAndPosTimerStricts)) {
                        for (Object[] objects : posAndPosTimerStricts) {
                            //------------------------------------------------------
                            //   Convert openo from moduleNo and moduleOpeNo.
                            //------------------------------------------------------
                            String triggerOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[0]));
                            String targetOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[5]));
                            totalcount = (int) cimJpaRepository
                                    .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                    "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, CimObjectUtils.toString(objects[5]), hFRPF_POSLISTd_SeqNo);
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, triggerOperationNo);
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, targetOperationNo);
                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(triggerOperationNo);
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(targetOperationNo);
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MODULE);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));
                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSTimeRestrictActionDO> posByRefKeyAndTableMakers =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPROD_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSTimeRestrictActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posByRefKeyAndTableMakers),
                                    new ErrorCode("pos time restrict action is null !"));
                            for (CimPOSTimeRestrictActionDO posByRefKeyAndTableMaker : posByRefKeyAndTableMakers) {
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posByRefKeyAndTableMaker.getAction())) {
                                    continue;
                                }
                                String actionOperationNo = BaseStaticMethod
                                        .convertModuleOpeNoToOpeNo(moduleNo,
                                                posByRefKeyAndTableMaker.getOperationNumber());
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();

                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posByRefKeyAndTableMaker.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posByRefKeyAndTableMaker.getAction());
                                qTimeActionInfo.setReasonCodeID(
                                        ObjectIdentifier.build(posByRefKeyAndTableMaker.getReasonCode(),
                                                posByRefKeyAndTableMaker.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(actionOperationNo);
                                qTimeActionInfo.setActionRouteID(
                                        Boolean.parseBoolean(qTimeActionInfo
                                                .getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posByRefKeyAndTableMaker.getTiming());
                                qTimeActionInfo.setReworkRouteID(
                                        ObjectIdentifier.build(posByRefKeyAndTableMaker.getMainPdID(),
                                                posByRefKeyAndTableMaker.getMainPdObj()));
                                qTimeActionInfo.setMessageID(
                                        ObjectIdentifier.build(posByRefKeyAndTableMaker.getMsgDefID(),
                                                posByRefKeyAndTableMaker.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posByRefKeyAndTableMaker.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");

                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
        }
        //==============================================================================
        //   By productgroup's qtime definitions
        //==============================================================================
        if (objQtimeCandidateListInRouteGetDRIn.getProductGroupID() != null) {
            //--------------------------------------------------------------------
            //   Get qtime definitions of MainPOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModNoList)) {
                for (String triggerModNo : triggerModNoList) {
                    StringBuffer posOPENumber = new StringBuffer();
                    posOPENumber.append(triggerModNo);
                    posOPENumber.append("%");

                    List<Object[]> posAndPosQrestPgrp = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO " +
                                            "FROM OMPRSS, OMPRSS_QTPRODFMLY QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO LIKE ?2 " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.PRODFMLY_ID = ?3",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString(),
                                    objQtimeCandidateListInRouteGetDRIn.getProductGroupID());
                    if (!CimArrayUtils.isEmpty(posAndPosQrestPgrp)) {
                        for (Object[] objects : posAndPosQrestPgrp) {
                            //------------------------------------------------------
                            //   Convert moduleNo and moduleOpeNo from openo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are same,
                            //  check target openo is higher than input openo.
                            //---------------------------------------------------------
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                                "FROM OMPRF_PRSSSEQ " +
                                                "WHERE REFKEY = ?1 " +
                                                "AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);

                            } else {
                                totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                                "FROM OMPRF_ROUTESEQ " +
                                                "WHERE REFKEY = ?1 " +
                                                "AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);

                            }
                            if (totalcount == 0) {
                                continue;
                            }

                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            // Exist qtime definition
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.parseLong(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);
                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestProductGroupActionDO> posProcessGroupActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPRODFMLY_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestProductGroupActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posProcessGroupActions),
                                    new ErrorCode("pos qrest progroup action is null !"));
                            for (CimPOSQrestProductGroupActionDO posProcessGroupAction : posProcessGroupActions) {
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posProcessGroupAction.getAction())) {
                                    continue;
                                }
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();

                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posProcessGroupAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posProcessGroupAction.getAction());
                                qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                        .build(posProcessGroupAction.getReasonCode(),
                                                posProcessGroupAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(
                                        posProcessGroupAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(Boolean.parseBoolean(
                                        qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posProcessGroupAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                        .build(posProcessGroupAction.getMainPdID(),
                                                posProcessGroupAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(ObjectIdentifier
                                        .build(posProcessGroupAction.getMsgDefID(),
                                                posProcessGroupAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posProcessGroupAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");

                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
            //--------------------------------------------------------------------
            //   Get qtime definitions of MainPOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String triggerModOpeNo : triggerModOpeNoList) {
                    StringBuffer posOPENumber = new StringBuffer();
                    posOPENumber.append(moduleNo);
                    posOPENumber.append(".");
                    posOPENumber.append(triggerModOpeNo);
                    List<Object[]> posAndPosQrestProGroup = cimJpaRepository.query("SELECT OMPRSS.OPE_NO, " +
                                    "QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, QREST.QT_TYPE, " +
                                    "QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTPRODFMLY QREST " +
                                    "WHERE OMPRSS.PRF_RKEY = ?1 AND  OMPRSS.OPE_NO = ?2 AND OMPRSS.ID = QREST.REFKEY " +
                                    "AND QREST.PRODFMLY_ID = ?3",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(),posOPENumber.toString(),
                            objQtimeCandidateListInRouteGetDRIn.getProductGroupID());
                    if (!CimArrayUtils.isEmpty(posAndPosQrestProGroup)) {
                        for (Object[] objects : posAndPosQrestProGroup) {
                            //------------------------------------------------------
                            //   Convert moduleNo and moduleOpeNo from openo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are same,
                            //  check target openo is higher than input openo.
                            //---------------------------------------------------------
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                            }
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are not same,
                            //  check target moduleno is higher than input moduleno.
                            //---------------------------------------------------------
                            else {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_ROUTESEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                            }
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            // Exist qtime definition
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestProductGroupActionDO> posProcessGroupActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPRODFMLY_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestProductGroupActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posProcessGroupActions),
                                    new ErrorCode("pos qrest progroup action is null !"));
                            for (CimPOSQrestProductGroupActionDO posProcessGroupAction : posProcessGroupActions) {
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(
                                                BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posProcessGroupAction.getAction())) {
                                    continue;
                                }
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posProcessGroupAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posProcessGroupAction.getAction());
                                qTimeActionInfo.setReasonCodeID(ObjectIdentifier.build(
                                        posProcessGroupAction.getReasonCode(),
                                        posProcessGroupAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(
                                        posProcessGroupAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(Boolean.parseBoolean(
                                        qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posProcessGroupAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(ObjectIdentifier.build(
                                        posProcessGroupAction.getMainPdID(), posProcessGroupAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(ObjectIdentifier.build(
                                        posProcessGroupAction.getMsgDefID(), posProcessGroupAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posProcessGroupAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");
                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
            //--------------------------------------------------------------------
            //   Get qtime definitions of ModulePOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String triggerModOpeNo : triggerModOpeNoList) {
                    List<Object[]> posAndPosQrestProGroups = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTPRODFMLY QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO = ?2 " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.PRODFMLY_ID = ?3",
                            objQtimeCandidateListInRouteGetDRIn.getModuleProcessFlow(),
                                    triggerModOpeNo, objQtimeCandidateListInRouteGetDRIn.getProductGroupID());
                    if (posAndPosQrestProGroups != null) {
                        for (Object[] objects : posAndPosQrestProGroups) {
                            String triggerOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[0]));
                            String targetOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[5]));
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                            "WHERE REFKEY = ?1 " +
                                            "AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, CimObjectUtils.toString(objects[5]), hFRPF_POSLISTd_SeqNo);
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, triggerOperationNo);
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, targetOperationNo);
                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(triggerOperationNo);
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(targetOperationNo);
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MODULE);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestProductGroupActionDO> posQrestProcessGroupActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTPRODFMLY_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestProductGroupActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posQrestProcessGroupActions),
                                    new ErrorCode("pos qrest progroup action is null !"));
                            for (CimPOSQrestProductGroupActionDO posQrestProcessGroupAction : posQrestProcessGroupActions) {
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posQrestProcessGroupAction.getAction())) {
                                    continue;
                                }
                                String actionOperationNo = BaseStaticMethod
                                        .convertModuleOpeNoToOpeNo(moduleNo,
                                                posQrestProcessGroupAction.getOperationNumber());
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();

                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posQrestProcessGroupAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posQrestProcessGroupAction.getAction());
                                qTimeActionInfo.setReasonCodeID(
                                        ObjectIdentifier.build(posQrestProcessGroupAction.getReasonCode(),
                                                posQrestProcessGroupAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(actionOperationNo);
                                qTimeActionInfo.setActionRouteID(
                                        Boolean.parseBoolean(qTimeActionInfo
                                                .getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posQrestProcessGroupAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(
                                        ObjectIdentifier.build(posQrestProcessGroupAction.getMainPdID(),
                                                posQrestProcessGroupAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(
                                        ObjectIdentifier.build(posQrestProcessGroupAction.getMsgDefID(),
                                                posQrestProcessGroupAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posQrestProcessGroupAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");

                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
        }
        //==============================================================================
        //   By Technology's qtime definitions
        //==============================================================================
        if (objQtimeCandidateListInRouteGetDRIn.getTechnologyID() != null) {
            if (!CimArrayUtils.isEmpty(triggerModNoList)) {
                for (String triggerModNo : triggerModNoList) {
                    StringBuffer posOPENumber = new StringBuffer();
                    posOPENumber.append(triggerModNo);
                    posOPENumber.append("%");
                    List<Object[]> posAndPosQrestTechs = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTTECH QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO LIKE '%'||?2||'%' " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.TECH_ID = ?3",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString(),
                                    objQtimeCandidateListInRouteGetDRIn.getTechnologyID());
                    if (!CimArrayUtils.isEmpty(posAndPosQrestTechs)) {
                        for (Object[] objects : posAndPosQrestTechs) {
                            //------------------------------------------------------
                            //   Convert moduleNo and moduleOpeNo from openo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are same,
                            //  check target openo is higher than input openo.
                            //---------------------------------------------------------
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);

                            }
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are not same,
                            //  check target moduleno is higher than input moduleno.
                            //---------------------------------------------------------
                            else {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_ROUTESEQ " +
                                                        "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                            }
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }

                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestProductGroupActionDO> posQrestProcessGroupActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTTECH_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestProductGroupActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posQrestProcessGroupActions),
                                    new ErrorCode("pos qrest progroup action list is null !"));
                            for (CimPOSQrestProductGroupActionDO posQrestProcessGroupAction : posQrestProcessGroupActions) {
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posQrestProcessGroupAction.getAction())) {
                                    continue;
                                }

                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posQrestProcessGroupAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posQrestProcessGroupAction.getAction());
                                qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                        .build(posQrestProcessGroupAction.getReasonCode(),
                                                posQrestProcessGroupAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(posQrestProcessGroupAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(Boolean
                                        .parseBoolean(qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posQrestProcessGroupAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                        .build(posQrestProcessGroupAction.getMainPdID(),
                                                posQrestProcessGroupAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(ObjectIdentifier
                                        .build(posQrestProcessGroupAction.getMsgDefID(),
                                                posQrestProcessGroupAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posQrestProcessGroupAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");
                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
            //--------------------------------------------------------------------
            //   Get qtime definitions of MainPOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String triggerModOpeNo : triggerModOpeNoList) {
                    StringBuffer posOPENumber = new StringBuffer();
                    posOPENumber.append(moduleNo);
                    posOPENumber.append(".");
                    posOPENumber.append(triggerModOpeNo);

                    List<Object[]> posAndPosQrestTechs =
                            cimJpaRepository.query("SELECT OMPRSS.OPE_NO,\n" +
                            "                            QREST.REFKEY,\n" +
                            "                            QREST.LINK_KEY,\n" +
                            "                            QREST.DURATION,\n" +
                            "                            QREST.QT_TYPE,\n" +
                            "                            QREST.TARGET_OPE_NO\n" +
                            "                     FROM   OMPRSS, OMPRSS_QTTECH QREST\n" +
                            "                     WHERE  OMPRSS.PRF_RKEY = ?\n" +
                            "                     AND    OMPRSS.OPE_NO = ?\n" +
                            "                     AND    OMPRSS.ID = QREST.REFKEY\n" +
                            "                     AND    QREST.TECH_ID = ?",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString(),
                            objQtimeCandidateListInRouteGetDRIn.getTechnologyID());
                    if (!CimArrayUtils.isEmpty(posAndPosQrestTechs)) {
                        for (Object[] objects : posAndPosQrestTechs) {
                            //------------------------------------------------------
                            //   Convert moduleNo and moduleOpeNo from openo.
                            //------------------------------------------------------
                            String targetModuleNo = BaseStaticMethod
                                    .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                            String targetModuleOpeNo = BaseStaticMethod
                                    .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                            //---------------------------------------------------------
                            //  If target moduleno and input moduleNo are same,
                            //  check target openo is higher than input openo.
                            //---------------------------------------------------------
                            if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                                totalcount = (int) cimJpaRepository
                                        .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                        "WHERE REFKEY = ?1 " +
                                                        "AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                        modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                            } else {
                                totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                                "FROM OMPRF_ROUTESEQ " +
                                                "WHERE REFKEY = ?1 " +
                                                "AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                        mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                            }
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, CimObjectUtils.toString(objects[0]));
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, CimObjectUtils.toString(objects[5]));

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestTechnologyActionDO> posQrestTechnologyActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTTECH_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestTechnologyActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posQrestTechnologyActions),
                                    new ErrorCode("pos qrest tec action list is null !"));
                            for (CimPOSQrestTechnologyActionDO posQrestTechnologyAction : posQrestTechnologyActions) {
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posQrestTechnologyAction.getAction())) {
                                    continue;
                                }

                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(CimNumberUtils
                                        .longValue(posQrestTechnologyAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posQrestTechnologyAction.getAction());
                                qTimeActionInfo.setReasonCodeID(
                                        ObjectIdentifier.build(posQrestTechnologyAction.getReasonCode(),
                                                posQrestTechnologyAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(posQrestTechnologyAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(
                                        Boolean.parseBoolean(qTimeActionInfo
                                                .getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posQrestTechnologyAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(
                                        ObjectIdentifier.build(posQrestTechnologyAction.getMainPdID(),
                                                posQrestTechnologyAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(
                                        ObjectIdentifier.build(posQrestTechnologyAction.getMsgDefID(),
                                                posQrestTechnologyAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posQrestTechnologyAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");
                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
            //--------------------------------------------------------------------
            //   Get qtime definitions of ModulePOS from forward module operation
            //--------------------------------------------------------------------
            if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
                for (String triggerModOpeNo : triggerModOpeNoList) {
                    List<Object[]> posAndPosQrestTech = cimJpaRepository
                            .query("SELECT OMPRSS.OPE_NO, QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, " +
                                            "QREST.QT_TYPE, QREST.TARGET_OPE_NO FROM OMPRSS, OMPRSS_QTTECH QREST " +
                                            "WHERE OMPRSS.PRF_RKEY = ?1 AND OMPRSS.OPE_NO =?2 " +
                                            "AND OMPRSS.ID = QREST.REFKEY AND QREST.TECH_ID = ?3",
                            objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), triggerModOpeNo,
                                    objQtimeCandidateListInRouteGetDRIn.getTechnologyID());
                    //------------------------------------------------------
                    //   Convert openo from moduleNo and moduleOpeNo.
                    //------------------------------------------------------
                    if (!CimArrayUtils.isEmpty(posAndPosQrestTech)) {
                        for (Object[] objects : posAndPosQrestTech) {
                            String triggerOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[0]));
                            String targetOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[5]));
                            totalcount = (int) cimJpaRepository
                                    .count("SELECT COUNT(REFKEY) FROM OMPRF_PRSSSEQ " +
                                                    "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, CimObjectUtils.toString(objects[5]), hFRPF_POSLISTd_SeqNo);
                            if (totalcount == 0) {
                                continue;
                            }
                            strings.set(0, mainPDOI.getValue());
                            strings.set(1, triggerOperationNo);
                            strings.set(2, mainPDOI.getValue());
                            strings.set(3, targetOperationNo);

                            String anOriginalQTime = CimStringUtils
                                    .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                            if (candidateList.containsKey(anOriginalQTime)) {
                                continue;
                            }
                            Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                            strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTriggerOperationNumber(triggerOperationNo);
                            strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                            strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                            strQtimeInfo.setQrestrictionTargetOperationNumber(targetOperationNo);
                            strQtimeInfo.setQrestrictionTargetTimeStamp("");
                            strQtimeInfo.setPreviousTargetInfo("");
                            strQtimeInfo.setQrestrictionRemainTime("");
                            strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                            strQtimeInfo.setSpecificControl("");
                            strQtimeInfo.setOriginalQTime(anOriginalQTime);
                            strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MODULE);
                            strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                            strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                            strQtimeInfo.setQrestrictionTargetBranchInfo("");
                            strQtimeInfo.setQrestrictionTargetReturnInfo("");
                            strQtimeInfo.setWatchDogRequired("");
                            strQtimeInfo.setActionDoneFlag("");
                            strQtimeInfo.setManualCreated(false);
                            strQtimeInfo.setWaferID(new ObjectIdentifier());
                            strQtimeInfo.setPreTrigger(false);
                            strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                            List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                            strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                            //------------------------------------------------------
                            //   Get qtime action
                            //------------------------------------------------------
                            List<CimPOSQrestTechnologyActionDO> posnQrestTechnologyActions =
                                    cimJpaRepository.query("SELECT  DURATION,\n" +
                                    "                                     ACTION,\n" +
                                    "                                     REASON_CODE,\n" +
                                    "                                     REASON_CODE_RKEY,\n" +
                                    "                                     OPE_NO,\n" +
                                    "                                     FH_TIMING,\n" +
                                    "                                     PRP_ID,\n" +
                                    "                                     PRP_RKEY,\n" +
                                    "                                     NOTIFY_ID,\n" +
                                    "                                     NOTIFY_RKEY\n" +
//                                    "                                     CUSTOM_FIELD\n" +
                                    "                             FROM    OMPRSS_QTTECH_ACT\n" +
                                    "                             WHERE   REFKEY = ?\n" +
                                    "                             AND     LINK_MARKER = ?\n" +
                                    "                             ORDER BY IDX_NO",CimPOSQrestTechnologyActionDO.class,
                                    CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                            Validations.check(CimArrayUtils.isEmpty(posnQrestTechnologyActions),
                                    new ErrorCode("pos qrest tec action list is null !"));
                            for (CimPOSQrestTechnologyActionDO posQrestTechnologyAction : posnQrestTechnologyActions) {
                                if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                        && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                        posQrestTechnologyAction.getAction())) {
                                    continue;
                                }

                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                strQtimeActionInfo.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                                qTimeActionInfo.setExpiredTimeDuration(
                                        CimNumberUtils.longValue(posQrestTechnologyAction.getExpiredTime()));
                                qTimeActionInfo.setQrestrictionAction(posQrestTechnologyAction.getAction());
                                qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                        .build(posQrestTechnologyAction.getReasonCode(),
                                                posQrestTechnologyAction.getReasonCodeObj()));
                                qTimeActionInfo.setActionOperationNumber(
                                        posQrestTechnologyAction.getOperationNumber());
                                qTimeActionInfo.setActionRouteID(Boolean
                                        .parseBoolean(qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                                qTimeActionInfo.setFutureHoldTiming(posQrestTechnologyAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                        .build(posQrestTechnologyAction.getMainPdID(),
                                                posQrestTechnologyAction.getMainPdObj()));
                                qTimeActionInfo.setMessageID(ObjectIdentifier
                                        .build(posQrestTechnologyAction.getMsgDefID(),
                                                posQrestTechnologyAction.getMsgDefObj()));
                                /*qTimeActionInfo.setCustomField(posQrestTechnologyAction.getCustomField());*/
                                qTimeActionInfo.setWatchDogRequired("");
                                qTimeActionInfo.setActionDoneFlag("");
                            }
                            candidateList.put(anOriginalQTime, strQtimeInfo);
                        }
                    }
                }
            }
        }
        //==============================================================================
        //   Default qtime definitions
        //==============================================================================
        //--------------------------------------------------------------------
        //   Get qtime definitions of MainPOS from forward module
        //--------------------------------------------------------------------
        if (!CimArrayUtils.isEmpty(triggerModNoList)) {
            for (String triggerModNo : triggerModNoList) {
                StringBuffer posOPENumber = new StringBuffer();
                posOPENumber.append(triggerModNo);
                posOPENumber.append(".%");
                List<Object[]> posAndPosQrestDefaults = cimJpaRepository.query("SELECT OMPRSS.OPE_NO, " +
                                "QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, QREST.QT_TYPE, QREST.TARGET_OPE_NO " +
                                "FROM OMPRSS,OMPRSS_QT QREST " +
                                "WHERE OMPRSS.PRF_RKEY =?1 AND OMPRSS.OPE_NO LIKE ?2 AND OMPRSS.ID = QREST.REFKEY",
                        objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString());
                if (!CimArrayUtils.isEmpty(posAndPosQrestDefaults)) {
                    for (Object[] objects : posAndPosQrestDefaults) {
                        //------------------------------------------------------
                        //   Convert moduleNo and moduleOpeNo from openo.
                        //------------------------------------------------------
                        String targetModuleNo = BaseStaticMethod
                                .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                        String targetModuleOpeNo = BaseStaticMethod
                                .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                        if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                        } else {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_ROUTESEQ WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                    mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                        }
                        if (totalcount == 0) {
                            continue;
                        }
                        strings.set(0, mainPDOI.getValue());
                        strings.set(1, CimObjectUtils.toString(objects[0]));
                        strings.set(2, mainPDOI.getValue());
                        strings.set(3, CimObjectUtils.toString(objects[5]));

                        String anOriginalQTime = CimStringUtils
                                .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                        if (candidateList.containsKey(anOriginalQTime)) {
                            continue;
                        }
                        Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                        strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                        strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                        strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                        strQtimeInfo.setQrestrictionTargetTimeStamp("");
                        strQtimeInfo.setPreviousTargetInfo("");
                        strQtimeInfo.setQrestrictionRemainTime("");
                        strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                        strQtimeInfo.setSpecificControl("");
                        strQtimeInfo.setOriginalQTime(anOriginalQTime);
                        strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                        strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                        strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                        strQtimeInfo.setQrestrictionTargetBranchInfo("");
                        strQtimeInfo.setQrestrictionTargetReturnInfo("");
                        strQtimeInfo.setWatchDogRequired("");
                        strQtimeInfo.setActionDoneFlag("");
                        strQtimeInfo.setManualCreated(false);
                        strQtimeInfo.setWaferID(new ObjectIdentifier());
                        strQtimeInfo.setPreTrigger(false);
                        strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                        List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                        //------------------------------------------------------
                        //   Get qtime action
                        //------------------------------------------------------
                        List<CimPOSQrestDefaultActionDO> posQrestDefaultActions =
                                cimJpaRepository.query("SELECT  DURATION,\n" +
                                "                                 ACTION,\n" +
                                "                                 REASON_CODE,\n" +
                                "                                 REASON_CODE_RKEY,\n" +
                                "                                 OPE_NO,\n" +
                                "                                 FH_TIMING,\n" +
                                "                                 PRP_ID,\n" +
                                "                                 PRP_RKEY,\n" +
                                "                                 NOTIFY_ID,\n" +
                                "                                 NOTIFY_RKEY\n" +
//                                "                                 CUSTOM_FIELD\n" +
                                "                         FROM    OMPRSS_QT_ACT\n" +
                                "                         WHERE   REFKEY = ?\n" +
                                "                         AND     LINK_MARKER = ?\n" +
                                "                         ORDER BY IDX_NO",CimPOSQrestDefaultActionDO.class,
                                CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                        Validations.check(CimArrayUtils.isEmpty(posQrestDefaultActions),
                                new ErrorCode("pos qrest def action list is null !"));
                        for (CimPOSQrestDefaultActionDO posQrestDefaultAction : posQrestDefaultActions) {
                            if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                    && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                    posQrestDefaultAction.getAction())) {
                                continue;
                            }

                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            strQtimeActionInfo.add(qTimeActionInfo);
                            qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                            qTimeActionInfo.setExpiredTimeDuration(
                                    CimLongUtils.longValue(posQrestDefaultAction.getExpiredTime()));
                            qTimeActionInfo.setQrestrictionAction(posQrestDefaultAction.getAction());
                            qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getReasonCode(),
                                            posQrestDefaultAction.getReasonCodeObj()));
                            qTimeActionInfo.setActionOperationNumber(posQrestDefaultAction.getOperationNumber());
                            qTimeActionInfo.setActionRouteID(Boolean.parseBoolean(
                                    qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                            qTimeActionInfo.setFutureHoldTiming(posQrestDefaultAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(ObjectIdentifier.build(
                                    posQrestDefaultAction.getMainPdID(), posQrestDefaultAction.getMainPdObj()));
                            qTimeActionInfo.setMessageID(ObjectIdentifier.build(
                                    posQrestDefaultAction.getMsgDefID(), posQrestDefaultAction.getMsgDefObj()));
                            /*qTimeActionInfo.setCustomField(posQrestDefaultAction.getCustomField());*/
                            qTimeActionInfo.setWatchDogRequired("");
                            qTimeActionInfo.setActionDoneFlag("");
                        }
                        candidateList.put(anOriginalQTime, strQtimeInfo);
                    }
                }
            }
        }
        //--------------------------------------------------------------------
        //   Get qtime definitions of MainPOS from forward module operation
        //--------------------------------------------------------------------
        if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
            for (String triggerModOpeNo : triggerModOpeNoList) {
                StringBuffer posOPENumber = new StringBuffer();
                posOPENumber.append(moduleNo);
                posOPENumber.append(".");
                posOPENumber.append(triggerModOpeNo);

                List<Object[]> posAndPosQrestDefaults =
                        cimJpaRepository.query("SELECT OMPRSS.OPE_NO,\n" +
                        "                        QREST.REFKEY,\n" +
                        "                        QREST.LINK_KEY,\n" +
                        "                        QREST.DURATION,\n" +
                        "                        QREST.QT_TYPE,\n" +
                        "                        QREST.TARGET_OPE_NO\n" +
                        "                 FROM   OMPRSS, OMPRSS_QT QREST\n" +
                        "                 WHERE  OMPRSS.PRF_RKEY = ?\n" +
                        "                 AND    OMPRSS.OPE_NO = ?\n" +
                        "                 AND    OMPRSS.ID = QREST.REFKEY",
                        objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString());
                if (!CimArrayUtils.isEmpty(posAndPosQrestDefaults)) {
                    for (Object[] objects : posAndPosQrestDefaults) {
                        //------------------------------------------------------
                        //   Convert moduleNo and moduleOpeNo from openo.
                        //------------------------------------------------------
                        String targetModuleNo = BaseStaticMethod
                                .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                        String targetModuleOpeNo = BaseStaticMethod
                                .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                        if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 " +
                                            "AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                        } else {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_ROUTESEQ WHERE REFKEY = ?1 " +
                                            "AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                    mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                        }
                        if (totalcount == 0) {
                            continue;
                        }
                        strings.set(0, mainPDOI.getValue());
                        strings.set(1, CimObjectUtils.toString(objects[0]));
                        strings.set(2, mainPDOI.getValue());
                        strings.set(3, CimObjectUtils.toString(objects[5]));
                        String anOriginalQTime = CimStringUtils
                                .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                        if (candidateList.containsKey(anOriginalQTime)) {
                            continue;
                        }
                        Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                        strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTriggerOperationNumber(
                                CimObjectUtils.toString(objects[0]));
                        strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                        strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTargetOperationNumber(
                                CimObjectUtils.toString(objects[5]));
                        strQtimeInfo.setQrestrictionTargetTimeStamp("");
                        strQtimeInfo.setPreviousTargetInfo("");
                        strQtimeInfo.setQrestrictionRemainTime("");
                        strQtimeInfo.setExpiredTimeDuration(Long.valueOf(
                                CimObjectUtils.toString(objects[3])));
                        strQtimeInfo.setSpecificControl("");
                        strQtimeInfo.setOriginalQTime(anOriginalQTime);
                        strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                        strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                        strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                        strQtimeInfo.setQrestrictionTargetBranchInfo("");
                        strQtimeInfo.setQrestrictionTargetReturnInfo("");
                        strQtimeInfo.setWatchDogRequired("");
                        strQtimeInfo.setActionDoneFlag("");
                        strQtimeInfo.setManualCreated(false);
                        strQtimeInfo.setWaferID(new ObjectIdentifier());
                        strQtimeInfo.setPreTrigger(false);
                        strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));
                        List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                        //------------------------------------------------------
                        //   Get qtime action
                        //------------------------------------------------------
                        List<CimPOSQrestDefaultActionDO> posQrestDefaultActions =
                                cimJpaRepository.query("SELECT  DURATION,\n" +
                                "                                 ACTION,\n" +
                                "                                 REASON_CODE,\n" +
                                "                                 REASON_CODE_RKEY,\n" +
                                "                                 OPE_NO,\n" +
                                "                                 FH_TIMING,\n" +
                                "                                 PRP_ID,\n" +
                                "                                 PRP_RKEY,\n" +
                                "                                 NOTIFY_ID,\n" +
                                "                                 NOTIFY_RKEY\n" +
//                                "                                 CUSTOM_FIELD\n" +
                                "                         FROM    OMPRSS_QT_ACT\n" +
                                "                         WHERE   REFKEY = ?\n" +
                                "                         AND     LINK_MARKER = ?\n" +
                                "                         ORDER BY IDX_NO",CimPOSQrestDefaultActionDO.class,
                                CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                        Validations.check(CimArrayUtils.isEmpty(posQrestDefaultActions),
                                new ErrorCode("pos qrest def action list is null !"));
                        for (CimPOSQrestDefaultActionDO posQrestDefaultAction : posQrestDefaultActions) {
                            if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                    && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                    posQrestDefaultAction.getAction())) {
                                continue;
                            }

                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            strQtimeActionInfo.add(qTimeActionInfo);
                            qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                            qTimeActionInfo.setExpiredTimeDuration(CimLongUtils.longValue(
                                    posQrestDefaultAction.getExpiredTime()));
                            qTimeActionInfo.setQrestrictionAction(posQrestDefaultAction.getAction());
                            qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getReasonCode(),
                                            posQrestDefaultAction.getReasonCodeObj()));
                            qTimeActionInfo.setActionOperationNumber(posQrestDefaultAction.getOperationNumber());
                            qTimeActionInfo.setActionRouteID(Boolean
                                    .parseBoolean(qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                            qTimeActionInfo.setFutureHoldTiming(posQrestDefaultAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMainPdID(), posQrestDefaultAction.getMainPdObj()));
                            qTimeActionInfo.setMessageID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMsgDefID(), posQrestDefaultAction.getMsgDefObj()));
                            /*qTimeActionInfo.setCustomField(posQrestDefaultAction.getCustomField());*/
                            qTimeActionInfo.setWatchDogRequired("");
                            qTimeActionInfo.setActionDoneFlag("");
                        }
                        candidateList.put(anOriginalQTime, strQtimeInfo);
                    }
                }
            }
        }
        //--------------------------------------------------------------------
        //   Get qtime definitions of MainPOS from forward module operation
        //--------------------------------------------------------------------
        if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
            for (String triggerModOpeNo : triggerModOpeNoList) {
                StringBuffer posOPENumber = new StringBuffer();
                posOPENumber.append(moduleNo);
                posOPENumber.append(".");
                posOPENumber.append(triggerModOpeNo);

                List<Object[]> posAndPosQrestDefaults = cimJpaRepository.query("SELECT OMPRSS.OPE_NO, " +
                                "QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, QREST.QT_TYPE, " +
                                "QREST.TARGET_OPE_NO FROM OMPRSS,OMPRSS_QT QREST " +
                                "WHERE OMPRSS.PRF_RKEY =?1 AND OMPRSS.OPE_NO =?2 AND OMPRSS.ID = QREST.REFKEY",
                        objQtimeCandidateListInRouteGetDRIn.getProcessFlow(), posOPENumber.toString());
                if (!CimArrayUtils.isEmpty(posAndPosQrestDefaults)) {
                    for (Object[] objects : posAndPosQrestDefaults) {
                        String targetModuleNo = BaseStaticMethod
                                .convertOpeNoToModuleNo(CimObjectUtils.toString(objects[5]));
                        String targetModuleOpeNo = BaseStaticMethod
                                .convertOpeNoToModuleOpeNo(CimObjectUtils.toString(objects[5]));
                        //---------------------------------------------------------
                        //  If target moduleno and input moduleNo are same,
                        //  check target openo is higher than input openo.
                        //---------------------------------------------------------
                        if (CimStringUtils.equals(targetModuleNo, moduleNo)) {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_PRSSSEQ " +
                                            "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                    modulePfdTheSystemKey, targetModuleOpeNo, hFRPF_POSLISTd_SeqNo);
                        } else {
                            totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                            "FROM OMPRF_ROUTESEQ " +
                                            "WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3",
                                    mainPfdTheSystemKey, targetModuleNo, sequenceNumber);
                        }
                        if (totalcount == 0) {
                            continue;
                        }
                        strings.set(0, mainPDOI.getValue());
                        strings.set(1, CimObjectUtils.toString(objects[0]));
                        strings.set(2, mainPDOI.getValue());
                        strings.set(3, CimObjectUtils.toString(objects[5]));
                        String anOriginalQTime = CimStringUtils
                                .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                        if (candidateList.containsKey(anOriginalQTime)) {
                            continue;
                        }
                        Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                        strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTriggerOperationNumber(CimObjectUtils.toString(objects[0]));
                        strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                        strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTargetOperationNumber(CimObjectUtils.toString(objects[5]));
                        strQtimeInfo.setQrestrictionTargetTimeStamp("");
                        strQtimeInfo.setPreviousTargetInfo("");
                        strQtimeInfo.setQrestrictionRemainTime("");
                        strQtimeInfo.setExpiredTimeDuration(Long.valueOf(CimObjectUtils.toString(objects[3])));
                        strQtimeInfo.setSpecificControl("");
                        strQtimeInfo.setOriginalQTime(anOriginalQTime);
                        strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
                        strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                        strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                        strQtimeInfo.setQrestrictionTargetBranchInfo("");
                        strQtimeInfo.setQrestrictionTargetReturnInfo("");
                        strQtimeInfo.setWatchDogRequired("");
                        strQtimeInfo.setActionDoneFlag("");
                        strQtimeInfo.setManualCreated(false);
                        strQtimeInfo.setWaferID(new ObjectIdentifier());
                        strQtimeInfo.setPreTrigger(false);
                        strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                        List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                        //------------------------------------------------------
                        //   Get qtime action
                        //------------------------------------------------------
                        List<CimPOSQrestDefaultActionDO> posQrestDefaultActions =
                                cimJpaRepository.query("SELECT  DURATION,\n" +
                                "                                 ACTION,\n" +
                                "                                 REASON_CODE,\n" +
                                "                                 REASON_CODE_RKEY,\n" +
                                "                                 OPE_NO,\n" +
                                "                                 FH_TIMING,\n" +
                                "                                 PRP_ID,\n" +
                                "                                 PRP_RKEY,\n" +
                                "                                 NOTIFY_ID,\n" +
                                "                                 NOTIFY_RKEY\n" +
//                                "                                 CUSTOM_FIELD\n" +
                                "                         FROM    OMPRSS_QT_ACT\n" +
                                "                         WHERE   REFKEY = ?\n" +
                                "                         AND     LINK_MARKER = ?\n" +
                                "                         ORDER BY IDX_NO",CimPOSQrestDefaultActionDO.class,
                                CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                        Validations.check(CimArrayUtils.isEmpty(posQrestDefaultActions),
                                new ErrorCode("pos qrest def action list is null !"));
                        for (CimPOSQrestDefaultActionDO posQrestDefaultAction : posQrestDefaultActions) {
                            if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                    && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                    posQrestDefaultAction.getAction())) {
                                continue;
                            }

                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            strQtimeActionInfo.add(qTimeActionInfo);
                            qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                            qTimeActionInfo.setExpiredTimeDuration(
                                    CimNumberUtils.longValue(posQrestDefaultAction.getExpiredTime()));
                            qTimeActionInfo.setQrestrictionAction(posQrestDefaultAction.getAction());
                            qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getReasonCode(),
                                            posQrestDefaultAction.getReasonCodeObj()));
                            qTimeActionInfo.setActionOperationNumber(posQrestDefaultAction.getOperationNumber());
                            qTimeActionInfo.setActionRouteID(Boolean
                                    .parseBoolean(qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                            qTimeActionInfo.setFutureHoldTiming(posQrestDefaultAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMainPdID(), posQrestDefaultAction.getMainPdObj()));
                            qTimeActionInfo.setMessageID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMsgDefID(), posQrestDefaultAction.getMsgDefObj()));
                            /*qTimeActionInfo.setCustomField(posQrestDefaultAction.getCustomField());*/
                            qTimeActionInfo.setWatchDogRequired("");
                            qTimeActionInfo.setActionDoneFlag("");
                        }
                        candidateList.put(anOriginalQTime, strQtimeInfo);
                    }
                }
            }
        }
        //--------------------------------------------------------------------
        //   Get qtime definitions of ModulePOS from forward module operation
        //--------------------------------------------------------------------
        if (!CimArrayUtils.isEmpty(triggerModOpeNoList)) {
            for (String triggerModOpeNo : triggerModOpeNoList) {
                List<Object[]> posAndPosQrestDefaults = cimJpaRepository.query("SELECT OMPRSS.OPE_NO, " +
                                "QREST.REFKEY, QREST.LINK_KEY, QREST.DURATION, QREST.QT_TYPE, QREST.TARGET_OPE_NO " +
                                "FROM OMPRSS,OMPRSS_QT QREST " +
                                "WHERE OMPRSS.PRF_RKEY =?1 AND OMPRSS.OPE_NO =?2 AND OMPRSS.ID = QREST.REFKEY",
                        objQtimeCandidateListInRouteGetDRIn.getModuleProcessFlow(), triggerModOpeNo);
                if (!CimArrayUtils.isEmpty(posAndPosQrestDefaults)) {
                    for (Object[] objects : posAndPosQrestDefaults) {
                        String triggerOperationNo = BaseStaticMethod
                                .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[0]));
                        String targetOperationNo = BaseStaticMethod
                                .convertModuleOpeNoToOpeNo(moduleNo, CimObjectUtils.toString(objects[5]));
                        totalcount = (int) cimJpaRepository.count("SELECT COUNT(REFKEY) " +
                                        "FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 AND LINK_KEY = ?2 AND IDX_NO >= ?3 ",
                                modulePfdTheSystemKey, CimObjectUtils.toString(objects[5]), hFRPF_POSLISTd_SeqNo);
                        if (totalcount == 0) {
                            continue;
                        }
                        strings.set(0, mainPDOI.getValue());
                        strings.set(1, triggerOperationNo);
                        strings.set(2, mainPDOI.getValue());
                        strings.set(3, targetOperationNo);
                        String anOriginalQTime = CimStringUtils
                                .arrayToDelimitedString(strings.toArray(), BizConstant.SP_KEY_SEPARATOR_DOT);
                        if (candidateList.containsKey(anOriginalQTime)) {
                            continue;
                        }
                        Infos.QrestTimeInfo strQtimeInfo = new Infos.QrestTimeInfo();
                        strQtimeInfo.setQrestrictionTriggerRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTriggerOperationNumber(triggerOperationNo);
                        strQtimeInfo.setQrestrictionTriggerTimeStamp("");
                        strQtimeInfo.setQrestrictionTargetRouteID(mainPDOI);
                        strQtimeInfo.setQrestrictionTargetOperationNumber(targetOperationNo);
                        strQtimeInfo.setQrestrictionTargetTimeStamp("");
                        strQtimeInfo.setPreviousTargetInfo("");
                        strQtimeInfo.setQrestrictionRemainTime("");
                        strQtimeInfo.setExpiredTimeDuration(
                                Long.valueOf(CimObjectUtils.toString(objects[3])));
                        strQtimeInfo.setSpecificControl("");
                        strQtimeInfo.setOriginalQTime(anOriginalQTime);
                        strQtimeInfo.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MODULE);
                        strQtimeInfo.setQrestrictionTriggerBranchInfo("");
                        strQtimeInfo.setQrestrictionTriggerReturnInfo("");
                        strQtimeInfo.setQrestrictionTargetBranchInfo("");
                        strQtimeInfo.setQrestrictionTargetReturnInfo("");
                        strQtimeInfo.setWatchDogRequired("");
                        strQtimeInfo.setActionDoneFlag("");
                        strQtimeInfo.setManualCreated(false);
                        strQtimeInfo.setWaferID(new ObjectIdentifier());
                        strQtimeInfo.setPreTrigger(false);
                        strQtimeInfo.setQTimeType(CimObjectUtils.toString(objects[4]));

                        List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);

                        //------------------------------------------------------
                        //   Get qtime action
                        //------------------------------------------------------
                        List<CimPOSQrestDefaultActionDO> posQrestDefaultActions =
                                cimJpaRepository.query("SELECT  DURATION,\n" +
                                "                                 ACTION,\n" +
                                "                                 REASON_CODE,\n" +
                                "                                 REASON_CODE_RKEY,\n" +
                                "                                 OPE_NO,\n" +
                                "                                 FH_TIMING,\n" +
                                "                                 PRP_ID,\n" +
                                "                                 PRP_RKEY,\n" +
                                "                                 NOTIFY_ID,\n" +
                                "                                 NOTIFY_RKEY\n" +
//                                "                                 CUSTOM_FIELD\n" +
                                "                         FROM    OMPRSS_QT_ACT\n" +
                                "                         WHERE   REFKEY = ?\n" +
                                "                         AND     LINK_MARKER = ?\n" +
                                "                         ORDER BY IDX_NO",CimPOSQrestDefaultActionDO.class,
                                CimObjectUtils.toString(objects[1]), CimObjectUtils.toString(objects[2]));
                        Validations.check(CimArrayUtils.isEmpty(posQrestDefaultActions),
                                new ErrorCode("pos qrest def action list is null !"));
                        for (CimPOSQrestDefaultActionDO posQrestDefaultAction : posQrestDefaultActions) {
                            if (qtimeDispatchPrecedeUseCustomFieldFlag == 0
                                    && CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                    posQrestDefaultAction.getAction())) {
                                continue;
                            }
                            String actionOperationNo = BaseStaticMethod
                                    .convertModuleOpeNoToOpeNo(moduleNo, posQrestDefaultAction.getOperationNumber());
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            strQtimeActionInfo.add(qTimeActionInfo);
                            qTimeActionInfo.setQrestrictionTargetTimeStamp("");
                            qTimeActionInfo.setExpiredTimeDuration(
                                    CimNumberUtils.longValue(posQrestDefaultAction.getExpiredTime()));
                            qTimeActionInfo.setQrestrictionAction(posQrestDefaultAction.getAction());
                            qTimeActionInfo.setReasonCodeID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getReasonCode(),
                                            posQrestDefaultAction.getReasonCodeObj()));
                            qTimeActionInfo.setActionOperationNumber(actionOperationNo);
                            qTimeActionInfo.setActionRouteID(Boolean.parseBoolean(
                                    qTimeActionInfo.getActionOperationNumber()) ? mainPDOI : blankID);
                            qTimeActionInfo.setFutureHoldTiming(posQrestDefaultAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMainPdID(), posQrestDefaultAction.getMainPdObj()));
                            qTimeActionInfo.setMessageID(ObjectIdentifier
                                    .build(posQrestDefaultAction.getMsgDefID(), posQrestDefaultAction.getMsgDefObj()));
                            /*qTimeActionInfo.setCustomField(posQrestDefaultAction.getCustomField());*/
                            qTimeActionInfo.setWatchDogRequired("");
                            qTimeActionInfo.setActionDoneFlag("");
                        }
                        candidateList.put(anOriginalQTime, strQtimeInfo);
                    }
                }
            }
        }
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        List<Infos.QrestTimeInfo> strQtimeInfoList = new ArrayList<>();

        Set<Map.Entry<String, Infos.QrestTimeInfo>> entries = candidateList.entrySet();
        Iterator<Map.Entry<String, Infos.QrestTimeInfo>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Infos.QrestTimeInfo> next = iterator.next();
            Infos.QrestTimeInfo value = next.getValue();
            strQtimeInfoList.add(value);
        }

        return strQtimeInfoList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strStartCassette
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/1 14:00:51
     */
    @Override
    public void qtimeStopByOpeStart(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassette) {
        int scLen = CimArrayUtils.getSize(strStartCassette);
        for (int i = 0; i < scLen; i++) {
            Infos.StartCassette startCassette = strStartCassette.get(i);
            String loadPurposeType = startCassette.getLoadPurposeType();
            if (CimStringUtils.equals(loadPurposeType, SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                continue;
            }

            List<Infos.LotInCassette> strLotInCassette = startCassette.getLotInCassetteList();
            int lcLen = CimArrayUtils.getSize(strLotInCassette);
            for (int j = 0; j < lcLen; j++) {
                Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                boolean operationStartFlag;
                operationStartFlag = CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag());
                if (!operationStartFlag) {
                    continue;
                }
                com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                String currentOperationNumber;
                currentOperationNumber = aLot.getOperationNumber();

                List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeq = aLot.findQTimeRestrictionsFor(currentOperationNumber);
                int WFQTSLength = 0;
                List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> WaferLevelQtimeSeq = null;

                int WaferLevelQtimeForWaferCount = 0;
                WaferLevelQtimeForWaferCount = aLot.getWaferLevelQTimeCount();

                if (WaferLevelQtimeForWaferCount > 0) {
                    WaferLevelQtimeSeq = aLot.findQTimeRestrictionsForWaferLevelQTime(currentOperationNumber);
                    WFQTSLength = CimArrayUtils.getSize(WaferLevelQtimeSeq);
                }

                int LotQTSLength = CimArrayUtils.getSize(qtimeSeq);
                for (int kk = 0; kk < WFQTSLength; kk++) {
                    qtimeSeq.add(WaferLevelQtimeSeq.get(kk));
                }

                int QTSLength;
                QTSLength = CimArrayUtils.getSize(qtimeSeq);
                for (int k = 0; k < QTSLength; k++) {
                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction qTime = qtimeSeq.get(k);
                    qTime.makeWatchDogNotRequired();
                    List<ProcessDTO.QTimeRestrictionAction> aQTimeRestrictionActionSeq = qTime.getQTimeRestrictionActions();
                    for (int iCnt1 = 0; iCnt1 < CimArrayUtils.getSize(aQTimeRestrictionActionSeq); iCnt1++) {
                        ProcessDTO.QTimeRestrictionAction qTimeAction = aQTimeRestrictionActionSeq.get(iCnt1);
                        qTimeAction.setWatchdogRequired(false);
                    }

                    ProcessDTO.QTimeRestrictionInfo qTimeRestrictionInfo = qTime.getQTimeRestrictionInfo();
                    Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                    strQtimeInfo.setWaferID(qTimeRestrictionInfo.getWaferID());
                    strQtimeInfo.setQTimeType(qTimeRestrictionInfo.getQTimeType());
                    strQtimeInfo.setPreTrigger(qTimeRestrictionInfo.getPreTrigger());

                    strQtimeInfo.setOriginalQTime(qTimeRestrictionInfo.getOriginalQTime());
                    strQtimeInfo.setProcessDefinitionLevel(qTimeRestrictionInfo.getProcessDefinitionLevel());
                    strQtimeInfo.setRestrictionTriggerRouteID(qTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    strQtimeInfo.setRestrictionTriggerOperationNumber(qTimeRestrictionInfo.getTriggerOperationNumber());
                    strQtimeInfo.setRestrictionTriggerBranchInfo(qTimeRestrictionInfo.getTriggerBranchInfo());
                    strQtimeInfo.setRestrictionTriggerReturnInfo(qTimeRestrictionInfo.getTriggerReturnInfo());
                    strQtimeInfo.setRestrictionTriggerTimeStamp(qTimeRestrictionInfo.getTriggerTimeStamp());
                    strQtimeInfo.setRestrictionTargetRouteID(qTimeRestrictionInfo.getTargetMainProcessDefinition());
                    strQtimeInfo.setRestrictionTargetOperationNumber(qTimeRestrictionInfo.getTargetOperationNumber());
                    strQtimeInfo.setRestrictionTargetBranchInfo(qTimeRestrictionInfo.getTargetBranchInfo());
                    strQtimeInfo.setRestrictionTargetReturnInfo(qTimeRestrictionInfo.getTargetReturnInfo());
                    strQtimeInfo.setRestrictionTargetTimeStamp(qTimeRestrictionInfo.getTargetTimeStamp());
                    strQtimeInfo.setPreviousTargetInfo(qTimeRestrictionInfo.getPreviousTargetInfo());
                    strQtimeInfo.setSpecificControl(qTimeRestrictionInfo.getControl());

                    boolean watchdogRequired = CimBooleanUtils.isTrue(qTimeRestrictionInfo.getWatchdogRequired());

                    if (CimBooleanUtils.isTrue(qTimeRestrictionInfo.getWatchdogRequired())) {
                        strQtimeInfo.setWatchDogRequired("Y");
                    } else if (!watchdogRequired) {
                        strQtimeInfo.setWatchDogRequired("N");
                    }

                    if (CimBooleanUtils.isTrue(qTimeRestrictionInfo.getActionDone())) {
                        strQtimeInfo.setActionDoneFlag("Y");
                    } else if (!CimBooleanUtils.isTrue(qTimeRestrictionInfo.getActionDone())) {
                        strQtimeInfo.setActionDoneFlag("N");
                    }

                    strQtimeInfo.setManualCreated(qTimeRestrictionInfo.getManualCreated());
                    List<ProcessDTO.QTimeRestrictionAction> actions = qTimeRestrictionInfo.getActions();

                    int actionLength = CimArrayUtils.getSize(actions);

                    if (actionLength != 0) {
                        List<Infos.QTimeActionInfo> strQtimeActionInfo = new ArrayList<>();
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfo);
                        for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                            Infos.QTimeActionInfo qtimeActionInfo = new Infos.QTimeActionInfo();
                            ProcessDTO.QTimeRestrictionAction action = actions.get(iCnt3);
                            strQtimeActionInfo.add(qtimeActionInfo);
                            qtimeActionInfo.setQrestrictionTargetTimeStamp(action.getTargetTimeStamp());
                            qtimeActionInfo.setQrestrictionAction(action.getAction());
                            qtimeActionInfo.setReasonCodeID(action.getReasonCode());
                            qtimeActionInfo.setActionRouteID(action.getActionRouteID());
                            qtimeActionInfo.setActionOperationNumber(action.getOperationNumber());
                            qtimeActionInfo.setFutureHoldTiming(action.getTiming());
                            qtimeActionInfo.setReworkRouteID(action.getMainProcessDefinition());
                            qtimeActionInfo.setMessageID(action.getMessageDefinition());
                            qtimeActionInfo.setCustomField(action.getCustomField());

                            if (CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                                qtimeActionInfo.setWatchDogRequired("Y");
                            } else if (!CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                                qtimeActionInfo.setWatchDogRequired("N");
                            }

                            if (CimBooleanUtils.isTrue(action.getActionDone())) {
                                qtimeActionInfo.setActionDoneFlag("Y");
                            } else if (!CimBooleanUtils.isTrue(action.getActionDone())) {
                                qtimeActionInfo.setActionDoneFlag("N");
                            }
                        }
                    }

                    Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                    params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                    params.setLotID(lotInCassette.getLotID());
                    params.setQtimeInfo(strQtimeInfo);
                    params.setClaimMemo("");
                    eventMethod.qTimeChangeEventMake(objCommon, params);
                }
            }
        }
    }

    @Override
    public void qTimeCheckForMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        log.info("【Method Entry】qTimeCheckForMerge()");
        // Input Parameter;
        log.info("in-para parentLotID = {}", parentLotID.getValue());
        log.info("in-para childLotID = {} ", childLotID.getValue());

        //  Check input parameter;
        if (CimObjectUtils.isEmpty(parentLotID.getValue())
                && CimObjectUtils.isEmpty(childLotID.getValue())) {
            log.info("Invalid input parameter.");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        String qTimeMergeRule = StandardProperties.OM_QT_INFO_MERGE_RULE.getValue();
        log.info("qTimeMergeRule = {}", qTimeMergeRule);
        if (!CimStringUtils.equals("1", qTimeMergeRule)) {
            log.info("qTimeMergeRule != 1");
            return;
        }

        //Get lot Object;
        CimLot aParentLot = baseCoreFactory.getBO(CimLot.class, parentLotID);
        Validations.check(aParentLot == null, retCodeConfig.getNotFoundLot());
        CimLot aChildLot = baseCoreFactory.getBO(CimLot.class, childLotID);
        Validations.check(aChildLot == null, retCodeConfig.getNotFoundLot());

        List<CimQTimeRestriction> aParentQTimeList = aParentLot.allQTimeRestrictions();
        List<CimQTimeRestriction> aChildQTimeList = aChildLot.allQTimeRestrictions();

        //Get process flow context;
        CimProcessFlowContext aParentPFX = aParentLot.getProcessFlowContext();
        Validations.check(aParentPFX == null, retCodeConfig.getNotFoundPfx());

        CimProcessFlowContext aChildPFX = aChildLot.getProcessFlowContext();
        Validations.check(aChildPFX == null, retCodeConfig.getNotFoundPfx());

        List<CimQTimeRestriction> aParentCheckQTimeList = new ArrayList<>();
        List<CimQTimeRestriction> aChildCheckQTimeList = new ArrayList<>();

        int nQTimeLen = CimArrayUtils.getSize(aParentQTimeList);
        int nQTimeNo = 0;
        for (nQTimeNo = 0; nQTimeNo < nQTimeLen; nQTimeNo++) {
            log.info("nQTimeLen/nQTimeNo : {}/{}", nQTimeLen, nQTimeNo);
            CimQTimeRestriction aQTime = aParentQTimeList.get(nQTimeNo);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());
            Boolean bIsInQTimeIntervalFlag = false;
            bIsInQTimeIntervalFlag= aParentPFX.isInQTimeInterval(aQTime);

            if (CimBooleanUtils.isTrue(bIsInQTimeIntervalFlag)) {
                log.info("bIsInQTimeIntervalFlag = true");
                aParentCheckQTimeList.add(aQTime);
            }
        }

        nQTimeLen = CimArrayUtils.getSize(aChildQTimeList);
        for (nQTimeNo = 0; nQTimeNo < nQTimeLen; nQTimeNo++) {
            log.info("nQTimeLen/nQTimeNo = {}/{}", nQTimeLen, nQTimeNo);
            CimQTimeRestriction aQTime = aChildQTimeList.get(nQTimeNo);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());
            Boolean bIsInQTimeIntervalFlag = false;
            bIsInQTimeIntervalFlag = aChildPFX.isInQTimeInterval(aQTime);

            if (CimBooleanUtils.isTrue(bIsInQTimeIntervalFlag)) {
                log.info("bIsInQTimeIntervalFlag = true");
                aChildCheckQTimeList.add(aQTime);
            }
        }

        if (CimArrayUtils.getSize(aParentCheckQTimeList) != CimArrayUtils.getSize(aChildCheckQTimeList)) {
            log.info("aParentCheckQTimeSeq.length() != aChildCheckQTimeSeq.length()");
            Validations.check(retCodeConfig.getDifferentQtimeInformation());
        }

        int nChildQTimeLen = CimArrayUtils.getSize(aChildCheckQTimeList);
        for (int nChildQTimeNo = 0; nChildQTimeNo < nChildQTimeLen; nChildQTimeNo++) {
            log.info("nChildQTimeLen/nChildQTimeNo = {}/{}", nChildQTimeLen, nChildQTimeNo);

            //Get Q-Time information ;
            CimQTimeRestriction aChildQTime = aChildCheckQTimeList.get(nChildQTimeNo);
            ProcessDTO.QTimeRestrictionInfo aChildQTimeInfo = aChildQTime.getQTimeRestrictionInfo();

            if (aChildQTimeInfo == null || CimObjectUtils.isEmpty(aChildQTimeInfo.getTriggerOperationNumber())) {
                Validations.check(retCodeConfig.getNotFoundQtime());
            }

            Duration aChildDuration = Duration.ZERO;
            if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aChildQTimeInfo.getTargetTimeStamp())) {
                log.info("aChildQTimeInfo->targetTimeStamp != SP_TIMESTAMP_NIL_OBJECT_STRING");
                Timestamp targetTime = Timestamp.valueOf(aChildQTimeInfo.getTargetTimeStamp());
                Timestamp triggerTime = Timestamp.valueOf(aChildQTimeInfo.getTriggerTimeStamp());
                aChildDuration = Duration.between(targetTime.toLocalDateTime(), triggerTime.toLocalDateTime());
            }

            Boolean bExistSameQTimeFlag = false;

            int nParentQTimeLen = CimArrayUtils.getSize(aParentCheckQTimeList);
            for (int nParentQTimeNo = 0; nParentQTimeNo < nParentQTimeLen; nParentQTimeNo++) {
                log.info("nParentQTimeLen/nParentQTimeNo = {}/{}", nParentQTimeLen, nParentQTimeNo);

                //Get Q-Time information;
                CimQTimeRestriction aParentQTime = aParentCheckQTimeList.get(nParentQTimeNo);
                ProcessDTO.QTimeRestrictionInfo aParentQTimeInfo = aParentQTime.getQTimeRestrictionInfo();

                if (aParentQTimeInfo == null || CimObjectUtils.isEmpty(aParentQTimeInfo.getTriggerOperationNumber())) {
                    log.info("Parent lot's Q-Time information was not found.");
                    Validations.check(retCodeConfig.getNotFoundQtime());
                }

                if (!CimStringUtils.equals(aChildQTimeInfo.getTriggerMainProcessDefinition().getValue(), aParentQTimeInfo.getTriggerMainProcessDefinition().getValue())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTriggerOperationNumber(), aParentQTimeInfo.getTriggerOperationNumber())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTargetMainProcessDefinition().getValue(), aParentQTimeInfo.getTargetMainProcessDefinition().getValue())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTargetOperationNumber(), aParentQTimeInfo.getTargetOperationNumber())
                        || !CimStringUtils.equals(aChildQTimeInfo.getOriginalQTime(), aParentQTimeInfo.getOriginalQTime())
                        || !CimStringUtils.equals(aChildQTimeInfo.getProcessDefinitionLevel(), aParentQTimeInfo.getProcessDefinitionLevel())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTriggerBranchInfo(), aParentQTimeInfo.getTriggerBranchInfo())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTriggerReturnInfo(), aParentQTimeInfo.getTriggerReturnInfo())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTargetBranchInfo(), aParentQTimeInfo.getTargetBranchInfo())
                        || !CimStringUtils.equals(aChildQTimeInfo.getTargetReturnInfo(), aParentQTimeInfo.getTargetReturnInfo())
                        || !CimStringUtils.equals(aChildQTimeInfo.getPreviousTargetInfo(), aParentQTimeInfo.getPreviousTargetInfo())
                        || !CimStringUtils.equals(aChildQTimeInfo.getControl(), aParentQTimeInfo.getControl())) {
                    log.info("Check next aParentCheckQTimeSeq   ...continue");
                    continue;
                }

                log.info("triggerMainProcessDefinition = {}", aChildQTimeInfo.getTriggerMainProcessDefinition().getValue());
                log.info("triggerOperationNumber = {}", aChildQTimeInfo.getTriggerOperationNumber());
                log.info("targetMainProcessDefinition = {}", aChildQTimeInfo.getTargetMainProcessDefinition().getValue());
                log.info("targetOperationNumber = {}", aChildQTimeInfo.getTargetOperationNumber());

                if (CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aChildQTimeInfo.getTargetTimeStamp())) {
                    log.info("aChildQTimeInfo.targetTimeStamp == SP_TIMESTAMP_NIL_OBJECT_STRING");
                    if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aParentQTimeInfo.getTargetTimeStamp())) {
                        log.info("aParentQTimeInfo.targetTimeStamp != SP_TIMESTAMP_NIL_OBJECT_STRING");
                        Validations.check(retCodeConfig.getDifferentQtimeInformation());
                    }
                } else {
                    log.info("aChildQTimeInfo.targetTimeStamp != SP_TIMESTAMP_NIL_OBJECT_STRING");
                    if (CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aParentQTimeInfo.getTargetTimeStamp())) {
                        log.info("aParentQTimeInfo.targetTimeStamp == SP_TIMESTAMP_NIL_OBJECT_STRING");
                        Validations.check(retCodeConfig.getDifferentQtimeInformation());
                    }

                    Timestamp targetTime = Timestamp.valueOf(aParentQTimeInfo.getTargetTimeStamp());
                    Timestamp triggerTime = Timestamp.valueOf(aParentQTimeInfo.getTriggerTimeStamp());
                    Duration aParentDuration = Duration.between(targetTime.toLocalDateTime(), triggerTime.toLocalDateTime());
                    if (aParentDuration.compareTo(aChildDuration) != 0) {
                        log.info("!aChildDuration.isEqualTo( aParentDuration )");
                        Validations.check(retCodeConfig.getDifferentQtimeInformation());
                    }
                }

                if (CimArrayUtils.getSize(aChildQTimeInfo.getActions()) != CimArrayUtils.getSize(aParentQTimeInfo.getActions())) {
                    log.info("aChildQTimeInfo.actions.length() != aParentQTimeInfo.actions.length()");
                    Validations.check(retCodeConfig.getDifferentQtimeInformation());
                }

                int nChildActionLen = CimArrayUtils.getSize(aChildQTimeInfo.getActions());
                for (int nChildActionNo = 0; nChildActionNo < nChildActionLen; nChildActionNo++) {
                    log.info("nChildActionLen/nChildActionNo = {}/{}", nChildActionLen, nChildActionNo);
                    ProcessDTO.QTimeRestrictionAction childQTimeAction = aChildQTimeInfo.getActions().get(nChildActionNo);
                    if (CimBooleanUtils.isTrue(childQTimeAction.getActionDone()) &&
                            !CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, childQTimeAction.getAction())) {
                        log.info("TRUE == aChildQTimeInfo.actions[nChildActionNo].actionDone");
                        Validations.check(retCodeConfig.getQtimeActionAlreadyDone());
                    }

                    Timestamp targetTime = Timestamp.valueOf(childQTimeAction.getTargetTimeStamp());
                    Timestamp triggerTime = Timestamp.valueOf(aChildQTimeInfo.getTriggerTimeStamp());
                    Duration aChildActionDuration = Duration.between(targetTime.toLocalDateTime(), triggerTime.toLocalDateTime());

                    Boolean bExistSameActionFlag = false;
                    int nParentActionLen = CimArrayUtils.getSize(aParentQTimeInfo.getActions());
                    for (int nParentActionNo = 0; nParentActionNo < nParentActionLen; nParentActionNo++) {
                        log.info("nParentActionLen/nParentActionNo = {}/{}", nParentActionLen, nParentActionNo);
                        ProcessDTO.QTimeRestrictionAction parentQTimeAction = aParentQTimeInfo.getActions().get(nParentActionNo);
                        if (!CimStringUtils.equals(childQTimeAction.getAction(), parentQTimeAction.getAction())) {
                            log.info("childActions.action != parentActions.action   ...continue");
                            continue;
                        }
                        if (CimBooleanUtils.isTrue(parentQTimeAction.getActionDone()) &&
                                !CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, parentQTimeAction.getAction())) {
                            log.info("TRUE == aParentQTimeInfo.actions[nParentActionNo].actionDone");
                            Validations.check(retCodeConfig.getQtimeActionAlreadyDone());
                        }

                        Timestamp parentTargetTime = Timestamp.valueOf(parentQTimeAction.getTargetTimeStamp());
                        Timestamp parentTriggerTime = Timestamp.valueOf(aParentQTimeInfo.getTriggerTimeStamp());
                        Duration aParentActionDuration = Duration.between(parentTargetTime.toLocalDateTime(), parentTriggerTime.toLocalDateTime());
                        if (aChildActionDuration.compareTo(aParentActionDuration) != 0) {
                            log.info("!aChildActionDuration.isEqualTo( aParentActionDuration )   ...continue");
                            continue;
                        }

                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD, childQTimeAction.getAction())) {
                            log.info("action = SP_QTimeRestriction_Action_ImmediateHold");
                            if (ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode())) {
                                log.info("reasonCode  = {} ", childQTimeAction.getReasonCode());
                                bExistSameActionFlag = true;
                                break;
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD, childQTimeAction.getAction())) {
                            log.info("action = SP_QTimeRestriction_Action_FutureHold");
                            if (ObjectIdentifier.equalsWithValue(childQTimeAction.getActionRouteID(), parentQTimeAction.getActionRouteID())
                                    && CimStringUtils.equals(childQTimeAction.getOperationNumber(), parentQTimeAction.getOperationNumber())
                                    && ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode())
                                    && CimStringUtils.equals(childQTimeAction.getTiming(), parentQTimeAction.getTiming())) {
                                log.info("actionRouteID = {}", childQTimeAction.getActionRouteID());
                                log.info("operationNumber = {} ", childQTimeAction.getOperationNumber());
                                log.info("reasonCode = {} ", childQTimeAction.getReasonCode());
                                log.info("timing = {} ", childQTimeAction.getTiming());
                                bExistSameActionFlag = true;
                                break;
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, childQTimeAction.getAction())) {
                            log.info("action = SP_QTimeRestriction_Action_FutureRework");
                            if (ObjectIdentifier.equalsWithValue(childQTimeAction.getActionRouteID(), parentQTimeAction.getActionRouteID())
                                    && CimStringUtils.equals(childQTimeAction.getOperationNumber(), parentQTimeAction.getOperationNumber())) {
                                log.info("actionRouteID  = {} ", childQTimeAction.getActionRouteID());
                                log.info("operationNumber = {} ", childQTimeAction.getOperationNumber());
                                bExistSameActionFlag = true;
                                break;
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_MAIL, childQTimeAction.getAction())) {
                            log.info("action = SP_QTimeRestriction_Action_Mail");
                            if (ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode()) &&
                                    ObjectIdentifier.equalsWithValue(childQTimeAction.getMessageDefinition(), parentQTimeAction.getMessageDefinition())) {
                                log.info("reasonCode = {}", childQTimeAction.getReasonCode().getValue());
                                log.info("messageDefinition = {}", childQTimeAction.getMessageDefinition().getValue());
                                bExistSameActionFlag = true;
                                break;
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, childQTimeAction.getAction())) {
                            log.info("action = SP_QTIME_RESTRICTION_ACTION_DISPATCH_PRECEDE");
                            bExistSameActionFlag = true;
                            break;
                        }
                    }

                    if (CimBooleanUtils.isFalse(bExistSameActionFlag)) {
                        log.info("FALSE == bExistSameActionFlag");
                        Validations.check(retCodeConfig.getDifferentQtimeInformation());
                    }
                }
                bExistSameQTimeFlag = true;
                break;
            }

            if (CimBooleanUtils.isFalse(bExistSameQTimeFlag)) {
                log.info("FALSE == bExistSameQTimeFlag");
                Validations.check(retCodeConfig.getDifferentQtimeInformation());
            }
        }

        log.info("【Method Exit】qTimeCheckForMerge()");
    }

    @Override
    public void qtimeQrestTimeFlagMaint(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber, Outputs.QrestTimeAction qrestTimeAction) {
        log.info("PPTManager_i::qtime_QrestTimeFlagMaint__180");
        int   i,j;
        int   nQTimeRestrictionSeqLen = 0;

        //===== Get the target lot object =======//
        com.fa.cim.newcore.bo.product.CimLot aLot ;
        aLot =baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);

        //===== Get the QTimeRestriction sequence of the target lot =======//
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeRestrictionSequence = null;

        int aQTimeRestrictionSequenceForWaferCount = 0;
        aQTimeRestrictionSequenceForWaferCount = aLot.getWaferLevelQTimeCount();

        if ( aQTimeRestrictionSequenceForWaferCount > 0 ) {
            log.info("Exist WaferLevelQTime");
            aQTimeRestrictionSequence = aLot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            log.info("Not exist WaferLevelQTime");
            aQTimeRestrictionSequence = aLot.allQTimeRestrictions();
        }

        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeRestrictionSequenceVar = aQTimeRestrictionSequence ;

        nQTimeRestrictionSeqLen = CimArrayUtils.getSize(aQTimeRestrictionSequence);
        log.info( "nQTimeRestrictionSeqLen = {}", nQTimeRestrictionSeqLen );

        //===== Loop for each QTimeRestriction object =======//
        for( i = 0 ; i < nQTimeRestrictionSeqLen ; i++ ) {
            //===== Nil check for each QTimeRestriction object =======//
            Validations.check(aQTimeRestrictionSequence.get(i)==null,new OmCode(retCodeConfig.getNotFoundSystemObj(),"QTimeRestriction"));

            //===== Check whether each QTimeRestriction is the target to update =======//
            ProcessDTO.QTimeRestrictionInfo qTimeRestrictionInfo = aQTimeRestrictionSequence.get(i).getQTimeRestrictionInfo();

            if( CimStringUtils.equals( qTimeRestrictionInfo.getTriggerMainProcessDefinition().getValue(), qrestTimeAction.getQrestrictionTriggerRouteID().getValue() ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTriggerOperationNumber()                 , qrestTimeAction.getQrestrictionTriggerOperationNumber()    ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTargetMainProcessDefinition().getValue() , qrestTimeAction.getQrestrictionTargetRouteID().getValue()  ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTargetOperationNumber()                  , qrestTimeAction.getQrestrictionTargetOperationNumber()     ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTriggerBranchInfo()                      , qrestTimeAction.getQrestrictionTriggerBranchInfo()         ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTriggerReturnInfo()                      , qrestTimeAction.getQrestrictionTriggerReturnInfo()         ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTargetBranchInfo()                       , qrestTimeAction.getQrestrictionTargetBranchInfo()          ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getOriginalQTime()                          , qrestTimeAction.getOriginalQTime()                         ) &&
                    CimStringUtils.equals( qTimeRestrictionInfo.getTargetReturnInfo()                       , qrestTimeAction.getQrestrictionTargetReturnInfo()          )) {
                log.info( "The target Q Restriction to update is found." );
                log.info("strQrestTimeAction.qrestrictionTriggerRouteID.identifier = {}",qrestTimeAction.getQrestrictionTriggerRouteID().getValue());
                log.info("strQrestTimeAction.qrestrictionTriggerOperationNumber = {}",qrestTimeAction.getQrestrictionTriggerOperationNumber());
                log.info("strQrestTimeAction.qrestrictionTargetRouteID.identifier = {}",qrestTimeAction.getQrestrictionTargetRouteID().getValue());
                log.info("strQrestTimeAction.qrestrictionTargetOperationNumber = {}",qrestTimeAction.getQrestrictionTargetOperationNumber());

                //===== This is the executed Q-Time =======//
                int    nNextUrgentQTimeSeqNum    = -1;
                int    nExecutedQTimeSeqNum      = -1;
                boolean bAllActionDoneFlagAreTrue = TRUE;

                //===== Get the QTimeRestrictionAction sequence of the target QTimeRestriction =======//
                int actionSeqNum = CimArrayUtils.getSize(qTimeRestrictionInfo.getActions());
                log.info("actionSeqNum = {}", actionSeqNum );

                //-------------------------------------------------------------
                // Update section of ACTION_DONE_FLAG of FRQTIME_ACTION table
                //-------------------------------------------------------------
                boolean bQTUpdated = FALSE;
                //===== Loop for each QTimeRestrictionAction =======//
                for ( j = 0 ; j < actionSeqNum ; j++ ) {
                    if( CimStringUtils.equals( qTimeRestrictionInfo.getActions().get(j).getAction(), BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD ) &&
                            CimStringUtils.equals( qrestTimeAction.getQrestrictionAction(),   BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD )   ) {
                        qTimeRestrictionInfo.getActions().get(j).setOperationNumber( qrestTimeAction.getActionOperationNumber());
                        bQTUpdated = TRUE;
                    }

                    //===== Check whether each QTimeRestrictionAction is the target to update =======//
                    long aDuration;
                    long targetTimeStamp= CimDateUtils.convertToOrInitialTime(qTimeRestrictionInfo.getActions().get(j).getTargetTimeStamp()).getTime();
                    aDuration = targetTimeStamp- CimDateUtils.convertToOrInitialTime(qTimeRestrictionInfo.getTriggerTimeStamp()).getTime();
                    long qTimeActionDuration = aDuration / 1000;

                    if( qTimeActionDuration == qrestTimeAction.getExpiredTimeDuration()  &&
                            CimStringUtils.equals( qTimeRestrictionInfo.getActions().get(j).getAction()                , qrestTimeAction.getQrestrictionAction() ) &&
                            CimStringUtils.equals( ObjectIdentifier.fetchValue(qTimeRestrictionInfo.getActions().get(j).getReasonCode()) , ObjectIdentifier.fetchValue(qrestTimeAction.getReasonCodeID()) ) &&
                            CimStringUtils.equals( ObjectIdentifier.fetchValue(qTimeRestrictionInfo.getActions().get(j).getActionRouteID())         , ObjectIdentifier.fetchValue(qrestTimeAction.getActionRouteID()) ) &&
                            CimStringUtils.equals( qTimeRestrictionInfo.getActions().get(j).getOperationNumber()       , qrestTimeAction.getActionOperationNumber() ) &&
                            CimStringUtils.equals( qTimeRestrictionInfo.getActions().get(j).getTiming()                , qrestTimeAction.getFutureHoldTiming() ) &&
                            CimStringUtils.equals( ObjectIdentifier.fetchValue(qTimeRestrictionInfo.getActions().get(j).getMainProcessDefinition()) , ObjectIdentifier.fetchValue(qrestTimeAction.getReworkRouteID()) ) &&
                            ObjectIdentifier.equalsWithValue(qTimeRestrictionInfo.getActions().get(j).getMessageDefinition(), qrestTimeAction.getMessageID()) &&
                            CimStringUtils.equals( qTimeRestrictionInfo.getActions().get(j).getCustomField()           , qrestTimeAction.getCustomField() ) ) {
                        //===== Update ACTION_DONE_FLAG of the target action =======//
                        qTimeRestrictionInfo.getActions().get(j).setActionDone( TRUE);
                        bQTUpdated = TRUE;

                        //===== Remember the sequence number of the executed action =======//
                        nExecutedQTimeSeqNum = j;

                        log.info( "The target action to update is found. nExecutedQTimeSeqNum={}",nExecutedQTimeSeqNum );
                    } else {
                        //===== Check ACTION_DONE_FLAG of all the QTimeRestrictionAction =======//
                        if( CimBooleanUtils.isFalse(qTimeRestrictionInfo.getActions().get(j).getActionDone()) ) {
                            bAllActionDoneFlagAreTrue = FALSE;
                        } else {
                            //===== Nothing to do when (qTimeRestrictionActionSeq[j].actionDone == TRUE) =======//
                        }
                    }
                }

                //--------------------------------------------------
                // Maintain FRQTIME table
                //--------------------------------------------------
                if( bAllActionDoneFlagAreTrue ) {
                    //===== Update ACTION_DONE_FLAG and WATCHDOG_REQ of FRQTIME table =======//
                    qTimeRestrictionInfo.setActionDone( TRUE);
                }

                //===== Update FRQTIME and FRQTIME_ACTION table =======//
                aQTimeRestrictionSequence.get(i).setQTimeRestrictionInfo(qTimeRestrictionInfo);
                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                if(bQTUpdated == TRUE) {
                    Infos.QtimeInfo strQtimeInfo=new Infos.QtimeInfo();
                    strQtimeInfo.setQTimeType                          ( qTimeRestrictionInfo.getQTimeType());
                    strQtimeInfo.setWaferID                            ( qTimeRestrictionInfo.getWaferID());
                    strQtimeInfo.setPreTrigger                         ( qTimeRestrictionInfo.getPreTrigger());
                    strQtimeInfo.setOriginalQTime                      ( qTimeRestrictionInfo.getOriginalQTime());
                    strQtimeInfo.setProcessDefinitionLevel             ( qTimeRestrictionInfo.getProcessDefinitionLevel());
                    strQtimeInfo.setRestrictionTriggerRouteID         ( qTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    strQtimeInfo.setRestrictionTriggerOperationNumber ( qTimeRestrictionInfo.getTriggerOperationNumber());
                    strQtimeInfo.setRestrictionTriggerBranchInfo      ( qTimeRestrictionInfo.getTriggerBranchInfo());
                    strQtimeInfo.setRestrictionTriggerReturnInfo      ( qTimeRestrictionInfo.getTriggerReturnInfo());
                    strQtimeInfo.setRestrictionTriggerTimeStamp       ( qTimeRestrictionInfo.getTriggerTimeStamp());
                    strQtimeInfo.setRestrictionTargetRouteID          ( qTimeRestrictionInfo.getTargetMainProcessDefinition());
                    strQtimeInfo.setRestrictionTargetOperationNumber  ( qTimeRestrictionInfo.getTargetOperationNumber());
                    strQtimeInfo.setRestrictionTargetBranchInfo       ( qTimeRestrictionInfo.getTargetBranchInfo());
                    strQtimeInfo.setRestrictionTargetReturnInfo       ( qTimeRestrictionInfo.getTargetReturnInfo());
                    strQtimeInfo.setRestrictionTargetTimeStamp        ( qTimeRestrictionInfo.getTargetTimeStamp());
                    strQtimeInfo.setPreviousTargetInfo                 ( qTimeRestrictionInfo.getPreviousTargetInfo());
                    strQtimeInfo.setSpecificControl                    ( qTimeRestrictionInfo.getControl());
                    if (TRUE.equals(qTimeRestrictionInfo.getWatchdogRequired())) {
                        strQtimeInfo.setWatchDogRequired ("Y");
                    } else if (FALSE.equals(qTimeRestrictionInfo.getWatchdogRequired())) {
                        strQtimeInfo.setWatchDogRequired ("N");
                    }
                    if (TRUE.equals(qTimeRestrictionInfo.getActionDone())) {
                        log.info("TRUE == qTimeRestrictionInfo->actionDone");
                        strQtimeInfo.setActionDoneFlag ("Y");
                    } else if (FALSE.equals(qTimeRestrictionInfo.getActionDone())) {
                        log.info("FALSE == qTimeRestrictionInfo->actionDone");
                        strQtimeInfo.setActionDoneFlag ("N");
                    }
                    strQtimeInfo.setManualCreated(qTimeRestrictionInfo.getManualCreated());
                    int actionLength = CimArrayUtils.getSize(qTimeRestrictionInfo.getActions());
                    log.info("qTimeRestrictionInfo->actions.length()={}", actionLength);
                    if ( actionLength != 0 ) {
                        log.info("actionLength != 0");
                        strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>(actionLength));
                        for ( int iCnt3=0; iCnt3<actionLength; iCnt3++ ) {
                            log.info( "loop to qTimeRestrictionInfo->actions.length()={}", iCnt3);
                            strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp ( qTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction          ( qTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID                ( qTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID               ( qTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber       ( qTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming            ( qTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID               ( qTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID                   ( qTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField                 ( qTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                            if (TRUE.equals(qTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                                log.info("TRUE == qTimeRestrictionInfo->actions[iCnt3].watchdogRequired");
                                strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("Y");
                            } else if (FALSE.equals(qTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                                log.info("FALSE == qTimeRestrictionInfo->actions[iCnt3].watchdogRequired");
                                strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("N");
                            }
                            if (TRUE.equals(qTimeRestrictionInfo.getActions().get(iCnt3).getActionDone())) {
                                log.info("TRUE == qTimeRestrictionInfo->actions[iCnt3].actionDone");
                                strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("Y");
                            } else if (FALSE.equals(qTimeRestrictionInfo.getActions().get(iCnt3).getActionDone())) {
                                log.info("FALSE == qTimeRestrictionInfo->actions[iCnt3].actionDone");
                                strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("N");
                            }
                        }
                    } else {
                        log.info( "actionLength == 0");
                        strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>(0));
                    }

                    Inputs.QTimeChangeEventMakeParams  strQTimeChangeEventMakeIn=new Inputs.QTimeChangeEventMakeParams();

                    strQTimeChangeEventMakeIn.setUpdateMode   ( BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                    strQTimeChangeEventMakeIn.setLotID        ( lotID);
                    strQTimeChangeEventMakeIn.setQtimeInfo    ( strQtimeInfo);
                    strQTimeChangeEventMakeIn.setClaimMemo    ( "");
                    eventMethod.qTimeChangeEventMake(
                            objCommon,
                            strQTimeChangeEventMakeIn );
                }
            } else {
                //===== Omit QTimeRestriction which is NOT the target to update =======//
                continue;
            }
        }

    }

    @Override
    public void qTimeInfoMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        log.info("【Method Entry】qTimeInfoMerge()");
        log.info("in-parameter parentLotID = {} ", parentLotID.getValue());
        log.info("in-parameter childLotID = {}", childLotID.getValue());

        // 【Step1】 Check input parameter;
        if (CimStringUtils.isEmpty(parentLotID.getValue()) &&
                CimStringUtils.isEmpty(childLotID.getValue())) {
            log.info("Invalid Input parameter.");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        Boolean bEmptyChildLotFlag = false;
        //【Step2】Get Child lot Object;
        com.fa.cim.newcore.bo.product.CimLot aChildLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, childLotID);
        if (aChildLot == null) {
            log.info("Not found lot by {}", childLotID.getValue());
            Validations.check(retCodeConfig.getNotFoundLot());
        }
        String strLotFinishedState = aChildLot.getLotFinishedState();
        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED, strLotFinishedState)) {
            log.info("strLotFinishedState = Emptied");
            bEmptyChildLotFlag = true;
        }

        List<CimQTimeRestriction> aParentQTimeList = new ArrayList<>();
        List<CimQTimeRestriction> aChildQTimeList = new ArrayList<>();

        String qTimeMergeRule = StandardProperties.OM_QT_INFO_MERGE_RULE.getValue();
        log.info("qTimeMergeRule = {}", qTimeMergeRule);
        if (bEmptyChildLotFlag || CimStringUtils.equals("1", qTimeMergeRule)
                || CimStringUtils.equals("2", qTimeMergeRule)) {
            log.info("TRUE = bEmptyChildLotFlag or qTimeMergeRule = 1 or qTimeMergeRule = 2");
            //【Step3】Get Child lot's Q-Time Restriction ;
            aChildQTimeList = aChildLot.allQTimeRestrictions();
        }

        if (CimStringUtils.equals("1", qTimeMergeRule)
                || CimStringUtils.equals("2", qTimeMergeRule)) {
            log.info("qTimeMergeRule = 1 or qTimeMergeRule = 2");

            //【Step4】 Get Parent lot Object;
            CimLot aParentLot = baseCoreFactory.getBO(CimLot.class, parentLotID);
            Validations.check(aParentLot == null, retCodeConfig.getNotFoundLot());

            //【Step5】Get Parent lot's Q-Time Restriction;
            aParentQTimeList = aParentLot.allQTimeRestrictions();

            //【Step6】Get Child lot's process flow context ;
            CimProcessFlowContext aChildPFX = aChildLot.getProcessFlowContext();
            Validations.check(aChildPFX == null, retCodeConfig.getNotFoundPfx());

            List<CimQTimeRestriction> aChildMergeQTimeList = new ArrayList<>();
            int nQTimeLen = CimArrayUtils.getSize(aChildQTimeList);
            for (int nQTimeNo = 0; nQTimeNo < nQTimeLen; nQTimeNo++) {
                log.info("nQTimeLen/nQTimeNo = {}/{}", nQTimeLen, nQTimeNo);
                CimQTimeRestriction aQTime = aChildQTimeList.get(nQTimeNo);

                Boolean bIsInQTimeIntervalFlag = aChildPFX.isInQTimeInterval(aQTime);
                if (bIsInQTimeIntervalFlag) {
                    log.info("bIsInQTimeIntervalFlag = TRUE");
                    aChildMergeQTimeList.add(aQTime);
                }
            }

            int nChildQTimeLen = CimArrayUtils.getSize(aChildMergeQTimeList);
            for (int nChildQTimeNo = 0; nChildQTimeNo < nChildQTimeLen; nChildQTimeNo++) {
                log.info("nChildQTimeLen/nChildQTimeNo = {}/{}", nChildQTimeLen, nChildQTimeNo);

                //【Step7】Get Q-Time information;
                CimQTimeRestriction aChildQTime = aChildMergeQTimeList.get(nChildQTimeNo);
                ProcessDTO.QTimeRestrictionInfo aChildQTimeInfo = aChildQTime.getQTimeRestrictionInfo();
                if (aChildQTimeInfo == null || CimStringUtils.isEmpty(aChildQTimeInfo.getTriggerOperationNumber())) {
                    log.info("Child lot's Q-Time information was not found.");
                    Validations.check(retCodeConfig.getNotFoundQtime());
                }

                Boolean bExistSameQTimeFlag = false;
                int nParentQTimeLen = CimArrayUtils.getSize(aParentQTimeList);
                for (int nParentQTimeNo = 0; nParentQTimeNo < nParentQTimeLen; nParentQTimeNo++) {
                    log.info("nParentQTimeLen/nParentQTimeNo = {}/{}", nParentQTimeLen, nParentQTimeNo);

                    //Get Q-Time information;
                    CimQTimeRestriction parentQTime = aParentQTimeList.get(nParentQTimeNo);
                    ProcessDTO.QTimeRestrictionInfo aParentQTimeInfo = parentQTime.getQTimeRestrictionInfo();
                    if (aParentQTimeInfo == null || CimStringUtils.isEmpty(aParentQTimeInfo.getTriggerOperationNumber())) {
                        log.info("Parent lot's Q-Time information was not found.");
                        Validations.check(retCodeConfig.getNotFoundQtime());
                    }

                    if (!CimStringUtils.equals(aChildQTimeInfo.getTriggerMainProcessDefinition().getValue(), aParentQTimeInfo.getTriggerMainProcessDefinition().getValue())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTriggerOperationNumber(), aParentQTimeInfo.getTriggerOperationNumber())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTargetMainProcessDefinition().getValue(), aParentQTimeInfo.getTargetMainProcessDefinition().getValue())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTargetOperationNumber(), aParentQTimeInfo.getTargetOperationNumber())
                            || !CimStringUtils.equals(aChildQTimeInfo.getOriginalQTime(), aParentQTimeInfo.getOriginalQTime())
                            || !CimStringUtils.equals(aChildQTimeInfo.getProcessDefinitionLevel(), aParentQTimeInfo.getProcessDefinitionLevel())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTriggerBranchInfo(), aParentQTimeInfo.getTriggerBranchInfo())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTriggerReturnInfo(), aParentQTimeInfo.getTriggerReturnInfo())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTargetBranchInfo(), aParentQTimeInfo.getTargetBranchInfo())
                            || !CimStringUtils.equals(aChildQTimeInfo.getTargetReturnInfo(), aParentQTimeInfo.getTargetReturnInfo())
                            || !CimStringUtils.equals(aChildQTimeInfo.getPreviousTargetInfo(), aParentQTimeInfo.getPreviousTargetInfo())
                            || !CimStringUtils.equals(aChildQTimeInfo.getControl(), aParentQTimeInfo.getControl())) {
                        log.info("Q-Time which is not same");

                        // There is Q-Time of same originalQTime in parent lot
                        if (!CimStringUtils.isEmpty(aChildQTimeInfo.getOriginalQTime()) &&
                                CimStringUtils.equals(aChildQTimeInfo.getOriginalQTime(), aParentQTimeInfo.getOriginalQTime())) {
                            log.info("There is Q-Time of same originalQTime in parent lot : {}", aChildQTimeInfo.getOriginalQTime());
                            bExistSameQTimeFlag = true;
                            break;
                        } else {
                            log.info("Check next aParentCheckQTimeSeq   ...continue");
                            continue;
                        }
                    }

                    log.info("triggerMainProcessDefinition = {}", aChildQTimeInfo.getTriggerMainProcessDefinition().getValue());
                    log.info("triggerOperationNumber = {}", aChildQTimeInfo.getTriggerOperationNumber());
                    log.info("targetMainProcessDefinition = {}", aChildQTimeInfo.getTargetMainProcessDefinition().getValue());
                    log.info("targetOperationNumber = {}", aChildQTimeInfo.getTargetOperationNumber());

                    int nChildActionLen = CimArrayUtils.getSize(aChildQTimeInfo.getActions());
                    Boolean bQTUpdated = false;
                    for (int nChildActionNo = 0; nChildActionNo < nChildActionLen; nChildActionNo++) {
                        log.info("nChildActionLen/nChildActionNo = {}/{}", nChildActionLen, nChildActionNo);
                        ProcessDTO.QTimeRestrictionAction childQTimeAction = aChildQTimeInfo.getActions().get(nChildActionNo);

                        if (childQTimeAction.getActionDone() &&
                                !CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, childQTimeAction.getAction())) {
                            log.info("TRUE == aChildQTimeInfo->actions[nChildActionNo].actionDone and other than DispatchPrecede   ...continue");
                            continue;
                        }

                        Timestamp childTargetTimeStamp = Timestamp.valueOf(childQTimeAction.getTargetTimeStamp());
                        Timestamp childTriggerTimeStamp = Timestamp.valueOf(aChildQTimeInfo.getTriggerTimeStamp());
                        Duration aChildActionDuration = Duration.between(childTargetTimeStamp.toLocalDateTime(), childTriggerTimeStamp.toLocalDateTime());

                        Boolean bExistSameActionFlag = false;

                        int nParentActionLen = CimArrayUtils.getSize(aParentQTimeInfo.getActions());
                        int nParentActionNo = 0;
                        for (nParentActionNo = 0; nParentActionNo < nParentActionLen; nParentActionNo++) {
                            log.info("nParentActionLen/nParentActionNo = {}/{}", nParentActionLen, nParentActionNo);
                            ProcessDTO.QTimeRestrictionAction parentQTimeAction = aParentQTimeInfo.getActions().get(nParentActionNo);
                            if (!CimStringUtils.equals(parentQTimeAction.getAction(), childQTimeAction.getAction())) {
                                log.info("childActions.action != parentActions.action   ...continue");
                                continue;
                            }

                            Timestamp parentTargetTimeStamp = Timestamp.valueOf(parentQTimeAction.getTargetTimeStamp());
                            Timestamp parentTriggerTimeStamp = Timestamp.valueOf(aParentQTimeInfo.getTriggerTimeStamp());
                            Duration aParentActionDuration = Duration.between(parentTargetTimeStamp.toLocalDateTime(), parentTriggerTimeStamp.toLocalDateTime());

                            if (aParentActionDuration.compareTo(aChildActionDuration) != 0) {
                                log.info("!aChildActionDuration.isEqualTo( aParentActionDuration )   ...continue");
                                continue;
                            }

                            if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD, childQTimeAction.getAction())) {
                                log.info("action = SP_QTimeRestriction_Action_ImmediateHold");
                                if (ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode())) {
                                    log.info("reasonCode = {} ", childQTimeAction.getReasonCode());
                                    bExistSameActionFlag = true;
                                    break;
                                }
                            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD, childQTimeAction.getAction())) {
                                log.info("action = SP_QTimeRestriction_Action_FutureHold");
                                if (ObjectIdentifier.equalsWithValue(childQTimeAction.getActionRouteID(), parentQTimeAction.getActionRouteID())
                                        && CimStringUtils.equals(childQTimeAction.getOperationNumber(), parentQTimeAction.getOperationNumber())
                                        && ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode())
                                        && CimStringUtils.equals(childQTimeAction.getTiming(), parentQTimeAction.getTiming())) {
                                    log.info("actionRouteID = {} ", childQTimeAction.getActionRouteID());
                                    log.info("operationNumber = {} ", childQTimeAction.getOperationNumber());
                                    log.info("reasonCode = {} ", childQTimeAction.getReasonCode());
                                    log.info("timing = {} ", childQTimeAction.getTiming());

                                    bExistSameActionFlag = true;
                                    break;
                                }
                            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, childQTimeAction.getAction())) {
                                log.info("action = SP_QTimeRestriction_Action_FutureRework");
                                if (ObjectIdentifier.equalsWithValue(childQTimeAction.getActionRouteID(), parentQTimeAction.getActionRouteID())
                                        && CimStringUtils.equals(childQTimeAction.getOperationNumber(), parentQTimeAction.getOperationNumber())) {
                                    log.info("actionRouteID = {}", childQTimeAction.getActionRouteID());
                                    log.info("operationNumber = {} ", childQTimeAction.getOperationNumber());
                                    bExistSameActionFlag = true;
                                    break;
                                }
                            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_MAIL, childQTimeAction.getAction())) {
                                log.info("action = SP_QTimeRestriction_Action_Mail");
                                if (ObjectIdentifier.equalsWithValue(childQTimeAction.getReasonCode(), parentQTimeAction.getReasonCode())
                                        && ObjectIdentifier.equalsWithValue(childQTimeAction.getMessageDefinition(), parentQTimeAction.getMessageDefinition())) {
                                    log.info("reasonCode = {}", childQTimeAction.getReasonCode());
                                    log.info("messageDefinition = {}", childQTimeAction.getMessageDefinition());
                                    bExistSameActionFlag = true;
                                    break;
                                }
                            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, childQTimeAction.getAction())) {
                                log.info("action = SP_QTimeRestriction_Action_DispatchPrecede");
                                bExistSameActionFlag = true;
                                break;
                            }
                        }

                        if (!bExistSameActionFlag) {
                            log.info("FALSE == bExistSameActionFlag");
                            log.info("Add Child lot's Q-Time Action");

                            if (aParentQTimeInfo.getActions() == null) {
                                List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                                actions.add(childQTimeAction);
                                aParentQTimeInfo.setActions(actions);
                            } else {
                                aParentQTimeInfo.getActions().set(nParentActionNo-1, childQTimeAction);
                            }
                            aParentQTimeInfo.setActionDone(false);
                            bQTUpdated = true;
                        } else {
                            log.info("TRUE == bExistSameActionFlag");
                            if (!aParentQTimeInfo.getActions().get(nParentActionNo).getActionDone()
                                    || CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                    aChildQTimeInfo.getActions().get(nChildActionNo).getAction())) {
                                log.info("FALSE == aParentQTimeInfo->actions[nParentActionNo].actionDone or DispatchPrecede action");

                                Timestamp cTargetTimeStamp = Timestamp.valueOf(aChildQTimeInfo.getActions().get(nChildActionNo).getTargetTimeStamp());
                                Timestamp pTargetTimeStamp = Timestamp.valueOf(aParentQTimeInfo.getActions().get(nParentActionNo).getTargetTimeStamp());
                                if (cTargetTimeStamp.compareTo(pTargetTimeStamp) < 0) {
                                    log.info("Set Child lot's Q-Time Action");
                                    aParentQTimeInfo.getActions().set(nParentActionNo, aChildQTimeInfo.getActions().get(nChildActionNo));
                                    bQTUpdated = true;
                                }
                            }
                        }
                    }

                    Timestamp triggerTimeStamp = Timestamp.valueOf(aChildQTimeInfo.getTriggerTimeStamp());
                    Timestamp timeStamp = Timestamp.valueOf(aParentQTimeInfo.getTriggerTimeStamp());
                    if (triggerTimeStamp.compareTo(timeStamp) < 0) {
                        log.info("Set aChildQTimeInfo->triggerTimeStamp is {}", aChildQTimeInfo.getTriggerTimeStamp());
                        aParentQTimeInfo.setTriggerTimeStamp(aChildQTimeInfo.getTriggerTimeStamp());
                        bQTUpdated = true;
                    }

                    if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aChildQTimeInfo.getTargetTimeStamp())) {
                        log.info("aChildQTimeInfo->targetTimeStamp != SP_TIMESTAMP_NIL_OBJECT_STRING");
                        if (CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, aParentQTimeInfo.getTargetTimeStamp())) {
                            log.info("aParentQTimeInfo->targetTimeStamp == SP_TIMESTAMP_NIL_OBJECT_STRING");
                            log.info("Set aChildQTimeInfo->targetTimeStamp is {}", aChildQTimeInfo.getTargetTimeStamp());
                            aParentQTimeInfo.setTargetTimeStamp(aChildQTimeInfo.getTargetTimeStamp());
                            bQTUpdated = true;
                        } else {
                            log.info("aParentQTimeInfo->targetTimeStamp != SP_TIMESTAMP_NIL_OBJECT_STRING");
                            Timestamp targetTimeStamp = Timestamp.valueOf(aChildQTimeInfo.getTargetTimeStamp());
                            Timestamp pTargetTime = Timestamp.valueOf(aParentQTimeInfo.getTargetTimeStamp());
                            if (targetTimeStamp.compareTo(pTargetTime) < 0) {
                                log.info("Set aChildQTimeInfo->targetTimeStamp is {}", aChildQTimeInfo.getTargetTimeStamp());
                                aParentQTimeInfo.setTargetTimeStamp(aChildQTimeInfo.getTargetTimeStamp());
                                bQTUpdated = true;
                            }
                        }
                    }

                    parentQTime.setQTimeRestrictionInfo(aParentQTimeInfo);

                    //【Step8】 Make event;
                    if (bQTUpdated) {
                        log.info("TRUE == bQTUpdated");
                        Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                        qtimeInfo.setWaferID(aParentQTimeInfo.getWaferID());
                        qtimeInfo.setQTimeType(aParentQTimeInfo.getQTimeType());
                        qtimeInfo.setPreTrigger(aParentQTimeInfo.getPreTrigger());
                        qtimeInfo.setOriginalQTime(aParentQTimeInfo.getOriginalQTime());
                        qtimeInfo.setProcessDefinitionLevel(aParentQTimeInfo.getProcessDefinitionLevel());
                        qtimeInfo.setRestrictionTriggerRouteID(aParentQTimeInfo.getTriggerMainProcessDefinition());
                        qtimeInfo.setRestrictionTriggerOperationNumber(aParentQTimeInfo.getTriggerOperationNumber());
                        qtimeInfo.setRestrictionTriggerBranchInfo(aParentQTimeInfo.getTriggerBranchInfo());
                        qtimeInfo.setRestrictionTriggerReturnInfo(aParentQTimeInfo.getTriggerReturnInfo());
                        qtimeInfo.setRestrictionTriggerTimeStamp(aParentQTimeInfo.getTriggerTimeStamp());
                        qtimeInfo.setRestrictionTargetRouteID(aParentQTimeInfo.getTargetMainProcessDefinition());
                        qtimeInfo.setRestrictionTargetOperationNumber(aParentQTimeInfo.getTargetOperationNumber());
                        qtimeInfo.setRestrictionTargetBranchInfo(aParentQTimeInfo.getTargetBranchInfo());
                        qtimeInfo.setRestrictionTargetReturnInfo(aParentQTimeInfo.getTargetReturnInfo());
                        qtimeInfo.setRestrictionTargetTimeStamp(aParentQTimeInfo.getTargetTimeStamp());
                        qtimeInfo.setPreviousTargetInfo(aParentQTimeInfo.getPreviousTargetInfo());
                        qtimeInfo.setSpecificControl(aParentQTimeInfo.getControl());
                        qtimeInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aParentQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                        qtimeInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aParentQTimeInfo.getActionDone()) ? "Y" : "N");
                        qtimeInfo.setManualCreated(aParentQTimeInfo.getManualCreated());
                        List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aParentQTimeInfo.getActions();
                        if (!CimObjectUtils.isEmpty(qTimeActions)) {
                            List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                            for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                                qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                                qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                                qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                                qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                                qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                                qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                                qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                                qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aParentQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                                qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aParentQTimeInfo.getActionDone()) ? "Y" : "N");
                                strQtimeActionInfoList.add(qTimeActionInfo);
                            }
                            qtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                        }
                        //qTimeChangeEvent_Make__180
                        Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                        qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                        qTimeChangeEventMakeParams.setLotID(parentLotID);
                        qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo);
                        qTimeChangeEventMakeParams.setClaimMemo("");
                        eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);
                    }
                    bExistSameQTimeFlag = true;
                    break;
                }

                if (!bExistSameQTimeFlag) {
                    log.info("FALSE == bExistSameQTimeFlag");

                    CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                    Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                    //addQTimeRestriction to lot;
                    aParentLot.addQTimeRestriction(aQTimeRestriction);
                    List<ProcessDTO.QTimeRestrictionAction> aChildMergeActionList = new ArrayList<>();
                    int nChildActionLen = CimArrayUtils.getSize(aChildQTimeInfo.getActions());
                    for (int nChildActionNo = 0; nChildActionNo < nChildActionLen; nChildActionNo++) {
                        log.info("nChildActionLen/nChildActionNo = {}/{}", nChildActionLen, nChildActionNo);
                        ProcessDTO.QTimeRestrictionAction childQTimeAction = aChildQTimeInfo.getActions().get(nChildActionNo);

                        if (childQTimeAction.getActionDone() &&
                                CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, childQTimeAction.getAction())) {
                            log.info("TRUE == aChildQTimeInfo->actions[nChildActionNo].actionDone and other than DispatchPrecede   ...continue");
                            continue;
                        }
                        aChildMergeActionList.add(childQTimeAction);
                    }

                    aChildQTimeInfo.setActions(aChildMergeActionList);
                    aChildQTimeInfo.setLotID(aParentLot.getLotID());
                    aQTimeRestriction.setQTimeRestrictionInfo(aChildQTimeInfo);

                    // Make event;
                    Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                    qtimeInfo.setWaferID(aChildQTimeInfo.getWaferID());
                    qtimeInfo.setQTimeType(aChildQTimeInfo.getQTimeType());
                    qtimeInfo.setPreTrigger(aChildQTimeInfo.getPreTrigger());
                    qtimeInfo.setOriginalQTime(aChildQTimeInfo.getOriginalQTime());
                    qtimeInfo.setProcessDefinitionLevel(aChildQTimeInfo.getProcessDefinitionLevel());
                    qtimeInfo.setRestrictionTriggerRouteID(aChildQTimeInfo.getTriggerMainProcessDefinition());
                    qtimeInfo.setRestrictionTriggerOperationNumber(aChildQTimeInfo.getTriggerOperationNumber());
                    qtimeInfo.setRestrictionTriggerBranchInfo(aChildQTimeInfo.getTriggerBranchInfo());
                    qtimeInfo.setRestrictionTriggerReturnInfo(aChildQTimeInfo.getTriggerReturnInfo());
                    qtimeInfo.setRestrictionTriggerTimeStamp(aChildQTimeInfo.getTriggerTimeStamp());
                    qtimeInfo.setRestrictionTargetRouteID(aChildQTimeInfo.getTargetMainProcessDefinition());
                    qtimeInfo.setRestrictionTargetOperationNumber(aChildQTimeInfo.getTargetOperationNumber());
                    qtimeInfo.setRestrictionTargetBranchInfo(aChildQTimeInfo.getTargetBranchInfo());
                    qtimeInfo.setRestrictionTargetReturnInfo(aChildQTimeInfo.getTargetReturnInfo());
                    qtimeInfo.setRestrictionTargetTimeStamp(aChildQTimeInfo.getTargetTimeStamp());
                    qtimeInfo.setPreviousTargetInfo(aChildQTimeInfo.getPreviousTargetInfo());
                    qtimeInfo.setSpecificControl(aChildQTimeInfo.getControl());
                    qtimeInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aChildQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                    qtimeInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aChildQTimeInfo.getActionDone()) ? "Y" : "N");
                    qtimeInfo.setManualCreated(aChildQTimeInfo.getManualCreated());
                    List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aChildQTimeInfo.getActions();
                    if (!CimObjectUtils.isEmpty(qTimeActions)) {
                        List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                        for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                            qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                            qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                            qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                            qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aChildQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                            qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aChildQTimeInfo.getActionDone()) ? "Y" : "N");
                            strQtimeActionInfoList.add(qTimeActionInfo);
                        }
                        qtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                    }
                    //qTimeChangeEvent_Make__180
                    Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                    qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                    qTimeChangeEventMakeParams.setLotID(parentLotID);
                    qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo);
                    qTimeChangeEventMakeParams.setClaimMemo("");
                    eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);

                }
            }
        }

        if (bEmptyChildLotFlag) {
            log.info("TRUE = bEmptyChildLotFlag");

            int nQTimeLen = CimArrayUtils.getSize(aChildQTimeList);
            int nQTimeNo = 0;
            for (nQTimeNo = 0; nQTimeNo < nQTimeLen; nQTimeNo++) {
                log.info("nQTimeLen/nQTimeNo = {}/{}", nQTimeLen, nQTimeNo);

                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = aChildQTimeList.get(nQTimeNo);
                //   Get Q-Time information for Child lot;
                ProcessDTO.QTimeRestrictionInfo aChildQTimeInfo = aQTime.getQTimeRestrictionInfo();
                if (aChildQTimeInfo == null || CimStringUtils.isEmpty(aChildQTimeInfo.getTriggerOperationNumber())) {
                    log.info("Child lot's Q-Time information was not found.");
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }

                aChildLot.removeQTimeRestriction(aQTime);

                qtimeRestrictionManager.removeQTimeRestriction(aQTime);

                // Make event;
                Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                qtimeInfo.setWaferID(aChildQTimeInfo.getWaferID());
                qtimeInfo.setQTimeType(aChildQTimeInfo.getQTimeType());
                qtimeInfo.setPreTrigger(aChildQTimeInfo.getPreTrigger());
                qtimeInfo.setOriginalQTime(aChildQTimeInfo.getOriginalQTime());
                qtimeInfo.setProcessDefinitionLevel(aChildQTimeInfo.getProcessDefinitionLevel());
                qtimeInfo.setRestrictionTriggerRouteID(aChildQTimeInfo.getTriggerMainProcessDefinition());
                qtimeInfo.setRestrictionTriggerOperationNumber(aChildQTimeInfo.getTriggerOperationNumber());
                qtimeInfo.setRestrictionTriggerBranchInfo(aChildQTimeInfo.getTriggerBranchInfo());
                qtimeInfo.setRestrictionTriggerReturnInfo(aChildQTimeInfo.getTriggerReturnInfo());
                qtimeInfo.setRestrictionTriggerTimeStamp(aChildQTimeInfo.getTriggerTimeStamp());
                qtimeInfo.setRestrictionTargetRouteID(aChildQTimeInfo.getTargetMainProcessDefinition());
                qtimeInfo.setRestrictionTargetOperationNumber(aChildQTimeInfo.getTargetOperationNumber());
                qtimeInfo.setRestrictionTargetBranchInfo(aChildQTimeInfo.getTargetBranchInfo());
                qtimeInfo.setRestrictionTargetReturnInfo(aChildQTimeInfo.getTargetReturnInfo());
                qtimeInfo.setRestrictionTargetTimeStamp(aChildQTimeInfo.getTargetTimeStamp());
                qtimeInfo.setPreviousTargetInfo(aChildQTimeInfo.getPreviousTargetInfo());
                qtimeInfo.setSpecificControl(aChildQTimeInfo.getControl());
                qtimeInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aChildQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                qtimeInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aChildQTimeInfo.getActionDone()) ? "Y" : "N");
                qtimeInfo.setManualCreated(aChildQTimeInfo.getManualCreated());
                List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aChildQTimeInfo.getActions();
                if (!CimObjectUtils.isEmpty(qTimeActions)) {
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                        qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aChildQTimeInfo.getWatchdogRequired()) ? "Y" : "N");
                        qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aChildQTimeInfo.getActionDone()) ? "Y" : "N");
                        strQtimeActionInfoList.add(qTimeActionInfo);
                    }
                    qtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                //qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
                qTimeChangeEventMakeParams.setLotID(childLotID);
                qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo);
                qTimeChangeEventMakeParams.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);

            }
        }

        log.info("【Method Exit】qTimeInfoMerge()");
    }

    @Override
    public List<Outputs.QrestTimeAction> lotQtimeInfoSortByAction(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.QtimeInfo> strQtimeInfoSeq) {
        List<Outputs.QrestTimeAction> qrestTimeActions = new ArrayList<>();
        /*------------------------------------------------------*/
        /*   Check lot state                                    */
        /*------------------------------------------------------*/
        Map<String, Infos.QtimeInfo> tmpLotQtimeInfoList = new HashMap<>();
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        Validations.check(null == lot, retCodeConfig.getNotFoundLot());
        //--------------------------------------------------
        // Turn off the flag to require watching
        //--------------------------------------------------
        //===== Get the target lot's process flow context =======//
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = lot.getProcessFlowContext();
        Validations.check(null == aProcessFlowContext, retCodeConfig.getNotFoundPfx());
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qTimeRestrictionList = null;
        Integer aQTimeListForWaferCount = lot.getWaferLevelQTimeCount();
        if (0 < aQTimeListForWaferCount) {
            qTimeRestrictionList = lot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            qTimeRestrictionList = lot.allQTimeRestrictions();
        }
        for (int i = 0; i < qTimeRestrictionList.size(); i++) {
            Validations.check(null == qTimeRestrictionList.get(i), retCodeConfig.getNotFoundQtime());
            //===== Get a Q-Time information =======//
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction qTime = qTimeRestrictionList.get(i);
            ProcessDTO.QTimeRestrictionInfo aQTimeInfo = qTime.getQTimeRestrictionInfo();
            if (null == aQTimeInfo || 1 > CimStringUtils.length(aQTimeInfo.getTriggerOperationNumber())) {
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            }
            //===== Check Q-Time Interval =======//
            Boolean qtimeIntervalFlag = false;
            qtimeIntervalFlag = aProcessFlowContext.isInQTimeInterval(qTime);
            if (!qtimeIntervalFlag) {
                String lotQtimeInfoKey = String.format("%d", i);
                //----- The key of QTimeIntervalInfo -------//
                Infos.QtimeInfo strLotQtimeInfo = new Infos.QtimeInfo();
                strLotQtimeInfo.setRestrictionTriggerRouteID           (aQTimeInfo.getTriggerMainProcessDefinition());
                strLotQtimeInfo.setRestrictionTriggerOperationNumber   (aQTimeInfo.getTriggerOperationNumber());
                strLotQtimeInfo.setRestrictionTargetRouteID            (aQTimeInfo.getTargetMainProcessDefinition());
                strLotQtimeInfo.setRestrictionTargetOperationNumber    (aQTimeInfo.getTargetOperationNumber());
                strLotQtimeInfo.setRestrictionTriggerBranchInfo        (aQTimeInfo.getTriggerBranchInfo());
                strLotQtimeInfo.setRestrictionTriggerReturnInfo        (aQTimeInfo.getTriggerReturnInfo());
                strLotQtimeInfo.setRestrictionTargetBranchInfo         (aQTimeInfo.getTargetBranchInfo());
                strLotQtimeInfo.setRestrictionTargetReturnInfo         (aQTimeInfo.getTargetReturnInfo());

                //===== Add to QTimeIntervalInfo list =======//
                String setKey = lotQtimeInfoKey;
                tmpLotQtimeInfoList.put(setKey, strLotQtimeInfo);

            }
            if (!CIMStateConst.CIM_LOT_STATE_ACTIVE.equals(lot.getLotState())) {
                //===== Change all flags to require watching =======//
                aQTimeInfo.setWatchdogRequired(false);

                int actionLen = CimArrayUtils.getSize(aQTimeInfo.getActions());
                for (int actionCnt = 0; actionCnt < actionLen; actionCnt++) {
                    aQTimeInfo.getActions().get(actionCnt).setWatchdogRequired(false);
                }

                //===== Set a Q-Time information =======//
                qTime.setQTimeRestrictionInfo( aQTimeInfo);
                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                Infos.QtimeInfo qTimeInfo = new Infos.QtimeInfo();
                qTimeInfo.setQTimeType(aQTimeInfo.getQTimeType());
                qTimeInfo.setWaferID(aQTimeInfo.getWaferID());
                qTimeInfo.setPreTrigger(aQTimeInfo.getPreTrigger());
                qTimeInfo.setOriginalQTime(aQTimeInfo.getOriginalQTime());
                qTimeInfo.setProcessDefinitionLevel(aQTimeInfo.getProcessDefinitionLevel());
                qTimeInfo.setRestrictionTriggerRouteID(aQTimeInfo.getTriggerMainProcessDefinition());
                qTimeInfo.setRestrictionTriggerOperationNumber(aQTimeInfo.getTriggerOperationNumber());
                qTimeInfo.setRestrictionTriggerBranchInfo(aQTimeInfo.getTriggerBranchInfo());
                qTimeInfo.setRestrictionTriggerReturnInfo(aQTimeInfo.getTriggerReturnInfo());
                qTimeInfo.setRestrictionTriggerTimeStamp(aQTimeInfo.getTriggerTimeStamp());
                qTimeInfo.setRestrictionTargetRouteID(aQTimeInfo.getTargetMainProcessDefinition());
                qTimeInfo.setRestrictionTargetOperationNumber(aQTimeInfo.getTargetOperationNumber());
                qTimeInfo.setRestrictionTargetBranchInfo(aQTimeInfo.getTargetBranchInfo());
                qTimeInfo.setRestrictionTargetReturnInfo(aQTimeInfo.getTargetReturnInfo());
                qTimeInfo.setRestrictionTargetTimeStamp(aQTimeInfo.getTargetTimeStamp());
                qTimeInfo.setPreviousTargetInfo(aQTimeInfo.getPreviousTargetInfo());
                qTimeInfo.setSpecificControl(aQTimeInfo.getControl());
                if (aQTimeInfo.getWatchdogRequired()) {
                    qTimeInfo.setWatchDogRequired("Y");
                } else {
                    qTimeInfo.setWatchDogRequired("N");
                }
                if (aQTimeInfo.getActionDone()) {
                    qTimeInfo.setActionDoneFlag("Y");
                } else {
                    qTimeInfo.setActionDoneFlag("N");
                }
                qTimeInfo.setManualCreated(aQTimeInfo.getManualCreated());
                if (!CimObjectUtils.isEmpty(aQTimeInfo.getActions())) {
                    List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                    qTimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
                    for (ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction : aQTimeInfo.getActions()) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfoList.add(qTimeActionInfo);
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeRestrictionAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeRestrictionAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeRestrictionAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeRestrictionAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeRestrictionAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeRestrictionAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeRestrictionAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeRestrictionAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeRestrictionAction.getCustomField());
                        if (qTimeRestrictionAction.getWatchdogRequired()) {
                            qTimeActionInfo.setWatchDogRequired("Y");
                        } else {
                            qTimeActionInfo.setWatchDogRequired("N");
                        }
                        if (qTimeRestrictionAction.getActionDone()) {
                            qTimeActionInfo.setActionDoneFlag("Y");
                        } else {
                            qTimeActionInfo.setActionDoneFlag("N");
                        }
                    }
                }
                // qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                params.setLotID(lotID);
                params.setQtimeInfo(qTimeInfo);
                params.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, params);
            }
        }
        if (!CimStringUtils.equals(lot.getLotState(), CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            return qrestTimeActions;
        }
        /*------------------------------------------------------*/
        /*   Declare local variables                            */
        /*------------------------------------------------------*/
        List<Outputs.QrestTimeAction> immediateHoldActionSeq = new ArrayList<>();
        List<Outputs.QrestTimeAction> futureHoldActionSeq = new ArrayList<>();
        List<Outputs.QrestTimeAction> futureReworkActionSeq = new ArrayList<>();
        List<Outputs.QrestTimeAction> mailActionSeq = new ArrayList<>();
        /*----------------------------------------------------------------*/
        /* Collect the following actions                                  */
        /*  1. ImmediateHold action                                       */
        /*  2. FutureHold action                                          */
        /*  3. futurerework action                                        */
        /*  4. Mail action                                                */
        /*----------------------------------------------------------------*/
        for (int i = 0; i < strQtimeInfoSeq.size(); i++) {
            //===== Check qtime Interval =======//
            Boolean qtimeIntervalFlag = true;
            for (Infos.QtimeInfo qtimeInfo : tmpLotQtimeInfoList.values()) {
                if (ObjectIdentifier.equalsWithValue(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID(), qtimeInfo.getRestrictionTriggerRouteID())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber(), qtimeInfo.getRestrictionTriggerOperationNumber())
                        && ObjectIdentifier.equalsWithValue(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID(), qtimeInfo.getRestrictionTargetRouteID())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber(), qtimeInfo.getRestrictionTargetOperationNumber())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo(), qtimeInfo.getRestrictionTriggerBranchInfo())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo(), qtimeInfo.getRestrictionTriggerReturnInfo())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo(), qtimeInfo.getRestrictionTargetBranchInfo())
                        && CimStringUtils.equals(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo(), qtimeInfo.getRestrictionTargetReturnInfo())) {
                    qtimeIntervalFlag = false;
                    break;
                }
            }

            if (!qtimeIntervalFlag) {
                continue;
            }
            /*----------------------------------------------------------------*/
            /*  Collect actions.                                              */
            /*----------------------------------------------------------------*/
            for (int j = 0; j < strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().size(); j++) {
                /*----------------------------------------------------------------*/
                /* Check each action. Omit the following actions                  */
                /*   1. The action of ACTION_DONE_FLAG == Y                       */
                /*   2. The action of WATCHDOG_REQ == N                           */
                /*   3. The action of TriggerTimeStamp < Current Time             */
                /*----------------------------------------------------------------*/
                String fmt = "yyyy-MM-dd-HH.mm.ss";
                if ("Y".equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionDoneFlag())
                        || "N".equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getWatchDogRequired())) {
                    continue;
                } else if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(),
                        CimDateUtils.convertToDate(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp())) < 0) {
                    continue;
                }
                /*----------------------------------------------------------------*/
                /*  Check actions Unique.                                         */
                /*----------------------------------------------------------------*/
                //----- The key of a Qtime action -------//
                Date date1 = CimDateUtils.convertToDate(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp());
                Date date2 = CimDateUtils.convertToDate(strQtimeInfoSeq.get(i).getRestrictionTriggerTimeStamp());
                Long aDuration = 0L;
                if (null != date1 && null != date2) {
                    aDuration = CimDateUtils.substractTimeStamp(date1.getTime(), date2.getTime());
                }
                Long qTimeActionDuration = aDuration / 1000;
                String durationSec = String.format("%d", qTimeActionDuration);
                //===== Add a Qtime action to the related list =======//
                List<String> QtimeActionKey = new ArrayList<>();
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getOriginalQTime());
                QtimeActionKey.add(durationSec);
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReasonCodeID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionRouteID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionOperationNumber());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getFutureHoldTiming());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReworkRouteID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getMessageID().getValue());
                QtimeActionKey.add(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getCustomField());
                //===== Add a Qtime action to the related list =======//
                String qtimeActionKey = CimStringUtils.join(QtimeActionKey, BizConstant.SP_KEY_SEPARATOR_DOT);
                List<String> qtimeInfoSortList = new ArrayList<>();
                if (qtimeInfoSortList.contains(qtimeActionKey)) {
                    continue;
                } else {
                    qtimeInfoSortList.add(qtimeActionKey);
                }

                /*----------------------------------------------------------------*/
                /* Collect actions of SP_QTimeRestriction_Action_ImmediateHold    */
                /*----------------------------------------------------------------*/
                if (BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD.equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction())) {
                    Outputs.QrestTimeAction qrestTimeAction = new Outputs.QrestTimeAction();
                    qrestTimeAction.setQrestrictionTriggerRouteID(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID());
                    qrestTimeAction.setQrestrictionTriggerOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber());
                    qrestTimeAction.setQrestrictionTriggerBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo());
                    qrestTimeAction.setQrestrictionTriggerReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo());
                    qrestTimeAction.setQrestrictionTargetRouteID(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID());
                    qrestTimeAction.setQrestrictionTargetOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber());
                    qrestTimeAction.setQrestrictionTargetBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo());
                    qrestTimeAction.setQrestrictionTargetReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo());
                    qrestTimeAction.setQrestrictionTargetTimeStamp(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp());
                    qrestTimeAction.setOriginalQTime(strQtimeInfoSeq.get(i).getOriginalQTime());
                    qrestTimeAction.setExpiredTimeDuration(qTimeActionDuration.intValue());
                    qrestTimeAction.setQrestrictionAction(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction());
                    qrestTimeAction.setReasonCodeID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReasonCodeID());
                    qrestTimeAction.setActionRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionRouteID());
                    qrestTimeAction.setActionOperationNumber(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionOperationNumber());
                    qrestTimeAction.setFutureHoldTiming(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getFutureHoldTiming());
                    qrestTimeAction.setReworkRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReworkRouteID());
                    qrestTimeAction.setMessageID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getMessageID());
                    qrestTimeAction.setCustomField(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getCustomField());
                    qrestTimeAction.setActionDoneOnlyFlag(false);
                    immediateHoldActionSeq.add(qrestTimeAction);
                    /*----------------------------------------------------------------*/
                    /* Collect actions of SP_QTimeRestriction_Action_FutureHold       */
                    /*----------------------------------------------------------------*/
                } else if (BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD.equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction())) {
                    Outputs.QrestTimeAction qrestTimeAction = new Outputs.QrestTimeAction();
                    qrestTimeAction.setQrestrictionTriggerRouteID(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID());
                    qrestTimeAction.setQrestrictionTriggerOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber());
                    qrestTimeAction.setQrestrictionTriggerBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo());
                    qrestTimeAction.setQrestrictionTriggerReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo());
                    qrestTimeAction.setQrestrictionTargetRouteID(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID());
                    qrestTimeAction.setQrestrictionTargetOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber());
                    qrestTimeAction.setQrestrictionTargetBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo());
                    qrestTimeAction.setQrestrictionTargetReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo());
                    qrestTimeAction.setQrestrictionTargetTimeStamp(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp());
                    qrestTimeAction.setOriginalQTime(strQtimeInfoSeq.get(i).getOriginalQTime());
                    qrestTimeAction.setExpiredTimeDuration(qTimeActionDuration.intValue());
                    qrestTimeAction.setQrestrictionAction(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction());
                    qrestTimeAction.setReasonCodeID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReasonCodeID());
                    qrestTimeAction.setActionRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionRouteID());
                    qrestTimeAction.setActionOperationNumber(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionOperationNumber());
                    qrestTimeAction.setFutureHoldTiming(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getFutureHoldTiming());
                    qrestTimeAction.setReworkRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReworkRouteID());
                    qrestTimeAction.setMessageID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getMessageID());
                    qrestTimeAction.setCustomField(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getCustomField());
                    qrestTimeAction.setActionDoneOnlyFlag(false);
                    futureHoldActionSeq.add(qrestTimeAction);
                    /*----------------------------------------------------------------*/
                    /* Collect actions of SP_QTimeRestriction_Action_FutureRework     */
                    /*----------------------------------------------------------------*/
                } else if (BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK.equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction())) {
                    Outputs.QrestTimeAction qrestTimeAction = new Outputs.QrestTimeAction();
                    qrestTimeAction.setQrestrictionTriggerRouteID(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID());
                    qrestTimeAction.setQrestrictionTriggerOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber());
                    qrestTimeAction.setQrestrictionTriggerBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo());
                    qrestTimeAction.setQrestrictionTriggerReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo());
                    qrestTimeAction.setQrestrictionTargetRouteID(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID());
                    qrestTimeAction.setQrestrictionTargetOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber());
                    qrestTimeAction.setQrestrictionTargetBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo());
                    qrestTimeAction.setQrestrictionTargetReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo());
                    qrestTimeAction.setQrestrictionTargetTimeStamp(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp());
                    qrestTimeAction.setOriginalQTime(strQtimeInfoSeq.get(i).getOriginalQTime());
                    qrestTimeAction.setExpiredTimeDuration(qTimeActionDuration.intValue());
                    qrestTimeAction.setQrestrictionAction(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction());
                    qrestTimeAction.setReasonCodeID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReasonCodeID());
                    qrestTimeAction.setActionRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionRouteID());
                    qrestTimeAction.setActionOperationNumber(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionOperationNumber());
                    qrestTimeAction.setFutureHoldTiming(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getFutureHoldTiming());
                    qrestTimeAction.setReworkRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReworkRouteID());
                    qrestTimeAction.setMessageID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getMessageID());
                    qrestTimeAction.setCustomField(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getCustomField());
                    qrestTimeAction.setActionDoneOnlyFlag(false);
                    futureReworkActionSeq.add(qrestTimeAction);
                    /*----------------------------------------------------------------*/
                    /* Collect actions of SP_QTimeRestriction_Action_Mail             */
                    /*----------------------------------------------------------------*/
                } else if (BizConstant.SP_QTIMERESTRICTION_ACTION_MAIL.equals(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction())) {
                    Outputs.QrestTimeAction qrestTimeAction = new Outputs.QrestTimeAction();
                    qrestTimeAction.setQrestrictionTriggerRouteID(strQtimeInfoSeq.get(i).getRestrictionTriggerRouteID());
                    qrestTimeAction.setQrestrictionTriggerOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTriggerOperationNumber());
                    qrestTimeAction.setQrestrictionTriggerBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerBranchInfo());
                    qrestTimeAction.setQrestrictionTriggerReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTriggerReturnInfo());
                    qrestTimeAction.setQrestrictionTargetRouteID(strQtimeInfoSeq.get(i).getRestrictionTargetRouteID());
                    qrestTimeAction.setQrestrictionTargetOperationNumber(strQtimeInfoSeq.get(i).getRestrictionTargetOperationNumber());
                    qrestTimeAction.setQrestrictionTargetBranchInfo(strQtimeInfoSeq.get(i).getRestrictionTargetBranchInfo());
                    qrestTimeAction.setQrestrictionTargetReturnInfo(strQtimeInfoSeq.get(i).getRestrictionTargetReturnInfo());
                    qrestTimeAction.setQrestrictionTargetTimeStamp(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionTargetTimeStamp());
                    qrestTimeAction.setOriginalQTime(strQtimeInfoSeq.get(i).getOriginalQTime());
                    qrestTimeAction.setExpiredTimeDuration(qTimeActionDuration.intValue());
                    qrestTimeAction.setQrestrictionAction(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getQrestrictionAction());
                    qrestTimeAction.setReasonCodeID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReasonCodeID());
                    qrestTimeAction.setActionRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionRouteID());
                    qrestTimeAction.setActionOperationNumber(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getActionOperationNumber());
                    qrestTimeAction.setFutureHoldTiming(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getFutureHoldTiming());
                    qrestTimeAction.setReworkRouteID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getReworkRouteID());
                    qrestTimeAction.setMessageID(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getMessageID());
                    qrestTimeAction.setCustomField(strQtimeInfoSeq.get(i).getStrQtimeActionInfoList().get(j).getCustomField());
                    qrestTimeAction.setActionDoneOnlyFlag(false);
                    mailActionSeq.add(qrestTimeAction);
                }
            }
        }
        if (!CimArrayUtils.isEmpty(immediateHoldActionSeq)) {
            qrestTimeActions.addAll(immediateHoldActionSeq);
        }
        if (!CimArrayUtils.isEmpty(futureHoldActionSeq)) {
            qrestTimeActions.addAll(futureHoldActionSeq);
        }
        if (!CimArrayUtils.isEmpty(futureReworkActionSeq)) {
            qrestTimeActions.addAll(futureReworkActionSeq);
        }
        if (!CimArrayUtils.isEmpty(mailActionSeq)) {
            qrestTimeActions.addAll(mailActionSeq);
        }
        return qrestTimeActions;
    }

    @Override
    public List<Outputs.QrestLotInfo> qTimeLotInfoGetDR(Infos.ObjCommon objCommon, Infos.QtimeListInqInfo qtimeListInqInfo) {
        List<Outputs.QrestLotInfo> qrestLotInfos = new ArrayList<>();
        Integer inactiveQTimeList = StandardProperties.OM_QT_SHOW_INACTIVE_LIST.getIntValue();
        Boolean bNeedAnd = false;
        String HV_BUFFER = null;
        if (inactiveQTimeList==1) {
            HV_BUFFER = "SELECT * FROM OMQT WHERE ";
        } else {
            HV_BUFFER = "SELECT * FROM OMQT WHERE TIMER_FLAG = 1  ";
            bNeedAnd = true;
        }
        String HV_TMPBUFFER = null;
        if (!CimStringUtils.isEmpty(qtimeListInqInfo.getLotID().getValue())) {
            if (bNeedAnd) {
                HV_TMPBUFFER = String.format(" AND LOT_ID = '%s' ", qtimeListInqInfo.getLotID().getValue());
            } else {
                HV_TMPBUFFER = String.format(" LOT_ID = '%s' ", qtimeListInqInfo.getLotID().getValue());
                bNeedAnd = true;
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (!ObjectIdentifier.isEmptyWithValue(qtimeListInqInfo.getWaferID())) {
            if (bNeedAnd) {
                HV_TMPBUFFER = String.format(" AND WAFER_ID = '%s' ", qtimeListInqInfo.getWaferID().getValue());
            } else {
                HV_TMPBUFFER = String.format(" WAFER_ID = '%s' ", qtimeListInqInfo.getWaferID().getValue());
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (!CimStringUtils.isEmpty(qtimeListInqInfo.getQTimeType())) {
            HV_TMPBUFFER = String.format(" AND TIMER_TYPE = '%s' ", qtimeListInqInfo.getQTimeType());
            HV_BUFFER += HV_TMPBUFFER;
        }

        HV_BUFFER += " ORDER BY LOT_ID, TARGET_TIME";

        if (!CimStringUtils.isEmpty(HV_BUFFER) && !CimStringUtils.isEmpty(HV_TMPBUFFER)) {
            String sql = HV_BUFFER;
            cimJpaRepository.query(sql, CimQTimeDO.class).forEach(qTime -> {
                if (qtimeListInqInfo.getActiveQTime() && !qTime.getWatchdogRequest()) {
                    return;
                }
                Outputs.QrestLotInfo qrestLotInfo = new Outputs.QrestLotInfo();
                qrestLotInfo.setLotID(ObjectIdentifier.build(qTime.getLotID(), qTime.getLotObj()));
                List<Infos.QtimeInfo> qtimeInfo180s = new ArrayList<>();
                Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                qtimeInfo.setRestrictionTriggerRouteID(ObjectIdentifier.build(qTime.getTriggerMainProcessDefinitionID(),
                        qTime.getTriggerMainProcessDefinitionObj()));
                qtimeInfo.setRestrictionTriggerOperationNumber(qTime.getTriggerOperationNumber());
                qtimeInfo.setRestrictionTriggerTimeStamp(qTime.getTriggerTime().toString());
                qtimeInfo.setRestrictionTargetRouteID(
                        ObjectIdentifier.build(qTime.getTargetMainProcessDefinitionID(),
                                qTime.getTargetMainProcessDefinitionObj())
                );
                qtimeInfo.setRestrictionTargetOperationNumber(qTime.getTargetOperationNumber());
                if (qTime.getTargetTime() == null ||
                        BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING.equals(qTime.getTargetTime())) {                                                                                                                                //D7000371
                    qtimeInfo.setRestrictionTargetTimeStamp("-");
                    qtimeInfo.setRestrictionRemainTime("-");
                } else {
                    qtimeInfo.setRestrictionTargetTimeStamp(qTime.getTargetTime().toString());

                    //---------------------------------
                    //   Calcurate remain seconds
                    //---------------------------------
                    Timestamp aTimeStamp = qTime.getTargetTime();
                    Timestamp aTmp = objCommon.getTimeStamp().getReportTimeStamp();

                    long aDuration;
                    aDuration = aTimeStamp.getTime() - aTmp.getTime();
                    double remainSeconds = Math.abs(aDuration / 1000);

                    //---------------------------------
                    //   Make Time Format +-hh:MM:SS
                    //---------------------------------
                    String timeSign;
                    String hhmmss;
                    int hours;
                    int minutes;
                    int seconds;

                    if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) == 0) {
                        timeSign = " ";
                    } else if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) < 0) {
                        timeSign = "+";
                    } else {
                        timeSign = "-";
                    }

                    hours = (int) (remainSeconds / 3600);
                    minutes = (int) ((remainSeconds - (hours * 3600)) / 60);
                    seconds = (int) (remainSeconds - (hours * 3600) - (minutes * 60));

                    if (hours < 100) {
                        hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                    } else if (hours < 10000) {
                        hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                    } else {
                        hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                    }

                    qtimeInfo.setRestrictionRemainTime(hhmmss);
                }
                if (null == qTime.getOriginalQTime()) {
                    List<String> strs = new ArrayList<>();
                    strs.add(qTime.getTriggerMainProcessDefinitionID());
                    strs.add(qTime.getTriggerOperationNumber());
                    strs.add(qTime.getTargetMainProcessDefinitionID());
                    strs.add(qTime.getTargetOperationNumber());
                    qtimeInfo.setOriginalQTime(CimArrayUtils.mergeStringIntoTokens(strs, BizConstant.SP_KEY_SEPARATOR_DOT));
                } else {
                    qtimeInfo.setOriginalQTime(qTime.getOriginalQTime());
                }
                qtimeInfo.setProcessDefinitionLevel(qTime.getProcessDefinitionLevel());
                qtimeInfo.setRestrictionTriggerBranchInfo(qTime.getTriggetBranchInfo());
                qtimeInfo.setRestrictionTriggerReturnInfo(qTime.getTriggetReturnInfo());
                qtimeInfo.setRestrictionTargetBranchInfo(qTime.getTargetBranchInfo());
                qtimeInfo.setRestrictionTargetReturnInfo(qTime.getTargetReturnInfo());
                qtimeInfo.setPreviousTargetInfo(qTime.getPreviousTargetInfo());
                qtimeInfo.setSpecificControl(qTime.getControl());
                qtimeInfo.setManualCreated(CimBooleanUtils.isTrue(qTime.getManualCreatedFlag()));
                if (CimBooleanUtils.isTrue(qTime.getWatchdogRequest())) {
                    qtimeInfo.setWatchDogRequired("Y");
                } else {
                    qtimeInfo.setWatchDogRequired("N");
                }
                if (CimBooleanUtils.isTrue(qTime.getActionDoneFlag())) {
                    qtimeInfo.setActionDoneFlag("Y");
                } else {
                    qtimeInfo.setActionDoneFlag("N");
                }
                qtimeInfo.setQTimeType(qTime.getQtimeType());
                qtimeInfo.setWaferID(ObjectIdentifier.build(
                        qTime.getWaferID(), qTime.getWaferobj()
                ));
                qtimeInfo.setPreTrigger(CimBooleanUtils.isTrue(qTime.getPreTriggerFlag()));
                List<Infos.QTimeActionInfo> qTimeActionInfos = new ArrayList<>();
                cimJpaRepository.query("SELECT TARGET_TIME,\n" +
                        "                           ACTION,\n" +
                        "                           REASON_CODE,\n" +
                        "                           REASON_CODE_RKEY,\n" +
                        "                           ACTION_PROCESS_ID, \n" +
                        "                           ACTION_PROCESS_RKEY,\n" +
                        "                           OPE_NO,\n" +
                        "                           FH_TIMING,\n" +
                        "                           PRP_ID,\n" +
                        "                           PRP_RKEY,\n" +
                        "                           NOTIFY_ID,\n" +
                        "                           NOTIFY_RKEY\n" +
//                        "                           CUSTOM_FIELD,\n" +
                        "                           TIMER_FLAG,\n" +
                        "                           ACTION_COMPLETE_FLAG\n" +
                        "                    FROM   OMQT_ACT\n" +
                        "                    WHERE  REFKEY = ?\n" +
                        "                    ORDER BY TARGET_TIME",CimQTimeActionDO.class,qTime.getId()).forEach(qTimeAction -> {
                    Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                    qTimeActionInfos.add(qTimeActionInfo);
                    qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTime());
                    qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                    qTimeActionInfo.setReasonCodeID(ObjectIdentifier.build(qTimeAction.getReasonCode(),
                            qTimeAction.getReasonCodeObj()));
                    qTimeActionInfo.setActionRouteID(ObjectIdentifier.build(qTimeAction.getActionRouteID(),
                            qTimeAction.getActionRouteObj()));
                    qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                    qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                    qTimeActionInfo.setReworkRouteID(ObjectIdentifier.build(
                            qTimeAction.getMainPdID(), qTimeAction.getMainPdObj()
                    ));
                    qTimeActionInfo.setMessageID(ObjectIdentifier.build(
                            qTimeAction.getMessageDefinitionID(), qTimeAction.getMessageDefinitionObj()
                    ));
                    /*qTimeActionInfo.setCustomField(qTimeAction.getCustomerField());*/
                    if (CimBooleanUtils.isTrue(qTimeAction.getWatchDogRequest())) {
                        qTimeActionInfo.setWatchDogRequired("Y");
                    } else {
                        qTimeActionInfo.setWatchDogRequired("N");
                    }
                    if (CimBooleanUtils.isTrue(qTimeAction.getActionDoneFlag())) {
                        qTimeActionInfo.setActionDoneFlag("Y");
                    } else {
                        qTimeActionInfo.setActionDoneFlag("N");
                    }
                });
                qtimeInfo.setStrQtimeActionInfoList(qTimeActionInfos);
                qtimeInfo180s.add(qtimeInfo);
                qrestLotInfo.setStrQtimeInfo(qtimeInfo180s);
                qrestLotInfos.add(qrestLotInfo);
            });
        }
        Validations.check(CimArrayUtils.isEmpty(qrestLotInfos),retCodeConfig.getNotFoundEntry());
        return qrestLotInfos;
    }

    @Override
    public Page<Outputs.QrestLotInfo> qTimeLotInfoGetDR(Infos.ObjCommon objCommon, Infos.QtimePageListInqInfo qtimePageListInqInfo) {
        List<Outputs.QrestLotInfo> qrestLotInfos = new ArrayList<>();
        Integer inactiveQTimeList = StandardProperties.OM_QT_SHOW_INACTIVE_LIST.getIntValue();
        Boolean bNeedAnd = false;
        String HV_BUFFER = null;
        if (inactiveQTimeList==1) {
            HV_BUFFER = "SELECT * FROM OMQT WHERE ";
        } else {
            HV_BUFFER = "SELECT * FROM OMQT WHERE TIMER_FLAG = 1  ";
            bNeedAnd = true;
        }
        String HV_TMPBUFFER = null;
        if (!CimStringUtils.isEmpty(qtimePageListInqInfo.getLotID().getValue())) {
            if (bNeedAnd) {
                HV_TMPBUFFER = String.format(" AND LOT_ID = '%s' ", qtimePageListInqInfo.getLotID().getValue());
            } else {
                HV_TMPBUFFER = String.format(" LOT_ID = '%s' ", qtimePageListInqInfo.getLotID().getValue());
                bNeedAnd = true;
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (!ObjectIdentifier.isEmptyWithValue(qtimePageListInqInfo.getWaferID())) {
            if (bNeedAnd) {
                HV_TMPBUFFER = String.format(" AND WAFER_ID = '%s' ", qtimePageListInqInfo.getWaferID().getValue());
            } else {
                HV_TMPBUFFER = String.format(" WAFER_ID = '%s' ", qtimePageListInqInfo.getWaferID().getValue());
            }
            HV_BUFFER += HV_TMPBUFFER;
        }
        if (!CimStringUtils.isEmpty(qtimePageListInqInfo.getQTimeType())) {
            HV_TMPBUFFER = String.format(" AND TIMER_TYPE = '%s' ", qtimePageListInqInfo.getQTimeType());
            HV_BUFFER += HV_TMPBUFFER;
        }

        HV_BUFFER += " ORDER BY LOT_ID, TARGET_TIME";

        String sql = HV_BUFFER;
        Page<CimQTimeDO> cimQTimeDOS = cimJpaRepository.query(sql, CimQTimeDO.class, qtimePageListInqInfo.getSearchCondition());
        if (!CimStringUtils.isEmpty(HV_BUFFER) && !CimStringUtils.isEmpty(HV_TMPBUFFER)) {
            cimQTimeDOS.forEach(qTime -> {
                if (qtimePageListInqInfo.getActiveQTime() && !qTime.getWatchdogRequest()) {
                    return;
                }
                Outputs.QrestLotInfo qrestLotInfo = new Outputs.QrestLotInfo();
                qrestLotInfo.setLotID(ObjectIdentifier.build(qTime.getLotID(), qTime.getLotObj()));
                List<Infos.QtimeInfo> qtimeInfo180s = new ArrayList<>();
                Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                qtimeInfo.setRestrictionTriggerRouteID(ObjectIdentifier.build(qTime.getTriggerMainProcessDefinitionID(),
                        qTime.getTriggerMainProcessDefinitionObj()));
                qtimeInfo.setRestrictionTriggerOperationNumber(qTime.getTriggerOperationNumber());
                qtimeInfo.setRestrictionTriggerTimeStamp(qTime.getTriggerTime().toString());
                qtimeInfo.setRestrictionTargetRouteID(
                        ObjectIdentifier.build(qTime.getTargetMainProcessDefinitionID(),
                                qTime.getTargetMainProcessDefinitionObj())
                );
                qtimeInfo.setRestrictionTargetOperationNumber(qTime.getTargetOperationNumber());
                if (qTime.getTargetTime() == null ||
                        BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING.equals(qTime.getTargetTime())) {                                                                                                                                //D7000371
                    qtimeInfo.setRestrictionTargetTimeStamp("-");
                    qtimeInfo.setRestrictionRemainTime("-");
                } else {
                    qtimeInfo.setRestrictionTargetTimeStamp(qTime.getTargetTime().toString());

                    //---------------------------------
                    //   Calcurate remain seconds
                    //---------------------------------
                    Timestamp aTimeStamp = qTime.getTargetTime();
                    Timestamp aTmp = objCommon.getTimeStamp().getReportTimeStamp();

                    long aDuration;
                    aDuration = aTimeStamp.getTime() - aTmp.getTime();
                    double remainSeconds = Math.abs(aDuration / 1000);

                    //---------------------------------
                    //   Make Time Format +-hh:MM:SS
                    //---------------------------------
                    String timeSign;
                    String hhmmss;
                    long hours;
                    long minutes;
                    long seconds;

                    if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) == 0) {
                        timeSign = " ";
                    } else if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) < 0) {
                        timeSign = "+";
                    } else {
                        timeSign = "-";
                    }

                    hours = (long) (remainSeconds / 3600);
                    minutes = (long) ((remainSeconds - (hours * 3600)) / 60);
                    seconds = (long) (remainSeconds - (hours * 3600) - (minutes * 60));

                    if (hours < 100) {
                        hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                    } else if (hours < 10000) {
                        hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                    } else {
                        hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                    }

                    qtimeInfo.setRestrictionRemainTime(hhmmss);
                }
                if (null == qTime.getOriginalQTime()) {
                    List<String> strs = new ArrayList<>();
                    strs.add(qTime.getTriggerMainProcessDefinitionID());
                    strs.add(qTime.getTriggerOperationNumber());
                    strs.add(qTime.getTargetMainProcessDefinitionID());
                    strs.add(qTime.getTargetOperationNumber());
                    qtimeInfo.setOriginalQTime(CimArrayUtils.mergeStringIntoTokens(strs, BizConstant.SP_KEY_SEPARATOR_DOT));
                } else {
                    qtimeInfo.setOriginalQTime(qTime.getOriginalQTime());
                }
                qtimeInfo.setProcessDefinitionLevel(qTime.getProcessDefinitionLevel());
                qtimeInfo.setRestrictionTriggerBranchInfo(qTime.getTriggetBranchInfo());
                qtimeInfo.setRestrictionTriggerReturnInfo(qTime.getTriggetReturnInfo());
                qtimeInfo.setRestrictionTargetBranchInfo(qTime.getTargetBranchInfo());
                qtimeInfo.setRestrictionTargetReturnInfo(qTime.getTargetReturnInfo());
                qtimeInfo.setPreviousTargetInfo(qTime.getPreviousTargetInfo());
                qtimeInfo.setSpecificControl(qTime.getControl());
                qtimeInfo.setManualCreated(CimBooleanUtils.isTrue(qTime.getManualCreatedFlag()));
                if (CimBooleanUtils.isTrue(qTime.getWatchdogRequest())) {
                    qtimeInfo.setWatchDogRequired("Y");
                } else {
                    qtimeInfo.setWatchDogRequired("N");
                }
                if (CimBooleanUtils.isTrue(qTime.getActionDoneFlag())) {
                    qtimeInfo.setActionDoneFlag("Y");
                } else {
                    qtimeInfo.setActionDoneFlag("N");
                }
                qtimeInfo.setQTimeType(qTime.getQtimeType());
                qtimeInfo.setWaferID(ObjectIdentifier.build(
                        qTime.getWaferID(), qTime.getWaferobj()
                ));
                qtimeInfo.setPreTrigger(CimBooleanUtils.isTrue(qTime.getPreTriggerFlag()));
                List<Infos.QTimeActionInfo> qTimeActionInfos = new ArrayList<>();
                cimJpaRepository.query("SELECT TARGET_TIME,\n" +
                        "                           ACTION,\n" +
                        "                           REASON_CODE,\n" +
                        "                           REASON_CODE_RKEY,\n" +
                        "                           ACTION_PROCESS_ID, \n" +
                        "                           ACTION_PROCESS_RKEY,\n" +
                        "                           OPE_NO,\n" +
                        "                           FH_TIMING,\n" +
                        "                           PRP_ID,\n" +
                        "                           PRP_RKEY,\n" +
                        "                           NOTIFY_ID,\n" +
                        "                           NOTIFY_RKEY\n" +
//                        "                           CUSTOM_FIELD,\n" +
                        "                           TIMER_FLAG,\n" +
                        "                           ACTION_COMPLETE_FLAG\n" +
                        "                    FROM   OMQT_ACT\n" +
                        "                    WHERE  REFKEY = ?\n" +
                        "                    ORDER BY TARGET_TIME",CimQTimeActionDO.class,qTime.getId()).forEach(qTimeAction -> {
                    Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                    qTimeActionInfos.add(qTimeActionInfo);
                    qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTime());
                    qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                    qTimeActionInfo.setReasonCodeID(ObjectIdentifier.build(qTimeAction.getReasonCode(),
                            qTimeAction.getReasonCodeObj()));
                    qTimeActionInfo.setActionRouteID(ObjectIdentifier.build(qTimeAction.getActionRouteID(),
                            qTimeAction.getActionRouteObj()));
                    qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                    qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                    qTimeActionInfo.setReworkRouteID(ObjectIdentifier.build(
                            qTimeAction.getMainPdID(), qTimeAction.getMainPdObj()
                    ));
                    qTimeActionInfo.setMessageID(ObjectIdentifier.build(
                            qTimeAction.getMessageDefinitionID(), qTimeAction.getMessageDefinitionObj()
                    ));
                    /*qTimeActionInfo.setCustomField(qTimeAction.getCustomerField());*/
                    if (CimBooleanUtils.isTrue(qTimeAction.getWatchDogRequest())) {
                        qTimeActionInfo.setWatchDogRequired("Y");
                    } else {
                        qTimeActionInfo.setWatchDogRequired("N");
                    }
                    if (CimBooleanUtils.isTrue(qTimeAction.getActionDoneFlag())) {
                        qTimeActionInfo.setActionDoneFlag("Y");
                    } else {
                        qTimeActionInfo.setActionDoneFlag("N");
                    }
                });
                qtimeInfo.setStrQtimeActionInfoList(qTimeActionInfos);
                qtimeInfo180s.add(qtimeInfo);
                qrestLotInfo.setStrQtimeInfo(qtimeInfo180s);
                qrestLotInfos.add(qrestLotInfo);
            });
        }
        Validations.check(CimArrayUtils.isEmpty(qrestLotInfos),retCodeConfig.getNotFoundEntry());
        return new PageImpl<>(qrestLotInfos,cimQTimeDOS.getPageable(),cimQTimeDOS.getTotalElements());
    }

    @Override
    public List<Outputs.QrestLotInfo> qTimeLotListGetDR(Infos.ObjCommon objCommon, String qTimeType) {
        List<Outputs.QrestLotInfo> qrestLotInfos = new ArrayList<>();
        Integer inactiveQTimeList = StandardProperties.OM_QT_SHOW_INACTIVE_LIST.getIntValue();
        String HV_BUFFER = null;
        if (1==inactiveQTimeList) {
            HV_BUFFER = "SELECT * FROM OMQT WHERE ACTION_COMPLETE_FLAG = 0  ";
        } else {
            HV_BUFFER = "SELECT * FROM OMQT WHERE TIMER_FLAG = 1 AND ACTION_COMPLETE_FLAG = 0   ";
        }
        String HV_TMPBUFFER = null;
        if (!CimStringUtils.isEmpty(qTimeType)) {
            HV_TMPBUFFER = String.format(" AND TIMER_TYPE = '%s' ", qTimeType);
            if (!CimStringUtils.isEmpty(HV_BUFFER) && !CimStringUtils.isEmpty(HV_TMPBUFFER)) {
                HV_BUFFER += HV_TMPBUFFER;
            }
        }
        HV_BUFFER += " ORDER BY LOT_ID, TARGET_TIME";
        String sql = HV_BUFFER;
        cimJpaRepository.query(sql, CimQTimeDO.class).forEach(qTime -> {
            Outputs.QrestLotInfo qrestLotInfo = new Outputs.QrestLotInfo();
            qrestLotInfo.setLotID(ObjectIdentifier.build(qTime.getLotID(), qTime.getLotObj()));
            List<Infos.QtimeInfo> qtimeInfos = new ArrayList<>();
            Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
            qtimeInfo.setRestrictionTriggerRouteID(ObjectIdentifier.build(qTime.getTriggerMainProcessDefinitionID(),
                    qTime.getTriggerMainProcessDefinitionObj()));
            qtimeInfo.setRestrictionTriggerOperationNumber(qTime.getTriggerOperationNumber());
            qtimeInfo.setRestrictionTriggerTimeStamp(qTime.getTriggerTime().toString());
            qtimeInfo.setRestrictionTargetRouteID(ObjectIdentifier.build(
                    qTime.getTargetMainProcessDefinitionID(),
                    qTime.getTargetMainProcessDefinitionObj()
            ));
            qtimeInfo.setRestrictionTargetOperationNumber(qTime.getTargetOperationNumber());
            if (qTime.getTargetTime() != null &&
                    !BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING.equals(qTime.getTargetTime().toString())) {
                qtimeInfo.setRestrictionTargetTimeStamp(qTime.getTargetTime().toString());
                //---------------------------------
                //   Calcurate remain seconds
                //---------------------------------
                Timestamp aTimeStamp = qTime.getTargetTime();
                Timestamp aTmp = objCommon.getTimeStamp().getReportTimeStamp();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                long aDuration;
                aDuration = aTimeStamp.getTime() - aTmp.getTime();
                double remainSeconds = Math.abs(aDuration / 1000);

                double remainMinSeconds = Math.abs((currentTime.getTime() - qTime.getTriggerTime().getTime()) / 1000);
                //---------------------------------
                //   Make Time Format +-hh:MM:SS
                //---------------------------------
                String timeSign;
                String hhmmss;
                int hours;
                int minutes;
                int seconds;

                if (CimDateUtils.compare(currentTime, qTime.getTriggerTime()) == 0) {
                    timeSign = " ";
                } else if (CimDateUtils.compare(currentTime, qTime.getTriggerTime()) < 0) {
                    timeSign = "+";
                } else {
                    timeSign = "-";
                }

                hours = (int) (remainMinSeconds / 3600);
                minutes = (int) ((remainMinSeconds - (hours * 3600)) / 60);
                seconds = (int) (remainMinSeconds - (hours * 3600) - (minutes * 60));

                if (hours < 100) {
                    hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else if (hours < 10000) {
                    hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else {
                    hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                }

                qtimeInfo.setMinRemainTime(hhmmss);

                if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) == 0) {
                    timeSign = " ";
                } else if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) < 0) {
                    timeSign = "+";
                } else {
                    timeSign = "-";
                }

                hours = (int) (remainSeconds / 3600);
                minutes = (int) ((remainSeconds - (hours * 3600)) / 60);
                seconds = (int) (remainSeconds - (hours * 3600) - (minutes * 60));

                if (hours < 100) {
                    hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else if (hours < 10000) {
                    hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else {
                    hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                }

                qtimeInfo.setRestrictionRemainTime(hhmmss);
            }
            if (null == qTime.getOriginalQTime()) {
                List<String> strs = new ArrayList<>();
                strs.add(qTime.getTriggerMainProcessDefinitionID());
                strs.add(qTime.getTriggerOperationNumber());
                strs.add(qTime.getTargetMainProcessDefinitionID());
                strs.add(qTime.getTargetOperationNumber());
                qtimeInfo.setOriginalQTime(CimArrayUtils.mergeStringIntoTokens(strs, BizConstant.SP_KEY_SEPARATOR_DOT));
            } else {
                qtimeInfo.setOriginalQTime(qTime.getOriginalQTime());
            }
            qtimeInfo.setProcessDefinitionLevel(qTime.getProcessDefinitionLevel());
            qtimeInfo.setRestrictionTriggerBranchInfo(qTime.getTriggetBranchInfo());
            qtimeInfo.setRestrictionTriggerReturnInfo(qTime.getTriggetReturnInfo());
            qtimeInfo.setRestrictionTargetBranchInfo(qTime.getTargetBranchInfo());
            qtimeInfo.setRestrictionTargetReturnInfo(qTime.getTargetReturnInfo());
            qtimeInfo.setPreviousTargetInfo(qTime.getPreviousTargetInfo());
            qtimeInfo.setSpecificControl(qTime.getControl());
            qtimeInfo.setManualCreated(CimBooleanUtils.isTrue(qTime.getManualCreatedFlag()));
            qtimeInfo.setWatchDogRequired("Y");
            qtimeInfo.setActionDoneFlag("N");
            qtimeInfo.setQTimeType(qTime.getQtimeType());
            qtimeInfo.setWaferID(ObjectIdentifier.build(qTime.getWaferID(), qTime.getWaferobj()));
            qtimeInfo.setPreTrigger(CimBooleanUtils.isTrue(qTime.getPreTriggerFlag()));
            qtimeInfos.add(qtimeInfo);
            qrestLotInfo.setStrQtimeInfo(qtimeInfos);
            qrestLotInfos.add(qrestLotInfo);
        });
        Validations.check(CimArrayUtils.isEmpty(qrestLotInfos),retCodeConfig.getNotFoundEntry());
        return qrestLotInfos;
    }

    @Override
    public Page<Outputs.QrestLotInfo> qTimeLotListGetDR(Infos.ObjCommon objCommon, String qTimeType, SearchCondition searchCondition) {
        List<Outputs.QrestLotInfo> qrestLotInfos = new ArrayList<>();
        Integer inactiveQTimeList = StandardProperties.OM_QT_SHOW_INACTIVE_LIST.getIntValue();
        String HV_BUFFER = null;
        if (1==inactiveQTimeList) {
            HV_BUFFER = "SELECT * FROM OMQT WHERE ACTION_COMPLETE_FLAG = 0  ";
        } else {
            HV_BUFFER = "SELECT * FROM OMQT WHERE TIMER_FLAG = 1 AND ACTION_COMPLETE_FLAG = 0   ";
        }
        String HV_TMPBUFFER = null;
        if (!CimStringUtils.isEmpty(qTimeType)) {
            HV_TMPBUFFER = String.format(" AND TIMER_TYPE = '%s' ", qTimeType);
            if (!CimStringUtils.isEmpty(HV_BUFFER) && !CimStringUtils.isEmpty(HV_TMPBUFFER)) {
                HV_BUFFER += HV_TMPBUFFER;
            }
        }
        HV_BUFFER += " ORDER BY LOT_ID, TARGET_TIME";
        String sql = HV_BUFFER;
        Page<CimQTimeDO> cimTimeDOS = cimJpaRepository.query(sql, CimQTimeDO.class, searchCondition);
        cimTimeDOS.forEach(qTime -> {
            Outputs.QrestLotInfo qrestLotInfo = new Outputs.QrestLotInfo();
            qrestLotInfo.setLotID(ObjectIdentifier.build(qTime.getLotID(), qTime.getLotObj()));
            List<Infos.QtimeInfo> qtimeInfos = new ArrayList<>();
            Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
            qtimeInfo.setRestrictionTriggerRouteID(ObjectIdentifier.build(qTime.getTriggerMainProcessDefinitionID(),
                    qTime.getTriggerMainProcessDefinitionObj()));
            qtimeInfo.setRestrictionTriggerOperationNumber(qTime.getTriggerOperationNumber());
            qtimeInfo.setRestrictionTriggerTimeStamp(qTime.getTriggerTime().toString());
            qtimeInfo.setRestrictionTargetRouteID(ObjectIdentifier.build(
                    qTime.getTargetMainProcessDefinitionID(),
                    qTime.getTargetMainProcessDefinitionObj()
            ));
            qtimeInfo.setRestrictionTargetOperationNumber(qTime.getTargetOperationNumber());
            if (qTime.getTargetTime() != null &&
                    !BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING.equals(qTime.getTargetTime().toString())) {
                qtimeInfo.setRestrictionTargetTimeStamp(qTime.getTargetTime().toString());
                //---------------------------------
                //   Calcurate remain seconds
                //---------------------------------
                Timestamp aTimeStamp = qTime.getTargetTime();
                Timestamp aTmp = objCommon.getTimeStamp().getReportTimeStamp();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                long aDuration;
                aDuration = aTimeStamp.getTime() - aTmp.getTime();
                double remainSeconds = Math.abs(aDuration / 1000);

                double remainMinSeconds = Math.abs((currentTime.getTime() - qTime.getTriggerTime().getTime()) / 1000);
                //---------------------------------
                //   Make Time Format +-hh:MM:SS
                //---------------------------------
                String timeSign;
                String hhmmss;
                int hours;
                int minutes;
                int seconds;

                if (CimDateUtils.compare(currentTime, qTime.getTriggerTime()) == 0) {
                    timeSign = " ";
                } else if (CimDateUtils.compare(currentTime, qTime.getTriggerTime()) < 0) {
                    timeSign = "+";
                } else {
                    timeSign = "-";
                }

                hours = (int) (remainMinSeconds / 3600);
                minutes = (int) ((remainMinSeconds - (hours * 3600)) / 60);
                seconds = (int) (remainMinSeconds - (hours * 3600) - (minutes * 60));

                if (hours < 100) {
                    hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else if (hours < 10000) {
                    hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else {
                    hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                }

                qtimeInfo.setMinRemainTime(hhmmss);

                if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) == 0) {
                    timeSign = " ";
                } else if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), qTime.getTargetTime()) < 0) {
                    timeSign = "+";
                } else {
                    timeSign = "-";
                }

                hours = (int) (remainSeconds / 3600);
                minutes = (int) ((remainSeconds - (hours * 3600)) / 60);
                seconds = (int) (remainSeconds - (hours * 3600) - (minutes * 60));

                if (hours < 100) {
                    hhmmss = String.format("%s%02d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else if (hours < 10000) {
                    hhmmss = String.format("%s%04d:%02d:%02d", timeSign, hours, minutes, seconds);
                } else {
                    hhmmss = String.format("%s%06d:%02d:%02d", timeSign, hours, minutes, seconds);
                }

                qtimeInfo.setRestrictionRemainTime(hhmmss);
            }
            if (null == qTime.getOriginalQTime()) {
                List<String> strs = new ArrayList<>();
                strs.add(qTime.getTriggerMainProcessDefinitionID());
                strs.add(qTime.getTriggerOperationNumber());
                strs.add(qTime.getTargetMainProcessDefinitionID());
                strs.add(qTime.getTargetOperationNumber());
                qtimeInfo.setOriginalQTime(CimArrayUtils.mergeStringIntoTokens(strs, BizConstant.SP_KEY_SEPARATOR_DOT));
            } else {
                qtimeInfo.setOriginalQTime(qTime.getOriginalQTime());
            }
            qtimeInfo.setProcessDefinitionLevel(qTime.getProcessDefinitionLevel());
            qtimeInfo.setRestrictionTriggerBranchInfo(qTime.getTriggetBranchInfo());
            qtimeInfo.setRestrictionTriggerReturnInfo(qTime.getTriggetReturnInfo());
            qtimeInfo.setRestrictionTargetBranchInfo(qTime.getTargetBranchInfo());
            qtimeInfo.setRestrictionTargetReturnInfo(qTime.getTargetReturnInfo());
            qtimeInfo.setPreviousTargetInfo(qTime.getPreviousTargetInfo());
            qtimeInfo.setSpecificControl(qTime.getControl());
            qtimeInfo.setManualCreated(CimBooleanUtils.isTrue(qTime.getManualCreatedFlag()));
            qtimeInfo.setWatchDogRequired("Y");
            qtimeInfo.setActionDoneFlag("N");
            qtimeInfo.setQTimeType(qTime.getQtimeType());
            qtimeInfo.setWaferID(ObjectIdentifier.build(qTime.getWaferID(), qTime.getWaferobj()));
            qtimeInfo.setPreTrigger(CimBooleanUtils.isTrue(qTime.getPreTriggerFlag()));
            qtimeInfos.add(qtimeInfo);
            qrestLotInfo.setStrQtimeInfo(qtimeInfos);
            qrestLotInfos.add(qrestLotInfo);
        });
        Validations.check(CimArrayUtils.isEmpty(qrestLotInfos),retCodeConfig.getNotFoundEntry());
        return new PageImpl<>(qrestLotInfos,cimTimeDOS.getPageable(),cimTimeDOS.getTotalElements());
    }

    @Override
    public void qTimeTargetOpeCancelReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        log.info("【Method Entry】qTimeTargetOpeCancelReplace()");
        //Trace InParameters
        log.info("in-parameter lotID = {} ", lotID.getValue());

        //Get lot Object
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        Validations.check(aLot == null, retCodeConfig.getNotFoundLot());
        //Find qtime Restrictions for target lot;
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qTimeRestrictionList = new ArrayList<>();
        int qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();
        if (qtimeSeqForWaferCount > 0) {
            log.info("Exist WaferLevelQTime");
            qTimeRestrictionList = aLot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            log.info("exist WaferLevelQTime");
            qTimeRestrictionList = aLot.allQTimeRestrictions();
        }

        int qtsLength = CimArrayUtils.getSize(qTimeRestrictionList);
        log.info("The length of qTimeRestrictionList is {}", qtsLength);
        for (int iCnt1 = 0; iCnt1 < qtsLength; iCnt1++) {
            log.info("loop to qtimeSeq.length = {}", iCnt1);
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qTimeRestrictionList.get(iCnt1);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime(),lotID);

            ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
            //todo: cancelReplaceTarget how to implement?
            // TODO: 2019/10/17
            aQTimeRestrictionInfo = aQTime.cancelReplaceTarget();
            if (null != aQTimeRestrictionInfo && !CimStringUtils.isEmpty(aQTimeRestrictionInfo.getOriginalQTime())) {
                log.info("aQTimeRestrictionInfo.getOriginalQTime() is not empty.");
                // Make event;
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());

                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());

                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                if (CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("true == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (CimBooleanUtils.isFalse(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("false == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("N");
                }

                if (CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("true == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (CimBooleanUtils.isFalse(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("false == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("N");
                }

                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                int actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());

                log.info("The length of aQTimeRestrictionInfo.getActions() is {}", actionLength);
                if (actionLength != 0) {
                    log.info("actionLength != 0");

                    List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                    for (int iCnt2 = 0; iCnt2 < actionLength; iCnt2++) {
                        log.info("loop {} to the length of aQTimeRestrictionInfo.getActions() ", iCnt2);
                        ProcessDTO.QTimeRestrictionAction qTimeAction = aQTimeRestrictionInfo.getActions().get(iCnt2);

                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                        if (CimBooleanUtils.isTrue(qTimeAction.getWatchdogRequired())) {
                            log.info("TRUE == qTimeAction.getWatchDogRequest()");
                            qTimeActionInfo.setWatchDogRequired("Y");
                        } else if (CimBooleanUtils.isFalse(qTimeAction.getWatchdogRequired())) {
                            log.info("FALSE == qTimeAction.getWatchDogRequest()");
                            qTimeActionInfo.setWatchDogRequired("N");
                        }

                        if (CimBooleanUtils.isTrue(qTimeAction.getActionDone())) {
                            log.info("TRUE == qTimeAction.getActionDoneFlag()");
                            qTimeActionInfo.setActionDoneFlag("Y");
                        } else if (CimBooleanUtils.isFalse(qTimeAction.getActionDone())) {
                            log.info("FALSE == qTimeAction.getActionDoneFlag()");
                            qTimeActionInfo.setActionDoneFlag("N");
                        }
                        qTimeActionInfoList.add(qTimeActionInfo);
                    }
                    strQtimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
                } else {
                    log.info("actionLength == 0");
                }

                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                params.setLotID(lotID);
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, params);
            }
        }

        log.info("【Method Exit】qTimeTargetOpeCancelReplace()");
    }

    @Override
    public void qTimeInfoUpdate(Infos.ObjCommon objCommon, Params.QtimerReqParams params) {
        log.info("【Method Entry】qTimeInfoUpdate()");
        //Trace InParameters;
        log.info("in-parm actionType = {}", params.getActionType());
        log.info("in-parm lotID = {}", params.getLotID().getValue());

        //Get lot Object；
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,params.getLotID());
        Validations.check(aLot == null, retCodeConfig.getNotFoundLot());

        int qtimeInfoLen = CimArrayUtils.getSize(params.getQtimeInfoList());
        log.info("qtimeInfoLen = {}", qtimeInfoLen);
        Timestamp currentTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();

        Map<String, String> actionKeysTable = new HashMap<>();
        Map<String, Integer> actionIndexList = new HashMap<>();


        //Create Q-Time timer;
        if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE, params.getActionType())) {
            log.info("actionType is Create");

            //lot's state should be "CIMFW_Lot_State_Active"
            String lotState = aLot.getLotState();
            Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState), new OmCode(retCodeConfig.getInvalidLotStat(),lotState));
            com.fa.cim.newcore.bo.pd.CimProcessOperation aPosProcessOperation = aLot.getProcessOperation();
            Validations.check(aPosProcessOperation == null, new OmCode(retCodeConfig.getNotFoundOperation(),""));
            //Get current route
            com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = aPosProcessOperation.getMainProcessDefinition();
            Validations.check(aMainPD == null, retCodeConfig.getNotFoundProcessDefinition());

            ObjectIdentifier currentRoute = new ObjectIdentifier(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
            //Get current operationNumber
            String currentOperationNumber = aPosProcessOperation.getOperationNumber();

            //Get lot's processState
            String processStateRetCode = lotMethod.lotProcessStateGet(objCommon, params.getLotID());

            //Get the target lot's process flow context
            com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
            Validations.check(aProcessFlowContext == null, new OmCode(retCodeConfig.getNotFoundPfx(),""));
            //Get branchInfo of lot's current route
            List<ProcessDTO.BranchInfo> branchInfoList = aProcessFlowContext.allBranchInfos();
            String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoList);

            //Get returnInfo of lot's current route
            List<ProcessDTO.ReturnInfo> returnInfoList = aProcessFlowContext.allReturnInfos();
            String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoList);

            for (int iCnt1 = 0; iCnt1 < qtimeInfoLen; iCnt1++) {
                log.info("loop {} to strQtimeInfoSeq.length()", iCnt1);
                Infos.QrestTimeInfo qtimeInfo = params.getQtimeInfoList().get(iCnt1);

                //Check for Create Q-Time
                //1. Existence check of the based Q-Time definition( Get candidate list of Q-Time definition);
                Inputs.QtimeDefinitionSelectionInqIn qtimeDefinitionSelectionInqIn = new Inputs.QtimeDefinitionSelectionInqIn();
                qtimeDefinitionSelectionInqIn.setBranchInfo("");
                qtimeDefinitionSelectionInqIn.setOperationNumber("");
                qtimeDefinitionSelectionInqIn.setRouteID(new ObjectIdentifier("", ""));
                qtimeDefinitionSelectionInqIn.setLotID(params.getLotID());
                List<Infos.QrestTimeInfo> CandidateListRetCode = this.qtimeCandidateListGet(objCommon, qtimeDefinitionSelectionInqIn);

                int candidateLen = CimArrayUtils.getSize(CandidateListRetCode);
                boolean qTimeDefExist = false;
                for (int candidateIdx = 0; candidateIdx < candidateLen; candidateIdx++) {
                    log.info("loop {} to candidateList length", candidateIdx);
                    Infos.QrestTimeInfo qrestTimeInfo = CandidateListRetCode.get(candidateIdx);

                    if (CimStringUtils.equals(qrestTimeInfo.getOriginalQTime(), qtimeInfo.getOriginalQTime())
                            && CimStringUtils.equals(qrestTimeInfo.getProcessDefinitionLevel(), qtimeInfo.getProcessDefinitionLevel())
                            && Objects.equals(qrestTimeInfo.getExpiredTimeDuration(), qtimeInfo.getExpiredTimeDuration())) {
                        log.info("Q-Time definition exist");

                        int qtimeDefineActionLen = CimArrayUtils.getSize(qrestTimeInfo.getStrQtimeActionInfoList());
                        int qtimeInparaActionLen = CimArrayUtils.getSize(qtimeInfo.getStrQtimeActionInfoList());

                        if (qtimeDefineActionLen != qtimeInparaActionLen) {
                            log.info("qtimeDefineActionLen != qtimeInparaActionLen");
                            continue;
                        }

                        boolean qTimeDefActionExist = true;
                        for (int qtimeInparaActionIdx = 0; qtimeInparaActionIdx < qtimeInparaActionLen; qtimeInparaActionIdx++) {
                            log.info("loop {} to the length {} of qtimeInfo.getStrQtimeActionInfoList()", iCnt1, qtimeInparaActionIdx);
                            Infos.QTimeActionInfo inputQtimeActionInfo = qtimeInfo.getStrQtimeActionInfoList().get(qtimeInparaActionIdx);

                            qTimeDefActionExist = false;
                            for (int qtimeDefineActionIdx = 0; qtimeDefineActionIdx < qtimeDefineActionLen; qtimeDefineActionIdx++) {
                                log.info("loop {} to the length {} of qrestTimeInfo.getStrQtimeActionInfo()", candidateIdx, qtimeDefineActionIdx);
                                Infos.QTimeActionInfo definedQtimeActionInfo = qrestTimeInfo.getStrQtimeActionInfoList().get(qtimeDefineActionIdx);

                                if (CimObjectUtils.equals(inputQtimeActionInfo.getExpiredTimeDuration(), definedQtimeActionInfo.getExpiredTimeDuration())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getQrestrictionAction(), definedQtimeActionInfo.getQrestrictionAction())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getReasonCodeID().getValue(), definedQtimeActionInfo.getReasonCodeID().getValue())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getActionRouteID().getValue(), definedQtimeActionInfo.getActionRouteID().getValue())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getActionOperationNumber(), definedQtimeActionInfo.getActionOperationNumber())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getFutureHoldTiming(), definedQtimeActionInfo.getFutureHoldTiming())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getReworkRouteID().getValue(), definedQtimeActionInfo.getReworkRouteID().getValue())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getMessageID().getValue(), definedQtimeActionInfo.getMessageID().getValue())
                                        && CimStringUtils.equals(inputQtimeActionInfo.getCustomField(), definedQtimeActionInfo.getCustomField())) {
                                    log.info("Q-Time definition action exist");
                                    qTimeDefActionExist = true;
                                    break;
                                }
                            }
                            if (false == qTimeDefActionExist) {
                                log.info("FALSE == qTimeDefActionExist");
                                break;
                            }
                        }
                        if (false == qTimeDefActionExist) {
                            log.info("Q-Time definition action does not exists");
                            continue;
                        }

                        qTimeDefExist = true;
                        break;
                    }
                }
                Validations.check(!qTimeDefExist, retCodeConfig.getNotFoundQTimeDefinition());
                //2. Check there is no duplicate Q-Time timer;
                List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeRestrictionList = aLot.allQTimeRestrictions();
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = null;
                for (com.fa.cim.newcore.bo.pd.CimQTimeRestriction item : aQTimeRestrictionList) {
                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction qtime = item;
                    if (CimStringUtils.equals(qtime.getOriginalQTime(), qtimeInfo.getOriginalQTime())) {
                        aQTimeRestriction = qtime;
                        break;
                    }
                }
                Validations.check(aQTimeRestriction != null, retCodeConfig.getDuplicateQTime());

                //3. TargetRoute/OpeNo should be forward operation including current operation;
                Inputs.ObjProcessOperationProcessRefListForLotIn inputs = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                inputs.setSearchDirection(true);
                inputs.setPosSearchFlag(false);
                inputs.setSearchCount(9999);
                inputs.setSearchRouteID(qtimeInfo.getQrestrictionTargetRouteID());
                inputs.setSearchOperationNumber(qtimeInfo.getQrestrictionTargetOperationNumber());
                inputs.setCurrentFlag(true);
                inputs.setLotID(params.getLotID());
                List<Infos.OperationNumberListAttributes> processRetCode = processMethod.processOperationNumberListForLot(objCommon, inputs);

                int opeLen = CimArrayUtils.getSize(processRetCode);
                Infos.OperationNumberListAttributes theLastAttributes = (opeLen > 0)
                        ? (processRetCode.get(opeLen - 1))
                        : (new Infos.OperationNumberListAttributes());
                if (!ObjectIdentifier.equalsWithValue(qtimeInfo.getQrestrictionTargetRouteID(), theLastAttributes.getRouteID())
                        || !CimStringUtils.equals(qtimeInfo.getQrestrictionTargetOperationNumber(), theLastAttributes.getOperationNumber())) {
                    log.info("The target operation may be more backward to the lot's current operation.");
                    throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                }

                //4. If Target Route/OperNo is equal to lot's current operation, lot's process state should be "SP_Lot_ProcState_Waiting"
                if (ObjectIdentifier.equalsWithValue(qtimeInfo.getQrestrictionTargetRouteID(), currentRoute)
                        && CimStringUtils.equals(qtimeInfo.getQrestrictionTargetOperationNumber(), currentOperationNumber)) {
                    log.info("Target Route/OperNo is equal to lot's current operation");
                    if (!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, processStateRetCode)) {
                        log.info("processState != LOT_PROCESS_STATE_WAITING");
                       Validations.check(retCodeConfig.getInvalidLotProcessState(), ObjectIdentifier.fetchValue(params.getLotID()),processStateRetCode);
                    }
                }

                //Create qtime timer object
                aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());

                //Add qtime timer object to lot
                aLot.addQTimeRestriction(aQTimeRestriction);

                // Get branchInfo and returnInfo for target operation;
                String branchInfoForTarget = "";
                String returnInfoForTarget = "";
                if (CimStringUtils.equals(currentRoute.getValue(), qtimeInfo.getQrestrictionTargetRouteID().getValue())) {
                    log.info("currentRoute == qrestrictionTargetRouteID");
                    branchInfoForTarget = branchInfo;
                    returnInfoForTarget = returnInfo;
                } else {
                    log.info("currentRoute != qrestrictionTargetRouteID");
                    int branchInfoLen = CimArrayUtils.getSize(branchInfoList);
                    int returnInfoLen = CimArrayUtils.getSize(returnInfoList);
                    if (branchInfoLen == returnInfoLen) {
                        log.info("branchInfoLen == returnInfoLen");

                        for (int branchInfoIdx = 0; branchInfoIdx < branchInfoLen; branchInfoIdx++) {
                            log.info("loop {} to branchInfoSeq.length()", branchInfoIdx);
                            ProcessDTO.BranchInfo branchInformation = branchInfoList.get(branchInfoIdx);
                            if (CimStringUtils.equals(qtimeInfo.getQrestrictionTargetRouteID().getValue(), branchInformation.getRouteID().getValue())) {
                                log.info("qrestrictionTargetRouteID == branchInfoSeq[].routeID");

                                List<ProcessDTO.BranchInfo> branchInfosForTarget = new ArrayList<>(branchInfoList);
                                branchInfoForTarget = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfosForTarget);

                                List<ProcessDTO.ReturnInfo> returnInfosForTarget = new ArrayList<>(returnInfoList);
                                returnInfoForTarget = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfosForTarget);
                                break;
                            }
                        }
                    }
                }

                //Set qtime info data from input parameters
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                aQTimeRestrictionInfo.setLotID(new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(currentRoute);
                aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStamp.toString());
                aQTimeRestrictionInfo.setOriginalQTime(qtimeInfo.getOriginalQTime());
                aQTimeRestrictionInfo.setProcessDefinitionLevel(qtimeInfo.getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(qtimeInfo.getQrestrictionTargetRouteID());
                aQTimeRestrictionInfo.setTargetOperationNumber(qtimeInfo.getQrestrictionTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(branchInfoForTarget);
                aQTimeRestrictionInfo.setTargetReturnInfo(returnInfoForTarget);
                aQTimeRestrictionInfo.setPreviousTargetInfo("");
                aQTimeRestrictionInfo.setControl("");
                aQTimeRestrictionInfo.setWatchdogRequired(true);
                aQTimeRestrictionInfo.setActionDone(false);
                aQTimeRestrictionInfo.setManualCreated(true);
                aQTimeRestrictionInfo.setQTimeType(qtimeInfo.getQTimeType());
                aQTimeRestrictionInfo.setWaferID(new ObjectIdentifier(""));
                aQTimeRestrictionInfo.setPreTrigger(false);


                String targetTimeStamp = "";
                String mostUrgentTargetTimeAction = "";
                String dispatchPrecedeTargetTimeStamp = BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING;

                // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....);
                Boolean setMostUrgentTimeFlag = false;
                if (BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND.longValue()== qtimeInfo.getExpiredTimeDuration()) {
                    log.info("-1 == qtimeInfo.getExpiredTimeDuration()");
                    setMostUrgentTimeFlag = true;
                } else {
                    log.info("SP_DISPATCH_PRECEDE_NOT_FOUND != qtimeInfo.expiredTimeDuration");
                    targetTimeStamp = currentTimeStamp.toLocalDateTime().plus(Duration.ofHours(qtimeInfo.getExpiredTimeDuration())).toString();
                }

                log.info("#### targetTime = {}", targetTimeStamp);

                int qtimeActionInfoLen = CimArrayUtils.getSize(qtimeInfo.getStrQtimeActionInfoList());
                log.info("the length of qtimeInfo.getStrQtimeActionInfoList() is {}", iCnt1, qtimeActionInfoLen);
                if (qtimeActionInfoLen != 0) {
                    List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                    for (int iCnt2 = 0; iCnt2 < qtimeActionInfoLen; iCnt2++) {
                        Infos.QTimeActionInfo qTimeActionInfo = qtimeInfo.getStrQtimeActionInfoList().get(iCnt2);
                        String targetTimeStampForAction = currentTimeStamp.toLocalDateTime().plus(Duration.ofMillis(qTimeActionInfo.getExpiredTimeDuration())).
                                format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        if (setMostUrgentTimeFlag) {
                            log.info("TRUE == setMostUrgentTimeFlag");
                            if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                log.info("0 == CIMFWStrLen( mostUrgentTargetTimeAction )");
                                //====== The first iteration of for() loop ======//
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                log.info("0 != CIMFWStrLen( mostUrgentTargetTimeAction )");
                                //====== The second iteration of for() loop ======//
                                if (mostUrgentTargetTimeAction.compareTo(targetTimeStampForAction) > 0) {
                                    log.info("mostUrgentTargetTimeAction > targetTimeStampForAction");
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    log.info("Nothing to do.");
                                }
                            }
                        }

                        ProcessDTO.QTimeRestrictionAction qTimeAction = new ProcessDTO.QTimeRestrictionAction();

                        qTimeAction.setTargetTimeStamp(targetTimeStampForAction);
                        qTimeAction.setAction(qTimeActionInfo.getQrestrictionAction());
                        qTimeAction.setReasonCode(qTimeActionInfo.getReasonCodeID());
                        qTimeAction.setActionRouteID(qTimeActionInfo.getActionRouteID());
                        qTimeAction.setOperationNumber(qTimeActionInfo.getActionOperationNumber());
                        qTimeAction.setTiming(qTimeActionInfo.getFutureHoldTiming());
                        qTimeAction.setMainProcessDefinition(qTimeActionInfo.getReworkRouteID());
                        qTimeAction.setMessageDefinition(qTimeActionInfo.getMessageID());
                        qTimeAction.setCustomField(qTimeActionInfo.getCustomField());
                        qTimeAction.setWatchdogRequired(true);

                        // Action is DispatchPrecede
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, qTimeActionInfo.getQrestrictionAction())) {
                            log.info("Action is DispatchPrecede");
                            qTimeAction.setActionDone(true);
                            dispatchPrecedeTargetTimeStamp = targetTimeStampForAction;
                        }
                        // Action is not DispatchPrecede
                        else {
                            log.info("Action is {}", qTimeActionInfo.getQrestrictionAction());
                            qTimeAction.setActionDone(false);
                        }
                        actions.add(qTimeAction);
                    }
                    aQTimeRestrictionInfo.setActions(actions);
                } else {
                    log.info("qtimeActionInfoLen == 0");
                }

                if (!CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                    log.info("mostUrgentTargetTimeAction is not blank.");
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                    log.info("aQTimeRestrictionInfo.targetTimeStamp = {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                } else {
                    log.info("mostUrgentTargetTimeAction is blank.");
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                    log.info("aQTimeRestrictionInfo.targetTimeStamp = {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                }

                // Check the DispatchPrecedeTargetTimeStamp is same as Q-TimeTargetTimeStamp
                if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, dispatchPrecedeTargetTimeStamp)
                        && !CimStringUtils.equals(dispatchPrecedeTargetTimeStamp, aQTimeRestrictionInfo.getTargetTimeStamp())) {
                    log.info("DispatchPrecedeTargetTimeStamp is not same as Q-TimeTargetTimeStamp");
                    throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                }

                //Set qtime info to qtime timer object
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                //Make event
                Infos.QtimeInfo qtimeInfo2 = new Infos.QtimeInfo();
                qtimeInfo2.setWaferID(aQTimeRestrictionInfo.getWaferID());
                qtimeInfo2.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                qtimeInfo2.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                qtimeInfo2.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                qtimeInfo2.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                qtimeInfo2.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                qtimeInfo2.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                qtimeInfo2.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                qtimeInfo2.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                qtimeInfo2.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                qtimeInfo2.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                qtimeInfo2.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                qtimeInfo2.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                qtimeInfo2.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                qtimeInfo2.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                qtimeInfo2.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                qtimeInfo2.setSpecificControl(aQTimeRestrictionInfo.getControl());
                qtimeInfo2.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                qtimeInfo2.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                qtimeInfo2.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aQTimeRestrictionInfo.getActions();
                if (!CimObjectUtils.isEmpty(qTimeActions)) {
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                        qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                        qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                        strQtimeActionInfoList.add(qTimeActionInfo);
                    }
                    qtimeInfo2.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                //qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                qTimeChangeEventMakeParams.setLotID(params.getLotID());
                qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo2);
                qTimeChangeEventMakeParams.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE, params.getActionType())) {
            log.info("actionType is Update");

            for (int iCnt1 = 0; iCnt1 < qtimeInfoLen; iCnt1++) {
                log.info("loop {} to the length of params.getQtimeInfoList()", iCnt1);
                Infos.QrestTimeInfo qtimeInfo = params.getQtimeInfoList().get(iCnt1);

                //Existence check of Q-Time timer to be updated;
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = aLot.findQTimeRestrictionByOriginalQTime(qtimeInfo.getOriginalQTime());
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());

                //Get the target lot's Q-Time information
                ProcessDTO.QTimeRestrictionInfo aQTimeInfo = aQTimeRestriction.getQTimeRestrictionInfo();
                if (aQTimeInfo == null || CimStringUtils.isEmpty(aQTimeInfo.getTriggerOperationNumber())) {
                    log.info("The target lot's Q-Time information was not found.");
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }

                String triggerTimeStamp = aQTimeInfo.getTriggerTimeStamp();
                boolean qTimeInfoChange = false;
                if (!CimStringUtils.equals(aQTimeInfo.getTargetTimeStamp(), qtimeInfo.getQrestrictionTargetTimeStamp())) {
                    log.info("targetTimeStamp is updated.");
                    qTimeInfoChange = true;

                    //It should be after trigger date time
                    if (!CimStringUtils.isEmpty(qtimeInfo.getQrestrictionTargetTimeStamp()) &&
                            CimDateUtils.compare(aQTimeInfo.getTriggerTimeStamp(), qtimeInfo.getQrestrictionTargetTimeStamp()) > 0) {
                        log.info("aQTimeInfo.getTriggerTimeStamp() is greater than qtimeInfo.getRestrictionTargetTimeStamp().");
                        Validations.check(retCodeConfig.getInvalidParameterWithMsg(), "Target Time should be after trigger date time.");
                    }

                    //Set targetTimeStamp
                    aQTimeRestriction.setTargetTimeStamp(CimStringUtils.isEmpty(qtimeInfo.getQrestrictionTargetTimeStamp()) ? null : Timestamp.valueOf(qtimeInfo.getQrestrictionTargetTimeStamp()));
                }

                if (!ObjectIdentifier.equalsWithValue(aQTimeInfo.getTargetMainProcessDefinition(), qtimeInfo.getQrestrictionTargetRouteID())
                        || !CimStringUtils.equals(aQTimeInfo.getTargetOperationNumber(), qtimeInfo.getQrestrictionTargetOperationNumber())) {
                    log.info("targetRoute/targetOperationNumber are updated.");
                    qTimeInfoChange = true;

                    //TargetRoute/OpeNo should be forward operation including current operation
                    Inputs.ObjProcessOperationProcessRefListForLotIn inParams = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                    inParams.setSearchDirection(true);
                    inParams.setPosSearchFlag(false);
                    inParams.setSearchCount(9999);
                    inParams.setSearchRouteID(qtimeInfo.getQrestrictionTargetRouteID());
                    inParams.setSearchOperationNumber(qtimeInfo.getQrestrictionTargetOperationNumber());
                    inParams.setCurrentFlag(true);
                    inParams.setLotID(params.getLotID());
                    List<Infos.OperationNumberListAttributes> processRetCode = processMethod.processOperationNumberListForLot(objCommon, inParams);

                    int processOperationNumberLen = CimArrayUtils.getSize(processRetCode);
                    Infos.OperationNumberListAttributes theLastAttributes = (processOperationNumberLen > 0)
                            ? (processRetCode.get(processOperationNumberLen - 1))
                            : (new Infos.OperationNumberListAttributes());
                    if (!ObjectIdentifier.equalsWithValue(qtimeInfo.getQrestrictionTargetRouteID(), theLastAttributes.getRouteID())
                            || !CimStringUtils.equals(qtimeInfo.getQrestrictionTargetOperationNumber(), theLastAttributes.getOperationNumber())) {
                        log.info("The target operation may be more backward to the lot's current operation.");
                        throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg(),"");
                    }

                    // Target route is changed
                    if (!ObjectIdentifier.equalsWithValue(aQTimeInfo.getTargetMainProcessDefinition(), qtimeInfo.getQrestrictionTargetRouteID())) {
                        log.info("targetRoute is updated.");

                        //Get the target lot's process flow context
                        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
                        Validations.check(aProcessFlowContext == null, new OmCode(retCodeConfig.getNotFoundPfx(),""));

                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aProcessDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,qtimeInfo.getQrestrictionTargetRouteID());
                        aQTimeRestriction.setTargetMainProcessDefinition( aProcessDefinition );

                        //Get branchInfo of lot's current route
                        List<ProcessDTO.BranchInfo> branchInfoList = aProcessFlowContext.allBranchInfos();

                        int branchInfoLen = CimArrayUtils.getSize(branchInfoList);
                        List<ProcessDTO.BranchInfo> tmpBranchInfoList = new ArrayList<>();
                        for (int branchInfoIdx = 0; branchInfoIdx < branchInfoLen; branchInfoIdx++) {
                            log.info("loop {} to the length of branchInfoList.", branchInfoIdx);
                            ProcessDTO.BranchInfo branchInfo = branchInfoList.get(branchInfoIdx);
                            if (CimStringUtils.equals(qtimeInfo.getQrestrictionTargetRouteID().getValue(), branchInfo.getRouteID().getValue())) {
                                log.info("TargetRouteID = BranchRouteID = {}", qtimeInfo.getQrestrictionTargetRouteID().getValue());
                                break;
                            }
                            tmpBranchInfoList.add(branchInfo);
                        }

                        //Get returnInfo of lot's current route
                        List<ProcessDTO.ReturnInfo> returnInfoList = aProcessFlowContext.allReturnInfos();

                        int returnInfoLen = CimArrayUtils.getSize(returnInfoList);
                        List<ProcessDTO.ReturnInfo> tmpReturnInfoList = new ArrayList<>();
                        for (int returnInfoIdx = 0; returnInfoIdx < returnInfoLen; returnInfoIdx++) {
                            log.info("loop {} to the length of returnInfoList.", returnInfoIdx);
                            ProcessDTO.ReturnInfo returnInfo = returnInfoList.get(returnInfoIdx);

                            if (CimStringUtils.equals(qtimeInfo.getQrestrictionTargetRouteID().getValue(), returnInfo.getRouteID().getValue())) {
                                log.info("TargetRouteID = ReturnRouteID = {}", qtimeInfo.getQrestrictionTargetRouteID().getValue());
                                break;
                            }
                            tmpReturnInfoList.add(returnInfo);
                        }

                        String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(tmpBranchInfoList);

                        String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(tmpReturnInfoList);

                        // Set branchInfo
                        aQTimeRestriction.setTargetBranchInfo(branchInfo);

                        // Set returnInfo
                        aQTimeRestriction.setTargetReturnInfo(returnInfo);
                    }

                    // Target operationNumber is changed
                    if (!CimStringUtils.equals(aQTimeInfo.getTargetOperationNumber(), qtimeInfo.getQrestrictionTargetOperationNumber())) {
                        log.info("targetOpeNo is updated.");
                        aQTimeRestriction.setTargetOperationNumber(qtimeInfo.getQrestrictionTargetOperationNumber());
                    }

                    // Check target operation. TargetRoute/OpeNo should be  operation to the forward than TriggerRoute/OpeNo.;
                    // Convert structure sequence, and add top route
                    List<Infos.BranchInfo> triggerBranchRouteList = setAllRouteList(Infos.BranchInfo.class, aQTimeInfo.getTriggerBranchInfo(), aQTimeInfo.getTriggerMainProcessDefinition(), aQTimeInfo.getTriggerOperationNumber());
                    List<Infos.ReturnInfo> triggerReturnRouteList = setAllRouteList(Infos.ReturnInfo.class, aQTimeInfo.getTriggerReturnInfo(), aQTimeInfo.getTriggerMainProcessDefinition(), aQTimeInfo.getTriggerOperationNumber());
                    List<Infos.BranchInfo> targetBranchRouteList = setAllRouteList(Infos.BranchInfo.class, aQTimeRestriction.getTargetBranchInfo(), qtimeInfo.getQrestrictionTargetRouteID(), qtimeInfo.getQrestrictionTargetOperationNumber());

                    // Search same as route of Q-Time trigger and target
                    // (If branch operation is the same, it is assumed the same route)
                    int trigInfoLen = CimArrayUtils.getSize(triggerBranchRouteList);
                    int targInfoLen = CimArrayUtils.getSize(targetBranchRouteList);
                    int chkRouteMax = (trigInfoLen <= targInfoLen) ? trigInfoLen : targInfoLen;

                    Infos.ReturnInfo checkTriggerInfo = null;
                    Infos.BranchInfo checkTargetInfo = null;
                    Boolean notExistOnCheckRouteFlag = false;
                    int sameRouteIdx = 0;

                    // Search from lower route to upper route
                    for (int chkRouteIdx = 0; chkRouteIdx < chkRouteMax; chkRouteIdx++) {
                        log.info("chkRouteIdx = {}", chkRouteIdx);
                        Infos.BranchInfo checkTriggerBranchRoute = triggerBranchRouteList.get(chkRouteIdx);
                        Infos.BranchInfo checkTargetBranchRoute = targetBranchRouteList.get(chkRouteIdx);
                        // Same route
                        if (ObjectIdentifier.equalsWithValue(checkTriggerBranchRoute.getRouteID(), checkTargetBranchRoute.getRouteID())) {
                            log.info("Same route is {}", checkTriggerBranchRoute.getRouteID().getValue());
                            // Sub route
                            if (0 < chkRouteIdx) {
                                log.info("0 < chkRouteIdx");
                                Infos.BranchInfo preTriggerBranchRoute = triggerBranchRouteList.get(chkRouteIdx - 1);
                                Infos.BranchInfo preTargetBranchRoute = targetBranchRouteList.get(chkRouteIdx - 1);
                                // The operation that branched is the same
                                if (CimStringUtils.equals(preTriggerBranchRoute.getOperationNumber(), preTargetBranchRoute.getOperationNumber())) {
                                    log.info("The operation {} that branched is the same.", preTriggerBranchRoute.getOperationNumber());
                                    // Same route
                                    // Check trigger operation is return operation of the route
                                    checkTriggerInfo = new Infos.ReturnInfo();
                                    checkTriggerInfo.setRouteID(triggerReturnRouteList.get(chkRouteIdx).getRouteID());
                                    checkTriggerInfo.setOperationNumber(triggerReturnRouteList.get(chkRouteIdx).getOperationNumber());
                                    checkTriggerInfo.setMainProcessFlow(triggerReturnRouteList.get(chkRouteIdx).getMainProcessFlow());
                                    checkTriggerInfo.setModuleProcessFlow(triggerReturnRouteList.get(chkRouteIdx).getModuleProcessFlow());
                                    checkTriggerInfo.setProcessFlow(triggerReturnRouteList.get(chkRouteIdx).getProcessFlow());

                                    // Check target operation is branch operation of the route
                                    checkTargetInfo = new Infos.BranchInfo();
                                    checkTargetInfo.setRouteID(checkTargetBranchRoute.getRouteID());
                                    checkTargetInfo.setOperationNumber(checkTargetBranchRoute.getOperationNumber());
                                    checkTargetInfo.setProcessOperation(checkTargetBranchRoute.getProcessOperation());
                                    checkTargetInfo.setReworkOutKey(checkTargetBranchRoute.getReworkOutKey());
                                    sameRouteIdx = chkRouteIdx;
                                }
                                // The operation that branched is not the same
                                else {
                                    log.info("The operation {} that branched is not the same {}",
                                            preTriggerBranchRoute.getOperationNumber(),
                                            preTargetBranchRoute.getOperationNumber());
                                    // Not same route
                                    break;
                                }
                            }
                            // Main route
                            else {
                                log.info("Main route");
                                // Same route
                                // Check trigger operation is return operation of the route
                                checkTriggerInfo = new Infos.ReturnInfo();
                                checkTriggerInfo.setRouteID(triggerReturnRouteList.get(chkRouteIdx).getRouteID());
                                checkTriggerInfo.setOperationNumber(triggerReturnRouteList.get(chkRouteIdx)
                                        .getOperationNumber());
                                checkTriggerInfo.setMainProcessFlow(triggerReturnRouteList.get(chkRouteIdx)
                                        .getMainProcessFlow());
                                checkTriggerInfo.setModuleProcessFlow(triggerReturnRouteList.get(chkRouteIdx)
                                        .getModuleProcessFlow());
                                checkTriggerInfo.setProcessFlow(triggerReturnRouteList.get(chkRouteIdx)
                                        .getProcessFlow());

                                // Check target operation is branch operation of the route
                                checkTargetInfo = new Infos.BranchInfo();
                                checkTargetInfo.setRouteID(checkTargetBranchRoute.getRouteID());
                                checkTargetInfo.setOperationNumber(checkTargetBranchRoute.getOperationNumber());
                                checkTargetInfo.setProcessOperation(checkTargetBranchRoute.getProcessOperation());
                                checkTargetInfo.setReworkOutKey(checkTargetBranchRoute.getReworkOutKey());
                                sameRouteIdx = chkRouteIdx;
                            }
                        }
                        // Not same route
                        else {
                            log.info("{} is different from {}",
                                    triggerReturnRouteList.get(chkRouteIdx).getRouteID().getValue(),
                                    checkTargetBranchRoute.getRouteID().getValue());
                            break;
                        }
                    }

                    // If trigger operation not exists on check route.
                    if ((trigInfoLen - 1) != sameRouteIdx) {
                        notExistOnCheckRouteFlag = true;
                    }

                    Boolean isAfterOperationFlag = false;

                    // Same route was found
                    if (null != checkTriggerInfo) {
                        log.info("Same route was found");

                        // Get main PD
                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = baseCoreFactory
                                .getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,
                                        checkTriggerInfo.getRouteID());

                        // Get main PF
                        com.fa.cim.newcore.bo.pd.CimProcessFlow aMainPF = aMainPD.getActiveMainProcessFlow();
                        Validations.check(aMainPF == null,
                                new OmCode(retCodeConfig.getNotFoundProcessFlow(),""));
                        // Get module number and module operation number of target operation
                        String targetModuleNo = BaseStaticMethod
                                .convertOpeNoToModuleNo(checkTargetInfo.getOperationNumber());
                        String targetModuleOpeNo = BaseStaticMethod
                                .convertOpeNoToModuleOpeNo(checkTargetInfo.getOperationNumber());

                        // Get module PD of target operation
                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aModulePD = aMainPF
                                .findProcessDefinitionOnDefault(targetModuleNo);
                        Validations.check(aModulePD == null, retCodeConfig.getNotFoundProcessDefinition());

                        // Get module PF of target operation
                        com.fa.cim.newcore.bo.pd.CimProcessFlow aModulePF = aModulePD.getActiveProcessFlow();
                        Validations.check(aModulePF == null, retCodeConfig.getNotFoundProcessFlow());

                        // Get module POS of target operation
                        com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aModulePOS = aModulePF
                                .findProcessOperationSpecificationOnDefault(targetModuleOpeNo);
                        Validations.check(aModulePOS == null, retCodeConfig.getNotFoundPos());
                        CimProcessFlowDO nilModuleProcessFlow = null;
                        if (notExistOnCheckRouteFlag) {
                            log.info("notExistOnCheckRouteFlag == TRUE");
                            // target operation is forward operation (including trigger operation);
                            isAfterOperationFlag = !(aMainPF.isAfterOperationNumberForMain(null,
                                    checkTriggerInfo.getOperationNumber(), checkTargetInfo.getOperationNumber()));
                        } else {
                            log.info("notExistOnCheckRouteFlag == FALSE");
                            // the first parameter is nilModuleProcessFlow or aMainPF : todo confirm?
                            isAfterOperationFlag = aMainPF.isAfterOperationNumberForMain(null,
                                    checkTargetInfo.getOperationNumber(), checkTriggerInfo.getOperationNumber());
                        }
                    }
                    Validations.check(!isAfterOperationFlag, retCodeConfigEx.getInvalidTargetOperation(),
                            checkTargetInfo.getOperationNumber(),checkTriggerInfo.getOperationNumber());
                }

                if (aQTimeInfo.getWatchdogRequired() && CimStringUtils.equals("N", qtimeInfo.getWatchDogRequired())) {
                    log.info("watchdogRequired is updated.");
                    qTimeInfoChange = true;

                    //Set watchdogRequired
                    aQTimeRestriction.makeWatchDogNotRequired();
                }

                if ((!aQTimeInfo.getWatchdogRequired()) && CimStringUtils.equals("Y", qtimeInfo.getWatchDogRequired())) {
                    log.info("watchdogRequired is updated.");
                    qTimeInfoChange = true;
                    //Set watchdogRequired
                    aQTimeRestriction.makeWatchDogRequired();
                }

                Boolean qTimeActionInfoChange = false;
                Boolean qTimeResetActionDone = false;
                List<ProcessDTO.QTimeRestrictionAction> aQTimeRestrictionActionList = aQTimeRestriction.getQTimeRestrictionActions();

                Boolean isWatchDogReqFlag = aQTimeRestriction.isWatchDogRequired();

                // Make current action's key list;
                int currentActionLen = CimArrayUtils.getSize(aQTimeRestrictionActionList);
                for (int currentActionIdx = 0; currentActionIdx < currentActionLen; currentActionIdx++) {
                    ProcessDTO.QTimeRestrictionAction aQTimeRestrictionAction = aQTimeRestrictionActionList.get(currentActionIdx);

                    // Action's key list for existence check;
                    String actionKey = aQTimeRestrictionAction.getAction();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += aQTimeRestrictionAction.getTargetTimeStamp();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += ObjectIdentifier.fetchValue(aQTimeRestrictionAction.getReasonCode());
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += ObjectIdentifier.fetchValue(aQTimeRestrictionAction.getActionRouteID());
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += aQTimeRestrictionAction.getOperationNumber();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += aQTimeRestrictionAction.getTiming();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += ObjectIdentifier.fetchValue(aQTimeRestrictionAction.getMainProcessDefinition());
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += ObjectIdentifier.fetchValue(aQTimeRestrictionAction.getMessageDefinition());
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += aQTimeRestrictionAction.getCustomField();
                    if (!actionIndexList.containsKey(actionKey)) {
                        log.info("add {} to actionIndexList", actionKey);
                        actionIndexList.put(actionKey, currentActionIdx);
                    }
                }

                int actionInfoLen = CimArrayUtils.getSize(qtimeInfo.getStrQtimeActionInfoList());

                // The input actions are different from current actions
                if (currentActionLen != actionInfoLen) {
                    log.info("Action was added or deleted");
                    qTimeActionInfoChange = true;
                }

                List<ProcessDTO.QTimeRestrictionAction> newActions = new ArrayList<>();
                for (int actionInfoIdx = 0; actionInfoIdx < actionInfoLen; actionInfoIdx++) {
                    Infos.QTimeActionInfo strQtimeActionInfo = qtimeInfo.getStrQtimeActionInfoList().get(actionInfoIdx);

                    // Action existence check;
                    String actionKey = strQtimeActionInfo.getQrestrictionAction();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getQrestrictionTargetTimeStamp();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getReasonCodeID().getValue();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getActionRouteID().getValue();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getActionOperationNumber();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getFutureHoldTiming();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getReworkRouteID().getValue();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getMessageID().getValue();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getCustomField();
                    int currentIndex = 0;
                    Boolean changedFlag = !(actionIndexList.containsKey(actionKey) && actionIndexList.containsValue(currentIndex));

                    // Duplication check
                    actionKey = strQtimeActionInfo.getQrestrictionAction();
                    actionKey += BizConstant.SP_KEY_SEPARATOR_DOT;
                    actionKey += strQtimeActionInfo.getQrestrictionTargetTimeStamp();
                    Validations.check(actionKeysTable.containsKey(actionKey), retCodeConfig.getInvalidParameterWithMsg());
                    actionKeysTable.put(actionKey, actionKey);

                    // Action changed or added
                    if (changedFlag) {
                        log.info("Action changed or added");

                        qTimeActionInfoChange = true;
                        qTimeResetActionDone = true;

                        // Q-Time action consistency check;
                        Inputs.ObjQTimeActionConsistencyCheck consistencyCheck = new Inputs.ObjQTimeActionConsistencyCheck();
                        consistencyCheck.setLotID(params.getLotID());
                        consistencyCheck.setStrQtimeActionInfo(strQtimeActionInfo);
                        consistencyCheck.setTriggerRouteID(aQTimeInfo.getTriggerMainProcessDefinition());
                        consistencyCheck.setTriggerOperationNumber(aQTimeInfo.getTriggerOperationNumber());
                        consistencyCheck.setTriggerBranchInfo(aQTimeInfo.getTriggerBranchInfo());
                        consistencyCheck.setTriggerReturnInfo(aQTimeInfo.getTriggerReturnInfo());
                        qTimeActionConsistencyCheck(objCommon, consistencyCheck);
                        // It should be after trigger date time
                        if (!CimStringUtils.isEmpty(strQtimeActionInfo.getQrestrictionTargetTimeStamp()) &&
                                CimDateUtils.compare(triggerTimeStamp, strQtimeActionInfo.getQrestrictionTargetTimeStamp()) == 0) {
                            log.info("triggerTimeStamp.isGreaterThan( strQtimeActionInfo.qrestrictionTargetTimeStamp ) == TRUE");
                            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                        }
                    }

                    Boolean dispatchPrecedeFlag = CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, strQtimeActionInfo.getQrestrictionAction());
                    // Action is DispatchPrecede
                    if (dispatchPrecedeFlag) {
                        log.info("DispatchPrecede action");
                        if (0!= CimDateUtils.compare(qtimeInfo.getQrestrictionTargetTimeStamp(), strQtimeActionInfo.getQrestrictionTargetTimeStamp())) {
                            log.info("Target time of Q-Time is different from target time of DispatchPrecede action.");
                            throw new ServiceException(retCodeConfig.getNotSameDispatchPrecedeTargetTime());
                        }
                    }

                    ProcessDTO.QTimeRestrictionAction qTimeActionInfo = new ProcessDTO.QTimeRestrictionAction();
                    qTimeActionInfo.setTargetTimeStamp       (strQtimeActionInfo.getQrestrictionTargetTimeStamp());
                    qTimeActionInfo.setAction                (strQtimeActionInfo.getQrestrictionAction());
                    qTimeActionInfo.setReasonCode            (strQtimeActionInfo.getReasonCodeID());
                    qTimeActionInfo.setActionRouteID         (strQtimeActionInfo.getActionRouteID());
                    qTimeActionInfo.setOperationNumber       (strQtimeActionInfo.getActionOperationNumber());
                    qTimeActionInfo.setTiming                (strQtimeActionInfo.getFutureHoldTiming());
                    qTimeActionInfo.setMainProcessDefinition (strQtimeActionInfo.getReworkRouteID());
                    qTimeActionInfo.setMessageDefinition     (strQtimeActionInfo.getMessageID());
                    qTimeActionInfo.setCustomField           (strQtimeActionInfo.getCustomField());

                    // Action changed or added
                    if (changedFlag) {
                        log.info("Action changed or added");
                        qTimeActionInfo.setWatchdogRequired(true);
                        qTimeActionInfo.setActionDone(dispatchPrecedeFlag);
                    } else {
                        log.info("Action is not changed");
                        qTimeActionInfo.setWatchdogRequired(aQTimeRestrictionActionList.get(currentIndex).getWatchdogRequired());
                        qTimeActionInfo.setActionDone(aQTimeRestrictionActionList.get(currentIndex).getActionDone());
                    }
                    if (!qTimeActionInfo.getWatchdogRequired().equals(isWatchDogReqFlag)) {
                        qTimeActionInfoChange = true;
                        qTimeActionInfo.setWatchdogRequired(isWatchDogReqFlag);
                    }
                    newActions.add(qTimeActionInfo);
                }

                if (qTimeActionInfoChange) {
                    log.info("qTimeActionInfoChange == TRUE");
                    //Set CimQTimeRestriction actions
                    aQTimeRestriction.setQTimeRestrictionActions( newActions );

                    if (qTimeResetActionDone) {
                        log.info("qTimeResetActionDone == TRUE");
                        //Reset CimQTimeRestriction actionDone
                        aQTimeRestriction.makeActionNotDone( );
                    }
                }

                if (qTimeInfoChange || qTimeActionInfoChange) {
                    log.info("qTimeInfoChange == TRUE || qTimeActionInfoChange == TRUE");

                    // for before migration(originalQTime is blank)
                    aQTimeRestriction.setOriginalQTime(qtimeInfo.getOriginalQTime());

                    //Make event
                    ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTimeRestriction.getQTimeRestrictionInfo();
                    if (aQTimeRestrictionInfo == null || CimStringUtils.isEmpty(aQTimeRestrictionInfo.getTriggerOperationNumber())) {
                        log.info("The target lot's Q-Time information was not found.");
                        throw new ServiceException(retCodeConfig.getNotFoundQtime());
                    }
                    Infos.QtimeInfo qtimeInfo2 = new Infos.QtimeInfo();
                    qtimeInfo2.setWaferID(aQTimeRestrictionInfo.getWaferID());
                    qtimeInfo2.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                    qtimeInfo2.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                    qtimeInfo2.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                    qtimeInfo2.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                    qtimeInfo2.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    qtimeInfo2.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                    qtimeInfo2.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                    qtimeInfo2.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                    qtimeInfo2.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                    qtimeInfo2.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                    qtimeInfo2.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                    qtimeInfo2.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                    qtimeInfo2.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                    qtimeInfo2.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                    qtimeInfo2.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                    qtimeInfo2.setSpecificControl(aQTimeRestrictionInfo.getControl());
                    qtimeInfo2.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                    qtimeInfo2.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                    qtimeInfo2.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                    List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aQTimeRestrictionInfo.getActions();
                    if (!CimObjectUtils.isEmpty(qTimeActions)) {
                        List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                        for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                            qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                            qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                            qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                            qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                            qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                            strQtimeActionInfoList.add(qTimeActionInfo);
                        }
                        qtimeInfo2.setStrQtimeActionInfoList(strQtimeActionInfoList);
                    }
                    //qTimeChangeEvent_Make__180
                    Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                    qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                    qTimeChangeEventMakeParams.setLotID(params.getLotID());
                    qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo2);
                    qTimeChangeEventMakeParams.setClaimMemo("");
                    eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);
                } else {
                    log.info("There is no updated data in Q-Time timer.");
                    throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                }
            }
        } else if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE, params.getActionType())) {
            log.info("actionType is Delete");

            for (int iCnt1 = 0; iCnt1 < qtimeInfoLen; iCnt1++) {
                Infos.QrestTimeInfo qtimeInfo = params.getQtimeInfoList().get(iCnt1);
                log.info("loop {} to the length of params.getQtimeInfoList()", iCnt1);

                //Existence check of Q-Time timer to be updated;
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = aLot.findQTimeRestrictionByOriginalQTime(qtimeInfo.getOriginalQTime());
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                //Prepare for event make
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTimeRestriction.getQTimeRestrictionInfo();
                if (aQTimeRestrictionInfo == null || CimStringUtils.isEmpty(aQTimeRestrictionInfo.getTriggerOperationNumber())) {
                    log.info("The target lot's Q-Time information was not found.");
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }

                //Remove qtime timer object from lot

                aLot.removeQTimeRestriction(aQTimeRestriction);

                //Delete qtime timer object
                qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestriction);


                //Make event
                Infos.QtimeInfo qtimeInfo2 = new Infos.QtimeInfo();
                qtimeInfo2.setWaferID(aQTimeRestrictionInfo.getWaferID());
                qtimeInfo2.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                qtimeInfo2.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                qtimeInfo2.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                qtimeInfo2.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                qtimeInfo2.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                qtimeInfo2.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                qtimeInfo2.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                qtimeInfo2.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                qtimeInfo2.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                qtimeInfo2.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                qtimeInfo2.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                qtimeInfo2.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                qtimeInfo2.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                qtimeInfo2.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                qtimeInfo2.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                qtimeInfo2.setSpecificControl(aQTimeRestrictionInfo.getControl());
                qtimeInfo2.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                qtimeInfo2.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                qtimeInfo2.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aQTimeRestrictionInfo.getActions();
                if (!CimObjectUtils.isEmpty(qTimeActions)) {
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                        qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                        qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                        strQtimeActionInfoList.add(qTimeActionInfo);
                    }
                    qtimeInfo2.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                //qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
                qTimeChangeEventMakeParams.setLotID(params.getLotID());
                qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo2);
                qTimeChangeEventMakeParams.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);
            }
        } else {
            log.info("actionType is invalid");
            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
        }

        log.info("【Method Exit】qTimeInfoUpdate()");
    }

    @Override
    public void qTimeActionConsistencyCheck(Infos.ObjCommon objCommon, Inputs.ObjQTimeActionConsistencyCheck consistencyCheck) {
        log.info("【Method Entry】qTimeActionConsistencyCheck()");
        //Trace InParameters
        log.info("in-parm lotID = {}", consistencyCheck.getLotID().getValue());
        log.info("in-parm qrestrictionAction = {}", consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction());
        log.info("in-parm reasonCodeID = {}", consistencyCheck.getStrQtimeActionInfo().getReasonCodeID().getValue());
        log.info("in-parm actionRouteID = {} ", consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue());
        log.info("in-parm actionOperationNumber = {}", consistencyCheck.getStrQtimeActionInfo().getActionOperationNumber());
        log.info("in-parm futureHoldTiming = {}", consistencyCheck.getStrQtimeActionInfo().getFutureHoldTiming());
        log.info("in-parm reworkRouteID = {}", consistencyCheck.getStrQtimeActionInfo().getReworkRouteID().getValue());
        log.info("in-parm messageID = {}", consistencyCheck.getStrQtimeActionInfo().getMessageID().getValue());
        log.info("in-parm customField = {}", consistencyCheck.getStrQtimeActionInfo().getCustomField());
        log.info("in-parm triggerRouteID = {}", consistencyCheck.getTriggerRouteID().getValue());
        log.info("in-parm triggerOperationNumber = {}", consistencyCheck.getTriggerOperationNumber());
        log.info("in-parm triggerBranchInfo = {}", consistencyCheck.getTriggerBranchInfo());
        log.info("in-parm triggerReturnInfo = {}", consistencyCheck.getTriggerReturnInfo());

        // FutureHold or futurerework
        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD  , consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())
                || CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
            log.info("FutureHold or futurerework");

            // ActionRoute/ActionOpeNo check
            if (CimStringUtils.isEmpty(consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue())
                    || CimStringUtils.isEmpty(consistencyCheck.getStrQtimeActionInfo().getActionOperationNumber())) {
                log.info("ActionRoute/ActionOpeNo is blank.");
                throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
            }

            Inputs.ObjProcessOperationProcessRefListForLotIn inputs = new Inputs.ObjProcessOperationProcessRefListForLotIn();
            inputs.setSearchDirection(true);
            inputs.setPosSearchFlag(false);
            inputs.setSearchCount(9999);
            inputs.setSearchRouteID(consistencyCheck.getStrQtimeActionInfo().getActionRouteID());
            inputs.setSearchOperationNumber(consistencyCheck.getStrQtimeActionInfo().getActionOperationNumber());
            inputs.setCurrentFlag(true);
            inputs.setLotID(consistencyCheck.getLotID());
            List<Infos.OperationProcessRefListAttributes> processOut = processMethod.processOperationProcessRefListForLot(objCommon, inputs);

            int opeLen = CimArrayUtils.getSize(processOut);
            //Verify that the target operation is not more backward to the target lot's current operation ;
            if (opeLen <= 0) {
                log.info("process_OperationProcessRefListForLot() opeLen =< 0 current process not found");
                throw new ServiceException(retCodeConfig.getNotFoundCorrpo());
            }

            Infos.OperationProcessRefListAttributes refListAttributes = processOut.get(opeLen - 1);
            if (!CimStringUtils.equals(consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue(), refListAttributes.getRouteID().getValue())
                    || !CimStringUtils.equals(consistencyCheck.getStrQtimeActionInfo().getActionOperationNumber(), refListAttributes.getOperationNumber())) {
                log.info("The action operation may be more backward to the lot's current operation.");
                throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
            }

            //The target lot ;
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,consistencyCheck.getLotID());
            Validations.check(aLot == null, retCodeConfig.getNotFoundLot());

            //The target lot's process flow context;
            com.fa.cim.newcore.bo.pd.CimProcessFlowContext aPFX = aLot.getProcessFlowContext();
            Validations.check(aPFX == null, retCodeConfig.getNotFoundPfx());

            //Get branchInfo of lot's current route
            List<ProcessDTO.BranchInfo> branchInfoList = aPFX.allBranchInfos();

            int branchInfoLen = CimArrayUtils.getSize(branchInfoList);
            List<ProcessDTO.BranchInfo> tmpBranchInfoList = new ArrayList<>();
            for (int branchInfoIdx = 0; branchInfoIdx < branchInfoLen; branchInfoIdx++) {
                ProcessDTO.BranchInfo branchInfo = branchInfoList.get(branchInfoIdx);
                if (CimStringUtils.equals(consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue(), branchInfo.getRouteID().getValue())) {
                    log.info("ActionRouteID and BranchRouteID both are {}.", consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue());
                    break;
                }
                tmpBranchInfoList.add(branchInfo);
            }
            String actionBranchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(tmpBranchInfoList);

            // Check action operation. ActionRoute/OpeNo should be  operation to the forward than TriggerRoute/OpeNo.;
            // Convert structure sequence, and add top route;
            List<Infos.SimpleOperationInfo> triggerBranchRouteList = setAllRouteList(Infos.SimpleOperationInfo.class, consistencyCheck.getTriggerBranchInfo(), consistencyCheck.getTriggerRouteID(), consistencyCheck.getTriggerOperationNumber());
            List<Infos.SimpleOperationInfo> triggerReturnRouteList = setAllRouteList(Infos.SimpleOperationInfo.class, consistencyCheck.getTriggerReturnInfo(), consistencyCheck.getTriggerRouteID(), consistencyCheck.getTriggerOperationNumber());
            List<Infos.SimpleOperationInfo> actionBranchRouteList = setAllRouteList(Infos.SimpleOperationInfo.class, actionBranchInfo, consistencyCheck.getStrQtimeActionInfo().getActionRouteID(), consistencyCheck.getStrQtimeActionInfo().getActionOperationNumber());

            // Search same as route of Q-Time trigger and target
            // (If branch operation is the same, it is assumed the same route)
            int trigInfoLen = CimArrayUtils.getSize(triggerBranchRouteList);
            int actionInfoLen = CimArrayUtils.getSize(actionBranchRouteList);
            int chkRouteMax = (trigInfoLen <= actionInfoLen) ? trigInfoLen : actionInfoLen;

            Infos.SimpleOperationInfo triggerInfoChecked = null;
            Infos.SimpleOperationInfo actionInfoChecked = null;
            Boolean notExistOnCheckRouteFlag = false;
            int sameRouteIdx = 0;

            // Search from lower route to upper route
            for (int chkRouteIdx = 0; chkRouteIdx < chkRouteMax; chkRouteIdx++) {
                log.info("chkRouteIdx = {}", chkRouteIdx);
                if (null == triggerBranchRouteList) {
                    break;
                }
                Infos.SimpleOperationInfo checkTriggerBranchRoute = triggerBranchRouteList.get(chkRouteIdx);
                Infos.SimpleOperationInfo checkActionBranchRoute = actionBranchRouteList.get(chkRouteIdx);
                // Same route
                if (CimStringUtils.equals(checkTriggerBranchRoute.getRouteID().getValue(), checkActionBranchRoute.getRouteID().getValue())) {
                    log.info("Same route is {}", checkTriggerBranchRoute.getRouteID().getValue());
                    // Sub route
                    if (0 < chkRouteIdx) {
                        log.info("0 < chkRouteIdx");
                        Infos.SimpleOperationInfo preTriggerBranchRoute = triggerBranchRouteList.get(chkRouteIdx - 1);
                        Infos.SimpleOperationInfo preActionBranchRoute = actionBranchRouteList.get(chkRouteIdx - 1);
                        // The operation that branched is the same
                        if (CimStringUtils.equals(preTriggerBranchRoute.getOpeNo(), preActionBranchRoute.getOpeNo())) {
                            log.info("The operation {} that branched is the same.", preTriggerBranchRoute.getOpeNo());
                            // Same route
                            // Check trigger operation is return operation of the route
                            triggerInfoChecked = new Infos.SimpleOperationInfo();
                            triggerInfoChecked.setRouteID(triggerReturnRouteList.get(chkRouteIdx).getRouteID());
                            triggerInfoChecked.setOpeNo(triggerReturnRouteList.get(chkRouteIdx).getOpeNo());

                            // Check target operation is branch operation of the route
                            actionInfoChecked = new Infos.SimpleOperationInfo();
                            actionInfoChecked.setRouteID(checkActionBranchRoute.getRouteID());
                            actionInfoChecked.setOpeNo(checkActionBranchRoute.getOpeNo());
                            sameRouteIdx = chkRouteIdx;
                        }
                        // The operation that branched is not the same
                        else {
                            log.info("The operation {} that branched is not the same {}", preTriggerBranchRoute.getOpeNo(), preActionBranchRoute.getOpeNo());
                            // Not same route
                            break;
                        }
                    }
                    // Main route
                    else {
                        log.info("Main route");
                        // Check trigger operation is return operation of the route
                        triggerInfoChecked = new Infos.SimpleOperationInfo();
                        triggerInfoChecked.setRouteID(triggerReturnRouteList.get(chkRouteIdx).getRouteID());
                        triggerInfoChecked.setOpeNo(triggerReturnRouteList.get(chkRouteIdx).getOpeNo());

                        // Check Action operation is branch operation of the route
                        actionInfoChecked = new Infos.SimpleOperationInfo();
                        actionInfoChecked.setRouteID(checkActionBranchRoute.getRouteID());
                        actionInfoChecked.setOpeNo(checkActionBranchRoute.getOpeNo());
                        sameRouteIdx = chkRouteIdx;
                        sameRouteIdx = chkRouteIdx;
                    }
                }
                // Not same route
                else {
                    log.info("{} is different from {}", triggerReturnRouteList.get(chkRouteIdx).getRouteID().getValue(), checkActionBranchRoute.getRouteID().getValue());
                    break;
                }
            }

            // If trigger operation not exists on check route.
            if ((trigInfoLen - 1) != sameRouteIdx) {
                notExistOnCheckRouteFlag = true;
            }

            Boolean whetherAfterOperationFlag = false;

            // Same route was found
            if (null != triggerInfoChecked) {
                log.info("qTimeActionConsistencyCheck(): Same route was found");

                // qTimeActionConsistencyCheck(): Get main PD
                com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainProcessDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,triggerInfoChecked.getRouteID());

                // qTimeActionConsistencyCheck(): Get main PF
                com.fa.cim.newcore.bo.pd.CimProcessFlow activeMainProcessFlow = aMainProcessDefinition.getActiveMainProcessFlow();
                Validations.check(activeMainProcessFlow == null, retCodeConfig.getNotFoundProcessFlow());
                //qTimeActionConsistencyCheck(): Get module number and module operation number of target operation
                String actionModuleNo = BaseStaticMethod.convertOpeNoToModuleNo(actionInfoChecked.getOpeNo());
                String actionModuleOpeNo = BaseStaticMethod.convertOpeNoToModuleOpeNo(actionInfoChecked.getOpeNo());

                // qTimeActionConsistencyCheck(): Get module PD of target operation
                com.fa.cim.newcore.bo.pd.CimProcessDefinition aModulePD = activeMainProcessFlow.findProcessDefinitionOnDefault(actionModuleNo);
                Validations.check(aModulePD == null, retCodeConfig.getNotFoundProcessDefinition());
                // qTimeActionConsistencyCheck(): Get module PF of target operation
                com.fa.cim.newcore.bo.pd.CimProcessFlow aModulePF = aModulePD.getActiveProcessFlow();
                Validations.check(aModulePF == null, retCodeConfig.getNotFoundProcessFlow());
                // Get module POS of target operation
                com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aModulePOS = aModulePF.findProcessOperationSpecificationOnDefault( actionModuleOpeNo);
                Validations.check(aModulePOS == null, retCodeConfig.getNotFoundPos());

                com.fa.cim.newcore.bo.pd.CimProcessFlow nilModuleProcessFlow = null;
                if (notExistOnCheckRouteFlag) {
                    log.info("qTimeActionConsistencyCheck(): notExistOnCheckRouteFlag == TRUE");
                    // target operation is forward operation (including trigger operation);
                    whetherAfterOperationFlag = !(activeMainProcessFlow.isAfterOperationNumberForMain(nilModuleProcessFlow, triggerInfoChecked.getOpeNo(), actionInfoChecked.getOpeNo()));
                } else {
                    log.info("qTimeActionConsistencyCheck(): notExistOnCheckRouteFlag == FALSE");
                    // the first parameter is nilModuleProcessFlow or aMainPF : todo confirm?
                    whetherAfterOperationFlag = activeMainProcessFlow.isAfterOperationNumberForMain(nilModuleProcessFlow, actionInfoChecked.getOpeNo(), triggerInfoChecked.getOpeNo());
                }
                Validations.check(!whetherAfterOperationFlag, retCodeConfig.getInvalidParameterWithMsg(),whetherAfterOperationFlag);
            }

            // FutureHold
            if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD,
                    consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
                log.info("FutureHold");

                // Check for "futureHoldTiming";
                if (!CimStringUtils.equals(BizConstant.SP_FUTUREHOLD_PRE , consistencyCheck.getStrQtimeActionInfo().getFutureHoldTiming())
                        && !CimStringUtils.equals(BizConstant.SP_FUTUREHOLD_POST, consistencyCheck.getStrQtimeActionInfo().getFutureHoldTiming())) {
                    log.info("Invalid FutureHoldTime {} is specified.", consistencyCheck.getStrQtimeActionInfo().getFutureHoldTiming());
                    throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                }

                // Check for "POST" FutureHoldTime;
                if (CimStringUtils.equals(BizConstant.SP_FUTUREHOLD_POST, consistencyCheck.getStrQtimeActionInfo().getFutureHoldTiming())) {
                    log.info("futureHoldTiming is POST");
                    // Get the target operation's next operation;
                    //The target operation's main process flow;
                    com.fa.cim.newcore.bo.pd.CimProcessFlow aMainPF = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessFlow.class,refListAttributes.getProcessRef().getMainProcessFlow());
                    Validations.check(aMainPF == null, retCodeConfig.getNotFoundProcessFlow());
                    //The target operation's main process definition;
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = aMainPF.getRootProcessDefinition();
                    Validations.check(aMainPD == null, retCodeConfig.getNotFoundProcessDefinition());

                    // Check the main process definition's flow type ;
                    String processFlowType = aMainPD.getProcessFlowType();

                    // If flow type is Main, check the ActionOpeNo is not the last operation.
                    if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN, processFlowType)) {
                        log.info("Action route's ProcessFlowType is Main");

                        //The target operation's module number
                        String moduleNo = refListAttributes.getProcessRef().getModuleNumber();

                        //The target operation's module profess flow
                        com.fa.cim.newcore.bo.pd.CimProcessFlow aModulePF = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessFlow.class,refListAttributes.getProcessRef().getModuleProcessFlow());
                        Validations.check(aModulePF == null, retCodeConfig.getNotFoundProcessFlow());
                        // The target operation's module profess operation specification
                        com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aModulePOS = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification.class,refListAttributes.getProcessRef().getModulePOS());
                        Validations.check(aModulePOS == null, retCodeConfig.getNotFoundPos());
                        AtomicReference<com.fa.cim.newcore.bo.pd.CimProcessFlow> aMainPF_out = new AtomicReference<>(), aModulePF_out = new AtomicReference<>();
                        AtomicReference<String> moduleNo_out = new AtomicReference<>();

                        com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification nextPosRetCode = aPFX.getNextProcessOperationSpecificationOnCurrentRouteFor(aMainPF,     moduleNo,     aModulePF,     aModulePOS,
                                aMainPF_out, moduleNo_out, aModulePF_out);

                        String moduleNum = moduleNo_out.get();
                        Validations.check(nextPosRetCode == null, new OmCode(retCodeConfig.getInvalidParameterWithMsg(),"When Action is FutureHold and ActionOpeNo is the last operation, only \"PRE\" is allowed as the FutureHoldTime."));
                    }
                }

                // Check for "reasonCodeID"
                // Verify that the target reason code exists;
                List<ObjectIdentifier> codeDataIDList = new ArrayList<>();
                ObjectIdentifier codeDataID = consistencyCheck.getStrQtimeActionInfo().getReasonCodeID();
                codeDataIDList.add(codeDataID);

                try {
                    codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, codeDataIDList);
                }catch (ServiceException e) {
                    log.info("qTimeActionConsistencyCheck(): The specified ReasonCode {} was not found in SP_ReasonCat_FutureHold category", codeDataIDList.get(0));
                    codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, codeDataIDList);
                }
            }
            // futurerework
            else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK,
                    consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
                log.info("futurerework");

                // Check for MainPDType of actionRoute
                // Get Process Type of the target route
                String processExistRetCode = processMethod.processExistenceCheck(objCommon,
                        consistencyCheck.getStrQtimeActionInfo().getActionRouteID(),
                        BizConstant.SP_PD_FLOWLEVEL_MAIN);

                //Verify that the type is not "Rework"
                if (CimStringUtils.equals(BizConstant.SP_MAINPDTYPE_REWORK, processExistRetCode)) {
                    log.info("The target route is rework route.");
                    throw new ServiceException(retCodeConfig.getInvalidPDLevel(),processExistRetCode);
                }

                Infos.OperationProcessRefListAttributes strOperationNameAttributesForRwkOpe = new Infos.OperationProcessRefListAttributes();
                //===== Verify that the target operation is not more backward to the target lot's current operation =======//
                if (opeLen > 1) {
                    log.info("The target operation is more forward to the target lot's current operation.");

                    //===== Verify that the target route is same as the target lot's current route =======//
                    Infos.OperationProcessRefListAttributes theFirstRefListAttributes = processOut.get(0);
                    if (!CimStringUtils.equals(consistencyCheck.getStrQtimeActionInfo().getActionRouteID().getValue(), theFirstRefListAttributes.getRouteID().getValue())) {
                        log.info("The target route is not same as the target lot's current route.");
                        //===== Get previous process reference =======//
                        Outputs.objProcessPreviousProcessReferenceOut previousPreocessRetCode = processMethod.processPreviousProcessReferenceGet(objCommon,refListAttributes.getProcessRef());
                        strOperationNameAttributesForRwkOpe.setOperationNumber(previousPreocessRetCode.getPreviousOperationNumber());
                        Infos.ProcessRef processRef = new Infos.ProcessRef();
                        processRef.setProcessFlow(previousPreocessRetCode.getPreviousProcessRef().getProcessFlow());
                        processRef.setProcessOperationSpecification(previousPreocessRetCode.getPreviousProcessRef().getProcessOperationSpecification());
                        processRef.setMainProcessFlow(previousPreocessRetCode.getPreviousProcessRef().getMainProcessFlow());
                        processRef.setModuleNumber(previousPreocessRetCode.getPreviousProcessRef().getModuleNumber());
                        processRef.setModuleProcessFlow(previousPreocessRetCode.getPreviousProcessRef().getModuleProcessFlow());
                        processRef.setModulePOS(previousPreocessRetCode.getPreviousProcessRef().getModulePOS());
                        strOperationNameAttributesForRwkOpe.setProcessRef(processRef);
                    } else {
                        log.info("The target route is same as the target lot's current route.");
                        strOperationNameAttributesForRwkOpe = processOut.get(opeLen - 2);
                    }
                }
                else
                {
                    log.info( "The target operation is same as the target lot's current operation.");

                    //===== Search the rework operation and reset the list of operation attribute =======//
                    ObjectIdentifier searchRouteID_dummy = new ObjectIdentifier("", "");

                    Inputs.ObjProcessOperationProcessRefListForLotIn inParams = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                    inParams.setSearchDirection(false);
                    inParams.setPosSearchFlag(true);
                    inParams.setSearchCount(2);
                    inParams.setSearchRouteID(searchRouteID_dummy);
                    inParams.setSearchOperationNumber("");
                    inParams.setCurrentFlag(true);
                    inParams.setLotID(consistencyCheck.getLotID());
                    List<Infos.OperationProcessRefListAttributes> processForReworkOut = processMethod.processOperationProcessRefListForLot(objCommon, inParams);
                    if(CimArrayUtils.getSize(processForReworkOut) > 1) {
                        strOperationNameAttributesForRwkOpe =  processForReworkOut.get(1);
                    }
                }

                // Check for "reworkRouteID";
                // Get the target rework route's active id ;
                ObjectIdentifier processActiveIdRetCode = processMethod.processActiveIDGet(objCommon,consistencyCheck.getStrQtimeActionInfo().getReworkRouteID());
                ObjectIdentifier  reworkRouteActiveID = processActiveIdRetCode;

                // Check for "reworkRouteID" and "actionOperationNumber";
                // Get the list of the rework routes which are connected to the rework operation
                String mainPF = "";
                mainPF = strOperationNameAttributesForRwkOpe.getProcessRef().getProcessFlow();
                log.info("The key of rework operation's process flow(Level:Main_Ope) is {}.", mainPF);

                String modulePOS = "";
                modulePOS = strOperationNameAttributesForRwkOpe.getProcessRef().getModulePOS();
                log.info("The key of rework operation's process operation specification(Level:Module) is {}.", modulePOS);

                List<Infos.ConnectedRoute> connectedRouteRetCode = processMethod.processConnectedRouteGetDR(objCommon,
                        mainPF,strOperationNameAttributesForRwkOpe.getOperationNumber(),
                        modulePOS,false,true );

                //Verify that the target rework route diverge from the rework operation ;
                int  routeCnt = 0;
                int  routeLen = CimArrayUtils.getSize(connectedRouteRetCode);
                for( routeCnt = 0; routeCnt < routeLen; routeCnt++ )
                {
                    Infos.ConnectedRoute connectedRoute = connectedRouteRetCode.get(routeCnt);
                    if (CimStringUtils.equals(reworkRouteActiveID.getValue(), connectedRoute.getRouteID().getValue())) {
                        log.info("The target rework route diverge from the rework operation.");
                        break;
                    }
                }

                if (routeCnt == routeLen) {
                    log.info("The target rework route don't diverge from the rework operation.");
                    Validations.check(retCodeConfig.getFtRwkDataInvalid(),SP_FUTUREREWORK_ITEM_REWORKROUTEID,ObjectIdentifier.fetchValue(reworkRouteActiveID));
                }

                // Check for "reasonCodeID";
                //Verify that the target reason code exists ;
                List<ObjectIdentifier> strReasonCodeList = new ArrayList<>();
                strReasonCodeList.add(consistencyCheck.getStrQtimeActionInfo().getReasonCodeID());

                codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_REWORK, strReasonCodeList);
            }
        }
        // ImmediateHold
        else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD,
                consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
            // Check for "reasonCodeID";
            //Verify that the target reason code exists;
            List<ObjectIdentifier> strReasonCodeList = new ArrayList<>();
            strReasonCodeList.add(consistencyCheck.getStrQtimeActionInfo().getReasonCodeID());

            try{
                codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, strReasonCodeList);
            }catch (ServiceException e) {
                log.info("The specified ReasonCode was not found in SP_ReasonCat_LotHold category");
                codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, strReasonCodeList);
            }
        }
        // Mail
        else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_MAIL,
                consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
            // Check for "messageID";
            Infos.objObjectIDListGetDR inputParams = new Infos.objObjectIDListGetDR();
            inputParams.setClassName(BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION);
            inputParams.setObjectID(consistencyCheck.getStrQtimeActionInfo().getMessageID());
            Outputs.ObjectIDList objectIDListRetCode = objectMethod.objectIDListGetDR(objCommon, inputParams);
            Validations.check(CimArrayUtils.getSize(objectIDListRetCode.getObjectIDInformationList()) <= 0, retCodeConfig.getInvalidParameterWithMsg());

            //------------------------------------------------------------
            // Check for "reasonCodeID"
            //------------------------------------------------------------
            //===== Verify that the target reason code exists =======//
            List<ObjectIdentifier> strReasonCodeList = new ArrayList<>();
            strReasonCodeList.add(consistencyCheck.getStrQtimeActionInfo().getReasonCodeID());

            codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_MAIL, strReasonCodeList);
        }
        // DispatchPrecede
        else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction())) {
            log.info("DispatchPrecede");
        }
        // Unknown
        else {
            log.info("Unknown action = {}", consistencyCheck.getStrQtimeActionInfo().getQrestrictionAction());
            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
        }

        log.info("【Method Exit】qTimeActionConsistencyCheck()");
    }

    @Override
    public Outputs.ObjQtimeAllClearByRouteChangeOut qtimeAllClearByRouteChange(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Outputs.ObjQtimeAllClearByRouteChangeOut objQtimeAllClearByRouteChangeOut = new Outputs.ObjQtimeAllClearByRouteChangeOut();
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        Validations.check(null == lot, retCodeConfig.getNotFoundLot());
        Outputs.ObjLotQtimeInfoGetForClearOut forClearOut = this.lotQtimeInfoGetForClear(objCommon, lotID, TRUE);
        Outputs.ObjLotQtimeInfoGetForClearOut objLotQtimeInfoGetForClearOut = forClearOut;
        objQtimeAllClearByRouteChangeOut.setStrLotHoldReleaseList(objLotQtimeInfoGetForClearOut.getStrLotHoldReleaseList());
        objQtimeAllClearByRouteChangeOut.setStrFutureHoldCancelList(objLotQtimeInfoGetForClearOut.getStrFutureHoldCancelList());
        objQtimeAllClearByRouteChangeOut.setStrFutureReworkCancelList(objLotQtimeInfoGetForClearOut.getStrFutureReworkCancelList());
        List<String> qTimeClearList = objLotQtimeInfoGetForClearOut.getQTimeClearList();
        if (!CimObjectUtils.isEmpty(qTimeClearList)) {
            for (String qtimeClear : qTimeClearList) {
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimQTimeRestriction.class,qtimeClear);
                if (null == aQTime || CimStringUtils.isEmpty(aQTime.getTriggerOperationNumber())) {
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }
                ProcessDTO.QTimeRestrictionInfo qTimeRestrictionInfo = aQTime.getQTimeRestrictionInfo();
                ObjectIdentifier waferID = qTimeRestrictionInfo.getWaferID();
                if (ObjectIdentifier.isEmpty(waferID)) {
                    lot.removeQTimeRestriction(aQTime);
                } else {
                    com.fa.cim.newcore.bo.product.CimWafer wafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,waferID);
                    Validations.check(null == wafer, retCodeConfig.getNotFoundWafer());
                    wafer.removeQTimeRestriction(aQTime);
                }
                qtimeRestrictionManager.removeQTimeRestriction(aQTime);

                Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                qtimeInfo.setWaferID(qTimeRestrictionInfo.getWaferID());
                qtimeInfo.setQTimeType(qTimeRestrictionInfo.getQTimeType());
                qtimeInfo.setPreTrigger(qTimeRestrictionInfo.getPreTrigger());
                qtimeInfo.setOriginalQTime(qTimeRestrictionInfo.getOriginalQTime());
                qtimeInfo.setProcessDefinitionLevel(qTimeRestrictionInfo.getProcessDefinitionLevel());
                qtimeInfo.setRestrictionTriggerRouteID(qTimeRestrictionInfo.getTriggerMainProcessDefinition());
                qtimeInfo.setRestrictionTriggerOperationNumber(qTimeRestrictionInfo.getTriggerOperationNumber());
                qtimeInfo.setRestrictionTriggerBranchInfo(qTimeRestrictionInfo.getTriggerBranchInfo());
                qtimeInfo.setRestrictionTriggerReturnInfo(qTimeRestrictionInfo.getTriggerReturnInfo());
                qtimeInfo.setRestrictionTriggerTimeStamp(qTimeRestrictionInfo.getTriggerTimeStamp());
                qtimeInfo.setRestrictionTargetRouteID(qTimeRestrictionInfo.getTargetMainProcessDefinition());
                qtimeInfo.setRestrictionTargetOperationNumber(qTimeRestrictionInfo.getTargetOperationNumber());
                qtimeInfo.setRestrictionTargetBranchInfo(qTimeRestrictionInfo.getTargetBranchInfo());
                qtimeInfo.setRestrictionTargetReturnInfo(qTimeRestrictionInfo.getTargetReturnInfo());
                qtimeInfo.setRestrictionTargetTimeStamp(qTimeRestrictionInfo.getTargetTimeStamp());
                qtimeInfo.setPreviousTargetInfo(qTimeRestrictionInfo.getPreviousTargetInfo());
                qtimeInfo.setSpecificControl(qTimeRestrictionInfo.getControl());
                qtimeInfo.setWatchDogRequired(CimBooleanUtils.isTrue(qTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                qtimeInfo.setActionDoneFlag(CimBooleanUtils.isTrue(qTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                qtimeInfo.setManualCreated(qTimeRestrictionInfo.getManualCreated());
                List<ProcessDTO.QTimeRestrictionAction> qTimeActions = qTimeRestrictionInfo.getActions();
                if (!CimObjectUtils.isEmpty(qTimeActions)) {
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    for (ProcessDTO.QTimeRestrictionAction qTimeAction : qTimeActions) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeAction.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(qTimeAction.getAction());
                        qTimeActionInfo.setReasonCodeID(qTimeAction.getReasonCode());
                        qTimeActionInfo.setActionRouteID(qTimeAction.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(qTimeAction.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(qTimeAction.getTiming());
                        qTimeActionInfo.setReworkRouteID(qTimeAction.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(qTimeAction.getMessageDefinition());
                        qTimeActionInfo.setCustomField(qTimeAction.getCustomField());
                        qTimeActionInfo.setWatchDogRequired(CimBooleanUtils.isTrue(qTimeRestrictionInfo.getWatchdogRequired()) ? "Y" : "N");
                        qTimeActionInfo.setActionDoneFlag(CimBooleanUtils.isTrue(qTimeRestrictionInfo.getActionDone()) ? "Y" : "N");
                        strQtimeActionInfoList.add(qTimeActionInfo);
                    }
                    qtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                //qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams qTimeChangeEventMakeParams = new Inputs.QTimeChangeEventMakeParams();
                qTimeChangeEventMakeParams.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
                qTimeChangeEventMakeParams.setLotID(lotID);
                qTimeChangeEventMakeParams.setQtimeInfo(qtimeInfo);
                qTimeChangeEventMakeParams.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, qTimeChangeEventMakeParams);
            }
        }
        return objQtimeAllClearByRouteChangeOut;
    }

    @Override
    public Outputs.ObjQtimeOriginalInformationGetOut qtimeOriginalInformationGet(Infos.ObjCommon objCommon, String originalQTime) {

        List<String> originalQTimeKeys = BaseStaticMethod.splitStringIntoTokens(originalQTime, "\\"+BizConstant.SP_KEY_SEPARATOR_DOT);
        if (CimObjectUtils.isEmpty(originalQTimeKeys) || originalQTimeKeys.size() != 8) {
            return null;
        }
        Outputs.ObjQtimeOriginalInformationGetOut originalInformation = new Outputs.ObjQtimeOriginalInformationGetOut();
        //----------------------------------
        //  trigger route ID
        //----------------------------------
        String originalString = CimArrayUtils.mergeStringIntoTokens(Arrays.asList(originalQTimeKeys.get(0), originalQTimeKeys.get(1)), BizConstant.SP_KEY_SEPARATOR_DOT);
        com.fa.cim.newcore.bo.pd.CimProcessDefinition processDefinition = baseCoreFactory.getBOByIdentifier(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, originalString);
        Validations.check(null == processDefinition, retCodeConfig.getNotFoundProcessDefinition(),originalString);
        originalInformation.setTriggerRouteID(new ObjectIdentifier(processDefinition.getIdentifier(), processDefinition.getPrimaryKey()));

        //----------------------------------
        //  trigger operation number
        //----------------------------------
        originalInformation.setTriggerOperationNumber(CimArrayUtils.mergeStringIntoTokens(Arrays.asList(originalQTimeKeys.get(2), originalQTimeKeys.get(3)), BizConstant.SP_KEY_SEPARATOR_DOT));

        //----------------------------------
        //  target route ID
        //----------------------------------
        originalString = CimArrayUtils.mergeStringIntoTokens(Arrays.asList(originalQTimeKeys.get(4), originalQTimeKeys.get(5)), BizConstant.SP_KEY_SEPARATOR_DOT);
        processDefinition = baseCoreFactory.getBOByIdentifier(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,originalString);
        Validations.check(null == processDefinition, retCodeConfig.getNotFoundProcessDefinition());
        originalInformation.setTargetRouteID(new ObjectIdentifier(processDefinition.getIdentifier(), processDefinition.getPrimaryKey()));

        //----------------------------------
        //  target operation number
        //----------------------------------
        originalInformation.setTargetOperationNumber(CimArrayUtils.mergeStringIntoTokens(Arrays.asList(originalQTimeKeys.get(6), originalQTimeKeys.get(7)), BizConstant.SP_KEY_SEPARATOR_DOT));

        return originalInformation;
    }

    //region private methods;
    private <T> List<T> setAllRouteList(Class<T> className, String stringInfo, ObjectIdentifier routeID, String operationNumber) {
        List<T> objects = null;
        if (className.equals(Infos.SimpleOperationInfo.class)) {
            objects = (List<T>) new ArrayList<Infos.SimpleOperationInfo>();
        } else if (className.equals(Infos.BranchInfo.class)) {
            objects = (List<T>) new ArrayList<Infos.BranchInfo>();
        } else if (className.equals(Infos.ReturnInfo.class)) {
            objects = (List<T>) new ArrayList<Infos.ReturnInfo>();
        }
        if (!CimStringUtils.isEmpty(stringInfo)) {
            log.info("stringInfo is not empty.");
            String[] strings = stringInfo.split("\\.");
            int stringLen = strings.length;
            if (0 == (stringLen % 4)) {
                log.info("(stringLen % 4) == 0");
                int infoCnt = stringLen / 4;
                for (int i = 0; i < infoCnt; i++) {
                    String mergedRouteID = strings[i * 4 + 0] + "." + strings[i * 4 + 1];
                    String mergedOperationNumber = strings[i * 4 + 2] + "." + strings[i * 4 + 3];
                    if (className.equals(Infos.SimpleOperationInfo.class)) {
                        Infos.SimpleOperationInfo simpleOperationInfo = new Infos.SimpleOperationInfo();
                        simpleOperationInfo.setRouteID(new ObjectIdentifier(mergedRouteID));
                        simpleOperationInfo.setOpeNo(mergedOperationNumber);
                        if (null != objects) {
                            objects.add((T) simpleOperationInfo);
                        }
                    } else if (className.equals(Infos.BranchInfo.class)) {
                        Infos.BranchInfo branchInfo = new Infos.BranchInfo();
                        branchInfo.setRouteID(new ObjectIdentifier(mergedRouteID));
                        branchInfo.setOperationNumber(mergedOperationNumber);
                        if (null != objects) {
                            objects.add((T) branchInfo);
                        }
                    } else if (className.equals(Infos.ReturnInfo.class)) {
                        Infos.ReturnInfo returnInfo = new Infos.ReturnInfo();
                        returnInfo.setRouteID(new ObjectIdentifier(mergedRouteID));
                        returnInfo.setOperationNumber(mergedOperationNumber);
                        if (null != objects) {
                            objects.add((T) returnInfo);
                        }
                    }
                }
            }
        } else {
            if (className.equals(Infos.SimpleOperationInfo.class)) {
                objects = (List<T>) new ArrayList<Infos.SimpleOperationInfo>();
                Infos.SimpleOperationInfo simpleOperationInfo = new Infos.SimpleOperationInfo();
                simpleOperationInfo.setRouteID(routeID);
                simpleOperationInfo.setOpeNo(operationNumber);
                objects.add((T) simpleOperationInfo);
            } else if (className.equals(Infos.BranchInfo.class)) {
                Infos.BranchInfo branchInfo = new Infos.BranchInfo();
                branchInfo.setRouteID(routeID);
                branchInfo.setOperationNumber(operationNumber);
                if (null != objects) {
                    objects.add((T) branchInfo);
                }
            } else if (className.equals(Infos.ReturnInfo.class)) {
                Infos.ReturnInfo returnInfo = new Infos.ReturnInfo();
                returnInfo.setRouteID(routeID);
                returnInfo.setOperationNumber(operationNumber);
                if (null != objects) {
                    objects.add((T) returnInfo);
                }
            }
        }
        return objects;
    }

    @Override
    public List<Infos.QTimeActionRegisterInfo> qtimeSetClearByOpeComp(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        List<Infos.QTimeActionRegisterInfo> outObjectList = new ArrayList<>();

        String useCustomFieldFlag = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getValue();
        int nUseCustomFieldFlag = CimNumberUtils.intValue(useCustomFieldFlag);

        //Loop for strStartCassette;
        int startCassetteLen = CimArrayUtils.getSize(startCassetteList);

        for (int i = 0; i < startCassetteLen; i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            // Loop for strLotInCassette;
            int lcLen = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
            for (int j = 0; j < lcLen; j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                //【Step1】Omit Not-Start lot;
                Boolean operationStartFlag = lotInCassette.getMoveInFlag();
                if (!operationStartFlag) {
                    continue;
                }

                //【Step2】Get lot Object ;
                com.fa.cim.newcore.bo.product.CimLot aLot=baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotInCassette.getLotID());
                //【Step3】get process operation by lot;
                com.fa.cim.newcore.bo.pd.CimProcessOperation aPosProcessOperation = aLot.getProcessOperation();
                Validations.check(aPosProcessOperation == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(),""));

                //【Step4】get current process definition by po;
                com.fa.cim.newcore.bo.pd.CimProcessDefinition currentRoute = aPosProcessOperation.getMainProcessDefinition();
                //【Step5】get process operation number;
                String currentOperationNumber = aPosProcessOperation.getOperationNumber();
                Validations.check(currentRoute == null, retCodeConfig.getNotFoundMainRoute());
                //【Step6】get process flow type;
                String processFlowType = currentRoute.getProcessFlowType();
                if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
                    Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
                    inputParams.setLotID(lotInCassette.getLotID());
                    inputParams.setSpecificControlFlag(true);
                    //【Step7】qtime Target Operation Replace;
                    qTimeTargetOpeReplace(objCommon, inputParams);
                }

                //【Step8】Clear qtime info from lot;
                Outputs.ObjLotQtimeInfoGetForClearOut qtimeInfoClearRetCode = this.lotQtimeInfoGetForClear(objCommon, lotInCassette.getLotID(), false);
                Infos.QTimeActionRegisterInfo actionReset = new Infos.QTimeActionRegisterInfo();
                outObjectList.add(actionReset);
                actionReset.setLotID(lotInCassette.getLotID());
                actionReset.setLotHoldList(qtimeInfoClearRetCode.getStrLotHoldReleaseList());
                actionReset.setFutureHoldList(qtimeInfoClearRetCode.getStrFutureHoldCancelList());
                actionReset.setFutureReworkList(qtimeInfoClearRetCode.getStrFutureReworkCancelList());
                int qTimeClearListLen = CimArrayUtils.getSize(qtimeInfoClearRetCode.getQTimeClearList());
                for (int k = 0; k < qTimeClearListLen; k++) {
                    String clearedQTimeId = qtimeInfoClearRetCode.getQTimeClearList().get(k);
                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction =baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimQTimeRestriction.class,clearedQTimeId);
                    Validations.check(aQTimeRestriction == null, new OmCode(retCodeConfig.getNotFoundSystemObj(),"QTimeRestriction"));
                    //【Step9】Get Qtime info;
                    ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTimeRestriction.getQTimeRestrictionInfo();
                    if (aQTimeRestrictionInfo == null ||
                            CimStringUtils.isEmpty(aQTimeRestrictionInfo.getTriggerOperationNumber())) {
                        log.info("The target lot's Q-Time information was not found.");
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFoundQtime(),lotInCassette.getLotID().getValue()));
                    }
                    //【Step10】remove qtime;
                    if (ObjectIdentifier.isEmptyWithValue(aQTimeRestrictionInfo.getWaferID())) {
                        log.info("wafer ID of the Q-Time is empty.");
                        aLot.removeQTimeRestriction(aQTimeRestriction);
                        // lotCore.removeQTimeRestriction(aQTimeRestriction);
                    } else {
                        log.info("wafer ID of the Q-Time is not empty.");
                        com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,aQTimeRestrictionInfo.getWaferID());
                        if (aPosWafer != null) {
                            aPosWafer.removeQTimeRestriction(aQTimeRestriction);
                        }
                    }
                    qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestriction);

                    //【Step11】make qtime event change;
                    Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                    qtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                    qtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                    qtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                    qtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                    qtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                    qtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    qtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                    qtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                    qtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                    qtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                    qtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                    qtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                    qtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                    qtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                    qtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                    qtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                    qtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                    if (aQTimeRestrictionInfo.getWatchdogRequired()) {
                        qtimeInfo.setWatchDogRequired("Y");
                    } else {
                        qtimeInfo.setWatchDogRequired("N");
                    }
                    if (aQTimeRestrictionInfo.getActionDone()) {
                        qtimeInfo.setActionDoneFlag("Y");
                    } else {
                        qtimeInfo.setActionDoneFlag("N");
                    }
                    qtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                    List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aQTimeRestrictionInfo.getActions();
                    int actionLength = CimArrayUtils.getSize(qTimeActions);
                    List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                    qtimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
                    if (actionLength != 0) {
                        for (ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction : qTimeActions) {
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfoList.add(qTimeActionInfo);
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeRestrictionAction.getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(qTimeRestrictionAction.getAction());
                            qTimeActionInfo.setReasonCodeID(qTimeRestrictionAction.getReasonCode());
                            qTimeActionInfo.setActionRouteID(qTimeRestrictionAction.getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(qTimeRestrictionAction.getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(qTimeRestrictionAction.getTiming());
                            qTimeActionInfo.setReworkRouteID(qTimeRestrictionAction.getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(qTimeRestrictionAction.getMessageDefinition());
                            qTimeActionInfo.setCustomField(qTimeRestrictionAction.getCustomField());
                            if (qTimeRestrictionAction.getWatchdogRequired()) {
                                qTimeActionInfo.setWatchDogRequired("Y");
                            } else {
                                qTimeActionInfo.setWatchDogRequired("N");
                            }
                            if (qTimeRestrictionAction.getActionDone()) {
                                qTimeActionInfo.setActionDoneFlag("Y");
                            } else {
                                qTimeActionInfo.setActionDoneFlag("N");
                            }
                        }
                    }
                    Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                    params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
                    params.setLotID(lotInCassette.getLotID());
                    params.setQtimeInfo(qtimeInfo);
                    eventMethod.qTimeChangeEventMake(objCommon, params);

                    //get qtime list;
                    List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qTimeRestrictionList = null;
                    if (CimStringUtils.equals( objCommon.getTransactionID() , "OEQPW012" )
                            || CimStringUtils.equals( objCommon.getTransactionID() , "OEQPW024" )) {
                        //【Step12】allQTimeRestrictionsWithWaferLevelQTimeOld;
                        qTimeRestrictionList = aLot.allQTimeRestrictionsWithWaferLevelQTime();

                    } else {
                        //【Step13】getWaferLevelQTimeCount;
                        int qtimeListForWaferCount = aLot.getWaferLevelQTimeCount();
                        if (qtimeListForWaferCount > 0) {
                            log.info("qtimeSetClearByOpeComp(): Exist WaferLevelQTime.");
                            //【Step14】allQTimeRestrictionsWithWaferLevelQTimeOld;
                            qTimeRestrictionList = aLot.allQTimeRestrictionsWithWaferLevelQTime();
                        } else {
                            log.info("qtimeSetClearByOpeComp(): Not exist WaferLevelQTime.");
                            //【Step15】allQTimeRestrictions;
                            qTimeRestrictionList = aLot.allQTimeRestrictions();
                        }
                    }
                    List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qTimeList = new ArrayList<>();
                    if (CimArrayUtils.getSize(qTimeRestrictionList) > 0) {
                        qTimeRestrictionList.forEach(item -> {
                            if (item != null) {
                                qTimeList.add(item);
                            }
                        });
                    }

                    int qtimeListLen = CimArrayUtils.getSize(qTimeList);
                    for (int ii = 0; ii < qtimeListLen; ii++) {
                        com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qTimeList.get(ii);
                        Validations.check(aQTime == null, new OmCode(retCodeConfig.getNotFoundQtime(),lotInCassette.getLotID().getValue()));
                        //【Step16】makeNotManualCreated;
                        Boolean isManualCreatedFlag = aQTime.isManualCreated();
                        if (CimBooleanUtils.isTrue(isManualCreatedFlag)) {
                            aQTime.makeNotManualCreated();
                        }
                        //【Step17】getPreTriggerFlag;
                        Boolean preTrigger = aQTime.isPreTrigger();
                        if (CimBooleanUtils.isTrue(preTrigger)) {
                            //【Step18】makeNotPreTrigger;
                            aQTime.makeNotPreTrigger();
                        }
                    }
                }
                //【Step19】getProcessFlowContext;
                com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
                Validations.check(aProcessFlowContext == null, new OmCode(retCodeConfig.getNotFoundPfx(),""));
                //【Step20】getProductSpecification;
                CimProductSpecification aProductSpecification = aLot.getProductSpecification();
                Validations.check(aProductSpecification == null, retCodeConfig.getNotFoundProductSpec());

                //【Step21】findTimeRestrictionsForProduct;
                String anProductSpec = aProductSpecification.getPrimaryKey();
                List<ProcessDTO.TimeRestrictionSpecification> aTimeRSList = aPosProcessOperation.findTimeRestrictionsForProduct(aProductSpecification);
                int nLen = CimArrayUtils.getSize(aTimeRSList);
                if (nLen != 0) {
                    Timestamp currentTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
                    String targetTimeStamp=null;
                    //【Step22】Get branchInfo of lot's current route
                    List<ProcessDTO.BranchInfo> branchInfoList = aProcessFlowContext.allBranchInfos();

                    //【Step23】Get returnInfo of lot's current route
                    List<ProcessDTO.ReturnInfo> returnInfoList = aProcessFlowContext.allReturnInfos();

                    //【Step24】getBranchInfoFromTopToMainRoute();
                    String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoList);

                    //【Step25】getReturnInfoFromTopToMainRoute();
                    String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoList);

                    for (int ii = 0; ii < nLen; ii++) {
                        ProcessDTO.TimeRestrictionSpecification timeRS = aTimeRSList.get(ii);
                        if (!CimStringUtils.equals(BizConstant.SP_QTIMETYPE_BYLOT, timeRS.getDefaultTimeRS().getQTimeType())) {
                            continue;
                        }

                        ObjectIdentifier oiMainPD = ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                        List<String> originalTimeList = new ArrayList<>();
                        originalTimeList.add(oiMainPD.getValue());
                        originalTimeList.add(currentOperationNumber);
                        originalTimeList.add(oiMainPD.getValue());
                        originalTimeList.add(timeRS.getDefaultTimeRS().getTargetOperationNumber());

                        //【Step26】Check if there is duplicate Q-Time timer;
                        String originalQTime = CimArrayUtils.mergeStringIntoTokens(originalTimeList,BizConstant.SP_KEY_SEPARATOR_DOT);
                        com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForLot = aLot.findQTimeRestrictionByOriginalQTime( originalQTime);
                        if (aQTimeRestrictionForLot != null) {
                            log.info("There is duplicate Q-Time timer.");
                            continue;
                        }

                        //【Step27】createQTimeRestriction;
                        com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                        Validations.check(aQTimeRestriction == null, new OmCode(retCodeConfig.getNotFoundSystemObj(),"QTimeRestriction"));

                        //【Step28】addQTimeRestriction;
                        aLot.addQTimeRestriction(aQTimeRestriction);

                        ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                        String currentTimeStampVar = currentTimeStamp.toString();
                        ObjectIdentifier waferID = new ObjectIdentifier();
                        ObjectIdentifier lotID = ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey());
                        aQTimeRestrictionInfo.setLotID(lotID);
                        ObjectIdentifier mainPD = ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                        aQTimeRestrictionInfo.setTriggerMainProcessDefinition(mainPD);
                        aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                        aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                        aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                        aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);

                        aQTimeRestrictionInfo.setTargetMainProcessDefinition(mainPD);
                        aQTimeRestrictionInfo.setTargetOperationNumber(timeRS.getDefaultTimeRS().getTargetOperationNumber());
                        aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                        aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                        aQTimeRestrictionInfo.setWatchdogRequired(true);
                        aQTimeRestrictionInfo.setActionDone(false);
                        aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                        aQTimeRestrictionInfo.setProcessDefinitionLevel(timeRS.getDefaultTimeRS().getProcessDefinitionLevel());
                        aQTimeRestrictionInfo.setManualCreated(false);
                        aQTimeRestrictionInfo.setQTimeType(timeRS.getDefaultTimeRS().getQTimeType());
                        aQTimeRestrictionInfo.setWaferID(waferID);
                        aQTimeRestrictionInfo.setPreTrigger(false);

                        int actionLength = CimArrayUtils.getSize(timeRS.getDefaultTimeRS().getActions());
                        String mostUrgentTargetTimeAction=null;

                        // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....);
                        Boolean setMostUrgentTimeFlag = false;
                        if (BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND == timeRS.getDefaultTimeRS().getExpiredTimeDuration().intValue()) {
                            setMostUrgentTimeFlag = true;
                        } else {
                            //【Step29】add duration;
                            Long duration = CimLongUtils.longValue(timeRS.getDefaultTimeRS().getExpiredTimeDuration());
                            targetTimeStamp = currentTimeStamp.toLocalDateTime().plusSeconds(duration/1000).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        }

                        if (actionLength != 0) {
                            List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                            aQTimeRestrictionInfo.setActions(actions);
                            for (int counter = 0; counter < actionLength; counter++) {
                                ProcessDTO.TimeRestrictionAction timeRSAction = timeRS.getDefaultTimeRS().getActions().get(counter);
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                                if (0 == nUseCustomFieldFlag &&
                                        CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRSAction.getAction())) {
                                    log.info("CustomField for DispatchPrecede is not used and action is DispatchPrecede");
                                    continue;
                                }

                                Long duration = CimLongUtils.longValue(timeRSAction.getExpiredTimeDuration());
                                String targetTimeStampForAction = CimDateUtils.getTimestampAsString(new Timestamp(currentTimeStamp.getTime() + duration));

                                if (setMostUrgentTimeFlag) {
                                    if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                        mostUrgentTargetTimeAction = targetTimeStampForAction;
                                    } else {
                                        if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                            mostUrgentTargetTimeAction = targetTimeStampForAction;
                                        }
                                    }
                                }

                                ProcessDTO.QTimeRestrictionAction qTimeAction = new ProcessDTO.QTimeRestrictionAction();
                                qTimeAction.setTargetTimeStamp(targetTimeStampForAction);
                                qTimeAction.setAction(timeRSAction.getAction());
                                qTimeAction.setReasonCode(timeRSAction.getReasonCode());

                                if (!CimStringUtils.isEmpty(timeRSAction.getOperationNumber())) {
                                    qTimeAction.setActionRouteID(oiMainPD);
                                }

                                qTimeAction.setOperationNumber(timeRSAction.getOperationNumber());
                                qTimeAction.setTiming(timeRSAction.getTiming());
                                qTimeAction.setMainProcessDefinition(timeRSAction.getMainProcessDefinition());
                                qTimeAction.setMessageDefinition(timeRSAction.getMessageDefinition());
                                qTimeAction.setWatchdogRequired(true);

                                // Action is DispatchPrecede
                                if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRSAction.getAction())) {
                                    log.info("Action is DispatchPrecede.");
                                    qTimeAction.setActionDone(true);
                                } else {
                                    log.info("Action is {}.", timeRSAction.getAction());
                                    qTimeAction.setActionDone(false);
                                }
                                actions.add(qTimeAction);
                            }
                        } else {
                            List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                            aQTimeRestrictionInfo.setActions(actions);
                        }

                        if (!CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                            aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                        } else {
                            aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                        }

                        //【Step30】setQTimeRestrictionInfo;
                        aQTimeRestriction.setQTimeRestrictionInfo( aQTimeRestrictionInfo);

                        //【Step31】make qtime event change;
                        Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                        qtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                        qtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                        qtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                        qtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                        qtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                        qtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                        qtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                        qtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                        qtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                        qtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                        qtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                        qtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                        qtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                        qtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                        qtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                        qtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                        qtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                        if (aQTimeRestrictionInfo.getWatchdogRequired()) {
                            qtimeInfo.setWatchDogRequired("Y");
                        } else {
                            qtimeInfo.setWatchDogRequired("N");
                        }
                        if (aQTimeRestrictionInfo.getActionDone()) {
                            qtimeInfo.setActionDoneFlag("Y");
                        } else {
                            qtimeInfo.setActionDoneFlag("N");
                        }
                        qtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                        List<ProcessDTO.QTimeRestrictionAction> qTimeActions = aQTimeRestrictionInfo.getActions();
                        actionLength = CimArrayUtils.getSize(qTimeActions);
                        List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                        qtimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
                        if (actionLength != 0) {
                            for (ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction : qTimeActions) {
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                qTimeActionInfoList.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeRestrictionAction.getTargetTimeStamp());
                                qTimeActionInfo.setQrestrictionAction(qTimeRestrictionAction.getAction());
                                qTimeActionInfo.setReasonCodeID(qTimeRestrictionAction.getReasonCode());
                                qTimeActionInfo.setActionRouteID(qTimeRestrictionAction.getActionRouteID());
                                qTimeActionInfo.setActionOperationNumber(qTimeRestrictionAction.getOperationNumber());
                                qTimeActionInfo.setFutureHoldTiming(qTimeRestrictionAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(qTimeRestrictionAction.getMainProcessDefinition());
                                qTimeActionInfo.setMessageID(qTimeRestrictionAction.getMessageDefinition());
                                qTimeActionInfo.setCustomField(qTimeRestrictionAction.getCustomField());
                                if (qTimeRestrictionAction.getWatchdogRequired()) {
                                    qTimeActionInfo.setWatchDogRequired("Y");
                                } else {
                                    qTimeActionInfo.setWatchDogRequired("N");
                                }
                                if (qTimeRestrictionAction.getActionDone()) {
                                    qTimeActionInfo.setActionDoneFlag("Y");
                                } else {
                                    qTimeActionInfo.setActionDoneFlag("N");
                                }
                            }
                        }
                        Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                        params.setLotID(lotInCassette.getLotID());
                        params.setQtimeInfo(qtimeInfo);
                        eventMethod.qTimeChangeEventMake(objCommon, params);
                    }
                    List<ProductDTO.WaferInfo> waferInfoList = aLot.getAllWaferInfo();
                    int wafersCount = CimArrayUtils.getSize(waferInfoList);
                    for (int ii = 0; ii < nLen; ii++) {
                        ProcessDTO.TimeRestrictionSpecification timeRS = aTimeRSList.get(ii);
                        if (!CimStringUtils.equals(BizConstant.SP_QTIMETYPE_BYWAFER, timeRS.getDefaultTimeRS().getQTimeType())) {
                            continue;
                        }
                        ObjectIdentifier mainProcessDefinitionID = ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                        //【Step32】Check if there is duplicate Q-Time timer;
                        String originalQTime = mainProcessDefinitionID.getValue();
                        originalQTime += BizConstant.SP_KEY_SEPARATOR_DOT + currentOperationNumber;
                        originalQTime += BizConstant.SP_KEY_SEPARATOR_DOT + mainProcessDefinitionID.getValue();
                        originalQTime += BizConstant.SP_KEY_SEPARATOR_DOT + timeRS.getDefaultTimeRS().getTargetOperationNumber();

                        Boolean allWaferFlag = true;
                        for (int k = 0; k < wafersCount; k++) {
                            ProductDTO.WaferInfo waferInfo = waferInfoList.get(k);
                            com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,waferInfo.getWaferID());

                            //【Step33】findQTimeRestrictionByOriginalQTime;
                            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForWafer = aPosWafer.findQTimeRestrictionByOriginalQTime( originalQTime);
                            if (aQTimeRestrictionForWafer == null) {
                                allWaferFlag = false;
                                break;
                            }
                        }
                        if (allWaferFlag) {
                            continue;
                        }

                        //【Step34】createQTimeRestriction;
                        com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                        Validations.check(aQTimeRestriction == null, new OmCode(retCodeConfig.getNotFoundSystemObj(),"QTimeRestriction"));
                        //【Step35】addQTimeRestriction;
                        aLot.addQTimeRestriction(aQTimeRestriction);

                        ProcessDTO.QTimeRestrictionInfo qTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                        String currentTimeStampVar = currentTimeStamp.toString();
                        ObjectIdentifier waferID = new ObjectIdentifier();
                        ObjectIdentifier lotID = ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey());
                        qTimeRestrictionInfo.setLotID(lotID);
                        qTimeRestrictionInfo.setTriggerMainProcessDefinition(mainProcessDefinitionID);
                        qTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                        qTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                        qTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                        qTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);

                        qTimeRestrictionInfo.setTargetOperationNumber(timeRS.getDefaultTimeRS().getTargetOperationNumber());
                        qTimeRestrictionInfo.setTargetMainProcessDefinition(mainProcessDefinitionID);
                        qTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                        qTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                        qTimeRestrictionInfo.setWatchdogRequired(true);
                        qTimeRestrictionInfo.setActionDone(false);
                        qTimeRestrictionInfo.setOriginalQTime(originalQTime);
                        qTimeRestrictionInfo.setProcessDefinitionLevel(timeRS.getDefaultTimeRS().getProcessDefinitionLevel());
                        qTimeRestrictionInfo.setManualCreated(false);
                        qTimeRestrictionInfo.setQTimeType(BizConstant.SP_QTIMETYPE_BYLOT);
                        qTimeRestrictionInfo.setWaferID(waferID);
                        qTimeRestrictionInfo.setPreTrigger(false);

                        int actionLen = CimArrayUtils.getSize(timeRS.getDefaultTimeRS().getActions());
                        String mostUrgentTargetTimeAction = null;

                        // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....);
                        Boolean mostUrgentTimeFlagSet = false;
                        if (BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND*1.0 ==  timeRS.getDefaultTimeRS().getExpiredTimeDuration() ) {
                            mostUrgentTimeFlagSet = true;
                        } else {
                            //【Step36】add duration;
                            Long expiredDuration = CimLongUtils.longValue(timeRS.getDefaultTimeRS().getExpiredTimeDuration());
                            targetTimeStamp = currentTimeStamp.toLocalDateTime().plusSeconds(expiredDuration/1000).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        }

                        if (actionLen > 0) {
                            log.info("qtime actions be found.");
                            List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                            qTimeRestrictionInfo.setActions(actions);
                            for (int counter = 0; counter < actionLen; counter++) {
                                ProcessDTO.TimeRestrictionAction timeRSAction = timeRS.getDefaultTimeRS().getActions().get(counter);
                                // CustomField for DispatchPrecede is not used and action is DispatchPrecede;
                                if (0 == nUseCustomFieldFlag &&
                                        CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRSAction.getAction())) {
                                    log.info("{}: CustomField for DispatchPrecede is not used and action is DispatchPrecede.", counter);
                                    continue;
                                }

                                Long duration = timeRSAction.getExpiredTimeDuration().longValue();
                                String targetTimeStampForAction = currentTimeStamp.toLocalDateTime().plusSeconds(duration/1000).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                                if (mostUrgentTimeFlagSet) {
                                    if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                        mostUrgentTargetTimeAction = targetTimeStampForAction;
                                    } else {
                                        if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                            mostUrgentTargetTimeAction = targetTimeStampForAction;
                                        }
                                    }
                                }

                                ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction = new ProcessDTO.QTimeRestrictionAction();
                                qTimeRestrictionAction.setTargetTimeStamp(targetTimeStampForAction);
                                qTimeRestrictionAction.setAction(timeRSAction.getAction());
                                qTimeRestrictionAction.setReasonCode(timeRSAction.getReasonCode());

                                if (!CimStringUtils.isEmpty(timeRSAction.getOperationNumber())) {
                                    qTimeRestrictionAction.setActionRouteID(mainProcessDefinitionID);
                                }

                                qTimeRestrictionAction.setOperationNumber(timeRSAction.getOperationNumber());
                                qTimeRestrictionAction.setTiming(timeRSAction.getTiming());
                                qTimeRestrictionAction.setMainProcessDefinition(timeRSAction.getMainProcessDefinition());
                                qTimeRestrictionAction.setMessageDefinition(timeRSAction.getMessageDefinition());
                                qTimeRestrictionAction.setWatchdogRequired(true);

                                // Action is DispatchPrecede
                                if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRSAction.getAction())) {
                                    qTimeRestrictionAction.setActionDone(true);
                                    log.info("Action is DispatchPrecede.");
                                } else {
                                    qTimeRestrictionAction.setActionDone(false);
                                    log.info("Action is not DispatchPrecede but {}.", timeRSAction.getAction());
                                }
                                actions.add(qTimeRestrictionAction);
                            }
                        } else {
                            log.info("not found qtime action.");
                            List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                            qTimeRestrictionInfo.setActions(actions);
                        }

                        if (!CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                            qTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                        } else {
                            qTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                        }

                        //【Step37】setQTimeRestrictionInfo;
//                        cimQTimeRestriction.setQTimeRestrictionInfo
                        aQTimeRestriction.setQTimeRestrictionInfo(qTimeRestrictionInfo);
                        //【Step38】make qtime event change;
                        Infos.QtimeInfo qtimeInfo = new Infos.QtimeInfo();
                        qtimeInfo.setQTimeType(qTimeRestrictionInfo.getQTimeType());
                        qtimeInfo.setWaferID(qTimeRestrictionInfo.getWaferID());
                        qtimeInfo.setPreTrigger(qTimeRestrictionInfo.getPreTrigger());
                        qtimeInfo.setOriginalQTime(qTimeRestrictionInfo.getOriginalQTime());
                        qtimeInfo.setProcessDefinitionLevel(qTimeRestrictionInfo.getProcessDefinitionLevel());
                        qtimeInfo.setRestrictionTriggerRouteID(qTimeRestrictionInfo.getTriggerMainProcessDefinition());
                        qtimeInfo.setRestrictionTriggerOperationNumber(qTimeRestrictionInfo.getTriggerOperationNumber());
                        qtimeInfo.setRestrictionTriggerBranchInfo(qTimeRestrictionInfo.getTriggerBranchInfo());
                        qtimeInfo.setRestrictionTriggerReturnInfo(qTimeRestrictionInfo.getTriggerReturnInfo());
                        qtimeInfo.setRestrictionTriggerTimeStamp(qTimeRestrictionInfo.getTriggerTimeStamp());
                        qtimeInfo.setRestrictionTargetRouteID(qTimeRestrictionInfo.getTargetMainProcessDefinition());
                        qtimeInfo.setRestrictionTargetOperationNumber(qTimeRestrictionInfo.getTargetOperationNumber());
                        qtimeInfo.setRestrictionTargetBranchInfo(qTimeRestrictionInfo.getTargetBranchInfo());
                        qtimeInfo.setRestrictionTargetReturnInfo(qTimeRestrictionInfo.getTargetReturnInfo());
                        qtimeInfo.setRestrictionTargetTimeStamp(qTimeRestrictionInfo.getTargetTimeStamp());
                        qtimeInfo.setPreviousTargetInfo(qTimeRestrictionInfo.getPreviousTargetInfo());
                        qtimeInfo.setSpecificControl(qTimeRestrictionInfo.getControl());
                        if (qTimeRestrictionInfo.getWatchdogRequired()) {
                            qtimeInfo.setWatchDogRequired("Y");
                        } else {
                            qtimeInfo.setWatchDogRequired("N");
                        }
                        if (qTimeRestrictionInfo.getActionDone()) {
                            qtimeInfo.setActionDoneFlag("Y");
                        } else {
                            qtimeInfo.setActionDoneFlag("N");
                        }
                        qtimeInfo.setManualCreated(qTimeRestrictionInfo.getManualCreated());
                        List<ProcessDTO.QTimeRestrictionAction> qTimeActions = qTimeRestrictionInfo.getActions();
                        int actionLength = CimArrayUtils.getSize(qTimeActions);
                        List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                        qtimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
                        if (actionLength != 0) {
                            for (ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction : qTimeActions) {
                                Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                                qTimeActionInfoList.add(qTimeActionInfo);
                                qTimeActionInfo.setQrestrictionTargetTimeStamp(qTimeRestrictionAction.getTargetTimeStamp());
                                qTimeActionInfo.setQrestrictionAction(qTimeRestrictionAction.getAction());
                                qTimeActionInfo.setReasonCodeID(qTimeRestrictionAction.getReasonCode());
                                qTimeActionInfo.setActionRouteID(qTimeRestrictionAction.getActionRouteID());
                                qTimeActionInfo.setActionOperationNumber(qTimeRestrictionAction.getOperationNumber());
                                qTimeActionInfo.setFutureHoldTiming(qTimeRestrictionAction.getTiming());
                                qTimeActionInfo.setReworkRouteID(qTimeRestrictionAction.getMainProcessDefinition());
                                qTimeActionInfo.setMessageID(qTimeRestrictionAction.getMessageDefinition());
                                qTimeActionInfo.setCustomField(qTimeRestrictionAction.getCustomField());
                                if (qTimeRestrictionAction.getWatchdogRequired()) {
                                    qTimeActionInfo.setWatchDogRequired("Y");
                                } else {
                                    qTimeActionInfo.setWatchDogRequired("N");
                                }
                                if (qTimeRestrictionAction.getActionDone()) {
                                    qTimeActionInfo.setActionDoneFlag("Y");
                                } else {
                                    qTimeActionInfo.setActionDoneFlag("N");
                                }
                            }
                        }
                        Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                        params.setLotID(lotInCassette.getLotID());
                        params.setQtimeInfo(qtimeInfo);
                        eventMethod.qTimeChangeEventMake(objCommon, params);
                    }
                }

                //【Step39】qTime_triggerOpe_Replace;
                if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
                    Inputs.QTimeTriggerOpeReplaceIn inParams = new Inputs.QTimeTriggerOpeReplaceIn();
                    inParams.setLotID(lotInCassette.getLotID());
                    qTimeTriggerOpeReplace(objCommon, inParams);
                }
            }
        }
        return outObjectList;
    }

    @Override
    public Outputs.ObjQtimeSetClearByOperationCompOut qtimeSetClearByOperationComp(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Outputs.ObjQtimeSetClearByOperationCompOut objQtimeSetClearByOperationCompOut = new Outputs.ObjQtimeSetClearByOperationCompOut();

        // PPT_CONVERT_LOTID_TO_LOT_OR
        CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        CimProcessOperation aProcessOperation = aLot.getProcessOperation();
        Validations.check(aProcessOperation == null, retCodeConfig.getNotFoundOperation());
        CimProcessDefinition currentRoute = aProcessOperation.getMainProcessDefinition();
        String currentOperationNumber = aProcessOperation.getOperationNumber();
        Validations.check(currentRoute == null, retCodeConfig.getNotFoundMainRoute());
        String processFlowType = currentRoute.getProcessFlowType();
        if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
            // qTime_targetOpe_Replace
            Infos.QTimeTargetOpeReplaceIn inputParams = new Infos.QTimeTargetOpeReplaceIn();
            inputParams.setLotID(lotID);
            inputParams.setSpecificControlFlag(true);
            this.qTimeTargetOpeReplace(objCommon, inputParams);
        }
        // lot_qtimeInfo_GetForClear
        Outputs.ObjLotQtimeInfoGetForClearOut strLotQtimeInfoGetForClearOut = this.lotQtimeInfoGetForClear(objCommon, lotID, false);
        objQtimeSetClearByOperationCompOut.setStrLotHoldReleaseList(strLotQtimeInfoGetForClearOut.getStrLotHoldReleaseList());
        objQtimeSetClearByOperationCompOut.setStrFutureHoldCancelList(strLotQtimeInfoGetForClearOut.getStrFutureHoldCancelList());
        objQtimeSetClearByOperationCompOut.setStrFutureReworkCancelList(strLotQtimeInfoGetForClearOut.getStrFutureReworkCancelList());
        String qtimeDispatchPrecedeUseCustomFieldFlag = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getValue();
        List<String> qTimeClearList = strLotQtimeInfoGetForClearOut.getQTimeClearList();
        if (CimArrayUtils.isNotEmpty(qTimeClearList)) {
            for (String qTimeClear : qTimeClearList) {
                CimQTimeRestriction aQTimeRestriction = baseCoreFactory.getBO(CimQTimeRestriction.class,qTimeClear);
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTimeRestriction.getQTimeRestrictionInfo();
                if (null == aQTimeRestrictionInfo || CimStringUtils.isEmpty(aQTimeRestrictionInfo.getTriggerOperationNumber())) {
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }
                if (ObjectIdentifier.isEmpty(aQTimeRestrictionInfo.getWaferID())) {
                    aLot.removeQTimeRestriction(aQTimeRestriction);
                } else {
                    com.fa.cim.newcore.bo.product.CimWafer aPosWafer;
                    aPosWafer=baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,aQTimeRestrictionInfo.getWaferID());
                    aPosWafer.removeQTimeRestriction(aQTimeRestriction);
                }
                qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestriction);
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                if (CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (CimBooleanUtils.isFalse(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    strQtimeInfo.setWatchDogRequired("N");
                }
                if (CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone())) {
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (CimBooleanUtils.isFalse(aQTimeRestrictionInfo.getActionDone())) {
                    strQtimeInfo.setActionDoneFlag("N");
                }

                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                int actionLength = aQTimeRestrictionInfo.getActions().size();
                if (actionLength != 0) {
                    List<ProcessDTO.QTimeRestrictionAction> actions = aQTimeRestrictionInfo.getActions();
                    List<Infos.QTimeActionInfo> tmpList = new ArrayList<>();
                    for (ProcessDTO.QTimeRestrictionAction action : actions) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(action.getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(action.getAction());
                        qTimeActionInfo.setReasonCodeID(action.getReasonCode());
                        qTimeActionInfo.setActionRouteID(action.getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(action.getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(action.getTiming());
                        qTimeActionInfo.setReworkRouteID(action.getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(action.getMessageDefinition());
                        qTimeActionInfo.setCustomField(action.getCustomField());
                        if (CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                            qTimeActionInfo.setWatchDogRequired("Y");
                        } else if (CimBooleanUtils.isFalse(action.getWatchdogRequired())) {
                            qTimeActionInfo.setWatchDogRequired("N");
                        }
                        if (CimBooleanUtils.isTrue(action.getActionDone())) {
                            qTimeActionInfo.setActionDoneFlag("Y");
                        } else if (CimBooleanUtils.isFalse(action.getActionDone())) {
                            qTimeActionInfo.setActionDoneFlag("N");
                        }
                        tmpList.add(qTimeActionInfo);
                    }
                    strQtimeInfo.setStrQtimeActionInfoList(tmpList);
                } else {
                    List<Infos.QTimeActionInfo> tmpList = new ArrayList<Infos.QTimeActionInfo>();
                    strQtimeInfo.setStrQtimeActionInfoList(tmpList);
                }
                // qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
                params.setLotID(lotID);
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, params);
            }
        }
        List<CimQTimeRestriction> qtimeSeq;
        int qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();
        if (qtimeSeqForWaferCount > 0) {
            qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            qtimeSeq = aLot.allQTimeRestrictions();
        }

        if (CimArrayUtils.isNotEmpty(qtimeSeq)) {
            for (CimQTimeRestriction aQTimeTmp : qtimeSeq) {
                Validations.check(aQTimeTmp == null, retCodeConfig.getNotFoundQtime());
                if (aQTimeTmp.isManualCreated()) {
                    aQTimeTmp.makeNotManualCreated();
                }
                if (aQTimeTmp.isPreTrigger()) {
                    aQTimeTmp.makeNotPreTrigger();
                }
            }
        }
        //Get target lot's process flow context
        CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        //------------- Create and Add Qtime-Restriction ------------------
        CimProductSpecification aProductSpecification = aLot.getProductSpecification();
        List<ProcessDTO.TimeRestrictionSpecification> aTimeRSSequence = aProcessOperation.findTimeRestrictionsForProduct(aProductSpecification);
        int nLen = aTimeRSSequence.size();
        if (nLen != 0) {
            Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
            String targetTimeStamp = null;
            List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();
            List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();
            String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute( branchInfoSeq );
            String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute( returnInfoSeq );
            for (ProcessDTO.TimeRestrictionSpecification timeRestrictionSpecification : aTimeRSSequence) {
                if (!CimStringUtils.equals(BizConstant.SP_QTIMETYPE_BYLOT, timeRestrictionSpecification.getDefaultTimeRS().getQTimeType())) {
                    continue;
                }
                ObjectIdentifier oiMainPD = ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                List<String> stsOrgQTime = new ArrayList<>();
                stsOrgQTime.add(oiMainPD.getValue());
                stsOrgQTime.add(currentOperationNumber);
                stsOrgQTime.add(oiMainPD.getValue());
                stsOrgQTime.add(timeRestrictionSpecification.getDefaultTimeRS().getTargetOperationNumber());
                String originalQTime = CimArrayUtils.mergeStringIntoTokens(stsOrgQTime, BizConstant.SP_KEY_SEPARATOR_DOT);
                //Check if there is duplicate Q-Time timer
                CimQTimeRestriction aQTimeRestrictionForLot = aLot.findQTimeRestrictionByOriginalQTime(originalQTime);
                if (aQTimeRestrictionForLot != null) {
                    continue;
                }
                CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                aLot.addQTimeRestriction(aQTimeRestriction);
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                String currentTimeStampVar = currentTimeStamp.toString();
                ObjectIdentifier waferID = null;
                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setQTimeType(timeRestrictionSpecification.getDefaultTimeRS().getQTimeType());
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setPreTrigger(false);
                aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setTargetOperationNumber(timeRestrictionSpecification.getDefaultTimeRS().getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setWatchdogRequired(true);
                aQTimeRestrictionInfo.setActionDone(false);
                aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                aQTimeRestrictionInfo.setProcessDefinitionLevel(timeRestrictionSpecification.getDefaultTimeRS().getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setManualCreated(false);
                int actionLength = timeRestrictionSpecification.getDefaultTimeRS().getActions().size();
                String mostUrgentTargetTimeAction = null;
                boolean setMostUrgentTimeFlag = false;
                if (BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND.longValue() == timeRestrictionSpecification.getDefaultTimeRS().getExpiredTimeDuration()) {
                    setMostUrgentTimeFlag = true;
                } else {
                    targetTimeStamp = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + timeRestrictionSpecification.getDefaultTimeRS().getExpiredTimeDuration())).toString();
                }
                if (actionLength != 0) {
                    int actionCnt = 0;
                    List<ProcessDTO.QTimeRestrictionAction> actionsTemp = new ArrayList<>();
                    for (int counter = 0; counter < actionLength; counter++) {
                        if (CimStringUtils.equals(qtimeDispatchPrecedeUseCustomFieldFlag, "0") &&
                                CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getAction())) {
                            continue;
                        }
                        String targetTimeStampForAction = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getExpiredTimeDuration())).toString();
                        if (setMostUrgentTimeFlag) {
                            if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    // Nothing to do.
                                }
                            }
                        }
                        ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction = new ProcessDTO.QTimeRestrictionAction();
                        qTimeRestrictionAction.setTargetTimeStamp(targetTimeStampForAction);
                        qTimeRestrictionAction.setAction(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getAction());
                        qTimeRestrictionAction.setReasonCode(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getReasonCode());
                        if (!CimStringUtils.isEmpty(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getOperationNumber())) {
                            qTimeRestrictionAction.setActionRouteID(oiMainPD);
                        }
                        qTimeRestrictionAction.setOperationNumber(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getOperationNumber());
                        qTimeRestrictionAction.setTiming(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getTiming());
                        qTimeRestrictionAction.setMainProcessDefinition(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getMainProcessDefinition());
                        qTimeRestrictionAction.setMessageDefinition(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getMessageDefinition());
                        qTimeRestrictionAction.setCustomField(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getCustomField());
                        qTimeRestrictionAction.setWatchdogRequired(true);
                        qTimeRestrictionAction.setActionDone(CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRestrictionSpecification.getDefaultTimeRS().getActions().get(counter).getAction()));
                        actionsTemp.add(qTimeRestrictionAction);
                        actionCnt++;
                    }
                    aQTimeRestrictionInfo.setActions(actionsTemp);
                }
                if (!CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                }
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                if (aQTimeRestrictionInfo.getWatchdogRequired()) {
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (!aQTimeRestrictionInfo.getWatchdogRequired()) {
                    strQtimeInfo.setWatchDogRequired("N");
                }
                if (aQTimeRestrictionInfo.getActionDone()) {
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (!aQTimeRestrictionInfo.getActionDone()) {
                    strQtimeInfo.setActionDoneFlag("N");
                }
                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                actionLength = aQTimeRestrictionInfo.getActions().size();
                if (actionLength != 0) {
                    List<ProcessDTO.QTimeRestrictionAction> actions = aQTimeRestrictionInfo.getActions();
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    if (CimArrayUtils.isNotEmpty(actions)) {
                        for (ProcessDTO.QTimeRestrictionAction action : actions) {
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(action.getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(action.getAction());
                            qTimeActionInfo.setReasonCodeID(action.getReasonCode());
                            qTimeActionInfo.setActionRouteID(action.getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(action.getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(action.getTiming());
                            qTimeActionInfo.setReworkRouteID(action.getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(action.getMessageDefinition());
                            qTimeActionInfo.setCustomField(action.getCustomField());
                            if (CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                                strQtimeInfo.setWatchDogRequired("Y");
                            } else if (!CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                                strQtimeInfo.setWatchDogRequired("N");
                            }

                            if (CimBooleanUtils.isTrue(action.getActionDone())) {
                                strQtimeInfo.setActionDoneFlag("Y");
                            } else if (!CimBooleanUtils.isTrue(action.getActionDone())) {
                                strQtimeInfo.setActionDoneFlag("N");
                            }
                            strQtimeActionInfoList.add(qTimeActionInfo);
                        }
                    }
                    strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                // qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                params.setLotID(lotID);
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, params);
            }
            List<ProductDTO.WaferInfo> waferInfoSeq = aLot.getAllWaferInfo();
            for (ProcessDTO.TimeRestrictionSpecification timeRestrictionSpecification : aTimeRSSequence) {
                if (!CimStringUtils.equals(BizConstant.SP_QTIMETYPE_BYWAFER, timeRestrictionSpecification.getDefaultTimeRS().getQTimeType())) {
                    continue;
                }
                ObjectIdentifier oiMainPD = ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                List<String> stsOrgQTime = new ArrayList<>();
                stsOrgQTime.add(oiMainPD.getValue());
                stsOrgQTime.add(currentOperationNumber);
                stsOrgQTime.add(oiMainPD.getValue());
                stsOrgQTime.add(timeRestrictionSpecification.getDefaultTimeRS().getTargetOperationNumber());
                String originalQTime = CimArrayUtils.mergeStringIntoTokens(stsOrgQTime, BizConstant.SP_KEY_SEPARATOR_DOT);
                boolean allWaferFlag = true;
                for (ProductDTO.WaferInfo waferInfo : waferInfoSeq) {
                    CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                    CimQTimeRestriction aQTimeRestrictionForWafer = aPosWafer.findQTimeRestrictionByOriginalQTime(originalQTime);
                    if (aQTimeRestrictionForWafer == null) {
                        allWaferFlag = false;
                        break;
                    }
                }
                if (allWaferFlag) {
                    continue;
                }
                CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                aLot.addQTimeRestriction(aQTimeRestriction);
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                // waferID is set to null. It may be wrong. [comment by Zack]
                ObjectIdentifier waferID = null;
                String currentTimeStampVar = currentTimeStamp.toString();
                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(ObjectIdentifier.build(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setTargetOperationNumber(timeRestrictionSpecification.getDefaultTimeRS().getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setWatchdogRequired(true);
                aQTimeRestrictionInfo.setActionDone(false);
                aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                aQTimeRestrictionInfo.setProcessDefinitionLevel(timeRestrictionSpecification.getDefaultTimeRS().getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setManualCreated(false);
                aQTimeRestrictionInfo.setQTimeType(BizConstant.SP_QTIMETYPE_BYLOT);
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setPreTrigger(false);
                int actionLength = CimArrayUtils.getSize(timeRestrictionSpecification.getDefaultTimeRS().getActions());
                String mostUrgentTargetTimeAction = null;
                boolean setMostUrgentTimeFlag = false;
                if (BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND.longValue() == timeRestrictionSpecification.getDefaultTimeRS().getExpiredTimeDuration()) {
                    setMostUrgentTimeFlag = true;
                } else {
                    targetTimeStamp = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + timeRestrictionSpecification.getDefaultTimeRS().getExpiredTimeDuration())).toString();
                }
                if (actionLength != 0) {
                    List<ProcessDTO.QTimeRestrictionAction> qTimeRestrictionActionList = new ArrayList<>();
                    for (int j = 0; j < actionLength; j++) {
                        if (CimStringUtils.equals(qtimeDispatchPrecedeUseCustomFieldFlag, "0") &&
                                CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getAction())) {
                            continue;
                        }
                        String targetTimeStampForAction = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getExpiredTimeDuration())).toString();
                        if (setMostUrgentTimeFlag) {
                            if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    // Nothing to do.
                                }
                            }
                        }
                        ProcessDTO.QTimeRestrictionAction qTimeRestrictionAction = new ProcessDTO.QTimeRestrictionAction();
                        qTimeRestrictionAction.setTargetTimeStamp(targetTimeStampForAction);
                        qTimeRestrictionAction.setAction(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getAction());
                        qTimeRestrictionAction.setReasonCode(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getReasonCode());
                        qTimeRestrictionAction.setOperationNumber(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getOperationNumber());
                        qTimeRestrictionAction.setTiming(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getTiming());
                        qTimeRestrictionAction.setMainProcessDefinition(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getMainProcessDefinition());
                        qTimeRestrictionAction.setMessageDefinition(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getMessageDefinition());
                        qTimeRestrictionAction.setCustomField(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getCustomField());
                        qTimeRestrictionAction.setWatchdogRequired(true);
                        if (!CimStringUtils.isEmpty(timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getOperationNumber())) {
                            qTimeRestrictionAction.setActionRouteID(oiMainPD);
                        }
                        // Action is DispatchPrecede
                        qTimeRestrictionAction.setActionDone(CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, timeRestrictionSpecification.getDefaultTimeRS().getActions().get(j).getAction()));
                        qTimeRestrictionActionList.add(qTimeRestrictionAction);
                    }
                    aQTimeRestrictionInfo.setActions(qTimeRestrictionActionList);
                }
                if (!CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                }
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);
                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                Infos.QtimeInfo strQtimeInfoTemp = new Infos.QtimeInfo();
                strQtimeInfoTemp.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfoTemp.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfoTemp.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfoTemp.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfoTemp.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfoTemp.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfoTemp.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfoTemp.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfoTemp.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfoTemp.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfoTemp.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfoTemp.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfoTemp.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfoTemp.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfoTemp.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfoTemp.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfoTemp.setSpecificControl(aQTimeRestrictionInfo.getControl());
                if (aQTimeRestrictionInfo.getWatchdogRequired()) {
                    strQtimeInfoTemp.setWatchDogRequired("Y");
                } else if (!aQTimeRestrictionInfo.getWatchdogRequired()) {
                    strQtimeInfoTemp.setWatchDogRequired("N");
                }
                if (aQTimeRestrictionInfo.getActionDone()) {
                    strQtimeInfoTemp.setActionDoneFlag("Y");
                } else if (!aQTimeRestrictionInfo.getActionDone()) {
                    strQtimeInfoTemp.setActionDoneFlag("N");
                }
                strQtimeInfoTemp.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                if (actionLength != 0) {
                    List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                    for (int j = 0; j < actionLength; j++) {
                        Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                        qTimeActionInfo.setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(j).getTargetTimeStamp());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(j).getTargetTimeStamp());
                        qTimeActionInfo.setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(j).getAction());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(j).getAction());
                        qTimeActionInfo.setReasonCodeID(aQTimeRestrictionInfo.getActions().get(j).getReasonCode());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setReasonCodeID(aQTimeRestrictionInfo.getActions().get(j).getReasonCode());
                        qTimeActionInfo.setActionRouteID(aQTimeRestrictionInfo.getActions().get(j).getActionRouteID());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setActionRouteID(aQTimeRestrictionInfo.getActions().get(j).getActionRouteID());
                        qTimeActionInfo.setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(j).getOperationNumber());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(j).getOperationNumber());
                        qTimeActionInfo.setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(j).getTiming());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(j).getTiming());
                        qTimeActionInfo.setReworkRouteID(aQTimeRestrictionInfo.getActions().get(j).getMainProcessDefinition());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setReworkRouteID(aQTimeRestrictionInfo.getActions().get(j).getMainProcessDefinition());
                        qTimeActionInfo.setMessageID(aQTimeRestrictionInfo.getActions().get(j).getMessageDefinition());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setMessageID(aQTimeRestrictionInfo.getActions().get(j).getMessageDefinition());
                        qTimeActionInfo.setCustomField(aQTimeRestrictionInfo.getActions().get(j).getCustomField());//    strQtimeInfoTemp.getStrQtimeActionInfoList().get(j).setCustomField(aQTimeRestrictionInfo.getActions().get(j).getCustomField());
                        if (aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired()) {
                            qTimeActionInfo.setWatchDogRequired("Y");
                        } else if (!aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired()) {
                            qTimeActionInfo.setWatchDogRequired("N");
                        }
                        if (aQTimeRestrictionInfo.getActions().get(j).getActionDone()) {
                            qTimeActionInfo.setActionDoneFlag("Y");
                        } else if (!aQTimeRestrictionInfo.getActions().get(j).getActionDone()) {
                            qTimeActionInfo.setActionDoneFlag("N");
                        }
                        strQtimeActionInfoList.add(qTimeActionInfo);
                    }
                    strQtimeInfoTemp.setStrQtimeActionInfoList(strQtimeActionInfoList);
                }
                // qTimeChangeEvent_Make__180
                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                params.setLotID(lotID);
                params.setQtimeInfo(strQtimeInfoTemp);
                params.setClaimMemo("");
                eventMethod.qTimeChangeEventMake(objCommon, params);
            }
        }
        if (CimStringUtils.equals(BizConstant.SP_FLOWTYPE_SUB, processFlowType)) {
            Inputs.QTimeTriggerOpeReplaceIn inParams = new Inputs.QTimeTriggerOpeReplaceIn();
            inParams.setLotID(lotID);
            qTimeTriggerOpeReplace(objCommon, inParams);
        }
        return objQtimeSetClearByOperationCompOut;
    }

    @Override
    public Outputs.QtimeClearByOpeStartCancelOut qtimeClearByOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Outputs.QtimeClearByOpeStartCancelOut out = new Outputs.QtimeClearByOpeStartCancelOut();
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        //===== Get the target lot's current operation =======
        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = aLot.getProcessOperation();
        Validations.check(aProcessOperation == null, retCodeConfig.getNotFoundPoForLot());
        String currentOperationNumber = aProcessOperation.getOperationNumber();
        //===== Get the target lot's process flow context =======
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());
        //===== Get the target lot's route ID =======
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aCurrentRoute = aProcessOperation.getMainProcessDefinition();
        Validations.check(aCurrentRoute == null, retCodeConfig.getNotFoundMainRoute());
        String currentRouteID = aCurrentRoute.getIdentifier();
        //===== Get branchInfo of lot's current route =======
        List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();
        String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute( branchInfoSeq );
        String qTimeWatchDogPerson = StandardProperties.OM_QT_SENTINEL_USER_ID.getValue();
        if (CimStringUtils.isEmpty(qTimeWatchDogPerson)) {
            qTimeWatchDogPerson = BizConstant.SP_QTIME_WATCH_DOG_PERSON;
        }
        String keepQtimeActionFlag = StandardProperties.OM_QT_ACTION_KEEP_ON_CLEAR.getValue();
        int qTimeLen = 0;
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeList = null;
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeListVar;
        if (CimStringUtils.equals(objCommon.getTransactionID(), "OEQPW012") || CimStringUtils.equals(objCommon.getTransactionID(),
                "OEQPW024")) {
            log.info("PartialOpeComp : TxID == {}", objCommon.getTransactionID());
            aQTimeList = aLot.allQTimeRestrictionsWithWaferLevelQTime();
            aQTimeListVar = aQTimeList;
            qTimeLen = aQTimeList.size();
        } else {
            int aQTimeListForWaferCount = aLot.getWaferLevelQTimeCount();
            if (aQTimeListForWaferCount > 0) {
                aQTimeList = aLot.allQTimeRestrictionsWithWaferLevelQTime();
                aQTimeListVar = aQTimeList;
                qTimeLen = aQTimeList.size();
            } else {
                log.debug("Not exist WaferLevelQTime");
            }
        }
        Map<String, Infos.LotHoldReq> tmpFutureHoldCancelList = new HashMap<>();
        int futureReworkCancelCnt = 0;
        int futureReworkCancelLen = qTimeLen;
        List<Infos.FutureReworkInfo> strFutureReworkCancelList = new ArrayList<>();
        List<Long> detailCntList = new ArrayList<>();
        for (int qTimeCnt = 0; qTimeCnt < qTimeLen; qTimeCnt++) {
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = aQTimeList.get(qTimeCnt);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());
            ProcessDTO.QTimeRestrictionInfo aQTimeInfo = aQTime.getQTimeRestrictionInfo();
            if (aQTimeInfo == null || CimStringUtils.isEmpty(aQTimeInfo.getTriggerOperationNumber())) {
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            }
            if (!CimStringUtils.equals(currentRouteID, aQTimeInfo.getTriggerMainProcessDefinition().getValue())
                    || !CimStringUtils.equals(currentOperationNumber, aQTimeInfo.getTriggerOperationNumber())
                    || !CimStringUtils.equals(branchInfo, aQTimeInfo.getTriggerBranchInfo())) {
                continue;
            }
            ObjectIdentifier actionRouteID = aQTimeInfo.getTriggerMainProcessDefinition();
            String originalTriggerOperationNumber = aQTimeInfo.getTriggerOperationNumber();
            String originalTargetOperationNumber = aQTimeInfo.getTargetOperationNumber();
            if (!CimStringUtils.isEmpty(aQTimeInfo.getOriginalQTime())) {
                //----------------------------------
                //  Get original Q-Time information
                //----------------------------------
                // qtime_originalInformation_Get
                Outputs.ObjQtimeOriginalInformationGetOut qtimeOriginalInformationOut = this.qtimeOriginalInformationGet(objCommon, aQTimeInfo.getOriginalQTime());
                //----------------------------------
                //  Set action route ID
                //----------------------------------
                if (!CimStringUtils.isEmpty(qtimeOriginalInformationOut.getTriggerRouteID().getValue())) {
                    actionRouteID = qtimeOriginalInformationOut.getTriggerRouteID();
                    originalTriggerOperationNumber = qtimeOriginalInformationOut.getTriggerOperationNumber();
                    originalTargetOperationNumber = qtimeOriginalInformationOut.getTargetOperationNumber();
                }
            }
            StringBuffer qTimeKey = new StringBuffer();
            qTimeKey.append(aQTimeInfo.getLotID().getValue());
            qTimeKey.append(BizConstant.SP_KEY_SEPARATOR_DOT);
            qTimeKey.append(actionRouteID.getValue());
            qTimeKey.append(BizConstant.SP_KEY_SEPARATOR_DOT);
            qTimeKey.append(originalTriggerOperationNumber);
            qTimeKey.append(BizConstant.SP_KEY_SEPARATOR_DOT);
            qTimeKey.append(originalTargetOperationNumber);
            ObjectIdentifier waferID = aQTimeInfo.getWaferID();
            if (!CimStringUtils.equals(keepQtimeActionFlag, "1")) {
                int actionLen = aQTimeInfo.getActions().size();
                for (int actionCnt = 0; actionCnt < actionLen; actionCnt++) {
                    if (CimBooleanUtils.isTrue(aQTimeInfo.getActions().get(actionCnt).getActionDone())) {
                        String reasonCode = aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue();
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD, aQTimeInfo.getActions().get(actionCnt).getAction())) {
                            if (CimStringUtils.isEmpty(aQTimeInfo.getActions().get(actionCnt).getOperationNumber())) {
                                continue;
                            }
                            ObjectIdentifier replaceActionRouteID;
                            if (CimStringUtils.isEmpty(aQTimeInfo.getActions().get(actionCnt).getActionRouteID().getValue())) {
                                replaceActionRouteID = aQTimeInfo.getActions().get(actionCnt).getActionRouteID();
                            } else {
                                replaceActionRouteID = actionRouteID;
                            }
                            StringBuilder futureHoldKey = new StringBuilder();
                            futureHoldKey.append(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD)
                                    .append(BizConstant.SP_KEY_SEPARATOR_DOT)
                                    .append(aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue())
                                    .append(BizConstant.SP_KEY_SEPARATOR_DOT)
                                    .append(qTimeWatchDogPerson)
                                    .append(BizConstant.SP_KEY_SEPARATOR_DOT)
                                    .append(replaceActionRouteID.getValue())
                                    .append(BizConstant.SP_KEY_SEPARATOR_DOT)
                                    .append(aQTimeInfo.getActions().get(actionCnt).getOperationNumber())
                                    .append(BizConstant.SP_KEY_SEPARATOR_DOT);
                            Infos.LotHoldReq strFutureHold = new Infos.LotHoldReq();
                            strFutureHold.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                            strFutureHold.setHoldReasonCodeID(aQTimeInfo.getActions().get(actionCnt).getReasonCode());
                            strFutureHold.setHoldUserID(ObjectIdentifier.build(qTimeWatchDogPerson, ""));
                            strFutureHold.setRouteID(replaceActionRouteID);
                            strFutureHold.setOperationNumber(aQTimeInfo.getActions().get(actionCnt).getOperationNumber());
                            strFutureHold.setRelatedLotID(ObjectIdentifier.build("", ""));
                            String futureHoldCancelKey = futureHoldKey.toString();
                            if (!tmpFutureHoldCancelList.containsKey(futureHoldCancelKey)) {
                                log.debug("A future hold action was added to the remain list.");
                                tmpFutureHoldCancelList.put(futureHoldCancelKey, strFutureHold);
                            } else {
                                log.debug("A future hold action was not added to the remain list.\"");
                                String futureHoldCancelKeyVar = futureHoldCancelKey;
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, aQTimeInfo.getActions().get(actionCnt).getAction())) {
                            //Cancel future rework
                            ObjectIdentifier replaceActionRouteID;
                            if (!CimStringUtils.isEmpty(aQTimeInfo.getActions().get(actionCnt).getActionRouteID().getValue())) {
                                //actionRouteID is replace RouteID
                                replaceActionRouteID = aQTimeInfo.getActions().get(actionCnt).getActionRouteID();
                            } else {
                                replaceActionRouteID = actionRouteID;
                            }
                            //===== Get a future rework request =======
                            Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams = new Params.FutureActionDetailInfoInqParams();
                            futureActionDetailInfoInqParams.setLotID(aQTimeInfo.getLotID());
                            Infos.OperationFutureActionAttributes attributes = new Infos.OperationFutureActionAttributes();
                            attributes.setRouteID(replaceActionRouteID);
                            attributes.setOperationNumber(aQTimeInfo.getActions().get(actionCnt).getOperationNumber());
                            futureActionDetailInfoInqParams.setOperationFutureActionAttributes(attributes);
                            Outputs.lotFutureReworkListGetDROut futureReworkListGetDROut = lotMethod.lotFutureReworkListGetDR(objCommon, futureActionDetailInfoInqParams);
                            if(futureReworkListGetDROut.getFutureReworkDetailInfoList().size()<1){
                                continue;
                            }else if(futureReworkListGetDROut.getFutureReworkDetailInfoList().size()>1){
                                throw new ServiceException(retCodeConfig.getInvalidInputParam());
                            } else {
                                log.info("A future rework request was found[{}].", aQTimeInfo.getActions().get(actionCnt).getOperationNumber());
                            }
                            //===== Check a future hold request =======
                            Infos.FutureReworkInfo strFutureRework = futureReworkListGetDROut.getFutureReworkDetailInfoList().get(0);
                            StringBuilder futureReworkTrigger = new StringBuilder();
                            futureReworkTrigger.append(qTimeKey).append(BizConstant.SP_KEY_SEPARATOR_DOT)
                                    .append(aQTimeInfo.getActions().get(actionCnt).getTargetTimeStamp());
                            int detailLen = strFutureRework.getFutureReworkDetailInfoList().size();
                            for (int detailCnt = 0; detailCnt < detailLen; detailCnt++) {
                                if (CimStringUtils.equals(futureReworkTrigger.toString(), strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getTrigger())
                                        && CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getMainProcessDefinition().getValue(),
                                        strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getReworkRouteID().getValue())
                                        && CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue(), strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getReasonCodeID().getValue())) {
                                    int ftrwkCnt = 0;
                                    for (ftrwkCnt = 0; ftrwkCnt < futureReworkCancelCnt; ftrwkCnt++) {
                                        if (CimStringUtils.equals(strFutureRework.getLotID().getValue(), strFutureReworkCancelList.get(ftrwkCnt).getLotID().getValue())
                                                && CimStringUtils.equals(strFutureRework.getRouteID().getValue(), strFutureReworkCancelList.get(ftrwkCnt).getRouteID().getValue())
                                                && CimStringUtils.equals(strFutureRework.getOperationNumber(), strFutureReworkCancelList.get(ftrwkCnt).getOperationNumber())) {
                                            log.info("LotID, RouteID and OperationNumber are matched.");
                                            break;
                                        }
                                    }
                                    if (ftrwkCnt < futureReworkCancelCnt) {
                                        int temp = detailCntList.get(ftrwkCnt).intValue();
                                        strFutureReworkCancelList.get(ftrwkCnt).getFutureReworkDetailInfoList().set(temp++, strFutureRework.getFutureReworkDetailInfoList().get(detailCnt));
                                    } else {
                                        if (futureReworkCancelLen <= futureReworkCancelCnt) {
                                            futureReworkCancelLen += actionLen;
                                        }
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setLotID(strFutureRework.getLotID());
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setRouteID(strFutureRework.getRouteID());
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setOperationNumber(strFutureRework.getOperationNumber());
                                        detailCntList.set(futureReworkCancelCnt, 0L);
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).getFutureReworkDetailInfoList().set(detailCntList.get(futureReworkCancelCnt).intValue(), strFutureRework.getFutureReworkDetailInfoList().get(detailCnt));
                                        Long aLong = detailCntList.get(futureReworkCancelCnt);
                                        detailCntList.set(futureReworkCancelCnt, aLong++);
                                        futureReworkCancelCnt++;
                                    }
                                    break;
                                }
                            }
                        } else {
                            log.debug("An action not to reset.");
                        }
                    }
                }
            }

            com.fa.cim.newcore.bo.product.CimWafer aWafer =baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,waferID);
            aWafer.removeQTimeRestriction(aQTime);
            qtimeRestrictionManager.removeQTimeRestriction(aQTime);
            Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
            strQtimeInfo.setOriginalQTime(aQTimeInfo.getOriginalQTime());
            strQtimeInfo.setProcessDefinitionLevel(aQTimeInfo.getProcessDefinitionLevel());
            strQtimeInfo.setRestrictionTriggerRouteID(aQTimeInfo.getTriggerMainProcessDefinition());
            strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeInfo.getTriggerOperationNumber());
            strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeInfo.getTriggerBranchInfo());
            strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeInfo.getTargetReturnInfo());
            strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeInfo.getTriggerTimeStamp());
            strQtimeInfo.setRestrictionTargetRouteID(aQTimeInfo.getTargetMainProcessDefinition());
            strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeInfo.getTargetOperationNumber());
            strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeInfo.getTargetBranchInfo());
            strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeInfo.getTargetReturnInfo());
            strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeInfo.getTargetTimeStamp());
            strQtimeInfo.setPreviousTargetInfo(aQTimeInfo.getPreviousTargetInfo());
            strQtimeInfo.setSpecificControl(aQTimeInfo.getControl());
            strQtimeInfo.setWaferID(aQTimeInfo.getWaferID());
            strQtimeInfo.setQTimeType(aQTimeInfo.getQTimeType());
            strQtimeInfo.setPreTrigger(aQTimeInfo.getPreTrigger());
            if (CimBooleanUtils.isTrue(aQTimeInfo.getWatchdogRequired())) {
                strQtimeInfo.setWatchDogRequired("Y");
            } else if (CimBooleanUtils.isFalse(aQTimeInfo.getWatchdogRequired())) {
                strQtimeInfo.setWatchDogRequired("N");
            }
            if (CimBooleanUtils.isTrue(aQTimeInfo.getActionDone())) {
                strQtimeInfo.setActionDoneFlag("Y");
            } else if (CimBooleanUtils.isFalse(aQTimeInfo.getActionDone())) {
                strQtimeInfo.setActionDoneFlag("Y");
            }
            strQtimeInfo.setManualCreated(aQTimeInfo.getManualCreated());
            int actionLength = aQTimeInfo.getActions().size();
            if (actionLength != 0) {
                List<ProcessDTO.QTimeRestrictionAction> actions = aQTimeInfo.getActions();
                List<Infos.QTimeActionInfo> qTimeActionInfoList = new ArrayList<>();
                for (ProcessDTO.QTimeRestrictionAction action : actions) {
                    Infos.QTimeActionInfo strQtimeActionInfo = new Infos.QTimeActionInfo();
                    strQtimeActionInfo.setQrestrictionTargetTimeStamp(action.getTargetTimeStamp());
                    strQtimeActionInfo.setQrestrictionAction(action.getAction());
                    strQtimeActionInfo.setReasonCodeID(action.getReasonCode());
                    strQtimeActionInfo.setActionRouteID(action.getActionRouteID());
                    strQtimeActionInfo.setActionOperationNumber(action.getOperationNumber());
                    strQtimeActionInfo.setFutureHoldTiming(action.getTiming());
                    strQtimeActionInfo.setReworkRouteID(action.getMainProcessDefinition());
                    strQtimeActionInfo.setMessageID(action.getMessageDefinition());
                    strQtimeActionInfo.setCustomField(action.getCustomField());
                    if (CimBooleanUtils.isTrue(action.getWatchdogRequired())) {
                        strQtimeActionInfo.setWatchDogRequired("Y");
                    } else if (CimBooleanUtils.isFalse(action.getWatchdogRequired())) {
                        strQtimeActionInfo.setWatchDogRequired("N");
                    }
                    if (CimBooleanUtils.isTrue(action.getActionDone())) {
                        strQtimeActionInfo.setActionDoneFlag("Y'");
                    } else if (CimBooleanUtils.isFalse(action.getActionDone())) {
                        strQtimeActionInfo.setActionDoneFlag("N");
                    }
                    qTimeActionInfoList.add(strQtimeActionInfo);
                }
                strQtimeInfo.setStrQtimeActionInfoList(qTimeActionInfoList);
            } else {
                log.debug("actionLength = 0");
            }
            // qTimeChangeEvent_Make__180
            Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
            params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE);
            params.setLotID(lotID);
            params.setQtimeInfo(strQtimeInfo);
            params.setClaimMemo("");
            eventMethod.qTimeChangeEventMake(objCommon, params);
        }
        if (!CimStringUtils.equals(keepQtimeActionFlag, "1")) {
            String getKey = null;
            Infos.LotHoldReq strGetFutureHold = new Infos.LotHoldReq();

            Iterator<Map.Entry<String, Infos.LotHoldReq>> it = tmpFutureHoldCancelList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Infos.LotHoldReq> entry = it.next();
                strGetFutureHold = entry.getValue();
                ProductDTO.FutureHoldRecord strSearchFutureHold = new ProductDTO.FutureHoldRecord();
                strSearchFutureHold.setHoldType               (strGetFutureHold.getHoldType());
                strSearchFutureHold.setReasonCode             (strGetFutureHold.getHoldReasonCodeID());
                strSearchFutureHold.setRequestPerson          (strGetFutureHold.getHoldUserID());
                strSearchFutureHold.setMainProcessDefinition  (strGetFutureHold.getRouteID());
                strSearchFutureHold.setOperationNumber        (strGetFutureHold.getOperationNumber());
                ProductDTO.FutureHoldRecord aFutureHold = aLot.findFutureHoldRecord( strSearchFutureHold);
                if (aFutureHold == null || CimStringUtils.isEmpty(aFutureHold.getHoldType())) {
                    it.remove();
                }
            }
            //===== Set data to return =======
            ArrayList<Infos.LotHoldReq> strFutureHoldCancelList = new ArrayList<>(tmpFutureHoldCancelList.values());
            out.setFutureHoldCancelList(strFutureHoldCancelList);
            out.setFutureReworkCancelList(strFutureReworkCancelList);
        } else {
            log.debug("1 == keepQtimeActionFlag");
        }
        return out;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQTimeTriggerOpeReplaceIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/6/18 15:56
     */
    @Override
    public void qTimeTriggerOpeReplace(Infos.ObjCommon strObjCommonIn,
                                       Inputs.QTimeTriggerOpeReplaceIn strQTimeTriggerOpeReplaceIn) {
        com.fa.cim.newcore.bo.product.CimLot aLot;
        aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,strQTimeTriggerOpeReplaceIn.getLotID());

        CimProductSpecification aTmpProductSpecification;
        CimProductSpecification aProductSpecification;

        aTmpProductSpecification = aLot.getProductSpecification();
        aProductSpecification = aTmpProductSpecification;
        Validations.check(aProductSpecification == null, retCodeConfig.getNotFoundProductSpec());
        //Get target lot's process flow context
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext;
        aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

        Boolean currentPOFlag = true;

        if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW006")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW008")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW014")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW023")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW012")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW024")) {

            // step-1: lot_CheckConditionForPO
            Boolean strLot_CheckConditionForPO_out = lotMethod.lotCheckConditionForPO(strObjCommonIn,
                    strQTimeTriggerOpeReplaceIn.getLotID());


            if ( CimBooleanUtils.isTrue(strLot_CheckConditionForPO_out) == TRUE ) {
            } else {
                currentPOFlag = FALSE;
            }
        }

        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation;
        com.fa.cim.newcore.bo.pd.CimProcessOperation aPosProcessOperation;
        if (TRUE.equals(currentPOFlag)) {

            //Get target lot's current operation
            aProcessOperation = aLot.getProcessOperation();
            aPosProcessOperation = aProcessOperation;
        } else {

            //Get target lot's previous operation
            aProcessOperation = aLot.getPreviousProcessOperation();
            aPosProcessOperation = aProcessOperation;
        }
        Validations.check(null == aPosProcessOperation, retCodeConfig.getNotFoundOperation());
        //Get current route
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aCurrentRoute;
        aCurrentRoute = aPosProcessOperation.getMainProcessDefinition();
        Validations.check(null == aCurrentRoute, retCodeConfig.getNotFoundMainRoute());
        String currentRouteID;
        currentRouteID = aCurrentRoute.getIdentifier();

        //Get current operationNumber
        String currentOperationNumber;
        currentOperationNumber = aPosProcessOperation.getOperationNumber();

        //Get branchInfo of lot's current route
        List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();

        //Get returnInfo of lot's current route
        List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();

        if (CimBooleanUtils.isTrue(currentPOFlag) != TRUE) {

            String previousBranchInfoStr;
            previousBranchInfoStr = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);

            String previousReturnInfoStr;
            previousReturnInfoStr = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

            if (0 < CimStringUtils.length(previousBranchInfoStr)) {

                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousBranchInfoStr, "\\" + BizConstant.SP_KEY_SEPARATOR_DOT);
                int previousTokensLen = CimArrayUtils.getSize(previousTokens);
                if (4 == previousTokensLen) {

                    List<String> strings = new ArrayList<>();
                    strings.add(previousTokens.get(0));
                    strings.add(previousTokens.get(1));
                    String previousBranchRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                    strings.set(0, previousTokens.get(2));
                    strings.set(1, previousTokens.get(3));
                    String previousBranchOpeNo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);

                    // Get PD object
                    com.fa.cim.newcore.bo.pd.CimProcessOperation aPosPD;
                    ObjectIdentifier aRouteID;
                    aRouteID = ObjectIdentifier.buildWithValue(previousBranchRouteIdentifier);
                    aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessOperation.class,aRouteID);

                    ProcessDTO.BranchInfo previousBranchInfo = new ProcessDTO.BranchInfo();
                    previousBranchInfo.setRouteID(ObjectIdentifier.build(previousBranchRouteIdentifier,
                            aPosPD.getPrimaryKey()));
                    previousBranchInfo.setOperationNumber(previousBranchOpeNo);
                    previousBranchInfo.setProcessOperation("");
                    previousBranchInfo.setReworkOutKey("");

                    String previousReworkOutKey;
                    previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);

                    if (0 < CimStringUtils.length(previousReworkOutKey)) {
                        previousBranchInfo.setReworkOutKey(previousReworkOutKey);
                    }

                    int nSeqLen = CimArrayUtils.getSize(branchInfoSeq);
                    branchInfoSeq.add(previousBranchInfo);
                }
            }

            if (0 < CimStringUtils.length(previousReturnInfoStr)) {

                List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousReturnInfoStr, "\\" + BizConstant.SP_KEY_SEPARATOR_DOT);
                int previousTokensLen = CimArrayUtils.getSize(previousTokens);
                if (4 == previousTokensLen) {

                    //------------------------------------
                    // Previous route
                    //------------------------------------
                    List<String> strings = new ArrayList<>();
                    strings.add(previousTokens.get(0));
                    strings.add(previousTokens.get(1));
                    String previousReturnRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);
                    strings.set(0, previousTokens.get(2));
                    strings.set(1, previousTokens.get(3));
                    String previousReturnOpeNo = CimArrayUtils.mergeStringIntoTokens(strings, BizConstant.SP_KEY_SEPARATOR_DOT);

                    // Get PD object
                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                    ObjectIdentifier aRouteID;
                    aRouteID = ObjectIdentifier.buildWithValue(previousReturnRouteIdentifier);

                    aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,aRouteID);

                    ProcessDTO.ReturnInfo previousReturnInfo = new ProcessDTO.ReturnInfo();
                    previousReturnInfo.setRouteID(ObjectIdentifier.build(previousReturnRouteIdentifier, aPosPD.getPrimaryKey()));
                    previousReturnInfo.setOperationNumber(previousReturnOpeNo);
                    previousReturnInfo.setProcessFlow("");
                    previousReturnInfo.setMainProcessFlow("");
                    previousReturnInfo.setModuleProcessFlow("");

                    // Add previous return infomation
                    int nSeqLen = CimArrayUtils.getSize(returnInfoSeq);
                    returnInfoSeq.add(previousReturnInfo);
                }
            }
        }

        String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoSeq);

        String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoSeq);

        Timestamp currentTimeStamp = strObjCommonIn.getTimeStamp().getReportTimeStamp();
        String currentTimeStampVar = CimDateUtils.getTimestampAsString(currentTimeStamp);

        //Get all Q-Time definitions to be replaced for Trigger Operation
        List<ProcessDTO.ReplaceTimeRestrictionSpecification> aReplaceTimeRSSeq = aPosProcessOperation.findReplaceTimeRestrictionsForReplaceTrigger(
                aProductSpecification,
                branchInfoSeq,
                returnInfoSeq,
                currentRouteID,
                currentOperationNumber);

        int nLen = CimArrayUtils.getSize(aReplaceTimeRSSeq);
        if (nLen != 0) {
            Integer qtimeDispatchPrecedeUseCustomFieldFlag = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getIntValue();
            String targetTimeStamp = null;
            for (int iCnt1 = 0; iCnt1 < nLen; iCnt1++) {
                if (!CimStringUtils.equals(aReplaceTimeRSSeq.get(iCnt1).getQTimeType(), BizConstant.SP_QTIMETYPE_BYLOT)) {
                    continue;
                }

                //Check if there is duplicate Q-Time timer
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForLot;
                aQTimeRestrictionForLot = aLot.findQTimeRestrictionByOriginalQTime( aReplaceTimeRSSeq.get(iCnt1).getOriginalQTime());
                if (aQTimeRestrictionForLot != null) {
                    continue;
                }

                //Create QTime timer object
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction;
                aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(null == aQTimeRestriction, retCodeConfig.getNotFoundSystemObj());
                //Add QTime timer object to lot
                aLot.addQTimeRestriction(aQTimeRestriction);

                //Set QTime info data from input parameters
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                ObjectIdentifier waferID = null;

                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(aCurrentRoute.getIdentifier(), aCurrentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setQTimeType(aReplaceTimeRSSeq.get(iCnt1).getQTimeType());
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setPreTrigger(FALSE);
                aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(aReplaceTimeRSSeq.get(iCnt1).getTargetRouteID());
                aQTimeRestrictionInfo.setTargetOperationNumber(aReplaceTimeRSSeq.get(iCnt1).getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(aReplaceTimeRSSeq.get(iCnt1).getTargetBranchInfo());
                aQTimeRestrictionInfo.setTargetReturnInfo(aReplaceTimeRSSeq.get(iCnt1).getTargetReturnInfo());
                aQTimeRestrictionInfo.setWatchdogRequired(TRUE);
                aQTimeRestrictionInfo.setActionDone(FALSE);
                aQTimeRestrictionInfo.setOriginalQTime(aReplaceTimeRSSeq.get(iCnt1).getOriginalQTime());
                aQTimeRestrictionInfo.setProcessDefinitionLevel(aReplaceTimeRSSeq.get(iCnt1).getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setPreviousTargetInfo("");
                aQTimeRestrictionInfo.setControl(aReplaceTimeRSSeq.get(iCnt1).getControl());
                aQTimeRestrictionInfo.setManualCreated(FALSE);

                String dispatchPrecedeTargetTime = null;
                String mostUrgentTargetTimeAction = null;
                //--------------------------------------------------------------------------------------------------------------------------
                // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....)
                //--------------------------------------------------------------------------------------------------------------------------
                Boolean setMostUrgentTimeFlag = FALSE;
                if (aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration() == null ||
                        BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND == aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration().intValue()) {
                    setMostUrgentTimeFlag = TRUE;
                } else {
                    targetTimeStamp = CimDateUtils.getTimestampAsString(new Timestamp(currentTimeStamp.getTime() + aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration().longValue()));
                }

                int actionLength = CimArrayUtils.getSize(aReplaceTimeRSSeq.get(iCnt1).getActions());
                if (actionLength != 0) {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                    int actionCnt = 0;
                    for (int iCnt2 = 0; iCnt2 < actionLength; iCnt2++) {
                        String targetTimeStampForAction;
                        targetTimeStampForAction = CimDateUtils.getTimestampAsString(
                                new Timestamp(currentTimeStamp.getTime() + aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getExpiredTimeDuration().longValue())
                        );

                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                            dispatchPrecedeTargetTime = targetTimeStampForAction;

                            if (0 == qtimeDispatchPrecedeUseCustomFieldFlag) {
                                continue;
                            }
                        } else if (TRUE.equals(setMostUrgentTimeFlag)) {
                            if (0 == CimStringUtils.length(mostUrgentTargetTimeAction)) {
                                //====== The first iteration of for() loop ======//
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                //====== The second iteration of for() loop ======//
                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    // Nothing to do.
                                }
                            }
                        }

                        aQTimeRestrictionInfo.getActions().add(new ProcessDTO.QTimeRestrictionAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTargetTimeStamp(targetTimeStampForAction);
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setAction(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setReasonCode(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getReasonCode());

                        if (0 < CimStringUtils.length(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getOperationNumber())) {
                            if (0 < CimStringUtils.length(ObjectIdentifier.fetchValue(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getActionRouteID()))) {
                                aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getActionRouteID());
                            } else {
                                aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(aReplaceTimeRSSeq.get(iCnt1).getTargetRouteID());
                            }
                        }

                        aQTimeRestrictionInfo.getActions().get(actionCnt).setOperationNumber(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getOperationNumber());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTiming(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getTiming());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMainProcessDefinition(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMainProcessDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMessageDefinition(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMessageDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setCustomField(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getCustomField());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setWatchdogRequired(TRUE);
                        // Action is DispatchPrecede
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(TRUE);
                        }
                        // Action is not DispatchPrecede
                        else {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(FALSE);
                        }
                        actionCnt++;
                    }
                } else {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                }

                if (CimStringUtils.length(dispatchPrecedeTargetTime) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(dispatchPrecedeTargetTime);
                } else if (CimStringUtils.length(mostUrgentTargetTimeAction) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                }

                //Set QTime info to QTime timer object
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                strQtimeInfo.setWatchDogRequired("Y");
                strQtimeInfo.setActionDoneFlag("N");
                strQtimeInfo.setManualCreated(FALSE);

                actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());

                if (actionLength != 0) {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                        strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField(
                                aQTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired(
                                "Y");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag(
                                CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone()) ? "Y" : "N");
                    }
                } else {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                }

                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                params.setLotID(strQTimeTriggerOpeReplaceIn.getLotID());
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                // step-2: qTimeChangeEvent_Make__180
                eventMethod.qTimeChangeEventMake(strObjCommonIn, params);
            }

            List<ProductDTO.WaferInfo> waferInfoSeq = aLot.getAllWaferInfo();

            for (int iCnt1 = 0; iCnt1 < nLen; iCnt1++) {
                if (!CimStringUtils.equals(aReplaceTimeRSSeq.get(iCnt1).getQTimeType(), BizConstant.SP_QTIMETYPE_BYWAFER)) {
                    continue;
                }

                Boolean allWaferFlag = TRUE;
                for (int i = 0; i < CimArrayUtils.getSize(waferInfoSeq); i++) {
                    com.fa.cim.newcore.bo.product.CimWafer aPosWafer;
                    aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,waferInfoSeq.get(i).getWaferID());

                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForWafer;
                    aQTimeRestrictionForWafer = aPosWafer.findQTimeRestrictionByOriginalQTime(
                            aReplaceTimeRSSeq.get(iCnt1).getOriginalQTime());

                    if (aQTimeRestrictionForWafer == null) {
                        allWaferFlag = FALSE;
                        break;
                    }
                }

                if (CimBooleanUtils.isTrue(allWaferFlag)) {
                    continue;
                }

                //Create QTime timer object
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction;
                aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                //Add QTime timer object to lot
                aLot.addQTimeRestriction( aQTimeRestriction);

                //Set QTime info data from input parameters
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                ObjectIdentifier waferID = null;

                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(aCurrentRoute.getIdentifier(),
                        aCurrentRoute.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerOperationNumber((currentOperationNumber));
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp((currentTimeStampVar));
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(aReplaceTimeRSSeq.get(iCnt1)
                        .getTargetRouteID());
                aQTimeRestrictionInfo.setTargetOperationNumber(aReplaceTimeRSSeq.get(iCnt1)
                        .getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(aReplaceTimeRSSeq.get(iCnt1)
                        .getTargetBranchInfo());
                aQTimeRestrictionInfo.setTargetReturnInfo(aReplaceTimeRSSeq.get(iCnt1)
                        .getTargetReturnInfo());
                aQTimeRestrictionInfo.setWatchdogRequired(TRUE);
                aQTimeRestrictionInfo.setActionDone(FALSE);
                aQTimeRestrictionInfo.setOriginalQTime(aReplaceTimeRSSeq.get(iCnt1)
                        .getOriginalQTime());
                aQTimeRestrictionInfo.setProcessDefinitionLevel(aReplaceTimeRSSeq.get(iCnt1)
                        .getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setPreviousTargetInfo((""));
                aQTimeRestrictionInfo.setControl(aReplaceTimeRSSeq.get(iCnt1)
                        .getControl());
                aQTimeRestrictionInfo.setManualCreated(FALSE);
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setQTimeType((BizConstant.SP_QTIMETYPE_BYLOT));
                aQTimeRestrictionInfo.setPreTrigger(FALSE);

                String dispatchPrecedeTargetTime = null;
                String mostUrgentTargetTimeAction = null;
                Boolean setMostUrgentTimeFlag = FALSE;
                if (aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration() == null ||
                        BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND == aReplaceTimeRSSeq.get(iCnt1)
                                .getExpiredTimeDuration().intValue()) {
                    setMostUrgentTimeFlag = TRUE;
                } else {
                    targetTimeStamp = CimDateUtils.getTimestampAsString(new Timestamp(currentTimeStamp.getTime()
                            + aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration().longValue()));
                }

                int actionLength = CimArrayUtils.getSize(aReplaceTimeRSSeq.get(iCnt1).getActions());
                if (actionLength != 0) {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                    int actionCnt = 0;
                    for (int iCnt2 = 0; iCnt2 < actionLength; iCnt2++) {
                        String targetTimeStampForAction;
                        targetTimeStampForAction = CimDateUtils.getTimestampAsString(
                                new Timestamp(currentTimeStamp.getTime() +
                                        aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getExpiredTimeDuration().longValue()));

                        // Action is DispatchPrecede
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE,
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                            dispatchPrecedeTargetTime = targetTimeStampForAction;

                            if (0 == qtimeDispatchPrecedeUseCustomFieldFlag) {
                                continue;
                            }
                        } else if (TRUE.equals(setMostUrgentTimeFlag)) {
                            if (0 == CimStringUtils.length(mostUrgentTargetTimeAction)) {
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    // Nothing to do.
                                }
                            }
                        }
                        aQTimeRestrictionInfo.getActions().add(new ProcessDTO.QTimeRestrictionAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTargetTimeStamp(
                                targetTimeStampForAction);
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setAction(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setReasonCode(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getReasonCode());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setOperationNumber(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getOperationNumber());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTiming(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getTiming());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMainProcessDefinition(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMainProcessDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMessageDefinition(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMessageDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setCustomField(
                                aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getCustomField());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setWatchdogRequired(
                                TRUE);
                        if (0 < CimStringUtils.length(aReplaceTimeRSSeq.get(iCnt1)
                                .getActions().get(iCnt2).getOperationNumber())) {
                            if (0 < CimStringUtils.length(aReplaceTimeRSSeq.get(iCnt1)
                                    .getActions().get(iCnt2).getActionRouteID().getValue())) {
                                aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(
                                        aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getActionRouteID());
                            } else {
                                aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(
                                        aReplaceTimeRSSeq.get(iCnt1).getTargetRouteID());
                            }
                        }

                        // Action is DispatchPrecede
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE
                                , aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(TRUE);
                        }
                        // Action is not DispatchPrecede
                        else {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(FALSE);
                        }
                        actionCnt++;
                    }
                } else {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                }

                if (CimStringUtils.length(dispatchPrecedeTargetTime) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(dispatchPrecedeTargetTime);
                } else if (CimStringUtils.length(mostUrgentTargetTimeAction) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp((mostUrgentTargetTimeAction));
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp((targetTimeStamp));
                }

                //Set QTime info to QTime timer object
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                strQtimeInfo.setWatchDogRequired("Y");
                strQtimeInfo.setActionDoneFlag(")N");
                strQtimeInfo.setManualCreated(FALSE);

                actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());

                if (actionLength != 0) {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                        strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getTargetTimeStamp());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getAction());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getReasonCode());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getActionRouteID());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getOperationNumber());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getTiming());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getMainProcessDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getMessageDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getCustomField());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("Y");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag(CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions()
                                .get(iCnt3).getActionDone()) ? "Y" : "N");
                    }
                } else {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                }

                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                params.setLotID(strQTimeTriggerOpeReplaceIn.getLotID());
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                // step-2: qTimeChangeEvent_Make__180
                eventMethod.qTimeChangeEventMake(strObjCommonIn, params);
            }
        }

        /*---------------------------------------------------*/
        /*   Find QTime Restrictions for target lot          */
        /*---------------------------------------------------*/
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeq;
        if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW012")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW024")) {
            qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            int qtimeSeqForWaferCount = 0;
            qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();

            if (qtimeSeqForWaferCount > 0) {
                qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();
            } else {
                qtimeSeq = aLot.allQTimeRestrictions();
            }
        }

        int QTSLength = CimArrayUtils.getSize(qtimeSeq);
        for (int iCnt4 = 0; iCnt4 < QTSLength; iCnt4++) {

            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qtimeSeq.get(iCnt4);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());

            ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTime.getQTimeRestrictionInfo();

            if (aQTimeRestrictionInfo == null || CimStringUtils.length(
                    aQTimeRestrictionInfo.getTriggerOperationNumber()) < 1) {
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            }

            if (CimStringUtils.equals(aQTimeRestrictionInfo.getControl(),
                    BizConstant.SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER)) {

                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(
                        ObjectIdentifier.build(aCurrentRoute.getIdentifier(), aCurrentRoute.getPrimaryKey()));

                aQTimeRestrictionInfo.setTriggerOperationNumber((currentOperationNumber));
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setControl((""));

                aQTime.setQTimeRestrictionInfo( aQTimeRestrictionInfo);

                //----------------------------------------------------------------
                // Make event
                //----------------------------------------------------------------
                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(BizConstant.SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER);

                if (CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (FALSE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    strQtimeInfo.setWatchDogRequired("N");
                }
                if (TRUE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone())) {
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (FALSE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActionDone())) {
                    strQtimeInfo.setActionDoneFlag("N");
                }

                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());

                int actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                if (actionLength != 0) {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    for (int iCnt5 = 0; iCnt5 < actionLength; iCnt5++) {
                        strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setQrestrictionTargetTimeStamp(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getTargetTimeStamp());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setQrestrictionAction(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getAction());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setReasonCodeID(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getReasonCode());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setActionRouteID(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getActionRouteID());  //DSN000100682
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setActionOperationNumber(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getOperationNumber());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setFutureHoldTiming(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getTiming());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setReworkRouteID(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getMainProcessDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setMessageID(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getMessageDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setCustomField(
                                aQTimeRestrictionInfo.getActions().get(iCnt5).getCustomField());

                        if (TRUE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions().get(iCnt5).getWatchdogRequired())) {
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setWatchDogRequired("Y");
                        } else if (FALSE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions().get(iCnt5).getWatchdogRequired())) {
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setWatchDogRequired("N");
                        }
                        if (TRUE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions().get(iCnt5).getActionDone())) {
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setActionDoneFlag("Y");
                        } else if (FALSE == CimBooleanUtils.isTrue(aQTimeRestrictionInfo.getActions().get(iCnt5).getActionDone())) {
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt5).setActionDoneFlag("N");
                        }
                    }
                } else {
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                }

                Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE);
                params.setLotID(strQTimeTriggerOpeReplaceIn.getLotID());
                params.setQtimeInfo(strQtimeInfo);
                params.setClaimMemo("");
                // step-2: qTimeChangeEvent_Make__180
                eventMethod.qTimeChangeEventMake(strObjCommonIn, params);
            }

        }
    }

    @Override
    public void qtimeSetByPJComp(Infos.ObjCommon objCommon, Inputs.QtimeSetByPJCompIn qtimeSetByPJCompIn) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        String qtimeDispatchPrecedeUseCustomFieldFlagStr = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getValue();
        int qtimeDispatchPrecedeUseCustomFieldFlag = null == qtimeDispatchPrecedeUseCustomFieldFlagStr ? 0 : Integer.parseInt(qtimeDispatchPrecedeUseCustomFieldFlagStr);

        /*--------------------*/
        /*   Get Current PO   */
        /*--------------------*/
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, qtimeSetByPJCompIn.getLotID());
        com.fa.cim.newcore.bo.pd.CimProcessOperation aPosProcessOperation = aLot.getProcessOperation();
        Validations.check(null == aPosProcessOperation, retCodeConfig.getNotFoundProcessOperation(), objCommon.getTransactionID());
        com.fa.cim.newcore.bo.pd.CimProcessDefinition currentRoute = aPosProcessOperation.getMainProcessDefinition();
        Validations.check(null == currentRoute, retCodeConfig.getNotFoundRoute(), objCommon.getTransactionID());
        String currentOperationNumber = aPosProcessOperation.getOperationNumber();
        String processFlowType = currentRoute.getProcessFlowType();
        //Get target lot's process flow context
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(null == aProcessFlowContext, retCodeConfig.getNotFoundPfx(), objCommon.getTransactionID());
        //Get target lot's ProductSpecification
        CimProductSpecification aProductSpecification = aLot.getProductSpecification();
        Validations.check(null == aProductSpecification, retCodeConfig.getNotFoundProductSpec(), objCommon.getTransactionID());
        List<ProcessDTO.TimeRestrictionSpecification> aTimeRSSequence = aPosProcessOperation.findTimeRestrictionsForProduct(aProductSpecification);
        int nLen = CimArrayUtils.getSize(aTimeRSSequence);
        log.info("(*aTimeRSSequence).length() : {}", nLen);
        if (nLen != 0) {
            log.info("qtimeSetByPJComp  nLen != 0");
            Timestamp currentTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
            String targetTimeStamp = null;
            //Get branchInfo of lot's current route
            List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();
            //Get returnInfo of lot's current route
            List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();
            String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoSeq);
            String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoSeq);

            /*--------------------------------------*/
            /*   Create and Add Qtime-Restriction   */
            /*--------------------------------------*/
            int waferIDSeqCnt = CimArrayUtils.getSize(qtimeSetByPJCompIn.getWaferIDSeq());
            for (int n = 0; n < nLen; n++) {
                if (!CimStringUtils.equals(aTimeRSSequence.get(n).getDefaultTimeRS().getQTimeType(), BizConstant.SP_QTIMETYPE_BYWAFER)) {
                    log.info("QTimeType != SP_QTimeType_ByWafer , continue");
                    continue;
                }
                ObjectIdentifier oiMainPD = new ObjectIdentifier(currentRoute.getIdentifier(), currentRoute.getPrimaryKey());
                List<String> stsOrgQTime = new ArrayList<>();
                stsOrgQTime.add(ObjectIdentifier.fetchValue(oiMainPD));
                stsOrgQTime.add(currentOperationNumber);
                stsOrgQTime.add(ObjectIdentifier.fetchValue(oiMainPD));
                stsOrgQTime.add(aTimeRSSequence.get(n).getDefaultTimeRS().getTargetOperationNumber());
                String originalQTime = CimArrayUtils.mergeStringIntoTokens(stsOrgQTime, BizConstant.SP_KEY_SEPARATOR_DOT);
                com.fa.cim.newcore.bo.product.CimWafer aWafer = null;
                for (int m = 0; m < waferIDSeqCnt; m++) {
                    log.info("", "Check QTime restriction , WaferID= {}", qtimeSetByPJCompIn.getWaferIDSeq().get(m).getValue());
                    //Get Wafer Object
                    aWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, qtimeSetByPJCompIn.getWaferIDSeq().get(m));
                    //Check if there is Q-Time triggered
                    Boolean resetQTimeFlag = false;
                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForWafer = aWafer.findQTimeRestrictionByOriginalQTime(originalQTime);
                    if (null != aQTimeRestrictionForWafer) {
                        // Reset the QTime related information
                        resetQTimeFlag = true;
                        // When there is QTime timer of  same trigger operation, those QTime timer are removed.
                        aWafer.removeQTimeRestriction(aQTimeRestrictionForWafer);
                        //Delete QTime timer object
                        qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestrictionForWafer);
                    }
                    //Create QTime timer object
                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                    Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                    //Add QTime timer object to wafer
                    log.info("", "aWafer->addQTimeRestriction");
                    aWafer.addQTimeRestriction(aQTimeRestriction);
                    ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                    String currentTimeStampVar = currentTimeStamp.toString();
                    aQTimeRestrictionInfo.setLotID(new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey()));
                    aQTimeRestrictionInfo.setTriggerMainProcessDefinition(new ObjectIdentifier(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                    aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                    aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                    aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                    aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStampVar);
                    aQTimeRestrictionInfo.setTargetMainProcessDefinition(new ObjectIdentifier(currentRoute.getIdentifier(), currentRoute.getPrimaryKey()));
                    aQTimeRestrictionInfo.setTargetOperationNumber(aTimeRSSequence.get(n).getDefaultTimeRS().getTargetOperationNumber());
                    aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                    aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                    aQTimeRestrictionInfo.setWatchdogRequired(true);
                    aQTimeRestrictionInfo.setActionDone(false);
                    aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                    aQTimeRestrictionInfo.setProcessDefinitionLevel(aTimeRSSequence.get(n).getDefaultTimeRS().getProcessDefinitionLevel());
                    aQTimeRestrictionInfo.setManualCreated(false);
                    aQTimeRestrictionInfo.setQTimeType(aTimeRSSequence.get(n).getDefaultTimeRS().getQTimeType());
                    aQTimeRestrictionInfo.setWaferID(new ObjectIdentifier(qtimeSetByPJCompIn.getWaferIDSeq().get(m).getValue(), aWafer.getPrimaryKey()));
                    aQTimeRestrictionInfo.setPreTrigger(true);
                    // loop for actions
                    int actionLength = CimArrayUtils.getSize(aTimeRSSequence.get(n).getDefaultTimeRS().getActions());

                    String mostUrgentTargetTimeAction = null;

                    //--------------------------------------------------------------------------------------------------------------------------
                    // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....)
                    //--------------------------------------------------------------------------------------------------------------------------
                    Boolean setMostUrgentTimeFlag = false;
                    if (Double.valueOf(BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND).doubleValue() == aTimeRSSequence.get(n).getDefaultTimeRS().getExpiredTimeDuration().doubleValue()) {
                        setMostUrgentTimeFlag = true;
                    } else {
                        //targetTimeStamp = currentTimeStamp.addDuration( (*aTimeRSSequence)[n].expiredTimeDuration ) ;
                        targetTimeStamp = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + aTimeRSSequence.get(n).getDefaultTimeRS().getExpiredTimeDuration())).toString();
                    }

                    log.info("", "#### targetTime = {}", targetTimeStamp);

                    if (actionLength != 0) {
                        log.info("", "(*aTimeRSSequence)[n].actions.length() : {}", actionLength);
                        List<ProcessDTO.QTimeRestrictionAction> actions = new ArrayList<>();
                        for (int counter = 0; counter < actionLength; counter++) {
                            // CustomField for DispatchPrecede is not used and action is DispatchPrecede
                            if (0 == qtimeDispatchPrecedeUseCustomFieldFlag &&
                                    CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getAction())) {
                                log.info("", "CustomField for DispatchPrecede is not used and action is DispatchPrecede");
                                continue;
                            }
                            String targetTimeStampForAction = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getExpiredTimeDuration())).toString();
                            if (TRUE.equals(setMostUrgentTimeFlag)) {
                                if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                    //====== The first iteration of for() loop ======//
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    //====== The second iteration of for() loop ======//
                                    if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                        mostUrgentTargetTimeAction = targetTimeStampForAction;
                                    }
                                }
                            }
                            ProcessDTO.QTimeRestrictionAction action = new ProcessDTO.QTimeRestrictionAction();
                            action.setTargetTimeStamp(targetTimeStampForAction);
                            action.setAction(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getAction());
                            action.setReasonCode(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getReasonCode());
                            action.setOperationNumber(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getOperationNumber());
                            action.setTiming(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getTiming());
                            action.setMainProcessDefinition(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getMainProcessDefinition());
                            action.setMessageDefinition(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getMessageDefinition());
                            action.setCustomField(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getCustomField());
                            action.setWatchdogRequired(true);
                            if (CimStringUtils.isNotEmpty(aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getOperationNumber())) {
                                action.setActionRouteID(oiMainPD);
                            }
                            // Action is DispatchPrecede
                            if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getAction())) {
                                log.info("Action is DispatchPrecede");
                                action.setActionDone(true);
                            }
                            // Action is not DispatchPrecede
                            else {
                                log.info("", "Action is : {}", aTimeRSSequence.get(n).getDefaultTimeRS().getActions().get(counter).getAction());
                                action.setActionDone(false);
                            }
                            actions.add(action);
                        }
                        aQTimeRestrictionInfo.setActions(actions);
                    }
                    if (CimStringUtils.isNotEmpty(mostUrgentTargetTimeAction)) {
                        aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                        log.info("", "aQTimeRestrictionInfo.targetTimeStamp : {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                    } else {
                        aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                        log.info("", "aQTimeRestrictionInfo.targetTimeStamp : {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                    }
                    //set data
                    log.info("", "aQTimeRestriction->setQTimeRestrictionInfo");
                    aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);
                    /*--------------------*/
                    /*     Make event     */
                    /*--------------------*/
                    Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                    strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                    strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                    strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                    strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                    strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                    strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                    strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                    strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                    strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                    strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                    strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                    strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                    strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                    strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                    strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                    strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                    if (TRUE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                        log.info("", "TRUE == aQTimeRestrictionInfo.watchdogRequired");
                        strQtimeInfo.setWatchDogRequired("Y");
                    } else if (FALSE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                        log.info("", "FALSE == aQTimeRestrictionInfo.watchdogRequired");
                        strQtimeInfo.setWatchDogRequired("N");
                    }
                    if (TRUE.equals(aQTimeRestrictionInfo.getActionDone())) {
                        log.info("", "TRUE == aQTimeRestrictionInfo.actionDone");
                        strQtimeInfo.setActionDoneFlag("Y");
                    } else if (FALSE.equals(aQTimeRestrictionInfo.getActionDone())) {
                        log.info("", "FALSE == aQTimeRestrictionInfo.actionDone");
                        strQtimeInfo.setActionDoneFlag("N");
                    }
                    strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                    actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                    log.info("", "aQTimeRestrictionInfo.actions.length() : {}", actionLength);
                    if (actionLength != 0) {
                        log.info("", "actionLength != 0");
                        List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                        for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                            log.info("", "loop to aQTimeRestrictionInfo.actions.length()", iCnt3);
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                            qTimeActionInfo.setReasonCodeID(aQTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                            qTimeActionInfo.setActionRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                            qTimeActionInfo.setReworkRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                            qTimeActionInfo.setCustomField(aQTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                            if (aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired()) {
                                log.info("", "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt3).watchdogRequired");
                                qTimeActionInfo.setWatchDogRequired("Y");
                            } else if (!aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired()) {
                                log.info("", "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt3).watchdogRequired");
                                qTimeActionInfo.setWatchDogRequired("N");
                            }
                            if (aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone()) {
                                log.info("", "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt3).actionDone");
                                qTimeActionInfo.setActionDoneFlag("Y");
                            } else if (!aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone()) {
                                log.info("", "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt3).actionDone");
                                qTimeActionInfo.setActionDoneFlag("N");
                            }
                            strQtimeActionInfoList.add(qTimeActionInfo);
                        }
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                    }

                    // qTimeChangeEvent_Make__180
                    Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                    if (resetQTimeFlag) {
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_RESET);
                    } else {
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                    }
                    params.setLotID(qtimeSetByPJCompIn.getLotID());
                    params.setQtimeInfo(strQtimeInfo);
                    params.setClaimMemo("");
                    eventMethod.qTimeChangeEventMake(objCommon, params);
                }
            }
        }

        /*---------------------------------------------*/
        /*  Replace Trigger Q-Time (Sub/Rework Route)  */
        /*---------------------------------------------*/
        if (CimStringUtils.equals(processFlowType, BizConstant.SP_FLOWTYPE_SUB)) {
            log.info("", "0 == CIMFWStrCmp( processFlowType, SP_FLOWTYPE_SUB)");
            Inputs.QTimeTriggerOpeReplaceByPJCompIn strQTimeTriggerOpeReplaceByPJCompIn = new Inputs.QTimeTriggerOpeReplaceByPJCompIn();
            strQTimeTriggerOpeReplaceByPJCompIn.setLotID(qtimeSetByPJCompIn.getLotID());
            strQTimeTriggerOpeReplaceByPJCompIn.setWaferIDSeq(qtimeSetByPJCompIn.getWaferIDSeq());
            this.qTimeTriggerOpeReplaceByPJComp(objCommon, strQTimeTriggerOpeReplaceByPJCompIn);
        }
    }

    @Override
    public void qTimeTriggerOpeReplaceByPJComp(Infos.ObjCommon objCommon, Inputs.QTimeTriggerOpeReplaceByPJCompIn qTimeTriggerOpeReplaceByPJCompIn) {
        //Get Lot Object
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, qTimeTriggerOpeReplaceByPJCompIn.getLotID());
        //Get target lot's ProductSpecification
        CimProductSpecification aProductSpecification = aLot.getProductSpecification();
        Validations.check(null == aProductSpecification, retCodeConfig.getNotFoundProductSpec(), objCommon.getTransactionID());
        //Get target lot's process flow context
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(null == aProcessFlowContext, retCodeConfig.getNotFoundPfx(), objCommon.getTransactionID());
        //--------------------------------------------------------------------------
        // Get PO from Current Operation.
        //--------------------------------------------------------------------------
        log.info("Get PO from Current Operation");
        //Get target lot's current operation
        CimProcessOperation aPosProcessOperation = aLot.getProcessOperation();
        Validations.check(null == aPosProcessOperation, retCodeConfig.getNotFoundProcessOperation(), objCommon.getTransactionID());

        //Get current route
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aCurrentRoute = aPosProcessOperation.getMainProcessDefinition();
        Validations.check(null == aCurrentRoute, retCodeConfig.getNotFoundRoute(), objCommon.getTransactionID());
        String currentRouteID = aCurrentRoute.getIdentifier();
        //Get current operationNumber
        String currentOperationNumber = aPosProcessOperation.getOperationNumber();
        //Get branchInfo of lot's current route
        List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();
        //Get returnInfo of lot's current route
        List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();
        String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoSeq);
        String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoSeq);
        Timestamp currentTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
        //Get all Q-Time definitions to be replaced for Trigger Operation
        List<ProcessDTO.ReplaceTimeRestrictionSpecification> aReplaceTimeRSSeq = aPosProcessOperation.findReplaceTimeRestrictionsForReplaceTrigger(aProductSpecification,
                branchInfoSeq, returnInfoSeq, currentRouteID, currentOperationNumber);
        int nLen = CimArrayUtils.getSize(aReplaceTimeRSSeq);
        if (nLen != 0) {
            log.info("aReplaceTimeRSSeq->length() : {}", nLen);
            String qtimeDispatchPrecedeUseCustomFieldFlagStr = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getValue();
            int qtimeDispatchPrecedeUseCustomFieldFlag = null == qtimeDispatchPrecedeUseCustomFieldFlagStr ? 0 : Integer.valueOf(qtimeDispatchPrecedeUseCustomFieldFlagStr);
            int waferIDSeqCnt = CimArrayUtils.getSize(qTimeTriggerOpeReplaceByPJCompIn.getWaferIDSeq());
            String targetTimeStamp = null;
            for (int iCnt1 = 0; iCnt1 < nLen; iCnt1++) {
                log.info("loop to aReplaceTimeRSSeq->length() : {}", iCnt1);

                if (!CimStringUtils.equals(aReplaceTimeRSSeq.get(iCnt1).getQTimeType(), BizConstant.SP_QTIMETYPE_BYWAFER)) {
                    log.info("", "QTimeType != SP_QTimeType_ByWafer , continue");
                    continue;
                }
                CimWafer aWafer = null;
                for (int m = 0; m < waferIDSeqCnt; m++) {
                    log.info("Check QTime restriction , WaferID= {} ", qTimeTriggerOpeReplaceByPJCompIn.getWaferIDSeq().get(m).getValue());
                    //Get Wafer Object
                    aWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, qTimeTriggerOpeReplaceByPJCompIn.getWaferIDSeq().get(m));
                    //Check if there is Q-Time triggered
                    Boolean resetQTimeFlag = false;
                    CimQTimeRestriction aQTimeRestrictionForWafer = aWafer.findQTimeRestrictionByOriginalQTime(aReplaceTimeRSSeq.get(iCnt1).getOriginalQTime());
                    if (null != aQTimeRestrictionForWafer) {
                        // Reset the QTime related information
                        resetQTimeFlag = true;
                        // When there is QTime timer of  same trigger operation, those QTime timer are removed.
                        aWafer.removeQTimeRestriction(aQTimeRestrictionForWafer);
                        //Delete QTime timer object
                        qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestrictionForWafer);
                    }
                    //Create QTime timer object
                    CimQTimeRestriction aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                    Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());
                    //Add QTime timer object to wafer
                    log.info("", "aWafer->addQTimeRestriction");
                    aWafer.addQTimeRestriction(aQTimeRestriction);
                    //Set QTime info data from input parameters
                    ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                    aQTimeRestrictionInfo.setLotID(new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey()));
                    aQTimeRestrictionInfo.setTriggerMainProcessDefinition(new ObjectIdentifier(aCurrentRoute.getIdentifier(), aCurrentRoute.getPrimaryKey()));
                    aQTimeRestrictionInfo.setQTimeType(aReplaceTimeRSSeq.get(iCnt1).getQTimeType());
                    aQTimeRestrictionInfo.setWaferID(new ObjectIdentifier(qTimeTriggerOpeReplaceByPJCompIn.getWaferIDSeq().get(m).getValue(), aWafer.getPrimaryKey()));
                    aQTimeRestrictionInfo.setPreTrigger(true);
                    aQTimeRestrictionInfo.setTriggerOperationNumber(currentOperationNumber);
                    aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                    aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                    aQTimeRestrictionInfo.setTriggerTimeStamp(currentTimeStamp.toString());
                    aQTimeRestrictionInfo.setTargetMainProcessDefinition(aReplaceTimeRSSeq.get(iCnt1).getTargetRouteID());
                    aQTimeRestrictionInfo.setTargetOperationNumber(aReplaceTimeRSSeq.get(iCnt1).getTargetOperationNumber());
                    aQTimeRestrictionInfo.setTargetBranchInfo(aReplaceTimeRSSeq.get(iCnt1).getTargetBranchInfo());
                    aQTimeRestrictionInfo.setTargetReturnInfo(aReplaceTimeRSSeq.get(iCnt1).getTargetReturnInfo());
                    aQTimeRestrictionInfo.setWatchdogRequired(true);
                    aQTimeRestrictionInfo.setActionDone(false);
                    aQTimeRestrictionInfo.setOriginalQTime(aReplaceTimeRSSeq.get(iCnt1).getOriginalQTime());
                    aQTimeRestrictionInfo.setProcessDefinitionLevel(aReplaceTimeRSSeq.get(iCnt1).getProcessDefinitionLevel());
                    aQTimeRestrictionInfo.setControl(aReplaceTimeRSSeq.get(iCnt1).getControl());
                    aQTimeRestrictionInfo.setManualCreated(false);

                    String dispatchPrecedeTargetTime = null;
                    String mostUrgentTargetTimeAction = null;
                    //--------------------------------------------------------------------------------------------------------------------------
                    // "expiredTimeDuration=0" means "dispatch precede" is not specified on SMS. Then, set default time stamp(1901-01-01.....)
                    //--------------------------------------------------------------------------------------------------------------------------
                    Boolean setMostUrgentTimeFlag = false;
                    if (Double.valueOf(BizConstant.SP_DISPATCH_PRECEDE_NOT_FOUND).doubleValue() == aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration().doubleValue()) {
                        log.info("SP_DISPATCH_PRECEDE_NOT_FOUND == aReplaceTimeRSSeq.get(iCnt1).expiredTimeDuration");
                        setMostUrgentTimeFlag = true;
                    } else {
                        log.info("SP_DISPATCH_PRECEDE_NOT_FOUND != aReplaceTimeRSSeq.get(iCnt1).expiredTimeDuration");
                        targetTimeStamp = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + aReplaceTimeRSSeq.get(iCnt1).getExpiredTimeDuration())).toString();
                    }
                    log.info("#### targetTime = {}", targetTimeStamp);
                    int actionLength = CimArrayUtils.getSize(aReplaceTimeRSSeq.get(iCnt1).getActions());
                    if (actionLength != 0) {
                        log.info("aReplaceTimeRSSeq.get(iCnt1).actions.length()", actionLength);
                        List<ProcessDTO.QTimeRestrictionAction> qTimeRestrictionActions = new ArrayList<>();
                        int actionCnt = 0;
                        for (int iCnt2 = 0; iCnt2 < actionLength; iCnt2++) {
                            log.info("loop to aReplaceTimeRSSeq.get(iCnt1).actions.length()", iCnt2);
                            String targetTimeStampForAction;
                            targetTimeStampForAction = new Timestamp(CimNumberUtils.longValue(currentTimeStamp.getTime() + aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getExpiredTimeDuration())).toString();

                            // Action is DispatchPrecede
                            if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                                log.info("", "Action is DispatchPrecede");
                                dispatchPrecedeTargetTime = targetTimeStampForAction;
                                // CustomField for DispatchPrecede is not used
                                if (0 == qtimeDispatchPrecedeUseCustomFieldFlag) {
                                    log.info("", "CustomField for DispatchPrecede is not used");
                                    continue;
                                }
                            } else if (TRUE.equals(setMostUrgentTimeFlag)) {
                                log.info("", "TRUE == setMostUrgentTimeFlag");
                                if (CimStringUtils.isEmpty(mostUrgentTargetTimeAction)) {
                                    log.info("", "0 == CIMFWStrLen( mostUrgentTargetTimeAction )");
                                    //====== The first iteration of for() loop ======//
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {
                                    log.info("", "0 != CIMFWStrLen( mostUrgentTargetTimeAction )");
                                    //====== The second iteration of for() loop ======//
                                    if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                        log.info("", "mostUrgentTargetTimeAction > targetTimeStampForAction");
                                        mostUrgentTargetTimeAction = targetTimeStampForAction;
                                    }
                                }
                            }
                            ProcessDTO.QTimeRestrictionAction action = new ProcessDTO.QTimeRestrictionAction();
                            action.setTargetTimeStamp(targetTimeStampForAction);
                            action.setAction(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction());
                            action.setReasonCode(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getReasonCode());
                            action.setOperationNumber(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getOperationNumber());
                            action.setTiming(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getTiming());
                            action.setMainProcessDefinition(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMainProcessDefinition());
                            action.setMessageDefinition(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getMessageDefinition());
                            action.setCustomField(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getCustomField());
                            action.setWatchdogRequired(true);
                            if (CimStringUtils.isNotEmpty(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getOperationNumber())) {
                                log.info("", "actionOpeNo is not blank");
                                if (!ObjectIdentifier.isEmpty(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getActionRouteID())) {
                                    log.info("", "actionRouteID is replace RouteID");
                                    action.setActionRouteID(aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getActionRouteID());
                                } else {
                                    log.info("", "actionRouteID is original Q-Time RouteID");
                                    action.setActionRouteID(aReplaceTimeRSSeq.get(iCnt1).getTargetRouteID());
                                }
                            }
                            // Action is DispatchPrecede
                            if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction())) {
                                log.info("", "Action is DispatchPrecede");
                                action.setActionDone(true);
                            }
                            // Action is not DispatchPrecede
                            else {
                                log.info("", "Action is : {}", aReplaceTimeRSSeq.get(iCnt1).getActions().get(iCnt2).getAction());
                                action.setActionDone(false);
                            }
                            qTimeRestrictionActions.add(action);
                        }
                        aQTimeRestrictionInfo.setActions(qTimeRestrictionActions);
                    }

                    if (CimStringUtils.isNotEmpty(dispatchPrecedeTargetTime)) {
                        log.info("CIMFWStrLen( dispatchPrecedeTargetTime ) != 0");
                        aQTimeRestrictionInfo.setTargetTimeStamp(dispatchPrecedeTargetTime);
                        log.info("aQTimeRestrictionInfo.targetTimeStamp : {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                    } else if (CimStringUtils.isNotEmpty(mostUrgentTargetTimeAction)) {
                        log.info("CIMFWStrLen( mostUrgentTargetTimeAction ) != 0");
                        aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                        log.info("aQTimeRestrictionInfo.targetTimeStamp : {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                    } else {
                        log.info("CIMFWStrLen( mostUrgentTargetTimeAction ) == 0");
                        aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                        log.info("aQTimeRestrictionInfo.targetTimeStamp : {}", aQTimeRestrictionInfo.getTargetTimeStamp());
                    }
                    //Set QTime info to QTime timer object
                    aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);
                    //----------------------------------------------------------------
                    // Make event
                    //----------------------------------------------------------------
                    Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                    strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                    strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                    strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                    strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                    strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                    strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                    strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                    strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                    strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                    strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                    strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                    strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                    strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                    strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                    strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                    strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                    strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                    strQtimeInfo.setWatchDogRequired("Y");
                    strQtimeInfo.setActionDoneFlag("N");
                    strQtimeInfo.setManualCreated(false);
                    actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                    if (actionLength != 0) {
                        log.info("", "actionLength != 0");
                        List<Infos.QTimeActionInfo> strQtimeActionInfoList = new ArrayList<>();
                        for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                            log.info("", "loop to aQTimeRestrictionInfo.actions.length()", iCnt3);
                            Infos.QTimeActionInfo qTimeActionInfo = new Infos.QTimeActionInfo();
                            qTimeActionInfo.setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                            qTimeActionInfo.setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                            qTimeActionInfo.setReasonCodeID(aQTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                            qTimeActionInfo.setActionRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());
                            qTimeActionInfo.setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                            qTimeActionInfo.setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                            qTimeActionInfo.setReworkRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                            qTimeActionInfo.setMessageID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                            qTimeActionInfo.setCustomField(aQTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                            qTimeActionInfo.setWatchDogRequired("Y");
                            qTimeActionInfo.setActionDoneFlag(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone() ? "Y" : "N");
                            strQtimeActionInfoList.add(qTimeActionInfo);
                        }
                        strQtimeInfo.setStrQtimeActionInfoList(strQtimeActionInfoList);
                    }
                    // qTimeChangeEvent_Make__180
                    Inputs.QTimeChangeEventMakeParams params = new Inputs.QTimeChangeEventMakeParams();
                    if (resetQTimeFlag) {
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_RESET);
                    } else {
                        params.setUpdateMode(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE);
                    }
                    params.setLotID(aQTimeRestrictionInfo.getLotID());
                    params.setQtimeInfo(strQtimeInfo);
                    params.setClaimMemo("");
                    eventMethod.qTimeChangeEventMake(objCommon, params);
                }
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQtimeLotSetClearByOperationCompIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.QtimeLotSetClearByOperationCompOut>
     * @throws
     * @author Ho
     * @date 2019/8/27 14:55
     */
    public Infos.QtimeLotSetClearByOperationCompOut qtimeLotSetClearByOperationComp(
            Infos.ObjCommon strObjCommonIn,
            Infos.QtimeLotSetClearByOperationCompIn strQtimeLotSetClearByOperationCompIn) {
        String methodName = null;

        log.info("qtimeLotSetClearByOperationComp");
        Timestamp triggerTimeStamp;
        ObjectIdentifier lotID = strQtimeLotSetClearByOperationCompIn.getLotID();

        com.fa.cim.newcore.bo.product.CimLot aLot;
        aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);

        com.fa.cim.newcore.bo.pd.CimProcessOperation aTmpProcessOperation;
        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation;
        if (strQtimeLotSetClearByOperationCompIn.getPreviousOperationFlag()) {

            log.info("" + "Get PO from the previous Operation.");
            aTmpProcessOperation = aLot.getPreviousProcessOperation();
            aProcessOperation = aTmpProcessOperation;

            Timestamp tmpTimeVar;
            tmpTimeVar = aProcessOperation.getActualCompTimeStamp();
            triggerTimeStamp = tmpTimeVar;
        } else {

            log.info("" + "Get PO from the current Operation.");
            aTmpProcessOperation = aLot.getProcessOperation();
            aProcessOperation = aTmpProcessOperation;

            triggerTimeStamp = strObjCommonIn.getTimeStamp().getReportTimeStamp();
        }
        Validations.check(aProcessOperation == null, retCodeConfig.getNotFoundOperation());

        com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD;
        aMainPD = aProcessOperation.getMainProcessDefinition();

        String operationNumber;
        operationNumber = aProcessOperation.getOperationNumber();

        Validations.check(aMainPD == null, retCodeConfig.getNotFoundMainRoute());

        String processFlowType;
        processFlowType = aMainPD.getProcessFlowType();

        if (CimStringUtils.equals(processFlowType, SP_FLOWTYPE_SUB)) {
            log.info("" + "StringUtils.equals( processFlowType+ SP_FLOWTYPE_SUB)");
            Infos.QTimeTargetOpeReplaceIn strQTimeTargetOpeReplaceIn = new Infos.QTimeTargetOpeReplaceIn();
            strQTimeTargetOpeReplaceIn.setLotID(lotID);
            strQTimeTargetOpeReplaceIn.setSpecificControlFlag(TRUE);

            // call qTimeTargetOpeReplace
            qTimeTargetOpeReplace(strObjCommonIn, strQTimeTargetOpeReplaceIn);

        }

        Infos.LotQtimeInfoGetForClearByProcessOperationOut strLotQtimeInfoGetForClearByProcessOperationOut;
        Infos.LotQtimeInfoGetForClearByProcessOperationIn strLotQtimeInfoGetForClearByProcessOperationIn = new Infos.LotQtimeInfoGetForClearByProcessOperationIn();
        strLotQtimeInfoGetForClearByProcessOperationIn.setLotID(lotID);
        strLotQtimeInfoGetForClearByProcessOperationIn.setAllClearFlag(FALSE);
        strLotQtimeInfoGetForClearByProcessOperationIn.setPreviousOperationFlag(strQtimeLotSetClearByOperationCompIn.getPreviousOperationFlag());

        // call lot_qtimeInfo_GetForClearByProcessOperation
        strLotQtimeInfoGetForClearByProcessOperationOut = lotQtimeInfoGetForClearByProcessOperation(
                strObjCommonIn,
                strLotQtimeInfoGetForClearByProcessOperationIn);

        Infos.QtimeLotSetClearByOperationCompOut strQtimeLotSetClearByOperationCompOut = new Infos.QtimeLotSetClearByOperationCompOut();
        strQtimeLotSetClearByOperationCompOut.setStrLotHoldReleaseList(strLotQtimeInfoGetForClearByProcessOperationOut.getStrLotHoldReleaseList());
        strQtimeLotSetClearByOperationCompOut.setStrFutureHoldCancelList(strLotQtimeInfoGetForClearByProcessOperationOut.getStrFutureHoldCancelList());
        strQtimeLotSetClearByOperationCompOut.setStrFutureReworkCancelList(strLotQtimeInfoGetForClearByProcessOperationOut.getStrFutureReworkCancelList());

        Integer qtimeDispatchPrecedeUseCustomFieldFlag = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getIntValue();
        qtimeDispatchPrecedeUseCustomFieldFlag = qtimeDispatchPrecedeUseCustomFieldFlag == null ? 0 : qtimeDispatchPrecedeUseCustomFieldFlag;

        int i = 0;
        int aQTimeRestrictionSequenceLen = CimArrayUtils.getSize(strLotQtimeInfoGetForClearByProcessOperationOut.getQTimeClearList());
        for (i = 0; i < aQTimeRestrictionSequenceLen; i++) {
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction;
            aQTimeRestriction = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimQTimeRestriction.class, strLotQtimeInfoGetForClearByProcessOperationOut.getQTimeClearList().get(i));
            Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());

            ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTimeRestriction.getQTimeRestrictionInfo();

            if (aQTimeRestrictionInfo == null || CimStringUtils.length(aQTimeRestrictionInfo.getTriggerOperationNumber()) < 1) {
                log.info("" + "The target lot's Q-Time information was not found.");
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            }

            if (ObjectIdentifier.isEmpty(aQTimeRestrictionInfo.getWaferID())) {
                log.info("" + "Wafer ID of the Q-Time is empty");

                aLot.removeQTimeRestriction(aQTimeRestriction);

            } else {
                log.info("" + "Wafer ID of the Q-Time is not empty");
                com.fa.cim.newcore.bo.product.CimWafer aPosWafer;
                aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, aQTimeRestrictionInfo.getWaferID());

                aPosWafer.removeQTimeRestriction(aQTimeRestriction);

            }

            qtimeRestrictionManager.removeQTimeRestriction(aQTimeRestriction);

            Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
            strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
            strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
            strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
            strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
            strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
            strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
            strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
            strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
            strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
            strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
            strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
            strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
            strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
            strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
            strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
            strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
            strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());

            if (TRUE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                log.info("" + "TRUE == aQTimeRestrictionInfo.getWatchdogRequired()");
                strQtimeInfo.setWatchDogRequired("Y");
            } else if (FALSE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                log.info("" + "FALSE == aQTimeRestrictionInfo.getWatchdogRequired()");
                strQtimeInfo.setWatchDogRequired("N");
            }
            if (TRUE.equals(aQTimeRestrictionInfo.getActionDone())) {
                log.info("" + "TRUE == aQTimeRestrictionInfo.getActionDone()");
                strQtimeInfo.setActionDoneFlag("Y");
            } else if (FALSE == aQTimeRestrictionInfo.getActionDone()) {
                log.info("" + "FALSE == aQTimeRestrictionInfo.getActionDone()");
                strQtimeInfo.setActionDoneFlag("N");
            }
            strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
            int actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
            log.info("" + "aQTimeRestrictionInfo.getArrayUtils().getSize(actions)" + actionLength);
            if (actionLength != 0) {
                log.info("" + "actionLength != 0");
                strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                for (int iCnt1 = 0; iCnt1 < actionLength; iCnt1++) {
                    log.info("" + "loop to aQTimeRestrictionInfo.getArrayUtils().getSize(actions)" + iCnt1);
                    strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(iCnt1).getTargetTimeStamp());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(iCnt1).getAction());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setReasonCodeID(aQTimeRestrictionInfo.getActions().get(iCnt1).getReasonCode());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setActionRouteID(aQTimeRestrictionInfo.getActions().get(iCnt1).getActionRouteID());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(iCnt1).getOperationNumber());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(iCnt1).getTiming());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setReworkRouteID(aQTimeRestrictionInfo.getActions().get(iCnt1).getMainProcessDefinition());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setMessageID(aQTimeRestrictionInfo.getActions().get(iCnt1).getMessageDefinition());
                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setCustomField(aQTimeRestrictionInfo.getActions().get(iCnt1).getCustomField());
                    if (TRUE == aQTimeRestrictionInfo.getActions().get(iCnt1).getWatchdogRequired()) {
                        log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt1).getWatchdogRequired()");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setWatchDogRequired("Y");
                    } else if (FALSE == aQTimeRestrictionInfo.getActions().get(iCnt1).getWatchdogRequired()) {
                        log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt1).getWatchdogRequired()");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setWatchDogRequired("N");
                    }
                    if (TRUE == aQTimeRestrictionInfo.getActions().get(iCnt1).getActionDone()) {
                        log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt1).getActionDone()");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setActionDoneFlag("Y");
                    } else if (FALSE == aQTimeRestrictionInfo.getActions().get(iCnt1).getActionDone()) {
                        log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt1).getActionDone()");
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt1).setActionDoneFlag("N");
                    }
                }
            } else {
                log.info("" + "actionLength == 0");
                strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
            }

            RetCode<Object> strQTimeChangeEventMakeOut;

            Inputs.QTimeChangeEventMakeParams strQTimeChangeEventMakeIn = new Inputs.QTimeChangeEventMakeParams();
            strQTimeChangeEventMakeIn.setUpdateMode(SP_QRESTTIME_OPECATEGORY_DELETE);
            strQTimeChangeEventMakeIn.setLotID(lotID);
            strQTimeChangeEventMakeIn.setQtimeInfo(strQtimeInfo);
            strQTimeChangeEventMakeIn.setClaimMemo("");

            // call qTime_ChangeEvent_Make
            eventMethod.qTimeChangeEventMake(
                    strObjCommonIn,
                    strQTimeChangeEventMakeIn);
        }

        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeq;

        int qtimeSeqForWaferCount = 0;
        qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();

        if (qtimeSeqForWaferCount > 0) {
            log.info("" + "Exist WaferLevelQTime");
            qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();

        } else {
            log.info("" + "Not exist WaferLevelQTime");
            qtimeSeq = aLot.allQTimeRestrictions();

        }

        int QTSLength = CimArrayUtils.getSize(qtimeSeq);
        log.info("" + "qtimeSeq.length()" + QTSLength);
        for (int iCnt2 = 0; iCnt2 < QTSLength; iCnt2++) {
            log.info("" + "loop to qtimeSeq.length()" + iCnt2);
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qtimeSeq.get(iCnt2);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());

            Boolean isManualCreatedFlag = FALSE;
            isManualCreatedFlag = aQTime.isManualCreated();

            if (isManualCreatedFlag) {
                log.info("" + "TRUE == isManualCreatedFlag");
                aQTime.makeNotManualCreated();

            }

            Boolean preTrigger = FALSE;
            preTrigger = aQTime.isPreTrigger();

            if (preTrigger) {
                log.info("" + "Make preTrigger FALSE.");
                aQTime.makeNotPreTrigger();

            }

        }

        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext;
        aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());
        CimProductSpecification aTmpProductSpecification;
        aTmpProductSpecification = aLot.getProductSpecification();

        CimProductSpecification aProductSpecification;
        aProductSpecification = aTmpProductSpecification;

        List<ProcessDTO.TimeRestrictionSpecification> aTimeRSSequence = null;
        List<ProcessDTO.TimeRestrictionSpecification> aTmpTRSSeq;

        aTimeRSSequence = aProcessOperation.findTimeRestrictionsForProduct(aProductSpecification);

        aTmpTRSSeq = aTimeRSSequence;

        int nLen = CimArrayUtils.getSize(aTimeRSSequence);
        if (nLen != 0) {
            Timestamp triggerTimeStampVar = triggerTimeStamp;
            String targetTimeStamp = null;

            List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();

            List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();

            if (TRUE == strQtimeLotSetClearByOperationCompIn.getPreviousOperationFlag()) {

                log.info("" + "previousOperationFlag = TRUE");

                String previousBranchInfo;
                previousBranchInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);

                String previousReturnInfo;
                previousReturnInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

                if (0 < CimStringUtils.length(previousBranchInfo)) {
                    log.info("" + "previousBranchInfo" + previousBranchInfo);

                    List<String> previousTokens = CimArrayUtils.generateList(previousBranchInfo.split("\\" + SP_KEY_SEPARATOR_DOT));
                    int previousTokensLen = CimArrayUtils.getSize(previousTokens);
                    if (4 == previousTokensLen) {
                        log.info("" + "previousTokensLen = 4");

                        List<String> strings;
                        strings = new ArrayList<>();
                        strings.add(previousTokens.get(0));
                        strings.add(previousTokens.get(1));
                        String previousBranchRouteIdentifier = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);
                        strings.set(0, previousTokens.get(2));
                        strings.set(1, previousTokens.get(3));
                        String previousBranchOpeNo = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);

                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                        ObjectIdentifier aRouteID = new ObjectIdentifier();
                        aRouteID.setValue(previousBranchRouteIdentifier);
                        aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, aRouteID);

                        ProcessDTO.BranchInfo previousBranchInfo1 = new ProcessDTO.BranchInfo();
                        previousBranchInfo1.setRouteID(ObjectIdentifier.build(
                                previousBranchRouteIdentifier,
                                aPosPD.getPrimaryKey()
                        ));
                        previousBranchInfo1.setOperationNumber(previousBranchOpeNo);
                        previousBranchInfo1.setProcessOperation("");
                        previousBranchInfo1.setReworkOutKey("");

                        String previousReworkOutKey;
                        previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);

                        if (0 < CimStringUtils.length(previousReworkOutKey)) {
                            log.info("" + "previousReworkOutKey" + previousReworkOutKey);
                            previousBranchInfo1.setReworkOutKey(previousReworkOutKey);
                        }

                        int nSeqLen = CimArrayUtils.getSize(branchInfoSeq);
                        branchInfoSeq.add(previousBranchInfo1);
                    }
                }

                if (0 < CimStringUtils.length(previousReturnInfo)) {
                    log.info("" + "previousReturnInfo" + previousReturnInfo);

                    String[] previousTokens = previousReturnInfo.split("\\" + SP_KEY_SEPARATOR_DOT);
                    int previousTokensLen = previousTokens.length;
                    if (4 == previousTokensLen) {
                        log.info("" + "previousTokensLen = 4");

                        String[] strings = new String[2];
                        strings[0] = (previousTokens[0]);
                        strings[1] = (previousTokens[1]);
                        String previousReturnRouteIdentifier = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);
                        strings[0] = previousTokens[2];
                        strings[1] = previousTokens[3];
                        String previousReturnOpeNo = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);

                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                        ObjectIdentifier aRouteID = new ObjectIdentifier();
                        aRouteID.setValue(previousReturnRouteIdentifier);
                        aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, aRouteID);

                        ProcessDTO.ReturnInfo previousReturnInfo1 = new ProcessDTO.ReturnInfo();
                        previousReturnInfo1.setRouteID(ObjectIdentifier.build(previousReturnRouteIdentifier,
                                aPosPD.getPrimaryKey()));
                        previousReturnInfo1.setOperationNumber(previousReturnOpeNo);
                        previousReturnInfo1.setProcessFlow("");
                        previousReturnInfo1.setMainProcessFlow("");
                        previousReturnInfo1.setModuleProcessFlow("");

                        int nSeqLen = CimArrayUtils.getSize(returnInfoSeq);
                        returnInfoSeq.add(previousReturnInfo1);
                    }
                }
            }

            String branchInfo;
            branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoSeq);

            String returnInfo;
            returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoSeq);

            for (i = 0; i < nLen; i++) {

                if (CimStringUtils.equals(aTimeRSSequence.get(i).getDefaultTimeRS().getQTimeType(), SP_QTIMETYPE_BYLOT)) {
                    log.info("" + "QTimeType != SP_QTIMETYPE_BYLOT + continue");
                    continue;
                }

                ObjectIdentifier oiMainPD;
                oiMainPD = ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
                String[] stsOrgQTime = new String[4];
                stsOrgQTime[0] = oiMainPD.getValue();
                stsOrgQTime[1] = operationNumber;
                stsOrgQTime[2] = oiMainPD.getValue();
                stsOrgQTime[3] = aTimeRSSequence.get(i).getDefaultTimeRS().getTargetOperationNumber();
                String originalQTime = CimStringUtils.join(stsOrgQTime, SP_KEY_SEPARATOR_DOT);

                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForLot;
                aQTimeRestrictionForLot = aLot.findQTimeRestrictionByOriginalQTime(originalQTime);

                if (aQTimeRestrictionForLot != null) {
                    log.info("" + "There is duplicate Q-Time timer" + originalQTime);
                    continue;
                }

                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction;
                aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());

                aLot.addQTimeRestriction(aQTimeRestriction);

                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();
                ObjectIdentifier waferID = null;

                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                aQTimeRestrictionInfo.setQTimeType(aTimeRSSequence.get(i).getDefaultTimeRS().getQTimeType());
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setPreTrigger(FALSE);
                aQTimeRestrictionInfo.setTriggerOperationNumber(operationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(CimDateUtils.getTimestampAsString(triggerTimeStampVar));
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                aQTimeRestrictionInfo.setTargetOperationNumber(aTimeRSSequence.get(i).getDefaultTimeRS().getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setWatchdogRequired(TRUE);
                aQTimeRestrictionInfo.setActionDone(FALSE);
                aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                aQTimeRestrictionInfo.setProcessDefinitionLevel(aTimeRSSequence.get(i).getDefaultTimeRS().getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setManualCreated(FALSE);

                log.info("" + "aQTimeRestrictionInfo.getQTimeType()" + aQTimeRestrictionInfo.getQTimeType());
                log.info("" + "aQTimeRestrictionInfo.getLotID().getIdentifier()" + aQTimeRestrictionInfo.getLotID().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTriggerMainProcessDefinition()" + aQTimeRestrictionInfo.getTriggerMainProcessDefinition().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTriggerOperationNumber()" + aQTimeRestrictionInfo.getTriggerOperationNumber());
                log.info("" + "aQTimeRestrictionInfo.getTriggerBranchInfo()" + aQTimeRestrictionInfo.getTriggerBranchInfo());
                log.info("" + "aQTimeRestrictionInfo.getTriggerReturnInfo()" + aQTimeRestrictionInfo.getTriggerReturnInfo());
                log.info("" + "aQTimeRestrictionInfo.getTriggerTimeStamp()" + aQTimeRestrictionInfo.getTriggerTimeStamp());
                log.info("" + "aQTimeRestrictionInfo.getTargetMainProcessDefinition()" + aQTimeRestrictionInfo.getTargetMainProcessDefinition().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTargetOperationNumber()" + aQTimeRestrictionInfo.getTargetOperationNumber());
                log.info("" + "aQTimeRestrictionInfo.getTargetBranchInfo()" + aQTimeRestrictionInfo.getTargetBranchInfo());
                log.info("" + "aQTimeRestrictionInfo.getTargetReturnInfo()" + aQTimeRestrictionInfo.getTargetReturnInfo());
                log.info("" + "aQTimeRestrictionInfo.getWatchdogRequired()" + aQTimeRestrictionInfo.getWatchdogRequired());
                log.info("" + "aQTimeRestrictionInfo.getActionDone()" + aQTimeRestrictionInfo.getActionDone());
                log.info("" + "aQTimeRestrictionInfo.getOriginalQTime()" + aQTimeRestrictionInfo.getOriginalQTime());
                log.info("" + "aQTimeRestrictionInfo.getProcessDefinitionLevel()" + aQTimeRestrictionInfo.getProcessDefinitionLevel());

                int actionLength = CimArrayUtils.getSize(aTimeRSSequence.get(i).getDefaultTimeRS().getActions());
                log.info("" + "(*aTimeRSSequence).getArrayUtils().getSize(actions)" + i + actionLength);

                String mostUrgentTargetTimeAction = null;

                Boolean setMostUrgentTimeFlag = FALSE;
                if (SP_DISPATCH_PRECEDE_NOT_FOUND * 1. == aTimeRSSequence.get(i).getDefaultTimeRS().getExpiredTimeDuration()) {
                    setMostUrgentTimeFlag = TRUE;
                } else {
                    targetTimeStamp = CimDateUtils.getTimestampAsString(new Timestamp((long) (triggerTimeStamp.getTime() + aTimeRSSequence.get(i).getDefaultTimeRS().getExpiredTimeDuration())));
                }
                log.info("" + "#### targetTime                 = " + targetTimeStamp);

                if (actionLength != 0) {
                    log.info("" + "(*aTimeRSSequence).get(i).getArrayUtils().getSize(actions)" + actionLength);
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                    int actionCnt = 0;
                    for (int counter = 0; counter < actionLength; counter++) {

                        if (0 == qtimeDispatchPrecedeUseCustomFieldFlag && CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getAction())) {
                            log.info("" + "CustomField for DispatchPrecede is not used and action is DispatchPrecede");
                            continue;
                        }

                        String targetTimeStampForAction;
                        targetTimeStampForAction = CimDateUtils.getTimestampAsString(new Timestamp((long) (triggerTimeStamp.getTime() + aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getExpiredTimeDuration())));

                        if (setMostUrgentTimeFlag) {
                            if (CimStringUtils.length(mostUrgentTargetTimeAction) == 0) {

                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {

                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {

                                }
                            }
                        }
                        aQTimeRestrictionInfo.getActions().add(new ProcessDTO.QTimeRestrictionAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTargetTimeStamp(targetTimeStampForAction);
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setAction(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setReasonCode(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getReasonCode());

                        if (0 < CimStringUtils.length(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getOperationNumber())) {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(oiMainPD);
                        }

                        aQTimeRestrictionInfo.getActions().get(actionCnt).setOperationNumber(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getOperationNumber());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTiming(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getTiming());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMainProcessDefinition(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getMainProcessDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMessageDefinition(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getMessageDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setCustomField(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getCustomField());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setWatchdogRequired(TRUE);

                        if (CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(counter).getAction())) {
                            log.info("" + "Action is DispatchPrecede");
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(TRUE);
                        } else {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(FALSE);
                        }
                        actionCnt++;

                    }
                    //aQTimeRestrictionInfo.setActions(new ArrayList<>());
                } else {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                }

                if (CimStringUtils.length(mostUrgentTargetTimeAction) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                    log.info("" + "aQTimeRestrictionInfo.getTargetTimeStamp()" + aQTimeRestrictionInfo.getTargetTimeStamp());
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                    log.info("" + "aQTimeRestrictionInfo.getTargetTimeStamp()" + aQTimeRestrictionInfo.getTargetTimeStamp());
                }

                log.info("" + "aQTimeRestriction.getSetQTimeRestrictionInfo()");
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                if (TRUE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("" + "TRUE == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (FALSE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("" + "FALSE == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("N");
                }
                if (TRUE.equals(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("" + "TRUE == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (FALSE.equals(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("" + "FALSE == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("N");
                }
                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                log.info("" + "ArrayUtils.getSize(aQTimeRestrictionInfo.getActions())" + actionLength);
                if (actionLength != 0) {
                    log.info("" + "actionLength != 0");
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                        log.info("" + "ArrayUtils.getSize(loop to aQTimeRestrictionInfo.getActions())" + iCnt3);
                        strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(iCnt3).getTargetTimeStamp());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(iCnt3).getAction());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID(aQTimeRestrictionInfo.getActions().get(iCnt3).getReasonCode());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionRouteID());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(iCnt3).getOperationNumber());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(iCnt3).getTiming());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMainProcessDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID(aQTimeRestrictionInfo.getActions().get(iCnt3).getMessageDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField(aQTimeRestrictionInfo.getActions().get(iCnt3).getCustomField());
                        if (TRUE.equals(aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                            log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("Y");
                        } else if (FALSE.equals(aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                            log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt3).getWatchdogRequired()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("N");
                        }
                        if (TRUE.equals(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone())) {
                            log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("Y");
                        } else if (FALSE.equals(aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone())) {
                            log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(iCnt3).getActionDone()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("N");
                        }
                    }
                } else {
                    log.info("" + "actionLength == 0");
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                }

                Inputs.QTimeChangeEventMakeParams strQTimeChangeEventMakeIn = new Inputs.QTimeChangeEventMakeParams();
                strQTimeChangeEventMakeIn.setUpdateMode(SP_QRESTTIME_OPECATEGORY_CREATE);
                strQTimeChangeEventMakeIn.setLotID(lotID);
                strQTimeChangeEventMakeIn.setQtimeInfo(strQtimeInfo);
                strQTimeChangeEventMakeIn.setClaimMemo("");

                // call qTimeChangeEventMake_180
                eventMethod.qTimeChangeEventMake(
                        strObjCommonIn,
                        strQTimeChangeEventMakeIn);

            }

            List<ProductDTO.WaferInfo> waferInfoSeq = aLot.getAllWaferInfo();

            for (i = 0; i < nLen; i++) {
                if (CimStringUtils.equals(aTimeRSSequence.get(i).getDefaultTimeRS().getQTimeType(), SP_QTIMETYPE_BYWAFER)) {
                    log.info("" + "QTimeType != SP_QTIMETYPE_BYWAFER + continue");
                    continue;
                }

                ObjectIdentifier oiMainPD;
                oiMainPD = ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
                String[] stsOrgQTime = new String[4];
                stsOrgQTime[0] = oiMainPD.getValue();
                stsOrgQTime[1] = (operationNumber);
                stsOrgQTime[2] = oiMainPD.getValue();
                stsOrgQTime[3] = (aTimeRSSequence.get(i).getDefaultTimeRS().getTargetOperationNumber());
                String originalQTime = CimStringUtils.join(stsOrgQTime, SP_KEY_SEPARATOR_DOT);

                Boolean allWaferFlag = TRUE;
                for (int j = 0; j < CimArrayUtils.getSize(waferInfoSeq); j++) {
                    com.fa.cim.newcore.bo.product.CimWafer aPosWafer;
                    aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class,
                            waferInfoSeq.get(j).getWaferID());

                    com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestrictionForWafer;
                    aQTimeRestrictionForWafer = aPosWafer.findQTimeRestrictionByOriginalQTime(originalQTime);

                    if (aQTimeRestrictionForWafer == null) {
                        log.info("" + "aQTimeRestrictionForWafer!= TRUE");
                        allWaferFlag = null;
                        break;
                    }
                }
                if (TRUE.equals(allWaferFlag)) {
                    log.info("" + "All Wafer's Q-Time triggered");
                    continue;
                }

                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTimeRestriction;
                aQTimeRestriction = qtimeRestrictionManager.createQTimeRestriction();
                Validations.check(aQTimeRestriction == null, retCodeConfig.getNotFoundSystemObj());

                log.info("" + "aLot.getAddQTimeRestriction()");
                aLot.addQTimeRestriction(aQTimeRestriction);

                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = new ProcessDTO.QTimeRestrictionInfo();

                ObjectIdentifier waferID = null;
                aQTimeRestrictionInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerMainProcessDefinition(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                aQTimeRestrictionInfo.setTriggerOperationNumber(operationNumber);
                aQTimeRestrictionInfo.setTriggerBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTriggerReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setTriggerTimeStamp(CimDateUtils.getTimestampAsString(triggerTimeStampVar));
                aQTimeRestrictionInfo.setTargetMainProcessDefinition(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                aQTimeRestrictionInfo.setTargetOperationNumber(aTimeRSSequence.get(i).getDefaultTimeRS().getTargetOperationNumber());
                aQTimeRestrictionInfo.setTargetBranchInfo(branchInfo);
                aQTimeRestrictionInfo.setTargetReturnInfo(returnInfo);
                aQTimeRestrictionInfo.setWatchdogRequired(TRUE);
                aQTimeRestrictionInfo.setActionDone(null);
                aQTimeRestrictionInfo.setOriginalQTime(originalQTime);
                aQTimeRestrictionInfo.setProcessDefinitionLevel(aTimeRSSequence.get(i).getDefaultTimeRS().getProcessDefinitionLevel());
                aQTimeRestrictionInfo.setManualCreated(FALSE);
                aQTimeRestrictionInfo.setQTimeType(SP_QTIMETYPE_BYLOT);
                aQTimeRestrictionInfo.setWaferID(waferID);
                aQTimeRestrictionInfo.setPreTrigger(FALSE);

                log.info("" + "aQTimeRestrictionInfo.getLotID().getIdentifier()" + aQTimeRestrictionInfo.getLotID().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTriggerMainProcessDefinition()" + aQTimeRestrictionInfo.getTriggerMainProcessDefinition().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTriggerOperationNumber()" + aQTimeRestrictionInfo.getTriggerOperationNumber());
                log.info("" + "aQTimeRestrictionInfo.getTriggerBranchInfo()" + aQTimeRestrictionInfo.getTriggerBranchInfo());
                log.info("" + "aQTimeRestrictionInfo.getTriggerReturnInfo()" + aQTimeRestrictionInfo.getTriggerReturnInfo());
                log.info("" + "aQTimeRestrictionInfo.getTriggerTimeStamp()" + aQTimeRestrictionInfo.getTriggerTimeStamp());
                log.info("" + "aQTimeRestrictionInfo.getTargetMainProcessDefinition()" + aQTimeRestrictionInfo.getTargetMainProcessDefinition().getValue());
                log.info("" + "aQTimeRestrictionInfo.getTargetOperationNumber()" + aQTimeRestrictionInfo.getTargetOperationNumber());
                log.info("" + "aQTimeRestrictionInfo.getTargetBranchInfo()" + aQTimeRestrictionInfo.getTargetBranchInfo());
                log.info("" + "aQTimeRestrictionInfo.getTargetReturnInfo()" + aQTimeRestrictionInfo.getTargetReturnInfo());
                log.info("" + "aQTimeRestrictionInfo.getWatchdogRequired()" + aQTimeRestrictionInfo.getWatchdogRequired());
                log.info("" + "aQTimeRestrictionInfo.getActionDone()" + aQTimeRestrictionInfo.getActionDone());
                log.info("" + "aQTimeRestrictionInfo.getOriginalQTime()" + aQTimeRestrictionInfo.getOriginalQTime());
                log.info("" + "aQTimeRestrictionInfo.getProcessDefinitionLevel()" + aQTimeRestrictionInfo.getProcessDefinitionLevel());
                log.info("" + "aQTimeRestrictionInfo.getQTimeType()" + aQTimeRestrictionInfo.getQTimeType());

                int actionLength = CimArrayUtils.getSize(aTimeRSSequence.get(i).getDefaultTimeRS().getActions());
                log.info("" + "(*aTimeRSSequence).getArrayUtils().getSize(actions)" + i + actionLength);

                String mostUrgentTargetTimeAction = null;

                Boolean setMostUrgentTimeFlag = FALSE;
                if (SP_DISPATCH_PRECEDE_NOT_FOUND * 1.0 == aTimeRSSequence.get(i).getDefaultTimeRS().getExpiredTimeDuration()) {
                    setMostUrgentTimeFlag = TRUE;
                } else {
                    targetTimeStamp = CimDateUtils.getTimestampAsString(new Timestamp(
                            (long) (triggerTimeStamp.getTime() + aTimeRSSequence.get(i).getDefaultTimeRS().getExpiredTimeDuration())
                    ));
                }
                log.info("" + "#### targetTime                 = " + targetTimeStamp);

                if (actionLength != 0) {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                    int actionCnt = 0;
                    for (int j = 0; j < actionLength; j++) {

                        if (0 == qtimeDispatchPrecedeUseCustomFieldFlag && CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getAction())) {
                            log.info("" + "CustomField for DispatchPrecede is not used and action is DispatchPrecede");
                            continue;
                        }

                        String targetTimeStampForAction;
                        targetTimeStampForAction = CimDateUtils.getTimestampAsString(new Timestamp(
                                (long) (triggerTimeStamp.getTime() + aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getExpiredTimeDuration())
                        ));

                        if (TRUE.equals(setMostUrgentTimeFlag)) {
                            if (CimStringUtils.length(mostUrgentTargetTimeAction) == 0) {
                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                            } else {
                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                } else {

                                }
                            }
                        }
                        aQTimeRestrictionInfo.getActions().add(new ProcessDTO.QTimeRestrictionAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTargetTimeStamp(targetTimeStampForAction);
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setAction(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getAction());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setReasonCode(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getReasonCode());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setOperationNumber(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getOperationNumber());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setTiming(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getTiming());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMainProcessDefinition(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getMainProcessDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setMessageDefinition(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getMessageDefinition());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setCustomField(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getCustomField());
                        aQTimeRestrictionInfo.getActions().get(actionCnt).setWatchdogRequired(TRUE);
                        if (0 < CimStringUtils.length(aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getOperationNumber())) {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionRouteID(oiMainPD);
                        }

                        if (CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aTimeRSSequence.get(i).getDefaultTimeRS().getActions().get(j).getAction())) {
                            log.info("" + "Action is DispatchPrecede");
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(TRUE);
                        } else {
                            aQTimeRestrictionInfo.getActions().get(actionCnt).setActionDone(FALSE);
                        }
                        actionCnt++;
                    }
                    //aQTimeRestrictionInfo.setActions(new ArrayList<>());
                } else {
                    aQTimeRestrictionInfo.setActions(new ArrayList<>());
                }

                if (CimStringUtils.length(mostUrgentTargetTimeAction) != 0) {
                    aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                    log.info("" + "aQTimeRestrictionInfo.getTargetTimeStamp()" + aQTimeRestrictionInfo.getTargetTimeStamp());
                } else {
                    aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                    log.info("" + "aQTimeRestrictionInfo.getTargetTimeStamp()" + aQTimeRestrictionInfo.getTargetTimeStamp());
                }

                log.info("" + "aQTimeRestriction.getSetQTimeRestrictionInfo()");
                aQTimeRestriction.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                strQtimeInfo.setQTimeType(aQTimeRestrictionInfo.getQTimeType());
                strQtimeInfo.setWaferID(aQTimeRestrictionInfo.getWaferID());
                strQtimeInfo.setPreTrigger(aQTimeRestrictionInfo.getPreTrigger());
                strQtimeInfo.setOriginalQTime(aQTimeRestrictionInfo.getOriginalQTime());
                strQtimeInfo.setProcessDefinitionLevel(aQTimeRestrictionInfo.getProcessDefinitionLevel());
                strQtimeInfo.setRestrictionTriggerRouteID(aQTimeRestrictionInfo.getTriggerMainProcessDefinition());
                strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeRestrictionInfo.getTriggerOperationNumber());
                strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeRestrictionInfo.getTriggerBranchInfo());
                strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeRestrictionInfo.getTriggerReturnInfo());
                strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeRestrictionInfo.getTriggerTimeStamp());
                strQtimeInfo.setRestrictionTargetRouteID(aQTimeRestrictionInfo.getTargetMainProcessDefinition());
                strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeRestrictionInfo.getTargetOperationNumber());
                strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeRestrictionInfo.getTargetBranchInfo());
                strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeRestrictionInfo.getTargetReturnInfo());
                strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeRestrictionInfo.getTargetTimeStamp());
                strQtimeInfo.setPreviousTargetInfo(aQTimeRestrictionInfo.getPreviousTargetInfo());
                strQtimeInfo.setSpecificControl(aQTimeRestrictionInfo.getControl());
                if (TRUE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("" + "TRUE == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("Y");
                } else if (FALSE.equals(aQTimeRestrictionInfo.getWatchdogRequired())) {
                    log.info("" + "FALSE == aQTimeRestrictionInfo.getWatchdogRequired()");
                    strQtimeInfo.setWatchDogRequired("N");
                }
                if (TRUE.equals(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("" + "TRUE == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("Y");
                } else if (FALSE.equals(aQTimeRestrictionInfo.getActionDone())) {
                    log.info("" + "FALSE == aQTimeRestrictionInfo.getActionDone()");
                    strQtimeInfo.setActionDoneFlag("N");
                }
                strQtimeInfo.setManualCreated(aQTimeRestrictionInfo.getManualCreated());
                actionLength = CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions());
                log.info("" + "ArrayUtils.getSize(aQTimeRestrictionInfo.getActions())" + actionLength);
                if (actionLength != 0) {
                    log.info("" + "actionLength != 0");
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                    for (int j = 0; j < actionLength; j++) {
                        log.info("" + "ArrayUtils.getSize(loop to aQTimeRestrictionInfo.getActions())" + j);
                        strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setQrestrictionTargetTimeStamp(aQTimeRestrictionInfo.getActions().get(j).getTargetTimeStamp());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setQrestrictionAction(aQTimeRestrictionInfo.getActions().get(j).getAction());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setReasonCodeID(aQTimeRestrictionInfo.getActions().get(j).getReasonCode());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setActionRouteID(aQTimeRestrictionInfo.getActions().get(j).getActionRouteID());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setActionOperationNumber(aQTimeRestrictionInfo.getActions().get(j).getOperationNumber());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setFutureHoldTiming(aQTimeRestrictionInfo.getActions().get(j).getTiming());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setReworkRouteID(aQTimeRestrictionInfo.getActions().get(j).getMainProcessDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setMessageID(aQTimeRestrictionInfo.getActions().get(j).getMessageDefinition());
                        strQtimeInfo.getStrQtimeActionInfoList().get(j).setCustomField(aQTimeRestrictionInfo.getActions().get(j).getCustomField());
                        if (TRUE.equals(aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired())) {
                            log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(j).setWatchDogRequired("Y");
                        } else if (FALSE.equals(aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired())) {
                            log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(j).getWatchdogRequired()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(j).setWatchDogRequired("N");
                        }
                        if (TRUE.equals(aQTimeRestrictionInfo.getActions().get(j).getActionDone())) {
                            log.info("" + "TRUE == aQTimeRestrictionInfo.getActions().get(j).getActionDone()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(j).setActionDoneFlag("Y");
                        } else if (FALSE.equals(aQTimeRestrictionInfo.getActions().get(j).getActionDone())) {
                            log.info("" + "FALSE == aQTimeRestrictionInfo.getActions().get(j).getActionDone()");
                            strQtimeInfo.getStrQtimeActionInfoList().get(j).setActionDoneFlag("N");
                        }
                    }
                } else {
                    log.info("" + "actionLength == 0");
                    strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                }

                Inputs.QTimeChangeEventMakeParams strQTimeChangeEventMakeIn = new Inputs.QTimeChangeEventMakeParams();
                strQTimeChangeEventMakeIn.setUpdateMode(SP_QRESTTIME_OPECATEGORY_CREATE);
                strQTimeChangeEventMakeIn.setLotID(lotID);
                strQTimeChangeEventMakeIn.setQtimeInfo(strQtimeInfo);
                strQTimeChangeEventMakeIn.setClaimMemo("");
                // call qTimeChangeEventMake_180
                eventMethod.qTimeChangeEventMake(
                        strObjCommonIn,
                        strQTimeChangeEventMakeIn);
            }

        }

        if (CimStringUtils.equals(processFlowType, SP_FLOWTYPE_SUB)) {
            log.info("" + "StringUtils.equals( processFlowType+ SP_FLOWTYPE_SUB)");

            Inputs.QTimeTriggerOpeReplaceIn strQTimeTriggerOpeReplaceIn = new Inputs.QTimeTriggerOpeReplaceIn();
            strQTimeTriggerOpeReplaceIn.setLotID(lotID);

            // call qTimeTriggerOpeReplace
            qTimeTriggerOpeReplace(
                    strObjCommonIn,
                    strQTimeTriggerOpeReplaceIn);
        }

        log.info("qtimeLotSetClearByOperationComp");
        return strQtimeLotSetClearByOperationCompOut;

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strLot_qtimeInfo_GetForClearByProcessOperation_in
     * @return com.fa.cim.dto.Infos.LotQtimeInfoGetForClearByProcessOperationOut
     * @throws
     * @author Ho
     * @date 2019/8/28 13:30
     */
    public Infos.LotQtimeInfoGetForClearByProcessOperationOut lotQtimeInfoGetForClearByProcessOperation(
            Infos.ObjCommon strObjCommonIn,
            Infos.LotQtimeInfoGetForClearByProcessOperationIn strLot_qtimeInfo_GetForClearByProcessOperation_in) {
        ObjectIdentifier lotID = strLot_qtimeInfo_GetForClearByProcessOperation_in.getLotID();

        com.fa.cim.newcore.bo.product.CimLot aLot;
        aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);

        com.fa.cim.newcore.bo.pd.CimProcessOperation aTmpProcessOperation;
        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation;
        if (TRUE.equals(strLot_qtimeInfo_GetForClearByProcessOperation_in.getPreviousOperationFlag())) {
            aTmpProcessOperation = aLot.getPreviousProcessOperation();
            aProcessOperation = aTmpProcessOperation;
        } else {
            aTmpProcessOperation = aLot.getProcessOperation();
            aProcessOperation = aTmpProcessOperation;
        }
        Validations.check(aProcessOperation == null, retCodeConfig.getNotFoundOperation());
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aRoute;
        aRoute = aProcessOperation.getMainProcessDefinition();
        Validations.check(aRoute == null, retCodeConfig.getNotFoundMainRoute());
        String routeID;
        routeID = aRoute.getIdentifier();

        String operationNumber;
        operationNumber = aProcessOperation.getOperationNumber();

        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext;
        aProcessFlowContext = aLot.getProcessFlowContext();
        Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());
        List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();

        List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();

        if (TRUE.equals(strLot_qtimeInfo_GetForClearByProcessOperation_in.getPreviousOperationFlag())) {

            String previousBranchInfo;
            previousBranchInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);

            String previousReturnInfo;
            previousReturnInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

            if (0 < CimStringUtils.length(previousBranchInfo)) {
                String[] previousTokens = previousBranchInfo.split("\\" + SP_KEY_SEPARATOR_DOT);
                int previousTokensLen = previousTokens.length;
                if (4 == previousTokensLen) {
                    String[] strings;
                    strings = new String[2];
                    strings[0] = previousTokens[0];
                    strings[1] = previousTokens[1];
                    String previousBranchRouteIdentifier = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens[2];
                    strings[1] = previousTokens[3];
                    String previousBranchOpeNo = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);

                    com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                    ObjectIdentifier aRouteID = new ObjectIdentifier();
                    aRouteID.setValue(previousBranchRouteIdentifier);

                    aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, aRouteID);

                    ProcessDTO.BranchInfo previousBranchInfo1 = new ProcessDTO.BranchInfo();
                    previousBranchInfo1.setRouteID(ObjectIdentifier.build(previousBranchRouteIdentifier,
                            aPosPD.getPrimaryKey()));
                    previousBranchInfo1.setOperationNumber(previousBranchOpeNo);
                    previousBranchInfo1.setProcessOperation("");
                    previousBranchInfo1.setReworkOutKey("");

                    String previousReworkOutKey;
                    previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);

                    if (0 < CimStringUtils.length(previousReworkOutKey)) {
                        previousBranchInfo1.setReworkOutKey(previousReworkOutKey);
                    }

                    int nSeqLen = CimArrayUtils.getSize(branchInfoSeq);
                    branchInfoSeq.add(previousBranchInfo1);
                }
            }

            if (0 < CimStringUtils.length(previousReturnInfo)) {

                String[] previousTokens = previousReturnInfo.split("\\" + SP_KEY_SEPARATOR_DOT);
                int previousTokensLen = previousTokens.length;
                if (4 == previousTokensLen) {
                    String[] strings;
                    strings = new String[2];
                    strings[0] = previousTokens[0];
                    strings[1] = previousTokens[1];
                    String previousReturnRouteIdentifier = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);
                    strings[0] = previousTokens[2];
                    strings[1] = previousTokens[3];
                    String previousReturnOpeNo = CimStringUtils.join(strings, SP_KEY_SEPARATOR_DOT);

                    ProcessDefinition aPosPD;
                    ObjectIdentifier aRouteID = ObjectIdentifier.buildWithValue(
                            previousReturnRouteIdentifier
                    );

                    aPosPD = baseCoreFactory.getBO(ProcessDefinition.class, aRouteID);

                    ProcessDTO.ReturnInfo previousReturnInfo1 = new ProcessDTO.ReturnInfo();
                    previousReturnInfo1.setRouteID(ObjectIdentifier.build(
                            previousReturnRouteIdentifier,
                            aPosPD.getPrimaryKey()
                    ));
                    previousReturnInfo1.setOperationNumber(previousReturnOpeNo);
                    previousReturnInfo1.setProcessFlow("");
                    previousReturnInfo1.setMainProcessFlow("");
                    previousReturnInfo1.setModuleProcessFlow("");

                    int nSeqLen = CimArrayUtils.getSize(returnInfoSeq);
                    returnInfoSeq.add(previousReturnInfo1);
                }
            }
        }

        String branchInfo;
        branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute(branchInfoSeq);

        String returnInfo;
        returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute(returnInfoSeq);

        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeList;

        int aQTimeListForWaferCount = 0;
        aQTimeListForWaferCount = aLot.getWaferLevelQTimeCount();

        if (aQTimeListForWaferCount > 0) {
            aQTimeList = aLot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            aQTimeList = aLot.allQTimeRestrictions();
        }

        int qTimeLen = CimArrayUtils.getSize(aQTimeList);

        int qTimeClearCnt = 0;
        List<String> qTimeClearList = new ArrayList<>();

        Map<String, Infos.LotHoldReq> tmpLotHoldRemainList = new HashMap<>();
        Map<String, Infos.LotHoldReq> tmpLotHoldReleaseList = new HashMap<>();

        //----- The list of future hold actions to remain or to cancel -------//
        Map<String, Infos.LotHoldReq> tmpFutureHoldRemainList = new HashMap<>();
        Map<String, Infos.LotHoldReq> tmpFutureHoldCancelList = new HashMap<>();

        //----- The list of future rework actions to remain or to cancel -------//
        int futureReworkCancelCnt = 0;
        int futureReworkCancelLen = qTimeLen;
        List<Infos.FutureReworkInfo> strFutureReworkCancelList;
        List<Long> detailCntList;

        String qTimeWatchDogPerson = StandardProperties.OM_QT_SENTINEL_USER_ID.getValue();
        if (qTimeWatchDogPerson == null || 0 == CimStringUtils.length(qTimeWatchDogPerson)) {
            qTimeWatchDogPerson = SP_QTIMEWATCHDOG_PERSON;
        } else {
        }

        strFutureReworkCancelList = new ArrayList<>();
        detailCntList = new ArrayList<>();

        Integer keepQtimeActionFlag = StandardProperties.OM_QT_ACTION_KEEP_ON_CLEAR.getIntValue();

        for (int qTimeCnt = 0; qTimeCnt < qTimeLen; qTimeCnt++) {
            com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = aQTimeList.get(qTimeCnt);
            Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());
            ProcessDTO.QTimeRestrictionInfo aQTimeInfo = aQTime.getQTimeRestrictionInfo();

            if (aQTimeInfo == null || CimStringUtils.length(aQTimeInfo.getTriggerOperationNumber()) < 1) {
                throw new ServiceException(retCodeConfig.getNotFoundQtime());
            }

            ObjectIdentifier actionRouteID = aQTimeInfo.getTriggerMainProcessDefinition();
            String originalTriggerOperationNumber = aQTimeInfo.getTriggerOperationNumber();
            String originalTargetOperationNumber = aQTimeInfo.getTargetOperationNumber();
            if (0 < CimStringUtils.length(aQTimeInfo.getOriginalQTime())) {

                Outputs.ObjQtimeOriginalInformationGetOut strQtime_originalInformation_Get_out;
                String strQtime_originalInformation_Get_in;
                strQtime_originalInformation_Get_in = aQTimeInfo.getOriginalQTime();

                // call qtime_originalInformation_Get
                strQtime_originalInformation_Get_out = qtimeOriginalInformationGet(
                        strObjCommonIn,
                        strQtime_originalInformation_Get_in);
                if (0 < CimStringUtils.length(strQtime_originalInformation_Get_out.getTriggerRouteID().getValue())) {
                    actionRouteID = strQtime_originalInformation_Get_out.getTriggerRouteID();
                    originalTriggerOperationNumber = strQtime_originalInformation_Get_out.getTriggerOperationNumber();
                    originalTargetOperationNumber = strQtime_originalInformation_Get_out.getTargetOperationNumber();
                }
            }

            //----- The key of a QTime -------//
            String qTimeKey = "";
            qTimeKey += aQTimeInfo.getLotID().getValue();
            qTimeKey += SP_KEY_SEPARATOR_DOT;
            qTimeKey += actionRouteID.getValue();
            qTimeKey += SP_KEY_SEPARATOR_DOT;
            qTimeKey += originalTriggerOperationNumber;
            qTimeKey += SP_KEY_SEPARATOR_DOT;
            qTimeKey += originalTargetOperationNumber;

            //===== Check whether a Q-Time's actions should be reset or not =======//
            Boolean remainActionFlag = FALSE;

            if (TRUE.equals(strLot_qtimeInfo_GetForClearByProcessOperation_in.getAllClearFlag())) {
                remainActionFlag = FALSE;
            } else {
                if (TRUE.equals(strLot_qtimeInfo_GetForClearByProcessOperation_in.getPreviousOperationFlag())) {
                    //--------------------------------------------------------------------------
                    // Check if the previous operation and return operation is in QTime interval
                    //--------------------------------------------------------------------------
                    remainActionFlag = aProcessFlowContext.isInQTimeIntervalForPreviousOperation( aQTime );
                } else {
                    //--------------------------------------------------------------------------
                    // Check if the current operation and return operation is in QTime interval
                    //--------------------------------------------------------------------------
                    remainActionFlag = aProcessFlowContext.isInQTimeInterval(aQTime);
                }

                if ((TRUE.equals(aQTimeInfo.getManualCreated()) || TRUE.equals(aQTimeInfo.getPreTrigger()))
                        && CimStringUtils.equals(routeID, aQTimeInfo.getTriggerMainProcessDefinition().getValue())
                        && CimStringUtils.equals(operationNumber, aQTimeInfo.getTriggerOperationNumber())
                        && CimStringUtils.equals(branchInfo, aQTimeInfo.getTriggerBranchInfo())
                        && CimStringUtils.equals(returnInfo, aQTimeInfo.getTriggerReturnInfo())) {
                    remainActionFlag = TRUE;
                }

                if (CimStringUtils.equals(aQTimeInfo.getControl(), SP_QRESTTIME_SPECIFICCONTROL_DELETE)) {
                    remainActionFlag = FALSE;
                }
            }

            if ((CimStringUtils.equals(routeID, aQTimeInfo.getTargetMainProcessDefinition().getValue())
                    && CimStringUtils.equals(operationNumber, aQTimeInfo.getTargetOperationNumber())
                    && (0 == CimStringUtils.length(aQTimeInfo.getTargetBranchInfo())
                    || CimStringUtils.equals(branchInfo, aQTimeInfo.getTargetBranchInfo())))
                    || !remainActionFlag) {
                qTimeClearList.add(aQTime.getPrimaryKey());
                qTimeClearCnt++;
            }
            // if this environment variable is open,QtimeInformation and futureInformation will be cancel
            if (1 == keepQtimeActionFlag) {
                continue;
            }

            //===== Classify a Q-Time's actions into the remain list or the reset list =======//
            int actionLen = CimArrayUtils.getSize(aQTimeInfo.getActions());
            for (int actionCnt = 0; actionCnt < actionLen; actionCnt++) {

                if (TRUE.equals(aQTimeInfo.getActions().get(actionCnt).getActionDone())) {
                    if (CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getAction(), SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD) ||
                            CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getAction(), SP_QTIMERESTRICTION_ACTION_FUTUREHOLD)) {
                        ObjectIdentifier dummyID = null;

                        //----- The key of a lot hold action -------//
                        String lotHoldKey = "";
                        lotHoldKey += SP_HOLDTYPE_QTIMEOVERHOLD;
                        lotHoldKey += SP_KEY_SEPARATOR_DOT;
                        lotHoldKey += aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue();
                        lotHoldKey += SP_KEY_SEPARATOR_DOT;
                        lotHoldKey += qTimeWatchDogPerson;
                        lotHoldKey += SP_KEY_SEPARATOR_DOT;

                        //----- The value of a lot hold action -------//
                        Infos.LotHoldReq strLotHold = new Infos.LotHoldReq();
                        strLotHold.setHoldType(SP_HOLDTYPE_QTIMEOVERHOLD);
                        strLotHold.setHoldReasonCodeID(aQTimeInfo.getActions().get(actionCnt).getReasonCode());
                        strLotHold.setHoldUserID(ObjectIdentifier.buildWithValue(qTimeWatchDogPerson));
                        strLotHold.setRelatedLotID(dummyID);

                        //===== Add a lot hold action to the related list =======//
                        if (remainActionFlag) {
                            String lotHoldRemainKey = lotHoldKey;
                            if (tmpLotHoldRemainList.put(lotHoldRemainKey, strLotHold) == null) {
                            } else {
                                String lotHoldRemainKey_var = lotHoldRemainKey;
                            }
                        } else {
                            String lotHoldReleaseKey = lotHoldKey;
                            if (tmpLotHoldReleaseList.put(lotHoldReleaseKey, strLotHold) == null) {
                            } else {
                                String lotHoldReleaseKey_var = lotHoldReleaseKey;
                            }
                        }

                        if (CimStringUtils.length(aQTimeInfo.getActions().get(actionCnt).getOperationNumber()) > 0) {
                            ObjectIdentifier replaceActionRouteID;
                            if (0 < CimStringUtils.length(aQTimeInfo.getActions().get(actionCnt).getActionRouteID().getValue())) {
                                replaceActionRouteID = aQTimeInfo.getActions().get(actionCnt).getActionRouteID();
                            } else {
                                replaceActionRouteID = actionRouteID;
                            }
                            //----- The key of a future hold action -------//
                            String futureHoldKey = "";
                            futureHoldKey = SP_HOLDTYPE_QTIMEOVERHOLD;
                            futureHoldKey += SP_KEY_SEPARATOR_DOT;
                            futureHoldKey += aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue();
                            futureHoldKey += SP_KEY_SEPARATOR_DOT;
                            futureHoldKey += qTimeWatchDogPerson;
                            futureHoldKey += SP_KEY_SEPARATOR_DOT;
                            futureHoldKey += replaceActionRouteID.getValue();
                            futureHoldKey += SP_KEY_SEPARATOR_DOT;
                            futureHoldKey += aQTimeInfo.getActions().get(actionCnt).getOperationNumber();
                            futureHoldKey += SP_KEY_SEPARATOR_DOT;

                            //----- The value of a future hold action -------//
                            Infos.LotHoldReq strFutureHold = new Infos.LotHoldReq();
                            strFutureHold.setHoldType(SP_HOLDTYPE_QTIMEOVERHOLD);
                            strFutureHold.setHoldReasonCodeID(aQTimeInfo.getActions().get(actionCnt).getReasonCode());
                            strFutureHold.setHoldUserID(ObjectIdentifier.buildWithValue(qTimeWatchDogPerson));
                            strFutureHold.setRouteID(replaceActionRouteID);
                            strFutureHold.setOperationNumber(aQTimeInfo.getActions().get(actionCnt).getOperationNumber());
                            strFutureHold.setRelatedLotID(dummyID);

                            //===== Add a future hold action to the related list =======//
                            if (remainActionFlag) {
                                String futureHoldRemainKey = futureHoldKey;
                                if (tmpFutureHoldRemainList.put(futureHoldRemainKey, strFutureHold) == null) {
                                } else {
                                    String futureHoldRemainKey_var = futureHoldRemainKey;
                                }
                            } else {
                                String futureHoldCancelKey = futureHoldKey;
                                if (tmpFutureHoldCancelList.put(futureHoldCancelKey, strFutureHold) == null) {
                                } else {
                                    String futureHoldCancelKey_var = futureHoldCancelKey;
                                }
                            }
                        }
                    } else if (CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getAction(), SP_QTIMERESTRICTION_ACTION_FUTUREREWORK) &&
                            !remainActionFlag) {
                        ObjectIdentifier replaceActionRouteID;
                        if (0 < CimStringUtils.length(aQTimeInfo.getActions().get(actionCnt).getActionRouteID().getValue())) {
                            replaceActionRouteID = aQTimeInfo.getActions().get(actionCnt).getActionRouteID();
                        } else {
                            replaceActionRouteID = actionRouteID;
                        }
                        //===== Get a future rework request =======//
                        Outputs.lotFutureReworkListGetDROut strLot_futureReworkList_GetDR_out;

                        Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams = new Params.FutureActionDetailInfoInqParams();
                        futureActionDetailInfoInqParams.setLotID(aQTimeInfo.getLotID());
                        Infos.OperationFutureActionAttributes operationFutureActionAttributes = new Infos.OperationFutureActionAttributes();
                        operationFutureActionAttributes.setRouteID(replaceActionRouteID);
                        operationFutureActionAttributes.setOperationNumber(aQTimeInfo.getActions().get(actionCnt).getOperationNumber());
                        futureActionDetailInfoInqParams.setOperationFutureActionAttributes(operationFutureActionAttributes);

                        // call lot_futureReworkList_GetDR
                        strLot_futureReworkList_GetDR_out = lotMethod.lotFutureReworkListGetDR(strObjCommonIn,
                                futureActionDetailInfoInqParams);
                        if( CimArrayUtils.getSize(strLot_futureReworkList_GetDR_out.getFutureReworkDetailInfoList()) < 1 ) {
                            continue;
                        } else if( CimArrayUtils.getSize(strLot_futureReworkList_GetDR_out.getFutureReworkDetailInfoList()) > 1 ) {
                            throw new ServiceException(retCodeConfig.getInvalidInputParam());
                        } else {
                        }

                        //===== Check a future hold request =======//
                        Infos.FutureReworkInfo strFutureRework = strLot_futureReworkList_GetDR_out.getFutureReworkDetailInfoList().get(0);

                        String futureReworkTrigger;
                        futureReworkTrigger = "";
                        futureReworkTrigger = qTimeKey;
                        futureReworkTrigger += SP_KEY_SEPARATOR_DOT;
                        futureReworkTrigger += aQTimeInfo.getActions().get(actionCnt).getTargetTimeStamp();

                        int detailLen = CimArrayUtils.getSize(strFutureRework.getFutureReworkDetailInfoList());
                        for (int detailCnt = 0; detailCnt < detailLen; detailCnt++) {
                            if (CimStringUtils.equals(futureReworkTrigger,
                                    strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getTrigger()) &&
                                    CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getMainProcessDefinition().getValue(),
                                            strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getReworkRouteID().getValue()) &&
                                    CimStringUtils.equals(aQTimeInfo.getActions().get(actionCnt).getReasonCode().getValue(),
                                            strFutureRework.getFutureReworkDetailInfoList().get(detailCnt).getReasonCodeID().getValue())) {

                                int ftrwkCnt = 0;
                                for (ftrwkCnt = 0; ftrwkCnt < futureReworkCancelCnt; ftrwkCnt++) {
                                    if (CimStringUtils.equals(strFutureRework.getLotID().getValue(), strFutureReworkCancelList.get(ftrwkCnt).getLotID().getValue()) &&
                                            CimStringUtils.equals(strFutureRework.getRouteID().getValue(), strFutureReworkCancelList.get(ftrwkCnt).getRouteID().getValue()) &&
                                            CimStringUtils.equals(strFutureRework.getOperationNumber(), strFutureReworkCancelList.get(ftrwkCnt).getOperationNumber())) {
                                        break;
                                    }
                                }

                                if (ftrwkCnt < futureReworkCancelCnt) {
                                    strFutureReworkCancelList.add(new Infos.FutureReworkInfo());
                                    int cnt = detailCntList.get(ftrwkCnt).intValue();
                                    detailCntList.set(cnt, cnt + 1L);
                                    strFutureReworkCancelList.get(ftrwkCnt).getFutureReworkDetailInfoList().add(
                                            strFutureRework.getFutureReworkDetailInfoList().get(detailCnt));
                                } else {

                                    if (futureReworkCancelLen <= futureReworkCancelCnt) {
                                        futureReworkCancelLen += actionLen;
                                    }

                                    strFutureReworkCancelList.add(new Infos.FutureReworkInfo());
                                    strFutureReworkCancelList.get(futureReworkCancelCnt).setLotID(strFutureRework.getLotID());
                                    strFutureReworkCancelList.get(futureReworkCancelCnt).setRouteID(strFutureRework.getRouteID());
                                    strFutureReworkCancelList.get(futureReworkCancelCnt).setOperationNumber(strFutureRework.getOperationNumber());

                                    strFutureReworkCancelList.get(futureReworkCancelCnt).setFutureReworkDetailInfoList(new ArrayList<>());
                                    detailCntList.add(0L);
//                                    strFutureReworkCancelList.get(futureReworkCancelCnt).getFutureReworkDetailInfoList().add(new Infos.FutureReworkDetailInfo());
                                    strFutureReworkCancelList.get(futureReworkCancelCnt).getFutureReworkDetailInfoList().add(
                                            strFutureRework.getFutureReworkDetailInfoList().get(detailCnt));
                                    int cnt = detailCntList.get(futureReworkCancelCnt).intValue();
                                    detailCntList.set(cnt, cnt + 1L);
                                    futureReworkCancelCnt++;
                                }

                                break;
                            }
                        }
                    } else {
                    }
                }
            }
        }

        for (int futureReworkCnt = 0; futureReworkCnt < futureReworkCancelCnt; futureReworkCnt++) {
//            strFutureReworkCancelList.get(futureReworkCnt).getFutureReworkDetailInfoList().length( detailCntList[futureReworkCnt] );
        }

        //===== Remove actions which are in the remain list from the reset list =======//
//        String getKey = null;

        //----- The lot hold actions -------//
//        Infos.HoldList strGetLotHold;

//        tmpLotHoldReleaseList.forEach((getKey, strGetLotHold) -> {
//            if (tmpLotHoldRemainList.containsKey(getKey) == TRUE) {
//                tmpLotHoldReleaseList.remove(getKey);
//            } else {
//                //===== Check whether a hold record exists or not =======//
//                ProductDTO.HoldRecord strSearchLotHold = new ProductDTO.HoldRecord();
//                strSearchLotHold.setHoldType(strGetLotHold.getHoldType());
//                strSearchLotHold.setReasonCode(strGetLotHold.getHoldReasonCodeID());
//                strSearchLotHold.setHoldPerson(strGetLotHold.getHoldUserID());
//
//                ProductDTO.HoldRecord aLotHold = aLot.findHoldRecord(strSearchLotHold);
//
//                if (aLotHold == null || CimStringUtils.length(aLotHold.getHoldType()) < 1) {
//                    tmpLotHoldReleaseList.remove(getKey);
//                }
//            }
//        });
        // map.remove(key) ，This usage can cause nullPoint Exception
        Iterator<Map.Entry<String, Infos.LotHoldReq>> releaseIterator = tmpLotHoldReleaseList.entrySet().iterator();
        while (releaseIterator.hasNext()) {
            Map.Entry<String, Infos.LotHoldReq> next = releaseIterator.next();
            String key = next.getKey();
            Infos.LotHoldReq lotHoldReq = next.getValue();
            if (tmpLotHoldRemainList.containsKey(key) == TRUE) {
                releaseIterator.remove();
            } else {
                //===== Check whether a hold record exists or not =======//
                ProductDTO.HoldRecord strSearchLotHold = new ProductDTO.HoldRecord();
                strSearchLotHold.setHoldType(lotHoldReq.getHoldType());
                strSearchLotHold.setReasonCode(lotHoldReq.getHoldReasonCodeID());
                strSearchLotHold.setHoldPerson(lotHoldReq.getHoldUserID());

                ProductDTO.HoldRecord aLotHold = aLot.findHoldRecord(strSearchLotHold);

                if (aLotHold == null || CimStringUtils.length(aLotHold.getHoldType()) < 1) {
                    releaseIterator.remove();
                }
            }
        }

        //----- The future hold actions -------//
//        pptHoldList  strGetFutureHold;

//        tmpFutureHoldCancelList.forEach((getKey, strGetFutureHold) -> {
//            if (tmpFutureHoldRemainList.containsKey(getKey) == TRUE) {
//                tmpFutureHoldCancelList.remove(getKey);
//            } else {
//                //===== Check whether a future hold request exists or not =======//
//                ProductDTO.FutureHoldRecord strSearchFutureHold = new ProductDTO.FutureHoldRecord();
//                strSearchFutureHold.setHoldType(strGetFutureHold.getHoldType());
//                strSearchFutureHold.setReasonCode(strGetFutureHold.getHoldReasonCodeID());
//                strSearchFutureHold.setRequestPerson(strGetFutureHold.getHoldUserID());
//                strSearchFutureHold.setMainProcessDefinition(strGetFutureHold.getRouteID());
//                strSearchFutureHold.setOperationNumber(strGetFutureHold.getOperationNumber());
//
//                ProductDTO.FutureHoldRecord aFutureHold = aLot.findFutureHoldRecord(strSearchFutureHold);
//
//                if (aFutureHold == null || CimStringUtils.length(aFutureHold.getHoldType()) < 1) {
//                    tmpFutureHoldCancelList.remove(getKey);
//                }
//            }
//        });
        Iterator<Map.Entry<String, Infos.LotHoldReq>> cancleIterator = tmpFutureHoldCancelList.entrySet().iterator();
        while (cancleIterator.hasNext()) {
            Map.Entry<String, Infos.LotHoldReq> next = cancleIterator.next();
            String key = next.getKey();
            Infos.LotHoldReq futureHold = next.getValue();
            if (tmpFutureHoldRemainList.containsKey(key) == TRUE) {
                cancleIterator.remove();
            } else {
                //===== Check whether a future hold request exists or not =======//
                ProductDTO.FutureHoldRecord strSearchFutureHold = new ProductDTO.FutureHoldRecord();
                strSearchFutureHold.setHoldType(futureHold.getHoldType());
                strSearchFutureHold.setReasonCode(futureHold.getHoldReasonCodeID());
                strSearchFutureHold.setRequestPerson(futureHold.getHoldUserID());
                strSearchFutureHold.setMainProcessDefinition(futureHold.getRouteID());
                strSearchFutureHold.setOperationNumber(futureHold.getOperationNumber());

                ProductDTO.FutureHoldRecord aFutureHold = aLot.findFutureHoldRecord(strSearchFutureHold);

                if (aFutureHold == null || CimStringUtils.length(aFutureHold.getHoldType()) < 1) {
                    cancleIterator.remove();
                }
            }
        }

        //===== Set data to return =======//
        List<Infos.LotHoldReq> strLotHoldReleaseList = new ArrayList<>(tmpLotHoldReleaseList.values());
        List<Infos.LotHoldReq> strFutureHoldCancelList = new ArrayList<>(tmpFutureHoldCancelList.values());

        Infos.LotQtimeInfoGetForClearByProcessOperationOut strLotQtimeInfoGetForClearByProcessOperationOut = new Infos.LotQtimeInfoGetForClearByProcessOperationOut();
        strLotQtimeInfoGetForClearByProcessOperationOut.setQTimeClearList(qTimeClearList);
        strLotQtimeInfoGetForClearByProcessOperationOut.setStrLotHoldReleaseList(strLotHoldReleaseList);
        strLotQtimeInfoGetForClearByProcessOperationOut.setStrFutureHoldCancelList(strFutureHoldCancelList);
        strLotQtimeInfoGetForClearByProcessOperationOut.setStrFutureReworkCancelList(strFutureReworkCancelList);

        return strLotQtimeInfoGetForClearByProcessOperationOut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQTimeTargetOpeReplaceIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @throws
     * @author Ho
     * @date 2019/9/19 17:11
     */
    public void qTimeTargetOpeReplace(
            Infos.ObjCommon strObjCommonIn,
            Infos.QTimeTargetOpeReplaceIn strQTimeTargetOpeReplaceIn) {
        log.info("qTimeTargetOpeReplace");

        com.fa.cim.newcore.bo.product.CimLot aLot;
        aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, strQTimeTargetOpeReplaceIn.getLotID());

        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeq;

        if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW012")
                || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW024")) {
            log.info("" + "PartialOpeComp : TxID == " + strObjCommonIn.getTransactionID());
            qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();

        } else {
            log.info("" + "TxID == " + strObjCommonIn.getTransactionID());
            int qtimeSeqForWaferCount = 0;
            qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();

            if (qtimeSeqForWaferCount > 0) {
                log.info("" + "Exist WaferLevelQTime");
                qtimeSeq = aLot.allQTimeRestrictionsWithWaferLevelQTime();

            } else {
                log.info("" + "Not exist WaferLevelQTime");
                qtimeSeq = aLot.allQTimeRestrictions();

            }
        }

        int QTSLength = CimArrayUtils.getSize(qtimeSeq);
        log.info("" + "qtimeSeq.length()" + QTSLength);
        if (QTSLength != 0) {
            log.info("" + "QTSLength != 0");

            CimProductSpecification aTmpProductSpecification;
            CimProductSpecification aProductSpecification;
            aTmpProductSpecification = aLot.getProductSpecification();
            aProductSpecification = aTmpProductSpecification;

            Validations.check(aProductSpecification == null, retCodeConfig.getNotFoundProductSpec());

            com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext;
            aProcessFlowContext = aLot.getProcessFlowContext();

            Validations.check(aProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

            Boolean currentPOFlag = TRUE;

            if (TRUE.equals(strQTimeTargetOpeReplaceIn.getSpecificControlFlag())) {
                log.info("" + "TRUE == strQTimeTargetOpeReplaceIn.getSpecificControlFlag()");

                if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW006")
                        || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW008")
                        || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW014")
                        || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW023")
                        || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW012")
                        || CimStringUtils.equals(strObjCommonIn.getTransactionID(), "OEQPW024")) {
                    log.info("" + "OpeComp transaction.");
                    Boolean strLotCheckConditionForPOOut;

                    // step1 - checkLotConditionForPO
                    strLotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(
                            strObjCommonIn,
                            strQTimeTargetOpeReplaceIn.getLotID());



                    if ( strLotCheckConditionForPOOut){
                        log.info(""+ "Get PO from the current Operation.");
                    }else{
                        log.info(""+ "Get PO from the previous Operation.");
                        currentPOFlag = FALSE;
                    }
                }
            }

            com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation;
            com.fa.cim.newcore.bo.pd.CimProcessOperation aPosProcessOperation;
            if (currentPOFlag) {

                log.info("" + "currentPOFlag == TRUE");

                aProcessOperation = aLot.getProcessOperation();
                aPosProcessOperation = aProcessOperation;

            } else {

                log.info("" + "currentPOFlag == FALSE");

                aProcessOperation = aLot.getPreviousProcessOperation();
                aPosProcessOperation = aProcessOperation;

            }

            Validations.check(aPosProcessOperation == null, retCodeConfig.getNotFoundOperation());

            com.fa.cim.newcore.bo.pd.CimProcessDefinition aCurrentRoute;
            aCurrentRoute = aPosProcessOperation.getMainProcessDefinition();

            Validations.check(aCurrentRoute == null, retCodeConfig.getNotFoundMainRoute());

            String currentRouteID;
            currentRouteID = aCurrentRoute.getIdentifier();

            List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();

            List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();

            Boolean firstOperationFlag = FALSE;

            if (!currentPOFlag) {

                log.info("" + "currentPOFlag == FALSE");

                String previousBranchInfo;
                previousBranchInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO);

                String previousReturnInfo;
                previousReturnInfo = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO);

                if (0 < CimStringUtils.length(previousBranchInfo)) {
                    log.info("" + "previousBranchInfo" + previousBranchInfo);

                    List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousBranchInfo, "\\" + SP_KEY_SEPARATOR_DOT);
                    int previousTokensLen = CimArrayUtils.getSize(previousTokens);
                    if (4 == previousTokensLen) {
                        log.info("" + "previousTokensLen = 4");

                        String[] strings;
                        strings = new String[2];
                        strings[0] = (previousTokens).get(0);
                        strings[1] = (previousTokens).get(1);
                        String previousBranchRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                        strings[0] = (previousTokens).get(2);
                        strings[1] = (previousTokens).get(3);
                        String previousBranchOpeNo = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                        ObjectIdentifier aRouteID = new ObjectIdentifier();
                        aRouteID.setValue(previousBranchRouteIdentifier);
                        aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, aRouteID);

                        ProcessDTO.BranchInfo previousBranchInfo1 = new ProcessDTO.BranchInfo();
                        previousBranchInfo1.getRouteID().setValue(previousBranchRouteIdentifier);
                        previousBranchInfo1.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                        previousBranchInfo1.setOperationNumber(previousBranchOpeNo);
                        previousBranchInfo1.setProcessOperation("");
                        previousBranchInfo1.setReworkOutKey("");

                        String previousReworkOutKey;
                        previousReworkOutKey = ThreadContextHolder.getThreadSpecificDataString(SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY);

                        if (0 < CimStringUtils.length(previousReworkOutKey)) {
                            log.info("" + "previousReworkOutKey" + previousReworkOutKey);
                            previousBranchInfo1.setReworkOutKey(previousReworkOutKey);
                        }

                        int nSeqLen = CimArrayUtils.getSize(branchInfoSeq);
                        branchInfoSeq.add(previousBranchInfo1);
                    }
                }

                if (0 < CimStringUtils.length(previousReturnInfo)) {
                    log.info("" + "previousReturnInfo" + previousReturnInfo);

                    List<String> previousTokens = BaseStaticMethod.splitStringIntoTokens(previousReturnInfo, "\\" + SP_KEY_SEPARATOR_DOT);
                    int previousTokensLen = CimArrayUtils.getSize(previousTokens);
                    if (4 == previousTokensLen) {
                        log.info("" + "previousTokensLen = 4");

                        String[] strings;
                        strings = new String[2];
                        strings[0] = (previousTokens).get(0);
                        strings[1] = (previousTokens).get(1);
                        String previousReturnRouteIdentifier = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);
                        strings[0] = (previousTokens).get(2);
                        strings[1] = (previousTokens).get(3);
                        String previousReturnOpeNo = CimArrayUtils.mergeStringIntoTokens(CimArrayUtils.generateList(strings), SP_KEY_SEPARATOR_DOT);

                        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPosPD;
                        ObjectIdentifier aRouteID = new ObjectIdentifier();
                        aRouteID.setValue(previousReturnRouteIdentifier);
                        aPosPD = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, aRouteID);

                        ProcessDTO.ReturnInfo previousReturnInfo1 = new ProcessDTO.ReturnInfo();
                        previousReturnInfo1.getRouteID().setValue(previousReturnRouteIdentifier);
                        previousReturnInfo1.getRouteID().setReferenceKey(aPosPD.getPrimaryKey());
                        previousReturnInfo1.setOperationNumber(previousReturnOpeNo);
                        previousReturnInfo1.setProcessFlow("");
                        previousReturnInfo1.setMainProcessFlow("");
                        previousReturnInfo1.setModuleProcessFlow("");

                        int nSeqLen = CimArrayUtils.getSize(returnInfoSeq);
                        returnInfoSeq.add(previousReturnInfo1);
                    }
                }

                com.fa.cim.newcore.bo.pd.CimProcessFlow aMainPF;
                aMainPF = aPosProcessOperation.getMainProcessFlow();

                Validations.check(aMainPF == null, retCodeConfig.getNotFoundProcessFlow());

                String aModuleNo;
                aModuleNo = aPosProcessOperation.getModuleNumber();

                com.fa.cim.newcore.bo.pd.CimProcessFlow aModulePF;
                aModulePF = aPosProcessOperation.getModuleProcessFlow();

                Validations.check(aModulePF == null, retCodeConfig.getNotFoundProcessFlow());

                com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aModulePOS;
                aModulePOS = aPosProcessOperation.getModuleProcessOperationSpecification();

                Validations.check(aModulePOS == null, retCodeConfig.getNotFoundPos());

                com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification aPrevPOS;
                AtomicReference<com.fa.cim.newcore.bo.pd.CimProcessFlow> outMainProcessFlow = new AtomicReference<>(), outModuleProcessFlow = new AtomicReference<>();
                AtomicReference<String> chrOutModuleNumber = new AtomicReference<>();
                String outModuleNumber;
                aPrevPOS = aMainPF.getPreviousProcessOperationSpecificationFor(aModuleNo,
                        aModulePF,
                        aModulePOS,
                        outMainProcessFlow,
                        chrOutModuleNumber,
                        outModuleProcessFlow);

                outModuleNumber = chrOutModuleNumber.get();

                if (aPrevPOS == null) {
                    log.info("" + "aPrevPOS = nil");
                    firstOperationFlag = TRUE;
                }
            } else {

                log.info("" + "currentPOFlag == TRUE");

                firstOperationFlag = aProcessFlowContext.isFirstOperationForProcessFlowOnCurrentRoute();

            }

            Integer qtimeDispatchPrecedeUseCustomFieldFlag = StandardProperties.OM_QTIME_DISPATCHPRECEDE_USE_CUSTOMFIELD.getIntValue();

            for (int iCnt1 = 0; iCnt1 < QTSLength; iCnt1++) {
                log.info("" + "loop to qtimeSeq.length()" + iCnt1);
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qtimeSeq.get(iCnt1);

                Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());

                if (FALSE.equals(strQTimeTargetOpeReplaceIn.getSpecificControlFlag())) {
                    String strControl;
                    strControl = aQTime.getControl();

                    if (CimStringUtils.equals(strControl, SP_QRESTTIME_SPECIFICCONTROL_REPLACETARGET)) {
                        log.info("" + "The QTime's control is ReplaceTarget.");
                        aQTime.setControl("");

                    } else {
                        log.info("" + "The QTime's control is not ReplaceTarget.");
                        continue;
                    }
                }
                ProcessDTO.QTimeRestrictionInfo aQTimeRestrictionInfo = aQTime.getQTimeRestrictionInfo();

                Validations.check(aQTimeRestrictionInfo == null || CimStringUtils.length(aQTimeRestrictionInfo.getTriggerOperationNumber()) < 1,
                        retCodeConfig.getNotFoundQtime());

                if (aQTimeRestrictionInfo == null || CimStringUtils.length(aQTimeRestrictionInfo.getTriggerOperationNumber()) < 1) {
                    log.info("" + "The target lot's Q-Time information was not found.");

                }


                ProcessDTO.ReplaceTimeRestrictionSpecification aReplaceTimeRS;
                aReplaceTimeRS = aPosProcessOperation.findReplaceTimeRestriction( aQTimeRestrictionInfo,
                        aProductSpecification,
                        branchInfoSeq,
                        returnInfoSeq,
                        currentRouteID );

                if (aReplaceTimeRS != null) {
                    log.info("" + "aReplaceTimeRS != null");
                    if (TRUE.equals(strQTimeTargetOpeReplaceIn.getSpecificControlFlag())) {
                        log.info("" + "TRUE == strQTimeTargetOpeReplaceIn.getSpecificControlFlag()");

                        if (CimStringUtils.equals(aReplaceTimeRS.getControl(), SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER)) {
                            log.info("" + "aReplaceTimeRS.getControl() == Retrigger");
                            if (firstOperationFlag) {
                                log.info("" + "First Operation");

                                Timestamp currentTimeStamp = strObjCommonIn.getTimeStamp().getReportTimeStamp();
                                Timestamp currentTimeStampVar = currentTimeStamp;

                                Timestamp aTimeStamp = CimDateUtils.convertToOrInitialTime(aQTimeRestrictionInfo.getTriggerTimeStamp());
                                aQTimeRestrictionInfo.setTriggerTimeStamp(CimDateUtils.getTimestampAsString(currentTimeStampVar));
                                aQTimeRestrictionInfo.setTargetTimeStamp(
                                        CimDateUtils.getTimestampAsString(new Timestamp(currentTimeStamp.getTime() -
                                                (aTimeStamp.getTime() - CimDateUtils.convertToOrInitialTime(aQTimeRestrictionInfo.getTargetTimeStamp()).getTime())))
                                );
                                aQTimeRestrictionInfo.setActionDone(FALSE);
                                aQTimeRestrictionInfo.setControl(aReplaceTimeRS.getControl());
                                aQTimeRestrictionInfo.setManualCreated(FALSE);

                                int actionLength = CimArrayUtils.getSize(aReplaceTimeRS.getActions());
                                log.info("" + "aReplaceTimeRS.getArrayUtils().getSize(actions)" + actionLength);

                                if (0 < actionLength) {
                                    log.info("" + "replace action");

                                    String targetTimeStamp = null;
                                    String dispatchPrecedeTargetTime = null;
                                    String mostUrgentTargetTimeAction = null;

                                    Boolean setMostUrgentTimeFlag = FALSE;
                                    if (SP_DISPATCH_PRECEDE_NOT_FOUND*1.0 == aReplaceTimeRS.getExpiredTimeDuration()) {
                                        log.info("" + "SP_DISPATCH_PRECEDE_NOT_FOUND == aReplaceTimeRS.getExpiredTimeDuration()");
                                        setMostUrgentTimeFlag = TRUE;
                                    } else {
                                        log.info("" + "SP_DISPATCH_PRECEDE_NOT_FOUND != aReplaceTimeRS.getExpiredTimeDuration()");
                                        targetTimeStamp = CimDateUtils.getTimestampAsString(new Timestamp((long) (currentTimeStamp.getTime() + aReplaceTimeRS.getExpiredTimeDuration())));
                                    }

                                    Outputs.ObjQtimeOriginalInformationGetOut strQtimeOriginalInformationGetOut;

                                    strQtimeOriginalInformationGetOut = qtimeOriginalInformationGet(
                                            strObjCommonIn,
                                            aReplaceTimeRS.getOriginalQTime() );

                                    ObjectIdentifier originalActionRouteID = strQtimeOriginalInformationGetOut.getTriggerRouteID();

                                    List<ProcessDTO.QTimeRestrictionAction> replaceActions;
                                    replaceActions = new ArrayList<>();
                                    int actionCnt = 0;

                                    for (int actionIndex = 0; actionIndex < actionLength; actionIndex++) {
                                        log.info("" + "loop to aReplaceTimeRS.getArrayUtils().getSize(actions)" + actionIndex);
                                        String targetTimeStampForAction = null;
                                        targetTimeStampForAction = CimDateUtils.getTimestampAsString(new Timestamp(
                                                (long) (currentTimeStamp.getTime() + aReplaceTimeRS.getActions().get(actionIndex).getExpiredTimeDuration())
                                        ));

                                        if (CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRS.getActions().get(actionIndex).getAction())) {
                                            log.info("" + "Action is DispatchPrecede");
                                            dispatchPrecedeTargetTime = targetTimeStampForAction;

                                            if( 0 == qtimeDispatchPrecedeUseCustomFieldFlag && CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRS.getActions().get(actionIndex).getAction()) ){
                                                log.info(""+ "CustomField for DispatchPrecede is not used and action is DispatchPrecede");
                                                continue;
                                            }
                                        } else if (setMostUrgentTimeFlag) {
                                            log.info("" + "TRUE == setMostUrgentTimeFlag");
                                            if (0 == CimStringUtils.length(mostUrgentTargetTimeAction)) {
                                                log.info("" + "0 == StringUtils.length( mostUrgentTargetTimeAction )");

                                                mostUrgentTargetTimeAction = targetTimeStampForAction;
                                            } else {
                                                log.info("" + "0 != StringUtils.length( mostUrgentTargetTimeAction )");

                                                if (CimDateUtils.compare(mostUrgentTargetTimeAction, targetTimeStampForAction) > 0) {
                                                    log.info("" + "mostUrgentTargetTimeAction > targetTimeStampForAction");
                                                    mostUrgentTargetTimeAction = targetTimeStampForAction;
                                                } else {
                                                    log.info("" + "else");

                                                }
                                            }
                                        }

                                        replaceActions.add(new ProcessDTO.QTimeRestrictionAction());
                                        replaceActions.get(actionCnt).setTargetTimeStamp(targetTimeStampForAction);
                                        replaceActions.get(actionCnt).setAction                (  aReplaceTimeRS.getActions().get(actionIndex).getAction() );
                                        replaceActions.get(actionCnt).setReasonCode            ( aReplaceTimeRS.getActions().get(actionIndex).getReasonCode());
                                        if( 0 < CimStringUtils.length(aReplaceTimeRS.getActions().get(actionIndex).getOperationNumber()) ){
                                            log.info(""+ "actionOpeNo is not blank");
                                            if(ObjectIdentifier.isNotEmpty(aReplaceTimeRS.getActions().get(actionIndex).getActionRouteID())){
                                                log.info(""+ "actionRouteID is replace RouteID");
                                                replaceActions.get(actionCnt).setActionRouteID     ( aReplaceTimeRS.getActions().get(actionIndex).getActionRouteID());
                                            }else{
                                                log.info(""+ "actionRouteID is original Q-Time RouteID");
                                                replaceActions.get(actionCnt).setActionRouteID(originalActionRouteID);
                                            }
                                        }
                                        replaceActions.get(actionCnt).setOperationNumber       (  aReplaceTimeRS.getActions().get(actionIndex).getOperationNumber() );
                                        replaceActions.get(actionCnt).setTiming                (  aReplaceTimeRS.getActions().get(actionIndex).getTiming() );
                                        replaceActions.get(actionCnt).setMainProcessDefinition ( aReplaceTimeRS.getActions().get(actionIndex).getMainProcessDefinition());
                                        replaceActions.get(actionCnt).setMessageDefinition     ( aReplaceTimeRS.getActions().get(actionIndex).getMessageDefinition());
                                        replaceActions.get(actionCnt).setCustomField           (  aReplaceTimeRS.getActions().get(actionIndex).getCustomField() );
                                        replaceActions.get(actionCnt).setWatchdogRequired      ( TRUE);

                                        if (CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aReplaceTimeRS.getActions().get(actionIndex).getAction())) {
                                            log.info("" + "Action is DispatchPrecede");
                                            replaceActions.get(actionCnt).setActionDone(TRUE);
                                        } else {
                                            replaceActions.get(actionCnt).setActionDone(FALSE);
                                        }
                                        actionCnt++;
                                    }

                                    aQTimeRestrictionInfo.setActions(replaceActions);

                                    if (CimStringUtils.length(dispatchPrecedeTargetTime) != 0) {
                                        log.info("" + "StringUtils.length( dispatchPrecedeTargetTime ) != 0");
                                        aQTimeRestrictionInfo.setTargetTimeStamp(dispatchPrecedeTargetTime);
                                    } else if (CimStringUtils.length(mostUrgentTargetTimeAction) != 0) {
                                        log.info("" + "StringUtils.length( mostUrgentTargetTimeAction ) != 0");
                                        aQTimeRestrictionInfo.setTargetTimeStamp(mostUrgentTargetTimeAction);
                                    } else {
                                        log.info("" + "StringUtils.length( mostUrgentTargetTimeAction ) == 0");
                                        aQTimeRestrictionInfo.setTargetTimeStamp(targetTimeStamp);
                                    }
                                    log.info("" + "aQTimeRestrictionInfo.getTargetTimeStamp()" + aQTimeRestrictionInfo.getTargetTimeStamp());
                                } else {
                                    log.info("" + "Update the target time of action");

                                    for (int x = 0; x < CimArrayUtils.getSize(aQTimeRestrictionInfo.getActions()); x++) {
                                        log.info("" + "Loop aQTimeRestrictionInfo.getActions() to update targetTimeStamp" + x);
                                        aQTimeRestrictionInfo.getActions().get(x).setTargetTimeStamp(
                                                CimDateUtils.getTimestampAsString(new Timestamp(currentTimeStamp.getTime() + (aTimeStamp.getTime() -
                                                        CimDateUtils.convertToOrInitialTime(aQTimeRestrictionInfo.getActions().get(x).getTargetTimeStamp()).getTime()))));
                                        if (!CimStringUtils.equals(SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE, aQTimeRestrictionInfo.getActions().get(x).getAction())) {
                                            aQTimeRestrictionInfo.getActions().get(x).setActionDone(FALSE);
                                        }
                                    }
                                }

                                aQTime.setQTimeRestrictionInfo(aQTimeRestrictionInfo);

                            } else {
                                log.info("" + "Not First Operation");
                            }
                        } else if (CimStringUtils.equals(aReplaceTimeRS.getControl(), SP_QRESTTIME_SPECIFICCONTROL_DELETE)) {
                            log.info("" + "aReplaceTimeRS.getControl() == Delete");
                            aQTime.setControl(aReplaceTimeRS.getControl());

                        }

                    } else {
                        log.info("" + "FALSE == strQTimeTargetOpeReplaceIn.getSpecificControlFlag()");
                        if (0 < CimStringUtils.length(aReplaceTimeRS.getTargetOperationNumber())) {
                            log.info("" + "0 < StringUtils.length(aReplaceTimeRS.getTargetOperationNumber())");

                            com.fa.cim.newcore.bo.pd.CimProcessDefinition aProcessDefinition = null;
                            aProcessDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,
                                    aReplaceTimeRS.getTargetRouteID());
                            ProcessDTO.QTimeRestrictionInfo aQTimeResInfo = null;

                            aQTimeResInfo = aQTime.replaceTargetWithActions( aProcessDefinition,
                                    aReplaceTimeRS.getTargetOperationNumber(),
                                    aReplaceTimeRS.getTargetBranchInfo(),
                                    aReplaceTimeRS.getTargetReturnInfo(),
                                    aReplaceTimeRS.getActions() );

                            Infos.QtimeInfo strQtimeInfo = new Infos.QtimeInfo();
                            strQtimeInfo.setQTimeType(aQTimeResInfo.getQTimeType());
                            strQtimeInfo.setWaferID(aQTimeResInfo.getWaferID());
                            strQtimeInfo.setPreTrigger(aQTimeResInfo.getPreTrigger());
                            strQtimeInfo.setOriginalQTime(aQTimeResInfo.getOriginalQTime());
                            strQtimeInfo.setProcessDefinitionLevel(aQTimeResInfo.getProcessDefinitionLevel());
                            strQtimeInfo.setRestrictionTriggerRouteID(aQTimeResInfo.getTriggerMainProcessDefinition());
                            strQtimeInfo.setRestrictionTriggerOperationNumber(aQTimeResInfo.getTriggerOperationNumber());
                            strQtimeInfo.setRestrictionTriggerBranchInfo(aQTimeResInfo.getTriggerBranchInfo());
                            strQtimeInfo.setRestrictionTriggerReturnInfo(aQTimeResInfo.getTriggerReturnInfo());
                            strQtimeInfo.setRestrictionTriggerTimeStamp(aQTimeResInfo.getTriggerTimeStamp());
                            strQtimeInfo.setRestrictionTargetRouteID(aQTimeResInfo.getTargetMainProcessDefinition());
                            strQtimeInfo.setRestrictionTargetOperationNumber(aQTimeResInfo.getTargetOperationNumber());
                            strQtimeInfo.setRestrictionTargetBranchInfo(aQTimeResInfo.getTargetBranchInfo());
                            strQtimeInfo.setRestrictionTargetReturnInfo(aQTimeResInfo.getTargetReturnInfo());
                            strQtimeInfo.setRestrictionTargetTimeStamp(aQTimeResInfo.getTargetTimeStamp());
                            strQtimeInfo.setPreviousTargetInfo(aQTimeResInfo.getPreviousTargetInfo());
                            strQtimeInfo.setSpecificControl(aQTimeResInfo.getControl());
                            if (TRUE.equals(aQTimeResInfo.getWatchdogRequired())) {
                                log.info("" + "TRUE == aQTimeResInfo.getWatchdogRequired()");
                                strQtimeInfo.setWatchDogRequired("Y");
                            } else if (FALSE.equals(aQTimeResInfo.getWatchdogRequired())) {
                                log.info("" + "FALSE == aQTimeResInfo.getWatchdogRequired()");
                                strQtimeInfo.setWatchDogRequired("N");
                            }
                            if (TRUE.equals(aQTimeResInfo.getActionDone())) {
                                log.info("" + "TRUE == aQTimeResInfo.getActionDone()");
                                strQtimeInfo.setActionDoneFlag("Y");
                            } else if (FALSE.equals(aQTimeResInfo.getActionDone())) {
                                log.info("" + "FALSE == aQTimeResInfo.getActionDone()");
                                strQtimeInfo.setActionDoneFlag("N");
                            }
                            strQtimeInfo.setManualCreated(aQTimeResInfo.getManualCreated());
                            int actionLength = CimArrayUtils.getSize(aQTimeResInfo.getActions());
                            log.info("" + "aQTimeResInfo.getArrayUtils().getSize(actions)" + actionLength);
                            if (actionLength != 0) {
                                log.info("" + "actionLength != 0");
                                strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                                for (int iCnt3 = 0; iCnt3 < actionLength; iCnt3++) {
                                    log.info("" + "loop to aQTimeResInfo.getArrayUtils().getSize(actions)" + iCnt3);
                                    strQtimeInfo.getStrQtimeActionInfoList().add(new Infos.QTimeActionInfo());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionTargetTimeStamp(aQTimeResInfo.getActions().get(iCnt3).getTargetTimeStamp());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setQrestrictionAction(aQTimeResInfo.getActions().get(iCnt3).getAction());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReasonCodeID(aQTimeResInfo.getActions().get(iCnt3).getReasonCode());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionRouteID(aQTimeResInfo.getActions().get(iCnt3).getActionRouteID());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionOperationNumber(aQTimeResInfo.getActions().get(iCnt3).getOperationNumber());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setFutureHoldTiming(aQTimeResInfo.getActions().get(iCnt3).getTiming());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setReworkRouteID(aQTimeResInfo.getActions().get(iCnt3).getMainProcessDefinition());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setMessageID(aQTimeResInfo.getActions().get(iCnt3).getMessageDefinition());
                                    strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setCustomField(aQTimeResInfo.getActions().get(iCnt3).getCustomField());
                                    if (TRUE.equals(aQTimeResInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                                        log.info("" + "TRUE == aQTimeResInfo.getActions().get(iCnt3).getWatchdogRequired()");
                                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("Y");
                                    } else if (FALSE.equals(aQTimeResInfo.getActions().get(iCnt3).getWatchdogRequired())) {
                                        log.info("" + "FALSE == aQTimeResInfo.getActions().get(iCnt3).getWatchdogRequired()");
                                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setWatchDogRequired("N");
                                    }
                                    if (TRUE.equals(aQTimeResInfo.getActions().get(iCnt3).getActionDone())) {
                                        log.info("" + "TRUE == aQTimeResInfo.getActions().get(iCnt3).getActionDone()");
                                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("Y");
                                    } else if (FALSE.equals(aQTimeResInfo.getActions().get(iCnt3).getActionDone())) {
                                        log.info("" + "FALSE == aQTimeResInfo.getActions().get(iCnt3).getActionDone()");
                                        strQtimeInfo.getStrQtimeActionInfoList().get(iCnt3).setActionDoneFlag("N");
                                    }
                                }
                            } else {
                                log.info("" + "actionLength == 0");
                                strQtimeInfo.setStrQtimeActionInfoList(new ArrayList<>());
                            }

                            Inputs.QTimeChangeEventMakeParams strQTimeChangeEventMakeIn = new Inputs.QTimeChangeEventMakeParams();
                            strQTimeChangeEventMakeIn.setUpdateMode(SP_QRESTTIME_OPECATEGORY_UPDATE);
                            strQTimeChangeEventMakeIn.setLotID(strQTimeTargetOpeReplaceIn.getLotID());
                            strQTimeChangeEventMakeIn.setQtimeInfo(strQtimeInfo);
                            strQTimeChangeEventMakeIn.setClaimMemo("");

                            eventMethod.qTimeChangeEventMake(
                                    strObjCommonIn,
                                    strQTimeChangeEventMakeIn);
                        }
                    }
                }
            }
        }

        log.info("qTimeTargetOpeReplace");
    }

    @Override
    public Outputs.ObjLotQtimeInfoGetForClearOut lotQtimeInfoGetForClear(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Boolean allClearFlag) {

        //--------------------------------------------------
        // Prepare data to check
        //--------------------------------------------------
        //===== Get the target lot's object =======//
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        Validations.check(null == lot, retCodeConfig.getNotFoundLot());
        com.fa.cim.newcore.bo.pd.CimProcessOperation processOperation = lot.getProcessOperation();
        Validations.check(null == processOperation, retCodeConfig.getNotFoundProcessOperation());
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aCurrentRoute = processOperation.getMainProcessDefinition();
        Validations.check(null == aCurrentRoute, retCodeConfig.getNotFoundProcessDefinition());
        String currentRouteID = aCurrentRoute.getIdentifier();
        String currentOperationNumber = processOperation.getOperationNumber();
        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext = lot.getProcessFlowContext();
        Validations.check(null == aProcessFlowContext, retCodeConfig.getNotFoundPfx());
        List<ProcessDTO.BranchInfo> branchInfoSeq = aProcessFlowContext.allBranchInfos();
        String branchInfo = cimFrameWorkGlobals.getBranchInfoFromTopToMainRoute( branchInfoSeq );
        List<ProcessDTO.ReturnInfo> returnInfoSeq = aProcessFlowContext.allReturnInfos();
        String returnInfo = cimFrameWorkGlobals.getReturnInfoFromTopToMainRoute( returnInfoSeq );

        //--------------------------------------------------
        // Check Q-Time settings
        //--------------------------------------------------
        //===== Get the target lot's Q-Time settings =======//
        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> aQTimeList;
        if (CimArrayUtils.binarySearch(new String[]{"OEQPW012", "OEQPW024"}, objCommon.getTransactionID())) {
            aQTimeList = lot.allQTimeRestrictionsWithWaferLevelQTime();
        } else {
            int aQTimeListForWaferCount = lot.getWaferLevelQTimeCount();
            if (aQTimeListForWaferCount > 0) {
                aQTimeList = lot.allQTimeRestrictionsWithWaferLevelQTime();
            } else {
                aQTimeList = lot.allQTimeRestrictions();
            }
        }
        //----- The list of qtime settings to clear -------//
        List<String> qTimeClearList = new ArrayList<>();
        //----- The list of lot hold actions to remain or to release -------//
        Map<String, Infos.LotHoldReq> tmpLotHoldRemainList = new HashMap<>();
        Map<String, Infos.LotHoldReq> tmpLotHoldReleaseList = new HashMap<>();
        //----- The list of future hold actions to remain or to cancel -------//
        Map<String, Infos.LotHoldReq> tmpFutureHoldRemainList = new HashMap<>();
        Map<String, Infos.LotHoldReq> tmpFutureHoldCancelList = new HashMap<>();
        //----- The list of future rework actions to remain or to cancel -------//
        List<Infos.FutureReworkInfo> strFutureReworkCancelList = new ArrayList<>();
        int futureReworkCancelCnt = 0;
        String qTimeWatchDogPerson = StandardProperties.OM_QT_SENTINEL_USER_ID.getValue();
        if (CimStringUtils.isEmpty(qTimeWatchDogPerson)) {
            qTimeWatchDogPerson = BizConstant.SP_QTIME_WATCH_DOG_PERSON;
        }
        Integer keepQtimeActionFlag = StandardProperties.OM_QT_ACTION_KEEP_ON_CLEAR.getIntValue();

        if (CimArrayUtils.isNotEmpty(aQTimeList)) {
            for (com.fa.cim.newcore.bo.pd.CimQTimeRestriction qTimeRestriction : aQTimeList) {
                //===== Get the target lot's Q-Time information =======//
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime = qTimeRestriction;
                Validations.check(aQTime == null, retCodeConfig.getNotFoundQtime());
                ProcessDTO.QTimeRestrictionInfo infoOut = aQTime.getQTimeRestrictionInfo();
                if (null == infoOut || CimStringUtils.isEmpty(infoOut.getTriggerOperationNumber())) {
                    throw new ServiceException(retCodeConfig.getNotFoundQtime());
                }
                ProcessDTO.QTimeRestrictionInfo aQTimeInfo = infoOut;
                ObjectIdentifier actionRouteID = aQTimeInfo.getTriggerMainProcessDefinition();
                String originalTriggerOperationNumber = aQTimeInfo.getTriggerOperationNumber();
                String originalTargetOperationNumber = aQTimeInfo.getTargetOperationNumber();
                String originalQTime = aQTimeInfo.getOriginalQTime();
                if (!CimStringUtils.isEmpty(originalQTime)) {
                    //----------------------------------
                    //  Get original Q-Time information
                    //----------------------------------
                    Outputs.ObjQtimeOriginalInformationGetOut originalInformationOut = this.qtimeOriginalInformationGet(objCommon, originalQTime);
                    //----------------------------------
                    //  Set action route ID
                    //----------------------------------
                    Outputs.ObjQtimeOriginalInformationGetOut objQtimeOriginalInformationGetOut = originalInformationOut;
                    if (null != objQtimeOriginalInformationGetOut && !ObjectIdentifier.isEmpty(objQtimeOriginalInformationGetOut.getTriggerRouteID())) {
                        actionRouteID = objQtimeOriginalInformationGetOut.getTriggerRouteID();
                        originalTriggerOperationNumber = objQtimeOriginalInformationGetOut.getTriggerOperationNumber();
                        originalTargetOperationNumber = objQtimeOriginalInformationGetOut.getTargetOperationNumber();
                    }
                }

                //----- The key of a qtime -------//
                String qTimeKey = String.format("%s.%s.%s.%s", aQTimeInfo.getLotID().getValue(), actionRouteID.getValue(), originalTriggerOperationNumber, originalTargetOperationNumber);

                //===== Check whether a Q-Time's actions should be reset or not =======//
                Boolean remainActionFlag = aProcessFlowContext.isInQTimeInterval(aQTime);

                if ((CimBooleanUtils.isTrue(aQTimeInfo.getManualCreated()) || CimBooleanUtils.isTrue(aQTimeInfo.getPreTrigger()))
                        && ObjectIdentifier.equalsWithValue(currentRouteID, aQTimeInfo.getTriggerMainProcessDefinition())
                        && currentOperationNumber.equals(aQTimeInfo.getTriggerOperationNumber())
                        && CimStringUtils.equals(branchInfo,aQTimeInfo.getTriggerBranchInfo())
                        && CimStringUtils.equals(returnInfo,aQTimeInfo.getTriggerReturnInfo())) {
                    remainActionFlag = true;
                }
                if (BizConstant.SP_QRESTTIME_SPECIFICCONTROL_DELETE.equals(aQTimeInfo.getControl())) {
                    remainActionFlag = false;
                }
                if (allClearFlag) {
                    remainActionFlag = false;
                }
                if ((ObjectIdentifier.equalsWithValue(currentRouteID, aQTimeInfo.getTargetMainProcessDefinition())
                        && currentOperationNumber.equals(aQTimeInfo.getTargetOperationNumber())
                        && (CimObjectUtils.isEmpty(aQTimeInfo.getTargetBranchInfo())
                        || CimStringUtils.equals(branchInfo, aQTimeInfo.getTargetBranchInfo())))
                        || !remainActionFlag) {
                    qTimeClearList.add(aQTime.getPrimaryKey());
                }

                //----- Keep Q-Time Actions if OM_QT_ACTION_KEEP_ON_CLEAR   == 1
                if (1 == keepQtimeActionFlag) {
                    continue;
                }

                List<ProcessDTO.QTimeRestrictionAction> actions = aQTimeInfo.getActions();
                for (ProcessDTO.QTimeRestrictionAction qTimeAction : actions) {
                    if (CimBooleanUtils.isTrue(qTimeAction.getActionDone())) {
                        if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD, qTimeAction.getAction()) ||
                                CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD, qTimeAction.getAction())) {
                            //----- The key of a lot hold action -------//
                            String lotHoldKey = String.format("%s.%s.%s.", BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD, qTimeAction.getReasonCode(), qTimeWatchDogPerson);

                            //----- The value of a lot hold action -------//
                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                            lotHoldReq.setHoldReasonCodeID(qTimeAction.getReasonCode());
                            lotHoldReq.setHoldUserID(new ObjectIdentifier(qTimeWatchDogPerson));

                            //===== Add a lot hold action to the related list =======//
                            if (remainActionFlag) {
                                tmpLotHoldRemainList.put(lotHoldKey, lotHoldReq);
                            } else {
                                tmpLotHoldReleaseList.put(lotHoldKey, lotHoldReq);
                            }

                            if (!CimStringUtils.isEmpty(qTimeAction.getOperationNumber())) {
                                ObjectIdentifier replaceActionRouteID = !ObjectIdentifier.isEmpty(qTimeAction.getActionRouteID()) ? qTimeAction.getActionRouteID() : actionRouteID;
                                String futureHoldKey = String.format("%s.%s.%s.%s.%s.", BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD,ObjectIdentifier.fetchValue(qTimeAction.getReasonCode()), qTimeWatchDogPerson, replaceActionRouteID.getValue(), qTimeAction.getOperationNumber());

                                //----- The value of a future hold action -------//
                                Infos.LotHoldReq futureHoldReq = new Infos.LotHoldReq();
                                futureHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                                futureHoldReq.setHoldReasonCodeID(qTimeAction.getReasonCode());
                                futureHoldReq.setHoldUserID(new ObjectIdentifier(qTimeWatchDogPerson));
                                futureHoldReq.setRouteID(replaceActionRouteID);
                                futureHoldReq.setOperationNumber(qTimeAction.getOperationNumber());

                                //===== Add a future hold action to the related list =======//
                                if (remainActionFlag) {
                                    tmpFutureHoldRemainList.put(futureHoldKey, futureHoldReq);
                                } else {
                                    tmpFutureHoldCancelList.put(futureHoldKey, futureHoldReq);
                                }
                            }
                        } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, qTimeAction.getAction()) && !remainActionFlag) {
                            ObjectIdentifier replaceActionRouteID = !ObjectIdentifier.isEmpty(qTimeAction.getActionRouteID()) ? qTimeAction.getActionRouteID() : actionRouteID;
                            //===== Get a future rework request =======//
                            List<Infos.FutureReworkInfo> reworkOut = lotMethod.lotFutureReworkListGetDR(objCommon, aQTimeInfo.getLotID(), replaceActionRouteID, qTimeAction.getOperationNumber());

                            if (CimObjectUtils.isEmpty(reworkOut)) {
                                continue;
                            } else if (CimArrayUtils.getSize(reworkOut)>1) {
                                throw new ServiceException(retCodeConfig.getInvalidInputParam());
                            } else {
                            }

                            //===== Check a future hold request =======//
                            Infos.FutureReworkInfo futureReworkInfo = reworkOut.get(0);
                            String futureReworkTrigger = String.format("%s.%s", qTimeKey, qTimeAction.getTargetTimeStamp());

                            List<Infos.FutureReworkDetailInfo> futureReworkDetailInfoList = futureReworkInfo.getFutureReworkDetailInfoList();
                            for (Infos.FutureReworkDetailInfo detailInfo : futureReworkDetailInfoList) {
                                if (CimStringUtils.equals(futureReworkTrigger, detailInfo.getTrigger()) &&
                                        ObjectIdentifier.equalsWithValue(qTimeAction.getMainProcessDefinition(), detailInfo.getReworkRouteID())
                                        && qTimeAction.getReasonCode().equals(detailInfo.getReasonCodeID())) {
                                    int ftrwkCnt;
                                    for (ftrwkCnt = 0; ftrwkCnt < futureReworkCancelCnt; ftrwkCnt++) {
                                        if (futureReworkInfo.getLotID().equals(strFutureReworkCancelList.get(ftrwkCnt).getLotID())
                                                && futureReworkInfo.getRouteID().equals(strFutureReworkCancelList.get(ftrwkCnt).getRouteID())
                                                && futureReworkInfo.getOperationNumber().equals(strFutureReworkCancelList.get(ftrwkCnt).getOperationNumber())) {
                                            break;
                                        }
                                    }
                                    if (ftrwkCnt < futureReworkCancelCnt) {
                                        strFutureReworkCancelList.get(ftrwkCnt).getFutureReworkDetailInfoList().add(futureReworkDetailInfoList.get(ftrwkCnt));
                                    } else {
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setLotID(futureReworkInfo.getLotID());
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setRouteID(futureReworkInfo.getRouteID());
                                        strFutureReworkCancelList.get(futureReworkCancelCnt).setOperationNumber(futureReworkInfo.getOperationNumber());
                                        futureReworkCancelCnt++;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        //--------------------------------------------------
        // Make data to return
        //--------------------------------------------------
        //----- The lot hold actions -------//
        Iterator<Map.Entry<String, Infos.LotHoldReq>> it = tmpLotHoldReleaseList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Infos.LotHoldReq> entry = it.next();
            if (tmpLotHoldRemainList.containsKey(entry.getKey())) {
                it.remove();
            } else {
                Infos.LotHoldReq lotHoldReq = entry.getValue();
                ProductDTO.HoldRecord holdRecord = new ProductDTO.HoldRecord();
                holdRecord.setHoldType(lotHoldReq.getHoldType());
                ObjectIdentifier holdReasonCodeID = lotHoldReq.getHoldReasonCodeID();
                ObjectIdentifier holdUserID = lotHoldReq.getHoldUserID();
                holdRecord.setReasonCode(holdReasonCodeID);
                holdRecord.setHoldPerson(holdUserID);
                holdRecord = lot.findHoldRecord(holdRecord);
                if (null == holdRecord || CimObjectUtils.isEmpty(holdRecord.getHoldType())) {
                    it.remove();
                }
            }
        }
        //----- The future hold actions -------//
        it = tmpFutureHoldCancelList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Infos.LotHoldReq> entry = it.next();
            if (tmpFutureHoldRemainList.containsKey(entry.getKey())) {
                it.remove();
            } else {
                //===== Check whether a future hold request exists or not =======//
                Infos.LotHoldReq strGetFutureHold = entry.getValue();
                ProductDTO.FutureHoldRecord strSearchFutureHold=new ProductDTO.FutureHoldRecord();
                strSearchFutureHold.setHoldType              ( strGetFutureHold.getHoldType());
                strSearchFutureHold.setReasonCode            ( strGetFutureHold.getHoldReasonCodeID());
                strSearchFutureHold.setRequestPerson         ( strGetFutureHold.getHoldUserID());
                strSearchFutureHold.setMainProcessDefinition ( strGetFutureHold.getRouteID());
                strSearchFutureHold.setOperationNumber       ( strGetFutureHold.getOperationNumber());

                ProductDTO.FutureHoldRecord aFutureHold;
                aFutureHold = lot.findFutureHoldRecord( strSearchFutureHold );

                if( aFutureHold == null || CimStringUtils.length( aFutureHold.getHoldType() ) < 1 ) {
                    log.info("A future hold action is not registered.={}", entry.getKey());
                    tmpFutureHoldCancelList.remove( entry.getKey() );
                }
            }
        }

        Outputs.ObjLotQtimeInfoGetForClearOut objLotQtimeInfoGetForClearOut = new Outputs.ObjLotQtimeInfoGetForClearOut();
        objLotQtimeInfoGetForClearOut.setQTimeClearList(qTimeClearList);
        objLotQtimeInfoGetForClearOut.setStrLotHoldReleaseList(new ArrayList<>(tmpLotHoldReleaseList.values()));
        objLotQtimeInfoGetForClearOut.setStrFutureHoldCancelList(new ArrayList<>(tmpFutureHoldCancelList.values()));
        objLotQtimeInfoGetForClearOut.setStrFutureReworkCancelList(strFutureReworkCancelList);
        return objLotQtimeInfoGetForClearOut;
    }
}
