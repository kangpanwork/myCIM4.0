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
public class DurableHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param ChamberStatusChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 17:36
     */
    public Response insertChamberStatusChangeHistory(Infos.Ohcschs ChamberStatusChangeRecord ) {
        String hFHCSCHS_EQP_ID                ;
        String hFHCSCHS_EQP_NAME              ;
        String hFHCSCHS_PROCRSC_ID            ;
        String hFHCSCHS_AREA_ID               ;
        String hFHCSCHS_PR_STATE              ;
        String hFHCSCHS_CLAIM_USER_ID         ;
        String hFHCSCHS_START_TIME            ;
        Double  hFHCSCHS_START_SHOP_DATE;
        String hFHCSCHS_END_TIME              ;
        Double  hFHCSCHS_END_SHOP_DATE;
        String hFHCSCHS_NEW_PR_STATE          ;
        String hFHCSCHS_CLAIM_MEMO            ;
        String hFHCSCHS_E10_STATE             ;
        String hFHCSCHS_ACT_E10_STATE         ;
        String hFHCSCHS_ACT_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_E10_STATE         ;
        String hFHCSCHS_NEW_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_ACT_E10_STATE     ;
        String hFHCSCHS_NEW_ACT_CHAMBER_STATE ;
        String hFHCSCHS_EVENT_CREATE_TIME     ;

        hFHCSCHS_EQP_ID = ChamberStatusChangeRecord.getEqp_id          ();
        hFHCSCHS_EQP_NAME = ChamberStatusChangeRecord.getEqp_name        ();
        hFHCSCHS_PROCRSC_ID = ChamberStatusChangeRecord.getProcrsc_id      ();
        hFHCSCHS_AREA_ID = ChamberStatusChangeRecord.getArea_id         ();
        hFHCSCHS_PR_STATE = ChamberStatusChangeRecord.getPr_state        ();
        hFHCSCHS_CLAIM_USER_ID = ChamberStatusChangeRecord.getClaim_user_id   ();
        hFHCSCHS_START_TIME = ChamberStatusChangeRecord.getStart_time      ();
        hFHCSCHS_START_SHOP_DATE  = ChamberStatusChangeRecord.getStart_shop_date ();
        hFHCSCHS_END_TIME = ChamberStatusChangeRecord.getEnd_time        ();
        hFHCSCHS_END_SHOP_DATE    = ChamberStatusChangeRecord.getEnd_shop_date();
        hFHCSCHS_NEW_PR_STATE = ChamberStatusChangeRecord.getNew_pr_state    ();
        hFHCSCHS_CLAIM_MEMO = ChamberStatusChangeRecord.getClaim_memo      ();
        hFHCSCHS_E10_STATE = ChamberStatusChangeRecord.getE10_state             ();
        hFHCSCHS_ACT_E10_STATE = ChamberStatusChangeRecord.getAct_e10_state         ();
        hFHCSCHS_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getAct_chamber_state     ();
        hFHCSCHS_NEW_E10_STATE = ChamberStatusChangeRecord.getNew_e10_state         ();
        hFHCSCHS_NEW_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_chamber_state     ();
        hFHCSCHS_NEW_ACT_E10_STATE = ChamberStatusChangeRecord.getNew_act_e10_state     ();
        hFHCSCHS_NEW_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_act_chamber_state ();
        hFHCSCHS_EVENT_CREATE_TIME = ChamberStatusChangeRecord.getEvent_create_time ();


        baseCore.insert("INSERT INTO OHCMBSC\n" +
                "            ( ID, EQP_ID                 ,\n" +
                "                    EQP_NAME               ,\n" +
                "                    PROCRES_ID             ,\n" +
                "                    BAY_ID                ,\n" +
                "                    PROCRES_STATE_ID               ,\n" +
                "                    TRX_USER_ID          ,\n" +
                "                    START_TIME             ,\n" +
                "                    START_WORK_DATE        ,\n" +
                "                    END_TIME               ,\n" +
                "                    END_WORK_DATE          ,\n" +
                "                    NEW_PROCRES_STATE_ID           ,\n" +
                "                    TRX_MEMO             ,\n" +
                "                    STORE_TIME             ,\n" +
                "                    EVENT_CREATE_TIME      ,\n" +
                "                    E10_STATE_ID              ,\n" +
                "                    ACTUAL_E10_STATE_ID          ,\n" +
                "                    ACTUAL_CMB_STATE_ID      ,\n" +
                "                    NEW_E10_STATE_ID          ,\n" +
                "                    NEW_CMB_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_E10_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_CMB_STATE_ID        )\n" +
                "        Values\n" +
                "                (  ?,?,\n" +
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
                "            CURRENT_TIMESTAMP               ,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Ohcschs.class)
                ,hFHCSCHS_EQP_ID
                ,hFHCSCHS_EQP_NAME
                ,hFHCSCHS_PROCRSC_ID
                ,hFHCSCHS_AREA_ID
                ,hFHCSCHS_PR_STATE
                ,hFHCSCHS_CLAIM_USER_ID
                ,convert(hFHCSCHS_START_TIME)
                ,hFHCSCHS_START_SHOP_DATE
                ,hFHCSCHS_END_TIME
                ,hFHCSCHS_END_SHOP_DATE
                ,hFHCSCHS_NEW_PR_STATE
                ,hFHCSCHS_CLAIM_MEMO
                ,convert(hFHCSCHS_EVENT_CREATE_TIME)
                ,hFHCSCHS_E10_STATE
                ,hFHCSCHS_ACT_E10_STATE
                ,hFHCSCHS_ACT_CHAMBER_STATE
                ,hFHCSCHS_NEW_E10_STATE
                ,hFHCSCHS_NEW_CHAMBER_STATE
                ,hFHCSCHS_NEW_ACT_E10_STATE
                ,hFHCSCHS_NEW_ACT_CHAMBER_STATE );

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
     * @return com.fa.cim.dto.Infos.ChamberStatusChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/1 10:38
     */
    public Infos.DurableEventRecord getEventData(String id) {
        String sql="Select * from OMEVDUR where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableEventRecord theEventData=new Infos.DurableEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setDurableType(convert(sqlData.get("DRBL_TYPE")));
            theEventData.setDurableID(convert(sqlData.get("DRBL_ID")));
            theEventData.setDescription(convert(sqlData.get("DESCRIPTION")));
            theEventData.setCategoryID(convert(sqlData.get("CATEGORY_ID")));
            theEventData.setInstanceName(convert(sqlData.get("FAB_INSTANCE")));
            theEventData.setUsageCheckRequiredFlag(convertB(sqlData.get("USAGE_CHECK_REQ")));
            theEventData.setDurationLimit(convertD(sqlData.get("MAX_USAGE_DUR")));
            theEventData.setTimeUsedLimit(convertL(sqlData.get("MAX_USAGE_COUNT")));
            theEventData.setIntervalBetweenPM(convertL(sqlData.get("PM_INTERVAL_TIME")));
            theEventData.setContents(convert(sqlData.get("CONTENTS")));
            theEventData.setContentsSize(convertL(sqlData.get("CONTENTS_SIZE")));
            theEventData.setCapacity(convertL(sqlData.get("CAPACITY")));

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
     * @date 2019/7/1 10:38
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVDUR_CDA WHERE REFKEY=?";
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
     * @param fhdrblhs
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/11 13:48
     */
    public Response insertDurableHistory( Infos.Ohdrblhs fhdrblhs ) {
        String hFHDRBLHS_ACTION           ;
        String hFHDRBLHS_DURABLE_TYPE     ;
        String hFHDRBLHS_DURABLE_ID       ;
        String hFHDRBLHS_DESCRIPTION      ;
        String hFHDRBLHS_CATEGORY_ID      ;
        String hFHDRBLHS_INSTANCE_NAME    ;
        Boolean hFHDRBLHS_USAGE_CHECK_REQ;
        Double  hFHDRBLHS_DURATION_LIMIT;
        Integer hFHDRBLHS_TIMES_USED_LIMIT;
        Integer hFHDRBLHS_INTVL_BTWN_PM;
        String hFHDRBLHS_CONTENTS         ;
        Integer hFHDRBLHS_CONTENTS_SIZE;
        Integer hFHDRBLHS_CAPACITY;
        String hFHDRBLHS_CLAIM_TIME       ;
        Double  hFHDRBLHS_CLAIM_SHOP_DATE;
        String hFHDRBLHS_CLAIM_USER_ID    ;
        String hFHDRBLHS_CLAIM_MEMO       ;
        String hFHDRBLHS_STORE_TIME       ;
        String hFHDRBLHS_EVENT_CREATE_TIME;

        hFHDRBLHS_ACTION  = fhdrblhs.getAction ();
        hFHDRBLHS_DURABLE_TYPE  = fhdrblhs.getDurable_type ();
        hFHDRBLHS_DURABLE_ID  = fhdrblhs.getDurable_id ();
        hFHDRBLHS_DESCRIPTION  = fhdrblhs.getDescription ();
        hFHDRBLHS_CATEGORY_ID  = fhdrblhs.getCategory_id ();
        hFHDRBLHS_INSTANCE_NAME  = fhdrblhs.getInstance_name ();
        hFHDRBLHS_USAGE_CHECK_REQ    = fhdrblhs.getUsage_check_required();
        hFHDRBLHS_DURATION_LIMIT     = fhdrblhs.getDuration_limit();
        hFHDRBLHS_TIMES_USED_LIMIT   = fhdrblhs.getTimes_used_limit();
        hFHDRBLHS_INTVL_BTWN_PM      = fhdrblhs.getInterval_between_pm();
        hFHDRBLHS_CONTENTS  = fhdrblhs.getContents ();
        hFHDRBLHS_CONTENTS_SIZE      = fhdrblhs.getContents_size();
        hFHDRBLHS_CAPACITY           = fhdrblhs.getCapacity();
        hFHDRBLHS_CLAIM_TIME  = fhdrblhs.getClaim_time ();
        hFHDRBLHS_CLAIM_SHOP_DATE    = fhdrblhs.getClaim_shop_date();
        hFHDRBLHS_CLAIM_USER_ID  = fhdrblhs.getClaim_user_id ();
        hFHDRBLHS_CLAIM_MEMO  = fhdrblhs.getClaim_memo ();
        hFHDRBLHS_EVENT_CREATE_TIME  = fhdrblhs.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHDUR (ID, TASK_TYPE,                     DRBL_TYPE,               DRBL_ID,\n" +
                "            DESCRIPTION,                CATEGORY_ID,                FAB_INSTANCE,\n" +
                "            USAGE_CHECK_REQ,            MAX_USAGE_DUR,             MAX_USAGE_COUNT,\n" +
                "            PM_INTERVAL_TIME,              CONTENTS,                   CONTENTS_SIZE,\n" +
                "            CAPACITY,\n" +
                "            TRX_TIME,                 TRX_WORK_DATE,            TRX_USER_ID,\n" +
                "            TRX_MEMO,                 STORE_TIME,                 EVENT_CREATE_TIME )\n" +
                "        VALUES   ( ?,?,          ?,    ?,\n" +
                "                               ?,     ?,     ?,\n" +
                "                               ?, ?,  ?,\n" +
                "                               ?,   ?,        ?,\n" +
                "                               ?,\n" +
                "                               ?,      ?, ?,\n" +
                "                               ?,      CURRENT_TIMESTAMP,          ?)",generateID(Infos.Ohdrblhs.class)
                ,hFHDRBLHS_ACTION
                ,hFHDRBLHS_DURABLE_TYPE
                ,hFHDRBLHS_DURABLE_ID
                ,hFHDRBLHS_DESCRIPTION
                ,hFHDRBLHS_CATEGORY_ID
                ,hFHDRBLHS_INSTANCE_NAME
                ,hFHDRBLHS_USAGE_CHECK_REQ
                ,hFHDRBLHS_DURATION_LIMIT
                ,hFHDRBLHS_TIMES_USED_LIMIT
                ,hFHDRBLHS_INTVL_BTWN_PM
                ,hFHDRBLHS_CONTENTS
                ,hFHDRBLHS_CONTENTS_SIZE
                ,hFHDRBLHS_CAPACITY
                ,convert(hFHDRBLHS_CLAIM_TIME)
                ,hFHDRBLHS_CLAIM_SHOP_DATE
                ,hFHDRBLHS_CLAIM_USER_ID
                ,hFHDRBLHS_CLAIM_MEMO
                ,convert(hFHDRBLHS_EVENT_CREATE_TIME ));

        return returnOK();
    }

}
