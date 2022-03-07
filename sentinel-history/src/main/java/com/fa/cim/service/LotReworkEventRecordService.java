package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
import static com.fa.cim.utils.StringUtils.strcmp;
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
 * @date 2019/6/3 17:05
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotReworkEventRecordService {

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
     * @param lotReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/3 17:05
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createLotReworkEventRecord(Infos.LotReworkEventRecord lotReworkEventRecord, List<Infos.UserDataSet> userDataSets) {
        Response iRc = returnOK();

        iRc = createFHOPEHS_PreviousOperationForRwk( lotReworkEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            return ( iRc );
        }
        iRc = createFHOPEHS_CurrentOperationForRwk( lotReworkEventRecord, userDataSets );
        if ( !isOk(iRc) ) {
            return ( iRc );
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
     * @param lotReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/3 17:11
     */
    public Response createFHOPEHS_CurrentOperationForRwk(Infos.LotReworkEventRecord lotReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs =new Infos.Ohopehs();
        Infos.OhopehsPasscnt fhopehs_passcnt =new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frpd resultData_pd_prev=new Infos.Frpd();
        Infos.Frlot resultData_lot;
        Infos.Frpos resultData_pos = new Infos.Frpos();
        Infos.Frpos resultData_pos_prev =new Infos.Frpos();
        Params.String castCategory   = new Params.String();
        Params.String productGrpID   = new Params.String();
        Params.String prodType       = new Params.String();
        Params.String techID         = new Params.String();
        Params.String custprodID     = new Params.String();
        Params.String stageGrpID     = new Params.String();
        Params.String prevStageGrpID = new Params.String();
        Params.String codeCategory   = new Params.String();
        Params.String mainPDID       = new Params.String();
        Response                   iRc = returnOK();

        resultData_lot=new Infos.Frlot();
        iRc = tableMethod.getFRLOT( lotReworkEventRecord.getLotData().getLotID (),     resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotReworkEventRecord.getLotData().getCassetteID(), castCategory ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotReworkEventRecord.getLotData().getOperationID(), resultData_pd ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotReworkEventRecord.getPreviousOperationID(), resultData_pd_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotReworkEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotReworkEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotReworkEventRecord.getLotData().getLotID(),lotReworkEventRecord.getLotData().getProductID (),
                custprodID );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS( lotReworkEventRecord.getPreviousObjrefPOS(),
                lotReworkEventRecord.getPreviousOperationNumber(),
                lotReworkEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        resultData_pos_prev.setOperationNO (lotReworkEventRecord.getPreviousOperationNumber         ()) ;
        resultData_pos_prev.setPdID        (lotReworkEventRecord.getPreviousOperationID             ()) ;

        iRc = tableMethod.getFRPOS( lotReworkEventRecord.getLotData().getObjrefPOS(),
                lotReworkEventRecord.getLotData().getOperationNumber(),
                lotReworkEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO (lotReworkEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID        (lotReworkEventRecord.getLotData().getOperationID     ());


        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( resultData_pos_prev.getStageID (), prevStageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        mainPDID.setValue(lotReworkEventRecord.getPreviousRouteID    ());

        iRc = tableMethod.getFRCODE( lotReworkEventRecord.getReasonCodeID (), codeCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotReworkEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotReworkEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type (resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotReworkEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category (castCategory.getValue() );
        fhopehs.setMainpd_id (lotReworkEventRecord.getLotData().getRouteID ());
        fhopehs.setOpe_no (lotReworkEventRecord.getLotData().getOperationNumber ());
        fhopehs.setPd_id  (lotReworkEventRecord.getLotData().getOperationID ());
        fhopehs.setOpe_pass_count    (lotReworkEventRecord.getLotData().getOperationPassCount ());
        fhopehs.setPd_name (resultData_pd.getOperationName ());
        fhopehs.setClaim_time(lotReworkEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date   (lotReworkEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotReworkEventRecord.getEventCommon().getUserID ());
        if( strcmp ( resultData_pos.getStageID (), resultData_pos_prev.getStageID ()) != 0 ) {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDSTAGE );
        }else{
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEFORWARDOPERATION );
        }
        if( variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW012_ID ) == 0 ||
                variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0   ) {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_REWORKCANCEL );
        } else {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_REWORK );
        }

        fhopehs.setProd_type (prodType.getValue() );
        fhopehs.setTest_type (resultData_pd.getTestType ());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotReworkEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue()  );
        fhopehs.setTech_id (techID.getValue() );
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID .getValue() );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos.getStageID ());
        fhopehs.setStagegrp_id (stageGrpID .getValue());
        fhopehs.setPhoto_layer (resultData_pos.getPhotoLayer ());
        fhopehs.setReticle_count     (0 );
        fhopehs.setFixture_count     (0 );
        fhopehs.setRparm_count       (0 );
        fhopehs.setInit_hold_flag    (0 );
        fhopehs.setLast_hldrel_flag  (0 );
        fhopehs.setHold_state (lotReworkEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        fhopehs.setReason_code (lotReworkEventRecord.getReasonCodeID().getIdentifier ());
        fhopehs.setReason_description(codeCategory.getValue() );
        fhopehs.setPrev_mainpd_id (lotReworkEventRecord.getPreviousRouteID ());
        fhopehs.setPrev_ope_no (lotReworkEventRecord.getPreviousOperationNumber ());
        fhopehs.setPrev_pd_id (lotReworkEventRecord.getPreviousOperationID ());
        fhopehs.setPrev_pass_count   (lotReworkEventRecord.getPreviousOperationPassCount ()==null?null:
                lotReworkEventRecord.getPreviousOperationPassCount ().intValue());
        fhopehs.setPrev_pd_name(resultData_pd_prev.getOperationName ());
        fhopehs.setPrev_photo_layer(resultData_pos_prev.getPhotoLayer());
        fhopehs.setPrev_stage_id (resultData_pos_prev.getStageID ());
        fhopehs.setPrev_stagegrp_id (prevStageGrpID.getValue() );
        fhopehs.setRework_count      (lotReworkEventRecord.getReworkCount ()==null?null:
                lotReworkEventRecord.getReworkCount ().intValue());
        fhopehs.setOrg_wafer_qty     (lotReworkEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty     (lotReworkEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty    (lotReworkEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty    (lotReworkEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty    (lotReworkEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty    (lotReworkEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit   (0 );
        fhopehs.setTotal_fail_unit   (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time  (lotReworkEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo  (lotReworkEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag     (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotReworkEventRecord.getEventCommon().getEventCreationTimeStamp ());
        fhopehs.setPd_type(resultData_pd.getPd_type ());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotReworkEventRecord.getProcessWafers()) > 0 ) {
            for( int i = 0; i < length(lotReworkEventRecord.getProcessWafers()); i++ ) {
                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(fhopehs.getLot_id ());
                fhopehs_passcnt.setMainpd_id(fhopehs.getMainpd_id ());
                fhopehs_passcnt.setOpe_no(fhopehs.getOpe_no ());
                fhopehs_passcnt.setClaim_time(fhopehs.getClaim_time ());
                fhopehs_passcnt.setOpe_pass_count (fhopehs.getOpe_pass_count());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type ());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category ());
                fhopehs_passcnt.setWafer_id(lotReworkEventRecord.getProcessWafers().get(i).getWaferID ());
                fhopehs_passcnt.setPass_count     (lotReworkEventRecord.getProcessWafers().get(i).getPassCount()==null?null:
                        lotReworkEventRecord.getProcessWafers().get(i).getPassCount().intValue());

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
     * @param lotReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/3 17:07
     */
    public Response createFHOPEHS_PreviousOperationForRwk(Infos.LotReworkEventRecord lotReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs =new Infos.Ohopehs();
        Infos.OhopehsRwkcnt fhopehs_rwkcnt =new Infos.OhopehsRwkcnt();
        Infos.OhopehsPasscnt fhopehs_passcnt =new Infos.OhopehsPasscnt();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frpd resultData_pd_prev=new Infos.Frpd();
        Infos.Frlot resultData_lot = new Infos.Frlot();
        Infos.Frpos resultData_pos_old =new Infos.Frpos();
        Infos.Frpos resultData_pos =new Infos.Frpos();
        Infos.Frpos resultData_pos_prev =new Infos.Frpos();
        Params.String castCategory  = new Params.String();
        Params.String productGrpID  = new Params.String();
        Params.String prodType      = new Params.String();
        Params.String techID        = new Params.String();
        Params.String custprodID    = new Params.String();
        Params.String stageGrpID    = new Params.String();
        Params.String prevStageGrpID= new Params.String();
        Params.String codeCategory  = new Params.String();
        Params.String mainPDID      = new Params.String();
        int                   i;
        Response                   iRc = returnOK();


        iRc = tableMethod.getFRLOT( lotReworkEventRecord.getLotData().getLotID (),      resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotReworkEventRecord.getLotData().getCassetteID(),  castCategory ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( lotReworkEventRecord.getOldCurrentPOData().getObjrefPOS(),
                lotReworkEventRecord.getOldCurrentPOData().getOperationNumber(),
                lotReworkEventRecord.getOldCurrentPOData().getObjrefMainPF(),
                resultData_pos_old ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        resultData_pos_old.setOperationNO (lotReworkEventRecord.getOldCurrentPOData().getOperationNumber         ()) ;
        resultData_pos_old.setPdID        (lotReworkEventRecord.getOldCurrentPOData().getOperationID             ()) ;

        iRc = tableMethod.getFRPOS( lotReworkEventRecord.getLotData().getObjrefPOS (),
                lotReworkEventRecord.getLotData().getOperationNumber(),
                lotReworkEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        resultData_pos.setOperationNO     (lotReworkEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotReworkEventRecord.getLotData().getOperationID     ());

        iRc = tableMethod.getFRPOS( lotReworkEventRecord.getPreviousObjrefPOS(),
                lotReworkEventRecord.getPreviousOperationNumber(),
                lotReworkEventRecord.getPreviousObjrefMainPF(),
                resultData_pos_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        resultData_pos.setOperationNO    (lotReworkEventRecord.getPreviousOperationNumber ());
        resultData_pos.setPdID           (lotReworkEventRecord.getPreviousOperationID     ());


        iRc = tableMethod.getFRPD( lotReworkEventRecord.getPreviousOperationID (), resultData_pd ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( resultData_pos_old.getPdID(),                    resultData_pd_prev ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotReworkEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotReworkEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotReworkEventRecord.getLotData().getLotID(),lotReworkEventRecord.getLotData().getProductID(), custprodID );
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


        mainPDID    .setValue(lotReworkEventRecord.getOldCurrentPOData().getRouteID    ());

        iRc = tableMethod.getFRCODE( lotReworkEventRecord.getReasonCodeID (), codeCategory );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotReworkEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotReworkEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type (resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotReworkEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category (castCategory.getValue() );
        fhopehs.setMainpd_id (lotReworkEventRecord.getPreviousRouteID ());
        fhopehs.setOpe_no (lotReworkEventRecord.getPreviousOperationNumber ());
        fhopehs.setPd_id  (lotReworkEventRecord.getPreviousOperationID ());
        fhopehs.setOpe_pass_count    (lotReworkEventRecord.getPreviousOperationPassCount ()==null?null:
                lotReworkEventRecord.getPreviousOperationPassCount ().intValue());
        fhopehs.setPd_name (resultData_pd.getOperationName ());
        fhopehs.setClaim_time(lotReworkEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date   (lotReworkEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotReworkEventRecord.getEventCommon().getUserID ());

        if( strcmp ( resultData_pos_prev.getStageID (), resultData_pos_old.getStageID ()) != 0 ) {
            fhopehs.setMove_type  (SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE );
        }else{
            fhopehs.setMove_type  (SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION );
        }
        if( variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW012_ID ) == 0 ||
                variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0   ) {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_REWORKCANCEL );
        } else {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_REWORK );
        }

        fhopehs.setProd_type (prodType .getValue());
        fhopehs.setTest_type (resultData_pd.getTestType ());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotReworkEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue()  );
        fhopehs.setTech_id (techID .getValue());
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID .getValue() );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos_prev.getStageID ());
        fhopehs.setStagegrp_id (stageGrpID .getValue());
        fhopehs.setPhoto_layer (resultData_pos_prev.getPhotoLayer ());
        fhopehs.setReticle_count     (0 );
        fhopehs.setFixture_count     (0 );
        fhopehs.setRparm_count       (0 );
        fhopehs.setInit_hold_flag    (0 );
        fhopehs.setLast_hldrel_flag  (0 );
        fhopehs.setHold_state (lotReworkEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        fhopehs.setReason_code (lotReworkEventRecord.getReasonCodeID().getIdentifier ());
        fhopehs.setReason_description (codeCategory.getValue() );
        fhopehs.setPrev_mainpd_id (mainPDID.getValue() );
        fhopehs.setPrev_ope_no (resultData_pos_old.getOperationNO ());
        fhopehs.setPrev_pd_id (resultData_pos_old.getPdID ());
        if( variableStrCmp ( fhopehs.getPrev_mainpd_id (), "") == 0 && ( variableStrCmp ( fhopehs.getPrev_ope_no(), "" ) == 0 ) ) {
            fhopehs.setPrev_pass_count   (0 );
        } else {
            fhopehs.setPrev_pass_count   (lotReworkEventRecord.getOldCurrentPOData().getOperationPassCount().intValue()-1);
            if( fhopehs.getPrev_pass_count ()< 0 )
                fhopehs.setPrev_pass_count (0);
        }
        fhopehs.setPrev_pd_name(resultData_pd_prev.getOperationName ()) ;
        fhopehs.setPrev_photo_layer(resultData_pos_old.getPhotoLayer ());
        fhopehs.setPrev_stage_id (resultData_pos_old.getStageID ());
        fhopehs.setPrev_stagegrp_id (prevStageGrpID.getValue() );
        fhopehs.setRework_count      (lotReworkEventRecord.getReworkCount ()==null?null:
                lotReworkEventRecord.getReworkCount ().intValue());
        fhopehs.setOrg_wafer_qty     (lotReworkEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty     (lotReworkEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty    (lotReworkEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty    (lotReworkEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty    (lotReworkEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty    (lotReworkEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit   (0 );
        fhopehs.setTotal_fail_unit   (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time  (lotReworkEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo  (lotReworkEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag     (CRITERIA_NA);
        fhopehs.setEvent_create_time(lotReworkEventRecord.getEventCommon().getEventCreationTimeStamp ());
        fhopehs.setPd_type(resultData_pd.getPd_type ());
        fhopehs.setPrev_pd_type(resultData_pd_prev.getPd_type ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if( length(lotReworkEventRecord.getReworkWafers()) > 0 ) {
            for( i = 0; i < length(lotReworkEventRecord.getReworkWafers()); i++ ) {
                fhopehs_rwkcnt = new Infos.OhopehsRwkcnt();
                fhopehs_rwkcnt.setLot_id(lotReworkEventRecord.getLotData().getLotID ());
                fhopehs_rwkcnt.setMainpd_id(lotReworkEventRecord.getPreviousRouteID ());
                fhopehs_rwkcnt.setOpe_no(lotReworkEventRecord.getPreviousOperationNumber ());
                fhopehs_rwkcnt.setOpe_pass_count (lotReworkEventRecord.getPreviousOperationPassCount()==null?null:
                        lotReworkEventRecord.getPreviousOperationPassCount().intValue());
                fhopehs_rwkcnt.setClaim_time(lotReworkEventRecord.getEventCommon().getEventTimeStamp ());
                if( variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW012_ID ) == 0 ||
                        variableStrCmp( lotReworkEventRecord.getEventCommon().getTransactionID(), OLOTW021_ID ) == 0   ) {
                    fhopehs_rwkcnt.setOpe_category(SP_OPERATIONCATEGORY_REWORKCANCEL );
                } else {
                    fhopehs_rwkcnt.setOpe_category(SP_OPERATIONCATEGORY_REWORK );
                }

                fhopehs_rwkcnt.setWafer_id(lotReworkEventRecord.getReworkWafers().get(i).getWaferID ());
                fhopehs_rwkcnt.setRework_count   (lotReworkEventRecord.getReworkWafers().get(i).getReworkCount()==null?null:
                        lotReworkEventRecord.getReworkWafers().get(i).getReworkCount().intValue());

                iRc = lotReworkHistoryService.insertLotOperationRwkcntHistory( fhopehs_rwkcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }
        if( length(lotReworkEventRecord.getProcessWafers()) > 0 ) {
            for( i = 0; i < length(lotReworkEventRecord.getProcessWafers()); i++ ) {
                fhopehs_passcnt=new Infos.OhopehsPasscnt();
                fhopehs_passcnt.setLot_id(fhopehs.getLot_id ());
                fhopehs_passcnt.setMainpd_id(fhopehs.getPrev_mainpd_id ());
                fhopehs_passcnt.setOpe_no(fhopehs.getPrev_ope_no ());
                fhopehs_passcnt.setClaim_time(fhopehs.getClaim_time ());
                fhopehs_passcnt.setOpe_pass_count (fhopehs.getPrev_pass_count());
                fhopehs_passcnt.setMove_type(fhopehs.getMove_type ());
                fhopehs_passcnt.setOpe_category(fhopehs.getOpe_category ());
                fhopehs_passcnt.setWafer_id(lotReworkEventRecord.getProcessWafers().get(i).getWaferID ());
                fhopehs_passcnt.setPass_count     (lotReworkEventRecord.getProcessWafers().get(i).getPreviousPassCount()==null?null:
                        lotReworkEventRecord.getProcessWafers().get(i).getPreviousPassCount().intValue());

                iRc = lotOperationHistoryService.insertLotOperationPasscntHistory( fhopehs_passcnt );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return(returnOK());
    }

}
