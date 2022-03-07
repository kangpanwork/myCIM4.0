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

import static com.fa.cim.Constant.SPConstant.CRITERIA_NA;
import static com.fa.cim.Constant.SPConstant.SP_MOVEMENTTYPE_NONMOVE;
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
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class InterFabXferEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param InterFabXferEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 13:10
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createInterFabXferEventRecord( Infos.InterFabXferEventRecord InterFabXferEventRecord , List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateInterFabXferEventRecord Function");
        Infos.Ohopehs        fhopehs = new Infos.Ohopehs();
        Response iRc = returnOK();
        Infos.Frpd           resultData_pd= new Infos.Frpd();
        Infos.Frlot          resultData_lot= new Infos.Frlot();
        Infos.Frpos          resultData_pos = new Infos.Frpos();
        Params.String                  castCategory   = new Params.String();
        Params.String                  productGrpID   = new Params.String();
        Params.String                  prodType       = new Params.String();
        Params.String                  techID         = new Params.String();
        Params.String                  custprodID     = new Params.String();
        Params.String                  stageGrpID     = new Params.String();
        Params.String                  areaID         = new Params.String();
        Params.String                  description    = new Params.String();
        Params.String                  locationID     = new Params.String();
        Params.String                  stockerID      = new Params.String();
        iRc = tableMethod.getFRCAST( InterFabXferEventRecord.getLotData().getCassetteID() , castCategory ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPD( InterFabXferEventRecord.getLotData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( InterFabXferEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( InterFabXferEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( InterFabXferEventRecord.getLotData().getProductID() , techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( InterFabXferEventRecord.getLotData().getLotID(),InterFabXferEventRecord.getLotData().getProductID() , custprodID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( InterFabXferEventRecord.getLotData().getObjrefPOS(),                    InterFabXferEventRecord.getLotData().getOperationNumber(),                    InterFabXferEventRecord.getLotData().getObjrefMainPF(),                    resultData_pos );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        resultData_pos.setOperationNO(InterFabXferEventRecord.getLotData().getOperationNumber() );
        resultData_pos.setPdID(InterFabXferEventRecord.getLotData().getOperationID()     );
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRBANK ( InterFabXferEventRecord .getLotData().getBankID() ,stockerID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTK( stockerID , areaID ,description  );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxBankInReq Function");
            return( iRc );
        }
        fhopehs = new Infos.Ohopehs();
        fhopehs.setLot_id(InterFabXferEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type(InterFabXferEventRecord.getLotData().getLotType() );
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType() );
        fhopehs.setCast_id(InterFabXferEventRecord.getLotData().getCassetteID() );
        fhopehs.setCast_category(castCategory.getValue() );
        fhopehs.setMainpd_id(InterFabXferEventRecord.getLotData().getRouteID() );
        fhopehs.setOpe_no(InterFabXferEventRecord.getLotData().getOperationNumber() );
        fhopehs.setPd_id(InterFabXferEventRecord.getLotData().getOperationID() );
        fhopehs.setOpe_pass_count(InterFabXferEventRecord.getLotData().getOperationPassCount() );
        fhopehs.setPd_name(resultData_pd.getOperationName() ) ;
        fhopehs.setClaim_time(InterFabXferEventRecord.getEventCommon().getEventTimeStamp() );
        fhopehs.setClaim_shop_date(InterFabXferEventRecord.getEventCommon().getEventShopDate() );
        fhopehs.setClaim_user_id(InterFabXferEventRecord.getEventCommon().getUserID() );
        fhopehs.setOpe_category(InterFabXferEventRecord.getOpeCategory());
        fhopehs.setOriginalFabID(InterFabXferEventRecord.getOriginalFabID());
        fhopehs.setDestinationFabID(InterFabXferEventRecord.getDestinationFabID());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
        fhopehs.setProd_type(prodType.getValue() );
        fhopehs.setTest_type(resultData_pd.getTestType() );
        fhopehs.setMfg_layer(resultData_lot.getMfgLayer() );
        fhopehs.setExt_priority(resultData_lot.getPriority() );
        fhopehs.setPriority_class(resultData_lot.getPriorityClass() );
        fhopehs.setProdspec_id(InterFabXferEventRecord.getLotData().getProductID() );
        fhopehs.setProdgrp_id(productGrpID .getValue());
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(resultData_lot.getCustomerID() );
        fhopehs.setCustprod_id(custprodID.getValue() );
        fhopehs.setOrder_no(resultData_lot.getOrderNO() );
        fhopehs.setStage_id(resultData_pos.getStageID() );
        fhopehs.setPhoto_layer(resultData_pos.getPhotoLayer()  );
        fhopehs.setStagegrp_id(stageGrpID.getValue() );
        fhopehs.setReticle_count(0 );
        fhopehs.setFixture_count(0 );
        fhopehs.setRparm_count(0 );
        fhopehs.setInit_hold_flag(0 );
        fhopehs.setLast_hldrel_flag(0 );
        fhopehs.setHold_state(InterFabXferEventRecord.getLotData().getHoldState() );
        fhopehs.setHold_time("1901-01-01-00.00.00.000000");
        fhopehs.setBank_id(InterFabXferEventRecord.getLotData().getBankID() );
        fhopehs.setPrev_mainpd_id(InterFabXferEventRecord.getLotData().getRouteID() );
        fhopehs.setPrev_ope_no(InterFabXferEventRecord.getLotData().getOperationNumber() );
        fhopehs.setPrev_pd_id(InterFabXferEventRecord.getLotData().getOperationID() );
        fhopehs.setPrev_pass_count(InterFabXferEventRecord.getLotData().getOperationPassCount());
        fhopehs.setPrev_pd_name(resultData_pd.getOperationName() );
        fhopehs.setPrev_photo_layer(resultData_pos.getPhotoLayer() );
        fhopehs.setPrev_stage_id(resultData_pos.getStageID() );
        fhopehs.setPrev_stagegrp_id(stageGrpID.getValue());
        fhopehs.setRework_count(0 );
        fhopehs.setOrg_wafer_qty(InterFabXferEventRecord.getLotData().getOriginalWaferQuantity() );
        fhopehs.setCur_wafer_qty(InterFabXferEventRecord.getLotData().getCurrentWaferQuantity() );
        fhopehs.setProd_wafer_qty(InterFabXferEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setCntl_wafer_qty(InterFabXferEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setClaim_prod_qty(InterFabXferEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setClaim_cntl_qty(InterFabXferEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setTotal_good_unit(0 );
        fhopehs.setTotal_fail_unit(0 );
        fhopehs.setLot_owner_id(resultData_lot.getLotOwner() );
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime() );
        fhopehs.setWfrhs_time(InterFabXferEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setClaim_memo(InterFabXferEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setCriteria_flag(CRITERIA_NA );
        fhopehs.setEvent_create_time(InterFabXferEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhopehs.setPd_type(resultData_pd.getPd_type() );
        fhopehs.setPrev_pd_type(resultData_pd.getPd_type() );
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateInterFabXferEventRecord(): InsertLotOperationHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateInterFabXferEventRecordFunction");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateInterFabXferEventRecord Function");
        return(returnOK());
    }

}
