package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * This file use to define the ReticleHistoryService class.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/9        ********             salt               create file
 *
 * @author: salt
 * @date: 2021/1/9 15:42
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class ReticleHistoryService {
    /**
     * OMEVRTLOPE table name
     */
    private static final String TABLE_NAME_OMEVRTLOPE = "OMEVRTLOPE";

    /**
     * OMEVRTLOPE_UDATA  table name
     */
    private static final String TABLE_NAME_OMEVRTLOPE_UDATA = "OMEVRTLOPE_CDA";

    @Autowired
    private BaseCore baseCore;

    public Infos.ReticleEventOperationalData getEventData(String id) {
        String sqlTemplate = "Select * from %s where id = ?";
        List<Map> dataList = baseCore.queryAllForMap(String.format(sqlTemplate, TABLE_NAME_OMEVRTLOPE), id);
        Infos.ReticleEventOperationalData theEventData = new Infos.ReticleEventOperationalData();
        for (Map<String, Object> data : dataList) {
            this.constructData(theEventData, data);
        }
        return theEventData;
    }

    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sqlTemplate = "SELECT * FROM %s WHERE REFKEY = ? ";
        List<Map> dataList = baseCore.queryAllForMap(String.format(sqlTemplate, TABLE_NAME_OMEVRTLOPE_UDATA), refKey);
        List<Infos.UserDataSet> userDataSets = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            Infos.UserDataSet userDataSet = new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(data.get("NAME")));
            userDataSet.setType(convert(data.get("DATA_TYPE")));
            userDataSet.setValue(convert(data.get("VALUE")));
            userDataSet.setOriginator(convert(data.get("SOURCE")));
        }
        return userDataSets;
    }

    /**
     * description:  construct
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param data      -
     * @param eventData -
     * @return
     * @author salt
     * @date 2021/1/9 16:00
     */
    private void constructData(Infos.ReticleEventOperationalData eventData, Map<String, Object> data) {
        eventData.setReticleId(convert(data.get("RETICLE_ID")));
        eventData.setReticleObj(convert(data.get("RETICLE_OBJ")));
        eventData.setReticleType(convert(data.get("RETICLE_TYPE")));
        eventData.setReticleStatus(convert(data.get("RETICLE_STATUS")));
        eventData.setReticleSubStatus(convert(data.get("RETICLE_SUB_STATUS")));
        eventData.setReticleGrade(convert(data.get("RETICLE_GRADE")));
        eventData.setReticleLocation(convert(data.get("RETICLE_LOCATION")));
        eventData.setOpeCategory(convert(data.get("OPE_CATEGORY")));
        eventData.setReticlePodId(convert(data.get("RETICLE_POD_ID")));
        eventData.setInspectionType(convert(data.get("INSPECTION_TYPE")));
        eventData.setEqpId(convert(data.get("EQP_ID")));
        eventData.setStocerId(convert(data.get("STOCKER_ID")));
        eventData.setReasonCode(convert(data.get("REASON_CODE")));
        eventData.setTxId(convert(data.get("TRX_ID")));
        eventData.setXferStatus(convert(data.get("XFER_STATUS")));
        eventData.setEventTime(convert(data.get("EVENT_TIME")));

        eventData.setClaimUserId(convert(data.get("TRX_USER_ID")));
        eventData.setClaimMemo(convert(data.get("TRX_MEMO")));
        eventData.setEventCreateTime(convert(StringUtils.convert(data.get("EVENT_CREATE_TIME"))));
        eventData.setDObjmanager(convert(data.get("ENTITY_MGR")));
        eventData.setReticleSubStatus(convert(data.get("RETICLE_SUB_STATUS")));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/9 16:35
     * @param reticleHistory -
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createReticleHistory(Infos.ReticleHistory reticleHistory) {

        String sql = "INSERT INTO OHRTLHS (" +
                "ID, RETICLE_ID, RETICLE_RKEY, RETICLE_TYPE, RETICLE_STATUS, RETICLE_GRADE," +
                " RETICLE_LOCATION, OPE_CATEGORY, TRX_USER_ID, RETICLE_POD_ID, TRX_TIME, INSPECTION_TYPE, TRX_MEMO, EQP_ID, " +
                "STOCKER_ID, REASON_CODE, TRANSACTION_ID, XFER_STATUS, RETICLE_SUB_STATUS" +
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        baseCore.insert(sql,generateID(Infos.ReticleHistory.class),
                reticleHistory.getReticleId(), reticleHistory.getReticleObj(), reticleHistory.getReticleType(),
                reticleHistory.getReticleStatus(), reticleHistory.getReticleGrade(), reticleHistory.getReticleLocation(),
                reticleHistory.getOpeCategory(), reticleHistory.getClaimUser(), reticleHistory.getReticlePodId(), reticleHistory.getClaimTime(),
                reticleHistory.getInspectionType(), reticleHistory.getClaimMemo(), reticleHistory.getEqpId(), reticleHistory.getStockerId(),
                reticleHistory.getReasonCode(), reticleHistory.getTransactionId(), reticleHistory.getXferStatus(),
                reticleHistory.getReticleSubStatus()
                );

        return returnOK();
    }
    

    /**
     * description: create reticle user data
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/9 17:46
     * @param userDataSetList -
     * @return
     */
    public void createUserData(List<Infos.UserDataSet> userDataSetList) {
        if (CollectionUtils.isEmpty(userDataSetList)) {
            return;
        }



    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/9 16:58
     * @param eventData -
     * @return
     */
    public Infos.ReticleHistory eventHistoryConversionReticleHistory(Infos.ReticleEventOperationalData eventData){
        Infos.ReticleHistory reticleHistory = new Infos.ReticleHistory();

        reticleHistory.setReticleId(eventData.getReticleId());
        reticleHistory.setReticleObj(eventData.getReticleObj());
        reticleHistory.setReticleType(eventData.getReticleType());
        reticleHistory.setReticleStatus(eventData.getReticleStatus());
        reticleHistory.setReticleGrade(eventData.getReticleGrade());
        reticleHistory.setReticleLocation(eventData.getReticleLocation());
        reticleHistory.setOpeCategory(eventData.getOpeCategory());
        reticleHistory.setClaimUser(eventData.getClaimUserId());
        reticleHistory.setReticlePodId(eventData.getReticlePodId());
        reticleHistory.setClaimTime(eventData.getEventCreateTime());
        reticleHistory.setInspectionType(eventData.getInspectionType());
        reticleHistory.setClaimMemo(eventData.getClaimMemo());
        reticleHistory.setEqpId(eventData.getEqpId());
        reticleHistory.setStockerId(eventData.getStocerId());
        reticleHistory.setReasonCode(eventData.getReasonCode());
        reticleHistory.setTransactionId(eventData.getTxId());
        reticleHistory.setXferStatus(eventData.getXferStatus());
        reticleHistory.setReticleSubStatus(eventData.getReticleSubStatus());

        return reticleHistory;
    }

    /**
     * description:  delete fifo data
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/9 17:44
     * @param tableName -
     * @param refKey -
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName,String refKey) {
        baseCore.insert(String.format("DELETE %s WHERE REFKEY = ? ",tableName),refKey);
    }
}
