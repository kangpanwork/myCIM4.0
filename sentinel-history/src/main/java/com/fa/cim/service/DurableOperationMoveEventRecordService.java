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
import java.util.Objects;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.ODRBW037_ID;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
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
 * @date 2019/7/23 10:32
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableOperationMoveEventRecordService {

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
     * @param durableOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 10:37
     */
    public Response createDurableOperationMoveEventRecord(Infos.DurableOperationMoveEventRecord durableOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreDurableOperationMoveEventRecord Function" );





        if(variableStrCmp(durableOperationMoveEventRecord.getEventCommon().getTransactionID(),ODRBW025_ID) != 0 ) {
            iRc = createFHDRBLOPEHS_PreviousOperationForOpe( durableOperationMoveEventRecord, userDataSets );
            if ( !isOk(iRc)) {
                log.info("HistoryWatchDogServer::CreDurableOperationMoveEventRecord Function" );
                return(iRc);
            }
        }




        iRc = createFHDRBLOPEHS_CurrentOperationForOpe( durableOperationMoveEventRecord, userDataSets );
        if ( !isOk(iRc)) {
            log.info("HistoryWatchDogServer::CreDurableOperationMoveEventRecord Function" );
            return(iRc);
        }

        log.info("HistoryWatchDogServer::CreDurableOperationMoveEventRecord Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 10:38
     */
    public Response createFHDRBLOPEHS_PreviousOperationForOpe(Infos.DurableOperationMoveEventRecord durableOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohdrblopehs    fhdrblopehs;
        Infos.Frpd           resultData_pd_old;
        Infos.Frpd           resultData_pd_prev;
        Infos.Frpos          resultData_pos_old ;
        Infos.Frpos          resultData_pos_prev ;
        Params.String oldStageGrpID = new Params.String();
        Params.String prevStageGrpID = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );




        resultData_pd_old =new Infos.Frpd();
        resultData_pd_prev =new Infos.Frpd();
        resultData_pos_old =new Infos.Frpos();
        resultData_pos_prev =new Infos.Frpos();

        iRc = tableMethod.getFRPOS( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getObjrefPOS(),
                durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationNumber(),
                durableOperationMoveEventRecord.getOldCurrentDurablePOData().getObjrefMainPF(),
                resultData_pos_old );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }

        resultData_pos_old.setOperationNO ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationNumber() );
        resultData_pos_old.setPdID        ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationID() );

        iRc = tableMethod.getFRPOS( durableOperationMoveEventRecord.getPreviousObjrefPOS(),
                durableOperationMoveEventRecord.getPreviousOperationNumber(),
                durableOperationMoveEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }

        resultData_pos_prev.setOperationNO( durableOperationMoveEventRecord.getPreviousOperationNumber() );
        resultData_pos_prev.setPdID(        durableOperationMoveEventRecord.getPreviousOperationID()     );

        iRc = tableMethod.getFRPD( resultData_pos_prev.getPdID(), resultData_pd_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_old.getPdID(), resultData_pd_old );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID(), prevStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_old.getStageID(), oldStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }




        fhdrblopehs =new Infos.Ohdrblopehs();
        fhdrblopehs.setDurable_id         ( durableOperationMoveEventRecord.getDurableData().getDurableID()        );
        fhdrblopehs.setDrbl_category      ( durableOperationMoveEventRecord.getDurableData().getDurableCategory()  );
        fhdrblopehs.setMainpd_id          ( durableOperationMoveEventRecord.getPreviousRouteID()              );
        fhdrblopehs.setOpe_no             ( durableOperationMoveEventRecord.getPreviousOperationNumber()      );
        fhdrblopehs.setPd_id              ( durableOperationMoveEventRecord.getPreviousOperationID()          );
        fhdrblopehs.setPd_name            ( resultData_pd_prev.getOperationName()                              );
        fhdrblopehs.setOpe_pass_count     ( durableOperationMoveEventRecord.getPreviousOperationPassCount());
        fhdrblopehs.setClaim_time         ( durableOperationMoveEventRecord.getEventCommon().getEventTimeStamp()   );
        fhdrblopehs.setClaim_shop_date    ( durableOperationMoveEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs.setClaim_user_id      ( durableOperationMoveEventRecord.getEventCommon().getUserID()           );
        if( variableStrCmp( resultData_pos_old.getStageID(), resultData_pos_prev.getStageID() ) != 0 ) {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE                             );
        }
        else
        {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION                         );
        }
        if(Objects.equals(durableOperationMoveEventRecord.getLocateBackFlag(), TRUE)) {
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_LOCATEBACKWARD                           );
        }else{
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_LOCATEFORWARD                            );
        }
        fhdrblopehs.setStage_id           ( resultData_pos_prev.getStageID()                                   );
        fhdrblopehs.setStagegrp_id        ( prevStageGrpID.getValue()                                                );
        fhdrblopehs.setPhoto_layer        ( ""                                                            );
        fhdrblopehs.setLocation_id        ( ""                                                            );
        fhdrblopehs.setArea_id            ( ""                                                            );
        fhdrblopehs.setEqp_id             ( ""                                                            );
        fhdrblopehs.setEqp_name           ( ""                                                            );
        fhdrblopehs.setOpe_mode           ( ""                                                            );
        fhdrblopehs.setLc_recipe_id       ( ""                                                            );
        fhdrblopehs.setRecipe_id          ( ""                                                            );
        fhdrblopehs.setPh_recipe_id       ( ""                                                            );
        fhdrblopehs.setRparm_count        ( 0L);
        fhdrblopehs.setBank_id            ( ""                                                            );
        fhdrblopehs.setPrev_bank_id       ( ""                                                            );
        fhdrblopehs.setPrev_mainpd_id     ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getRouteID()         );
        fhdrblopehs.setPrev_ope_no        ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationNumber() );
        fhdrblopehs.setPrev_pd_id         ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationID()     );
        fhdrblopehs.setPrev_pd_name       ( resultData_pd_old.getOperationName()                               );
        fhdrblopehs.setPrev_pass_count    ( durableOperationMoveEventRecord.getOldCurrentDurablePOData().getOperationPassCount());
        fhdrblopehs.setPrev_stage_id      ( resultData_pos_old.getStageID()                                    );
        fhdrblopehs.setPrev_stagegrp_id   ( oldStageGrpID.getValue()                                                 );
        fhdrblopehs.setPrev_photo_layer   ( ""                                                            );
        fhdrblopehs.setDctrl_job          ( durableOperationMoveEventRecord.getDurableControlJobID()          );
        fhdrblopehs.setDrbl_owner_id      ( ""                                                            );
        fhdrblopehs.setPlan_end_time      ( "1901-01-01-00.00.00.000000"                                  );
        fhdrblopehs.setCriteria_flag      ( convert(CRITERIA_NA));
        fhdrblopehs.setClaim_memo         ( durableOperationMoveEventRecord.getEventCommon().getEventMemo()        );
        fhdrblopehs.setRparm_change_type  ( ""                                                            );
        fhdrblopehs.setEvent_create_time  ( durableOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp()   );
        fhdrblopehs.setOriginal_fab_id    ( ""                                                            );
        fhdrblopehs.setDestination_fab_id ( ""                                                            );
        fhdrblopehs.setHold_state         ( durableOperationMoveEventRecord.getDurableData().getHoldState()        );
        fhdrblopehs.setInit_hold_flag     ( 0);
        fhdrblopehs.setHold_time          ( "1901-01-01-00.00.00.000000"                                  );
        fhdrblopehs.setHold_shop_date     ( 0D);
        fhdrblopehs.setHold_user_id       ( ""                                                            );
        fhdrblopehs.setHold_type          ( ""                                                            );
        fhdrblopehs.setHold_reason_code   ( ""                                                            );
        fhdrblopehs.setHold_reason_desc   ( ""                                                            );
        fhdrblopehs.setReason_code        ( ""                                                            );
        fhdrblopehs.setReason_description ( ""                                                            );
        fhdrblopehs.setRework_count       ( 0);
        fhdrblopehs.setHold_ope_no        ( ""                                                            );
        fhdrblopehs.setHold_reason_ope_no ( ""                                                            );




        iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHDRBLOPEHS_PreviousOperationForOpe(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_PreviousOperationForOpe Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 10:40
     */
    public Response createFHDRBLOPEHS_CurrentOperationForOpe(Infos.DurableOperationMoveEventRecord durableOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohdrblopehs    fhdrblopehs;
        Infos.Frpd           resultData_pd_cur;
        Infos.Frpd           resultData_pd_prev;
        Infos.Frpos          resultData_pos_cur ;
        Infos.Frpos          resultData_pos_prev ;
        Params.String curStageGrpID = new Params.String();
        Params.String prevStageGrpID = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );




        resultData_pd_cur =new Infos.Frpd();
        resultData_pd_prev =new Infos.Frpd();
        resultData_pos_cur =new Infos.Frpos();
        resultData_pos_prev =new Infos.Frpos();

        iRc = tableMethod.getFRPOS( durableOperationMoveEventRecord.getDurableData().getObjrefPOS() ,
                durableOperationMoveEventRecord.getDurableData().getOperationNumber(),
                durableOperationMoveEventRecord.getDurableData().getObjrefMainPF(),
                resultData_pos_cur ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_TxPassThruReq Function" );
            return( iRc );
        }

        resultData_pos_cur.setOperationNO( durableOperationMoveEventRecord.getDurableData().getOperationNumber() );
        resultData_pos_cur.setPdID(        durableOperationMoveEventRecord.getDurableData().getOperationID()     );

        iRc = tableMethod.getFRPOS( durableOperationMoveEventRecord.getPreviousObjrefPOS(),
                durableOperationMoveEventRecord.getPreviousOperationNumber(),
                durableOperationMoveEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }

        resultData_pos_prev.setOperationNO( durableOperationMoveEventRecord.getPreviousOperationNumber() );
        resultData_pos_prev.setPdID(        durableOperationMoveEventRecord.getPreviousOperationID()     );

        iRc = tableMethod.getFRPD( resultData_pos_cur.getPdID(), resultData_pd_cur );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_prev.getPdID(), resultData_pd_prev );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_cur.getStageID(), curStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID(), prevStageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }




        fhdrblopehs =new Infos.Ohdrblopehs();
        fhdrblopehs.setDurable_id         ( durableOperationMoveEventRecord.getDurableData().getDurableID()        );
        fhdrblopehs.setDrbl_category      ( durableOperationMoveEventRecord.getDurableData().getDurableCategory()  );
        fhdrblopehs.setMainpd_id          ( durableOperationMoveEventRecord.getDurableData().getRouteID()          );
        fhdrblopehs.setOpe_no             ( durableOperationMoveEventRecord.getDurableData().getOperationNumber()  );
        fhdrblopehs.setPd_id              ( durableOperationMoveEventRecord.getDurableData().getOperationID()      );
        fhdrblopehs.setPd_name            ( resultData_pd_cur.getOperationName()                               );
        fhdrblopehs.setOpe_pass_count     ( durableOperationMoveEventRecord.getDurableData().getOperationPassCount());
        fhdrblopehs.setClaim_time         ( durableOperationMoveEventRecord.getEventCommon().getEventTimeStamp()   );
        fhdrblopehs.setClaim_shop_date    ( durableOperationMoveEventRecord.getEventCommon().getEventShopDate());
        fhdrblopehs.setClaim_user_id      ( durableOperationMoveEventRecord.getEventCommon().getUserID()           );
        if( variableStrCmp( resultData_pos_cur.getStageID(), resultData_pos_prev.getStageID() ) != 0 ) {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEFORWARDSTAGE                              );
        }
        else
        {
            fhdrblopehs.setMove_type      ( SP_MOVEMENTTYPE_MOVEFORWARDOPERATION                          );
        }
        if(variableStrCmp(durableOperationMoveEventRecord.getEventCommon().getTransactionID(),ODRBW025_ID) == 0 ) {
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_GATEPASS                                 );
        }
        else if(Objects.equals(durableOperationMoveEventRecord.getLocateBackFlag(), TRUE)) {
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_LOCATEBACKWARD                           );
        }
        else
        {
            fhdrblopehs.setOpe_category   ( SP_OPERATIONCATEGORY_LOCATEFORWARD                            );
        }
        fhdrblopehs.setStage_id           ( resultData_pos_cur.getStageID()                                    );
        fhdrblopehs.setStagegrp_id        ( curStageGrpID.getValue()                                                 );
        fhdrblopehs.setPhoto_layer        ( ""                                                            );
        fhdrblopehs.setLocation_id        ( ""                                                            );
        fhdrblopehs.setArea_id            ( ""                                                            );
        fhdrblopehs.setEqp_id             ( ""                                                            );
        fhdrblopehs.setEqp_name           ( ""                                                            );
        fhdrblopehs.setOpe_mode           ( ""                                                            );
        fhdrblopehs.setLc_recipe_id       ( ""                                                            );
        fhdrblopehs.setRecipe_id          ( ""                                                            );
        fhdrblopehs.setPh_recipe_id       ( ""                                                            );
        fhdrblopehs.setRparm_count        ( 0L);
        fhdrblopehs.setBank_id            ( ""                                                            );
        fhdrblopehs.setPrev_bank_id       ( ""                                                            );
        fhdrblopehs.setPrev_mainpd_id     ( durableOperationMoveEventRecord.getPreviousRouteID()              );
        fhdrblopehs.setPrev_ope_no        ( durableOperationMoveEventRecord.getPreviousOperationNumber()      );
        fhdrblopehs.setPrev_pd_id         ( durableOperationMoveEventRecord.getPreviousOperationID()          );
        fhdrblopehs.setPrev_pd_name       ( resultData_pd_prev.getOperationName()                              );
        fhdrblopehs.setPrev_pass_count    ( durableOperationMoveEventRecord.getPreviousOperationPassCount());
        fhdrblopehs.setPrev_stage_id      ( resultData_pos_prev.getStageID()                                   );
        fhdrblopehs.setPrev_stagegrp_id   ( prevStageGrpID.getValue()                                               );
        fhdrblopehs.setPrev_photo_layer   ( ""                                                            );
        fhdrblopehs.setDctrl_job          ( durableOperationMoveEventRecord.getDurableControlJobID()          );
        fhdrblopehs.setDrbl_owner_id      ( ""                                                            );
        fhdrblopehs.setPlan_end_time      ( "1901-01-01-00.00.00.000000"                                  );
        fhdrblopehs.setCriteria_flag      ( convert(CRITERIA_NA));
        fhdrblopehs.setClaim_memo         ( durableOperationMoveEventRecord.getEventCommon().getEventMemo()        );
        fhdrblopehs.setRparm_change_type  ( ""                                                            );
        fhdrblopehs.setEvent_create_time  ( durableOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhdrblopehs.setOriginal_fab_id    ( ""                                                            );
        fhdrblopehs.setDestination_fab_id ( ""                                                            );
        fhdrblopehs.setHold_state         ( durableOperationMoveEventRecord.getDurableData().getHoldState()        );
        fhdrblopehs.setInit_hold_flag     ( 0);
        fhdrblopehs.setHold_time          ( "1901-01-01-00.00.00.000000"                                  );
        fhdrblopehs.setHold_shop_date     ( 0D);
        fhdrblopehs.setHold_user_id       ( ""                                                            );
        fhdrblopehs.setHold_type          ( ""                                                            );
        fhdrblopehs.setHold_reason_code   ( ""                                                            );
        fhdrblopehs.setHold_reason_desc   ( ""                                                            );
        fhdrblopehs.setReason_code        ( ""                                                            );
        fhdrblopehs.setReason_description ( ""                                                            );
        fhdrblopehs.setRework_count       ( 0);
        fhdrblopehs.setHold_ope_no        ( ""                                                            );
        fhdrblopehs.setHold_reason_ope_no ( ""                                                            );




        iRc = durableOperationStartHistoryService.insertDRBLHistory_FHDRBLOPEHS( fhdrblopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHDRBLOPEHS_CurrentOperationForOpe(): InsertDRBLHistory_FHDRBLOPEHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateFHDRBLOPEHS_CurrentOperationForOpe Function" );
        return( returnOK() );
    }

}
