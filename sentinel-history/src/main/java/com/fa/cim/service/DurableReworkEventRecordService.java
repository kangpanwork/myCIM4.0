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
import static com.fa.cim.Constant.TransactionConstant.ODRBW037_ID;
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
 * @date 2019/7/18 18:00
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableReworkEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableReworkHistoryService durableReworkHistoryService;

    @Autowired
    private DurableOperationStartHistoryService durableOperationStartHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurableReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/18 18:10
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createDurableReworkEventRecord( Infos.DurableReworkEventRecord DurableReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::posDurableReworkEventRecord Function");
        iRc = createFHDRBLOPEHS_PreviousOperationForRwk( DurableReworkEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::posDurableReworkEventRecord Function");
            return ( iRc );
        }
        iRc = createFHDRBLOPEHS_CurrentOperationForRwk( DurableReworkEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::posDurableReworkEventRecord Function");
            return ( iRc );
        }
        log.info("HistoryWatchDogServer::posDurableReworkEventRecord Function");
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurableReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/18 18:11
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFHDRBLOPEHS_PreviousOperationForRwk( Infos.DurableReworkEventRecord DurableReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohdrblopehs    fhdrblopehs= new Infos.Ohdrblopehs();
        Infos.Frpd           resultData_pd_old= new Infos.Frpd();
        Infos.Frpd           resultData_pd_prev= new Infos.Frpd();
        Infos.Frpos          resultData_pos_old = new Infos.Frpos();
        Infos.Frpos          resultData_pos_prev = new Infos.Frpos();
        Params.String                  oldStageGrpID  = new Params.String();
        Params.String                  prevStageGrpID = new Params.String();
        Params.String                  codeDescription = new Params.String();
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
        resultData_pd_old  = new Infos.Frpd();
        resultData_pd_prev  = new Infos.Frpd();
        resultData_pos_old  = new Infos.Frpos();
        resultData_pos_prev  = new Infos.Frpos();
        iRc = tableMethod.getFRPOS( DurableReworkEventRecord.getOldCurrentDurablePOData().getObjrefPOS(),
                DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationNumber(),
                DurableReworkEventRecord.getOldCurrentDurablePOData().getObjrefMainPF(),
                resultData_pos_old );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        resultData_pos_old.setOperationNO(DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationNumber() );
        resultData_pos_old.setPdID(DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationID() );
        iRc = tableMethod.getFRPOS( DurableReworkEventRecord.getPreviousObjrefPOS(),
                DurableReworkEventRecord.getPreviousOperationNumber(),
                DurableReworkEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        resultData_pos_prev.setOperationNO(DurableReworkEventRecord.getPreviousOperationNumber() );
        resultData_pos_prev.setPdID(DurableReworkEventRecord.getPreviousOperationID()     );
        iRc = tableMethod.getFRPD( resultData_pos_prev.getPdID(), resultData_pd_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_old.getPdID(), resultData_pd_old );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID(), prevStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_old.getStageID(), oldStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCODE( DurableReworkEventRecord.getReasonCodeID(), codeDescription );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
            return( iRc );
        }
        fhdrblopehs  = new Infos.Ohdrblopehs();
        fhdrblopehs.setDurable_id(DurableReworkEventRecord.getDurableData().getDurableID()        );
        fhdrblopehs.setDrbl_category(DurableReworkEventRecord.getDurableData().getDurableCategory()  );
        fhdrblopehs.setMainpd_id(DurableReworkEventRecord.getPreviousRouteID()              );
        fhdrblopehs.setOpe_no(DurableReworkEventRecord.getPreviousOperationNumber()      );
        fhdrblopehs.setPd_id(DurableReworkEventRecord.getPreviousOperationID()          );
        fhdrblopehs.setPd_name(resultData_pd_prev.getOperationName()                       );
        fhdrblopehs.setOpe_pass_count(DurableReworkEventRecord.getPreviousOperationPassCount());
        fhdrblopehs.setClaim_time(DurableReworkEventRecord.getEventCommon().getEventTimeStamp()   );
        fhdrblopehs.setClaim_shop_date(DurableReworkEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs.setClaim_user_id(DurableReworkEventRecord.getEventCommon().getUserID()           );
        if( variableStrCmp( resultData_pos_old.getStageID(), resultData_pos_prev.getStageID() ) != 0 ) {
            fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE                      );
        }
        else
        {
            fhdrblopehs.setMove_type(SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION                  );
        }
        if(variableStrCmp(DurableReworkEventRecord.getEventCommon().getTransactionID(),ODRBW037_ID) == 0 ) {
            fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_REWORKCANCEL                      );
        }else{
            fhdrblopehs.setOpe_category(SP_OPERATIONCATEGORY_REWORK                            );
        }
        fhdrblopehs.setStage_id(resultData_pos_prev.getStageID()                            );
        fhdrblopehs.setStagegrp_id(prevStageGrpID.getValue());
        fhdrblopehs.setPhoto_layer(""                                                     );
        fhdrblopehs.setLocation_id(""                                                     );
        fhdrblopehs.setArea_id(""                                                     );
        fhdrblopehs.setEqp_id(""                                                     );
        fhdrblopehs.setEqp_name(""                                                     );
        fhdrblopehs.setOpe_mode(""                                                     );
        fhdrblopehs.setLc_recipe_id(""                                                     );
        fhdrblopehs.setRecipe_id(""                                                     );
        fhdrblopehs.setPh_recipe_id(""                                                     );
        fhdrblopehs.setRparm_count(0L);
        fhdrblopehs.setBank_id(""                                                     );
        fhdrblopehs.setPrev_bank_id(""                                                     );
        fhdrblopehs.setPrev_mainpd_id(DurableReworkEventRecord.getOldCurrentDurablePOData().getRouteID()         );
        fhdrblopehs.setPrev_ope_no(DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationNumber() );
        fhdrblopehs.setPrev_pd_id(DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationID()     );
        fhdrblopehs.setPrev_pd_name(resultData_pd_old.getOperationName()                        );
        fhdrblopehs.setPrev_pass_count(DurableReworkEventRecord.getOldCurrentDurablePOData().getOperationPassCount());
        fhdrblopehs.setPrev_stage_id(resultData_pos_old.getStageID()                             );
        fhdrblopehs.setPrev_stagegrp_id(oldStageGrpID.getValue());
        fhdrblopehs.setPrev_photo_layer(""                                                     );
        fhdrblopehs.setDctrl_job(""                                                     );
        fhdrblopehs.setDrbl_owner_id(""                                                     );
        fhdrblopehs.setPlan_end_time("1901-01-01-00.00.00.000000");
        fhdrblopehs.setCriteria_flag(convertB(CRITERIA_NA));
        fhdrblopehs.setClaim_memo(DurableReworkEventRecord.getEventCommon().getEventMemo()        );
        fhdrblopehs.setRparm_change_type(""                                                     );
        fhdrblopehs.setEvent_create_time(DurableReworkEventRecord.getEventCommon().getEventCreationTimeStamp()   );
        fhdrblopehs.setOriginal_fab_id(""                                                     );
        fhdrblopehs.setDestination_fab_id(""                                                     );
        fhdrblopehs.setHold_state(DurableReworkEventRecord.getDurableData().getHoldState()        );
        fhdrblopehs.setInit_hold_flag(0);
        fhdrblopehs.setHold_time("1901-01-01-00.00.00.000000");
        fhdrblopehs.setHold_shop_date(0D);
        fhdrblopehs.setHold_user_id(""                                                     );
        fhdrblopehs.setHold_type(""                                                     );
        fhdrblopehs.setHold_reason_code(""                                                     );
        fhdrblopehs.setHold_reason_desc(""                                                     );
        fhdrblopehs.setReason_code(DurableReworkEventRecord.getReasonCodeID().getIdentifier()      );
        fhdrblopehs.setReason_description(codeDescription.getValue());
        fhdrblopehs.setRework_count(convertI(DurableReworkEventRecord.getReworkCount()));
        fhdrblopehs.setHold_ope_no(""                                                     );
        fhdrblopehs.setHold_reason_ope_no(""                                                     );
        iRc = durableReworkHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_TxPassThruReq(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_TxPassThruReq Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function");
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param DurableReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 10:12
     */
    public Response createFHDRBLOPEHS_CurrentOperationForRwk(Infos.DurableReworkEventRecord DurableReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohdrblopehs    fhdrblopehs;
        Infos.Frpd           resultData_pd_cur;
        Infos.Frpd           resultData_pd_prev;
        Infos.Frpos          resultData_pos_cur ;
        Infos.Frpos          resultData_pos_prev ;
        Params.String curStageGrpID = new Params.String();
        Params.String prevStageGrpID = new Params.String();
        Params.String codeDescription = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );




        resultData_pd_cur =new Infos.Frpd();
        resultData_pd_prev =new Infos.Frpd();
        resultData_pos_cur =new Infos.Frpos();
        resultData_pos_prev =new Infos.Frpos();

        iRc = tableMethod.getFRPOS( DurableReworkEventRecord.getDurableData().getObjrefPOS() ,
                DurableReworkEventRecord.getDurableData().getOperationNumber(),
                DurableReworkEventRecord.getDurableData().getObjrefMainPF(),
                resultData_pos_cur ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_TxPassThruReq Function" );
            return( iRc );
        }

        resultData_pos_cur.setOperationNO( DurableReworkEventRecord.getDurableData().getOperationNumber() );
        resultData_pos_cur.setPdID(        DurableReworkEventRecord.getDurableData().getOperationID()     );

        iRc = tableMethod.getFRPOS( DurableReworkEventRecord.getPreviousObjrefPOS(),
                DurableReworkEventRecord.getPreviousOperationNumber(),
                DurableReworkEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }

        resultData_pos_prev.setOperationNO( DurableReworkEventRecord.getPreviousOperationNumber() );
        resultData_pos_prev.setPdID(        DurableReworkEventRecord.getPreviousOperationID()     );

        iRc = tableMethod.getFRPD( resultData_pos_cur.getPdID(), resultData_pd_cur );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_prev.getPdID(), resultData_pd_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_cur.getStageID(), curStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID(), prevStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCODE( DurableReworkEventRecord.getReasonCodeID(), codeDescription );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForRwk Function" );
            return( iRc );
        }




        fhdrblopehs =new Infos.Ohdrblopehs();
        fhdrblopehs.setDurable_id         ( DurableReworkEventRecord.getDurableData().getDurableID()        );
        fhdrblopehs.setDrbl_category      ( DurableReworkEventRecord.getDurableData().getDurableCategory()  );
        fhdrblopehs.setMainpd_id          ( DurableReworkEventRecord.getDurableData().getRouteID()          );
        fhdrblopehs.setOpe_no             ( DurableReworkEventRecord.getDurableData().getOperationNumber()  );
        fhdrblopehs.setPd_id              ( DurableReworkEventRecord.getDurableData().getOperationID()      );
        fhdrblopehs.setPd_name            ( resultData_pd_cur.getOperationName()                        );
        fhdrblopehs.setOpe_pass_count     ( DurableReworkEventRecord.getDurableData().getOperationPassCount());
        fhdrblopehs.setClaim_time         ( DurableReworkEventRecord.getEventCommon().getEventTimeStamp()   );
        fhdrblopehs.setClaim_shop_date    ( DurableReworkEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs.setClaim_user_id      ( DurableReworkEventRecord.getEventCommon().getUserID()           );
        if( variableStrCmp( resultData_pos_cur.getStageID(), resultData_pos_prev.getStageID() ) != 0 ) {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEFORWARDSTAGE                       );
        }
        else
        {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEFORWARDOPERATION                   );
        }
        if(variableStrCmp(DurableReworkEventRecord.getEventCommon().getTransactionID(),ODRBW037_ID) == 0 ) {
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_REWORKCANCEL                      );
        }else{
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_REWORK                            );
        }
        fhdrblopehs.setStage_id           ( resultData_pos_cur.getStageID()                             );
        fhdrblopehs.setStagegrp_id        ( curStageGrpID.getValue()                                          );
        fhdrblopehs.setPhoto_layer        ( ""                                                     );
        fhdrblopehs.setLocation_id        ( ""                                                     );
        fhdrblopehs.setArea_id            ( ""                                                     );
        fhdrblopehs.setEqp_id             ( ""                                                     );
        fhdrblopehs.setEqp_name           ( ""                                                     );
        fhdrblopehs.setOpe_mode           ( ""                                                     );
        fhdrblopehs.setLc_recipe_id       ( ""                                                     );
        fhdrblopehs.setRecipe_id          ( ""                                                     );
        fhdrblopehs.setPh_recipe_id       ( ""                                                     );
        fhdrblopehs.setRparm_count        ( 0L);
        fhdrblopehs.setBank_id            ( ""                                                     );
        fhdrblopehs.setPrev_bank_id       ( ""                                                     );
        fhdrblopehs.setPrev_mainpd_id     ( DurableReworkEventRecord.getPreviousRouteID()              );
        fhdrblopehs.setPrev_ope_no        ( DurableReworkEventRecord.getPreviousOperationNumber()      );
        fhdrblopehs.setPrev_pd_id         ( DurableReworkEventRecord.getPreviousOperationID()          );
        fhdrblopehs.setPrev_pd_name       ( resultData_pd_prev.getOperationName()                       );
        fhdrblopehs.setPrev_pass_count    ( DurableReworkEventRecord.getPreviousOperationPassCount());
        fhdrblopehs.setPrev_stage_id      ( resultData_pos_prev.getStageID()                            );
        fhdrblopehs.setPrev_stagegrp_id   ( prevStageGrpID.getValue()                                         );
        fhdrblopehs.setPrev_photo_layer   ( ""                                                     );
        fhdrblopehs.setDctrl_job          ( ""                                                     );
        fhdrblopehs.setDrbl_owner_id      ( ""                                                     );
        fhdrblopehs.setPlan_end_time      ( "1901-01-01-00.00.00.000000"                           );
        fhdrblopehs.setCriteria_flag      ( convertB(CRITERIA_NA));
        fhdrblopehs.setClaim_memo         ( DurableReworkEventRecord.getEventCommon().getEventMemo()        );
        fhdrblopehs.setRparm_change_type  ( ""                                                     );
        fhdrblopehs.setEvent_create_time  ( DurableReworkEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhdrblopehs.setOriginal_fab_id    ( ""                                                     );
        fhdrblopehs.setDestination_fab_id ( ""                                                     );
        fhdrblopehs.setHold_state         ( DurableReworkEventRecord.getDurableData().getHoldState()        );
        fhdrblopehs.setInit_hold_flag     ( 0);
        fhdrblopehs.setHold_time          ( "1901-01-01-00.00.00.000000"                           );
        fhdrblopehs.setHold_shop_date     ( 0D);
        fhdrblopehs.setHold_user_id       ( ""                                                     );
        fhdrblopehs.setHold_type          ( ""                                                     );
        fhdrblopehs.setHold_reason_code   ( ""                                                     );
        fhdrblopehs.setHold_reason_desc   ( ""                                                     );
        fhdrblopehs.setReason_code        ( DurableReworkEventRecord.getReasonCodeID().getIdentifier()      );
        fhdrblopehs.setReason_description ( codeDescription.getValue()                                        );
        fhdrblopehs.setRework_count       ( DurableReworkEventRecord.getReworkCount()==null?null:
                DurableReworkEventRecord.getReworkCount().intValue());
        fhdrblopehs.setHold_ope_no        ( ""                                                     );
        fhdrblopehs.setHold_reason_ope_no ( ""                                                     );




        iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHDRBLOPEHS_CurrentOperationForRwk(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForRwk Function" );
        return( returnOK() );
    }

}
