package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class SystemMessageHistoryService {

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
     * @return com.fa.cim.dto.Infos.DurableReworkEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/23 10:17
     */
    public Infos.SystemMessageEventRecord getEventData(String id) {
        String sql="Select * from OMEVNOTIFY where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.SystemMessageEventRecord theEventData=new Infos.SystemMessageEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setSubSystemID(convert(sqlData.get("SERVICE_ID")));
            theEventData.setSystemMessageCode(convert(sqlData.get("NOTIFY_ID")));
            theEventData.setSystemMessageText(convert(sqlData.get("NOTIFY_MSG")));
            theEventData.setNotifyFlag(convertB(sqlData.get("NOTIFY_FLAG")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setEquipmentState(convert(sqlData.get("EQP_STATE_ID")));
            theEventData.setStockerID(convert(sqlData.get("STK_ID")));
            theEventData.setStockerState(convert(sqlData.get("STK_STATE")));
            theEventData.setAGVID(convert(sqlData.get("VEHICLE_ID")));
            theEventData.setAGVState(convert(sqlData.get("VEHICLE_STATE")));
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setLotState(convert(sqlData.get("LOT_STATE")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            theEventData.setOperationID(convert(sqlData.get("STEP_ID")));

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
     * @date 2019/7/23 10:28
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVNOTIFY_CDA  WHERE REFKEY=?";
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
     * @param systemMessageRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 14:03
     */
    public Response     insertSystemMessageHistory( Infos.Ohsysmhs systemMessageRecord ) {
        String     hFHSYSMHS_SUBSYSTEM_ID   ;
        String     hFHSYSMHS_SYS_MSG_CODE   ;
        String     hFHSYSMHS_SYS_MSG_TEXT   ;

        Integer hFHSYSMHS_NOTIFY_FLAG    ;
        String     hFHSYSMHS_EQP_ID         ;
        String     hFHSYSMHS_EQP_STATE      ;
        String     hFHSYSMHS_STK_ID         ;
        String     hFHSYSMHS_STK_STATE      ;
        String     hFHSYSMHS_AGV_ID         ;
        String     hFHSYSMHS_AGV_STATE      ;
        String     hFHSYSMHS_LOT_ID         ;
        String     hFHSYSMHS_LOT_STATE      ;
        String     hFHSYSMHS_MAINPD_ID      ;
        String     hFHSYSMHS_OPE_NO         ;
        String     hFHSYSMHS_PD_ID          ;
        String     hFHSYSMHS_PD_NAME        ;
        String     hFHSYSMHS_CLAM_TIME      ;
        Double   hFHSYSMHS_CLAM_SHOP_DATE ;
        String     hFHSYSMHS_CLAM_USER_ID   ;
        String     hFHSYSMHS_CLAIM_MEMO     ;
        String     hFHSYSMHS_EVENT_CREATE_TIME ;


        log.info("HistoryWatchDogServer::InsertSystemMessageHistory Function" );

        hFHSYSMHS_SUBSYSTEM_ID=   systemMessageRecord.getSub_system_id() ;
        hFHSYSMHS_SYS_MSG_CODE=    systemMessageRecord.getSys_msg_code()  ;
        hFHSYSMHS_SYS_MSG_TEXT=    systemMessageRecord.getSys_msg_text()  ;
        hFHSYSMHS_NOTIFY_FLAG    = systemMessageRecord.getNotify_flag();
        hFHSYSMHS_EQP_ID=          systemMessageRecord.getEqp_id()        ;
        hFHSYSMHS_EQP_STATE=       systemMessageRecord.getEqp_state()     ;
        hFHSYSMHS_STK_ID=          systemMessageRecord.getStk_id()        ;
        hFHSYSMHS_STK_STATE=       systemMessageRecord.getStk_state()     ;
        hFHSYSMHS_AGV_ID=          systemMessageRecord.getAgv_id()        ;
        hFHSYSMHS_AGV_STATE=       systemMessageRecord.getAgv_state()     ;
        hFHSYSMHS_LOT_ID=          systemMessageRecord.getLot_id()        ;
        hFHSYSMHS_LOT_STATE=       systemMessageRecord.getLot_state()     ;
        hFHSYSMHS_MAINPD_ID=       systemMessageRecord.getMainpd_id()     ;
        hFHSYSMHS_OPE_NO=          systemMessageRecord.getOpe_no()        ;
        hFHSYSMHS_PD_ID=           systemMessageRecord.getPd_id()         ;
        hFHSYSMHS_PD_NAME=         systemMessageRecord.getPd_name()       ;
        hFHSYSMHS_CLAM_TIME=       systemMessageRecord.getClam_time()     ;
        hFHSYSMHS_CLAM_SHOP_DATE = systemMessageRecord.getClam_shop_date();
        hFHSYSMHS_CLAM_USER_ID=    systemMessageRecord.getClam_user_id()  ;
        hFHSYSMHS_CLAIM_MEMO=      systemMessageRecord.getClaim_memo()    ;
        hFHSYSMHS_EVENT_CREATE_TIME= systemMessageRecord.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHNOTIFY\n"+
                        "(ID,  SERVICE_ID,\n"+
                        "NOTIFY_ID,\n"+
                        "NOTIFY_MSG,\n"+
                        "NOTIFY_FLAG,\n"+
                        "EQP_ID,\n"+
                        "EQP_STATE,\n"+
                        "STK_ID,\n"+
                        "STK_STATE,\n"+
                        "VEHICLE_ID,\n"+
                        "VEHICLE_STATE,\n"+
                        "LOT_ID,\n"+
                        "LOT_STATE,\n"+
                        "PROCESS_ID,\n"+
                        "OPE_NO,\n"+
                        "STEP_ID,\n"+
                        "PD_NAME,\n"+
                        "TRX_TIME,\n"+
                        "TRX_WORK_DATE,\n"+
                        "TRX_USER_ID,\n"+
                        "TRX_MEMO,\n"+
                        "EVENT_CREATE_TIME,\n"+
                        "STORE_TIME )\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP )",generateID(Infos.Ohsysmhs.class),
                hFHSYSMHS_SUBSYSTEM_ID,
                hFHSYSMHS_SYS_MSG_CODE,
                hFHSYSMHS_SYS_MSG_TEXT,
                hFHSYSMHS_NOTIFY_FLAG,
                hFHSYSMHS_EQP_ID,
                hFHSYSMHS_EQP_STATE,
                hFHSYSMHS_STK_ID,
                hFHSYSMHS_STK_STATE,
                hFHSYSMHS_AGV_ID,
                hFHSYSMHS_AGV_STATE,
                hFHSYSMHS_LOT_ID,
                hFHSYSMHS_LOT_STATE,
                hFHSYSMHS_MAINPD_ID,
                hFHSYSMHS_OPE_NO,
                hFHSYSMHS_PD_ID,
                hFHSYSMHS_PD_NAME,
                convert(hFHSYSMHS_CLAM_TIME),
                hFHSYSMHS_CLAM_SHOP_DATE,
                hFHSYSMHS_CLAM_USER_ID,
                hFHSYSMHS_CLAIM_MEMO,
                convert(hFHSYSMHS_EVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertSystemMessageHistory Function" );
        return( returnOK() );
    }

}
