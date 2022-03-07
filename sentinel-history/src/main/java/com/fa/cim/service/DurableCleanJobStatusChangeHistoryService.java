package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.convert;
import static com.fa.cim.utils.BaseUtils.generateID;

@Repository
@Slf4j
public class DurableCleanJobStatusChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    public Infos.DurableCleanJobStatusChangeEventRecord getEventData(String id) {
        String sql = "SELECT * FROM OMEVJOBSTCH WHERE ID = ?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableCleanJobStatusChangeEventRecord theEventData = new Infos.DurableCleanJobStatusChangeEventRecord();
        if (null != sqlDatas && sqlDatas.size() > 0) {
            for (Map<String, Object> sqlData : sqlDatas) {

                theEventData.setDurableID(convert(sqlData.get("DURABLE_ID")));
                theEventData.setDurableType(convert(sqlData.get("DURABLE_TYPE")));
                theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
                theEventData.setJobStatus(convert(sqlData.get("JOB_STATUS")));
                theEventData.setStatusChangeTime(convert(sqlData.get("STAT_CHG_TIME")));
                theEventData.setProcess(convert(sqlData.get("PROCESS")));
                theEventData.setRoute(convert(sqlData.get("ROUTE")));
                theEventData.setStep(convert(sqlData.get("STEP")));
                theEventData.setOperationNo(convert(sqlData.get("OPE_NO")));
                theEventData.setEqpID(convert(sqlData.get("EQP_ID")));
                theEventData.setChamberID(convert(sqlData.get("CHAMBER_ID")));

                Infos.EventData eventCommon = new Infos.EventData();
                theEventData.setEventCommon(eventCommon);
                eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
                eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

                eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
                eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
                eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));
            }
        }

        return theEventData;
    }


    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql = "SELECT * FROM OMEVJOBSTCH_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets = new ArrayList<>();
        for (Map<String, Object> uData : uDatas) {
            Infos.UserDataSet userDataSet = new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    public void deleteFIFO(String tableName,String refKey) {
        String sql=String.format("DELETE %s WHERE REFKEY=?",tableName);
        log.info(" >>> Delete FIFO Form: {} - Reference Key: {}", tableName ,refKey);
        baseCore.insert(sql,refKey);
    }


    public List<String> getEventFIFO(String tableName){
        String sql=String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC",tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events=new ArrayList<>();
        fifos.forEach(fifo->events.add(convert(fifo[0])));
        return events;
    }

    public void createHistory(Infos.DurableCleanJobStatusChangeEventRecord eventRecord, List<Infos.UserDataSet> userDataSets) {
        Assert.isTrue(null != eventRecord, "Durable Clean Job Status evnet is null.");
        String SQL = "INSERT\n" +
                "INTO\n" +
                "    OHDRJOBSTCHS\n" +
                "    (\n" +
                "        ID,\n" +
                "        DRBL_TYPE,\n" +
                "        DRBL_ID,\n" +
                "        ACTION_CODE,\n" +
                "        JOB_STATUS,\n" +
                "        STAT_CHG_TIME,\n" +
                "        PROCESS,\n" +
                "        ROUTE,\n" +
                "        STEP,\n" +
                "        OPE_NO,\n" +
                "        EQP_ID,\n" +
                "        CHAMBER_ID,\n" +
                "        TRX_USER_ID,\n" +
                "        TRX_MEMO,\n" +
                "        STORE_TIME,\n" +
                "        EVENT_CREATE_TIME\n" +
                "    )\n" +
                "    VALUES\n" +
                "    (\n" +
                "        ?1 ,\n" +
                "        ?2 ,\n" +
                "        ?3 ,\n" +
                "        ?4 ,\n" +
                "        ?5 ,\n" +
                "        ?6 ,\n" +
                "        ?7 ,\n" +
                "        ?8 ,\n" +
                "        ?9 ,\n" +
                "        ?10 ,\n" +
                "        ?11 ,\n" +
                "        ?12 ,\n" +
                "        ?13 ,\n" +
                "        ?14 ,\n" +
                "        CURRENT_TIMESTAMP ,\n" +
                "        ?15\n" +
                "    )";
        baseCore.insert(SQL,
                generateID("OHDRJOBSTCHS"),
                eventRecord.getDurableType(),
                eventRecord.getDurableID(),
                eventRecord.getAction(),
                eventRecord.getJobStatus(),
                convert(eventRecord.getStatusChangeTime()),
                eventRecord.getProcess(),
                eventRecord.getRoute(),
                eventRecord.getStep(),
                eventRecord.getOperationNo(),
                eventRecord.getEqpID(),
                eventRecord.getChamberID(),
                eventRecord.getEventCommon().getUserID(),
                eventRecord.getEventCommon().getEventMemo(),
                convert(eventRecord.getEventCommon().getEventCreationTimeStamp()));
        log.info(">>> Create OHDRJOBSTCHS history success.");
    }
}
