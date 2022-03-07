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
public class DurableXferJobStatusChangeHistoryService {

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
     * @return com.fa.cim.dto.Infos.DurableXferJobStatusChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/3 13:44
     */
    public Infos.DurableXferJobStatusChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVDURXFERSC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableXferJobStatusChangeEventRecord theEventData=new Infos.DurableXferJobStatusChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setDurableType(convert(sqlData.get("DRBL_TYPE")));
            theEventData.setOperationCategory(convert(sqlData.get("OPE_CATEGORY")));
            theEventData.setCarrierID(convert(sqlData.get("CARRIER_ID")));
            theEventData.setJobID(convert(sqlData.get("JOB_ID")));
            theEventData.setCarrierJobID(convert(sqlData.get("CARRIER_JOB_ID")));
            theEventData.setTransportType(convert(sqlData.get("TRANSPORT_TYPE")));
            theEventData.setZoneType(convert(sqlData.get("ZONE_TYPE")));
            theEventData.setN2purgeFlag(convertL(sqlData.get("N2P_FLAG")));
            theEventData.setFromMachineID(convert(sqlData.get("FROM_EQP_ID")));
            theEventData.setFromPortID(convert(sqlData.get("FROM_PORT_ID")));
            theEventData.setToStockerGroup(convert(sqlData.get("TO_STOCKER_GROUP")));
            theEventData.setToMachineID(convert(sqlData.get("TO_EQP_ID")));
            theEventData.setToPortID(convert(sqlData.get("TO_PORT_ID")));
            theEventData.setExpectedStrtTime(convert(sqlData.get("EXP_START_TIME")));
            theEventData.setExpectedEndTime(convert(sqlData.get("EXP_END_TIME")));
            theEventData.setEstimateStrtTime(convert(sqlData.get("EST_START_TIME")));
            theEventData.setEstimateEndTime(convert(sqlData.get("EST_END_TIME")));
            theEventData.setMandatoryFlag(convertL(sqlData.get("MANDATORY_FLAG")));
            theEventData.setPriority(convert(sqlData.get("PRIORITY")));
            theEventData.setJobStatus(convert(sqlData.get("JOB_STATUS")));
            theEventData.setCarrierJobStatus(convert(sqlData.get("CARRIER_JOB_STATUS")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVDURXFERSC_CDA WHERE REFKEY=?";
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

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableXferJobStatusChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 13:37
     */
    public Response insertDurableXferJobStatusChangeHistory ( Infos.Ohxferjobhs durableXferJobStatusChangeRecord ) {
        String hFHXFERJOBHS_DURABLE_TYPE       ="";
        String hFHXFERJOBHS_OPE_CATEGORY       ="";
        String hFHXFERJOBHS_CARRIER_ID         ="";
        String hFHXFERJOBHS_JOB_ID             ="";
        String hFHXFERJOBHS_CARRIER_JOB_ID     ="";
        String hFHXFERJOBHS_TRANSPORT_TYPE     ="";
        String hFHXFERJOBHS_ZONE_TYPE          ="";
        Integer hFHXFERJOBHS_N2PURGE_FLAG = 0;
        String hFHXFERJOBHS_FROM_MACHINE_ID    ="";
        String hFHXFERJOBHS_FROM_PORT_ID       ="";
        String hFHXFERJOBHS_TO_STOCKER_GROUP   ="";
        String hFHXFERJOBHS_TO_MACHINE_ID      ="";
        String hFHXFERJOBHS_TO_PORT_ID         ="";
        String hFHXFERJOBHS_EXPECTED_STRT_TIME ="";
        String hFHXFERJOBHS_EXPECTED_END_TIME  ="";
        String hFHXFERJOBHS_ESTIMATE_STRT_TIME ="";
        String hFHXFERJOBHS_ESTIMATE_END_TIME  ="";
        Integer hFHXFERJOBHS_MANDATORY_FLAG = 0;
        String hFHXFERJOBHS_PRIORITY           ="";
        String hFHXFERJOBHS_JOB_STATUS         ="";
        String hFHXFERJOBHS_CARRIER_JOB_STATUS ="";
        String hFHXFERJOBHS_CLAIM_TIME         ="";
        String hFHXFERJOBHS_CLAIM_USER_ID      ="";
        String hFHXFERJOBHS_CLAIM_MEMO         ="";
        String hFHXFERJOBHS_STORE_TIME         ="";
        String hFHXFERJOBHS_EVENT_CREATE_TIME  ="";

        hFHXFERJOBHS_DURABLE_TYPE          = durableXferJobStatusChangeRecord.getDurable_type         ();
        hFHXFERJOBHS_OPE_CATEGORY          = durableXferJobStatusChangeRecord.getOpe_category         ();
        hFHXFERJOBHS_CARRIER_ID            = durableXferJobStatusChangeRecord.getCarrier_id           ();
        hFHXFERJOBHS_JOB_ID                = durableXferJobStatusChangeRecord.getJob_id               ();
        hFHXFERJOBHS_CARRIER_JOB_ID        = durableXferJobStatusChangeRecord.getCarrier_job_id       ();
        hFHXFERJOBHS_TRANSPORT_TYPE        = durableXferJobStatusChangeRecord.getTransport_type       ();
        hFHXFERJOBHS_ZONE_TYPE             = durableXferJobStatusChangeRecord.getZone_type            ();
        hFHXFERJOBHS_N2PURGE_FLAG   = durableXferJobStatusChangeRecord.getN2purge_flag();
        hFHXFERJOBHS_FROM_MACHINE_ID       = durableXferJobStatusChangeRecord.getFrom_machine_id      ();
        hFHXFERJOBHS_FROM_PORT_ID          = durableXferJobStatusChangeRecord.getFrom_port_id         ();
        hFHXFERJOBHS_TO_STOCKER_GROUP      = durableXferJobStatusChangeRecord.getTo_stocker_group     ();
        hFHXFERJOBHS_TO_MACHINE_ID         = durableXferJobStatusChangeRecord.getTo_machine_id        ();
        hFHXFERJOBHS_TO_PORT_ID            = durableXferJobStatusChangeRecord.getTo_port_id           ();
        hFHXFERJOBHS_EXPECTED_STRT_TIME    = durableXferJobStatusChangeRecord.getExpected_start_time  ();
        hFHXFERJOBHS_EXPECTED_END_TIME     = durableXferJobStatusChangeRecord.getExpected_end_time    ();
        hFHXFERJOBHS_ESTIMATE_STRT_TIME    = durableXferJobStatusChangeRecord.getEstimate_start_time  ();
        hFHXFERJOBHS_ESTIMATE_END_TIME     = durableXferJobStatusChangeRecord.getEstimate_end_time    ();
        hFHXFERJOBHS_MANDATORY_FLAG   = durableXferJobStatusChangeRecord.getMandatory_flag();
        hFHXFERJOBHS_PRIORITY              = durableXferJobStatusChangeRecord.getPriority             ();
        hFHXFERJOBHS_JOB_STATUS            = durableXferJobStatusChangeRecord.getJob_status           ();
        hFHXFERJOBHS_CARRIER_JOB_STATUS    = durableXferJobStatusChangeRecord.getCarrier_job_status   ();
        hFHXFERJOBHS_CLAIM_TIME            = durableXferJobStatusChangeRecord.getClaim_time           ();
        hFHXFERJOBHS_CLAIM_USER_ID         = durableXferJobStatusChangeRecord.getClaim_user_id        ();
        hFHXFERJOBHS_CLAIM_MEMO            = durableXferJobStatusChangeRecord.getClaim_memo           ();
        hFHXFERJOBHS_STORE_TIME            = durableXferJobStatusChangeRecord.getStore_time           ();
        hFHXFERJOBHS_EVENT_CREATE_TIME     = durableXferJobStatusChangeRecord.getEvent_create_time    ();

        baseCore.insert("INSERT INTO OHDURXFERSC (ID, DRBL_TYPE\n" +
                "            , OPE_CATEGORY\n" +
                "            , CARRIER_ID\n" +
                "            , JOB_ID\n" +
                "            , CARRIER_JOB_ID\n" +
                "            , TRANSPORT_TYPE\n" +
                "            , ZONE_TYPE\n" +
                "            , N2P_FLAG\n" +
                "            , FROM_EQP_ID\n" +
                "            , FROM_PORT_ID\n" +
                "            , TO_STOCKER_GROUP\n" +
                "            , TO_EQP_ID\n" +
                "            , TO_PORT_ID\n" +
                "            , EXP_START_TIME\n" +
                "            , EXP_END_TIME\n" +
                "            , EST_START_TIME\n" +
                "            , EST_END_TIME\n" +
                "            , MANDATORY_FLAG\n" +
                "            , PRIORITY\n" +
                "            , JOB_STATUS\n" +
                "            , CARRIER_JOB_STATUS\n" +
                "            , TRX_TIME\n" +
                "            , TRX_USER_ID\n" +
                "            , TRX_MEMO\n" +
                "            , STORE_TIME\n" +
                "            , EVENT_CREATE_TIME\n" +
                "    )\n" +
                "        VALUES (?, ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "            , CURRENT_TIMESTAMP\n" +
                "                                   , ?\n" +
                "                                   )",generateID(Infos.Ohxferjobhs.class)
                ,hFHXFERJOBHS_DURABLE_TYPE
                ,hFHXFERJOBHS_OPE_CATEGORY
                ,hFHXFERJOBHS_CARRIER_ID
                ,hFHXFERJOBHS_JOB_ID
                ,hFHXFERJOBHS_CARRIER_JOB_ID
                ,hFHXFERJOBHS_TRANSPORT_TYPE
                ,hFHXFERJOBHS_ZONE_TYPE
                ,hFHXFERJOBHS_N2PURGE_FLAG
                ,hFHXFERJOBHS_FROM_MACHINE_ID
                ,hFHXFERJOBHS_FROM_PORT_ID
                ,hFHXFERJOBHS_TO_STOCKER_GROUP
                ,hFHXFERJOBHS_TO_MACHINE_ID
                ,hFHXFERJOBHS_TO_PORT_ID
                ,hFHXFERJOBHS_EXPECTED_STRT_TIME
                ,hFHXFERJOBHS_EXPECTED_END_TIME
                ,hFHXFERJOBHS_ESTIMATE_STRT_TIME
                ,hFHXFERJOBHS_ESTIMATE_END_TIME
                ,hFHXFERJOBHS_MANDATORY_FLAG
                ,hFHXFERJOBHS_PRIORITY
                ,hFHXFERJOBHS_JOB_STATUS
                ,hFHXFERJOBHS_CARRIER_JOB_STATUS
                ,convert(hFHXFERJOBHS_CLAIM_TIME)
                ,hFHXFERJOBHS_CLAIM_USER_ID
                ,hFHXFERJOBHS_CLAIM_MEMO
                ,convert(hFHXFERJOBHS_EVENT_CREATE_TIME));

        return( returnOK() );
    }

}
