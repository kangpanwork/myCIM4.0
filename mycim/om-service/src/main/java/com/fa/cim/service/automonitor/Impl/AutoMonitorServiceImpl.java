package com.fa.cim.service.automonitor.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.machine.CimEqpMonitor;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.service.automonitor.IAutoMonitorService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OmService
@Slf4j
public class AutoMonitorServiceImpl implements IAutoMonitorService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IAutoMonitorService autoMonitorService;

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ITimeStampMethod timeStampMethod;

    @Autowired
    private IConstraintService constraintService;

    @Override
    public Results.AMJObLotDeleteReqResult sxAMJObLotDeleteReq(Infos.ObjCommon objCommon, Params.AMJObLotDeleteReqInParams amjObLotDeleteReqInParams){
        Results.AMJObLotDeleteReqResult result = new Results.AMJObLotDeleteReqResult();
        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        if ( ObjectIdentifier.isEmpty(amjObLotDeleteReqInParams.getLotID())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //Acquire EqpMonitor Job Information on Lot
        Infos.EqpMonitorJobLotInfo  strLot_eqpMonitorJob_Get_out = lotMethod.lotEqpMonitorJobGet(objCommon, amjObLotDeleteReqInParams.getLotID() );

        log.debug( "eqpMonitorJobID : {}", strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());
        if ( ObjectIdentifier.isEmpty(strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID())) {
            log.debug("strLot_eqpMonitorJob_Get_out has no information");
            return result;
        }
        //Get EqpMonitor job information
        List<Infos.EqpMonitorJobInfo>  strEqpMonitorJob_info_Get_out = null;
        try {
            strEqpMonitorJob_info_Get_out = equipmentMethod.eqpMonitorJobInfoGet(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID(), strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqpMonitorJob(), e.getCode())) {
                return result;
            } else {
                throw e;
            }
        }

        Boolean bFoundLot = false;
        Boolean bExitFlag = false;
        for(int i = 0; i < CimArrayUtils.getSize(strEqpMonitorJob_info_Get_out.get(0).getStrEqpMonitorLotInfoSeq()); i++) {
            log.debug("loop through strEqpMonitorLotInfoSeq : {}",strEqpMonitorJob_info_Get_out.get(0).getStrEqpMonitorLotInfoSeq().get(i).getLotID());
            if ( ObjectIdentifier.equalsWithValue(strEqpMonitorJob_info_Get_out.get(0).getStrEqpMonitorLotInfoSeq().get(i).getLotID(), amjObLotDeleteReqInParams.getLotID())) {
                log.debug( "Found the lot");
                bFoundLot= true;
                bExitFlag = strEqpMonitorJob_info_Get_out.get(0).getStrEqpMonitorLotInfoSeq().get(i).getExitFlag();
                break;
            }
        }

        if (CimBooleanUtils.isFalse(bFoundLot) || CimBooleanUtils.isFalse(bExitFlag)) {
            log.debug("exitFlag is FALSE");
            return result;
        }

        //Lock EqpMonitor object
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, strLot_eqpMonitorJob_Get_out.getEqpMonitorID());

        //----------------------------------------------------------------
        //  Remove EqpMonitor lot from EqpMonitor job
        //----------------------------------------------------------------
        //Lock EqpMonitor job object
        // object_LockForEqpMonitorJob
        objectLockMethod.objectLockForEqpMonitorJob(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID(), strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());

        //Delete Lot from EqpMonitor job
        equipmentMethod.eqpMonitorJobLotUpdate(objCommon, amjObLotDeleteReqInParams.getLotID(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_LOTREMOVE);


        //Create EqpMonitor Job History Event
        Inputs.EqpMonitorJobChangeEventMakeParams strEqpMonitorJobChangeEvent_Make_in = new Inputs.EqpMonitorJobChangeEventMakeParams();
        strEqpMonitorJobChangeEvent_Make_in.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP);
        strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorID(strLot_eqpMonitorJob_Get_out.getEqpMonitorID());
        strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorJobID(strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());
        List<Infos.EqpMonitorLotInfo> eqpMonitorLotInfos = new ArrayList<>();
        Infos.EqpMonitorLotInfo eqpMonitorLotInfo = new Infos.EqpMonitorLotInfo();
        eqpMonitorLotInfo.setLotID(strLot_eqpMonitorJob_Get_out.getLotID());
        eqpMonitorLotInfo.setStartSeqNo(strLot_eqpMonitorJob_Get_out.getStartSeqNo().intValue());
        eqpMonitorLotInfo.setExitFlag(strLot_eqpMonitorJob_Get_out.getExitFlag());
        eqpMonitorLotInfo.setResult(strLot_eqpMonitorJob_Get_out.getResult().intValue());
        eqpMonitorLotInfos.add(eqpMonitorLotInfo);
        strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorLotInfoList(eqpMonitorLotInfos);

        eventMethod.eqpMonitorJobChangeEventMake(objCommon, strEqpMonitorJobChangeEvent_Make_in );

        //Get EqpMonitor job information
        //Check again after lot remove
        List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID(), strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());

        //
        if ( 0 == CimArrayUtils.getSize(strEqpMonitorJobInfoGetOut.get(0).getStrEqpMonitorLotInfoSeq()))   //Lot relevant to EqpMonitor Job doesn't exist
        {
            //Get equipment monitor job id list
            List<ObjectIdentifier> strEqpMonitor_eqpMonitorJobIDs_Get_out = equipmentMethod.eqpMonitorEqpMonitorJobIDsGet(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID() );


            log.debug( "strEqpMonitor_eqpMonitorJobIDs_Get_out.eqpMonitorJobIDs.length() : {}", CimArrayUtils.getSize(strEqpMonitor_eqpMonitorJobIDs_Get_out));
            //----------------------------------------------------------------
            //  When EqpMonitor job is "Passed", EqpMonitor jobs started before the job are aborted
            //----------------------------------------------------------------
            for (int iCnt = 0; iCnt < CimArrayUtils.getSize(strEqpMonitor_eqpMonitorJobIDs_Get_out); iCnt++ ) {
                log.debug( "loop to strEqpMonitor_eqpMonitorJobIDs_Get_out.eqpMonitorJobIDs.length() : {}", iCnt);
                log.debug( "eqpMonitorJob ID ： {}", strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt));
                if (ObjectIdentifier.equalsWithValue(strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt), strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID()) ) {
                    log.debug("Not target EqpMonitor job");
                    //Get EqpMonitor job information
                    List<Infos.EqpMonitorJobInfo>  strEqpMonitorJob_info_Get_out2 = equipmentMethod.eqpMonitorJobInfoGet(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID(), strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt) );

                    //Abort EqpMonitor job started before the start time of this EqpMonitor job among jobs for the same EqpMonitor
                    if (!CimStringUtils.equals(strEqpMonitorJobInfoGetOut.get(0).getStartTimeStamp(), strEqpMonitorJob_info_Get_out2.get(0).getStartTimeStamp())) {
                        log.debug( "EqpMonitor job among jobs for the same EqpMonitor started before the start time of target EqpMonitor job");
                        //Update status of EqpMonitor job to Aborted
                        Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
                        strAMJobStatusChangeRptInParm.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
                        strAMJobStatusChangeRptInParm.setEqpMonitorID(strLot_eqpMonitorJob_Get_out.getEqpMonitorID());
                        strAMJobStatusChangeRptInParm.setEqpMonitorJobID(strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt));
                        strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED);
                        String strAMJobStatusChangeRptResult = autoMonitorService.sxAMJobStatusChangeRpt(objCommon, strAMJobStatusChangeRptInParm);
                    }
                }
            }

            //----------------------------------------------------------------
            //  No EqpMonitor lot connected to EqpMonitor job => Passed!!
            //----------------------------------------------------------------
            log.debug("0 == strEqpMonitorJobInfoGetOut.strEqpMonitorJobInfoSeq[0].strEqpMonitorLotInfoSeq.length()");
            //Update status of EqpMonitor job to "Passed"
            Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
            strAMJobStatusChangeRptInParm.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
            strAMJobStatusChangeRptInParm.setEqpMonitorID(strLot_eqpMonitorJob_Get_out.getEqpMonitorID());
            strAMJobStatusChangeRptInParm.setEqpMonitorJobID(strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID());
            strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_PASSED);
            String strAMJobStatusChangeRptResult = autoMonitorService.sxAMJobStatusChangeRpt(objCommon, strAMJobStatusChangeRptInParm);

            //Get EqpMonitor information
            List<Infos.EqpMonitorDetailInfo> strEqpMonitor_info_Get_out = equipmentMethod.eqpMonitorInfoGet( objCommon, null,  strLot_eqpMonitorJob_Get_out.getEqpMonitorID());

            //Update equipment status in case following conditions are met
            if (!ObjectIdentifier.isEmpty( strEqpMonitor_info_Get_out.get(0).getEqpStateAtPassed().getEquipmentStatusCode()) ) {
                log.debug("eqpStateAtPassed is specified");
                //Get EqpMonitor job list for the same Eqp/Chamber
                Infos.EqpMonitorJobListGetDRIn strEqpMonitorJob_list_GetDR_in = new Infos.EqpMonitorJobListGetDRIn();
                strEqpMonitorJob_list_GetDR_in.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
                strEqpMonitorJob_list_GetDR_in.setChamberID(strLot_eqpMonitorJob_Get_out.getChamberID());
                List<Infos.EqpMonitorJobDetailInfo>  strEqpMonitorJob_list_GetDR_out = equipmentMethod.eqpMonitorJobListGetDR(objCommon, strEqpMonitorJob_list_GetDR_in );

                Boolean bEqpStatusChange = true;
                for (int iCnt = 0; iCnt < CimArrayUtils.getSize(strEqpMonitorJob_list_GetDR_out); iCnt++ ) {
                    log.debug( "loop to strEqpMonitorJob_list_GetDR_out.strEqpMonitorJobDetailInfoSeq.length() : {}", strEqpMonitorJob_list_GetDR_out.get(iCnt).getEqpMonitorJobID());
                    // Lock EqpMonitor job
                    //  - To get read lock
                    //  - Check object existence
                    // object_LockForEqpMonitorJob
                    objectLockMethod.objectLockForEqpMonitorJob(objCommon, strEqpMonitorJob_list_GetDR_out.get(iCnt).getEqpMonitorID(), strEqpMonitorJob_list_GetDR_out.get(iCnt).getEqpMonitorJobID());


                    // Get EqpMonitor job info for the job
                    List<Infos.EqpMonitorDetailInfo>  strEqpMonitor_info_Get_out2 = equipmentMethod.eqpMonitorInfoGet(objCommon, null, strEqpMonitorJob_list_GetDR_out.get(iCnt).getEqpMonitorID());


                    if ( CimStringUtils.equals(strEqpMonitor_info_Get_out2.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_RUNNING)
                            && !ObjectIdentifier.isEmpty( strEqpMonitor_info_Get_out2.get(0).getEqpStateAtStart().getEquipmentStatusCode()) ) {
                        log.debug( "bEqpStatusChange = FALSE");
                        bEqpStatusChange = false;
                        break;
                    }
                }

                if (CimBooleanUtils.isTrue(bEqpStatusChange)) {
                    log.debug("bEqpStatusChange == TRUE");
                    if ( !ObjectIdentifier.isEmpty( strLot_eqpMonitorJob_Get_out.getChamberID())) {
                        //Update Chamber's Status
                        log.debug("call txChamberStatusChangeReq");

                        List<Infos.EqpChamberStatus> strEqpChamberStatus = new ArrayList<>();

                        Infos.EqpChamberStatus eqpChamberStatus =new Infos.EqpChamberStatus();

                        eqpChamberStatus.setChamberID(strLot_eqpMonitorJob_Get_out.getChamberID());
                        eqpChamberStatus.setChamberStatusCode(strEqpMonitor_info_Get_out.get(0).getEqpStateAtPassed().getEquipmentStatusCode());

                        //-------------------------------------------------------
                        //   Call txChamberStatusChangeReq
                        //-------------------------------------------------------
                        Results.ChamberStatusChangeReqResult strChamberStatusChangeReqResult  = null;
                        try {
                            strChamberStatusChangeReqResult = equipmentService.sxChamberStatusChangeReq(objCommon, strLot_eqpMonitorJob_Get_out.getEquipmentID(), strEqpChamberStatus, "");
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                                log.debug("rc == RC_INVALID_STATE_TRANSITION");
                                //-------------------------
                                //   Call System Message
                                //-------------------------
                                StringBuffer messageSb = new StringBuffer();
                                messageSb.append("Equipment Monitor operation information.\n")
                                        .append(" EqpMonitor ID: ").append(strLot_eqpMonitorJob_Get_out.getEqpMonitorID()).append("\n")
                                        .append(" Equipment ID : ").append(strLot_eqpMonitorJob_Get_out.getEquipmentID()).append("\n")
                                        .append(" Chamber ID  : ").append(strLot_eqpMonitorJob_Get_out.getChamberID()).append("\n")
                                        .append(" Target Eqp Status : ").append(ObjectIdentifier.fetchValue(strEqpMonitor_info_Get_out.get(0).getEqpStateAtPassed().getEquipmentStatusCode())).append("\n")
                                        .append(" EqpMonitor Job ID : ").append(strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID()).append("\n")
                                        .append("  Operation Point   : Equipment Monitor operation Failed.\n");

                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EMONSERR);
                                alertMessageRptParams.setSystemMessageText(messageSb.toString());
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
                                alertMessageRptParams.setLotID(strLot_eqpMonitorJob_Get_out.getLotID());
                                alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                Results.AlertMessageRptResult strAlertMessageRptResult = systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                            } else {
                                throw e;
                            }
                        }


                    } else {
                        //Update Eqp's Status
                        log.debug("call txEqpStatusChangeReq");

                        Results.EqpStatusChangeReqResult  strEqpStatusChangeReqResult = null;
                        try {
                            strEqpStatusChangeReqResult = equipmentService.sxEqpStatusChangeReq(objCommon, strLot_eqpMonitorJob_Get_out.getEquipmentID(), strEqpMonitor_info_Get_out.get(0).getEqpStateAtPassed().getEquipmentStatusCode(), "" );
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                                log.debug("", "rc == RC_INVALID_STATE_TRANSITION");
                                //-------------------------
                                //   Call System Message
                                //-------------------------
                                StringBuffer messageSb = new StringBuffer();
                                messageSb.append("Equipment Monitor operation information.\n")
                                        .append(" EqpMonitor ID: ").append(strLot_eqpMonitorJob_Get_out.getEqpMonitorID()).append("\n")
                                        .append(" Equipment ID : ").append(strLot_eqpMonitorJob_Get_out.getEquipmentID()).append("\n")
                                        .append(" Target Eqp Status : ").append(ObjectIdentifier.fetchValue(strEqpMonitor_info_Get_out.get(0).getEqpStateAtPassed().getEquipmentStatusCode())).append("\n")
                                        .append(" EqpMonitor Job ID : ").append(strLot_eqpMonitorJob_Get_out.getEqpMonitorJobID()).append("\n")
                                        .append("  Operation Point   : Equipment Monitor operation Failed.\n");

                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EMONSERR);
                                alertMessageRptParams.setSystemMessageText(messageSb.toString());
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
                                alertMessageRptParams.setLotID(strLot_eqpMonitorJob_Get_out.getLotID());
                                alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                Results.AlertMessageRptResult strAlertMessageRptResult = systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);

                            } else {
                                throw e;
                            }
                        }
                    }
                } //[bEqpStatusChange == true]
            }

            List<ObjectIdentifier>  strEqpMonitor_eqpMonitorJobIDs_Get_out2 = equipmentMethod.eqpMonitorEqpMonitorJobIDsGet(objCommon, strLot_eqpMonitorJob_Get_out.getEqpMonitorID() );


            if ( 0 == CimArrayUtils.getSize(strEqpMonitor_eqpMonitorJobIDs_Get_out2)
                    && !CimStringUtils.equals(strEqpMonitor_info_Get_out.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER) ) {
                log.debug("Update status of EqpMonitor");
                //Update status of EqpMonitor
                Params.AMStatusChangeRptInParm strAMStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
                strAMStatusChangeRptInParm.setEquipmentID(strLot_eqpMonitorJob_Get_out.getEquipmentID());
                strAMStatusChangeRptInParm.setEqpMonitorID(strLot_eqpMonitorJob_Get_out.getEqpMonitorID());
                if (CimStringUtils.isEmpty(strEqpMonitor_info_Get_out.get(0).getWarningTime()) ) {
                    log.debug("warningTime is not specified");
                    strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WAITING);
                } else {
                    log.debug( "warningTime is specified");
                    strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WARNING);
                }

                //defult
                strAMStatusChangeRptInParm.setUser(objCommon.getUser());
                strAMStatusChangeRptInParm.setWarningActionFlag(false);
                strAMStatusChangeRptInParm.setForceRunFlag(false);
                Results.AMStatusChangeRptResult strAMStatusChangeRptResult = autoMonitorService.sxAMStatusChangeRpt(objCommon, strAMStatusChangeRptInParm);
            }
        }

        // Return to caller
        return result;
    }

    @Override
    public Results.AMJobLotReserveReqResult sxAMJobLotReserveReq(Infos.ObjCommon objCommon, Params.AMJobLotReserveReqInParams params){
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        // Initialize
        Results.AMJobLotReserveReqResult amJobLotReserveReqResult = new Results.AMJobLotReserveReqResult();

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        //Trace InParameters

        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        //Blank input check for in-parameter "equipmentID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());
        //Blank input check for in-parameter "eqpMonitorID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorID()), retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());
        //It is Error Return if length of strProductLotMap is 0
        Validations.check((0 == CimArrayUtils.getSize(params.getStrProductLotMap())), retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());

        //It is Error Return if length of strProductLotMap[ ].eqpMonitorLotIDs is 0
        int mapSize = CimArrayUtils.getSize(params.getStrProductLotMap());
        for (int iCnt1=0; iCnt1<mapSize; iCnt1++ ) {
            log.info("loop to strAMJobLotReserveReqInParm.strProductLotMap.size() : {}", mapSize);
            List<ObjectIdentifier> eqpMonitorLotIDs = params.getStrProductLotMap().get(iCnt1).getEqpMonitorLotIDs();
            int eqpMonitorLot = CimArrayUtils.getSize(eqpMonitorLotIDs);
            log.info("strAMJobLotReserveReqInParm.strProductLotMap[iCnt1].eqpMonitorLotIDs.size() : {}" , eqpMonitorLot);
            Validations.check(0 == eqpMonitorLot, retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());


            //2020/08/27 - jerry
            eqpMonitorLotIDs.forEach(lotID ->{
                objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
            });
        }


        if (ObjectIdentifier.isEmpty(params.getEqpMonitorJobID()) ) {
            log.info("Input parameter eqpMonitorJobID is blank");
            objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());
        } else {
            log.info("Input parameter eqpMonitorJobID is not blank");
            objectLockMethod.objectLockForEqpMonitorJob(objCommon, params.getEqpMonitorID(), params.getEqpMonitorJobID());

            //Get EqpMonitorJob information
            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, params.getEqpMonitorID(), params.getEqpMonitorJobID());

            //It is Error Return if present EqpMonitor Job's Status is not Requested
            Validations.check(!CimStringUtils.equals(strEqpMonitorJobInfoGetOut.get(0).getMonitorJobStatus(), BizConstant.SP_EQPMONITORJOB_STATUS_REQUESTED)
                    , retCodeConfig.getInvalidEqpMonitorJobStatus(),strEqpMonitorJobInfoGetOut.get(0).getMonitorJobStatus());

            //Set eqpMonitorJobID to Result.eqpMonitorJobID
            amJobLotReserveReqResult.setEqpMonitorJobID(params.getEqpMonitorJobID());
        }

        //Get EqpMonitor information
        List<Infos.EqpMonitorDetailInfo> strEqpMonitorInfoGetOut = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());

        if ( ObjectIdentifier.isEmpty(params.getEqpMonitorJobID()) ) {
            log.info("Input parameter eqpMonitorJobID is blank");
            log.info("monitorStatus : {}", strEqpMonitorInfoGetOut.get(0).getMonitorStatus());
            //It is Error Return if Status of the Present EqpMonitor is except Following Status
            if ( !CimStringUtils.equals(strEqpMonitorInfoGetOut.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_WAITING)
                    && !CimStringUtils.equals(strEqpMonitorInfoGetOut.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_WARNING)
                    && !CimStringUtils.equals(strEqpMonitorInfoGetOut.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_RUNNING) ) {
                log.info("Invalid monitorStatus : {}", strEqpMonitorInfoGetOut.get(0).getMonitorStatus());
                Validations.check(retCodeConfig.getInvalidEqpmonstatus());
            }
        }

        //EqpMonitor product definition information and strProductLotMap[ ].strEqpMonitorProductInfo must match
        Boolean matchFlag = true;
        List<Infos.EqpMonitorProductLotMap> strProductLotMapSeq = params.getStrProductLotMap();
        List<Infos.EqpMonitorProductInfo> strProductInfoSeq = strEqpMonitorInfoGetOut.get(0).getStrEqpMonitorProductInfoSeq();
        int productLotMapSize = CimArrayUtils.getSize(strProductLotMapSeq);
        int productInfoSize = CimArrayUtils.getSize(strProductInfoSeq);

        log.info("strProductLotMapSeq.Size ： {}", productLotMapSize);
        log.info("strProductInfoSeq.Size() ： {}", productInfoSize);
        if ( productLotMapSize != productInfoSize) {
            log.info("strProductLotMapSeq.length() != strProductInfoSeq.length()");
            matchFlag = false;
        } else {
            log.info("strProductLotMapSeq.length() == strProductInfoSeq.length()");
            for (int iCnt2=0; iCnt2<productLotMapSize; iCnt2++ ) {
                log.info("strProductLotMapSeq[iCnt2].strEqpMonitorProductInfo.productID : {}", strProductLotMapSeq.get(iCnt2).getStrEqpMonitorProductInfo().getProductID());
                Boolean foundFlag = false;
                for (int iCnt3=0; iCnt3<productInfoSize; iCnt3++ ) {
                    log.info("strProductInfoSeq[iCnt3].productID : {}", strProductInfoSeq.get(iCnt3).getProductID());
                    if (!ObjectIdentifier.equalsWithValue(strProductLotMapSeq.get(iCnt2).getStrEqpMonitorProductInfo().getRecipeID(), strProductInfoSeq.get(iCnt3).getRecipeID()) ){
                        log.debug("recipe id not match...");
                        continue;
                    }
                    if ( ObjectIdentifier.equalsWithValue(strProductLotMapSeq.get(iCnt2).getStrEqpMonitorProductInfo().getProductID(), strProductInfoSeq.get(iCnt3).getProductID()) ) {
                        if ( strProductLotMapSeq.get(iCnt2).getStrEqpMonitorProductInfo().getWaferCount().intValue() == strProductInfoSeq.get(iCnt3).getWaferCount().intValue()
                                && strProductLotMapSeq.get(iCnt2).getStrEqpMonitorProductInfo().getStartSeqNo().intValue() == strProductInfoSeq.get(iCnt3).getStartSeqNo().intValue() ) {
                            log.info("foundFlag = TRUE");
                            foundFlag = true;
                            break;
                        }
                    }
                }
                if (!foundFlag) {
                    log.info("foundFlag == FALSE");
                    matchFlag = false;
                    break;
                }
            }
        }
        Validations.check(!matchFlag, retCodeConfig.getInvalidParameterWithMsg());

        for (int iCnt1=0; iCnt1<productLotMapSize; iCnt1++ ) {
            int eqpMonitorLotSize = CimArrayUtils.getSize(strProductLotMapSeq.get(iCnt1).getEqpMonitorLotIDs());
            for (int iCnt2=0; iCnt2 < eqpMonitorLotSize; iCnt2++ ) {
                //Get the EqpMonitor section label information defined as the process of Lot
                List<Infos.EqpMonitorLabelInfo> equipmentMonitorOperationLabel = lotMethod.lotEqpMonitorOperationLabelGet(objCommon, strProductLotMapSeq.get(iCnt1).getEqpMonitorLotIDs().get(iCnt2));

                //It is Error Return if EqpMonitor Section Label is not Monitor
                Boolean bMonitorFlag = false;
                String operationLabel = null;
                int eqpmonitorLabelInfoSize = CimArrayUtils.getSize(equipmentMonitorOperationLabel);
                for (int iCnt3=0; iCnt3 < eqpmonitorLabelInfoSize; iCnt3++ ) {
                    operationLabel = equipmentMonitorOperationLabel.get(iCnt3).getOperationLabel(); //PSN000098921
                    if ( CimStringUtils.equals(operationLabel, BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR) ) {
                        bMonitorFlag = true;
                    }
                }
                Validations.check(!bMonitorFlag , retCodeConfig.getInvalidOperationlabel()
                        ,operationLabel,strProductLotMapSeq.get(iCnt1).getEqpMonitorLotIDs().get(iCnt2).getValue());
            }
        }

        ObjectIdentifier equipmentStatusCode = strEqpMonitorInfoGetOut.get(0).getEqpStateAtStart().getEquipmentStatusCode();
        log.info("eqpStateAtStart : {}", equipmentStatusCode);
        if ( !ObjectIdentifier.isEmpty(equipmentStatusCode) ) {
            log.info("", "strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].eqpStateAtStart.equipmentStatusCode.identifier is not empty");
            //-----------------------------------------------------------------
            //  Get started EqpMonitor Job information by equipmentID/chamberID
            //-----------------------------------------------------------------
            Infos.EqpMonitorJobListGetDRIn eqpMonitorJobListGetDRIn = new Infos.EqpMonitorJobListGetDRIn();
            eqpMonitorJobListGetDRIn.setEquipmentID(params.getEquipmentID());
            eqpMonitorJobListGetDRIn.setChamberID(strEqpMonitorInfoGetOut.get(0).getChamberID());
            List<Infos.EqpMonitorJobDetailInfo> eqpMonitorJobListGetDR = equipmentMethod.eqpMonitorJobListGetDR(objCommon, eqpMonitorJobListGetDRIn);

            //eqpStateAtStart should be in agreement with eqpStateAtStart of the EqpMonitor when the EqpMonitor job of same equipmentID/chamberID is already started
            int eqpMonitorJobDetaInfoSize = CimArrayUtils.getSize(eqpMonitorJobListGetDR);
            log.info("", "strEqpMonitorJob_list_GetDR_out.strEqpMonitorJobDetailInfoSeq.length() ： {}", eqpMonitorJobDetaInfoSize);
            for (int iCnt1=0; iCnt1 < eqpMonitorJobDetaInfoSize; iCnt1++ ) {
                log.info("loop to strEqpMonitorJob_list_GetDR_out.strEqpMonitorJobDetailInfoSeq.length() : {}", eqpMonitorJobListGetDR.get(iCnt1).getEqpMonitorID());
                if ( !ObjectIdentifier.equalsWithValue(eqpMonitorJobListGetDR.get(iCnt1).getEqpMonitorID(), params.getEqpMonitorID()) ) {
                    log.info("", "strEqpMonitorJob_list_GetDR_out.strEqpMonitorJobDetailInfoSeq[iCnt1].eqpMonitorID != strAMJobLotReserveReqInParm.eqpMonitorID");
                    //Get EqpMonitor information
                    List<Infos.EqpMonitorDetailInfo> strEqpMonitorInfoGetOut2 = equipmentMethod.eqpMonitorInfoGet(objCommon, null, eqpMonitorJobListGetDR.get(iCnt1).getEqpMonitorID());

                    log.info("strEqpMonitor_info_Get_out2.strEqpMonitorDetailInfos[0].eqpStateAtStart.equipmentStatusCode : {}", strEqpMonitorInfoGetOut2.get(0).getEqpStateAtStart().getEquipmentStatusCode());
                    log.info("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].eqpStateAtStart.equipmentStatusCode: {}", strEqpMonitorInfoGetOut.get(0).getEqpStateAtStart().getEquipmentStatusCode());
                    if ( !ObjectIdentifier.isEmpty(strEqpMonitorInfoGetOut2.get(0).getEqpStateAtStart().getEquipmentStatusCode())) {
                        log.info("strEqpMonitor_info_Get_out2.strEqpMonitorDetailInfos[0].eqpStateAtStart.equipmentStatusCode.identifier is not empty");
                        if ( !ObjectIdentifier.equalsWithValue(strEqpMonitorInfoGetOut2.get(0).getEqpStateAtStart().getEquipmentStatusCode(),
                                strEqpMonitorInfoGetOut.get(0).getEqpStateAtStart().getEquipmentStatusCode()) ) {
                            log.info("", "eqpStateAtStart of input eqpMonitorID is invalid for the request");
                            Validations.check(true, retCodeConfig.getInvalidEqpstateatstart());
                        }
                    }
                }
            }
        }

        Inputs.LotCheckConditionForEqpMonitorLotReserveIn lotCheckConditionForEqpMonitorLotReserveIn = new Inputs.LotCheckConditionForEqpMonitorLotReserveIn();
        lotCheckConditionForEqpMonitorLotReserveIn.setEqpMonitorID(params.getEqpMonitorID());
        lotCheckConditionForEqpMonitorLotReserveIn.setEquipmentID(params.getEquipmentID());
        if ( strEqpMonitorInfoGetOut.get(0).getKitFlag()) {
            log.info("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].kitFlag is TRUE");
            lotCheckConditionForEqpMonitorLotReserveIn.setCheckLevel(BizConstant.SP_EQPMONITOR_LEVEL_EQPMONKIT);
        } else {
            log.info("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].kitFlag is FALSE");
            lotCheckConditionForEqpMonitorLotReserveIn.setCheckLevel(BizConstant.SP_EQPMONITOR_LEVEL_EQPMONNOKIT);
        }
        lotCheckConditionForEqpMonitorLotReserveIn.setStrProductLotMap(params.getStrProductLotMap());
        lotMethod.lotCheckConditionForEqpMonitorLotReserve(objCommon, lotCheckConditionForEqpMonitorLotReserveIn);

        if (ObjectIdentifier.isEmpty( params.getEqpMonitorJobID()) ) {
            log.info("Input parameter eqpMonitorJobID is blank");
            //Change EqpMonitor Status into Running
            Params.AMStatusChangeRptInParm strAMStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
            strAMStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
            strAMStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
            strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_RUNNING);
            strAMStatusChangeRptInParm.setForceRunFlag(params.getForceRunFlag());
            Results.AMStatusChangeRptResult strAMStatusChangeRptResult  = null;
            try {
                strAMStatusChangeRptResult = autoMonitorService.sxAMStatusChangeRpt( objCommon, strAMStatusChangeRptInParm);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getSameEqpMonState(), e.getCode())) {
                    throw e;
                }
            }
            if (!CimObjectUtils.isEmpty(strAMStatusChangeRptResult)) {
                amJobLotReserveReqResult.setEqpMonitorJobID(strAMStatusChangeRptResult.getEqpMonitorJobID());
                amJobLotReserveReqResult.setNextExecutionTime(strAMStatusChangeRptResult.getNextExecutionTime());
            }
        }

        //Reserve EqpMonitor lots
        Params.AMJobLotReserveReqInParams strEqpMonitorJobLotReserveIn = params;
        strEqpMonitorJobLotReserveIn.setEqpMonitorJobID(amJobLotReserveReqResult.getEqpMonitorJobID());
        equipmentMethod.eqpMonitorJobLotReserve(objCommon, strEqpMonitorJobLotReserveIn);

        //Change EqpMonitor job Status
        Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
        strAMJobStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
        strAMJobStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
        strAMJobStatusChangeRptInParm.setEqpMonitorJobID(params.getEqpMonitorJobID());
        if ( strEqpMonitorInfoGetOut.get(0).getKitFlag() ) {
            log.info("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].kitFlag is TRUE");
            strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_RESERVED);
        } else {
            log.info("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].kitFlag is FALSE");
            strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_READY);
        }
        autoMonitorService.sxAMJobStatusChangeRpt(objCommon, strAMJobStatusChangeRptInParm);

        //Get EqpMonitor job information
        List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, params.getEqpMonitorID(), amJobLotReserveReqResult.getEqpMonitorJobID());

        //Create EqpMonitor Job Change Event
        // eqpMonitorJobChangeEvent_Make
        Inputs.EqpMonitorJobChangeEventMakeParams eqpMonitorJobChangeEventMakeParams = new Inputs.EqpMonitorJobChangeEventMakeParams();
        eqpMonitorJobChangeEventMakeParams.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART);
        eqpMonitorJobChangeEventMakeParams.setEqpMonitorID(params.getEqpMonitorID());
        eqpMonitorJobChangeEventMakeParams.setEqpMonitorJobID(amJobLotReserveReqResult.getEqpMonitorJobID());
        eqpMonitorJobChangeEventMakeParams.setEqpMonitorLotInfoList(strEqpMonitorJobInfoGetOut.get(0).getStrEqpMonitorLotInfoSeq());
        eqpMonitorJobChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.eqpMonitorJobChangeEventMake(objCommon, eqpMonitorJobChangeEventMakeParams);
        // Return to caller
        return amJobLotReserveReqResult;
    }

    @Override
    public String sxAMJobStatusChangeRpt(Infos.ObjCommon objCommon, Params.AMJobStatusChangeRptInParm params) {
        // Initialize
        log.info("AMJobStatusChangeRptInParm : {}", params);
        //--------------------------
        //  In parameters check
        //--------------------------
        //Blank input check for in-parameter "equipmentID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfigEx.getBlankInputParameter());
        //Blank input check for in-parameter "eqpMonitorID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorID()), retCodeConfigEx.getBlankInputParameter());
        //Blank input check for in-parameter "eqpMonitorJobID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorJobID()),retCodeConfigEx.getBlankInputParameter() );

        //Either of Requested/Reserved/Ready/Executing/Passed/Failed/Aborted must be specified as monitorJobStatus
        if (!CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_REQUESTED,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_RESERVED,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_READY,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_EXECUTING,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_PASSED,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_FAILED,params.getMonitorJobStatus())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED,params.getMonitorJobStatus())) {
            log.error("Input parameter monitorJobStatus is invalid");
            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
        }

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_RESERVED,params.getMonitorJobStatus())
                && !CimStringUtils.equals(TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ.getValue(), objCommon.getTransactionID())) {
            Validations.check(retCodeConfig.getCalledFromInvalidTransaction());
        }

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_EXECUTING,params.getMonitorJobStatus())
                //TxMoveInReq
                //TxMoveInForIBReq
                && !CimStringUtils.equals("OEQPW005",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW004",objCommon.getTransactionID())) {
            log.error("Status change to Executing is available only by OpeStart.");
            throw new ServiceException(retCodeConfig.getCalledFromInvalidTransaction());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_FAILED,params.getMonitorJobStatus())
                //TxAMVerifyReq
                //TxMoveOutReq
                //TxMoveOutForIBReq
                //TxEDCByPJRpt
                //TxForceMoveOutReq
                //TxForceMoveOutForIBReq
                //TxPartialMoveOutReq
                //TxPartialMoveOutForIBReq
                && !CimStringUtils.equals("OAMNW003",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW006",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW008",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEDCR002",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW014",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW023",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW012",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW024",objCommon.getTransactionID())) {
            log.error("Status change to Failed is available by TxAMVerifyReq.");
            throw new ServiceException(retCodeConfig.getCalledFromInvalidTransaction());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_PASSED,params.getMonitorJobStatus())
                //TxAMJObLotDeleteReq
                //TxMoveOutReq
                //TxMoveOutForIBReq
                //TxForceMoveOutReq
                //TxForceMoveOutForIBReq
                //TxPartialMoveOutReq
                //TxPartialMoveOutForIBReq
                //TxPassThruReq
                && !CimStringUtils.equals("OAMNW004",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW006",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW008",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW014",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW023",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW012",objCommon.getTransactionID())
                && !CimStringUtils.equals("OEQPW024",objCommon.getTransactionID())
                && !CimStringUtils.equals("OLOTW005",objCommon.getTransactionID())) {
            log.error("Status change to Failed is available by TxAMVerifyReq.");
            throw new ServiceException(retCodeConfig.getCalledFromInvalidTransaction());
        }

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_PASSED,params.getMonitorJobStatus())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_FAILED,params.getMonitorJobStatus())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED,params.getMonitorJobStatus())) {
            // object_Lock
            objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());
        }

        //Lock EqpMonitorJob object
        // object_LockForEqpMonitorJob
        objectLockMethod.objectLockForEqpMonitorJob(objCommon, params.getEqpMonitorID(), params.getEqpMonitorJobID());

        //Get EqpMonitor information
        List<Infos.EqpMonitorDetailInfo> strEqpMonitorInfoGetOut = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());
        //Get EqpMonitorJob information
        List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, params.getEqpMonitorID(), params.getEqpMonitorJobID());

        Infos.EqpMonitorJobInfo strEqpMonitorJobInfo = strEqpMonitorJobInfoGetOut.get(0);
        int lenEqpMonitorLot = CimArrayUtils.getSize(strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq());
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_RESERVED,params.getMonitorJobStatus())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_READY,params.getMonitorJobStatus())) {
            log.info("loop to strEqpMonitorJobInfo.strEqpMonitorLotInfoSeq.siez() : {}", lenEqpMonitorLot);
            for (int i = 0; i < lenEqpMonitorLot; i++) {
                if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_LOTSTATUS_EXECUTING,strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq().get(i).getMonitorLotStatus())) {
                    log.error("monitorJobStatus is invalid because a status of EqpMonitor lot is Executing");
                    throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                }
            }
        }

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_REQUESTED,params.getMonitorJobStatus())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_PASSED,params.getMonitorJobStatus())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_FAILED,params.getMonitorJobStatus())) {
            if (0 < lenEqpMonitorLot) {
                log.error("monitorJobStatus is invalid because EqpMonitor lot is connected to EqpMonitor job");
                throw new ServiceException(retCodeConfig.getNotAllowedEqpMonitorStat());
            }
        }
        //Change the status of EqpMonitor Job
        //eqpMonitorJob_status_Change
        Infos.EqpMonitorJobStatusChangeIn eqpMonitorJobStatusChangeIn = new Infos.EqpMonitorJobStatusChangeIn();
        eqpMonitorJobStatusChangeIn.setEquipmentID(params.getEquipmentID());
        eqpMonitorJobStatusChangeIn.setEqpMonitorID(params.getEqpMonitorID());
        eqpMonitorJobStatusChangeIn.setEqpMonitorJobID(params.getEqpMonitorJobID());
        eqpMonitorJobStatusChangeIn.setMonitorJobStatus(params.getMonitorJobStatus());
        String eqpMonitorJobStatusChange = null;
        try {
            eqpMonitorJobStatusChange = equipmentMethod.eqpMonitorJobStatusChange(objCommon, eqpMonitorJobStatusChangeIn);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getSameEqpmonJobStat(), e.getCode())) {
                throw e;
            }
        }

        //Make event for StatusChange
        Inputs.EqpMonitorJobChangeEventMakeParams eqpMonitorJobChangeEventMakeParams = new Inputs.EqpMonitorJobChangeEventMakeParams();
        eqpMonitorJobChangeEventMakeParams.setTransactionID(objCommon.getTransactionID());
        eqpMonitorJobChangeEventMakeParams.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE);
        eqpMonitorJobChangeEventMakeParams.setEqpMonitorID(params.getEqpMonitorID());
        eqpMonitorJobChangeEventMakeParams.setEqpMonitorJobID(params.getEqpMonitorJobID());
        eqpMonitorJobChangeEventMakeParams.setPreviousMonitorJobStatus(strEqpMonitorJobInfo.getMonitorJobStatus());
        eqpMonitorJobChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.EQP_MONITOR_LOT_RESERVE_REQ.getValue())){
            eqpMonitorJobChangeEventMakeParams.setEqpMonitorLotInfoList(strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq());
        }
        eventMethod.eqpMonitorJobChangeEventMake(objCommon, eqpMonitorJobChangeEventMakeParams);

        if (CimStringUtils.equals(params.getMonitorJobStatus(), BizConstant.SP_EQPMONITORJOB_STATUS_PASSED)
                || CimStringUtils.equals(params.getMonitorJobStatus(), BizConstant.SP_EQPMONITORJOB_STATUS_FAILED)
                || CimStringUtils.equals(params.getMonitorJobStatus(), BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED)){
            //Make event for EQPMonComp
            Inputs.EqpMonitorJobChangeEventMakeParams eqpMonitorJobChangeEventMakeParams2 = new Inputs.EqpMonitorJobChangeEventMakeParams();
            eqpMonitorJobChangeEventMakeParams2.setTransactionID(objCommon.getTransactionID());
            eqpMonitorJobChangeEventMakeParams2.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP);
            eqpMonitorJobChangeEventMakeParams2.setEqpMonitorID(params.getEqpMonitorID());
            eqpMonitorJobChangeEventMakeParams2.setEqpMonitorJobID(params.getEqpMonitorJobID());
            eqpMonitorJobChangeEventMakeParams2.setMonitorJobStatus(params.getMonitorJobStatus());
            eqpMonitorJobChangeEventMakeParams2.setEqpMonitorLotInfoList(strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq());
            eqpMonitorJobChangeEventMakeParams2.setClaimMemo(params.getClaimMemo());
            eventMethod.eqpMonitorJobChangeEventMake(objCommon, eqpMonitorJobChangeEventMakeParams2);

            //eqpMonitorJob_Completed
            Infos.EqpMonitorJobCompletedIn eqpMonitorJobCompletedIn = new Infos.EqpMonitorJobCompletedIn();
            eqpMonitorJobCompletedIn.setEqpMonitorID(params.getEqpMonitorID());
            eqpMonitorJobCompletedIn.setEqpMonitorJobID(params.getEqpMonitorJobID());
            eqpMonitorJobCompletedIn.setMonitorJobStatus(params.getMonitorJobStatus());

            equipmentMethod.eqpMonitorJobCompleted(objCommon, eqpMonitorJobCompletedIn);

            //Get EqpMonitorJob information
            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut2 = equipmentMethod.eqpMonitorJobInfoGet(objCommon, params.getEqpMonitorID(), null);

            int eqpMonitorJobCnt = CimArrayUtils.getSize(strEqpMonitorJobInfoGetOut2);
            if (CimStringUtils.equals(BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED,params.getMonitorJobStatus())
                    && !CimStringUtils.equals(BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER,strEqpMonitorInfoGetOut.get(0).getMonitorStatus())
                    && 0 == eqpMonitorJobCnt) {
                //txAMStatusChangeRpt
                Params.AMStatusChangeRptInParm amStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
                amStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
                amStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
                amStatusChangeRptInParm.setForceRunFlag(false);
                amStatusChangeRptInParm.setWarningActionFlag(false);
                if (null == strEqpMonitorInfoGetOut.get(0).getWarningTime()) {
                    amStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WAITING);
                } else {
                    amStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WARNING);
                }
                log.info("amStatusChangeRptInParm : {}", amStatusChangeRptInParm);
                Results.AMStatusChangeRptResult  amStatusChangeRptResult = autoMonitorService.sxAMStatusChangeRpt(objCommon, amStatusChangeRptInParm);
            }
        }
        return eqpMonitorJobStatusChange;
    }

    @Override
    public String sxAMRecoverReq(Infos.ObjCommon objCommon, Params.AMRecoverReqParams params) {
        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        //Blank input check for in-parameter "equipmentID" and "eqpMonitorID"
        if (ObjectIdentifier.isEmpty(params.getEquipmentID()) || ObjectIdentifier.isEmpty(params.getEqpMonitorID()) ) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //Lock EqpMonitor object
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());

        //Get EqpMonitor information
        List<Infos.EqpMonitorDetailInfo> strEqpMonitor_info_Get_out = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());


        //MonitorStatus must be "MonitorOver"
        if (!CimStringUtils.equals(strEqpMonitor_info_Get_out.get(0).getMonitorStatus(), BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER) ) {
            Validations.check(retCodeConfig.getInvalidEqpmonstatus());
        }

        //MonitorType must be "Routine"
        if (!CimStringUtils.equals(strEqpMonitor_info_Get_out.get(0).getMonitorType(), BizConstant.SP_EQPMONITOR_TYPE_ROUTINE) ) {
            Validations.check(retCodeConfig.getInvalidEqpmontype());
        }

        //Acquire ID list of EqpMonitor jobs from an EqpMonitor
        List<ObjectIdentifier> strEqpMonitor_eqpMonitorJobIDs_Get_out = equipmentMethod.eqpMonitorEqpMonitorJobIDsGet(objCommon, params.getEqpMonitorID());


        for (int iCnt = 0; iCnt< CimArrayUtils.getSize(strEqpMonitor_eqpMonitorJobIDs_Get_out); iCnt++ ){
            //Lock EqpMonitor Job object
            //object_LockForEqpMonitorJob
            objectLockMethod.objectLockForEqpMonitorJob(objCommon, params.getEqpMonitorID(), strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt));
        }

        for (int iCnt = 0; iCnt< CimArrayUtils.getSize(strEqpMonitor_eqpMonitorJobIDs_Get_out); iCnt++ ) {
            //Abort EqpMonitor job
            Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
            strAMJobStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
            strAMJobStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
            strAMJobStatusChangeRptInParm.setEqpMonitorJobID(strEqpMonitor_eqpMonitorJobIDs_Get_out.get(iCnt));
            strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_ABORTED);
            autoMonitorService.sxAMJobStatusChangeRpt(objCommon, strAMJobStatusChangeRptInParm);

        }

        //Change the status of EqpMonitor to "Waiting"
        Params.AMStatusChangeRptInParm strAMStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
        strAMStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
        strAMStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
        strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WAITING);
        strAMStatusChangeRptInParm.setForceRunFlag(false);
        strAMStatusChangeRptInParm.setWarningActionFlag(false);
        autoMonitorService.sxAMStatusChangeRpt(objCommon, strAMStatusChangeRptInParm);

        //Reset the state of EqpMonitor
        equipmentMethod.eqpMonitorConditionReset(objCommon, params.getEquipmentID(), params.getEqpMonitorID() );

        //Make event
        Inputs.EqpMonitorChangeEventMakeParams strEqpMonitorChangeEvent_Make_in = new Inputs.EqpMonitorChangeEventMakeParams();
        strEqpMonitorChangeEvent_Make_in.setTransactionID(objCommon.getTransactionID());
        strEqpMonitorChangeEvent_Make_in.setOpeCategory(BizConstant.SP_EQPMONITOR_OPECATEGORY_RESET);
        strEqpMonitorChangeEvent_Make_in.setEqpMonitorID(params.getEqpMonitorID());
        eventMethod.eqpMonitorChangeEventMake(objCommon, strEqpMonitorChangeEvent_Make_in );

        return null;
    }

    @Override
    public String sxAMScheduleChgReq(Infos.ObjCommon objCommon, Params.EqpMonitorScheduleUpdateInParm params) {
        // Initialize
        log.info("EqpMonitorScheduleUpdateInParm : {} ", params);
        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        //Blank input check for in-parameter "equipmentID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfig.getInvalidParameter());

        //Blank input check for in-parameter "eqpMonitorID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorID()), retCodeConfigEx.getBlankInputParameter());

        //Either of Skip/Postpone/ForceRun must be specified as actionType
        if (!BizConstant.SP_EQPMONITOR_SCHEDULE_SKIP.equals(params.getActionType())
                && !BizConstant.SP_EQPMONITOR_SCHEDULE_POSTPONE.equals(params.getActionType())
                && !BizConstant.SP_EQPMONITOR_SCHEDULE_FORCERUN.equals(params.getActionType())) {
            log.error("monitorStatus is invalid");
            Validations.check(true, retCodeConfig.getInvalidParameterWithMsg());
        }
        //Lock EqpMonitor object
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());

        // step45 - eqpMonitor_info_Get
        List<Infos.EqpMonitorDetailInfo> strEqpMonitorInfoGetOut = null;
        try {
            strEqpMonitorInfoGetOut = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());
        } catch (ServiceException e) {
            if (!CimObjectUtils.isEmpty(strEqpMonitorInfoGetOut) && !BizConstant.SP_EQPMONITOR_TYPE_ROUTINE.equals(strEqpMonitorInfoGetOut.get(0).getMonitorType())) {
                log.error("Invalid monitorType : {}", strEqpMonitorInfoGetOut.get(0).getMonitorType());
                throw e;
            }
        }
        //Check EqpMonitor type

        //Calculate the present nextExecutionTime
        Infos.EqpMonitorNextExecutionTimeCalculateIn eqpMonitorNextExecutionTimeCalculateIn = new Infos.EqpMonitorNextExecutionTimeCalculateIn();
        if (strEqpMonitorInfoGetOut != null) {
            eqpMonitorNextExecutionTimeCalculateIn.setCurrentScheduleBaseTime(Timestamp.valueOf(strEqpMonitorInfoGetOut.get(0).getScheduleBaseTimeStamp()));
            eqpMonitorNextExecutionTimeCalculateIn.setExecutionInterval(strEqpMonitorInfoGetOut.get(0).getExecutionInterval());
            eqpMonitorNextExecutionTimeCalculateIn.setScheduleAdjustment(strEqpMonitorInfoGetOut.get(0).getScheduleAdjustment());
        }
        eqpMonitorNextExecutionTimeCalculateIn.setFutureTimeRequireFlag(false);
        log.info("eqpMonitor_nextExecutionTime_Calculate in params : {}", eqpMonitorNextExecutionTimeCalculateIn);
        Results.EqpMonitorNextExecutionTimeCalculateResult eqpMonitorNextExecutionTimeCalculateResultObject = equipmentMethod.eqpMonitorNextExecutionTimeCalculate(objCommon, eqpMonitorNextExecutionTimeCalculateIn);

        if (BizConstant.SP_EQPMONITOR_SCHEDULE_FORCERUN.equals(params.getActionType())) {
            if (objCommon.getTimeStamp().getReportTimeStamp().getTime() > Timestamp.valueOf(eqpMonitorNextExecutionTimeCalculateResultObject.getNextExecutionTime()).getTime()) {
                return null;
            }
        }
        if (BizConstant.SP_EQPMONITOR_SCHEDULE_SKIP.equals(params.getActionType())
                || BizConstant.SP_EQPMONITOR_SCHEDULE_POSTPONE.equals(params.getActionType())) {
            if (strEqpMonitorInfoGetOut != null && !BizConstant.SP_EQPMONITOR_STATUS_WAITING.equals(strEqpMonitorInfoGetOut.get(0).getMonitorStatus())
                    && !BizConstant.SP_EQPMONITOR_STATUS_WARNING.equals(strEqpMonitorInfoGetOut.get(0).getMonitorStatus())) {
                log.error("Invalid monitorStatus : {}", strEqpMonitorInfoGetOut.get(0).getMonitorStatus());
                Validations.check(retCodeConfig.getInvalidEquipmentStatus(), ObjectIdentifier.fetchValue(strEqpMonitorInfoGetOut.get(0).getEquipmentID()),
                        strEqpMonitorInfoGetOut.get(0).getMonitorStatus());
            }
        }
        //Change the next execution time of EqpMonitor
        Infos.EqpMonitorScheduleUpdateIn eqpMonitorScheduleUpdateIn = new Infos.EqpMonitorScheduleUpdateIn();
        eqpMonitorScheduleUpdateIn.setActionType(params.getActionType());
        eqpMonitorScheduleUpdateIn.setEquipmentID(params.getEquipmentID());
        eqpMonitorScheduleUpdateIn.setEqpMonitorID(params.getEqpMonitorID());
        if (null != params.getPostponeTime()) {
            eqpMonitorScheduleUpdateIn.setPostponeTime(params.getPostponeTime());
        }
        String eqpMonitorScheduleUpdate = equipmentMethod.eqpMonitorScheduleUpdate(objCommon, eqpMonitorScheduleUpdateIn);

        // eqpMonitorChangeEvent_Make
        Inputs.EqpMonitorChangeEventMakeParams eqpMonitorChangeEventMakeParams = new Inputs.EqpMonitorChangeEventMakeParams();
        eqpMonitorChangeEventMakeParams.setTransactionID(objCommon.getTransactionID());
        eqpMonitorChangeEventMakeParams.setOpeCategory(params.getActionType());
        eqpMonitorChangeEventMakeParams.setEqpMonitorID(params.getEqpMonitorID());
        eqpMonitorChangeEventMakeParams.setPreviousNextExecutionTime(eqpMonitorNextExecutionTimeCalculateResultObject.getNextExecutionTime());
        eqpMonitorChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.eqpMonitorChangeEventMake(objCommon, eqpMonitorChangeEventMakeParams);
        // Return to caller

        return eqpMonitorScheduleUpdate;
    }

    @Override
    public void sxAMSetReq(Infos.ObjCommon objCommon, Params.AMSetReqInParm params) {
        // Initialize
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        log.info(" AMSetReqInParm : {}",params);
        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        //Blank input check for in-parameter "equipmentID"
        Validations.check (ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfigEx.getBlankInputParameter(),params.getEquipmentID());


        //Blank input check for in-parameter "eqpMonitorID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorID()), retCodeConfigEx.getBlankInputParameter(),params.getEqpMonitorID());


        //Create, Update, or Delete must be specified as actionType
        Validations.check (!CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE, params.getActionType())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE, params.getActionType())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_DELETE, params.getActionType()),retCodeConfig.getInvalidParameter());

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE, params.getActionType())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE, params.getActionType())) {
            log.info("actionType is Create or Update");
            //Routine or Manual must be specified as monitorType
            Validations.check (!CimStringUtils.equals(BizConstant.SP_EQPMONITOR_TYPE_ROUTINE, params.getMonitorType())
                    && !CimStringUtils.equals(BizConstant.SP_EQPMONITOR_TYPE_MANUAL, params.getMonitorType()), retCodeConfig.getInvalidParameter());

            int eqpMonProductInfoSize = CimArrayUtils.getSize(params.getStrEqpMonitorProductInfoSeq());
            log.info("loop to strInParm.strEqpMonitorProductInfoSeq.size : {}", eqpMonProductInfoSize);
            //It is Error Return if length of strEqpMonitorProductInfoSeq is 0
            Validations.check(0 == eqpMonProductInfoSize, retCodeConfig.getInvalidParameter());

            //It is Error Return if strEqpMonitorProductInfoSeq[ ].productID is blank and wafer count is 0
            for (int i = 0; i < eqpMonProductInfoSize; i++) {
                Infos.EqpMonitorProductInfo monitorProductInfo = params.getStrEqpMonitorProductInfoSeq().get(i);
                Validations.check (ObjectIdentifier.isEmpty(monitorProductInfo.getProductID()),retCodeConfig.getInvalidParameter());
                Validations.check (ObjectIdentifier.isEmpty(monitorProductInfo.getRecipeID()),retCodeConfig.getInvalidParameter());

                Validations.check (0 == monitorProductInfo.getWaferCount(),retCodeConfig.getInvalidParameter());

                Validations.check(0 == monitorProductInfo.getStartSeqNo(), retCodeConfig.getInvalidParameter());

            }

            if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_TYPE_ROUTINE, params.getMonitorType())) {
                log.info("monitorType is Routine");
                //Time must be specified as scheduleType
                Validations.check(!CimStringUtils.equals(BizConstant.SP_EQPMONITOR_SCHEDULE_TYPE_TIME, params.getScheduleType()),retCodeConfig.getInvalidParameter());

                //startTimeStamp must be specified
                Validations.check(CimStringUtils.isEmpty(params.getStartTimeStamp()),retCodeConfig.getInvalidParameter());

                //executionInterval must be specified(One or more)
                Validations.check(0 >= params.getExecutionInterval(), retCodeConfig.getInvalidParameterWithMsg(),params.getExecutionInterval());

                //expirationInterval must be specified(More than executionInterval)
                Validations.check(params.getExpirationInterval() <= params.getExecutionInterval(),retCodeConfig.getInvalidParameterWithMsg(), "Inhibit Timer must be larger than Execution Interval");
            }
            int eqpMonActionInfoSize = CimArrayUtils.getSize(params.getStrEqpMonitorActionInfoSeq());
            if (0 < eqpMonActionInfoSize) {
                log.info("strInParm.strEqpMonitorActionInfoSeq.size() > 0");
                for (int i = 0; i < eqpMonActionInfoSize; i++) {
                    Infos.EqpMonitorActionInfo eqpMonitorActionInfo = params.getStrEqpMonitorActionInfoSeq().get(i);
                    //action must be specified
                    Validations.check (CimStringUtils.isEmpty(eqpMonitorActionInfo.getAction()),retCodeConfig.getInvalidParameter());

                    //eventType must be specified
                    Validations.check (CimStringUtils.isEmpty(eqpMonitorActionInfo.getEventType()),retCodeConfig.getInvalidParameter());


                    if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_ACTION_MAIL, eqpMonitorActionInfo.getAction())) {
                        //sysMessageCodeID must be specified
                        Validations.check (ObjectIdentifier.isEmpty(eqpMonitorActionInfo.getSysMessageCodeID()),retCodeConfig.getInvalidParameter());

                    }
                    if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_ACTION_INHIBIT, eqpMonitorActionInfo.getAction())) {
                        //reasonCodeID must be specified
                        Validations.check(ObjectIdentifier.isEmpty(eqpMonitorActionInfo.getReasonCodeID()),retCodeConfig.getInvalidParameter());

                    }
                }
            }
        }

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE, params.getActionType())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_DELETE, params.getActionType())) {
            // object_Lock
            objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());
        }
        Infos.EqpMonitorDetailInfo strEqpMonitorDetailInfo = new Infos.EqpMonitorDetailInfo();
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE, params.getActionType())) {
            log.info("actionType is Create");
            //Set input parameters to strEqpMonitorDetailInfo
            strEqpMonitorDetailInfo.setEquipmentID(params.getEquipmentID());
            strEqpMonitorDetailInfo.setEqpMonitorID(params.getEqpMonitorID());
            strEqpMonitorDetailInfo.setChamberID(params.getChamberID());
            strEqpMonitorDetailInfo.setDescription(params.getDescription());
            strEqpMonitorDetailInfo.setMonitorType(params.getMonitorType());
            strEqpMonitorDetailInfo.setScheduleType(params.getScheduleType());
            strEqpMonitorDetailInfo.setStrEqpMonitorProductInfoSeq(params.getStrEqpMonitorProductInfoSeq());
            String startTime = params.getStartTimeStamp();
            if (CimStringUtils.isEmpty(startTime)) {
                params.setStartTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
            }
            strEqpMonitorDetailInfo.setStartTimeStamp(CimDateUtils.convertToOrInitialTime(params.getStartTimeStamp()));
            strEqpMonitorDetailInfo.setExecutionInterval(params.getExecutionInterval());
            strEqpMonitorDetailInfo.setWarningInterval(params.getWarningInterval());
            strEqpMonitorDetailInfo.setExpirationInterval(params.getExpirationInterval());
            strEqpMonitorDetailInfo.setStandAloneFlag(params.getStandAloneFlag());
            strEqpMonitorDetailInfo.setKitFlag(params.getKitFlag());
            strEqpMonitorDetailInfo.setMaxRetryCount(params.getMaxRetryCount());
            strEqpMonitorDetailInfo.setEqpStateAtStart(params.getEqpStateAtStart());
            strEqpMonitorDetailInfo.setEqpStateAtPassed(params.getEqpStateAtPassed());
            strEqpMonitorDetailInfo.setEqpStateAtFailed(params.getEqpStateAtFailed());
            strEqpMonitorDetailInfo.setStrEqpMonitorActionInfoSeq(params.getStrEqpMonitorActionInfoSeq());

            //Set default warningTime

            //Set default lastMonitorTimeStamp
            strEqpMonitorDetailInfo.setLastMonitorTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

            //Set default lastMonitorResult

            //Set default scheduleAdjustment
            strEqpMonitorDetailInfo.setScheduleAdjustment(0);
            if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_TYPE_ROUTINE, params.getMonitorType())) {
                //Calculate and set scheduleBaseTimeStamp
                Integer minutes = 0 - params.getExecutionInterval();
                String targetTimeStamp = timeStampMethod.timeStampDoCalculation(objCommon, params.getStartTimeStamp(),
                        0, 0, minutes, 0, 0);
                strEqpMonitorDetailInfo.setScheduleBaseTimeStamp(targetTimeStamp);
                strEqpMonitorDetailInfo.setLastMonitorPassedTimeStamp(CimDateUtils.convertToOrInitialTime(params.getStartTimeStamp()));
            } else {
                log.info("monitorType is Manual");
                //Set default scheduleBaseTimeStamp
                strEqpMonitorDetailInfo.setScheduleBaseTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);

                //Set present time to lastMonitorPassedTimeStamp
                strEqpMonitorDetailInfo.setLastMonitorPassedTimeStamp(new Timestamp(System.currentTimeMillis()));

            }
        } else if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE, params.getActionType())) {
            log.info("actionType is Update");
            List<Infos.EqpMonitorDetailInfo> strEqpMonitorInfoGetOut = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());
            strEqpMonitorDetailInfo = strEqpMonitorInfoGetOut.get(0);
            if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_TYPE_ROUTINE, params.getMonitorType())) {
                log.info("monitorType is Routine");
                if (!CimStringUtils.equals(params.getStartTimeStamp(), strEqpMonitorDetailInfo.getStartTimeStamp().toString())) {
                    log.info("strInParm.startTimeStamp != strEqpMonitorDetailInfo.startTimeStamp");
                    //Calculate and set scheduleBaseTimeStamp
                    Integer minutes = 0 - params.getExecutionInterval();
                    String targetTimeStamp = timeStampMethod.timeStampDoCalculation(objCommon, params.getStartTimeStamp(),
                            0, 0, minutes, 0, 0);
                    strEqpMonitorDetailInfo.setScheduleBaseTimeStamp(targetTimeStamp);
                } else if (params.getExecutionInterval().intValue() != strEqpMonitorDetailInfo.getExecutionInterval().intValue()) {
                    log.info("strInParm.executionInterval != strEqpMonitorDetailInfo.executionInterval");
                    //executionInterval changed, recalculate scheduleBaseTimeStamp
                    Integer minutes = 0 - params.getExecutionInterval();
                    String targetTimeStamp = timeStampMethod.timeStampDoCalculation(objCommon, strEqpMonitorDetailInfo.getNextExecutionTime(),
                            0, 0, minutes, 0, 0);
                    strEqpMonitorDetailInfo.setScheduleBaseTimeStamp(targetTimeStamp);
                }
                if (!CimStringUtils.equals(params.getStartTimeStamp(), strEqpMonitorDetailInfo.getStartTimeStamp().toString())
                        || params.getExecutionInterval().intValue() != strEqpMonitorDetailInfo.getExecutionInterval().intValue()) {
                    log.info("strInParm.startTimeStamp != strEqpMonitorDetailInfo.startTimeStamp or strInParm.executionInterval != strEqpMonitorDetailInfo.executionInterval");
                    strEqpMonitorDetailInfo.setScheduleAdjustment(0);
                }
                if (!CimStringUtils.equals(params.getStartTimeStamp(), strEqpMonitorDetailInfo.getStartTimeStamp().toString())
                        || params.getExpirationInterval().intValue() != strEqpMonitorDetailInfo.getExpirationInterval().intValue()) {
                    log.info("strInParm.startTimeStamp != strEqpMonitorDetailInfo.startTimeStamp or strInParm.expirationInterval != strEqpMonitorDetailInfo.expirationInterval");
                    Infos.EqpMonitorNextExecutionTimeCalculateIn eqpMonitorNextExecutionTimeCalculateIn = new Infos.EqpMonitorNextExecutionTimeCalculateIn();
                    eqpMonitorNextExecutionTimeCalculateIn.setCurrentScheduleBaseTime(CimDateUtils.convertToOrInitialTime(strEqpMonitorDetailInfo.getScheduleBaseTimeStamp()));
                    eqpMonitorNextExecutionTimeCalculateIn.setExecutionInterval(params.getExecutionInterval());
                    eqpMonitorNextExecutionTimeCalculateIn.setScheduleAdjustment(strEqpMonitorDetailInfo.getScheduleAdjustment());
                    eqpMonitorNextExecutionTimeCalculateIn.setLastMonitorPassedTime(strEqpMonitorDetailInfo.getLastMonitorPassedTimeStamp());
                    eqpMonitorNextExecutionTimeCalculateIn.setExpirationInterval(params.getExpirationInterval());
                    eqpMonitorNextExecutionTimeCalculateIn.setFutureTimeRequireFlag(false);
                    log.info("eqpMonitor_nextExecutionTime_Calculate in params : {}", eqpMonitorNextExecutionTimeCalculateIn);
                    Results.EqpMonitorNextExecutionTimeCalculateResult eqpMonitorNextExecutionTimeCalculateResult = equipmentMethod.eqpMonitorNextExecutionTimeCalculate(objCommon, eqpMonitorNextExecutionTimeCalculateIn);

                }
            }
            //Set input parameters to strEqpMonitorDetailInfo
            strEqpMonitorDetailInfo.setEquipmentID(params.getEquipmentID());
            strEqpMonitorDetailInfo.setEqpMonitorID(params.getEqpMonitorID());
            strEqpMonitorDetailInfo.setChamberID(params.getChamberID());
            strEqpMonitorDetailInfo.setDescription(params.getDescription());
            strEqpMonitorDetailInfo.setMonitorType(params.getMonitorType());
            strEqpMonitorDetailInfo.setScheduleType(params.getScheduleType());
            strEqpMonitorDetailInfo.setStrEqpMonitorProductInfoSeq(params.getStrEqpMonitorProductInfoSeq());
            if (null == params.getStartTimeStamp()) {
                params.setStartTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
            }
            strEqpMonitorDetailInfo.setStartTimeStamp(CimDateUtils.convertToOrInitialTime(params.getStartTimeStamp()));
            strEqpMonitorDetailInfo.setExecutionInterval(params.getExecutionInterval());
            strEqpMonitorDetailInfo.setWarningInterval(params.getWarningInterval());
            strEqpMonitorDetailInfo.setExpirationInterval(params.getExpirationInterval());
            strEqpMonitorDetailInfo.setStandAloneFlag(params.getStandAloneFlag());
            strEqpMonitorDetailInfo.setKitFlag(params.getKitFlag());
            strEqpMonitorDetailInfo.setMaxRetryCount(params.getMaxRetryCount());
            strEqpMonitorDetailInfo.setEqpStateAtStart(params.getEqpStateAtStart());
            strEqpMonitorDetailInfo.setEqpStateAtPassed(params.getEqpStateAtPassed());
            strEqpMonitorDetailInfo.setEqpStateAtFailed(params.getEqpStateAtFailed());
            strEqpMonitorDetailInfo.setStrEqpMonitorActionInfoSeq(params.getStrEqpMonitorActionInfoSeq());
        } else if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_DELETE, params.getActionType())) {
            //eqpMonitorChangeEvent_Make
            Inputs.EqpMonitorChangeEventMakeParams eqpMonitorChangeEventMakeParams = new Inputs.EqpMonitorChangeEventMakeParams();
            eqpMonitorChangeEventMakeParams.setOpeCategory(BizConstant.SP_EQPMONITOR_OPECATEGORY_DELETE);
            eqpMonitorChangeEventMakeParams.setEqpMonitorID(params.getEqpMonitorID());
            eqpMonitorChangeEventMakeParams.setTransactionID(objCommon.getTransactionID());
            eqpMonitorChangeEventMakeParams.setPreviousMonitorStatus("");
            eqpMonitorChangeEventMakeParams.setPreviousNextExecutionTime("");
            eqpMonitorChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
            eventMethod.eqpMonitorChangeEventMake(objCommon, eqpMonitorChangeEventMakeParams);

            Params.AMStatusChangeRptInParm amStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
            amStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
            amStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
            amStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_DELETED);
            amStatusChangeRptInParm.setForceRunFlag(false);
            amStatusChangeRptInParm.setWarningActionFlag(false);
            log.info("amStatusChangeRptInParm : {}", amStatusChangeRptInParm);
            Results.AMStatusChangeRptResult strAMStatusChangeRptResult = autoMonitorService.sxAMStatusChangeRpt(objCommon, amStatusChangeRptInParm);

            strEqpMonitorDetailInfo.setEquipmentID(params.getEquipmentID());
            strEqpMonitorDetailInfo.setEqpMonitorID(params.getEqpMonitorID());
        }
        strEqpMonitorDetailInfo.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        strEqpMonitorDetailInfo.setLastClaimedUser(objCommon.getUser().getUserID());

        //Perform eqpMonitor update
        Infos.EqpMonitorInfoUpdateIn eqpMonitorInfoUpdateIn = new Infos.EqpMonitorInfoUpdateIn();
        eqpMonitorInfoUpdateIn.setActionType(params.getActionType());
        eqpMonitorInfoUpdateIn.setStrEqpMonitorDetailInfo(strEqpMonitorDetailInfo);
        log.info("EqpMonitorInfoUpdateIn : {}", eqpMonitorInfoUpdateIn);
        ObjectIdentifier eqpMonitorInfoUpdate = equipmentMethod.eqpMonitorInfoUpdate(objCommon, eqpMonitorInfoUpdateIn);

        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE, params.getActionType())) {
            Params.AMStatusChangeRptInParm amStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
            amStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
            amStatusChangeRptInParm.setEqpMonitorID(eqpMonitorInfoUpdate);
            amStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WAITING);
            amStatusChangeRptInParm.setForceRunFlag(false);
            amStatusChangeRptInParm.setWarningActionFlag(false);
            log.info("amStatusChangeRptInParm : {}", amStatusChangeRptInParm);
            Results.AMStatusChangeRptResult  strAMStatusChangeRptResult = autoMonitorService.sxAMStatusChangeRpt(objCommon, amStatusChangeRptInParm);
        }
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE, params.getActionType())
                || CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE, params.getActionType())) {
            //eqpMonitorChangeEvent_Make
            Inputs.EqpMonitorChangeEventMakeParams eqpMonitorChangeEventMakeParams2 = new Inputs.EqpMonitorChangeEventMakeParams();
            if (CimStringUtils.equals(params.getActionType(), BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE)){
                eqpMonitorChangeEventMakeParams2.setOpeCategory(BizConstant.SP_EQPMONITOR_OPECATEGORY_CREATE);
            }
            if (CimStringUtils.equals(params.getActionType(), BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE)){
                eqpMonitorChangeEventMakeParams2.setOpeCategory(BizConstant.SP_EQPMONITOR_OPECATEGORY_UPDATE);
            }
            eqpMonitorChangeEventMakeParams2.setEqpMonitorID(eqpMonitorInfoUpdate);
            eqpMonitorChangeEventMakeParams2.setTransactionID(objCommon.getTransactionID());
            eqpMonitorChangeEventMakeParams2.setPreviousMonitorStatus("");
            eqpMonitorChangeEventMakeParams2.setPreviousNextExecutionTime("");
            eqpMonitorChangeEventMakeParams2.setClaimMemo(params.getClaimMemo());
            eventMethod.eqpMonitorChangeEventMake(objCommon, eqpMonitorChangeEventMakeParams2);
        }
    }

    @Override
    public Results.AMStatusChangeRptResult sxAMStatusChangeRpt(Infos.ObjCommon objCommon, Params.AMStatusChangeRptInParm params) {

        // Initialize
        Results.AMStatusChangeRptResult amStatusChangeRptResult = new Results.AMStatusChangeRptResult();

        //----------------------------
        //  In parameters check
        //----------------------------
        //Blank input check for in-parameter "equipmentID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfig.getInvalidParameter());

        //Blank input check for in-parameter "eqpMonitorID"
        Validations.check(ObjectIdentifier.isEmpty(params.getEqpMonitorID()), retCodeConfig.getInvalidParameter());


        //Either of Waiting/Running/Deleted/Warning/MonitorOver must be specified as monitorStatus
        if (!BizConstant.SP_EQPMONITOR_STATUS_WAITING.equals(params.getMonitorStatus())
                && !BizConstant.SP_EQPMONITOR_STATUS_RUNNING.equals(params.getMonitorStatus())
                && !BizConstant.SP_EQPMONITOR_STATUS_DELETED.equals(params.getMonitorStatus())
                && !BizConstant.SP_EQPMONITOR_STATUS_WARNING.equals(params.getMonitorStatus())
                && !BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER.equals(params.getMonitorStatus())) {
            log.error("monitorStatus is invalid");
            Validations.check(retCodeConfig.getInvalidParameterWithMsg(),params.getMonitorStatus());

        }
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_STATUS_WAITING, params.getMonitorStatus())
                && !"OAMNW005".equals(objCommon.getTransactionID())
                && !"OAMNW001".equals(objCommon.getTransactionID())
                && !"OAMNW003".equals(objCommon.getTransactionID())
                && !"OAMNR002".equals(objCommon.getTransactionID())
                && !"OAMNW004".equals(objCommon.getTransactionID())
                && !"OLOTW005".equals(objCommon.getTransactionID())
                && !"OEQPW006".equals(objCommon.getTransactionID())
                && !"OEQPW008".equals(objCommon.getTransactionID())
                && !"OEDCR002".equals(objCommon.getTransactionID())
                && !"OEQPW014".equals(objCommon.getTransactionID())
                && !"OEQPW023".equals(objCommon.getTransactionID())
                && !"OEQPW012".equals(objCommon.getTransactionID())
                && !"OEQPW024".equals(objCommon.getTransactionID())) {
            log.error("transactionID : {}", objCommon.getTransactionID());
            Validations.check(retCodeConfig.getCalledFromInvalidTransaction());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_STATUS_DELETED, params.getMonitorStatus())
                && !"OAMNW001".equals(objCommon.getTransactionID())) {
            log.error("Status change to Deleted is available by TxAMSetReq");
            Validations.check(retCodeConfig.getCalledFromInvalidTransaction());
        }
        //Lock EqpMonitor object
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, params.getEqpMonitorID());

        List<Infos.EqpMonitorDetailInfo> eqpMonitorDetailInfos = equipmentMethod.eqpMonitorInfoGet(objCommon, null, params.getEqpMonitorID());
        if (params.getForceRunFlag() && BizConstant.SP_EQPMONITOR_STATUS_WAITING.equals(params.getMonitorStatus())
                && BizConstant.SP_EQPMONITOR_STATUS_RUNNING.equals(eqpMonitorDetailInfos.get(0).getMonitorStatus())) {
            log.info("monitorStatus is Running, Input parameter warningActionFlag is TRUE and monitorStatus is Warning");
            amStatusChangeRptResult.setMonitorStatus(eqpMonitorDetailInfos.get(0).getMonitorStatus());
        } else {
            if (BizConstant.SP_EQPMONITOR_STATUS_RUNNING.equals(params.getMonitorStatus())
                    && BizConstant.SP_EQPMONITOR_TYPE_ROUTINE.equals(eqpMonitorDetailInfos.get(0).getMonitorType())) {
                if (params.getForceRunFlag()) {
                    Params.EqpMonitorScheduleUpdateInParm eqpMonitorScheduleUpdateInParm = new Params.EqpMonitorScheduleUpdateInParm();
                    eqpMonitorScheduleUpdateInParm.setEqpMonitorID(params.getEqpMonitorID());
                    eqpMonitorScheduleUpdateInParm.setEquipmentID(params.getEquipmentID());
                    eqpMonitorScheduleUpdateInParm.setActionType(BizConstant.SP_EQPMONITOR_SCHEDULE_FORCERUN);
                    String strAMScheduleChgReqResult = autoMonitorService.sxAMScheduleChgReq(objCommon, eqpMonitorScheduleUpdateInParm);
                } else {
                    Timestamp aTimeStamp = Timestamp.valueOf(eqpMonitorDetailInfos.get(0).getNextExecutionTime());
                    Validations.check(0 == CimDateUtils.compare(aTimeStamp, objCommon.getTimeStamp().getReportTimeStamp()),retCodeConfig.getExceedExpirationTime());
                }
            }
            if (BizConstant.SP_EQPMONITOR_STATUS_WAITING.equals(params.getMonitorStatus())
                    || BizConstant.SP_EQPMONITOR_STATUS_WARNING.equals(params.getMonitorStatus())) {
                //Check that an EqpMonitor job does not exist in an EqpMonitor
                if (BizConstant.SP_EQPMONITOR_STATUS_WAITING.equals(params.getMonitorStatus())) {
                    List<ObjectIdentifier> strEqpMonitorEqpMonitorJobIDsGetOut =
                            equipmentMethod.eqpMonitorEqpMonitorJobIDsGet(objCommon, params.getEqpMonitorID());
                    // BUG-2734 don`t need to Validation monitorJob
                    int nJobLen = CimArrayUtils.getSize(strEqpMonitorEqpMonitorJobIDsGetOut);
                    log.info("nJobLen : {}", nJobLen);
                    Validations.check(0 < nJobLen, retCodeConfig.getRunningEqpMonJob());
                }
            }
            if (BizConstant.SP_EQPMONITOR_STATUS_RUNNING.equals(params.getMonitorStatus())) {
                log.info("Input parameter monitorStatus is Running : {}", params.getMonitorStatus());
                ObjectIdentifier strEqpMonitorJobCreateOut = equipmentMethod.eqpMonitorJobCreate(objCommon, params.getEqpMonitorID());

                amStatusChangeRptResult.setEqpMonitorJobID(strEqpMonitorJobCreateOut);

                Params.AMJobStatusChangeRptInParm amJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
                amJobStatusChangeRptInParm.setEqpMonitorID(params.getEqpMonitorID());
                amJobStatusChangeRptInParm.setEquipmentID(params.getEquipmentID());
                amJobStatusChangeRptInParm.setEqpMonitorJobID(strEqpMonitorJobCreateOut);
                amJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_REQUESTED);
                String strAMJobStatusChangeRptResult = autoMonitorService.sxAMJobStatusChangeRpt(objCommon, amJobStatusChangeRptInParm);

                if (BizConstant.SP_EQPMONITOR_TYPE_ROUTINE.equals(eqpMonitorDetailInfos.get(0).getMonitorType())) {
                    Infos.EqpMonitorScheduleUpdateIn eqpMonitorScheduleUpdateIn = new Infos.EqpMonitorScheduleUpdateIn();
                    eqpMonitorScheduleUpdateIn.setEquipmentID(params.getEquipmentID());
                    eqpMonitorScheduleUpdateIn.setEqpMonitorID(params.getEqpMonitorID());
                    eqpMonitorScheduleUpdateIn.setActionType(BizConstant.SP_EQPMONITOR_SCHEDULE_NEXT);
                    eqpMonitorScheduleUpdateIn.setPostponeTime(0L);
                    String eqpMonitorScheduleUpdate = equipmentMethod.eqpMonitorScheduleUpdate(objCommon, eqpMonitorScheduleUpdateIn);

                    amStatusChangeRptResult.setNextExecutionTime(eqpMonitorScheduleUpdate);
                }
            }
            //Change Status of an EqpMonitor job into Requested
            //eqpMonitor_status_Change
            Infos.EqpMonitorStatusChangeIn eqpMonitorStatusChangeIn = new Infos.EqpMonitorStatusChangeIn();
            eqpMonitorStatusChangeIn.setEqpMonitorID(params.getEqpMonitorID());
            eqpMonitorStatusChangeIn.setEquipmentID(params.getEquipmentID());
            eqpMonitorStatusChangeIn.setMonitorStatus(params.getMonitorStatus());
            String eqpMonitorStatusChange = null;
            try {
                eqpMonitorStatusChange = equipmentMethod.eqpMonitorStatusChange(objCommon, eqpMonitorStatusChangeIn);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getSameEqpMonState(), e.getCode())) {
                    throw e;
                }
            }
            amStatusChangeRptResult.setMonitorStatus(eqpMonitorStatusChange);
            //eqpMonitorChangeEvent_Make
            Inputs.EqpMonitorChangeEventMakeParams eqpMonitorChangeEventMakeParams = new Inputs.EqpMonitorChangeEventMakeParams();
            eqpMonitorChangeEventMakeParams.setTransactionID(objCommon.getTransactionID());
            eqpMonitorChangeEventMakeParams.setOpeCategory(BizConstant.SP_EQPMONITOR_OPECATEGORY_STATUSCHANGE);
            eqpMonitorChangeEventMakeParams.setEqpMonitorID(params.getEqpMonitorID());
            eqpMonitorChangeEventMakeParams.setPreviousMonitorStatus(eqpMonitorDetailInfos.get(0).getMonitorStatus());
            eqpMonitorChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
            eventMethod.eqpMonitorChangeEventMake(objCommon, eqpMonitorChangeEventMakeParams);
        }


        if (BizConstant.SP_EQPMONITOR_STATUS_WARNING.equals(params.getMonitorStatus())
                || BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER.equals(params.getMonitorStatus())) {
            //--------------------------------------------------
            //   Perform actions accompanying the status change
            //--------------------------------------------------
            if ((BizConstant.SP_EQPMONITOR_STATUS_WARNING.equals(params.getMonitorStatus())
                    && CimStringUtils.isEmpty(eqpMonitorDetailInfos.get(0).getWarningTime()))
                    || (BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER.equals(params.getMonitorStatus())
                    && !BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER.equals(eqpMonitorDetailInfos.get(0).getMonitorStatus()))) {
                List<Infos.EqpMonitorActionInfo> strPerformedActionInfoSeq = new ArrayList<>();
                List<Infos.EqpMonitorActionInfo> strEqpMonitorActionInfoSeq = eqpMonitorDetailInfos.get(0).getStrEqpMonitorActionInfoSeq();
                int lenEqpMonitorActionInfo = CimArrayUtils.getSize(strEqpMonitorActionInfoSeq);
                for (int i = 0; i < lenEqpMonitorActionInfo; i++) {
                    if (CimStringUtils.equals(params.getMonitorStatus(), strEqpMonitorActionInfoSeq.get(i).getEventType())) {
                        if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_ACTION_INHIBIT, strEqpMonitorActionInfoSeq.get(i).getAction())) {
                            //Inhibit equipment or chamber
                            Infos.EntityInhibitDetailAttributes strInhibitAttr = new Infos.EntityInhibitDetailAttributes();
                            if (!ObjectIdentifier.isEmpty(eqpMonitorDetailInfos.get(0).getChamberID())) {
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier.setObjectID(eqpMonitorDetailInfos.get(0).getEquipmentID());
                                entityIdentifier.setAttribution(ObjectIdentifier.fetchValue(eqpMonitorDetailInfos.get(0).getChamberID()));
                                entities.add(entityIdentifier);
                                strInhibitAttr.setEntities(entities);
                            } else {
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(eqpMonitorDetailInfos.get(0).getEquipmentID());
                                entities.add(entityIdentifier);
                                strInhibitAttr.setEntities(entities);
                            }

                            // 增加recipe inhibit
                            Optional.ofNullable(eqpMonitorDetailInfos.get(0).getStrEqpMonitorProductInfoSeq()).ifPresent(eqpMonitorProductInfos -> {
                                eqpMonitorProductInfos.forEach(eqpMonitorProductInfo -> {
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier.setObjectID(eqpMonitorProductInfo.getRecipeID());
                                    strInhibitAttr.getEntities().add(entityIdentifier);
                                });
                            });

                            strInhibitAttr.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                            strInhibitAttr.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            strInhibitAttr.setReasonCode(ObjectIdentifier.fetchValue(strEqpMonitorActionInfoSeq.get(i).getReasonCodeID()));
                            strInhibitAttr.setOwnerID(objCommon.getUser().getUserID());
                            strInhibitAttr.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                            Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                            mfgRestrictReqParams.setUser(params.getUser());
                            mfgRestrictReqParams.setEntityInhibitDetailAttributes(strInhibitAttr);
                            Infos.EntityInhibitDetailInfo strMfgRestrictReqResult = null;
                            try {
//                                strMfgRestrictReqResult = constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strInhibitAttr);
                                mfgRestrictReq_110Params.setClaimMemo(params.getClaimMemo());
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);

                            } catch (ServiceException e) {
                                if (Validations.isEquals(retCodeConfig.getDuplicatedEntityInhibit(),e.getCode())) {
                                    log.info("rc == RC_DUPLICATE_INHIBIT");
                                    continue;
                                } else  {
                                    log.error("txMfgRestrictReq() != RC_OK && RC_DUPLICATE_INHIBIT");
                                    throw e;
                                }
                            }
                        }
                        // -----------------------------------------------------
                        // if status of monitor plan is warning or monitorOver ,|
                        // need to send a email for user                        |
                        // -----------------------------------------------------
                        Infos.EqpMonitorActionExecuteIn eqpMonitorActionExecuteIn
                                = new Infos.EqpMonitorActionExecuteIn();
                        eqpMonitorActionExecuteIn.setEqpMonitorID(params.getEqpMonitorID());
                        eqpMonitorActionExecuteIn.setEventType(params.getMonitorStatus());
                        eqpMonitorActionExecuteIn.setStrEqpMonitorActionInfo(
                                eqpMonitorDetailInfos.get(0).getStrEqpMonitorActionInfoSeq().get(i));
                        equipmentMethod.eqpMonitorActionExecute(objCommon, eqpMonitorActionExecuteIn);
                        strPerformedActionInfoSeq.add(
                                eqpMonitorDetailInfos.get(0).getStrEqpMonitorActionInfoSeq().get(i));
                    }
                }
                amStatusChangeRptResult.setStrEqpMonitorActionInfoSeq(strPerformedActionInfoSeq);
            }
        }
        return amStatusChangeRptResult;
    }

    @Override
    public Results.AMVerifyReqResult sxAMVerifyReq(Infos.ObjCommon objCommon, Params.AMVerifyReqInParams amVerifyReqInParams){
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        // Initialize
        Results.AMVerifyReqResult result = new Results.AMVerifyReqResult();

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        //Trace InParameters
        log.debug("in-parm equipmentID : {}", amVerifyReqInParams.getEquipmentID());
        log.debug("in-parm controlJobID : {}", amVerifyReqInParams.getControlJobID());
        log.debug("in-parm lotID : {}", amVerifyReqInParams.getLotID());

        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        if (ObjectIdentifier.isEmpty(amVerifyReqInParams.getEquipmentID())) {
            log.error("Input parameter equipmentID is invalid");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        if (ObjectIdentifier.isEmpty(amVerifyReqInParams.getControlJobID())) {
            log.error("Input parameter controlJobID is invalid");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        if (ObjectIdentifier.isEmpty(amVerifyReqInParams.getLotID())) {
            log.error("Input parameter lotID is invalid");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //Acquire EqpMonitor Job Information on Lot
        Infos.EqpMonitorJobLotInfo strEqpMonLotInfo = lotMethod.lotEqpMonitorJobGet(objCommon,
                amVerifyReqInParams.getLotID());

        if (ObjectIdentifier.isEmpty(strEqpMonLotInfo.getEqpMonitorJobID())) {
            log.debug("strEqpMonLotInfo has no information");
            return result;
        }

        if (!CimStringUtils.equals(strEqpMonLotInfo.getMonitorLotStatus(),
                BizConstant.SP_EQPMONITOR_LOTSTATUS_EXECUTING)) {
            log.debug("monitorLotStatus is not Executing");
            return result;
        }

        //Acquire the EqpMonitor section label information defined on the process of Lot
        List<Infos.EqpMonitorLabelInfo> strEqpMonLabelSeq = lotMethod.lotEqpMonitorOperationLabelGet(objCommon,
                amVerifyReqInParams.getLotID());

        boolean bLabelCheck = false;
        log.debug("strEqpMonLabelSeq.length() : {}", CimArrayUtils.getSize(strEqpMonLabelSeq));
        for (int iCnt1 = 0; iCnt1 < CimArrayUtils.getSize(strEqpMonLabelSeq); iCnt1++ ) {
            Infos.EqpMonitorLabelInfo eqpMonitorLabelInfo = strEqpMonLabelSeq.get(iCnt1);
            if ((CimStringUtils.equals(eqpMonitorLabelInfo.getOperationLabel(),
                    BizConstant.SP_EQPMONITOR_OPELABEL_POSTMEASUREMENT)
                    && CimBooleanUtils.isTrue(strEqpMonLotInfo.getExitFlag())
                    && CimBooleanUtils.isTrue(eqpMonitorLabelInfo.isExitFlag())
                    || (CimStringUtils.equals(eqpMonitorLabelInfo.getOperationLabel(),
                    BizConstant.SP_EQPMONITOR_OPELABEL_POSTMEASUREMENT)
                    && CimStringUtils.equals(strEqpMonLotInfo.getMonitorOpeKey(),
                    eqpMonitorLabelInfo.getMonitorOperationKey())))) {
                log.debug("bLabelCheck = TRUE");
                bLabelCheck = true;
                break;
            }
        }

        if (CimBooleanUtils.isFalse(bLabelCheck)) {
            log.debug("bLabelCheck == FALSE");
            return result;
        }

        log.debug("strEqpMonLotInfo.result : {}", strEqpMonLotInfo.getResult());
        if ( 1 == strEqpMonLotInfo.getResult() )   //Spec/SPC check result is failed
        {
            log.debug("1 == strEqpMonLotInfo.result");
            //Lock EqpMonitor object
            // object_Lock
            objectLockMethod.objectLock(objCommon, CimEqpMonitor.class, strEqpMonLotInfo.getEqpMonitorID());

            //Lock EqpMonitor job object
            // object_LockForEqpMonitorJob
            objectLockMethod.objectLockForEqpMonitorJob(objCommon,
                    strEqpMonLotInfo.getEqpMonitorID(),
                    strEqpMonLotInfo.getEqpMonitorJobID());

            //Create EqpMonitor Job History Event
            Inputs.EqpMonitorJobChangeEventMakeParams strEqpMonitorJobChangeEvent_Make_in = new Inputs
                    .EqpMonitorJobChangeEventMakeParams();
            strEqpMonitorJobChangeEvent_Make_in.setOpeCategory(BizConstant
                    .SP_EQPMONITORJOB_OPECATEGORY_EQPMONITORLOTFAILED);
            strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
            strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
            List<Infos.EqpMonitorLotInfo> eqpMonitorLotInfos = new ArrayList<>();
            Infos.EqpMonitorLotInfo strEqpMonitorLotInfo = new Infos.EqpMonitorLotInfo();
            strEqpMonitorLotInfo.setLotID(strEqpMonLotInfo.getLotID());
            strEqpMonitorLotInfo.setStartSeqNo(strEqpMonLotInfo.getStartSeqNo().intValue());
            eqpMonitorLotInfos.add(strEqpMonitorLotInfo);
            strEqpMonitorJobChangeEvent_Make_in.setEqpMonitorLotInfoList(eqpMonitorLotInfos);
            eventMethod.eqpMonitorJobChangeEventMake(objCommon, strEqpMonitorJobChangeEvent_Make_in );


            //Get retryCount information
            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfos = equipmentMethod.eqpMonitorJobInfoGet(objCommon,
                    strEqpMonLotInfo.getEqpMonitorID(),
                    strEqpMonLotInfo.getEqpMonitorJobID() );

            Integer retryCount = strEqpMonitorJobInfos.get(0).getRetryCount();
            Infos.EqpMonitorJobInfo strEqpMonitorJobInfo = strEqpMonitorJobInfos.get(0);
            log.debug("retryCount : {}", retryCount);

            //Get EqpMonitor information
            List<Infos.EqpMonitorDetailInfo> strEqpMonInfos = equipmentMethod.eqpMonitorInfoGet(objCommon,
                    null,
                    strEqpMonLotInfo.getEqpMonitorID());

            Infos.EqpMonitorDetailInfo strEqpMonInfo = strEqpMonInfos.get(0);

            //Acquire maxRetryCount of EqpMonitor
            Integer maxRetryCount = strEqpMonInfo.getMaxRetryCount();

            if ( retryCount < maxRetryCount ) {
                log.debug("retryCount < maxRetryCount");
                //Update retryCount of EqpMonitor job
                Long strEqpMonitorJobRetryCountIncrementOut = equipmentMethod.eqpMonitorJobretryCountIncrement(objCommon,
                        strEqpMonLotInfo.getEqpMonitorID(),
                        strEqpMonLotInfo.getEqpMonitorJobID() );

                //Delete Lot in relation to EqpMonitor job
                List<ObjectIdentifier> strEqpMonitorJobLotRemoveOut = equipmentMethod.eqpMonitorJobLotRemove(objCommon,
                        strEqpMonLotInfo.getEqpMonitorID(),
                        strEqpMonLotInfo.getEqpMonitorJobID() );

                //Create EqpMonitor Job History Event
                Inputs.EqpMonitorJobChangeEventMakeParams strEqpMonitorJobChangeEventMakeIn = new Inputs
                        .EqpMonitorJobChangeEventMakeParams();
                strEqpMonitorJobChangeEventMakeIn.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP);
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorLotInfoList(strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq());
                eventMethod.eqpMonitorJobChangeEventMake(objCommon, strEqpMonitorJobChangeEventMakeIn);


                //Status of EqpMonitor job is updated to "Requested"
                Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
                strAMJobStatusChangeRptInParm.setEquipmentID(strEqpMonLotInfo.getEquipmentID());
                strAMJobStatusChangeRptInParm.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                strAMJobStatusChangeRptInParm.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
                strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_REQUESTED);
                String strAMJobStatusChangeRptResult = autoMonitorService
                        .sxAMJobStatusChangeRpt(objCommon, strAMJobStatusChangeRptInParm);

            } else {
                log.debug("retryCount >= maxRetryCount");
                //Perform action at the time of EqpMonitor Fail
                boolean bInhibitFlag = false;
                ObjectIdentifier reasonCode = null;
                boolean bMailFlag = false;
                Infos.EqpMonitorActionInfo strEqpMonitorActionInfo = null;
                for (int iCnt2 = 0; iCnt2 < CimArrayUtils.getSize(strEqpMonInfos.get(0)
                        .getStrEqpMonitorActionInfoSeq()); iCnt2++ ) {
                    log.debug("loop to strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].strEqpMonitorActionInfoSeq.length() : {}", iCnt2);
                    Infos.EqpMonitorActionInfo eqpMonitorActionInfo = strEqpMonInfos.get(0)
                            .getStrEqpMonitorActionInfoSeq().get(iCnt2);
                    if (CimStringUtils.equals(eqpMonitorActionInfo.getEventType(),
                            BizConstant.SP_EQPMONITOR_EVENT_FAILED)
                            && CimStringUtils.equals(eqpMonitorActionInfo.getAction(),
                            BizConstant.SP_EQPMONITOR_ACTION_INHIBIT) ) {
                        log.debug("bInhibitFlag = TRUE");
                        bInhibitFlag = true;
                        reasonCode = eqpMonitorActionInfo.getReasonCodeID();
                    }
                    if (CimStringUtils.equals(eqpMonitorActionInfo.getEventType(),
                            BizConstant.SP_EQPMONITOR_EVENT_FAILED)
                            && CimStringUtils.equals(eqpMonitorActionInfo.getAction(),
                            BizConstant.SP_EQPMONITOR_ACTION_MAIL) ) {
                        log.debug("bMailFlag = TRUE");
                        bMailFlag               = true;
                        strEqpMonitorActionInfo = eqpMonitorActionInfo;
                    }
                }

                if (CimBooleanUtils.isTrue(bInhibitFlag)) {
                    log.debug("bInhibitFlag == TRUE");
                    //Inhibit equipment or chamber

                    Params.MfgRestrictReqParams strInhibitAttr = new Params.MfgRestrictReqParams();
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos
                            .EntityInhibitDetailAttributes();
                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                    if(!ObjectIdentifier.isEmpty(strEqpMonInfos.get(0).getChamberID())) {
                        log.debug("chamberID is specified");
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                        entityIdentifier.setObjectID(strEqpMonInfos.get(0).getEquipmentID());
                        entityIdentifier.setAttribution(ObjectIdentifier.fetchValue(strEqpMonInfos
                                .get(0).getChamberID()));
                        entityIdentifiers.add(entityIdentifier);
                    } else {
                        log.debug("chamberID is not specified");
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(strEqpMonInfos.get(0).getEquipmentID());
                        entityIdentifiers.add(entityIdentifier);
                    }

                    // 增加recipe inhibit,tool monitor手动方式
                    Optional.ofNullable(strEqpMonInfos.get(0).getStrEqpMonitorProductInfoSeq())
                            .ifPresent(eqpMonitorProductInfos -> {
                                eqpMonitorProductInfos.forEach(eqpMonitorProductInfo -> {
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier.setObjectID(eqpMonitorProductInfo.getRecipeID());
                                    entityIdentifiers.add(entityIdentifier);
                                });
                            });

                    entityInhibitDetailAttributes.setEntities(entityIdentifiers);
                    entityInhibitDetailAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                    entityInhibitDetailAttributes.setReasonCode(ObjectIdentifier.fetchValue(reasonCode));
                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                    entityInhibitDetailAttributes.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    Outputs.ObjLotPreviousOperationInfoGetOut  strLot_previousOperationInfo_Get_out = lotMethod.lotPreviousOperationInfoGet(objCommon, amVerifyReqInParams.getLotID());
                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfoList = new ArrayList<>();
                    Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                    entityInhibitReasonDetailInfo.setRelatedLotID(ObjectIdentifier.fetchValue(amVerifyReqInParams.getLotID()));
                    entityInhibitReasonDetailInfo.setRelatedControlJobID(ObjectIdentifier.fetchValue(strLot_previousOperationInfo_Get_out.getControlJobID()));
                    entityInhibitReasonDetailInfo.setRelatedRouteID(ObjectIdentifier.fetchValue(strLot_previousOperationInfo_Get_out.getRouteID()));
                    entityInhibitReasonDetailInfo.setRelatedProcessDefinitionID(ObjectIdentifier.fetchValue(strLot_previousOperationInfo_Get_out.getOperationID()));
                    entityInhibitReasonDetailInfo.setRelatedOperationNumber(strLot_previousOperationInfo_Get_out.getOperationNumber());
                    entityInhibitReasonDetailInfo.setRelatedOperationPassCount(strLot_previousOperationInfo_Get_out.getOperationPass());
                    entityInhibitReasonDetailInfoList.add(entityInhibitReasonDetailInfo);
                    entityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfoList);
                    strInhibitAttr.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);

                    //Inhibit Entity
                    try {
//                        Infos.EntityInhibitDetailInfo  mfgRestrictReq = constraintService.sxMfgRestrictReq(strInhibitAttr, objCommon);

                        //Step5 - txMfgRestrictReq__110
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(entityInhibitDetailAttributes);
                        mfgRestrictReq_110Params.setClaimMemo("");
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), e.getCode())) {
                            throw e;
                        }
                    }
                }

                if (CimBooleanUtils.isTrue(bMailFlag)) {
                    log.debug("bMailFlag == TRUE");
                    Infos.EqpMonitorActionExecuteIn strEqpMonitor_action_Execute_in = new Infos
                            .EqpMonitorActionExecuteIn();
                    strEqpMonitor_action_Execute_in.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                    strEqpMonitor_action_Execute_in.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
                    strEqpMonitor_action_Execute_in.setEventType(BizConstant.SP_EQPMONITOR_EVENT_FAILED);
                    strEqpMonitor_action_Execute_in.setStrEqpMonitorActionInfo(strEqpMonitorActionInfo);
                    equipmentMethod.eqpMonitorActionExecute(objCommon, strEqpMonitor_action_Execute_in );
                }

                //Update eqp status if "Eqp State at Failed" is specified
                if (!ObjectIdentifier.isEmpty(strEqpMonInfos.get(0).getEqpStateAtFailed().getEquipmentStatusCode())) {
                    log.debug("eqpStateAtFailed is specified");
                    if (!ObjectIdentifier.isEmpty(strEqpMonLotInfo.getChamberID())) {
                        //Update Chamber's Status
                        log.debug("call txChamberStatusChangeReq");

                        List<Infos.EqpChamberStatus> strEqpChamberStatus = new ArrayList<>();
                        Infos.EqpChamberStatus eqpChamberStatus = new Infos.EqpChamberStatus();
                        eqpChamberStatus.setChamberID(strEqpMonLotInfo.getChamberID());
                        eqpChamberStatus.setChamberStatusCode(strEqpMonInfos.get(0).getEqpStateAtFailed()
                                .getEquipmentStatusCode());
                        strEqpChamberStatus.add(eqpChamberStatus);

                        //-------------------------------------------------------
                        //   Call txChamberStatusChangeReq
                        //-------------------------------------------------------
                        try {
                            Results.ChamberStatusChangeReqResult chamberStatusChangeReqResult = equipmentService
                                    .sxChamberStatusChangeReq(objCommon, strEqpMonLotInfo.getEquipmentID(),
                                            strEqpChamberStatus, null);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                                log.debug("rc == RC_INVALID_STATE_TRANSITION");
                                //-------------------------
                                //   Call System Message
                                //-------------------------

                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EMONSERR);
                                String messageSb = "Equipment Monitor operation information.\n" +
                                        " EqpMonitor ID: " + strEqpMonLotInfo.getEqpMonitorID() + "\n" +
                                        " Equipment ID : " + strEqpMonLotInfo.getEquipmentID() + "\n" +
                                        " Chamber ID   : " + strEqpMonLotInfo.getChamberID() + "\n" +
                                        " Target Eqp Status : " + ObjectIdentifier.fetchValue(strEqpMonInfos.get(0)
                                        .getEqpStateAtFailed().getEquipmentStatusCode()) + "\n" +
                                        " EqpMonitor Job ID : " + strEqpMonLotInfo.getEqpMonitorJobID() + "\n" +
                                        "  Operation Point   : Equipment Monitor operation Failed.\n";
                                alertMessageRptParams.setSystemMessageText(messageSb);
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setEquipmentID(strEqpMonLotInfo.getEquipmentID());
                                alertMessageRptParams.setLotID(amVerifyReqInParams.getLotID());
                                alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp()
                                        .getReportTimeStamp().toString());
                                Results.AlertMessageRptResult alertMessageRptResult = systemService
                                        .sxAlertMessageRpt(objCommon, alertMessageRptParams);
                            } else {
                                throw e;
                            }
                        }

                    } else {
                        //Update Eqp's Status
                        log.debug("call txEqpStatusChangeReq");
                        Results.EqpStatusChangeReqResult strEqpStatusChangeReqResult = null;
                        try {
                            strEqpStatusChangeReqResult = equipmentService.sxEqpStatusChangeReq(objCommon,
                                    strEqpMonLotInfo.getEquipmentID(),
                                    strEqpMonInfos.get(0).getEqpStateAtFailed().getEquipmentStatusCode(), "");
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                                log.debug("rc == RC_INVALID_STATE_TRANSITION");
                                //-------------------------
                                //   Call System Message
                                //-------------------------

                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EMONSERR);
                                String messageSb = "Equipment Monitor operation information.\n" +
                                        " EqpMonitor ID: " + strEqpMonLotInfo.getEqpMonitorID() + "\n" +
                                        " Equipment ID : " + strEqpMonLotInfo.getEquipmentID() + "\n" +
                                        " Target Eqp Status : " + ObjectIdentifier.fetchValue(strEqpMonInfos.get(0)
                                        .getEqpStateAtFailed().getEquipmentStatusCode()) + "\n" +
                                        " EqpMonitor Job ID : " + strEqpMonLotInfo.getEqpMonitorJobID() + "\n" +
                                        "  Operation Point   : Equipment Monitor operation Failed.\n";
                                alertMessageRptParams.setSystemMessageText(messageSb);
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setEquipmentID(strEqpMonLotInfo.getEquipmentID());
                                alertMessageRptParams.setLotID(amVerifyReqInParams.getLotID());
                                alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp()
                                        .getReportTimeStamp().toString());
                                Results.AlertMessageRptResult alertMessageRptResult = systemService
                                        .sxAlertMessageRpt(objCommon, alertMessageRptParams);

                            } else {
                                throw e;
                            }
                        }
                    }
                }

                //Delete Lot in relation to EqpMonitor job
                List<ObjectIdentifier> strEqpMonitorJoblotRemoveOut = equipmentMethod.eqpMonitorJobLotRemove(objCommon,
                        strEqpMonLotInfo.getEqpMonitorID(),
                        strEqpMonLotInfo.getEqpMonitorJobID() );

                //Create EqpMonitor Job History Event
                Inputs.EqpMonitorJobChangeEventMakeParams strEqpMonitorJobChangeEventMakeIn = new Inputs
                        .EqpMonitorJobChangeEventMakeParams();
                strEqpMonitorJobChangeEventMakeIn.setOpeCategory(BizConstant.SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP);
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
                strEqpMonitorJobChangeEventMakeIn.setEqpMonitorLotInfoList(strEqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq());
                eventMethod.eqpMonitorJobChangeEventMake(objCommon, strEqpMonitorJobChangeEventMakeIn );

                //Change status of EqpMonitor job into "Failed"
                Params.AMJobStatusChangeRptInParm strAMJobStatusChangeRptInParm = new Params.AMJobStatusChangeRptInParm();
                strAMJobStatusChangeRptInParm.setEquipmentID(strEqpMonLotInfo.getEquipmentID());
                strAMJobStatusChangeRptInParm.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                strAMJobStatusChangeRptInParm.setEqpMonitorJobID(strEqpMonLotInfo.getEqpMonitorJobID());
                strAMJobStatusChangeRptInParm.setMonitorJobStatus(BizConstant.SP_EQPMONITORJOB_STATUS_FAILED);
                String strAMJobStatusChangeRptResult = autoMonitorService.sxAMJobStatusChangeRpt(objCommon,
                        strAMJobStatusChangeRptInParm);

                List<ObjectIdentifier> strEqpMonitorEqpMonitorJobIDsGetOut = equipmentMethod
                        .eqpMonitorEqpMonitorJobIDsGet(objCommon, strEqpMonLotInfo.getEqpMonitorID() );

                log.debug("strEqpMonitorEqpMonitorJobIDsGetOut.eqpMonitorJobIDs.length() : {}",
                        CimArrayUtils.getSize(strEqpMonitorEqpMonitorJobIDsGetOut));
                log.debug("strEqpMonitor_info_Get_out.strEqpMonitorDetailInfos[0].monitorStatus : {}",
                        strEqpMonInfos.get(0).getMonitorStatus());
                if ( 0 == CimArrayUtils.getSize(strEqpMonitorEqpMonitorJobIDsGetOut)
                        && !CimStringUtils.equals(strEqpMonInfos.get(0).getMonitorStatus(),
                        BizConstant.SP_EQPMONITOR_STATUS_MONITOROVER) ) {
                    log.debug("Update status of EqpMonitor");
                    //Update status of EqpMonitor
                    Params.AMStatusChangeRptInParm strAMStatusChangeRptInParm = new Params.AMStatusChangeRptInParm();
                    strAMStatusChangeRptInParm.setEquipmentID(strEqpMonLotInfo.getEquipmentID());
                    strAMStatusChangeRptInParm.setEqpMonitorID(strEqpMonLotInfo.getEqpMonitorID());
                    if (CimStringUtils.isEmpty(strEqpMonInfos.get(0).getWarningTime()) ) {
                        log.debug( "warningTime is not specified");
                        strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WAITING);
                    } else {
                        log.debug("warningTime is specified");
                        strAMStatusChangeRptInParm.setMonitorStatus(BizConstant.SP_EQPMONITOR_STATUS_WARNING);
                    }

                    strAMStatusChangeRptInParm.setUser(objCommon.getUser());
                    strAMStatusChangeRptInParm.setWarningActionFlag(false);
                    strAMStatusChangeRptInParm.setForceRunFlag(false);
                    Results.AMStatusChangeRptResult amStatusChangeRptResult = autoMonitorService
                            .sxAMStatusChangeRpt(objCommon, strAMStatusChangeRptInParm);

                }
            }
        }

        // Return to caller
        return result;
    }

    @Override
    public void sxEqpMonitorWaferUsedCountUpdateReq(Infos.ObjCommon objCommon, Params.EqpMonitorUsedCountUpdateReqInParam eqpMonitorUsedCountUpdateReqInParam){
        //-----------------------------
        //Check Input Parameter
        //-----------------------------
        ObjectIdentifier lotID = null;
        List<Infos.EqpMonitorWaferUsedCount> strEqpMonitorWaferUsedCountSeq = new ArrayList<>();
        if(CimArrayUtils.isNotEmpty(eqpMonitorUsedCountUpdateReqInParam.getStrEqpLotMonitorWaferUsedCountSeq())) {
            log.info("strEqpLotMonitorWaferUsedCountSeq length > 0");
            lotID = eqpMonitorUsedCountUpdateReqInParam.getStrEqpLotMonitorWaferUsedCountSeq().get(0).getLotID();
            strEqpMonitorWaferUsedCountSeq = eqpMonitorUsedCountUpdateReqInParam.getStrEqpLotMonitorWaferUsedCountSeq().get(0).getErrorCode();
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        List<Infos.ScrapWafers> strInputWafers = new ArrayList<>(CimArrayUtils.getSize(strEqpMonitorWaferUsedCountSeq));

        //Check Lot type
        String lotTypeGet = lotMethod.lotTypeGet(objCommon, lotID);

        if (!CimStringUtils.equals(lotTypeGet, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                && !CimStringUtils.equals(lotTypeGet, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
            log.info("Invalid input parameter");
            Validations.check(new OmCode(retCodeConfigEx.getInvalidLottypeForEqpmonitor()), lotTypeGet);
        }

        if(ObjectIdentifier.isEmpty(lotID) || 0 == CimArrayUtils.getSize(strEqpMonitorWaferUsedCountSeq)) {
            log.info("Invalid input parameter of lotID");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        for (int i = 0; i< CimArrayUtils.getSize(strEqpMonitorWaferUsedCountSeq); i++){
            log.info("Loop through strEqpMonitorWaferUsedCountSeq : {}",i);
            Infos.ScrapWafers scrapWafers = new Infos.ScrapWafers();
            scrapWafers.setWaferID(strEqpMonitorWaferUsedCountSeq.get(i).getWaferID());
            if(strEqpMonitorWaferUsedCountSeq.get(i).getEqpMonitorUsedCount() < 0) {
                log.info("Invalid eqpMonitorUsedCount");
                Validations.check(retCodeConfig.getInvalidInputParam());
            }
            strInputWafers.add(scrapWafers);
        }

        //--------------------------------
        //   Lock objects to be updated
        //--------------------------------
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        /*------------------------------------------------------------------------*/
        /*   Check conditions of the Lot                                          */
        /*------------------------------------------------------------------------*/

        log.info("Check InPostProcessFlag.");
        //----------------------------------
        //  Get InPostProcessFlag of Lot
        //----------------------------------

        Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //----------------------------------------------
        //  If Lot is in post process, returns error
        //----------------------------------------------
        if(CimBooleanUtils.isTrue(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            log.info("Lot is in post process.");

            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
            log.info("userGroupIDsLen : {}", userGroupIDsLen);


            int nCnt ;
            for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++) {
                log.info("# Loop[{}]/userID : {}", nCnt, userGroupIDs.get(nCnt));
            }
            if (nCnt == userGroupIDsLen) {
                log.info("NOT External Post Process User!");
                Validations.check(retCodeConfig.getLotInPostProcess());
            }
        }

        /**=======================================**/
        /** ProcessState should not be Processing **/
        /**=======================================**/

        String strLot_processState_Get_out = lotMethod.lotProcessStateGet(objCommon, lotID);
        if (CimStringUtils.equals(strLot_processState_Get_out,BizConstant.SP_LOT_PROCSTATE_PROCESSING)) {
            Validations.check(retCodeConfig.getInvalidLotProcstat());
        }

        /*------------------------------------------------------------------------*/
        /*   Check all claimed wafer existance                                    */
        /*------------------------------------------------------------------------*/
        lotMethod.lotMaterialsCheckExistance(objCommon,lotID,strInputWafers);

        /*------------------------------------------------------------------------*/
        /*   Do eqpMonitorUsedCount update                                        */
        /*------------------------------------------------------------------------*/

        Inputs.ObjEqpMonitorWaferUsedCountUpdateIn strEqpMonitorWaferUsedCountUpdate_in = new Inputs.ObjEqpMonitorWaferUsedCountUpdateIn();
        strEqpMonitorWaferUsedCountUpdate_in.setLotID(lotID);
        strEqpMonitorWaferUsedCountUpdate_in.setAction(BizConstant.SP_EQPMONUSEDCNT_ACTION_UPDATE);
        strEqpMonitorWaferUsedCountUpdate_in.setStrEqpMonitorWaferUsedCountSeq(strEqpMonitorWaferUsedCountSeq);
        equipmentMethod.eqpMonitorWaferUsedCountUpdate(objCommon,strEqpMonitorWaferUsedCountUpdate_in);

        /**========================================**/
        /** Create Operation History               **/
        /**========================================**/

        Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams strEqpMonitorWaferUsedCountUpdateEvent_Make_in = new Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams();
        strEqpMonitorWaferUsedCountUpdateEvent_Make_in.setLotID(lotID);
        strEqpMonitorWaferUsedCountUpdateEvent_Make_in.setTransactionID(objCommon.getTransactionID());
        strEqpMonitorWaferUsedCountUpdateEvent_Make_in.setStrEqpMonitorWaferUsedCountList(strEqpMonitorWaferUsedCountSeq);
        strEqpMonitorWaferUsedCountUpdateEvent_Make_in.setClaimMemo(eqpMonitorUsedCountUpdateReqInParam.getClaimMemo());
        eventMethod.eqpMonitorWaferUsedCountUpdateEventMake(objCommon, strEqpMonitorWaferUsedCountUpdateEvent_Make_in);
    }

}
