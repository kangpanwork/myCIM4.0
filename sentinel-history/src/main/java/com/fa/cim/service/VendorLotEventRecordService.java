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
 * @date 2019/6/28 13:03
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class VendorLotEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private VendorLotHistoryService vendorLotHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createVendorLotEventRecord( Infos.VendorLotEventRecord vendorLotEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
        Response iRc = returnOK();
        Infos.Ohopehs  fhopehs= new Infos.Ohopehs();
        Infos.Frlot    lotData= new Infos.Frlot();
        Params.String            prodGrpID = new Params.String();
        Params.String            prodType = new Params.String();
        Params.String            techID = new Params.String();
        Params.String            custProdID = new Params.String();
        Params.String            stockerID = new Params.String();
        Params.String            stkName = new Params.String();
        Params.String            areaID = new Params.String();
        Params.String            locationID = new Params.String();
        Infos.Ohvltrhs fhvltrhs= new Infos.Ohvltrhs();
        iRc = tableMethod.getFRLOT(      vendorLotEventRecord.getLotData().getLotID(),     lotData );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( vendorLotEventRecord.getLotData().getProductID(), prodGrpID,prodType );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(  vendorLotEventRecord.getLotData().getProductID(), techID );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }


        iRc = tableMethod.getFRCUSTPROD( vendorLotEventRecord.getLotData().getLotID(),
                vendorLotEventRecord.getLotData().getProductID(),                   custProdID );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRBANK(     vendorLotEventRecord.getLotData().getBankID(),    stockerID );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTK(      stockerID,                               areaID,   stkName );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRAREA(     areaID,                                  locationID );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return( iRc );
        }
        fhopehs = new Infos.Ohopehs();
        fhopehs.setLot_id(vendorLotEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type(vendorLotEventRecord.getLotData().getLotType() ) ;
        fhopehs.setSub_lot_type(lotData.getSubLotType() ) ;
        fhopehs.setOpe_pass_count(0);
        fhopehs.setClaim_time(vendorLotEventRecord.getEventCommon().getEventTimeStamp() ) ;
        fhopehs.setClaim_shop_date(vendorLotEventRecord.getEventCommon().getEventShopDate());
        fhopehs.setClaim_user_id(vendorLotEventRecord.getEventCommon().getUserID() );
        fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
        if( variableStrCmp( vendorLotEventRecord.getEventCommon().getTransactionID(), OBNKW001_ID) == 0 ||
                variableStrCmp( vendorLotEventRecord.getEventCommon().getTransactionID(), OBNKW004_ID) == 0 )
        {
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_VENDORLOTRECEIVE );
        }else{
            fhopehs.setOpe_category(SP_OPERATIONCATEGORY_VENDORLOTRETURN );
        }
        fhopehs.setProd_type(prodType.getValue() );
        fhopehs.setMfg_layer(lotData.getMfgLayer() );
        fhopehs.setExt_priority(lotData.getPriority());
        fhopehs.setPriority_class(lotData.getPriorityClass());
        fhopehs.setProdspec_id(vendorLotEventRecord.getLotData().getProductID() );
        fhopehs.setProdgrp_id(prodGrpID.getValue() );
        fhopehs.setTech_id(techID.getValue() );
        fhopehs.setCustomer_id(lotData.getCustomerID() );
        fhopehs.setCustprod_id(custProdID.getValue() );
        fhopehs.setOrder_no(lotData.getOrderNO() );
        fhopehs.setLocation_id(locationID.getValue() );
        fhopehs.setArea_id(areaID.getValue() );
        fhopehs.setReticle_count(0);
        fhopehs.setFixture_count(0);
        fhopehs.setRparm_count(0);
        fhopehs.setInit_hold_flag(0);
        fhopehs.setLast_hldrel_flag(0);
        fhopehs.setHold_state(vendorLotEventRecord.getLotData().getHoldState() );
        fhopehs.setHold_time("1901-01-01-00.00.00.000000");
        fhopehs.setBank_id(vendorLotEventRecord.getLotData().getBankID() );
        fhopehs.setPrev_pass_count(0);
        fhopehs.setRework_count(0);
        fhopehs.setOrg_wafer_qty(vendorLotEventRecord.getLotData().getOriginalWaferQuantity());
        fhopehs.setCur_wafer_qty(vendorLotEventRecord.getLotData().getCurrentWaferQuantity());
        fhopehs.setProd_wafer_qty(vendorLotEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setCntl_wafer_qty(vendorLotEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setClaim_prod_qty(vendorLotEventRecord.getLotData().getProductWaferQuantity());
        fhopehs.setClaim_cntl_qty(vendorLotEventRecord.getLotData().getControlWaferQuantity());
        fhopehs.setTotal_good_unit(0);
        fhopehs.setTotal_fail_unit(0);
        fhopehs.setLot_owner_id(lotData.getLotOwner() );
        fhopehs.setPlan_end_time(lotData.getPlanEndTime() );
        fhopehs.setWfrhs_time(vendorLotEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setClaim_memo(vendorLotEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setCriteria_flag(CRITERIA_NA);
        fhopehs.setEvent_create_time(vendorLotEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs ) ;
        if ( !isOk(iRc) ) {

            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord(): InsertLotOperationHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return ( iRc );
        }
        fhvltrhs = new Infos.Ohvltrhs();
        fhvltrhs.setLot_id(vendorLotEventRecord.getLotData().getLotID() );
        fhvltrhs.setLot_type(vendorLotEventRecord.getLotData().getLotType() ) ;
        fhvltrhs.setVendor_lot_id(vendorLotEventRecord.getVendorLotID() ) ;
        fhvltrhs.setVendor_name(lotData.getVendor_name() ) ;
        fhvltrhs.setProd_type(prodType.getValue() );
        fhvltrhs.setProdspec_id(vendorLotEventRecord.getLotData().getProductID() );
        fhvltrhs.setProdgrp_id(prodGrpID.getValue() );
        fhvltrhs.setTech_id(techID.getValue() );
        fhvltrhs.setWafer_qty(vendorLotEventRecord.getClaimQuantity()==null?
                null:vendorLotEventRecord.getClaimQuantity().intValue());
        fhvltrhs.setClaim_time(vendorLotEventRecord.getEventCommon().getEventTimeStamp() ) ;
        fhvltrhs.setClaim_shop_date(vendorLotEventRecord.getEventCommon().getEventShopDate());
        fhvltrhs.setClaim_user_id(vendorLotEventRecord.getEventCommon().getUserID() );
        fhvltrhs.setClaim_memo(vendorLotEventRecord.getEventCommon().getEventMemo() );
        fhvltrhs.setEvent_create_time(vendorLotEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = vendorLotHistoryService.insertVendorLotReceiveHistory( fhvltrhs ) ;
        if ( !isOk(iRc) ) {

            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord(): InsertVendorLotReceiveHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
            return ( iRc );
        }
        log.info("HistoryWatchDogServer::CreateVendorLotEventRecord Function");
        return( returnOK() );
    }

}
