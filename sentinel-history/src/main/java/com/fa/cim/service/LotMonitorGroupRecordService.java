package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.convert;

/**
 * description:  
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/7/26 0026          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/7/26 0026 15:52  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
@Component
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class LotMonitorGroupRecordService {

	@Autowired
	private BaseCore baseCore;

	@Transactional(rollbackFor = Exception.class)
	public List<String> getEventFIFO(String tableName) {
		String sqlTemplate = "SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,"
				+ "'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE AND ID!='-1' ORDER BY EVENT_TIME ASC FOR UPDATE";
		String sql = String.format(sqlTemplate, tableName);
		List<Object[]> fifos = baseCore.queryAll(sql);
		List<String> events = new ArrayList<>();
		fifos.forEach(fifo -> events.add(convert(fifo[0])));
		markFIFO(tableName);
		return events;
	}

	/**
	 * description: mark fifo
	 * <p>
	 * </p>
	 * change history: date defect person comments
	 * -----------------------------------------------------------------------------------------------------------------
	 *
	 * @param tableName
	 *            - 表名
	 * @author YJ
	 * @date 2021/7/26 15:16
	 */
	private void markFIFO(String tableName) {
		String sql = String.format(
				"UPDATE %s SET ID='-1' WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE", tableName);
		baseCore.insert(sql);
	}

}
