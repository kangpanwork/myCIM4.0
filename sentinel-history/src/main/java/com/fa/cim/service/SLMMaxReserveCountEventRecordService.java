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
public class SLMMaxReserveCountEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private SLMMaxReserveCountHistoryService slmMaxReserveCountHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param SLMMaxReserveCountEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 9:50
     */
    public Response createSLMMaxReserveCountEventRecord(Infos.SLMMaxReserveCountEventRecord SLMMaxReserveCountEventRecord, List<Infos.UserDataSet> userDataSets) {
        log.info("HistoryWatchDogServer::CreateSLMMaxReserveCountEventRecord " );

        Infos.Ohslmmaxrsvcnths            fhslmmaxrsvcnths_Record;

        Response iRc = returnOK();
        fhslmmaxrsvcnths_Record=new Infos.Ohslmmaxrsvcnths();
        log.info("equipmentID        : "+ SLMMaxReserveCountEventRecord.getMachineID() );
        log.info("machineContainerID : "+ SLMMaxReserveCountEventRecord.getMachineContainerID() );
        log.info("maxReserveCount    : "+ SLMMaxReserveCountEventRecord.getMaxReserveCount() );
        log.info("claimTime          : "+ SLMMaxReserveCountEventRecord.getEventCommon().getEventTimeStamp() );
        log.info("claimUser          : "+ SLMMaxReserveCountEventRecord.getEventCommon().getUserID() );
        log.info("claimMemo          : "+ SLMMaxReserveCountEventRecord.getEventCommon().getEventMemo() );
        log.info("EventCreateTime    : "+ SLMMaxReserveCountEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhslmmaxrsvcnths_Record.setEquipmentID        ( SLMMaxReserveCountEventRecord.getMachineID() );
        fhslmmaxrsvcnths_Record.setMachineContainerID ( SLMMaxReserveCountEventRecord.getMachineContainerID() );
        fhslmmaxrsvcnths_Record.setClaimTime          ( SLMMaxReserveCountEventRecord.getEventCommon().getEventTimeStamp()  );
        fhslmmaxrsvcnths_Record.setClaimUser          ( SLMMaxReserveCountEventRecord.getEventCommon().getUserID() );
        fhslmmaxrsvcnths_Record.setClaimMemo          ( SLMMaxReserveCountEventRecord.getEventCommon().getEventMemo() );
        fhslmmaxrsvcnths_Record.setEventCreateTime    ( SLMMaxReserveCountEventRecord.getEventCommon().getEventCreationTimeStamp() );
        fhslmmaxrsvcnths_Record.setMaxReserveCount    ( convertI(SLMMaxReserveCountEventRecord.getMaxReserveCount()) );
        iRc = slmMaxReserveCountHistoryService.insertSLMMaxReserveCountHistory_FHSLMMAXRSVCNTHS( fhslmmaxrsvcnths_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createSLMMaxReserveCountEventRecord(): InsertSLMMaxReserveCountHistory_FHSLMMAXRSVCNTHS SQL Error Occured" );
            log.info("HistoryWatchDogServer::CreateSLMMaxReserveCountEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateSLMMaxReserveCountEventRecord Function" );
        return( returnOK() );
    }

}
