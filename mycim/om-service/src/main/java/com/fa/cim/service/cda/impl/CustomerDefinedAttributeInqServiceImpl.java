package com.fa.cim.service.cda.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.*;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.method.IPersonMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.service.cda.ICustomerDefinedAttributeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 18:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class CustomerDefinedAttributeInqServiceImpl implements ICustomerDefinedAttributeInqService {

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Override
    public Results.CDAInfoInqResult sxCDAInfoInq(Infos.ObjCommon objCommon, Params.CDAInfoInqParams params) {
        //--------------------------------------------------
        //  Get User Defined Data Attribute Information
        //--------------------------------------------------

        Results.CDAInfoInqResult result = new Results.CDAInfoInqResult();

        Outputs.ObjUserDefinedAttributeInfoGetDROut out = personMethod.userDefinedAttributeInfoGetDR(objCommon, params.getClassID());

        // success
        result.setStrUserDefinedDataSeq(out.getStrUserDefinedDataSeq());

        return result;
    }

    @Override
    public List<Infos.UserData> sxCDAValueInq(Infos.ObjCommon objCommon, Params.CDAValueInqParams params) {
        Inputs.ObjObjectGetIn objObjectGetIn = new Inputs.ObjObjectGetIn();
        objObjectGetIn.setStringifiedObjectReference(params.getStrCDAValueInqInParm().getStringifiedObjectReference());
        objObjectGetIn.setClassName(params.getStrCDAValueInqInParm().getClassName());
        objObjectGetIn.setStrHashedInfoSeq(params.getStrCDAValueInqInParm().getStrHashedInfoSeq());

        //step1 - object_Get
        CimBO baseBO= objectMethod.objectGet(objCommon, objObjectGetIn);

        //step2 - object_userData_Get__101
        return objectMethod.objectUserDataGet(objCommon, params.getStrCDAValueInqInParm(), baseBO);
    }
}
