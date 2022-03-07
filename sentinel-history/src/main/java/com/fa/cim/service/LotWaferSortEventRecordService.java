package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.fa.cim.Constant.SPConstant.*;
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
 * @date 2019/7/25 11:29
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotWaferSortEventRecordService {

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
     * @param lotWaferSortEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 11:31
     */
    public Response createLotWaferSortEventRecord(Infos.LotWaferSortEventRecord lotWaferSortEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwlths        fhwlths ;
        Infos.Frwafer        resultData ;
        Params.String castCategory = new Params.String();
        Params.String prev_castCategory = new Params.String();
        int                   i;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );

        for(i = 0 ; i < length(lotWaferSortEventRecord.getCurrentWafers()); i++) {
            iRc = tableMethod.getFRCAST( lotWaferSortEventRecord.getCurrentWafers().get(i).getDestinationCassetteID() ,
                    castCategory );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }

            iRc = tableMethod.getFRCAST( lotWaferSortEventRecord.getCurrentWafers().get(i).getOriginalCassetteID() ,
                    prev_castCategory );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }

            resultData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferSortEventRecord.getCurrentWafers().get(i).getWaferID() ,   resultData ) ;
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id (            lotWaferSortEventRecord.getCurrentWafers().get(i).getWaferID() );
            fhwlths.setCur_lot_id(           lotWaferSortEventRecord.getLotID() );

            fhwlths.setEquipmentID(          lotWaferSortEventRecord.getEquipmentID());
            fhwlths.setSorterJobID(          lotWaferSortEventRecord.getSorterJobID());
            fhwlths.setComponentJobID(       lotWaferSortEventRecord.getComponentJobID());
            if( variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), TXPCC101_ID ) == 0 ||
                    variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), TXPCC103_ID ) == 0 ) {
                fhwlths.setCur_cast_id (           ""           );
                fhwlths.setCur_cast_category (     ""           );
                fhwlths.setCur_cast_slot_no  ( 0 );

                fhwlths.setPrev_cast_slot_no ( 0 );
            }
            else
            {

                fhwlths.setCur_cast_id( lotWaferSortEventRecord.getCurrentWafers().get(i).getDestinationCassetteID() );
                fhwlths.setCur_cast_category(  castCategory.getValue() );
                fhwlths.setPrev_cast_id(lotWaferSortEventRecord.getCurrentWafers().get(i).getOriginalCassetteID() );
                fhwlths.setPrev_cast_category( prev_castCategory.getValue() );
                fhwlths.setCur_cast_slot_no ( lotWaferSortEventRecord.getCurrentWafers().get(i).getDestinationSlotNumber()==null?null:
                        lotWaferSortEventRecord.getCurrentWafers().get(i).getDestinationSlotNumber().intValue());
                fhwlths.setPrev_cast_slot_no  ( lotWaferSortEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber()==null?null:
                        lotWaferSortEventRecord.getCurrentWafers().get(i).getOriginalSlotNumber().intValue());
            }
            fhwlths.setClaim_shop_date       ( lotWaferSortEventRecord.getEventCommon().getEventShopDate() );

            fhwlths.setClaim_user_id( lotWaferSortEventRecord.getEventCommon().getUserID() );
            fhwlths.setClaim_time(    lotWaferSortEventRecord.getEventCommon().getEventTimeStamp() );
            if( variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTR003_ID ) == 0 ||
                    variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTW001_ID ) == 0 ||
                    variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTW002_ID ) ==0 ) {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_WAFERSORT );

            }
            else if (variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), TXEQR016_ID ) == 0) {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_SLMCASSETTECHANGE );

            } else
            {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_CASSETTECHANGE );
            }
            fhwlths.setApply_wafer_flag(   "Y" );
            fhwlths.setProdspec_id (       resultData.getProductID() );
            fhwlths.setGood_unit_count   ( resultData.getGoodDiceQty() );
            fhwlths.setRepair_unit_count ( resultData.getRepairedDiceQty() );
            fhwlths.setFail_unit_count   ( resultData.getBadDiceQty() );
            fhwlths.setExist_flag  (       "Y" );
            if(Objects.equals(lotWaferSortEventRecord.getCurrentWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer      ( true );
            }else{
                fhwlths.setControl_wafer      ( false );
            }
            fhwlths.setAlias_wafer_name( resultData.getAlias_wafer_name() );
            fhwlths.setEvent_create_time( lotWaferSortEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createLotWafersortEventRecord(): InsertLotWaferHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }
        }
        for(i = 0; i < length(lotWaferSortEventRecord.getSourceWafers()); i++) {
            iRc = tableMethod.getFRCAST( lotWaferSortEventRecord.getSourceWafers().get(i).getOriginalCassetteID(), castCategory );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }

            resultData=new Infos.Frwafer();
            iRc = tableMethod.getFRWAFER( lotWaferSortEventRecord.getSourceWafers().get(i).getWaferID(), resultData );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }
            fhwlths=new Infos.Ohwlths();
            fhwlths.setWafer_id(           lotWaferSortEventRecord.getSourceWafers().get(i).getWaferID() );
            fhwlths.setCur_lot_id(         lotWaferSortEventRecord.getLotID() );
            fhwlths.setCur_cast_id(        lotWaferSortEventRecord.getSourceWafers().get(i).getOriginalCassetteID() );
            fhwlths.setCur_cast_category(  castCategory.getValue() );
            fhwlths.setCur_cast_slot_no  ( lotWaferSortEventRecord.getSourceWafers().get(i).getOriginalSlotNumber()==null?null:
                    lotWaferSortEventRecord.getSourceWafers().get(i).getOriginalSlotNumber().intValue() );
            fhwlths.setPrev_cast_slot_no ( 0);

            fhwlths.setClaim_user_id(      lotWaferSortEventRecord.getEventCommon().getUserID() );
            fhwlths.setClaim_time(         lotWaferSortEventRecord.getEventCommon().getEventTimeStamp() );
            fhwlths.setClaim_shop_date   ( lotWaferSortEventRecord.getEventCommon().getEventShopDate());
            if( variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTR003_ID ) == 0 ||
                    variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTW001_ID ) == 0 ||
                    variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), OSRTW002_ID ) == 0 ) {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_WAFERSORT );

            }
            else if (variableStrCmp( lotWaferSortEventRecord.getEventCommon().getTransactionID(), TXEQR016_ID ) == 0) {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_SLMCASSETTECHANGE );

            }
            else
            {
                fhwlths.setOpe_category( SP_OPERATIONCATEGORY_CASSETTECHANGE );
            }

            fhwlths.setApply_wafer_flag(   "N" );
            fhwlths.setProdspec_id(        resultData.getProductID() );
            fhwlths.setGood_unit_count   ( resultData.getGoodDiceQty());
            fhwlths.setRepair_unit_count ( resultData.getRepairedDiceQty());
            fhwlths.setFail_unit_count   ( resultData.getBadDiceQty());
            fhwlths.setExist_flag(         "Y" );
            if(Objects.equals(lotWaferSortEventRecord.getSourceWafers().get(i).getControlWaferFlag(), TRUE)) {
                fhwlths.setControl_wafer ( true);
            }
            else
            {
                fhwlths.setControl_wafer ( false);
            }

            fhwlths.setAlias_wafer_name(   resultData.getAlias_wafer_name() );
            fhwlths.setEvent_create_time( lotWaferSortEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = lotOperationHistoryService.insertLotWaferHistory( fhwlths );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createLotWaferSortEventRecord(): InsertLotWaferHistory SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateLotWaferSortEventRecord Function" );
        return(returnOK());
    }

}
