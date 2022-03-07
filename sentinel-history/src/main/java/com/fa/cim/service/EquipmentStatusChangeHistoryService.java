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
public class EquipmentStatusChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentStatusChangeRecord
     * @return org.springframework.stereotype.Repository
     * @exception
     * @author Ho
     * @date 2019/6/27 13:22
     */
    @Transactional(rollbackFor = Exception.class)
    public Response insertEquipmentStatusChangeHistory(Infos.Oheschs equipmentStatusChangeRecord ) {
        String hFHESCHS_EQP_ID                  ;
        String hFHESCHS_EQP_NAME                ;
        String hFHESCHS_STK_ID                  ;
        String hFHESCHS_STK_NAME                ;
        String hFHESCHS_AREA_ID                 ;
        String hFHESCHS_EQP_STATE               ;
        String hFHESCHS_OPE_MODE                ;
        String hFHESCHS_CLAIM_USER_ID           ;
        String hFHESCHS_START_TIME              ;
        Double  hFHESCHS_START_SHOP_DATE;
        String hFHESCHS_END_TIME                ;
        Double  hFHESCHS_END_SHOP_DATE;
        String hFHESCHS_NEW_EQP_STATE           ;
        String hFHESCHS_NEW_OPE_MODE            ;
        String hFHESCHS_CLAIM_MEMO              ;
        String hFHESCHS_EVENT_CREATE_TIME       ;
        String hFHESCHS_E10_STATE               ;
        String hFHESCHS_ACT_E10_STATE           ;
        String hFHESCHS_ACT_EUIPMENT_STATE      ;
        String hFHESCHS_NEW_E10_STATE           ;
        String hFHESCHS_NEW_EQUIPMENT_STATE     ;
        String hFHESCHS_NEW_ACT_E10_STATE       ;
        String hFHESCHS_NEW_ACT_EQUIPMENT_STATE ;

        hFHESCHS_EQP_ID = equipmentStatusChangeRecord.getEqp_id          ();
        hFHESCHS_EQP_NAME = equipmentStatusChangeRecord.getEqp_name        ();
        hFHESCHS_STK_ID = equipmentStatusChangeRecord.getStk_id          ();
        hFHESCHS_STK_NAME = equipmentStatusChangeRecord.getStk_name        ();
        hFHESCHS_AREA_ID = equipmentStatusChangeRecord.getArea_id         ();
        hFHESCHS_EQP_STATE = equipmentStatusChangeRecord.getEqp_state       ();
        hFHESCHS_OPE_MODE = equipmentStatusChangeRecord.getOpe_mode        ();
        hFHESCHS_CLAIM_USER_ID = equipmentStatusChangeRecord.getClaim_user_id   ();
        hFHESCHS_START_TIME = equipmentStatusChangeRecord.getStart_time      ();
        hFHESCHS_START_SHOP_DATE  = equipmentStatusChangeRecord.getStart_shop_date ();
        hFHESCHS_END_TIME = equipmentStatusChangeRecord.getEnd_time        ();
        hFHESCHS_END_SHOP_DATE    = equipmentStatusChangeRecord.getEnd_shop_date();
        hFHESCHS_NEW_EQP_STATE = equipmentStatusChangeRecord.getNew_eqp_state   ();
        hFHESCHS_NEW_OPE_MODE = equipmentStatusChangeRecord.getNew_ope_mode    ();
        hFHESCHS_CLAIM_MEMO = equipmentStatusChangeRecord.getClaim_memo      ();
        hFHESCHS_EVENT_CREATE_TIME = equipmentStatusChangeRecord.getEvent_create_time ();
        hFHESCHS_E10_STATE = equipmentStatusChangeRecord.getE10_state ();
        hFHESCHS_ACT_E10_STATE = equipmentStatusChangeRecord.getAct_e10_state ();
        hFHESCHS_ACT_EUIPMENT_STATE = equipmentStatusChangeRecord.getAct_equipment_state();
        hFHESCHS_NEW_E10_STATE = equipmentStatusChangeRecord.getNew_e10_state();
        hFHESCHS_NEW_EQUIPMENT_STATE = equipmentStatusChangeRecord.getNew_equipment_state();
        hFHESCHS_NEW_ACT_E10_STATE = equipmentStatusChangeRecord.getNew_act_e10_state();
        hFHESCHS_NEW_ACT_EQUIPMENT_STATE = equipmentStatusChangeRecord.getNew_act_equipment_state();

        baseCore.insert("INSERT INTO OHEQSC\n" +
                "            (  ID,EQP_ID,\n" +
                "                    EQP_NAME,\n" +
                "                    STK_ID,\n" +
                "                    STK_NAME,\n" +
                "                    BAY_ID,\n" +
                "                    EQP_STATE_ID,\n" +
                "                    OPE_MODE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    START_TIME,\n" +
                "                    START_WORK_DATE,\n" +
                "                    END_TIME,\n" +
                "                    END_WORK_DATE,\n" +
                "                    NEW_EQP_STATE,\n" +
                "                    NEW_OPE_MODE,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    E10_STATE_ID,\n" +
                "                    ACTUAL_E10_STATE_ID,\n" +
                "                    ACTUAL_EQP_STATE_ID,\n" +
                "                    NEW_E10_STATE_ID,\n" +
                "                    NEW_EQP_STATE_ID,\n" +
                "                    NEW_ACTUAL_E10_STATE_ID,\n" +
                "                    NEW_ACTUAL_EQP_STATE_ID,\n" +
                "                    REASON_CODE,\n" +
                "                    REASON_DESC )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Oheschs.class)
                ,hFHESCHS_EQP_ID
                ,hFHESCHS_EQP_NAME
                ,hFHESCHS_STK_ID
                ,hFHESCHS_STK_NAME
                ,hFHESCHS_AREA_ID
                ,hFHESCHS_EQP_STATE
                ,hFHESCHS_OPE_MODE
                ,hFHESCHS_CLAIM_USER_ID
                ,convert(hFHESCHS_START_TIME)
                ,hFHESCHS_START_SHOP_DATE
                ,convert(hFHESCHS_END_TIME)
                ,hFHESCHS_END_SHOP_DATE
                ,hFHESCHS_NEW_EQP_STATE
                ,hFHESCHS_NEW_OPE_MODE
                ,hFHESCHS_CLAIM_MEMO
                ,convert(hFHESCHS_EVENT_CREATE_TIME)
                ,hFHESCHS_E10_STATE
                ,hFHESCHS_ACT_E10_STATE
                ,hFHESCHS_ACT_EUIPMENT_STATE
                ,hFHESCHS_NEW_E10_STATE
                ,hFHESCHS_NEW_EQUIPMENT_STATE
                ,hFHESCHS_NEW_ACT_E10_STATE
                ,hFHESCHS_NEW_ACT_EQUIPMENT_STATE,
                equipmentStatusChangeRecord.getReasonCode(),
                equipmentStatusChangeRecord.getReasonDescription());

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
     * @return com.fa.cim.dto.Infos.EquipmentStatusChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/27 13:39
     */
    public Infos.EquipmentStatusChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVEQSC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EquipmentStatusChangeEventRecord theEventData=new Infos.EquipmentStatusChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map sqlData : sqlDatas) {
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setStockerID(convert(sqlData.get("STK_ID")));
            theEventData.setEquipmentState(convert(sqlData.get("EQP_STATE_ID")));
            theEventData.setE10State(convert(sqlData.get("E10_STATE_ID")));
            theEventData.setActualEquipmentState(convert(sqlData.get("ACTUAL_EQP_STATE_ID")));
            theEventData.setActualE10State(convert(sqlData.get("ACTUAL_E10_STATE_ID")));
            theEventData.setOperationMode(convert(sqlData.get("OPE_MODE")));
            theEventData.setStartTimeStamp(convert(sqlData.get("START_TIME")));
            theEventData.setNewEquipmentState(convert(sqlData.get("NEW_EQP_STATE_ID")));
            theEventData.setNewE10State(convert(sqlData.get("NEW_E10_STATE_ID")));
            theEventData.setNewActualEquipmentState(convert(sqlData.get("NEW_ACTUAL_EQP_STATE_ID ")));
            theEventData.setNewActualE10State(convert(sqlData.get("NEW_ACTUAL_E10_STATE_ID ")));
            theEventData.setNewOperationMode(convert(sqlData.get("NEW_OPE_MODE ")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            theEventData.setReasonCode(objectIdentifier(convert(sqlData.get("REASON_CODE_ID")),
                    convert(sqlData.get("REASON_CODE_RKEY"))));
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
     * @date 2019/6/27 13:46
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVEQSC_CDA WHERE REFKEY=?";
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
