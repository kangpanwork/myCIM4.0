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
public class PlannedSplitEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private PlannedSplitHistoryService plannedSplitHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param plannedSplitEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:12
     */
    public Response createPlannedSplitEventRecord(Infos.PlannedSplitEventRecord plannedSplitEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreatePlannedSplitEventRecord Function" );
        Infos.Ohplsphs        fhplsphs;
        Infos.OhplsphsWafer  fhplsphs_wafer;
        int                   i,j;
        Response iRc = returnOK();
        for(i = 0 ; i < length(plannedSplitEventRecord.getSubRoutes()); i++) {

            fhplsphs=new Infos.Ohplsphs();
            //add psmJobID for history
            fhplsphs.setPlsplitjob_id(  plannedSplitEventRecord.getPsmJobID() );
            //add runCardID for history
            fhplsphs.setRuncard_id(  plannedSplitEventRecord.getRunCardID() );
            fhplsphs.setAction_code(       plannedSplitEventRecord.getAction() );
            fhplsphs.setLotfamily_id(      plannedSplitEventRecord.getLotFamilyID() );
            fhplsphs.setSplit_route_id(    plannedSplitEventRecord.getSplitRouteID() );
            fhplsphs.setSplit_ope_no(      plannedSplitEventRecord.getSplitOperationNumber() );
            fhplsphs.setOriginal_route_id( plannedSplitEventRecord.getOriginalRouteID() );
            fhplsphs.setOriginal_ope_no(   plannedSplitEventRecord.getOriginalOperationNumber() );
            fhplsphs.setActionemail      ( convertI(plannedSplitEventRecord.getActionEMail()));
            fhplsphs.setActionhold       ( convertI(plannedSplitEventRecord.getActionHold()));
            fhplsphs.setSeq_no           ( i);
            fhplsphs.setSub_route_id(      plannedSplitEventRecord.getSubRoutes().get(i).getSubRouteID() );
            fhplsphs.setReturn_ope_no(     plannedSplitEventRecord.getSubRoutes().get(i).getReturnOperationNumber() );
            fhplsphs.setMerge_ope_no(      plannedSplitEventRecord.getSubRoutes().get(i).getMergeOperationNumber() );
            fhplsphs.setParent_lot_id(     plannedSplitEventRecord.getSubRoutes().get(i).getParentLotID() );
            fhplsphs.setChild_lot_id(      plannedSplitEventRecord.getSubRoutes().get(i).getChildLotID() );
            fhplsphs.setMemo(              plannedSplitEventRecord.getSubRoutes().get(i).getMemo() );
            fhplsphs.setClaim_time (       plannedSplitEventRecord.getEventCommon().getEventTimeStamp() );
            fhplsphs.setClaim_shop_date  ( plannedSplitEventRecord.getEventCommon().getEventShopDate());
            fhplsphs.setClaim_user_id(     plannedSplitEventRecord.getEventCommon().getUserID() );
            fhplsphs.setClaim_memo(        plannedSplitEventRecord.getEventCommon().getEventMemo() );
            fhplsphs.setEvent_create_time( plannedSplitEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = plannedSplitHistoryService.insertPlannedSplitEventRecord_FHPLSPHS( fhplsphs );

            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createPlannedSplitEventRecord(): InsertPlannedSplitEventRecord_FHELCHS SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreatePlannedSplitEventRecord Function" );
                return( iRc );
            }

            for(j = 0 ; j < length(plannedSplitEventRecord.getSubRoutes().get(i).getWafers()); j++) {
                fhplsphs_wafer=new Infos.OhplsphsWafer();

                //add psmJobID for history
                fhplsphs_wafer.setPlsplitjob_id(  plannedSplitEventRecord.getPsmJobID() );
                //add runCardID for history
                fhplsphs_wafer.setRuncard_id(  plannedSplitEventRecord.getRunCardID() );
                fhplsphs_wafer.setAction_code(       plannedSplitEventRecord.getAction() );
                fhplsphs_wafer.setLotfamily_id(      plannedSplitEventRecord.getLotFamilyID() );
                fhplsphs_wafer.setSplit_route_id(    plannedSplitEventRecord.getSplitRouteID() );
                fhplsphs_wafer.setSplit_ope_no(      plannedSplitEventRecord.getSplitOperationNumber() );
                fhplsphs_wafer.setOriginal_route_id( plannedSplitEventRecord.getOriginalRouteID() );
                fhplsphs_wafer.setOriginal_ope_no(   plannedSplitEventRecord.getOriginalOperationNumber() );
                fhplsphs_wafer.setSeq_no           ( i);
                fhplsphs_wafer.setSub_route_id(      plannedSplitEventRecord.getSubRoutes().get(i).getSubRouteID() );
                fhplsphs_wafer.setParent_lot_id(     plannedSplitEventRecord.getSubRoutes().get(i).getParentLotID() );
                fhplsphs_wafer.setChild_lot_id(      plannedSplitEventRecord.getSubRoutes().get(i).getChildLotID() );
                fhplsphs_wafer.setWafer_id(          plannedSplitEventRecord.getSubRoutes().get(i).getWafers().get(j).getWaferID() );
                fhplsphs_wafer.setSuccess_flag(      plannedSplitEventRecord.getSubRoutes().get(i).getWafers().get(j).getSuccessFlag() );
                fhplsphs_wafer.setClaim_time(        plannedSplitEventRecord.getEventCommon().getEventTimeStamp() );
                iRc = plannedSplitHistoryService.insertPlannedSplitEventRecord_FHPLSPHS_WAFER( fhplsphs_wafer );

                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createPlannedSplitEventRecord(): InsertPlannedSplitEventRecord_FHELCHS_WAFER SQL Error Occured" );
                    log.info("HistoryWatchDogServer::CreatePlannedSplitEventRecord Function" );
                    return( iRc );
                }
            }
        }

        log.info("HistoryWatchDogServer::CreatePlannedSPlitEventRecord Function" );
        return(returnOK());

    }

}
