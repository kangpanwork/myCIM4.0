package com.fa.cim.service;

import com.fa.cim.dto.Infos;
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
 * @date 2019/7/25 11:29
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotFlowBatchEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotFlowBatchHistoryService lotFlowBatchHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotFlowBatchEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:11
     */
    public Response createLotFlowBatchEventRecord(Infos.LotFlowBatchEventRecord lotFlowBatchEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateLotFlowBatchEventRecord Function" );

        Infos.Ohflbchhs      fhflbchhs;
        Response iRc = returnOK();
        fhflbchhs=new Infos.Ohflbchhs();
        fhflbchhs.setLot_id(          lotFlowBatchEventRecord.getLotData().getLotID() );
        fhflbchhs.setLot_type(        lotFlowBatchEventRecord.getLotData().getLotType() );
        fhflbchhs.setCast_id(         lotFlowBatchEventRecord.getLotData().getCassetteID() );
        fhflbchhs.setLot_status(      lotFlowBatchEventRecord.getLotData().getLotStatus() );
        fhflbchhs.setCustomer_id(     lotFlowBatchEventRecord.getLotData().getCustomerID() );
        fhflbchhs.setPriority_class ( lotFlowBatchEventRecord.getLotData().getPriorityClass()==null?null:
                lotFlowBatchEventRecord.getLotData().getPriorityClass().intValue());
        fhflbchhs.setProdspec_id(     lotFlowBatchEventRecord.getLotData().getProductID() );
        fhflbchhs.setOriginal_qty   ( lotFlowBatchEventRecord.getLotData().getOriginalWaferQuantity());
        fhflbchhs.setCur_qty        ( lotFlowBatchEventRecord.getLotData().getCurrentWaferQuantity());
        fhflbchhs.setProd_qty       ( lotFlowBatchEventRecord.getLotData().getProductWaferQuantity());
        fhflbchhs.setCntl_qty       ( lotFlowBatchEventRecord.getLotData().getControlWaferQuantity());
        fhflbchhs.setLot_hold_state(  lotFlowBatchEventRecord.getLotData().getHoldState() );
        fhflbchhs.setBank_id(         lotFlowBatchEventRecord.getLotData().getBankID() );
        fhflbchhs.setMainpd_id(       lotFlowBatchEventRecord.getLotData().getRouteID() );
        fhflbchhs.setOpe_no(          lotFlowBatchEventRecord.getLotData().getOperationNumber() );
        fhflbchhs.setPd_id(           lotFlowBatchEventRecord.getLotData().getOperationID() );
        fhflbchhs.setPass_count     ( lotFlowBatchEventRecord.getLotData().getOperationPassCount());
        fhflbchhs.setWafer_his_time(  lotFlowBatchEventRecord.getLotData().getWaferHistoryTimeStamp() );

        if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW001_ID ) == 0 ) {
            fhflbchhs.setEvent_type(  EVTYPE_FLOWBATCHING );
        }
        else if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW003_ID ) == 0 ) {
            fhflbchhs.setEvent_type(  EVTYPE_EQP_RESERVE );
        }
        else if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW004_ID ) == 0 ) {
            fhflbchhs.setEvent_type(  EVTYPE_EQP_RESERVE_CANCEL );
        }
        else if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW005_ID ) == 0 ) {
            fhflbchhs.setEvent_type(  EVTYPE_LOTREMOVE );
        }
        else if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW006_ID ) == 0 ) {
            fhflbchhs.setEvent_type(  EVTYPE_REFLOWBATCHING );
        }

        if( variableStrCmp( lotFlowBatchEventRecord.getEventCommon().getTransactionID(), OFLWW005_ID ) == 0 ) {
            fhflbchhs.setTarget_ope_no(   "" );
            fhflbchhs.setTarget_eqp_id(   "" );
        }
        else
        {
            fhflbchhs.setTarget_ope_no(   lotFlowBatchEventRecord.getTargetOperationNumber() );
            fhflbchhs.setTarget_eqp_id(   lotFlowBatchEventRecord.getTargetEquipmentID() );
        }

        fhflbchhs.setFlowbatch_id(    lotFlowBatchEventRecord.getFlowBatchID() );
        fhflbchhs.setFr_flowbatch_id( ""                                   );
        fhflbchhs.setClaim_time(      lotFlowBatchEventRecord.getEventCommon().getEventTimeStamp() );
        fhflbchhs.setClaim_shop_date( lotFlowBatchEventRecord.getEventCommon().getEventShopDate() );
        fhflbchhs.setClaim_user_id(   lotFlowBatchEventRecord.getEventCommon().getUserID() );
        fhflbchhs.setClaim_memo(      lotFlowBatchEventRecord.getEventCommon().getEventMemo() );
        fhflbchhs.setEvent_create_time( lotFlowBatchEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = lotFlowBatchHistoryService.insertLotFlowBatchHistory( fhflbchhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createLotFlowBatchEventRecord(): InsertLotFlowBatchHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateLotFlowBatchEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateLotFlowBatchEventRecord Function" );
        return(returnOK());
    }

}
