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
public class DurableBankMoveHistoryService {

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
     * @return com.fa.cim.dto.Infos.ChamberStatusChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/1 10:38
     */
    public Infos.DurableBankMoveEventRecord getEventData(String id) {
        String sql="Select * from OMEVDURBNKMOV where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.DurableBankMoveEventRecord theEventData=new Infos.DurableBankMoveEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        Infos.DurableEventData durableData=new Infos.DurableEventData();
        theEventData.setDurableData(durableData);
        for (Map<String,Object> sqlData:sqlDatas) {
            durableData.setDurableID(convert(sqlData.get("DRBL_ID")));
            durableData.setDurableCategory(convert(sqlData.get("DRBL_CATEGORY")));
            durableData.setDurableStatus(convert(sqlData.get("DRBL_STATUS")));
            durableData.setHoldState(convert(sqlData.get("DRBL_HOLD_STATE")));
            durableData.setBankID(convert(sqlData.get("BANK_ID")));
            durableData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            durableData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            durableData.setOperationID(convert(sqlData.get("STEP_ID")));
            durableData.setOperationPassCount(convertL(sqlData.get("PASS_COUNT")));
            durableData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            durableData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            durableData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));
            durableData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));

            theEventData.setPreviousBankID(convert(sqlData.get("PREV_BANK_ID")));

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
     * @date 2019/7/1 10:38
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVDURBNKMOV_CDA WHERE REFKEY=?";
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
     * @param fhdcjschs_record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/12 11:22
     */
    public Response insertDRBLCJStatusChangeHistory_FHDCJSCHS( Infos.Ohdcjschs fhdcjschs_record ) {
        String hDCTRLJOB_ID         ;
        String hDCTRLJOB_STATE      ;
        String hDRBL_CATEGORY       ;
        String hEQP_ID              ;
        String hEQP_DESCRIPTION     ;
        String hCLAIM_TIME          ;
        Double  hCLAIM_SHOP_DATE;
        String hCLAIM_USER_ID       ;
        String hCLAIM_MEMO          ;
        String hEVENT_CREATE_TIME   ;

        hDCTRLJOB_ID       = "";
        hDCTRLJOB_STATE    = "";
        hDRBL_CATEGORY     = "";
        hEQP_ID            = "";
        hEQP_DESCRIPTION   = "";
        hCLAIM_TIME        = "";
        hCLAIM_USER_ID     = "";
        hCLAIM_MEMO        = "";
        hEVENT_CREATE_TIME = "";

        hDCTRLJOB_ID        = fhdcjschs_record.getDctrljob_id          ();
        hDCTRLJOB_STATE     = fhdcjschs_record.getDctrljob_state       ();
        hDRBL_CATEGORY      = fhdcjschs_record.getDrbl_category        ();
        hEQP_ID             = fhdcjschs_record.getEqp_id               ();
        hEQP_DESCRIPTION    = fhdcjschs_record.getEqp_description      ();
        hCLAIM_TIME         = fhdcjschs_record.getClaim_time           ();
        hCLAIM_SHOP_DATE    = fhdcjschs_record.getClaim_shop_date();
        hCLAIM_USER_ID      = fhdcjschs_record.getClaim_user_id        ();
        hCLAIM_MEMO         = fhdcjschs_record.getClaim_memo           ();
        hEVENT_CREATE_TIME  = fhdcjschs_record.getEvent_create_time    ();

        baseCore.insert("INSERT INTO OHDCJSC\n" +
                "            ( ID,DCJ_ID,\n" +
                "                    DCJ_STATE,\n" +
                "                    DRBL_CATEGORY,\n" +
                "                    EQP_ID,\n" +
                "                    EQP_DESC,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME )\n" +
                "        Values\n" +
                "                (?, ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                    ?)",generateID(Infos.Ohdcjschs.class)
                ,hDCTRLJOB_ID
                ,hDCTRLJOB_STATE
                ,hDRBL_CATEGORY
                ,hEQP_ID
                ,hEQP_DESCRIPTION
                ,convert(hCLAIM_TIME)
                ,hCLAIM_USER_ID
                ,hCLAIM_MEMO
                ,convert(hEVENT_CREATE_TIME ));

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhdcjschs_drbl_record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/12 11:29
     */
    public Response insertDRBLCJStatusChangeHistory_FHDCJSCHS_DRBL( Infos.Ohdcjschs_drbl fhdcjschs_drbl_record ) {
        String hDCTRLJOBL_ID_DRBL   ;
        String hDURABLE_ID          ;
        String hJSDRBL_CATEGORY     ;
        String hPRODSPEC_ID         ;
        String hMAINPD_ID           ;
        String hOPE_NO              ;
        String hPD_ID               ;
        Long   hOPE_PASS_COUNT;
        String hPD_NAME             ;
        String hCLAIM_DRBL_TIME     ;

        hDCTRLJOBL_ID_DRBL   = "";
        hDURABLE_ID          = "";
        hJSDRBL_CATEGORY     = "";
        hPRODSPEC_ID         = "";
        hMAINPD_ID           = "";
        hOPE_NO              = "";
        hPD_ID               = "";
        hPD_NAME             = "";
        hCLAIM_DRBL_TIME     = "";

        hDCTRLJOBL_ID_DRBL   = fhdcjschs_drbl_record.getDctrljob_id    ();
        hDURABLE_ID          = fhdcjschs_drbl_record.getDurable_id     ();
        hJSDRBL_CATEGORY     = fhdcjschs_drbl_record.getDrbl_category  ();
        hPRODSPEC_ID         = fhdcjschs_drbl_record.getProdspec_id    ();
        hMAINPD_ID           = fhdcjschs_drbl_record.getMainpd_id      ();
        hOPE_NO              = fhdcjschs_drbl_record.getOpe_no         ();
        hPD_ID               = fhdcjschs_drbl_record.getPd_id          ();
        hOPE_PASS_COUNT      = fhdcjschs_drbl_record.getOpe_pass_count();
        hPD_NAME             = fhdcjschs_drbl_record.getPd_name        ();
        hCLAIM_DRBL_TIME     = fhdcjschs_drbl_record.getClaim_time     ();

        baseCore.insert("INSERT INTO OHDCJSC_DRBLS\n" +
                "            (ID, DCJ_ID,\n" +
                "                    DURABLE_ID,\n" +
                "                    DRBL_CATEGORY,\n" +
                "                    PROD_ID,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    STEP_ID,\n" +
                "                    OPE_PASS_COUNT,\n" +
                "                    STEP_NAME,\n" +
                "                    TRX_TIME,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                (?, ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "            CURRENT_TIMESTAMP)",generateID(Infos.Ohdcjschs_drbl.class)
                ,hDCTRLJOBL_ID_DRBL
                ,hDURABLE_ID
                ,hJSDRBL_CATEGORY
                ,hPRODSPEC_ID
                ,hMAINPD_ID
                ,hOPE_NO
                ,hPD_ID
                ,hOPE_PASS_COUNT
                ,hPD_NAME
                ,convert(hCLAIM_DRBL_TIME));

        return( returnOK() );
    }

}
