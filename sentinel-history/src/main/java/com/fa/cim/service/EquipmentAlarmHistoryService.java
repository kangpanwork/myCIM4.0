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
public class EquipmentAlarmHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentAlarmRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 16:55
     */
    public Response insertEquipmentAlarmHistory(Infos.Ohalmhs equipmentAlarmRecord ) {
        String hFHALMHS_EQP_ID         ;
        String hFHALMHS_EQP_NAME       ;
        String hFHALMHS_STK_ID         ;
        String hFHALMHS_STK_NAME       ;
        String hFHALMHS_AGV_ID         ;
        String hFHALMHS_ALARM_TEXT     ;
        String hFHALMHS_CLAIM_TIME     ;
        Double  hFHALMHS_CLAIM_SHOP_DATE;
        String hFHALMHS_CLAIM_USER_ID  ;
        String hFHALMHS_ALARM_CATEGORY ;
        String hFHALMHS_ALARM_CODE     ;
        String hFHALMHS_ALARM_ID       ;
        String hFHALMHS_CLAIM_MEMO     ;
        String hFHALMHS_EVENT_CREATE_TIME ;

        hFHALMHS_EQP_ID = equipmentAlarmRecord.getEqp_id          ();
        hFHALMHS_EQP_NAME = equipmentAlarmRecord.getEqp_name        ();
        hFHALMHS_STK_ID = equipmentAlarmRecord.getStk_id          ();
        hFHALMHS_STK_NAME = equipmentAlarmRecord.getStk_name        ();
        hFHALMHS_AGV_ID = equipmentAlarmRecord.getAgv_id          ();
        hFHALMHS_ALARM_CATEGORY = equipmentAlarmRecord.getAlarm_category  ();
        hFHALMHS_ALARM_CODE = equipmentAlarmRecord.getAlarm_code      ();
        hFHALMHS_ALARM_ID = equipmentAlarmRecord.getAlarm_id        ();
        hFHALMHS_ALARM_TEXT = equipmentAlarmRecord.getAlarm_text      ();
        hFHALMHS_CLAIM_TIME = equipmentAlarmRecord.getClaim_time      ();
        hFHALMHS_CLAIM_SHOP_DATE  = equipmentAlarmRecord.getClaim_shop_date  ();
        hFHALMHS_CLAIM_USER_ID = equipmentAlarmRecord.getClaim_user_id   ();
        hFHALMHS_CLAIM_MEMO = equipmentAlarmRecord.getClaim_memo      ();
        hFHALMHS_EVENT_CREATE_TIME = equipmentAlarmRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHALARM\n" +
                "            (  ID,EQP_ID,\n" +
                "                    EQP_NAME,\n" +
                "                    STK_ID,\n" +
                "                    STK_NAME,\n" +
                "                    ALARM_CATEGORY,\n" +
                "                    ALARM_CODE,\n" +
                "                    ALARM_ID,\n" +
                "                    ALARM_TEXT,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                (  ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP )",generateID(Infos.Ohalmhs.class)
                ,hFHALMHS_EQP_ID
                ,hFHALMHS_EQP_NAME
                ,hFHALMHS_STK_ID
                ,hFHALMHS_STK_NAME
                ,hFHALMHS_ALARM_CATEGORY
                ,hFHALMHS_ALARM_CODE
                ,hFHALMHS_ALARM_ID
                ,hFHALMHS_ALARM_TEXT
                ,convert(hFHALMHS_CLAIM_TIME)
                ,hFHALMHS_CLAIM_USER_ID
                ,hFHALMHS_CLAIM_MEMO
                ,convert(hFHALMHS_EVENT_CREATE_TIME));

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
     * @return com.fa.cim.dto.Infos.EquipmentAlarmEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/28 17:03
     */
    public Infos.EquipmentAlarmEventRecord getEventData(String id) {
        String sql="Select * from OMEVALARM where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EquipmentAlarmEventRecord theEventData=new Infos.EquipmentAlarmEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setStockerID(convert(sqlData.get("STK_ID")));
            theEventData.setAlarmCategory(convert(sqlData.get("ALARM_CATEGORY")));
            theEventData.setAlarmCode(convert(sqlData.get("ALARM_CODE")));
            theEventData.setAlarmID(convert(sqlData.get("ALARM_ID")));
            theEventData.setAlarmText(convert(sqlData.get("ALARM_TEXT")));

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
     * @date 2019/6/28 17:06
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVALARM_CDA WHERE REFKEY=?";
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
