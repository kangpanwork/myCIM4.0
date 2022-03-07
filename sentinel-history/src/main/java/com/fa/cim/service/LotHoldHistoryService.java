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
 * @date 2019/5/31 16:16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotHoldHistoryService {

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
     * @return com.fa.cim.dto.Infos.LotReworkEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/5 13:15
     */
    public Infos.LotHoldEventRecord getEventData(String id) {
        String sql="Select * from OMEVLHOLD where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotHoldEventRecord theEventData=new Infos.LotHoldEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.LotHoldEventData> holdRecords = new ArrayList<>();
        theEventData.setHoldRecords(holdRecords);
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

            sql="Select * from OMEVLHOLD_HOLD where REFKEY=?";
            List<Map> sqlHolds=baseCore.queryAllForMap(sql,id);

            for (Map sqlHold:sqlHolds) {
                Infos.LotHoldEventData holdRecord = new Infos.LotHoldEventData();
                holdRecords.add(holdRecord);

                holdRecord.setMovementFlag(convertB(sqlHold.get("MOVE_FLAG")));
                holdRecord.setChangeStateFlag(convertB(sqlHold.get("STATE_CHG_FLAG")));
                holdRecord.setHoldType(convert(sqlHold.get("HOLD_TYPE")));
                holdRecord.setHoldReasonCodeID(objectIdentifier(convert(sqlHold.get("HOLD_REASON_ID")),
                        convert(sqlHold.get("HOLD_REASON_RKEY"))));
                holdRecord.setHoldUserID(convert(sqlHold.get("HOLD_USER_ID")));
                holdRecord.setHoldTimeStamp(convert(sqlHold.get("HOLD_TIME")));
                holdRecord.setResponsibleOperationFlag(convertB(sqlHold.get("RESP_OPE_FLAG")));
                holdRecord.setHoldClaimMemo(convert(sqlHold.get("HOLD_TRX_MEMO")));
                holdRecord.setResponsibleOperationExistFlag(convertB(sqlHold.get("RESP_OPE_EXIST_FLAG")));
                holdRecord.setDepartmentNamePlate(convert(sqlHold.get("DPT_NAME_PLATE")));
            }

            theEventData.setReleaseReasonCodeID(objectIdentifier(convert(sqlData.get("REL_REASON_ID")),
                    convert(sqlData.get("REL_REASON_RKEY"))));

            theEventData.setPreviousRouteID(convert(sqlData.get("PREV_PROCESS_ID")));
            theEventData.setPreviousOperationNumber(convert(sqlData.get("PREV_OPE_NO")));
            theEventData.setPreviousOperationID(convert(sqlData.get("PREV_STEP_ID")));
            theEventData.setPreviousOperationPassCount(convertL(sqlData.get("PREV_PASS_COUNT")));
            theEventData.setPreviousObjrefPOS(convert(sqlData.get("PREV_PRSS_RKEY")));
            theEventData.setPreviousObjrefMainPF(convert(sqlData.get("PREV_MROUTE_PRF_RKEY")));
            theEventData.setPreviousObjrefModulePOS(convert(sqlData.get("PREV_ROUTE_PRSS_RKEY")));

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
     * @date 2019/6/5 13:50
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVLHOLD_CDA WHERE REFKEY=?";
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

}
