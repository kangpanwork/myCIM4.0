package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
 * @date 2019/7/25 11:29
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class FutureSplitEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private FutureSplitHistoryService futureSplitHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param futureSplitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:12
     */
    public Response createFutureSplitEventRecord(com.fa.cim.fsm.Infos.FutureSplitEventRecord futureSplitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreatefutureSplitEventRecord Function" );
        com.fa.cim.fsm.Infos.Ohfuturehs        fhplsphs;
        com.fa.cim.fsm.Infos.OhfuturehsWafer  fhplsphs_wafer;
        int                   i,j;
        Response iRc = returnOK();
        for(i = 0 ; i < length(futureSplitEventRecord.getRoutes()); i++) {

            fhplsphs=new com.fa.cim.fsm.Infos.Ohfuturehs();
            //add psmJobID for history
            fhplsphs.setFusplitjob_id(  futureSplitEventRecord.getFsmJobID() );
            fhplsphs.setAction_code(       futureSplitEventRecord.getAction() );
            fhplsphs.setLotfamily_id(      futureSplitEventRecord.getLotFamilyID() );
            fhplsphs.setSplit_route_id(    futureSplitEventRecord.getSplitRouteID() );
            fhplsphs.setSplit_ope_no(      futureSplitEventRecord.getSplitOperationNumber() );
            fhplsphs.setOriginal_route_id( futureSplitEventRecord.getOriginalRouteID() );
            fhplsphs.setOriginal_ope_no(   futureSplitEventRecord.getOriginalOperationNumber() );
            fhplsphs.setActionemail      ( convertI(futureSplitEventRecord.getActionEMail()));
            fhplsphs.setActionhold       ( convertI(futureSplitEventRecord.getActionHold()));
            fhplsphs.setSeq_no           ( i);
            fhplsphs.setRoute_id(      futureSplitEventRecord.getRoutes().get(i).getRouteID() );
            fhplsphs.setReturn_ope_no(     futureSplitEventRecord.getRoutes().get(i).getReturnOperationNumber() );
            fhplsphs.setMerge_ope_no(      futureSplitEventRecord.getRoutes().get(i).getMergeOperationNumber() );
            fhplsphs.setParent_lot_id(     futureSplitEventRecord.getRoutes().get(i).getParentLotID() );
            fhplsphs.setChild_lot_id(      futureSplitEventRecord.getRoutes().get(i).getChildLotID() );
            fhplsphs.setMemo(              futureSplitEventRecord.getRoutes().get(i).getMemo() );
            fhplsphs.setClaim_time (       futureSplitEventRecord.getEventCommon().getEventTimeStamp() );
            fhplsphs.setClaim_shop_date  ( futureSplitEventRecord.getEventCommon().getEventShopDate());
            fhplsphs.setClaim_user_id(     futureSplitEventRecord.getEventCommon().getUserID() );
            fhplsphs.setClaim_memo(        futureSplitEventRecord.getEventCommon().getEventMemo() );
            fhplsphs.setEvent_create_time( futureSplitEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = futureSplitHistoryService.insertFutureSplitEventRecord_FHFUTUREHS( fhplsphs );

            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFutureSplitEventRecord(): InsertFutureSplitEventRecord_FHELCHS SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateFutureSplitEventRecord Function" );
                return( iRc );
            }

            for(j = 0 ; j < length(futureSplitEventRecord.getRoutes().get(i).getWafers()); j++) {
                fhplsphs_wafer=new com.fa.cim.fsm.Infos.OhfuturehsWafer();

                //add psmJobID for history
                fhplsphs_wafer.setFusplitjob_id(  futureSplitEventRecord.getFsmJobID() );
                fhplsphs_wafer.setAction_code(       futureSplitEventRecord.getAction() );
                fhplsphs_wafer.setLotfamily_id(      futureSplitEventRecord.getLotFamilyID() );
                fhplsphs_wafer.setSplit_route_id(    futureSplitEventRecord.getSplitRouteID() );
                fhplsphs_wafer.setSplit_ope_no(      futureSplitEventRecord.getSplitOperationNumber() );
                fhplsphs_wafer.setOriginal_route_id( futureSplitEventRecord.getOriginalRouteID() );
                fhplsphs_wafer.setOriginal_ope_no(   futureSplitEventRecord.getOriginalOperationNumber() );
                fhplsphs_wafer.setSeq_no           ( i);
                fhplsphs_wafer.setRoute_id(      futureSplitEventRecord.getRoutes().get(i).getRouteID() );
                fhplsphs_wafer.setParent_lot_id(     futureSplitEventRecord.getRoutes().get(i).getParentLotID() );
                fhplsphs_wafer.setChild_lot_id(      futureSplitEventRecord.getRoutes().get(i).getChildLotID() );
                fhplsphs_wafer.setWafer_id(          futureSplitEventRecord.getRoutes().get(i).getWafers().get(j).getWaferID() );
                // task-3988 add group_no
                fhplsphs_wafer.setGroup_no(          futureSplitEventRecord.getRoutes().get(i).getWafers().get(j).getGroupNo());
                fhplsphs_wafer.setSuccess_flag(      futureSplitEventRecord.getRoutes().get(i).getWafers().get(j).getSuccessFlag() );
                fhplsphs_wafer.setClaim_time(        futureSplitEventRecord.getEventCommon().getEventTimeStamp() );
                iRc = futureSplitHistoryService.insertFutureSplitEventRecord_FHFUTUREHS_WAFER( fhplsphs_wafer );

                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createFutureSplitEventRecord(): InsertFutureSplitEventRecord_FHELCHS_WAFER SQL Error Occured" );
                    log.info("HistoryWatchDogServer::CreateFutureSplitEventRecord Function" );
                    return( iRc );
                }
            }
        }

        log.info("HistoryWatchDogServer::CreateFutureSplitEventRecord Function" );
        return(returnOK());

    }

}
