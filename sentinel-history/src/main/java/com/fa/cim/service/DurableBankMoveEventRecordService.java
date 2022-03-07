package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.ODRBW019_ID;
import static com.fa.cim.Constant.TransactionConstant.ODRBW021_ID;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/7/11 10:37
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableBankMoveEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableControlJobStatusChangeHistoryService durableControlJobStatusChangeHistoryService;

    @Autowired
    private DurableOperationStartHistoryService durableOperationStartHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurableBankMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/12 13:28
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createDurableBankMoveEventRecord( Infos.DurableBankMoveEventRecord DurableBankMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableBankMoveEventRecord Function");
        Infos.Ohdrblopehs fhdrblopehs= new Infos.Ohdrblopehs();
        Infos.Frpd        resultData_pd= new Infos.Frpd();
        Infos.Frpos       resultData_pos= new Infos.Frpos();
        Params.String   stageGrpID  = new Params.String();
        Params.String   areaID      = new Params.String();
        Params.String   locationID  = new Params.String();
        Params.String   stockerID   = new Params.String();
        Params.String   description = new Params.String();
        Response iRc = returnOK();
        fhdrblopehs = new Infos.Ohdrblopehs();
        resultData_pd = new Infos.Frpd();
        resultData_pos = new Infos.Frpos();
        iRc = tableMethod.getFRPD( DurableBankMoveEventRecord.getDurableData().getOperationID(), resultData_pd );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurableBankMoveEventRecord.getDurableData().getObjrefPOS(),                    DurableBankMoveEventRecord.getDurableData().getOperationNumber(),                    DurableBankMoveEventRecord.getDurableData().getObjrefMainPF(),                    resultData_pos );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc );
        }
        resultData_pos.setOperationNO(DurableBankMoveEventRecord.getDurableData().getOperationNumber() );
        resultData_pos.setPdID(DurableBankMoveEventRecord.getDurableData().getOperationID()     );
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID(), stageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc);
        }
        iRc = tableMethod.getFRBANK( DurableBankMoveEventRecord.getDurableData().getBankID(), stockerID );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc);
        }
        iRc = tableMethod.getFRSTK( stockerID,areaID,description );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc);
        }
        iRc = tableMethod.getFRAREA( areaID,locationID );
        if( !isOk(iRc) ) {
            log.info("HistroyWatchDogServer::CreateDurableBankMoveEventRecord Function");
            return( iRc);
        }
        fhdrblopehs.setDurable_id(DurableBankMoveEventRecord.getDurableData().getDurableID()       );
        fhdrblopehs.setDrbl_category(DurableBankMoveEventRecord.getDurableData().getDurableCategory() );
        if( variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW021_ID ) == 0 ) {
            fhdrblopehs.setMainpd_id("" );
            fhdrblopehs.setOpe_no("" );
            fhdrblopehs.setPd_id("" );
            fhdrblopehs.setOpe_pass_count(0L);
            fhdrblopehs.setPd_name("" );
        }
        else
        {
            fhdrblopehs.setMainpd_id(DurableBankMoveEventRecord.getDurableData().getRouteID()         );
            fhdrblopehs.setOpe_no(DurableBankMoveEventRecord.getDurableData().getOperationNumber() );
            fhdrblopehs.setPd_id(DurableBankMoveEventRecord.getDurableData().getOperationID()     );
            fhdrblopehs.setOpe_pass_count(DurableBankMoveEventRecord.getDurableData().getOperationPassCount());
            fhdrblopehs.setPd_name(resultData_pd.getOperationName()                             );
        }
        fhdrblopehs.setClaim_time(DurableBankMoveEventRecord.getEventCommon().getEventTimeStamp()  );
        fhdrblopehs.setClaim_shop_date(DurableBankMoveEventRecord.getEventCommon().getEventShopDate()  );
        fhdrblopehs.setClaim_user_id(DurableBankMoveEventRecord.getEventCommon().getUserID()          );
        if( variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW021_ID ) == 0 ) {
            fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE);
        }
        else if( variableStrCmp( fhdrblopehs.getMainpd_id(),"" ) == 0 ) {
            fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_START);
        }
        else
        {
            fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_END);
        }
        if( variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW021_ID ) == 0 ) {
            fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_MOVEBANK);
        }
        else if( variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW019_ID) == 0 ) {
            fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_BANKINCANCEL);
        }
        else
        {
            fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_BANKIN );
        }
        if( variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW021_ID ) == 0 ) {
            fhdrblopehs.setStage_id("" );
            fhdrblopehs.setStagegrp_id("" );
            fhdrblopehs.setLocation_id(locationID.getValue() );
            fhdrblopehs.setArea_id(areaID.getValue() );
        }
        else
        {
            fhdrblopehs.setStage_id(resultData_pos.getStageID() );
            fhdrblopehs.setStagegrp_id(stageGrpID.getValue() );
            fhdrblopehs.setLocation_id("");
            fhdrblopehs.setArea_id("");
        }
        fhdrblopehs.setPhoto_layer("");
        fhdrblopehs.setEqp_id("");
        fhdrblopehs.setEqp_name("");
        fhdrblopehs.setOpe_mode("");
        fhdrblopehs.setLc_recipe_id("");
        fhdrblopehs.setRecipe_id("");
        fhdrblopehs.setPh_recipe_id("");
        fhdrblopehs.setRparm_count(0L   );
        fhdrblopehs.setBank_id(DurableBankMoveEventRecord.getDurableData().getBankID() );
        if(   variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW021_ID ) == 0
                || variableStrCmp( DurableBankMoveEventRecord.getEventCommon().getTransactionID(), ODRBW019_ID ) == 0 ) {
            fhdrblopehs.setPrev_bank_id(DurableBankMoveEventRecord.getPreviousBankID() );
        }
        else
        {
            fhdrblopehs.setPrev_bank_id("");
        }
        fhdrblopehs.setPrev_mainpd_id("" );
        fhdrblopehs.setPrev_ope_no("" );
        fhdrblopehs.setPrev_pd_id("" );
        fhdrblopehs.setPrev_pd_name("" );
        fhdrblopehs.setPrev_pass_count(0L);
        fhdrblopehs.setPrev_stage_id("" );
        fhdrblopehs.setPrev_stagegrp_id("" );
        fhdrblopehs.setPrev_photo_layer("" );
        fhdrblopehs.setDctrl_job("" );
        fhdrblopehs.setDrbl_owner_id("" );
        fhdrblopehs.setPlan_end_time("1901-01-01-00.00.00.000000");
        fhdrblopehs.setCriteria_flag(convertB(CRITERIA_NA ));
        fhdrblopehs.setClaim_memo(DurableBankMoveEventRecord.getEventCommon().getEventMemo() );
        fhdrblopehs.setStore_time("" );
        fhdrblopehs.setRparm_change_type("" );
        fhdrblopehs.setEvent_create_time(DurableBankMoveEventRecord.getEventCommon().getEventCreationTimeStamp());
        fhdrblopehs.setOriginal_fab_id("" );
        fhdrblopehs.setDestination_fab_id("" );
        fhdrblopehs.setHold_state(DurableBankMoveEventRecord.getDurableData().getHoldState() );
        fhdrblopehs.setInit_hold_flag(0 );
        fhdrblopehs.setHold_time("1901-01-01-00.00.00.000000" );
        fhdrblopehs.setHold_shop_date(0D);
        fhdrblopehs.setHold_user_id("" );
        fhdrblopehs.setHold_type("" );
        fhdrblopehs.setHold_reason_code("" );
        fhdrblopehs.setHold_reason_desc("" );
        fhdrblopehs.setReason_code("" );
        fhdrblopehs.setReason_description("" );
        fhdrblopehs.setRework_count(0);
        fhdrblopehs.setHold_ope_no("" );
        fhdrblopehs.setHold_reason_ope_no("" );
        iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::credrblbankmoveeventrecord(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured");
            log.info("HistoryWatchDogServer::credrblbankmoveeventrecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateDurableBankMoveEventRecord Function");
        return( returnOK() );
    }

}
