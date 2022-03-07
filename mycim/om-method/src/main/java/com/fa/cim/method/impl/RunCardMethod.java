package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.runcard.CimRunCardDO;
import com.fa.cim.entity.nonruntime.runcard.CimRunCardPsmDO;
import com.fa.cim.entity.nonruntime.runcard.CimRunCardPsmDocDO;
import com.fa.cim.entitysuper.NonRuntimeEntity;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.product.CimPlannedSplitJob;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/15                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/15 17:04
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class RunCardMethod implements IRunCardMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IExperimentalMethod experimentalMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Override
    public Infos.RunCardInfo getRunCardInfo(Infos.ObjCommon objCommon,String runCardID) {
        //Check runCardID exsit
        log.info("Check runCard exsit");
        if (CimStringUtils.isEmpty(runCardID)){
            return null;
        }
        Infos.RunCardInfo runCardInfo = null;

        String sql = "SELECT ID, " +
                "RUNCARD_ID, " +
                "LOT_ID, " +
                "LOT_RKEY, " +
                "RUNCARD_STATE, " +
                "OWNER_ID, " +
                "OWNER_RKEY, " +
                "EXT_APROVAL_FLAG, " +
                "CREATE_TIME, " +
                "UPDATE_TIME, " +
                "APPROVERS, " +
                "TRX_MEMO, " +
                "AUTO_COMPLETE_FLAG " +
                "FROM RUNCARD WHERE RUNCARD_ID = ?1 ORDER BY CREATE_TIME";
        //Query runCardInfo
        log.info("Query runCardInfo");
        Object[] queryResutls = cimJpaRepository.queryOne(sql, runCardID);
        List<String> approveUsers = null;
        if (null != queryResutls){
            runCardInfo = new Infos.RunCardInfo();
            runCardInfo.setRunCardID((String)(queryResutls[1]));
            runCardInfo.setLotID(ObjectIdentifier.build((String)(queryResutls[2]),(String)(queryResutls[3])));
            runCardInfo.setRunCardState(String.valueOf(queryResutls[4]));
            runCardInfo.setOwner(ObjectIdentifier.build((String)(queryResutls[5]),(String)(queryResutls[6])));
            runCardInfo.setExtApprovalFlag(CimBooleanUtils.getBoolean(String.valueOf(queryResutls[7])));
            runCardInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (queryResutls[8])));
            runCardInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (queryResutls[9])));
            //approve Users convert to List
            if (CimStringUtils.isNotEmpty((String)(queryResutls[10]))){
                approveUsers = CimArrayUtils.convertSplitFormatStringToList((String)(queryResutls[10]), BizConstant.SEPARATOR_COMMA);
            }
            runCardInfo.setApprovers(approveUsers);
            runCardInfo.setClaimMemo((String)(queryResutls[11]));
            runCardInfo.setAutoCompleteFlag(null != queryResutls[12] && CimBooleanUtils.getBoolean(String.valueOf(queryResutls[12])));

            String refkey = (String)(queryResutls[0]);
            List<Object[]> psmResultList = cimJpaRepository.query("SELECT ID, PSM_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME,PSM_KEY FROM RUNCARD_PSM WHERE REFKEY = ?1 ORDER BY CREATE_TIME", refkey);
            List<Infos.RunCardPsmInfo> psmInfoList = new ArrayList<>();
            runCardInfo.setRunCardPsmDocInfos(psmInfoList);
            if (CimArrayUtils.isNotEmpty(psmResultList)){
                for (Object[] psmResult : psmResultList) {
                    Infos.RunCardPsmInfo runCardPsmInfo = new Infos.RunCardPsmInfo();
                    psmInfoList.add(runCardPsmInfo);
                    runCardPsmInfo.setPsmJobID((String)(psmResult[1]));
                    runCardPsmInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmResult[2]));
                    runCardPsmInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[3])));
                    runCardPsmInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[4])));
                    runCardPsmInfo.setPsmKey((String)(psmResult[5]));

                    //query RUNCARD_PSM_DOC
                    String psmJobID = (String)(psmResult[1]);
                    List<Infos.RunCardPsmDocInfo> runCardPsmDocInfoList = new ArrayList<>();
                    runCardPsmInfo.setPsmDocInfos(runCardPsmDocInfoList);
                    List<Object[]> psmDocResultList = cimJpaRepository.query("SELECT DOC_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME,PSM_KEY,PSM_JOB_ID FROM RUNCARD_PSM_DOC WHERE PSM_JOB_ID = ?1 ORDER BY CREATE_TIME", psmJobID);
                    if (CimArrayUtils.isNotEmpty(psmDocResultList)){
                        for (Object[] psmDocResult : psmDocResultList) {
                            Infos.RunCardPsmDocInfo runCardPsmDocInfo = new Infos.RunCardPsmDocInfo();
                            runCardPsmDocInfoList.add(runCardPsmDocInfo);
                            runCardPsmDocInfo.setDocJobID((String)(psmDocResult[0]));
                            runCardPsmDocInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmDocResult[1]));
                            runCardPsmDocInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[2])));
                            runCardPsmDocInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[3])));
                            runCardPsmDocInfo.setPsmKey((String)(psmDocResult[4]));
                            runCardPsmDocInfo.setPsmJobID((String)(psmDocResult[5]));
                        }
                    }
                }
            }
        }
        return runCardInfo;
    }


    @Override
    public List<String> getApproveUsersFromUserGroupAndFunction(String userGroupID,String functionID) {
        List<String> approveGroupUsers = new ArrayList<>();
        List<String> approveFunctionUsers = new ArrayList<>();
        String sql = "SELECT A.USER_GRP_ID, B.USER_ID FROM OMUSER_USERGRP A ,OMUSER B WHERE A.USER_GRP_ID = ?1 AND A.REFKEY = B.ID";
        List<Object[]> queryResults = cimJpaRepository.query(sql, userGroupID);
        if (CimArrayUtils.isNotEmpty(queryResults)){
            for (Object[] queryResutl : queryResults) {
                approveGroupUsers.add(CimObjectUtils.toString(queryResutl[1]));
            }
        }else {
            Validations.check(true,retCodeConfigEx.getNotFoundRunCardApprovalUserGroup());
        }
        String subSystemID = BizConstant.SP_SUBSYSTEMID_MM;
        String permission = BizConstant.SP_MM_PERMISSION_ACCESS;
        String sqlTemp = "SELECT OMACCESSGRP_ACCESS.LINK_KEY,OMUSER.USER_ID FROM OMUSER_ACCESSGRP, OMACCESSGRP, OMACCESSGRP_ACCESS, OMUSER\n" +
                "                         WHERE OMACCESSGRP.ID = OMUSER_ACCESSGRP.ACCESS_GRP_RKEY\n" +
                "                         AND OMUSER_ACCESSGRP.REFKEY = OMUSER.ID \n" +
                "                         AND OMACCESSGRP.SERVICES_ID = ?1 AND OMACCESSGRP_ACCESS.REFKEY = OMACCESSGRP.ID\n" +
                "                         AND OMACCESSGRP_ACCESS.LINK_KEY = ?2 AND OMACCESSGRP_ACCESS.PERMISSION = ?3 ";
        List<Object[]> queryResutlsTemp = cimJpaRepository.query(sqlTemp, subSystemID, functionID, permission);
        if (CimArrayUtils.isNotEmpty(queryResutlsTemp)){
            for (Object[] queryResutl : queryResutlsTemp) {
                approveFunctionUsers.add(CimObjectUtils.toString(queryResutl[1]));
            }
        }else {
            Validations.check(CimArrayUtils.isEmpty(approveFunctionUsers),retCodeConfigEx.getNoSettingApproveUser());
        }
        List<String> result = approveGroupUsers.stream().filter(approveFunctionUsers::contains).collect(Collectors.toList());
        return result;
    }

    @Override
    public Infos.RunCardInfo getRunCardFromLotID(ObjectIdentifier lotID) {
        Infos.RunCardInfo runCardInfo = null;
        //Check lotID exsit
        log.info("Check lotID exsit");
        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == lot,retCodeConfig.getNotFoundLot());

        String sql = "SELECT ID, " +
                "RUNCARD_ID, " +
                "LOT_ID, " +
                "LOT_RKEY, " +
                "RUNCARD_STATE, " +
                "OWNER_ID, " +
                "OWNER_RKEY, " +
                "EXT_APROVAL_FLAG, " +
                "CREATE_TIME, " +
                "UPDATE_TIME, " +
                "APPROVERS, " +
                "TRX_MEMO, " +
                "AUTO_COMPLETE_FLAG " +
                "FROM RUNCARD WHERE LOT_ID = ?1 ORDER BY CREATE_TIME";

        //Query runCardInfo
        log.info("Query runCardInfo");
        Object[] queryResutls = cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(lotID));
        List<String> approveUsers = null;
        if (null != queryResutls){
            runCardInfo = new Infos.RunCardInfo();
            runCardInfo.setRunCardID((String)(queryResutls[1]));
            runCardInfo.setLotID(ObjectIdentifier.build((String)(queryResutls[2]),(String)(queryResutls[3])));
            runCardInfo.setRunCardState(String.valueOf(queryResutls[4]));
            runCardInfo.setOwner(ObjectIdentifier.build((String)(queryResutls[5]),(String)(queryResutls[6])));
            runCardInfo.setExtApprovalFlag(CimBooleanUtils.getBoolean(String.valueOf(queryResutls[7])));
            runCardInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (queryResutls[8])));
            runCardInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (queryResutls[9])));
            //approve Users convert to List
            if (CimStringUtils.isNotEmpty((String)(queryResutls[10]))){
                approveUsers = CimArrayUtils.convertSplitFormatStringToList((String)(queryResutls[10]), BizConstant.SEPARATOR_COMMA);
            }
            runCardInfo.setApprovers(approveUsers);
            runCardInfo.setClaimMemo((String)(queryResutls[11]));
            runCardInfo.setAutoCompleteFlag(null != queryResutls[12] && CimBooleanUtils.getBoolean(String.valueOf(queryResutls[12])));

            String refkey = (String)(queryResutls[0]);
            List<Object[]> psmResultList = cimJpaRepository.query("SELECT ID, PSM_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME,PSM_KEY FROM RUNCARD_PSM WHERE REFKEY = ?1 ORDER BY CREATE_TIME", refkey);
            List<Infos.RunCardPsmInfo> psmInfoList = new ArrayList<>();
            runCardInfo.setRunCardPsmDocInfos(psmInfoList);
            if (CimArrayUtils.isNotEmpty(psmResultList)){
                for (Object[] psmResult : psmResultList) {
                    Infos.RunCardPsmInfo runCardPsmInfo = new Infos.RunCardPsmInfo();
                    psmInfoList.add(runCardPsmInfo);
                    runCardPsmInfo.setPsmJobID((String)(psmResult[1]));
                    runCardPsmInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmResult[2]));
                    runCardPsmInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[3])));
                    runCardPsmInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[4])));
                    runCardPsmInfo.setPsmKey((String)(psmResult[5]));

                    //query RUNCARD_PSM_DOC
                    String psmJobID = (String)(psmResult[1]);
                    List<Infos.RunCardPsmDocInfo> runCardPsmDocInfoList = new ArrayList<>();
                    runCardPsmInfo.setPsmDocInfos(runCardPsmDocInfoList);
                    List<Object[]> psmDocResultList = cimJpaRepository.query("SELECT DOC_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME, PSM_KEY, PSM_JOB_ID FROM RUNCARD_PSM_DOC WHERE PSM_JOB_ID = ?1 ORDER BY CREATE_TIME", psmJobID);
                    if (CimArrayUtils.isNotEmpty(psmDocResultList)){
                        for (Object[] psmDocResult : psmDocResultList) {
                            Infos.RunCardPsmDocInfo runCardPsmDocInfo = new Infos.RunCardPsmDocInfo();
                            runCardPsmDocInfoList.add(runCardPsmDocInfo);
                            runCardPsmDocInfo.setDocJobID((String)(psmDocResult[0]));
                            runCardPsmDocInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmDocResult[1]));
                            runCardPsmDocInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[2])));
                            runCardPsmDocInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[3])));
                            runCardPsmDocInfo.setPsmKey((String)(psmDocResult[4]));
                            runCardPsmDocInfo.setPsmJobID((String)(psmDocResult[5]));
                        }
                    }
                }
            }
        }
        return runCardInfo;
    }

    @Override
    public void  updateRunCardInfo(Infos.ObjCommon objCommon, Infos.RunCardInfo runCardInfoUpdate) {
        //Get runCardInfo and check runCard exsit
        log.info("Get runCardInfo and check runCard exsit");
        Boolean createFlag = null;
        if (CimStringUtils.isNotEmpty(runCardInfoUpdate.getRunCardID())){
            Infos.RunCardInfo checkRunCardInfo = this.getRunCardInfo(objCommon,runCardInfoUpdate.getRunCardID());
            createFlag = null == checkRunCardInfo;
        }else {
            createFlag = true;
        }

        //Construct runCard data
        log.info("Construct runCard data");
        CimRunCardDO cimRunCardDO = null;
        if (CimBooleanUtils.isFalse(createFlag)){
            //RunCard data exsit and update data
            log.info("RunCard data exsit and update data");
            cimRunCardDO = cimJpaRepository.queryOne("SELECT * FROM RUNCARD WHERE RUNCARD_ID = ?1", CimRunCardDO.class, runCardInfoUpdate.getRunCardID());
            Validations.check(null == cimRunCardDO,retCodeConfigEx.getNotFoundRunCard());
        }else {
            //RunCard data doesn't exsit and create data
            log.info("RunCard data doesn't exsit and create data");
            cimRunCardDO = new CimRunCardDO();
        }

        cimRunCardDO.setRunCardID(runCardInfoUpdate.getRunCardID());
        cimRunCardDO.setApprovers(CimStringUtils.join(runCardInfoUpdate.getApprovers(), BizConstant.SEPARATOR_COMMA));
        cimRunCardDO.setExtApprovalFlag(runCardInfoUpdate.getExtApprovalFlag());
        cimRunCardDO.setLotID(ObjectIdentifier.fetchValue(runCardInfoUpdate.getLotID()));
        cimRunCardDO.setLotObj(ObjectIdentifier.fetchReferenceKey(runCardInfoUpdate.getLotID()));
        cimRunCardDO.setOwnerID(ObjectIdentifier.fetchValue(runCardInfoUpdate.getOwner()));
        cimRunCardDO.setOwnerObj(ObjectIdentifier.fetchReferenceKey(runCardInfoUpdate.getOwner()));
        cimRunCardDO.setRunCardSate(runCardInfoUpdate.getRunCardState());
        cimRunCardDO.setClaimMemo(runCardInfoUpdate.getClaimMemo());
        cimRunCardDO.setAutoCompleteFlag(runCardInfoUpdate.getAutoCompleteFlag());
        cimRunCardDO.setUpdateTime(CimDateUtils.convertTo(runCardInfoUpdate.getUpdateTime()));
        cimRunCardDO.setCreateTime(CimDateUtils.convertTo(runCardInfoUpdate.getCreateTime()));

        //Update runCard data
        log.info("Update runCard data");
        CimRunCardDO runCardDo = cimJpaRepository.save(cimRunCardDO);
        String runCardDoID = runCardDo.getId();

        CimRunCardPsmDO cimRunCardPsmDO = null;
        CimRunCardPsmDocDO cimRunCardPsmDocDO = null;

        //Check psm/doc info exsit and update
        log.info("Check psm info exsit and update");
        if (CimArrayUtils.isNotEmpty(runCardInfoUpdate.getRunCardPsmDocInfos())){
            for (int i = 0; i < CimArrayUtils.getSize(runCardInfoUpdate.getRunCardPsmDocInfos()); i++) {
                //CREATE RUNCARD_PSM
                cimRunCardPsmDO = new CimRunCardPsmDO();
                cimRunCardPsmDO.setCreateTime(CimDateUtils.getCurrentTimeStamp());
                cimRunCardPsmDO.setUpdateTime(null);
                cimRunCardPsmDO.setPsmJobID(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmJobID());
                cimRunCardPsmDO.setReferenceKey(runCardDoID);
                cimRunCardPsmDO.setSequenceNumber(i);
                cimRunCardPsmDO.setPsmKey(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmKey());
                //UPDATE RUNCARD_PSM
                CimRunCardPsmDO runCardPsmDO = cimJpaRepository.save(cimRunCardPsmDO);
                String runCardPsmDOPsmJobID = runCardPsmDO.getPsmJobID();

                //Check doc info exsit and update
                log.info("Check doc info exsit and update");
                if (CimArrayUtils.isNotEmpty(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmDocInfos())){
                    for (int j = 0; j < CimArrayUtils.getSize(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmDocInfos()); j++) {
                        //CREATE RUNCARD_PSM_DOC
                        cimRunCardPsmDocDO = new CimRunCardPsmDocDO();
                        cimRunCardPsmDocDO.setCreateTime(CimDateUtils.getCurrentTimeStamp());
                        cimRunCardPsmDocDO.setUpdateTime(CimDateUtils.convertTo(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmDocInfos().get(j).getUpdateTime()));
                        cimRunCardPsmDocDO.setDocJobID(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmDocInfos().get(j).getDocJobID());
                        cimRunCardPsmDocDO.setPsmJobID(runCardPsmDOPsmJobID);
                        cimRunCardPsmDocDO.setSequenceNumber(j);
                        cimRunCardPsmDocDO.setPsmKey(runCardInfoUpdate.getRunCardPsmDocInfos().get(i).getPsmDocInfos().get(j).getPsmKey());
                        //UPDATE RUNCARD_PSM_DOC
                        cimJpaRepository.save(cimRunCardPsmDocDO);
                    }
                }
            }
        }
        //Sort rest runCard psm info by refkey and createTime
        log.info("Sort rest runCard psm info by refkey and createTime");
        this.sortRunCardInfo(runCardDoID);
    }

    @Override
    public void removePsmDocRunCardInfo(Infos.ObjCommon objCommon,String psmJobID,String psmKey,String runCardID) {
        Validations.check(CimStringUtils.isEmpty(psmJobID),retCodeConfig.getInvalidInputParam());
        //Remove runCard psm info by psmKey
        log.info("Remove runCard psm info by psmKey");
        List<CimRunCardPsmDO> runCardPsmList = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM WHERE PSM_JOB_ID = ?1", CimRunCardPsmDO.class, psmJobID);
        if (CimArrayUtils.isNotEmpty(runCardPsmList)){
            for (CimRunCardPsmDO cimRunCardPsmDO : runCardPsmList) {
                //Remove doc info
                log.info("Remove doc info");
                List<CimRunCardPsmDocDO> cimRunCardPsmDocDOS = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM_DOC WHERE PSM_JOB_ID = ?1 AND PSM_KEY= ?2", CimRunCardPsmDocDO.class, cimRunCardPsmDO.getPsmJobID(), psmKey);
                if (CimArrayUtils.isNotEmpty(cimRunCardPsmDocDOS)){
                    for (CimRunCardPsmDocDO cimRunCardPsmDocDO : cimRunCardPsmDocDOS) {
                        List<String> docIDs = Arrays.asList(cimRunCardPsmDocDO.getDocJobID());
                        //Get doc info
                        log.info("Get doc info");
                        Inputs.ObjFPCInfoGetDRIn docParam = new Inputs.ObjFPCInfoGetDRIn();
                        docParam.setFPCIDs(docIDs);
                        docParam.setLotID(null);
                        docParam.setLotFamilyID(null);
                        docParam.setMainPDID(null);
                        docParam.setMainOperNo("");
                        docParam.setOrgMainPDID(null);
                        docParam.setOrgOperNo("");
                        docParam.setSubMainPDID(null);
                        docParam.setSubOperNo("");
                        docParam.setEquipmentID(null);
                        docParam.setWaferIDInfoGetFlag(true);
                        docParam.setRecipeParmInfoGetFlag(true);
                        docParam.setReticleInfoGetFlag(true);
                        docParam.setDcSpecItemInfoGetFlag(true);
                        List<Infos.FPCInfo> docInfoResult = fpcMethod.fpcInfoGetDR(objCommon, docParam);

                        List<Infos.FPCInfoAction> docInfoActionList = new ArrayList<>();
                        Optional.ofNullable(docInfoResult).ifPresent(list -> list.forEach(data ->{
                            Infos.FPCInfoAction docInfoAction = new Infos.FPCInfoAction();
                            docInfoAction.setActionType(BizConstant.SP_FPCINFO_DELETE);
                            docInfoAction.setStrFPCInfo(data);
                            docInfoActionList.add(docInfoAction);
                        }));
                        //Remove doc data
                        log.info("Remove doc data and runCard doc data");
                        fpcMethod.fpcInfoDeleteDR(objCommon, docIDs);

                        //Make doc history single by runcard operation
                        log.info("Make doc history single by runcard operation");
                        eventMethod.fpcInfoRegistEventMake(objCommon, TransactionIDEnum.FPCDELETE_REQ.getValue(), docInfoActionList, null,runCardID);
                    }
                }

                //Remove runcard psm data
                log.info("Remove runcard psm data");
                this.removeEntitys("SELECT * FROM RUNCARD_PSM WHERE PSM_JOB_ID = ?1 AND PSM_KEY =?2", CimRunCardPsmDO.class,psmJobID,psmKey);

                //Sort rest runCard psm info by refkey and createTime
                log.info("Sort rest runCard psm info by refkey and createTime");
                this.sortRunCardInfo(cimRunCardPsmDO.getReferenceKey());
            }
        }
    }

    @Override
    public void removeAllRunCardInfo(Infos.ObjCommon objCommon, String runCardID,Boolean removeFRRUNCARDFlag) {
        //Get runCardInfo
        log.info("Get runCardInfo");
        CimRunCardDO cimRunCardDO = cimJpaRepository.queryOne("SELECT * FROM RUNCARD WHERE RUNCARD_ID = ?1", CimRunCardDO.class, runCardID);
        Validations.check(null == cimRunCardDO,retCodeConfigEx.getNotFoundRunCard());

        //Remove runCard data
        log.info("Remove runCard data");
        if (CimBooleanUtils.isTrue(removeFRRUNCARDFlag)){
            this.removeEntitys("SELECT * FROM RUNCARD WHERE RUNCARD_ID = ?1", CimRunCardDO.class,cimRunCardDO.getRunCardID());
        }
        List<CimRunCardPsmDO> runCardPsmList = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM WHERE REFKEY = ?1", CimRunCardPsmDO.class, cimRunCardDO.getId());

        //Check psm data exsit and delete
        log.info("Check psm data exsit and delete");
        if (CimArrayUtils.isNotEmpty(runCardPsmList)){
            Set<String> psmJobIDs = new HashSet<>();
            runCardPsmList.forEach(cimRunCardPsmDO -> psmJobIDs.add(cimRunCardPsmDO.getPsmJobID()));
            for (String psmJobID : psmJobIDs) {
                //Get psm info
                log.info("Get psm info");
                CimPlannedSplitJob plannedSplitJob = baseCoreFactory.getBO(CimPlannedSplitJob.class, ObjectIdentifier.buildWithValue(psmJobID));
                Validations.check(null == plannedSplitJob, retCodeConfig.getNotFoundExperimentalLotData());

                ObjectIdentifier lotFamilyID = ObjectIdentifier.build(plannedSplitJob.getLotFamily().getIdentifier(),plannedSplitJob.getLotFamily().getPrimaryKey());
                ObjectIdentifier splitRouteID = ObjectIdentifier.build(plannedSplitJob.getSplitMainProcessDefinition().getIdentifier(), plannedSplitJob.getSplitMainProcessDefinition().getPrimaryKey());
                String splitOperationNumber = plannedSplitJob.getSplitOperationNumber();
                String originalOperationNumber = plannedSplitJob.getOriginalOperationNumber();
                ObjectIdentifier originalRouteID = ObjectIdentifier.build(plannedSplitJob.getOriginalMainProcessDefinition().getIdentifier(), plannedSplitJob.getOriginalMainProcessDefinition().getPrimaryKey());
                ProductDTO.PlannedSplitJobInfo plannedSplitJobInfo = plannedSplitJob.getPlannedSplitJobInfo();
                //Remove psm data
                log.info("Remove psm data");
                productManager.removePlannedSplitJobFor(ObjectIdentifier.fetchValue(lotFamilyID),
                        ObjectIdentifier.fetchValue(splitRouteID),
                        splitOperationNumber,
                        ObjectIdentifier.fetchValue(originalRouteID),
                        originalOperationNumber);

                //Make psm history single by runCard operation
                log.info("Make psm history single by runCard operation");
                this.makePsmHistoryEventByRunCard(objCommon,psmJobID,lotFamilyID,splitRouteID,splitOperationNumber,originalOperationNumber,originalRouteID,plannedSplitJobInfo,runCardID);

                //Check doc data
                log.info("Check doc data");
                List<CimRunCardPsmDocDO> runCardPsmDocDatas = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM_DOC WHERE PSM_JOB_ID = ?1", CimRunCardPsmDocDO.class, psmJobID);
                if (CimArrayUtils.isNotEmpty(runCardPsmDocDatas)){
                    for (CimRunCardPsmDocDO runCardPsmDocData : runCardPsmDocDatas) {

                        List<String> docIDs = Arrays.asList(runCardPsmDocData.getDocJobID());
                        //Get doc info
                        log.info("Get doc info");
                        Inputs.ObjFPCInfoGetDRIn docParam = new Inputs.ObjFPCInfoGetDRIn();
                        docParam.setFPCIDs(docIDs);
                        docParam.setLotID(null);
                        docParam.setLotFamilyID(null);
                        docParam.setMainPDID(null);
                        docParam.setMainOperNo("");
                        docParam.setOrgMainPDID(null);
                        docParam.setOrgOperNo("");
                        docParam.setSubMainPDID(null);
                        docParam.setSubOperNo("");
                        docParam.setEquipmentID(null);
                        docParam.setWaferIDInfoGetFlag(true);
                        docParam.setRecipeParmInfoGetFlag(true);
                        docParam.setReticleInfoGetFlag(true);
                        docParam.setDcSpecItemInfoGetFlag(true);
                        List<Infos.FPCInfo> docInfoResult = fpcMethod.fpcInfoGetDR(objCommon, docParam);

                        List<Infos.FPCInfoAction> docInfoActionList = new ArrayList<>();
                        Optional.ofNullable(docInfoResult).ifPresent(list -> list.forEach(data ->{
                            Infos.FPCInfoAction docInfoAction = new Infos.FPCInfoAction();
                            docInfoAction.setActionType(BizConstant.SP_FPCINFO_DELETE);
                            docInfoAction.setStrFPCInfo(data);
                            docInfoActionList.add(docInfoAction);
                        }));

                        //Remove doc data
                        log.info("Remove doc data and runCard doc data");
                        fpcMethod.fpcInfoDeleteDR(objCommon,docIDs);

                        //Make doc history single by runcard operation
                        log.info("Make doc history single by runcard operation");
                        eventMethod.fpcInfoRegistEventMake(objCommon, TransactionIDEnum.FPCDELETE_REQ.getValue(), docInfoActionList, null,cimRunCardDO.getRunCardID());
                    }
                }
            }
        }
        //Remove runCard psm data
        log.info("Remove runCard psm data");
        this.removeEntitys("SELECT * FROM RUNCARD_PSM WHERE REFKEY = ?1", CimRunCardPsmDO.class,cimRunCardDO.getId());
    }

    @Override
    public void makePsmHistoryEventByRunCard(Infos.ObjCommon objCommon,
                                             String psmJobID,
                                             ObjectIdentifier lotFamilyID,
                                             ObjectIdentifier splitRouteID,
                                             String splitOperationNumber,
                                             String originalOperationNumber,
                                             ObjectIdentifier originalRouteID,
                                             ProductDTO.PlannedSplitJobInfo plannedSplitJobInfo,
                                             String runCardID) {
        Infos.ExperimentalLotRegistInfo experimentalLotRegistInfo = new Infos.ExperimentalLotRegistInfo();
        experimentalLotRegistInfo.setRunCardID(runCardID);
        experimentalLotRegistInfo.setPsmJobID(psmJobID);
        experimentalLotRegistInfo.setUserID(objCommon.getUser().getUserID().getValue());
        experimentalLotRegistInfo.setAction(BizConstant.SP_EWR_ACTION_DELETE);
        experimentalLotRegistInfo.setLotFamilyID(lotFamilyID);
        experimentalLotRegistInfo.setSplitRouteID(splitRouteID);
        experimentalLotRegistInfo.setSplitOperationNumber(splitOperationNumber);
        experimentalLotRegistInfo.setOriginalRouteID(originalRouteID);
        experimentalLotRegistInfo.setOriginalOperationNumber(originalOperationNumber);
        experimentalLotRegistInfo.setActionEMail(plannedSplitJobInfo.isActionEMail());
        experimentalLotRegistInfo.setActionHold(plannedSplitJobInfo.isActionHold());
        experimentalLotRegistInfo.setTestMemo(plannedSplitJobInfo.getTestMemo());

        int lenDetailInfoSeq = CimArrayUtils.getSize(plannedSplitJobInfo.getPlannedSplitJobInfoDetails());
        List<Infos.ExperimentalLotRegist> strExperimentalLotRegistSeq = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(plannedSplitJobInfo.getPlannedSplitJobInfoDetails())){
            for (ProductDTO.PlannedSplitJobInfoDetail plannedSplitJobInfoDetail : plannedSplitJobInfo.getPlannedSplitJobInfoDetails()) {
                Infos.ExperimentalLotRegist experimentalLotRegist = new Infos.ExperimentalLotRegist();
                experimentalLotRegist.setSubRouteID(plannedSplitJobInfoDetail.getSubRouteID());
                experimentalLotRegist.setReturnOperationNumber(plannedSplitJobInfoDetail.getReturnOperationNumber());
                experimentalLotRegist.setMergeOperationNumber(plannedSplitJobInfoDetail.getMergeOperationNumber());
                experimentalLotRegist.setMemo(plannedSplitJobInfoDetail.getMemo());
                experimentalLotRegist.setWaferIDs(plannedSplitJobInfoDetail.getWafers());
                strExperimentalLotRegistSeq.add(experimentalLotRegist);
            }
        }
        experimentalLotRegistInfo.setStrExperimentalLotRegistSeq(strExperimentalLotRegistSeq);
        String tx = TransactionIDEnum.EXPERIMENTAL_LOT_DELETE_REQ.getValue();
        String testMemo = experimentalLotRegistInfo.getTestMemo();
        eventMethod.experimentalLotRegistEventMake(objCommon, tx, testMemo, experimentalLotRegistInfo);
    }

    @Override
    public Infos.RunCardPsmKeyInfo getRunCardPsmKeyInfoByPSMKey(String psmKey) {
        if (CimStringUtils.isEmpty(psmKey)) return null;
        Infos.RunCardPsmKeyInfo psmKeyInfo = new Infos.RunCardPsmKeyInfo();
        String[] split = psmKey.split(BizConstant.HYPHEN);
        psmKeyInfo.setSubRouteID(split[0]);
        psmKeyInfo.setSplitOperationNumber(split[1]);
        String[] wafers = split[2].split(BizConstant.SEPARATOR_COMMA);
        psmKeyInfo.setWaferList(Arrays.asList(wafers));
        return psmKeyInfo;
    }


    private void sortRunCardInfo(String runCardDoID) {
        List<CimRunCardPsmDO> psmList = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM WHERE REFKEY = ?1 ORDER BY CREATE_TIME", CimRunCardPsmDO.class, runCardDoID);
        if (CimArrayUtils.isNotEmpty(psmList)){
            for (int i = 0; i < psmList.size(); i++) {
                psmList.get(i).setSequenceNumber(i);
                cimJpaRepository.save(psmList.get(i));
            }
        }
    }

    @Override
    public boolean isRunCard(List<ObjectIdentifier> waferIDs) {
        String sql = "SELECT LOT_ID FROM OMWAFER f WHERE f.WAFER_ID = ?1";
        String waferLotID = (String) cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(waferIDs.get(0)))[0];
        boolean sameLotFlag = waferIDs.stream().allMatch(x -> cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(x))[0].equals(waferLotID));

        if (CimBooleanUtils.isTrue(sameLotFlag)) {
            log.info("Get runCard Info by lotID: {}",waferLotID);
            Infos.RunCardInfo runCardInfo = getRunCardFromLotID(new ObjectIdentifier(waferLotID));
            if (null == runCardInfo){
                String sqlTemp = "SELECT a.SPLIT_LOT_ID FROM OMLOT a WHERE a.LOT_ID = (SELECT LOT_ID FROM OMWAFER b WHERE b.WAFER_ID = ?1)";
                String splitWaferLotID = (String) cimJpaRepository.queryOne(sqlTemp, ObjectIdentifier.fetchValue(waferIDs.get(0)))[0];
                if (null != splitWaferLotID) {
                    boolean sameSplitLotFlag = waferIDs.stream().allMatch(x -> {
                        Object[] objects = cimJpaRepository.queryOne(sqlTemp, ObjectIdentifier.fetchValue(x));
                        return objects[0] != null && objects[0].equals(splitWaferLotID);
                    });
                    if (CimBooleanUtils.isTrue(sameSplitLotFlag)) {
                        runCardInfo = getRunCardFromLotID(new ObjectIdentifier(splitWaferLotID));
                    }
                }
            }
            return !CimObjectUtils.isEmpty(runCardInfo);
        }
        return false;
    }

    @Override
    public Infos.RunCardInfo getSplitLotRunCardInfo(List<ObjectIdentifier> waferIDs) {
        Infos.RunCardInfo runCardInfo = null;
        String sql = "SELECT LOT_ID FROM OMWAFER f WHERE f.WAFER_ID = ?1";
        String waferLotID = (String) cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(waferIDs.get(0)))[0];
        boolean sameLotFlag = waferIDs.stream().allMatch(x -> cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(x))[0].equals(waferLotID));

        if (CimBooleanUtils.isTrue(sameLotFlag)) {
            runCardInfo = getRunCardFromLotID(new ObjectIdentifier(waferLotID));
        }
        if (null == runCardInfo){
            String sqlTemp = "SELECT a.SPLIT_LOT_ID FROM OMLOT a WHERE a.LOT_ID = (SELECT LOT_ID FROM OMWAFER b WHERE b.WAFER_ID = ?1)";
            String splitWaferLotID = (String) cimJpaRepository.queryOne(sqlTemp, ObjectIdentifier.fetchValue(waferIDs.get(0)))[0];
            if (null != splitWaferLotID) {
                boolean sameSplitLotFlag = waferIDs.stream().allMatch(x -> {
                    Object[] objects = cimJpaRepository.queryOne(sqlTemp, ObjectIdentifier.fetchValue(x));
                    return objects[0] != null && objects[0].equals(splitWaferLotID);
                });
                if (CimBooleanUtils.isTrue(sameSplitLotFlag)) {
                    runCardInfo = getRunCardFromLotID(new ObjectIdentifier(splitWaferLotID));
                }
            }
        }
        return runCardInfo;
    }

    @Override
    public Infos.RunCardInfo getRunCardInfoByPsm(Infos.ObjCommon objCommon,String psmJobID) {
        Infos.RunCardInfo result = null;
        CimRunCardPsmDO runCardPsmDOEx = new CimRunCardPsmDO();
        runCardPsmDOEx.setPsmJobID(psmJobID);
        List<CimRunCardPsmDO> allRunCardPsm = cimJpaRepository.findAll(Example.of(runCardPsmDOEx));
        if (CimArrayUtils.isNotEmpty(allRunCardPsm)){
            String runcardObj = allRunCardPsm.get(0).getReferenceKey();
            boolean match = allRunCardPsm.parallelStream().allMatch(rcPsm ->
                    CimStringUtils.equals(rcPsm.getReferenceKey(), runcardObj));
            if (match){
                CimRunCardDO runCardDOEx = new CimRunCardDO();
                runCardDOEx.setId(runcardObj);
                Optional<CimRunCardDO> runCardOptional = cimJpaRepository.findOne(Example.of(runCardDOEx));
                if (runCardOptional.isPresent()){
                    return this.getRunCardInfo(objCommon,runCardOptional.get().getRunCardID());
                }
            }
        }
        return result;
    }

    @Override
    public Infos.RunCardInfo getRunCardInfoByDoc(Infos.ObjCommon objCommon, Infos.FPCInfo docInfo) {
        //if docID not empty
        Infos.RunCardInfo result = null;
        if (CimStringUtils.isNotEmpty(docInfo.getFpcID())) {
            CimRunCardPsmDocDO runCardPsmDocEx = new CimRunCardPsmDocDO();
            runCardPsmDocEx.setDocJobID(docInfo.getFpcID());
            Optional<CimRunCardPsmDocDO> runCardPsmDocDOOptional = cimJpaRepository
                    .findOne(Example.of(runCardPsmDocEx));
            if (runCardPsmDocDOOptional.isPresent()) {
                result = this.getRunCardByPsmKey(objCommon, runCardPsmDocDOOptional.get().getPsmKey());
            }
        }
        if (null == result) {
            if (CimStringUtils.isNotEmpty(docInfo.getPsmKey())) {
                result = this.getRunCardByPsmKey(objCommon, docInfo.getPsmKey());
            }
        }
        if (null == result) {
            //create psm key by docInfo (but we can not use it to find runcard after split, so the psmKey is safe)
            StringBuilder sb = new StringBuilder();
            String splitOpeNumber = BizConstant.EMPTY;
            if (CimStringUtils.isNotEmpty(docInfo.getSubOperationNumber())){
                splitOpeNumber = docInfo.getSubOperationNumber();
            }else {
                splitOpeNumber = docInfo.getOriginalOperationNumber();
            }
            if (CimArrayUtils.isNotEmpty(docInfo.getLotWaferInfoList())){
                List<String> waferSeq = docInfo.getLotWaferInfoList()
                        .stream()
                        .map(lotWaferInfo -> ObjectIdentifier.fetchValue(lotWaferInfo.getWaferID()))
                        .collect(Collectors.toList());
                sb.append(ObjectIdentifier.fetchValue(docInfo.getMainProcessDefinitionID()))
                        .append(BizConstant.HYPHEN)
                        .append(splitOpeNumber)
                        .append(BizConstant.HYPHEN)
                        .append(CimStringUtils.join(waferSeq, BizConstant.SEPARATOR_COMMA));
            }
            result = this.getRunCardByPsmKey(objCommon, sb.toString());
        }
        return result;
    }

    @Override
    public void setPsmKeyFromPsmInfo(ProductDTO.PlannedSplitJobInfo aPlannedSplitJobInfo) {
        String splitOperationNumber = aPlannedSplitJobInfo.getSplitOperationNumber();
        Optional.ofNullable(aPlannedSplitJobInfo.getPlannedSplitJobInfoDetails()).ifPresent(list -> list.forEach(data ->{
            List<String> waferSeq = new ArrayList<>();
            ObjectIdentifier subRouteID = data.getSubRouteID();
            List<ObjectIdentifier> wafers = data.getWafers();
            wafers.forEach(wafer -> waferSeq.add(wafer.getValue()));
            String waferString = CimStringUtils.join(waferSeq, BizConstant.SEPARATOR_COMMA);
            data.setPsmKey(ObjectIdentifier.fetchValue(subRouteID) + BizConstant.HYPHEN + splitOperationNumber + BizConstant.HYPHEN + waferString);
        }));
    }

    @Override
    public Infos.RunCardPsmKeyInfo getRunCardPsmKeyInfo(String splitOperationNumber, ObjectIdentifier subRouteID, List<ObjectIdentifier> waferIDs, String psmKey) {
        StringBuilder sb = new StringBuilder();
        List<String> waferSeq = new ArrayList<>();
        waferIDs.forEach(wafer -> waferSeq.add(wafer.getValue()));
        sb.append(ObjectIdentifier.fetchValue(subRouteID)).append(BizConstant.HYPHEN).append(splitOperationNumber).append(BizConstant.HYPHEN).append(CimStringUtils.join(waferSeq, BizConstant.SEPARATOR_COMMA));
        if (CimStringUtils.equals(sb.toString(),psmKey)){
            Infos.RunCardPsmKeyInfo runCardPsmKeyInfo = new Infos.RunCardPsmKeyInfo();
            runCardPsmKeyInfo.setSubRouteID(ObjectIdentifier.fetchValue(subRouteID));
            runCardPsmKeyInfo.setSplitOperationNumber(splitOperationNumber);
            runCardPsmKeyInfo.setWaferList(waferSeq);
            return runCardPsmKeyInfo;
        }
        return null;
    }

    @Override
    public void removeRunCardPsmDocFromPsmKey(Infos.ObjCommon objCommon,String changePsmKey,String remveDocPsmKey,Boolean removeDocFlag,String runCardID) {
        if (CimBooleanUtils.isTrue(removeDocFlag)){
            log.info("RunCard removeDocFlag is : {}",removeDocFlag);
            if (CimStringUtils.equals(changePsmKey,remveDocPsmKey)){
                //Delete psm info by Delete psm info by changePsmKey
                log.info("Delete psm info by Delete psm info by changePsmKey");
                List<CimRunCardPsmDocDO> runCardPsmDocs = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM_DOC WHERE PSM_KEY = ?1", CimRunCardPsmDocDO.class, changePsmKey);
                if (CimArrayUtils.isNotEmpty(runCardPsmDocs)){
                    for (CimRunCardPsmDocDO runCardPsmDoc : runCardPsmDocs) {
                        List<String> docIDs = Arrays.asList(runCardPsmDoc.getDocJobID());
                        //Get doc info
                        log.info("Get doc info");
                        Inputs.ObjFPCInfoGetDRIn docParam = new Inputs.ObjFPCInfoGetDRIn();
                        docParam.setFPCIDs(docIDs);
                        docParam.setLotID(null);
                        docParam.setLotFamilyID(null);
                        docParam.setMainPDID(null);
                        docParam.setMainOperNo("");
                        docParam.setOrgMainPDID(null);
                        docParam.setOrgOperNo("");
                        docParam.setSubMainPDID(null);
                        docParam.setSubOperNo("");
                        docParam.setEquipmentID(null);
                        docParam.setWaferIDInfoGetFlag(true);
                        docParam.setRecipeParmInfoGetFlag(true);
                        docParam.setReticleInfoGetFlag(true);
                        docParam.setDcSpecItemInfoGetFlag(true);
                        List<Infos.FPCInfo> docInfoResult = fpcMethod.fpcInfoGetDR(objCommon, docParam);

                        List<Infos.FPCInfoAction> docInfoActionList = new ArrayList<>();
                        Optional.ofNullable(docInfoResult).ifPresent(list -> list.forEach(data ->{
                            Infos.FPCInfoAction docInfoAction = new Infos.FPCInfoAction();
                            docInfoAction.setActionType(BizConstant.SP_FPCINFO_DELETE);
                            docInfoAction.setStrFPCInfo(data);
                            docInfoActionList.add(docInfoAction);
                        }));

                        //Remove doc data and runCard doc data
                        log.info("Remove doc data and runCard doc data");
                        fpcMethod.fpcInfoDeleteDR(objCommon,docIDs);
                        //Make doc hsitory single by runCard operation
                        log.info("Make doc hsitory single by runCard operation");
                        eventMethod.fpcInfoRegistEventMake(objCommon, TransactionIDEnum.FPCDELETE_REQ.getValue(), docInfoActionList, null,runCardID);
                    }
                }
            }
        }
        //Remove runCard psm data by changePsmKey
        log.info("Remove runCard psm data by changePsmKey");
        this.removeEntitys("SELECT * FROM RUNCARD_PSM WHERE PSM_KEY = ?1",CimRunCardPsmDO.class,changePsmKey);
    }

    @Override
    public void removeRunCardPsmDocFromDocJobID(String docJobID) {
        //Delete specified doc data
        log.info("Delete specified doc data");
        CimRunCardPsmDocDO runCardPsmDoc = cimJpaRepository.queryOne("SELECT * FROM RUNCARD_PSM_DOC rpd WHERE rpd.DOC_JOB_ID = ?1", CimRunCardPsmDocDO.class, docJobID);
        if (!CimObjectUtils.isEmpty(runCardPsmDoc)) {
            //Delete specified doc data
            log.info("Delete specified doc data");
            cimJpaRepository.delete(runCardPsmDoc);

            //Update the remaining RUNCARD_PSM_DOC data according to D_SEQNO
            log.info("Update the remaining RUNCARD_PSM_DOC data according to D_SEQNO");
            List<CimRunCardPsmDocDO> runCardPsmDocs = cimJpaRepository.query("SELECT * FROM RUNCARD_PSM_DOC rpd WHERE rpd.PSM_JOB_ID = ?1 AND PSM_KEY = ?2 ORDER BY IDX_NO",
                    CimRunCardPsmDocDO.class, runCardPsmDoc.getPsmJobID(), runCardPsmDoc.getPsmKey());
            if (!CimObjectUtils.isEmpty(runCardPsmDocs)) {
                for (CimRunCardPsmDocDO next : runCardPsmDocs) {
                    Integer removeSequenceNumber = runCardPsmDoc.getSequenceNumber();
                    if (next.getSequenceNumber() < removeSequenceNumber) {
                        continue;
                    }
                    next.setSequenceNumber(next.getSequenceNumber() - 1);
                }
                runCardPsmDocs.forEach(x -> cimJpaRepository.save(x));
            }
        }
    }

    @Override
    public void runCardAutoCompleteAction(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //Get psm info by lotID
        log.info("Get psm info by lotID");
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(CimObjectUtils.isEmpty(aLot),retCodeConfig.getNotFoundLot());
        //Get lotFamily
        log.info("Get lotFamily");
        CimLotFamily aLotFamily = aLot.getLotFamily();
        Validations.check(CimObjectUtils.isEmpty(aLotFamily), retCodeConfig.getNotFoundLotFamily());

        String lotFamilyId = aLotFamily.getIdentifier();
        List<Infos.ExperimentalLotInfo> experimentalLotInfos = experimentalMethod.experimentalLotListGetDR(objCommon, lotFamilyId, "", "",
                "", "", false, true);

        if (CimArrayUtils.isEmpty(experimentalLotInfos)){
            //none psm setting contine
            return;
        }
        //Check psm condition - execFlag/runCardFlag is exsit and check psm info contains all lot wafer
        log.info("Check psm condition - execFlag/runCardFlag is exsit and check psm info contains all lot wafer");
        List<ObjectIdentifier> lotWafers = aLot.getAllWaferInfo().stream().map(ProductDTO.WaferInfo::getWaferID).collect(Collectors.toList());
        Boolean containAllWafer = false;
        Boolean psmKeyFlag = false;
        String onePsmKey = null;
        for (Infos.ExperimentalLotInfo experimentalLotInfo : experimentalLotInfos) {
            if (CimBooleanUtils.isFalse(experimentalLotInfo.getRunCardFlag())){
                return;
            }
            if (CimBooleanUtils.isFalse(experimentalLotInfo.getExecFlag())){
                return;
            }
            //Check psm info contains all lot wafer and psmKey all exsit
            log.info("Check psm info contains all lot wafer and psmKey all exsit");
            if (CimArrayUtils.isNotEmpty(experimentalLotInfo.getStrExperimentalLotDetailInfoSeq())){
                for (Infos.ExperimentalLotDetailInfo experimentalLotDetailInfo : experimentalLotInfo.getStrExperimentalLotDetailInfoSeq()) {
                    if (!CimStringUtils.isEmpty(experimentalLotDetailInfo.getPsmKey())){
                        psmKeyFlag = true;
                        onePsmKey = experimentalLotDetailInfo.getPsmKey();
                        if (experimentalLotDetailInfo.getWaferIDs().containsAll(lotWafers)){
                            containAllWafer = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!containAllWafer || !psmKeyFlag){
            return;
        }

        //Get RunCard infomation by psmKey
        log.info("Get RunCard infomation by psmKey");
        Infos.RunCardInfo runCardInfo = null;
        if (CimStringUtils.isNotEmpty(onePsmKey)){
            runCardInfo = this.getRunCardByPsmKey(objCommon, onePsmKey);
        }

        //RunCard exsit so prove the lot is a runCard operation result
        log.info("RunCard exsit so prove the lot is a runCard operation result");
        if (null != runCardInfo){
            //Ceck auto complete flag is true
            log.info("Check auto complete flag is true");
            if (CimBooleanUtils.isFalse(runCardInfo.getAutoCompleteFlag())){
                return;
            }
            //Check all psm wafer's lot are on psm split Route
            log.info("Check all psm wafer's lot are on psm split Route");
            List<ObjectIdentifier> psmWaferList = new ArrayList<>();
            Boolean allBackSplitRouteFlag = true;
            experimentalLotInfos.forEach(experimentalLotInfo -> experimentalLotInfo.getStrExperimentalLotDetailInfoSeq().forEach(experimentalLotDetailInfo -> psmWaferList.addAll(experimentalLotDetailInfo.getWaferIDs())));
            for (ObjectIdentifier waferID : psmWaferList) {
                ObjectIdentifier waferLotGet = waferMethod.waferLotGet(objCommon, waferID);
                Outputs.ObjLotCurrentOperationInfoGetOut operationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, waferLotGet);
                if (!ObjectIdentifier.equalsWithValue(experimentalLotInfos.get(0).getSplitRouteID(), operationInfoGetOut.getRouteID())){
                    allBackSplitRouteFlag = false;
                    break;
                }
            }
            if (CimBooleanUtils.isTrue(allBackSplitRouteFlag)){
                //Check runCard state and update to done
                log.info("Check runCard state and update to done");
                if (CimStringUtils.equals(BizConstant.RUNCARD_DONE,runCardInfo.getRunCardState()) || !CimStringUtils.equals(BizConstant.RUNCARD_RUNNING,runCardInfo.getRunCardState())){
                    //RunCard is complete by manuel or run card state is not Running
                    log.info("RunCard is complete by manuel or run card state is not Running");
                    return;
                }
                //Update run card state to done automatic
                log.info("Update run card state to done automatic");
                runCardInfo.setRunCardState(BizConstant.RUNCARD_DONE);
                runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                runCardInfo.getRunCardPsmDocInfos().clear();
                this.updateRunCardInfo(objCommon,runCardInfo);

                //Make runcard history
                log.info("Make runcard history");
                eventMethod.runCardEventMake(objCommon,BizConstant.RUNCARD_ACTION_COMPLETE,runCardInfo);

                //Remove psm/doc info
                log.info("Remove psm/doc info");
                this.removeAllRunCardInfo(objCommon,runCardInfo.getRunCardID(),false);
            }
        }
    }


    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = cimJpaRepository.query(querySql, clz, objects);
        if (CimArrayUtils.isNotEmpty(objs)){
            objs.forEach(x -> cimJpaRepository.delete(x));
        }
    }

    private Infos.RunCardInfo getRunCardByPsmKey(Infos.ObjCommon objCommon,String psmKey){
        List<Object> resultListQuery = cimJpaRepository.queryOneColumn("SELECT REFKEY FROM RUNCARD_PSM WHERE PSM_KEY = ?1", psmKey);
        String refkey = null;
        if (CimArrayUtils.isNotEmpty(resultListQuery)){
            refkey = CimObjectUtils.toString(resultListQuery.get(0));
        }
        List<Object> runCardIDList = null;
        if (CimStringUtils.isNotEmpty(refkey)){
            runCardIDList  = cimJpaRepository.queryOneColumn("SELECT RUNCARD_ID FROM RUNCARD WHERE ID = ?1", refkey);
        }
        if (CimArrayUtils.isNotEmpty(runCardIDList)){
            return this.getRunCardInfo(objCommon, CimObjectUtils.toString(runCardIDList.get(0)));
        }
        return null;
    }
}
