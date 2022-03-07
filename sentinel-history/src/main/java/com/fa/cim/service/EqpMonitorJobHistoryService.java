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
//@Transactional(rollbackFor = Exception.class)
public class EqpMonitorJobHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhemjobhs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 15:04
     */
    public Response insertEqpMonJobHistory_FHEQPMONJOBHS ( Infos.Oheqpmonjobhs fhemjobhs_Record ) {
        String hFHEQPMONJOBHSOPE_CATEGORY       ="";
        String hFHEQPMONJOBHSEQP_ID             ="";
        String hFHEQPMONJOBHSCHAMBER_ID         ="";
        String hFHEQPMONJOBHSEQPMON_ID          ="";
        String hFHEQPMONJOBHSEQPMONJOB_ID       ="";
        String hFHEQPMONJOBHSMONJOB_STATUS      ="";
        String hFHEQPMONJOBHSPREV_MONJOB_STATUS ="";
        Long hFHEQPMONJOBHSRETRY_COUNT               =0L;
        String hFHEQPMONJOBHSCLAIM_TIME         ="";
        String hFHEQPMONJOBHSCLAIM_USER_ID      ="";
        String hFHEQPMONJOBHSCLAIM_MEMO         ="";
        String hFHEQPMONJOBHSSTORE_TIME         ="";
        String hFHEQPMONJOBHSEVENT_CREATE_TIME  ="";

        hFHEQPMONJOBHSOPE_CATEGORY       = fhemjobhs_Record.getOpeCategory        ();
        hFHEQPMONJOBHSEQP_ID             = fhemjobhs_Record.getEqpID              ();
        hFHEQPMONJOBHSCHAMBER_ID         = fhemjobhs_Record.getChamberID          ();
        hFHEQPMONJOBHSEQPMON_ID          = fhemjobhs_Record.getEqpMonID           ();
        hFHEQPMONJOBHSEQPMONJOB_ID       = fhemjobhs_Record.getEqpMonJobID        ();
        hFHEQPMONJOBHSMONJOB_STATUS      = fhemjobhs_Record.getMonJobStatus       ();
        hFHEQPMONJOBHSPREV_MONJOB_STATUS = fhemjobhs_Record.getPrevMonJobStatus   ();
        hFHEQPMONJOBHSRETRY_COUNT  = fhemjobhs_Record.getRetryCount();
        hFHEQPMONJOBHSCLAIM_TIME         = fhemjobhs_Record.getClaimTime          ();
        hFHEQPMONJOBHSCLAIM_USER_ID      = fhemjobhs_Record.getClaimUser          ();
        hFHEQPMONJOBHSCLAIM_MEMO         = fhemjobhs_Record.getClaimMemo          ();
        hFHEQPMONJOBHSEVENT_CREATE_TIME  = fhemjobhs_Record.getEventCreateTime    ();

        baseCore.insert("INSERT INTO OHAMONJOB(ID,OPE_CATEGORY,\n" +
                "            EQP_ID,\n" +
                "            CHAMBER_ID,\n" +
                "            AM_PLAN_ID,\n" +
                "            AM_JOB_ID,\n" +
                "            AM_JOB_STATUS,\n" +
                "            PREV_AM_JOB_STATUS,\n" +
                "            RETRY_COUNT,\n" +
                "            TRX_TIME,\n" +
                "            TRX_USER_ID,\n" +
                "            TRX_MEMO,\n" +
                "            STORE_TIME,\n" +
                "            EVENT_CREATE_TIME)\n" +
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
                "                                     ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                                     ?)",generateID(Infos.Oheqpmonjobhs.class)
                ,hFHEQPMONJOBHSOPE_CATEGORY
                ,hFHEQPMONJOBHSEQP_ID
                ,hFHEQPMONJOBHSCHAMBER_ID
                ,hFHEQPMONJOBHSEQPMON_ID
                ,hFHEQPMONJOBHSEQPMONJOB_ID
                ,hFHEQPMONJOBHSMONJOB_STATUS
                ,hFHEQPMONJOBHSPREV_MONJOB_STATUS
                ,hFHEQPMONJOBHSRETRY_COUNT
                ,convert(hFHEQPMONJOBHSCLAIM_TIME)
                ,hFHEQPMONJOBHSCLAIM_USER_ID
                ,hFHEQPMONJOBHSCLAIM_MEMO
                ,convert(hFHEQPMONJOBHSEVENT_CREATE_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhemjobhs_lot_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 15:09
     */
    public Response insertEqpMonJobHistory_FHEQPMONJOBHS_LOT ( Infos.Oheqpmonjobhs_lot fhemjobhs_lot_Record ) {
        String hFHEQPMONJOBHS_LOTEQPMONJOB_ID  ="";
        String hFHEQPMONJOBHS_LOTEVENT_TYPE    ="";
        String hFHEQPMONJOBHS_LOTLOT_ID        ="";
        String hFHEQPMONJOBHS_LOTPRODSPEC_ID   ="";
        String hFHEQPMONJOBHS_LOTCLAIM_TIME    ="";
        Long hFHEQPMONJOBHS_LOTSTART_SEQ_NO = 0L;

        hFHEQPMONJOBHS_LOTEQPMONJOB_ID    = fhemjobhs_lot_Record.getEqpMonJobID ();
        hFHEQPMONJOBHS_LOTEVENT_TYPE      = fhemjobhs_lot_Record.getEventType ();
        hFHEQPMONJOBHS_LOTLOT_ID          = fhemjobhs_lot_Record.getLotID ();
        hFHEQPMONJOBHS_LOTPRODSPEC_ID     = fhemjobhs_lot_Record.getProdSpecID();
        hFHEQPMONJOBHS_LOTCLAIM_TIME      = fhemjobhs_lot_Record.getClaimTime ();
        hFHEQPMONJOBHS_LOTSTART_SEQ_NO = fhemjobhs_lot_Record.getStartSeqNo();

        baseCore.insert("INSERT INTO OHAMONJOB_LOT(ID,AM_JOB_ID,\n" +
                "            EVENT_TYPE,\n" +
                "            LOT_ID,\n" +
                "            PROD_ID,\n" +
                "            START_SEQ_NO,\n" +
                "            TRX_TIME)\n" +
                "        VALUES (?, ?,\n" +
                "                                          ?,\n" +
                "                                          ?,\n" +
                "                                          ?,\n" +
                "                                          ?,\n" +
                "                                          ?)",generateID(Infos.Oheqpmonjobhs_lot.class)
                ,hFHEQPMONJOBHS_LOTEQPMONJOB_ID
                ,hFHEQPMONJOBHS_LOTEVENT_TYPE
                ,hFHEQPMONJOBHS_LOTLOT_ID
                ,hFHEQPMONJOBHS_LOTPRODSPEC_ID
                ,hFHEQPMONJOBHS_LOTSTART_SEQ_NO
                ,convert(hFHEQPMONJOBHS_LOTCLAIM_TIME ));

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
    public Infos.EqpMonitorJobEventRecord getEventData(String id) {
        String sql="Select * from OMEVAMONJOB where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EqpMonitorJobEventRecord theEventData=new Infos.EqpMonitorJobEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.EqpMonitorLotEventData> monitorLots=new ArrayList<>();
        theEventData.setMonitorLots(monitorLots);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setOpeCategory(convert(sqlData.get("OPE_CATEGORY")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setChamberID(convert(sqlData.get("CHAMBER_ID")));
            theEventData.setEqpMonitorID(convert(sqlData.get("AM_PLAN_ID")));
            theEventData.setEqpMonitorJobID(convert(sqlData.get("AM_JOB_ID")));
            theEventData.setMonitorJobStatus(convert(sqlData.get("AM_JOB_STATUS")));
            theEventData.setPrevMonitorJobStatus(convert(sqlData.get("PREV_AM_JOB_STATUS")));
            theEventData.setRetryCount(convertL(sqlData.get("RETRY_COUNT")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVAMONJOB_LOT WHERE REFKEY=?";
            List<Map> sqlMonitorLots=baseCore.queryAllForMap(sql,id);

            for (Map sqlMonitorLot:sqlMonitorLots) {
                Infos.EqpMonitorLotEventData monitorLot=new Infos.EqpMonitorLotEventData();
                monitorLots.add(monitorLot);

                monitorLot.setLotID(convert(sqlMonitorLot.get("LOT_ID")));
                monitorLot.setProductSpecificationID(convert(sqlMonitorLot.get("PROD_ID")));
                monitorLot.setStartSeqNo(convertL(sqlMonitorLot.get("START_SEQ_NO")));
                monitorLot.setMainPDID(convert(sqlMonitorLot.get("PROCESS_ID")));
                monitorLot.setOpeNo(convert(sqlMonitorLot.get("OPE_NO")));
                monitorLot.setPdID(convert(sqlMonitorLot.get("STEP_ID")));
                monitorLot.setOpePassCount(convertL(sqlMonitorLot.get("PASS_COUNT")));
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
                ,hFHEQPMONHS_SCHCHGNEXT_EXECUTION_TIME
                ,hFHEQPMONHS_SCHCHGPREV_NEXT_EXECUTION_TIME
                ,hFHEQPMONHS_SCHCHGCLAIM_TIME );

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
        String sql="SELECT * FROM OMEVAMONJOB_CDA WHERE REFKEY=?";
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
