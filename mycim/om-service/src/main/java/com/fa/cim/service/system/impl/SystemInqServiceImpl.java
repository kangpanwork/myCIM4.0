package com.fa.cim.service.system.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ICodeMethod;
import com.fa.cim.method.IEnvironmentMethod;
import com.fa.cim.service.system.ISystemInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SystemInqServiceImpl implements ISystemInqService {

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    IEnvironmentMethod environmentMethod;

    @Override
    public List<Infos.CodeInfo> sxCodeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier category) {
        return codeMethod.codeListGetDR(objCommon,category);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/8 15:37
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<Results.OMSEnvInfoInqResult>
     */
    @Override
    public Results.OMSEnvInfoInqResult sxOMSEnvInfoInq(Infos.ObjCommon objCommon) {
        Results.OMSEnvInfoInqResult omsEnvInfoInqResult = new Results.OMSEnvInfoInqResult();
        List<Infos.EnvVariableList> environmentVariableRetCode = environmentMethod.environmentVariableGet(objCommon);
        omsEnvInfoInqResult.setPptEnvVariableList(environmentVariableRetCode);
        return omsEnvInfoInqResult;
    }

    @Override
    public List<Infos.ReasonCodeAttributes> sxReasonCodeListByCategoryInq(Infos.ObjCommon objCommon, String codeCategory) {
        List<Results.ReasonCodeResult> codes = new ArrayList<>();
        // step 1 - Check PosCategory, get PosCode
        return codeMethod.codeFillInTxPLQ010DR(objCommon, codeCategory);
    }
}
