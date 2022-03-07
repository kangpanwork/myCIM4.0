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
import static com.fa.cim.Constant.TransactionConstant.ODRBW029_ID;
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
public class DurableOperationCompleteEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableOperationCompleteHistoryService durableOperationCompleteHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurableOperationCompleteEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/12 10:31
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createDurableOperationCompleteEventRecord( Infos.DurableOperationCompleteEventRecord DurableOperationCompleteEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
        Infos.Ohdrblopehs         fhdrblopehs_record= new Infos.Ohdrblopehs();
        Infos.Ohdrblopehs_rparm   fhdrblopehs_rparm_record= new Infos.Ohdrblopehs_rparm();
        Infos.Frpd                resultData_pd= new Infos.Frpd();
        Infos.Frpd                resultData_pd_prev= new Infos.Frpd();
        Infos.Frpos               resultData_pos= new Infos.Frpos();
        Infos.Frpos               resultData_pos_prev= new Infos.Frpos();
        Response iRc = returnOK();
        Params.String                       stageGrpID     = new Params.String();
        Params.String                       areaID         = new Params.String();
        Params.String                       eqpName        = new Params.String();
        Params.String                       locationID     = new Params.String();
        Params.String                       prevStageGrpID = new Params.String();
        fhdrblopehs_record = new Infos.Ohdrblopehs();
        fhdrblopehs_rparm_record = new Infos.Ohdrblopehs_rparm();
        resultData_pd = new Infos.Frpd();
        resultData_pd_prev = new Infos.Frpd();
        resultData_pos = new Infos.Frpos();
        resultData_pos_prev = new Infos.Frpos();
        iRc = tableMethod.getFRPD( DurableOperationCompleteEventRecord.getDurableData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPD( DurableOperationCompleteEventRecord.getPreviousOperationID(),resultData_pd_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurableOperationCompleteEventRecord.getDurableData().getObjrefPOS() ,                    DurableOperationCompleteEventRecord.getDurableData().getOperationNumber(),                    DurableOperationCompleteEventRecord.getDurableData().getObjrefMainPF(),                    resultData_pos ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurableOperationCompleteEventRecord.getPreviousObjrefPOS(),                    DurableOperationCompleteEventRecord.getPreviousOperationNumber(),                    DurableOperationCompleteEventRecord.getPreviousObjrefMainPF(),                    resultData_pos_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID() , prevStageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFREQP( DurableOperationCompleteEventRecord.getEquipmentID() , areaID , eqpName  );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        if( variableStrCmp ( resultData_pos.getStageID() , resultData_pos_prev.getStageID() ) != 0 ) {
        }
        else
        {
        }
        fhdrblopehs_record.setDurable_id(DurableOperationCompleteEventRecord.getDurableData().getDurableID()               );
        fhdrblopehs_record.setDrbl_category(DurableOperationCompleteEventRecord.getDurableData().getDurableCategory()         );
        fhdrblopehs_record.setMainpd_id(DurableOperationCompleteEventRecord.getDurableData().getRouteID()                 );
        fhdrblopehs_record.setOpe_no(DurableOperationCompleteEventRecord.getDurableData().getOperationNumber()         );
        fhdrblopehs_record.setPd_id(DurableOperationCompleteEventRecord.getDurableData().getOperationID()             );
        fhdrblopehs_record.setOpe_pass_count(DurableOperationCompleteEventRecord.getDurableData().getOperationPassCount());
        fhdrblopehs_record.setPd_name(resultData_pd.getOperationName()                                              );
        fhdrblopehs_record.setClaim_time(DurableOperationCompleteEventRecord.getEventCommon().getEventTimeStamp()          );
        fhdrblopehs_record.setClaim_shop_date(DurableOperationCompleteEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs_record.setClaim_user_id(DurableOperationCompleteEventRecord.getEventCommon().getUserID()                  );
        if( variableStrCmp ( resultData_pos.getStageID() , resultData_pos_prev.getStageID() ) != 0 ) {
            fhdrblopehs_record.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDSTAGE                                     );
        }
        else
        {
            fhdrblopehs_record.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDOPERATION                                 );
        }
        fhdrblopehs_record.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE                                   );
        fhdrblopehs_record.setStage_id(resultData_pos.getStageID()                                                   );
        fhdrblopehs_record.setStagegrp_id(stageGrpID.getValue());
        fhdrblopehs_record.setPhoto_layer(""                                                                       );
        fhdrblopehs_record.setLocation_id(locationID.getValue());
        fhdrblopehs_record.setArea_id(areaID.getValue());
        fhdrblopehs_record.setEqp_id(DurableOperationCompleteEventRecord.getEquipmentID()                         );
        fhdrblopehs_record.setEqp_name(eqpName .getValue());
        fhdrblopehs_record.setOpe_mode(DurableOperationCompleteEventRecord.getOperationMode()                       );
        fhdrblopehs_record.setLc_recipe_id(DurableOperationCompleteEventRecord.getLogicalRecipeID()                     );
        fhdrblopehs_record.setRecipe_id(DurableOperationCompleteEventRecord.getMachineRecipeID()                     );
        fhdrblopehs_record.setPh_recipe_id(DurableOperationCompleteEventRecord.getPhysicalRecipeID()                    );
        fhdrblopehs_record.setRparm_count(convertL(length(DurableOperationCompleteEventRecord.getRecipeParameters())));
        fhdrblopehs_record.setBank_id(""                                                                       );
        fhdrblopehs_record.setPrev_bank_id(""                                                                       );
        fhdrblopehs_record.setPrev_mainpd_id(DurableOperationCompleteEventRecord.getPreviousRouteID()                     );
        fhdrblopehs_record.setPrev_ope_no(DurableOperationCompleteEventRecord.getPreviousOperationNumber()             );
        fhdrblopehs_record.setPrev_pd_id(DurableOperationCompleteEventRecord.getPreviousOperationID()                 );
        fhdrblopehs_record.setPrev_pd_name(resultData_pd_prev.getOperationName()                                         );
        fhdrblopehs_record.setPrev_pass_count(DurableOperationCompleteEventRecord.getPreviousOperationPassCount());
        fhdrblopehs_record.setPrev_stage_id(resultData_pos_prev.getStageID()                                              );
        fhdrblopehs_record.setPrev_stagegrp_id(prevStageGrpID.getValue());
        fhdrblopehs_record.setPrev_photo_layer(""                                                                       );
        fhdrblopehs_record.setDctrl_job(DurableOperationCompleteEventRecord.getDurableControlJobID()                 );
        fhdrblopehs_record.setDrbl_owner_id(""                                                                       );
        fhdrblopehs_record.setPlan_end_time("1901-01-01-00.00.00.000000");
        fhdrblopehs_record.setCriteria_flag(convertB(CRITERIA_NA));
        fhdrblopehs_record.setClaim_memo(DurableOperationCompleteEventRecord.getEventCommon().getEventMemo()               );
        fhdrblopehs_record.setRparm_change_type(""                                                                       );
        fhdrblopehs_record.setEvent_create_time(DurableOperationCompleteEventRecord.getEventCommon().getEventCreationTimeStamp()  );
        fhdrblopehs_record.setOriginal_fab_id(""                                                                       );
        fhdrblopehs_record.setDestination_fab_id(""                                                                       );
        fhdrblopehs_record.setHold_state(DurableOperationCompleteEventRecord.getDurableData().getHoldState()               );
        fhdrblopehs_record.setInit_hold_flag(0);
        fhdrblopehs_record.setHold_time("1901-01-01-00.00.00.000000");
        fhdrblopehs_record.setHold_shop_date(0.0);
        fhdrblopehs_record.setHold_user_id(""                                                                       );
        fhdrblopehs_record.setHold_type(""                                                                       );
        fhdrblopehs_record.setHold_reason_code(""                                                                       );
        fhdrblopehs_record.setHold_reason_desc(""                                                                       );
        fhdrblopehs_record.setReason_code(""                                                                       );
        fhdrblopehs_record.setReason_description(""                                                                       );
        fhdrblopehs_record.setRework_count(0);
        fhdrblopehs_record.setHold_ope_no(""                                                                       );
        fhdrblopehs_record.setHold_reason_ope_no(""                                                                       );
        iRc = durableOperationCompleteHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs_record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
            return( iRc );
        }
        int paramLen = length(DurableOperationCompleteEventRecord.getRecipeParameters());
        if ( paramLen>0 ) {
            for( int i = 0; i < paramLen; i++ ) {
                fhdrblopehs_rparm_record = new Infos.Ohdrblopehs_rparm();
                fhdrblopehs_rparm_record.setDurable_id(DurableOperationCompleteEventRecord.getDurableData().getDurableID()                 );
                fhdrblopehs_rparm_record.setDrbl_category(DurableOperationCompleteEventRecord.getDurableData().getDurableCategory()            );
                fhdrblopehs_rparm_record.setMainpd_id(DurableOperationCompleteEventRecord.getDurableData().getRouteID()                    );
                fhdrblopehs_rparm_record.setOpe_no(DurableOperationCompleteEventRecord.getDurableData().getOperationNumber()            );
                fhdrblopehs_rparm_record.setOpe_pass_count(DurableOperationCompleteEventRecord.getDurableData().getOperationPassCount());
                fhdrblopehs_rparm_record.setClaim_time(DurableOperationCompleteEventRecord.getEventCommon().getEventTimeStamp()             );
                fhdrblopehs_rparm_record.setOpe_category(SP_OPERATIONCATEGORY_OPERATIONCOMPLETE                                      );
                fhdrblopehs_rparm_record.setRparm_name(DurableOperationCompleteEventRecord.getRecipeParameters().get(i).getParameterName()      );
                fhdrblopehs_rparm_record.setRparm_value(DurableOperationCompleteEventRecord.getRecipeParameters().get(i).getParameterValue()     );
                iRc = durableOperationCompleteHistoryService.insertDRBLHistory_FHDRBLOPEHS_RPARM( fhdrblopehs_rparm_record );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord(): InsertDRBLHistory_FHDRBLOPEHS_RPARM SQL Error Occured");
                    log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
                    return( iRc );
                }
            }
        }
        log.info("HistoryWatchDogServer::CreateDurableOperationCompleteEventRecord Function");
        return( returnOK() );
    }

}
