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
import static com.fa.cim.Constant.TransactionConstant.*;
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
public class DurablePFXEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableOperationCompleteHistoryService durableOperationCompleteHistoryService;

    @Autowired
    private DurableOperationStartHistoryService durableOperationStartHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurablePFXEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/12 10:48
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createDurablePFXEventRecord( Infos.DurablePFXEventRecord DurablePFXEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
        Infos.Ohdrblopehs         fhdrblopehs_record= new Infos.Ohdrblopehs();
        Infos.Frpd                resultData_pd= new Infos.Frpd();
        Infos.Frpos               resultData_pos= new Infos.Frpos();
        Response iRc = returnOK();
        Params.String                       stageGrpID   = new Params.String();
        Params.String                       areaID       = new Params.String();
        Params.String                       eqpName      = new Params.String();
        Params.String                       locationID   = new Params.String();
        fhdrblopehs_record = new Infos.Ohdrblopehs();
        resultData_pd = new Infos.Frpd();
        resultData_pos = new Infos.Frpos();
        iRc = tableMethod.getFRPD( DurablePFXEventRecord.getDurableData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurablePFXEventRecord.getDurableData().getObjrefPOS() ,                    DurablePFXEventRecord.getDurableData().getOperationNumber(),                    DurablePFXEventRecord.getDurableData().getObjrefMainPF(),                    resultData_pos ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
            return( iRc );
        }
        if( variableStrCmp( DurablePFXEventRecord.getEventCommon().getTransactionID(), ODRBW030_ID ) == 0 ) {
        }
        else
        {
        }
        fhdrblopehs_record.setDurable_id(DurablePFXEventRecord.getDurableData().getDurableID()               );
        fhdrblopehs_record.setDrbl_category(DurablePFXEventRecord.getDurableData().getDurableCategory()         );
        fhdrblopehs_record.setMainpd_id(DurablePFXEventRecord.getDurableData().getRouteID()                 );
        fhdrblopehs_record.setOpe_no(DurablePFXEventRecord.getDurableData().getOperationNumber()         );
        fhdrblopehs_record.setPd_id(DurablePFXEventRecord.getDurableData().getOperationID()             );
        fhdrblopehs_record.setOpe_pass_count(DurablePFXEventRecord.getDurableData().getOperationPassCount());
        fhdrblopehs_record.setPd_name(resultData_pd.getOperationName()                                );
        fhdrblopehs_record.setClaim_time(DurablePFXEventRecord.getEventCommon().getEventTimeStamp()          );
        fhdrblopehs_record.setClaim_shop_date(DurablePFXEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs_record.setClaim_user_id(DurablePFXEventRecord.getEventCommon().getUserID()                  );
        fhdrblopehs_record.setMove_type(SP_MOVEMENTTYPE_NONMOVE                                    );
        if( variableStrCmp( DurablePFXEventRecord.getEventCommon().getTransactionID(), ODRBW030_ID ) == 0 ) {
            fhdrblopehs_record.setOpe_category(SP_OPERATIONCATEGORY_DURABLESTART                                   ) ;
        }
        else
        {
            fhdrblopehs_record.setOpe_category(SP_OPERATIONCATEGORY_DURABLESTARTCANCEL                             ) ;
        }
        fhdrblopehs_record.setStage_id(resultData_pos.getStageID()                                     );
        fhdrblopehs_record.setStagegrp_id(stageGrpID.getValue()                                                 );
        fhdrblopehs_record.setPhoto_layer(""                                                         );
        fhdrblopehs_record.setLocation_id(""                                                         );
        fhdrblopehs_record.setArea_id(""                                                         );
        fhdrblopehs_record.setEqp_id(""                                                         );
        fhdrblopehs_record.setEqp_name(""                                                         );
        fhdrblopehs_record.setOpe_mode(""                                                         );
        fhdrblopehs_record.setLc_recipe_id(""                                                         );
        fhdrblopehs_record.setRecipe_id(""                                                         );
        fhdrblopehs_record.setPh_recipe_id(""                                                         );
        fhdrblopehs_record.setRparm_count(0L);
        fhdrblopehs_record.setBank_id(DurablePFXEventRecord.getDurableData().getBankID()                  );
        fhdrblopehs_record.setPrev_bank_id(""                                                         );
        fhdrblopehs_record.setPrev_mainpd_id(""                                                         );
        fhdrblopehs_record.setPrev_ope_no(""                                                         );
        fhdrblopehs_record.setPrev_pd_id(""                                                         );
        fhdrblopehs_record.setPrev_pd_name(""                                                         );
        fhdrblopehs_record.setPrev_pass_count(0L);
        fhdrblopehs_record.setPrev_stage_id(""                                                         );
        fhdrblopehs_record.setPrev_stagegrp_id(""                                                         );
        fhdrblopehs_record.setPrev_photo_layer(""                                                         );
        fhdrblopehs_record.setDctrl_job(""                                                         );
        fhdrblopehs_record.setDrbl_owner_id(""                                                         );
        fhdrblopehs_record.setPlan_end_time("1901-01-01-00.00.00.000000");
        fhdrblopehs_record.setCriteria_flag(convertB(CRITERIA_NA));
        fhdrblopehs_record.setClaim_memo(DurablePFXEventRecord.getEventCommon().getEventMemo()               );
        fhdrblopehs_record.setRparm_change_type(""                                                         );
        fhdrblopehs_record.setEvent_create_time(DurablePFXEventRecord.getEventCommon().getEventCreationTimeStamp()  );
        fhdrblopehs_record.setOriginal_fab_id(""                                                         );
        fhdrblopehs_record.setDestination_fab_id(""                                                         );
        fhdrblopehs_record.setHold_state(DurablePFXEventRecord.getDurableData().getHoldState()               );
        fhdrblopehs_record.setInit_hold_flag(0);
        fhdrblopehs_record.setHold_time("1901-01-01-00.00.00.000000");
        fhdrblopehs_record.setHold_shop_date(0D);
        fhdrblopehs_record.setHold_user_id(""                                                         );
        fhdrblopehs_record.setHold_type(""                                                         );
        fhdrblopehs_record.setHold_reason_code(""                                                         );
        fhdrblopehs_record.setHold_reason_desc(""                                                         );
        fhdrblopehs_record.setReason_code(""                                                         );
        fhdrblopehs_record.setReason_description(""                                                         );
        fhdrblopehs_record.setRework_count(0);
        fhdrblopehs_record.setHold_ope_no(""                                                         );
        fhdrblopehs_record.setHold_reason_ope_no(""                                                         );
        iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs_record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateDurablePFXEventRecord Function");
        return( returnOK() );
    }

}
