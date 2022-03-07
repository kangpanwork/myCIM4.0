package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IPilotRecipeJobEventMethod;
import com.fa.cim.newcore.bo.event.CimRecipeJobEvent;
import com.fa.cim.newcore.bo.event.EventManager;
import com.fa.cim.newcore.dto.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@OmMethod
@Slf4j
public class PilotRecipeJobEventMethod implements IPilotRecipeJobEventMethod {

    @Autowired
    private EventManager eventManager;

    @Override
    public void pilotEventMake(Infos.ObjCommon objCommon, String transactionID, String memo, com.fa.cim.fsm.Infos.PilotEventMakeInfo pilotEventMakeInfo) {
        Event.RecipeJobEventRecord recipeJobEventRecord = new Event.RecipeJobEventRecord();

        log.debug("set event common param");
        recipeJobEventRecord.setEventCommon(setEventData(objCommon, transactionID, memo));

        log.debug("set event record params");
        recipeJobEventRecord.setAction(pilotEventMakeInfo.getAction());
        recipeJobEventRecord.setRecipeGroupID(ObjectIdentifier.fetchValue(pilotEventMakeInfo.getRecipeGroupID()));
        recipeJobEventRecord.setLotID(ObjectIdentifier.fetchValue(pilotEventMakeInfo.getLotID()));
        recipeJobEventRecord.setStatus(pilotEventMakeInfo.getStatus());
        recipeJobEventRecord.setEqpID(ObjectIdentifier.fetchValue(pilotEventMakeInfo.getEqpID()));
        recipeJobEventRecord.setPrType(pilotEventMakeInfo.getPrType());
        recipeJobEventRecord.setPilotWaferCount(pilotEventMakeInfo.getPiLotWaferCount());
        recipeJobEventRecord.setCoverLevel(pilotEventMakeInfo.getCoverLevel());
        recipeJobEventRecord.setCoverRecipe(pilotEventMakeInfo.getCoverRecipe());
        recipeJobEventRecord.setFromEqpState(pilotEventMakeInfo.getFromEqpState());
        recipeJobEventRecord.setToEqpState(pilotEventMakeInfo.getToEqpState());

        log.debug("set recipe param");
        List<com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo> piLotRecipeIDs = pilotEventMakeInfo.getPiLotRecipeIDs();
        List<String> subRecipes = Optional.ofNullable(piLotRecipeIDs).orElse(Collections.emptyList()).stream()
                .map(ele -> ObjectIdentifier.fetchValue(ele.getRecipeIDs()))
                .collect(Collectors.toList());
        recipeJobEventRecord.setRecipes(subRecipes);

        log.debug("create event");
        eventManager.createEvent(recipeJobEventRecord, CimRecipeJobEvent.class);
    }

    public Event.EventData setEventData(Infos.ObjCommon objCommon, String transactionID, String memo) {
        Event.EventData eventData = new Event.EventData();
        eventData.setTransactionID(transactionID);
        eventData.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventData.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventData.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        eventData.setEventMemo(memo);
        eventData.setEventCreationTimeStamp(CimDateUtils.getCurrentTimeStamp().toString());
        return eventData;
    }
}
