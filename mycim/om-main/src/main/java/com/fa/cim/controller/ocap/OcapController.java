package com.fa.cim.controller.ocap;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.ocap.IOcapController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.ocap.IOcapService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/1/22         ********            Nyx                create file
 *
 * @author: hd
 * @date: 2021/1/22 13:50
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IOcapController.class, confirmableKey = "OcapConfirm", cancellableKey = "OcapCancel")
@RequestMapping("/ocap")
@Listenable
public class OcapController implements IOcapController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IOcapService ocapService;


    @ResponseBody
    @RequestMapping(value = "/ocap_update/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.OCAP_UPDATE_RPT)
    public Response ocapUpdateRpt(@RequestBody Params.OcapReqParams ocapReqParams) {

        if (log.isDebugEnabled()) {
            log.debug("step1 : Initialising transactionID");
        }
        final String transactionID = TransactionIDEnum.OCAP_UPDATE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) {
            log.debug("step2 : Privilege Check machineID and RecipeList");
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(ocapReqParams.getEquipmentID());
        accessControlCheckInqParams.setMachineRecipeIDList(Collections.singletonList(ocapReqParams.getRecipeID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, ocapReqParams.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) {
            log.debug("step3 : Main Process. call ocapUpdateRpt");
        }
        ocapService.ocapUpdateRpt(objCommon, ocapReqParams);
        return Response.createSucc(transactionID, null);
    }
}