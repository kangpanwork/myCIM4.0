package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>ReticleInformationCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/9/009 09:40
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleInformationCase {
    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);

    public void DRB1_3_2SearchReticleWitWrongID(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(ObjectIdentifier.buildWithValue("wrongID"));
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        if (!CimObjectUtils.isEmpty(result)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }
}
