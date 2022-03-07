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
//@Transactional(rollbackFor = Exception.class)
public class EqpMonUsedCountHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationEMUcntRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 15:47
     */
    public Response insertLotOperationEqpMonUsedCntHistory( Infos.Ohopehs_emucnt lotOperationEMUcntRecord ) {
        String hFHOPEHS_EQPMONCNT_LOT_ID         ;
        String hFHOPEHS_EQPMONCNT_MAINPD_ID      ;
        String hFHOPEHS_EQPMONCNT_OPE_NO         ;
        Integer hFHOPEHS_EQPMONCNT_OPE_PASS_COUNT;
        String hFHOPEHS_EQPMONCNT_CLAIM_TIME     ;
        String hFHOPEHS_EQPMONCNT_OPE_CATEGORY   ;
        String hFHOPEHS_EQPMONCNT_WAFER_ID       ;
        Integer hFHOPEHS_EQPMONCNT_EQPMON_USED;

        hFHOPEHS_EQPMONCNT_LOT_ID = lotOperationEMUcntRecord.getLot_id         ();
        hFHOPEHS_EQPMONCNT_MAINPD_ID = lotOperationEMUcntRecord.getMainpd_id      ();
        hFHOPEHS_EQPMONCNT_OPE_NO = lotOperationEMUcntRecord.getOpe_no         ();
        hFHOPEHS_EQPMONCNT_OPE_PASS_COUNT  = lotOperationEMUcntRecord.getOpe_pass_count ();
        hFHOPEHS_EQPMONCNT_CLAIM_TIME = lotOperationEMUcntRecord.getClaim_time     ();
        hFHOPEHS_EQPMONCNT_OPE_CATEGORY = lotOperationEMUcntRecord.getOpe_category   ();
        hFHOPEHS_EQPMONCNT_WAFER_ID = lotOperationEMUcntRecord.getWafer_id       ();
        hFHOPEHS_EQPMONCNT_EQPMON_USED     = lotOperationEMUcntRecord.getEqpmonused_count   ();

        baseCore.insert("INSERT INTO OHLOTOPE_AMCNT\n" +
                "            ( ID,LOT_ID,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    OPE_PASS_COUNT,\n" +
                "                    TRX_TIME,\n" +
                "                    OPE_CATEGORY,\n" +
                "                    WAFER_ID,\n" +
                "                    AM_USED_COUNT )\n" +
                "        VALUES\n" +
                "                ( ?,?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?,\n" +
                "               ?)",generateID(Infos.Ohopehs_emucnt.class)
                ,hFHOPEHS_EQPMONCNT_LOT_ID
                ,hFHOPEHS_EQPMONCNT_MAINPD_ID
                ,hFHOPEHS_EQPMONCNT_OPE_NO
                ,hFHOPEHS_EQPMONCNT_OPE_PASS_COUNT
                ,convert(hFHOPEHS_EQPMONCNT_CLAIM_TIME)
                ,hFHOPEHS_EQPMONCNT_OPE_CATEGORY
                ,hFHOPEHS_EQPMONCNT_WAFER_ID
                ,hFHOPEHS_EQPMONCNT_EQPMON_USED );

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
     * @return com.fa.cim.dto.Infos.EqpMonitorCountEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/1 15:52
     */
    public Infos.EqpMonitorCountEventRecord getEventData(String id) {
        String sql="Select * from OMEVAMONCNT where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EqpMonitorCountEventRecord theEventData=new Infos.EqpMonitorCountEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.EqpMonitorWaferCountEventData> wafers=new ArrayList<>();
        theEventData.setWafers(wafers);
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

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));

            sql="SELECT * FROM OMEVAMONCNT_WFR WHERE REFKEY=?";
            List<Map> sqlWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlWafer:sqlWafers){
                Infos.EqpMonitorWaferCountEventData wafer=new Infos.EqpMonitorWaferCountEventData();
                wafers.add(wafer);

                wafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                wafer.setEqpMonitorUsedCount(convertL(sqlWafer.get("AMON_USED_COUNT")));
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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVAMONCNT_CDA WHERE REFKEY=?";
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
