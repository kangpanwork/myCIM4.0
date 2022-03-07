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
public class BackupOperationHistoryService {

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
    public Infos.BackupOperationEventRecord getEventData(String id) {
        String sql="Select * from OMEVBCKUP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.BackupOperationEventRecord theEventData=new Infos.BackupOperationEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);

        for (Map<String,Object> sqlData:sqlDatas) {
            lotData.setLotID(convert(sqlData.get("LOT_ID")));
            lotData.setLotType(convert(sqlData.get("LOT_TYPE")));
            lotData.setCassetteID(convert(sqlData.get("CARRIER_ID")));
            lotData.setLotStatus(convert(sqlData.get("LOT_STATUS")));
            lotData.setCustomerID(convert(sqlData.get("CUSTOMER_ID")));
            lotData.setPriorityClass(convertL(sqlData.get("LOT_PRIORITY")));
            lotData.setProductID(convert(sqlData.get("PROD_ID")));
            lotData.setOriginalWaferQuantity(convertI(sqlData.get("ORIGINAL_QTY")));
            lotData.setCurrentWaferQuantity(convertI(sqlData.get("CUR_QTY")));
            lotData.setProductWaferQuantity(convertI(sqlData.get("PROD_QTY")));
            lotData.setControlWaferQuantity(convertI(sqlData.get("NPW_QTY")));
            lotData.setHoldState(convert(sqlData.get("LOT_HOLD_STATE")));
            lotData.setBankID(convert(sqlData.get("BANK_ID")));
            lotData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            lotData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            lotData.setOperationID(convert(sqlData.get("STEP_ID")));
            lotData.setOperationPassCount(convertI(sqlData.get("PASS_COUNT")));
            lotData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            lotData.setWaferHistoryTimeStamp(convert(sqlData.get("WAFER_HIS_TIME")));
            lotData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));
            lotData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            lotData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));

            theEventData.setRequest(convert(sqlData.get("REQUEST")));
            theEventData.setHostName(convert(sqlData.get("CHANNEL_NAME")));
            theEventData.setServerName(convert(sqlData.get("SERVER_NAME")));
            theEventData.setItDaemonPort(convert(sqlData.get("CHANNEL_PORT")));
            theEventData.setEntryRouteID(convert(sqlData.get("ENTRY_PROCESS_ID")));
            theEventData.setEntryOperationNumber(convert(sqlData.get("ENTRY_OPE_NO")));
            theEventData.setExitRouteID(convert(sqlData.get("EXIT_PROCESS_ID")));
            theEventData.setExitOperationNumber(convert(sqlData.get("EXIT_OPE_NO")));

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
        String sql="SELECT * FROM OMEVBCKUP_CDA  WHERE REFKEY=?";
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
     * @param backupOperationRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:05
     */
    public Response insertBackupOperationHistory( Infos.Ohbuophs backupOperationRecord ) {
        String hFHBUOPHS_LOT_ID             ;
        String hFHBUOPHS_LOT_TYPE           ;
        String hFHBUOPHS_CAST_ID            ;
        String hFHBUOPHS_CAST_CATEGORY      ;
        String hFHBUOPHS_MAINPD_ID          ;
        String hFHBUOPHS_OPE_NO             ;
        String hFHBUOPHS_PD_ID              ;

        Integer hFHBUOPHS_OPE_PASS_COUNT;
        String hFHBUOPHS_PD_NAME            ;
        String hFHBUOPHS_CLAIM_TIME         ;
        double hFHBUOPHS_CLAIM_SHOP_DATE;
        String hFHBUOPHS_CLAIM_USER_ID      ;
        String hFHBUOPHS_OPE_CATEGORY       ;
        String hFHBUOPHS_HOST_NAME          ;
        String hFHBUOPHS_SERVER_NAME        ;
        String hFHBUOPHS_IT_DAEMON_PORT     ;
        String hFHBUOPHS_ENTRY_ROUTE_ID     ;
        String hFHBUOPHS_ENTRY_OPE_NO       ;
        String hFHBUOPHS_EXIT_ROUTE_ID      ;
        String hFHBUOPHS_EXIT_OPE_NO        ;
        String hFHBUOPHS_PRODSPEC_ID        ;
        String hFHBUOPHS_CUSTOMER_ID        ;
        String hFHBUOPHS_STAGE_ID           ;
        String hFHBUOPHS_STAGEGRP_ID        ;
        String hFHBUOPHS_HOLD_STATE         ;
        String hFHBUOPHS_BANK_ID            ;

        Integer hFHBUOPHS_ORG_WAFER_QTY;

        Integer hFHBUOPHS_CUR_WAFER_QTY;

        Integer hFHBUOPHS_PROD_WAFER_QTY;

        Integer hFHBUOPHS_CNTL_WAFER_QTY;
        String hFHBUOPHS_LOT_OWNER_ID       ;
        String hFHBUOPHS_PLAN_END_TIME      ;
        String hFHBUOPHS_WFRHS_TIME         ;
        String hFHBUOPHS_EVENT_MEMO         ;
        String hFHBUOPHS_EVENT_CREATE_TIME  ;

        hFHBUOPHS_LOT_ID=           backupOperationRecord.getLot_id()           ;
        hFHBUOPHS_LOT_TYPE=         backupOperationRecord.getLot_type()         ;
        hFHBUOPHS_CAST_ID=          backupOperationRecord.getCast_id()          ;
        hFHBUOPHS_CAST_CATEGORY=    backupOperationRecord.getCast_category()    ;
        hFHBUOPHS_MAINPD_ID=        backupOperationRecord.getMainpd_id()        ;
        hFHBUOPHS_OPE_NO=           backupOperationRecord.getOpe_no()           ;
        hFHBUOPHS_PD_ID=            backupOperationRecord.getPd_id()            ;
        hFHBUOPHS_OPE_PASS_COUNT  = backupOperationRecord.getOpe_pass_count();
        hFHBUOPHS_PD_NAME=          backupOperationRecord.getPd_name()          ;
        hFHBUOPHS_CLAIM_TIME=       backupOperationRecord.getClaim_time()       ;
        hFHBUOPHS_CLAIM_SHOP_DATE = backupOperationRecord.getClaim_shop_date();
        hFHBUOPHS_CLAIM_USER_ID=    backupOperationRecord.getClaim_user_id()    ;
        hFHBUOPHS_OPE_CATEGORY=     backupOperationRecord.getOpe_category()     ;
        hFHBUOPHS_HOST_NAME=        backupOperationRecord.getHost_name()        ;
        hFHBUOPHS_SERVER_NAME=      backupOperationRecord.getServer_name()      ;
        hFHBUOPHS_IT_DAEMON_PORT=   backupOperationRecord.getIt_daemon_port()   ;
        hFHBUOPHS_ENTRY_ROUTE_ID=   backupOperationRecord.getEntry_route_id()   ;
        hFHBUOPHS_ENTRY_OPE_NO=     backupOperationRecord.getEntry_ope_no()     ;
        hFHBUOPHS_EXIT_ROUTE_ID=    backupOperationRecord.getExit_route_id()    ;
        hFHBUOPHS_EXIT_OPE_NO=      backupOperationRecord.getExit_ope_no()      ;
        hFHBUOPHS_PRODSPEC_ID=      backupOperationRecord.getProdspec_id()      ;
        hFHBUOPHS_CUSTOMER_ID=      backupOperationRecord.getCustomer_id()      ;
        hFHBUOPHS_STAGE_ID=         backupOperationRecord.getStage_id()         ;
        hFHBUOPHS_STAGEGRP_ID=      backupOperationRecord.getStagegrp_id()      ;
        hFHBUOPHS_HOLD_STATE=       backupOperationRecord.getHold_state()       ;
        hFHBUOPHS_BANK_ID=          backupOperationRecord.getBank_id()          ;
        hFHBUOPHS_ORG_WAFER_QTY   = backupOperationRecord.getOrg_wafer_qty();
        hFHBUOPHS_CUR_WAFER_QTY   = backupOperationRecord.getCur_wafer_qty();
        hFHBUOPHS_PROD_WAFER_QTY  = backupOperationRecord.getProd_wafer_qty();
        hFHBUOPHS_CNTL_WAFER_QTY  = backupOperationRecord.getCntl_wafer_qty();
        hFHBUOPHS_LOT_OWNER_ID=     backupOperationRecord.getLot_owner_id()     ;
        hFHBUOPHS_PLAN_END_TIME=    backupOperationRecord.getPlan_end_time()    ;
        hFHBUOPHS_WFRHS_TIME=       backupOperationRecord.getWfrhs_time()       ;
        hFHBUOPHS_EVENT_MEMO=       backupOperationRecord.getEvent_memo()       ;
        hFHBUOPHS_EVENT_CREATE_TIME= backupOperationRecord.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHBCKUP\n"+
                        "(ID,  LOT_ID,\n"+
                        "LOT_TYPE,\n"+
                        "CARRIER_ID,\n"+
                        "CARRIER_CATEGORY,\n"+
                        "PROCESS_ID,\n"+
                        "OPE_NO,\n"+
                        "STEP_ID,\n"+
                        "OPE_PASS_COUNT,\n"+
                        "STEP_NAME,\n"+
                        "TRX_TIME,\n"+
                        "TRX_WORK_DATE,\n"+
                        "TRX_USER_ID,\n"+
                        "OPE_CATEGORY,\n"+
                        "CHANNEL_NAME,\n"+
                        "SERVER_NAME,\n"+
                        "IT_DAEMON_PORT,\n"+
                        "ENTRY_PROCESS_ID,\n"+
                        "ENTRY_OPE_NO,\n"+
                        "EXIT_PROCESS_ID,\n"+
                        "EXIT_OPE_NO,\n"+
                        "PRODSPEC_ID,\n"+
                        "CUSTOMER_ID,\n"+
                        "STAGE_ID,\n"+
                        "STAGE_GRP_ID,\n"+
                        "HOLD_STATE,\n"+
                        "BANK_ID,\n"+
                        "ORG_WAFER_QTY,\n"+
                        "CUR_WAFER_QTY,\n"+
                        "PROD_WAFER_QTY,\n"+
                        "CNTL_WAFER_QTY,\n"+
                        "LOT_OWNER_ID,\n"+
                        "PLAN_END_TIME,\n"+
                        "WFRHS_TIME,\n"+
                        "EVENT_MEMO,\n"+
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
                        "CURRENT_TIMESTAMP )",generateID(Infos.Ohbuophs.class),
                hFHBUOPHS_LOT_ID,
                hFHBUOPHS_LOT_TYPE,
                hFHBUOPHS_CAST_ID,
                hFHBUOPHS_CAST_CATEGORY,
                hFHBUOPHS_MAINPD_ID,
                hFHBUOPHS_OPE_NO,
                hFHBUOPHS_PD_ID,
                hFHBUOPHS_OPE_PASS_COUNT,
                hFHBUOPHS_PD_NAME,
                convert(hFHBUOPHS_CLAIM_TIME),
                hFHBUOPHS_CLAIM_SHOP_DATE,
                hFHBUOPHS_CLAIM_USER_ID,
                hFHBUOPHS_OPE_CATEGORY,
                hFHBUOPHS_HOST_NAME,
                hFHBUOPHS_SERVER_NAME,
                hFHBUOPHS_IT_DAEMON_PORT,
                hFHBUOPHS_ENTRY_ROUTE_ID,
                hFHBUOPHS_ENTRY_OPE_NO,
                hFHBUOPHS_EXIT_ROUTE_ID,
                hFHBUOPHS_EXIT_OPE_NO,
                hFHBUOPHS_PRODSPEC_ID,
                hFHBUOPHS_CUSTOMER_ID,
                hFHBUOPHS_STAGE_ID,
                hFHBUOPHS_STAGEGRP_ID,
                hFHBUOPHS_HOLD_STATE,
                hFHBUOPHS_BANK_ID,
                hFHBUOPHS_ORG_WAFER_QTY,
                hFHBUOPHS_CUR_WAFER_QTY,
                hFHBUOPHS_PROD_WAFER_QTY,
                hFHBUOPHS_CNTL_WAFER_QTY,
                hFHBUOPHS_LOT_OWNER_ID,
                convert(hFHBUOPHS_PLAN_END_TIME),
                convert(hFHBUOPHS_WFRHS_TIME),
                hFHBUOPHS_EVENT_MEMO,
                convert(hFHBUOPHS_EVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertBackupOperationHistory Function" );

        log.info("HistoryWatchDogServer::InsertBackupOperationHistory Function" );
        return( returnOK() );
    }

}
