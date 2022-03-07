package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
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
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableOperationMoveHistoryService {

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
                ,hFHCSCHS_END_TIME
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
    public Infos.DurableOperationMoveEventRecord getEventData(String id) {
        String sql="Select * from OMEVDURMVOP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableOperationMoveEventRecord theEventData=new Infos.DurableOperationMoveEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        Infos.DurableEventData durableData=new Infos.DurableEventData();
        theEventData.setDurableData(durableData);

        Infos.DurableProcessOperationEventData oldCurrentDurablePOData=new Infos.DurableProcessOperationEventData();
        theEventData.setOldCurrentDurablePOData(oldCurrentDurablePOData);

        List<Infos.RecipeParmEventData> recipeParameters=new ArrayList<>();
        theEventData.setRecipeParameters(recipeParameters);

        for (Map<String,Object> sqlData:sqlDatas) {
            durableData.setDurableID(convert(sqlData.get("DRBL_ID")));
            durableData.setDurableCategory(convert(sqlData.get("DRBL_CATEGORY")));
            durableData.setDurableStatus(convert(sqlData.get("DRBL_STATUS")));
            durableData.setHoldState(convert(sqlData.get("DRBL_HOLD_STATE")));
            durableData.setBankID(convert(sqlData.get("BANK_ID")));
            durableData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            durableData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            durableData.setOperationID(convert(sqlData.get("STEP_ID")));
            durableData.setOperationPassCount(convertL(sqlData.get("PASS_COUNT")));
            durableData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            durableData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            durableData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));
            durableData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setOperationMode(convert(sqlData.get("OPE_MODE")));
            theEventData.setLogicalRecipeID(convert(sqlData.get("LRCP_ID")));
            theEventData.setMachineRecipeID(convert(sqlData.get("MRCP_ID")));
            theEventData.setPhysicalRecipeID(convert(sqlData.get("PRCP_ID")));

            sql="SELECT * FROM OMEVDURMVOP_RPARAM WHERE REFKEY=?";
            List<Map> sqlRecipeParameters = baseCore.queryAllForMap(sql, id);

            for (Map sqlRecipeParameter:sqlRecipeParameters){
                Infos.RecipeParmEventData recipeParameter=new Infos.RecipeParmEventData();
                recipeParameters.add(recipeParameter);

                recipeParameter.setParameterName(convert(sqlRecipeParameter.get("RPARAM_NAME")));
                recipeParameter.setParameterValue(convert(sqlRecipeParameter.get("RPARAM_VAL")));
            }

            theEventData.setPreviousRouteID(convert(sqlData.get("PREV_PROCESS_ID")));
            theEventData.setPreviousOperationNumber(convert(sqlData.get("PREV_OPE_NO")));
            theEventData.setPreviousOperationID(convert(sqlData.get("PREV_STEP_ID")));
            theEventData.setPreviousOperationPassCount(convertL(sqlData.get("PREV_PASS_COUNT")));
            theEventData.setPreviousObjrefPOS(convert(sqlData.get("PREV_PRSS_RKEY")));
            theEventData.setPreviousObjrefMainPF(convert(sqlData.get("PREV_MROUTE_PRF_RKEY")));
            theEventData.setPreviousObjrefModulePOS(convert(sqlData.get("PREV_ROUTE_PRSS_RKEY")));

            oldCurrentDurablePOData.setObjrefPOS(convert(sqlData.get("OLD_PRSS_RKEY")));
            oldCurrentDurablePOData.setObjrefMainPF(convert(sqlData.get("OLD_MROUTE_PRF_RKEY")));
            oldCurrentDurablePOData.setObjrefModulePOS(convert(sqlData.get("OLD_ROUTE_PRSS_RKEY")));
            oldCurrentDurablePOData.setRouteID(convert(sqlData.get("OLD_PROCESS_ID")));
            oldCurrentDurablePOData.setOperationNumber(convert(sqlData.get("OLD_OPE_NO")));
            oldCurrentDurablePOData.setOperationID(convert(sqlData.get("OLD_STEP_ID")));
            oldCurrentDurablePOData.setOperationPassCount(convertL(sqlData.get("OLD_PASS_COUNT")));
            oldCurrentDurablePOData.setObjrefPO(convert(sqlData.get("OLD_PROPE_RKEY")));

            theEventData.setDurableControlJobID(convert(sqlData.get("DCJ_ID")));
            theEventData.setLocateBackFlag(convertB(sqlData.get("SKIP_BACK")));

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
        String sql="SELECT * FROM OMEVDURMVOP_CDA WHERE REFKEY=?";
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
     * @param fhdrblopehs_record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/18 18:26
     */
    public Response insertDRBLHistory_FHDRBLOPEHS( Infos.Ohdrblopehs fhdrblopehs_record ) {
        String hFHDRBLOPEHS_DURABLE_ID           ;
        String hFHDRBLOPEHS_DRBL_CATEGORY        ;
        String hFHDRBLOPEHS_MAINPD_ID            ;
        String hFHDRBLOPEHS_OPE_NO               ;
        String hFHDRBLOPEHS_PD_ID                ;
        Long hFHDRBLOPEHS_OPE_PASS_COUNT;
        String hFHDRBLOPEHS_PD_NAME              ;
        String hFHDRBLOPEHS_CLAIM_TIME           ;
        Double       hFHDRBLOPEHS_CLAIM_SHOP_DATE;
        String hFHDRBLOPEHS_CLAIM_USER_ID        ;
        String hFHDRBLOPEHS_MOVE_TYPE            ;
        String hFHDRBLOPEHS_OPE_CATEGORY         ;
        String hFHDRBLOPEHS_STAGE_ID             ;
        String hFHDRBLOPEHS_STAGEGRP_ID          ;
        String hFHDRBLOPEHS_PHOTO_LAYER          ;
        String hFHDRBLOPEHS_LOCATION_ID          ;
        String hFHDRBLOPEHS_AREA_ID              ;
        String hFHDRBLOPEHS_EQP_ID               ;
        String hFHDRBLOPEHS_EQP_NAME             ;
        String hFHDRBLOPEHS_OPE_MODE             ;
        String hFHDRBLOPEHS_LC_RECIPE_ID         ;
        String hFHDRBLOPEHS_RECIPE_ID            ;
        String hFHDRBLOPEHS_PH_RECIPE_ID         ;
        Long hFHDRBLOPEHS_RPARM_COUNT;
        String hFHDRBLOPEHS_BANK_ID              ;
        String hFHDRBLOPEHS_PREV_BANK_ID         ;
        String hFHDRBLOPEHS_PREV_MAINPD_ID       ;
        String hFHDRBLOPEHS_PREV_OPE_NO          ;
        String hFHDRBLOPEHS_PREV_PD_ID           ;
        String hFHDRBLOPEHS_PREV_PD_NAME         ;
        Long hFHDRBLOPEHS_PREV_PASS_COUNT;
        String hFHDRBLOPEHS_PREV_STAGE_ID        ;
        String hFHDRBLOPEHS_PREV_STAGEGRP_ID     ;
        String hFHDRBLOPEHS_PREV_PHOTO_LAYER     ;
        String hFHDRBLOPEHS_DCTRL_JOB            ;
        String hFHDRBLOPEHS_DRBL_OWNER_ID        ;
        String hFHDRBLOPEHS_PLAN_END_TIME        ;
        Boolean hFHDRBLOPEHS_CRITERIA_FLAG;
        String hFHDRBLOPEHS_CLAIM_MEMO         ;
        String hFHDRBLOPEHS_RPARM_CHANGE_TYPE    ;
        String hFHDRBLOPEHS_EVENT_CREATE_TIME    ;
        String hFHDRBLOPEHS_ORIGINAL_FAB_ID      ;
        String hFHDRBLOPEHS_DESTINATION_FAB_ID   ;
        String hFHDRBLOPEHS_HOLD_STATE           ;
        String hFHDRBLOPEHS_HOLD_TIME            ;
        String hFHDRBLOPEHS_HOLD_USER_ID         ;
        String hFHDRBLOPEHS_HOLD_TYPE            ;
        String hFHDRBLOPEHS_HOLD_REASON_CODE     ;
        String hFHDRBLOPEHS_HOLD_REASON_DESC     ;
        String hFHDRBLOPEHS_REASON_CODE          ;
        String hFHDRBLOPEHS_REASON_DESCRIPTION   ;
        String hFHDRBLOPEHS_HOLD_OPE_NO          ;
        String hFHDRBLOPEHS_HOLD_REASON_OPE_NO   ;
        Integer hFHDRBLOPEHS_INIT_HOLD_FLAG;
        Integer hFHDRBLOPEHS_REWORK_COUNT;
        Double       hFHDRBLOPEHS_HOLD_SHOP_DATE;

        hFHDRBLOPEHS_DURABLE_ID          ="";
        hFHDRBLOPEHS_DRBL_CATEGORY       ="";
        hFHDRBLOPEHS_MAINPD_ID           ="";
        hFHDRBLOPEHS_OPE_NO              ="";
        hFHDRBLOPEHS_PD_ID               ="";
        hFHDRBLOPEHS_PD_NAME             ="";
        hFHDRBLOPEHS_CLAIM_TIME          ="";
        hFHDRBLOPEHS_CLAIM_USER_ID       ="";
        hFHDRBLOPEHS_MOVE_TYPE           ="";
        hFHDRBLOPEHS_OPE_CATEGORY        ="";
        hFHDRBLOPEHS_STAGE_ID            ="";
        hFHDRBLOPEHS_STAGEGRP_ID         ="";
        hFHDRBLOPEHS_PHOTO_LAYER         ="";
        hFHDRBLOPEHS_LOCATION_ID         ="";
        hFHDRBLOPEHS_AREA_ID             ="";
        hFHDRBLOPEHS_EQP_ID              ="";
        hFHDRBLOPEHS_EQP_NAME            ="";
        hFHDRBLOPEHS_OPE_MODE            ="";
        hFHDRBLOPEHS_LC_RECIPE_ID        ="";
        hFHDRBLOPEHS_RECIPE_ID           ="";
        hFHDRBLOPEHS_PH_RECIPE_ID        ="";
        hFHDRBLOPEHS_BANK_ID             ="";
        hFHDRBLOPEHS_PREV_BANK_ID        ="";
        hFHDRBLOPEHS_PREV_MAINPD_ID      ="";
        hFHDRBLOPEHS_PREV_OPE_NO         ="";
        hFHDRBLOPEHS_PREV_PD_ID          ="";
        hFHDRBLOPEHS_PREV_PD_NAME        ="";
        hFHDRBLOPEHS_PREV_STAGE_ID       ="";
        hFHDRBLOPEHS_PREV_STAGEGRP_ID    ="";
        hFHDRBLOPEHS_PREV_PHOTO_LAYER    ="";
        hFHDRBLOPEHS_DCTRL_JOB           ="";
        hFHDRBLOPEHS_DRBL_OWNER_ID       ="";
        hFHDRBLOPEHS_PLAN_END_TIME       ="";
        hFHDRBLOPEHS_CLAIM_MEMO          ="";
        hFHDRBLOPEHS_RPARM_CHANGE_TYPE   ="";
        hFHDRBLOPEHS_EVENT_CREATE_TIME   ="";
        hFHDRBLOPEHS_ORIGINAL_FAB_ID     ="";
        hFHDRBLOPEHS_DESTINATION_FAB_ID  ="";
        hFHDRBLOPEHS_HOLD_STATE          ="";
        hFHDRBLOPEHS_HOLD_TIME           ="";
        hFHDRBLOPEHS_HOLD_USER_ID        ="";
        hFHDRBLOPEHS_HOLD_TYPE           ="";
        hFHDRBLOPEHS_HOLD_REASON_CODE    ="";
        hFHDRBLOPEHS_HOLD_REASON_DESC    ="";
        hFHDRBLOPEHS_REASON_CODE         ="";
        hFHDRBLOPEHS_REASON_DESCRIPTION  ="";
        hFHDRBLOPEHS_HOLD_OPE_NO         ="";
        hFHDRBLOPEHS_HOLD_REASON_OPE_NO  ="";

        hFHDRBLOPEHS_DURABLE_ID         = fhdrblopehs_record.getDurable_id         ();
        hFHDRBLOPEHS_DRBL_CATEGORY      = fhdrblopehs_record.getDrbl_category      ();
        hFHDRBLOPEHS_MAINPD_ID          = fhdrblopehs_record.getMainpd_id          ();
        hFHDRBLOPEHS_OPE_NO             = fhdrblopehs_record.getOpe_no             ();
        hFHDRBLOPEHS_PD_ID              = fhdrblopehs_record.getPd_id              ();
        hFHDRBLOPEHS_OPE_PASS_COUNT     = fhdrblopehs_record.getOpe_pass_count();
        hFHDRBLOPEHS_PD_NAME            = fhdrblopehs_record.getPd_name            ();
        hFHDRBLOPEHS_CLAIM_TIME         = fhdrblopehs_record.getClaim_time         ();
        hFHDRBLOPEHS_CLAIM_SHOP_DATE    = fhdrblopehs_record.getClaim_shop_date();
        hFHDRBLOPEHS_CLAIM_USER_ID      = fhdrblopehs_record.getClaim_user_id      ();
        hFHDRBLOPEHS_MOVE_TYPE          = fhdrblopehs_record.getMove_type          ();
        hFHDRBLOPEHS_OPE_CATEGORY       = fhdrblopehs_record.getOpe_category       ();
        hFHDRBLOPEHS_STAGE_ID           = fhdrblopehs_record.getStage_id           ();
        hFHDRBLOPEHS_STAGEGRP_ID        = fhdrblopehs_record.getStagegrp_id        ();
        hFHDRBLOPEHS_PHOTO_LAYER        = fhdrblopehs_record.getPhoto_layer        ();
        hFHDRBLOPEHS_LOCATION_ID        = fhdrblopehs_record.getLocation_id        ();
        hFHDRBLOPEHS_AREA_ID            = fhdrblopehs_record.getArea_id            ();
        hFHDRBLOPEHS_EQP_ID             = fhdrblopehs_record.getEqp_id             ();
        hFHDRBLOPEHS_EQP_NAME           = fhdrblopehs_record.getEqp_name           ();
        hFHDRBLOPEHS_OPE_MODE           = fhdrblopehs_record.getOpe_mode           ();
        hFHDRBLOPEHS_LC_RECIPE_ID       = fhdrblopehs_record.getLc_recipe_id       ();
        hFHDRBLOPEHS_RECIPE_ID          = fhdrblopehs_record.getRecipe_id          ();
        hFHDRBLOPEHS_PH_RECIPE_ID       = fhdrblopehs_record.getPh_recipe_id       ();
        hFHDRBLOPEHS_RPARM_COUNT        = fhdrblopehs_record.getRparm_count();
        hFHDRBLOPEHS_BANK_ID            = fhdrblopehs_record.getBank_id            ();
        hFHDRBLOPEHS_PREV_BANK_ID       = fhdrblopehs_record.getPrev_bank_id       ();
        hFHDRBLOPEHS_PREV_MAINPD_ID     = fhdrblopehs_record.getPrev_mainpd_id     ();
        hFHDRBLOPEHS_PREV_OPE_NO        = fhdrblopehs_record.getPrev_ope_no        ();
        hFHDRBLOPEHS_PREV_PD_ID         = fhdrblopehs_record.getPrev_pd_id         ();
        hFHDRBLOPEHS_PREV_PD_NAME       = fhdrblopehs_record.getPrev_pd_name       ();
        hFHDRBLOPEHS_PREV_PASS_COUNT    = fhdrblopehs_record.getPrev_pass_count();
        hFHDRBLOPEHS_PREV_STAGE_ID      = fhdrblopehs_record.getPrev_stage_id      ();
        hFHDRBLOPEHS_PREV_STAGEGRP_ID   = fhdrblopehs_record.getPrev_stagegrp_id   ();
        hFHDRBLOPEHS_PREV_PHOTO_LAYER   = fhdrblopehs_record.getPrev_photo_layer   ();
        hFHDRBLOPEHS_DCTRL_JOB          = fhdrblopehs_record.getDctrl_job          ();
        hFHDRBLOPEHS_DRBL_OWNER_ID      = fhdrblopehs_record.getDrbl_owner_id      ();
        hFHDRBLOPEHS_PLAN_END_TIME      = fhdrblopehs_record.getPlan_end_time      ();
        hFHDRBLOPEHS_CRITERIA_FLAG      = fhdrblopehs_record.getCriteria_flag();
        hFHDRBLOPEHS_CLAIM_MEMO         = fhdrblopehs_record.getClaim_memo         ();
        hFHDRBLOPEHS_RPARM_CHANGE_TYPE  = fhdrblopehs_record.getRparm_change_type  ();
        hFHDRBLOPEHS_EVENT_CREATE_TIME  = fhdrblopehs_record.getEvent_create_time  ();
        hFHDRBLOPEHS_ORIGINAL_FAB_ID    = fhdrblopehs_record.getOriginal_fab_id    ();
        hFHDRBLOPEHS_DESTINATION_FAB_ID = fhdrblopehs_record.getDestination_fab_id ();
        hFHDRBLOPEHS_HOLD_STATE         = fhdrblopehs_record.getHold_state         ();
        hFHDRBLOPEHS_HOLD_TIME          = fhdrblopehs_record.getHold_time          ();
        hFHDRBLOPEHS_HOLD_USER_ID       = fhdrblopehs_record.getHold_user_id       ();
        hFHDRBLOPEHS_HOLD_TYPE          = fhdrblopehs_record.getHold_type          ();
        hFHDRBLOPEHS_HOLD_REASON_CODE   = fhdrblopehs_record.getHold_reason_code   ();
        hFHDRBLOPEHS_HOLD_REASON_DESC   = fhdrblopehs_record.getHold_reason_desc   ();
        hFHDRBLOPEHS_REASON_CODE        = fhdrblopehs_record.getReason_code        ();
        hFHDRBLOPEHS_REASON_DESCRIPTION = fhdrblopehs_record.getReason_description ();
        hFHDRBLOPEHS_HOLD_OPE_NO        = fhdrblopehs_record.getHold_ope_no ();
        hFHDRBLOPEHS_HOLD_REASON_OPE_NO = fhdrblopehs_record.getHold_reason_ope_no ();
        hFHDRBLOPEHS_INIT_HOLD_FLAG     = fhdrblopehs_record.getInit_hold_flag();
        hFHDRBLOPEHS_REWORK_COUNT       = fhdrblopehs_record.getRework_count();
        hFHDRBLOPEHS_HOLD_SHOP_DATE     = fhdrblopehs_record.getHold_shop_date();

        baseCore.insert("INSERT INTO OHDUROPE\n" +
                "            (ID, DRBL_ID,\n" +
                "                    DRBL_CATEGORY,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    STEP_ID,\n" +
                "                    OPE_PASS_COUNT,\n" +
                "                    STEP_NAME,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    MOVE_TYPE,\n" +
                "                    OPE_CATEGORY,\n" +
                "                    STAGE_ID,\n" +
                "                    STAGE_GRP_ID,\n" +
                "                    PHOTO_LAYER,\n" +
                "                    LOCATION_ID,\n" +
                "                    BAY_ID,\n" +
                "                    EQP_ID,\n" +
                "                    EQP_NAME,\n" +
                "                    OPE_MODE,\n" +
                "                    LRCP_ID,\n" +
                "                    MRCP_ID,\n" +
                "                    PRCP_ID,\n" +
                "                    RPARAM_COUNT,\n" +
                "                    BANK_ID,\n" +
                "                    PREV_BANK_ID,\n" +
                "                    PREV_PROCESS_ID,\n" +
                "                    PREV_OPE_NO,\n" +
                "                    PREV_STEP_ID,\n" +
                "                    PREV_PD_NAME,\n" +
                "                    PREV_PASS_COUNT,\n" +
                "                    PREV_STAGE_ID,\n" +
                "                    PREV_STAGE_GRP_ID,\n" +
                "                    PREV_PHOTO_LAYER,\n" +
                "                    DCJ_ID,\n" +
                "                    DRBL_OWNER_ID,\n" +
                "                    PLAN_END_TIME,\n" +
                "                    CRITERIA_FLAG,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    RPARAM_CHG_TYPE,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    ORIGINAL_FAB_ID,\n" +
                "                    DESTINATION_FAB_ID,\n" +
                "                    HOLD_STATE        , \n" +
                "                    INITIAL_HOLD_FLAG    , \n" +
                "                    HOLD_TIME         , \n" +
                "                    HOLD_USER_ID      , \n" +
                "                    HOLD_TYPE         , \n" +
                "                    HOLD_REASON_CODE  , \n" +
                "                    HOLD_REASON_DESC  , \n" +
                "                    REASON_CODE       , \n" +
                "                    REASON_DESC, \n" +
                "                    REWORK_COUNT      , \n" +
                "                    HOLD_OPE_NO       , \n" +
                "                    HOLD_REASON_OPE_NO  \n" +
                "            )\n" +
                "        Values\n" +
                "                (?, ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?, \n" +
                "                    ?\n" +
                "                     )",generateID(Infos.Ohdrblopehs.class)
                ,hFHDRBLOPEHS_DURABLE_ID
                ,hFHDRBLOPEHS_DRBL_CATEGORY
                ,hFHDRBLOPEHS_MAINPD_ID
                ,hFHDRBLOPEHS_OPE_NO
                ,hFHDRBLOPEHS_PD_ID
                ,hFHDRBLOPEHS_OPE_PASS_COUNT
                ,hFHDRBLOPEHS_PD_NAME
                ,convert(hFHDRBLOPEHS_CLAIM_TIME)
                ,hFHDRBLOPEHS_CLAIM_USER_ID
                ,hFHDRBLOPEHS_MOVE_TYPE
                ,hFHDRBLOPEHS_OPE_CATEGORY
                ,hFHDRBLOPEHS_STAGE_ID
                ,hFHDRBLOPEHS_STAGEGRP_ID
                ,hFHDRBLOPEHS_PHOTO_LAYER
                ,hFHDRBLOPEHS_LOCATION_ID
                ,hFHDRBLOPEHS_AREA_ID
                ,hFHDRBLOPEHS_EQP_ID
                ,hFHDRBLOPEHS_EQP_NAME
                ,hFHDRBLOPEHS_OPE_MODE
                ,hFHDRBLOPEHS_LC_RECIPE_ID
                ,hFHDRBLOPEHS_RECIPE_ID
                ,hFHDRBLOPEHS_PH_RECIPE_ID
                ,hFHDRBLOPEHS_RPARM_COUNT
                ,hFHDRBLOPEHS_BANK_ID
                ,hFHDRBLOPEHS_PREV_BANK_ID
                ,hFHDRBLOPEHS_PREV_MAINPD_ID
                ,hFHDRBLOPEHS_PREV_OPE_NO
                ,hFHDRBLOPEHS_PREV_PD_ID
                ,hFHDRBLOPEHS_PREV_PD_NAME
                ,hFHDRBLOPEHS_PREV_PASS_COUNT
                ,hFHDRBLOPEHS_PREV_STAGE_ID
                ,hFHDRBLOPEHS_PREV_STAGEGRP_ID
                ,hFHDRBLOPEHS_PREV_PHOTO_LAYER
                ,hFHDRBLOPEHS_DCTRL_JOB
                ,hFHDRBLOPEHS_DRBL_OWNER_ID
                ,convert(hFHDRBLOPEHS_PLAN_END_TIME)
                ,hFHDRBLOPEHS_CRITERIA_FLAG
                ,hFHDRBLOPEHS_CLAIM_MEMO
                ,hFHDRBLOPEHS_RPARM_CHANGE_TYPE
                ,convert(hFHDRBLOPEHS_EVENT_CREATE_TIME)
                ,hFHDRBLOPEHS_ORIGINAL_FAB_ID
                ,hFHDRBLOPEHS_DESTINATION_FAB_ID
                ,hFHDRBLOPEHS_HOLD_STATE
                ,hFHDRBLOPEHS_INIT_HOLD_FLAG
                ,convert(hFHDRBLOPEHS_HOLD_TIME)
                ,hFHDRBLOPEHS_HOLD_USER_ID
                ,hFHDRBLOPEHS_HOLD_TYPE
                ,hFHDRBLOPEHS_HOLD_REASON_CODE
                ,hFHDRBLOPEHS_HOLD_REASON_DESC
                ,hFHDRBLOPEHS_REASON_CODE
                ,hFHDRBLOPEHS_REASON_DESCRIPTION
                ,hFHDRBLOPEHS_REWORK_COUNT
                ,hFHDRBLOPEHS_HOLD_OPE_NO
                ,hFHDRBLOPEHS_HOLD_REASON_OPE_NO  );

        return( returnOK() );
    }

}
