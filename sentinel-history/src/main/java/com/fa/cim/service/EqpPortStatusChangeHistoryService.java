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
public class EqpPortStatusChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentPortStatusChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 14:14
     */
    public Response insertEqpPortStatusChangeHistory (Infos.Oheqpportschs equipmentPortStatusChangeRecord ) {
        String hFHEQPPORTSCHS_PORT_TYPE        ="";
        String hFHEQPPORTSCHS_PORT_ID          ="";
        String hFHEQPPORTSCHS_EQP_ID           ="";
        String hFHEQPPORTSCHS_PORT_USAGE       ="";
        String hFHEQPPORTSCHS_PORT_STATE       ="";
        String hFHEQPPORTSCHS_ACCESS_MODE      ="";
        String hFHEQPPORTSCHS_DISP_STATE       ="";
        String hFHEQPPORTSCHS_DISP_TIME        ="";
        String hFHEQPPORTSCHS_DISP_DRBL_ID     ="";
        String hFHEQPPORTSCHS_CLAIM_TIME       ="";
        String hFHEQPPORTSCHS_CLAIM_USER_ID    ="";
        String hFHEQPPORTSCHS_CLAIM_MEMO       ="";
        String hFHEQPPORTSCHS_STORE_TIME       ="";
        String hFHEQPPORTSCHS_EVENT_CREATE_TIME="";

        hFHEQPPORTSCHS_PORT_TYPE        = equipmentPortStatusChangeRecord.getPort_type         ();
        hFHEQPPORTSCHS_PORT_ID          = equipmentPortStatusChangeRecord.getPort_id           ();
        hFHEQPPORTSCHS_EQP_ID           = equipmentPortStatusChangeRecord.getEqp_id            ();
        hFHEQPPORTSCHS_PORT_USAGE       = equipmentPortStatusChangeRecord.getPort_usage        ();
        hFHEQPPORTSCHS_PORT_STATE       = equipmentPortStatusChangeRecord.getPort_state        ();
        hFHEQPPORTSCHS_ACCESS_MODE      = equipmentPortStatusChangeRecord.getAccess_mode       ();
        hFHEQPPORTSCHS_DISP_STATE       = equipmentPortStatusChangeRecord.getDiap_state        ();
        hFHEQPPORTSCHS_DISP_TIME        = equipmentPortStatusChangeRecord.getDiap_time         ();
        hFHEQPPORTSCHS_DISP_DRBL_ID     = equipmentPortStatusChangeRecord.getDiap_debl_id      ();
        hFHEQPPORTSCHS_CLAIM_TIME       = equipmentPortStatusChangeRecord.getClaim_time        ();
        hFHEQPPORTSCHS_CLAIM_USER_ID    = equipmentPortStatusChangeRecord.getClaim_user_id     ();
        hFHEQPPORTSCHS_CLAIM_MEMO       = equipmentPortStatusChangeRecord.getClaim_memo        ();
        hFHEQPPORTSCHS_STORE_TIME       = equipmentPortStatusChangeRecord.getStore_time        ();
        hFHEQPPORTSCHS_EVENT_CREATE_TIME= equipmentPortStatusChangeRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHEQPRTSC ( ID,PORT_TYPE\n" +
                "            , PORT_ID\n" +
                "            , EQP_ID\n" +
                "            , PORT_USAGE\n" +
                "            , PORT_STATE\n" +
                "            , ACCESS_MODE\n" +
                "            , DISP_STATE\n" +
                "            , DISP_TIME\n" +
                "            , DISP_CARRIER_ID\n" +
                "            , TRX_TIME\n" +
                "            , TRX_USER_ID\n" +
                "            , TRX_MEMO\n" +
                "            , STORE_TIME\n" +
                "            , EVENT_CREATE_TIME\n" +
                "    )\n" +
                "        VALUES ( ?,?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "                                   , ?\n" +
                "            , CURRENT_TIMESTAMP\n" +
                "                                   , ?\n" +
                "                                   )",generateID(Infos.Oheqpportschs.class)
                ,hFHEQPPORTSCHS_PORT_TYPE
                ,hFHEQPPORTSCHS_PORT_ID
                ,hFHEQPPORTSCHS_EQP_ID
                ,hFHEQPPORTSCHS_PORT_USAGE
                ,hFHEQPPORTSCHS_PORT_STATE
                ,hFHEQPPORTSCHS_ACCESS_MODE
                ,hFHEQPPORTSCHS_DISP_STATE
                ,convert(hFHEQPPORTSCHS_DISP_TIME)
                ,hFHEQPPORTSCHS_DISP_DRBL_ID
                ,convert(hFHEQPPORTSCHS_CLAIM_TIME)
                ,hFHEQPPORTSCHS_CLAIM_USER_ID
                ,hFHEQPPORTSCHS_CLAIM_MEMO
                ,convert(hFHEQPPORTSCHS_EVENT_CREATE_TIME));

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
     * @return com.fa.cim.dto.Infos.EqpPortStatusChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/28 14:22
     */
    public Infos.EqpPortStatusChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVEQPRTSC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EqpPortStatusChangeEventRecord theEventData=new Infos.EqpPortStatusChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setPortType(convert(sqlData.get("PORT_TYPE")));
            theEventData.setPortID(convert(sqlData.get("PORT_ID")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setPortUsage(convert(sqlData.get("PORT_USAGE")));
            theEventData.setPortStatus(convert(sqlData.get("PORT_STATE")));
            theEventData.setAccessMode(convert(sqlData.get("ACCESS_MODE")));
            theEventData.setDispatchState(convert(sqlData.get("DISP_STATE")));
            theEventData.setDispatchTime(convert(sqlData.get("DISP_TIME")));
            theEventData.setDispatchDurableID(convert(sqlData.get("DISP_CARRIER_ID")));

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
     * @date 2019/6/28 14:31
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVEQPRTSC_CDA WHERE REFKEY=?";
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
