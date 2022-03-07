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
public class WaferSortJobHistoryService {

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
    public Infos.WaferSortJobEventRecord getEventData(String id) {
        String sql="Select * from OMEVSORTJOB where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.WaferSortJobEventRecord theEventData=new Infos.WaferSortJobEventRecord();

        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.SortJobComponentEventData> componentJobs=new ArrayList<>();
        theEventData.setComponentJobs(componentJobs);
        List<Infos.SortJobSlotMapEventData> slotMaps=new ArrayList<>();
        theEventData.setSlotMaps(slotMaps);
        Infos.WaferSortJobPostActRecord postActRecord = new Infos.WaferSortJobPostActRecord();
        theEventData.setPostActRecord(postActRecord);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setOperation(convert(sqlData.get("OPERATION")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setPortGroupID(convert(sqlData.get("PORT_GROUP_ID")));
            theEventData.setSorterJobID(convert(sqlData.get("SORTER_JOB_ID")));
            theEventData.setSorterJobStatus(convert(sqlData.get("SORTER_JOB_STATUS")));
            theEventData.setWaferIDReadFlag(convertB(sqlData.get("WAFER_ID_READ_FLAG")));
            theEventData.setComponentJobCount(convertI(sqlData.get("COMPONENT_JOB_COUNT")));
            theEventData.setCtrlJobID(convert(sqlData.get("CTRLJOB_ID")));

            sql="SELECT * FROM OMEVSORTJOB_COMP WHERE REFKEY=?";
            List<Map> sqlComponentJobs = baseCore.queryAllForMap(sql, id);

            for (Map sqlComponentJob:sqlComponentJobs){
                Infos.SortJobComponentEventData componentJob=new Infos.SortJobComponentEventData();
                componentJobs.add(componentJob);

                componentJob.setComponentJobID(convert(sqlComponentJob.get("COMPONENT_JOB_ID")));
                componentJob.setDestinationCarrierID(convert(sqlComponentJob.get("DEST_CARRIER_ID")));
                componentJob.setDestinationPortID(convert(sqlComponentJob.get("DEST_PORT_ID")));
                componentJob.setSourceCarrierID(convert(sqlComponentJob.get("SRC_CARRIER_ID")));
                componentJob.setSourcePortID(convert(sqlComponentJob.get("SRC_PORT_ID")));
                componentJob.setComponentJobStatus(convert(sqlComponentJob.get("COMPONENT_JOB_STATUS")));
                componentJob.setActionCode(convert(sqlComponentJob.get("ACTION_CODE")));
                componentJob.setOperation(convert(sqlComponentJob.get("OPERATION")));
            }

            sql="SELECT * FROM OMEVSORTJOB_COMP_SLOTMAP WHERE REFKEY=?";
            List<Map> sqlSlotMaps = baseCore.queryAllForMap(sql, id);

            for (Map sqlSlotMap:sqlSlotMaps){
                Infos.SortJobSlotMapEventData slotMap=new Infos.SortJobSlotMapEventData();
                slotMaps.add(slotMap);

                slotMap.setComponentJobID(convert(sqlSlotMap.get("COMPONENT_JOB_ID")));
                slotMap.setDestinationPosition(convertI(sqlSlotMap.get("DEST_POSITION")));
                slotMap.setLotID(convert(sqlSlotMap.get("LOT_ID")));
                slotMap.setSourcePosition(convertI(sqlSlotMap.get("SRC_POSITION")));
                slotMap.setWaferID(convert(sqlSlotMap.get("WAFER_ID")));
                slotMap.setAliasName(convert(sqlSlotMap.get("ALIAS_NAME")));
                slotMap.setDirection(convert(sqlSlotMap.get("DIRECTION")));
                slotMap.setReplyTimestamp(convert(sqlSlotMap.get("REPLY_TIMESTAMP")));
                slotMap.setSortStatus(convertI(sqlSlotMap.get("SORTER_STATUS")));
            }

            sql = "SELECT * FROM OMEVSORTJOB_POSTACT WHERE REFKEY = ?";
            List<Map> postActMaps = baseCore.queryAllForMap(sql, id);
            for (Map postActMap : postActMaps) {
                postActRecord.setSorterJobID(convert(postActMap.get("SORTER_JOB_ID")));
                postActRecord.setActionCode(convert(postActMap.get("ACTION_CODE")));
                postActRecord.setProductOrderID(convert(postActMap.get("PRODUCT_ORDER_ID")));
                postActRecord.setVendorID(convert(postActMap.get("VENDOR_ID")));
                postActRecord.setWaferCount(convertI(postActMap.get("WAFER_COUNT")));
                postActRecord.setSourceProductID(convert(postActMap.get("SOURCE_PRODUCT_ID")));
                postActRecord.setParentLotId(convert(postActMap.get("PARENT_LOT_ID")));
                postActRecord.setChildLotId(convert(postActMap.get("CHILD_LOT_ID")));

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
        String sql="SELECT * FROM OMEVSORTJOB_CDA WHERE REFKEY=?";
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
     * @param
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 13:41
     */
    public com.fa.cim.dto.Infos.Output insertSortJob(Infos.Ohsortjobhs ohsortjobhs) {
        log.info("HistoryWatchDogServer::insertSortJob Function" );

        String  equipmentID = "";
        String  portGroupID = "";
        String  sorterJobID = "";
        String  operation = "";
        String  sorterJobStatus = "";
        Integer   waferIDReadFlag = 0;
        String  claimTime = "";
        String  claimUser = "";
        String  claimMemo = "";
        String  storeTime = "";
        String  eventCreateTime = "";
        String ctrlJobID = "";
        Integer componentJobCount = 0;

        equipmentID = ohsortjobhs.getEquipmentID();
        portGroupID = ohsortjobhs.getPortGroupID();
        sorterJobID = ohsortjobhs.getSorterJobID();
        operation = ohsortjobhs.getOperation();
        sorterJobStatus = ohsortjobhs.getSorterJobStatus();
        waferIDReadFlag = ohsortjobhs.getWaferIDReadFlag();
        claimTime = ohsortjobhs.getClaimTime();
        claimUser = ohsortjobhs.getClaimUser();
        claimMemo = ohsortjobhs.getClaimMemo();
        storeTime = ohsortjobhs.getStoreTime();
        eventCreateTime = ohsortjobhs.getEventCreateTime();
        ctrlJobID = ohsortjobhs.getCtrlJobID();
        componentJobCount = ohsortjobhs.getComponentJobCount();

        String ID = generateID(Infos.Ohsortjobhs.class);
        baseCore.insert("INSERT INTO OHSORTJOB ( ID, EQP_ID, PORT_GROUP_ID, SORTER_JOB_ID, OPERATION, SORTER_JOB_STATUS, WAFER_ID_READ_FLAG, " +
                        "TRX_TIME, TRX_USER_ID, TRX_MEMO, STORE_TIME, EVENT_CREATE_TIME, CTRLJOB_ID, COMPONENT_JOB_COUNT )\n" +
                        "VALUES\n" +
                        "\t( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )",
                ID,
                equipmentID,
                portGroupID,
                sorterJobID,
                operation,
                sorterJobStatus,
                waferIDReadFlag,
                convert(claimTime),
                claimUser,
                claimMemo,
                convert(storeTime),
                convert(eventCreateTime),
                ctrlJobID,
                componentJobCount);

        com.fa.cim.dto.Infos.Output output = new com.fa.cim.dto.Infos.Output();
        output.setResponse(returnOK());
        output.setRefkey(ID);
        log.info("HistoryWatchDogServer::insertSortJob Function" );
        return(output);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 13:46
     */
    public Response insertComponentJob(Infos.OhsortjobhsComponent componentRecord) {
        log.info("HistoryWatchDogServer::insertComponentJob Function" );

        String componentJobID = "";
        String sourceCassetteID = "";
        String destinationCassetteID = "";
        String sourcePortID = "";
        String destinationPortID = "";
        String componentJobStatus = "";
        String actionCode = "";
        String operation = "";
        String claimTime = "";
        String claimUser = "";
        String refkey = "";

        componentJobID = componentRecord.getComponentJobID();
        sourceCassetteID = componentRecord.getSourceCassetteID();
        destinationCassetteID = componentRecord.getDestinationCassetteID();
        sourcePortID = componentRecord.getSourcePortID();
        destinationPortID = componentRecord.getDestinationPortID();
        componentJobStatus = componentRecord.getComponentJobStatus();
        actionCode = componentRecord.getActionCode();
        operation = componentRecord.getOperation();
        claimTime = componentRecord.getClaimTime();
        claimUser = componentRecord.getClaimUser();
        refkey = componentRecord.getRefkey();

        baseCore.insert("INSERT INTO OHSORTJOB_COMP ( ID, COMPONENT_JOB_ID, SRC_CARRIER_ID, DEST_CARRIER_ID, " +
                        "SRC_PORT_ID, DEST_PORT_ID, TRX_TIME, REFKEY, COMPONENT_JOB_STATUS, ACTION_CODE, TRX_USER_ID, OPERATION )\n" +
                        "VALUES\n" +
                        "\t( ?,?,?,?,?,?,?,?,?,?,?,? )",
                generateID(Infos.OhsortjobhsComponent.class),
                componentJobID,
                sourceCassetteID,
                destinationCassetteID,
                sourcePortID,
                destinationPortID,
                convert(claimTime),
                refkey,
                componentJobStatus,
                actionCode,
                claimUser,
                operation);

        log.info("HistoryWatchDogServer::insertComponentJob Function" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 14:12
     */
    public Response inserSlotMap(Infos.OhsortjobhsSlotmap slotmapRecord) {
        log.info("HistoryWatchDogServer::inserSlotMap Function" );

        String componentJobID = "";
        String lotID = "";
        String waferID = "";
        Integer destinationSlotPosition = 0;
        Integer sourceSlotPosition = 0;
        String aliasName = "";
        String direction = "";
        Integer sorterStatus = 0;
        String claimTime = "";
        String claimUser = "";
        String refkey = "";

        componentJobID = slotmapRecord.getComponentJobID();
        lotID = slotmapRecord.getLotID();
        waferID = slotmapRecord.getWaferID();
        destinationSlotPosition = slotmapRecord.getDestinationSlotPosition();
        sourceSlotPosition = slotmapRecord.getSourceSlotPosition();
        aliasName = slotmapRecord.getAliasName();
        direction = slotmapRecord.getDirection();
        sorterStatus = slotmapRecord.getSorterStatus();
        claimTime = slotmapRecord.getClaimTime();
        claimUser = slotmapRecord.getClaimUser();
        refkey = slotmapRecord.getRefkey();

        baseCore.insert("INSERT INTO OHSORTJOB_COMP_SLOTMAP ( ID, COMPONENT_JOB_ID, LOT_ID, WAFER_ID, DEST_POSITION, " +
                        "SRC_POSITION, TRX_TIME, REFKEY, TRX_USER_ID, ALIAS_NAME, DIRECTION, SORTER_STATUS )\n" +
                        "VALUES\n" +
                        "\t( ?,?,?,?,?,?,?,?,?,?,?,? )",
                generateID(Infos.OhsortjobhsSlotmap.class),
                componentJobID,
                lotID,
                waferID,
                destinationSlotPosition,
                sourceSlotPosition,
                convert(claimTime),
                refkey,
                claimUser,
                aliasName,
                direction,
                sorterStatus);

        log.info("HistoryWatchDogServer::inserSlotMap Function" );
        return(returnOK());
    }

    /**
    * description:
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/3 10:53 上午 ZH Create
    *
    * @author ZH
    * @date 2021/7/3 10:53 上午
    * @param  ‐
    * @return com.fa.cim.dto.Response
    */
    public Response insertPostAct(Infos.OhPostAct sortHistoryPostActRecord) {
        log.info("HistoryWatchDogServer::insertPostAct Function" );

        String sorterJobID = "";
        String actionCode = "";
        String productOrderID = "";
        String vendorID = "";
        Integer waferCount = 0;
        String sourceProductID = "";
        String claimTime = "";
        String claimUser = "";
        String refkey = "";
        String childLotId = "";
        String parentLotId = "";

        sorterJobID = sortHistoryPostActRecord.getSorterJobID();
        actionCode = sortHistoryPostActRecord.getActionCode();
        productOrderID = sortHistoryPostActRecord.getProductOrderID();
        vendorID = sortHistoryPostActRecord.getVendorID();
        waferCount = sortHistoryPostActRecord.getWaferCount();
        sourceProductID = sortHistoryPostActRecord.getSourceProductID();
        claimTime = sortHistoryPostActRecord.getClaimTime();
        claimUser = sortHistoryPostActRecord.getClaimUser();
        refkey = sortHistoryPostActRecord.getRefkey();
        childLotId = sortHistoryPostActRecord.getChildLotId();
        parentLotId = sortHistoryPostActRecord.getParentLotId();

        baseCore.insert("INSERT INTO OHSORTJOB_POSTACT ( ID, SORTER_JOB_ID, ACTION_CODE, PRODUCT_ORDER_ID, VENDOR_ID," +
                        " WAFER_COUNT, SOURCE_PRODUCT_ID, TRX_TIME, TRX_USER_ID, REFKEY,PARENT_LOT_ID,CHILD_LOT_ID )\n" +
                        "VALUES\n" +
                        "\t( ?,?,?,?,?,?,?,?,?,?,?,? )",
                generateID(Infos.OhPostAct.class),
                sorterJobID,
                actionCode,
                productOrderID,
                vendorID,
                waferCount,
                sourceProductID,
                convert(claimTime),
                claimUser,
                refkey,
                parentLotId,
                childLotId);

        log.info("HistoryWatchDogServer::insertPostAct Function" );
        return(returnOK());
    }
}
