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
public class OwnerChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private OwnerChangeHistoryService ownerChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param ownerChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:48
     */
    public Response createOwnerChangeEventRecord(Infos.OwnerChangeEventRecord ownerChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateOwnerChangeEventRecord Function" );

        Infos.Ohowchhs         fhowchhs_Record;
        Infos.OhowchhsChgobj  fhowchhs_chgobj_Record;

        Response iRc = returnOK();
        fhowchhs_Record=new Infos.Ohowchhs();
        fhowchhs_chgobj_Record=new Infos.OhowchhsChgobj();
        log.info("fromOwnerID      : "+ ownerChangeEventRecord.getFromOwnerID()                        );
        log.info("toOwnerID        : "+ ownerChangeEventRecord.getToOwnerID()                          );
        log.info("claimTime        : "+ ownerChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        log.info("claimUser        : "+ ownerChangeEventRecord.getEventCommon().getUserID()                 );
        log.info("claimMemo        : "+ ownerChangeEventRecord.getEventCommon().getEventMemo()              );
        log.info("EventCreateTime  : "+ ownerChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhowchhs_Record.setFromOwnerID     ( ownerChangeEventRecord.getFromOwnerID()                        );
        fhowchhs_Record.setToOwnerID       ( ownerChangeEventRecord.getToOwnerID()                          );
        fhowchhs_Record.setClaimTime       ( ownerChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhowchhs_Record.setClaimUser       ( ownerChangeEventRecord.getEventCommon().getUserID()                 );
        fhowchhs_Record.setClaimMemo       ( ownerChangeEventRecord.getEventCommon().getEventMemo()              );
        fhowchhs_Record.setEventCreateTime ( ownerChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = ownerChangeHistoryService.insertOwnerChangeHistory_FHOWCHHS( fhowchhs_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createOwnerChangeEventRecord(): InsertOwnerChangeHistory_FHOWCHHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateOwnerChangeEventRecord Function" );
            return( iRc );
        }
        int objLen = length(ownerChangeEventRecord.getChangeObjects());

        for( int objCnt = 0; objCnt < objLen; objCnt++ ) {
            fhowchhs_chgobj_Record=new Infos.OhowchhsChgobj();
            fhowchhs_chgobj_Record.setFromOwnerID ( ownerChangeEventRecord.getFromOwnerID()                      );
            fhowchhs_chgobj_Record.setToOwnerID   ( ownerChangeEventRecord.getToOwnerID()                        );
            fhowchhs_chgobj_Record.setObjectName  ( ownerChangeEventRecord.getChangeObjects().get(objCnt).getObjectName() );
            fhowchhs_chgobj_Record.setHashedInfo  ( ownerChangeEventRecord.getChangeObjects().get(objCnt).getHashedInfo() );
            fhowchhs_chgobj_Record.setClaimTime   ( ownerChangeEventRecord.getEventCommon().getEventTimeStamp()       );
            iRc = ownerChangeHistoryService.insertOwnerChangeHistory_FHOWCHHS_CHGOBJ( fhowchhs_chgobj_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createOwnerChangeEventRecord(): InsertOwnerChangeHistory_FHOWCHHS_CHGOBJ SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateOwnerChangeEventRecord Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::CreateOwnerChangeEventRecord Function" );
        return( returnOK() );
    }

}
