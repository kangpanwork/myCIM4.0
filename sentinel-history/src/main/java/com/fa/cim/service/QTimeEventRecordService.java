package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
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
 * @author Ho
 * @date 2019/2/25 11:08:05
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class QTimeEventRecordService {

    @Autowired
    private QTimeHistoryService qTimeHistoryService;


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param qTimeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/3/29 10:37
     */
    public Response createQTimeEventRecord (Infos.QTimeEventRecord qTimeEventRecord, List<Infos.UserDataSet> userDataSets) {
        Infos.Ohqtimehs fhqtimehs;
        Infos.OhqtimehsAction fhqtimehs_action;

        Response iRc = returnOK();
        fhqtimehs=new Infos.Ohqtimehs();
        fhqtimehs_action=new Infos.OhqtimehsAction();

        fhqtimehs.setQtime_type(qTimeEventRecord.getQTimeType());
        fhqtimehs.setLot_id(qTimeEventRecord.getLotID());
        fhqtimehs.setWafer_id(qTimeEventRecord.getWaferID ());
        fhqtimehs.setOrg_qtime(qTimeEventRecord.getOriginalQTime());
        fhqtimehs.setPd_level(qTimeEventRecord.getProcessDefinitionLevel());
        fhqtimehs.setOpe_category(qTimeEventRecord.getOpeCategory());
        fhqtimehs.setTrigger_mainpd_id(qTimeEventRecord.getTriggerMainProcessDefinitionID());
        fhqtimehs.setTrigger_ope_no(qTimeEventRecord.getTriggerOperationNumber());
        fhqtimehs.setTrigger_branch_info(qTimeEventRecord.getTriggerBranchInfo());
        fhqtimehs.setTrigger_return_info(qTimeEventRecord.getTriggerReturnInfo());
        fhqtimehs.setTrigger_time(qTimeEventRecord.getTriggerTimeStamp());
        fhqtimehs.setTarget_mainpd_id(qTimeEventRecord.getTargetMainProcessDefinitionID());
        fhqtimehs.setTarget_ope_no(qTimeEventRecord.getTargetOperationNumber());
        fhqtimehs.setTarget_branch_info(qTimeEventRecord.getTargetBranchInfo());
        fhqtimehs.setTarget_return_info(qTimeEventRecord.getTargetReturnInfo());
        fhqtimehs.setTarget_time(qTimeEventRecord.getTargetTimeStamp());
        fhqtimehs.setPrev_target_info(qTimeEventRecord.getPreviousTargetInfo());
        fhqtimehs.setControl(qTimeEventRecord.getControl());
        fhqtimehs.setWatchdog_req          (qTimeEventRecord.getWatchdogRequired());
        fhqtimehs.setAction_done_flag      (qTimeEventRecord.getActionDone());
        fhqtimehs.setManual_created_flag   (qTimeEventRecord.getManualCreated());
        fhqtimehs.setPre_trigger_flag      (qTimeEventRecord.getPreTrigger());

        fhqtimehs.setClaim_time         ( qTimeEventRecord.getEventCommon().getEventTimeStamp        () );
        fhqtimehs.setClaim_user_id      ( qTimeEventRecord.getEventCommon().getUserID                () );
        fhqtimehs.setClaim_memo         ( qTimeEventRecord.getEventCommon().getEventMemo             () );
        fhqtimehs.setEvent_create_time  ( qTimeEventRecord.getEventCommon().getEventCreationTimeStamp() );

        iRc = qTimeHistoryService.insertQTimeHistory( fhqtimehs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        if(length(qTimeEventRecord.getActions())>0) {
            for(int i = 0; i<length(qTimeEventRecord.getActions()); i++) {

                fhqtimehs_action.setLot_id(qTimeEventRecord.getLotID());
                fhqtimehs_action.setOrg_qtime(qTimeEventRecord.getOriginalQTime());
                fhqtimehs_action.setTarget_time(qTimeEventRecord.getActions().get(i).getTargetTimeStamp());
                fhqtimehs_action.setAction(qTimeEventRecord.getActions().get(i).getAction());
                fhqtimehs_action.setReason_code(qTimeEventRecord.getActions().get(i).getReasonCode());
                fhqtimehs_action.setAction_route_id(qTimeEventRecord.getActions().get(i).getActionRouteID());
                fhqtimehs_action.setOpe_no(qTimeEventRecord.getActions().get(i).getOperationNumber());
                fhqtimehs_action.setTiming(qTimeEventRecord.getActions().get(i).getTiming());
                fhqtimehs_action.setMainpd_id(qTimeEventRecord.getActions().get(i).getMainProcessDefinitionID());
                fhqtimehs_action.setMsgdef_id(qTimeEventRecord.getActions().get(i).getMessageDefinitionID());
                fhqtimehs_action.setCustom_field(qTimeEventRecord.getActions().get(i).getCustomField());
                fhqtimehs_action.setClaim_time(qTimeEventRecord.getEventCommon().getEventTimeStamp());
                fhqtimehs_action.setWafer_id(qTimeEventRecord.getWaferID());

                fhqtimehs_action.setWatchdog_req     (qTimeEventRecord.getActions().get(i).getWatchdogRequired());
                fhqtimehs_action.setAction_done_flag (qTimeEventRecord.getActions().get(i).getActionDone());

                iRc = qTimeHistoryService.insertQTimeActionHistory( fhqtimehs_action );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
            }
        }

        return returnOK();
    }

}
