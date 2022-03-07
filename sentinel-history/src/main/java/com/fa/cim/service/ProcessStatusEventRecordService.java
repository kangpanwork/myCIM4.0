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
import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;
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
 * @date 2019/6/6 16:48
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProcessStatusEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private WaferChamberProcessHistoryService waferChamberProcessHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processStatusEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/2 14:49
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createProcessStatusEventRecord( Infos.ProcessStatusEventRecord processStatusEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs      fhopehs= new Infos.Ohopehs();
        Infos.Frpd         resultData_pd= new Infos.Frpd();
        Infos.Frlrcp       resultData_lrcp= new Infos.Frlrcp();
        Infos.Frlot        resultData_lot= new Infos.Frlot();
        Infos.Frpos        resultData_pos= new Infos.Frpos();
        Params.String                castCategory   = new Params.String();
        Params.String                productGrpID   = new Params.String();
        Params.String                prodType       = new Params.String();
        Params.String                techID         = new Params.String();
        Params.String                custprodID     = new Params.String();
        Params.String                stageGrpID     = new Params.String();
        Params.String                areaID         = new Params.String();
        Params.String                eqpName        = new Params.String();
        Params.String                locationID     = new Params.String();
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
        iRc = tableMethod.getFRCAST( processStatusEventRecord.getLotData().getCassetteID() , castCategory ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPD( processStatusEventRecord.getLotData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRLRCP( processStatusEventRecord.getLogicalRecipeID() , resultData_lrcp ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( processStatusEventRecord.getLotData().getProductID(), productGrpID, prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( processStatusEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( processStatusEventRecord.getLotData().getProductID() , techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( processStatusEventRecord.getLotData().getLotID(), processStatusEventRecord.getLotData().getProductID() , custprodID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( processStatusEventRecord.getLotData().getObjrefPOS() ,                    processStatusEventRecord.getLotData().getOperationNumber(),                    processStatusEventRecord.getLotData().getObjrefMainPF(),                    resultData_pos ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        resultData_pos.setOperationNO(processStatusEventRecord.getLotData().getOperationNumber() );
        resultData_pos.setPdID(processStatusEventRecord.getLotData().getOperationID()     );
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFREQP( processStatusEventRecord.getEquipmentID() , areaID , eqpName );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRAREA( areaID ,locationID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        fhopehs = new Infos.Ohopehs();
        fhopehs.setLot_id(processStatusEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type(processStatusEventRecord.getLotData().getLotType() );
        fhopehs.setSub_lot_type(resultData_lot.getSubLotType() );
        fhopehs.setCast_id(processStatusEventRecord.getLotData().getCassetteID());
        fhopehs.setCast_category(castCategory.getValue() );
        fhopehs.setMainpd_id(processStatusEventRecord.getLotData().getRouteID() );
        fhopehs.setOpe_no(processStatusEventRecord.getLotData().getOperationNumber() );
        fhopehs.setPd_id(processStatusEventRecord.getLotData().getOperationID() );
        fhopehs.setOpe_pass_count(processStatusEventRecord.getLotData().getOperationPassCount() );
        fhopehs.setPd_name(resultData_pd.getOperationName() );
        fhopehs.setHold_state(processStatusEventRecord.getLotData().getHoldState()) ;
        fhopehs.setClaim_time(processStatusEventRecord.getEventCommon().getEventTimeStamp() );
        fhopehs.setClaim_shop_date(processStatusEventRecord.getEventCommon().getEventShopDate() );
        fhopehs.setClaim_user_id(processStatusEventRecord.getEventCommon().getUserID());
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
        if( variableStrCmp( processStatusEventRecord.getActionCode(), SP_PCSTACTIONCODE_PROCESSSTART ) == 0 ) {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PROCESSSTART );
        }else{
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_PROCESSEND );
        }
        fhopehs.setProd_type(prodType.getValue() );
        fhopehs.setTest_type(resultData_lrcp.getTest_type() );
        fhopehs.setMfg_layer(resultData_lot.getMfgLayer() );
        fhopehs.setExt_priority(resultData_lot.getPriority() );
        fhopehs.setPriority_class(resultData_lot.getPriorityClass() );
        fhopehs.setProdspec_id(processStatusEventRecord.getLotData().getProductID());
        fhopehs.setProdgrp_id(productGrpID.getValue()  );
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(resultData_lot.getCustomerID() );
        fhopehs.setCustprod_id(custprodID.getValue()  );
        fhopehs.setOrder_no(resultData_lot.getOrderNO() );
        fhopehs.setStage_id(resultData_pos.getStageID() );
        fhopehs.setStagegrp_id(stageGrpID.getValue() );
        fhopehs.setPhoto_layer(resultData_pos.getPhotoLayer() );
        fhopehs.setLocation_id(locationID.getValue() );
        fhopehs.setArea_id(areaID.getValue() );
        fhopehs.setEqp_id(processStatusEventRecord.getEquipmentID() );
        fhopehs.setEqp_name(eqpName.getValue() ) ;
        fhopehs.setOpe_mode(processStatusEventRecord.getOperationMode() );
        fhopehs.setLc_recipe_id(processStatusEventRecord.getLogicalRecipeID() );
        fhopehs.setRecipe_id(processStatusEventRecord.getMachineRecipeID() );
        fhopehs.setPh_recipe_id(processStatusEventRecord.getPhysicalRecipeID() );
        fhopehs.setReticle_count(0 );
        fhopehs.setFixture_count(0 );
        fhopehs.setRparm_count(0 );
        fhopehs.setInit_hold_flag(0 );
        fhopehs.setLast_hldrel_flag(0 );
        fhopehs.setHold_time("1901-01-01-00.00.00.000000") ;
        fhopehs.setHold_shop_date(0.0 );
        fhopehs.setPrev_pass_count(0 );
        fhopehs.setFlowbatch_id(processStatusEventRecord.getBatchID() );
        fhopehs.setCtrl_job(processStatusEventRecord.getControlJobID() );
        fhopehs.setRework_count(0 );
        fhopehs.setOrg_wafer_qty(processStatusEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty(processStatusEventRecord.getLotData().getCurrentWaferQuantity() );
        fhopehs.setProd_wafer_qty(processStatusEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setCntl_wafer_qty(processStatusEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setClaim_prod_qty(processStatusEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setClaim_cntl_qty(processStatusEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setTotal_good_unit(0 );
        fhopehs.setTotal_fail_unit(0 );
        fhopehs.setLot_owner_id(resultData_lot.getLotOwner() );
        fhopehs.setPlan_end_time(resultData_lot.getPlanEndTime() );
        fhopehs.setWfrhs_time(processStatusEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setCriteria_flag(CRITERIA_NA);
        fhopehs.setClaim_memo(processStatusEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setEvent_create_time(processStatusEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhopehs.setPd_type(resultData_pd.getPd_type() );
        iRc = lotOperationHistoryService.insertLotOperationHistory(fhopehs);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord(): InsertLotOperationHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateProcessStatusEventRecord Function");
        return(returnOK());
    }

}
