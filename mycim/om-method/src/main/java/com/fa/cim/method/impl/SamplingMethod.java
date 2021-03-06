package com.fa.cim.method.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.feign.ISamplingFeign;
import com.fa.cim.method.ISamplingMethod;
import com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.sampling.AdvancedWaferSamplingResultParam;
import com.fa.cim.sampling.AdvancedWaferSamplingResults;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * description:  sampling method
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/6/25 0025          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/6/25 0025 15:08  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
@OmMethod
@Slf4j
public class SamplingMethod implements ISamplingMethod {
    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ISamplingFeign samplingFeign;


    @Override
	public Infos.AdvancedWaferSamplingConvertInfo lotProcessAdvancedWaferSampling(Infos.ObjCommon objCommon,
			Infos.LotInCassette lotInCassette, ObjectIdentifier equipmentId) {
		// step1. find equipment BO
		CimMachine cimEqpBO = baseCoreFactory.getBO(CimMachine.class, equipmentId);
		Validations.check(Objects.isNull(cimEqpBO), new OmCode(retCodeConfig.getNotFoundEqp(), equipmentId.getValue()));
		if (log.isInfoEnabled()) {
			log.info("lotProcessAdvancedWaferSampling()->info : ");
		}
		AdvancedWaferSamplingResultParam advancedWaferSamplingResultParam = new AdvancedWaferSamplingResultParam();

		// step2. ??????????????????
		ObjectIdentifier lotId = lotInCassette.getLotID();
		CimLot cimLotBO = baseCoreFactory.getBO(CimLot.class, lotId);
		CimProductSpecification productSpecification = cimLotBO.getProductSpecification();

		// ??????product ID
		ObjectIdentifier productSpecId = productSpecification.getProductSpecID();

		// ??????technology ID
		CimTechnology technology = productSpecification.getProductGroup().getTechnology();
		ObjectIdentifier technologyId = ObjectIdentifier.build(technology.getIdentifier(), technology.getPrimaryKey());

		// machine recipe Id
		CimProcessOperation processOperation = cimLotBO.getProcessOperation();
		CimLogicalRecipe logicalRecipe = processOperation.findLogicalRecipeFor(productSpecification);
		CimMachineRecipe machineRecipe;

		// ==========================================================================================//
		// ?????????1????????????logic recipe???????????????machine recipe , ?????????0 ???????????????????????????machine recipe //
		// ==========================================================================================//
		int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
		if (1 == searchCondition) {
			machineRecipe = logicalRecipe.findMachineRecipeFor(cimLotBO, cimEqpBO);
		} else {
			machineRecipe = logicalRecipe.findMachineRecipeForSubLotType(cimEqpBO, cimLotBO.getSubLotType());
		}

		// ??????????????????wafer sampling ???????????????
		User duplicateUser = objCommon.getUser().duplicate();
		duplicateUser.setFunctionID(TransactionIDEnum.WAFER_SAMPLING_RULE_SEL.getValue());
		advancedWaferSamplingResultParam.setUser(duplicateUser);
		advancedWaferSamplingResultParam.setEquipmentId(equipmentId);
		advancedWaferSamplingResultParam
				.setMachineRecipeId(Objects.isNull(machineRecipe) ? null : machineRecipe.getMachineRecipeID());
		advancedWaferSamplingResultParam.setProductId(productSpecId);
		advancedWaferSamplingResultParam.setTechnologyId(technologyId);
		if (log.isInfoEnabled()) {
			log.info("oms call sampling service : ??????advanced wafer sampling ??????. request param :{}",
					advancedWaferSamplingResultParam);
		}

		// machine recipe is null , ?????????edc?????????
		if (Objects.nonNull(machineRecipe)) {
			// EDC
			CimDataCollectionDefinition dataCollectionDefinition = logicalRecipe
					.findDataCollectionDefinitionForSubLotType(cimEqpBO, machineRecipe, cimLotBO.getSubLotType());
			if (Objects.nonNull(dataCollectionDefinition)) {
				// ??????edc Setting type ???General / Specific
				// ?????????EDC item
				// edc item ?????????meas.type = Wafer / Site ???item
				List<EDCDTO.DCItemDefinition> dcItems = dataCollectionDefinition.getDCItems();
				if (CollectionUtil.isNotEmpty(dcItems)) {
					List<AdvancedWaferSamplingResultParam.DcItemDefinitionParam> dcItemList = dcItems.parallelStream()
							.filter(dcItem -> {
								String measType = dcItem.getMeasType();
								// meas type = wafer / site
								boolean isWaferOrSite = StrUtil.equals(BizConstant.SP_DCDEF_MEAS_WAFER, measType)
										|| StrUtil.equals(BizConstant.SP_DCDEF_MEAS_SITE, measType);
								// item type = Raw
								return isWaferOrSite && StrUtil.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW);
							})
							.map(dcItem -> {
								// ??????wafer position ????????????
								AdvancedWaferSamplingResultParam.DcItemDefinitionParam dcItemDefinitionParam =
										new AdvancedWaferSamplingResultParam.DcItemDefinitionParam();
								dcItemDefinitionParam.setWaferPosition(CimNumberUtils.intValue(dcItem.getWaferPosition()));
								dcItemDefinitionParam.setMeasType(dcItem.getMeasType());
								return dcItemDefinitionParam;
							})
							.collect(Collectors.toList());
					// ??????edc setting Type / dc item??????, call sampling service.
					advancedWaferSamplingResultParam
							.setDataCollectionSettingType(dataCollectionDefinition.getDCSettingType());
					advancedWaferSamplingResultParam.setDcItemDefinitionParamList(dcItemList);
				}
			}
		}

