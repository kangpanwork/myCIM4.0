package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class APCInterfaceHistoryService {

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
                ,convert(hFHCSCHS_END_TIME)
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
    public Infos.APCInterfaceEventRecord getEventData(String id) {
        String sql="Select * from FREVAPCIF where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.APCInterfaceEventRecord theEventData=new Infos.APCInterfaceEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setAPC_systemName(convert(sqlData.get("APC_SYSTEM_NAME")));
            theEventData.setStoreTime(convert(sqlData.get("STORE_TIME")));
            theEventData.setOperationCategory(convert(sqlData.get("OPE_CATEGORY")));
            theEventData.setEquipmentDescription(convert(sqlData.get("EQP_DESC")));
            theEventData.setIgnoreAbleFlag(convertB(sqlData.get("IGNORE_ABLE")));
            theEventData.setAPC_responsibleUserID(convert(sqlData.get("APC_REP1_USER_ID")));
            theEventData.setAPC_subResponsibleUserID(convert(sqlData.get("APC_REP2_USER_ID")));
            theEventData.setAPC_configState(convert(sqlData.get("APC_CONFIG_STATE")));
            theEventData.setAPC_registeredUserID(convert(sqlData.get("REGISTERED_USER_ID")));
            theEventData.setRegisteredTime(convert(sqlData.get("REIGSTERED_TIME")));
            theEventData.setRegisteredMemo(convert(sqlData.get("REGISTERED_MEMO")));
            theEventData.setApprovedUserID(convert(sqlData.get("APPROVED_USER_ID")));
            theEventData.setApprovedTime(convert(sqlData.get("APPROVED_TIME")));
            theEventData.setApprovedMemo(convert(sqlData.get("APPROVED_MEMO")));

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
        String sql="SELECT * FROM OMEVAPCIF_CDA  WHERE REFKEY=?";
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
     * @param APCInterfaceEventRecord_FHAPCIFHS
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 13:29
     */
    public Response insertAPCInterfaceEventRecord_FHAPCIFHS(Infos.Ohapcifhs APCInterfaceEventRecord_FHAPCIFHS) {
        String  hFHAPCIFHSEQP_ID               ;
        String  hFHAPCIFHSAPC_SYSTEM_NAME      ;

        String  hFHAPCIFHSOPE_CATEGORY         ;
        String  hFHAPCIFHSEQP_DESCRIPTION     ;

        Integer hFHAPCIFHSIGNOREABLE               ;
        String  hFHAPCIFHSAPC_REP1_USER_ID     ;
        String  hFHAPCIFHSAPC_REP2_USER_ID     ;
        String  hFHAPCIFHSAPC_CONFIG_STATE     ;
        String  hFHAPCIFHSREGISTERED_USER_ID   ;
        String  hFHAPCIFHSREGISTERED_TIME      ;
        String  hFHAPCIFHSREGISTERED_MEMO     ;
        String  hFHAPCIFHSAPPROVED_USER_ID     ;
        String  hFHAPCIFHSAPPROVED_TIME        ;
        String  hFHAPCIFHSAPPROVED_MEMO        ;
        String  hFHAPCIFHS_EVENT_CREATE_TIME  ;

        log.info("HistoryWatchDogServer::InsertAPCInterfaceEventRecord_FHAPCIFHS Function" );

        hFHAPCIFHSEQP_ID=             APCInterfaceEventRecord_FHAPCIFHS.getEquipmentID()              ;
        hFHAPCIFHSAPC_SYSTEM_NAME=    APCInterfaceEventRecord_FHAPCIFHS.getAPC_systemName()           ;
        hFHAPCIFHSOPE_CATEGORY=       APCInterfaceEventRecord_FHAPCIFHS.getOperationCategory()        ;
        hFHAPCIFHSEQP_DESCRIPTION=    APCInterfaceEventRecord_FHAPCIFHS.getEquipmentDescription()     ;
        hFHAPCIFHSIGNOREABLE        = APCInterfaceEventRecord_FHAPCIFHS.getIgnoreAbleFlag()            ;
        hFHAPCIFHSAPC_REP1_USER_ID=   APCInterfaceEventRecord_FHAPCIFHS.getAPC_responsibleUserID()    ;
        hFHAPCIFHSAPC_REP2_USER_ID=   APCInterfaceEventRecord_FHAPCIFHS.getAPC_subResponsibleUserID() ;
        hFHAPCIFHSAPC_CONFIG_STATE=   APCInterfaceEventRecord_FHAPCIFHS.getAPC_configState()          ;
        hFHAPCIFHSREGISTERED_USER_ID= APCInterfaceEventRecord_FHAPCIFHS.getAPC_registeredUserID()     ;
        hFHAPCIFHSREGISTERED_TIME=    APCInterfaceEventRecord_FHAPCIFHS.getRegisteredTime()           ;
        hFHAPCIFHSREGISTERED_MEMO=    APCInterfaceEventRecord_FHAPCIFHS.getRegisteredMemo()           ;
        hFHAPCIFHSAPPROVED_USER_ID=   APCInterfaceEventRecord_FHAPCIFHS.getApprovedUserID()           ;
        hFHAPCIFHSAPPROVED_TIME=      APCInterfaceEventRecord_FHAPCIFHS.getApprovedTime()             ;
        hFHAPCIFHSAPPROVED_MEMO=      APCInterfaceEventRecord_FHAPCIFHS.getApprovedMemo()             ;
        hFHAPCIFHS_EVENT_CREATE_TIME= APCInterfaceEventRecord_FHAPCIFHS.getEventCreationTimeStamp()   ;

        baseCore.insert("INSERT INTO FHAPCIFHS\n"+
                        "(ID,  EQP_ID,\n"+
                        "APC_SYSTEM_NAME,\n"+
                        "STORE_TIME,\n"+
                        "OPE_CATEGORY,\n"+
                        "EQP_DESC,\n"+
                        "IGNOREABLE,\n"+
                        "APC_REP1_USER_ID,\n"+
                        "APC_REP2_USER_ID,\n"+
                        "APC_CONFIG_STATE,\n"+
                        "REGISTERED_USER_ID,\n"+
                        "REGISTERED_TIME,\n"+
                        "REGISTERED_MEMO,\n"+
                        "APPROVED_USER_ID,\n"+
                        "APPROVED_TIME,\n"+
                        "APPROVED_MEMO,\n"+
                        "EVENT_CREATE_TIME )\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP,\n"+
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
                        "?,\n"+
                        "? )",generateID(Infos.Ohapcifhs.class),
                hFHAPCIFHSEQP_ID,
                hFHAPCIFHSAPC_SYSTEM_NAME,
                hFHAPCIFHSOPE_CATEGORY,
                hFHAPCIFHSEQP_DESCRIPTION,
                hFHAPCIFHSIGNOREABLE,
                hFHAPCIFHSAPC_REP1_USER_ID,
                hFHAPCIFHSAPC_REP2_USER_ID,
                hFHAPCIFHSAPC_CONFIG_STATE,
                hFHAPCIFHSREGISTERED_USER_ID,
                convert(hFHAPCIFHSREGISTERED_TIME),
                hFHAPCIFHSREGISTERED_MEMO,
                hFHAPCIFHSAPPROVED_USER_ID,
                convert(hFHAPCIFHSAPPROVED_TIME),
                hFHAPCIFHSAPPROVED_MEMO,
                convert(hFHAPCIFHS_EVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertAPCInterfaceEventRecord_FHAPCIFHS Function" );
        return( returnOK() );
    }

}
