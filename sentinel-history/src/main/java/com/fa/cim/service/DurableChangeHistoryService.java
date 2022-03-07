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
public class DurableChangeHistoryService {

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
    public Infos.DurableChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVDURSC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableChangeEventRecord theEventData=new Infos.DurableChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setDurableID(convert(sqlData.get("DRBL_ID")));
            theEventData.setDurableType(convert(sqlData.get("DRBL_TYPE")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setDurableStatus(convert(sqlData.get("DRBL_STATUS")));
            theEventData.setDurableSubStatus(convert(sqlData.get("DRBL_SUB_STATE_ID")));
            theEventData.setXferStatus(convert(sqlData.get("XFER_STATUS")));
            theEventData.setXferStatChgTimeStamp(convert(sqlData.get("XFER_STATE_CHG_TIME")));
            theEventData.setLocation(convert(sqlData.get("LOCATION")));

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
        String sql="SELECT * FROM OMEVDURSC_CDA WHERE REFKEY=?";
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
     * @param durableChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/9 16:06
     */
    public Response insertDurableChangeHistory( Infos.Ohdrchs durableChangeRecord ) {
        String hFHDRCHS_DURABLE_ID       ;
        String hFHDRCHS_DURABLE_TYPE     ;
        String hFHDRCHS_ACTION_CODE      ;
        String hFHDRCHS_DURABLE_STATUS   ;
        String hFHDRCHS_XFER_STATUS      ;
        String hFHDRCHS_XFER_STAT_CHG_TIME ;
        String hFHDRCHS_LOCATION         ;
        String hFHDRCHS_CLAIM_TIME       ;
        Double  hFHDRCHS_CLAIM_SHOP_DATE;
        String hFHDRCHS_CLAIM_USER_ID    ;
        String hFHDRCHS_CLAIM_MEMO       ;
        String hFHDRCHS_EVENT_CREATE_TIME ;
        String hFHDRCHS_DRBLSUBSTATE_ID  ;

        hFHDRCHS_DURABLE_ID = durableChangeRecord.getDurable_id     ();
        hFHDRCHS_DURABLE_TYPE = durableChangeRecord.getDurable_type   ();
        hFHDRCHS_ACTION_CODE = durableChangeRecord.getAction_code    ();
        hFHDRCHS_DURABLE_STATUS = durableChangeRecord.getDurable_status        ();
        hFHDRCHS_XFER_STATUS = durableChangeRecord.getXfer_status    ();
        hFHDRCHS_XFER_STAT_CHG_TIME = durableChangeRecord.getXfer_stat_chg_time    ();
        hFHDRCHS_LOCATION = durableChangeRecord.getLocation       ();
        hFHDRCHS_CLAIM_TIME = durableChangeRecord.getClaim_time     ();
        hFHDRCHS_CLAIM_SHOP_DATE  = durableChangeRecord.getClaim_shop_date();
        hFHDRCHS_CLAIM_USER_ID = durableChangeRecord.getClaim_user_id  ();
        hFHDRCHS_CLAIM_MEMO = durableChangeRecord.getClaim_memo     ();
        hFHDRCHS_EVENT_CREATE_TIME = durableChangeRecord.getEvent_create_time ();
        hFHDRCHS_DRBLSUBSTATE_ID = durableChangeRecord.getDurable_sub_status ();

        baseCore.insert("INSERT INTO OHDURSC\n" +
                "            ( ID, DRBL_ID,\n" +
                "                    DRBL_TYPE,\n" +
                "                    ACTION_CODE,\n" +
                "                    DRBL_STATUS,\n" +
                "                    XFER_STATUS,\n" +
                "                    XFER_STATE_CHG_TIME,\n" +
                "                    LOCATION,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    DRBL_SUB_STATE_ID,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                (?,  ?,    \n" +
                "                ?,  \n" +
                "                ?,   \n" +
                "                ?,\n" +
                "                ?,   \n" +
                "                ?,\n" +
                "                ?,      \n" +
                "                ?,    \n" +
                "                ?,\n" +
                "                ?, \n" +
                "                ?,    \n" +
                "                ?,\n" +
                "                ?,\n" +
                "        CURRENT_TIMESTAMP )",generateID(Infos.Ohdrchs.class)
                ,hFHDRCHS_DURABLE_ID
                ,hFHDRCHS_DURABLE_TYPE
                ,hFHDRCHS_ACTION_CODE
                ,hFHDRCHS_DURABLE_STATUS
                ,hFHDRCHS_XFER_STATUS
                ,convert(hFHDRCHS_XFER_STAT_CHG_TIME)
                ,hFHDRCHS_LOCATION
                ,convert(hFHDRCHS_CLAIM_TIME)
                ,hFHDRCHS_CLAIM_SHOP_DATE
                ,hFHDRCHS_CLAIM_USER_ID
                ,hFHDRCHS_CLAIM_MEMO
                ,convert(hFHDRCHS_EVENT_CREATE_TIME)
                ,hFHDRCHS_DRBLSUBSTATE_ID);

        return( returnOK() );
    }

}
