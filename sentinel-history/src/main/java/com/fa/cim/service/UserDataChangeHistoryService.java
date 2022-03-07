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
public class UserDataChangeHistoryService {

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
    public Infos.UserDataChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVCDA where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.UserDataChangeEventRecord theEventData=new Infos.UserDataChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.UserDataChangeActionEventData> actions=new ArrayList<>();
        theEventData.setActions(actions);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setClassName(convert(sqlData.get("CDA_CLASS")));
            theEventData.setHashedInfo(convert(sqlData.get("CDA_INFO")));

            sql="SELECT * FROM OMEVCDA_ACT WHERE REFKEY=?";
            List<Map> sqlActions = baseCore.queryAllForMap(sql, id);

            for (Map sqlAction:sqlActions){
                Infos.UserDataChangeActionEventData action=new Infos.UserDataChangeActionEventData();
                actions.add(action);

                action.setName(convert(sqlAction.get("NAME")));
                action.setOrig(convert(sqlAction.get("SOURCE")));
                action.setActionCode(convert(sqlAction.get("TASK_TYPE")));
                action.setFromType(convert(sqlAction.get("OLD_DATA_TYPE")));
                action.setFromValue(convert(sqlAction.get("OLD_VALUE")));
                action.setToType(convert(sqlAction.get("NEW_DATA_TYPE")));
                action.setToValue(convert(sqlAction.get("NEW_VALUE")));
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
        String sql="SELECT * FROM OMEVFMCWRSVLIM_UDATA  WHERE REFKEY=?";
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
     * @param fhudaths_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:02
     */
    public Response  insertUserDataChangeHistory_FHUDATHS( Infos.Ohudaths fhudaths_Record ) {
        String    hFHUDATHSCLASS_NAME                     ;
        String    hFHUDATHSHASHED_INFO                    ;
        String    hFHUDATHSCLAIM_TIME                     ;
        String    hFHUDATHSCLAIM_USER_ID                  ;
        String    hFHUDATHSCLAIM_MEMO                     ;
        String    hFHUDATHSEVENT_CREATE_TIME              ;

        log.info("HistoryWatchDogServer::InsertUserDataChangeHistory_FHUDATHS Function" );

        hFHUDATHSCLASS_NAME              ="";
        hFHUDATHSHASHED_INFO             ="";
        hFHUDATHSCLAIM_TIME              ="";
        hFHUDATHSCLAIM_USER_ID           ="";
        hFHUDATHSCLAIM_MEMO              ="";
        hFHUDATHSEVENT_CREATE_TIME       ="";

        hFHUDATHSCLASS_NAME        = fhudaths_Record.getClassName()       ;
        hFHUDATHSHASHED_INFO       = fhudaths_Record.getHashedInfo()      ;
        hFHUDATHSCLAIM_TIME        = fhudaths_Record.getClaimTime()       ;
        hFHUDATHSCLAIM_USER_ID     = fhudaths_Record.getClaimUser()       ;
        hFHUDATHSCLAIM_MEMO        = fhudaths_Record.getClaimMemo()       ;
        hFHUDATHSEVENT_CREATE_TIME = fhudaths_Record.getEventCreateTime() ;

        baseCore.insert("INSERT INTO OHCDA (ID,\n"+
                        "CDA_CLASS        ,CDA_INFO       ,TRX_TIME        ,TRX_USER_ID     ,\n"+
                        "TRX_MEMO        ,STORE_TIME        ,EVENT_CREATE_TIME )\n"+
                        "Values (?,\n"+
                        "?        ,?       ,?        ,?     ,\n"+
                        "?        ,CURRENT_TIMESTAMP           ,? )",generateID(Infos.Ohudaths.class),
                hFHUDATHSCLASS_NAME,
                hFHUDATHSHASHED_INFO,
                convert(hFHUDATHSCLAIM_TIME),
                hFHUDATHSCLAIM_USER_ID,
                hFHUDATHSCLAIM_MEMO,
                convert(hFHUDATHSEVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertUserDataChangeHistory_FHUDATHS Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param fhudaths_action_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 10:02
     */
    public Response insertUserDataChangeHistory_FHUDATHS_ACTION( Infos.OhudathsAction  fhudaths_action_Record ) {
        String    hFHUDATHS_ACTIONCLASS_NAME            ;
        String    hFHUDATHS_ACTIONHASHED_INFO           ;
        String    hFHUDATHS_ACTIONNAME                  ;
        String    hFHUDATHS_ACTIONORIG                  ;
        String    hFHUDATHS_ACTIONACTION_CODE           ;
        String    hFHUDATHS_ACTIONFROM_TYPE             ;
        String    hFHUDATHS_ACTIONFROM_VALUE            ;
        String    hFHUDATHS_ACTIONTO_TYPE               ;
        String    hFHUDATHS_ACTIONTO_VALUE              ;
        String    hFHUDATHS_ACTIONCLAIM_TIME            ;

        log.info("HistoryWatchDogServer::InsertUserDataChangeHistory_FHUDATHS_ACTION Function" );

        hFHUDATHS_ACTIONCLASS_NAME   ="";
        hFHUDATHS_ACTIONHASHED_INFO  ="";
        hFHUDATHS_ACTIONNAME         ="";
        hFHUDATHS_ACTIONORIG         ="";
        hFHUDATHS_ACTIONACTION_CODE  ="";
        hFHUDATHS_ACTIONFROM_TYPE    ="";
        hFHUDATHS_ACTIONFROM_VALUE   ="";
        hFHUDATHS_ACTIONTO_TYPE      ="";
        hFHUDATHS_ACTIONTO_VALUE     ="";
        hFHUDATHS_ACTIONCLAIM_TIME   ="";

        hFHUDATHS_ACTIONCLASS_NAME  = fhudaths_action_Record.getClassName()  ;
        hFHUDATHS_ACTIONHASHED_INFO = fhudaths_action_Record.getHashedInfo() ;
        hFHUDATHS_ACTIONNAME        = fhudaths_action_Record.getName()       ;
        hFHUDATHS_ACTIONORIG        = fhudaths_action_Record.getOrig()       ;
        hFHUDATHS_ACTIONACTION_CODE = fhudaths_action_Record.getActionCode() ;
        hFHUDATHS_ACTIONFROM_TYPE   = fhudaths_action_Record.getFromType()   ;
        hFHUDATHS_ACTIONFROM_VALUE  = fhudaths_action_Record.getFromValue()  ;
        hFHUDATHS_ACTIONTO_TYPE     = fhudaths_action_Record.getToType()     ;
        hFHUDATHS_ACTIONTO_VALUE    = fhudaths_action_Record.getToValue()    ;
        hFHUDATHS_ACTIONCLAIM_TIME  = fhudaths_action_Record.getClaimTime()  ;

        baseCore.insert("INSERT INTO OHCDA_ACTION (ID,\n"+
                        "CDA_CLASS   ,CDA_INFO  ,NAME         ,SOURCE         ,\n"+
                        "TASK_TYPE  ,OLD_DATA_TYPE    ,OLD_VALUE   ,NEW_DATA_TYPE      ,\n"+
                        "NEW_VALUE     ,TRX_TIME   )\n"+
                        "Values  (?,\n"+
                        "?  ,? ,?        ,\n"+
                        "?        ,? ,?   ,\n"+
                        "?  ,?     ,?    ,\n"+
                        "?  )",generateID(Infos.OhudathsAction.class),
                hFHUDATHS_ACTIONCLASS_NAME,
                hFHUDATHS_ACTIONHASHED_INFO,
                hFHUDATHS_ACTIONNAME,
                hFHUDATHS_ACTIONORIG,
                hFHUDATHS_ACTIONACTION_CODE,
                hFHUDATHS_ACTIONFROM_TYPE,
                hFHUDATHS_ACTIONFROM_VALUE,
                hFHUDATHS_ACTIONTO_TYPE,
                hFHUDATHS_ACTIONTO_VALUE,
                convert(hFHUDATHS_ACTIONCLAIM_TIME));

        log.info("HistoryWatchDogServer::InsertUserDataChangeHistory_FHUDATHS_ACTION Function" );
        return( returnOK() );
    }

}
