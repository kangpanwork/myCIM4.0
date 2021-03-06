package com.fa.cim.method.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.feign.IEsecFeign;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ILayoutRecipeMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description: layout recipe recipe method
 * <p>
 * change history: date defect# person comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/2 0002 ******** YJ create file
 *
 * @author: YJ
 * @date: 2021/3/2 0002 19:24
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class LayoutRecipeMethod implements ILayoutRecipeMethod {


    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    /**
     * esec service
     */
    @Autowired
    private IEsecFeign esecFeign;

    @Override
    public List<Infos.StartCassette> equipmentFurnaceRecipeConvert(Infos.ObjCommon objCommon,
                                                                   LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams
                                                                           convertEquipmentFurnaceRecipeParams) {
        // step1. check param
        Validations.check(ObjectIdentifier.isEmpty(convertEquipmentFurnaceRecipeParams.getEquipmentId()) ||
                        CollectionUtil.isEmpty(convertEquipmentFurnaceRecipeParams.getStartCassettes()),
                retCodeConfig.getInvalidInputParam());


        // step2. ?????????ui??????????????????????????????????????????????????????
        if (log.isInfoEnabled()) {
            log.info(
                    "equipmentFurnaceRecipeConvert()-> info : ???????????????lot?????????????????????design specific control ????????????????????? "
                            + "convert to = {} , params = {} ",
                    convertEquipmentFurnaceRecipeParams.isDesignatedFurnaceControl(),
                    convertEquipmentFurnaceRecipeParams.getStartCassettes());
        }
        // ??????????????????????????????????????????furnace recipe??????
        if (convertEquipmentFurnaceRecipeParams.isDesignatedFurnaceControl()) {
            return convertEquipmentFurnaceRecipeParams.getStartCassettes();
        }

        /*---------------------------------------*/
        /* get special control                   */
        /*---------------------------------------*/
        CimMachine machine = baseCoreFactory.getBO(CimMachine.class, convertEquipmentFurnaceRecipeParams.getEquipmentId());
        Validations.check(Objects.isNull(machine), retCodeConfig.getNotFoundMachine());
        List<String> specialEquipmentControls = machine.getSpecialEquipmentControls();
        if (CollectionUtils.isEmpty(specialEquipmentControls)) {
            if (log.isInfoEnabled()) {
                log.info("equipmentFurnaceRecipeConvert()-> info : special controls is null...");
            }
            return convertEquipmentFurnaceRecipeParams.getStartCassettes();
        }

        /*---------------------------------------*/
        /* get special control  is furnace       */
        /*---------------------------------------*/
        boolean isFurnaceEquipment = specialEquipmentControls.parallelStream().noneMatch(
                special -> CimStringUtils.equals(special, BizConstant.SP_MC_SPECIALEQUIPMENTCONTROL_FURNACE));
        if (isFurnaceEquipment) {
            if (log.isInfoEnabled()) {
                log.info("equipmentFurnaceRecipeConvert()-> info : this  special controls is not furnace...");
            }
            return convertEquipmentFurnaceRecipeParams.getStartCassettes();
        }

        // step3.  call esec furnace recipe
        User user = objCommon.getUser().duplicate();
        user.setFunctionID(LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams.TX_ID);
        convertEquipmentFurnaceRecipeParams.setUser(user);
        Response response = esecFeign.equipmentFurnaceRecipeConvertRpt(convertEquipmentFurnaceRecipeParams);
        if (log.isInfoEnabled()) {
            log.info("esecFeign.chamberLevelRecipeMoveQueryRpt() -> info : " +
                    "call esec ??????chamber level recipe , response = {}", response);
        }
        Validations.check(!CimNumberUtils.eq(OmCode.SUCCESS_CODE, response.getCode()),
                String.join("ESEC: ->>", response.getMessage()));

        return ((JSONArray) response.getBody()).stream()
                .map(v -> JSONObject.toJavaObject(JSONObject.parseObject(v.toString()), Infos.StartCassette.class))
                .collect(Collectors.toList());
    }


    @Override
    public void controlJobIsCarrierLotFurnaceSpecificControl(Infos.ObjCommon objCommon, ObjectIdentifier cassetteId,
                                                             ObjectIdentifier equipmentId) {

        // step1. check basic param
        Validations.check(ObjectIdentifier.isEmpty(cassetteId), retCodeConfig.getNotFoundCassette(),cassetteId.getValue());
        Validations.check(ObjectIdentifier.isEmpty(equipmentId), retCodeConfig.getNotFoundEquipment(), equipmentId.getValue());

        // step2. check equipment
        CimMachine machine = baseCoreFactory.getBO(CimMachine.class, equipmentId);
        Validations.check(Objects.isNull(machine), retCodeConfig.getNotFoundEquipment(), equipmentId.getValue());

        // step3. ????????????furnace ??????
        List<String> specialEquipmentControls = machine.getSpecialEquipmentControls();
        if (CollectionUtils.isEmpty(specialEquipmentControls)) {
            if (log.isInfoEnabled()) {
                log.info("equipmentFurnaceRecipeConvert()-> info : special controls is null...");
            }
            return;
        }

        /*----------------------------------------*/
        /* check eqp is furnace                   */
        /*----------------------------------------*/
        if (isFurnaceAndSpecificControl(specialEquipmentControls)) {
            return;
        }

        // step4. get carrier control job
        CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteId);
        // ??????????????? carrier
        Validations.check(Objects.isNull(cassette), retCodeConfig.getNotFoundCassette(),cassetteId.getValue());
        CimControlJob controlJob = cassette.getControlJob();
        if (Objects.isNull(controlJob)) {
            if (log.isInfoEnabled()) {
                log.info("controlJobIsCarrierLotFurnaceSpecificControl()-> info : control job is null , {}",
                        cassetteId);
            }
            return;
        }

        // step4. ??????control job ?????? carrier ??????
        List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = controlJob.getStartCassetteInfo();
        if (CollectionUtil.isEmpty(startCassetteInfo)) {
            if (log.isInfoEnabled()) {
                log.info(
                        "controlJobIsCarrierLotFurnaceSpecificControl()-> info : control job in startCassetteInfo is null ,{}",
                        cassetteId);
            }
            return;
        }

        // step4.1 ?????????????????????????????????
        boolean isPositionMode = startCassetteInfo.parallelStream()
                .flatMap(psc -> psc.getLotInCassetteInfo().parallelStream())
                .anyMatch(lot -> {
                    try {
                        Integer.parseInt(lot.getProcessSpecificControl());
                        return false;
                    } catch (Exception e) {
                        return true;
                    }
                });
        if (isPositionMode) {
            if (log.isInfoEnabled()) {
                log.info("controlJobIsCarrierLotFurnaceSpecificControl() -> info : ????????????layout ????????????????????????check.");
            }
            return;
        }


        // step5. ???????????????carrier(??????lot????????????) ????????????
        String cassetteIdValue = ObjectIdentifier.fetchValue(cassetteId);

        Map<String, String> carrierSequence = startCassetteInfo.parallelStream()
                .filter(psc -> CollectionUtil.isNotEmpty(psc.getLotInCassetteInfo()))
                .collect(Collectors.toMap(psc -> ObjectIdentifier.fetchValue(psc.getCassetteID()), psc -> {
                    String processSpecificControl = psc.getLotInCassetteInfo().get(0).getProcessSpecificControl();
                    return StrUtil.isBlank(processSpecificControl) ? BizConstant.EMPTY : processSpecificControl;
                }));

        // ?????????????????????carrier??????
        String processSpecificControl = carrierSequence.get(cassetteIdValue);
        // carrier ??? ?????????, ????????????lot????????????carrier??????lot??????????????????
        if (StrUtil.isBlank(processSpecificControl)) {
            if (log.isInfoEnabled()) {
                log.info("controlJobIsCarrierLotFurnaceSpecificControl()-> info : find lot is empty {}", cassetteId);
            }
            return;
        }

        // step. ???????????????????????????carrier
        Infos.EqpPortInfo strEquipmentPortInfoForInternalBufferGetDROut = equipmentMethod
                .equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentId);
        List<String> eqpInCarrier = strEquipmentPortInfoForInternalBufferGetDROut.getEqpPortStatuses().parallelStream()
                .map(eqpPortStatus -> ObjectIdentifier.fetchValue(eqpPortStatus.getLoadedCassetteID()))
                .collect(Collectors.toList());

        Optional<Integer> optMaxSeq = eqpInCarrier.stream()
                .filter(carrierId -> StrUtil.isNotBlank(carrierSequence.get(carrierId)))
                .map(carrierId -> Integer.valueOf(carrierSequence.get(carrierId)))
                .max(Comparator.comparing(seq -> seq));

        if (optMaxSeq.isPresent()) {
            Validations.check(optMaxSeq.get() + 1 != Integer.parseInt(processSpecificControl),
                    retCodeConfigEx.getLayoutSpecificControlNotMatch());
        } else {
            Validations.check(1 != Integer.parseInt(processSpecificControl),
                    retCodeConfigEx.getLayoutSpecificControlNotMatch());
        }
    }

    /**
     * ??????????????????furnace ?????????????????????specific control
     *
     * @param specialEquipmentControls - equipment specific controls
     * @return is true / false
     */
    private boolean isFurnaceAndSpecificControl(List<String> specialEquipmentControls) {
        boolean isFurnace = false;
        boolean isSpecificControl = false;
        for (String specialEquipmentControl : specialEquipmentControls) {
            if (isFurnace && isSpecificControl) {
                break;
            }
            if (StrUtil.equals(BizConstant.LAYOUT_RECIPE_SPECIFIC_CONTROL_SEQUENCE, specialEquipmentControl)) {
                isSpecificControl = true;
            }
            if (StrUtil.equals(BizConstant.SP_MC_SPECIALEQUIPMENTCONTROL_FURNACE, specialEquipmentControl)) {
                isFurnace = true;
            }
        }

        if (!isFurnace || !isSpecificControl) {
            if (log.isInfoEnabled()) {
                log.info("equipmentFurnaceRecipeConvert()-> info : equipment is not furnace specific control");
            }
            return true;
        }
        return false;
    }
}