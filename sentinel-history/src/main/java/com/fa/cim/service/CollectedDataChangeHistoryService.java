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
@Transactional(rollbackFor = Exception.class)
public class CollectedDataChangeHistoryService {

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
    public Infos.CollectedDataChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVCHGEDC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.CollectedDataChangeEventRecord theEventData=new Infos.CollectedDataChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.ChangedDCData> changedDCDataSeq=new ArrayList<>();
        theEventData.setChangedDCDataSeq(changedDCDataSeq);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));
            theEventData.setDataCollectionDefinitionID(convert(sqlData.get("EDC_PLAN_ID")));

            sql="SELECT * FROM OMEVCHGEDC_ITEM WHERE REFKEY=?";
            List<Map> sqlChangedDCDataSeq = baseCore.queryAllForMap(sql, id);

            for (Map sqlChangedDCData:sqlChangedDCDataSeq){
                Infos.ChangedDCData changedDCData=new Infos.ChangedDCData();
                changedDCDataSeq.add(changedDCData);

                changedDCData.setDataCollectionItemName(convert(sqlChangedDCData.get("EDC_ITEM_NAME")));
                changedDCData.setPreviousDataValue(convert(sqlChangedDCData.get("PRE_DATA_VAL")));
                changedDCData.setCurrentDataValue(convert(sqlChangedDCData.get("CUR_DATA_VAL")));
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
        String sql="SELECT * FROM OMEVCHGEDC_CDA  WHERE REFKEY=?";
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
     * @param fhcdchghs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 18:02
     */
    public Response  insertCollectedDataChangeHistory_FHCDCHGHS( Infos.Ohcdchghs fhcdchghs_Record ) {
        String    hFHCDCHGHSLOT_ID             ;
        String    hFHCDCHGHSCTRLJOB_ID         ;
        String    hFHCDCHGHSDCDEF_ID            ;
        String    hFHCDCHGHSDCITEM_NAME        ;
        String    hFHCDCHGHSPRE_DATA_VAL       ;
        String    hFHCDCHGHSCUR_DATA_VAL       ;
        String    hFHCDCHGHSCLAIM_TIME         ;
        String    hFHCDCHGHSCLAIM_USER_ID      ;
        String    hFHCDCHGHSCLAIM_MEMO         ;
        String    hFHCDCHGHSEVENT_CREATE_TIME  ;

        log.info("HistoryWatchDogServer::InsertCollectedDataChangeHistory_FHCDCHGHS Function" );

        hFHCDCHGHSLOT_ID                  ="";
        hFHCDCHGHSCTRLJOB_ID              ="";
        hFHCDCHGHSDCDEF_ID                ="";
        hFHCDCHGHSDCITEM_NAME             ="";
        hFHCDCHGHSPRE_DATA_VAL            ="";
        hFHCDCHGHSCUR_DATA_VAL            ="";
        hFHCDCHGHSCLAIM_TIME              ="";
        hFHCDCHGHSCLAIM_USER_ID           ="";
        hFHCDCHGHSCLAIM_MEMO              ="";
        hFHCDCHGHSEVENT_CREATE_TIME       ="";

        hFHCDCHGHSLOT_ID            = fhcdchghs_Record.getLotID()             ;
        hFHCDCHGHSCTRLJOB_ID        = fhcdchghs_Record.getCtrljob_id()        ;
        hFHCDCHGHSDCDEF_ID          = fhcdchghs_Record.getDcdef_id()          ;
        hFHCDCHGHSDCITEM_NAME       = fhcdchghs_Record.getDcitem_name()       ;
        hFHCDCHGHSPRE_DATA_VAL      = fhcdchghs_Record.getPre_dcitem_value()  ;
        hFHCDCHGHSCUR_DATA_VAL      = fhcdchghs_Record.getCur_dcitem_value()  ;
        hFHCDCHGHSCLAIM_TIME        = fhcdchghs_Record.getClaimTime()         ;
        hFHCDCHGHSCLAIM_USER_ID     = fhcdchghs_Record.getClaimUser()         ;
        hFHCDCHGHSCLAIM_MEMO        = fhcdchghs_Record.getClaimMemo()         ;
        hFHCDCHGHSEVENT_CREATE_TIME = fhcdchghs_Record.getEventCreateTime()   ;

        baseCore.insert("INSERT INTO OHEDCCHG (ID,\n"+
                        "LOT_ID        ,CJ_ID       ,EDC_PLAN_ID    ,EDC_ITEM_NAME    ,PRE_DATA_VAL   ,CUR_DATA_VAL   ,\n"+
                        "TRX_TIME    ,TRX_USER_ID    ,TRX_MEMO  ,STORE_TIME     ,EVENT_CREATE_TIME )\n"+
                        "Values (?,\n"+
                        "?       ,?      ,?    ,? ,?    ,? ,\n"+
                        "?   ,?   ,?  ,CURRENT_TIMESTAMP      ,? )",generateID(Infos.Ohcdchghs.class),
                hFHCDCHGHSLOT_ID,
                hFHCDCHGHSCTRLJOB_ID,
                hFHCDCHGHSDCDEF_ID,
                hFHCDCHGHSDCITEM_NAME,
                hFHCDCHGHSPRE_DATA_VAL,
                hFHCDCHGHSCUR_DATA_VAL,
                convert(hFHCDCHGHSCLAIM_TIME),
                hFHCDCHGHSCLAIM_USER_ID,
                hFHCDCHGHSCLAIM_MEMO,
                convert(hFHCDCHGHSEVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertCollectedDataChangeHistory_FHCDCHGHS Function" );
        return( returnOK() );
    }

}
