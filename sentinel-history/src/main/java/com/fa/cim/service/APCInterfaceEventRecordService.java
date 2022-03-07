package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
//@Transactional(rollbackFor = Exception.class)
public class APCInterfaceEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private APCInterfaceHistoryService apcInterfaceHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param APCInterfaceEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 13:26
     */
    public Response createAPCInterfaceEventRecord(Infos.APCInterfaceEventRecord APCInterfaceEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateAPCInterfaceEventRecord Function" );

        Infos.Ohapcifhs      fhapcifhs;
        Response iRc = returnOK();
        fhapcifhs=new Infos.Ohapcifhs();
        fhapcifhs.setEquipmentID(              APCInterfaceEventRecord.getEquipmentID()              );
        fhapcifhs.setAPC_systemName(           APCInterfaceEventRecord.getAPC_systemName()           );
        fhapcifhs.setOperationCategory(        APCInterfaceEventRecord.getOperationCategory()        );
        fhapcifhs.setEquipmentDescription(     APCInterfaceEventRecord.getEquipmentDescription()     );
        fhapcifhs.setIgnoreAbleFlag          ( convertI(APCInterfaceEventRecord.getIgnoreAbleFlag())            );
        fhapcifhs.setAPC_responsibleUserID(    APCInterfaceEventRecord.getAPC_responsibleUserID()    );
        fhapcifhs.setAPC_subResponsibleUserID( APCInterfaceEventRecord.getAPC_subResponsibleUserID() );
        fhapcifhs.setAPC_configState(          APCInterfaceEventRecord.getAPC_configState()          );
        fhapcifhs.setAPC_registeredUserID(     APCInterfaceEventRecord.getAPC_registeredUserID()     );
        fhapcifhs.setRegisteredTime(           APCInterfaceEventRecord.getRegisteredTime()           );
        fhapcifhs.setRegisteredMemo(           APCInterfaceEventRecord.getRegisteredMemo()           );
        fhapcifhs.setApprovedUserID(           APCInterfaceEventRecord.getApprovedUserID()           );
        fhapcifhs.setApprovedTime(             APCInterfaceEventRecord.getApprovedTime()             );
        fhapcifhs.setApprovedMemo(             APCInterfaceEventRecord.getApprovedMemo()             );
        fhapcifhs.setEventCreationTimeStamp(   APCInterfaceEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = apcInterfaceHistoryService.insertAPCInterfaceEventRecord_FHAPCIFHS( fhapcifhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createAPCInterfaceEventRecord(): InsertAPCInterfaceEventHistory_fhapcifhs SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateAPCInterfaceEventRecord_fhapcifhs Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateAPCInterfaceEventRecord Function" );
        return(returnOK());

    }

}
