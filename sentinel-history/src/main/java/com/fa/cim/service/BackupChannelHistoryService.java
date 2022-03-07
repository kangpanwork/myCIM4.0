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
//@Transactional(rollbackFor = Exception.class)
public class BackupChannelHistoryService {

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
                ,convert(hFHCSCHS_END_TIME)
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
    public Infos.BackupChannelEventRecord getEventData(String id) {
        String sql="Select * from OMEVBCKUPCH where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.BackupChannelEventRecord theEventData=new Infos.BackupChannelEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setRequest(convert(sqlData.get("REQUEST")));
            theEventData.setCategoryLevel(convert(sqlData.get("CATEGORY_LEVEL")));
            theEventData.setCategoryID(convert(sqlData.get("CATEGORY_ID")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            theEventData.setHostName(convert(sqlData.get("CHANNEL_NAME")));
            theEventData.setServerName(convert(sqlData.get("SERVER_NAME")));
            theEventData.setItDaemonPort(convert(sqlData.get("CHANNEL_PORT")));
            theEventData.setEntryRouteID(convert(sqlData.get("ENTRY_PROCESS_ID")));
            theEventData.setEntryOperationNumber(convert(sqlData.get("ENTRY_OPE_NO")));
            theEventData.setExitRouteID(convert(sqlData.get("EXIT_PROCESS_ID")));
            theEventData.setExitOperationNumber(convert(sqlData.get("EXIT_OPE_NO")));
            theEventData.setState(convert(sqlData.get("STATE")));
            theEventData.setStartTime(convert(sqlData.get("START_TIME")));
            theEventData.setEndTime(convert(sqlData.get("END_TIME")));

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
        String sql="SELECT * FROM OMEVBCKUPCH_CDA  WHERE REFKEY=?";
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
     * @param backupChannelRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:18
     */
    public Response insertBackupChannelHistory( Infos.Ohbucnhs backupChannelRecord ) {
        String hFHBUCNHS_CATEGORY_LEVEL ;
        String hFHBUCNHS_CATEGORY_ID    ;
        String hFHBUCNHS_ROUTE_ID       ;
        String hFHBUCNHS_OPE_NO         ;
        String hFHBUCNHS_HOST_NAME      ;
        String hFHBUCNHS_SERVER_NAME    ;
        String hFHBUCNHS_IT_DAEMON_PORT ;
        String hFHBUCNHS_ENTRY_ROUTE_ID ;
        String hFHBUCNHS_ENTRY_OPE_NO   ;
        String hFHBUCNHS_EXIT_ROUTE_ID  ;
        String hFHBUCNHS_EXIT_OPE_NO    ;
        String hFHBUCNHS_STATE          ;
        String hFHBUCNHS_START_TIME     ;
        String hFHBUCNHS_END_TIME       ;
        String hFHBUCNHS_OPE_CATEGORY   ;
        String hFHBUCNHS_CLAIM_TIME     ;
        double hFHBUCNHS_CLAIM_SHOP_DATE;
        String hFHBUCNHS_CLAIM_USER_ID  ;
        String hFHBUCNHS_EVENT_MEMO     ;
        String hFHBUCNHS_EVENT_CREATE_TIME ;

        log.info("HistoryWatchDogServer::InsertBackupChannelHistory Function" );

        hFHBUCNHS_CATEGORY_LEVEL=     backupChannelRecord.getCategory_level() ;
        hFHBUCNHS_CATEGORY_ID=        backupChannelRecord.getCategory_id()    ;
        hFHBUCNHS_ROUTE_ID=           backupChannelRecord.getRoute_id()       ;
        hFHBUCNHS_OPE_NO=             backupChannelRecord.getOpe_no()         ;
        hFHBUCNHS_HOST_NAME=          backupChannelRecord.getHost_name()      ;
        hFHBUCNHS_SERVER_NAME=        backupChannelRecord.getServer_name()    ;
        hFHBUCNHS_IT_DAEMON_PORT=     backupChannelRecord.getIt_daemon_port() ;
        hFHBUCNHS_ENTRY_ROUTE_ID=     backupChannelRecord.getEntry_route_id() ;
        hFHBUCNHS_ENTRY_OPE_NO=       backupChannelRecord.getEntry_ope_no()   ;
        hFHBUCNHS_EXIT_ROUTE_ID=      backupChannelRecord.getExit_route_id()  ;
        hFHBUCNHS_EXIT_OPE_NO=        backupChannelRecord.getExit_ope_no()    ;
        hFHBUCNHS_STATE=              backupChannelRecord.getState()          ;
        hFHBUCNHS_START_TIME=         backupChannelRecord.getStart_time()     ;
        hFHBUCNHS_END_TIME=           backupChannelRecord.getEnd_time()       ;
        hFHBUCNHS_OPE_CATEGORY=       backupChannelRecord.getOpe_category()   ;
        hFHBUCNHS_CLAIM_TIME=         backupChannelRecord.getClaim_time()     ;
        hFHBUCNHS_CLAIM_SHOP_DATE =   backupChannelRecord.getClaim_shop_date();
        hFHBUCNHS_CLAIM_USER_ID=      backupChannelRecord.getClaim_user_id()  ;
        hFHBUCNHS_EVENT_MEMO=         backupChannelRecord.getEvent_memo()     ;
        hFHBUCNHS_EVENT_CREATE_TIME=  backupChannelRecord.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHBCKUPCHCHG\n"+
                        "(ID,  CATEGORY_LEVEL,\n"+
                        "CATEGORY_ID,\n"+
                        "PROCESS_ID,\n"+
                        "OPE_NO,\n"+
                        "CHANNEL_NAME,\n"+
                        "SERVER_NAME,\n"+
                        "CHANNEL_PORT,\n"+
                        "ENTRY_PROCESS_ID,\n"+
                        "ENTRY_OPE_NO,\n"+
                        "EXIT_PROCESS_ID,\n"+
                        "EXIT_OPE_NO,\n"+
                        "STATE,\n"+
                        "START_TIME,\n"+
                        "END_TIME,\n"+
                        "OPE_CATEGORY,\n"+
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
                        "CURRENT_TIMESTAMP )",generateID(Infos.Ohbucnhs.class),
                hFHBUCNHS_CATEGORY_LEVEL,
                hFHBUCNHS_CATEGORY_ID,
                hFHBUCNHS_ROUTE_ID,
                hFHBUCNHS_OPE_NO,
                hFHBUCNHS_HOST_NAME,
                hFHBUCNHS_SERVER_NAME,
                hFHBUCNHS_IT_DAEMON_PORT,
                hFHBUCNHS_ENTRY_ROUTE_ID,
                hFHBUCNHS_ENTRY_OPE_NO,
                hFHBUCNHS_EXIT_ROUTE_ID,
                hFHBUCNHS_EXIT_OPE_NO,
                hFHBUCNHS_STATE,
                convert(hFHBUCNHS_START_TIME),
                convert(hFHBUCNHS_END_TIME),
                hFHBUCNHS_OPE_CATEGORY,
                convert(hFHBUCNHS_CLAIM_TIME),
                hFHBUCNHS_CLAIM_SHOP_DATE,
                hFHBUCNHS_CLAIM_USER_ID,
                hFHBUCNHS_EVENT_MEMO,
                convert(hFHBUCNHS_EVENT_CREATE_TIME));


        log.info("HistoryWatchDogServer::InsertBackupChannelHistory Function" );
        return( returnOK() );
    }

}
