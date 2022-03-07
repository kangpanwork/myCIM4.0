package com.fa.cim.method.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.ISamplingMethod;
import com.fa.cim.method.IStartCassetteMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/17       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/12/17 16:13
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class StartCassetteMethod implements IStartCassetteMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ISamplingMethod samplingMethod;

    @Override
    public Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSet(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes, ObjectIdentifier equipmentID) {
        Outputs.ObjStartCassetteProcessJobExecFlagSetOut objStartCassetteProcessJobExecFlagSetOut = new Outputs.ObjStartCassetteProcessJobExecFlagSetOut();
        boolean slotmapConflictFlag = false;
        List<Infos.ObjSamplingMessageAttribute> samplingMessage = new ArrayList<>();
        objStartCassetteProcessJobExecFlagSetOut.setSamplingMessage(samplingMessage);
        for (Infos.StartCassette startCassette : startCassettes) {
            List<Infos.LotInCassette> lotInCassettes = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassettes)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                ObjectIdentifier lotID = lotInCassette.getLotID();
                if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    //--------------------------------------------------------------
                    // Check and set processobExecFlag with RecycleSampling Setting.
                    //---------------------------------------------------------------
                    try {
                        Infos.LotInCassette samplingOut = lotMethod.lotProcessJobExecFlagGetRecycleSampling(objCommon, lotInCassette, lotID);
                        log.info("lot_processJobExecFlag_GetRecycleSampling() : rc == RC_OK");
                        // Set processJobExecFlag by recycle sampling setting.
                        lotInCassette = samplingOut;
                        continue;
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getIgnoreRecycleSmpl(), e.getCode()) && !Validations.isEquals(retCodeConfig.getNoSmplSetting(), e.getCode())) {
                            addObjSamplingMessageAttribute(samplingMessage, objCommon, lotID, BizConstant.SP_SAMPLING_ERROR_MAIL, e.getMessage());
                            e.setData(objStartCassetteProcessJobExecFlagSetOut);
                            throw e;
                        }
                        if (Validations.isEquals(retCodeConfig.getIgnoreRecycleSmpl(), e.getCode())) {
                            log.error("lot_processJobExecFlag_GetRecycleSampling() : rc == RC_IGNORE_RECYCLE_SMPL");
                            addObjSamplingMessageAttribute(samplingMessage, objCommon, lotID, BizConstant.SP_SAMPLING_IGNORED_MAIL, e.getMessage());
                        }
                    }

                    //----------------------------------------------------------
                    // advanced wafer sampling
                    //----------------------------------------------------------
                    try {
						Infos.AdvancedWaferSamplingConvertInfo samplingOut = samplingMethod
								.lotProcessAdvancedWaferSampling(objCommon, lotInCassette, equipmentID);
						if (samplingOut.isHitSampling()) {
							if (log.isInfoEnabled()) {
								log.info("lotProcessAdvancedWaferSampling() : rc == RC_OK");
							}
							continue;
						}
                    } catch (ServiceException e) {
						if (Validations.isEquals(retCodeConfig.getSmplInvalidSlotSelect(), e.getCode())) {
							samplingMessage(samplingMessage, lotID, BizConstant.SP_SAMPLING_WARN_MAIL, e.getMessage());
							slotmapConflictFlag = true;
							continue;
						} else {
							throw e;
						}
                    }


                    //--------------------------------------------------------------
                    // RecycleSampling setting is not found or ignored.
                    // Check and set processobExecFlag with PolicySampling Setting.
                    //--------------------------------------------------------------
                    Infos.LotInCassette policySamplingOut = null;
                    try {
                        policySamplingOut = lotMethod.lotProcessJobExecFlagGetPolicySampling(objCommon, lotInCassette, lotID, equipmentID);
                        lotInCassette = policySamplingOut;
                    } catch (ServiceException e) {
                        policySamplingOut = e.getData(Infos.LotInCassette.class);
                        if (!Validations.isEquals(retCodeConfig.getSmplInvalidSlotSelect(), e.getCode()) && !Validations.isEquals(retCodeConfig.getNoSmplSetting(), e.getCode())) {
                            addObjSamplingMessageAttribute(samplingMessage, objCommon, lotID, BizConstant.SP_SAMPLING_ERROR_MAIL, e.getMessage());
                            e.setData(objStartCassetteProcessJobExecFlagSetOut);
                            throw e;
                        }
                        if (Validations.isEquals(retCodeConfig.getSmplInvalidSlotSelect(), e.getCode())) {
                            samplingMessage(samplingMessage, lotID, BizConstant.SP_SAMPLING_WARN_MAIL, e.getMessage());
                            lotInCassette = policySamplingOut;
                            slotmapConflictFlag = true;
                            continue;
                        }

                        //--------------------------------------------------------------
                        // Sampling setting is not found or ignored.
                        // All the wafers in lot will be processed
                        //--------------------------------------------------------------
                        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                        if (!CimArrayUtils.isEmpty(lotWaferList)) {
                            for (Infos.LotWafer lotWafer : lotWaferList) {
                                lotWafer.setProcessJobExecFlag(true);
                            }
                        }
                    }
                }
            }
        }
        objStartCassetteProcessJobExecFlagSetOut.setStartCassettes(startCassettes);
        if(slotmapConflictFlag){
            throw new ServiceException(retCodeConfig.getSmplSlotmapConflictWarn(), objStartCassetteProcessJobExecFlagSetOut);
        }
        return objStartCassetteProcessJobExecFlagSetOut;
    }

    /**
     * description:  组装sampling message 信息
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/7/3 0003 13:35                        YJ                Create
     *
     * @author YJ
     * @date 2021/7/3 0003 13:35
     * @param samplingMessage - sampling 中得信息list
     * @param lotID - lot id
     * @param spSamplingWarnMail - 邮件类型
     * @param message - 消息
     */
	private void samplingMessage(List<Infos.ObjSamplingMessageAttribute> samplingMessage, ObjectIdentifier lotID,
			int spSamplingWarnMail, String message) {
		Infos.ObjSamplingMessageAttribute objSamplingMessageAttribute = new Infos.ObjSamplingMessageAttribute();
		objSamplingMessageAttribute.setLotID(lotID);
		objSamplingMessageAttribute.setMessageType(spSamplingWarnMail);
		objSamplingMessageAttribute.setMessageText(message);
		samplingMessage.add(objSamplingMessageAttribute);
	}

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param samplingMessage
     * @param objCommon
     * @param lotID
     * @param messageType
     * @param messageText         -
     * @return void
     * @author Nyx
     * @date 2018/12/18 17:41
     */
	private void addObjSamplingMessageAttribute(List<Infos.ObjSamplingMessageAttribute> samplingMessage,
			Infos.ObjCommon objCommon, ObjectIdentifier lotID, int messageType, String messageText) {
		String message = lotMethod.lotSamplingMessageCreate(objCommon, lotID, messageType, messageText);
		samplingMessage(samplingMessage, lotID, messageType, message);
	}
}
