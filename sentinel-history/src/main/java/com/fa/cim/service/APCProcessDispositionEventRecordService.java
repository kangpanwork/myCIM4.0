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
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class APCProcessDispositionEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private APCProcessDispositionHistoryService apcProcessDispositionHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param APCProcessDispositionEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 10:53
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createAPCProcessDispositionEventRecord( Infos.APCProcessDispositionEventRecord APCProcessDispositionEventRecord,
                                                            List<Infos.UserDataSet> userDataSets ) {
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateAPCProcessDispositionEventRecord Function");
        iRc = createFHPRCDPHS( APCProcessDispositionEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateAPCProcessDispositionEventRecord Function");
            return( iRc );
        }
        iRc = returnOK();
        iRc = createFHPRCDPHS_LOTS( APCProcessDispositionEventRecord, userDataSets );
        if (!isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateAPCProcessDispositionEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateAPCProcessDispositionEventRecord Function");
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param APCProcessDispositionEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 11:05
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createFHPRCDPHS( Infos.APCProcessDispositionEventRecord APCProcessDispositionEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::createFHPRCDPHS Function");
        Infos.Ohprcdphs      fhprcdphs= new Infos.Ohprcdphs();
        Response iRc = returnOK();
        fhprcdphs = new Infos.Ohprcdphs();
        fhprcdphs.setCtrl_job(APCProcessDispositionEventRecord.getCtrlJob()                         );
        fhprcdphs.setClaim_time(APCProcessDispositionEventRecord.getEventCommon().getEventTimeStamp()      );
        fhprcdphs.setEqp_id(APCProcessDispositionEventRecord.getEqpID()                           );
        fhprcdphs.setEqp_descripstion(APCProcessDispositionEventRecord.getEqpDescription()                  );
        fhprcdphs.setClaim_user_id(APCProcessDispositionEventRecord.getEventCommon().getUserID()              );
        fhprcdphs.setApc_system_name(APCProcessDispositionEventRecord.getAPC_systemName()                  );
        fhprcdphs.setReq_category(APCProcessDispositionEventRecord.getRequestCategory()                 );
        fhprcdphs.setClaim_memo(APCProcessDispositionEventRecord.getEventCommon().getEventMemo()           );
        fhprcdphs.setEvent_create_time(APCProcessDispositionEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = apcProcessDispositionHistoryService.insertAPCProcessDispositionHistory( fhprcdphs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createFHPRCDPHS(): InsertAPCProcessDispositionHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::createFHPRCDPHS Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::createFHPRCDPHS Function");
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param APCProcessDispositionEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 11:12
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createFHPRCDPHS_LOTS( Infos.APCProcessDispositionEventRecord APCProcessDispositionEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::createFHPRCDPHS_LOTS Function");
        Infos.Ohprcdphs_lots  fhprcdphs_lots= new Infos.Ohprcdphs_lots();
        Response iRc = returnOK();
        int           i   = 0;
        int           count = 0;
        count = length(APCProcessDispositionEventRecord.getLots());
        for( i = 0 ; i < count ; i++ ) {
            fhprcdphs_lots = new Infos.Ohprcdphs_lots();
            fhprcdphs_lots.setCtrl_job(APCProcessDispositionEventRecord.getCtrlJob()                   );
            fhprcdphs_lots.setClaim_time(APCProcessDispositionEventRecord.getEventCommon().getEventTimeStamp());
            fhprcdphs_lots.setLot_id(APCProcessDispositionEventRecord.getLots().get(i).getLotID()         );
            fhprcdphs_lots.setCast_id(APCProcessDispositionEventRecord.getLots().get(i).getCastID()        );
            fhprcdphs_lots.setLot_type(APCProcessDispositionEventRecord.getLots().get(i).getLotType()       );
            fhprcdphs_lots.setSub_lot_type(APCProcessDispositionEventRecord.getLots().get(i).getSubLotType()    );
            fhprcdphs_lots.setProdspec_id(APCProcessDispositionEventRecord.getLots().get(i).getProdSpecID()    );
            fhprcdphs_lots.setMainpd_id(APCProcessDispositionEventRecord.getLots().get(i).getMainPDID()      );
            fhprcdphs_lots.setOpe_no(APCProcessDispositionEventRecord.getLots().get(i).getOpeNo()         );
            fhprcdphs_lots.setPd_id(APCProcessDispositionEventRecord.getLots().get(i).getPdID()          );
            fhprcdphs_lots.setOpe_pass_count(APCProcessDispositionEventRecord.getLots().get(i).getOpePassCount()==null?
                    null:APCProcessDispositionEventRecord.getLots().get(i).getOpePassCount().intValue());
            fhprcdphs_lots.setPd_name(APCProcessDispositionEventRecord.getLots().get(i).getPdName()        );
            iRc = apcProcessDispositionHistoryService.insertAPCProcessDispositionLotsHistory( fhprcdphs_lots );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createFHPRCDPHS_LOTS(): InsertAPCProcessDispositionLotsHistory SQL Error Occured");
                log.info("HistoryWatchDogServer::createFHPRCDPHS_LOTS Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::createFHPRCDPHS_LOTS Function");
        return(returnOK());
    }

}
