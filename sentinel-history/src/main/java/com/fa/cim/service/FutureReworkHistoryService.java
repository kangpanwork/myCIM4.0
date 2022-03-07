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
public class FutureReworkHistoryService {

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
     * @return com.fa.cim.dto.Infos.ProcessJobChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/2 17:22
     */
    public Infos.FutureReworkEventRecord getEventData(String id) {
        String sql="Select * from OMEVLRWK where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.FutureReworkEventRecord theEventData=new Infos.FutureReworkEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        List<Infos.FutureReworkRouteEventData> reworkRoutes=new ArrayList<>();
        theEventData.setReworkRoutes(reworkRoutes);
        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setOperationNumber(convert(sqlData.get("OPE_NO")));

            sql="SELECT * FROM OMEVFRWK_RWKFLW WHERE REFKEY=?";
            List<Map> sqlReworkRoutes=baseCore.queryAllForMap(sql,id);

            for (Map sqlReworkRoute:sqlReworkRoutes) {
                Infos.FutureReworkRouteEventData reworkRoute=new Infos.FutureReworkRouteEventData();
                reworkRoutes.add(reworkRoute);

                reworkRoute.setTrigger(convert(sqlReworkRoute.get("TRG")));
                reworkRoute.setReworkRouteID(convert(sqlReworkRoute.get("REWORK_FLOW_ID")));
                reworkRoute.setReturnOperationNumber(convert(sqlReworkRoute.get("RETURN_OPE_NO")));
                reworkRoute.setReasonCodeID(objectIdentifier(convert(sqlReworkRoute.get("REASON_ID")),
                        convert(sqlReworkRoute.get("REASON_RKEY"))));
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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVFRWK_CDA WHERE REFKEY=?";
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
     * @param fhfrwkhs_Record
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 13:56
     */
    public Response insertFutureReworkHistory( Infos.Ohfrwkhs fhfrwkhs_Record ) {
        String hFHFRWKHSACTION             ;
        String hFHFRWKHSLOT_ID             ;
        String hFHFRWKHSMAINPD_ID          ;
        String hFHFRWKHSOPE_NO             ;
        String hFHFRWKHSTRIGGER            ;
        String hFHFRWKHSRWK_ROUTE_ID       ;
        String hFHFRWKHSRETURN_OPE_NO      ;
        String hFHFRWKHSREASON_ID          ;
        String hFHFRWKHSREASON_DESCRIPTION ;
        String hFHFRWKHSCLAIM_TIME         ;
        Double  hFHFRWKHSCLAIM_SHOP_DATE;
        String hFHFRWKHSCLAIM_USER_ID      ;
        String hFHFRWKHSCLAIM_MEMO         ;
        String hFHFRWKHSEVENT_CREATE_TIME  ;

        hFHFRWKHSACTION             = fhfrwkhs_Record.getAction_code        ();
        hFHFRWKHSLOT_ID             = fhfrwkhs_Record.getLot_id             ();
        hFHFRWKHSMAINPD_ID          = fhfrwkhs_Record.getMainpd_id          ();
        hFHFRWKHSOPE_NO             = fhfrwkhs_Record.getOpe_no             ();
        hFHFRWKHSTRIGGER            = fhfrwkhs_Record.getTrigger            ();
        hFHFRWKHSRWK_ROUTE_ID       = fhfrwkhs_Record.getRwk_route_id       ();
        hFHFRWKHSRETURN_OPE_NO      = fhfrwkhs_Record.getReturn_ope_no      ();
        hFHFRWKHSREASON_ID          = fhfrwkhs_Record.getReason_code        ();
        hFHFRWKHSREASON_DESCRIPTION = fhfrwkhs_Record.getReason_description ();
        hFHFRWKHSCLAIM_TIME         = fhfrwkhs_Record.getClaim_time         ();
        hFHFRWKHSCLAIM_SHOP_DATE    = fhfrwkhs_Record.getClaim_shop_date();
        hFHFRWKHSCLAIM_USER_ID      = fhfrwkhs_Record.getClaim_user_id      ();
        hFHFRWKHSCLAIM_MEMO         = fhfrwkhs_Record.getClaim_memo         ();
        hFHFRWKHSEVENT_CREATE_TIME  = fhfrwkhs_Record.getEvent_create_time  ();

        baseCore.insert("INSERT INTO OHFRWK\n" +
                "            ( ID, TASK_TYPE,\n" +
                "                    LOT_ID,\n" +
                "                    PROCESS_ID,\n" +
                "                    OPE_NO,\n" +
                "                    TRIG,\n" +
                "                    REWORK_FLOW_ID,\n" +
                "                    RETURN_OPE_NO,\n" +
                "                    REASON_ID,\n" +
                "                    REASON_DESC,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME )\n" +
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
                "            CURRENT_TIMESTAMP,\n" +
                "                ?)",generateID(Infos.Ohfrwkhs.class)
                ,hFHFRWKHSACTION
                ,hFHFRWKHSLOT_ID
                ,hFHFRWKHSMAINPD_ID
                ,hFHFRWKHSOPE_NO
                ,hFHFRWKHSTRIGGER
                ,hFHFRWKHSRWK_ROUTE_ID
                ,hFHFRWKHSRETURN_OPE_NO
                ,hFHFRWKHSREASON_ID
                ,hFHFRWKHSREASON_DESCRIPTION
                ,convert(hFHFRWKHSCLAIM_TIME)
                ,hFHFRWKHSCLAIM_SHOP_DATE
                ,hFHFRWKHSCLAIM_USER_ID
                ,hFHFRWKHSCLAIM_MEMO
                ,convert(hFHFRWKHSEVENT_CREATE_TIME ));

        return( returnOK() );
    }

}
