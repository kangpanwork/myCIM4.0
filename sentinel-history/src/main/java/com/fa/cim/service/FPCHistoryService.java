package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/5/31 16:16
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class FPCHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param ChamberStatusChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 17:36
     */
    public Response insertChamberStatusChangeHistory(Infos.Ohcschs ChamberStatusChangeRecord ) {
        String hFHCSCHS_EQP_ID                ;
        String hFHCSCHS_EQP_NAME              ;
        String hFHCSCHS_PROCRSC_ID            ;
        String hFHCSCHS_AREA_ID               ;
        String hFHCSCHS_PR_STATE              ;
        String hFHCSCHS_CLAIM_USER_ID         ;
        String hFHCSCHS_START_TIME            ;
        Double  hFHCSCHS_START_SHOP_DATE;
        String hFHCSCHS_END_TIME              ;
        Double  hFHCSCHS_END_SHOP_DATE;
        String hFHCSCHS_NEW_PR_STATE          ;
        String hFHCSCHS_CLAIM_MEMO            ;
        String hFHCSCHS_E10_STATE             ;
        String hFHCSCHS_ACT_E10_STATE         ;
        String hFHCSCHS_ACT_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_E10_STATE         ;
        String hFHCSCHS_NEW_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_ACT_E10_STATE     ;
        String hFHCSCHS_NEW_ACT_CHAMBER_STATE ;
        String hFHCSCHS_EVENT_CREATE_TIME     ;

        hFHCSCHS_EQP_ID = ChamberStatusChangeRecord.getEqp_id          ();
        hFHCSCHS_EQP_NAME = ChamberStatusChangeRecord.getEqp_name        ();
        hFHCSCHS_PROCRSC_ID = ChamberStatusChangeRecord.getProcrsc_id      ();
        hFHCSCHS_AREA_ID = ChamberStatusChangeRecord.getArea_id         ();
        hFHCSCHS_PR_STATE = ChamberStatusChangeRecord.getPr_state        ();
        hFHCSCHS_CLAIM_USER_ID = ChamberStatusChangeRecord.getClaim_user_id   ();
        hFHCSCHS_START_TIME = ChamberStatusChangeRecord.getStart_time      ();
        hFHCSCHS_START_SHOP_DATE  = ChamberStatusChangeRecord.getStart_shop_date ();
        hFHCSCHS_END_TIME = ChamberStatusChangeRecord.getEnd_time        ();
        hFHCSCHS_END_SHOP_DATE    = ChamberStatusChangeRecord.getEnd_shop_date();
        hFHCSCHS_NEW_PR_STATE = ChamberStatusChangeRecord.getNew_pr_state    ();
        hFHCSCHS_CLAIM_MEMO = ChamberStatusChangeRecord.getClaim_memo      ();
        hFHCSCHS_E10_STATE = ChamberStatusChangeRecord.getE10_state             ();
        hFHCSCHS_ACT_E10_STATE = ChamberStatusChangeRecord.getAct_e10_state         ();
        hFHCSCHS_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getAct_chamber_state     ();
        hFHCSCHS_NEW_E10_STATE = ChamberStatusChangeRecord.getNew_e10_state         ();
        hFHCSCHS_NEW_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_chamber_state     ();
        hFHCSCHS_NEW_ACT_E10_STATE = ChamberStatusChangeRecord.getNew_act_e10_state     ();
        hFHCSCHS_NEW_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_act_chamber_state ();
        hFHCSCHS_EVENT_CREATE_TIME = ChamberStatusChangeRecord.getEvent_create_time ();


        baseCore.insert("INSERT INTO OHCMBSC\n" +
                "            ( ID, EQP_ID                 ,\n" +
                "                    EQP_NAME               ,\n" +
                "                    PROCRES_ID             ,\n" +
                "                    BAY_ID                ,\n" +
                "                    PROCRES_STATE_ID               ,\n" +
                "                    TRX_USER_ID          ,\n" +
                "                    START_TIME             ,\n" +
                "                    START_WORK_DATE        ,\n" +
                "                    END_TIME               ,\n" +
                "                    END_WORK_DATE          ,\n" +
                "                    NEW_PROCRES_STATE_ID           ,\n" +
                "                    TRX_MEMO             ,\n" +
                "                    STORE_TIME             ,\n" +
                "                    EVENT_CREATE_TIME      ,\n" +
                "                    E10_STATE_ID              ,\n" +
                "                    ACTUAL_E10_STATE_ID          ,\n" +
                "                    ACTUAL_CMB_STATE_ID      ,\n" +
                "                    NEW_E10_STATE_ID          ,\n" +
                "                    NEW_CMB_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_E10_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_CMB_STATE_ID        )\n" +
                "        Values\n" +
                "                (  ?,?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP               ,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Ohcschs.class)
                ,hFHCSCHS_EQP_ID
                ,hFHCSCHS_EQP_NAME
                ,hFHCSCHS_PROCRSC_ID
                ,hFHCSCHS_AREA_ID
                ,hFHCSCHS_PR_STATE
                ,hFHCSCHS_CLAIM_USER_ID
                ,convert(hFHCSCHS_START_TIME)
                ,hFHCSCHS_START_SHOP_DATE
                ,convert(hFHCSCHS_END_TIME)
                ,hFHCSCHS_END_SHOP_DATE
                ,hFHCSCHS_NEW_PR_STATE
                ,hFHCSCHS_CLAIM_MEMO
                ,convert(hFHCSCHS_EVENT_CREATE_TIME)
                ,hFHCSCHS_E10_STATE
                ,hFHCSCHS_ACT_E10_STATE
                ,hFHCSCHS_ACT_CHAMBER_STATE
                ,hFHCSCHS_NEW_E10_STATE
                ,hFHCSCHS_NEW_CHAMBER_STATE
                ,hFHCSCHS_NEW_ACT_E10_STATE
                ,hFHCSCHS_NEW_ACT_CHAMBER_STATE );

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.DurableReworkEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/23 10:17
     */
    public Infos.FPCEventRecord getEventData(String id) {
        String sql="Select * from OMEVDOC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.FPCEventRecord theEventData=new Infos.FPCEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.WaferRecipeParameterEventData> wafers=new ArrayList<>();
        theEventData.setWafers(wafers);
        List<Infos.ReticleEventData> reticles=new ArrayList<>();
        theEventData.setReticles(reticles);
        List<Infos.DCSpecItemEventData> dcSpecItems=new ArrayList<>();
        theEventData.setDcSpecItems(dcSpecItems);
        List<Infos.CorrespondingOperationEventData> correspondingOperations=new ArrayList<>();
        theEventData.setCorrespondingOperations(correspondingOperations);

        for (Map<String,Object> sqlData:sqlDatas) {
            //add runCardID for history
            theEventData.setRunCardID(convert(sqlData.get("RUNCARD_ID")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setFPCID(convert(sqlData.get("DOC_ID")));
            theEventData.setLotFamilyID(convert(sqlData.get("LOTFAMILY_ID")));
            theEventData.setMainPDID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            theEventData.setOriginalMainPDID(convert(sqlData.get("ORIG_PROCESS_ID")));
            theEventData.setOriginalOperationNumber(convert(sqlData.get("ORIG_OPE_NO")));
            theEventData.setSubMainPDID(convert(sqlData.get("SUB_PROCESS_ID")));
            theEventData.setSubOperationNumber(convert(sqlData.get("SUB_OPE_NO")));
            theEventData.setFPCGroupNo(convert(sqlData.get("DOC_GROUP_NO")));
            theEventData.setFPCType(convert(sqlData.get("DOC_TYPE")));
            theEventData.setMergeMainPDID(convert(sqlData.get("MERGE_PROCESS_ID")));
            theEventData.setMergeOperationNumber(convert(sqlData.get("MERGE_OPE_NO")));
            theEventData.setFPCCategory(convert(sqlData.get("DOC_CATEGORY_ID")));
            theEventData.setPdID(convert(sqlData.get("STEP_ID")));
            theEventData.setPdType(convert(sqlData.get("STEP_TYPE")));
            theEventData.setCorrespondingOperNo(convert(sqlData.get("CORR_OPE_NO")));
            theEventData.setSkipFlag(convertB(sqlData.get("SKIP_FLAG")));
            theEventData.setRestrictEqpFlag(convertB(sqlData.get("LIMIT_EQP_FLAG")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setMachineRecipeID(convert(sqlData.get("MRCP_ID")));
            theEventData.setDcDefID(convert(sqlData.get("EDC_PLAN_ID")));
            theEventData.setDcSpecID(convert(sqlData.get("EDC_SPEC_ID")));
            theEventData.setRecipeParameterChangeType(convert(sqlData.get("RPARAM_CHG_TYPE")));
            theEventData.setSendEmailFlag(convertB(sqlData.get("SEND_EMAIL_FLAG")));
            theEventData.setHoldLotFlag(convertB(sqlData.get("HOLD_LOT_FLAG")));
            theEventData.setDescription(convert(sqlData.get("DESCRIPTION")));
            theEventData.setCreateTime(convert(sqlData.get("CREATE_TIME")));
            theEventData.setUpdateTime(convert(sqlData.get("UPDATE_TIME")));

            sql="SELECT * FROM OMEVDOC_WAFER WHERE REFKEY=?";
            List<Map> sqlWafers = baseCore.queryAllForMap(sql, id);

            for (Map sqlWafer:sqlWafers){
                Infos.WaferRecipeParameterEventData wafer=new Infos.WaferRecipeParameterEventData();
                wafers.add(wafer);

                wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));

                List<Infos.RecipeParameterEventData> recipeParameters=new ArrayList<>();
                wafer.setRecipeParameters(recipeParameters);

                sql="SELECT * FROM OMEVDOC_WAFER_RPARAM WHERE REFKEY=?";
                List<Map> sqlRecipeParameters = baseCore.queryAllForMap(sql, id);

                for (Map sqlRecipeParameter:sqlRecipeParameters){
                    Infos.RecipeParameterEventData recipeParameter=new Infos.RecipeParameterEventData();
                    recipeParameters.add(recipeParameter);

                    recipeParameter.setSeq_No(convertL(sqlRecipeParameter.get("SEQ_NO")));
                    recipeParameter.setParameterName(convert(sqlRecipeParameter.get("RPARAM_NAME")));
                    recipeParameter.setParameterValue(convert(sqlRecipeParameter.get("RPARAM_VAL")));
                    recipeParameter.setUseCurrentSettingValueFlag(convertB(sqlRecipeParameter.get("RPARAM_USE_CUR_FLAG")));
                    recipeParameter.setParameterUnit(convert(sqlRecipeParameter.get("RPARAM_UNIT")));
                    recipeParameter.setParameterDataType(convert(sqlRecipeParameter.get("RPARAM_DATA_TYPE")));
                    recipeParameter.setParameterLowerLimit(convert(sqlRecipeParameter.get("RPARAM_LOWER_LIMIT")));
                    recipeParameter.setParameterUpperLimit(convert(sqlRecipeParameter.get("RPARAM_UPPER_LIMIT")));
                    recipeParameter.setParameterTargetValue(convert(sqlRecipeParameter.get("RPARAM_TARGET_VAL")));
                }
            }

            sql="SELECT * FROM OMEVDOC_RTCL WHERE REFKEY=?";
            List<Map> sqlReticles = baseCore.queryAllForMap(sql, id);

            for (Map sqlReticle:sqlReticles){
                Infos.ReticleEventData reticle=new Infos.ReticleEventData();
                reticles.add(reticle);

                reticle.setSeq_No(convertL(sqlReticle.get("SEQ_NO")));
                reticle.setReticleID(convert(sqlReticle.get("RTCL_ID")));
                reticle.setReticleGroupID(convert(sqlReticle.get("RTCL_GROUP_ID")));
            }

            sql="SELECT * FROM OMEVDOC_EDCSPEC WHERE REFKEY=?";
            List<Map> sqlDcSpecItems = baseCore.queryAllForMap(sql, id);

            for (Map sqlDcSpecItem:sqlDcSpecItems){
                Infos.DCSpecItemEventData dcSpecItem=new Infos.DCSpecItemEventData();
                dcSpecItems.add(dcSpecItem);

                dcSpecItem.setDcItemName(convert(sqlDcSpecItem.get("EDC_ITEM_NAME")));
                dcSpecItem.setScreenUpperRequired(convertB(sqlDcSpecItem.get("SCRN_UP_FLAG")));
                dcSpecItem.setScreenUpperLimit(convertD(sqlDcSpecItem.get("SCRN_UP_LIMIT")));
                dcSpecItem.setScreenUpperActions(convert(sqlDcSpecItem.get("SCRN_UP_ACT")));
                dcSpecItem.setScreenLowerRequired(convertB(sqlDcSpecItem.get("SCRN_LO_FLAG")));
                dcSpecItem.setScreenLowerLimit(convertD(sqlDcSpecItem.get("SCRN_LO_LIMIT")));
                dcSpecItem.setScreenLowerActions(convert(sqlDcSpecItem.get("SCRN_LO_ACT")));
                dcSpecItem.setSpecUpperRequired(convertB(sqlDcSpecItem.get("SPEC_UP_FLAG")));
                dcSpecItem.setSpecUpperLimit(convertD(sqlDcSpecItem.get("SPEC_UP_LIMIT ")));
                dcSpecItem.setSpecUpperActions(convert(sqlDcSpecItem.get("SPEC_UP_ACT")));
                dcSpecItem.setSpecLowerRequired(convertB(sqlDcSpecItem.get("SPEC_LO_FLAG")));
                dcSpecItem.setSpecLowerLimit(convertD(sqlDcSpecItem.get("SPEC_LO_LIMIT")));
                dcSpecItem.setSpecLowerActions(convert(sqlDcSpecItem.get("SPEC_LO_AC")));
                dcSpecItem.setControlUpperRequired(convertB(sqlDcSpecItem.get("CTRL_UP_FLAG")));
                dcSpecItem.setControlUpeerLimit(convertD(sqlDcSpecItem.get("CTRL_UP_LIMIT ")));
                dcSpecItem.setControlUpperActions(convert(sqlDcSpecItem.get("CTRL_UP_ACT")));
                dcSpecItem.setControlLowerRequired(convertB(sqlDcSpecItem.get("CTRL_LO_FLAG")));
                dcSpecItem.setControlLowerLimit(convertD(sqlDcSpecItem.get("CTRL_LO_LIMIT")));
                dcSpecItem.setControlLowerActions(convert(sqlDcSpecItem.get("CTRL_LO_ACT")));
                dcSpecItem.setDcItemTargetValue(convertD(sqlDcSpecItem.get("EDC_ITEM_TARGET_VAL")));
                dcSpecItem.setDcItemTag(convert(sqlDcSpecItem.get("EDC_ITEM_TAG")));
                dcSpecItem.setDcSpecGroup(convert(sqlDcSpecItem.get("EDC_SPEC_GROUP")));
            }

            sql="SELECT * FROM OMEVDOC_MEASRELATED WHERE REFKEY=?";
            List<Map> sqlCorrespondingOperations = baseCore.queryAllForMap(sql, id);

            for (Map sqlCorrespondingOperation:sqlCorrespondingOperations){
                Infos.CorrespondingOperationEventData correspondingOperation=new Infos.CorrespondingOperationEventData();
                correspondingOperations.add(correspondingOperation);

                correspondingOperation.setCorrespondingOperationNumber(convert(sqlCorrespondingOperation.get("CORR_OPE_NO")));
                correspondingOperation.setDcSpecGroup(convert(sqlCorrespondingOperation.get("EDC_SPEC_GROUP")));
            }

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));
        }
        return theEventData;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param refKey
     * @return java.util.List<com.fa.cim.dto.Infos.UserDataSet>
     * @exception
     * @author Ho
     * @date 2019/7/23 10:28
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVDOC_CDA  WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets=new ArrayList<>();
        for (Map<String,Object> uData:uDatas) {
            Infos.UserDataSet userDataSet=new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName,String refKey) {
        String sql=String.format("DELETE %s WHERE REFKEY=?",tableName);
        baseCore.insert(sql,refKey);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.Custom.List<java.lang.String>
     * @exception
     * @author Ho
     * @date 2019/4/19 17:41
     */
    public List<String> getEventFIFO(String tableName){
        String sql=String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC",tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events=new ArrayList<>();
        fifos.forEach(fifo->events.add(convert(fifo[0])));
        return events;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhfpchs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:28
     */
    public Response  insertFPCHistory_FHFPCHS( Infos.Ohfpchs             fhfpchs_Record ) {
        String    hFHFPCHS_action                     ;
        String    hFHFPCHS_FPCID                      ;
        String    hFHFPCHS_lotFamilyID                ;
        String    hFHFPCHS_mainPDID                   ;
        String    hFHFPCHS_operationNumber            ;
        String    hFHFPCHS_originalMainPDID           ;
        String    hFHFPCHS_originalOperationNumber    ;
        String    hFHFPCHS_subMainPDID                ;
        String    hFHFPCHS_subOperationNumber         ;
        String    hFHFPCHS_FPCGroupNo                 ;
        String    hFHFPCHS_FPCType                    ;
        String    hFHFPCHS_mergeMainPDID              ;
        String    hFHFPCHS_mergeOperationNumber       ;
        String    hFHFPCHS_FPCCategory                ;
        String    hFHFPCHS_pdID                       ;
        String    hFHFPCHS_pdType                     ;
        String    hFHFPCHS_correspondingOperNo        ;

        Integer hFHFPCHS_skipFlag                   ;

        Integer hFHFPCHS_restrictEqpFlag            ;
        String    hFHFPCHS_equipmentID                ;
        String    hFHFPCHS_machineRecipeID            ;
        String    hFHFPCHS_dcDefID                    ;
        String    hFHFPCHS_dcSpecID                   ;
        String    hFHFPCHS_recipeParameterChangeType  ;
        String    hFHFPCHS_description                ;

        Integer hFHFPCHS_sendEmailFlag              ;

        Integer hFHFPCHS_holdLotFlag                ;
        String    hFHFPCHS_createTime                 ;
        String    hFHFPCHS_updateTime                 ;

        String    hFHFPCHS_claimTime                  ;
        String    hFHFPCHS_claimUserID                ;
        String    hFHFPCHS_claim_memo                 ;
        String    hFHFPCHS_eventCreationTime          ;
        //add runCardID for history
        String    hFHFPCHS_runCardID          ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS Function" );

        hFHFPCHS_action                   = fhfpchs_Record.getAction()                    ;
        hFHFPCHS_FPCID                    = fhfpchs_Record.getFPCID()                     ;
        hFHFPCHS_lotFamilyID              = fhfpchs_Record.getLotFamilyID()               ;
        hFHFPCHS_mainPDID                 = fhfpchs_Record.getMainPDID()                  ;
        hFHFPCHS_operationNumber          = fhfpchs_Record.getOperationNumber()           ;
        hFHFPCHS_originalMainPDID         = fhfpchs_Record.getOriginalMainPDID()          ;
        hFHFPCHS_originalOperationNumber  = fhfpchs_Record.getOriginalOperationNumber()   ;
        hFHFPCHS_subMainPDID              = fhfpchs_Record.getSubMainPDID()               ;
        hFHFPCHS_subOperationNumber       = fhfpchs_Record.getSubOperationNumber()        ;
        hFHFPCHS_FPCGroupNo               = fhfpchs_Record.getFPCGroupNo()                ;
        hFHFPCHS_FPCType                  = fhfpchs_Record.getFPCType()                   ;
        hFHFPCHS_mergeMainPDID            = fhfpchs_Record.getMergeMainPDID()             ;
        hFHFPCHS_mergeOperationNumber     = fhfpchs_Record.getMergeOperationNumber()      ;
        hFHFPCHS_FPCCategory              = fhfpchs_Record.getFPCCategory()               ;
        hFHFPCHS_pdID                     = fhfpchs_Record.getPdID()                      ;
        hFHFPCHS_pdType                   = fhfpchs_Record.getPdType()                    ;
        hFHFPCHS_correspondingOperNo      = fhfpchs_Record.getCorrespondingOperNo()       ;
        hFHFPCHS_skipFlag                 = fhfpchs_Record.getSkipFlag()                   ;
        hFHFPCHS_restrictEqpFlag          = fhfpchs_Record.getRestrictEqpFlag()            ;
        hFHFPCHS_equipmentID              = fhfpchs_Record.getEquipmentID()               ;
        hFHFPCHS_machineRecipeID          = fhfpchs_Record.getMachineRecipeID()           ;
        hFHFPCHS_dcDefID                  = fhfpchs_Record.getDcDefID()                   ;
        hFHFPCHS_dcSpecID                 = fhfpchs_Record.getDcSpecID()                  ;
        hFHFPCHS_recipeParameterChangeType= fhfpchs_Record.getRecipeParameterChangeType() ;
        hFHFPCHS_description              = fhfpchs_Record.getDescription()               ;
        hFHFPCHS_sendEmailFlag            = fhfpchs_Record.getSendEmailFlag()              ;
        hFHFPCHS_holdLotFlag              = fhfpchs_Record.getHoldLotFlag()                ;
        hFHFPCHS_createTime               = fhfpchs_Record.getCreateTime()                ;
        hFHFPCHS_updateTime               = fhfpchs_Record.getUpdateTime()                ;

        hFHFPCHS_claimTime                = fhfpchs_Record.getClaim_time()        ;
        hFHFPCHS_claimUserID              = fhfpchs_Record.getClaim_user_id()     ;
        hFHFPCHS_claim_memo               = fhfpchs_Record.getClaim_memo()        ;
        hFHFPCHS_eventCreationTime        = fhfpchs_Record.getEvent_create_time() ;
        hFHFPCHS_runCardID                = fhfpchs_Record.getRunCardID();

        baseCore.insert("INSERT INTO OHDOC\n"+
                        "(ID,   TASK_TYPE             ,DOC_ID             ,LOTFAMILY_ID       ,PROCESS_ID          ,OPE_NO             ,\n"+
                        "ORIG_PROCESS_ID      ,ORIG_OPE_NO         ,SUB_PROCESS_ID      ,SUB_OPE_NO         ,DOC_GROUP_NO       ,\n"+
                        "DOC_TYPE           ,MERGE_PROCESS_ID    ,MERGE_OPE_NO       ,DOC_CATEGORY_ID    ,STEP_ID              ,\n"+
                        "STEP_TYPE            ,CORR_OPE_NO  ,SKIP_FLAG          ,LIMIT_EQP_FLAG  ,EQP_ID             ,\n"+
                        "RPARAM_CHG_TYPE   ,MRCP_ID          ,EDC_PLAN_ID           ,EDC_SPEC_ID          ,SEND_EMAIL_FLAG    ,\n"+
                        "HOLD_LOT_FLAG      ,DESCRIPTION        ,CREATE_TIME        ,UPDATE_TIME        ,TRX_TIME         ,\n"+
                        "TRX_USER_ID      ,TRX_MEMO         ,STORE_TIME         ,EVENT_CREATE_TIME,     RUNCARD_ID  )\n"+
                        "Values\n"+
                        "(?,   ?                      ,?                       ,?                 ,?                    ,?             ,\n"+
                        "?            ,?     ,?                 ,?          ,?                  ,\n"+
                        "?                     ,?               ,?        ,?                 ,?                        ,\n"+
                        "?                      ,?         ,?                    ,?             ,?                 ,\n"+
                        "?   ,?             ,?                     ,?                    ,?               ,\n"+
                        "?                 ,?                 ,?                  ,?                  ,?                   ,\n"+
                        "?                 ,?                  ,CURRENT_TIMESTAMP                     ,?           ,        ?)",generateID(Infos.Ohfpchs.class),
                hFHFPCHS_action,
                hFHFPCHS_FPCID,
                hFHFPCHS_lotFamilyID,
                hFHFPCHS_mainPDID,
                hFHFPCHS_operationNumber,
                hFHFPCHS_originalMainPDID,
                hFHFPCHS_originalOperationNumber,
                hFHFPCHS_subMainPDID,
                hFHFPCHS_subOperationNumber,
                hFHFPCHS_FPCGroupNo,
                hFHFPCHS_FPCType,
                hFHFPCHS_mergeMainPDID,
                hFHFPCHS_mergeOperationNumber,
                hFHFPCHS_FPCCategory,
                hFHFPCHS_pdID,
                hFHFPCHS_pdType,
                hFHFPCHS_correspondingOperNo,
                hFHFPCHS_skipFlag,
                hFHFPCHS_restrictEqpFlag,
                hFHFPCHS_equipmentID,
                hFHFPCHS_recipeParameterChangeType,
                hFHFPCHS_machineRecipeID,
                hFHFPCHS_dcDefID,
                hFHFPCHS_dcSpecID,
                hFHFPCHS_sendEmailFlag,
                hFHFPCHS_holdLotFlag,
                hFHFPCHS_description,
                convert(hFHFPCHS_createTime),
                convert(hFHFPCHS_updateTime),
                convert(hFHFPCHS_claimTime),
                hFHFPCHS_claimUserID,
                hFHFPCHS_claim_memo,
                convert(hFHFPCHS_eventCreationTime),
                hFHFPCHS_runCardID);

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhfpchs_wafer_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:29
     */
    public Response  insertFPCHistory_FHFPCHS_WAFER( Infos.OhfpchsWafer       fhfpchs_wafer_Record ) {
        String    hFHFPCHS_WAFER_FPCID                   ;
        String    hFHFPCHS_WAFER_wafer_id                ;
        String    hFHFPCHS_WAFER_claimTime               ;
        String    hFHFPCHS_WAFER_runCardID               ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_WAFER Function" );

        hFHFPCHS_WAFER_FPCID      = fhfpchs_wafer_Record.getFPCID()      ;
        hFHFPCHS_WAFER_wafer_id   = fhfpchs_wafer_Record.getWafer_id()   ;
        hFHFPCHS_WAFER_claimTime  = fhfpchs_wafer_Record.getClaim_time() ;
        hFHFPCHS_WAFER_runCardID  = fhfpchs_wafer_Record.getRunCardID();

        baseCore.insert("INSERT INTO OHDOC_WAFER (ID,DOC_ID, WAFER_ID, TRX_TIME, RUNCARD_ID ) Values  (?,  ?, ?, ? , ?)",generateID(Infos.OhfpchsWafer.class),
                hFHFPCHS_WAFER_FPCID,
                hFHFPCHS_WAFER_wafer_id,
                convert(hFHFPCHS_WAFER_claimTime),
                hFHFPCHS_WAFER_runCardID);

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_WAFER Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:33
     */
    public Response  insertFPCHistory_FHFPCHS_WAFER_RPARM( Infos.OhfpchsWaferRparm       fhfpchs_wafer_rparm_Record ) {
        String    hFHFPCHS_WAFER_RPARM_FPCID                      ;
        String    hFHFPCHS_WAFER_RPARM_wafer_id                   ;

        Integer hFHFPCHS_WAFER_RPARM_seq_no                         ;
        String    hFHFPCHS_WAFER_RPARM_parameterName              ;
        String    hFHFPCHS_WAFER_RPARM_parameterUnit              ;
        String    hFHFPCHS_WAFER_RPARM_parameterDataType          ;
        String    hFHFPCHS_WAFER_RPARM_parameterLowerLimit        ;
        String    hFHFPCHS_WAFER_RPARM_parameterUpperLimit        ;
        String    hFHFPCHS_WAFER_RPARM_parameterValue            ;

        Integer hFHFPCHS_WAFER_RPARM_useCurrentSettingValueFlag     ;
        String    hFHFPCHS_WAFER_RPARM_parameterTargetValue      ;
        String    hFHFPCHS_WAFER_RPARM_parameterTag              ;
        String    hFHFPCHS_WAFER_RPARM_claimTime                  ;
        //add runCardID for history
        String    hFHFPCHS_WAFER_RPARM_runCardID                  ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_WAFER_RPARM Function" );

        hFHFPCHS_WAFER_RPARM_FPCID                      = fhfpchs_wafer_rparm_Record.getFPCID()                     ;
        hFHFPCHS_WAFER_RPARM_wafer_id                   = fhfpchs_wafer_rparm_Record.getWafer_id()                  ;
        hFHFPCHS_WAFER_RPARM_seq_no                     = fhfpchs_wafer_rparm_Record.getSeq_no()                    ;
        hFHFPCHS_WAFER_RPARM_parameterName              = fhfpchs_wafer_rparm_Record.getParameterName()             ;
        hFHFPCHS_WAFER_RPARM_parameterUnit              = fhfpchs_wafer_rparm_Record.getParameterUnit()             ;
        hFHFPCHS_WAFER_RPARM_parameterDataType          = fhfpchs_wafer_rparm_Record.getParameterDataType()         ;
        hFHFPCHS_WAFER_RPARM_parameterLowerLimit        = fhfpchs_wafer_rparm_Record.getParameterLowerLimit()       ;
        hFHFPCHS_WAFER_RPARM_parameterUpperLimit        = fhfpchs_wafer_rparm_Record.getParameterUpperLimit()       ;
        hFHFPCHS_WAFER_RPARM_parameterValue             = fhfpchs_wafer_rparm_Record.getParameterValue()            ;
        hFHFPCHS_WAFER_RPARM_useCurrentSettingValueFlag = fhfpchs_wafer_rparm_Record.getUseCurrentSettingValueFlag();
        hFHFPCHS_WAFER_RPARM_parameterTargetValue       = fhfpchs_wafer_rparm_Record.getParameterTargetValue()      ;
        hFHFPCHS_WAFER_RPARM_parameterTag               = fhfpchs_wafer_rparm_Record.getParameterTag()              ;
        hFHFPCHS_WAFER_RPARM_claimTime                  = fhfpchs_wafer_rparm_Record.getClaim_time()                ;
        hFHFPCHS_WAFER_RPARM_runCardID                  = fhfpchs_wafer_rparm_Record.getRunCardID()                ;

        baseCore.insert("INSERT INTO OHDOC_WAFER_RPARAM\n"+
                        "(ID, DOC_ID, WAFER_ID, IDX_NO, RPARAM_NAME, RPARAM_UNIT, RPARAM_DATA_TYPE, RPARAM_LOWER_LIMIT,\n"+
                        "RPARAM_UPPER_LIMIT, RPARAM_USE_CUR_FLAG, RPARAM_TARGET_VAL, RPARAM_VAL, TRX_TIME, RUNCARD_ID)\n"+
                        "Values\n"+
                        "(?,  ?                     , ?                  ,\n"+
                        "?                    , ?             ,\n"+
                        "\n"+
                        "?             , ?         ,\n"+
                        "?       , ?       ,\n"+
                        "?, ?      ,\n"+
                        "?            , ?              , ?)",generateID(Infos.OhfpchsWaferRparm.class),
                hFHFPCHS_WAFER_RPARM_FPCID,
                hFHFPCHS_WAFER_RPARM_wafer_id,
                hFHFPCHS_WAFER_RPARM_seq_no,
                hFHFPCHS_WAFER_RPARM_parameterName,
                hFHFPCHS_WAFER_RPARM_parameterUnit,
                hFHFPCHS_WAFER_RPARM_parameterDataType,
                hFHFPCHS_WAFER_RPARM_parameterLowerLimit,
                hFHFPCHS_WAFER_RPARM_parameterUpperLimit,
                hFHFPCHS_WAFER_RPARM_useCurrentSettingValueFlag,
                hFHFPCHS_WAFER_RPARM_parameterTargetValue,
                hFHFPCHS_WAFER_RPARM_parameterValue,
                convert(hFHFPCHS_WAFER_RPARM_claimTime),
                hFHFPCHS_WAFER_RPARM_runCardID);

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_WAFER_RPARM Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhfpchs_dcspecs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:35
     */
    public Response  insertFPCHistory_FHFPCHS_DCSPECS( Infos.OhfpchsDcspecs       fhfpchs_dcspecs_Record ) {
        String    hFHFPCHS_DCSPECS_FPCID                 ;
        String    hFHFPCHS_DCSPECS_dcItemName            ;

        Integer hFHFPCHS_DCSPECS_screenUpperRequired   ;
        Double  hFHFPCHS_DCSPECS_screenUpperLimit      ;

        String    hFHFPCHS_DCSPECS_screenUpperActions   ;

        Integer hFHFPCHS_DCSPECS_screenLowerRequired   ;
        Double  hFHFPCHS_DCSPECS_screenLowerLimit      ;

        String    hFHFPCHS_DCSPECS_screenLowerActions   ;

        Integer hFHFPCHS_DCSPECS_specUpperRequired     ;
        Double  hFHFPCHS_DCSPECS_specUpperLimit        ;

        String    hFHFPCHS_DCSPECS_specUpperActions     ;

        Integer hFHFPCHS_DCSPECS_specLowerRequired     ;
        Double  hFHFPCHS_DCSPECS_specLowerLimit        ;

        String    hFHFPCHS_DCSPECS_specLowerActions     ;

        Integer hFHFPCHS_DCSPECS_controlUpperRequired  ;
        Double  hFHFPCHS_DCSPECS_controlUpeerLimit     ;

        String    hFHFPCHS_DCSPECS_controlUpperActions  ;

        Integer hFHFPCHS_DCSPECS_controlLowerRequired  ;
        Double  hFHFPCHS_DCSPECS_controlLowerLimit     ;

        String    hFHFPCHS_DCSPECS_controlLowerActions  ;
        Double  hFHFPCHS_DCSPECS_dcItemTargetValue     ;

        String    hFHFPCHS_DCSPECS_dcItemTag            ;
        String    hFHFPCHS_DCSPECS_dcSpecGroup            ;
        String    hFHFPCHS_DCSPECS_claimTime              ;
        //add runCardID for history
        String    hFHFPCHS_DCSPECS_runCardID              ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_DCSPECS Function" );

        hFHFPCHS_DCSPECS_FPCID                = fhfpchs_dcspecs_Record.getFPCID()               ;
        hFHFPCHS_DCSPECS_dcItemName           = fhfpchs_dcspecs_Record.getDcItemName()          ;
        hFHFPCHS_DCSPECS_screenUpperRequired  = fhfpchs_dcspecs_Record.getScreenUpperRequired() ;
        hFHFPCHS_DCSPECS_screenUpperLimit     = fhfpchs_dcspecs_Record.getScreenUpperLimit()    ;
        hFHFPCHS_DCSPECS_screenUpperActions   = fhfpchs_dcspecs_Record.getScreenUpperActions()  ;
        hFHFPCHS_DCSPECS_screenLowerRequired  = fhfpchs_dcspecs_Record.getScreenLowerRequired() ;
        hFHFPCHS_DCSPECS_screenLowerLimit     = fhfpchs_dcspecs_Record.getScreenLowerLimit()    ;
        hFHFPCHS_DCSPECS_screenLowerActions   = fhfpchs_dcspecs_Record.getScreenLowerActions()  ;
        hFHFPCHS_DCSPECS_specUpperRequired    = fhfpchs_dcspecs_Record.getSpecUpperRequired()   ;
        hFHFPCHS_DCSPECS_specUpperLimit       = fhfpchs_dcspecs_Record.getSpecUpperLimit()      ;
        hFHFPCHS_DCSPECS_specUpperActions     = fhfpchs_dcspecs_Record.getSpecUpperActions()    ;
        hFHFPCHS_DCSPECS_specLowerRequired    = fhfpchs_dcspecs_Record.getSpecLowerRequired()   ;
        hFHFPCHS_DCSPECS_specLowerLimit       = fhfpchs_dcspecs_Record.getSpecLowerLimit()      ;
        hFHFPCHS_DCSPECS_specLowerActions     = fhfpchs_dcspecs_Record.getSpecLowerActions()    ;
        hFHFPCHS_DCSPECS_controlUpperRequired = fhfpchs_dcspecs_Record.getControlUpperRequired();
        hFHFPCHS_DCSPECS_controlUpeerLimit    = fhfpchs_dcspecs_Record.getControlUpeerLimit()   ;
        hFHFPCHS_DCSPECS_controlUpperActions  = fhfpchs_dcspecs_Record.getControlUpperActions() ;
        hFHFPCHS_DCSPECS_controlLowerRequired = fhfpchs_dcspecs_Record.getControlLowerRequired();
        hFHFPCHS_DCSPECS_controlLowerLimit    = fhfpchs_dcspecs_Record.getControlLowerLimit()   ;
        hFHFPCHS_DCSPECS_controlLowerActions  = fhfpchs_dcspecs_Record.getControlLowerActions() ;
        hFHFPCHS_DCSPECS_dcItemTargetValue    = fhfpchs_dcspecs_Record.getDcItemTargetValue()   ;
        hFHFPCHS_DCSPECS_dcItemTag            = fhfpchs_dcspecs_Record.getDcItemTag()           ;
        hFHFPCHS_DCSPECS_dcSpecGroup          = fhfpchs_dcspecs_Record.getDcSpecGroup()         ;
        hFHFPCHS_DCSPECS_claimTime            = fhfpchs_dcspecs_Record.getClaim_time()          ;
        hFHFPCHS_DCSPECS_runCardID            = fhfpchs_dcspecs_Record.getRunCardID()          ;

        baseCore.insert("INSERT INTO OHDOC_EDCSPEC\n"+
                        "(ID, DOC_ID              ,EDC_ITEM_NAME         ,SCRN_UP_FLAG      ,SCRN_UP_LIMIT    ,SCRN_UP_ACT  ,\n"+
                        "SCRN_LO_FLAG      ,SCRN_LO_LIMIT    ,SCRN_LO_ACT  ,SPEC_UP_FLAG      ,SPEC_UP_LIMIT    ,\n"+
                        "SPEC_UP_ACT  ,SPEC_LO_FLAG      ,SPEC_LO_LIMIT    ,SPEC_LO_ACT  ,CTRL_UP_FLAG      ,\n"+
                        "CTRL_UP_LIMIT    ,CTRL_UP_ACT  ,CTRL_LO_FLAG      ,CTRL_LO_LIMIT    ,CTRL_LO_ACT  ,\n"+
                        "\n"+
                        "EDC_ITEM_TARGET_VAL ,EDC_ITEM_TAG          ,EDC_SPEC_GROUP       ,TRX_TIME,     RUNCARD_ID)\n"+
                        "Values\n"+
                        "(?,  ?               ,?          ,? ,\n"+
                        "?    ,?  ,? ,\n"+
                        "?    ,?  ,?   ,\n"+
                        "?      ,?    ,?   ,\n"+
                        "?      ,?    ,?,\n"+
                        "?   ,? ,?,\n"+
                        "?   ,? ,?   ,\n"+
                        "\n"+
                        "?           ,?         ,?,     ?)",generateID(Infos.OhfpchsDcspecs.class),
                hFHFPCHS_DCSPECS_FPCID,
                hFHFPCHS_DCSPECS_dcItemName,
                hFHFPCHS_DCSPECS_screenUpperRequired,
                hFHFPCHS_DCSPECS_screenUpperLimit,
                hFHFPCHS_DCSPECS_screenUpperActions,
                hFHFPCHS_DCSPECS_screenLowerRequired,
                hFHFPCHS_DCSPECS_screenLowerLimit,
                hFHFPCHS_DCSPECS_screenLowerActions,
                hFHFPCHS_DCSPECS_specUpperRequired,
                hFHFPCHS_DCSPECS_specUpperLimit,
                hFHFPCHS_DCSPECS_specUpperActions,
                hFHFPCHS_DCSPECS_specLowerRequired,
                hFHFPCHS_DCSPECS_specLowerLimit,
                hFHFPCHS_DCSPECS_specLowerActions,
                hFHFPCHS_DCSPECS_controlUpperRequired,
                hFHFPCHS_DCSPECS_controlUpeerLimit,
                hFHFPCHS_DCSPECS_controlUpperActions,
                hFHFPCHS_DCSPECS_controlLowerRequired,
                hFHFPCHS_DCSPECS_controlLowerLimit,
                hFHFPCHS_DCSPECS_controlLowerActions,
                hFHFPCHS_DCSPECS_dcItemTargetValue,
                hFHFPCHS_DCSPECS_dcItemTag,
                hFHFPCHS_DCSPECS_dcSpecGroup,
                convert(hFHFPCHS_DCSPECS_claimTime),
                hFHFPCHS_DCSPECS_runCardID);

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_DCSPECS Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhfpchs_rtcl_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:44
     */
    public Response  insertFPCHistory_FHFPCHS_RTCL( Infos.OhfpchsRtcl        fhfpchs_rtcl_Record ) {
        String    hFHFPCHS_RTCL_FPCID          ;

        Integer hFHFPCHS_RTCL_seq_no             ;
        String    hFHFPCHS_RTCL_reticleID      ;
        String    hFHFPCHS_RTCL_reticleGroupID ;
        String    hFHFPCHS_RTCL_claimTime      ;
        //add runCardID for history
        String    hFHFPCHS_RTCL_runCard      ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_RTCL Function" );

        hFHFPCHS_RTCL_FPCID          = fhfpchs_rtcl_Record.getFPCID()           ;
        hFHFPCHS_RTCL_seq_no         = fhfpchs_rtcl_Record.getSeq_no()           ;
        hFHFPCHS_RTCL_reticleID      = fhfpchs_rtcl_Record.getReticleID()       ;
        hFHFPCHS_RTCL_reticleGroupID = fhfpchs_rtcl_Record.getReticleGroupID()  ;
        hFHFPCHS_RTCL_claimTime      = fhfpchs_rtcl_Record.getClaim_time()      ;
        hFHFPCHS_RTCL_runCard      = fhfpchs_rtcl_Record.getRunCardID()      ;

        baseCore.insert("INSERT INTO OHDOC_RTCL\n"+
                        "(ID, DOC_ID ,IDX_NO ,RTCL_ID, RTCL_GROUP_ID, TRX_TIME, RUNCARD_ID)\n"+
                        "Values\n"+
                        "(?, ?  ,?  ,?  ,?, ?, ?)",generateID(Infos.OhfpchsRtcl.class),
                hFHFPCHS_RTCL_FPCID,
                hFHFPCHS_RTCL_seq_no,
                hFHFPCHS_RTCL_reticleID,
                hFHFPCHS_RTCL_reticleGroupID,
                convert(hFHFPCHS_RTCL_claimTime),
                hFHFPCHS_RTCL_runCard);

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_RTCL Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhfpchs_corope_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:46
     */
    public Response  insertFPCHistory_FHFPCHS_COROPE( Infos.OhfpchsCorope        fhfpchs_corope_Record ) {
        String    hFHFPCHS_COROPE_FPCID                  ;
        Integer hFHFPCHS_COROPE_seq_no ;
        String    hFHFPCHS_COROPE_correspondingOpeNo     ;
        String    hFHFPCHS_COROPE_dcSpecGroup            ;
        String    hFHFPCHS_COROPE_claimTime              ;
        //add runCardID for history
        String    hFHFPCHS_COROPE_runCardID              ;

        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_COROPE Function" );

        hFHFPCHS_COROPE_FPCID                   = fhfpchs_corope_Record.getFPCID()              ;
        hFHFPCHS_COROPE_seq_no                  = fhfpchs_corope_Record.getSeq_no()              ;
        hFHFPCHS_COROPE_correspondingOpeNo      = fhfpchs_corope_Record.getCorrespondingOpeNo() ;
        hFHFPCHS_COROPE_dcSpecGroup             = fhfpchs_corope_Record.getDcSpecGroup()        ;
        hFHFPCHS_COROPE_claimTime               = fhfpchs_corope_Record.getClaim_time()         ;
        hFHFPCHS_COROPE_runCardID               = fhfpchs_corope_Record.getRunCardID()         ;

        baseCore.insert("INSERT INTO OHDOC_MEASRELATED\n"+
                        "(ID, DOC_ID, IDX_NO, CORR_OPE_NO, EDC_SPEC_GROUP, TRX_TIME, RUNCARD_ID)\n"+
                        "Values\n"+
                        "(?, ?, ?, ?, ?, ?, ?)",generateID(Infos.OhfpchsCorope.class),
                hFHFPCHS_COROPE_FPCID,
                hFHFPCHS_COROPE_seq_no,
                hFHFPCHS_COROPE_correspondingOpeNo,
                hFHFPCHS_COROPE_dcSpecGroup,
                convert(hFHFPCHS_COROPE_claimTime),
                hFHFPCHS_COROPE_runCardID);


        log.info("HistoryWatchDogServer::InsertFPCHistory_FHFPCHS_COROPE Function" );
        return( returnOK() );
    }

}
