package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
//@Transactional(rollbackFor = Exception.class)
public class ControlJobStatusChangeHistoryService {

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
    public Infos.ControlJobStatusChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVCJSC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.ControlJobStatusChangeEventRecord theEventData=new Infos.ControlJobStatusChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.ControlJobStatusChangeLotEventData> lots=new ArrayList<>();
        theEventData.setLots(lots);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setCtrlJob(convert(sqlData.get("CJ_ID")));
            theEventData.setCtrlJobState(convert(sqlData.get("CJ_STATE")));
            theEventData.setEqpID(convert(sqlData.get("EQP_ID")));
            theEventData.setEqpDescription(convert(sqlData.get("EQP_DESC")));

            sql="SELECT * FROM OMEVCJSC_LOTS WHERE REFKEY=?";
            List<Map> sqlLots = baseCore.queryAllForMap(sql, id);

            for (Map sqlLot:sqlLots){
                Infos.ControlJobStatusChangeLotEventData lot=new Infos.ControlJobStatusChangeLotEventData();
                lots.add(lot);

                lot.setLotID(convert(sqlLot.get("LOT_ID")));
                lot.setCastID(convert(sqlLot.get("CARRIER_ID")));
                lot.setLotType(convert(sqlLot.get("LOT_TYPE")));
                lot.setSubLotType(convert(sqlLot.get("SUB_LOT_TYPE")));
                lot.setProdSpecID(convert(sqlLot.get("PROD_ID")));
                lot.setMainPDID(convert(sqlLot.get("PROCESS_ID")));
                lot.setOpeNo(convert(sqlLot.get("OPE_NO")));
                lot.setPdID(convert(sqlLot.get("STEP_ID")));
                lot.setOpePassCount(convertL(sqlLot.get("OPE_PASS_COUNT")));
                lot.setPdName(convert(sqlLot.get("STEP_NAME")));
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
        String sql="SELECT * FROM OMEVCJSC_CDA  WHERE REFKEY=?";
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

    //@Transactional(rollbackFor = Exception.class)
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
     * @param fhcjschs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 10:59
     */
    public Response  insertControlJobStatusChangeHistory( Infos.Ohcjschs fhcjschs_Record ) {
        String    hFHCJSCHSCTRLJOB_ID        ;
        String    hFHCJSCHSCTRLJOB_STATE     ;
        String    hFHCJSCHSEQP_ID            ;
        String    hFHCJSCHSEQP_DESCRIPTION   ;
        String    hFHCJSCHSCLAIM_TIME        ;
        double  hFHCJSCHSCLAIM_SHOP_DATE;
        String    hFHCJSCHSCLAIM_USER_ID     ;
        String    hFHCJSCHSCLAIM_MEMO        ;
        String    hFHCJSCHSEVENT_CREATE_TIME ;

        log.info("HistoryWatchDogServer::InsertControlJobStatusChangeHistory Function" );

        hFHCJSCHSCTRLJOB_ID       = fhcjschs_Record.getCtrljob_id()        ;
        hFHCJSCHSCTRLJOB_STATE    = fhcjschs_Record.getCtrljob_state()     ;
        hFHCJSCHSEQP_ID           = fhcjschs_Record.getEqp_id()            ;
        hFHCJSCHSEQP_DESCRIPTION  = fhcjschs_Record.getEqp_descripstion()  ;
        hFHCJSCHSCLAIM_TIME       = fhcjschs_Record.getClaim_time()        ;
        hFHCJSCHSCLAIM_SHOP_DATE  = fhcjschs_Record.getClaim_shop_date();
        hFHCJSCHSCLAIM_USER_ID    = fhcjschs_Record.getClaim_user_id()     ;
        hFHCJSCHSCLAIM_MEMO       = fhcjschs_Record.getClaim_memo()        ;
        hFHCJSCHSEVENT_CREATE_TIME= fhcjschs_Record.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHCJSC\n"+
                        "(ID,  CJ_ID,\n"+
                        "CJ_STATE,\n"+
                        "EQP_ID,\n"+
                        "EQP_DESC,\n"+
                        "TRX_TIME,\n"+
                        "TRX_WORK_DATE,\n"+
                        "TRX_USER_ID,\n"+
                        "TRX_MEMO,\n"+
                        "STORE_TIME,\n"+
                        "EVENT_CREATE_TIME )\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP,\n"+
                        "? )",generateID(Infos.Ohcjschs.class),
                hFHCJSCHSCTRLJOB_ID,
                hFHCJSCHSCTRLJOB_STATE,
                hFHCJSCHSEQP_ID,
                hFHCJSCHSEQP_DESCRIPTION,
                convert(hFHCJSCHSCLAIM_TIME),
                hFHCJSCHSCLAIM_SHOP_DATE,
                hFHCJSCHSCLAIM_USER_ID,
                hFHCJSCHSCLAIM_MEMO,
                convert(hFHCJSCHSEVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertControlJobStatusChangeHistory Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhcjschs_lots_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/26 11:11
     */
    public Response  insertControlJobStatusChangeLotsHistory(Infos.OhcjschsLots fhcjschs_lots_Record) {
        String  hFHCJSCHS_LOTSCTRLJOB_ID         ;
        String  hFHCJSCHS_LOTSLOT_ID             ;
        String  hFHCJSCHS_LOTSCAST_ID            ;
        String  hFHCJSCHS_LOTSLOT_TYPE           ;
        String  hFHCJSCHS_LOTSSUB_LOT_TYPE       ;
        String  hFHCJSCHS_LOTSPRODSPEC_ID        ;
        String  hFHCJSCHS_LOTSMAINPD_ID          ;
        String  hFHCJSCHS_LOTSOPE_NO             ;
        String  hFHCJSCHS_LOTSPD_ID              ;

        Integer hFHCJSCHS_LOTSOPE_PASS_COUNT         ;
        String  hFHCJSCHS_LOTSPD_NAME            ;
        String  hFHCJSCHS_LOTSCLAIM_TIME         ;

        log.info("HistoryWatchDogServer::InsertControlJobStatusChangeLotsHistory Function" );

        hFHCJSCHS_LOTSCTRLJOB_ID     = fhcjschs_lots_Record.getCtrljob_id()        ;
        hFHCJSCHS_LOTSLOT_ID         = fhcjschs_lots_Record.getLot_id()            ;
        hFHCJSCHS_LOTSCAST_ID        = fhcjschs_lots_Record.getCast_id()           ;
        hFHCJSCHS_LOTSLOT_TYPE       = fhcjschs_lots_Record.getLot_type()          ;
        hFHCJSCHS_LOTSSUB_LOT_TYPE   = fhcjschs_lots_Record.getSub_lot_type()      ;
        hFHCJSCHS_LOTSPRODSPEC_ID    = fhcjschs_lots_Record.getProdspec_id()       ;
        hFHCJSCHS_LOTSMAINPD_ID      = fhcjschs_lots_Record.getMainpd_id()         ;
        hFHCJSCHS_LOTSOPE_NO         = fhcjschs_lots_Record.getOpe_no()            ;
        hFHCJSCHS_LOTSPD_ID          = fhcjschs_lots_Record.getPd_id()             ;
        hFHCJSCHS_LOTSOPE_PASS_COUNT = fhcjschs_lots_Record.getOpe_pass_count();
        hFHCJSCHS_LOTSPD_NAME        = fhcjschs_lots_Record.getPd_name()           ;
        hFHCJSCHS_LOTSCLAIM_TIME     = fhcjschs_lots_Record.getClaim_time()        ;

        baseCore.insert("INSERT INTO OHCJSC_LOTS\n"+
                        "(ID,  CJ_ID,\n"+
                        "LOT_ID,\n"+
                        "CARRIER_ID,\n"+
                        "LOT_TYPE,\n"+
                        "SUB_LOT_TYPE,\n"+
                        "PROD_ID,\n"+
                        "PROCESS_ID,\n"+
                        "OPE_NO,\n"+
                        "STEP_ID,\n"+
                        "OPE_PASS_COUNT,\n"+
                        "STEP_NAME,\n"+
                        "TRX_TIME,\n"+
                        "STORE_TIME )\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP          )",generateID(Infos.OhcjschsLots.class),
                hFHCJSCHS_LOTSCTRLJOB_ID,
                hFHCJSCHS_LOTSLOT_ID,
                hFHCJSCHS_LOTSCAST_ID,
                hFHCJSCHS_LOTSLOT_TYPE,
                hFHCJSCHS_LOTSSUB_LOT_TYPE,
                hFHCJSCHS_LOTSPRODSPEC_ID,
                hFHCJSCHS_LOTSMAINPD_ID,
                hFHCJSCHS_LOTSOPE_NO,
                hFHCJSCHS_LOTSPD_ID,
                hFHCJSCHS_LOTSOPE_PASS_COUNT,
                hFHCJSCHS_LOTSPD_NAME,
                convert(hFHCJSCHS_LOTSCLAIM_TIME));

        log.info("HistoryWatchDogServer::InsertControlJobStatusChangeLotsHistory Function" );
        return( returnOK() );
    }

}
