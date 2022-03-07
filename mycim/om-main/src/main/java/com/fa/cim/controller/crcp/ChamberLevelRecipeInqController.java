package com.fa.cim.controller.crcp;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.crcp.IChamberLevelRecipeInqController;
import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.crcp.IChamberLevelRecipeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description: chamber level recipe inq controller
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/26          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/26 15:12
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/prc")
@Listenable
public class ChamberLevelRecipeInqController implements IChamberLevelRecipeInqController {

    @Autowired
    private IChamberLevelRecipeInqService chamberLevelRecipeInqService;

    @Autowired
    private IAccessInqService accessInqService;

    @ResponseBody
    @PostMapping(value = "/chamber_level_recipe_designated/inq")
    @Override
    @CimMapping(TransactionIDEnum.CRCP_DESIGNATED_INQ)
    public Response chamberLevelRecipeDesignatedInq(@RequestBody ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam) {
        String txId = TransactionIDEnum.CRCP_DESIGNATED_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step-1:txAccessControlCheckInq;
        if (log.isDebugEnabled()) {
            log.debug("【Step-3】txAccessControlCheckInq(...)");
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setUser(chamberLevelRecipeReserveParam.getUser());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                accessControlCheckInqParams.getUser(), accessControlCheckInqParams);


        //Step-2:chamberLevelRecipeDesignatedInq;
        if (log.isDebugEnabled()) {
            log.debug("【Step-4】call-chamberLevelRecipeDesignatedInq(...)");
        }
        List<Infos.StartCassette> result =
                chamberLevelRecipeInqService.sxChamberLevelRecipeDesignatedInq(objCommon, chamberLevelRecipeReserveParam);
        return Response.createSucc(txId, result);
    }
}
