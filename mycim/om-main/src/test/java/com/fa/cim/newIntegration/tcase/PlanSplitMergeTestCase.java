package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.controller.psm.PlannedSplitMergeController;
import com.fa.cim.controller.psm.PlannedSplitMergeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.dto.TestInfos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description: PSM test case
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/4 14:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class PlanSplitMergeTestCase {

    @Autowired
    private PlannedSplitMergeController plannedSplitMergeController;

    @Autowired
    private PlannedSplitMergeInqController plannedSplitMergeInqController;

    @Autowired
    private CommonTestCase commonTestCase;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response experimentalLotList(ObjectIdentifier familyLotID, Boolean detailRequireFlag) {
        Params.PSMLotDefinitionListInqParams params = new Params.PSMLotDefinitionListInqParams();
        params.setClaimMemo("");
        params.setDetailRequireFlag(detailRequireFlag);
        params.setLotFamilyID(familyLotID);
        params.setUser(getUser());
        return plannedSplitMergeInqController.psmLotDefinitionListInq(params);
    }

    public void psmLotInfoSetReqCase(TestInfos.PsmUpdateInfo psmUpdateInfo) {
        //【step1】init param
        Params.PSMLotInfoSetReqParams params = new Params.PSMLotInfoSetReqParams();
        String actionTime = CimDateUtils.convertToSpecString(new Timestamp(System.currentTimeMillis()));
        params.setClaimMemo("");
        params.setExecFlag(false);
        params.setActionTimeStamp(actionTime);
        params.setTestMemo("");
        params.setUser(getUser());
        params.setModifyUserID(getUser().getUserID());
        List<Infos.ExperimentalLotDetailInfo> experimentalLotDetailInfoList = new ArrayList<>();
        Infos.ExperimentalLotDetailInfo experimentalLotDetailInfo = new Infos.ExperimentalLotDetailInfo();
        //【step2】query psm list to get split point number
        List<Infos.ExperimentalLotInfo> experimentalLotInfos = (List<Infos.ExperimentalLotInfo>) this.experimentalLotList(psmUpdateInfo.getLotFamilyID(), true).getBody();
        for (int i = 0; i < CimArrayUtils.getSize(experimentalLotInfos); i++) {
            if (CimStringUtils.equals(experimentalLotInfos.get(i).getSplitOperationNumber(),psmUpdateInfo.getSplitOperationNumber())
                    && CimStringUtils.equals(experimentalLotInfos.get(i).getSplitRouteID(),psmUpdateInfo.getSplitRouteID())
                    && CimStringUtils.equals(experimentalLotInfos.get(i).getStrExperimentalLotDetailInfoSeq().get(0).getMergeOperationNumber(),psmUpdateInfo.getMergeOperationNumber())
                    && CimStringUtils.equals(experimentalLotInfos.get(i).getStrExperimentalLotDetailInfoSeq().get(0).getReturnOperationNumber(),psmUpdateInfo.getReturnOperationNumber())){
                experimentalLotDetailInfoList = experimentalLotInfos.get(i).getStrExperimentalLotDetailInfoSeq();
            }
        }
        experimentalLotDetailInfoList.add(experimentalLotDetailInfo);
        //【step3】set experimentalLotDetailInfo
        experimentalLotDetailInfo.setActionTimeStamp(actionTime);
        experimentalLotDetailInfo.setExecFlag(false);
        experimentalLotDetailInfo.setMemo("");
        experimentalLotDetailInfo.setMergeOperationNumber(psmUpdateInfo.getMergeOperationNumber());
        experimentalLotDetailInfo.setReturnOperationNumber(psmUpdateInfo.getReturnOperationNumber());
        experimentalLotDetailInfo.setWaferIDs(psmUpdateInfo.getWaferList());
        experimentalLotDetailInfo.setSiInfo("");
        if (CimBooleanUtils.isTrue(psmUpdateInfo.getDynamicFlag())){
            //【step4】dynamic branch psm
            params.setActionEMail(psmUpdateInfo.getActionEMail());
            params.setActionHold(psmUpdateInfo.getActionHold());
            params.setLotFamilyID(psmUpdateInfo.getLotFamilyID());
            params.setOriginalOperationNumber(psmUpdateInfo.getOriginalOperationNumber());
            params.setOriginalRouteID(psmUpdateInfo.getOriginalRouteID());
            params.setSplitOperationNumber(psmUpdateInfo.getSplitOperationNumber());
            params.setSplitRouteID(psmUpdateInfo.getSplitRouteID());

            experimentalLotDetailInfo.setDynamicFlag(psmUpdateInfo.getDynamicFlag());
            //【step5】get dynamic branch
            List<Infos.DynamicRouteList> dynamicRouteLists = (List<Infos.DynamicRouteList>)commonTestCase.dynamicPathListInqCase("Branch","").getBody();
            experimentalLotDetailInfo.setSubRouteID(dynamicRouteLists.get(0).getRouteID());
        }else {
            //【step6】branch route psm
            params.setActionEMail(psmUpdateInfo.getActionEMail());
            params.setActionHold(psmUpdateInfo.getActionHold());
            params.setLotFamilyID(psmUpdateInfo.getLotFamilyID());
            params.setOriginalOperationNumber(psmUpdateInfo.getOriginalOperationNumber());
            params.setOriginalRouteID(psmUpdateInfo.getOriginalRouteID());
            params.setSplitOperationNumber(psmUpdateInfo.getSplitOperationNumber());
            params.setSplitRouteID(psmUpdateInfo.getSplitRouteID());

            experimentalLotDetailInfo.setDynamicFlag(psmUpdateInfo.getDynamicFlag());
            experimentalLotDetailInfo.setSubRouteID(psmUpdateInfo.getSubRouteID());
        }
        params.setStrExperimentalLotDetailInfoSeq(experimentalLotDetailInfoList);
        plannedSplitMergeController.psmLotInfoSetReq(params);
    }

    public void modifyPsmRecord(List<Infos.ExperimentalLotInfo> experimentalLotInfos){
        if (!CimArrayUtils.isEmpty(experimentalLotInfos)){
            Params.PSMLotInfoSetReqParams params = new Params.PSMLotInfoSetReqParams();
            for (Infos.ExperimentalLotInfo experimentalLotInfo : experimentalLotInfos) {
                experimentalLotInfo.setActionTimeStamp(CimDateUtils.convertToSpecString(new Timestamp(System.currentTimeMillis())));
                params.setStrExperimentalLotDetailInfoSeq(experimentalLotInfo.getStrExperimentalLotDetailInfoSeq());
                params.setUser(getUser());
                params.setTestMemo("");
                params.setSplitRouteID(experimentalLotInfo.getSplitRouteID());
                params.setSplitOperationNumber(experimentalLotInfo.getSplitOperationNumber());
                params.setOriginalRouteID(experimentalLotInfo.getOriginalRouteID());
                params.setOriginalOperationNumber(experimentalLotInfo.getOriginalOperationNumber());
                params.setModifyUserID(getUser().getUserID());
                params.setLotFamilyID(experimentalLotInfo.getLotFamilyID());
                params.setExecFlag(false);
                params.setActionTimeStamp( CimDateUtils.convertToSpecString(new Timestamp(System.currentTimeMillis())));
                params.setClaimMemo("");
                params.setActionHold(experimentalLotInfo.getActionHold());
                params.setActionEMail(experimentalLotInfo.getActionEMail());
            }
            plannedSplitMergeController.psmLotInfoSetReq(params);
        }
    }

    public ObjectIdentifier getSubRouteID(Infos.RouteInfo routeInfo, String splitOperationNum, ObjectIdentifier routeID) {
        ObjectIdentifier subRouteID = null;
        if (routeInfo == null){
            return subRouteID;
        }
        if (CimObjectUtils.equalsWithValue(routeID,routeInfo.getRouteID())){
            if (CimArrayUtils.getSize(routeInfo.getStrOperationInformationList()) == 0 ){
                return subRouteID;
            }
            for (Infos.OperationInformation operationInformation : routeInfo.getStrOperationInformationList()) {
                if (CimStringUtils.equals(splitOperationNum,operationInformation.getOperationNumber())){
                    if (CimArrayUtils.getSize(operationInformation.getStrNestedRouteInfoList()) == 0){
                        //no have branch
                        subRouteID = null;
                    }else {
                        for (Infos.NestedRouteInfo nestedRouteInfo : operationInformation.getStrNestedRouteInfoList()) {
                            if (CimStringUtils.equals("Branch",nestedRouteInfo.getRoutePDType())){
                                subRouteID = nestedRouteInfo.getRouteID();
                                //get the first
                                break;
                            }
                        }
                    }
                }
            }
        }
        return subRouteID;
    }



    public Response psmLotRemoveReqCase(ObjectIdentifier familyLotID, ObjectIdentifier splitRouteID, String splitOperationNum, ObjectIdentifier originalRouteID, String originalOperationNumber) {
        Params.PSMLotRemoveReqParams params = new Params.PSMLotRemoveReqParams();
        params.setClaimMemo("");
        params.setLotFamilyID(familyLotID);
        params.setOriginalOperationNumber(originalOperationNumber);
        params.setOriginalRouteID(originalRouteID);
        params.setSplitOperationNumber(splitOperationNum);
        params.setSplitRouteID(splitRouteID);
        params.setUser(getUser());
        return plannedSplitMergeController.psmLotRemoveReq(params);
    }
}
