package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
 * @date 2019/6/5 14:16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotOperationMoveEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotReworkHistoryService lotReworkHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/5 14:16
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createLotOperationMoveEventRecord(Infos.LotOperationMoveEventRecord lotOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets) {
        Response            iRc = returnOK();
        Boolean prev_flag;

        if(variableStrCmp(lotOperationMoveEventRecord.getEventCommon().getTransactionID(),OLOTW005_ID) == 0 ) {
            iRc = createFHOPEHS_TxPassThruReq( lotOperationMoveEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                return(iRc);
            }
        } else if( (variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW013_ID) == 0 ) ||
                (variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLSTW004_ID) == 0 ) ||
                (variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLSTW001_ID) == 0 )  ) {
            prev_flag = false;
            iRc = createFHOPEHS_CurrentOperationForOpe( lotOperationMoveEventRecord, prev_flag, userDataSets );
            if ( !isOk(iRc) ) {
                return(iRc);
            }
        } else {
            prev_flag = FALSE;
            iRc = createFHOPEHS_PreviousOperationForOpe( lotOperationMoveEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                return(iRc);
            }
            prev_flag = TRUE;

            iRc = createFHOPEHS_CurrentOperationForOpe( lotOperationMoveEventRecord, prev_flag, userDataSets );
            if ( !isOk(iRc) ) {
                return(iRc);
            }
        }
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/5 14:23
     */
    public Response createFHOPEHS_PreviousOperationForOpe(Infos.LotOperationMoveEventRecord lotOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs =new Infos.Ohopehs();
        Infos.OhopehsPasscnt fhopehs_passcnt = new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd = new Infos.Frpd();
        Infos.Frpd resultData_pd_prev = new Infos.Frpd();
        Infos.Frlot resultData_lot =new Infos.Frlot();
        Infos.Frpos resultData_pos_old = new Infos.Frpos();
        Infos.Frpos resultData_pos_prev = new Infos.Frpos();
        Infos.Frpos resultData_pos = new Infos.Frpos();
        Params.String castCategory  = new Params.String();
        Params.String productGrpID  = new Params.String();
        Params.String prodType      = new Params.String();
        Params.String techID        = new Params.String();
        Params.String custprodID    = new Params.String();
        Params.String stageGrpID    = new Params.String();
        Params.String prevStageGrpID= new Params.String();
        Params.String mainPDID      = new Params.String();

        Response                   iRc = returnOK();

        iRc = tableMethod.getFRLOT( lotOperationMoveEventRecord.getLotData().getLotID (),      resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotOperationMoveEventRecord.getLotData().getCassetteID(),  castCategory ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getOldCurrentPOData().getObjrefPOS(),
                lotOperationMoveEventRecord.getOldCurrentPOData().getOperationNumber(),
                lotOperationMoveEventRecord.getOldCurrentPOData().getObjrefMainPF(),
                resultData_pos_old );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos_old.setOperationNO (lotOperationMoveEventRecord.getOldCurrentPOData().getOperationNumber ());
        resultData_pos_old.setPdID        (lotOperationMoveEventRecord.getOldCurrentPOData().getOperationID ());

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getLotData().getObjrefPOS (),
                lotOperationMoveEventRecord.getLotData().getOperationNumber(),
                lotOperationMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO     (lotOperationMoveEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotOperationMoveEventRecord.getLotData().getOperationID     ());



        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getPreviousObjrefPOS(),
                lotOperationMoveEventRecord.getPreviousOperationNumber(),
                lotOperationMoveEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ){
            return( iRc );
        }

        resultData_pos_prev.setOperationNO (lotOperationMoveEventRecord.getPreviousOperationNumber ());
        resultData_pos_prev.setPdID(lotOperationMoveEventRecord.getPreviousOperationID     ());

        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getPreviousOperationID(),  resultData_pd ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_old.getPdID(),                           resultData_pd_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotOperationMoveEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotOperationMoveEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotOperationMoveEventRecord.getLotData().getLotID(),lotOperationMoveEventRecord.getLotData().getProductID (),
                custprodID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_old.getStageID (), prevStageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        mainPDID .setValue(lotOperationMoveEventRecord.getOldCurrentPOData().getRouteID    ());

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotOperationMoveEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotOperationMoveEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type (resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotOperationMoveEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category (castCategory.getValue());
        fhopehs.setMainpd_id (lotOperationMoveEventRecord.getPreviousRouteID ());
        fhopehs.setOpe_no (lotOperationMoveEventRecord.getPreviousOperationNumber ());
        fhopehs.setPd_id  (lotOperationMoveEventRecord.getPreviousOperationID ());
        fhopehs.setOpe_pass_count    (lotOperationMoveEventRecord.getPreviousOperationPassCount ()==null?null:
                lotOperationMoveEventRecord.getPreviousOperationPassCount ().intValue());
        fhopehs.setPd_name (resultData_pd.getOperationName ());
        fhopehs.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date   (lotOperationMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotOperationMoveEventRecord.getEventCommon().getUserID ());
        if( variableStrCmp ( resultData_pos_prev.getStageID (), resultData_pos_old.getStageID ()) != 0 ) {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE  );
        }else{
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION );
        }
        if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW003_ID ) == 0 ||
                variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW004_ID ) == 0 ) {
            if( isTrue(lotOperationMoveEventRecord.getLocateBackFlag ()) ) {
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_LOCATEBACKWARD );
            }else{
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_LOCATEFORWARD );
            }
        }

        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW035_ID) == 0 ||
                variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW006_ID) == 0 )

        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BRANCH );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW037_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BRANCHCANCEL );
        }
        else if( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_MOVETOSPLIT );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), TXTRC044_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_PILOTSPLIT );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), TXTRC045_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_PILOTMERGE );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW006_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_SCHEDULECHANGE );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW007_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_REQUEUE );
        }
        fhopehs.setProd_type (prodType.getValue() );
        fhopehs.setTest_type (resultData_pd.getTestType ());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotOperationMoveEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue()  );
        fhopehs.setTech_id (techID.getValue() );
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID .getValue() );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos_prev.getStageID ());
        fhopehs.setStagegrp_id (stageGrpID.getValue() );

        fhopehs.setPhoto_layer (resultData_pos_prev.getPhotoLayer ());
        fhopehs.setReticle_count     (0 );
        fhopehs.setFixture_count     (0 );
        fhopehs.setRparm_count       (0 );
        fhopehs.setInit_hold_flag    (0 );
        fhopehs.setLast_hldrel_flag  (0 );
        fhopehs.setHold_state (lotOperationMoveEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        fhopehs.setPrev_mainpd_id (mainPDID.getValue() );
        fhopehs.setPrev_ope_no (resultData_pos_old.getOperationNO ());
        fhopehs.setPrev_pd_id (resultData_pos_old.getPdID ());
        if( variableStrCmp ( fhopehs.getPrev_mainpd_id (), "") == 0 && ( variableStrCmp ( fhopehs.getPrev_ope_no(), "" ) == 0 ) ) {
            fhopehs.setPrev_pass_count   (0 );
        } else {
            fhopehs.setPrev_pass_count   (lotOperationMoveEventRecord.getOldCurrentPOData().getOperationPassCount().intValue()-1);
            if( fhopehs.getPrev_pass_count() < 0 )
                fhopehs.setPrev_pass_count (0);
        }
        fhopehs.setPrev_pd_name(resultData_pd_prev.getOperationName ());
        fhopehs.setPrev_photo_layer(resultData_pos_old.getPhotoLayer ());
        fhopehs.setPrev_stage_id (resultData_pos_old.getStageID ());
        fhopehs.setPrev_stagegrp_id (prevStageGrpID.getValue() );

        fhopehs.setFlowbatch_id (lotOperationMoveEventRecord.getBatchID ());
        fhopehs.setCtrl_job (lotOperationMoveEventRecord.getControlJobID ());
        fhopehs.setRework_count      (0 );
        fhopehs.setOrg_wafer_qty     (lotOperationMoveEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty     (lotOperationMoveEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit   (0 );
        fhopehs.setTotal_fail_unit   (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time  (lotOperationMoveEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo  (lotOperationMoveEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag     (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());
        fhopehs.setPd_type(resultData_pd.getPd_type ());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotOperationMoveEventRecord.getProcessWafers()) > 0 ) {
            for( int i = 0; i < length(lotOperationMoveEventRecord.getProcessWafers()); i++ ) {
                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(fhopehs.getLot_id ());
                fhopehs_passcnt.setMainpd_id(fhopehs.getPrev_mainpd_id ());
                fhopehs_passcnt.setOpe_no(fhopehs.getPrev_ope_no ());
                fhopehs_passcnt.setClaim_time(fhopehs.getClaim_time ());
                fhopehs_passcnt.setOpe_pass_count (fhopehs.getPrev_pass_count());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type ());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category ());
                fhopehs_passcnt.setWafer_id(lotOperationMoveEventRecord.getProcessWafers().get(i).getWaferID ());
                fhopehs_passcnt.setPass_count     (lotOperationMoveEventRecord.getProcessWafers().get(i).getPreviousPassCount()==null?null:
                        lotOperationMoveEventRecord.getProcessWafers().get(i).getPreviousPassCount().intValue());

                iRc = lotOperationHistoryService.insertLotOperationPasscntHistory( fhopehs_passcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param prev_flag
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/5 14:21
     */
    public Response createFHOPEHS_CurrentOperationForOpe(Infos.LotOperationMoveEventRecord lotOperationMoveEventRecord,
                                                         Boolean prev_flag, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs = new Infos.Ohopehs();
        Infos.OhopehsPasscnt fhopehs_passcnt =new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frpd resultData_pd_prev=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos =new Infos.Frpos();
        Infos.Frpos resultData_pos_prev =new Infos.Frpos();
        Params.String castCategory  =new Params.String();
        Params.String productGrpID  =new Params.String();
        Params.String prodType      =new Params.String();
        Params.String techID        =new Params.String();
        Params.String custprodID    =new Params.String();
        Params.String stageGrpID    =new Params.String();
        Params.String prevStageGrpID=new Params.String();
        Params.String mainPDID      =new Params.String();

        Response                   iRc = returnOK();

        iRc = tableMethod.getFRLOT( lotOperationMoveEventRecord.getLotData().getLotID (),      resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotOperationMoveEventRecord.getLotData().getCassetteID (), castCategory ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( isTrue(prev_flag ) ){
            iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getPreviousObjrefPOS(),
                    lotOperationMoveEventRecord.getPreviousOperationNumber(),
                    lotOperationMoveEventRecord.getPreviousObjrefMainPF(),
                    resultData_pos_prev );
            if( !isOk(iRc) ) {
                return( iRc );
            }

           resultData_pos_prev.setOperationNO (lotOperationMoveEventRecord.getPreviousOperationNumber ());
           resultData_pos_prev.setPdID(lotOperationMoveEventRecord.getPreviousOperationID     ());

        } else {
            iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getOldCurrentPOData().getObjrefPOS(),
                    lotOperationMoveEventRecord.getOldCurrentPOData().getOperationNumber(),
                    lotOperationMoveEventRecord.getOldCurrentPOData().getObjrefMainPF(),
                    resultData_pos_prev );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            resultData_pos_prev.setOperationNO (lotOperationMoveEventRecord.getOldCurrentPOData().getOperationNumber ());
            resultData_pos_prev.setPdID        (lotOperationMoveEventRecord.getOldCurrentPOData().getOperationID ());

        }

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getLotData().getObjrefPOS (),
                lotOperationMoveEventRecord.getLotData().getOperationNumber(),
                lotOperationMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO     (lotOperationMoveEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotOperationMoveEventRecord.getLotData().getOperationID     ());


        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getLotData().getOperationID (), resultData_pd ) ;                         //P3100166
        if( !isOk(iRc) )
        {
            return( iRc );
        }

        if(Objects.equals(prev_flag, TRUE)){
            iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getPreviousOperationID(),  resultData_pd_prev ) ;                //P3100166
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }else{
            iRc = tableMethod.getFRPD( resultData_pos_prev.getPdID(),  resultData_pd_prev ) ;
            if( !isOk(iRc) ) {
                return( iRc );
            }
        }
        iRc = tableMethod.getFRPRODSPEC( lotOperationMoveEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotOperationMoveEventRecord.getLotData().getProductID(), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotOperationMoveEventRecord.getLotData().getLotID(),
                lotOperationMoveEventRecord.getLotData().getProductID (), custprodID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID (), prevStageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( isTrue(prev_flag ) ){
            mainPDID    .setValue(lotOperationMoveEventRecord.getPreviousRouteID             ());

        }else{

            mainPDID    .setValue(lotOperationMoveEventRecord.getOldCurrentPOData().getRouteID    ());

        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotOperationMoveEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotOperationMoveEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotOperationMoveEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category (castCategory.getValue() );
        fhopehs.setMainpd_id (lotOperationMoveEventRecord.getLotData().getRouteID ());
        fhopehs.setOpe_no (lotOperationMoveEventRecord.getLotData().getOperationNumber ());
        fhopehs.setPd_id  (lotOperationMoveEventRecord.getLotData().getOperationID ());
        fhopehs.setOpe_pass_count    (lotOperationMoveEventRecord.getLotData().getOperationPassCount ());
        fhopehs.setPd_name (resultData_pd.getOperationName ());
        fhopehs.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date   (lotOperationMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotOperationMoveEventRecord.getEventCommon().getUserID ());
        if( variableStrCmp ( resultData_pos.getStageID (), resultData_pos_prev.getStageID ()) != 0 )
        {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDSTAGE  );
        }else{
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDOPERATION );
        }
        if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW003_ID ) == 0 ||
                variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW004_ID ) == 0 )
        {
            if( isTrue(lotOperationMoveEventRecord.getLocateBackFlag ()) ) {
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_LOCATEBACKWARD );
            }else{
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_LOCATEFORWARD );
            }
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW035_ID) == 0 ||
                variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW006_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BRANCH );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLOTW037_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BRANCHCANCEL );
        }
        else if( variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OLOTW030_ID ) == 0 ||
                variableStrCmp( lotOperationMoveEventRecord.getEventCommon().getTransactionID(), OLOTW032_ID ) == 0   )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_MOVETOSPLIT );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), TXTRC044_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_PILOTSPLIT );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), TXTRC045_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_PILOTMERGE );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW006_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_SCHEDULECHANGE );
        }
