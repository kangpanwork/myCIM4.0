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

import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;
import static com.fa.cim.utils.StringUtils.strchr;

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
public class BackupChannelEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private BackupChannelHistoryService backupChannelHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param backupChannelEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:16
     */
    public Response createBackupChannelEventRecord(Infos.BackupChannelEventRecord backupChannelEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateBackupChannelEventRecord Function" );

        Infos.Ohbucnhs       fhbucnhs;
        int                   i;
        Response iRc = returnOK();
        fhbucnhs=new Infos.Ohbucnhs();
        fhbucnhs.setCategory_level(      backupChannelEventRecord.getCategoryLevel()              );
        fhbucnhs.setCategory_id(         backupChannelEventRecord.getCategoryID()                 );
        fhbucnhs.setRoute_id(            backupChannelEventRecord.getRouteID()                    );
        fhbucnhs.setOpe_no(              backupChannelEventRecord.getOperationNumber()            );
        fhbucnhs.setHost_name(           backupChannelEventRecord.getHostName()                   );
        fhbucnhs.setServer_name(         backupChannelEventRecord.getServerName()                 );
        fhbucnhs.setIt_daemon_port(      backupChannelEventRecord.getItDaemonPort()               );
        fhbucnhs.setEntry_route_id(      backupChannelEventRecord.getEntryRouteID()               );
        fhbucnhs.setEntry_ope_no(        backupChannelEventRecord.getEntryOperationNumber()       );
        fhbucnhs.setExit_route_id(       backupChannelEventRecord.getExitRouteID()                );
        fhbucnhs.setExit_ope_no(         backupChannelEventRecord.getExitOperationNumber()        );
        fhbucnhs.setState(               backupChannelEventRecord.getState()                      );
        fhbucnhs.setStart_time(          backupChannelEventRecord.getStartTime()                  );
        fhbucnhs.setEnd_time(            backupChannelEventRecord.getEndTime()                    );
        fhbucnhs.setOpe_category(        backupChannelEventRecord.getRequest()                    );
        fhbucnhs.setClaim_time(          backupChannelEventRecord.getEventCommon().getEventTimeStamp() );
        fhbucnhs.setClaim_shop_date    ( backupChannelEventRecord.getEventCommon().getEventShopDate() );
        fhbucnhs.setClaim_user_id(       backupChannelEventRecord.getEventCommon().getUserID()         );
        fhbucnhs.setEvent_memo(          backupChannelEventRecord.getEventCommon().getEventMemo()      );
        fhbucnhs.setEvent_create_time(   backupChannelEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = backupChannelHistoryService.insertBackupChannelHistory( fhbucnhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createBackupChannelEventRecord(): InsertBackupChannelHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateBackupChannelEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateBackupChannelEventRecord Function" );
        return(returnOK());

    }

}
