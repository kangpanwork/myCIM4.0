package com.fa.cim.service.access.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.method.IOwnerMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.service.access.IAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@OmService
@Slf4j
public class AccessServiceImpl implements IAccessService {

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IOwnerMethod ownerMethod;

    public Results.OwnerChangeReqResult sxOwnerChangeReq(List<Infos.OwnerChangeObject> strOwnerChangeObjectSeq, Infos.ObjCommon objCommon, Infos.OwnerChangeReqInParm strOwnerChangeReqInParm, Infos.OwnerChangeDefinition strOwnerChangeDefinition, Infos.OwnerChangeDefObjDefinition strOwnerChangeDefObjDefinition, String claimMemo,CimBO bo) {

        //init
        Results.OwnerChangeReqResult out = new Results.OwnerChangeReqResult();

        int count = 0;
        List<Infos.OwnerChangeObject> aOwnerChangeObjectSeq = new ArrayList<>();
        List<Infos.OwnerChangeErrorInfo> strOwnerChangeErrorInfoSeq = new ArrayList<>();
        out.setStrOwnerChangeErrorInfoSeq(strOwnerChangeErrorInfoSeq);
        Infos.OwnerChangeErrorInfo ownerChangeErrorInfo = new Infos.OwnerChangeErrorInfo();
        strOwnerChangeErrorInfoSeq.add(ownerChangeErrorInfo);

        //=========================================================================
        // Change request of object
        //=========================================================================
        if (!CimObjectUtils.isEmpty(bo)){
            //---------------------------------------------
            // Get class ID information
            //---------------------------------------------
            //【step1】 - object_classIDInfo_GetDR
            log.info("call object_classIDInfo_GetDR()");
            Outputs.ObjectClassIDInfoGetDROut getDROut = null;
            try {
                getDROut = objectMethod.ObjectClassIDInfoGetDR(objCommon, bo);
            } catch (ServiceException e){
                log.error("object_classIDInfo_GetDR() rc != RC_OK");
                ownerChangeErrorInfo.setObjectName(CimObjectUtils.isEmpty(getDROut) ? null : getDROut.getClassName());
                List<Infos.HashedInfo> hashedInfoList = new ArrayList<>();
                ownerChangeErrorInfo.setStrHashedInfoSeq(hashedInfoList);
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfoList.add(hashedInfo);
                hashedInfo.setHashData("bo:getIdentifier");
                hashedInfo.setHashKey("OBJ");
                e.setData(ownerChangeErrorInfo);
                throw e;
            }
            //---------------------------------------------
            // Update Owner information
            //---------------------------------------------
            log.info("call ownerChange_Update()");
            //【step2】 - ownerChange_Update
            Inputs.ObjOwnerChangeUpdateIn in = new Inputs.ObjOwnerChangeUpdateIn();
            in.setStringifiedObjectReference("bo:getIdentifier");
            in.setStrOwnerChangeDefinition(strOwnerChangeDefinition);
            in.setStrOwnerChangeReqInParm(strOwnerChangeReqInParm);
            Boolean ownerChangeUpdate = null;
            try {
                ownerChangeUpdate = ownerMethod.ownerChangeUpdate(objCommon,in,bo);
            } catch (ServiceException e){
                ownerChangeErrorInfo.setObjectName(getDROut.getClassName());
                ownerChangeErrorInfo.setStrHashedInfoSeq(getDROut.getStrHashedInfoSeq());
                e.setData(ownerChangeErrorInfo);
                throw e;
            }
            //updated
            if (CimBooleanUtils.isTrue(ownerChangeUpdate)){
                Infos.OwnerChangeObject ownerChangeObject = new Infos.OwnerChangeObject();
                aOwnerChangeObjectSeq.add(count,ownerChangeObject);
                ownerChangeObject.setObjectName(getDROut.getClassName());
                StringBuilder keyString = new StringBuilder("");
                for (int i = 0; i < CimArrayUtils.getSize(getDROut.getStrHashedInfoSeq()); i++) {
                    if (i > 0){
                        keyString.append(".");
                    }
                    keyString.append(getDROut.getStrHashedInfoSeq().get(i).getHashKey());
                    keyString.append(":");
                    keyString.append(getDROut.getStrHashedInfoSeq().get(i).getHashData());
                }
                ownerChangeObject.setHashedInfo(keyString.toString());
            }
        }
        //=========================================================================
        // Change request of user table
        //=========================================================================
        if (CimStringUtils.isNotEmpty(strOwnerChangeDefObjDefinition.getTableName()) && CimStringUtils.isNotEmpty(strOwnerChangeDefObjDefinition.getColumnName())){
            //---------------------------------------------
            // Update table Owner information
            //---------------------------------------------
            //【step3】 - ownerChange_defObj_UpdateDR
            Inputs.ObjOwnerChangeDefObjUpdateDRIn input = new Inputs.ObjOwnerChangeDefObjUpdateDRIn();
            input.setFromOwnerID(strOwnerChangeReqInParm.getFromOwnerID());
            input.setToOwnerID(strOwnerChangeReqInParm.getToOwnerID());
            input.setStrOwnerChangeDefObjDefinition(strOwnerChangeDefObjDefinition);
            List<Infos.OwnerChangeObject> ownerChangeDefObjUpdateDR = null;
            try {
                ownerChangeDefObjUpdateDR = ownerMethod.ownerChangeDefObjUpdateDR(objCommon,input);
            } catch (ServiceException e){
                log.error("ownerChange_defObj_UpdateDR() rc != RC_OK");
                ownerChangeErrorInfo.setObjectName(strOwnerChangeDefObjDefinition.getTableName());
                e.setData(ownerChangeErrorInfo);
                throw e;
            }
            for (int i = 0; i < CimArrayUtils.getSize(ownerChangeDefObjUpdateDR); i++) {
                aOwnerChangeObjectSeq.add(count,ownerChangeDefObjUpdateDR.get(i));
            }
        }
        return out;
    }

}
