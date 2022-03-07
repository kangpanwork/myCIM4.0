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
public class ProductRequestHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productRequestRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/24 15:40
     */
    public Response insertProductRequestHistory(Infos.Ohprqhs productRequestRecord ) {
        String hFHPRQHS_LOT_ID         ;
        String hFHPRQHS_LOT_TYPE       ;
        String hFHPRQHS_SUB_LOT_TYPE   ;
        String hFHPRQHS_MAINPD_ID      ;
        String hFHPRQHS_CLAIM_TIME     ;
        Double   hFHPRQHS_CLAIM_SHOP_DATE     ;
        String hFHPRQHS_CLAIM_USER_ID  ;
        String hFHPRQHS_OPE_CATEGORY   ;
        String hFHPRQHS_PROD_TYPE      ;
        Integer hFHPRQHS_PROD_QTY            ;
        String hFHPRQHS_MFG_LAYER      ;
        String hFHPRQHS_PLAN_START_TIME;
        String hFHPRQHS_PLAN_END_TIME  ;
        String hFHPRQHS_LOT_GEN_TYPE   ;
        String hFHPRQHS_LOT_SCH_MODE   ;
        String hFHPRQHS_LOT_GEN_MODE   ;
        String hFHPRQHS_PROD_DEF_MODE  ;
        Integer hFHPRQHS_EXT_PRIORITY        ;
        Integer hFHPRQHS_PRIORITY_CLASS      ;
        String hFHPRQHS_PRODSPEC_ID    ;
        String hFHPRQHS_LOT_OWNER_ID   ;
        String hFHPRQHS_PRODGRP_ID     ;
        String hFHPRQHS_TECH_ID        ;
        String hFHPRQHS_CUSTPROD_ID    ;
        String hFHPRQHS_ORDER_NO       ;
        String hFHPRQHS_CUSTOMER_ID    ;
        String hFHPRQHS_START_BANK_ID  ;
        String hFHPRQHS_LOT_COMMENT    ;
        String hFHPRQHS_CLAIM_MEMO     ;
        String hFHPRQHS_EVENT_CREATE_TIME ;

        hFHPRQHS_LOT_ID = productRequestRecord.getLot_id ();
        hFHPRQHS_LOT_TYPE = productRequestRecord.getLot_type();
        hFHPRQHS_SUB_LOT_TYPE = productRequestRecord.getSub_lot_type();
        hFHPRQHS_MAINPD_ID = productRequestRecord.getMainpd_id();
        hFHPRQHS_CLAIM_TIME = productRequestRecord.getClaim_time();
        hFHPRQHS_CLAIM_SHOP_DATE  = productRequestRecord.getClaim_shop_date();
        hFHPRQHS_CLAIM_USER_ID = productRequestRecord.getClaim_user_id();
        hFHPRQHS_OPE_CATEGORY = productRequestRecord.getOpe_category();
        hFHPRQHS_PROD_TYPE = productRequestRecord.getProd_type();
        hFHPRQHS_PROD_QTY        = productRequestRecord.getProd_qty();
        hFHPRQHS_MFG_LAYER = productRequestRecord.getMfg_layer();
        hFHPRQHS_PLAN_START_TIME = productRequestRecord.getPlan_start_time();
        hFHPRQHS_PLAN_END_TIME = productRequestRecord.getPlan_end_time();
        hFHPRQHS_LOT_GEN_TYPE = productRequestRecord.getLot_gen_type();
        hFHPRQHS_LOT_SCH_MODE = productRequestRecord.getLot_sch_mode();
        hFHPRQHS_LOT_GEN_MODE = productRequestRecord.getLot_gen_mode();
        hFHPRQHS_PROD_DEF_MODE = productRequestRecord.getProd_def_mode();
        hFHPRQHS_EXT_PRIORITY    = productRequestRecord.getExt_priority();
        hFHPRQHS_PRIORITY_CLASS  = productRequestRecord.getPriority_class();
        hFHPRQHS_PRODSPEC_ID = productRequestRecord.getProdspec_id();
        hFHPRQHS_LOT_OWNER_ID = productRequestRecord.getLot_owner_id();
        hFHPRQHS_PRODGRP_ID = productRequestRecord.getProdgrp_id();
        hFHPRQHS_TECH_ID = productRequestRecord.getTech_id();
        hFHPRQHS_CUSTPROD_ID = productRequestRecord.getCustprod_id ();
        hFHPRQHS_ORDER_NO = productRequestRecord.getOrder_no();
        hFHPRQHS_CUSTOMER_ID = productRequestRecord.getCustomer_id();
        hFHPRQHS_START_BANK_ID = productRequestRecord.getStart_bank_id();
        hFHPRQHS_LOT_COMMENT = productRequestRecord.getLot_comment();
        hFHPRQHS_CLAIM_MEMO = productRequestRecord.getClaim_memo();
        hFHPRQHS_EVENT_CREATE_TIME = productRequestRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHPRORDER\n" +
                "            (  ID,LOT_ID,\n" +
                "                    LOT_TYPE,\n" +
                "                    SUB_LOT_TYPE,\n" +
                "                    PROCESS_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    OPE_CATEGORY,\n" +
                "                    PROD_TYPE,\n" +
                "                    PROD_QTY,\n" +
                "                    MFG_LAYER,\n" +
                "                    PLAN_START_TIME,\n" +
                "                    PLAN_END_TIME,\n" +
                "                    RELEASE_TYPE,\n" +
                "                    SCHEDULE_TYPE,\n" +
                "                    LOT_ID_CREATE_MODE,\n" +
                "                    PROD_DEF_MODE,\n" +
                "                    LOT_PRIORITY,\n" +
                "                    PROD_ID,\n" +
                "                    LOT_OWNER_ID,\n" +
                "                    PRODFMLY_ID,\n" +
                "                    TECH_ID,\n" +
                "                    CUSTPROD_ID,\n" +
                "                    MFG_ORDER_NO,\n" +
                "                    CUSTOMER_ID,\n" +
                "                    START_BANK_ID,\n" +
                "                    PO_COMMENT,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    STORE_TIME     )\n" +
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
                "            CURRENT_TIMESTAMP     )",generateID(Infos.Ohprqhs.class)
                ,hFHPRQHS_LOT_ID
                ,hFHPRQHS_LOT_TYPE
                ,hFHPRQHS_SUB_LOT_TYPE
                ,hFHPRQHS_MAINPD_ID
                ,convert(hFHPRQHS_CLAIM_TIME)
                ,hFHPRQHS_CLAIM_SHOP_DATE
                ,hFHPRQHS_CLAIM_USER_ID
                ,hFHPRQHS_OPE_CATEGORY
                ,hFHPRQHS_PROD_TYPE
                ,hFHPRQHS_PROD_QTY
                ,hFHPRQHS_MFG_LAYER
                ,convert(hFHPRQHS_PLAN_START_TIME)
                ,convert(hFHPRQHS_PLAN_END_TIME)
                ,hFHPRQHS_LOT_GEN_TYPE
                ,hFHPRQHS_LOT_SCH_MODE
                ,hFHPRQHS_LOT_GEN_MODE
                ,hFHPRQHS_PROD_DEF_MODE
                ,hFHPRQHS_PRIORITY_CLASS
                ,hFHPRQHS_PRODSPEC_ID
                ,hFHPRQHS_LOT_OWNER_ID
                ,hFHPRQHS_PRODGRP_ID
                ,hFHPRQHS_TECH_ID
                ,hFHPRQHS_CUSTPROD_ID
                ,hFHPRQHS_ORDER_NO
                ,hFHPRQHS_CUSTOMER_ID
                ,hFHPRQHS_START_BANK_ID
                ,hFHPRQHS_LOT_COMMENT
                ,hFHPRQHS_CLAIM_MEMO
                ,convert(hFHPRQHS_EVENT_CREATE_TIME));

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
     * @return com.fa.cim.dto.Infos.LotReworkEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/5 13:15
     */
    public Infos.ProductRequestEventRecord getEventData(String id) {
        String sql="Select * from OMEVPREQ where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.ProductRequestEventRecord theEventData=new Infos.ProductRequestEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setLotID(convert(sqlData.get("LOT_ID")));
            theEventData.setLotType(convert(sqlData.get("LOT_TYPE")));
            theEventData.setSubLotType(convert(sqlData.get("SUB_LOT_TYPE")));
            theEventData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            theEventData.setProductQuantity(convertL(sqlData.get("PROD_QTY")));
            theEventData.setPlanStartTime(convert(sqlData.get("PLAN_START_TIME")));
            theEventData.setPlanCompTime(convert(sqlData.get("PLAN_END_TIME")));
            theEventData.setLotGenerationType(convert(sqlData.get("RELEASE_TYPE")));
            theEventData.setLotScheduleMode(convert(sqlData.get("SCHEDULE_TYPE")));
            theEventData.setLotIDGenerationMode(convert(sqlData.get("LOT_ID_CREATE_MODE")));
            theEventData.setProductDefinitionMode(convert(sqlData.get("PROD_DEF_MODE")));
            theEventData.setPriorityClass(convertL(sqlData.get("LOT_PRIORITY")));
            theEventData.setProductID(convert(sqlData.get("PROD_ID")));
            theEventData.setLotOwnerID(convert(sqlData.get("LOT_OWNER_ID")));
            theEventData.setOrderNumber(convert(sqlData.get("MFG_ORDER_NO")));
            theEventData.setCustomerID(convert(sqlData.get("CUSTOMER_ID")));

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
     * @date 2019/6/5 13:50
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVPREQ_CDA WHERE REFKEY=?";
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
