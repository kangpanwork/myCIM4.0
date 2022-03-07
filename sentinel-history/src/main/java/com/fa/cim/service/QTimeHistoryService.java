package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 15:22:19
 */
@Slf4j
@Repository
public class QTimeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.QTimeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/26 14:06
     */
    public Infos.QTimeEventRecord getEventData(String id) {
        String sql="Select * from OMEVQT where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.QTimeEventRecord theEventData=new Infos.QTimeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.QTimeActionEventData> actions=new ArrayList<>();
        theEventData.setActions(actions);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setWaferID(convert(sqlData.get("WAFER_ID")));
            theEventData.setOriginalQTime(convert(sqlData.get("INITIAL_QTIME_INFO")));
            theEventData.setQTimeType(convert(sqlData.get("TIMER_TYPE")));
            theEventData.setProcessDefinitionLevel(convert(sqlData.get("PRP_LEVEL")));
            theEventData.setOpeCategory(convert(sqlData.get("TASK_TYPE")));
            theEventData.setTriggerMainProcessDefinitionID(convert(sqlData.get("TRIGGER_PROCESS_ID")));
            theEventData.setTriggerOperationNumber(convert(sqlData.get("TRIGGER_OPE_NO")));
            theEventData.setTriggerBranchInfo(convert(sqlData.get("TRIGGER_CONNECT_INFO")));
            theEventData.setTriggerReturnInfo(convert(sqlData.get("TRIGGER_RETURN_INFO")));
            theEventData.setTriggerTimeStamp(convert(sqlData.get("TRIGGER_TIME")));
            theEventData.setTargetMainProcessDefinitionID(convert(sqlData.get("TARGET_PROCESS_ID")));
            theEventData.setTargetOperationNumber(convert(sqlData.get("TARGET_OPE_NO")));
            theEventData.setTargetBranchInfo(convert(sqlData.get("TARGET_CONNECT_INFO")));
            theEventData.setTargetReturnInfo(convert(sqlData.get("TARGET_RETURN_INFO")));
            theEventData.setTargetTimeStamp(convert(sqlData.get("TARGET_TIME")));
            theEventData.setPreviousTargetInfo(convert(sqlData.get("PREV_TARGET_INFO")));
            theEventData.setControl(convert(sqlData.get("SPECIAL_ACTION")));
            theEventData.setWatchdogRequired(convertB(sqlData.get("TIMER_FLAG")));
            theEventData.setActionDone(convertB(sqlData.get("ACTION_COMPLETE_FLAG")));
            theEventData.setManualCreated(convertB(sqlData.get("EDIT_FLAG")));
            theEventData.setPreTrigger(convertB(sqlData.get("PJ_COMP_TRIGGER_FLAG")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVQT_ACT WHERE REFKEY=?";
            List<Map> sqlActions = baseCore.queryAllForMap(sql, id);

            for (Map sqlAction:sqlActions){
                Infos.QTimeActionEventData action=new Infos.QTimeActionEventData();
                actions.add(action);

                action.setTargetTimeStamp(convert(sqlAction.get("TARGET_TIME")));
                action.setAction(convert(sqlAction.get("TASK_TYPE")));
                action.setReasonCode(convert(sqlAction.get("REASON_CODE")));
                action.setActionRouteID(convert(sqlAction.get("ACTION_PROCESS_ID")));
                action.setOperationNumber(convert(sqlAction.get("OPE_NO")));
                action.setTiming(convert(sqlAction.get("FH_TIMING")));
                action.setMainProcessDefinitionID(convert(sqlAction.get("PROCESS_ID")));
                action.setMessageDefinitionID(convert(sqlAction.get("NOTIFY_ID")));
                action.setWatchdogRequired(convertB(sqlAction.get("TIMER_FLAG")));
                action.setActionDone(convertB(sqlAction.get("ACTION_COMPLETE_FLAG")));
            }
        }
        return theEventData;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param refKey
     * @return java.util.List<com.fa.cim.dto.Infos.UserDataSet>
     * @exception
     * @author Ho
     * @date 2019/7/23 10:28
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVQT_CDA  WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets=new ArrayList<>();
        for (Map<String,Object> uData:uDatas) {
            Infos.UserDataSet userDataSet=new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param qTimeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/3/29 15:30
     */
    public Response insertQTimeHistory ( Infos.Ohqtimehs qTimeRecord ) {
        log.info("HistoryWatchDogServer::InsertQTimeHistory Function" );

        String hFHQTIMEHSQTIME_TYPE           = "";
        String hFHQTIMEHSLOT_ID               = "";
        String hFHQTIMEHSWAFER_ID             = "";
        String hFHQTIMEHSORG_QTIME            = "";
        String hFHQTIMEHSPD_LEVEL             = "";
        String hFHQTIMEHSOPE_CATEGORY         = "";
        String hFHQTIMEHSTRIGGER_MAINPD_ID    = "";
        String hFHQTIMEHSTRIGGER_OPE_NO       = "";
        String hFHQTIMEHSTRIGGER_BRANCH_INFO  = "";
        String hFHQTIMEHSTRIGGER_RETURN_INFO  = "";
        String hFHQTIMEHSTRIGGER_TIME         = "";
        String hFHQTIMEHSTARGET_MAINPD_ID     = "";
        String hFHQTIMEHSTARGET_OPE_NO        = "";
        String hFHQTIMEHSTARGET_BRANCH_INFO   = "";
        String hFHQTIMEHSTARGET_RETURN_INFO   = "";
        String hFHQTIMEHSTARGET_TIME          = "";
        String hFHQTIMEHSPREV_TARGET_INFO     = "";
        String hFHQTIMEHSCONTROL              = "";
        String hFHQTIMEHSCLAIM_TIME           = "";
        String hFHQTIMEHSCLAIM_USER_ID        = "";
        String hFHQTIMEHSCLAIM_MEMO           = "";
        String hFHQTIMEHSSTORE_TIME           = "";
        String hFHQTIMEHSEVENT_CREATE_TIME    = "";
        Integer hFHQTIMEHSWATCHDOG_REQ        = 0;
        Integer hFHQTIMEHSACTION_DONE_FLAG    = 0;
        Integer hFHQTIMEHSMANUAL_CREATED_FLAG = 0;
        Integer hFHQTIMEHSPRE_TRIGGER_FLAG    = 0;

        hFHQTIMEHSQTIME_TYPE           = qTimeRecord.getQtime_type()          ;
        hFHQTIMEHSLOT_ID               = qTimeRecord.getLot_id()              ;
        hFHQTIMEHSWAFER_ID             = qTimeRecord.getWafer_id()            ;
        hFHQTIMEHSORG_QTIME            = qTimeRecord.getOrg_qtime()           ;
        hFHQTIMEHSPD_LEVEL             = qTimeRecord.getPd_level()            ;
        hFHQTIMEHSOPE_CATEGORY         = qTimeRecord.getOpe_category()        ;
        hFHQTIMEHSTRIGGER_MAINPD_ID    = qTimeRecord.getTrigger_mainpd_id()   ;
        hFHQTIMEHSTRIGGER_OPE_NO       = qTimeRecord.getTrigger_ope_no()      ;
        hFHQTIMEHSTRIGGER_BRANCH_INFO  = qTimeRecord.getTrigger_branch_info() ;
        hFHQTIMEHSTRIGGER_RETURN_INFO  = qTimeRecord.getTrigger_return_info() ;
        hFHQTIMEHSTRIGGER_TIME         = qTimeRecord.getTrigger_time()        ;
        hFHQTIMEHSTARGET_MAINPD_ID     = qTimeRecord.getTarget_mainpd_id()    ;
        hFHQTIMEHSTARGET_OPE_NO        = qTimeRecord.getTarget_ope_no()       ;
        hFHQTIMEHSTARGET_BRANCH_INFO   = qTimeRecord.getTarget_branch_info()  ;
        hFHQTIMEHSTARGET_RETURN_INFO   = qTimeRecord.getTarget_return_info()  ;
        hFHQTIMEHSTARGET_TIME          = qTimeRecord.getTarget_time()         ;
        hFHQTIMEHSPREV_TARGET_INFO     = qTimeRecord.getPrev_target_info()    ;
        hFHQTIMEHSCONTROL              = qTimeRecord.getControl()             ;
        hFHQTIMEHSCLAIM_TIME           = qTimeRecord.getClaim_time()          ;
        hFHQTIMEHSCLAIM_USER_ID        = qTimeRecord.getClaim_user_id()       ;
        hFHQTIMEHSCLAIM_MEMO           = qTimeRecord.getClaim_memo()          ;
        hFHQTIMEHSSTORE_TIME           = qTimeRecord.getStore_time()          ;
        hFHQTIMEHSEVENT_CREATE_TIME    = qTimeRecord.getEvent_create_time()   ;
        hFHQTIMEHSWATCHDOG_REQ        = convertI(qTimeRecord.getWatchdog_req());
        hFHQTIMEHSACTION_DONE_FLAG    = convertI(qTimeRecord.getAction_done_flag());
        hFHQTIMEHSMANUAL_CREATED_FLAG = convertI(qTimeRecord.getManual_created_flag());
        hFHQTIMEHSPRE_TRIGGER_FLAG    = convertI(qTimeRecord.getPre_trigger_flag());

        baseCore.insert("INSERT INTO OHQT (ID,  TIMER_TYPE           ,\n"+
                        "LOT_ID               ,\n"+
                        "WAFER_ID             ,\n"+
                        "INITIAL_QTIME_INFO            ,\n"+
                        "PRP_LEVEL             ,\n"+
                        "OPE_CATEGORY         ,\n"+
                        "TRIGGER_PROCESS_ID    ,\n"+
                        "TRIGGER_OPE_NO       ,\n"+
                        "TRIGGER_CONNECT_INFO  ,\n"+
                        "TRIGGER_RETURN_INFO  ,\n"+
                        "TRIGGER_TIME         ,\n"+
                        "TARGET_PROCESS_ID     ,\n"+
                        "TARGET_OPE_NO        ,\n"+
                        "TARGET_CONNECT_INFO   ,\n"+
                        "TARGET_RETURN_INFO   ,\n"+
                        "TARGET_TIME          ,\n"+
                        "PREV_TARGET_INFO     ,\n"+
                        "SPECIAL_ACTION              ,\n"+
                        "TIMER_FLAG         ,\n"+
                        "ACTION_COMPLETE_FLAG     ,\n"+
                        "EDIT_FLAG  ,\n"+
                        "PJ_COMP_TRIGGER_FLAG     ,\n"+
                        "TRX_TIME           ,\n"+
                        "TRX_USER_ID        ,\n"+
                        "TRX_MEMO           ,\n"+
                        "STORE_TIME           ,\n"+
                        "EVENT_CREATE_TIME    )\n"+
                        "\n"+
                        "VALUES (?, ?           ,\n"+
                        "?               ,\n"+
                        "?             ,\n"+
                        "?            ,\n"+
                        "?             ,\n"+
                        "?         ,\n"+
                        "?    ,\n"+
                        "?       ,\n"+
                        "?  ,\n"+
                        "?  ,\n"+
                        "?         ,\n"+
                        "?     ,\n"+
                        "?        ,\n"+
                        "?   ,\n"+
                        "?   ,\n"+
                        "?          ,\n"+
                        "?     ,\n"+
                        "?              ,\n"+
                        "?         ,\n"+
                        "?     ,\n"+
                        "?  ,\n"+
                        "?     ,\n"+
                        "?           ,\n"+
                        "?        ,\n"+
                        "?           ,\n"+
                        "CURRENT_TIMESTAMP               ,\n"+
                        "?  ) ",generateID(Infos.Ohqtimehs.class),
                hFHQTIMEHSQTIME_TYPE,
                hFHQTIMEHSLOT_ID,
                hFHQTIMEHSWAFER_ID,
                hFHQTIMEHSORG_QTIME,
                hFHQTIMEHSPD_LEVEL,
                hFHQTIMEHSOPE_CATEGORY,
                hFHQTIMEHSTRIGGER_MAINPD_ID,
                hFHQTIMEHSTRIGGER_OPE_NO,
                hFHQTIMEHSTRIGGER_BRANCH_INFO,
                hFHQTIMEHSTRIGGER_RETURN_INFO,
                convert(hFHQTIMEHSTRIGGER_TIME),
                hFHQTIMEHSTARGET_MAINPD_ID,
                hFHQTIMEHSTARGET_OPE_NO,
                hFHQTIMEHSTARGET_BRANCH_INFO,
                hFHQTIMEHSTARGET_RETURN_INFO,
                convert(hFHQTIMEHSTARGET_TIME),
                hFHQTIMEHSPREV_TARGET_INFO,
                hFHQTIMEHSCONTROL,
                hFHQTIMEHSWATCHDOG_REQ,
                hFHQTIMEHSACTION_DONE_FLAG,
                hFHQTIMEHSMANUAL_CREATED_FLAG,
                hFHQTIMEHSPRE_TRIGGER_FLAG,
                convert(hFHQTIMEHSCLAIM_TIME),
                hFHQTIMEHSCLAIM_USER_ID,
                hFHQTIMEHSCLAIM_MEMO,
                convert(hFHQTIMEHSEVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertQTimeHistory Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param qTimeActionRecord
     * @return org.springframework.stereotype.Repository
     * @exception
     * @author Ho
     * @date 2019/3/29 15:41
     */
    public Response insertQTimeActionHistory ( Infos.OhqtimehsAction qTimeActionRecord ) {
        log.info("HistoryWatchDogServer::InsertQTimeActionHistory Function" );

        String hFHQTIMEHS_ACTIONLOT_ID             = "" ;
        String hFHQTIMEHS_ACTIONWAFER_ID           = "" ;
        String hFHQTIMEHS_ACTIONORG_QTIME          = "" ;
        String hFHQTIMEHS_ACTIONTARGET_TIME        = "" ;
        String hFHQTIMEHS_ACTIONACTION             = "" ;
        String hFHQTIMEHS_ACTIONREASON_CODE        = "" ;
        String hFHQTIMEHS_ACTIONACTION_ROUTE_ID    = "" ;
        String hFHQTIMEHS_ACTIONOPE_NO             = "" ;
        String hFHQTIMEHS_ACTIONTIMING             = "" ;
        String hFHQTIMEHS_ACTIONMAINPD_ID          = "" ;
        String hFHQTIMEHS_ACTIONMSGDEF_ID          = "" ;
        String hFHQTIMEHS_ACTIONCUSTOM_FIELD       = "" ;
        Boolean hFHQTIMEHS_ACTIONWATCHDOG_REQ       = false;
        Boolean hFHQTIMEHS_ACTIONACTION_DONE_FLAG   = false;
        String hFHQTIMEHS_ACTIONCLAIM_TIME         = "" ;

        hFHQTIMEHS_ACTIONLOT_ID           =qTimeActionRecord.getLot_id()       ;
        hFHQTIMEHS_ACTIONWAFER_ID         =qTimeActionRecord.getWafer_id()     ;
        hFHQTIMEHS_ACTIONORG_QTIME        =qTimeActionRecord.getOrg_qtime()    ;
        hFHQTIMEHS_ACTIONTARGET_TIME      =qTimeActionRecord.getTarget_time()  ;
        hFHQTIMEHS_ACTIONACTION           =qTimeActionRecord.getAction()       ;
        hFHQTIMEHS_ACTIONREASON_CODE      =qTimeActionRecord.getReason_code()  ;
        hFHQTIMEHS_ACTIONACTION_ROUTE_ID  =qTimeActionRecord.getAction_route_id();
        hFHQTIMEHS_ACTIONOPE_NO           =qTimeActionRecord.getOpe_no()       ;
        hFHQTIMEHS_ACTIONTIMING           =qTimeActionRecord.getTiming()       ;
        hFHQTIMEHS_ACTIONMAINPD_ID        =qTimeActionRecord.getMainpd_id()    ;
        hFHQTIMEHS_ACTIONMSGDEF_ID        =qTimeActionRecord.getMsgdef_id()    ;
        hFHQTIMEHS_ACTIONCUSTOM_FIELD     =qTimeActionRecord.getCustom_field() ;
        hFHQTIMEHS_ACTIONWATCHDOG_REQ                     = qTimeActionRecord.getWatchdog_req();
        hFHQTIMEHS_ACTIONACTION_DONE_FLAG                 = qTimeActionRecord.getAction_done_flag();
        hFHQTIMEHS_ACTIONCLAIM_TIME       =qTimeActionRecord.getClaim_time()   ;

        baseCore.insert("INSERT INTO OHQT_ACT (ID, LOT_ID           ,\n"+
                        "WAFER_ID         ,\n"+
                        "INITIAL_QTIME_INFO        ,\n"+
                        "TARGET_TIME      ,\n"+
                        "ACTION           ,\n"+
                        "REASON_CODE      ,\n"+
                        "ACTION_PROCESS_ID  ,\n"+
                        "OPE_NO           ,\n"+
                        "FH_TIMING           ,\n"+
                        "PROCESS_ID        ,\n"+
                        "NOTIFY_ID        ,\n"+
                        "TIMER_FLAG     ,\n"+
                        "ACTION_COMPLETE_FLAG ,\n"+
                        "TRX_TIME       )\n"+
                        "VALUES (?, ?           ,\n"+
                        "?         ,\n"+
                        "?        ,\n"+
                        "?      ,\n"+
                        "?           ,\n"+
                        "?      ,\n"+
                        "?  ,\n"+
                        "?           ,\n"+
                        "?           ,\n"+
                        "?        ,\n"+
                        "?        ,\n"+
                        "?     ,\n"+
                        "? ,\n"+
                        "? )",generateID(Infos.OhqtimehsAction.class),
                hFHQTIMEHS_ACTIONLOT_ID,
                hFHQTIMEHS_ACTIONWAFER_ID,
                hFHQTIMEHS_ACTIONORG_QTIME,
                convert(hFHQTIMEHS_ACTIONTARGET_TIME),
                hFHQTIMEHS_ACTIONACTION,
                hFHQTIMEHS_ACTIONREASON_CODE,
                hFHQTIMEHS_ACTIONACTION_ROUTE_ID,
                hFHQTIMEHS_ACTIONOPE_NO,
                hFHQTIMEHS_ACTIONTIMING,
                hFHQTIMEHS_ACTIONMAINPD_ID,
                hFHQTIMEHS_ACTIONMSGDEF_ID,
                hFHQTIMEHS_ACTIONWATCHDOG_REQ,
                hFHQTIMEHS_ACTIONACTION_DONE_FLAG,
                convert(hFHQTIMEHS_ACTIONCLAIM_TIME));

        log.info("HistoryWatchDogServer::InsertQTimeActionHistory Function" );
        return( returnOK() );
    }

}
