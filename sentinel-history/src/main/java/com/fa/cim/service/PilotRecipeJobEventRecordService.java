package com.fa.cim.service;

import com.fa.cim.dto.Response;
import com.fa.cim.fsm.Infos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author zh
 * @exception
 * @date 2021/3/4 14:14
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class PilotRecipeJobEventRecordService {

    @Autowired
    private PilotRecipeJobHistoryService pilotRecipeJobHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eventRecord
     * @return
     * @throws
     * @author zh
     * @date 2021/3/4 14:16
     */
    public Response createPilotRecipeJobEventRecord(Infos.PilotEventRecord eventRecord) {
        log.info("HistoryWatchDogServer::createPilotRecipeJobEventRecord Function");
        Infos.OhPilotJobhs fhPilotJobhs;
        Infos.OhPilotJobhsRecipe fhPilotJobhsRecipe;

        Response iRc = returnOK();
        fhPilotJobhs = new Infos.OhPilotJobhs();
        fhPilotJobhsRecipe = new Infos.OhPilotJobhsRecipe();

        fhPilotJobhs.setAction_code(eventRecord.getAction());
        fhPilotJobhs.setRecipe_group_id(eventRecord.getRecipeGroupID());
        fhPilotJobhs.setLot_id(eventRecord.getLotID());
        fhPilotJobhs.setStatus(eventRecord.getStatus());
        fhPilotJobhs.setEqp_id(eventRecord.getEqpID());
        fhPilotJobhs.setPr_type(eventRecord.getPrType());
        fhPilotJobhs.setPilot_wafer_count(eventRecord.getPiLotWaferCount());
        fhPilotJobhs.setCover_level(eventRecord.getCoverLevel());
        fhPilotJobhs.setCover_recipe(eventRecord.getCoverRecipe());
        fhPilotJobhs.setFrom_eqp_state(eventRecord.getFromEqpState());
        fhPilotJobhs.setTo_eqp_state(eventRecord.getToEqpState());
        fhPilotJobhs.setEvent_create_time(eventRecord.getEventCommon().getEventCreationTimeStamp());
        fhPilotJobhs.setClaim_user_id(eventRecord.getEventCommon().getUserID());
        fhPilotJobhs.setClaim_memo(eventRecord.getEventCommon().getEventMemo());

        com.fa.cim.dto.Infos.Output output = pilotRecipeJobHistoryService.insertPilotJob_OHEVRECIPEPRJOBHS(fhPilotJobhs);
        if( !isOk(output.getResponse()) ) {
            log.info("HistoryWatchDogServer::createPilotRecipeJobEventRecord(): insertPilotJob_OHEVRECIPEPRJOBHS SQL Error Occured" );
            return( iRc );
        }

        if (length(eventRecord.getSubRecipes()) > 0) {
            for (int i = 0; i < length(eventRecord.getSubRecipes()); i++) {
                fhPilotJobhsRecipe.setRecipe_id(eventRecord.getSubRecipes().get(i).getRecipeID());
                fhPilotJobhsRecipe.setRefkey(output.getRefkey());
                iRc = pilotRecipeJobHistoryService.insertPilotJob_OHEVRECIPEPRJOBHS_RECIPE(fhPilotJobhsRecipe);
            }
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createPilotRecipeJobEventRecord(): insertPilotJob_OHEVRECIPEPRJOBHS_RECIPE SQL Error Occured" );
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::createPilotRecipeJobEventRecord Function" );
        return returnOK();
    }

}
