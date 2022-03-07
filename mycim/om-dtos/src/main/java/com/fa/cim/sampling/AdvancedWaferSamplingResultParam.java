package com.fa.cim.sampling;

import java.util.List;
import java.util.stream.Collectors;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;

import lombok.Data;

/**
 * description:  接收对应参数，进行wafer 抽样。
 *
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/6/24 0024          ********            Decade            create file
 * @author: YJ
 * @date: 2021/6/24 0024 9:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class AdvancedWaferSamplingResultParam {
    /**
     * user
     */
    private User user;

    /**
     * 设备Id
     */
    private ObjectIdentifier equipmentId;

    /**
     * machine recipe Id
     */
    private ObjectIdentifier machineRecipeId;

    /**
     * product Id
     */
    private ObjectIdentifier productId;

    /**
     * technology Id
     */
    private ObjectIdentifier technologyId;

    /**
     * wafer list
     */
    private List<WaferParam> waferParamList;

    /**
     * edc setting type
     */
    private String dataCollectionSettingType;

    /**
     * ecc item
     */
    private List<DcItemDefinitionParam> dcItemDefinitionParamList;

    /**
     * edc item
     */
    @Data
    public static class DcItemDefinitionParam {
        /** meas.type */
        private String measType;
        /** wafer count */
        private Integer waferPosition;
    }

    /**
     * wafer
     */
	@Data
	public static class WaferParam {
		/**
		 * wafer Id
		 */
		private ObjectIdentifier waferId;

		/**
		 * slot Number
		 */
		private Integer slotNumber;
	}

	/**
	 * description:  info.lotWafer convert to lotWaferParam
	 * change history:
	 * date             defect             person             comments
	 * ---------------------------------------------------------------------------------------------------------------------
	 * 2021/6/25 0025 16:59                        YJ                Create
	 *
	 * @author YJ
	 * @date 2021/6/25 0025 16:59
	 * @param lotWaferList - lot wafer
	 */
	public void infoLotWaferConvertToParam(List<Infos.LotWafer> lotWaferList) {
		this.waferParamList = lotWaferList.parallelStream().map(lotWafer -> {
			WaferParam waferParam = new WaferParam();
			waferParam.setWaferId(lotWafer.getWaferID());
			waferParam.setSlotNumber(lotWafer.getSlotNumber().intValue());
			return waferParam;
		}).collect(Collectors.toList());
	}
}
