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
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.Constant.TransactionConstant.ODRBW025_ID;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

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
public class LotWaferScrapEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotWaferScrapHistoryService lotWaferScrapHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    public Response createLotWaferScrapEventRecord(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

        if((variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW042_ID ) == 0)
                || (variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW030_ID ) == 0) ) {



            iRc = createFHOPEHS_TxScrapWaferNotOnRoute( lotWaferScrapEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

                return ( iRc );
            }
        }else{



            iRc = createFHOPEHS_TxScrapWafer( lotWaferScrapEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

                return ( iRc );
            }
        }





        if( (variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW042_ID ) != 0)
                && (variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW030_ID ) != 0) ) {



            iRc = createFHSCRHS( lotWaferScrapEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

                return ( iRc );
            }
        }







        if( variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW043_ID ) == 0 ) {
            iRc = createFHWLTHS_Scrap( lotWaferScrapEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

                return ( iRc );
            }

        } else if( variableStrCmp(lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW041_ID ) == 0 ){
            iRc = createFHWLTHS_ScrapCancel( lotWaferScrapEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );

                return ( iRc );
            }
        }

        log.info("HistoryWatchDogServer::CreateLotWaferScrapEventRecord Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 15:05
     */
    public Response createFHOPEHS_TxScrapWaferNotOnRoute(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs        fhopehs ;
        Infos.Frpd           resultData_pd;
        Infos.Frlot          resultData_lot = new Infos.Frlot();
        Infos.Frpos          resultData_pos ;
        Params.String castCategory = new Params.String() ;
        Params.String productGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custprodID = new Params.String();
        Params.String stageGrpID = new Params.String();
        int                   i;
        Params.Param<Long>           productWaferQuantity = new Params.Param<>(0L) ;
        Params.Param<Long>           controlWaferQuantity = new Params.Param<>(0L) ;
        Params.String codeCategory = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
        iRc = tableMethod.getFRLOT( lotWaferScrapEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotWaferScrapEventRecord.getLotData().getCassetteID() , castCategory ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotWaferScrapEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotWaferScrapEventRecord.getLotData().getProductID() , techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferScrapEventRecord.getLotData().getLotID(),lotWaferScrapEventRecord.getLotData().getProductID() , custprodID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCODE( lotWaferScrapEventRecord.getScrapWafers().get(0).getReasonCodeID() , codeCategory );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }
        for( i = 0; i < length(lotWaferScrapEventRecord.getScrapWafers()); i++ ) {
            if(!Objects.equals(lotWaferScrapEventRecord.getScrapWafers().get(i).getControlWaferFlag(), TRUE)) {
                productWaferQuantity.setValue(productWaferQuantity.getValue()+1);
            }else{
                controlWaferQuantity.setValue(controlWaferQuantity.getValue()+1);
            }
        }
        fhopehs =new Infos.Ohopehs();
        fhopehs.setLot_id (            lotWaferScrapEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type (          lotWaferScrapEventRecord.getLotData().getLotType() );
        fhopehs.setSub_lot_type (      resultData_lot.getSubLotType() );
        fhopehs.setOpe_pass_count    ( 0 );
        fhopehs.setClaim_time (        lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() );
        fhopehs.setClaim_shop_date   ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
        fhopehs.setClaim_user_id (     lotWaferScrapEventRecord.getEventCommon().getUserID() );
        fhopehs.setMove_type (         SP_MOVEMENTTYPE_NONMOVE );

        if( variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID(), OLOTW042_ID ) == 0 ) {
            fhopehs.setOpe_category(  SP_OPERATIONCATEGORY_WAFERSCRAP );
        }
        else
        {
            fhopehs.setOpe_category(  SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL );
        }

        fhopehs.setProd_type (         prodType.getValue() );
        fhopehs.setMfg_layer (         resultData_lot.getMfgLayer() );
        fhopehs.setExt_priority      ( resultData_lot.getPriority() );
        fhopehs.setPriority_class    ( resultData_lot.getPriorityClass() );
        fhopehs.setProdspec_id (       lotWaferScrapEventRecord.getLotData().getProductID() );
        fhopehs.setProdgrp_id  (       productGrpID.getValue()  );
        fhopehs.setTech_id (           techID.getValue() );
        fhopehs.setCustomer_id (       resultData_lot.getCustomerID() );
        fhopehs.setCustprod_id (       custprodID.getValue() );
        fhopehs.setOrder_no (          resultData_lot.getOrderNO() );
        fhopehs.setReticle_count     ( 0 );
        fhopehs.setFixture_count     ( 0 );
        fhopehs.setRparm_count       ( 0 );
        fhopehs.setInit_hold_flag    ( 0 );
        fhopehs.setLast_hldrel_flag  ( 0 );
        fhopehs.setHold_state (        lotWaferScrapEventRecord.getLotData().getHoldState() );
        fhopehs.setHold_time (         "1901-01-01-00.00.00.000000" );
        fhopehs.setBank_id (           lotWaferScrapEventRecord.getLotData().getBankID() );
        fhopehs.setPrev_pass_count   ( 0 );
        fhopehs.setRework_count      ( 0 );
        fhopehs.setOrg_wafer_qty     ( lotWaferScrapEventRecord.getLotData().getOriginalWaferQuantity() );
        fhopehs.setCur_wafer_qty     ( lotWaferScrapEventRecord.getLotData().getCurrentWaferQuantity() );
        fhopehs.setProd_wafer_qty    ( lotWaferScrapEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setCntl_wafer_qty    ( lotWaferScrapEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setClaim_prod_qty    ( productWaferQuantity.getValue()==null?null:
                productWaferQuantity.getValue().intValue());
        fhopehs.setClaim_cntl_qty    ( controlWaferQuantity.getValue()==null?null:
                controlWaferQuantity.getValue().intValue());
        fhopehs.setTotal_good_unit   ( 0 );
        fhopehs.setTotal_fail_unit   ( 0 );
        fhopehs.setLot_owner_id(       resultData_lot.getLotOwner() );
        fhopehs.setPlan_end_time (     resultData_lot.getPlanEndTime() );
        fhopehs.setWfrhs_time (        lotWaferScrapEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setClaim_memo (        lotWaferScrapEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setReason_code (       lotWaferScrapEventRecord.getScrapWafers().get(0).getReasonCodeID().getIdentifier() );
        fhopehs.setReason_description ( codeCategory.getValue() );
        fhopehs.setCriteria_flag     ( CRITERIA_NA);
        fhopehs.setEvent_create_time(  lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafernotOnRoute(): InsertLotOperationHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWaferNotOnRoute Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 15:05
     */
    public Response createFHOPEHS_TxScrapWafer(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohopehs        fhopehs ;
        Infos.Frpd           resultData_pd;
        Infos.Frlot          resultData_lot;
        Infos.Frpos          resultData_pos = new Infos.Frpos();
        Params.String castCategory = new Params.String() ;
        Params.String productGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custprodID = new Params.String();
        Params.String stageGrpID = new Params.String();
        int                   i;
        Params.Param<Long>           productWaferQuantity = new Params.Param<>(0L) ;
        Params.Param<Long>           controlWaferQuantity = new Params.Param<>(0L) ;
        Params.String codeCategory = new Params.String();
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
        resultData_lot=new Infos.Frlot();
        iRc = tableMethod.getFRLOT( lotWaferScrapEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCAST( lotWaferScrapEventRecord.getLotData().getCassetteID() , castCategory ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        resultData_pd=new Infos.Frpd();
        iRc = tableMethod.getFRPD( lotWaferScrapEventRecord.getLotData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC( lotWaferScrapEventRecord.getLotData().getProductID() ,productGrpID , prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( lotWaferScrapEventRecord.getLotData().getProductID() , techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferScrapEventRecord.getLotData().getLotID(),lotWaferScrapEventRecord.getLotData().getProductID() , custprodID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPOS( lotWaferScrapEventRecord.getLotData().getObjrefPOS() ,
                lotWaferScrapEventRecord.getLotData().getOperationNumber(),
                lotWaferScrapEventRecord.getLotData().getObjrefMainPF(),
                resultData_pos ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        resultData_pos.setOperationNO     (            lotWaferScrapEventRecord.getLotData().getOperationNumber() );
        resultData_pos.setPdID            (            lotWaferScrapEventRecord.getLotData().getOperationID()     );
        iRc = tableMethod.getFRSTAGE( resultData_pos.getStageID() , stageGrpID ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }
        for( i = 0; i < length(lotWaferScrapEventRecord.getScrapWafers()); i++ ) {
            if(!Objects.equals(lotWaferScrapEventRecord.getScrapWafers().get(i).getControlWaferFlag(), TRUE)) {
                productWaferQuantity.setValue(productWaferQuantity.getValue()+1) ;
            }else{
                controlWaferQuantity.setValue(controlWaferQuantity.getValue()+1) ;
            }
        }
        fhopehs =new Infos.Ohopehs();
        fhopehs.setLot_id (            lotWaferScrapEventRecord.getLotData().getLotID() );
        fhopehs.setLot_type (          lotWaferScrapEventRecord.getLotData().getLotType() );
        fhopehs.setSub_lot_type (      resultData_lot.getSubLotType() );
        fhopehs.setCast_id (           lotWaferScrapEventRecord.getLotData().getCassetteID() );
        fhopehs.setCast_category (     castCategory.getValue() );
        fhopehs.setMainpd_id (         lotWaferScrapEventRecord.getLotData().getRouteID() );
        fhopehs.setOpe_no (            lotWaferScrapEventRecord.getLotData().getOperationNumber() );
        fhopehs.setPd_id (             lotWaferScrapEventRecord.getLotData().getOperationID() );
        fhopehs.setOpe_pass_count    ( lotWaferScrapEventRecord.getLotData().getOperationPassCount() );
        fhopehs.setPd_name (           resultData_pd.getOperationName() );
        fhopehs.setClaim_time (        lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() );
        fhopehs.setClaim_shop_date   ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
        fhopehs.setClaim_user_id (     lotWaferScrapEventRecord.getEventCommon().getUserID() );
        fhopehs.setMove_type (         SP_MOVEMENTTYPE_NONMOVE );

        if( variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID(), OLOTW043_ID ) == 0 ) {
            fhopehs.setOpe_category(  SP_OPERATIONCATEGORY_WAFERSCRAP );
        }
        else if( variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID(), OLOTW041_ID ) == 0 ) {
            fhopehs.setOpe_category(  SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL );
        }else{
            fhopehs.setOpe_category(  SP_OPERATIONCATEGORY_DIEADJUST );
        }

        fhopehs.setProd_type (         prodType.getValue() );
        fhopehs.setTest_type (         resultData_pd.getTestType() );
        fhopehs.setMfg_layer (         resultData_lot.getMfgLayer() );
        fhopehs.setExt_priority      ( resultData_lot.getPriority() );
        fhopehs.setPriority_class    ( resultData_lot.getPriorityClass() );
        fhopehs.setProdspec_id (       lotWaferScrapEventRecord.getLotData().getProductID() );
        fhopehs.setProdgrp_id  (       productGrpID.getValue()  );
        fhopehs.setTech_id (           techID.getValue() );
        fhopehs.setCustomer_id (       resultData_lot.getCustomerID() );
        fhopehs.setCustprod_id (       custprodID.getValue() );
        fhopehs.setOrder_no (          resultData_lot.getOrderNO() );
        fhopehs.setStage_id (          resultData_pos.getStageID() );
        fhopehs.setStagegrp_id (       stageGrpID.getValue() );
        fhopehs.setPhoto_layer (       resultData_pos.getPhotoLayer() );
        fhopehs.setReticle_count     ( 0 );
        fhopehs.setFixture_count     ( 0 );
        fhopehs.setRparm_count       ( 0 );
        fhopehs.setInit_hold_flag    ( 0 );
        fhopehs.setLast_hldrel_flag  ( 0 );
        fhopehs.setHold_state (        lotWaferScrapEventRecord.getLotData().getHoldState() );
        fhopehs.setHold_time (         "1901-01-01-00.00.00.000000" );
        fhopehs.setBank_id (           lotWaferScrapEventRecord.getLotData().getBankID() );
        fhopehs.setPrev_pass_count   ( 0 );
        fhopehs.setRework_count      ( 0 );
        fhopehs.setOrg_wafer_qty     ( lotWaferScrapEventRecord.getLotData().getOriginalWaferQuantity() );
        fhopehs.setCur_wafer_qty     ( lotWaferScrapEventRecord.getLotData().getCurrentWaferQuantity() );
        fhopehs.setProd_wafer_qty    ( lotWaferScrapEventRecord.getLotData().getProductWaferQuantity() );
        fhopehs.setCntl_wafer_qty    ( lotWaferScrapEventRecord.getLotData().getControlWaferQuantity() );
        fhopehs.setClaim_prod_qty    ( productWaferQuantity.getValue()==null?null:
                productWaferQuantity.getValue().intValue());
        fhopehs.setClaim_cntl_qty    ( controlWaferQuantity.getValue()==null?null:
                controlWaferQuantity.getValue().intValue());
        fhopehs.setTotal_good_unit   ( 0 );
        fhopehs.setTotal_fail_unit   ( 0 );
        fhopehs.setLot_owner_id(       resultData_lot.getLotOwner() );
        fhopehs.setPlan_end_time (     resultData_lot.getPlanEndTime() );
        fhopehs.setWfrhs_time (        lotWaferScrapEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhopehs.setClaim_memo (        lotWaferScrapEventRecord.getEventCommon().getEventMemo() );
        fhopehs.setCriteria_flag     ( CRITERIA_NA);
        fhopehs.setEvent_create_time(  lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhopehs.setPd_type(	         resultData_pd.getPd_type() );
        iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer(): InsertLotOperationHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::createFHOPEHS_TxScrapWafer Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 15:10
     */
    public Response createFHWLTHS_ScrapCancel(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths      fhwlths ;
        Infos.Frwafer      resultData  ;
        Params.String castCategory = new Params.String() ;
        Params.String codeCategory = new Params.String() ;
        int                 i;
        int                 j;
        Boolean scrap_flag;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );
        iRc = tableMethod.getFRCAST( lotWaferScrapEventRecord.getLotData().getCassetteID(),castCategory);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );
            return( iRc );
        }
        for(i = 0 ; i < length(lotWaferScrapEventRecord.getCurrentWafers()); i++) {

            resultData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferScrapEventRecord.getCurrentWafers().get(i).getWaferID(),resultData );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );
                return( iRc );
            }
            scrap_flag = FALSE;
            for( j = 0 ; j < length(lotWaferScrapEventRecord.getScrapWafers()); j++ ) {
                if( variableStrCmp( lotWaferScrapEventRecord.getCurrentWafers().get(i).getWaferID() , lotWaferScrapEventRecord.getScrapWafers().get(j).getWaferID())== 0 ) {

                    iRc = tableMethod.getFRCODE( lotWaferScrapEventRecord.getScrapWafers().get(j).getReasonCodeID() , codeCategory );
                    if( !isOk(iRc) ) {
                        log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );
                        return( iRc );
                    }
                    scrap_flag = TRUE;
                    break;
                }
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id (              lotWaferScrapEventRecord.getCurrentWafers().get(i).getWaferID() ) ;
            fhwlths.setCur_lot_id (            lotWaferScrapEventRecord.getLotData().getLotID() ) ;
            fhwlths.setCur_cast_id (           lotWaferScrapEventRecord.getLotData().getCassetteID() ) ;
            fhwlths.setCur_cast_category (     castCategory.getValue() );
            fhwlths.setCur_cast_slot_no      ( lotWaferScrapEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber()==null?null:
                    lotWaferScrapEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber().intValue());
            fhwlths.setClaim_user_id (         lotWaferScrapEventRecord.getEventCommon().getUserID() ) ;
            fhwlths.setClaim_time (            lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() ) ;
            fhwlths.setClaim_shop_date       ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
            fhwlths.setOpe_category (          SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL ) ;
            if(Objects.equals(scrap_flag, TRUE)) {
                fhwlths.setApply_wafer_flag  ( "Y" ) ;
            }else{
                fhwlths.setApply_wafer_flag  ( "N" ) ;
            }
            fhwlths.setProdspec_id (           lotWaferScrapEventRecord.getLotData().getProductID() );
            fhwlths.setGood_unit_count       ( resultData.getGoodDiceQty() );
            fhwlths.setRepair_unit_count     ( resultData.getRepairedDiceQty() );
            fhwlths.setFail_unit_count       ( resultData.getBadDiceQty() );
            fhwlths.setExist_flag (            "Y" ) ;
            if(Objects.equals(lotWaferScrapEventRecord.getCurrentWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer      ( TRUE );
            }else{
                fhwlths.setControl_wafer      ( FALSE );
            }
            fhwlths.setPrev_cast_slot_no      ( 0 );
            if(Objects.equals(scrap_flag, TRUE)) {
                fhwlths.setReason_code (        lotWaferScrapEventRecord.getScrapWafers().get(j).getReasonCodeID().getIdentifier() ) ;
                fhwlths.setReason_description(  codeCategory.getValue() ) ;
            }

            fhwlths.setAlias_wafer_name(        resultData.getAlias_wafer_name() ) ;
            fhwlths.setEvent_create_time(       lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel(): InsertLotWaferHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::createFHWLTHS_ScrapCancel Function" );

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/23 15:26
     */
    public Response createFHSCRHS(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohscrhs  fhscrhs;
        Infos.Ohscrhs  fhscrhs2=new Infos.Ohscrhs();
        Infos.Frlot    lotData;
        Infos.Frpd     pdData;
        Infos.Frpd     pdData_reason;
        Infos.Frpos    Data;
        Infos.Frpos    Data_reason;
        Infos.Frwafer  waferData;
        Params.String codeDescription = new Params.String();
        Params.String prodGrpID = new Params.String();
        Params.String prodType = new Params.String();
        Params.String techID = new Params.String();
        Params.String custProdID = new Params.String();
        Params.String eqpID = new Params.String();
        Params.String eqpName = new Params.String();
        Params.String areaID = new Params.String();
        Params.String stageGrpID = new Params.String();
        Params.String stageGrpID_reason = new Params.String();
        Params.String locationID = new Params.String();
        Params.String castCategory = new Params.String();
        int             i;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHSCRHS Function" );
        lotData=new Infos.Frlot();
        iRc = tableMethod.getFRLOT( lotWaferScrapEventRecord.getLotData().getLotID(), lotData );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODSPEC(lotWaferScrapEventRecord.getLotData().getProductID(), prodGrpID, prodType );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP(lotWaferScrapEventRecord.getLotData().getProductID(), techID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCAST(lotWaferScrapEventRecord.getLotData().getCassetteID(), castCategory );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRCUSTPROD( lotWaferScrapEventRecord.getLotData().getLotID(), lotWaferScrapEventRecord.getLotData().getProductID(), custProdID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        pdData=new Infos.Frpd();
        iRc = tableMethod.getFRPD( lotWaferScrapEventRecord.getLotData().getOperationID(), pdData );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        pdData_reason=new Infos.Frpd();
        iRc = tableMethod.getFRPD( lotWaferScrapEventRecord.getReasonOperationID(), pdData_reason );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        Data=new Infos.Frpos();
        iRc = tableMethod.getFRPOS( lotWaferScrapEventRecord.getLotData().getObjrefPOS() ,
                lotWaferScrapEventRecord.getLotData().getOperationNumber(),
                lotWaferScrapEventRecord.getLotData().getObjrefMainPF(),
                Data ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }

        Data.setOperationNO     (            lotWaferScrapEventRecord.getLotData().getOperationNumber() );
        Data.setPdID            (            lotWaferScrapEventRecord.getLotData().getOperationID()     );
        Data_reason=new Infos.Frpos();
        iRc = tableMethod.getFRPOS_Abnormal( lotWaferScrapEventRecord.getLotData().getLotID(),
                lotWaferScrapEventRecord.getReasonRouteID(),
                lotWaferScrapEventRecord.getReasonOperationNumber(),
                lotWaferScrapEventRecord.getReasonOperationPassCount(),
                Data_reason );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRPO( lotWaferScrapEventRecord.getLotData().getLotID(),
                lotWaferScrapEventRecord.getReasonRouteID(),
                lotWaferScrapEventRecord.getReasonOperationNumber(),
                lotWaferScrapEventRecord.getReasonOperationPassCount(),
                eqpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFREQP( eqpID.getValue(), areaID, eqpName );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRAREA(areaID, locationID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE(Data.getStageID(),        stageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE(Data_reason.getStageID(), stageGrpID_reason );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHSCRHS Function" );
            return( iRc );
        }

        for(i = 0; i < length(lotWaferScrapEventRecord.getScrapWafers()); i++) {
            iRc = tableMethod.getFRCODE( lotWaferScrapEventRecord.getScrapWafers().get(i).getReasonCodeID(), codeDescription );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHSCRHS Function" );
                return( iRc );
            }
            waferData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferScrapEventRecord.getScrapWafers().get(i).getWaferID(), waferData );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHSCRHS Function" );
                return( iRc );
            }
            if(variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID(),OLOTW041_ID ) == 0 ) {

                fhscrhs2=new Infos.Ohscrhs();
                iRc = tableMethod.getFHSCRHS(lotWaferScrapEventRecord.getScrapWafers().get(i).getWaferID(), fhscrhs2 );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createFHSCRHS Function" );
                    return( iRc );
                }
            }
            fhscrhs=new Infos.Ohscrhs();
            fhscrhs.setLot_id             ( lotWaferScrapEventRecord.getLotData().getLotID() );
            fhscrhs.setWafer_id           ( lotWaferScrapEventRecord.getScrapWafers().get(i).getWaferID() );
            fhscrhs.setReason_code        ( lotWaferScrapEventRecord.getScrapWafers().get(i).getReasonCodeID().getIdentifier() );
            fhscrhs.setReason_description ( codeDescription.getValue() );
            fhscrhs.setScrap_unit_count   ( 1);
            fhscrhs.setProdspec_id        ( lotWaferScrapEventRecord.getLotData().getProductID() );
            fhscrhs.setLot_owner_id       ( lotData.getLotOwner() );
            fhscrhs.setProdgrp_id         ( prodGrpID.getValue() );
            fhscrhs.setTech_id            ( techID.getValue() );
            fhscrhs.setCustprod_id        ( custProdID.getValue() );
            fhscrhs.setOrder_no           ( lotData.getOrderNO() );
            fhscrhs.setCustomer_id        ( lotData.getCustomerID() );
            if(Objects.equals(lotWaferScrapEventRecord.getScrapWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhscrhs.setControl_wafer  ( 1 );
            }else{
                fhscrhs.setControl_wafer  ( 0 );
            }
            fhscrhs.setGood_unit_wafer    ( waferData.getGoodDiceQty() );
            fhscrhs.setRepair_unit_wafer  ( waferData.getRepairedDiceQty() );
            fhscrhs.setFail_unit_wafer    ( waferData.getBadDiceQty() );
            fhscrhs.setLot_type           ( lotWaferScrapEventRecord.getLotData().getLotType() );
            fhscrhs.setCast_id            ( lotWaferScrapEventRecord.getLotData().getCassetteID() );
            fhscrhs.setCast_category      ( castCategory.getValue() );
            fhscrhs.setProd_type          ( prodType.getValue() );
            fhscrhs.setClaim_mainpd_id    ( lotWaferScrapEventRecord.getLotData().getRouteID() );
            fhscrhs.setClaim_ope_no       ( lotWaferScrapEventRecord.getLotData().getOperationNumber() );
            fhscrhs.setClaim_pd_id        ( lotWaferScrapEventRecord.getLotData().getOperationID() );

            fhscrhs.setClaim_pass_count   ( lotWaferScrapEventRecord.getLotData().getOperationPassCount());
            fhscrhs.setClaim_ope_name     ( pdData.getOperationName() );
            fhscrhs.setClaim_test_type    ( pdData.getTestType() );
            fhscrhs.setClaim_time         ( lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() );

            fhscrhs.setClaim_shop_date    ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
            fhscrhs.setClaim_user_id      ( lotWaferScrapEventRecord.getEventCommon().getUserID() );
            fhscrhs.setClaim_stage_id     ( Data.getStageID() );
            fhscrhs.setClaim_stagegrp_id  ( stageGrpID.getValue() );
            fhscrhs.setClaim_photo_layer  ( Data.getPhotoLayer() );
            fhscrhs.setClaim_department   ( pdData.getDepartment() );
            fhscrhs.setClaim_bank_id      ( lotWaferScrapEventRecord.getLotData().getBankID() );
            if(variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID(), OLOTW043_ID) == 0  ) {
                fhscrhs.setReason_lot_id      ( lotWaferScrapEventRecord.getLotData().getLotID() );
                fhscrhs.setReason_mainpd_id   ( lotWaferScrapEventRecord.getReasonRouteID() );
                fhscrhs.setReason_ope_no      ( lotWaferScrapEventRecord.getReasonOperationNumber() );
                fhscrhs.setReason_pd_id       ( lotWaferScrapEventRecord.getReasonOperationID() );
                fhscrhs.setReason_pass_count  ( lotWaferScrapEventRecord.getReasonOperationPassCount()==null?null:
                        lotWaferScrapEventRecord.getReasonOperationPassCount().intValue());
                fhscrhs.setReason_ope_name    ( pdData_reason.getOperationName() );
                fhscrhs.setReason_test_type   ( pdData_reason.getTestType() );
                fhscrhs.setReason_stage_id    ( Data_reason.getStageID() );
                fhscrhs.setReason_stagegrp_id ( stageGrpID_reason.getValue() );
                fhscrhs.setReason_photo_layer ( Data_reason.getPhotoLayer() );
                fhscrhs.setReason_department  ( pdData_reason.getDepartment() );
                fhscrhs.setReason_location_id ( locationID.getValue() );
                fhscrhs.setReason_area_id     ( areaID.getValue() );
                fhscrhs.setReason_eqp_id      ( eqpID.getValue() );
                fhscrhs.setReason_eqp_name    ( eqpName.getValue() );
                fhscrhs.setScrap_type         ( "WS" );
            }else{
                fhscrhs.setReason_lot_id      ( fhscrhs2.getReason_lot_id()      );
                fhscrhs.setReason_mainpd_id   ( fhscrhs2.getReason_mainpd_id()   );
                fhscrhs.setReason_ope_no      ( fhscrhs2.getReason_ope_no()      );
                fhscrhs.setReason_pd_id       ( fhscrhs2.getReason_pd_id()       );
                fhscrhs.setReason_pass_count  ( fhscrhs2.getReason_pass_count() );
                fhscrhs.setReason_ope_name    ( fhscrhs2.getReason_ope_name()    );
                fhscrhs.setReason_test_type   ( fhscrhs2.getReason_test_type()   );
                fhscrhs.setReason_stage_id    ( fhscrhs2.getReason_stage_id()    );
                fhscrhs.setReason_stagegrp_id ( fhscrhs2.getReason_stagegrp_id() );
                fhscrhs.setReason_photo_layer ( fhscrhs2.getReason_photo_layer() );
                fhscrhs.setReason_department  ( fhscrhs2.getReason_department()  );
                fhscrhs.setReason_location_id ( fhscrhs2.getReason_location_id() );
                fhscrhs.setReason_area_id     ( fhscrhs2.getReason_area_id()     );
                fhscrhs.setReason_eqp_id      ( fhscrhs2.getReason_eqp_id()      );
                fhscrhs.setReason_eqp_name    ( fhscrhs2.getReason_eqp_name()    );
                fhscrhs.setScrap_type         ( "WC" );
            }
            fhscrhs.setClaim_memo         ( lotWaferScrapEventRecord.getEventCommon().getEventMemo() );
            fhscrhs.setEvent_create_time(   lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotWaferScrapHistoryService.insertLotWaferScrapHistory( fhscrhs );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHSCRHS(): InsertLotWaferScrapHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::createFHSCRHS Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::createFHSCRHS Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 15:07
     */
    public Response  createFHWLTHS_Scrap(Infos.LotWaferScrapEventRecord lotWaferScrapEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths      fhwlths ;
        Infos.Frwafer      resultData_prev ;
        Infos.Frwafer      resultData_cur  ;
        Params.String castCategory = new Params.String() ;
        Params.String codeCategory = new Params.String() ;
        int                 i;
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
        iRc = tableMethod.getFRCAST( lotWaferScrapEventRecord.getLotData().getCassetteID(),castCategory);
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
            return( iRc );
        }
        for(i = 0 ; i < length(lotWaferScrapEventRecord.getScrapWafers()); i++) {
            resultData_prev=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferScrapEventRecord.getScrapWafers().get(i).getWaferID(),resultData_prev );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
                return( iRc );
            }
            iRc = tableMethod.getFRCODE( lotWaferScrapEventRecord.getScrapWafers().get(i).getReasonCodeID() , codeCategory );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
                return( iRc );
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id (              lotWaferScrapEventRecord.getScrapWafers().get(i).getWaferID() ) ;
            fhwlths.setCur_lot_id  (           lotWaferScrapEventRecord.getLotData().getLotID() ) ;
            fhwlths.setCur_cast_id (           lotWaferScrapEventRecord.getLotData().getCassetteID() ) ;
            fhwlths.setCur_cast_category (     castCategory.getValue() ) ;
            fhwlths.setCur_cast_slot_no      ( 0 );
            fhwlths.setClaim_user_id (         lotWaferScrapEventRecord.getEventCommon().getUserID() ) ;
            fhwlths.setClaim_time (            lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() ) ;
            fhwlths.setClaim_shop_date       ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
            fhwlths.setOpe_category (          SP_OPERATIONCATEGORY_WAFERSCRAP ) ;
            if( variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID() , TXDFC001_ID ) == 0 ) {
                fhwlths.setOpe_category (          SP_OPERATIONCATEGORY_DIEADJUST ) ;
            }
            fhwlths.setApply_wafer_flag (      "Y" ) ;
            fhwlths.setProdspec_id  (          lotWaferScrapEventRecord.getLotData().getProductID() ) ;
            fhwlths.setGood_unit_count       ( resultData_prev.getGoodDiceQty() );
            fhwlths.setRepair_unit_count     ( resultData_prev.getRepairedDiceQty() );
            fhwlths.setFail_unit_count       ( resultData_prev.getBadDiceQty() );
            fhwlths.setExist_flag (            "N" ) ;
            if(Objects.equals(lotWaferScrapEventRecord.getScrapWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer      ( TRUE );
            }else{
                fhwlths.setControl_wafer      ( false );
            }

            fhwlths.setPrev_cast_slot_no     ( 0 );
            fhwlths.setReason_code (           lotWaferScrapEventRecord.getScrapWafers().get(i).getReasonCodeID().getIdentifier() ) ;
            fhwlths.setReason_description (    codeCategory.getValue() ) ;
            fhwlths.setAlias_wafer_name (      resultData_prev.getAlias_wafer_name() ) ;
            fhwlths.setEvent_create_time(      lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap(): InsertLotWaferHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
                return( iRc );
            }
        }
        for(i = 0 ; i < length(lotWaferScrapEventRecord.getCurrentWafers()); i++) {
            resultData_cur=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferScrapEventRecord.getCurrentWafers().get(i).getWaferID(),resultData_cur );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
                return( iRc );
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id (              lotWaferScrapEventRecord.getCurrentWafers().get(i).getWaferID() );
            fhwlths.setCur_lot_id  (           lotWaferScrapEventRecord.getLotData().getLotID() );
            fhwlths.setCur_cast_id (           lotWaferScrapEventRecord.getLotData().getCassetteID() );
            fhwlths.setCur_cast_category (     castCategory.getValue() );
            fhwlths.setCur_cast_slot_no      ( lotWaferScrapEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber()==null?null:
                    lotWaferScrapEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber().intValue());
            fhwlths.setClaim_user_id (         lotWaferScrapEventRecord.getEventCommon().getUserID() );
            fhwlths.setClaim_time (            lotWaferScrapEventRecord.getEventCommon().getEventTimeStamp() );
            fhwlths.setClaim_shop_date       ( lotWaferScrapEventRecord.getEventCommon().getEventShopDate() );
            fhwlths.setOpe_category (          SP_OPERATIONCATEGORY_WAFERSCRAP );
            if( variableStrCmp( lotWaferScrapEventRecord.getEventCommon().getTransactionID() , TXDFC001_ID ) == 0 ) {
                fhwlths.setOpe_category (          SP_OPERATIONCATEGORY_DIEADJUST ) ;
            }

            fhwlths.setApply_wafer_flag (      "N" );
            fhwlths.setProdspec_id (           lotWaferScrapEventRecord.getLotData().getProductID() );
            fhwlths.setGood_unit_count       ( resultData_cur.getGoodDiceQty() );
            fhwlths.setRepair_unit_count     ( resultData_cur.getRepairedDiceQty() );
            fhwlths.setFail_unit_count       ( resultData_cur.getBadDiceQty() );
            fhwlths.setExist_flag (            "Y" );
            if(Objects.equals(lotWaferScrapEventRecord.getCurrentWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer      ( true );
            }else{
                fhwlths.setControl_wafer      ( false );
            }
            fhwlths.setPrev_cast_slot_no      ( 0 );

            fhwlths.setAlias_wafer_name(       resultData_cur.getAlias_wafer_name() );
            fhwlths.setEvent_create_time(      lotWaferScrapEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap(): InsertLotWaferHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::createFHWLTHS_Scrap Function" );

        return(returnOK());
    }

}
