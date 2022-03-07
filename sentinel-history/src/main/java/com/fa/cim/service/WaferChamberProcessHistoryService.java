package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
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
 * @author Ho
 * @exception
 * @date 2019/6/6 16:46
 */
@Repository
//@Transactional(rollbackFor = Exception.class)
public class WaferChamberProcessHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param waferChamberProcessRecord
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/6/14 11:44
     */
    public Response insertWaferChamberProcessHistory(Infos.Ohwcphs waferChamberProcessRecord) {
        String hFHWCPHS_CTRLJOB_ID;
        String hFHWCPHS_WAFER_ID;
        String hFHWCPHS_LOT_ID;
        String hFHWCPHS_MAINPD_ID;
        String hFHWCPHS_OPE_NO;
        Integer hFHWCPHS_OPE_PASS_COUNT;
        String hFHWCPHS_EQP_ID;
        String hFHWCPHS_EQP_NAME;
        String hFHWCPHS_PROCRSC_ID;
        String hFHWCPHS_PROC_TIME;
        Double hFHWCPHS_PROC_SHOP_DATE;
        String hFHWCPHS_CLAIM_TIME;
        Double hFHWCPHS_CLAIM_SHOP_DATE;
        String hFHWCPHS_CLAIM_USER_ID;
        String hFHWCPHS_CLAIM_MEMO;
        String hFHWCPHS_STORE_TIME;
        String hFHWCPHS_ACTION_CODE;
        String hFHWCPHS_EVENT_CREATE_TIME;

        hFHWCPHS_CTRLJOB_ID = waferChamberProcessRecord.getCtrl_job();
        hFHWCPHS_WAFER_ID = waferChamberProcessRecord.getWafer_id();
        hFHWCPHS_LOT_ID = waferChamberProcessRecord.getLot_id();
        hFHWCPHS_MAINPD_ID = waferChamberProcessRecord.getMainpd_id();
        hFHWCPHS_OPE_NO = waferChamberProcessRecord.getOpe_no();
        hFHWCPHS_OPE_PASS_COUNT = waferChamberProcessRecord.getOpe_pass_count();
        hFHWCPHS_EQP_ID = waferChamberProcessRecord.getEqp_id();
        hFHWCPHS_EQP_NAME = waferChamberProcessRecord.getEqp_name();
        hFHWCPHS_PROCRSC_ID = waferChamberProcessRecord.getProcrsc_id();
        hFHWCPHS_PROC_TIME = waferChamberProcessRecord.getProc_time();
        hFHWCPHS_PROC_SHOP_DATE = waferChamberProcessRecord.getProc_shop_date();
        hFHWCPHS_CLAIM_TIME = waferChamberProcessRecord.getClaim_time();
        hFHWCPHS_CLAIM_SHOP_DATE = waferChamberProcessRecord.getClaim_shop_date();
        hFHWCPHS_CLAIM_USER_ID = waferChamberProcessRecord.getClaim_user_id();
        hFHWCPHS_CLAIM_MEMO = waferChamberProcessRecord.getClaim_memo();
        hFHWCPHS_ACTION_CODE = waferChamberProcessRecord.getAction_code();
        hFHWCPHS_EVENT_CREATE_TIME = waferChamberProcessRecord.getEvent_create_time();

        baseCore.insert("INSERT INTO OHWFCMB\n" +
                        "            ( ID,CJ_ID,\n" +
                        "                    WAFER_ID,\n" +
                        "                    LOT_ID,\n" +
                        "                    PROCESS_ID,\n" +
                        "                    OPE_NO,\n" +
                        "                    OPE_PASS_COUNT,\n" +
                        "                    EQP_ID,\n" +
                        "                    EQP_NAME,\n" +
                        "                    PROCRES_ID,\n" +
                        "                    PROC_TIME,\n" +
                        "                    TRX_TIME,\n" +
                        "                    TRX_USER_ID,\n" +
                        "                    TRX_MEMO,\n" +
                        "                    STORE_TIME,\n" +
                        "                    EVENT_CREATE_TIME,\n" +
                        "                    ACTION_CODE)\n" +
                        "        VALUES\n" +
                        "                ( ?,?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "            CURRENT_TIMESTAMP,\n" +
                        "               ?,\n" +
                        "               ?)", generateID(Infos.Ohwcphs.class)
                , hFHWCPHS_CTRLJOB_ID
                , hFHWCPHS_WAFER_ID
                , hFHWCPHS_LOT_ID
                , hFHWCPHS_MAINPD_ID
                , hFHWCPHS_OPE_NO
                , hFHWCPHS_OPE_PASS_COUNT
                , hFHWCPHS_EQP_ID
                , hFHWCPHS_EQP_NAME
                , hFHWCPHS_PROCRSC_ID
                , convert(hFHWCPHS_PROC_TIME)
                , convert(hFHWCPHS_CLAIM_TIME)
                , hFHWCPHS_CLAIM_USER_ID
                , hFHWCPHS_CLAIM_MEMO
                , convert(hFHWCPHS_EVENT_CREATE_TIME)
                , hFHWCPHS_ACTION_CODE);

        return (returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.WaferChamberProcessEventRecord
     * @throws
     * @author Ho
     * @date 2019/6/14 14:03
     */
    public Infos.WaferChamberProcessEventRecord getEventData(String id) {
        String sql = "Select * from OMEVWFCMB where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.WaferChamberProcessEventRecord theEventData = new Infos.WaferChamberProcessEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String, Object> sqlData : sqlDatas) {
            theEventData.setWaferID(convert(sqlData.get("WAFER_ID")));
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOpeNo(convert(sqlData.get("OPE_NO")));
            theEventData.setPassCount(convertL(sqlData.get("PASS_COUNT")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setProcessResourceID(convert(sqlData.get("PROCRES_ID")));
            theEventData.setProcessTime(convert(sqlData.get("CHAMBER_PROC_TIME")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            theEventData.setActionCode(convert(sqlData.get("CHAMBER_EVENT_CODE")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));

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
     * @throws
     * @author Ho
     * @date 2019/6/4 13:37
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql = "SELECT * FROM OMEVWFCMB_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets = new ArrayList<>();
        for (Map<String, Object> uData : uDatas) {
            Infos.UserDataSet userDataSet = new Infos.UserDataSet();
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
     * @throws
     * @author Ho
     * @date 2019/6/4 11:25
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName, String refKey) {
        String sql = String.format("DELETE %s WHERE REFKEY=?", tableName);
        baseCore.insert(sql, refKey);
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
     * @throws
     * @author Ho
     * @date 2019/4/19 17:41
     */
    public List<String> getEventFIFO(String tableName) {
        String sql = String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC", tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events = new ArrayList<>();
        fifos.forEach(fifo -> events.add(convert(fifo[0])));
        return events;
    }

}
