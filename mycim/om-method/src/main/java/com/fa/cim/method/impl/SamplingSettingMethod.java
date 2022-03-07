package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.ISamplingSettingMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/19       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/12/19 14:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class SamplingSettingMethod  implements ISamplingSettingMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public void samplingSettingFormatCheck(Infos.ObjCommon objCommon, String samplingPolicyName, String samplingWaferAttribute) {
        Validations.check(CimStringUtils.isEmpty(samplingPolicyName), retCodeConfig.getInvalidSmplSetting());
        // "If the Policy Name is invalid return error
        if (!CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOP, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_BOTTOM, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_RANDOM, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_EVEN, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_ODD, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOPANDBOTTOM, samplingPolicyName)
                && !CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_SLOT, samplingPolicyName)) {
            log.error("Policy name is invalid.");
            Validations.check(retCodeConfig.getInvalidSmplSetting());
        }
        // "If the Policy Name is "Even" or "Odd", the wafer Count / Slot Number should be blank."
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_EVEN, samplingPolicyName) || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_ODD, samplingPolicyName)) {
            Validations.check(!CimStringUtils.isEmpty(samplingWaferAttribute), retCodeConfig.getInvalidSmplSetting());
            //如果是even和odd 没有samplingWaferAttribute
            return;
        } else {
            Validations.check(CimStringUtils.isEmpty(samplingWaferAttribute), retCodeConfig.getInvalidSmplSetting());
        }

        int capacity = StandardProperties.OM_CARRIER_STORE_SIZE.getIntValue();

        String splitChar = BizConstant.SAMPLING_SEPARATOR;
        //tokenize attribute by delimiter COMMA
        String[] inputStrDataVar = samplingWaferAttribute.split(splitChar);
        //count comma, and seek blank
        int commaCount = samplingWaferAttribute.lastIndexOf(splitChar) + splitChar.length() == samplingWaferAttribute.length() ? inputStrDataVar.length : inputStrDataVar.length - 1;
        log.info("commaCount = {}", commaCount);
        int len = inputStrDataVar.length;

        StringBuilder endptr = new StringBuilder();
        Long value;
        String valueName = "value";
        String bErrorFlagName = "bErrorFlag";
        Map<String, Object> map = null;

        // "If the Policy Name is "Top", "Bottom" or "Random", the wafer Count / Slot Number should be one integer data."
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOP, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_BOTTOM, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_RANDOM, samplingPolicyName)) {
            boolean bErrorFlag = false;
            if (len != 1 || commaCount != 0) {
                log.info("len != 1 || commaCount != 0");
                bErrorFlag = true;
            } else {
                map = checkStrtol(inputStrDataVar[0], endptr);
                bErrorFlag = (boolean) map.get(bErrorFlagName);
            }
            Validations.check(bErrorFlag, retCodeConfig.getInvalidSmplSetting());
        }

        // "If the Policy Name is "Top and Bottom", the wafer Count / Slot Number should be two kinds of integer data."
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOPANDBOTTOM, samplingPolicyName)) {
            boolean bErrorFlag = false;
            if (len != 2 || commaCount != 1) {
                log.info("len != 2 || commaCount != 1");
                bErrorFlag = true;
            } else {
                map = checkStrtol(inputStrDataVar[0], endptr);
                bErrorFlag = (boolean) map.get(bErrorFlagName);
                map = checkStrtol(inputStrDataVar[1], endptr);
                bErrorFlag = (boolean) map.get(bErrorFlagName);
            }
            Validations.check(bErrorFlag, retCodeConfig.getInvalidSmplSetting());
        }

        // "If the Policy Name is "Slot", the wafer Count / Slot Number should be one or more kinds of integer data."
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_SLOT, samplingPolicyName)) {
            boolean bErrorFlag = false;
            if (len == 0 || len <= commaCount) {
                log.info("len == 0 || len <= commaCount");
                bErrorFlag = true;
            } else {
                for (int i = 0; i < len; i++) {
                    map = checkStrtol(inputStrDataVar[0], endptr);
                    bErrorFlag = (boolean) map.get(bErrorFlagName);
                    if (bErrorFlag) {
                        break;
                    }
                }
            }
            Validations.check(bErrorFlag, retCodeConfig.getInvalidSmplSetting());
        }

        // "If the Policy Name is "Slot", the wafer Count / Slot Number should be one or more kinds of integer data."
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOP, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_BOTTOM, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_RANDOM, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_TOPANDBOTTOM, samplingPolicyName)
                || CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_SLOT, samplingPolicyName)) {
            boolean bErrorFlag = false;
            for (int i = 0; i < len; i++) {
                map = checkStrtol(inputStrDataVar[0], endptr);
                value = (Long) map.get(valueName);
                if (value < 1 || value > capacity) {
                    bErrorFlag = true;
                    break;
                }
            }
            Validations.check(bErrorFlag, retCodeConfig.getInvalidSmplSetting());
        }

        // "If the Policy Name is "Slot", the wafer Count / Slot Number should not specify duplicate integer data. "
        if (CimStringUtils.equals(BizConstant.SP_SAMPLING_POLICY_SLOT, samplingPolicyName)) {
            boolean bErrorFlag = false;
            for (int i = 0; i < len; i++) {
                for (int j = i + 1; j < len; j++) {
                    if (inputStrDataVar[i].equals(inputStrDataVar[j])) {
                        bErrorFlag = true;
                        break;
                    }
                }
                if (bErrorFlag) {
                    break;
                }
            }
            Validations.check(bErrorFlag, retCodeConfig.getInvalidSmplSetting());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param inputStrDataVar
     * @param endptr          -
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author Nyx
     * @date 2018/12/20 15:58
     */
    private Map<String, Object> checkStrtol(String inputStrDataVar, StringBuilder endptr) {
        Map<String, Object> map = new HashMap<>();
        Long value = null;
        boolean bErrorFlag = false;
        try {
            value = BaseStaticMethod.strtol(inputStrDataVar, endptr);
        } catch (Exception e) {
            bErrorFlag = true;
        }
        if (!CimStringUtils.isEmpty(endptr.toString())) {
            bErrorFlag = true;
        }
        map.put("value", value);
        map.put("bErrorFlag", bErrorFlag);
        return map;

    }
}
