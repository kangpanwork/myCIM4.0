package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.controller.lotstart.LotStartInqController;
import com.fa.cim.controller.plan.PlanController;
import com.fa.cim.controller.plan.PlanInqController;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/16                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/16 14:12
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotScheduleTestCase {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private PlanController scheduleController;

    @Autowired
    private PlanInqController scheduleInqController;

    @Autowired
    private LotStartInqController lotStartInqController;

    public Response newProdOrderCreateReq(Params.NewProdOrderCreateReqParams params){
        params.setUser(testUtils.getUser());
        return scheduleController.newProdOrderCreateReq(params);
    }

    public Response lotPlanChangeReq(Params.LotScheduleChangeReqParams params){
        params.setClaimMemo("");
        params.setUser(testUtils.getUser());
        return scheduleController.lotPlanChangeReq(params);
    }

    public Response getReleasedLotList(String productID,String subLotType){
        Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams = new Params.ProductOrderReleasedListInqParams();
        productOrderReleasedListInqParams.setUser(testUtils.getUser());
        productOrderReleasedListInqParams.setSearchCondition(new SearchCondition());
        productOrderReleasedListInqParams.setProductID(productID);
        if (!CimStringUtils.isEmpty(subLotType)){
            productOrderReleasedListInqParams.setSubLotType(subLotType);
        }
        productOrderReleasedListInqParams.setLotType("Production");
        Response result = lotStartInqController.productOrderReleasedListInq(productOrderReleasedListInqParams);
        return result;
    }

    public Response newProdOrderCancelReq(Params.NewProdOrderCancelReqParams params){
        params.setClaimMemo("");
        params.setUser(testUtils.getUser());
        return scheduleController.newProdOrderCancelReq(params);
    }

    public Response newProdOrderModifyReq(Params.NewProdOrderModifyReqParams params){
        params.setUser(testUtils.getUser());
        return scheduleController.newProdOrderModifyReq(params);
    }

    public Response prodOrderChangeReq(Params.ProdOrderChangeReqParams params){
        params.setUser(testUtils.getUser());
        params.setClaimMemo("");
        return scheduleController.prodOrderChangeReq(params);
    }
}
