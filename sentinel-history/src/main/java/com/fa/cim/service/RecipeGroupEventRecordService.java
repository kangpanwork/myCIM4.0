package com.fa.cim.service;

import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;

/**
* description:
* change history:
* date             defect             person             comments
* ---------------------------------------------------------------------------------------------------------------------
* 2021/3/3 17:27                     Aoki                Create
*
* @author Aoki
* @date 2021/3/3 17:27
* @return
*/
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class RecipeGroupEventRecordService {

    @Autowired
    private RecipeGroupHistoryService recipeGroupHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param recipeGroupEventRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:12
     */
    public Response createRecipeGroupEventRecord(com.fa.cim.pr.Infos.RecipeGroupEventRecord recipeGroupEventRecord) {
         log.info("HistoryWatchDogServer::createRecipeGroupEventRecord Function");
         Response iRc = returnOK();
        com.fa.cim.pr.Infos.RecipeGroupHs recipeGroupHs = new com.fa.cim.pr.Infos.RecipeGroupHs();
        com.fa.cim.pr.Infos.RecipeGroupHsPr recipeGroupHsPr = new com.fa.cim.pr.Infos.RecipeGroupHsPr();

        //make history for recipeGroup OHRECIPEGROUPHS
        recipeGroupHs.setAction(recipeGroupEventRecord.getAction());
        recipeGroupHs.setClaimMemo(recipeGroupEventRecord.getClaimMemo());
        recipeGroupHs.setClaimUserID(recipeGroupEventRecord.getEventCommon().getUserID());
        recipeGroupHs.setEventCreateTime(recipeGroupEventRecord.getEventCommon().getEventCreationTimeStamp());
        recipeGroupHs.setCreateTime(recipeGroupEventRecord.getEventCommon().getEventCreationTimeStamp());
        recipeGroupHs.setRecipeGroupID(recipeGroupEventRecord.getRecipeGroupID());
        recipeGroupHs.setType(recipeGroupEventRecord.getType());
        // add history for recipeGroup OHRECIPEGROUPHS
        iRc = recipeGroupHistoryService.insertRecipeGroupRecord_OHRECIPEGROUPHS(recipeGroupHs);
        if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createRecipeGroupEventRecord(): InsertFutureSplitEventRecord_FHELCHS SQL Error Occured" );
                log.info("HistoryWatchDogServer::createRecipeGroupEventRecord Function" );
                return( iRc );
        }

        //make history for recipeGroup OHRECIPEGROUPHS_PR
        recipeGroupHsPr.setEquipmentID(recipeGroupEventRecord.getRecipeGroupEventData().getEquipmentID());
        recipeGroupHsPr.setPilotRunType(recipeGroupEventRecord.getRecipeGroupEventData().getPilotRunType());
        recipeGroupHsPr.setPilotWaferCount(recipeGroupEventRecord.getRecipeGroupEventData().getPilotWaferCount());
        recipeGroupHsPr.setCoverLevel(recipeGroupEventRecord.getRecipeGroupEventData().getCoverLevel());
        recipeGroupHsPr.setCoverRecipe(recipeGroupEventRecord.getRecipeGroupEventData().getCoverRecipe());
        recipeGroupHsPr.setFromEqpState(recipeGroupEventRecord.getRecipeGroupEventData().getFromEqpState());
        recipeGroupHsPr.setToEqpState(recipeGroupEventRecord.getRecipeGroupEventData().getToEqpState());
        recipeGroupHsPr.setClaimMemo(recipeGroupEventRecord.getRecipeGroupEventData().getClaimMemo());
        // add history for recipeGroup OHRECIPEGROUPHS_PR
        iRc = recipeGroupHistoryService.insertRecipeGroupRecord_OHRECIPEGROUPHS_PR(recipeGroupHsPr);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createRecipeGroupEventRecord(): insertRecipeGroupRecord_OHRECIPEGROUPHS_PR SQL Error Occured" );
            log.info("HistoryWatchDogServer::createRecipeGroupEventRecord Function" );
            return( iRc );
        }

        //make history for recipe OHRECIPEGROUPHS_RECIPE
        List<com.fa.cim.pr.Infos.RecipeEventData> recipeEventData = recipeGroupEventRecord.getRecipeEventData();

        recipeEventData.forEach(a->{
            com.fa.cim.pr.Infos.RecipeGroupHsRecipe recipeGroupHsRecipe = new com.fa.cim.pr.Infos.RecipeGroupHsRecipe();
            recipeGroupHsRecipe.setRecipeID(a.getRecipeID());
            // add history for recipe OHRECIPEGROUPHS_RECIPE
            Response res = recipeGroupHistoryService.insertRecipeGroupRecord_OHRECIPEGROUPHS_RECIPE(recipeGroupHsRecipe);
        });
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createRecipeGroupEventRecord(): insertRecipeGroupRecord_OHRECIPEGROUPHS_RECIPE SQL Error Occured" );
            log.info("HistoryWatchDogServer::createRecipeGroupEventRecord Function" );
            return( iRc);
        }
        log.info("HistoryWatchDogServer::createRecipeGroupEventRecord Function" );
        return  (returnOK());
    }
}
