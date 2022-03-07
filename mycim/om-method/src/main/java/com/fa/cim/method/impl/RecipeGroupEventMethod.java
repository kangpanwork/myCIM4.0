package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IRecipeGroupEventMethod;
import com.fa.cim.newcore.bo.event.CimRecipeGroupEvent;
import com.fa.cim.newcore.bo.event.EventManager;
import com.fa.cim.newcore.dto.event.Event;
import com.fa.cim.pr.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/4        ********            Aoki                create file
 *
 * @author: Aoki
 * @date: 2021/3/4 16:18
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class RecipeGroupEventMethod implements IRecipeGroupEventMethod {
    @Autowired
    private EventManager eventManager;

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 16:22                     Aoki                Create
    *
    * @author Aoki
    * @date 2021/3/4 16:22
    * @param objCommon
    * @param txId
    * @param testMemo
    * @param strExperimentalGroupInfo
    * @return void
    */
    @Override
    public void experimentalRecipeGroupRegistEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, Info.ExperimentalGroupInfo strExperimentalGroupInfo) {

        //initial
        log.info("in para transactionID,{}", txId);
        log.info("in para testMemo,{}", testMemo);

        Event.EventData eventCommon = new Event.EventData();
        Event.RecipeGroupEventRecord recipeGroupEventRecord = new Event.RecipeGroupEventRecord();

        recipeGroupEventRecord.setEventCommon(eventCommon);
        List<Event.RecipeGroupPilotData> recipeGroupPilotDatas = new ArrayList<>();
        Event.RecipeGroupPilotData recipeGroupPilotData = new Event.RecipeGroupPilotData();
        List<String> recipes = new ArrayList<>();
        recipeGroupEventRecord.setRecipes(recipes);

        // set eventCommon data
        eventCommon.setTransactionID(txId);
        eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
        eventCommon.setEventMemo(testMemo);

        // set recipeGroupEventRecord data
        recipeGroupEventRecord.setAction(strExperimentalGroupInfo.getAction());
        recipeGroupEventRecord.setType(strExperimentalGroupInfo.getType());
        recipeGroupEventRecord.setRecipeGroupID(strExperimentalGroupInfo.getRecipeGroupID().getValue());

        // set recipeGroupEventData
        recipeGroupPilotData.setEquipmentID(strExperimentalGroupInfo.getRecipeGroupEventData().getEquipmentID().getValue());
        recipeGroupPilotData.setPilotRunType(strExperimentalGroupInfo.getRecipeGroupEventData().getPilotRunType());
        recipeGroupPilotData.setPilotWaferCount(strExperimentalGroupInfo.getRecipeGroupEventData().getPilotWaferCount());
        recipeGroupPilotData.setCoverLevel(strExperimentalGroupInfo.getRecipeGroupEventData().getCoverLevel());
        recipeGroupPilotData.setCoverRecipe(strExperimentalGroupInfo.getRecipeGroupEventData().getCoverRecipe());
        recipeGroupPilotData.setFromEqpState(strExperimentalGroupInfo.getRecipeGroupEventData().getFromEqpState());
        recipeGroupPilotData.setToEqoState(strExperimentalGroupInfo.getRecipeGroupEventData().getToEqpState());
        recipeGroupPilotData.setClaimMemo(strExperimentalGroupInfo.getRecipeGroupEventData().getClaimMemo());

        recipeGroupPilotDatas.add(recipeGroupPilotData);
        recipeGroupEventRecord.setPilots(recipeGroupPilotDatas);
        // set recipeEventData
        strExperimentalGroupInfo.getRecipeIDs().stream().parallel().forEach(re->{
            recipes.add(re.getRecipeID().getValue());
        });
        log.info("start save event data");
         eventManager.createEvent(recipeGroupEventRecord, CimRecipeGroupEvent.class);
    }
}
