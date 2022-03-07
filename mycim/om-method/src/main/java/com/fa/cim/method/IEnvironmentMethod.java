package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * This file use to define the IEnvironmentMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/8        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/8 15:39
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEnvironmentMethod {
    /**     
     * description:
     *   Increase the value of define when you add an environment variable.
     *   method:environmentVariable_Get
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/8 15:43
     * @param objCommon -  
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjEnvironmentVariableGetOut>
     */
    List<Infos.EnvVariableList> environmentVariableGet(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/12/4 16:33
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     */
    void environmentVariablSet(Infos.ObjCommon objCommon, Params.OMSEnvModifyReqParams params);
}