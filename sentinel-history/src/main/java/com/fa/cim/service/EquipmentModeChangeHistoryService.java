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
 * @date 2019/6/28 14:56
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class EquipmentModeChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentModeChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 15:04
     */
    public Response insertEquipmentModeChangeHistory(Infos.Ohemchs equipmentModeChangeRecord ) {
        String hFHEMCHS_EQP_ID                  ;
        String hFHEMCHS_EQP_NAME                ;
        String hFHEMCHS_PORT_ID                 ;
        String hFHEMCHS_AREA_ID                 ;
        String hFHEMCHS_OPE_MODE                ;
        String hFHEMCHS_ONLINE_MODE             ;
        String hFHEMCHS_DISP_MODE               ;
        String hFHEMCHS_ACCESS_MODE             ;
        String hFHEMCHS_OPE_START_MODE          ;
        String hFHEMCHS_OPE_COMP_MODE           ;
        String hFHEMCHS_DESCRIPTION             ;
        String hFHEMCHS_CLAIM_TIME              ;
        Double  hFHEMCHS_CLAIM_SHOP_DATE              ;
        String hFHEMCHS_CLAIM_USER_ID           ;
        String hFHEMCHS_CLAIM_MEMO              ;
        String hFHEMCHS_EVENT_CREATE_TIME       ;

        hFHEMCHS_EQP_ID = equipmentModeChangeRecord.getEqp_id         ();
        hFHEMCHS_EQP_NAME = equipmentModeChangeRecord.getEqp_name       ();
        hFHEMCHS_PORT_ID = equipmentModeChangeRecord.getPort_id        ();
        hFHEMCHS_AREA_ID = equipmentModeChangeRecord.getArea_id        ();
        hFHEMCHS_OPE_MODE = equipmentModeChangeRecord.getOpe_mode       ();
        hFHEMCHS_ONLINE_MODE = equipmentModeChangeRecord.getOnline_mode    ();
        hFHEMCHS_DISP_MODE = equipmentModeChangeRecord.getDisp_mode      ();
        hFHEMCHS_ACCESS_MODE = equipmentModeChangeRecord.getAccess_mode    ();
        hFHEMCHS_OPE_START_MODE = equipmentModeChangeRecord.getOpe_start_mode ();
        hFHEMCHS_OPE_COMP_MODE = equipmentModeChangeRecord.getOpe_comp_mode  ();
        hFHEMCHS_DESCRIPTION = equipmentModeChangeRecord.getDescription    ();
        hFHEMCHS_CLAIM_TIME = equipmentModeChangeRecord.getClaim_time      ();
        hFHEMCHS_CLAIM_SHOP_DATE  = equipmentModeChangeRecord.getClaim_shop_date();
        hFHEMCHS_CLAIM_USER_ID = equipmentModeChangeRecord.getClaim_user_id  ();
        hFHEMCHS_CLAIM_MEMO = equipmentModeChangeRecord.getClaim_memo     ();
        hFHEMCHS_EVENT_CREATE_TIME = equipmentModeChangeRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHEQPMODECHG\n" +
                "            ( ID, EQP_ID,\n" +
                "                    EQP_NAME,\n" +
                "                    PORT_ID,\n" +
                "                    BAY_ID,\n" +
                "                    OPE_MODE,\n" +
                "                    ONLINE_MODE,\n" +
                "                    DISPATCH_MODE,\n" +
                "                    ACCESS_MODE,\n" +
                "                    MOVE_IN_MODE,\n" +
                "                    MOVE_OUT_MODE,\n" +
                "                    DESCRIPTION,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP     )",generateID(Infos.Ohemchs.class)
                ,hFHEMCHS_EQP_ID
                ,hFHEMCHS_EQP_NAME
                ,hFHEMCHS_PORT_ID
                ,hFHEMCHS_AREA_ID
                ,hFHEMCHS_OPE_MODE
                ,hFHEMCHS_ONLINE_MODE
                ,hFHEMCHS_DISP_MODE
                ,hFHEMCHS_ACCESS_MODE
                ,hFHEMCHS_OPE_START_MODE
                ,hFHEMCHS_OPE_COMP_MODE
                ,hFHEMCHS_DESCRIPTION
                ,convert(hFHEMCHS_CLAIM_TIME)
                ,hFHEMCHS_CLAIM_SHOP_DATE
                ,hFHEMCHS_CLAIM_USER_ID
                ,hFHEMCHS_CLAIM_MEMO
                ,convert(hFHEMCHS_EVENT_CREATE_TIME));

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
     * @return com.fa.cim.dto.Infos.EquipmentModeChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/28 15:11
     */
    public Infos.EquipmentModeChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVEQMCHG where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EquipmentModeChangeEventRecord theEventData=new Infos.EquipmentModeChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setPortID(convert(sqlData.get("PORT_ID")));
            theEventData.setOperationMode(convert(sqlData.get("OPE_MODE_ID")));
            theEventData.setOnlineMode(convert(sqlData.get("ONLINE_MODE")));
            theEventData.setDispatchMode(convert(sqlData.get("DISPATCH_MODE")));
            theEventData.setAccessMode(convert(sqlData.get("ACCESS_MODE")));
            theEventData.setOperationStartMode(convert(sqlData.get("START_MODE")));
            theEventData.setOperationCompMode(convert(sqlData.get("COMP_MODE")));
            theEventData.setDescription(convert(sqlData.get("DESCRIPTION")));

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
     * @date 2019/6/28 15:18
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVEQMCHG_CDA WHERE REFKEY=?";
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
