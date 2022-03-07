package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * CimLotTerminateEvent
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-7-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-7-10 15:13
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Repository
public class LotTerminateHistoryService {

    @Autowired
    private BaseCore baseCore;

    public Infos.LotTerminateEventRecord getEventData(String id) {
        String sql = "SELECT * FROM OMEVLOTTRM WHERE ID = ? and ROWNUM < 2";
        List<Map> list = baseCore.queryAllForMap(sql, id);
        if (list == null || list.size() < 1)
            return null;

        Infos.LotTerminateEventRecord eventRecord = new Infos.LotTerminateEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        eventRecord.setEventCommon(eventCommon);

        Infos.LotEventData lotData = new Infos.LotEventData();
        eventRecord.setLotData(lotData);

        for (Map data : list) {
            lotData.setLotID(convert(data.get("LOT_ID")));
            lotData.setLotType(convert(data.get("LOT_TYPE")));
            lotData.setCassetteID(convert(data.get("CARRIER_ID")));
            lotData.setLotStatus(convert(data.get("LOT_STATUS")));
            lotData.setCustomerID(convert(data.get("CUSTOMER_ID")));
            lotData.setPriorityClass(convertL(data.get("LOT_PRIORITY")));
            lotData.setProductID(convert(data.get("PROD_ID")));
            lotData.setOriginalWaferQuantity(convertI(data.get("ORIGINAL_QTY")));
            lotData.setCurrentWaferQuantity(convertI(data.get("CUR_QTY")));
            lotData.setProductWaferQuantity(convertI(data.get("PROD_QTY")));
            lotData.setControlWaferQuantity(convertI(data.get("NPW_QTY")));
            lotData.setHoldState(convert(data.get("LOT_HOLD_STATE")));
            lotData.setBankID(convert(data.get("BANK_ID")));
            lotData.setRouteID(convert(data.get("PROCESS_ID")));
            lotData.setOperationNumber(convert(data.get("OPE_NO")));
            lotData.setOperationID(convert(data.get("STEP_ID")));
            lotData.setOperationPassCount(convertI(data.get("PASS_COUNT")));
            lotData.setObjrefPOS(convert(data.get("PRSS_RKEY")));
            lotData.setWaferHistoryTimeStamp(convert(data.get("WAFER_HIS_TIME")));
            lotData.setObjrefPO(convert(data.get("PROPE_RKEY")));
            lotData.setObjrefMainPF(convert(data.get("MROUTE_PRF_RKEY")));
            lotData.setObjrefModulePOS(convert(data.get("ROUTE_PRSS_RKEY")));

            eventCommon.setTransactionID(convert(data.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(data.get("EVENT_TIME")));

            eventCommon.setUserID(convert(data.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(data.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(data.get("EVENT_CREATE_TIME")));

            eventRecord.setReasonCode(objectIdentifier(convert(data.get("REASON_CODE_ID")),
                    convert(data.get("REASON_CODE_RKEY"))));
        }

        return eventRecord;
    }

    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql = "SELECT * FROM OMEVLOTTRM_CDA WHERE REFKEY = ?";
        List<Map> list = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets = Lists.newArrayList();
        for (Map data : list) {
            Infos.UserDataSet userDataSet = new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(data.get("NAME")));
            userDataSet.setType(convert(data.get("TYPE")));
            userDataSet.setValue(convert(data.get("VALUE")));
            userDataSet.setOriginator(convert(data.get("ORIG")));
        }
        return userDataSets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName, String refKey) {
        String sql = String.format("DELETE %s WHERE REFKEY = ?", tableName);
        baseCore.insert(sql, refKey);
    }

    public List<String> getEventFIFO(String tableName) {
        String sql = String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE " +
                "TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF') < SYSDATE ORDER BY EVENT_TIME ASC", tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events = new ArrayList<>();
        fifos.forEach(fifo -> events.add(convert(fifo[0])));
        return events;
    }

    public Response insertLotTerminateHistory(Infos.Ohtrmhs record) {
        baseCore.insert("INSERT INTO OHTERMINATE(ID,\n" +
                        "                        LOT_ID,\n" +
                        "                        WAFER_ID,\n" +
                        "                        REASON_CODE,\n" +
                        "                        REASON_DESC,\n" +
                        "                        PROD_ID,\n" +
                        "                        LOT_OWNER_ID,\n" +
                        "                        PRODFMLY_ID,\n" +
                        "                        TECH_ID,\n" +
                        "                        CUSTPROD_ID,\n" +
                        "                        ORDER_NO,\n" +
                        "                        CUSTOMER_ID,\n" +
                        "                        NPW_WAFER,\n" +
                        "                        GOOD_UNIT_WAFER,\n" +
                        "                        REPAIR_UNIT_WAFER,\n" +
                        "                        FAIL_UNIT_WAFER,\n" +
                        "                        LOT_TYPE,\n" +
                        "                        CARRIER_ID,\n" +
                        "                        CARRIER_CATEGORY,\n" +
                        "                        PROD_TYPE,\n" +
                        "                        PROCESS_ID,\n" +
                        "                        OPE_NO,\n" +
                        "                        STEP_ID,\n" +
                        "                        PASS_COUNT,\n" +
                        "                        OPE_NAME,\n" +
                        "                        TEST_TYPE,\n" +
                        "                        TRX_TIME,\n" +
                        "                        TRX_WORK_DATE,\n" +
                        "                        TRX_USER_ID,\n" +
                        "                        STAGE_ID,\n" +
                        "                        STAGE_GRP_ID,\n" +
                        "                        PHOTO_LAYER,\n" +
                        "                        DEPARTMENT,\n" +
                        "                        BANK_ID,\n" +
                        "                        REASON_LOT_ID,\n" +
                        "                        REASON_PROCESS_ID,\n" +
                        "                        REASON_OPE_NO,\n" +
                        "                        REASON_STEP_ID,\n" +
                        "                        REASON_PASS_COUNT,\n" +
                        "                        REASON_OPE_NAME,\n" +
                        "                        REASON_TEST_TYPE,\n" +
                        "                        REASON_STAGE_ID,\n" +
                        "                        REASON_STAGE_GRP_ID,\n" +
                        "                        REASON_PHOTO_LAYER,\n" +
                        "                        REASON_DEPARTMENT,\n" +
                        "                        REASON_LOCATION_ID,\n" +
                        "                        REASON_BAY_ID,\n" +
                        "                        REASON_EQP_ID,\n" +
                        "                        REASON_EQP_NAME,\n" +
                        "                        TERMINATE_TYPE,\n" +
                        "                        TRX_MEMO,\n" +
                        "                        EVENT_CREATE_TIME,\n" +
                        "                        STORE_TIME )\n" +
                        "        Values (?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                    ?,\n" +
                        "                CURRENT_TIMESTAMP )",
                generateID(Infos.Ohtrmhs.class),
                record.getLotId(),
                record.getWaferId(),
                record.getReasonCode(),
                record.getReasonDescription(),
                record.getProdspecId(),
                record.getLotOwnerId(),
                record.getProdgrpId(),
                record.getTechId(),
                record.getCustprodId(),
                record.getOrderNo(),
                record.getCustomerId(),
                record.getControlWafer(),
                record.getGoodUnitWafer(),
                record.getRepairUnitWafer(),
                record.getFailUnitWafer(),
                record.getLotType(),
                record.getCastId(),
                record.getCastCategory(),
                record.getProdType(),
                record.getClaimMainpdId(),
                record.getClaimOpeNo(),
                record.getClaimPdId(),
                record.getClaimPassCount(),
                record.getClaimOpeName(),
                record.getClaimTestType(),
                convert(record.getClaimTime()),
                record.getClaimShopDate(),
                record.getClaimUserId(),
                record.getClaimStageId(),
                record.getClaimStagegrpId(),
                record.getClaimPhotoLayer(),
                record.getClaimDepartment(),
                record.getClaimBankId(),
                record.getReasonLotId(),
                record.getReasonMainpdId(),
                record.getReasonOpeNo(),
                record.getReasonPdId(),
                record.getReasonPassCount(),
                record.getReasonOpeName(),
                record.getReasonTestType(),
                record.getReasonStageId(),
                record.getReasonStagegrpId(),
                record.getReasonPhotoLayer(),
                record.getReasonDepartment(),
                record.getReasonLocationId(),
                record.getReasonAreaId(),
                record.getReasonEqpId(),
                record.getReasonEqpName(),
                record.getTerminateType(),
                record.getClaimMemo(),
                convert(record.getEventCreateTime()));

        return (returnOK());
    }

}
