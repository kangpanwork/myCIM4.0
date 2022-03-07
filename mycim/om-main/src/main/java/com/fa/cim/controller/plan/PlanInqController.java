package com.fa.cim.controller.plan;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.plan.IPlanInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.plan.IPlanInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/18       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/10/18 22:38
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@RequestMapping("/plan")
@Listenable
public class PlanInqController implements IPlanInqController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IPlanInqService planInqService;

    @ResponseBody
    @RequestMapping(value = "/lot_plan_change_reserve_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCHEDULE_CHANGERESERVATION_LIST_INQ)
    public Response lotPlanChangeReserveListInq(@RequestBody Params.LotPlanChangeReserveListInqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.SCHEDULE_CHANGERESERVATION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> productIDs = new ArrayList<>();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        if (CimStringUtils.equals(params.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT) && !CimStringUtils.isEmpty(params.getObjectID())) {
            lotIDs.add(new ObjectIdentifier(params.getObjectID()));
        }
        if (!CimStringUtils.isEmpty(params.getProductID())) {
            productIDs.add(new ObjectIdentifier(params.getProductID()));
        }
        if (!CimStringUtils.isEmpty(params.getRouteID())) {
            routeIDs.add(new ObjectIdentifier(params.getRouteID()));
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setProductIDList(productIDs);
        accessControlCheckInqParams.setRouteIDList(routeIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // 【step3】call txLotPlanChangeReserveListInq__110
        Results.LotPlanChangeReserveListInqResult lotPlanChangeReserveListInqResult = planInqService.sxLotPlanChangeReserveListInq(objCommon, params);
        return Response.createSucc(transactionID, lotPlanChangeReserveListInqResult);
    }
}