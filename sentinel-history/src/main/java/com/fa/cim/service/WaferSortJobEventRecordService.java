package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import com.fa.cim.utils.StringUtils;
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
public class WaferSortJobEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private WaferSortJobHistoryService waferSortJobHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param waferSortJobEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 14:15
     */
    public Response createWaferSortJobEventRecord(Infos.WaferSortJobEventRecord waferSortJobEventRecord, List<Infos.UserDataSet> userDataSets) {
        log.info("HistoryWatchDogServer::CreateWaferSortJobEventRecord " );

        Infos.Ohsortjobhs            sortJobHistoryRecord;
        Infos.OhsortjobhsComponent  sortJobHistoryComponentRecord;
        Infos.OhsortjobhsSlotmap    sortJobHistorySlotmapRecord;
        Infos.OhPostAct sortHistoryPostActRecord;

        Response iRc ;
        sortJobHistoryRecord=new Infos.Ohsortjobhs();
        sortJobHistoryRecord.setEquipmentID(waferSortJobEventRecord.getEquipmentID());
        sortJobHistoryRecord.setPortGroupID(waferSortJobEventRecord.getPortGroupID());
        sortJobHistoryRecord.setSorterJobID(waferSortJobEventRecord.getSorterJobID());
        sortJobHistoryRecord.setSorterJobStatus(waferSortJobEventRecord.getSorterJobStatus());
        sortJobHistoryRecord.setWaferIDReadFlag(convertI(waferSortJobEventRecord.isWaferIDReadFlag()));
        sortJobHistoryRecord.setClaimTime(waferSortJobEventRecord.getEventCommon().getEventTimeStamp());
        sortJobHistoryRecord.setClaimUser(waferSortJobEventRecord.getEventCommon().getUserID());
        sortJobHistoryRecord.setClaimMemo(waferSortJobEventRecord.getEventCommon().getEventMemo());
        sortJobHistoryRecord.setEventCreateTime(waferSortJobEventRecord.getEventCommon().getEventCreationTimeStamp());
        sortJobHistoryRecord.setStoreTime(waferSortJobEventRecord.getEventCommon().getEventTimeStamp());
        sortJobHistoryRecord.setCtrlJobID(waferSortJobEventRecord.getCtrlJobID());
        sortJobHistoryRecord.setComponentJobCount(waferSortJobEventRecord.getComponentJobCount());
        sortJobHistoryRecord.setOperation(waferSortJobEventRecord.getOperation());


        Infos.Output output = waferSortJobHistoryService.insertSortJob(sortJobHistoryRecord);
        if( !isOk(output.getResponse()) ) {
            log.info("HistoryWatchDogServer::createWaferSortJobEventRecord(): InsertWaferSortJobHistory_FHSORTJOBHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreatewaferSortJobEventRecord Function" );
            return(output.getResponse());
        }

        if (length(waferSortJobEventRecord.getComponentJobs()) > 0) {
            for (Infos.SortJobComponentEventData componentJob : waferSortJobEventRecord.getComponentJobs()) {
                sortJobHistoryComponentRecord = new Infos.OhsortjobhsComponent();
                sortJobHistoryComponentRecord.setComponentJobID(componentJob.getComponentJobID());
                sortJobHistoryComponentRecord.setSourceCassetteID(componentJob.getSourceCarrierID());
                sortJobHistoryComponentRecord.setDestinationCassetteID(componentJob.getDestinationCarrierID());
                sortJobHistoryComponentRecord.setSourcePortID(componentJob.getSourcePortID());
                sortJobHistoryComponentRecord.setDestinationPortID(componentJob.getDestinationPortID());
                sortJobHistoryComponentRecord.setComponentJobStatus(componentJob.getComponentJobStatus());
                sortJobHistoryComponentRecord.setActionCode(componentJob.getActionCode());
                sortJobHistoryComponentRecord.setOperation(componentJob.getOperation());
                sortJobHistoryComponentRecord.setClaimTime(waferSortJobEventRecord.getEventCommon().getEventTimeStamp());
                sortJobHistoryComponentRecord.setClaimUser(waferSortJobEventRecord.getEventCommon().getUserID());
                sortJobHistoryComponentRecord.setRefkey(output.getRefkey());

                iRc = waferSortJobHistoryService.insertComponentJob(sortJobHistoryComponentRecord);
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createWaferSortJobEventRecord(): InsertWaferSortJobHistory_FHSORTJOBHS_COMPONENT SQL Error Occured" );
                    log.info("HistoryWatchDogServer::CreateWaferSortJobEventRecord Function" );
                    return( iRc );
                }
            }
        }

        if (length(waferSortJobEventRecord.getSlotMaps()) > 0) {
            for (Infos.SortJobSlotMapEventData slotMap : waferSortJobEventRecord.getSlotMaps()) {
                sortJobHistorySlotmapRecord = new Infos.OhsortjobhsSlotmap();
                sortJobHistorySlotmapRecord.setComponentJobID(slotMap.getComponentJobID());
                sortJobHistorySlotmapRecord.setLotID(slotMap.getLotID());
                sortJobHistorySlotmapRecord.setWaferID(slotMap.getWaferID());
                sortJobHistorySlotmapRecord.setDestinationSlotPosition(slotMap.getDestinationPosition());
                sortJobHistorySlotmapRecord.setSourceSlotPosition(slotMap.getSourcePosition());
                sortJobHistorySlotmapRecord.setAliasName(slotMap.getAliasName());
                sortJobHistorySlotmapRecord.setDirection(slotMap.getDirection());
                sortJobHistorySlotmapRecord.setSorterStatus(slotMap.getSortStatus());
                sortJobHistorySlotmapRecord.setClaimTime(waferSortJobEventRecord.getEventCommon().getEventTimeStamp());
                sortJobHistorySlotmapRecord.setClaimUser(waferSortJobEventRecord.getEventCommon().getUserID());
                sortJobHistorySlotmapRecord.setRefkey(output.getRefkey());

                iRc = waferSortJobHistoryService.inserSlotMap( sortJobHistorySlotmapRecord );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::createWaferSortJobEventRecord(): InsertWaferSortJobHistory_FHSORTJOBHS_SLOTMAP SQL Error Occured" );
                    log.info("HistoryWatchDogServer::CreateWaferSortJobEventRecord Function" );
                    return(iRc);
                }
            }
        }

        Infos.WaferSortJobPostActRecord postActRecord = waferSortJobEventRecord.getPostActRecord();
        if (postActRecord != null && StringUtils.length(postActRecord.getSorterJobID()) > 0) {
            sortHistoryPostActRecord = new Infos.OhPostAct();
            sortHistoryPostActRecord.setSorterJobID(postActRecord.getSorterJobID());
            sortHistoryPostActRecord.setActionCode(postActRecord.getActionCode());
            sortHistoryPostActRecord.setProductOrderID(postActRecord.getProductOrderID());
            sortHistoryPostActRecord.setVendorID(postActRecord.getVendorID());
            sortHistoryPostActRecord.setWaferCount(postActRecord.getWaferCount());
            sortHistoryPostActRecord.setSourceProductID(postActRecord.getSourceProductID());
            sortHistoryPostActRecord.setClaimTime(waferSortJobEventRecord.getEventCommon().getEventTimeStamp());
            sortHistoryPostActRecord.setClaimUser(waferSortJobEventRecord.getEventCommon().getUserID());
            sortHistoryPostActRecord.setRefkey(output.getRefkey());
            sortHistoryPostActRecord.setParentLotId(postActRecord.getParentLotId());
            sortHistoryPostActRecord.setChildLotId(postActRecord.getChildLotId());

            waferSortJobHistoryService.insertPostAct(sortHistoryPostActRecord);
        }


        log.info("HistoryWatchDogServer::CreateWaferSortJobEventRecord Function" );
        return( returnOK() );
    }

}
