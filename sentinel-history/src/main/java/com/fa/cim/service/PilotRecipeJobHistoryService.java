package com.fa.cim.service;

import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Response;
import com.fa.cim.fsm.Infos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 * @author zh
 * @date 2021/3/34 16:16
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class PilotRecipeJobHistoryService {

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
     * @return com.fa.cim.fsm.Infos.PilotEventRecord
     * @exception
     * @author zh
     * @date 2021/3/4 14:56
     */
    public Infos.PilotEventRecord getEventData(String id) {
        String sql="Select * from OMEVRECIPEPRJOB where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.PilotEventRecord eventRecord = new Infos.PilotEventRecord();

        com.fa.cim.dto.Infos.EventData evenCommon = new com.fa.cim.dto.Infos.EventData();
        eventRecord.setEventCommon(evenCommon);

        List<Infos.PilotRecipeEventData> eventDatas = new ArrayList<>();
        eventRecord.setSubRecipes(eventDatas);

        for (Map sqlData : sqlDatas) {
            //add psmJobID for history
            eventRecord.setAction(convert(sqlData.get("TASK_TYPE")));
            eventRecord.setRecipeGroupID(convert(sqlData.get("RECIPE_GROUP_ID")));
            eventRecord.setLotID(convert(sqlData.get("LOT_ID")));
            eventRecord.setStatus(convert(sqlData.get("STATUS")));
            eventRecord.setEqpID(convert(sqlData.get("EQP_ID")));
            eventRecord.setPrType(convert(sqlData.get("PR_TYPE")));
            eventRecord.setPiLotWaferCount(((BigDecimal)convert(sqlData.get("PILOT_WAFER_COUNT"))).intValue());
            eventRecord.setCoverLevel(((BigDecimal)convert(sqlData.get("COVER_LEVEL"))).intValue());
            eventRecord.setCoverRecipe(((BigDecimal)convert(sqlData.get("COVER_RECIPE_FLAG"))).intValue());
            eventRecord.setFromEqpState(convert(sqlData.get("FROM_EQP_STATE")));
            eventRecord.setToEqpState(convert(sqlData.get("TO_EQP_STATE")));

            evenCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            evenCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            evenCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));
            evenCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));

            evenCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVRECIPEPRJOB_RECIPE WHERE REFKEY=?";
            List<Map> subRecipes = baseCore.queryAllForMap(sql, id);
            for (Map subRecipe : subRecipes) {
                Infos.PilotRecipeEventData eventData = new Infos.PilotRecipeEventData();
                eventDatas.add(eventData);

                eventData.setRecipeID(convert(subRecipe.get("RECIPE_ID")));
            }
        }
        return eventRecord;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhPilotJobhs
     * @return com.fa.cim.dto.Response
     * @exception
     * @author zh
     * @date 2021/3/4 17:36
     */
    public com.fa.cim.dto.Infos.Output insertPilotJob_OHEVRECIPEPRJOBHS(Infos.OhPilotJobhs fhPilotJobhs) {
        String hPILOTJOB_ACTION_CODE;
        String hPILOTJOB_RECIPE_GROUP_ID;
        String hPILOTJOB_LOT_ID;
        String hPILOTJOB_STATUS;
        String hPILOTJOB_EQP_ID;
        String hPILOTJOB_PR_TYPE;
        Integer hPILOTJOB_PILOT_WAFER_COUNT;
        Integer hPILOTJOB_COVER_LEVEL;
        Integer hPILOTJOB_COVER_RECIPE;
        String hPILOTJOB_FROM_EQP_STATE;
        String hPILOTJOB_TO_EQP_STATE;
        String hPILOTJOB_EVENT_CREATE_TIME;
        String hPILOTJOB_CLAIM_USER_ID;
        String hPILOTJOB_CLAIM_MEMO;

        log.info("HistoryWatchDogServer::insertPilotJob_OHEVRECIPEPRJOBHS Function" );

        hPILOTJOB_ACTION_CODE = fhPilotJobhs.getAction_code();
        hPILOTJOB_RECIPE_GROUP_ID = fhPilotJobhs.getRecipe_group_id();
        hPILOTJOB_LOT_ID = fhPilotJobhs.getLot_id();
        hPILOTJOB_STATUS = fhPilotJobhs.getStatus();
        hPILOTJOB_EQP_ID = fhPilotJobhs.getEqp_id();
        hPILOTJOB_PR_TYPE = fhPilotJobhs.getPr_type();
        hPILOTJOB_PILOT_WAFER_COUNT = fhPilotJobhs.getPilot_wafer_count();
        hPILOTJOB_COVER_LEVEL = fhPilotJobhs.getCover_level();
        hPILOTJOB_COVER_RECIPE = fhPilotJobhs.getCover_recipe();
        hPILOTJOB_FROM_EQP_STATE = fhPilotJobhs.getFrom_eqp_state();
        hPILOTJOB_TO_EQP_STATE = fhPilotJobhs.getTo_eqp_state();
        hPILOTJOB_EVENT_CREATE_TIME = fhPilotJobhs.getEvent_create_time();
        hPILOTJOB_CLAIM_USER_ID = fhPilotJobhs.getClaim_user_id();
        hPILOTJOB_CLAIM_MEMO = fhPilotJobhs.getClaim_memo();
        String refkey = generateID(Infos.PilotEventRecord.class);

        com.fa.cim.dto.Infos.Output output = new com.fa.cim.dto.Infos.Output();
        output.setRefkey(refkey);

        baseCore.insert("INSERT INTO OHRECIPEPRJOBHS (\n" +
                        "\tID,\n" +
                        "\tACTION,\n" +
                        "\tRECIPE_GROUP_ID,\n" +
                        "\tLOT_ID,\n" +
                        "\tSTATUS,\n" +
                        "\tEQP_ID,\n" +
                        "\tPR_TYPE,\n" +
                        "\tPILOT_WAFER_COUNT,\n" +
                        "\tCOVER_LEVEL,\n" +
                        "\tCOVER_RECIPE_FLAG,\n" +
                        "\tFROM_EQP_STATE,\n" +
                        "\tTO_EQP_STATE,\n" +
                        "\tCREATE_TIME,\n" +
                        "\tEVENT_CREATE_TIME,\n" +
                        "\tTRX_USER_ID,\n" +
                        "\tTRX_MEMO \n" +
                        ")\n" +
                        "VALUES\n" +
                        "\t(?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?,?)",
                refkey,
                hPILOTJOB_ACTION_CODE,
                hPILOTJOB_RECIPE_GROUP_ID,
                hPILOTJOB_LOT_ID,
                hPILOTJOB_STATUS,
                hPILOTJOB_EQP_ID,
                hPILOTJOB_PR_TYPE,
                hPILOTJOB_PILOT_WAFER_COUNT,
                hPILOTJOB_COVER_LEVEL,
                hPILOTJOB_COVER_RECIPE,
                hPILOTJOB_FROM_EQP_STATE,
                hPILOTJOB_TO_EQP_STATE,
                hPILOTJOB_EVENT_CREATE_TIME,
                hPILOTJOB_CLAIM_USER_ID,
                hPILOTJOB_CLAIM_MEMO);

        log.info("HistoryWatchDogServer::insertPilotJob_OHEVRECIPEPRJOBHS Function" );
        output.setResponse(returnOK());
        return output;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhPilotJobhs
     * @return com.fa.cim.dto.Response
     * @exception
     * @author zh
     * @date 2021/3/4 17:36
     */
    public Response insertPilotJob_OHEVRECIPEPRJOBHS_RECIPE(Infos.OhPilotJobhsRecipe fhPilotJobhs) {
        log.info("HistoryWatchDogServer::insertPilotJob_OHEVRECIPEPRJOBHS_RECIPE Function" );
        String hPILOTJOB_RECIPE_ID;
        String hREFKEY;

        hPILOTJOB_RECIPE_ID = fhPilotJobhs.getRecipe_id();
        hREFKEY = fhPilotJobhs.getRefkey();

        baseCore.insert("INSERT INTO OHRECIPEPRJOBHS_RECIPE ( \n" +
                "\tID,\n" +
                "\tRECIPE_ID,\n" +
                "\tREFKEY\n" +
                ")\n" +
                "VALUES\n" +
                "\t(?,?,?)",
                generateID(Infos.OhPilotJobhsRecipe.class),
                hPILOTJOB_RECIPE_ID,
                hREFKEY);

        log.info("HistoryWatchDogServer::insertPilotJob_OHEVRECIPEPRJOBHS_RECIPE Function" );
        return returnOK();
    }
}
