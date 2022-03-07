package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
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
 * @date 2019/6/6 16:46
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotWaferMoveHistoryService {

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
     * @return com.fa.cim.dto.Infos.LotOperationMoveEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/6 10:34
     */
    public Infos.LotWaferMoveEventRecord getEventData(String id) {
        String sql="Select * from OMEVLWFMV where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotWaferMoveEventRecord theEventData=new Infos.LotWaferMoveEventRecord();
        List<Infos.NewWaferEventData> currentWafers=new ArrayList<>();
        theEventData.setCurrentWafers(currentWafers);
        Infos.LotEventData destinationLotData=new Infos.LotEventData();
        theEventData.setDestinationLotData(destinationLotData);
        List<Infos.SourceLotEventData> sourceLots=new ArrayList<>();
        theEventData.setSourceLots(sourceLots);
        List<Infos.NewWaferEventData> sourceWafers=new ArrayList<>();
        theEventData.setSourceWafers(sourceWafers);
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            destinationLotData.setLotID(convert(sqlData.get("LOT_ID")));
            destinationLotData.setLotType(convert(sqlData.get("LOT_TYPE")));
            destinationLotData.setCassetteID(convert(sqlData.get("CARRIER_ID")));
            destinationLotData.setLotStatus(convert(sqlData.get("LOT_STATUS")));
            destinationLotData.setCustomerID(convert(sqlData.get("CUSTOMER_ID")));
            destinationLotData.setPriorityClass(convertL(sqlData.get("LOT_PRIORITY")));
            destinationLotData.setProductID(convert(sqlData.get("PROD_ID")));
            destinationLotData.setOriginalWaferQuantity(convertI(sqlData.get("ORIGINAL_QTY")));
            destinationLotData.setCurrentWaferQuantity(convertI(sqlData.get("CUR_QTY")));
            destinationLotData.setProductWaferQuantity(convertI(sqlData.get("PROD_QTY")));
            destinationLotData.setControlWaferQuantity(convertI(sqlData.get("NPW_QTY")));
            destinationLotData.setHoldState(convert(sqlData.get("LOT_HOLD_STATE")));
            destinationLotData.setBankID(convert(sqlData.get("BANK_ID")));
            destinationLotData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            destinationLotData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            destinationLotData.setOperationID(convert(sqlData.get("STEP_ID")));
            destinationLotData.setOperationPassCount(convertI(sqlData.get("PASS_COUNT")));
            destinationLotData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            destinationLotData.setWaferHistoryTimeStamp(convert(sqlData.get("WAFER_HIS_TIME")));
            destinationLotData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));
            destinationLotData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            destinationLotData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));

            sql="select * from OMEVLWFMV_CURWFR where refkey=?";
            List<Map> sqlCurrentWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlCurrentWafer:sqlCurrentWafers) {
                Infos.NewWaferEventData currentWafer=new Infos.NewWaferEventData();
                currentWafers.add(currentWafer);
                currentWafer.setWaferID(convert(sqlCurrentWafer.get("WAFER_ID")));
                currentWafer.setControlWaferFlag(convertB(sqlCurrentWafer.get("NPW_WAFER")));
                currentWafer.setSlotNumber(convertL(sqlCurrentWafer.get("SLOT_NO")));
                currentWafer.setOriginalWaferID(convert(sqlCurrentWafer.get("ORIG_WAFER_ID")));
            }

            sql="Select * from OMEVLWFMV_SRCWFR where refkey=?";
            List<Map> sqlSourceWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlSourceWafer:sqlSourceWafers) {
                Infos.NewWaferEventData sourceWafer=new Infos.NewWaferEventData();
                sourceWafers.add(sourceWafer);
                sourceWafer.setWaferID(convert(sqlSourceWafer.get("WAFER_ID")));
                sourceWafer.setControlWaferFlag(convertB(sqlSourceWafer.get("NPW_WAFER")));
                sourceWafer.setSlotNumber(convertL(sqlSourceWafer.get("SLOT_NO")));
                sourceWafer.setOriginalWaferID(convert(sqlSourceWafer.get("ORIG_WAFER_ID")));
            }

            sql="select * from OMEVLWFMV_SRCLOT where refkey=?";
            List<Map> sqlSourceLots=baseCore.queryAllForMap(sql,id);

            for (Map sqlSourceLot:sqlSourceLots) {
                Infos.SourceLotEventData sourceLot=new Infos.SourceLotEventData();
                sourceLots.add(sourceLot);

                Infos.LotEventData sourceLotData=new Infos.LotEventData();
                sourceLot.setSourceLotData(sourceLotData);

                sourceLotData.setLotID(convert(sqlSourceLot.get("LOT_ID")));
                sourceLotData.setLotType(convert(sqlSourceLot.get("LOT_TYPE")));
                sourceLotData.setCassetteID(convert(sqlSourceLot.get("CARRIER_ID")));
                sourceLotData.setLotStatus(convert(sqlSourceLot.get("LOT_STATUS")));
                sourceLotData.setCustomerID(convert(sqlSourceLot.get("CUSTOMER_ID")));
                sourceLotData.setPriorityClass(convertL(sqlSourceLot.get("LOT_PRIORITY")));
                sourceLotData.setProductID(convert(sqlSourceLot.get("PROD_ID")));
                sourceLotData.setOriginalWaferQuantity(convertI(sqlSourceLot.get("ORIGINAL_QTY")));
                sourceLotData.setCurrentWaferQuantity(convertI(sqlSourceLot.get("CUR_QTY")));
                sourceLotData.setProductWaferQuantity(convertI(sqlSourceLot.get("PROD_QTY")));
                sourceLotData.setControlWaferQuantity(convertI(sqlSourceLot.get("NPW_QTY")));
                sourceLotData.setHoldState(convert(sqlSourceLot.get("LOT_HOLD_STATE")));
                sourceLotData.setBankID(convert(sqlSourceLot.get("BANK_ID")));
                sourceLotData.setRouteID(convert(sqlSourceLot.get("PROCESS_ID")));
                sourceLotData.setOperationNumber(convert(sqlSourceLot.get("OPE_NO")));
                sourceLotData.setOperationID(convert(sqlSourceLot.get("STEP_ID")));
                sourceLotData.setOperationPassCount(convertI(sqlSourceLot.get("PASS_COUNT")));
                sourceLotData.setObjrefPOS(convert(sqlSourceLot.get("PRSS_RKEY")));
                sourceLotData.setWaferHistoryTimeStamp(convert(sqlSourceLot.get("WAFER_HIS_TIME")));
                sourceLotData.setObjrefPO(convert(sqlSourceLot.get("PROPE_RKEY")));
                sourceLotData.setObjrefMainPF(convert(sqlSourceLot.get("MROUTE_PRF_RKEY")));
                sourceLotData.setObjrefModulePOS(convert(sqlSourceLot.get("ROUTE_PRSS_RKEY")));

                sql="select * from OMEVLWFMV_SRCLOT_SW where refkey=? and LINK_MARKER=?";
                sqlSourceWafers=baseCore.queryAllForMap(sql,id,sqlSourceLot.get("IDX_NO"));

                List<Infos.WaferEventData> theSourceWafers=new ArrayList<>();
                sourceLot.setSourceWafers(theSourceWafers);

                for (Map sqlSourceWafer: sqlSourceWafers) {
                    Infos.WaferEventData sourceWafer=new Infos.WaferEventData();
                    theSourceWafers.add(sourceWafer);
                    sourceWafer.setWaferID(convert(sqlSourceWafer.get("WAFER_ID")));
                    sourceWafer.setOriginalWaferID(convert(sqlSourceWafer.get("ORIG_WAFER_ID")));
                    sourceWafer.setControlWaferFlag(convertB(sqlSourceWafer.get("NPW_WAFER")));
                    sourceWafer.setOriginalCassetteID(convert(sqlSourceWafer.get("ORIG_CARRIER_ID")));
                    sourceWafer.setOriginalSlotNumber(convertL(sqlSourceWafer.get("ORIG_SLOT_NO")));
                    sourceWafer.setDestinationCassetteID(convert(sqlSourceWafer.get("DEST_CARRIER_ID")));
                    sourceWafer.setDestinationSlotNumber(convertL(sqlSourceWafer.get("DEST_SLOT_NO")));
                }

                sql="select * from OMEVLWFMV_SRCLOT_CW where refkey=? and LINK_MARKER=?";
                sqlCurrentWafers=baseCore.queryAllForMap(sql,id,sqlSourceLot.get("IDX_NO"));

                List<Infos.WaferEventData> theCurrentWafers=new ArrayList<>();
                sourceLot.setCurrentWafers(theCurrentWafers);

                for (Map sqlCurrentWafer: sqlCurrentWafers) {
                    Infos.WaferEventData sourceWafer=new Infos.WaferEventData();
                    theCurrentWafers.add(sourceWafer);
                    sourceWafer.setWaferID(convert(sqlCurrentWafer.get("WAFER_ID")));
                    sourceWafer.setOriginalWaferID(convert(sqlCurrentWafer.get("ORIG_WAFER_ID")));
                    sourceWafer.setControlWaferFlag(convertB(sqlCurrentWafer.get("NPW_WAFER")));
                    sourceWafer.setOriginalCassetteID(convert(sqlCurrentWafer.get("ORIG_CARRIER_ID")));
                    sourceWafer.setOriginalSlotNumber(convertL(sqlCurrentWafer.get("ORIG_SLOT_NO")));
                    sourceWafer.setDestinationCassetteID(convert(sqlCurrentWafer.get("DEST_CARRIER_ID")));
                    sourceWafer.setDestinationSlotNumber(convertL(sqlCurrentWafer.get("DEST_SLOT_NO")));
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
     * @date 2019/6/4 13:37
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVLWFMV_CDA WHERE REFKEY=?";
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

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param tableName
     * @param refKey
     * @return void
     * @exception
     * @author Ho
     * @date 2019/6/4 11:25
     */
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

}
