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

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.OAMNW002_ID;
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
public class EqpMonitorJobEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EqpMonitorJobHistoryService eqpMonitorJobHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param eqpMonitorJobEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 13:44
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createEqpMonitorJobEventRecord( Infos.EqpMonitorJobEventRecord eqpMonitorJobEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord Function");
        Infos.Oheqpmonjobhs             fhemjobhs_Record= new Infos.Oheqpmonjobhs();
        Infos.Oheqpmonjobhs_lot         fhemjobhs_lot_Record= new Infos.Oheqpmonjobhs_lot();
        Infos.Ohopehs                   fhopehs= new Infos.Ohopehs();
        Infos.Frlot                     lotData= new Infos.Frlot();
        Infos.Frpd                      pdData= new Infos.Frpd();
        Response iRc = returnOK();
        Params.String                  equipmentName  = new Params.String();
        Params.String                  areaID         = new Params.String();
        fhemjobhs_Record = new Infos.Ohdispctrlhs();
        fhemjobhs_lot_Record = new Infos.Oheqpmonjobhs_lot();
        fhopehs = new Infos.Ohopehs();
        lotData = new Infos.Frlot();
        pdData = new Infos.Frpd();
        if ( 0 == variableStrCmp(eqpMonitorJobEventRecord.getOpeCategory(),SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE)
                || 0 == variableStrCmp(eqpMonitorJobEventRecord.getOpeCategory(),SP_EQPMONITORJOB_OPECATEGORY_EQPMONITORLOTFAILED) ) {
            fhemjobhs_Record.setOpeCategory(eqpMonitorJobEventRecord.getOpeCategory()                 );
            fhemjobhs_Record.setEqpID(eqpMonitorJobEventRecord.getEquipmentID()                 );
            fhemjobhs_Record.setChamberID(eqpMonitorJobEventRecord.getChamberID()                   );
            fhemjobhs_Record.setEqpMonID(eqpMonitorJobEventRecord.getEqpMonitorID()                );
            fhemjobhs_Record.setEqpMonJobID(eqpMonitorJobEventRecord.getEqpMonitorJobID()             );
            fhemjobhs_Record.setMonJobStatus(eqpMonitorJobEventRecord.getMonitorJobStatus()            );
            fhemjobhs_Record.setPrevMonJobStatus(eqpMonitorJobEventRecord.getPrevMonitorJobStatus()        );
            fhemjobhs_Record.setRetryCount(eqpMonitorJobEventRecord.getRetryCount());
            fhemjobhs_Record.setClaimTime(eqpMonitorJobEventRecord.getEventCommon().getEventTimeStamp()         );
            fhemjobhs_Record.setClaimUser(eqpMonitorJobEventRecord.getEventCommon().getUserID()                 );
            fhemjobhs_Record.setClaimMemo(eqpMonitorJobEventRecord.getEventCommon().getEventMemo()              );
            fhemjobhs_Record.setEventCreateTime(eqpMonitorJobEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = eqpMonitorJobHistoryService.insertEqpMonJobHistory_FHEQPMONJOBHS( fhemjobhs_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord(): InsertEqpMonJobHistory_FHEQPMONJOBHS SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord Function");
                return( iRc );
            }
            if ( 0 == variableStrCmp(eqpMonitorJobEventRecord.getMonitorJobStatus(),SP_EQPMONITORJOB_STATUS_RESERVED)
                    || 0 == variableStrCmp(eqpMonitorJobEventRecord.getOpeCategory(),SP_EQPMONITORJOB_OPECATEGORY_EQPMONITORLOTFAILED)
                    || ( 0 == variableStrCmp(eqpMonitorJobEventRecord.getMonitorJobStatus(), SP_EQPMONITORJOB_STATUS_READY) && 0 == variableStrCmp(eqpMonitorJobEventRecord.getEventCommon().getTransactionID(),OAMNW002_ID)) ) {
                int nLots = length(eqpMonitorJobEventRecord.getMonitorLots());
                for(int i = 0; i<nLots; i++ ) {
                    fhemjobhs_lot_Record = new Infos.Oheqpmonjobhs_lot();
                    fhemjobhs_lot_Record.setEqpMonJobID(eqpMonitorJobEventRecord.getEqpMonitorJobID());
                    fhemjobhs_lot_Record.setEventType(eqpMonitorJobEventRecord.getOpeCategory());
                    fhemjobhs_lot_Record.setLotID(eqpMonitorJobEventRecord.getMonitorLots().get(i).getLotID());
                    fhemjobhs_lot_Record.setProdSpecID(eqpMonitorJobEventRecord.getMonitorLots().get(i).getProductSpecificationID());
                    fhemjobhs_lot_Record.setStartSeqNo(eqpMonitorJobEventRecord.getMonitorLots().get(i).getStartSeqNo());
                    fhemjobhs_lot_Record.setClaimTime(eqpMonitorJobEventRecord.getEventCommon().getEventTimeStamp());
                    iRc = eqpMonitorJobHistoryService.insertEqpMonJobHistory_FHEQPMONJOBHS_LOT( fhemjobhs_lot_Record );
                    if( !isOk(iRc) ) {
                        log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord(): InsertEqpMonJobHistory_FHEQPMONJOBHS_LOT SQL Error Occured");
                        log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord Function");
                        return( iRc );
                    }
                }
            }
        }
        if ( 0 == variableStrCmp(eqpMonitorJobEventRecord.getOpeCategory(),SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART)
                || 0 == variableStrCmp(eqpMonitorJobEventRecord.getOpeCategory(),SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP) ) {
            int nLots = length(eqpMonitorJobEventRecord.getMonitorLots());
            for(int j = 0; j<nLots; j++ ) {
                fhopehs = new Infos.Ohopehs();
                lotData = new Infos.Frlot();
                iRc = tableMethod.getFRLOT( eqpMonitorJobEventRecord.getMonitorLots().get(j).getLotID(), lotData );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getFRLOT Function");
                    return( iRc );
                }
                fhopehs.setLot_id(eqpMonitorJobEventRecord.getMonitorLots().get(j).getLotID());
                fhopehs.setLot_type(lotData.getLotType() );
                fhopehs.setSub_lot_type(lotData.getSubLotType() );
                fhopehs.setLot_owner_id(lotData.getLotOwner() );
                fhopehs.setPlan_end_time(lotData.getPlanEndTime() );
                fhopehs.setMfg_layer(lotData.getMfgLayer() );
                fhopehs.setCustomer_id(lotData.getCustomerID() );
                fhopehs.setOrder_no(lotData.getOrderNO() );
                fhopehs.setExt_priority(lotData.getPriority() );
                fhopehs.setPriority_class(lotData.getPriorityClass() );
                Params.String                  productGrpID   = new Params.String();
                Params.String                  prodType       = new Params.String();
                iRc = tableMethod.getFRPRODSPEC( eqpMonitorJobEventRecord.getMonitorLots().get(j).getProductSpecificationID() ,productGrpID , prodType );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getFRPRODSPEC Function");
                    return( iRc );
                }
                fhopehs.setProdgrp_id(productGrpID.getValue() );
                fhopehs.setProd_type(prodType.getValue() );
                Params.String                  techID         = new Params.String();
                iRc = tableMethod.getFRPRODGRP( eqpMonitorJobEventRecord.getMonitorLots().get(j).getProductSpecificationID() , techID );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getFRPRODGRP Function");
                    return( iRc );
                }
                fhopehs.setTech_id(techID.getValue() );
                Params.String                  custprodID     = new Params.String();
                iRc = tableMethod.getFRCUSTPROD( eqpMonitorJobEventRecord.getMonitorLots().get(j).getLotID(),eqpMonitorJobEventRecord.getMonitorLots().get(j).getProductSpecificationID() , custprodID );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getFRCUSTPROD Function");
                    return( iRc );
                }
                fhopehs.setCustprod_id(custprodID.getValue() );
                fhopehs.setReticle_count(0 );
                fhopehs.setFixture_count(0 );
                fhopehs.setRparm_count(0 );
                fhopehs.setInit_hold_flag(0 );
                fhopehs.setLast_hldrel_flag(0 );
                fhopehs.setRework_count(0 );
                fhopehs.setTotal_good_unit(0 );
                fhopehs.setTotal_fail_unit(0 );
                fhopehs.setCriteria_flag(CRITERIA_NA);
                fhopehs.setHold_time("1901-01-01-00.00.00.000000");
                fhopehs.setWfrhs_time("1901-01-01-00.00.00.000000");
                fhopehs.setOpe_category(eqpMonitorJobEventRecord.getOpeCategory());
                fhopehs.setEqpMonJobID(eqpMonitorJobEventRecord.getEqpMonitorJobID());
                fhopehs.setMainpd_id(eqpMonitorJobEventRecord.getMonitorLots().get(j).getMainPDID());
                fhopehs.setOpe_no(eqpMonitorJobEventRecord.getMonitorLots().get(j).getOpeNo());
                fhopehs.setPd_id(eqpMonitorJobEventRecord.getMonitorLots().get(j).getPdID());
                fhopehs.setOpe_pass_count(eqpMonitorJobEventRecord.getMonitorLots().get(j).getOpePassCount()==null?
                        null:eqpMonitorJobEventRecord.getMonitorLots().get(j).getOpePassCount().intValue());
                fhopehs.setMove_type(SP_MOVEMENTTYPE_NONMOVE );
                fhopehs.setEqp_id(eqpMonitorJobEventRecord.getEquipmentID());
                fhopehs.setProdspec_id(eqpMonitorJobEventRecord.getMonitorLots().get(j).getProductSpecificationID());
                iRc = tableMethod.getFREQP( eqpMonitorJobEventRecord.getEquipmentID(), areaID, equipmentName );
                if ( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getOMEQP Function");
                    return iRc;
                }
                fhopehs.setEqp_name(equipmentName.getValue() );
                iRc = tableMethod.getFRPD( eqpMonitorJobEventRecord.getMonitorLots().get(j).getPdID(), pdData );
                if ( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord-getFRPD Function");
                    return iRc;
                }
                fhopehs.setPd_name(pdData.getOperationName() );
                fhopehs.setTest_type(pdData.getTestType() );
                fhopehs.setPd_type(pdData.getPd_type() );
                fhopehs.setClaim_time(eqpMonitorJobEventRecord.getEventCommon().getEventTimeStamp()         );
                fhopehs.setClaim_user_id(eqpMonitorJobEventRecord.getEventCommon().getUserID()                 );
                fhopehs.setClaim_memo(eqpMonitorJobEventRecord.getEventCommon().getEventMemo()              );
                fhopehs.setEvent_create_time(eqpMonitorJobEventRecord.getEventCommon().getEventCreationTimeStamp() );
                iRc = lotOperationHistoryService.insertLotOperationHistory( fhopehs );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord(): InsertLotOperationHistory SQL Error Occured");
                    log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord Function");
                    return( iRc );
                }
            }
        }
        log.info("HistoryWatchDogServer::CreateEqpMonitorJobEventRecord Function");
        return( returnOK() );
    }

}