//D4100120 Add Start
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW007_ID) == 0 )
        {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_REQUEUE );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OPLNW013_ID) == 0 )
        {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_NONMOVE  );
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_WIPLOTRESET );
        }
        else if( variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLSTW004_ID) == 0 ||
                variableStrCmp ( lotOperationMoveEventRecord.getEventCommon().getTransactionID (), OLSTW001_ID) == 0 )
        {
            fhopehs.setMove_type    (SP_MOVEMENTTYPE_STB ) ;
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_STB );
        }
        fhopehs.setProd_type (prodType.getValue() );
        fhopehs.setTest_type (resultData_pd.getTestType ());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotOperationMoveEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue()  );
        fhopehs.setTech_id (techID.getValue() );
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID.getValue()  );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos.getStageID ());
        fhopehs.setStagegrp_id (stageGrpID.getValue() );
        fhopehs.setPhoto_layer (resultData_pos.getPhotoLayer ());
        fhopehs.setReticle_count     (0 );
        fhopehs.setFixture_count     (0 );
        fhopehs.setRparm_count       (0 );
        fhopehs.setInit_hold_flag    (0 );
        fhopehs.setLast_hldrel_flag  (0 );
        fhopehs.setHold_state (lotOperationMoveEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        if( isTrue(prev_flag ) ){
            fhopehs.setPrev_mainpd_id (lotOperationMoveEventRecord.getPreviousRouteID ());
            fhopehs.setPrev_ope_no (lotOperationMoveEventRecord.getPreviousOperationNumber());
            fhopehs.setPrev_pd_id (lotOperationMoveEventRecord.getPreviousOperationID ());
            fhopehs.setPrev_pass_count   (lotOperationMoveEventRecord.getPreviousOperationPassCount ()==null?null:
                    lotOperationMoveEventRecord.getPreviousOperationPassCount ().intValue());
        } else {
            fhopehs.setPrev_mainpd_id (mainPDID.getValue() );
            fhopehs.setPrev_ope_no (resultData_pos_prev.getOperationNO ());
            fhopehs.setPrev_pd_id (resultData_pos_prev.getPdID ());
            fhopehs.setPrev_pass_count   (0) ;
        }
        fhopehs.setPrev_pd_name (resultData_pd_prev.getOperationName ());
        fhopehs.setPrev_photo_layer (resultData_pos_prev.getPhotoLayer ());
        fhopehs.setPrev_stage_id (resultData_pos_prev.getStageID ());
        fhopehs.setPrev_stagegrp_id (prevStageGrpID .getValue());
        fhopehs.setFlowbatch_id (lotOperationMoveEventRecord.getBatchID ());
        fhopehs.setCtrl_job (lotOperationMoveEventRecord.getControlJobID ());
        fhopehs.setRework_count      (0 );
        fhopehs.setOrg_wafer_qty     (lotOperationMoveEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty     (lotOperationMoveEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit   (0 );
        fhopehs.setTotal_fail_unit   (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time  (lotOperationMoveEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo  (lotOperationMoveEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag     (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());
        fhopehs.setPd_type(resultData_pd.getPd_type ());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotOperationMoveEventRecord.getProcessWafers()) > 0 ) {
            for( int i = 0; i < length(lotOperationMoveEventRecord.getProcessWafers()); i++ ) {
                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(fhopehs.getLot_id ());
                fhopehs_passcnt.setMainpd_id(fhopehs.getMainpd_id ());
                fhopehs_passcnt.setOpe_no(fhopehs.getOpe_no ());
                fhopehs_passcnt.setClaim_time(fhopehs.getClaim_time ());
                fhopehs_passcnt.setOpe_pass_count (fhopehs.getOpe_pass_count());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type ());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category ());
                fhopehs_passcnt.setWafer_id(lotOperationMoveEventRecord.getProcessWafers().get(i).getWaferID ());
                fhopehs_passcnt.setPass_count     (lotOperationMoveEventRecord.getProcessWafers().get(i).getPassCount()==null?null:
                        lotOperationMoveEventRecord.getProcessWafers().get(i).getPassCount().intValue());

                iRc = lotOperationHistoryService.insertLotOperationPasscntHistory( fhopehs_passcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/5 14:17
     */
    public Response createFHOPEHS_TxPassThruReq(Infos.LotOperationMoveEventRecord lotOperationMoveEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs =new Infos.Ohopehs();
        Infos.OhopehsPasscnt fhopehs_passcnt =new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frpd resultData_pd_prev=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos =new Infos.Frpos();
        Infos.Frpos resultData_pos_prev =new Infos.Frpos();
        Params.String castCategory  =new Params.String();
        Params.String productGrpID  =new Params.String();
        Params.String prodType      =new Params.String();
        Params.String techID        =new Params.String();
        Params.String custprodID    =new Params.String();
        Params.String stageGrpID    =new Params.String();
        Params.String prev_stageGrpID=new Params.String();
        Params.String areaID        =new Params.String();
        Params.String eqpName       =new Params.String();
        Params.String locationID    =new Params.String();
        Response                   iRc = returnOK();


        iRc = tableMethod.getFRLOT( lotOperationMoveEventRecord.getLotData().getLotID (),     resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getPreviousObjrefPOS(),
                lotOperationMoveEventRecord.getPreviousOperationNumber(),
                lotOperationMoveEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos_prev.setOperationNO (lotOperationMoveEventRecord.getPreviousOperationNumber ());
        resultData_pos_prev.setPdID        (lotOperationMoveEventRecord.getPreviousOperationID ());

        iRc = tableMethod.getFRPOS( lotOperationMoveEventRecord.getLotData().getObjrefPOS (),
                lotOperationMoveEventRecord.getLotData().getOperationNumber(),
                lotOperationMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO     (lotOperationMoveEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotOperationMoveEventRecord.getLotData().getOperationID     ());


        iRc = tableMethod.getFRCAST( lotOperationMoveEventRecord.getLotData().getCassetteID(), castCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getLotData().getOperationID(), resultData_pd );
        if( !isOk(iRc) )
        {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotOperationMoveEventRecord.getPreviousOperationID(), resultData_pd_prev );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotOperationMoveEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotOperationMoveEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) )
        {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotOperationMoveEventRecord.getLotData().getLotID(),
                lotOperationMoveEventRecord.getLotData().getProductID (), custprodID );
        if( !isOk(iRc) )
        {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID (), prev_stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFREQP( lotOperationMoveEventRecord.getEquipmentID (), areaID , eqpName  );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotOperationMoveEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotOperationMoveEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type (resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotOperationMoveEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category (castCategory.getValue() );
        fhopehs.setMainpd_id (lotOperationMoveEventRecord.getLotData().getRouteID ());
        fhopehs.setOpe_no (lotOperationMoveEventRecord.getLotData().getOperationNumber ());
        fhopehs.setPd_id  (lotOperationMoveEventRecord.getLotData().getOperationID ());
        fhopehs.setOpe_pass_count    (lotOperationMoveEventRecord.getLotData().getOperationPassCount ());
        fhopehs.setPd_name (resultData_pd.getOperationName ());
        fhopehs.setClaim_time(lotOperationMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date   (lotOperationMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotOperationMoveEventRecord.getEventCommon().getUserID ());
        if( variableStrCmp ( resultData_pos.getStageID (), resultData_pos_prev.getStageID ()) != 0 ) {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDSTAGE );
        }else{
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDOPERATION );
        }

        fhopehs.setOpe_category (SP_OPERATIONCATEGORY_GATEPASS );
        fhopehs.setProd_type (prodType.getValue() );
        fhopehs.setTest_type (resultData_pd.getTestType ());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotOperationMoveEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue()  );
        fhopehs.setTech_id (techID.getValue() );
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID.getValue()  );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos.getStageID ());
        fhopehs.setStagegrp_id (stageGrpID.getValue() );
        fhopehs.setPhoto_layer (resultData_pos.getPhotoLayer ());
        fhopehs.setReticle_count     (0 );
        fhopehs.setFixture_count     (0 );
        fhopehs.setRparm_count       (0 );
        fhopehs.setInit_hold_flag    (0 );
        fhopehs.setLast_hldrel_flag  (0 );
        fhopehs.setHold_state (lotOperationMoveEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        fhopehs.setPrev_mainpd_id (lotOperationMoveEventRecord.getPreviousRouteID ());
        fhopehs.setPrev_ope_no (lotOperationMoveEventRecord.getPreviousOperationNumber ());
        fhopehs.setPrev_pd_id  (lotOperationMoveEventRecord.getPreviousOperationID ());
        fhopehs.setPrev_pass_count   (lotOperationMoveEventRecord.getPreviousOperationPassCount ()==null?null:
                lotOperationMoveEventRecord.getPreviousOperationPassCount ().intValue());
        fhopehs.setPrev_pd_name (resultData_pd_prev.getOperationName ());
        fhopehs.setPrev_photo_layer(resultData_pos_prev.getPhotoLayer ());
        fhopehs.setPrev_stage_id(resultData_pos_prev.getStageID ());
        fhopehs.setPrev_stagegrp_id(prev_stageGrpID.getValue() );
        fhopehs.setFlowbatch_id (lotOperationMoveEventRecord.getBatchID ());
        fhopehs.setCtrl_job (lotOperationMoveEventRecord.getControlJobID ());
        fhopehs.setRework_count      (0 );
        fhopehs.setOrg_wafer_qty     (lotOperationMoveEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty     (lotOperationMoveEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty    (lotOperationMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty    (lotOperationMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit   (0 );
        fhopehs.setTotal_fail_unit   (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time  (lotOperationMoveEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo  (lotOperationMoveEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag     (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotOperationMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());
        fhopehs.setPd_type(resultData_pd.getPd_type ());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotOperationMoveEventRecord.getProcessWafers())  > 0 ) {
            for( int i = 0; i < length(lotOperationMoveEventRecord.getProcessWafers()); i++ ) {

                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(fhopehs.getLot_id ());
                fhopehs_passcnt.setMainpd_id(fhopehs.getMainpd_id ());
                fhopehs_passcnt.setOpe_no(fhopehs.getOpe_no ());
                fhopehs_passcnt.setOpe_pass_count (fhopehs.getOpe_pass_count());
                fhopehs_passcnt.setClaim_time(fhopehs.getClaim_time ());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type ());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category ());
                fhopehs_passcnt.setWafer_id(lotOperationMoveEventRecord.getProcessWafers().get(i).getWaferID ());
                fhopehs_passcnt.setPass_count     (lotOperationMoveEventRecord.getProcessWafers().get(i).getPassCount()==null?null:
                        lotOperationMoveEventRecord.getProcessWafers().get(i).getPassCount().intValue());

                iRc = lotOperationHistoryService.insertLotOperationPasscntHistory( fhopehs_passcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

}
