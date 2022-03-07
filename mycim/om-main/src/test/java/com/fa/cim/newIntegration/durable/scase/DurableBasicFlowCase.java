package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>DurableBasicFlowCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/2/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/2/27/027 09:40
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DurableBasicFlowCase {

    @Autowired
    private TestUtils testUtils;

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
    public ObjectIdentifier carrierID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");
    public ObjectIdentifier reticleID = new ObjectIdentifier("Reticle_B05");

    public void DRBCarrierList(){
        Params.CarrierListInqParams params = new Params.CarrierListInqParams();
        params.setMaxRetrieveCount(300L);
        params.setUser(getUser());
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        params.setSearchCondition(searchCondition);
        durableInqController.carrierListInq(params);
    }

    public void DRB3_2_1CarrierInformation(){
        Params.CarrierDetailInfoInqParams params = new Params.CarrierDetailInfoInqParams();
        params.setUser(getUser());
        params.setDurableOperationInfoFlag(true);
        params.setDurableWipOperationInfoFlag(true);
        params.setCassetteID(carrierID);
        durableInqController.carrierDetailInfoInq(params);
    }

    public void DRB1_3_1ReticleInformation(){
        Params.ReticleDetailInfoInqParams params = new Params.ReticleDetailInfoInqParams();
        params.setUser(getUser());
        params.setDurableOperationInfoFlag(true);
        params.setDurableWipOperationInfoFlag(true);
        params.setReticleID(reticleID);
        durableInqController.reticleDetailInfoInq(params);
    }
}
