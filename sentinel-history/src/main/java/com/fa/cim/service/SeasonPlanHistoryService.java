package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.convert;
import static com.fa.cim.utils.BaseUtils.generateID;

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
public class SeasonPlanHistoryService {

    @Autowired
    private BaseCore baseCore;


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
      * @param event
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 14:14
     */
    public String insertOHSEASON(String event) {
        baseCore.insert("INSERT INTO OHSEASON (ID, TRX_ID, EVENT_TIME, TRX_USER_ID, TRX_MEMO, EVENT_CREATE_TIME, OPE_CATEGORY, ACTION_TIME,\n" +
                "                      CREATE_TIME, SEASON_ID, SEASON_RKEY, COND_TYPE, SEASON_TYPE, PRODUCT_ID, PRODUCT_RKEY, EQP_ID,\n" +
                "                      EQP_RKEY, LAST_SEASON_TIME, USER_ID, USER_RKEY, STATUS, PRIORITY, ENTITY_MGR)\n" +
                "SELECT ID,\n" +
                "       TRX_ID,\n" +
                "       EVENT_TIME,\n" +
                "       TRX_USER_ID,\n" +
                "       TRX_MEMO,\n" +
                "       EVENT_CREATE_TIME,\n" +
                "       OPE_CATEGORY,\n" +
                "       ACTION_TIME,\n" +
                "       CREATE_TIME,\n" +
                "       SEASON_ID,\n" +
                "       SEASON_RKEY,\n" +
                "       COND_TYPE,\n" +
                "       SEASON_TYPE,\n" +
                "       PRODUCT_ID,\n" +
                "       PRODUCT_RKEY,\n" +
                "       EQP_ID,\n" +
                "       EQP_RKEY,\n" +
                "       LAST_SEASON_TIME,\n" +
                "       USER_ID,\n" +
                "       USER_RKEY,\n" +
                "       STATUS,\n" +
                "       PRIORITY,\n" +
                "       ENTITY_MGR\n" +
                "FROM OHSEASON f2\n" +
                "WHERE ID = ?",event);
        String id=generateID("OHSEASON");
        baseCore.insert("UPDATE OHSEASON SET ID =? WHERE ID =?",id,event);
        return id;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @param id
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 14:28
     */
    public void insertOHSEASONCHAMBER(String event, String id) {
        baseCore.insertChildTable("OMEVSEASON_CHAMBER","OHSEASON_CHAMBER",event,id);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @param id
     * @return void
     * @exception 
     * @author ho
     * @date 2020/7/31 14:35
     */
    public void insertOHSEASONPARAM(String event, String id) {
        baseCore.insertChildTable("OMEVSEASON_PARAM","OHSEASON_PARAM",event,id);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @param id
     * @return void
     * @exception 
     * @author ho
     * @date 2020/7/31 14:45
     */
    public void insertOHSEASONPRODRECIPE(String event, String id) {
        baseCore.insertChildTable("OMEVSEASON_PRODRECIPE","OHSEASON_PRODRECIPE",event,id);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @param id
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 14:57
     */
    public void insertOHSEASONPRODUCT(String event, String id) {
        baseCore.insertChildTable("OMEVSEASON_PRODUCT","OHSEASON_PRODUCT",event,id);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @param id
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 15:00
     */
    public void insertOHSEASONUDATA(String event, String id) {
        baseCore.insertChildTable("OMEVSEASON_CDA","OHSEASON_CDA",event,id);
    }
}
