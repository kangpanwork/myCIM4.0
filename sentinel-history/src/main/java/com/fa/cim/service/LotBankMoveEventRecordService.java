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
 * @date 2019/6/25 13:44
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotBankMoveEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotHoldHistoryService lotHoldHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotBankMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/25 13:50
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createLotBankMoveEventRecord(Infos.LotBankMoveEventRecord lotBankMoveEventRecord,
                                                 List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateLotBankMoveEventRecord Function");

        Response                 iRc = returnOK();

        if( ( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW008_ID ) == 0 )||
                ( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW011_ID ) == 0 )||
                ( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW012_ID ) == 0 ) ) {                                                                                              // 3100031
            iRc = createFHOPEHS_TxBankMoveReq(lotBankMoveEventRecord,userDataSets);
            if( !isOk(iRc) ) {
                return ( iRc );
            }
        }else{
            iRc = createFHOPEHS_TxBankInReq(lotBankMoveEventRecord,userDataSets);
            if( !isOk(iRc) ) {
                return ( iRc );
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
     * @param lotBankMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/25 13:56
     */
    private Response createFHOPEHS_TxBankMoveReq(Infos.LotBankMoveEventRecord lotBankMoveEventRecord,
                                                 List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs=new Infos.Ohopehs();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos =new Infos.Frpos();
        Params.String castCategory  =new Params.String();
        Params.String productGrpID  =new Params.String();
        Params.String prodType      =new Params.String();
        Params.String techID        =new Params.String();
        Params.String custprodID    =new Params.String();
        Params.String stageGrpID    =new Params.String();
        Params.String areaID        =new Params.String();
        Params.String description   =new Params.String();
        Params.String locationID    =new Params.String();
        Params.String stockerID     =new Params.String();
        Response                   iRc = returnOK();


        iRc = tableMethod.getFRCAST( lotBankMoveEventRecord.getLotData().getCassetteID (), castCategory ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotBankMoveEventRecord.getLotData().getOperationID (), resultData_pd ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotBankMoveEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( lotBankMoveEventRecord.getLotData().getLotID (), resultData_lot ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotBankMoveEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotBankMoveEventRecord.getLotData().getLotID(),lotBankMoveEventRecord.getLotData().getProductID ()
                , custprodID );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRPOS( lotBankMoveEventRecord.getLotData().getObjrefPOS (),
                lotBankMoveEventRecord.getLotData().getOperationNumber(),
                lotBankMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO     (lotBankMoveEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotBankMoveEventRecord.getLotData().getOperationID     ());

        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRBANK ( lotBankMoveEventRecord .getLotData().getBankID (),stockerID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTK( stockerID , areaID ,description  );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotBankMoveEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotBankMoveEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotBankMoveEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category(castCategory.getValue() );
        fhopehs.setOpe_pass_count     (0 );
        fhopehs.setClaim_time (lotBankMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date    (lotBankMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotBankMoveEventRecord.getEventCommon().getUserID ());
        fhopehs.setMove_type (SP_MOVEMENTTYPE_NONMOVE );

        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW008_ID) == 0 ) {
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_MOVEBANK );
        }else{
            if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW011_ID) == 0 ) {
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_SHIP );
            }else{
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_SHIPCANCEL );
            }
        }

        fhopehs.setProd_type (prodType .getValue());
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotBankMoveEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID .getValue());
        fhopehs.setTech_id (techID .getValue());
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID .getValue());
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setLocation_id (locationID .getValue());
        fhopehs.setArea_id (areaID .getValue());
        fhopehs.setReticle_count      (0 );
        fhopehs.setFixture_count      (0 );
        fhopehs.setRparm_count        (0 );
        fhopehs.setInit_hold_flag     (0 );
        fhopehs.setLast_hldrel_flag   (0 );
        fhopehs.setHold_state (lotBankMoveEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );
        fhopehs.setBank_id  (lotBankMoveEventRecord.getLotData().getBankID ());

        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW008_ID ) == 0 ) {
            fhopehs.setPrev_bank_id (lotBankMoveEventRecord.getPreviousBankID ());
        }

        fhopehs.setPrev_pass_count    (0 );
        fhopehs.setRework_count       (0 );
        fhopehs.setOrg_wafer_qty      (lotBankMoveEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty      (lotBankMoveEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty     (lotBankMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty     (lotBankMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty     (lotBankMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty     (lotBankMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit    (0 );
        fhopehs.setTotal_fail_unit    (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time (resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time (lotBankMoveEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo (lotBankMoveEventRecord.getEventCommon().getEventMemo ());

        fhopehs.setCriteria_flag      (CRITERIA_NA );
        fhopehs.setEvent_create_time(lotBankMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

        log.info("Insert Lot Operation History (OHLOTOPE)");
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
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
     * @param lotBankMoveEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/25 13:58
     */
    private Response createFHOPEHS_TxBankInReq(Infos.LotBankMoveEventRecord lotBankMoveEventRecord,
                                               List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs fhopehs =new Infos.Ohopehs();
        Infos.Frpd resultData_pd=new Infos.Frpd();
        Infos.Frlot resultData_lot=new Infos.Frlot();
        Infos.Frpos resultData_pos =new Infos.Frpos();
        Params.String castCategory  =new Params.String();
        Params.String productGrpID  =new Params.String();
        Params.String prodType      =new Params.String();
        Params.String techID        =new Params.String();
        Params.String custprodID    =new Params.String();
        Params.String stageGrpID    =new Params.String();
        Params.String areaID        =new Params.String();
        Params.String description   =new Params.String();
        Params.String locationID    =new Params.String();
        Params.String stockerID     =new Params.String();
        Response                   iRc = returnOK();

        iRc = tableMethod.getFRCAST( lotBankMoveEventRecord.getLotData().getCassetteID (), castCategory ) ;
        if( !isOk(iRc) ){
            return( iRc );
        }
        iRc = tableMethod.getFRPD( lotBankMoveEventRecord.getLotData().getOperationID (), resultData_pd ) ;
        if( !isOk(iRc) ){
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotBankMoveEventRecord.getLotData().getProductID (),productGrpID , prodType );
        if( !isOk(iRc) ){
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( lotBankMoveEventRecord.getLotData().getLotID (), resultData_lot ) ;
        if( !isOk(iRc) ){
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotBankMoveEventRecord.getLotData().getProductID (), techID );
        if( !isOk(iRc) ){
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotBankMoveEventRecord.getLotData().getLotID(),
                lotBankMoveEventRecord.getLotData().getProductID (), custprodID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( lotBankMoveEventRecord.getLotData().getObjrefPOS(),
                lotBankMoveEventRecord.getLotData().getOperationNumber(),
                lotBankMoveEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        resultData_pos.setOperationNO     (lotBankMoveEventRecord.getLotData().getOperationNumber ());
        resultData_pos.setPdID            (lotBankMoveEventRecord.getLotData().getOperationID     ());

        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID (), stageGrpID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRBANK ( lotBankMoveEventRecord .getLotData().getBankID (),stockerID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRSTK( stockerID , areaID ,description  );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhopehs=new Infos.Ohopehs();
        fhopehs.setLot_id (lotBankMoveEventRecord.getLotData().getLotID ());
        fhopehs.setLot_type (lotBankMoveEventRecord.getLotData().getLotType ());
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType ());
        fhopehs.setCast_id (lotBankMoveEventRecord.getLotData().getCassetteID ());
        fhopehs.setCast_category(castCategory.getValue() );
        fhopehs.setMainpd_id (lotBankMoveEventRecord.getLotData().getRouteID ());
        fhopehs.setOpe_no (lotBankMoveEventRecord.getLotData().getOperationNumber ());
        fhopehs.setPd_id (lotBankMoveEventRecord.getLotData().getOperationID ());
        fhopehs.setOpe_pass_count   (lotBankMoveEventRecord.getLotData().getOperationPassCount ());
        fhopehs.setPd_name (resultData_pd.getOperationName ()) ;
        fhopehs.setClaim_time (lotBankMoveEventRecord.getEventCommon().getEventTimeStamp ());
        fhopehs.setClaim_shop_date  (lotBankMoveEventRecord.getEventCommon().getEventShopDate ());
        fhopehs.setClaim_user_id (lotBankMoveEventRecord.getEventCommon().getUserID ());
        fhopehs.setPd_type(resultData_pd.getPd_type());

        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW013_ID ) == 0  )
        {
            fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVENONPRODBANK );
            fhopehs.setOpe_category (SP_OPERATIONCATEGORY_NONPRODBANKIN );
        }else{
            if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW014_ID ) == 0 )
            {
                fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEWIPAREA );
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_NONPRODBANKOUT );
            }else if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW007_ID ) == 0 ){
                fhopehs.setMove_type (SP_MOVEMENTTYPE_MOVEWIPAREA );
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BANKINCANCEL );
            }else{
                fhopehs.setMove_type (SP_MOVEMENTTYPE_END );
                fhopehs.setOpe_category (SP_OPERATIONCATEGORY_BANKIN );
            }
        }

        fhopehs.setProd_type (prodType .getValue());
        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW014_ID ) == 0 ) {
            fhopehs.setTest_type (resultData_pd.getTestType ());
        }
        fhopehs.setMfg_layer (resultData_lot.getMfgLayer ());
        fhopehs.setExt_priority      (resultData_lot.getPriority ());
        fhopehs.setPriority_class    (resultData_lot.getPriorityClass ());
        fhopehs.setProdspec_id (lotBankMoveEventRecord.getLotData().getProductID ());
        fhopehs.setProdgrp_id (productGrpID.getValue() );
        fhopehs.setTech_id (techID.getValue() );
        fhopehs.setCustomer_id (resultData_lot.getCustomerID ());
        fhopehs.setCustprod_id (custprodID.getValue() );
        fhopehs.setOrder_no (resultData_lot.getOrderNO ());
        fhopehs.setStage_id (resultData_pos.getStageID ());
        fhopehs.setPhoto_layer (resultData_pos.getPhotoLayer  ());
        fhopehs.setStagegrp_id (stageGrpID.getValue() );
        fhopehs.setReticle_count      (0 );
        fhopehs.setFixture_count      (0 );
        fhopehs.setRparm_count        (0 );
        fhopehs.setInit_hold_flag     (0 );
        fhopehs.setLast_hldrel_flag   (0 );
        fhopehs.setHold_state (lotBankMoveEventRecord.getLotData().getHoldState ());
        fhopehs.setHold_time ("1901-01-01-00.00.00.000000" );

        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW014_ID ) != 0 )
        {
            fhopehs.setBank_id  (lotBankMoveEventRecord.getLotData().getBankID ());
        }
        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW014_ID ) == 0 ||
                variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW007_ID ) == 0 )
        {
            fhopehs.setPrev_bank_id (lotBankMoveEventRecord.getPreviousBankID ());
        }
        if( variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW013_ID ) == 0 ||
                variableStrCmp( lotBankMoveEventRecord.getEventCommon().getTransactionID (), OBNKW006_ID ) == 0  )
        {
            fhopehs.setPrev_mainpd_id (lotBankMoveEventRecord.getLotData().getRouteID ());
            fhopehs.setPrev_ope_no (lotBankMoveEventRecord.getLotData().getOperationNumber ());
            fhopehs.setPrev_pd_id (lotBankMoveEventRecord.getLotData().getOperationID ());
            fhopehs.setPrev_pass_count (lotBankMoveEventRecord.getLotData().getOperationPassCount ());
            fhopehs.setPrev_pd_name (resultData_pd.getOperationName ()) ;
            fhopehs.setPrev_photo_layer(resultData_pos.getPhotoLayer ());
            fhopehs.setPrev_stage_id (resultData_pos.getStageID ());
            fhopehs.setPrev_stagegrp_id (stageGrpID .getValue());
            fhopehs.setPrev_pd_type(resultData_pd.getPd_type());
        }else{
            fhopehs.setPrev_pass_count    (0 );
        }

        fhopehs.setRework_count       (0 );
        fhopehs.setOrg_wafer_qty      (lotBankMoveEventRecord.getLotData().getOriginalWaferQuantity ());
        fhopehs.setCur_wafer_qty      (lotBankMoveEventRecord.getLotData().getCurrentWaferQuantity ());
        fhopehs.setProd_wafer_qty     (lotBankMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setCntl_wafer_qty     (lotBankMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setClaim_prod_qty     (lotBankMoveEventRecord.getLotData().getProductWaferQuantity ());
        fhopehs.setClaim_cntl_qty     (lotBankMoveEventRecord.getLotData().getControlWaferQuantity ());
        fhopehs.setTotal_good_unit    (0 );
        fhopehs.setTotal_fail_unit    (0 );
        fhopehs.setLot_owner_id (resultData_lot.getLotOwner ());
        fhopehs.setPlan_end_time (resultData_lot.getPlanEndTime ());
        fhopehs.setWfrhs_time (lotBankMoveEventRecord.getLotData().getWaferHistoryTimeStamp ());
        fhopehs.setClaim_memo (lotBankMoveEventRecord.getEventCommon().getEventMemo ());
        fhopehs.setCriteria_flag      (CRITERIA_NA );
        fhopehs.setEvent_create_time(lotBankMoveEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        return(returnOK());
    }

}
