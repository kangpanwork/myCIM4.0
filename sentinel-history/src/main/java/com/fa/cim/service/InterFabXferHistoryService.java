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
public class InterFabXferHistoryService {

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
     * @return com.fa.cim.dto.Infos.ProcessJobChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/2 17:22
     */
    public Infos.InterFabXferEventRecord getEventData(String id) {
        String sql="Select * from OMEVCFX where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.InterFabXferEventRecord theEventData=new Infos.InterFabXferEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
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

            theEventData.setOpeCategory(convert(sqlData.get("OPE_CATEGORY")));
            theEventData.setOriginalFabID(convert(sqlData.get("ORIG_FAB_ID")));
            theEventData.setOriginalRouteID(convert(sqlData.get("ORIG_PROCESS_ID")));
            theEventData.setOriginalOpeNumber(convert(sqlData.get("ORIG_OPE_NO")));
            theEventData.setDestinationFabID(convert(sqlData.get("DEST_FAB_ID")));
            theEventData.setDestinationRouteID(convert(sqlData.get("DEST_PROCESS_ID")));
            theEventData.setDestinationOpeNumber(convert(sqlData.get("DEST_OPE_NO")));

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
        String sql="SELECT * FROM OMEVCFX_CDA WHERE REFKEY=?";
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
     * @param fhprcdphs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 11:08
     */
    public Response insertAPCProcessDispositionHistory( Infos.Ohprcdphs fhprcdphs_Record ) {
        String hFHPRCDPHSCTRL_JOB          ;
        String hFHPRCDPHSCLAIM_TIME        ;
        String hFHPRCDPHSEQP_ID            ;
        String hFHPRCDPHSEQP_DESCRIPTION   ;
        String hFHPRCDPHSCLAIM_USER_ID     ;
        String hFHPRCDPHSAPC_SYSTEM_NAME   ;
        String hFHPRCDPHSREQ_CATEGORY      ;
        String hFHPRCDPHSCLAIM_MEMO        ;
        String hFHPRCDPHSSTORE_TIME        ;
        String hFHPRCDPHSEVENT_CREATE_TIME ;

        hFHPRCDPHSCTRL_JOB           = fhprcdphs_Record.getCtrl_job          ();
        hFHPRCDPHSCLAIM_TIME         = fhprcdphs_Record.getClaim_time        ();
        hFHPRCDPHSEQP_ID             = fhprcdphs_Record.getEqp_id            ();
        hFHPRCDPHSEQP_DESCRIPTION    = fhprcdphs_Record.getEqp_descripstion  ();
        hFHPRCDPHSCLAIM_USER_ID      = fhprcdphs_Record.getClaim_user_id     ();
        hFHPRCDPHSAPC_SYSTEM_NAME    = fhprcdphs_Record.getApc_system_name   ();
        hFHPRCDPHSREQ_CATEGORY       = fhprcdphs_Record.getReq_category      ();
        hFHPRCDPHSCLAIM_MEMO         = fhprcdphs_Record.getClaim_memo        ();
        hFHPRCDPHSEVENT_CREATE_TIME  = fhprcdphs_Record.getEvent_create_time ();

        baseCore.insert("INSERT INTO FHPRCDPHS\n" +
                "            ( ID, CJ_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    EQP_ID,\n" +
                "                    EQP_DESC,\n" +
                "                    TRX_USER_ID,\n" +
                "                    APC_SYSTEM_NAME,\n" +
                "                    REQ_CATEGORY,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                ?)",generateID(Infos.Ohprcdphs.class)
                ,hFHPRCDPHSCTRL_JOB
                ,convert(hFHPRCDPHSCLAIM_TIME)
                ,hFHPRCDPHSEQP_ID
                ,hFHPRCDPHSEQP_DESCRIPTION
                ,hFHPRCDPHSCLAIM_USER_ID
                ,hFHPRCDPHSAPC_SYSTEM_NAME
                ,hFHPRCDPHSREQ_CATEGORY
                ,hFHPRCDPHSCLAIM_MEMO
                ,convert(hFHPRCDPHSEVENT_CREATE_TIME ));


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhprcdphs_lots_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/3 11:16
     */
    public Response insertAPCProcessDispositionLotsHistory(Infos.Ohprcdphs_lots fhprcdphs_lots_Record) {
        String hFHPRCDPHS_LOTSCTRL_JOB          ;
        String hFHPRCDPHS_LOTSCLAIM_TIME        ;
        String hFHPRCDPHS_LOTSLOT_ID            ;
        String hFHPRCDPHS_LOTSCAST_ID           ;
        String hFHPRCDPHS_LOTSLOT_TYPE          ;
        String hFHPRCDPHS_LOTSSUB_LOT_TYPE      ;
        String hFHPRCDPHS_LOTSPRODSPEC_ID       ;
        String hFHPRCDPHS_LOTSMAINPD_ID         ;
        String hFHPRCDPHS_LOTSOPE_NO            ;
        String hFHPRCDPHS_LOTSPD_ID             ;
        Integer hFHPRCDPHS_LOTSOPE_PASS_COUNT         ;
        String hFHPRCDPHS_LOTSPD_NAME           ;

        hFHPRCDPHS_LOTSCTRL_JOB       = fhprcdphs_lots_Record.getCtrl_job          ();
        hFHPRCDPHS_LOTSCLAIM_TIME     = fhprcdphs_lots_Record.getClaim_time        ();
        hFHPRCDPHS_LOTSLOT_ID         = fhprcdphs_lots_Record.getLot_id            ();
        hFHPRCDPHS_LOTSCAST_ID        = fhprcdphs_lots_Record.getCast_id           ();
        hFHPRCDPHS_LOTSLOT_TYPE       = fhprcdphs_lots_Record.getLot_type          ();
        hFHPRCDPHS_LOTSSUB_LOT_TYPE   = fhprcdphs_lots_Record.getSub_lot_type      ();
        hFHPRCDPHS_LOTSPRODSPEC_ID    = fhprcdphs_lots_Record.getProdspec_id       ();
        hFHPRCDPHS_LOTSMAINPD_ID      = fhprcdphs_lots_Record.getMainpd_id         ();
        hFHPRCDPHS_LOTSOPE_NO         = fhprcdphs_lots_Record.getOpe_no            ();
        hFHPRCDPHS_LOTSPD_ID          = fhprcdphs_lots_Record.getPd_id             ();
        hFHPRCDPHS_LOTSOPE_PASS_COUNT                   = fhprcdphs_lots_Record.getOpe_pass_count();
        hFHPRCDPHS_LOTSPD_NAME        = fhprcdphs_lots_Record.getPd_name           ();

        baseCore.insert("INSERT INTO FHPRCDPHS_LOTS\n" +
                "            ( ID, CJ_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    LOT_ID,\n" +
                "                    CARRIER_ID,\n" +
                "                    LOT_TYPE,\n" +
                "                    SUB_LOT_TYPE,\n" +
                "                    PRODSPEC_ID,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    STEP_ID,\n" +
                "                    OPE_PASS_COUNT,\n" +
                "                    STEP_NAME,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
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
                "            CURRENT_TIMESTAMP          )",generateID(Infos.Ohprcdphs_lots.class)
                ,hFHPRCDPHS_LOTSCTRL_JOB
                ,hFHPRCDPHS_LOTSCLAIM_TIME
                ,hFHPRCDPHS_LOTSLOT_ID
                ,hFHPRCDPHS_LOTSCAST_ID
                ,hFHPRCDPHS_LOTSLOT_TYPE
                ,hFHPRCDPHS_LOTSSUB_LOT_TYPE
                ,hFHPRCDPHS_LOTSPRODSPEC_ID
                ,hFHPRCDPHS_LOTSMAINPD_ID
                ,hFHPRCDPHS_LOTSOPE_NO
                ,hFHPRCDPHS_LOTSPD_ID
                ,hFHPRCDPHS_LOTSOPE_PASS_COUNT
                ,hFHPRCDPHS_LOTSPD_NAME);


        return( returnOK() );
    }

}
