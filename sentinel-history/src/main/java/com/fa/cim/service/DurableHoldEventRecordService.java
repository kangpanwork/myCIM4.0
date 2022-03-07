package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/7/15 10:34
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableHoldEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableControlJobStatusChangeHistoryService durableControlJobStatusChangeHistoryService;

    @Autowired
    private DurableOperationStartHistoryService durableOperationStartHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createDurableHoldEventRecord( Infos.DurableHoldEventRecord DurableHoldEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
        Infos.Ohdrblopehs fhdrblopehs= new Infos.Ohdrblopehs();
        Infos.Frpd        resultData_pd_cur= new Infos.Frpd();
        Infos.Frpd        resultData_pd_prev= new Infos.Frpd();
        Infos.Frpos       resultData_pos_cur= new Infos.Frpos();
        Infos.Frpos       resultData_pos_prev= new Infos.Frpos();
        Params.String    curStageGrpID   = new Params.String();
        Params.String    prevStageGrpID  = new Params.String();
        Params.String    codeCategory_hold = new Params.String();
        Params.String    codeCategory_release = new Params.String();
        Timestamp shopDate = new Timestamp(0);
        int     i=0;
        Response iRc = returnOK();
        resultData_pd_cur = new Infos.Frpd();
        resultData_pd_prev = new Infos.Frpd();
        resultData_pos_cur = new Infos.Frpos();
        resultData_pos_prev = new Infos.Frpos();
        iRc = tableMethod.getFRPD( DurableHoldEventRecord.getPreviousOperationID(),resultData_pd_prev );
        if(!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPD( DurableHoldEventRecord.getDurableData().getOperationID(),resultData_pd_cur );
        if(!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurableHoldEventRecord.getPreviousObjrefPOS(),                    DurableHoldEventRecord.getPreviousOperationNumber(),                    DurableHoldEventRecord.getPreviousObjrefMainPF(),                    resultData_pos_prev);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID(), prevStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( DurableHoldEventRecord.getDurableData().getObjrefPOS(),
                DurableHoldEventRecord.getDurableData().getOperationNumber(),
                DurableHoldEventRecord.getDurableData().getObjrefMainPF(),
                resultData_pos_cur );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_cur.getStageID(), curStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCALENDAR( DurableHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp(), shopDate);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCODE( DurableHoldEventRecord.getReleaseReasonCodeID(), codeCategory_release );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
            return( iRc );
        }
        for( i = 0; i < length(DurableHoldEventRecord.getHoldRecords()); i++ ) {
            iRc = tableMethod.getFRCODE(  DurableHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID(),codeCategory_hold);
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
                return( iRc );
            }
            fhdrblopehs = new Infos.Ohdrblopehs();
            fhdrblopehs.setDurable_id(DurableHoldEventRecord.getDurableData().getDurableID() );
            fhdrblopehs.setDrbl_category(DurableHoldEventRecord.getDurableData().getDurableCategory() );
            if(   (Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag(), FALSE)
                    && Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), TRUE))
                    ||(Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag(), TRUE)
                    && Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), FALSE)) ) {
                fhdrblopehs.setMainpd_id(DurableHoldEventRecord.getPreviousRouteID()            );
                fhdrblopehs.setOpe_no(DurableHoldEventRecord.getPreviousOperationNumber()    );
                fhdrblopehs.setPd_id(DurableHoldEventRecord.getPreviousOperationID()        );
                fhdrblopehs.setOpe_pass_count(DurableHoldEventRecord.getPreviousOperationPassCount());
                fhdrblopehs.setPd_name(resultData_pd_prev.getOperationName()                   );
                fhdrblopehs.setStage_id(resultData_pos_prev.getStageID()                        );
                fhdrblopehs.setStagegrp_id(prevStageGrpID.getValue());
            }
            else
            {
                fhdrblopehs.setMainpd_id(DurableHoldEventRecord.getDurableData().getRouteID()           );
                fhdrblopehs.setOpe_no(DurableHoldEventRecord.getDurableData().getOperationNumber()   );
                fhdrblopehs.setPd_id(DurableHoldEventRecord.getDurableData().getOperationID()       );
                fhdrblopehs.setOpe_pass_count(DurableHoldEventRecord.getDurableData().getOperationPassCount());
                fhdrblopehs.setPd_name(resultData_pd_cur.getOperationName()                       );
                fhdrblopehs.setStage_id(resultData_pos_cur.getStageID()                            );
                fhdrblopehs.setStagegrp_id(curStageGrpID.getValue());
            }
            fhdrblopehs.setClaim_time(DurableHoldEventRecord.getEventCommon().getEventTimeStamp()    );
            fhdrblopehs.setClaim_shop_date(DurableHoldEventRecord.getEventCommon().getEventShopDate()     );
            fhdrblopehs.setClaim_user_id(DurableHoldEventRecord.getEventCommon().getUserID()            );
            if( variableStrCmp( DurableHoldEventRecord.getEventCommon().getTransactionID(), ODRBW036_ID ) == 0 ) {
                if(Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), FALSE)) {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
                }
                else if( variableStrCmp( resultData_pos_prev.getStageID(), resultData_pos_cur.getStageID() ) != 0 ) {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE );
                }
                else
                {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION );
                }
                fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_DURABLEHOLD );
                fhdrblopehs.setClaim_memo(DurableHoldEventRecord.getHoldRecords().get(i).getHoldClaimMemo() );
                fhdrblopehs.setHold_time(DurableHoldEventRecord.getEventCommon().getEventTimeStamp());
                fhdrblopehs.setInit_hold_flag(convertI(DurableHoldEventRecord.getHoldRecords().get(i).getChangeStateFlag()));
                fhdrblopehs.setHold_shop_date(DurableHoldEventRecord.getEventCommon().getEventShopDate());
                fhdrblopehs.setReason_code(DurableHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier() );
                fhdrblopehs.setReason_description(codeCategory_hold.getValue() );
            }
            else
            {
                if(Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getMovementFlag(), FALSE)) {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
                }
                else if( variableStrCmp( resultData_pos_prev.getStageID(), resultData_pos_cur.getStageID() ) != 0 ) {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDSTAGE );
                }
                else
                {
                    fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEFORWARDOPERATION );
                }
                fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE       );
                fhdrblopehs.setClaim_memo(DurableHoldEventRecord.getEventCommon().getEventMemo() );
                fhdrblopehs.setHold_time(DurableHoldEventRecord.getHoldRecords().get(i).getHoldTimeStamp() );
                fhdrblopehs.setInit_hold_flag(0);
                fhdrblopehs.setHold_shop_date(convertD(shopDate.getTime()));
                fhdrblopehs.setReason_code(DurableHoldEventRecord.getReleaseReasonCodeID().getIdentifier() );
                fhdrblopehs.setReason_description(codeCategory_release.getValue() );
            }
            fhdrblopehs.setPhoto_layer("" );
            fhdrblopehs.setLocation_id("" );
            fhdrblopehs.setArea_id("" );
            fhdrblopehs.setEqp_id("" );
            fhdrblopehs.setEqp_name("" );
            fhdrblopehs.setOpe_mode("" );
            fhdrblopehs.setLc_recipe_id("" );
            fhdrblopehs.setRecipe_id("" );
            fhdrblopehs.setPh_recipe_id("" );
            fhdrblopehs.setRparm_count(0L);
            fhdrblopehs.setBank_id("" );
            fhdrblopehs.setPrev_bank_id("" );
            if(Objects.equals(DurableHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationExistFlag(), TRUE)) {
                fhdrblopehs.setPrev_mainpd_id(DurableHoldEventRecord.getPreviousRouteID()         );
                fhdrblopehs.setPrev_ope_no(DurableHoldEventRecord.getPreviousOperationNumber() );
                fhdrblopehs.setPrev_pd_id(DurableHoldEventRecord.getPreviousOperationID()     );
                fhdrblopehs.setPrev_pd_name(resultData_pd_prev.getOperationName()                );
                fhdrblopehs.setPrev_pass_count(DurableHoldEventRecord.getPreviousOperationPassCount());
                fhdrblopehs.setPrev_stage_id(resultData_pos_prev.getStageID()                     );
                fhdrblopehs.setPrev_stagegrp_id(prevStageGrpID.getValue()                                  );
            }
            else
            {
                fhdrblopehs.setPrev_mainpd_id(DurableHoldEventRecord.getDurableData().getRouteID()        );
                fhdrblopehs.setPrev_ope_no(DurableHoldEventRecord.getDurableData().getOperationNumber());
                fhdrblopehs.setPrev_pd_id(DurableHoldEventRecord.getDurableData().getOperationID()    );
                fhdrblopehs.setPrev_pd_name(resultData_pd_cur.getOperationName() );
                fhdrblopehs.setPrev_pass_count(0L);
                fhdrblopehs.setPrev_stage_id(resultData_pos_cur.getStageID() );
                fhdrblopehs.setPrev_stagegrp_id(curStageGrpID.getValue() );
            }
            fhdrblopehs.setPrev_photo_layer("" );
            fhdrblopehs.setDctrl_job("" );
            fhdrblopehs.setPlan_end_time("1901-01-01-00.00.00.000000" );
            fhdrblopehs.setCriteria_flag(convertB(CRITERIA_NA));
            fhdrblopehs.setStore_time("" );
            fhdrblopehs.setRparm_change_type("");
            fhdrblopehs.setEvent_create_time(DurableHoldEventRecord.getEventCommon().getEventCreationTimeStamp() );
            fhdrblopehs.setOriginal_fab_id("" );
            fhdrblopehs.setDestination_fab_id("" );
            fhdrblopehs.setHold_state(DurableHoldEventRecord.getDurableData().getHoldState() );
            fhdrblopehs.setHold_user_id(DurableHoldEventRecord.getHoldRecords().get(i).getHoldUserID());
            fhdrblopehs.setHold_type(DurableHoldEventRecord.getHoldRecords().get(i).getHoldType()  );
            fhdrblopehs.setHold_reason_code(DurableHoldEventRecord.getHoldRecords().get(i).getHoldReasonCodeID().getIdentifier() );
            fhdrblopehs.setHold_reason_desc(codeCategory_hold.getValue() );
            fhdrblopehs.setRework_count(0);
            fhdrblopehs.setHold_ope_no(DurableHoldEventRecord.getDurableData().getOperationNumber() );
            fhdrblopehs.setHold_reason_ope_no(DurableHoldEventRecord.getHoldRecords().get(i).getResponsibleOperationNumber() );
            iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::credrblholdeventrecord(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured");
                log.info("HistoryWatchDogServer::credrblholdeventrecord Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateDurableHoldEventRecord Function");
        return( returnOK() );
    }

}
