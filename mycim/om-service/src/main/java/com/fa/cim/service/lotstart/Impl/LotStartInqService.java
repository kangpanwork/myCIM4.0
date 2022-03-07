package com.fa.cim.service.lotstart.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.OmPage;
import com.fa.cim.common.utils.CimPageUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProductGroupMethod;
import com.fa.cim.method.IProductMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.service.lotstart.ILotStartInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 17:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class LotStartInqService implements ILotStartInqService {

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IProductGroupMethod productGroupMethod;

    @Override
    public Outputs.ObjProductRequestGetDetailOut sxProductOrderInq(Infos.ObjCommon objCommon,Params.ProductOrderInqParams requestInqParams) {
        return productMethod.productRequestGetDetail(objCommon, requestInqParams.getProductRequestId());
    }

    @Override
    public List<Infos.ProductIDListAttributes> sxProductIdListInq(Infos.ObjCommon objCommon, Params.ProductIdListInqInParams params) {
        return productMethod.productSpecificationFillInTxPCQ015DR180(objCommon, params);
    }

    @Override
    public Results.ProductOrderReleasedListInqResult sxProductOrderReleasedListInq(Infos.ObjCommon objCommon, Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams) {
        if (CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getSourceProductID())){
            SearchCondition searchCondition = productOrderReleasedListInqParams.getSearchCondition();
            productOrderReleasedListInqParams.setSearchCondition(null);
            List<Infos.ProdReqListAttribute> allData=new ArrayList<>();
            Optional.ofNullable(productMethod.productIDGetBySourceProductID(objCommon,productOrderReleasedListInqParams)).ifPresent(products->products.forEach(productID->{
                productOrderReleasedListInqParams.setProductID(productID);
                Results.ProductOrderReleasedListInqResult productOrderReleasedListInqResult = productMethod.productRequestGetListDR(objCommon, productOrderReleasedListInqParams);
                allData.addAll(productOrderReleasedListInqResult.getProductReqListAttributePage().getContent());
            }));
            Results.ProductOrderReleasedListInqResult productOrderReleasedListInqResult=new Results.ProductOrderReleasedListInqResult();
            if (searchCondition!=null&&searchCondition.getPage()!=null&&searchCondition.getSize()!=null){
                OmPage<Infos.ProdReqListAttribute> omPage=new OmPage<>();
                omPage.init(CimPageUtils.convertListToPage(allData,searchCondition.getPage(),searchCondition.getSize()));
                productOrderReleasedListInqResult.setProductReqListAttributePage(omPage);
            }else{
                productOrderReleasedListInqResult.setProdReqListAttributeList(allData);
            }
            return productOrderReleasedListInqResult;
        }
        return productMethod.productRequestGetListDR(objCommon, productOrderReleasedListInqParams);
    }

    @Override
    public List<ObjectIdentifier> sxProductRelatedSourceProductInq(Infos.ObjCommon objCommon, Params.ProductRelatedSourceProductInqParams productOrderReleasedListInqParams) {
        return productMethod.sourceProductGet(objCommon,productOrderReleasedListInqParams.getProductID());
    }

    @Override
    public Results.SourceLotListInqResult sxSourceLotListInq(Infos.ObjCommon objCommon, Params.SourceLotListInqParams sourceLotListInqParams) {
        //【step1】get source lot
        log.debug("【step1】get source lot");
        Outputs.ObjLotGetSourceLotsOut objLotGetSourceLotsOut = lotMethod.lotGetSourceLotsDR(objCommon, sourceLotListInqParams);
        Results.SourceLotListInqResult sourceLotListInqResult = new Results.SourceLotListInqResult();
        sourceLotListInqResult.setSourceLotList(objLotGetSourceLotsOut.getSourceLotList());
        sourceLotListInqResult.setStartBankID(objLotGetSourceLotsOut.getStartBankID());
        return sourceLotListInqResult;
    }

    @Override
    public Results.WaferLotStartCancelInfoInqResult sxWaferLotStartCancelInfoInq(Infos.ObjCommon objCommon, Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams) {

        //Check input STBCancelledLotID
        Validations.check(ObjectIdentifier.isEmptyWithValue(waferLotStartCancelInfoInqParams.getStbCancelledLotID()),retCodeConfig.getInvalidInputParam() );
        //Check OM_LOT_START_CANCEL
        String tmpSTBCancelEnv = StandardProperties.OM_LOT_START_CANCEL.getValue();
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_STBCANCEL_ON, tmpSTBCancelEnv), retCodeConfig.getLotStbCancelOff());
        //Get STB Cancel Information
        Outputs.ObjLotSTBCancelInfoOut objLotSTBCancelInfoOut = lotMethod.lotSTBCancelInfoGetDR(objCommon, waferLotStartCancelInfoInqParams.getStbCancelledLotID());

        Results.WaferLotStartCancelInfoInqResult out = new Results.WaferLotStartCancelInfoInqResult();
        out.setStbCancelledLotInfo(objLotSTBCancelInfoOut.getStbCancelledLotInfo());
        out.setNewPreparedLotInfoList(objLotSTBCancelInfoOut.getNewPreparedLotInfoList());
        out.setStbCancelWaferInfoList(objLotSTBCancelInfoOut.getStbCancelWaferInfoList());
        return out;
    }
    @Override
    public List<Infos.ProductGroupIDListAttributes> sxAllProductGroupListInq(Infos.ObjCommon objCommon, Params.AllProductGroupListInq param){

        //step1 - productGroup_listAttributes_GetDR
        List<Infos.ProductGroupIDListAttributes> productGroupListAttributes;
        try{
            productGroupListAttributes = productGroupMethod.productGroupListAttributesGetDR(objCommon);
        }catch (ServiceException ex){
            if (Validations.isEquals(retCodeConfig.getSomeProductGroupDataError(), ex.getCode())) {
                throw new ServiceException(new OmCode(ex.getCode(), ex.getMessage()));
            }
            throw ex;
        }
        return productGroupListAttributes;
    }
}