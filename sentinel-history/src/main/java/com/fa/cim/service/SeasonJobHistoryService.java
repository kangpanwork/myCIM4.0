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
public class SeasonJobHistoryService {

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
     * @return java.lang.String
     * @exception
     * @author ho
     * @date 2020/7/31 15:04
     */
    public String insertOHSEASONJOB(String event) {
        baseCore.insert("INSERT\n" +
                "\tINTO\n" +
                "\tOHSEASONJOB (ID,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tEVENT_CREATE_TIME,\n" +
                "\tOPE_CATEGORY,\n" +
                "\tACTION_TIME,\n" +
                "\tCREATE_TIME,\n" +
                "\tCOND_TYPE,\n" +
                "\tSEASON_TYPE,\n" +
                "\tSEASON_PRODUCT_ID,\n" +
                "\tSEASON_PRODUCT_RKEY,\n" +
                "\tEQP_ID,\n" +
                "\tEQP_RKEY,\n" +
                "\tUSER_ID,\n" +
                "\tUSER_RKEY,\n" +
                "\tPRIORITY,\n" +
                "\tSEASON_JOB_ID,\n" +
                "\tSEASON_JOB_RKEY,\n" +
                "\tSEASON_ID,\n" +
                "\tSEASON_RKEY,\n" +
                "\tCHAMBER,\n" +
                "\tSEASON_JOB_STATUS,\n" +
                "\tSEASON_LOT_ID,\n" +
                "\tSEASON_LOT_RKEY,\n" +
                "\tSEASON_CARRIER_ID,\n" +
                "\tSEASON_CARRIER_RKEY,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_RKEY,\n" +
                "\tCARRIER_ID,\n" +
                "\tCARRIER_RKEY,\n" +
                "\tSEASON_RCP_ID,\n" +
                "\tSEASON_RCP_RKEY,\n" +
                "\tWAFER_QTY,\n" +
                "\tMIN_SEASON_WAFER_COUNT,\n" +
                "\tMAX_IDLE_TIME,\n" +
                "\tINTERVAL_BETWEEN_SEASON,\n" +
                "\tSEASON_GROUP_FLAG,\n" +
                "\tNO_IDLE_FLAG,\n" +
                "\tFROM_RECIPE,\n" +
                "\tTO_RECIPE,\n" +
                "\tMOVE_IN_TIME,\n" +
                "\tMOVE_OUT_TIME,\n" +
                "\tOPE_MEMO,\n" +
                "\tENTITY_MGR,\n" +
                "\tWAIT_FLAG)\n" +
                "SELECT\n" +
                "\tID,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tTO_TIMESTAMP(EVENT_CREATE_TIME,\n" +
                "\t'yyyy-mm-dd-hh24:mi:ss.ff'),\n" +
                "\tACTION,\n" +
                "\tACTION_TIME,\n" +
                "\tCREATE_TIME,\n" +
                "\tCOND_TYPE,\n" +
                "\tSEASON_TYPE,\n" +
                "\tSEASON_PRODUCT_ID,\n" +
                "\tSEASON_PRODUCT_RKEY,\n" +
                "\tEQP_ID,\n" +
                "\tEQP_RKEY,\n" +
                "\tUSER_ID,\n" +
                "\tUSER_RKEY,\n" +
                "\tPRIORITY,\n" +
                "\tSEASON_JOB_ID,\n" +
                "\tSEASON_JOB_RKEY,\n" +
                "\tSEASON_ID,\n" +
                "\tSEASON_RKEY,\n" +
                "\tCHAMBER,\n" +
                "\tSEASON_JOB_STATUS,\n" +
                "\tSEASON_LOT_ID,\n" +
                "\tSEASON_LOT_RKEY,\n" +
                "\tSEASON_CARRIER_ID,\n" +
                "\tSEASON_CARRIER_RKEY,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_RKEY,\n" +
                "\tCARRIER_ID,\n" +
                "\tCARRIER_RKEY,\n" +
                "\tSEASON_RCP_ID,\n" +
                "\tSEASON_RCP_RKEY,\n" +
                "\tWAFER_QTY,\n" +
                "\tMIN_SEASON_WAFER_COUNT,\n" +
                "\tMAX_IDLE_TIME,\n" +
                "\tINTERVAL_BETWEEN_SEASON,\n" +
                "\tSEASON_GROUP_FLAG,\n" +
                "\tNO_IDLE_FLAG,\n" +
                "\tFROM_RECIPE,\n" +
                "\tTO_RECIPE,\n" +
                "\tMOVE_IN_TIME,\n" +
                "\tMOVE_OUT_TIME,\n" +
                "\tOPE_MEMO,\n" +
                "\tENTITY_MGR,\n" +
                "\tWAIT_FLAG\n" +
                "FROM\n" +
                "\tOMEVSEASONJOB f2\n" +
                "WHERE\n" +
                "\tID = ?", event);
        String id=generateID("OHSEASONJOB");
        baseCore.insert("UPDATE OHSEASONJOB SET ID =? WHERE ID =?",id,event);
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
     * @date 2020/7/31 15:05
     */
    public void insertOHSEASONJOBUDATA(String event, String id) {
        baseCore.insertChildTable("OMEVSEASONJOB_CDA","OHSEASONJOB_CDA",event,id);
    }
}
