package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;

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
public class EqpMonitorEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EqpMonitorHistoryService eqpMonitorHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpMonitorEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 19:23
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createEqpMonitorEventRecord( Infos.EqpMonitorEventRecord eqpMonitorEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
        Infos.Oheqpmonhs             fheqpmonhs_Record= new Infos.Oheqpmonhs();
        Infos.Oheqpmonhs_def         fheqpmonhs_def_Record= new Infos.Oheqpmonhs_def();
        Infos.Oheqpmonhs_defaction   fheqpmonhs_defaction_Record= new Infos.Oheqpmonhs_defaction();
        Infos.Oheqpmonhs_defprodspec fheqpmonhs_defprodspec_Record= new Infos.Oheqpmonhs_defprodspec();
        Infos.Oheqpmonhs_schchg      fheqpmonhs_schchg_Record= new Infos.Oheqpmonhs_schchg();
        Response iRc = returnOK();
        fheqpmonhs_Record = new Infos.Oheqpmonhs();
        fheqpmonhs_def_Record = new Infos.Oheqpmonhs_def();
        fheqpmonhs_defaction_Record = new Infos.Oheqpmonhs_defaction();
        fheqpmonhs_defprodspec_Record = new Infos.Oheqpmonhs_defprodspec();
        fheqpmonhs_schchg_Record = new Infos.Oheqpmonhs_schchg();
        fheqpmonhs_Record.setOpeCategory(eqpMonitorEventRecord.getOpeCategory()                 );
        fheqpmonhs_Record.setEqpID(eqpMonitorEventRecord.getEquipmentID()                 );
        fheqpmonhs_Record.setChamberID(eqpMonitorEventRecord.getChamberID()                   );
        fheqpmonhs_Record.setEqpMonID(eqpMonitorEventRecord.getEqpMonitorID()                );
        fheqpmonhs_Record.setMonitorType(eqpMonitorEventRecord.getMonitorType()                 );
        fheqpmonhs_Record.setMonitorStatus(eqpMonitorEventRecord.getMonitorStatus()               );
        fheqpmonhs_Record.setPrevMonitorStatus(eqpMonitorEventRecord.getPrevMonitorStatus()           );
        fheqpmonhs_Record.setClaimTime(eqpMonitorEventRecord.getEventCommon().getEventTimeStamp()         );
        fheqpmonhs_Record.setClaimUser(eqpMonitorEventRecord.getEventCommon().getUserID()                 );
        fheqpmonhs_Record.setClaimMemo(eqpMonitorEventRecord.getEventCommon().getEventMemo()              );
        fheqpmonhs_Record.setEventCreateTime(eqpMonitorEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = eqpMonitorHistoryService.insertEqpMonHistory_FHEQPMONHS( fheqpmonhs_Record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord(): InsertEqpMonHistory_FHEQPMONHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
            return( iRc );
        }
        if( 0 == variableStrCmp(eqpMonitorEventRecord.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_CREATE)
                || 0 == variableStrCmp(eqpMonitorEventRecord.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_UPDATE) ) {
            fheqpmonhs_def_Record.setEqpMonID(eqpMonitorEventRecord.getEqpMonitorID() );
            fheqpmonhs_def_Record.setDescription(eqpMonitorEventRecord.getMonitorDefs().get(0).getDescription() );
            fheqpmonhs_def_Record.setScheduleType(eqpMonitorEventRecord.getMonitorDefs().get(0).getScheduleType() );
            fheqpmonhs_def_Record.setStartTimeStamp(eqpMonitorEventRecord.getMonitorDefs().get(0).getStartTimeStamp() );
            fheqpmonhs_def_Record.setExecutionInterval(eqpMonitorEventRecord.getMonitorDefs().get(0).getExecutionInterval());
            fheqpmonhs_def_Record.setWarningInterval(eqpMonitorEventRecord.getMonitorDefs().get(0).getWarningInterval());
            fheqpmonhs_def_Record.setExpirationInterval(eqpMonitorEventRecord.getMonitorDefs().get(0).getExpirationInterval());
            fheqpmonhs_def_Record.setStandAloneFlag(eqpMonitorEventRecord.getMonitorDefs().get(0).getStandAloneFlag());
            fheqpmonhs_def_Record.setKitFlag(eqpMonitorEventRecord.getMonitorDefs().get(0).getKitFlag());
            fheqpmonhs_def_Record.setMaxRetryCount(eqpMonitorEventRecord.getMonitorDefs().get(0).getMaxRetryCount());
            fheqpmonhs_def_Record.setMachineStateAtStart(eqpMonitorEventRecord.getMonitorDefs().get(0).getMachineStateAtStart());
            fheqpmonhs_def_Record.setMachineStateAtPassed(eqpMonitorEventRecord.getMonitorDefs().get(0).getMachineStateAtPassed());
            fheqpmonhs_def_Record.setMachineStateAtFailed(eqpMonitorEventRecord.getMonitorDefs().get(0).getMachineStateAtFailed());
            fheqpmonhs_def_Record.setClaimTime(eqpMonitorEventRecord.getEventCommon().getEventTimeStamp());
            iRc = eqpMonitorHistoryService.insertEqpMonHistory_FHEQPMONHS_DEF( fheqpmonhs_def_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord(): InsertEqpMonHistory_FHEQPMONHS_DEF SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
                return( iRc );
            }
            int nProd = length(eqpMonitorEventRecord.getMonitorDefprods());
            for(int i = 0; i< nProd; i++ ) {
                fheqpmonhs_defprodspec_Record = new Infos.Oheqpmonhs_defprodspec();
                fheqpmonhs_defprodspec_Record.setEqpMonID(eqpMonitorEventRecord.getEqpMonitorID() );
                fheqpmonhs_defprodspec_Record.setProdSpecID(eqpMonitorEventRecord.getMonitorDefprods().get(i).getProductSpecificationID());
                fheqpmonhs_defprodspec_Record.setWaferCount(eqpMonitorEventRecord.getMonitorDefprods().get(i).getWaferCount());
                fheqpmonhs_defprodspec_Record.setStartSeqNo(eqpMonitorEventRecord.getMonitorDefprods().get(i).getStartSeqNo());
                fheqpmonhs_defprodspec_Record.setClaimTime(eqpMonitorEventRecord.getEventCommon().getEventTimeStamp());
                iRc = eqpMonitorHistoryService.insertEqpMonHistory_FHEQPMONHS_DEFPRODSPEC( fheqpmonhs_defprodspec_Record );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord(): InsertEqpMonHistory_FHEQPMONHS_DEFPRODSPEC SQL Error Occured");
                    log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
                    return( iRc );
                }
            }
            int nAct = length(eqpMonitorEventRecord.getMonitorDefactions());
            for(int j = 0; j<nAct; j++ ) {
                fheqpmonhs_defaction_Record = new Infos.Oheqpmonhs_defaction();
                fheqpmonhs_defaction_Record.setEqpMonID(eqpMonitorEventRecord.getEqpMonitorID() );
                fheqpmonhs_defaction_Record.setEventType(eqpMonitorEventRecord.getMonitorDefactions().get(j).getEventType());
                fheqpmonhs_defaction_Record.setAction(eqpMonitorEventRecord.getMonitorDefactions().get(j).getAction());
                fheqpmonhs_defaction_Record.setReasonCode(eqpMonitorEventRecord.getMonitorDefactions().get(j).getReasonCode());
                fheqpmonhs_defaction_Record.setSysMessageID(eqpMonitorEventRecord.getMonitorDefactions().get(j).getSysMessageID());
                fheqpmonhs_defaction_Record.setClaimTime(eqpMonitorEventRecord.getEventCommon().getEventTimeStamp());
                iRc = eqpMonitorHistoryService.insertEqpMonHistory_FHEQPMONHS_DEFACTION( fheqpmonhs_defaction_Record );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord(): InsertEqpMonHistory_FHEQPMONHS_DEFACTION SQL Error Occured");
                    log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
                    return( iRc );
                }
            }
        }
        else if ( 0 == variableStrCmp(eqpMonitorEventRecord.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_POSTPONE)
                || 0 == variableStrCmp(eqpMonitorEventRecord.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_SKIP)
                || 0 == variableStrCmp(eqpMonitorEventRecord.getOpeCategory(), SP_EQPMONITOR_OPECATEGORY_FORCERUN) ) {
            fheqpmonhs_schchg_Record.setEqpMonID(eqpMonitorEventRecord.getEqpMonitorID() );
            fheqpmonhs_schchg_Record.setPrevNextExecutionTime(eqpMonitorEventRecord.getMonitorSchchgs().get(0).getPrevNextExecutionTime());
            fheqpmonhs_schchg_Record.setNextExecutionTime(eqpMonitorEventRecord.getMonitorSchchgs().get(0).getNextExecutionTime());
            fheqpmonhs_schchg_Record.setClaimTime(eqpMonitorEventRecord.getEventCommon().getEventTimeStamp());
            iRc = eqpMonitorHistoryService.insertEqpMonHistory_FHEQPMONHS_SCHCHG( fheqpmonhs_schchg_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord(): InsertEqpMonHistory_FHEQPMONHS_SCHCHG SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateEqpMonitorEventRecord Function");
        return( returnOK() );
    }

}
