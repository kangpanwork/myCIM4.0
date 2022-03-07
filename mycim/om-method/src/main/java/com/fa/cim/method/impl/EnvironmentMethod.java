package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IEnvironmentMethod;
import com.fa.cim.newcore.bo.env.CimEnvironmentVariable;
import com.fa.cim.newcore.impl.bo.env.componets.source.SwitchPropertySource;
import com.fa.cim.newcore.impl.bo.env.componets.sync.event.SyncToEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/8        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/8 15:44
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class EnvironmentMethod  implements IEnvironmentMethod{

    @Autowired
    private SwitchPropertySource propertySource;

    @Override
    public List<Infos.EnvVariableList> environmentVariableGet(Infos.ObjCommon objCommon) {
        List<Infos.EnvVariableList> envVariableListList = new ArrayList<>();
        List<CimEnvironmentVariable> environmentVariables = propertySource.allFromLocal();
        if (null == environmentVariables) {
            return envVariableListList;
        }
        for (CimEnvironmentVariable environmentVariable : environmentVariables) {
            Infos.EnvVariableList envVariableList = new Infos.EnvVariableList();
            envVariableList.setEnvValue(environmentVariable.getValue());
            envVariableList.setEnvName(environmentVariable.getName());
            envVariableListList.add(envVariableList);
        }
        return envVariableListList;
    }

    @Override
    public void environmentVariablSet(Infos.ObjCommon objCommon, Params.OMSEnvModifyReqParams params) {
        //---------------------------------
        // Set PPTMGR Environment Variable
        //---------------------------------
        if (CimArrayUtils.isEmpty(params.getStrSvcEnvVariable())) {
            return;
        }
        for (Infos.EnvVariableList envVariableList : params.getStrSvcEnvVariable()) {
            CimEnvironmentVariable cimEnvironmentVariable = propertySource.find(envVariableList.getEnvName());
            cimEnvironmentVariable.setValue(envVariableList.getEnvValue());
            cimEnvironmentVariable.makeSyncToRequired();
            SyncToEvent syncToEvent = new SyncToEvent(this);
            SpringContextUtil.getApplicationContext().publishEvent(syncToEvent);
        }
    }

}