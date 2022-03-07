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
public class BondingGroupEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private BondingGroupHistoryService bondingGroupHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bondingGroupEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:28
     */
    public Response createBondingGroupEventRecord(Infos.BondingGroupEventRecord bondingGroupEventRecord , List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateBondingGroupEventRecord Function" );
        iRc = createFHBGRPHS( bondingGroupEventRecord, userDataSets );
        if (!isOk(iRc)) {
            log.info("HistoryWatchDogServer::CreateBondingGroupEventRecord Function" );
            return ( iRc );
        }
        iRc = createFHBGRPHS_MAP( bondingGroupEventRecord, userDataSets );
        if (!isOk(iRc)) {
            log.info("HistoryWatchDogServer::CreateBondingGroupEventRecord Function" );
            return ( iRc );
        }

        log.info("HistoryWatchDogServer::CreateBondingGroupEventRecord Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bondingGroupEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:30
     */
    public Response createFHBGRPHS(Infos.BondingGroupEventRecord bondingGroupEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohbgrphs      fhbgrphs ;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHBGRPHS Function" );
        fhbgrphs=new Infos.Ohbgrphs();
        fhbgrphs.setBondingGroupID (     bondingGroupEventRecord.getBondingGroupID() );
        fhbgrphs.setAction (             bondingGroupEventRecord.getAction() );
        fhbgrphs.setBondingGroupStatus ( bondingGroupEventRecord.getBondingGroupStatus() );
        fhbgrphs.setEquipmentID (        bondingGroupEventRecord.getEquipmentID() );
        fhbgrphs.setControlJobID (       bondingGroupEventRecord.getControlJobID() );
        fhbgrphs.setTx_id (              bondingGroupEventRecord.getEventCommon().getTransactionID() ) ;
        fhbgrphs.setClaim_time (         bondingGroupEventRecord.getEventCommon().getEventTimeStamp() );
        fhbgrphs.setClaim_user_id (      bondingGroupEventRecord.getEventCommon().getUserID() );
        fhbgrphs.setClaim_memo  (        bondingGroupEventRecord.getEventCommon().getEventMemo() );
        fhbgrphs.setEvent_create_time(   bondingGroupEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = bondingGroupHistoryService.insertBondingGroupHistory( fhbgrphs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHBGRPHS(): InsertBondingGroupHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::createFHBGRPHS Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::createFHBGRPHS Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bondingGroupEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:33
     */
    public Response createFHBGRPHS_MAP(Infos.BondingGroupEventRecord bondingGroupEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.OhbgrphsMap         fhbgrphs_map;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::createFHBGRPHS_MAP Function" );
        int mapLen = length(bondingGroupEventRecord.getBondingMapInfos());
        for ( int mapCnt = 0; mapCnt < mapLen; mapCnt++ ) {
            fhbgrphs_map=new Infos.OhbgrphsMap();
            fhbgrphs_map.setBondingGroupID(   bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBondingGroupID() );
            fhbgrphs_map.setAction(           bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getAction() );
            fhbgrphs_map.setBondingSeqNo    ( bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBondingSeqNo()==null?null:
                    bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBondingSeqNo().intValue());
            fhbgrphs_map.setBaseLotID(        bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBaseLotID() );
            fhbgrphs_map.setBaseProductID(    bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBaseProductID() );
            fhbgrphs_map.setBaseWaferID(      bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBaseWaferID() );
            fhbgrphs_map.setBaseBondingSide(  bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getBaseBondingSide() );
            fhbgrphs_map.setTopLotID(         bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getTopLotID() );
            fhbgrphs_map.setTopProductID(     bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getTopProductID() );
            fhbgrphs_map.setTopWaferID(       bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getTopWaferID() );
            fhbgrphs_map.setTopBondingSide(   bondingGroupEventRecord.getBondingMapInfos().get(mapCnt).getTopBondingSide() );
            fhbgrphs_map.setClaim_time(       bondingGroupEventRecord.getEventCommon().getEventTimeStamp() );
            iRc = bondingGroupHistoryService.insertBondingGroupHistory_map( fhbgrphs_map );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHBGRPHS_MAP(): InsertBondingGroupHistory_map SQL Error Occurred" );

                log.info("HistoryWatchDogServer::createFHBGRPHS_MAP Function" );
                return iRc;
            }
        }

        log.info("HistoryWatchDogServer::createFHBGRPHS_MAP Function" );
        return returnOK();

    }

}
