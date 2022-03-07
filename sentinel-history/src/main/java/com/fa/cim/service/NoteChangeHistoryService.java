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
public class NoteChangeHistoryService {

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
    public Infos.NoteChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVMEMO where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.NoteChangeEventRecord theEventData=new Infos.NoteChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setObjectID(convert(sqlData.get("MEMO_OBJ_ID")));
            theEventData.setNoteType(convert(sqlData.get("MEMO_TYPE")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOperationID(convert(sqlData.get("STEP_ID")));
            theEventData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            theEventData.setNoteTitle(convert(sqlData.get("MEMO_HEADER")));
            theEventData.setNoteContents(convert(sqlData.get("MEMO_CONTENTS")));
            theEventData.setOwnerID(convert(sqlData.get("OWNER_ID")));

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
        String sql="SELECT * FROM OMEVMEMO_CDA WHERE REFKEY=?";
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
     * @param noteRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/11 10:43
     */
    public Response insertNoteChangeHistory ( Infos.Ohnotehs noteRecord ) {
        String hFHNOTEHSOBJECT_ID        ="";
        String hFHNOTEHSNOTE_TYPE        ="";
        String hFHNOTEHSACTION           ="";
        String hFHNOTEHSMAINPD_ID        ="";
        String hFHNOTEHSPD_ID            ="";
        String hFHNOTEHSOPE_NO           ="";
        String hFHNOTEHSNOTE_TITLE       ="";
        String hFHNOTEHSNOTE_CONTENTS    ="";
        String hFHNOTEHSOWNER_ID         ="";
        String hFHNOTEHSCLAIM_USER_ID    ="";
        String hFHNOTEHSCLAIM_MEMO       ="";
        String hFHNOTEHSCLAIM_TIME       ="";
        Double hFHNOTEHSCLAIM_SHOP_DATE = 0.0;
        String hFHNOTEHSSTORE_TIME       ="";
        String hFHNOTEHSEVENT_CREATE_TIME="";

        hFHNOTEHSOBJECT_ID         = noteRecord.getObject_id         ();
        hFHNOTEHSNOTE_TYPE         = noteRecord.getNote_type         ();
        hFHNOTEHSACTION            = noteRecord.getAction            ();
        hFHNOTEHSMAINPD_ID         = noteRecord.getMainpd_id         ();
        hFHNOTEHSPD_ID             = noteRecord.getPd_id             ();
        hFHNOTEHSOPE_NO            = noteRecord.getOpe_no            ();
        hFHNOTEHSNOTE_TITLE        = noteRecord.getNote_title        ();
        hFHNOTEHSNOTE_CONTENTS     = noteRecord.getNote_contents     ();
        hFHNOTEHSOWNER_ID          = noteRecord.getOwner_id          ();
        hFHNOTEHSCLAIM_USER_ID     = noteRecord.getClaim_user_id     ();
        hFHNOTEHSCLAIM_MEMO        = noteRecord.getClaim_memo        ();
        hFHNOTEHSCLAIM_TIME        = noteRecord.getClaim_time        ();
        hFHNOTEHSCLAIM_SHOP_DATE  = noteRecord.getClaim_shop_date();
        hFHNOTEHSEVENT_CREATE_TIME = noteRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHMEMO (  ID, MEMO_OBJ_ID      ,\n" +
                "            MEMO_TYPE      ,\n" +
                "            TASK_TYPE         ,\n" +
                "            PROCESS_ID      ,\n" +
                "            STEP_ID          ,\n" +
                "            OPE_NO         ,\n" +
                "            MEMO_HEADER     ,\n" +
                "            MEMO_CONTENTS  ,\n" +
                "            OWNER_ID       ,\n" +
                "            TRX_USER_ID  ,\n" +
                "            TRX_MEMO     ,\n" +
                "            TRX_TIME     ,\n" +
                "            STORE_TIME     ,\n" +
                "            EVENT_CREATE_TIME )\n" +
                "        VALUES (?, ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     ?,\n" +
                "                                     CURRENT_TIMESTAMP )",generateID(Infos.Ohnotehs.class)
                ,hFHNOTEHSOBJECT_ID
                ,hFHNOTEHSNOTE_TYPE
                ,hFHNOTEHSACTION
                ,hFHNOTEHSMAINPD_ID
                ,hFHNOTEHSPD_ID
                ,hFHNOTEHSOPE_NO
                ,hFHNOTEHSNOTE_TITLE
                ,hFHNOTEHSNOTE_CONTENTS
                ,hFHNOTEHSOWNER_ID
                ,hFHNOTEHSCLAIM_USER_ID
                ,hFHNOTEHSCLAIM_MEMO
                ,convert(hFHNOTEHSCLAIM_TIME)
                ,convert(hFHNOTEHSEVENT_CREATE_TIME ));

        return( returnOK() );
    }


}
