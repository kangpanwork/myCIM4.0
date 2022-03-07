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
public class FutureSplitHistoryService {

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


        baseCore.insert("INSERT INTO OMFSMHS\n" +
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
    public com.fa.cim.fsm.Infos.FutureSplitEventRecord getEventData(String id) {
        String sql="Select * from OMEVFSM where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        com.fa.cim.fsm.Infos.FutureSplitEventRecord theEventData=new com.fa.cim.fsm.Infos.FutureSplitEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<com.fa.cim.fsm.Infos.FutureSplitRouteEventData> subRoutes=new ArrayList<>();
        theEventData.setRoutes(subRoutes);

        for (Map<String,Object> sqlData:sqlDatas) {
            //add psmJobID for history
            theEventData.setFsmJobID(convert(sqlData.get("FUSPLITJOB_ID")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setLotFamilyID(convert(sqlData.get("LOTFAMILY_ID")));
            theEventData.setSplitRouteID(convert(sqlData.get("SPLIT_MAIN_PROCESS_ID")));
            theEventData.setSplitOperationNumber(convert(sqlData.get("SPLIT_OPE_NO")));
            theEventData.setOriginalRouteID(convert(sqlData.get("ORIG_MAIN_PROCESS_ID")));
            theEventData.setOriginalOperationNumber(convert(sqlData.get("ORIG_OPE_NO")));
            theEventData.setActionEMail(convertB(sqlData.get("MAIL_ACTION")));
            theEventData.setActionHold(convertB(sqlData.get("HOLD_ACTION")));
            // task-3988 the database name has been changed
            sql="SELECT * FROM OMEVFSM_RT WHERE REFKEY=?";
            List<Map> sqlSubRoutes = baseCore.queryAllForMap(sql, id);

            for (Map sqlSubRoute:sqlSubRoutes){
                com.fa.cim.fsm.Infos.FutureSplitRouteEventData subRoute=new com.fa.cim.fsm.Infos.FutureSplitRouteEventData();
                subRoutes.add(subRoute);

                subRoute.setRouteID(convert(sqlSubRoute.get("PROCESS_ID")));
                subRoute.setReturnOperationNumber(convert(sqlSubRoute.get("RETURN_OPE_NO")));
                subRoute.setMergeOperationNumber(convert(sqlSubRoute.get("MERGE_OPE_NO")));
                subRoute.setParentLotID(convert(sqlSubRoute.get("PARENT_LOT_ID")));
                subRoute.setChildLotID(convert(sqlSubRoute.get("CHILD_LOT_ID")));
                subRoute.setMemo(convert(sqlSubRoute.get("MEMO")));
                List<com.fa.cim.fsm.Infos.FutureSplitedWaferEventData> wafers=new ArrayList<>();
                subRoute.setWafers(wafers);

                // task-3988 the database name has been changed
                sql="SELECT * FROM OMEVFSM_RT_WF WHERE REFKEY=? AND LINK_MARKER=?";
                List<Map> sqlWafers = baseCore.queryAllForMap(sql, id, sqlSubRoute.get("IDX_NO"));

                for (Map sqlWafer:sqlWafers){
                    com.fa.cim.fsm.Infos.FutureSplitedWaferEventData wafer=new com.fa.cim.fsm.Infos.FutureSplitedWaferEventData();
                    wafers.add(wafer);
                    // task-3988 add WAFER_GROUP
                    wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                    wafer.setGroupNo(convert(sqlWafer.get("WAFER_GROUP")));
                    wafer.setSuccessFlag(convert(sqlWafer.get("SUCCESS_FLAG")));
                }

            }

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
        // task-3988 the database name has been changed
        String sql="SELECT * FROM OMEVFSM_CDA  WHERE REFKEY=?";
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
     * @param futureSplitEventRecord_FUTUREHS
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:22
     */
    public Response  insertFutureSplitEventRecord_FHFUTUREHS( com.fa.cim.fsm.Infos.Ohfuturehs   futureSplitEventRecord_FUTUREHS ) {
        String    hFUTUREHS_ACTION_CODE      ;
        String    hFUTUREHS_LOTFAMILY_ID     ;
        String    hFUTUREHS_SPLIT_MAINPD_ID  ;
        String    hFUTUREHS_SPLIT_OPE_NO     ;
        String    hFUTUREHS_ORIG_MAINPD_ID   ;
        String    hFUTUREHS_ORIG_OPE_NO      ;

        Integer hFUTUREHS_MAIL_ACTION;

        Integer hFUTUREHS_HOLD_ACTION;

        Integer hFUTUREHS_SEQ_NO;
        String    hFUTUREHS_ROUTE_ID     ;
        String    hFUTUREHS_RETURN_OPE_NO    ;
        String    hFUTUREHS_MERGE_OPE_NO     ;
        String    hFUTUREHS_PARENTLOT_ID     ;
        String    hFUTUREHS_CHILDLOT_ID      ;
        String    hFUTUREHS_MEMO             ;
        String    hFUTUREHS_CLAIM_TIME       ;
        Double  hFUTUREHS_CLAIM_SHOP_DATE;
        String    hFUTUREHS_CLAIM_USER_ID    ;
        String    hFUTUREHS_CLAIM_MEMO       ;
        String    hFUTUREHS_STORE_TIME       ;
        String    hFUTUREHS_EVENT_CREATE_TIME;

        //add psmJobID for history
        String hFUTUREHS_FUSPLITJOB_ID;

        log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTUREHS Function" );

        hFUTUREHS_ACTION_CODE=       futureSplitEventRecord_FUTUREHS.getAction_code() ;
        hFUTUREHS_LOTFAMILY_ID=      futureSplitEventRecord_FUTUREHS.getLotfamily_id() ;
        hFUTUREHS_SPLIT_MAINPD_ID=   futureSplitEventRecord_FUTUREHS.getSplit_route_id() ;
        hFUTUREHS_SPLIT_OPE_NO=      futureSplitEventRecord_FUTUREHS.getSplit_ope_no() ;
        hFUTUREHS_ORIG_MAINPD_ID=    futureSplitEventRecord_FUTUREHS.getOriginal_route_id() ;
        hFUTUREHS_ORIG_OPE_NO=       futureSplitEventRecord_FUTUREHS.getOriginal_ope_no() ;
        hFUTUREHS_MAIL_ACTION      = futureSplitEventRecord_FUTUREHS.getActionemail();
        hFUTUREHS_HOLD_ACTION      = futureSplitEventRecord_FUTUREHS.getActionhold();
        hFUTUREHS_SEQ_NO           = futureSplitEventRecord_FUTUREHS.getSeq_no();
        hFUTUREHS_ROUTE_ID=      futureSplitEventRecord_FUTUREHS.getRoute_id() ;
        hFUTUREHS_RETURN_OPE_NO=     futureSplitEventRecord_FUTUREHS.getReturn_ope_no() ;
        hFUTUREHS_MERGE_OPE_NO=      futureSplitEventRecord_FUTUREHS.getMerge_ope_no() ;
        hFUTUREHS_PARENTLOT_ID=      futureSplitEventRecord_FUTUREHS.getParent_lot_id() ;
        hFUTUREHS_CHILDLOT_ID=       futureSplitEventRecord_FUTUREHS.getChild_lot_id() ;
        hFUTUREHS_MEMO=              futureSplitEventRecord_FUTUREHS.getMemo() ;
        hFUTUREHS_CLAIM_TIME=        futureSplitEventRecord_FUTUREHS.getClaim_time() ;
        hFUTUREHS_CLAIM_SHOP_DATE  = futureSplitEventRecord_FUTUREHS.getClaim_shop_date();
        hFUTUREHS_CLAIM_USER_ID=     futureSplitEventRecord_FUTUREHS.getClaim_user_id() ;
        hFUTUREHS_CLAIM_MEMO=        futureSplitEventRecord_FUTUREHS.getClaim_memo() ;
        hFUTUREHS_EVENT_CREATE_TIME= futureSplitEventRecord_FUTUREHS.getEvent_create_time() ;

        //add psmJobID for history
        hFUTUREHS_FUSPLITJOB_ID = futureSplitEventRecord_FUTUREHS.getFusplitjob_id();
        // task-3988 delete runCard information

        // task-3988 the database name has been changed
        baseCore.insert("INSERT INTO OMFSMHS (ID, ACTION,                  LOTFAMILY_ID,               SPLIT_PROCESS_ID,\n"+
                        "SPLIT_OPE_NO,            ORIG_PROCESS_ID,             ORIG_OPE_NO,\n"+
                        "MAIL_ACTION,             HOLD_ACTION,                SEQ_NO,\n"+
                        "PROCESS_ID,            RETURN_OPE_NO,              MERGE_OPE_NO,\n"+
                        "PARENTLOT_ID,            CHILDLOT_ID,                MEMO,\n"+
                        "TRX_TIME,              TRX_SHOP_DATE,            TRX_USER_ID,\n"+
                        "TRX_MEMO,              STORE_TIME,                 EVENT_CREATE_TIME, FUSPLITJOB_ID)\n"+
                        "VALUES   (?, ?,  ?,    ?,\n"+
                        "?, ?,  ?,\n"+
                        "?,  ?,     ?,\n"+
                        "?, ?,   ?,\n"+
                        "?, ?,     ?,\n"+
                        "?,   ?, ?,\n"+
                        "?,   CURRENT_TIMESTAMP,          ?, ? )",generateID(com.fa.cim.fsm.Infos.Ohfuturehs.class),
                hFUTUREHS_ACTION_CODE,
                hFUTUREHS_LOTFAMILY_ID,
                hFUTUREHS_SPLIT_MAINPD_ID,
                hFUTUREHS_SPLIT_OPE_NO,
                hFUTUREHS_ORIG_MAINPD_ID,
                hFUTUREHS_ORIG_OPE_NO,
                hFUTUREHS_MAIL_ACTION,
                hFUTUREHS_HOLD_ACTION,
                hFUTUREHS_SEQ_NO,
                hFUTUREHS_ROUTE_ID,
                hFUTUREHS_RETURN_OPE_NO,
                hFUTUREHS_MERGE_OPE_NO,
                hFUTUREHS_PARENTLOT_ID,
                hFUTUREHS_CHILDLOT_ID,
                hFUTUREHS_MEMO,
                convert(hFUTUREHS_CLAIM_TIME),
                hFUTUREHS_CLAIM_SHOP_DATE,
                hFUTUREHS_CLAIM_USER_ID,
                hFUTUREHS_CLAIM_MEMO,
                convert(hFUTUREHS_EVENT_CREATE_TIME),
                hFUTUREHS_FUSPLITJOB_ID);
                // task-3988 delete runCard information

        log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTUREHS Function" );
        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param futureSplitEventRecord_FHFUTURE_WAFER
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:25
     */
    public Response  insertFutureSplitEventRecord_FHFUTUREHS_WAFER( com.fa.cim.fsm.Infos.OhfuturehsWafer  futureSplitEventRecord_FHFUTURE_WAFER ) {
        String  hFUTUREHS_WAFER_ACTION_CODE    ;
        String  hFUTUREHS_WAFER_LOTFAMILY_ID   ;
        String  hFUTUREHS_WAFER_SPLIT_MAINPD_ID;
        String  hFUTUREHS_WAFER_SPLIT_OPE_NO   ;
        String  hFUTUREHS_WAFER_ORIG_MAINPD_ID ;
        String  hFUTUREHS_WAFER_ORIG_OPE_NO    ;

        Integer hFUTUREHS_WAFER_SEQ_NO;
        String  hFUTUREHS_WAFER_SUB_ROUTE_ID   ;
        String  hFUTUREHS_WAFER_PARENTLOT_ID   ;
        String  hFUTUREHS_WAFER_CHILDLOT_ID    ;
        String  hFUTUREHS_WAFER_WAFER_ID       ;
        String  hFUTUREHS_WAFER_WAFER_GROUP;
        String  hFUTUREHS_WAFER_SUCCESS_FLAG   ;
        String  hFUTUREHS_WAFER_CLAIM_TIME     ;
        //add psmJobID for history
        String  hFUTUREHS_WAFER_FUSPLITJOB_ID     ;
        // task-3988 delete runCard information

        log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTURE_WAFER Function" );

        hFUTUREHS_WAFER_ACTION_CODE=     futureSplitEventRecord_FHFUTURE_WAFER.getAction_code() ;
        hFUTUREHS_WAFER_LOTFAMILY_ID=    futureSplitEventRecord_FHFUTURE_WAFER.getLotfamily_id() ;
        hFUTUREHS_WAFER_SPLIT_MAINPD_ID= futureSplitEventRecord_FHFUTURE_WAFER.getSplit_route_id() ;
        hFUTUREHS_WAFER_SPLIT_OPE_NO=    futureSplitEventRecord_FHFUTURE_WAFER.getSplit_ope_no() ;
        hFUTUREHS_WAFER_ORIG_MAINPD_ID=  futureSplitEventRecord_FHFUTURE_WAFER.getOriginal_route_id() ;
        hFUTUREHS_WAFER_ORIG_OPE_NO=     futureSplitEventRecord_FHFUTURE_WAFER.getOriginal_ope_no() ;
        hFUTUREHS_WAFER_SEQ_NO         = futureSplitEventRecord_FHFUTURE_WAFER.getSeq_no();
        hFUTUREHS_WAFER_SUB_ROUTE_ID=    futureSplitEventRecord_FHFUTURE_WAFER.getRoute_id() ;
        hFUTUREHS_WAFER_PARENTLOT_ID=    futureSplitEventRecord_FHFUTURE_WAFER.getParent_lot_id() ;
        hFUTUREHS_WAFER_CHILDLOT_ID=     futureSplitEventRecord_FHFUTURE_WAFER.getChild_lot_id() ;
        hFUTUREHS_WAFER_WAFER_ID=        futureSplitEventRecord_FHFUTURE_WAFER.getWafer_id() ;
        // task-3988 add groupNo
        hFUTUREHS_WAFER_WAFER_GROUP= futureSplitEventRecord_FHFUTURE_WAFER.getGroup_no();
        hFUTUREHS_WAFER_SUCCESS_FLAG=    futureSplitEventRecord_FHFUTURE_WAFER.getSuccess_flag() ;
        hFUTUREHS_WAFER_CLAIM_TIME=      futureSplitEventRecord_FHFUTURE_WAFER.getClaim_time() ;
        hFUTUREHS_WAFER_FUSPLITJOB_ID=   futureSplitEventRecord_FHFUTURE_WAFER.getFusplitjob_id();
        // task-3988 delete runCard information

        // task-3988 the database name has been changed
        baseCore.insert("INSERT INTO OMFSMHS_WAFER (ID, ACTION,                        LOTFAMILY_ID,                    SPLIT_PROCESS_ID,\n"+
                        "SPLIT_OPE_NO,                  ORIG_PROCESS_ID,                  ORIG_OPE_NO,\n"+
                        "SEQ_NO,                        ROUTE_ID,                    PARENTLOT_ID,\n"+
                        "CHILDLOT_ID,                   WAFER_ID,    WAFER_GROUP,                    SUCCESS_FLAG,\n"+
                        "TRX_TIME , FUSPLITJOB_ID)\n"+
                        "VALUES         (?, ?,  ?,   ?,?,\n"+
                        "?, ?, ?,\n"+
                        "?,       ?,   ?,\n"+
                        "?,  ?,       ?,\n"+
                        "?, ? )",generateID(com.fa.cim.fsm.Infos.OhfuturehsWafer.class),
                hFUTUREHS_WAFER_ACTION_CODE,
                hFUTUREHS_WAFER_LOTFAMILY_ID,
                hFUTUREHS_WAFER_SPLIT_MAINPD_ID,
                hFUTUREHS_WAFER_SPLIT_OPE_NO,
                hFUTUREHS_WAFER_ORIG_MAINPD_ID,
                hFUTUREHS_WAFER_ORIG_OPE_NO,
                hFUTUREHS_WAFER_SEQ_NO,
                hFUTUREHS_WAFER_SUB_ROUTE_ID,
                hFUTUREHS_WAFER_PARENTLOT_ID,
                hFUTUREHS_WAFER_CHILDLOT_ID,
                hFUTUREHS_WAFER_WAFER_ID,
                hFUTUREHS_WAFER_WAFER_GROUP,
                hFUTUREHS_WAFER_SUCCESS_FLAG,
                convert(hFUTUREHS_WAFER_CLAIM_TIME),
                hFUTUREHS_WAFER_FUSPLITJOB_ID);


        log.info("HistoryWatchDogServer::InsertFutureSplitEventRecord_FUTURE_WAFER Function" );
        return returnOK();
    }

}
