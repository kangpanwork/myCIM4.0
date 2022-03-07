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
public class OwnerChangeHistoryService {

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
    public Infos.OwnerChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVOWNERCHG where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.OwnerChangeEventRecord theEventData=new Infos.OwnerChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.OwnerChangeObjectEventData> changeObjects=new ArrayList<>();
        theEventData.setChangeObjects(changeObjects);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setFromOwnerID(convert(sqlData.get("OLD_OWNER_ID")));
            theEventData.setToOwnerID(convert(sqlData.get("NEW_OWNER_ID")));

            sql="SELECT * FROM OMEVOWNERCHG_INFO WHERE REFKEY=?";
            List<Map> sqlChangeObjects = baseCore.queryAllForMap(sql, id);

            for (Map sqlChangeObject:sqlChangeObjects){
                Infos.OwnerChangeObjectEventData changeObject=new Infos.OwnerChangeObjectEventData();
                changeObjects.add(changeObject);

                changeObject.setObjectName(convert(sqlChangeObject.get("CLASS_NAME")));
                changeObject.setHashedInfo(convert(sqlChangeObject.get("CLASS_INFO_SEQ")));
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
        String sql="SELECT * FROM OMEVOWNERCHG_CDA  WHERE REFKEY=?";
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
     * @param fhowchhs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:51
     */
    public Response  insertOwnerChangeHistory_FHOWCHHS( Infos.Ohowchhs fhowchhs_Record ) {
        log.info("HistoryWatchDogServer::InsertOwnerChangeHistory_FHOWCHHS Function" );

        String hFHOWCHHSFROM_OWNER_ID           ="";
        String hFHOWCHHSTO_OWNER_ID             ="";
        String hFHOWCHHSCLAIM_TIME              ="";
        String hFHOWCHHSCLAIM_USER_ID           ="";
        String hFHOWCHHSCLAIM_MEMO              ="";
        String hFHOWCHHSEVENT_CREATE_TIME       ="";

        hFHOWCHHSFROM_OWNER_ID     = fhowchhs_Record.getFromOwnerID()     ;
        hFHOWCHHSTO_OWNER_ID       = fhowchhs_Record.getToOwnerID()       ;
        hFHOWCHHSCLAIM_TIME        = fhowchhs_Record.getClaimTime()       ;
        hFHOWCHHSCLAIM_USER_ID     = fhowchhs_Record.getClaimUser()       ;
        hFHOWCHHSCLAIM_MEMO        = fhowchhs_Record.getClaimMemo()       ;
        hFHOWCHHSEVENT_CREATE_TIME = fhowchhs_Record.getEventCreateTime() ;

        baseCore.insert("INSERT INTO OHOWNERCHG (ID,\n"+
                        "OLD_OWNER_ID     ,NEW_OWNER_ID       ,TRX_TIME        ,TRX_USER_ID     ,\n"+
                        "TRX_MEMO        ,STORE_TIME        ,EVENT_CREATE_TIME )\n"+
                        "Values (?,\n"+
                        "?     ,?       ,?        ,?     ,\n"+
                        "?        ,CURRENT_TIMESTAMP           ,? )",generateID(Infos.Ohowchhs.class),
                hFHOWCHHSFROM_OWNER_ID,
                hFHOWCHHSTO_OWNER_ID,
                convert(hFHOWCHHSCLAIM_TIME),
                hFHOWCHHSCLAIM_USER_ID,
                hFHOWCHHSCLAIM_MEMO,
                convert(hFHOWCHHSEVENT_CREATE_TIME));


        log.info("HistoryWatchDogServer::InsertOwnerChangeHistory_FHOWCHHS Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhowchhs_chgobj_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:53
     */
    public Response insertOwnerChangeHistory_FHOWCHHS_CHGOBJ( Infos.OhowchhsChgobj  fhowchhs_chgobj_Record ) {
        log.info("HistoryWatchDogServer::InsertOwnerChangeHistory_FHOWCHHS_CHGOBJ Function" );

        String hFHOWCHHS_CHGOBJFROM_OWNER_ID="";
        String hFHOWCHHS_CHGOBJTO_OWNER_ID  ="";
        String hFHOWCHHS_CHGOBJOBJECT_NAME  ="";
        String hFHOWCHHS_CHGOBJHASHED_INFO  ="";
        String hFHOWCHHS_CHGOBJCLAIM_TIME   ="";

        hFHOWCHHS_CHGOBJFROM_OWNER_ID= fhowchhs_chgobj_Record.getFromOwnerID() ;
        hFHOWCHHS_CHGOBJTO_OWNER_ID  = fhowchhs_chgobj_Record.getToOwnerID()   ;
        hFHOWCHHS_CHGOBJOBJECT_NAME  = fhowchhs_chgobj_Record.getObjectName()  ;
        hFHOWCHHS_CHGOBJHASHED_INFO  = fhowchhs_chgobj_Record.getHashedInfo()  ;
        hFHOWCHHS_CHGOBJCLAIM_TIME   = fhowchhs_chgobj_Record.getClaimTime()   ;

        baseCore.insert("INSERT INTO OHOWNERCHG_CHGINFO (ID,\n"+
                        "OLD_OWNER_ID   ,NEW_OWNER_ID  ,CLASS_NAME         ,\n"+
                        "CLASS_INFO_SEQ     ,TRX_TIME   )\n"+
                        "Values  (?,\n"+
                        "? ,? ,? ,\n"+
                        "?   ,?  )",generateID(Infos.OhowchhsChgobj.class),
                hFHOWCHHS_CHGOBJFROM_OWNER_ID,
                hFHOWCHHS_CHGOBJTO_OWNER_ID,
                hFHOWCHHS_CHGOBJOBJECT_NAME,
                hFHOWCHHS_CHGOBJHASHED_INFO,
                convert(hFHOWCHHS_CHGOBJCLAIM_TIME));

        log.info("HistoryWatchDogServer::InsertOwnerChangeHistory_FHOWCHHS_CHGOBJ Function" );
        return( returnOK() );
    }

}
