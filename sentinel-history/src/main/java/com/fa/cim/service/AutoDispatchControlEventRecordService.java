package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;

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
public class AutoDispatchControlEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private AutoDispatchControlHistoryService autoDispatchControlHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param autoDispatchControlEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:26
     */
    public Response createAutoDispatchControlEventRecord(Infos.AutoDispatchControlEventRecord autoDispatchControlEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateAutoDispatchControlEventRecord Function" );

        Infos.Ohdispctrlhs fhdispctrlhs_Record;

        Response iRc = returnOK();
        fhdispctrlhs_Record=new Infos.Ohdispctrlhs();
        log.info("lotID             : "+ autoDispatchControlEventRecord.getLotID()                            );
        log.info("action            : "+ autoDispatchControlEventRecord.getAction()                             );
        log.info("routeID           : "+ autoDispatchControlEventRecord.getRouteID()                            );
        log.info("operationNumber   : "+ autoDispatchControlEventRecord.getOperationNumber()                    );
        log.info("singleTriggerFlag : "+ autoDispatchControlEventRecord.getSingleTriggerFlag()                  );
        log.info("description       : "+ autoDispatchControlEventRecord.getDescription()                        );
        log.info("claimTime         : "+ autoDispatchControlEventRecord.getEventCommon().getEventTimeStamp()         );
        log.info("claimUser         : "+ autoDispatchControlEventRecord.getEventCommon().getUserID()                 );
        log.info("claimMemo         : "+ autoDispatchControlEventRecord.getEventCommon().getEventMemo()              );
        log.info("EventCreateTime   : "+ autoDispatchControlEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhdispctrlhs_Record.setLotID             ( autoDispatchControlEventRecord.getLotID()                              );
        fhdispctrlhs_Record.setAction            ( autoDispatchControlEventRecord.getAction()                             );
        fhdispctrlhs_Record.setRouteID           ( autoDispatchControlEventRecord.getRouteID()                            );
        fhdispctrlhs_Record.setOperationNumber   ( autoDispatchControlEventRecord.getOperationNumber()                    );
        fhdispctrlhs_Record.setSingleTriggerFlag ( autoDispatchControlEventRecord.getSingleTriggerFlag());
        fhdispctrlhs_Record.setDescription       ( autoDispatchControlEventRecord.getDescription()                        );
        fhdispctrlhs_Record.setClaimTime         ( autoDispatchControlEventRecord.getEventCommon().getEventTimeStamp()         );
        fhdispctrlhs_Record.setClaimUser         ( autoDispatchControlEventRecord.getEventCommon().getUserID()                 );
        fhdispctrlhs_Record.setClaimMemo         ( autoDispatchControlEventRecord.getEventCommon().getEventMemo()              );
        fhdispctrlhs_Record.setEventCreateTime   ( autoDispatchControlEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = autoDispatchControlHistoryService.insertAutoDispatchControlHistory_FHDISPCTRLHS( fhdispctrlhs_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createAutoDispatchControlEventRecord(): InsertAutoDispatchControlHistory_FHDISPCTRLHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateAutoDispatchControlEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateAutoDispatchControlEventRecord Function" );
        return( returnOK() );
    }

}
