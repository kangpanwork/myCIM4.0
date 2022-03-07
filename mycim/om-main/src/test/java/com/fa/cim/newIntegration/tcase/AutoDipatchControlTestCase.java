package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.dispatch.DispatchInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/19          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/19 12:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class AutoDipatchControlTestCase {

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private DispatchInqController dispatchInqController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public void autoDispatchControlUpdate(List<Infos.AutoDispatchControlUpdateInfo> autoDispatchControlUpdateInfoList, ObjectIdentifier lotID){
        Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams = new Params.AutoDispatchConfigModifyReqParams();
        autoDispatchConfigModifyReqParams.setUser(getUser());
        Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo = new Infos.LotAutoDispatchControlUpdateInfo();
        lotAutoDispatchControlUpdateInfo.setLotID(lotID);
        lotAutoDispatchControlUpdateInfo.setAutoDispatchControlUpdateInfoList(autoDispatchControlUpdateInfoList);
        autoDispatchConfigModifyReqParams.setLotAutoDispatchControlUpdateInfoList(Arrays.asList(lotAutoDispatchControlUpdateInfo));
        dispatchController.autoDispatchConfigModifyReq(autoDispatchConfigModifyReqParams);
    }

    public List<Infos.LotAutoDispatchControlInfo> getLotAutoDispatchControlInfo(ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber){
        Params.AutoDispatchConfigInqParams autoDispatchConfigInqParams = new Params.AutoDispatchConfigInqParams();
        autoDispatchConfigInqParams.setLotID(lotID);
        autoDispatchConfigInqParams.setOperationNumber(operationNumber);
        autoDispatchConfigInqParams.setRouteID(routeID);
        autoDispatchConfigInqParams.setUser(getUser());
        return ((Results.AutoDispatchConfigInqResult)dispatchInqController.AutoDispatchConfigInq(autoDispatchConfigInqParams).getBody()).getLotAutoDispatchControlInfoList();
    }
}