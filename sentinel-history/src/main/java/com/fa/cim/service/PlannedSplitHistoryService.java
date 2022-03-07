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
public class PlannedSplitHistoryService {

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
    public Infos.PlannedSplitEventRecord getEventData(String id) {
        String sql="Select * from OMEVPSM where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.PlannedSplitEventRecord theEventData=new Infos.PlannedSplitEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.SplitSubRouteEventData> subRoutes=new ArrayList<>();
        theEventData.setSubRoutes(subRoutes);

        for (Map<String,Object> sqlData:sqlDatas) {
            //add psmJobID for history
            theEventData.setPsmJobID(convert(sqlData.get("PLSPLITJOB_ID")));
            //add runCardID for histroy
            theEventData.setRunCardID(convert(sqlData.get("RUNCARD_ID")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setLotFamilyID(convert(sqlData.get("LOTFAMILY_ID")));
            theEventData.setSplitRouteID(convert(sqlData.get("SPLIT_PROCESS_ID")));
            theEventData.setSplitOperationNumber(convert(sqlData.get("SPLIT_OPE_NO")));
            theEventData.setOriginalRouteID(convert(sqlData.get("ORIG_PROCESS_ID")));
            theEventData.setOriginalOperationNumber(convert(sqlData.get("ORIG_OPE_NO")));
            theEventData.setActionEMail(convertB(sqlData.get("MAIL_ACTION_REQD")));
            theEventData.setActionHold(convertB(sqlData.get("HOLD_ACTION_REQD")));

            sql="SELECT * FROM OMEVPSM_SUBPROC WHERE REFKEY=?";
            List<Map> sqlSubRoutes = baseCore.queryAllForMap(sql, id);

            for (Map sqlSubRoute:sqlSubRoutes){
                Infos.SplitSubRouteEventData subRoute=new Infos.SplitSubRouteEventData();
                subRoutes.add(subRoute);

                subRoute.setSubRouteID(convert(sqlSubRoute.get("SUB_PROCESS_ID")));
                subRoute.setReturnOperationNumber(convert(sqlSubRoute.get("RETURN_OPE_NO")));
                subRoute.setMergeOperationNumber(convert(sqlSubRoute.get("MERGE_OPE_NO")));
                subRoute.setParentLotID(convert(sqlSubRoute.get("PARENT_LOT_ID")));
                subRoute.setChildLotID(convert(sqlSubRoute.get("CHILD_LOT_ID")));
                subRoute.setMemo(convert(sqlSubRoute.get("PSM_MEMO")));
                List<Infos.SplitedWaferEventData> wafers=new ArrayList<>();
                subRoute.setWafers(wafers);

                sql="SELECT * FROM OMEVPSM_SUBPROC_WFR WHERE REFKEY=? AND LINK_MARKER=?";
                List<Map> sqlWafers = baseCore.queryAllForMap(sql, id, sqlSubRoute.get("IDX_NO"));

                for (Map sqlWafer:sqlWafers){
                    Infos.SplitedWaferEventData wafer=new Infos.SplitedWaferEventData();
                    wafers.add(wafer);

                    wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                    wafer.setSuccessFlag(convert(sqlWafer.get("SPLIT_SUCCESS_FLAG")));
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
        String sql="SELECT * FROM OMEVPSM_CDA  WHERE REFKEY=?";
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
     * @param plannedSplitEventRecord_FHPLSPHS
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:22
     */
    public Response  insertPlannedSplitEventRecord_FHPLSPHS( Infos.Ohplsphs   plannedSplitEventRecord_FHPLSPHS ) {
        String    hFHPLSPHS_ACTION_CODE      ;
        String    hFHPLSPHS_LOTFAMILY_ID     ;
        String    hFHPLSPHS_SPLIT_MAINPD_ID  ;
        String    hFHPLSPHS_SPLIT_OPE_NO     ;
        String    hFHPLSPHS_ORIG_MAINPD_ID   ;
        String    hFHPLSPHS_ORIG_OPE_NO      ;

        Integer hFHPLSPHS_MAIL_ACTION;

        Integer hFHPLSPHS_HOLD_ACTION;

        Integer hFHPLSPHS_SEQ_NO;
        String    hFHPLSPHS_SUB_ROUTE_ID     ;
        String    hFHPLSPHS_RETURN_OPE_NO    ;
        String    hFHPLSPHS_MERGE_OPE_NO     ;
        String    hFHPLSPHS_PARENTLOT_ID     ;
        String    hFHPLSPHS_CHILDLOT_ID      ;
        String    hFHPLSPHS_MEMO             ;
        String    hFHPLSPHS_CLAIM_TIME       ;
        Double  hFHPLSPHS_CLAIM_SHOP_DATE;
        String    hFHPLSPHS_CLAIM_USER_ID    ;
        String    hFHPLSPHS_CLAIM_MEMO       ;
        String    hFHPLSPHS_STORE_TIME       ;
        String    hFHPLSPHS_EVENT_CREATE_TIME;

        //add psmJobID for history
        String hFHPLSPHS_PLSPLITJOB_ID;

        //add runCardID for history
        String hFHPLSPHS_RUNCARD_ID;

        log.info("HistoryWatchDogServer::InsertPlannedSplitEventRecord_FHPLSPHS Function" );

        hFHPLSPHS_ACTION_CODE=       plannedSplitEventRecord_FHPLSPHS.getAction_code() ;
        hFHPLSPHS_LOTFAMILY_ID=      plannedSplitEventRecord_FHPLSPHS.getLotfamily_id() ;
        hFHPLSPHS_SPLIT_MAINPD_ID=   plannedSplitEventRecord_FHPLSPHS.getSplit_route_id() ;
        hFHPLSPHS_SPLIT_OPE_NO=      plannedSplitEventRecord_FHPLSPHS.getSplit_ope_no() ;
        hFHPLSPHS_ORIG_MAINPD_ID=    plannedSplitEventRecord_FHPLSPHS.getOriginal_route_id() ;
        hFHPLSPHS_ORIG_OPE_NO=       plannedSplitEventRecord_FHPLSPHS.getOriginal_ope_no() ;
        hFHPLSPHS_MAIL_ACTION      = plannedSplitEventRecord_FHPLSPHS.getActionemail();
        hFHPLSPHS_HOLD_ACTION      = plannedSplitEventRecord_FHPLSPHS.getActionhold();
        hFHPLSPHS_SEQ_NO           = plannedSplitEventRecord_FHPLSPHS.getSeq_no();
        hFHPLSPHS_SUB_ROUTE_ID=      plannedSplitEventRecord_FHPLSPHS.getSub_route_id() ;
        hFHPLSPHS_RETURN_OPE_NO=     plannedSplitEventRecord_FHPLSPHS.getReturn_ope_no() ;
        hFHPLSPHS_MERGE_OPE_NO=      plannedSplitEventRecord_FHPLSPHS.getMerge_ope_no() ;
        hFHPLSPHS_PARENTLOT_ID=      plannedSplitEventRecord_FHPLSPHS.getParent_lot_id() ;
        hFHPLSPHS_CHILDLOT_ID=       plannedSplitEventRecord_FHPLSPHS.getChild_lot_id() ;
        hFHPLSPHS_MEMO=              plannedSplitEventRecord_FHPLSPHS.getMemo() ;
        hFHPLSPHS_CLAIM_TIME=        plannedSplitEventRecord_FHPLSPHS.getClaim_time() ;
        hFHPLSPHS_CLAIM_SHOP_DATE  = plannedSplitEventRecord_FHPLSPHS.getClaim_shop_date();
        hFHPLSPHS_CLAIM_USER_ID=     plannedSplitEventRecord_FHPLSPHS.getClaim_user_id() ;
        hFHPLSPHS_CLAIM_MEMO=        plannedSplitEventRecord_FHPLSPHS.getClaim_memo() ;
        hFHPLSPHS_EVENT_CREATE_TIME= plannedSplitEventRecord_FHPLSPHS.getEvent_create_time() ;

        //add psmJobID for history
        hFHPLSPHS_PLSPLITJOB_ID = plannedSplitEventRecord_FHPLSPHS.getPlsplitjob_id();

        //add runCardID for history
        hFHPLSPHS_RUNCARD_ID = plannedSplitEventRecord_FHPLSPHS.getRuncard_id();

        baseCore.insert("INSERT INTO OHPSM (ID, TASK_TYPE,                  LOTFAMILY_ID,               SPLIT_PROCESS_ID,\n"+
                        "SPLIT_OPE_NO,            ORIG_PROCESS_ID,             ORIG_OPE_NO,\n"+
                        "MAIL_ACTION,             HOLD_ACTION,                IDX_NO,\n"+
                        "SUB_PROCESS_ID,            RETURN_OPE_NO,              MERGE_OPE_NO,\n"+
                        "PARENT_LOT_ID,            CHILD_LOT_ID,                TEST_MEMO,\n"+
                        "TRX_TIME,              TRX_WORK_DATE,            TRX_USER_ID,\n"+
                        "TRX_MEMO,              STORE_TIME,                 EVENT_CREATE_TIME, PLSPLITJOB_ID, RUNCARD_ID )\n"+
                        "VALUES   (?, ?,  ?,    ?,\n"+
                        "?, ?,  ?,\n"+
                        "?,  ?,     ?,\n"+
                        "?, ?,   ?,\n"+
                        "?, ?,     ?,\n"+
                        "?,   ?, ?,\n"+
                        "?,   CURRENT_TIMESTAMP,          ?, ?, ? )",generateID(Infos.Ohplsphs.class),
                hFHPLSPHS_ACTION_CODE,
                hFHPLSPHS_LOTFAMILY_ID,
                hFHPLSPHS_SPLIT_MAINPD_ID,
                hFHPLSPHS_SPLIT_OPE_NO,
                hFHPLSPHS_ORIG_MAINPD_ID,
                hFHPLSPHS_ORIG_OPE_NO,
                hFHPLSPHS_MAIL_ACTION,
                hFHPLSPHS_HOLD_ACTION,
                hFHPLSPHS_SEQ_NO,
                hFHPLSPHS_SUB_ROUTE_ID,
                hFHPLSPHS_RETURN_OPE_NO,
                hFHPLSPHS_MERGE_OPE_NO,
                hFHPLSPHS_PARENTLOT_ID,
                hFHPLSPHS_CHILDLOT_ID,
                hFHPLSPHS_MEMO,
                convert(hFHPLSPHS_CLAIM_TIME),
                hFHPLSPHS_CLAIM_SHOP_DATE,
                hFHPLSPHS_CLAIM_USER_ID,
                hFHPLSPHS_CLAIM_MEMO,
                convert(hFHPLSPHS_EVENT_CREATE_TIME),
                hFHPLSPHS_PLSPLITJOB_ID,
                hFHPLSPHS_RUNCARD_ID);

        log.info("HistoryWatchDogServer::InsertPlannedSplitEventRecord_FHPLSPHS Function" );
        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param plannedSplitEventRecord_FHPLSPHS_WAFER
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 13:25
     */
    public Response  insertPlannedSplitEventRecord_FHPLSPHS_WAFER( Infos.OhplsphsWafer  plannedSplitEventRecord_FHPLSPHS_WAFER ) {
        String  hFHPLSPHS_WAFER_ACTION_CODE    ;
        String  hFHPLSPHS_WAFER_LOTFAMILY_ID   ;
        String  hFHPLSPHS_WAFER_SPLIT_MAINPD_ID;
        String  hFHPLSPHS_WAFER_SPLIT_OPE_NO   ;
        String  hFHPLSPHS_WAFER_ORIG_MAINPD_ID ;
        String  hFHPLSPHS_WAFER_ORIG_OPE_NO    ;

        Integer hFHPLSPHS_WAFER_SEQ_NO;
        String  hFHPLSPHS_WAFER_SUB_ROUTE_ID   ;
        String  hFHPLSPHS_WAFER_PARENTLOT_ID   ;
        String  hFHPLSPHS_WAFER_CHILDLOT_ID    ;
        String  hFHPLSPHS_WAFER_WAFER_ID       ;
        String  hFHPLSPHS_WAFER_SUCCESS_FLAG   ;
        String  hFHPLSPHS_WAFER_CLAIM_TIME     ;
        //add psmJobID for history
        String  hFHPLSPHS_WAFER_PLSPLITJOB_ID     ;

        //add runCardID for history
        String  hFHPLSPHS_WAFER_RUNCARD_ID     ;

        log.info("HistoryWatchDogServer::InsertPlannedSplitEventRecord_FHPLSPHS_WAFER Function" );

        hFHPLSPHS_WAFER_ACTION_CODE=     plannedSplitEventRecord_FHPLSPHS_WAFER.getAction_code() ;
        hFHPLSPHS_WAFER_LOTFAMILY_ID=    plannedSplitEventRecord_FHPLSPHS_WAFER.getLotfamily_id() ;
        hFHPLSPHS_WAFER_SPLIT_MAINPD_ID= plannedSplitEventRecord_FHPLSPHS_WAFER.getSplit_route_id() ;
        hFHPLSPHS_WAFER_SPLIT_OPE_NO=    plannedSplitEventRecord_FHPLSPHS_WAFER.getSplit_ope_no() ;
        hFHPLSPHS_WAFER_ORIG_MAINPD_ID=  plannedSplitEventRecord_FHPLSPHS_WAFER.getOriginal_route_id() ;
        hFHPLSPHS_WAFER_ORIG_OPE_NO=     plannedSplitEventRecord_FHPLSPHS_WAFER.getOriginal_ope_no() ;
        hFHPLSPHS_WAFER_SEQ_NO         = plannedSplitEventRecord_FHPLSPHS_WAFER.getSeq_no();
        hFHPLSPHS_WAFER_SUB_ROUTE_ID=    plannedSplitEventRecord_FHPLSPHS_WAFER.getSub_route_id() ;
        hFHPLSPHS_WAFER_PARENTLOT_ID=    plannedSplitEventRecord_FHPLSPHS_WAFER.getParent_lot_id() ;
        hFHPLSPHS_WAFER_CHILDLOT_ID=     plannedSplitEventRecord_FHPLSPHS_WAFER.getChild_lot_id() ;
        hFHPLSPHS_WAFER_WAFER_ID=        plannedSplitEventRecord_FHPLSPHS_WAFER.getWafer_id() ;
        hFHPLSPHS_WAFER_SUCCESS_FLAG=    plannedSplitEventRecord_FHPLSPHS_WAFER.getSuccess_flag() ;
        hFHPLSPHS_WAFER_CLAIM_TIME=      plannedSplitEventRecord_FHPLSPHS_WAFER.getClaim_time() ;
        hFHPLSPHS_WAFER_PLSPLITJOB_ID=   plannedSplitEventRecord_FHPLSPHS_WAFER.getPlsplitjob_id();
        hFHPLSPHS_WAFER_RUNCARD_ID=      plannedSplitEventRecord_FHPLSPHS_WAFER.getRuncard_id();

        baseCore.insert("INSERT INTO OHPSM_WAFER (ID, ACTION,                        LOTFAMILY_ID,                    SPLIT_PROCESS_ID,\n"+
                        "SPLIT_OPE_NO,                  ORIG_PROCESS_ID,                  ORIG_OPE_NO,\n"+
                        "IDX_NO,                        SUB_PROCESS_ID,                    PARENT_LOT_ID,\n"+
                        "CHILD_LOT_ID,                   WAFER_ID,                        PSM_EXEC_RESULT,\n"+
                        "TRX_TIME , PLSPLITJOB_ID, RUNCARD_ID )\n"+
                        "VALUES         (?, ?,  ?,   ?,\n"+
                        "?, ?, ?,\n"+
                        "?,       ?,   ?,\n"+
                        "?,  ?,       ?,\n"+
                        "?, ?, ? )",generateID(Infos.OhplsphsWafer.class),
                hFHPLSPHS_WAFER_ACTION_CODE,
                hFHPLSPHS_WAFER_LOTFAMILY_ID,
                hFHPLSPHS_WAFER_SPLIT_MAINPD_ID,
                hFHPLSPHS_WAFER_SPLIT_OPE_NO,
                hFHPLSPHS_WAFER_ORIG_MAINPD_ID,
                hFHPLSPHS_WAFER_ORIG_OPE_NO,
                hFHPLSPHS_WAFER_SEQ_NO,
                hFHPLSPHS_WAFER_SUB_ROUTE_ID,
                hFHPLSPHS_WAFER_PARENTLOT_ID,
                hFHPLSPHS_WAFER_CHILDLOT_ID,
                hFHPLSPHS_WAFER_WAFER_ID,
                hFHPLSPHS_WAFER_SUCCESS_FLAG,
                convert(hFHPLSPHS_WAFER_CLAIM_TIME),
                hFHPLSPHS_WAFER_PLSPLITJOB_ID,
                hFHPLSPHS_WAFER_RUNCARD_ID);


        log.info("HistoryWatchDogServer::InsertPlannedSplitEventRecord_FHPLSPHS_WAFER Function" );
        return returnOK();
    }

}
