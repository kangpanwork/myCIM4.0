package com.fa.cim.method.impl;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IAMSMethod;
import com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>AMSMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/7/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/7/27/027 17:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class AMSMethod implements IAMSMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public Results.OmsMsgInqResult OmsMsgInfoGet() {
        List<CimMessageDefinition> CimMessageDefinitions = baseCoreFactory.getAllBO(CimMessageDefinition.class);
        Results.OmsMsgInqResult omsMsgInqResult = new Results.OmsMsgInqResult();
        List<Infos.OmsMsgInfo> omsMsgInfoList = new ArrayList<>();
        omsMsgInqResult.setOmsMsgInqResult(omsMsgInfoList);
        for (CimMessageDefinition messageDefinition : CimMessageDefinitions){
            Infos.OmsMsgInfo omsMsgInfo = new Infos.OmsMsgInfo();
            omsMsgInfoList.add(omsMsgInfo);
            omsMsgInfo.setTempFileName(messageDefinition.getTemplateFileName());
            omsMsgInfo.setMessageDistributeType(messageDefinition.getMessageDistributionType());
            omsMsgInfo.setDescription(messageDefinition.getDescription());
            omsMsgInfo.setMessageDefinitionID(messageDefinition.getIdentifier());
            omsMsgInfo.setMessageMediaID(messageDefinition.getMessageMediaID());
            omsMsgInfo.setMessageType(messageDefinition.getMessageType());
            omsMsgInfo.setPrimaryMessage(messageDefinition.getPrimaryMessage());
            omsMsgInfo.setSecondaryMessage(messageDefinition.getSecondaryMessage());
            omsMsgInfo.setSubSystem(messageDefinition.getSubsystem());
        }
        return omsMsgInqResult;
    }
}
