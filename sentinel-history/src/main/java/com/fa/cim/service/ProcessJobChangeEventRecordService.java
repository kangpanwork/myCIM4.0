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
public class ProcessJobChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ProcessJobChangeHistoryService processJobChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processJobChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/2 16:59
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createProcessJobChangeEventRecord( Infos.ProcessJobChangeEventRecord processJobChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord Function");
        Infos.Ohpjchs        fhpjchs_Record= new Infos.Ohpjchs();
        Infos.Ohpjchs_wafer  fhpjchs_wafer_Record= new Infos.Ohpjchs_wafer();
        Infos.Ohpjchs_rparm  fhpjchs_rparm_Record= new Infos.Ohpjchs_rparm();
        Response iRc = returnOK();
        fhpjchs_Record = new Infos.Ohpjchs();
        fhpjchs_wafer_Record = new Infos.Ohpjchs_wafer();
        fhpjchs_rparm_Record = new Infos.Ohpjchs_rparm();
        fhpjchs_Record.setCtrlJob(processJobChangeEventRecord.getCtrlJob()                            );
        fhpjchs_Record.setPrcsJob(processJobChangeEventRecord.getPrcsJob()                            );
        fhpjchs_Record.setOpeCategory(processJobChangeEventRecord.getOpeCategory()                        );
        fhpjchs_Record.setProcessStart(processJobChangeEventRecord.getProcessStart()                       );
        fhpjchs_Record.setCurrentState(processJobChangeEventRecord.getCurrentState()                       );
        fhpjchs_Record.setClaimTime(processJobChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhpjchs_Record.setClaim_shop_date(processJobChangeEventRecord.getEventCommon().getEventShopDate());
        fhpjchs_Record.setClaimUser(processJobChangeEventRecord.getEventCommon().getUserID()                 );
        fhpjchs_Record.setClaimMemo(processJobChangeEventRecord.getEventCommon().getEventMemo()              );
        fhpjchs_Record.setEventCreateTime(processJobChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = processJobChangeHistoryService.insertProcessJobChangeHistory_FHPJCHS( fhpjchs_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord(): InsertProcessJobChangeHistory_FHPJCHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord Function");
            return( iRc );
        }
        int waferLen = length(processJobChangeEventRecord.getWafers());
        for( int waferCnt = 0; waferCnt < waferLen; waferCnt++ ) {
            fhpjchs_wafer_Record = new Infos.Ohpjchs_wafer();
            fhpjchs_wafer_Record.setPrcsJob(processJobChangeEventRecord.getPrcsJob()                    );
            fhpjchs_wafer_Record.setWaferID(processJobChangeEventRecord.getWafers().get(waferCnt).getWaferID()   );
            fhpjchs_wafer_Record.setLotID(processJobChangeEventRecord.getWafers().get(waferCnt).getLotID()     );
            fhpjchs_wafer_Record.setClaimTime(processJobChangeEventRecord.getEventCommon().getEventTimeStamp() );
            iRc = processJobChangeHistoryService.insertProcessJobChangeHistory_FHPJCHS_WAFER( fhpjchs_wafer_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord(): InsertProcessJobChangeHistory_FHPJCHS_WAFER SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord Function");
                return( iRc );
            }
        }
        int rparmLen = length(processJobChangeEventRecord.getRecipeParameters());
        for( int rparmCnt = 0; rparmCnt < rparmLen; rparmCnt++ ) {
            fhpjchs_rparm_Record = new Infos.Ohpjchs_rparm();
            fhpjchs_rparm_Record.setPrcsJob(processJobChangeEventRecord.getPrcsJob()                                           );
            fhpjchs_rparm_Record.setParameterName(processJobChangeEventRecord.getRecipeParameters().get(rparmCnt).getParameterName()          );
            fhpjchs_rparm_Record.setPreviousParameterValue(processJobChangeEventRecord.getRecipeParameters().get(rparmCnt).getPreviousParameterValue() );
            fhpjchs_rparm_Record.setParameterValue(processJobChangeEventRecord.getRecipeParameters().get(rparmCnt).getParameterValue()         );
            fhpjchs_rparm_Record.setClaimTime(processJobChangeEventRecord.getEventCommon().getEventTimeStamp()                        );
            iRc = processJobChangeHistoryService.insertProcessJobChangeHistory_FHPJCHS_RPARM( fhpjchs_rparm_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord(): InsertProcessJobChangeHistory_FHPJCHS_RPARM SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateProcessJobChangeEventRecord Function");
        return( returnOK() );
    }

}
