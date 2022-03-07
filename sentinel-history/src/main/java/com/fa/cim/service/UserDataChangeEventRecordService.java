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
public class UserDataChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private UserDataChangeHistoryService userDataChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param UserDataChangeEventRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:00
     */
    public Response createUserDataChangeEventRecord( Infos.UserDataChangeEventRecord UserDataChangeEventRecord) {
        log.info("HistoryWatchDogServer::CreateUserDataChangeEventRecord " );

        Infos.Ohudaths         fhudaths_Record;
        Infos.OhudathsAction  fhudaths_action_Record;

        Response iRc = returnOK();
        fhudaths_Record=new Infos.Ohudaths();
        fhudaths_action_Record=new Infos.OhudathsAction();
        log.info("className        : "+ UserDataChangeEventRecord.getClassName()                          );
        log.info("hashedInfo       : "+ UserDataChangeEventRecord.getHashedInfo()                         );
        log.info("claimTime        : "+ UserDataChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        log.info("claimUser        : "+ UserDataChangeEventRecord.getEventCommon().getUserID()                 );
        log.info("claimMemo        : "+ UserDataChangeEventRecord.getEventCommon().getEventMemo()              );
        log.info("EventCreateTime  : "+ UserDataChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhudaths_Record.setClassName       ( UserDataChangeEventRecord.getClassName()                          );
        fhudaths_Record.setHashedInfo      ( UserDataChangeEventRecord.getHashedInfo()                         );
        fhudaths_Record.setClaimTime       ( UserDataChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhudaths_Record.setClaimUser       ( UserDataChangeEventRecord.getEventCommon().getUserID()                 );
        fhudaths_Record.setClaimMemo       ( UserDataChangeEventRecord.getEventCommon().getEventMemo()              );
        fhudaths_Record.setEventCreateTime ( UserDataChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = userDataChangeHistoryService.insertUserDataChangeHistory_FHUDATHS( fhudaths_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createUserDataChangeEventRecord(): InsertUserDataChangeHistory_FHUDATHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateUserDataChangeEventRecord Function" );
            return( iRc );
        }
        int actionLen = length(UserDataChangeEventRecord.getActions());

        for( int actionCnt = 0; actionCnt < actionLen; actionCnt++ ) {
            fhudaths_action_Record=new Infos.OhudathsAction();
            fhudaths_action_Record.setClassName  ( UserDataChangeEventRecord.getClassName()                     );
            fhudaths_action_Record.setHashedInfo ( UserDataChangeEventRecord.getHashedInfo()                    );
            fhudaths_action_Record.setName       ( UserDataChangeEventRecord.getActions().get(actionCnt).getName()       );
            fhudaths_action_Record.setOrig       ( UserDataChangeEventRecord.getActions().get(actionCnt).getOrig()       );
            fhudaths_action_Record.setActionCode ( UserDataChangeEventRecord.getActions().get(actionCnt).getActionCode() );
            fhudaths_action_Record.setFromType   ( UserDataChangeEventRecord.getActions().get(actionCnt).getFromType()   );
            fhudaths_action_Record.setFromValue  ( UserDataChangeEventRecord.getActions().get(actionCnt).getFromValue()  );
            fhudaths_action_Record.setToType     ( UserDataChangeEventRecord.getActions().get(actionCnt).getToType()     );
            fhudaths_action_Record.setToValue    ( UserDataChangeEventRecord.getActions().get(actionCnt).getToValue()    );
            fhudaths_action_Record.setClaimTime  ( UserDataChangeEventRecord.getEventCommon().getEventTimeStamp()    );
            iRc = userDataChangeHistoryService.insertUserDataChangeHistory_FHUDATHS_ACTION( fhudaths_action_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createUserDataChangeEventRecord(): InsertUserDataChangeHistory_FHUDATHS_ACTION SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateUserDataChangeEventRecord Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::CreateUserDataChangeEventRecord Function" );
        return( returnOK() );
    }

}
