package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class EqpMonitorHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fheqpmonhs_def_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 22:14
     */
    public Response insertEqpMonHistory_FHEQPMONHS_DEF ( Infos.Oheqpmonhs_def fheqpmonhs_def_Record ) {
        String hFHEQPMONHS_DEFEQPMON_ID            ="";
        String hFHEQPMONHS_DEFDESCRIPTION          ="";
        String hFHEQPMONHS_DEFSCHEDULE_TYPE        ="";
        String hFHEQPMONHS_DEFSTART_TIME           ="";
        Long hFHEQPMONHS_DEFEXECUTION_INTERVAL   =0L;
        Long hFHEQPMONHS_DEFWARNING_INTERVAL     =0L;
        Long hFHEQPMONHS_DEFEXPIRATION_INTERVAL  =0L;
        Boolean hFHEQPMONHS_DEFSTAND_ALONE_FLAG     =false;
        Boolean hFHEQPMONHS_DEFKIT_FLAG             =false;
        Long hFHEQPMONHS_DEFMAX_RETRY_COUNT      =0L;
        String hFHEQPMONHS_DEFSTART_EQP_STATE_ID   ="";
        String hFHEQPMONHS_DEFPASS_EQP_STATE_ID    ="";
        String hFHEQPMONHS_DEFFAIL_EQP_STATE_ID    ="";
        String hFHEQPMONHS_DEFCLAIM_TIME           ="";

        hFHEQPMONHS_DEFEQPMON_ID           = fheqpmonhs_def_Record.getEqpMonID();
        hFHEQPMONHS_DEFDESCRIPTION         = fheqpmonhs_def_Record.getDescription();
        hFHEQPMONHS_DEFSCHEDULE_TYPE       = fheqpmonhs_def_Record.getScheduleType();
        hFHEQPMONHS_DEFSTART_TIME          = fheqpmonhs_def_Record.getStartTimeStamp();
        hFHEQPMONHS_DEFEXECUTION_INTERVAL                   = fheqpmonhs_def_Record.getExecutionInterval();
        hFHEQPMONHS_DEFWARNING_INTERVAL                     = fheqpmonhs_def_Record.getWarningInterval();
        hFHEQPMONHS_DEFEXPIRATION_INTERVAL                  = fheqpmonhs_def_Record.getExpirationInterval();
        hFHEQPMONHS_DEFSTAND_ALONE_FLAG                     = fheqpmonhs_def_Record.getStandAloneFlag();
        hFHEQPMONHS_DEFKIT_FLAG                             = fheqpmonhs_def_Record.getKitFlag();
        hFHEQPMONHS_DEFMAX_RETRY_COUNT                      = fheqpmonhs_def_Record.getMaxRetryCount();
        hFHEQPMONHS_DEFSTART_EQP_STATE_ID  = fheqpmonhs_def_Record.getMachineStateAtStart ();
        hFHEQPMONHS_DEFPASS_EQP_STATE_ID   = fheqpmonhs_def_Record.getMachineStateAtPassed ();
        hFHEQPMONHS_DEFFAIL_EQP_STATE_ID   = fheqpmonhs_def_Record.getMachineStateAtFailed ();
        hFHEQPMONHS_DEFCLAIM_TIME          = fheqpmonhs_def_Record.getClaimTime ();

        baseCore.insert("INSERT INTO OHAMON_PLAN(ID,AM_PLAN_ID,\n" +
                "            DESCRIPTION,\n" +
                "            PLAN_TYPE,\n" +
                "            START_TIME,\n" +
                "            AM_EXEC_INTERVAL,\n" +
                "            AM_WARN_INTERVAL,\n" +
                "            AM_EXPIRE_INTERVAL,\n" +
                "            KIT_FLAG,\n" +
                "            MAX_RETRY_COUNT,\n" +
                "            START_EQP_STATE_ID,\n" +
                "            PASS_EQP_STATE_ID,\n" +
                "            FAIL_EQP_STATE_ID,\n" +
                "            TRX_TIME)\n" +
                "        VALUES (?, ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?,\n" +
                "                                       ?)",generateID(Infos.Oheqpmonhs_def.class)
                ,hFHEQPMONHS_DEFEQPMON_ID
                ,hFHEQPMONHS_DEFDESCRIPTION
                ,hFHEQPMONHS_DEFSCHEDULE_TYPE
                ,convert(hFHEQPMONHS_DEFSTART_TIME)
                ,hFHEQPMONHS_DEFEXECUTION_INTERVAL
                ,hFHEQPMONHS_DEFWARNING_INTERVAL
                ,hFHEQPMONHS_DEFEXPIRATION_INTERVAL
                ,hFHEQPMONHS_DEFKIT_FLAG
                ,hFHEQPMONHS_DEFMAX_RETRY_COUNT
                ,hFHEQPMONHS_DEFSTART_EQP_STATE_ID
                ,hFHEQPMONHS_DEFPASS_EQP_STATE_ID
                ,hFHEQPMONHS_DEFFAIL_EQP_STATE_ID
                ,convert(hFHEQPMONHS_DEFCLAIM_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fheqpmonhs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 22:09
     */
    public Response insertEqpMonHistory_FHEQPMONHS (Infos.Oheqpmonhs fheqpmonhs_Record ) {
        String hFHEQPMONHSOPE_CATEGORY       ="";
        String hFHEQPMONHSEQP_ID             ="";
        String hFHEQPMONHSCHAMBER_ID         ="";
        String hFHEQPMONHSEQPMON_ID          ="";
        String hFHEQPMONHSMONITOR_TYPE       ="";
        String hFHEQPMONHSMON_STATUS         ="";
        String hFHEQPMONHSPREV_MON_STATUS    ="";
        String hFHEQPMONHSCLAIM_TIME         ="";
        String hFHEQPMONHSCLAIM_USER_ID      ="";
        String hFHEQPMONHSCLAIM_MEMO         ="";
        String hFHEQPMONHSEVENT_CREATE_TIME  ="";

        hFHEQPMONHSOPE_CATEGORY      = fheqpmonhs_Record.getOpeCategory        ();
        hFHEQPMONHSEQP_ID            = fheqpmonhs_Record.getEqpID              ();
        hFHEQPMONHSCHAMBER_ID        = fheqpmonhs_Record.getChamberID          ();
        hFHEQPMONHSEQPMON_ID         = fheqpmonhs_Record.getEqpMonID           ();
        hFHEQPMONHSMONITOR_TYPE      = fheqpmonhs_Record.getMonitorType        ();
        hFHEQPMONHSMON_STATUS        = fheqpmonhs_Record.getMonitorStatus      ();
        hFHEQPMONHSPREV_MON_STATUS   = fheqpmonhs_Record.getPrevMonitorStatus  ();
        hFHEQPMONHSCLAIM_TIME        = fheqpmonhs_Record.getClaimTime          ();
        hFHEQPMONHSCLAIM_USER_ID     = fheqpmonhs_Record.getClaimUser          ();
        hFHEQPMONHSCLAIM_MEMO        = fheqpmonhs_Record.getClaimMemo          ();
        hFHEQPMONHSEVENT_CREATE_TIME = fheqpmonhs_Record.getEventCreateTime    ();

        baseCore.insert("INSERT INTO OHAMON (ID, OPE_CATEGORY,\n" +
                "            EQP_ID,\n" +
                "            CHAMBER_ID,\n" +
                "            AM_PLAN_ID,\n" +
                "            AM_TYPE,\n" +
                "            AM_STATUS,\n" +
                "            PREV_AM_STATUS,\n" +
                "            TRX_TIME,\n" +
                "            TRX_USER_ID,\n" +
                "            TRX_MEMO,\n" +
                "            STORE_TIME,\n" +
                "            EVENT_CREATE_TIME )\n" +
                "        VALUES (?, ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                                     ?)",generateID(Infos.Oheqpmonhs.class)
                ,hFHEQPMONHSOPE_CATEGORY
                ,hFHEQPMONHSEQP_ID
                ,hFHEQPMONHSCHAMBER_ID
                ,hFHEQPMONHSEQPMON_ID
                ,hFHEQPMONHSMONITOR_TYPE
                ,hFHEQPMONHSMON_STATUS
                ,hFHEQPMONHSPREV_MON_STATUS
                ,convert(hFHEQPMONHSCLAIM_TIME)
                ,hFHEQPMONHSCLAIM_USER_ID
                ,hFHEQPMONHSCLAIM_MEMO
                ,convert(hFHEQPMONHSEVENT_CREATE_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fheqpmonhs_defprodspec_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 22:21
     */
    public Response insertEqpMonHistory_FHEQPMONHS_DEFPRODSPEC ( Infos.Oheqpmonhs_defprodspec fheqpmonhs_defprodspec_Record ) {
        String hFHEQPMONHS_DEFPRODSPECEQPMON_ID   ="";
        String hFHEQPMONHS_DEFPRODSPECPRODSPEC_ID ="";
        Long hFHEQPMONHS_DEFPRODSPECWAFER_COUNT  = 0L;
        Long hFHEQPMONHS_DEFPRODSPECSTART_SEQ_NO = 0L;
        String hFHEQPMONHS_DEFPRODSPECCLAIM_TIME  ="";

        hFHEQPMONHS_DEFPRODSPECEQPMON_ID   = fheqpmonhs_defprodspec_Record.getEqpMonID ();
        hFHEQPMONHS_DEFPRODSPECPRODSPEC_ID = fheqpmonhs_defprodspec_Record.getProdSpecID ();
        hFHEQPMONHS_DEFPRODSPECWAFER_COUNT  = fheqpmonhs_defprodspec_Record.getWaferCount();
        hFHEQPMONHS_DEFPRODSPECSTART_SEQ_NO = fheqpmonhs_defprodspec_Record.getStartSeqNo();
        hFHEQPMONHS_DEFPRODSPECCLAIM_TIME  = fheqpmonhs_defprodspec_Record.getClaimTime ();

        baseCore.insert("INSERT INTO OHAMON_DEFPROD(ID,AM_PLAN_ID,\n" +
                "            PROD_ID,\n" +
                "            WAFER_COUNT,\n" +
                "            START_SEQ_NO,\n" +
                "            TRX_TIME)\n" +
                "        VALUES (?, ?,\n" +
                "                                               ?,\n" +
                "                                               ?,\n" +
                "                                               ?,\n" +
                "                                               ?)",generateID(Infos.Oheqpmonhs_defprodspec.class)
                ,hFHEQPMONHS_DEFPRODSPECEQPMON_ID
                ,hFHEQPMONHS_DEFPRODSPECPRODSPEC_ID
                ,hFHEQPMONHS_DEFPRODSPECWAFER_COUNT
                ,hFHEQPMONHS_DEFPRODSPECSTART_SEQ_NO
                ,convert(hFHEQPMONHS_DEFPRODSPECCLAIM_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.EqpMonitorEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/1 10:46
     */
    public Infos.EqpMonitorEventRecord getEventData(String id) {
        String sql="Select * from OMEVAMON where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EqpMonitorEventRecord theEventData=new Infos.EqpMonitorEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.EqpMonitorDefEventData> monitorDefs = new ArrayList<>();
        List<Infos.EqpMonitorDefprodEventData> monitorDefprods = new ArrayList<>();
        List<Infos.EqpMonitorDefactionEventData> monitorDefactions = new ArrayList<>();
        List<Infos.EqpMonitorSchchgEventData> monitorSchchgs = new ArrayList<>();
        theEventData.setMonitorDefs(monitorDefs);
        theEventData.setMonitorDefprods(monitorDefprods);
        theEventData.setMonitorDefactions(monitorDefactions);
        theEventData.setMonitorSchchgs(monitorSchchgs);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setOpeCategory(convert(sqlData.get("TASK_TYPE")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setChamberID(convert(sqlData.get("CHAMBER_ID")));
            theEventData.setEqpMonitorID(convert(sqlData.get("AM_PLAN_ID")));
            theEventData.setMonitorType(convert(sqlData.get("AM_TYPE")));
            theEventData.setMonitorStatus(convert(sqlData.get("AM_STATUS")));
            theEventData.setPrevMonitorStatus(convert(sqlData.get("PREV_AM_STATUS")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVAMON_PLAN WHERE REFKEY=?";
            List<Map> sqlMonitorDefs=baseCore.queryAllForMap(sql,id);

            for (Map sqlMonitorDef:sqlMonitorDefs) {
                Infos.EqpMonitorDefEventData monitorDef=new Infos.EqpMonitorDefEventData();
                monitorDefs.add(monitorDef);

                monitorDef.setDescription(convert(sqlMonitorDef.get("DESCRIPTION")));
                monitorDef.setScheduleType(convert(sqlMonitorDef.get("PLAN_TYPE")));
                monitorDef.setStartTimeStamp(convert(sqlMonitorDef.get("START_TIME")));
                monitorDef.setExecutionInterval(convertL(sqlMonitorDef.get("AM_EXEC_INTERVAL")));
                monitorDef.setWarningInterval(convertL(sqlMonitorDef.get("AM_WARN_INTERVAL")));
                monitorDef.setKitFlag(convertB(sqlMonitorDef.get("KIT_FLAG")));
                monitorDef.setMaxRetryCount(convertL(sqlMonitorDef.get("MAX_RETRY_COUNT")));
                monitorDef.setMachineStateAtStart(convert(sqlMonitorDef.get("START_EQP_STATE_ID")));
                monitorDef.setMachineStateAtPassed(convert(sqlMonitorDef.get("PASS_EQP_STATE_ID")));
                monitorDef.setMachineStateAtFailed(convert(sqlMonitorDef.get("FAIL_EQP_STATE_ID")));

            }

            sql="SELECT * FROM OMEVAMON_DEFPROD WHERE REFKEY=?";
            List<Map> sqlMonitorDefprods=baseCore.queryAllForMap(sql,id);

            for (Map sqlMonitorDefprod:sqlMonitorDefprods){
                Infos.EqpMonitorDefprodEventData monitorDefprod=new Infos.EqpMonitorDefprodEventData();
                monitorDefprods.add(monitorDefprod);

                monitorDefprod.setProductSpecificationID(convert(sqlMonitorDefprod.get("PROD_ID")));
                monitorDefprod.setWaferCount(convertL(sqlMonitorDefprod.get("WAFER_COUNT")));
                monitorDefprod.setStartSeqNo(convertL(sqlMonitorDefprod.get("START_SEQ_NO")));
            }

            sql="SELECT * FROM OMEVAMON_DEFACT WHERE REFKEY=?";
            List<Map> sqlMonitorDefactions=baseCore.queryAllForMap(sql,id);

            for (Map sqlMonitorDefaction:sqlMonitorDefactions){
                Infos.EqpMonitorDefactionEventData monitorDefaction=new Infos.EqpMonitorDefactionEventData();
                monitorDefactions.add(monitorDefaction);

                monitorDefaction.setEventType(convert(sqlMonitorDefaction.get("EVENT_TYPE")));
                monitorDefaction.setAction(convert(sqlMonitorDefaction.get("TASK_TYPE")));
                monitorDefaction.setReasonCode(convert(sqlMonitorDefaction.get("REASON_CODE")));
                monitorDefaction.setSysMessageID(convert(sqlMonitorDefaction.get("NOTIFY_ID")));
//                monitorDefaction.setCustomField(convert(sqlMonitorDefaction.get("CUSTOM_FIELD")));
            }

            sql="SELECT * FROM OMEVAMON_SCHCHG WHERE REFKEY=?";
            List<Map> sqlMonitorSchchgs=baseCore.queryAllForMap(sql,id);

            for (Map sqlMonitorSchchg:sqlMonitorSchchgs) {
                Infos.EqpMonitorSchchgEventData monitorSchchg=new Infos.EqpMonitorSchchgEventData();
                monitorSchchgs.add(monitorSchchg);

                monitorSchchg.setPrevNextExecutionTime(convert(sqlMonitorSchchg.get("PREV_NEXT_EXEC_TIME")));
                monitorSchchg.setNextExecutionTime(convert(sqlMonitorSchchg.get("NEXT_EXEC_TIME")));
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
     * @param fheqpmonhs_defaction_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 22:25
     */
    public Response insertEqpMonHistory_FHEQPMONHS_DEFACTION ( Infos.Oheqpmonhs_defaction fheqpmonhs_defaction_Record ) {
        String hFHEQPMONHS_DEFACTIONEQPMON_ID     ="";
        String hFHEQPMONHS_DEFACTIONEVENT_TYPE    ="";
        String hFHEQPMONHS_DEFACTIONACTION        ="";
        String hFHEQPMONHS_DEFACTIONREASON_CODE   ="";
        String hFHEQPMONHS_DEFACTIONSYSMESSAGE_ID ="";
        String hFHEQPMONHS_DEFACTIONCLAIM_TIME    ="";

        hFHEQPMONHS_DEFACTIONEQPMON_ID     = fheqpmonhs_defaction_Record.getEqpMonID    ();
        hFHEQPMONHS_DEFACTIONEVENT_TYPE    = fheqpmonhs_defaction_Record.getEventType   ();
        hFHEQPMONHS_DEFACTIONACTION        = fheqpmonhs_defaction_Record.getAction      ();
        hFHEQPMONHS_DEFACTIONREASON_CODE   = fheqpmonhs_defaction_Record.getReasonCode  ();
        hFHEQPMONHS_DEFACTIONSYSMESSAGE_ID = fheqpmonhs_defaction_Record.getSysMessageID();
        hFHEQPMONHS_DEFACTIONCLAIM_TIME    = fheqpmonhs_defaction_Record.getClaimTime   ();

        baseCore.insert("INSERT INTO OHAMON_DEFACT(ID,AM_PLAN_ID,\n" +
                "            PROD_ID,\n" +
                "            TASK_TYPE,\n" +
                "            REASON_CODE,\n" +
                "            NOTIFY_ID,\n" +
                "            TRX_TIME)\n" +
                "        VALUES ( ?,?,\n" +
                "                                              ?,\n" +
                "                                              ?,\n" +
                "                                              ?,\n" +
                "                                              ?,\n" +
                "                                              ?)",generateID(Infos.Oheqpmonhs_defaction.class)
                ,hFHEQPMONHS_DEFACTIONEQPMON_ID
                ,hFHEQPMONHS_DEFACTIONEVENT_TYPE
                ,hFHEQPMONHS_DEFACTIONACTION
                ,hFHEQPMONHS_DEFACTIONREASON_CODE
                ,hFHEQPMONHS_DEFACTIONSYSMESSAGE_ID
                ,convert(hFHEQPMONHS_DEFACTIONCLAIM_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fheqpmonhs_schchg_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/30 22:29
     */
    public Response insertEqpMonHistory_FHEQPMONHS_SCHCHG ( Infos.Oheqpmonhs_schchg fheqpmonhs_schchg_Record ) {
        String hFHEQPMONHS_SCHCHGEQPMON_ID                ="";
        String hFHEQPMONHS_SCHCHGNEXT_EXECUTION_TIME      ="";
        String hFHEQPMONHS_SCHCHGPREV_NEXT_EXECUTION_TIME ="";
        String hFHEQPMONHS_SCHCHGCLAIM_TIME               ="";

        hFHEQPMONHS_SCHCHGEQPMON_ID                 = fheqpmonhs_schchg_Record.getEqpMonID ();
        hFHEQPMONHS_SCHCHGNEXT_EXECUTION_TIME       = fheqpmonhs_schchg_Record.getNextExecutionTime ();
        hFHEQPMONHS_SCHCHGPREV_NEXT_EXECUTION_TIME  = fheqpmonhs_schchg_Record.getPrevNextExecutionTime ();
        hFHEQPMONHS_SCHCHGCLAIM_TIME                = fheqpmonhs_schchg_Record.getClaimTime ();

        baseCore.insert("INSERT INTO OHAMON_SCHCHG(ID,AM_PLAN_ID,\n" +
                "            NEXT_EXEC_TIME,\n" +
                "            PREV_NEXT_EXEC_TIME,\n" +
                "            TRX_TIME)\n" +
                "        VALUES ( ?,?,\n" +
                "                                          ?,\n" +
                "                                          ?,\n" +
                "                                          ?)",generateID(Infos.Oheqpmonhs_schchg.class)
                ,hFHEQPMONHS_SCHCHGEQPMON_ID
                ,convert(hFHEQPMONHS_SCHCHGNEXT_EXECUTION_TIME)
                ,convert(hFHEQPMONHS_SCHCHGPREV_NEXT_EXECUTION_TIME)
                ,convert(hFHEQPMONHS_SCHCHGCLAIM_TIME) );

        return( returnOK() );
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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVAMON_CDA WHERE REFKEY=?";
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName,String refKey) {
        String sql=String.format("DELETE %s WHERE REFKEY=?",tableName);
        baseCore.insert(sql,refKey);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.Custom.List<java.lang.String>
     * @exception
     * @author Ho
     * @date 2019/4/19 17:41
     */
    public List<String> getEventFIFO(String tableName){
        String sql=String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC",tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events=new ArrayList<>();
        fifos.forEach(fifo->events.add(convert(fifo[0])));
        return events;
    }

}
