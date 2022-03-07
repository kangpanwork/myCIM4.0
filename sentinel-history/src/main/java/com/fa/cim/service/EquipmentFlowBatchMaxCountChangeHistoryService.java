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
public class EquipmentFlowBatchMaxCountChangeHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.EquipmentFlowBatchMaxCountChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/1 16:49
     */
    public Infos.EquipmentFlowBatchMaxCountChangeEventRecord getEventData(String id) {
        String sql="Select * from OMEVFBMXC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EquipmentFlowBatchMaxCountChangeEventRecord theEventData=new Infos.EquipmentFlowBatchMaxCountChangeEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setNewFlowBatchMaxCount(convertL(sqlData.get("FLOWB_MAX_COUNT")));

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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVFBMXC_CDA WHERE REFKEY=?";
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
     * @param flowBatchMaxCountChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 16:43
     */
    public Response insertEquipmentFlowBatchMaxCountChangeHistory( Infos.Ohfbmchs flowBatchMaxCountChangeRecord ) {
        String hFHFBMCHSEQP_ID                     ;
        Integer hFHFBMCHSMAX_COUNT ;
        String hFHFBMCHSCLAIM_TIME                 ;
        String hFHFBMCHSCLAIM_USER_ID              ;
        String hFHFBMCHSCLAIM_MEMO                 ;
        String hFHFBMCHSSTORE_TIME                 ;
        String hFHFBMCHSEVENT_CREATE_TIME          ;

        hFHFBMCHSEQP_ID = flowBatchMaxCountChangeRecord.getEqp_id          ();
        hFHFBMCHSMAX_COUNT         = flowBatchMaxCountChangeRecord.getMaxCount();
        hFHFBMCHSCLAIM_TIME = flowBatchMaxCountChangeRecord.getClaimTime       ();
        hFHFBMCHSCLAIM_USER_ID = flowBatchMaxCountChangeRecord.getClaimUser       ();
        hFHFBMCHSCLAIM_MEMO = flowBatchMaxCountChangeRecord.getClaimMemo       ();
        hFHFBMCHSEVENT_CREATE_TIME = flowBatchMaxCountChangeRecord.getEventCreateTime ();

        baseCore.insert("INSERT INTO OHFBMXCNTCHG\n" +
                "            (ID,EQP_ID,\n" +
                "                    FLOWB_MAX_COUNT,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME )\n" +
                "        Values\n" +
                "                (?,?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                ?)",generateID(Infos.Ohfbmchs.class)
                ,hFHFBMCHSEQP_ID
                ,hFHFBMCHSMAX_COUNT
                ,convert(hFHFBMCHSCLAIM_TIME)
                ,hFHFBMCHSCLAIM_USER_ID
                ,hFHFBMCHSCLAIM_MEMO
                ,convert(hFHFBMCHSEVENT_CREATE_TIME));

        return( returnOK() );
    }

}
