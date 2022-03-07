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
public class VendorLotHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param vendorLotReceiveRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 13:18
     */
    public Response insertVendorLotReceiveHistory(Infos.Ohvltrhs vendorLotReceiveRecord ) {
        String hFHVLTRHS_LOT_ID          ;
        String hFHVLTRHS_LOT_TYPE        ;
        String hFHVLTRHS_VENDOR_LOT_ID   ;
        String hFHVLTRHS_VENDOR_NAME     ;
        String hFHVLTRHS_PROD_TYPE       ;
        String hFHVLTRHS_PRODSPEC_ID     ;
        String hFHVLTRHS_PRODGRP_ID      ;
        String hFHVLTRHS_TECH_ID         ;
        Integer hFHVLTRHS_WAFER_QTY       ;
        String hFHVLTRHS_CLAIM_TIME      ;
        Double hFHVLTRHS_CLAIM_SHOP_DATE ;
        String hFHVLTRHS_CLAIM_USER_ID   ;
        String hFHVLTRHS_CLAIM_MEMO      ;
        String hFHVLTRHS_EVENT_CREATE_TIME ;

        hFHVLTRHS_LOT_ID= vendorLotReceiveRecord.getLot_id        ();
        hFHVLTRHS_LOT_TYPE= vendorLotReceiveRecord.getLot_type      ();
        hFHVLTRHS_VENDOR_LOT_ID= vendorLotReceiveRecord.getVendor_lot_id ();
        hFHVLTRHS_VENDOR_NAME= vendorLotReceiveRecord.getVendor_name   ();
        hFHVLTRHS_PROD_TYPE= vendorLotReceiveRecord.getProd_type     ();
        hFHVLTRHS_PRODSPEC_ID= vendorLotReceiveRecord.getProdspec_id   ();
        hFHVLTRHS_PRODGRP_ID= vendorLotReceiveRecord.getProdgrp_id    ();
        hFHVLTRHS_TECH_ID= vendorLotReceiveRecord.getTech_id       ();
        hFHVLTRHS_WAFER_QTY       = vendorLotReceiveRecord.getWafer_qty      ();
        hFHVLTRHS_CLAIM_TIME= vendorLotReceiveRecord.getClaim_time    ();
        hFHVLTRHS_CLAIM_SHOP_DATE = vendorLotReceiveRecord.getClaim_shop_date();
        hFHVLTRHS_CLAIM_USER_ID= vendorLotReceiveRecord.getClaim_user_id ();
        hFHVLTRHS_CLAIM_MEMO= vendorLotReceiveRecord.getClaim_memo    ();
        hFHVLTRHS_EVENT_CREATE_TIME= vendorLotReceiveRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHMATLRCV\n" +
                "            (   ID,LOT_ID,\n" +
                "                    LOT_TYPE,\n" +
                "                    VENDOR_LOT_ID,\n" +
                "                    VENDOR_NAME,\n" +
                "                    PROD_TYPE,\n" +
                "                    PROD_ID,\n" +
                "                    PRODFMLY_ID,\n" +
                "                    TECH_ID,\n" +
                "                    WAFER_QTY,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    STORE_TIME           )\n" +
                "        Values\n" +
                "                (   ?,?,\n" +
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
                "            CURRENT_TIMESTAMP         )",generateID(Infos.Ohvltrhs.class)
                ,hFHVLTRHS_LOT_ID
                ,hFHVLTRHS_LOT_TYPE
                ,hFHVLTRHS_VENDOR_LOT_ID
                ,hFHVLTRHS_VENDOR_NAME
                ,hFHVLTRHS_PROD_TYPE
                ,hFHVLTRHS_PRODSPEC_ID
                ,hFHVLTRHS_PRODGRP_ID
                ,hFHVLTRHS_TECH_ID
                ,hFHVLTRHS_WAFER_QTY
                ,convert(hFHVLTRHS_CLAIM_TIME)
                ,hFHVLTRHS_CLAIM_SHOP_DATE
                ,hFHVLTRHS_CLAIM_USER_ID
                ,hFHVLTRHS_CLAIM_MEMO
                ,convert(hFHVLTRHS_EVENT_CREATE_TIME));

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
     * @return com.fa.cim.dto.Infos.VendorLotEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/28 13:27
     */
    public Infos.VendorLotEventRecord getEventData(String id) {
        String sql="Select * from OMEVVENDLOT where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.VendorLotEventRecord theEventData=new Infos.VendorLotEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);
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

            theEventData.setVendorLotID(convert(sqlData.get("VENDOR_LOT_ID")));
            theEventData.setClaimQuantity(convertL(sqlData.get("TRX_QTY")));

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
     * @date 2019/6/28 13:31
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVVENDLOT_CDA WHERE REFKEY=?";
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
