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
public class LotFutureHoldHistoryService {

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
     * @return com.fa.cim.dto.Infos.ProcessJobChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/2 17:22
     */
    public Infos.LotFutureHoldEventRecord getEventData(String id) {
        String sql="Select * from OMEVFHOLD where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotFutureHoldEventRecord theEventData=new Infos.LotFutureHoldEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setEntryType(convert(sqlData.get("TASK_TYPE")));
            theEventData.setHoldType(convert(sqlData.get("HOLD_TYPE")));
            theEventData.setRegisterReasonCode(convert(sqlData.get("REG_REASON_CODE")));
            theEventData.setRegisterPerson(convert(sqlData.get("REG_USER_ID")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOpeNo(convert(sqlData.get("OPE_NO")));
            theEventData.setSingleTriggerFlag(convertB(sqlData.get("SINGLE_TRIGGER_FLAG")));
            theEventData.setPostFlag(convertB(sqlData.get("POST_FLAG")));
            theEventData.setRelatedLotID(convert(sqlData.get("RELATED_LOT_ID")));
            theEventData.setReleaseReasonCode(convert(sqlData.get("RELEASE_REASON_CODE")));

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
        String sql="SELECT * FROM OMEVFHOLD_CDA WHERE REFKEY=?";
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
     * @param lotFutureHoldRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 13:36
     */
    public Response insertLotFutureHoldHistory( Infos.Ohfhldhs lotFutureHoldRecord ) {
        String hFHFHLDHS_LOT_ID           ;
        String hFHFHLDHS_ENTRY_TYPE       ;
        String hFHFHLDHS_HOLD_TYPE        ;
        String hFHFHLDHS_REG_REASON_CODE  ;
        String hFHFHLDHS_REG_PERSON_ID    ;
        String hFHFHLDHS_MAINPD_ID        ;
        String hFHFHLDHS_OPE_NO           ;
        Boolean hFHFHLDHS_SINGLE_TRIG_FLAG ;
        Boolean hFHFHLDHS_POST_FLAG        ;
        String hFHFHLDHS_RELATED_LOT_ID   ;
        String hFHFHLDHS_REL_REASON_CODE  ;
        String hFHFHLDHS_CLAIM_TIME       ;
        Double  hFHFHLDHS_CLAIM_SHOP_DATE  ;
        String hFHFHLDHS_CLAIM_USER_ID    ;
        String hFHFHLDHS_CLAIM_MEMO      ;
        String hFHFHLDHS_EVENT_CREATE_TIME ;

        hFHFHLDHS_LOT_ID            = lotFutureHoldRecord.getLot_id          ();
        hFHFHLDHS_ENTRY_TYPE        = lotFutureHoldRecord.getEntry_type      ();
        hFHFHLDHS_HOLD_TYPE         = lotFutureHoldRecord.getHold_Type       ();
        hFHFHLDHS_REG_REASON_CODE   = lotFutureHoldRecord.getReg_reason_code ();
        hFHFHLDHS_REG_PERSON_ID     = lotFutureHoldRecord.getReg_person_id   ();
        hFHFHLDHS_MAINPD_ID         = lotFutureHoldRecord.getMainpd_id       ();
        hFHFHLDHS_OPE_NO            = lotFutureHoldRecord.getOpe_no          ();
        hFHFHLDHS_SINGLE_TRIG_FLAG  = lotFutureHoldRecord.getSingle_trig_flag ();
        hFHFHLDHS_POST_FLAG         = lotFutureHoldRecord.getPost_flag();
        hFHFHLDHS_RELATED_LOT_ID    = lotFutureHoldRecord.getRelated_lot_id  ();
        hFHFHLDHS_REL_REASON_CODE   = lotFutureHoldRecord.getRel_reason_code ();
        hFHFHLDHS_CLAIM_TIME        = lotFutureHoldRecord.getClaim_time      ();
        hFHFHLDHS_CLAIM_SHOP_DATE   = lotFutureHoldRecord.getClaim_shop_date  ();
        hFHFHLDHS_CLAIM_USER_ID     = lotFutureHoldRecord.getClaim_user_id   ();
        hFHFHLDHS_CLAIM_MEMO        = lotFutureHoldRecord.getClaim_memo      ();
        hFHFHLDHS_EVENT_CREATE_TIME = lotFutureHoldRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHLOTFHOLD\n" +
                "            ( ID, LOT_ID           ,\n" +
                "                    TASK_TYPE       ,\n" +
                "                    HOLD_TYPE        ,\n" +
                "                    REG_REASON_CODE  ,\n" +
                "                    REG_USER_ID    ,\n" +
                "                    PROCESS_ID        ,\n" +
                "                    OPE_NO           ,\n" +
                "                    SINGLE_TRIGGER_FLAG ,\n" +
                "                    POST_FLAG        ,\n" +
                "                    RELATED_LOT_ID   ,\n" +
                "                    RELEASE_REASON_CODE  ,\n" +
                "                    TRX_TIME       ,\n" +
                "                    TRX_WORK_DATE  ,\n" +
                "                    TRX_USER_ID    ,\n" +
                "                    TRX_MEMO       ,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP )",generateID(Infos.Ohfhldhs.class)
                ,hFHFHLDHS_LOT_ID
                ,hFHFHLDHS_ENTRY_TYPE
                ,hFHFHLDHS_HOLD_TYPE
                ,hFHFHLDHS_REG_REASON_CODE
                ,hFHFHLDHS_REG_PERSON_ID
                ,hFHFHLDHS_MAINPD_ID
                ,hFHFHLDHS_OPE_NO
                ,hFHFHLDHS_SINGLE_TRIG_FLAG
                ,hFHFHLDHS_POST_FLAG
                ,hFHFHLDHS_RELATED_LOT_ID
                ,hFHFHLDHS_REL_REASON_CODE
                ,convert(hFHFHLDHS_CLAIM_TIME)
                ,hFHFHLDHS_CLAIM_SHOP_DATE
                ,hFHFHLDHS_CLAIM_USER_ID
                ,hFHFHLDHS_CLAIM_MEMO
                ,convert(hFHFHLDHS_EVENT_CREATE_TIME));

        return( returnOK() );
    }

}
