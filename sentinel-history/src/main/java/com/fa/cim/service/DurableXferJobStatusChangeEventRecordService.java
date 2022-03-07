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

import static com.fa.cim.Constant.SPConstant.CRITERIA_NA;
import static com.fa.cim.Constant.SPConstant.SP_MOVEMENTTYPE_NONMOVE;
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
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableXferJobStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableXferJobStatusChangeHistoryService durableXferJobStatusChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableXferJobStatusChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 13:33
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createDurableXferJobStatusChangeEventRecord( Infos.DurableXferJobStatusChangeEventRecord durableXferJobStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableXferJobStatusChangeEventRecord Function");
        Infos.Ohxferjobhs fhxferjobhs= new Infos.Ohxferjobhs();
        fhxferjobhs = new Infos.Ohxferjobhs();
        Response iRc = returnOK();
        fhxferjobhs.setDurable_type(durableXferJobStatusChangeEventRecord.getDurableType()                        );
        fhxferjobhs.setOpe_category(durableXferJobStatusChangeEventRecord.getOperationCategory()                  );
        fhxferjobhs.setCarrier_id(durableXferJobStatusChangeEventRecord.getCarrierID()                          );
        fhxferjobhs.setJob_id(durableXferJobStatusChangeEventRecord.getJobID()                              );
        fhxferjobhs.setCarrier_job_id(durableXferJobStatusChangeEventRecord.getCarrierJobID()                       );
        fhxferjobhs.setTransport_type(durableXferJobStatusChangeEventRecord.getTransportType()                      );
        fhxferjobhs.setZone_type(durableXferJobStatusChangeEventRecord.getZoneType()                           );
        fhxferjobhs.setN2purge_flag(durableXferJobStatusChangeEventRecord.getN2purgeFlag()==null?
                null:durableXferJobStatusChangeEventRecord.getN2purgeFlag().intValue());
        fhxferjobhs.setFrom_machine_id(durableXferJobStatusChangeEventRecord.getFromMachineID()                      );
        fhxferjobhs.setFrom_port_id(durableXferJobStatusChangeEventRecord.getFromPortID()                         );
        fhxferjobhs.setTo_stocker_group(durableXferJobStatusChangeEventRecord.getToStockerGroup()                     );
        fhxferjobhs.setTo_machine_id(durableXferJobStatusChangeEventRecord.getToMachineID()                        );
        fhxferjobhs.setTo_port_id(durableXferJobStatusChangeEventRecord.getToPortID()                           );
        fhxferjobhs.setExpected_start_time(durableXferJobStatusChangeEventRecord.getExpectedStrtTime()                   );
        fhxferjobhs.setExpected_end_time(durableXferJobStatusChangeEventRecord.getExpectedEndTime()                    );
        fhxferjobhs.setEstimate_start_time(durableXferJobStatusChangeEventRecord.getEstimateStrtTime()                   );
        fhxferjobhs.setEstimate_end_time(durableXferJobStatusChangeEventRecord.getEstimateEndTime()                    );
        fhxferjobhs.setMandatory_flag(durableXferJobStatusChangeEventRecord.getMandatoryFlag()==null?
                null:durableXferJobStatusChangeEventRecord.getMandatoryFlag().intValue());
        fhxferjobhs.setPriority(durableXferJobStatusChangeEventRecord.getPriority()                           );
        fhxferjobhs.setJob_status(durableXferJobStatusChangeEventRecord.getJobStatus()                          );
        fhxferjobhs.setCarrier_job_status(durableXferJobStatusChangeEventRecord.getCarrierJobStatus()                   );
        fhxferjobhs.setClaim_time(durableXferJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhxferjobhs.setClaim_user_id(durableXferJobStatusChangeEventRecord.getEventCommon().getUserID()                 );
        fhxferjobhs.setClaim_memo(durableXferJobStatusChangeEventRecord.getEventCommon().getEventMemo()              );
        fhxferjobhs.setStore_time(""                                                                        );
        fhxferjobhs.setEvent_create_time(durableXferJobStatusChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = durableXferJobStatusChangeHistoryService.insertDurableXferJobStatusChangeHistory( fhxferjobhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableXferJobStatusChangeEventRecord(): InsertDurableXferJobStatusChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurableXferJobStatusChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateDurableXferJobStatusChangeEventRecord Function");
        return ( returnOK() );
    }

}
