package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
 * @date 2019/7/25 11:29
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class RecipeBodyManageEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private RecipeBodyManageHistoryService recipeBodyManageHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param recipeBodyManageEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 16:21
     */
    public Response createRecipeBodyManageEventRecord(Infos.RecipeBodyManageEventRecord recipeBodyManageEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohrcpmhs     fhrcpmhs;
        Params.String areaID = new Params.String();
        Params.String eqpName = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateRecipeBodyManageHistory Function" );
        iRc = tableMethod.getFREQP( recipeBodyManageEventRecord.getEquipmentID(), areaID, eqpName );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateRecipeBodyManageEventRecord Function" );
            return( iRc );
        }
        fhrcpmhs=new Infos.Ohrcpmhs();

        fhrcpmhs.setEqp_id(        recipeBodyManageEventRecord.getEquipmentID() );
        fhrcpmhs.setEqp_name(      eqpName.getValue() );
        fhrcpmhs.setAction_code(   recipeBodyManageEventRecord.getActionCode() );
        fhrcpmhs.setRecipe_id(     recipeBodyManageEventRecord.getMachineRecipeID() );
        fhrcpmhs.setPh_recipe_id(  recipeBodyManageEventRecord.getPhysicalRecipeID());
        fhrcpmhs.setFile_location( recipeBodyManageEventRecord.getFileLocation() );
        fhrcpmhs.setFile_name(     recipeBodyManageEventRecord.getFileName() );
        fhrcpmhs.setFormat_flag (  convertI(recipeBodyManageEventRecord.getFormatFlag()));
        fhrcpmhs.setClaim_memo(    recipeBodyManageEventRecord.getEventCommon().getEventMemo() );
        fhrcpmhs.setEvent_create_time( recipeBodyManageEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = recipeBodyManageHistoryService.insertRecipeBodyManageHistory(fhrcpmhs);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createRecipeBodyManageEventRecord(): InsertRecipeBodyManageHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateRecipeBodyManageEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateRecipeBodyManageEventRecord Function" );
        return(returnOK());
    }

}
