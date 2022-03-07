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

import static com.fa.cim.utils.BaseUtils.*;
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
public class BackupOperationEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private BackupOperationHistoryService backupOperationHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param backupOperationEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 13:53
     */
    public Response createBackupOperationEventRecord(Infos.BackupOperationEventRecord backupOperationEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );

        Infos.Ohbuophs       fhbuophs;
        int                   i;
        Response iRc = returnOK();
        Params.String castCategory = new Params.String();
        Params.String moduleNo = new Params.String();
        Params.String stageID = new Params.String();
        Params.String stageGrpID = new Params.String();
        Infos.Frpd           resultData_pd;
        Infos.Frlot          resultData_lot;
        fhbuophs=new Infos.Ohbuophs();
        resultData_pd=new Infos.Frpd();
        resultData_lot=new Infos.Frlot();
        iRc = tableMethod.getFRCAST( backupOperationEventRecord.getLotData().getCassetteID(), castCategory );
        {
        }
        iRc = tableMethod.getFRPD( backupOperationEventRecord.getLotData().getOperationID() , resultData_pd ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
            return( iRc );
        }
        iRc = tableMethod.getFRLOT( backupOperationEventRecord.getLotData().getLotID() , resultData_lot ) ;
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
            return( iRc );
        }
       moduleNo.setValue(backupOperationEventRecord.getLotData().getOperationNumber());
        strchr(moduleNo,'.');
        iRc = tableMethod.getFRPF( backupOperationEventRecord.getLotData().getObjrefMainPF(), moduleNo.getValue(), stageID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
            return( iRc );
        }

        iRc = tableMethod.getFRSTAGE( stageID.getValue(), stageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
            return( iRc );
        }
        fhbuophs.setLot_id(              backupOperationEventRecord.getLotData().getLotID()                 );
        fhbuophs.setLot_type(            backupOperationEventRecord.getLotData().getLotType()               );
        fhbuophs.setCast_id(             backupOperationEventRecord.getLotData().getCassetteID()            );
        fhbuophs.setCast_category(       castCategory.getValue()                                              );
        fhbuophs.setMainpd_id(           backupOperationEventRecord.getLotData().getRouteID()               );
        fhbuophs.setOpe_no(              backupOperationEventRecord.getLotData().getOperationNumber()       );
        fhbuophs.setPd_id(               backupOperationEventRecord.getLotData().getOperationID()           );
        fhbuophs.setOpe_pass_count     ( backupOperationEventRecord.getLotData().getOperationPassCount());
        fhbuophs.setPd_name(             resultData_pd.getOperationName()                               );
        fhbuophs.setClaim_time(          backupOperationEventRecord.getEventCommon().getEventTimeStamp()    );
        fhbuophs.setClaim_shop_date    ( backupOperationEventRecord.getEventCommon().getEventShopDate());
        fhbuophs.setClaim_user_id(       backupOperationEventRecord.getEventCommon().getUserID()            );
        fhbuophs.setOpe_category(        backupOperationEventRecord.getRequest()                       );
        fhbuophs.setHost_name(           backupOperationEventRecord.getHostName()                      );
        fhbuophs.setServer_name(         backupOperationEventRecord.getServerName()                    );
        fhbuophs.setIt_daemon_port(      backupOperationEventRecord.getItDaemonPort()                  );
        fhbuophs.setEntry_route_id(      backupOperationEventRecord.getEntryRouteID()                  );
        fhbuophs.setEntry_ope_no(        backupOperationEventRecord.getEntryOperationNumber()          );
        fhbuophs.setExit_route_id(       backupOperationEventRecord.getExitRouteID()                   );
        fhbuophs.setExit_ope_no(         backupOperationEventRecord.getExitOperationNumber()           );
        fhbuophs.setProdspec_id(         backupOperationEventRecord.getLotData().getProductID()             );
        fhbuophs.setCustomer_id(         backupOperationEventRecord.getLotData().getCustomerID()            );
        fhbuophs.setStage_id(            stageID.getValue()                                                   );

        fhbuophs.setStagegrp_id(         stageGrpID.getValue()                                                );
        fhbuophs.setHold_state(          backupOperationEventRecord.getLotData().getHoldState()             );
        fhbuophs.setBank_id(             backupOperationEventRecord.getLotData().getBankID()                );
        fhbuophs.setOrg_wafer_qty      ( backupOperationEventRecord.getLotData().getOriginalWaferQuantity());
        fhbuophs.setCur_wafer_qty      ( backupOperationEventRecord.getLotData().getCurrentWaferQuantity());
        fhbuophs.setProd_wafer_qty     ( backupOperationEventRecord.getLotData().getProductWaferQuantity());
        fhbuophs.setCntl_wafer_qty     ( backupOperationEventRecord.getLotData().getControlWaferQuantity());
        fhbuophs.setLot_owner_id(        resultData_lot.getLotOwner()                                   );
        fhbuophs.setPlan_end_time(       resultData_lot.getPlanEndTime()                                );
        fhbuophs.setWfrhs_time(          backupOperationEventRecord.getLotData().getWaferHistoryTimeStamp() );
        fhbuophs.setEvent_memo(          backupOperationEventRecord.getEventCommon().getEventMemo()         );
        fhbuophs.setEvent_create_time(   backupOperationEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = backupOperationHistoryService.insertBackupOperationHistory( fhbuophs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createBackupOperationEventRecord(): InsertBackupOperationHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateBackupOperationEventRecord Function" );
        return(returnOK());

    }

}
