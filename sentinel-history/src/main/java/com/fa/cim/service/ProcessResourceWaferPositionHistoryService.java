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
@Transactional(rollbackFor = Exception.class)
public class ProcessResourceWaferPositionHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processResourceWaferPositionRecord
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/6/14 10:50
     */
    public Response insertProcessResourceWaferPositionHistory(Infos.Ohprwphs processResourceWaferPositionRecord) {
        String hFHPROCRSCWPOS_WAFER_ID;
        String hFHPROCRSCWPOS_LOT_ID;
        String hFHPROCRSCWPOS_CTRL_JOB;
        String hFHPROCRSCWPOS_MAINPD_ID;
        String hFHPROCRSCWPOS_OPE_NO;
        Integer hFHPROCRSCWPOS_OPE_PASS_COUNT;
        String hFHPROCRSCWPOS_EQP_ID;
        String hFHPROCRSCWPOS_EQP_NAME;
        String hFHPROCRSCWPOS_PROCRSC_ID;
        String hFHPROCRSCWPOS_WAFER_POSITION;
        String hFHPROCRSCWPOS_PROC_TIME;
        Double hFHPROCRSCWPOS_PROC_SHOP_DATE;
        String hFHPROCRSCWPOS_CLAIM_TIME;
        Double hFHPROCRSCWPOS_CLAIM_SHOP_DATE;
        String hFHPROCRSCWPOS_CLAIM_USER_ID;
        String hFHPROCRSCWPOS_CLAIM_MEMO;
        String hFHPROCRSCWPOS_STORE_TIME;
        String hFHPROCRSCWPOS_EVENT_CREATE_TIME;

        hFHPROCRSCWPOS_WAFER_ID = processResourceWaferPositionRecord.getWafer_id();
        hFHPROCRSCWPOS_LOT_ID = processResourceWaferPositionRecord.getLot_id();
        hFHPROCRSCWPOS_CTRL_JOB = processResourceWaferPositionRecord.getCtrljob_id();
        hFHPROCRSCWPOS_MAINPD_ID = processResourceWaferPositionRecord.getMainpd_id();
        hFHPROCRSCWPOS_OPE_NO = processResourceWaferPositionRecord.getOpe_no();
        hFHPROCRSCWPOS_OPE_PASS_COUNT = processResourceWaferPositionRecord.getOpe_pass_count();
        hFHPROCRSCWPOS_EQP_ID = processResourceWaferPositionRecord.getEqp_id();
        hFHPROCRSCWPOS_EQP_NAME = processResourceWaferPositionRecord.getEqp_name();
        hFHPROCRSCWPOS_PROCRSC_ID = processResourceWaferPositionRecord.getProcrsc_id();
        hFHPROCRSCWPOS_WAFER_POSITION = processResourceWaferPositionRecord.getWafer_position();
        hFHPROCRSCWPOS_PROC_TIME = processResourceWaferPositionRecord.getProc_time();
        hFHPROCRSCWPOS_PROC_SHOP_DATE = processResourceWaferPositionRecord.getProc_shop_date();
        hFHPROCRSCWPOS_CLAIM_TIME = processResourceWaferPositionRecord.getClaim_time();
        hFHPROCRSCWPOS_CLAIM_SHOP_DATE = processResourceWaferPositionRecord.getClaim_shop_date();
        hFHPROCRSCWPOS_CLAIM_USER_ID = processResourceWaferPositionRecord.getClaim_user_id();
        hFHPROCRSCWPOS_CLAIM_MEMO = processResourceWaferPositionRecord.getClaim_memo();
        hFHPROCRSCWPOS_EVENT_CREATE_TIME = processResourceWaferPositionRecord.getEvent_create_time();

        baseCore.insert("INSERT INTO OHPRCRESWP\n" +
                        "            ( ID,WAFER_ID,\n" +
                        "                    LOT_ID,\n" +
                        "                    CJ_ID,\n" +
                        "                    PROCESS_ID,\n" +
                        "                    OPE_NO,\n" +
                        "                    OPE_PASS_COUNT,\n" +
                        "                    EQP_ID,\n" +
                        "                    EQP_NAME,\n" +
                        "                    PROCRES_ID,\n" +
                        "                    PROC_TIME,\n" +
                        "                    TRX_TIME,\n" +
                        "                    TRX_WORK_DATE,\n" +
                        "                    TRX_USER_ID,\n" +
                        "                    TRX_MEMO,\n" +
                        "                    EVENT_CREATE_TIME,\n" +
                        "                    WAFER_POSITION,\n"+
                        "                    STORE_TIME)\n" +
                        "        VALUES\n" +
                        "             ( ?,\n" +
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
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               ?,\n" +
                        "               CURRENT_TIMESTAMP)", generateID(Infos.Ohprwphs.class),
                hFHPROCRSCWPOS_WAFER_ID
                , hFHPROCRSCWPOS_LOT_ID
                , hFHPROCRSCWPOS_CTRL_JOB
                , hFHPROCRSCWPOS_MAINPD_ID
                , hFHPROCRSCWPOS_OPE_NO
                , hFHPROCRSCWPOS_OPE_PASS_COUNT
                , hFHPROCRSCWPOS_EQP_ID
                , hFHPROCRSCWPOS_EQP_NAME
                , hFHPROCRSCWPOS_PROCRSC_ID
                , convert(hFHPROCRSCWPOS_PROC_TIME)
                , convert(hFHPROCRSCWPOS_CLAIM_TIME)
                , hFHPROCRSCWPOS_CLAIM_SHOP_DATE
                , hFHPROCRSCWPOS_CLAIM_USER_ID
                , hFHPROCRSCWPOS_CLAIM_MEMO
                , convert(hFHPROCRSCWPOS_EVENT_CREATE_TIME)
                , hFHPROCRSCWPOS_WAFER_POSITION);

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
     * @return com.fa.cim.dto.Infos.ProcessResourceWaferPositionEventRecord
     * @throws
     * @author Ho
     * @date 2019/6/14 13:50
     */
    public Infos.ProcessResourceWaferPositionEventRecord getEventData(String id) {
        String sql = "Select * from OMEVPRCRESWP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.ProcessResourceWaferPositionEventRecord theEventData = new Infos.ProcessResourceWaferPositionEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String, Object> sqlData : sqlDatas) {
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setWaferID(convert(sqlData.get("WAFER_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));
            theEventData.setMainPDID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOpeNo(convert(sqlData.get("OPE_NO")));
            theEventData.setOpePassCount(convertL(sqlData.get("OPE_PASS_COUNT")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setProcessResourceID(convert(sqlData.get("PROCRES_ID")));
            theEventData.setWaferPosition(convert(sqlData.get("WAFER_POSITION")));
            theEventData.setProcessTime(convert(sqlData.get("PROC_TIME")));

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
     * @throws
     * @author Ho
     * @date 2019/6/4 13:37
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql = "SELECT * FROM OMEVPRCRESWP_CDA WHERE REFKEY=?";
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
        String sql = String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY ID", tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events = new ArrayList<>();
        fifos.forEach(fifo -> events.add(convert(fifo[0])));
        return events;
    }

}
