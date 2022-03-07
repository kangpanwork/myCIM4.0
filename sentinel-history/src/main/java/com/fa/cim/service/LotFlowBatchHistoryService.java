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
public class LotFlowBatchHistoryService {

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
    public Infos.LotFlowBatchEventRecord getEventData(String id) {
        String sql="Select * from OMEVFBOP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotFlowBatchEventRecord theEventData=new Infos.LotFlowBatchEventRecord();
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

            theEventData.setFlowBatchID(convert(sqlData.get("FLOWB_ID")));
            theEventData.setEventType(convert(sqlData.get("EVENT_TYPE")));
            theEventData.setTargetOperationNumber(convert(sqlData.get("TARGET_OPE_NO")));
            theEventData.setTargetEquipmentID(convert(sqlData.get("TARGET_EQP_ID")));
            theEventData.setFromFlowBatchID(convert(sqlData.get("SRC_FLOWB_ID")));

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
        String sql="SELECT * FROM OMEVFBOP_CDA  WHERE REFKEY=?";
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
     * @param lotFlowBatchRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:15
     */
    public Response insertLotFlowBatchHistory( Infos.Ohflbchhs lotFlowBatchRecord ) {
        String    hFHFLBCHHS_LOT_ID          ;
        String    hFHFLBCHHS_LOT_TYPE        ;
        String    hFHFLBCHHS_CAST_ID         ;
        String    hFHFLBCHHS_LOT_STATUS      ;
        String    hFHFLBCHHS_CUSTOMER_ID     ;

        Integer hFHFLBCHHS_PRIORITY_CLASS  ;
        String    hFHFLBCHHS_PRODSPEC_ID     ;

        Integer hFHFLBCHHS_ORIGINAL_QTY ;

        Integer hFHFLBCHHS_CUR_QTY ;

        Integer hFHFLBCHHS_PROD_QTY ;

        Integer hFHFLBCHHS_CNTL_QTY ;
        String    hFHFLBCHHS_LOT_HOLD_STATE  ;
        String    hFHFLBCHHS_BANK_ID         ;
        String    hFHFLBCHHS_MAINPD_ID       ;
        String    hFHFLBCHHS_OPE_NO          ;
        String    hFHFLBCHHS_PD_ID           ;

        Integer hFHFLBCHHS_PASS_COUNT;
        String    hFHFLBCHHS_WAFER_HIS_TIME  ;
        String    hFHFLBCHHS_EVENT_TYPE      ;
        String    hFHFLBCHHS_TARGET_OPE_NO   ;
        String    hFHFLBCHHS_TARGET_EQP_ID   ;
        String    hFHFLBCHHS_FLOWBATCH_ID    ;
        String    hFHFLBCHHS_FR_FLOWBATCH_ID ;
        String    hFHFLBCHHS_CLAIM_TIME      ;
        Double  hFHFLBCHHS_CLAIM_SHOP_DATE ;
        String    hFHFLBCHHS_CLAIM_USER_ID   ;
        String    hFHFLBCHHS_CLAIM_MEMO      ;
        String    hFHFLBCHHS_EVENT_CREATE_TIME ;

        log.info("HistoryWatchDogServer::InsertLotFlowBatchHistory Function" );

        hFHFLBCHHS_LOT_ID=            lotFlowBatchRecord.getLot_id()          ;
        hFHFLBCHHS_LOT_TYPE=          lotFlowBatchRecord.getLot_type()        ;
        hFHFLBCHHS_CAST_ID=           lotFlowBatchRecord.getCast_id()         ;
        hFHFLBCHHS_LOT_STATUS=        lotFlowBatchRecord.getLot_status()      ;
        hFHFLBCHHS_CUSTOMER_ID=       lotFlowBatchRecord.getCustomer_id()     ;
        hFHFLBCHHS_PRIORITY_CLASS  =  lotFlowBatchRecord.getPriority_class()   ;
        hFHFLBCHHS_PRODSPEC_ID=       lotFlowBatchRecord.getProdspec_id()     ;
        hFHFLBCHHS_ORIGINAL_QTY    =  lotFlowBatchRecord.getOriginal_qty()     ;
        hFHFLBCHHS_CUR_QTY         =  lotFlowBatchRecord.getCur_qty()          ;
        hFHFLBCHHS_PROD_QTY        =  lotFlowBatchRecord.getProd_qty()         ;
        hFHFLBCHHS_CNTL_QTY        =  lotFlowBatchRecord.getCntl_qty()         ;
        hFHFLBCHHS_LOT_HOLD_STATE=    lotFlowBatchRecord.getLot_hold_state()  ;
        hFHFLBCHHS_BANK_ID=           lotFlowBatchRecord.getBank_id()         ;
        hFHFLBCHHS_MAINPD_ID=         lotFlowBatchRecord.getMainpd_id()       ;
        hFHFLBCHHS_OPE_NO=            lotFlowBatchRecord.getOpe_no()          ;
        hFHFLBCHHS_PD_ID=             lotFlowBatchRecord.getPd_id()           ;
        hFHFLBCHHS_PASS_COUNT       = lotFlowBatchRecord.getPass_count()       ;
        hFHFLBCHHS_WAFER_HIS_TIME=    lotFlowBatchRecord.getWafer_his_time()  ;
        hFHFLBCHHS_EVENT_TYPE=        lotFlowBatchRecord.getEvent_type()      ;
        hFHFLBCHHS_TARGET_OPE_NO=     lotFlowBatchRecord.getTarget_ope_no()   ;
        hFHFLBCHHS_TARGET_EQP_ID=     lotFlowBatchRecord.getTarget_eqp_id()   ;
        hFHFLBCHHS_FLOWBATCH_ID=      lotFlowBatchRecord.getFlowbatch_id()    ;
        hFHFLBCHHS_FR_FLOWBATCH_ID=   lotFlowBatchRecord.getFr_flowbatch_id() ;
        hFHFLBCHHS_CLAIM_TIME=        lotFlowBatchRecord.getClaim_time()      ;
        hFHFLBCHHS_CLAIM_SHOP_DATE  = lotFlowBatchRecord.getClaim_shop_date()  ;
        hFHFLBCHHS_CLAIM_USER_ID=     lotFlowBatchRecord.getClaim_user_id()   ;
        hFHFLBCHHS_CLAIM_MEMO=        lotFlowBatchRecord.getClaim_memo()      ;
        hFHFLBCHHS_EVENT_CREATE_TIME= lotFlowBatchRecord.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHFLOWB\n"+
                        "(ID,  LOT_ID,\n"+
                        "LOT_TYPE,\n"+
                        "CARRIER_ID,\n"+
                        "LOT_STATUS,\n"+
                        "CUSTOMER_ID,\n"+
                        "LOT_PRIORITY,\n"+
                        "PROD_ID,\n"+
                        "ORIGINAL_QTY,\n"+
                        "CUR_QTY,\n"+
                        "PROD_QTY,\n"+
                        "NPW_QTY,\n"+
                        "LOT_HOLD_STATE,\n"+
                        "BANK_ID,\n"+
                        "PROCESS_ID,\n"+
                        "OPE_NO,\n"+
                        "STEP_ID,\n"+
                        "PASS_COUNT,\n"+
                        "WAFER_HIS_TIME,\n"+
                        "EVENT_TYPE,\n"+
                        "TARGET_OPE_NO,\n"+
                        "TARGET_EQP_ID,\n"+
                        "FLOWB_ID,\n"+
                        "SRC_FLOWB_ID,\n"+
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
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP )",generateID(Infos.Ohflbchhs.class),
                hFHFLBCHHS_LOT_ID,
                hFHFLBCHHS_LOT_TYPE,
                hFHFLBCHHS_CAST_ID,
                hFHFLBCHHS_LOT_STATUS,
                hFHFLBCHHS_CUSTOMER_ID,
                hFHFLBCHHS_PRIORITY_CLASS,
                hFHFLBCHHS_PRODSPEC_ID,
                hFHFLBCHHS_ORIGINAL_QTY,
                hFHFLBCHHS_CUR_QTY,
                hFHFLBCHHS_PROD_QTY,
                hFHFLBCHHS_CNTL_QTY,
                hFHFLBCHHS_LOT_HOLD_STATE,
                hFHFLBCHHS_BANK_ID,
                hFHFLBCHHS_MAINPD_ID,
                hFHFLBCHHS_OPE_NO,
                hFHFLBCHHS_PD_ID,
                hFHFLBCHHS_PASS_COUNT,
                convert(hFHFLBCHHS_WAFER_HIS_TIME),
                hFHFLBCHHS_EVENT_TYPE,
                hFHFLBCHHS_TARGET_OPE_NO,
                hFHFLBCHHS_TARGET_EQP_ID,
                hFHFLBCHHS_FLOWBATCH_ID,
                hFHFLBCHHS_FR_FLOWBATCH_ID,
                convert(hFHFLBCHHS_CLAIM_TIME),
                hFHFLBCHHS_CLAIM_SHOP_DATE,
                hFHFLBCHHS_CLAIM_USER_ID,
                hFHFLBCHHS_CLAIM_MEMO,
                convert(hFHFLBCHHS_EVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertLotFlowBatchHistory Function" );
        return( returnOK() );
    }

}
