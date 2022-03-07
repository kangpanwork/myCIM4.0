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
public class LotWaferStackHistoryService {

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
    public Infos.LotWaferStackEventRecord getEventData(String id) {
        String sql="Select * from OMEVBOND where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotWaferStackEventRecord theEventData=new Infos.LotWaferStackEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);
        List<Infos.StackWaferEventData> wafers=new ArrayList<>();
        theEventData.setWafers(wafers);

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

            theEventData.setBondingCategory(convert(sqlData.get("REASON_PROCESS_ID")));
            theEventData.setRelatedLotID(convert(sqlData.get("RELATED_LOT_ID")));
            theEventData.setEquipmentID(convert(sqlData.get("REASON_STEP_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));
            theEventData.setBondingGroupID(convert(sqlData.get("BONDING_GRP_ID")));

            sql="SELECT * FROM OMEVBOND_WFR WHERE REFKEY=?";
            List<Map> sqlWafers = baseCore.queryAllForMap(sql, id);

            for (Map sqlWafer:sqlWafers){
                Infos.StackWaferEventData wafer=new Infos.StackWaferEventData();
                wafers.add(wafer);

                wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                wafer.setOriginalWaferID(convert(sqlWafer.get("ORIG_WAFER_ID")));
                wafer.setAliasWaferName(convert(sqlWafer.get("WAFER_ALIAS")));
                wafer.setOriginalAliasWaferName(convert(sqlWafer.get("ORIG_WAFER_ALIAS")));
                wafer.setControlWaferFlag(convertB(sqlWafer.get("NPW_WAFER")));
                wafer.setRelatedWaferID(convert(sqlWafer.get("RELATED_WAFER")));
                wafer.setOriginalCassetteID(convert(sqlWafer.get("ORIG_CARRIER_ID")));
                wafer.setOriginalSlotNumber(convertL(sqlWafer.get("ORIG_SLOT_NO")));
                wafer.setDestinationCassetteID(convert(sqlWafer.get("DEST_CARRIER_ID")));
                wafer.setDestinationSlotNumber(convertL(sqlWafer.get("DEST_SLOT_NO")));
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
        String sql="SELECT * FROM OMEVBOND_CDA WHERE REFKEY=?";
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

//    /**
//     * description:
//     * <p></p>
//     * change history:
//     * date             defect             person             comments
//     * ---------------------------------------------------------------------------------------------------------------------
//     *
//     * @param fhsortjobhs_Record
//     * @return com.fa.cim.dto.Response
//     * @exception
//     * @author Ho
//     * @date 2019/7/25 13:41
//     */
//    public Response  insertWaferSortJobHistory_FHSORTJOBHS( Infos.Ohsortjobhs    fhsortjobhs_Record  ) {
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS Function" );
//
//        String hFHSORTJOBHSEQP_ID               ="";
//        String hFHSORTJOBHSPORT_GROUP_ID        ="";
//        String hFHSORTJOBHSSORTER_JOB_ID        ="";
//        String hFHSORTJOBHSSORTER_JOB_CATEGORY  ="";
//        String hFHSORTJOBHSCOMPONENT_JOB_ID     ="";
//        String hFHSORTJOBHSACTION               ="";
//        String hFHSORTJOBHSSORTER_JOB_STATUS    ="";
//        String hFHSORTJOBHSCOMPONENT_JOB_STATUS ="";
//        String hFHSORTJOBHSCLAIM_TIME           ="";
//        String hFHSORTJOBHSCLAIM_USER_ID        ="";
//        String hFHSORTJOBHSCLAIM_MEMO           ="";
//        String hFHSORTJOBHSSTORE_TIME           ="";
//        String hFHSORTJOBHSEVENT_CREATE_TIME    ="";
//        Integer hFHSORTJOBHSWAFER_ID_READ_FLAG = 0;
//
//        hFHSORTJOBHSEQP_ID                 = fhsortjobhs_Record.getEquipmentID()        ;
//        hFHSORTJOBHSPORT_GROUP_ID          = fhsortjobhs_Record.getPortGroupID()        ;
//        hFHSORTJOBHSSORTER_JOB_ID          = fhsortjobhs_Record.getSorterJobID()        ;
//        hFHSORTJOBHSSORTER_JOB_CATEGORY    = fhsortjobhs_Record.getSorterJobCategory()  ;
//        hFHSORTJOBHSCOMPONENT_JOB_ID       = fhsortjobhs_Record.getComponentJobID()     ;
//        hFHSORTJOBHSACTION                 = fhsortjobhs_Record.getAction()             ;
//        hFHSORTJOBHSSORTER_JOB_STATUS      = fhsortjobhs_Record.getSorterJobStatus()    ;
//        hFHSORTJOBHSCOMPONENT_JOB_STATUS   = fhsortjobhs_Record.getComponentJobStatus() ;
//        hFHSORTJOBHSCLAIM_TIME             = fhsortjobhs_Record.getClaimTime()          ;
//        hFHSORTJOBHSCLAIM_USER_ID          = fhsortjobhs_Record.getClaimUser()          ;
//        hFHSORTJOBHSCLAIM_MEMO             = fhsortjobhs_Record.getClaimMemo()          ;
//        hFHSORTJOBHSSTORE_TIME             = fhsortjobhs_Record.getStoreTime()          ;
//        hFHSORTJOBHSEVENT_CREATE_TIME      = fhsortjobhs_Record.getEventCreateTime()    ;
//        hFHSORTJOBHSWAFER_ID_READ_FLAG  = fhsortjobhs_Record.getWaferIDReadFlag();
//
//        baseCore.insert("INSERT INTO OHSORTJOB (ID,\n"+
//                        "EQP_ID              ,PORT_GROUP_ID       ,SORTER_JOB_ID       ,SORTER_JOB_CATEGORY, COMPONENT_JOB_ID    ,\n"+
//                        "TASK_TYPE              ,SORTER_JOB_STATUS   ,COMPONENT_JOB_STATUS,WAFER_ID_READ_FLAG  ,\n"+
//                        "TRX_TIME          ,TRX_USER_ID       ,TRX_MEMO          ,STORE_TIME          ,EVENT_CREATE_TIME   )\n"+
//                        "Values (?,\n"+
//                        "?               ,?        ,?        ,?     ,\n"+
//                        "?               ,?    ,? ,?   ,\n"+
//                        "?           ,?        ,?           ,CURRENT_TIMESTAMP           ,\n"+
//                        "?    )",generateID(Infos.Ohsortjobhs.class),
//                hFHSORTJOBHSEQP_ID,
//                hFHSORTJOBHSPORT_GROUP_ID,
//                hFHSORTJOBHSSORTER_JOB_ID,
//                hFHSORTJOBHSSORTER_JOB_CATEGORY,
//                hFHSORTJOBHSCOMPONENT_JOB_ID,
//                hFHSORTJOBHSACTION,
//                hFHSORTJOBHSSORTER_JOB_STATUS,
//                hFHSORTJOBHSCOMPONENT_JOB_STATUS,
//                hFHSORTJOBHSWAFER_ID_READ_FLAG,
//                convert(hFHSORTJOBHSCLAIM_TIME),
//                hFHSORTJOBHSCLAIM_USER_ID,
//                hFHSORTJOBHSCLAIM_MEMO,
//                convert(hFHSORTJOBHSEVENT_CREATE_TIME));
//
//
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS Function" );
//        return( returnOK() );
//    }
//
//    /**
//     * description:
//     * <p></p>
//     * change history:
//     * date             defect             person             comments
//     * ---------------------------------------------------------------------------------------------------------------------
//     *
//     * @param fhsortjobhs_component_Record
//     * @return com.fa.cim.dto.Response
//     * @exception
//     * @author Ho
//     * @date 2019/7/25 13:46
//     */
//    public Response insertWaferSortJobHistory_FHSORTJOBHS_COMPONENT( Infos.OhsortjobhsComponent  fhsortjobhs_component_Record ) {
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS_COMPONENT Function" );
//
//        String hFHSORTJOBHS_COMPONENTSORTER_JOB_ID     ="";
//        String hFHSORTJOBHS_COMPONENTCOMPONENT_JOB_ID  ="";
//        String hFHSORTJOBHS_COMPONENTSRC_CAST_ID       ="";
//        String hFHSORTJOBHS_COMPONENTDEST_CAST_ID      ="";
//        String hFHSORTJOBHS_COMPONENTSRC_PORT_ID       ="";
//        String hFHSORTJOBHS_COMPONENTDEST_PORT_ID      ="";
//        String hFHSORTJOBHS_COMPONENTCLAIM_TIME        ="";
//        Integer hFHSORTJOBHS_COMPONENTJOB_SEQ = 0;
//
//        hFHSORTJOBHS_COMPONENTSORTER_JOB_ID    = fhsortjobhs_component_Record.getSorterJobID()          ;
//        hFHSORTJOBHS_COMPONENTCOMPONENT_JOB_ID = fhsortjobhs_component_Record.getComponentJobID()       ;
//        hFHSORTJOBHS_COMPONENTSRC_CAST_ID      = fhsortjobhs_component_Record.getSourceCassetteID()     ;
//        hFHSORTJOBHS_COMPONENTDEST_CAST_ID     = fhsortjobhs_component_Record.getDestinationCassetteID();
//        hFHSORTJOBHS_COMPONENTSRC_PORT_ID      = fhsortjobhs_component_Record.getSourcePortID()         ;
//        hFHSORTJOBHS_COMPONENTDEST_PORT_ID     = fhsortjobhs_component_Record.getDestinationPortID()    ;
//        hFHSORTJOBHS_COMPONENTCLAIM_TIME       = fhsortjobhs_component_Record.getClaimTime()            ;
//        hFHSORTJOBHS_COMPONENTJOB_SEQ  = fhsortjobhs_component_Record.getJobSeq();
//
//        baseCore.insert("INSERT INTO OHSORTJOB_COMP (ID,\n"+
//                        "SORTER_JOB_ID     ,COMPONENT_JOB_ID  ,JOB_SEQ           ,SRC_CARRIER_ID       ,\n"+
//                        "DEST_CARRIER_ID      ,SRC_PORT_ID       ,DEST_PORT_ID      ,TRX_TIME        )\n"+
//                        "Values  (?,\n"+
//                        "?   ,?,?         ,\n"+
//                        "?     ,?    ,?     ,\n"+
//                        "?    ,?      )",generateID(Infos.OhsortjobhsComponent.class),
//                hFHSORTJOBHS_COMPONENTSORTER_JOB_ID,
//                hFHSORTJOBHS_COMPONENTCOMPONENT_JOB_ID,
//                hFHSORTJOBHS_COMPONENTJOB_SEQ,
//                hFHSORTJOBHS_COMPONENTSRC_CAST_ID,
//                hFHSORTJOBHS_COMPONENTDEST_CAST_ID,
//                hFHSORTJOBHS_COMPONENTSRC_PORT_ID,
//                hFHSORTJOBHS_COMPONENTDEST_PORT_ID,
//                convert(hFHSORTJOBHS_COMPONENTCLAIM_TIME));
//
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS_COMPONENT Function" );
//        return( returnOK() );
//    }
//
//    /**
//     * description:
//     * <p></p>
//     * change history:
//     * date             defect             person             comments
//     * ---------------------------------------------------------------------------------------------------------------------
//     *
//     * @param fhsortjobhs_slotmap_Record
//     * @return com.fa.cim.dto.Response
//     * @exception
//     * @author Ho
//     * @date 2019/7/25 14:12
//     */
//    public Response insertWaferSortJobHistory_FHSORTJOBHS_SLOTMAP( Infos.OhsortjobhsSlotmap    fhsortjobhs_slotmap_Record ) {
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS_SLOTMAP Function" );
//
//        String hFHSORTJOBHS_SLOTMAPSORTER_JOB_ID    = "";
//        String hFHSORTJOBHS_SLOTMAPCOMPONENT_JOB_ID = "";
//        String hFHSORTJOBHS_SLOTMAPLOT_ID           = "";
//        String hFHSORTJOBHS_SLOTMAPWAFER_ID         = "";
//        String hFHSORTJOBHS_SLOTMAPDEST_CAST_ID     = "";
//        String hFHSORTJOBHS_SLOTMAPDEST_PORT_ID     = "";
//        String hFHSORTJOBHS_SLOTMAPSRC_CAST_ID      = "";
//        String hFHSORTJOBHS_SLOTMAPSRC_PORT_ID      = "";
//        String hFHSORTJOBHS_SLOTMAPCLAIM_TIME       = "";
//        Integer hFHSORTJOBHS_SLOTMAPDEST_M_B_SIVIEW = 0;
//        Integer hFHSORTJOBHS_SLOTMAPDEST_POSITION    = 0;
//        Integer hFHSORTJOBHS_SLOTMAPSRC_M_B_SIVIEW   = 0;
//        Integer hFHSORTJOBHS_SLOTMAPSRC_POSITION     = 0;
//
//        hFHSORTJOBHS_SLOTMAPSORTER_JOB_ID    = fhsortjobhs_slotmap_Record.getSorterJobID()          ;
//        hFHSORTJOBHS_SLOTMAPCOMPONENT_JOB_ID = fhsortjobhs_slotmap_Record.getComponentJobID()       ;
//        hFHSORTJOBHS_SLOTMAPLOT_ID           = fhsortjobhs_slotmap_Record.getLotID()                ;
//        hFHSORTJOBHS_SLOTMAPWAFER_ID         = fhsortjobhs_slotmap_Record.getWaferID()              ;
//        hFHSORTJOBHS_SLOTMAPDEST_CAST_ID     = fhsortjobhs_slotmap_Record.getDestinationCassetteID();
//        hFHSORTJOBHS_SLOTMAPDEST_PORT_ID     = fhsortjobhs_slotmap_Record.getDestinationPortID()    ;
//        hFHSORTJOBHS_SLOTMAPSRC_CAST_ID      = fhsortjobhs_slotmap_Record.getSourceCassetteID()     ;
//        hFHSORTJOBHS_SLOTMAPSRC_PORT_ID      = fhsortjobhs_slotmap_Record.getSourcePortID()         ;
//        hFHSORTJOBHS_SLOTMAPCLAIM_TIME       = fhsortjobhs_slotmap_Record.getClaimTime()            ;
//        hFHSORTJOBHS_SLOTMAPDEST_M_B_SIVIEW  = fhsortjobhs_slotmap_Record.getDestinationManagedBySiview();
//        hFHSORTJOBHS_SLOTMAPDEST_POSITION    = fhsortjobhs_slotmap_Record.getDestinationSlotPosition();
//        hFHSORTJOBHS_SLOTMAPSRC_M_B_SIVIEW   = fhsortjobhs_slotmap_Record.getSourceManagedBySiview();
//        hFHSORTJOBHS_SLOTMAPSRC_POSITION     = fhsortjobhs_slotmap_Record.getSourceSlotPosition();
//
//        baseCore.insert("INSERT INTO OHSORTJOB_COMP_SLOTMAP (ID,\n"+
//                        "SORTER_JOB_ID        ,COMPONENT_JOB_ID     ,LOT_ID               ,WAFER_ID             ,\n"+
//                        "DEST_CARRIER_ID         ,DEST_PORT_ID         ,DEST_CARRIER_IS_REGIST      ,DEST_POSITION        ,\n"+
//                        "SRC_CARRIER_ID          ,SRC_PORT_ID          ,SRC_CARRIER_IS_REGIST       ,SRC_POSITION         ,TRX_TIME           )\n"+
//                        "Values (?,\n"+
//                        "?    ,    ? ,    ?           ,\n"+
//                        "?         ,    ?     ,    ?     ,\n"+
//                        "?  ,    ?    ,    ?      ,\n"+
//                        "?      ,    ?   ,    ?     ,    ?       )",generateID(Infos.OhsortjobhsSlotmap.class),
//                hFHSORTJOBHS_SLOTMAPSORTER_JOB_ID,
//                hFHSORTJOBHS_SLOTMAPCOMPONENT_JOB_ID,
//                hFHSORTJOBHS_SLOTMAPLOT_ID,
//                hFHSORTJOBHS_SLOTMAPWAFER_ID,
//                hFHSORTJOBHS_SLOTMAPDEST_CAST_ID,
//                hFHSORTJOBHS_SLOTMAPDEST_PORT_ID,
//                hFHSORTJOBHS_SLOTMAPDEST_M_B_SIVIEW,
//                hFHSORTJOBHS_SLOTMAPDEST_POSITION,
//                hFHSORTJOBHS_SLOTMAPSRC_CAST_ID,
//                hFHSORTJOBHS_SLOTMAPSRC_PORT_ID,
//                hFHSORTJOBHS_SLOTMAPSRC_M_B_SIVIEW,
//                hFHSORTJOBHS_SLOTMAPSRC_POSITION,
//                convert(hFHSORTJOBHS_SLOTMAPCLAIM_TIME));
//
//        log.info("HistoryWatchDogServer::InsertWaferSortJobHistory_FHSORTJOBHS_SLOTMAP Function" );
//        return( returnOK() );
//    }

}
