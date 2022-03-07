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
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EqpMonUsedCountRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EqpMonUsedCountHistoryService eqpMonUsedCountHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpMonitorCountEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 15:43
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createEqpMonUsedCountRecord( Infos.EqpMonitorCountEventRecord eqpMonitorCountEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord Function");
        Infos.Ohopehs                   fhopehs= new Infos.Ohopehs();
        Infos.Ohopehs_emucnt            fhopehs_emucnt= new Infos.Ohopehs_emucnt();
        Infos.Frlot                     lotData= new Infos.Frlot();
        Infos.Frpd                      pdData= new Infos.Frpd();
        Infos.Frpos                     resultData_pos= new Infos.Frpos();
        Response iRc = returnOK();
        Params.String                  equipmentName  = new Params.String();
        Params.String                  areaID         = new Params.String();
        Params.String                  locationID     = new Params.String();
        fhopehs_emucnt = new Infos.Ohopehs_emucnt();
        fhopehs = new Infos.Ohopehs();
        lotData = new Infos.Frlot();
        pdData = new Infos.Frpd();
        resultData_pos = new Infos.Frpos();
        iRc = tableMethod.getFRLOT( eqpMonitorCountEventRecord.getLotData().getLotID(), lotData );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRLOT Function");
            return( iRc );
        }
        fhopehs.setLot_id(eqpMonitorCountEventRecord.getLotData().getLotID());
        fhopehs.setLot_type(eqpMonitorCountEventRecord.getLotData().getLotType() );
        fhopehs.setCast_id(eqpMonitorCountEventRecord.getLotData().getCassetteID() );
        fhopehs.setHold_state(eqpMonitorCountEventRecord.getLotData().getHoldState());
        fhopehs.setBank_id(eqpMonitorCountEventRecord.getLotData().getBankID());
        fhopehs.setCtrl_job(eqpMonitorCountEventRecord.getControlJobID() );
        fhopehs.setSub_lot_type(lotData.getSubLotType() );
        fhopehs.setLot_owner_id(lotData.getLotOwner() );
        fhopehs.setPlan_end_time(lotData.getPlanEndTime() );
        fhopehs.setMfg_layer(lotData.getMfgLayer() );
        fhopehs.setCustomer_id(lotData.getCustomerID() );
        fhopehs.setOrder_no(lotData.getOrderNO() );
        fhopehs.setExt_priority(lotData.getPriority() );
        fhopehs.setPriority_class(lotData.getPriorityClass() );
        fhopehs.setOrg_wafer_qty(eqpMonitorCountEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty(eqpMonitorCountEventRecord.getLotData().getCurrentWaferQuantity());
        fhopehs.setProd_wafer_qty(eqpMonitorCountEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty(eqpMonitorCountEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setClaim_cntl_qty(eqpMonitorCountEventRecord.getLotData().getControlWaferQuantity());
        Params.String                  cast_category  = new Params.String();
        iRc = tableMethod.getFRCAST( eqpMonitorCountEventRecord.getLotData().getCassetteID(), cast_category );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRCAST Function");
            return iRc;
        }
        fhopehs.setCast_category(cast_category.getValue() );
        iRc = tableMethod.getFRPOS( eqpMonitorCountEventRecord.getLotData().getObjrefPOS() ,                    eqpMonitorCountEventRecord.getLotData().getOperationNumber(),                    eqpMonitorCountEventRecord.getLotData().getObjrefMainPF(),                    resultData_pos ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRPOS Function");
            return( iRc );
        }
        Params.String                  stageGrpID  = new Params.String();
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRSTAGE Function");
            return( iRc );
        }
        fhopehs.setStage_id(resultData_pos.getStageID() );
        fhopehs.setStagegrp_id(stageGrpID.getValue() );
        fhopehs.setPhoto_layer(resultData_pos.getPhotoLayer() );
        Params.String                  productGrpID   = new Params.String();
        Params.String                  prodType       = new Params.String();
        iRc = tableMethod.getFRPRODSPEC( eqpMonitorCountEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRPRODSPEC Function");
            return( iRc );
        }
        fhopehs.setProdgrp_id(productGrpID.getValue() );
        fhopehs.setProd_type(prodType.getValue() );
        Params.String                  techID         = new Params.String();
        iRc = tableMethod.getFRPRODGRP( eqpMonitorCountEventRecord.getLotData().getProductID() , techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRPRODGRP Function");
            return( iRc );
        }
        fhopehs.setTech_id(techID.getValue() );
        Params.String                  custprodID     = new Params.String();
        iRc = tableMethod.getFRCUSTPROD( eqpMonitorCountEventRecord.getLotData().getLotID(),eqpMonitorCountEventRecord.getLotData().getProductID() , custprodID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRCUSTPROD Function");
            return( iRc );
        }
        fhopehs.setCustprod_id(custprodID.getValue() );
        fhopehs.setReticle_count(0 );
        fhopehs.setFixture_count(0 );
        fhopehs.setRparm_count(0 );
        fhopehs.setInit_hold_flag(0 );
        fhopehs.setLast_hldrel_flag(0 );
        fhopehs.setRework_count(0 );
        fhopehs.setTotal_good_unit(0 );
        fhopehs.setTotal_fail_unit(0 );
        fhopehs.setCriteria_flag(CRITERIA_NA);
        fhopehs.setHold_time("1901-01-01-00.00.00.000000" );
        fhopehs.setWfrhs_time("1901-01-01-00.00.00.000000" );
        fhopehs.setOpe_category(SP_OPERATIONCATEGORY_EQPMONUSEDCNTUPDATE);
        fhopehs.setMainpd_id(eqpMonitorCountEventRecord.getLotData().getRouteID());
        fhopehs.setOpe_no(eqpMonitorCountEventRecord.getLotData().getOperationNumber());
        fhopehs.setPd_id(eqpMonitorCountEventRecord.getLotData().getOperationID());
        fhopehs.setOpe_pass_count(eqpMonitorCountEventRecord.getLotData().getOperationPassCount());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
        fhopehs.setProdspec_id(eqpMonitorCountEventRecord.getLotData().getProductID());
        if( 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW006_ID )
                || 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW008_ID )
                || 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW012_ID )
                || 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW024_ID )
                || 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW014_ID )
                || 0 == variableStrCmp( eqpMonitorCountEventRecord.getEventCommon().getTransactionID(), OEQPW023_ID ) ) {
            fhopehs.setEqp_id(eqpMonitorCountEventRecord.getEquipmentID());
            iRc = tableMethod.getFREQP( eqpMonitorCountEventRecord.getEquipmentID(), areaID, equipmentName );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getOMEQP Function");
                return iRc;
            }
            fhopehs.setEqp_name(equipmentName.getValue() );
            iRc = tableMethod.getFRAREA(areaID, locationID );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRAREA Function");
                return( iRc );
            }
            fhopehs.setLocation_id(locationID.getValue() );
            fhopehs.setArea_id(areaID.getValue() );
        }
        iRc = tableMethod.getFRPD( eqpMonitorCountEventRecord.getLotData().getOperationID(), pdData );
        if ( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord-getFRPD Function");
            return iRc;
        }
        fhopehs.setPd_name(pdData.getOperationName() );
        fhopehs.setTest_type(pdData.getTestType() );
        fhopehs.setClaim_time(eqpMonitorCountEventRecord.getEventCommon().getEventTimeStamp()         );
        fhopehs.setClaim_user_id(eqpMonitorCountEventRecord.getEventCommon().getUserID()                 );
        fhopehs.setClaim_memo(eqpMonitorCountEventRecord.getEventCommon().getEventMemo()              );
        fhopehs.setEvent_create_time(eqpMonitorCountEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhopehs.setPd_type(pdData.getPd_type() );
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord(): InsertLotOperationHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord Function");
            return( iRc );
        }
        for( int i = 0; i < length(eqpMonitorCountEventRecord.getWafers()); i++ ) {
            fhopehs_emucnt = new Infos.Ohopehs_emucnt();
            fhopehs_emucnt.setLot_id(eqpMonitorCountEventRecord.getLotData().getLotID() );
            fhopehs_emucnt.setMainpd_id(eqpMonitorCountEventRecord.getLotData().getRouteID() );
            fhopehs_emucnt.setOpe_no(eqpMonitorCountEventRecord.getLotData().getOperationNumber() );
            fhopehs_emucnt.setOpe_pass_count(eqpMonitorCountEventRecord.getLotData().getOperationPassCount());
            fhopehs_emucnt.setClaim_time(eqpMonitorCountEventRecord.getEventCommon().getEventTimeStamp() );
            fhopehs_emucnt.setOpe_category(SP_OPERATIONCATEGORY_EQPMONUSEDCNTUPDATE );
            fhopehs_emucnt.setWafer_id(eqpMonitorCountEventRecord.getWafers().get(i).getWaferID() );
            fhopehs_emucnt.setEqpmonused_count(eqpMonitorCountEventRecord.getWafers().get(i).getEqpMonitorUsedCount()==null?
                    null:eqpMonitorCountEventRecord.getWafers().get(i).getEqpMonitorUsedCount().intValue());
            iRc = eqpMonUsedCountHistoryService.insertLotOperationEqpMonUsedCntHistory( fhopehs_emucnt );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord(): InsertLotOperationEqpMonUsedCntHistory SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateEqpMonUsedCountRecord Function");
        return( returnOK() );
    }

}
