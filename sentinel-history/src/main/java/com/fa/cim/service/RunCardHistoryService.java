package com.fa.cim.service;

import com.fa.cim.core.BaseCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.fa.cim.utils.BaseUtils.generateID;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/8/20                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/8/20 13:06
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class RunCardHistoryService {

    @Autowired
    private BaseCore baseCore;


    public void insertOHRUNCARD(String event) {
        baseCore.insert("INSERT\n" +
                "\tINTO\n" +
                "\tOHRUNCARD (ID,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tEVENT_CREATE_TIME,\n" +
                "\tOPE_CATEGORY,\n" +
                "\tRUNCARD_ID,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_RKEY,\n" +
                "\tRUNCARD_STATE,\n" +
                "\tOWNER_ID,\n" +
                "\tOWNER_RKEY,\n" +
                "\tEXT_APROVAL_FLAG,\n" +
                "\tCREATE_TIME,\n" +
                "\tUPDATE_TIME,\n" +
                "\tAPPROVERS,\n" +
                "\tRUNCARD_TYPE,\n" +
                "\tAUTO_COMPLETE_FLAG)\n" +
                "SELECT\n" +
                "\tID,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tTO_TIMESTAMP(EVENT_CREATE_TIME,\n" +
                "\t'yyyy-mm-dd-hh24:mi:ss.ff'),\n" +
                "\tACTION,\n" +
                "\tRUNCARD_ID,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_RKEY,\n" +
                "\tRUNCARD_STATE,\n" +
                "\tOWNER_ID,\n" +
                "\tOWNER_RKEY,\n" +
                "\tEXT_APROVAL_FLAG,\n" +
                "\tCREATE_TIME,\n" +
                "\tUPDATE_TIME,\n" +
                "\tAPPROVERS,\n" +
                "\tRUNCARD_TYPE,\n" +
                "\tAUTO_COMPLETE_FLAG\n" +
                "FROM\n" +
                "\tOMEVRUNCARD f2\n" +
                "WHERE\n" +
                "\tID = ?",event);
        String id=generateID("OHRUNCARD");
        baseCore.insert("UPDATE OHRUNCARD SET ID =? WHERE ID =?",id,event);
    }
}
