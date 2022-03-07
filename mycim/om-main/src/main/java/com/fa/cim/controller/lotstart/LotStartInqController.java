package com.fa.cim.controller.lotstart;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.lotStart.ILotStartInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.service.lotstart.ILotStartInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:58
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/lotstart")
@Listenable
public class LotStartInqController implements ILotStartInqController {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IBankInqService productOrderInqService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IBankInqService allProductGroupListInqService;
    @Autowired
    private ILotStartInqService lotStartInqService;

    @ResponseBody
    @RequestMapping(value = "/product_order/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PRODUCT_REQUEST_INQ)
    public Response productOrderInq(@RequestBody Params.ProductOrderInqParams requestInqParams) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.PRODUCT_REQUEST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestInqParams.getUser(), accessControlCheckInqParams);

        //step3: call sxProductOrderInq(...);
        Outputs.ObjProductRequestGetDetailOut result = lotStartInqService.sxProductOrderInq(objCommon, requestInqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/product_id_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PRODUCT_ID_LIST_INQ)
    public Response productIdListInq(@RequestBody Params.ProductIdListInqInParams params) {
        //init params
        final String transactionID = TransactionIDEnum.PRODUCT_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call txBankListInq(...)
        List<Infos.ProductIDListAttributes> result = null;
        try {
            result = lotStartInqService.sxProductIdListInq(objCommon, params);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getSomeProdspDataError(), e.getCode())) {
                throw e;
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/product_order_released_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RELEASE_LOT_LIST_INQ_REQ)
    public Response productOrderReleasedListInq(@RequestBody Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.RELEASE_LOT_LIST_INQ_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step1 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, productOrderReleasedListInqParams.getUser(), accessControlCheckInqParams);
        //step2 call sxProductOrderReleasedListInq(...)
        Results.ProductOrderReleasedListInqResult result = lotStartInqService.sxProductOrderReleasedListInq(objCommon, productOrderReleasedListInqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/product_related_source_product/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PRODUCT_RELATED_SOURCE_PRODUCT_INQ)
    public Response productRelatedSourceProductInq(@RequestBody Params.ProductRelatedSourceProductInqParams productOrderReleasedListInqParams) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.PRODUCT_RELATED_SOURCE_PRODUCT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step1 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setProductIDList(CimArrayUtils.generateList(productOrderReleasedListInqParams.getProductID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, productOrderReleasedListInqParams.getUser(), accessControlCheckInqParams);
        //step2 call sxProductRelatedSourceProductInq(...)
        List<ObjectIdentifier> result = lotStartInqService.sxProductRelatedSourceProductInq(objCommon, productOrderReleasedListInqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/source_lot_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SOURCE_LOT_INQ)
    public Response sourceLotListInq(@RequestBody Params.SourceLotListInqParams sourceLotListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.SOURCE_LOT_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getProductIDList().add(sourceLotListInqParams.getProductID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, sourceLotListInqParams.getUser(), accessControlCheckInqParams);

        // call SourceLotListInq() service
        log.debug("【step3】call txSourceLotListInq()");
        Results.SourceLotListInqResult result = lotStartInqService.sxSourceLotListInq(objCommon, sourceLotListInqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_lot_start_cancel_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STB_CANCEL_INFO_INQ)
    public Response WaferLotStartCancelInfoInq(@RequestBody Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.STB_CANCEL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(waferLotStartCancelInfoInqParams.getStbCancelledLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, waferLotStartCancelInfoInqParams.getUser(), accessControlCheckInqParams);

        //STEP-4: call sxWaferLotStartCancelInfoInq;
        Results.WaferLotStartCancelInfoInqResult result = lotStartInqService.sxWaferLotStartCancelInfoInq(objCommon, waferLotStartCancelInfoInqParams);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_product_group_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PRODUCT_GROUP_ID_LIST_INQ)
    public Response allProductGroupListInq(@RequestBody Params.AllProductGroupListInq param) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.PRODUCT_GROUP_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);

        //step3 - txAllProductGroupListInq
        List<Infos.ProductGroupIDListAttributes> allProductGroupListInq = lotStartInqService.sxAllProductGroupListInq(objCommon, param);
        return Response.createSucc(transactionID, allProductGroupListInq);
    }
}