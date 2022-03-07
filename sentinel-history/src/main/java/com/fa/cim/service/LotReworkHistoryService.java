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
 * @exception
 * @author Ho
 * @date 2019/5/31 16:16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotReworkHistoryService {

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
     * @date 2019/6/4 13:33
     */
    public Infos.LotReworkEventRecord getEventData(String id) {
        String sql="Select * from OMEVLRWK where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotReworkEventRecord theEventData=new Infos.LotReworkEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);
        Infos.ProcessOperationEventData oldCurrentPOData=new Infos.ProcessOperationEventData();
        theEventData.setOldCurrentPOData(oldCurrentPOData);
        List<Infos.WaferReworkCountEventData> reworkWafers=new ArrayList<>();
        theEventData.setReworkWafers(reworkWafers);
        List<Infos.WaferPassCountEventData> processWafers = new ArrayList<>();
        theEventData.setProcessWafers(processWafers);
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
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

            theEventData.setReasonCodeID(objectIdentifier(convert(sqlData.get("REASON_ID")),
                    convert(sqlData.get("REASON_RKEY"))));
            theEventData.setPreviousRouteID(convert(sqlData.get("PREV_PROCESS_ID")));
            theEventData.setPreviousOperationNumber(convert(sqlData.get("PREV_OPE_NO")));
            theEventData.setPreviousOperationPassCount(convertL(sqlData.get("PREV_PASS_COUNT")));
            theEventData.setPreviousObjrefPOS(convert(sqlData.get("PREV_PRSS_RKEY")));
            theEventData.setPreviousObjrefMainPF(convert(sqlData.get("PREV_MROUTE_PRF_RKEY")));
            theEventData.setPreviousObjrefModulePOS(convert(sqlData.get("PREV_ROUTE_PRSS_RKEY")));

            oldCurrentPOData.setObjrefPOS(convert(sqlData.get("OLD_PRSS_RKEY")));
            oldCurrentPOData.setObjrefMainPF(convert(sqlData.get("OLD_MROUTE_PRF_RKEY")));
            oldCurrentPOData.setObjrefModulePOS(convert(sqlData.get("OLD_ROUTE_PRSS_RKEY")));
            oldCurrentPOData.setRouteID(convert(sqlData.get("OLD_PROCESS_ID")));
            oldCurrentPOData.setOperationNumber(convert(sqlData.get("OLD_OPE_NO")));
            oldCurrentPOData.setOperationID(convert(sqlData.get("OLD_STEP_ID")));
            oldCurrentPOData.setOperationPassCount(convertL(sqlData.get("OLD_PASS_COUNT")));
            oldCurrentPOData.setObjrefPO(convert(sqlData.get("OLD_PROPE_RKEY")));

            theEventData.setReworkCount(convertL(sqlData.get("REWORK_COUNT")));

            sql="Select * from OMEVLRWK_RWKCNT where REFKEY=?";
            List<Map> sqlWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlWafer:sqlWafers) {
                Infos.WaferReworkCountEventData reworkWafer=new Infos.WaferReworkCountEventData();
                reworkWafers.add(reworkWafer);
                reworkWafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                reworkWafer.setReworkCount(convertL(sqlWafer.get("REWORK_COUNT")));
            }

            sql="Select * from OMEVLRWK_PASSCNT where REFKEY=?";
            List<Map> sqlProcessWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlWafer:sqlProcessWafers) {
                Infos.WaferPassCountEventData processWafer = new Infos.WaferPassCountEventData();
                processWafers.add(processWafer);
                processWafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                processWafer.setPreviousPassCount(convertL(sqlWafer.get("PREV_PASS_COUNT")));
                processWafer.setPassCount(convertL(sqlWafer.get("PASS_COUNT")));
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
        String sql="SELECT * FROM OMEVLRWK_CDA WHERE REFKEY=?";
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
     * @param lotOperationRwkcntRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/4 11:27
     */
    public Response insertLotOperationRwkcntHistory(Infos.OhopehsRwkcnt lotOperationRwkcntRecord ) {
        String hFHOPEHS_RWKCNT_LOT_ID         ;
        String hFHOPEHS_RWKCNT_MAINPD_ID      ;
        String hFHOPEHS_RWKCNT_OPE_NO         ;
        Integer hFHOPEHS_RWKCNT_OPE_PASS_COUNT;
        String hFHOPEHS_RWKCNT_CLAIM_TIME     ;
        String hFHOPEHS_RWKCNT_OPE_CATEGORY   ;
        String hFHOPEHS_RWKCNT_WAFER_ID       ;
        Integer hFHOPEHS_RWKCNT_REWORK_COUNT;

        hFHOPEHS_RWKCNT_LOT_ID = lotOperationRwkcntRecord.getLot_id         ();
        hFHOPEHS_RWKCNT_MAINPD_ID = lotOperationRwkcntRecord.getMainpd_id      ();
        hFHOPEHS_RWKCNT_OPE_NO = lotOperationRwkcntRecord.getOpe_no         ();
        hFHOPEHS_RWKCNT_OPE_PASS_COUNT = lotOperationRwkcntRecord.getOpe_pass_count ();
        hFHOPEHS_RWKCNT_CLAIM_TIME = lotOperationRwkcntRecord.getClaim_time     ();
        hFHOPEHS_RWKCNT_OPE_CATEGORY = lotOperationRwkcntRecord.getOpe_category   ();
        hFHOPEHS_RWKCNT_WAFER_ID = lotOperationRwkcntRecord.getWafer_id       ();
        hFHOPEHS_RWKCNT_REWORK_COUNT   = lotOperationRwkcntRecord.getRework_count   ();

        baseCore.insert("INSERT INTO OHLOTOPE_RWKCNT\n" +
                "            ( ID, LOT_ID,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    OPE_PASS_COUNT,\n" +
                "                    TRX_TIME,\n" +
                "                    OPE_CATEGORY,\n" +
                "                    WAFER_ID,\n" +
                "                    REWORK_COUNT )\n" +
                "        VALUES\n" +
                "                ( ?,?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?)",generateID(Infos.OhopehsRwkcnt.class)
                , hFHOPEHS_RWKCNT_LOT_ID
                , hFHOPEHS_RWKCNT_MAINPD_ID
                , hFHOPEHS_RWKCNT_OPE_NO
                , hFHOPEHS_RWKCNT_OPE_PASS_COUNT
                , convert(hFHOPEHS_RWKCNT_CLAIM_TIME)
                , hFHOPEHS_RWKCNT_OPE_CATEGORY
                , hFHOPEHS_RWKCNT_WAFER_ID
                , hFHOPEHS_RWKCNT_REWORK_COUNT );

        return( returnOK() );
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
