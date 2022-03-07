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
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/4        ********            Aoki                create file
 *
 * @author: Aoki
 * @date: 2021/3/4 10:38
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class RecipeGroupHistoryService {

        @Autowired
        private BaseCore baseCore;


       /**
       * description:
       * change history:
       * date             defect             person             comments
       * ---------------------------------------------------------------------------------------------------------------------
       * 2021/3/4 10:42                     Aoki                Create
       *
       * @author Aoki
       * @date 2021/3/4 10:42
       * @param id
       * @return com.fa.cim.fsm.Infos.FutureSplitEventRecord
       */
        public com.fa.cim.pr.Infos.RecipeGroupEventRecord getEventData(String id) {
            // get history from OMEVRECIPEGROUP
            String sql = "Select * from OMEVRECIPEGROUP where id=?";
            List<Map> baseDatas = baseCore.queryAllForMap(sql, id);

            com.fa.cim.pr.Infos.RecipeGroupEventRecord recipeGroupEventRecord = new com.fa.cim.pr.Infos.RecipeGroupEventRecord();
            Infos.EventData eventCommon = new Infos.EventData();
            recipeGroupEventRecord.setEventCommon(eventCommon);

            com.fa.cim.pr.Infos.RecipeGroupEventData recipeGroupEventData = new com.fa.cim.pr.Infos.RecipeGroupEventData();
            List<com.fa.cim.pr.Infos.RecipeEventData> recipeEventData = new ArrayList<>();
            recipeGroupEventRecord.setRecipeEventData(recipeEventData);

            baseDatas.stream().parallel().forEach(a->{
                recipeGroupEventRecord.setAction(convert(a.get("TASK_TYPE")));
                recipeGroupEventRecord.setRecipeGroupID(convert(a.get("RECIPE_GROUP_ID")));
                recipeGroupEventRecord.setType(convert(a.get("TYPE")));
                recipeGroupEventRecord.setClaimMemo(convert(a.get("TRX_MEMO")));

                // get history from OMEVRECIPEGROUP
                String prSql = "SELECT * FROM OMEVRECIPEGROUP_PR WHERE REFKEY=?";
                List<Map> recipeGroupData = baseCore.queryAllForMap(prSql, id);
                recipeGroupData.stream().parallel().forEach(rg->{
                    recipeGroupEventData.setPilotRunType(convert(rg.get("PR_TYPE")));
                    recipeGroupEventData.setPilotWaferCount(convertI(rg.get("PILOT_WAFER_COUNT")));
                    recipeGroupEventData.setCoverLevel(convertI(rg.get("COVER_LEVEL")));
                    recipeGroupEventData.setCoverRecipe(convertB(rg.get("COVER_RECIPE_FLAG")));
                    recipeGroupEventData.setEquipmentID(convert(rg.get("EQUIPMENT_ID")));
                    recipeGroupEventData.setFromEqpState(convert(rg.get("FROM_EQP_STATE")));
                    recipeGroupEventData.setToEqpState(convert(rg.get("TO_EQP_STATE")));
                    recipeGroupEventData.setClaimMemo(convert(rg.get("TRX_MEMO")));
                });
                recipeGroupEventRecord.setRecipeGroupEventData(recipeGroupEventData);

                // get history from OMEVRECIPEGROUP_RECIPE
                String recipeSql = "SELECT * FROM OMEVRECIPEGROUP_RECIPE WHERE REFKEY=?";
                List<Map> recipeData = baseCore.queryAllForMap(recipeSql, id);
                recipeData.stream().parallel().forEach(re->{
                    com.fa.cim.pr.Infos.RecipeEventData recipe = new com.fa.cim.pr.Infos.RecipeEventData();
                    recipe.setRecipeID(convert(re.get("RECIPE_ID")));
                    recipeEventData.add(recipe);
                });

                eventCommon.setTransactionID(convert(a.get("TRX_ID")));
                eventCommon.setEventTimeStamp(convert(a.get("EVENT_TIME")));

                eventCommon.setUserID(convert(a.get("TRX_USER_ID")));
                eventCommon.setEventMemo(convert(a.get("TRX_MEMO")));
                eventCommon.setEventCreationTimeStamp(convert(a.get("EVENT_CREATE_TIME")));

            });

            return recipeGroupEventRecord;
        }

        @Transactional(rollbackFor = Exception.class)
        public void deleteFIFO(String tableName, String refKey) {
            String sql = String.format("DELETE %s WHERE REFKEY=?", tableName);
            baseCore.insert(sql, refKey);
        }

        /**
        * description:
        * change history:
        * date             defect             person             comments
        * ---------------------------------------------------------------------------------------------------------------------
        * 2021/3/4 10:41                     Aoki                Create
        *
        * @author Aoki
        * @date 2021/3/4 10:41
        * @param tableName
        * @return java.util.List<java.lang.String>
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
       * change history:
       * date             defect             person             comments
       * ---------------------------------------------------------------------------------------------------------------------
       * 2021/3/4 10:41                     Aoki                Create
       *
       * @author Aoki
       * @date 2021/3/4 10:41
       * @param insertRecipeGroupRecord_OHRECIPEGROUPHS
       * @return com.fa.cim.dto.Response
       */
        public Response insertRecipeGroupRecord_OHRECIPEGROUPHS(com.fa.cim.pr.Infos.RecipeGroupHs insertRecipeGroupRecord_OHRECIPEGROUPHS) {


            // add history for OMEVRECIPEGROUP
            String action;
            String recipeGroupID;
            String type;
            String eventCreateTime;
            String createTime;
            String claimUserID;
            String claimMemo;
            log.info("HistoryWatchDogServer::insertRecipeGroupRecord_OMEVRECIPEGROUPHS Function");

            action = insertRecipeGroupRecord_OHRECIPEGROUPHS.getAction();
            recipeGroupID = insertRecipeGroupRecord_OHRECIPEGROUPHS.getRecipeGroupID();
            type = insertRecipeGroupRecord_OHRECIPEGROUPHS.getType();
            eventCreateTime = insertRecipeGroupRecord_OHRECIPEGROUPHS.getEventCreateTime();
            createTime = insertRecipeGroupRecord_OHRECIPEGROUPHS.getCreateTime();
            claimUserID = insertRecipeGroupRecord_OHRECIPEGROUPHS.getClaimUserID();
            claimMemo = insertRecipeGroupRecord_OHRECIPEGROUPHS.getClaimMemo();
            baseCore.insert("INSERT INTO OHRECIPEGROUPHS(ID,TASK_TYPE,RECIPE_GROUP_ID,TYPE,EVENT_CREATE_TIME,CREATE_TIME,TRX_USER_ID,TRX_MEMO) VALUES (?,?,?,?,?,?,?,?)",
                    generateID(com.fa.cim.pr.Infos.RecipeGroupHs.class),
                    action,
                    recipeGroupID,
                    type,
                    eventCreateTime,
                    createTime,
                    claimUserID,
                    claimMemo);

            log.info("HistoryWatchDogServer::insertRecipeGroupRecord_OHRECIPEGROUPHS Function");
            return returnOK();
        }

        /**
        * description:
        * change history:
        * date             defect             person             comments
        * ---------------------------------------------------------------------------------------------------------------------
        * 2021/3/4 15:50                     Aoki                Create
        *
        * @author Aoki
        * @date 2021/3/4 15:50
        * @param insertRecipeGroupRecord_OHRECIPEGROUPHS_PR
        * @return com.fa.cim.dto.Response
        */
        public Response insertRecipeGroupRecord_OHRECIPEGROUPHS_PR(com.fa.cim.pr.Infos.RecipeGroupHsPr insertRecipeGroupRecord_OHRECIPEGROUPHS_PR) {

            String equipmentID;
            String pilotRunTyoe;
            Integer pilotWaferCount;
            Integer coverLevel;
            Boolean coverRecipe;
            String fromEqpState;
            String toEqpState;
            String claimMemo;

            equipmentID = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getEquipmentID();
            pilotRunTyoe = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getPilotRunType();
            pilotWaferCount = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getPilotWaferCount();
            coverLevel = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getCoverLevel();
            coverRecipe = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getCoverRecipe();
            fromEqpState = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getFromEqpState();
            toEqpState = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getToEqpState();
            claimMemo = insertRecipeGroupRecord_OHRECIPEGROUPHS_PR.getClaimMemo();

            log.info("HistoryWatchDogServer::insertRecipeGroupRecord_OMEVRECIPEGROUP_PR Function");

            baseCore.insert("INSERT INTO OHRECIPEGROUPHS_PR(ID,EQP_ID,PR_TYPE,PILOT_WAFER_COUNT,COVER_LEVEL,COVER_RECIPE_FLAG,FROM_EQP_STATE,TO_EQP_STATE,TRX_MEMO) VALUES (?,?,?,?,?,?,?,?,?)", generateID(com.fa.cim.pr.Infos.RecipeGroupHsPr.class),
                    equipmentID,
                    pilotRunTyoe,
                    pilotWaferCount,
                    coverLevel,
                    coverRecipe,
                    fromEqpState,
                    toEqpState,
                    claimMemo);

            log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTURE_WAFER Function");
            return returnOK();
        }

        /**
        * description:
        * change history:
        * date             defect             person             comments
        * ---------------------------------------------------------------------------------------------------------------------
        * 2021/3/4 15:50                     Aoki                Create
        *
        * @author Aoki
        * @date 2021/3/4 15:50
        * @param recipeGroupEventRecord_OHRECIPEGROUPHS_RECIPE
        * @return com.fa.cim.dto.Response
        */
        public Response insertRecipeGroupRecord_OHRECIPEGROUPHS_RECIPE(com.fa.cim.pr.Infos.RecipeGroupHsRecipe recipeGroupEventRecord_OHRECIPEGROUPHS_RECIPE) {

           String recipeID;

           recipeID = recipeGroupEventRecord_OHRECIPEGROUPHS_RECIPE.getRecipeID();

            log.info("HistoryWatchDogServer::insertRecipeGroupRecord_OMEVRECIPEGROUP_PR Function");

            baseCore.insert("INSERT INTO OHRECIPEGROUPHS_RECIPE(ID,RECIPE_ID) VALUES (?,?)", generateID(com.fa.cim.pr.Infos.RecipeGroupHsRecipe.class),
                    recipeID);

            log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTURE_WAFER Function");
            return returnOK();
        }
    }
