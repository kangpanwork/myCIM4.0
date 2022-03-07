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
 * @author Ho
 * @exception
 * @date 2019/5/31 16:16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProcessJobChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.ProcessJobChangeEventRecord
     * @throws
     * @author Ho
     * @date 2019/7/2 17:22
     */
    public Infos.ProcessJobChangeEventRecord getEventData(String id) {
        String sql = "Select * from OMEVPJCHG where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.ProcessJobChangeEventRecord theEventData = new Infos.ProcessJobChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.ProcessJobChangeWaferEventData> wafers = new ArrayList<>();
        theEventData.setWafers(wafers);
        List<Infos.ProcessJobChangeRecipeParameterEventData> recipeParameters = new ArrayList<>();
        theEventData.setRecipeParameters(recipeParameters);
        for (Map<String, Object> sqlData : sqlDatas) {

            theEventData.setCtrlJob(convert(sqlData.get("CJ_ID")));
            theEventData.setPrcsJob(convert(sqlData.get("PJ_ID")));
            theEventData.setOpeCategory(convert(sqlData.get("OPE_CATEGORY")));
            theEventData.setProcessStart(convert(sqlData.get("PROCESS_START_FLAG")));
            theEventData.setCurrentState(convert(sqlData.get("CUR_STATE")));

            sql = "SELECT * FROM OMEVPJCHG_WAFER WHERE REFKEY=?";
            List<Map> sqlWafers = baseCore.queryAllForMap(sql, id);

            for (Map sqlWafer : sqlWafers) {
                Infos.ProcessJobChangeWaferEventData wafer = new Infos.ProcessJobChangeWaferEventData();
                wafers.add(wafer);

                wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                wafer.setLotID(convert(sqlWafer.get("LOT_ID")));
            }

            sql = "SELECT * FROM OMEVPJCHG_RPARAM WHERE REFKEY=?";
            List<Map> sqlRecipeParameters = baseCore.queryAllForMap(sql, id);

            for (Map sqlRecipeParameter : sqlRecipeParameters) {
                Infos.ProcessJobChangeRecipeParameterEventData recipeParameter = new Infos.ProcessJobChangeRecipeParameterEventData();
                recipeParameters.add(recipeParameter);

                recipeParameter.setParameterName(convert(sqlRecipeParameter.get("RPARAM_NAME")));
                recipeParameter.setPreviousParameterValue(convert(sqlRecipeParameter.get("PREV_RPARAM_VAL")));
                recipeParameter.setParameterValue(convert(sqlRecipeParameter.get("RPARAM_VAL")));
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
     * @throws
     * @author Ho
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql = "SELECT * FROM OMEVPJCHG_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets = new ArrayList<>();
        for (Map<String, Object> uData : uDatas) {
            Infos.UserDataSet userDataSet = new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName, String refKey) {
        String sql = String.format("DELETE %s WHERE REFKEY=?", tableName);
        baseCore.insert(sql, refKey);
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
     * @throws
     * @author Ho
     * @date 2019/4/19 17:41
     */
    public List<String> getEventFIFO(String tableName) {
        String sql = String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC", tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events = new ArrayList<>();
        fifos.forEach(fifo -> events.add(convert(fifo[0])));
        return events;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param flowBatchMaxCountChangeRecord
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/7/1 16:43
     */
    public Response insertEquipmentFlowBatchMaxCountChangeHistory(Infos.Ohfbmchs flowBatchMaxCountChangeRecord) {
        String hFHFBMCHSEQP_ID;
        Integer hFHFBMCHSMAX_COUNT;
        String hFHFBMCHSCLAIM_TIME;
        String hFHFBMCHSCLAIM_USER_ID;
        String hFHFBMCHSCLAIM_MEMO;
        String hFHFBMCHSSTORE_TIME;
        String hFHFBMCHSEVENT_CREATE_TIME;

        hFHFBMCHSEQP_ID = flowBatchMaxCountChangeRecord.getEqp_id();
        hFHFBMCHSMAX_COUNT = flowBatchMaxCountChangeRecord.getMaxCount();
        hFHFBMCHSCLAIM_TIME = flowBatchMaxCountChangeRecord.getClaimTime();
        hFHFBMCHSCLAIM_USER_ID = flowBatchMaxCountChangeRecord.getClaimUser();
        hFHFBMCHSCLAIM_MEMO = flowBatchMaxCountChangeRecord.getClaimMemo();
        hFHFBMCHSEVENT_CREATE_TIME = flowBatchMaxCountChangeRecord.getEventCreateTime();

        baseCore.insert("INSERT INTO OHFBMXCNTCHG\n" +
                        "            (ID,EQP_ID,\n" +
                        "                    FLOWB_MAX_COUNT,\n" +
                        "                    TRX_TIME,\n" +
                        "                    TRX_USER_ID,\n" +
                        "                    TRX_MEMO,\n" +
                        "                    STORE_TIME,\n" +
                        "                    EVENT_CREATE_TIME )\n" +
                        "        Values\n" +
                        "                (?,?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "            CURRENT_TIMESTAMP,\n" +
                        "                ?)", generateID(Infos.Ohfbmchs.class)
                , hFHFBMCHSEQP_ID
                , hFHFBMCHSMAX_COUNT
                , convert(hFHFBMCHSCLAIM_TIME)
                , hFHFBMCHSCLAIM_USER_ID
                , hFHFBMCHSCLAIM_MEMO
                , convert(hFHFBMCHSEVENT_CREATE_TIME));

        return (returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processHoldRecord
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/7/2 16:06
     */
    public Response insertProcessHoldHistory(Infos.Ohphlhs processHoldRecord) {
        String hFHPHLHS_MAINPD_ID;
        String hFHPHLHS_OPE_NO;
        String hFHPHLHS_PD_ID;
        String hFHPHLHS_OPE_NAME;
        String hFHPHLHS_PROD_ID;
        String hFHPHLHS_CLAIM_TIME;
        Double hFHPHLHS_CLAIM_SHOP_DATE;
        String hFHPHLHS_CLAIM_USER_ID;
        String hFHPHLHS_ENTRY_TYPE;
        String hFHPHLHS_HOLD_TYPE;
        String hFHPHLHS_REASON_CODE;
        String hFHPHLHS_REASON_DESCRIPTION;
        Integer hFHPHLHS_HOLD_FLAG;
        String hFHPHLHS_STAGE_ID;
        String hFHPHLHS_STAGEGRP_ID;
        String hFHPHLHS_PHOTO_LAYER;
        String hFHPHLHS_CLAIM_MEMO;
        String hFHPHLHS_EVENT_CREATE_TIME;

        hFHPHLHS_MAINPD_ID = processHoldRecord.getMainpd_id();
        hFHPHLHS_OPE_NO = processHoldRecord.getOpe_no();
        hFHPHLHS_PD_ID = processHoldRecord.getPd_id();
        hFHPHLHS_OPE_NAME = processHoldRecord.getOpe_name();
        hFHPHLHS_PROD_ID = processHoldRecord.getProd_id();
        hFHPHLHS_CLAIM_TIME = processHoldRecord.getClaim_time();
        hFHPHLHS_CLAIM_SHOP_DATE = processHoldRecord.getClaim_shop_date();
        hFHPHLHS_CLAIM_USER_ID = processHoldRecord.getClaim_user_id();
        hFHPHLHS_ENTRY_TYPE = processHoldRecord.getEntry_type();
        hFHPHLHS_HOLD_TYPE = processHoldRecord.getHold_type();
        hFHPHLHS_REASON_CODE = processHoldRecord.getReason_code();
        hFHPHLHS_REASON_DESCRIPTION = processHoldRecord.getReason_description();
        hFHPHLHS_HOLD_FLAG = processHoldRecord.getHold_flag();
        hFHPHLHS_STAGE_ID = processHoldRecord.getStage_id();
        hFHPHLHS_STAGEGRP_ID = processHoldRecord.getStagegrp_id();
        hFHPHLHS_PHOTO_LAYER = processHoldRecord.getPhoto_layer();
        hFHPHLHS_CLAIM_MEMO = processHoldRecord.getClaim_memo();
        hFHPHLHS_EVENT_CREATE_TIME = processHoldRecord.getEvent_create_time();

        baseCore.insert("INSERT INTO OHBCKUP\n" +
                        "            ( ID, PROCESS_ID,\n" +
                        "                    OPE_NO,\n" +
                        "                    STEP_ID,\n" +
                        "                    OPE_NAME,\n" +
                        "                    PROD_ID,\n" +
                        "                    TRX_TIME,\n" +
                        "                    TRX_WORK_DATE,\n" +
                        "                    TRX_USER_ID,\n" +
                        "                    TASK_TYPE,\n" +
                        "                    HOLD_TYPE,\n" +
                        "                    REASON_CODE,\n" +
                        "                    REASON_DESC,\n" +
                        "                    WIP_HOLD_FLAG,\n" +
                        "                    STAGE_ID,\n" +
                        "                    STAGE_GRP_ID,\n" +
                        "                    PHOTO_LAYER,\n" +
                        "                    TRX_MEMO,\n" +
                        "                    EVENT_CREATE_TIME,\n" +
                        "                    STORE_TIME )\n" +
                        "        Values\n" +
                        "                ( ?, ?,\n" +
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
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "            CURRENT_TIMESTAMP )", generateID(Infos.Ohphlhs.class)
                , hFHPHLHS_MAINPD_ID
                , hFHPHLHS_OPE_NO
                , hFHPHLHS_PD_ID
                , hFHPHLHS_OPE_NAME
                , hFHPHLHS_PROD_ID
                , hFHPHLHS_CLAIM_TIME
                , hFHPHLHS_CLAIM_SHOP_DATE
                , hFHPHLHS_CLAIM_USER_ID
                , hFHPHLHS_ENTRY_TYPE
                , hFHPHLHS_HOLD_TYPE
                , hFHPHLHS_REASON_CODE
                , hFHPHLHS_REASON_DESCRIPTION
                , hFHPHLHS_HOLD_FLAG
                , hFHPHLHS_STAGE_ID
                , hFHPHLHS_STAGEGRP_ID
                , hFHPHLHS_PHOTO_LAYER
                , hFHPHLHS_CLAIM_MEMO
                , hFHPHLHS_EVENT_CREATE_TIME);

        return (returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhpjchs_Record
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/7/2 17:04
     */
    public Response insertProcessJobChangeHistory_FHPJCHS(Infos.Ohpjchs fhpjchs_Record) {
        String hFHPJCHSCTRLJOB_ID = "";
        String hFHPJCHSPRCSJOB_ID = "";
        String hFHPJCHSOPE_CATEGORY = "";
        String hFHPJCHSPRCS_ST = "";
        String hFHPJCHSCUR_STATE = "";
        String hFHPJCHSCLAIM_TIME = "";
        String hFHPJCHSCLAIM_USER_ID = "";
        String hFHPJCHSCLAIM_MEMO = "";
        String hFHPJCHSEVENT_CREATE_TIME = "";

        hFHPJCHSCTRLJOB_ID = fhpjchs_Record.getCtrlJob();
        hFHPJCHSPRCSJOB_ID = fhpjchs_Record.getPrcsJob();
        hFHPJCHSOPE_CATEGORY = fhpjchs_Record.getOpeCategory();
        hFHPJCHSPRCS_ST = fhpjchs_Record.getProcessStart();
        hFHPJCHSCUR_STATE = fhpjchs_Record.getCurrentState();
        hFHPJCHSCLAIM_TIME = fhpjchs_Record.getClaimTime();
        Double hFHPJCHSCLAIM_SHOP_DATE = fhpjchs_Record.getClaim_shop_date();
        hFHPJCHSCLAIM_USER_ID = fhpjchs_Record.getClaimUser();
        hFHPJCHSCLAIM_MEMO = fhpjchs_Record.getClaimMemo();
        hFHPJCHSEVENT_CREATE_TIME = fhpjchs_Record.getEventCreateTime();

        baseCore.insert("INSERT INTO OHPJCHG (ID,\n" +
                        " CJ_ID,\n" +
                        " PJ_ID,\n" +
                        " OPE_CATEGORY,\n" +
                        " PROCESS_START_FLAG,\n" +
                        " CUR_STATE,\n" +
                        " TRX_TIME,\n" +
                        " TRX_WORK_DATE,\n" +
                        " TRX_USER_ID,\n" +
                        " TRX_MEMO,\n" +
                        " EVENT_CREATE_TIME,\n" +
                        " STORE_TIME)" +
                        " Values (" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n?," +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "CURRENT_TIMESTAMP)",
                generateID(Infos.Ohpjchs.class)
                , hFHPJCHSCTRLJOB_ID
                , hFHPJCHSPRCSJOB_ID
                , hFHPJCHSOPE_CATEGORY
                , hFHPJCHSPRCS_ST
                , hFHPJCHSCUR_STATE
                , convert(hFHPJCHSCLAIM_TIME)
                , convert(hFHPJCHSCLAIM_SHOP_DATE)
                , hFHPJCHSCLAIM_USER_ID
                , hFHPJCHSCLAIM_MEMO
                , convert(hFHPJCHSEVENT_CREATE_TIME));

        return (returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhpjchs_wafer_Record
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/7/2 17:12
     */
    public Response insertProcessJobChangeHistory_FHPJCHS_WAFER(Infos.Ohpjchs_wafer fhpjchs_wafer_Record) {
        String hFHPJCHS_WAFERPRCSJOB_ID = "";
        String hFHPJCHS_WAFERWAFER_ID = "";
        String hFHPJCHS_WAFERLOT_ID = "";
        String hFHPJCHS_WAFERCLAIM_TIME = "";

        hFHPJCHS_WAFERPRCSJOB_ID = fhpjchs_wafer_Record.getPrcsJob();
        hFHPJCHS_WAFERWAFER_ID = fhpjchs_wafer_Record.getWaferID();
        hFHPJCHS_WAFERLOT_ID = fhpjchs_wafer_Record.getLotID();
        hFHPJCHS_WAFERCLAIM_TIME = fhpjchs_wafer_Record.getClaimTime();

        baseCore.insert("INSERT INTO OHPJCHG_WAFER (ID,\n" +
                        "            PJ_ID      ,WAFER_ID     ,LOT_ID          ,\n" +
                        "            TRX_TIME   )\n" +
                        "        Values  ( ?, \n" +
                        "                    ?,?,?,\n" +
                        "                    ?)", generateID(Infos.Ohpjchs_wafer.class)
                , hFHPJCHS_WAFERPRCSJOB_ID
                , hFHPJCHS_WAFERWAFER_ID
                , hFHPJCHS_WAFERLOT_ID
                , convert(hFHPJCHS_WAFERCLAIM_TIME));


        return (returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhpjchs_rparm_Record
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/7/2 17:15
     */
    public Response insertProcessJobChangeHistory_FHPJCHS_RPARM(Infos.Ohpjchs_rparm fhpjchs_rparm_Record) {
        String hFHPJCHS_RPARMPRCSJOB_ID = "";
        String hFHPJCHS_RPARMRPARM_NAME = "";
        String hFHPJCHS_RPARMPRE_RPARM_VALUE = "";
        String hFHPJCHS_RPARMRPARM_VALUE = "";
        String hFHPJCHS_RPARMCLAIM_TIME = "";

        hFHPJCHS_RPARMPRCSJOB_ID = fhpjchs_rparm_Record.getPrcsJob();
        hFHPJCHS_RPARMRPARM_NAME = fhpjchs_rparm_Record.getParameterName();
        hFHPJCHS_RPARMPRE_RPARM_VALUE = fhpjchs_rparm_Record.getPreviousParameterValue();
        hFHPJCHS_RPARMRPARM_VALUE = fhpjchs_rparm_Record.getParameterValue();
        hFHPJCHS_RPARMCLAIM_TIME = fhpjchs_rparm_Record.getClaimTime();

        baseCore.insert("INSERT INTO OHPJCHG_RPARAM (ID,\n" +
                        "            PJ_ID     ,RPARAM_NAME     ,PREV_RPARAM_VAL,\n" +
                        "            RPARAM_VAL    ,TRX_TIME     )\n" +
                        "        Values  (  ?,\n" +
                        "                    ?,?,?,\n" +
                        "                    ?,?)", generateID(Infos.Ohpjchs_rparm.class)
                , hFHPJCHS_RPARMPRCSJOB_ID
                , hFHPJCHS_RPARMRPARM_NAME
                , hFHPJCHS_RPARMPRE_RPARM_VALUE
                , hFHPJCHS_RPARMRPARM_VALUE
                , convert(hFHPJCHS_RPARMCLAIM_TIME));

        return (returnOK());
    }

}
