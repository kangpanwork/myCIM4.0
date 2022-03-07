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
public class SLMSwitchEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private SLMSwitchHistoryService slmSwitchHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param SLMSwitchEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 9:41
     */
    public Response createSLMSwitchEventRecord(Infos.SLMSwitchEventRecord SLMSwitchEventRecord, List<Infos.UserDataSet> userDataSets) {
        log.info("HistoryWatchDogServer::CreateSLMSwitchEventRecord " );

        Infos.Ohslmswitchhs            fhslmswitchhs_Record;

        Response iRc = returnOK();
        fhslmswitchhs_Record=new Infos.Ohslmswitchhs();
        log.info("equipmentID        : "+ SLMSwitchEventRecord.getMachineID() );
        log.info("SLMSwitch          : "+ SLMSwitchEventRecord.getSLMSwitch() );
        log.info("claimTime          : "+ SLMSwitchEventRecord.getEventCommon().getEventTimeStamp() );
        log.info("claimUser          : "+ SLMSwitchEventRecord.getEventCommon().getUserID() );
        log.info("claimMemo          : "+ SLMSwitchEventRecord.getEventCommon().getEventMemo() );
        log.info("EventCreateTime    : "+ SLMSwitchEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhslmswitchhs_Record.setEquipmentID        ( SLMSwitchEventRecord.getMachineID() );
        fhslmswitchhs_Record.setSLMSwitch          ( SLMSwitchEventRecord.getSLMSwitch()  );
        fhslmswitchhs_Record.setClaimTime          ( SLMSwitchEventRecord.getEventCommon().getEventTimeStamp()  );
        fhslmswitchhs_Record.setClaimUser          ( SLMSwitchEventRecord.getEventCommon().getUserID() );
        fhslmswitchhs_Record.setClaimMemo          ( SLMSwitchEventRecord.getEventCommon().getEventMemo() );
        fhslmswitchhs_Record.setEventCreateTime    ( SLMSwitchEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = slmSwitchHistoryService.insertSLMSwitchHistory_FHSLMSWITCHHS( fhslmswitchhs_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createSLMSwitchEventRecord(): InsertSLMSwitchHistory_FHSLMSWITCHHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateSLMSwitchEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateSLMSwitchEventRecord Function" );
        return( returnOK() );
    }

}
