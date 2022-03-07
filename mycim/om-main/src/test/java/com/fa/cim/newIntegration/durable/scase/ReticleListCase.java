package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * description:
 * <p>ReticleListCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/9/009 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class ReticleListCase {
    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private STBCase stbCase;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);

    public void DRB1_1_1ReticleListInquiryForAllRticles(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        if (ObjectUtils.isEmpty(result)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_1_2ReticleListInquiryViaLotID(){
        //STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setLotID(lotID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
    }

    public void DRB1_1_3ReticleListInquiryViaDurableStatus(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setReticleStatus("AVAILABLE");
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        if (ObjectUtils.isEmpty(result)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_1_4ReticleListInquiryViaEquipmentID(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(testCommonData.getEQUIPMENTID3());
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        if (ObjectUtils.isEmpty(result)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_1_5ReticleListInquiryViaEquipmentIDAndLotID(){
        //STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(testCommonData.getEQUIPMENTID3());
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setLotID(lotID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
    }

    public void DRB1_1_6ReticleListInquiryViaReticleID(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(testCommonData.getRETICLEID());
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
        if (ObjectUtils.isEmpty(result)){
            throw new ServiceException(retCodeConfig.getError());
        }
    }

    public void DRB1_1_7ReticleListInquiryViaReticlePartNO(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(emptyID);
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        reticleListInqParams.setReticlePartNumber("10");
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
    }

    public void DRB1_1_7ReticleListInquiryViaReticleGroupID(){
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(getUser());
        reticleListInqParams.setEquipmentID(emptyID);
        reticleListInqParams.setBankID(emptyID);
        reticleListInqParams.setWhiteDefSearchCriteria("ALL");
        reticleListInqParams.setMaxRetrieveCount(300);
        reticleListInqParams.setReticleGroupID(testCommonData.getRETICLEPODID());
        reticleListInqParams.setReticleID(emptyID);
        reticleListInqParams.setLotID(emptyID);
        reticleListInqParams.setDurableSubStatus(emptyID);
        Response response = durableInqController.reticleListInq(reticleListInqParams);
        Results.ReticleListInqResult result = (Results.ReticleListInqResult) response.getBody();
    }

}