		// ??????wafer ??????
		advancedWaferSamplingResultParam.infoLotWaferConvertToParam(lotInCassette.getLotWaferList());

		return callSamplingServiceExecWaferSampling(advancedWaferSamplingResultParam, lotInCassette);
	}

	/**
	 * description: call sampling service execute advanced wafer sampling change
	 * history: date defect person comments
	 * ---------------------------------------------------------------------------------------------------------------------
	 * 2021/6/25 0025 11:04 YJ Create
	 *
	 * @author YJ
	 * @date 2021/6/25 0025 11:04
	 * @param advancedWaferSamplingResultParam
	 *            - advanced wafer sampling param
	 * @param lotInCassette
	 *            - lot in cassette
	 * @return advanced convert to info
	 */
	private Infos.AdvancedWaferSamplingConvertInfo callSamplingServiceExecWaferSampling(
			AdvancedWaferSamplingResultParam advancedWaferSamplingResultParam, Infos.LotInCassette lotInCassette) {
		// ------------------------------------------------//
		// oms call sampling service, ??????wafer ??????????????? //
		// ------------------------------------------------//
		Infos.AdvancedWaferSamplingConvertInfo advancedWaferSamplingConvertInfo = new Infos.AdvancedWaferSamplingConvertInfo();
		Response response;
		try {
			response = samplingFeign.advancedWaferSamplingCompile(advancedWaferSamplingResultParam);
		} catch (Exception e) {
			log.warn("oms call sampling service-> error : {}", e.getMessage());
			advancedWaferSamplingConvertInfo.setHitSampling(false);
			advancedWaferSamplingConvertInfo.setLotInCassette(lotInCassette);
			return advancedWaferSamplingConvertInfo;
		}
		// ????????????????????????error code?????????
		if (!CimNumberUtils.eq(OmCode.SUCCESS_CODE, response.getCode())) {
			// if response error
			log.error("waferSamplingCheck() -> error:{}", response);
			advancedWaferSamplingConvertInfo.setHitSampling(false);
			// error Code??????
			throwServiceException(response);
			return advancedWaferSamplingConvertInfo;
		}

		Object body = response.getBody();
		AdvancedWaferSamplingResults advancedWaferSamplingResults = JSONObject.toJavaObject((JSONObject) body,
				AdvancedWaferSamplingResults.class);
		if (log.isInfoEnabled()) {
			log.info("advancedWaferSamplingCompile()-> info : call sampling service, ????????????????????????: {}",
					advancedWaferSamplingResults);
		}

		if (advancedWaferSamplingResults.getHitSampling()) {
			List<AdvancedWaferSamplingResults.WaferResult> lotWaferListResult = advancedWaferSamplingResults
					.getLotWaferList();
			Map<ObjectIdentifier, Boolean> waferSamplingResultsMap = lotWaferListResult.parallelStream()
					.collect(Collectors.toMap(AdvancedWaferSamplingResults.WaferResult::getWaferId,
							AdvancedWaferSamplingResults.WaferResult::getProcessJobExecFlag));
			lotInCassette.getLotWaferList().forEach(
					lotWafer -> lotWafer.setProcessJobExecFlag(waferSamplingResultsMap.get(lotWafer.getWaferID())));
			// ??????????????????
			advancedWaferSamplingConvertInfo.setHitSampling(true);
		}
		// call sampling service
		if (log.isInfoEnabled()) {
			log.info("advancedWaferSamplingCompile()->info : oms call sampling service... Lot = {}",
					lotInCassette.getLotID());
		}
		advancedWaferSamplingConvertInfo.setLotInCassette(lotInCassette);
		return advancedWaferSamplingConvertInfo;
	}

	/**
	 * description:  sampling error convert to OMS error
	 * change history:
	 * date             defect             person             comments
	 * -----------------------------------------------------------------------------------------------------------------
	 * 2021/7/3 0003 13:50                        YJ                Create
	 *
	 * @author YJ
	 * @date 2021/7/3 0003 13:50
	 * @param response - response sampling service code
	 */
	public void throwServiceException(Response response) {
		Integer code = response.getCode();
		if (Objects.nonNull(code)
				&& String.valueOf(code).startsWith(BizConstant.SAMPLING_WAFER_LOGIC_ERROR_CODE_PREFIX)) {
			Validations.check(new OmCode(code, response.getMessage()));
		}
	}

}
