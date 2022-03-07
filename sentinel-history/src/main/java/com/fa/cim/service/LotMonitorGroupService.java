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

import static com.fa.cim.utils.BaseUtils.convert;
import static com.fa.cim.utils.BaseUtils.convertI;

/**
 * description:  lot monitor group service
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/7/26 0026          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/7/26 0026 15:55  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotMonitorGroupService {

    @Autowired
    private BaseCore baseCore;

	@Autowired
	private LotOperationHistoryService lotOperationHistoryService;

    public Infos.LotMonitorGroupEventRecord getEventData(String id) {
		String sql = "Select * from OMEVLMG where id=?";
		List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
		Infos.LotMonitorGroupEventRecord lotMonitorGroupEventRecord = new Infos.LotMonitorGroupEventRecord();
		sqlDatas.parallelStream().forEach(map -> {
			lotMonitorGroupEventRecord.setLotId(convert(map.get("LOT_ID")));
			lotMonitorGroupEventRecord.setLotObj(convert(map.get("LOT_RKEY")));
			lotMonitorGroupEventRecord.setMonitorGroupId(convert(map.get("MON_GRP_ID")));
			lotMonitorGroupEventRecord.setOperationId(convert(map.get("OPE_ID")));
			lotMonitorGroupEventRecord.setOperationObj(convert(map.get("OPE_RKEY")));
			lotMonitorGroupEventRecord.setOperationNumber(convert(map.get("OPE_NO")));
			lotMonitorGroupEventRecord.setProcessFlowId(convert(map.get("PRP_ID")));
			lotMonitorGroupEventRecord.setOperationPassCount(convertI(map.get("OPE_PASS_COUNT")));
			lotMonitorGroupEventRecord.setOperationType(convert(map.get("STEP_TYPE")));
			lotMonitorGroupEventRecord.setLotType(convert(map.get("LOT_TYPE")));
			lotMonitorGroupEventRecord.setSubLotType(convert(map.get("SUB_LOT_TYPE")));
			lotMonitorGroupEventRecord.setCarrierId(convert(map.get("CARRIER_ID")));
			lotMonitorGroupEventRecord.setCarrierCategory(convert(map.get("CARRIER_CATEGORY")));
			lotMonitorGroupEventRecord.setOperationName(convert(map.get("STEP_NAME")));
			lotMonitorGroupEventRecord.setHoldState(convert(map.get("HOLD_STATE")));
			lotMonitorGroupEventRecord.setProductType(convert(map.get("PROD_TYPE")));
			lotMonitorGroupEventRecord.setProductId(convert(map.get("PROD_ID")));
			lotMonitorGroupEventRecord.setTechnologyId(convert(map.get("TECH_ID")));
			lotMonitorGroupEventRecord.setLotPriority(convertI(map.get("LOT_PRIORITY")));
			lotMonitorGroupEventRecord.setMfgLayer(convert(map.get("MFG_LAYER")));
			lotMonitorGroupEventRecord.setProductFmlyId(convert(map.get("PRODFMLY_ID")));
			lotMonitorGroupEventRecord.setStageId(convert(map.get("STAGE_ID")));
			lotMonitorGroupEventRecord.setStageGroupId(convert(map.get("STAGE_GRP_ID")));
			lotMonitorGroupEventRecord.setLotOwnerId(convert(map.get("LOT_OWNER_ID")));


			Infos.EventData eventCommon = new Infos.EventData();
			lotMonitorGroupEventRecord.setEventData(eventCommon);
			eventCommon.setEventTimeStamp(convert(map.get("EVENT_TIME")));
			eventCommon.setTransactionID(convert(map.get("TRX_ID")));
			eventCommon.setUserID(convert(map.get("TRX_USER_ID")));
			eventCommon.setEventMemo(convert(map.get("TRX_MEMO")));
			eventCommon.setEventCreationTimeStamp(convert(map.get("EVENT_CREATE_TIME")));
		});
		return lotMonitorGroupEventRecord;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param refKey
     * @return java.util.List<com.fa.cim.dto.Infos.UserDataSet>
     * @exception
     * @author Ho
     * @date 2019/6/5 13:50
     */
	public List<Infos.UserDataSet> allUserDataSets(String refKey) {
		String sql = "SELECT * FROM OMEVLMG_CDA WHERE REFKEY=?";
		List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
		List<Infos.UserDataSet> userDataSets = new ArrayList<>();
		for (Map<String, Object> uData : uDatas) {
			Infos.UserDataSet userDataSet = new Infos.UserDataSet();
			userDataSets.add(userDataSet);
			userDataSet.setName(convert(uData.get("NAME")));
			userDataSet.setType(convert(uData.get("TYPE")));
			userDataSet.setValue(convert(uData.get("VALUE")));
			userDataSet.setOriginator(convert(uData.get("ORIG")));
		}
		return userDataSets;
	}

	public Response createLayoutHistory(Infos.LotMonitorGroupEventRecord eventData,
			List<Infos.UserDataSet> userDataSets) {
		Infos.Ohopehs ohopehs = new Infos.Ohopehs();
		ohopehs.setLot_id(eventData.getLotId());
		ohopehs.setOpe_no(eventData.getOperationNumber());
		ohopehs.setOpe_pass_count(eventData.getOperationPassCount());
		ohopehs.setMainpd_id(eventData.getProcessFlowId());
		ohopehs.setPd_id(eventData.getOperationId());
		ohopehs.setMon_grp_id(eventData.getMonitorGroupId());
		ohopehs.setOpe_category("MonitorGrouping");
		ohopehs.setPd_type(eventData.getOperationType());
		ohopehs.setLot_type(eventData.getLotType());
		ohopehs.setSub_lot_type(eventData.getSubLotType());
		ohopehs.setCast_id(eventData.getCarrierId());
		ohopehs.setCast_category(eventData.getCarrierCategory());
		ohopehs.setPd_name(eventData.getOperationName());
		ohopehs.setHold_state(eventData.getHoldState());
		ohopehs.setProd_type(eventData.getProductType());
		ohopehs.setProdspec_id(eventData.getProductId());
		ohopehs.setTech_id(eventData.getTechnologyId());
		ohopehs.setPriority_class(eventData.getLotPriority());
		ohopehs.setMfg_layer(eventData.getMfgLayer());
		ohopehs.setProdgrp_id(eventData.getProductFmlyId());
		ohopehs.setStage_id(eventData.getStageId());
		ohopehs.setStagegrp_id(eventData.getStageGroupId());
		ohopehs.setLot_owner_id(eventData.getLotOwnerId());
		ohopehs.setClaim_time(eventData.getEventData().getEventTimeStamp());
		ohopehs.setClaim_user_id(eventData.getEventData().getUserID());
		ohopehs.setMove_type("NonMove");
		ohopehs.setEvent_create_time(eventData.getEventData().getEventCreationTimeStamp());
		return lotOperationHistoryService.insertLotOperationHistory(ohopehs);
	}

	public void deleteFIFO(String fifoTableName, String event) {
		String sql = String.format("DELETE %s WHERE REFKEY=?", fifoTableName);
		baseCore.insert(sql, event);
	}
}
