package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IUploadInventoryReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IUploadInventoryReqService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 13:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UploadInventoryReqController implements IUploadInventoryReqController {

    @Autowired
    private IUploadInventoryReqService uploadInventoryReqService;
    @Autowired
    private IUtilsComp utilsComp;

    @RequestMapping(value = "/upload_inventroy/req", method = RequestMethod.POST)
    public Response tmsUploadInventoryReq(@RequestBody Params.UploadInventoryReqParmas uploadInventoryReqParmas) {
        log.info("tmsUploadInventoryReq Request Json" + JSON.toJSONString(uploadInventoryReqParmas));
        Results.UploadInventoryReqResult result = new Results.UploadInventoryReqResult();
        final String transactionID = TransactionIDEnum.OM09.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = uploadInventoryReqParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OM09, user);

        log.info("【step1】 - uploadInventoryReqService.sxUploadInventoryReq");
        result = uploadInventoryReqService.sxUploadInventoryReq(objCommon, uploadInventoryReqParmas);
        log.info("tmsUploadInventoryReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
