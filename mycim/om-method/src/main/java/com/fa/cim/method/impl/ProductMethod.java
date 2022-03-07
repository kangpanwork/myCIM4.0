package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.OmPage;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.entity.runtime.lottype.CimLotTypeSubLotTypeDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productrequest.CimProductRequestDO;
import com.fa.cim.entity.runtime.productrequest.CimProductRequestSourceLotDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationSourceDO;
import com.fa.cim.entity.runtime.technology.CimTechnologyDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProductMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.planning.CimLotSchedule;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.planning.PlanManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.ProductSpecificationManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.planning.PlanDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mchnmngm.MaterialLocation;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.newcore.standard.prdctspc.ProductSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 * 0
 *
 * @author Bear
 * @since 2018/6/21 10:31
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ProductMethod  implements IProductMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ProductSpecificationManager productSpecificationManager;

    @Autowired
    private PersonManager personManager;

    @Autowired
    private PlanManager planManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *                                                         Sun Update the second input parameter from String to ObjectIdentifier
     * @param objCommon -
     * @param productRequestID -
     * @return com.fa.cim.pojo.obj.Outputs.ObjProductRequestGetDetailOut
     * @author NYX
     * @since 2018/5/7
     */
    @Override
    public Outputs.ObjProductRequestGetDetailOut productRequestGetDetail(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID) {
        Outputs.ObjProductRequestGetDetailOut productOrderInq = new Outputs.ObjProductRequestGetDetailOut();
        com.fa.cim.newcore.bo.planning.CimProductRequest productRequestBO = baseCoreFactory.getBO(com.fa.cim.newcore.bo.planning.CimProductRequest.class, productRequestID);
        Validations.check(productRequestBO == null, retCodeConfig.getNoProductRequest());
        Infos.ProdReqInq prodReqInq = new Infos.ProdReqInq();
        prodReqInq.setLotID(new ObjectIdentifier(productRequestBO.getIdentifier(), productRequestBO.getPrimaryKey()));
        prodReqInq.setLotType(productRequestBO.getLotType());
        prodReqInq.setProductID(new ObjectIdentifier(productRequestBO.getProductSpecification().getIdentifier(), productRequestBO.getProductSpecification().getPrimaryKey()));
        prodReqInq.setDueTimeStamp(CimDateUtils.convertToSpecString(productRequestBO.getDeliveryDateTime()));
        CimProductSpecification aProdSp = baseCoreFactory.getBO(CimProductSpecification.class, prodReqInq.getProductID());
        Validations.check(aProdSp == null, new OmCode(retCodeConfig.getInvalidProdId(), prodReqInq.getProductID().getValue()));
        prodReqInq.setProductType(aProdSp.getProductType());
        prodReqInq.setProductUsage(aProdSp.getProductUsage());
        com.fa.cim.newcore.bo.prodspec.CimProductGroup aProductGroup = aProdSp.getProductGroup();
        Validations.check(aProductGroup == null, new OmCode(retCodeConfig.getNotFoundProductGroup(), ""));
        prodReqInq.setProductGroupID(new ObjectIdentifier(aProductGroup.getIdentifier(), aProductGroup.getPrimaryKey()));
        com.fa.cim.newcore.bo.prodspec.CimTechnology aTechnology = aProductGroup.getTechnology();
        Validations.check(aTechnology == null, new OmCode(retCodeConfig.getNotFoundTechnology(), ""));
        prodReqInq.setTechnologyCode(aTechnology.getIdentifier());
        prodReqInq.setPriorityClass(String.valueOf(productRequestBO.getPriorityClass()));
        prodReqInq.setExternalPriority(String.valueOf(productRequestBO.getSchedulePriority()));
        prodReqInq.setOrderNumber(productRequestBO.getOrderNumber());
        prodReqInq.setCustomerCode(productRequestBO.getCustomer().getIdentifier());
        prodReqInq.setReleaseTimeStamp(CimDateUtils.convertToSpecString(productRequestBO.getPlanReleaseDateTime()));
        prodReqInq.setRouteID(new ObjectIdentifier(productRequestBO.getMainProcessDefinition().getIdentifier(), productRequestBO.getMainProcessDefinition().getPrimaryKey()));
        prodReqInq.setStartBankID(new ObjectIdentifier(productRequestBO.getStartBank().getIdentifier(), productRequestBO.getStartBank().getPrimaryKey()));
        prodReqInq.setProductCount(productRequestBO.getProductQuantity());
        prodReqInq.setDepartment(productRequestBO.getDepartmentID());
        prodReqInq.setSection(productRequestBO.getSectionID());
        prodReqInq.setReasonCode(productRequestBO.getReasonCodeID());
        ObjectIdentifier dummy = new ObjectIdentifier(productRequestBO.getIdentifier(), productRequestBO.getPrimaryKey());
        com.fa.cim.newcore.bo.planning.CimProductRequest aProdReq = baseCoreFactory.getBO(com.fa.cim.newcore.bo.planning.CimProductRequest.class, dummy);
        Validations.check(aProdReq == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), "*****"));
        List<PlanDTO.SourceLotEx> theSourceLots = aProdReq.allSourceLots();
        if (!CimObjectUtils.isEmpty(theSourceLots)){
            List<Infos.SourceLotsAttributes> sourceLotsAttributesList = new ArrayList<>();
            prodReqInq.setStrSourceLotsAttributes(sourceLotsAttributesList);
            for (PlanDTO.SourceLotEx sourceLotEx : theSourceLots){
                Infos.SourceLotsAttributes sourceLotsAttributes = new Infos.SourceLotsAttributes();
                sourceLotsAttributesList.add(sourceLotsAttributes);
                sourceLotsAttributes.setLotID(sourceLotEx.getLotID());
                com.fa.cim.newcore.bo.product.CimLot aSourceLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, sourceLotEx.getLotID());
                Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), sourceLotEx.getLotID().getValue()));
                List<MaterialContainer> aMaterialContainerList = aSourceLot.materialContainers();
                if (!CimObjectUtils.isEmpty(aMaterialContainerList)){
                    com.fa.cim.newcore.bo.durable.CimCassette aSourceCassette = (com.fa.cim.newcore.bo.durable.CimCassette) aMaterialContainerList.get(0);
                    MaterialLocation aMatrlLoc = null;
                    if (aSourceCassette != null){
                        sourceLotsAttributes.setCassetteID(new ObjectIdentifier(aSourceCassette.getIdentifier(), aSourceCassette.getPrimaryKey()));
                        sourceLotsAttributes.setTransferStatus(aSourceCassette.getTransportState());
                        aMatrlLoc = aSourceCassette.getLocation();
                        if (aMatrlLoc != null){
                            com.fa.cim.newcore.bo.machine.CimMaterialLocation aPosMtrlLoc = (com.fa.cim.newcore.bo.machine.CimMaterialLocation) aMatrlLoc;
                            MachineDTO.Coordinate3D c3D = aPosMtrlLoc.getCoordinate();
                            sourceLotsAttributes.setShelfPositionX(String.valueOf(c3D.getX()));
                            sourceLotsAttributes.setShelfPositionY(String.valueOf(c3D.getY()));
                            sourceLotsAttributes.setShelfPositionZ(String.valueOf(c3D.getZ()));
                        }
                        Machine aMachine = aSourceCassette.currentAssignedMachine();
                        if (aMachine != null){
                            boolean isStorageBool = aMachine.isStorageMachine();
                            if (isStorageBool){
                                sourceLotsAttributes.setStockerID(new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                            }
                        }
                    }
                }
                List<ObjectIdentifier> waferIDList = sourceLotEx.getWaferIDList();
                if (!CimObjectUtils.isEmpty(waferIDList)){
                    List<Infos.SourceWafersAttributes> sourceWafersAttributesList = new ArrayList<>();
                    sourceLotsAttributes.setSourceWafersAttributesList(sourceWafersAttributesList);
                    int count = 0;
                    for (ObjectIdentifier waferID : waferIDList){
                        com.fa.cim.newcore.bo.product.CimWafer aWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, waferID);
                        Validations.check(aWafer == null, retCodeConfig.getNotFoundWafer());
                        Infos.SourceWafersAttributes sourceWafersAttributes = new Infos.SourceWafersAttributes();
                        sourceWafersAttributesList.add(sourceWafersAttributes);
                        sourceWafersAttributes.setWaferID(new ObjectIdentifier(aWafer.getIdentifier(), aWafer.getPrimaryKey()));
                        sourceWafersAttributes.setGoodUnitCount(String.valueOf(aWafer.getGoodDiceQuantity()));
                        sourceWafersAttributes.setFailUnitCount(String.valueOf(aWafer.getBadDiceQuantity()));
                        if (count == 0){
                            ProductSpecification aPS = aWafer.getProductSpecification();
                            List<ObjectIdentifier> sourceProductIDList = new ArrayList<>();
                            prodReqInq.setSourceProductID(sourceProductIDList);
                            sourceProductIDList.add(new ObjectIdentifier(aPS.getIdentifier(), aPS.getPrimaryKey()));
                        }
                        count++;
                    }
                }
            }
        }
        productOrderInq.setProdReqInq(prodReqInq);
        return productOrderInq;
    }

    @Override
    public Outputs.ObjProductBOMInfoGetOut productBOMInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier productID) {

        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(null == productSpecification,retCodeConfig.getNotFoundProductSpec());
        com.fa.cim.newcore.bo.parts.CimBom bom = productSpecification.getBom();

        Validations.check(CimObjectUtils.isEmpty(bom),new OmCode(retCodeConfig.getBomNotDefined(), productID.getValue()));
        Outputs.ObjProductBOMInfoGetOut objProductBOMInfoGetOut = new Outputs.ObjProductBOMInfoGetOut();
        objProductBOMInfoGetOut.setBomID(ObjectIdentifier.build(bom.getIdentifier(), bom.getPrimaryKey()));
        objProductBOMInfoGetOut.setBomDescription(bom.getDescription());
        return objProductBOMInfoGetOut;
    }

    @Override
    public List<ObjectIdentifier> sourceProductGet(Infos.ObjCommon objCommon, ObjectIdentifier productID) {

        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(null == productSpecification,retCodeConfig.getNotFoundProductSpec());

        return Optional.ofNullable(productSpecification.allSourceProductSpecifications()).orElse(new ArrayList<>()).stream().map(
                cimProductSpecification -> ObjectIdentifier.build(cimProductSpecification.getIdentifier(),cimProductSpecification.getPrimaryKey())).collect(Collectors.toList());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2018/8/3 13:15
     * @param objCommon -
     * @param productOrderReleasedListInqParams -
     * @return com.fa.cim.pojo.Outputs.ObjProductRequestGetLisOut
     */
    @Override
    public  Results.ProductOrderReleasedListInqResult productRequestGetListDR(Infos.ObjCommon objCommon, Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams) {
        SearchCondition searchCondition = new SearchCondition();
        if (null != productOrderReleasedListInqParams.getSearchCondition()) {
            searchCondition.setPage(productOrderReleasedListInqParams.getSearchCondition().getPage());
            searchCondition.setSize(productOrderReleasedListInqParams.getSearchCondition().getSize());
        }

        String sql = " SELECT * FROM   OMPRORDER \n" +
                "                                WHERE ";
        sql += String.format(" PO_PROD_STATE = '%s'", CIMStateConst.CIM_PRRQ_PROD_STATE_NOTINRELEASE);
        // task-3899 ProdOrderID --> productRequstID update Fuzzy query sql of productRequstID
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getProductRequstID()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getProductRequstID())){
            sql += String.format(" AND PROD_ORDER_ID LIKE '%%%s%%'", productOrderReleasedListInqParams.getProductRequstID());
        }
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getLotType()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getLotType())){
            sql += String.format(" AND LOT_TYPE = '%s'", productOrderReleasedListInqParams.getLotType());
        }
        // task-3899 SubLotType --> subLotType update Fuzzy query sql of subLotType
        if(CimStringUtils.isNotEmpty( productOrderReleasedListInqParams.getSubLotType()) && !CimStringUtils.equals("%",  productOrderReleasedListInqParams.getSubLotType())){
            sql += String.format(" AND SUB_LOT_TYPE LIKE '%%%s%%'",  productOrderReleasedListInqParams.getSubLotType());
        }
        // task-3899 ProductID --> productID update Fuzzy query sql of productID
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getProductID()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getProductID())){
            sql += String.format(" AND PROD_ID LIKE '%%%s%%'", productOrderReleasedListInqParams.getProductID());
        }
        // task-3899 Main PF --> RouteID update Fuzzy query sql of RouteID
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getRouteID()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getRouteID())){
            sql += String.format(" AND MAIN_PROCESS_ID LIKE '%%%s%%'", productOrderReleasedListInqParams.getRouteID());
        }
        // task-3899 Manufacturing Layer --> manufacturingLayerID update Fuzzy query sql of manufacturingLayerID
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getManufacturingLayerID()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getManufacturingLayerID())){
            sql += String.format(" AND MFG_LAYER LIKE '%%%s%%'", productOrderReleasedListInqParams.getManufacturingLayerID());
        }
        // task-3899 BankID --> bankID update Fuzzy query sql of bankID
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getStartBankID()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getStartBankID())){
            sql += String.format(" AND START_BANK_ID LIKE '%%%s%%'", productOrderReleasedListInqParams.getStartBankID());
        }
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getPriorityClass())){
            sql += String.format(" AND LOT_PRIORITY = %s", productOrderReleasedListInqParams.getPriorityClass());
        }
        // task-3899 OrderNumber --> orderNumber update Fuzzy query sql of orderNumber
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getOrderNumber()) && !CimStringUtils.equals("%", productOrderReleasedListInqParams.getOrderNumber())){
            sql += String.format(" AND MFG_ORDER_NO LIKE '%%%s%%'", productOrderReleasedListInqParams.getOrderNumber());
        }
        // task-3899 CustomerID --> customerID update Fuzzy query sql of customerID
        if(CimStringUtils.isNotEmpty( productOrderReleasedListInqParams.getCustomerCode()) && !CimStringUtils.equals("%",  productOrderReleasedListInqParams.getCustomerCode())){
            sql += String.format(" AND CUSTOMER_ID LIKE '%%%s%%'",  productOrderReleasedListInqParams.getCustomerCode());
        }
        if(CimStringUtils.isNotEmpty(productOrderReleasedListInqParams.getPlanReleaseDate())){
            sql += String.format(" AND PLAN_RELEASE_DATE <=  to_date('%s','yyyy-mm-dd hh24:mi:ss')", productOrderReleasedListInqParams.getPlanReleaseDate());
        }

        sql +=  " ORDER BY  PLAN_RELEASE_DATE,"
                +"    LOT_PRIORITY,"
//                +"    SCHEDULE_PRIORITY,"
                +"    PROD_ORDER_ID  ";

        Page<CimProductRequestDO> productRequestPage = cimJpaRepository.query(sql, CimProductRequestDO.class, searchCondition);

        if (null == productRequestPage || CimArrayUtils.isEmpty(productRequestPage.getContent())){
            return null;
        }

        List<Infos.ProdReqListAttribute> strProdReqListAttributes = new ArrayList<>();
        for (CimProductRequestDO p : productRequestPage.getContent()) {
            Infos.ProdReqListAttribute prodReqListAttribute = new Infos.ProdReqListAttribute();
            //set strProdReqListAttributes values

            prodReqListAttribute.setLotID(new ObjectIdentifier(p.getProductRequestID(),p.getId()));
            prodReqListAttribute.setLotType(p.getLotType());
            prodReqListAttribute.setSubLotType(p.getSubLotType());
            prodReqListAttribute.setProductID(new ObjectIdentifier(p.getProductSpecificationID(),p.getProductSpecificationObj()));
            prodReqListAttribute.setDueTimeStamp(null == p.getDeliveryTime() ? null : p.getDeliveryTime().toString());
            prodReqListAttribute.setManufacturingLayerID(new ObjectIdentifier(p.getManufacturingLayer(),p.getMainProcessDefinitionObj()));

            String priorityClass = (p.getPriorityClass() == null) ? "" : p.getPriorityClass().toString();
            prodReqListAttribute.setPriorityClass(priorityClass);

            String schedulePriority = (p.getSchedulePriority() == null) ? "" : p.getSchedulePriority().toString();
            prodReqListAttribute.setExternalPriority(schedulePriority);

            prodReqListAttribute.setOrderNumber(p.getOrderNumber());
            prodReqListAttribute.setCustomerCode(p.getCustomerID());
            prodReqListAttribute.setReleaseTimeStamp(null == p.getPlanReleaseTime() ? null : p.getPlanReleaseTime().toString());
            prodReqListAttribute.setRouteID(new ObjectIdentifier(p.getMainProcessDefinitionID(),p.getMainProcessDefinitionObj()));
            prodReqListAttribute.setStartBankID(new ObjectIdentifier(p.getStartBankID(),p.getStartBankObj()));

            Long waferCount = CimObjectUtils.isEmpty(p.getWaferCount()) ? 0L : Long.parseLong(p.getWaferCount().toString());
            prodReqListAttribute.setProductCount(waferCount);

            prodReqListAttribute.setProductRequestName(p.getProductRequestID());
            prodReqListAttribute.setPlanState(p.getProductRequestPlanState());
            prodReqListAttribute.setProdState(p.getProductRequestProductionState());

            Long planPriority = CimObjectUtils.isEmpty(p.getPlanPriority()) ? 0L : Long.parseLong(p.getPlanPriority().toString());
            prodReqListAttribute.setPlanPriority(planPriority);

            prodReqListAttribute.setLotOwnerID(new ObjectIdentifier(p.getLotOwnerID(),p.getLotOwnerObj()));
            prodReqListAttribute.setEndBankID(new ObjectIdentifier(p.getEndBankID(),p.getEndBankObj()));
            prodReqListAttribute.setLotGenType(p.getLotGenerationType());
            prodReqListAttribute.setScheduleMode(p.getScheduleMode());
            prodReqListAttribute.setLotGenMode(p.getLotGenerationMode());
            prodReqListAttribute.setLotComment(p.getLotComment());
            prodReqListAttribute.setClaimUserID(new ObjectIdentifier(p.getClaimUserID(),p.getClaimUserObj()));
            prodReqListAttribute.setClaimedTimeStamp(null == p.getClaimTime() ? null : p.getClaimTime().toString());
            prodReqListAttribute.setLotScheduleID(new ObjectIdentifier(p.getLotScheduleID(),p.getLotScheduleObj()));
            prodReqListAttribute.setOrderType(p.getOrderType());

            if (CimBooleanUtils.isTrue(productOrderReleasedListInqParams.getSourceLotFlag())) {
                log.info("productOrderReleasedListInqParams.sourceLotFlag = {}", productOrderReleasedListInqParams.getSourceLotFlag());

                String querySourceLot = String.format("select * from OMPRORDER_SRCLOT where ID = '%s' ", p.getId());
                List<CimProductRequestSourceLotDO> sourceLotList = cimJpaRepository.query(querySourceLot, CimProductRequestSourceLotDO.class);

                List<ObjectIdentifier> sourceLots = new ArrayList<>();
                for (CimProductRequestSourceLotDO sourceLot : sourceLotList) {
                    sourceLots.add(new ObjectIdentifier(sourceLot.getSourceLotID(), sourceLot.getSourceLotObj()));
                }

                prodReqListAttribute.setSourceLotIDList(sourceLots);
            }

            strProdReqListAttributes.add(prodReqListAttribute);
        }

        OmPage<Infos.ProdReqListAttribute> prodReqListAttributePage = new OmPage<>();
        prodReqListAttributePage.init(productRequestPage);
        prodReqListAttributePage.setContent(strProdReqListAttributes);

        Results.ProductOrderReleasedListInqResult objProductRequestGetLisOut = new Results.ProductOrderReleasedListInqResult();
        objProductRequestGetLisOut.setProductReqListAttributePage(prodReqListAttributePage);

        return objProductRequestGetLisOut;
    }

    @Override
    public List<String> productIDGetBySourceProductID(Infos.ObjCommon objCommon, Params.ProductOrderReleasedListInqParams productOrderReleasedListInqParams) {
        List<Object> objects = cimJpaRepository.queryOneColumn("SELECT\n" +
                "\tDISTINCT f.PROD_ID \n" +
                "from\n" +
                "\tOMPRODINFO f,\n" +
                "\tOMPRODINFO_SRC fs,\n" +
                "\tOMPRORDER f2\n" +
                "where\n" +
                "\tfs.REFKEY = f.ID\n" +
                "\tand f2.PROD_RKEY = f.ID\n" +
                "\tand fs.SRC_PROD_ID =?\n" +
                "\tand f2.START_BANK_ID = ?", productOrderReleasedListInqParams.getSourceProductID(), productOrderReleasedListInqParams.getStartBankID());
        List<String> result=new ArrayList<>();
        Optional.ofNullable(objects).ifPresent(objects1 -> objects1.forEach(obj->result.add(CimObjectUtils.toString(obj))));
        return result;
    }

    /**
     * description:
     * Created lot should have the following status
     * (1) productrequest
     * - Planning State     : Planned
     * - Production State   : Completed
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    - common object
     * @param bankID       - bank id
     * @param sourceLotID  - source lot id
     * @param productCount - product count
     * @param lotType      - lot type
     * @param subLotType   - sub lot type
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjProductRequestForVendorLotReleaseOut>
     * @author Bear
     * @date 2018/4/18
     */
    @Override
    public Outputs.ObjProductRequestForVendorLotReleaseOut productRequestForVendorLotRelease(Infos.ObjCommon objCommon, ObjectIdentifier bankID
            , ObjectIdentifier sourceLotID, int productCount, String lotType, String subLotType) {
        //【step1】get product-id for bankInVendor lot
        log.info("【step1】 get product-id for bankInVendor lot");
        com.fa.cim.newcore.bo.product.CimLot aSourceLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, sourceLotID);
        Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), sourceLotID.getValue()));
        CimProductSpecification aSourceProdSpec = aSourceLot.getProductSpecification();
        Validations.check(aSourceProdSpec == null, retCodeConfig.getNotFoundProductSpec());
        ObjectIdentifier productID = new ObjectIdentifier(aSourceProdSpec.getIdentifier(), aSourceProdSpec.getPrimaryKey());
        // 【step2】  - create product request for lot preparation
        log.info("【step2】 create product request for lot preparation");
        // prepare input parameter of productRequest_Release()
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setProductQuantity(productCount);
        String time = CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp());
        releaseLotAttributes.setPlannedStartTime(time);
        releaseLotAttributes.setPlannedFinishTime(time);
        Outputs.ObjProductRequestReleaseOut objProductRequestReleaseOut = this.productRequestRelease(objCommon, releaseLotAttributes);
        // 【step3】update the productRequest info
        log.info("【step3】update the productRequest info");
        com.fa.cim.newcore.bo.factory.CimBank aPosBank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(aPosBank == null, new OmCode(retCodeConfig.getNotFoundBank(), bankID.getValue()));
        com.fa.cim.newcore.bo.planning.CimProductRequest prdReq = baseCoreFactory.getBO(com.fa.cim.newcore.bo.planning.CimProductRequest.class, objProductRequestReleaseOut.getCreateProductRequest());
        Validations.check(prdReq == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), productID.getValue()));
        prdReq.setAllStatesForVendorLot();
        prdReq.setStartBank(aPosBank);
        prdReq.setEndBank(aPosBank);
        // 【step4】prepare output structure
        log.info("【step4】prepare output structure");
        Outputs.ObjProductRequestForVendorLotReleaseOut objProductRequestForVendorLotReleaseOut = new Outputs.ObjProductRequestForVendorLotReleaseOut();
        ObjectIdentifier createdProductRequestID = new ObjectIdentifier(prdReq.getIdentifier(), prdReq.getPrimaryKey());
        objProductRequestForVendorLotReleaseOut.setCreateProductRequestID(createdProductRequestID);
        return objProductRequestForVendorLotReleaseOut;
    }

    /**
     * description:
     * create new product request object and return objectIdentifier of it
     * if strReleaseLotAttributes.strDirectedSourceLot is specified, it will be
     * set into created product request, and specified wafers are reserved(allocated)
     * for STB which uses this product request
     * if strReleaseLotAttributes.strReleaseLotSchedule is input from input parameter
     * operation schedule is created and it is set into created product request
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *  2019/01/03      Bug-235            Bear
     * @param objCommon
     * @param releaseLotAttributes -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjProductRequestReleaseOut>
     * @author Bear
     * @date 2018/4/19
     */
    @Override
    public Outputs.ObjProductRequestReleaseOut productRequestRelease(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes) {
        // step1 - get object reference of product specification.
        // this ID must be given from input parameter.
        log.info("【step1】get object reference of product specification. the ID must be given from input parameter");
        ObjectIdentifier productID = releaseLotAttributes.getProductID();
        CimProductSpecification aProductSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(aProductSpecification == null, new OmCode(retCodeConfig.getInvalidProdId(), productID.getValue()));
        //-----------------------------------------------------
        // get object reference of Customer.
        //-----------------------------------------------------
        log.info("【step2】get object reference of customer.");
        com.fa.cim.newcore.bo.prodspec.CimCustomer aCustomer = productSpecificationManager.findCustomerNamed(releaseLotAttributes.getCustomerCode());
        // 【step3】get object reference of lot Owner.
        log.info("【step3】get object reference of lot Owner.");
        com.fa.cim.newcore.bo.person.CimPerson aLotOwner = null;
        String lotOwner = releaseLotAttributes.getLotOwner();
        if (!CimStringUtils.isEmpty(lotOwner)) {
            log.info("the lotOwner is not blank...");
            aLotOwner = personManager.findPersonNamed(lotOwner);
            Validations.check(aLotOwner == null, new OmCode(retCodeConfig.getNotFoundPerson(), lotOwner));
        }
        // 【step4】get object reference of request user.
        log.info("【step4】get object reference of request user.");
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        //【step5】get object reference of main process definition...
        log.info("【step5】get object reference of main process definition");
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainProcessDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class, releaseLotAttributes.getRouteID());
        String aMfgLayer = null;
        com.fa.cim.newcore.bo.factory.CimBank aStartBank = null;
        com.fa.cim.newcore.bo.factory.CimBank aEndBank = null;
        if (null != aMainProcessDefinition) {
            log.info("mainProcessDefinition is not null...");
            //-----------------------------------------------------
            // get object reference of MFG Layer.
            //-----------------------------------------------------
            aMfgLayer = aMainProcessDefinition.getMFGLayer();
            //-----------------------------------------------------
            // get object reference of Start Bank.
            //-----------------------------------------------------
            aStartBank = aMainProcessDefinition.getStartBank();
            //-----------------------------------------------------
            // get object reference of End Bank.
            //-----------------------------------------------------
            aEndBank = aMainProcessDefinition.getEndBank();
        }
        //【step6】create product request
        log.info("【step6】create product request");
        com.fa.cim.newcore.bo.planning.CimProductRequest newProductRequest = null;
        if (!ObjectIdentifier.isEmptyWithValue(releaseLotAttributes.getLotID())){
            newProductRequest = planManager.createProductRequestNamed(releaseLotAttributes.getLotID().getValue(), aProductSpecification);
        } else {
            if (CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_DUMMYLOT)
                    || CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)
                    || CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                    || CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)
                    || CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_VENDORLOT)
                    || CimStringUtils.equals(releaseLotAttributes.getLotType(), BizConstant.SP_LOT_TYPE_RECYCLELOT)){
                int retryCnt = 0;
                com.fa.cim.newcore.bo.planning.CimProductRequest prdReq = null;
                String assignedLotID = null;
                while (true){
                    com.fa.cim.newcore.bo.product.CimLot aLot = null;
                    while (true){
                        assignedLotID = lotMethod.lotTypeLotIDAssign(objCommon, releaseLotAttributes.getLotType(), releaseLotAttributes.getProductID(), releaseLotAttributes.getSubLotType());
                        // Check Existence of lot that was specified systematically
                        aLot = productManager.findLotNamed(assignedLotID);
                        if (aLot == null){
                            log.info("lot_MakeVendorLot:Unassigned LotID");
                            break;
                        } else {
                            log.info("lot_MakeVendorLot:Assigned LotID, Continue");
                        }
                    }
                    // Create ProductRequest for Monitor Lot
                    try {
                        newProductRequest = planManager.createProductRequestNamed(assignedLotID, aProductSpecification);
                        break;
                    } catch (ServiceException e){
                        retryCnt++;
                        log.info("Retry create product request = retry count ....{}", retryCnt);
                    }

                }
            } else {
                throw new ServiceException(retCodeConfig.getLotTypeNotSupported());
            }
        }
        //【step7】set information into created product request
        log.info("【step7】set information into created product request");
        newProductRequest.setLotType(releaseLotAttributes.getLotType());
        newProductRequest.setSubLotType(releaseLotAttributes.getSubLotType());
        newProductRequest.setPlanReleaseDateTime(releaseLotAttributes.getPlannedStartTime());
        newProductRequest.setDeliveryDateTime(releaseLotAttributes.getPlannedFinishTime());
        newProductRequest.setProductQuantity(releaseLotAttributes.getProductQuantity());
        newProductRequest.setLastClaimedPerson(aPerson);
        newProductRequest.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        newProductRequest.makeNotPlanned();
        newProductRequest.makeNotInRelease();
        newProductRequest.setDepartmentID(releaseLotAttributes.getDepartment());
        newProductRequest.setSectionID(releaseLotAttributes.getSection());
        newProductRequest.setReasonCodeID(releaseLotAttributes.getReasonCode());
        if (aLotOwner != null){
            newProductRequest.setLotOwner(aLotOwner);
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getManufacturingOrderNumber())){
            newProductRequest.setOrderNumber(releaseLotAttributes.getManufacturingOrderNumber());
        }
        if (aCustomer != null){
            newProductRequest.setCustomer(aCustomer);
        }
        if (aMainProcessDefinition != null){
            newProductRequest.setMainProcessDefinition(aMainProcessDefinition);
        }
        if (!CimStringUtils.isEmpty(aMfgLayer)){
            newProductRequest.setMFGLayer(aMfgLayer);
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getLotGenerationType())){
            newProductRequest.setLotGenerationType(releaseLotAttributes.getLotGenerationType());
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getSchedulingMode())){
            newProductRequest.setScheduleMode(releaseLotAttributes.getSchedulingMode());
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getLotIDGenerationMode())){
            newProductRequest.setLotIdGenerationMode(releaseLotAttributes.getLotIDGenerationMode());
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getProductDefinitionMode())){
            newProductRequest.setProductMode(releaseLotAttributes.getProductDefinitionMode());
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getPriorityClass())){
            newProductRequest.setPriorityClass(Long.parseLong(releaseLotAttributes.getPriorityClass()));
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getExternalPriority())){
            newProductRequest.setSchedulePriority(Integer.parseInt(releaseLotAttributes.getExternalPriority()));
        }
        if (aStartBank != null){
            newProductRequest.setStartBank(aStartBank);
        }
        if (aEndBank != null){
            newProductRequest.setEndBank(aEndBank);
        }
        if (!CimStringUtils.isEmpty(releaseLotAttributes.getLotComment())){
            newProductRequest.setLotComment(releaseLotAttributes.getLotComment());
        }
        //【step8】prepare PosSourceSequence structure information
        // and set Source lot's information info new product request
        log.info("【Step8】prepare PosSourceSequence structure information and set Source lot's information info new product request");
        List<Infos.DirectedSourceLot> directedSourceLotList = releaseLotAttributes.getDirectedSourceLotList();
        int lenDirectSrcLot = CimArrayUtils.getSize(directedSourceLotList);
        if (lenDirectSrcLot != 0) {
            List<PlanDTO.SourceLotEx> aSourceLotList = new ArrayList<>();
            com.fa.cim.newcore.bo.product.CimLot aSourceLot = null;
            com.fa.cim.newcore.bo.product.CimWafer aWafer = null;
            for (Infos.DirectedSourceLot directedSourceLot : directedSourceLotList){
                aSourceLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, directedSourceLot.getSourceLotID());
                Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), directedSourceLot.getSourceLotID().getValue()));
                PlanDTO.SourceLotEx sourceLot = new PlanDTO.SourceLotEx();
                aSourceLotList.add(sourceLot);
                sourceLot.setLotID(new ObjectIdentifier(aSourceLot.getIdentifier(), aSourceLot.getPrimaryKey()));
                aSourceLot.setNotAllocatedQuantity();
                aSourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                aSourceLot.setLastClaimedPerson(aPerson);
                List<Infos.SourceProduct> strSourceProductList = directedSourceLot.getStrSourceProduct();
                List<ObjectIdentifier> waferIDList = new ArrayList<>();
                sourceLot.setWaferIDList(waferIDList);
                for (Infos.SourceProduct sourceProduct : strSourceProductList){
                    ObjectIdentifier aSourceProduct = new ObjectIdentifier(sourceProduct.getSourceProduct());
                    aWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, aSourceProduct);
                    Validations.check(aWafer == null, retCodeConfig.getNotFoundWafer());
                    ObjectIdentifier waferID = new ObjectIdentifier(aWafer.getIdentifier());
                    waferIDList.add(waferID);
                    boolean bWaferAlreadySTBAllocated = aWafer.isSTBAllocated();
                    Validations.check(bWaferAlreadySTBAllocated, new OmCode(retCodeConfig.getWaferAllocated(), aWafer.getIdentifier()));
                    aWafer.makeSTBAllocated();
                    aWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    aWafer.setLastClaimedPerson(aPerson);
                }
                newProductRequest.setSourceLots(aSourceLotList);
            }
        }
        //【step9】create lot schedule and set it into new product request
        log.info("【step9】create lot schedule and set it into new product request...");
        List<Infos.ReleaseLotSchedule> releaseLotScheduleList = releaseLotAttributes.getReleaseLotScheduleList();
        int lenLotSch = CimArrayUtils.getSize(releaseLotScheduleList);
        if (lenLotSch > 0) {
            com.fa.cim.newcore.bo.planning.CimLotSchedule aLotSchedule = planManager.createLotScheduleNamed(releaseLotAttributes.getLotID().getValue());
            Validations.check(aLotSchedule == null, new OmCode(retCodeConfig.getNotFoundLotSchedule(), ""));
            newProductRequest.setLotSchedule(aLotSchedule);
            //【step9-1】create lot operation schedule and set it into new product request
            log.info("【step9-1】create lot operation schedule and set it into new product request...");
            for (Infos.ReleaseLotSchedule releaseLotSchedule : releaseLotScheduleList){
                String aMadeUpKey = releaseLotAttributes.getRouteID().getValue() + "." + releaseLotSchedule.getOperationNumber();
                com.fa.cim.newcore.bo.planning.CimLotOperationSchedule aLotOperationSchedule = aLotSchedule.createLotOperationScheduleNamed(aMadeUpKey);
                Validations.check(aMainProcessDefinition == null, new OmCode(retCodeConfig.getNotFoundRoute(), releaseLotAttributes.getRouteID().getValue()));
                Validations.check(aLotOperationSchedule == null, new OmCode(retCodeConfig.getNotFoundLotOperationSchedule(),""));
                aLotOperationSchedule.setMainProcessDefinition(aMainProcessDefinition);
                aLotOperationSchedule.setOperationNumber(releaseLotSchedule.getOperationNumber());
                if (!ObjectIdentifier.isEmptyWithValue(releaseLotSchedule.getEquipmentID())){
                    CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, releaseLotSchedule.getEquipmentID());
                    Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), releaseLotSchedule.getEquipmentID().getValue()));
                    aLotOperationSchedule.setPlannedMachine(aMachine);
                } else {
                    aLotOperationSchedule.setPlannedMachine(null);
                }
                if (!CimStringUtils.isEmpty(releaseLotSchedule.getPlannedStartTime())){
                    aLotOperationSchedule.setPlannedStartDateTime(CimDateUtils.convertToOrInitialTime(releaseLotSchedule.getPlannedStartTime()));
                } else {
                    aLotOperationSchedule.setPlannedStartDateTime(null);
                }
                if (!CimStringUtils.isEmpty(releaseLotSchedule.getPlannedFinishTime())){
                    aLotOperationSchedule.setPlannedEndDateTime(CimDateUtils.convertToOrInitialTime(releaseLotSchedule.getPlannedFinishTime()));
                } else {
                    aLotOperationSchedule.setPlannedEndDateTime(null);
                }
            }
        }
        //-------------------------------------
        // set output structure
        //-------------------------------------
        Outputs.ObjProductRequestReleaseOut objProductRequestReleaseOut = new Outputs.ObjProductRequestReleaseOut();
        ObjectIdentifier createProductRequestID = new ObjectIdentifier(newProductRequest.getIdentifier(), newProductRequest.getPrimaryKey());
        objProductRequestReleaseOut.setCreateProductRequest(createProductRequestID);
        return objProductRequestReleaseOut;
    }


    @Override
    public Outputs.ObjProductRequestReleaseBySTBCancelOut productRequestReleaseBySTBCancel(Infos.ObjCommon objCommon, Inputs.ObjProductRequestReleaseBySTBCancelIn in) {
        Outputs.ObjProductRequestReleaseBySTBCancelOut result = new Outputs.ObjProductRequestReleaseBySTBCancelOut();
        ObjectIdentifier productID = in.getProductID();
        ObjectIdentifier bankID = in.getBankID();
        String lotType = in.getLotType();
        String subLotType = in.getSubLotType();
        int productCount = in.getProductCount();
        log.info(String.format("[InParam]productID:%s", productID));
        log.info(String.format("[InParam]bankID:%s", bankID));
        log.info(String.format("[InParam]sourceLotType:%s", lotType));
        log.info(String.format("[InParam]subLotType:%s", subLotType));
        log.info(String.format("[InParam]productCount:%d", productCount));

        // check input parameter
        CimProductSpecification aPosProductSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(aPosProductSpecification == null, new OmCode(retCodeConfig.getInvalidProdId(), productID.getValue()));
        com.fa.cim.newcore.bo.factory.CimBank aPosBank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(aPosBank == null, new OmCode(retCodeConfig.getNotFoundBank(), bankID.getValue()));
        //【Step1】check input subLotType
        log.info("【Step1】check input subLotType");
        log.info("check input subLotType...");
        if (!CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_VENDORLOT)
            && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)
            && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)
            && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
            && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)
            && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_RECYCLELOT)){
            throw new ServiceException(new OmCode(retCodeConfig.getLotTypeNotSupportedForPrepare()));
        }
        List<Infos.LotTypeInfo> lotTypeInfoList = lotMethod.lotTypeSubLotTypeInfoGet(objCommon, lotType);
        List<Infos.SubLotType> lotTypeSubLotTypeList = lotTypeInfoList.get(0).getStrSubLotTypes();
        int nLen = CimArrayUtils.getSize(lotTypeSubLotTypeList);
        int i= 0;
        for (i = 0; i < nLen; i++) {
            String tmp = lotTypeSubLotTypeList.get(i).getSubLotType();
            if (CimStringUtils.equals(subLotType, tmp)) {
                log.info(String.format("input sub lot Type which is'%s' is valid...", tmp));
                break;
            }
        }
        if (i == nLen){
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundSubLotType(), subLotType));
        }

        // check input productCount
        if (productCount > 25 || productCount <= 0) {
            log.info("productCount <= 0 || productCount > 25");
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidProductCount(), String.valueOf(productCount)));
        }

        //【Step2】create product request for lot preparation
        log.info("【Step2】create product request for lot preparation");
        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();
        releaseLotAttributes.setProductID(productID);
        releaseLotAttributes.setLotType(lotType);
        releaseLotAttributes.setSubLotType(subLotType);
        releaseLotAttributes.setProductQuantity(productCount);
        releaseLotAttributes.setPlannedStartTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
        releaseLotAttributes.setPlannedFinishTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
        Outputs.ObjProductRequestReleaseOut objProductRequestReleaseOut = this.productRequestRelease(objCommon, releaseLotAttributes);
        log.info(String.format("created product request which product request id is '%s'", objProductRequestReleaseOut.getCreateProductRequest().getValue()));
        // Update productrequest Info
        log.info("update productRequest info...");
        com.fa.cim.newcore.bo.planning.CimProductRequest aProdReq = baseCoreFactory.getBO(com.fa.cim.newcore.bo.planning.CimProductRequest.class, objProductRequestReleaseOut.getCreateProductRequest());
        Validations.check(null == aProdReq, new OmCode(retCodeConfig.getNotFoundProductRequest(), objProductRequestReleaseOut.getCreateProductRequest().getValue()));
        aProdReq.setAllStatesForVendorLot();;
        aProdReq.setStartBank(aPosBank);
        aProdReq.setEndBank(aPosBank);
        //------------------------------------------
        // Prepare output structure
        //------------------------------------------
        result.setCreatedProductRequestID(new ObjectIdentifier(aProdReq.getIdentifier(), aProdReq.getPrimaryKey()));
        return result;
    }

    @Override
    public void productRequestCheckForRelease(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes) {
        if (!CimStringUtils.equals(objCommon.getTransactionID(), "OPLNW003")) {
            //【step1】Check if the lotID already exists in productrequest table.
            // Checked only when product Request is newly created. (Skipped for updating Tx:OPLNW003)
            log.debug("【step1】check if the lotID already exists in productrequest table.");
            com.fa.cim.newcore.bo.planning.CimProductRequest productRequest = planManager.findProductRequestNamed(ObjectIdentifier.fetchValue(releaseLotAttributes.getLotID()));
            Validations.check(null != productRequest, new OmCode(retCodeConfig.getExistProductRequest(), ObjectIdentifier.fetchValue(releaseLotAttributes.getLotID())));

            //【step2】Check if the lotID already exists in lot table in the case lotGenerationType is not "By Source lot"
            log.debug("【step2】Check if the lotID already exists in lot table in the case lotGenerationType is not \"By Source lot\"");
            if (!CimStringUtils.equals(BizConstant.SP_PPRSP_SOURCELOT, releaseLotAttributes.getLotGenerationType())) {
                log.debug("lotGenerationType != SP_PPrSp_SourceLot");
                com.fa.cim.newcore.bo.product.CimLot lot = productManager.findLotNamed(ObjectIdentifier.fetchValue(releaseLotAttributes.getLotID()));
                Validations.check(null != lot, new OmCode(retCodeConfig.getDuplicateLot(), releaseLotAttributes.getLotID().getValue()));
            } else {
                //in the case of lotGenrationType is "By Source lot"
                log.debug("in the case of lotGenrationType is \"By Source lot\"");

                //【step3】check if all of strDirectedSourceLot[].sourceLotID exist in lot table.
                log.debug("【step3】check if all of strDirectedSourceLot[].sourceLotID exist in lot table.");
                int number = 0;
                boolean sameProductRequestReleasedFlag = false;
                com.fa.cim.newcore.bo.product.CimLot sourceLot = null;
                for (int i = 0; i < CimArrayUtils.getSize(releaseLotAttributes.getDirectedSourceLotList()); i++) {
                    Infos.DirectedSourceLot directedSourceLot = releaseLotAttributes.getDirectedSourceLotList().get(i);
                    sourceLot = productManager.findLotNamed(directedSourceLot.getSourceLotID().getValue());
                    if (null == sourceLot) {
                        log.error("not found source lot");
                        Validations.check(true,retCodeConfig.getNotFoundSourceLot());
                    }

                    if (ObjectIdentifier.equalsWithValue(releaseLotAttributes.getLotID(), directedSourceLot.getSourceLotID())) {
                        sameProductRequestReleasedFlag = true;
                        number = i;
                    }
                }

                if (sameProductRequestReleasedFlag) {
                    //In the case productRequestID is equal to one of sourceLots.
                    log.debug("In the case productRequestID is equal to one of sourceLots.");
                    //【step4】Check if productSpecID of the sourceLotID is equal to one of the lotID.
                    log.debug("【step4】check if productSpecID of the sourceLotID is equal to one of the lotID.");

                    CimProductSpecification productSpecification = null;
                    if (null != sourceLot) {
                        productSpecification = sourceLot.getProductSpecification();
                    }
                    if (null == productSpecification) {
                        log.error("not found product request spec");
                        Validations.check(true,retCodeConfig.getNotFoundProductSpec());
                    }
                    String productID = productSpecification.getIdentifier();
                    if (ObjectIdentifier.equalsWithValue(releaseLotAttributes.getProductID(), productID)) {
                        log.error("duplicate lot");
                        Validations.check(true, new OmCode(retCodeConfig.getDuplicateLot(), releaseLotAttributes.getLotID().getValue()));
                    }

                    //【step5】check if all of waferID of the sourceLotID are equal to ones of the lotID.
                    log.debug("【step5】check if all of waferID of the sourceLotID are equal to ones of the lotID.");
                    int sourceProductLen = CimArrayUtils.getSize(releaseLotAttributes.getDirectedSourceLotList().get(number).getStrSourceProduct());

                    List<Material> waferList = sourceLot.allMaterial();
                    int materialLen = CimArrayUtils.getSize(waferList);
                    if (materialLen < sourceProductLen) {
                        log.error("not match source product");
                        Validations.check(true, new OmCode(retCodeConfig.getNotMatchSourceProduct(), releaseLotAttributes.getLotID().getValue()));
                    }

                    CimWafer wafer = null;
                    String waferID = null;
                    boolean foundFlag = false;
                    for (int i = 0; i < materialLen; i++) {
                        wafer = (CimWafer)waferList.get(i);
                        Validations.check(null == wafer, retCodeConfig.getNotFoundWafer());

                        waferID = wafer.getIdentifier();
                        foundFlag = false;
                        for (int j = 0; j < sourceProductLen; j++) {
                            Infos.SourceProduct sourceProduct = releaseLotAttributes.getDirectedSourceLotList().get(number).getStrSourceProduct().get(j);
                            if (CimStringUtils.equals(waferID, sourceProduct.getSourceProduct())) {
                                log.debug("foundFlag = true");
                                foundFlag = true;
                                break;
                            }
                        }

                        if (!foundFlag) {
                            log.debug("found Flag = false");
                            Validations.check(CimBooleanUtils.isFalse(wafer.isControlWafer()), new OmCode(retCodeConfig.getNotMatchSourceProduct(), releaseLotAttributes.getLotID().getValue()));
                        }

                    }
                } else {
                    com.fa.cim.newcore.bo.product.CimLot lot = productManager.findLotNamed(releaseLotAttributes.getLotID().getValue());
                    Validations.check(null != lot, new OmCode(retCodeConfig.getDuplicateLot(), ObjectIdentifier.fetchValue(releaseLotAttributes.getLotID())));
                }
            }

            //【step6】check if the state of product specification is not "Obsoleted", or "Draft.
            log.debug("【step6】check if the state of product specification is not \"Obsoleted\", or \"Draft.");
            CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, releaseLotAttributes.getProductID());
            if (null == productSpecification) {
                log.error("not found product spec");
                Validations.check(true,retCodeConfig.getNotFoundProductSpec());
            }

            String productState = productSpecification.getState();
            if (CimStringUtils.equals(productState, BizConstant.SP_PRODUCTSPECIFICATION_STATE_OBSOLETE)
             || CimStringUtils.equals(productState, BizConstant.SP_PRODUCTSPECIFICATION_STATE_DRAFT)) {
                log.error("invalid product state");
                Validations.check(true, new OmCode(retCodeConfig.getInvalidProductStat(), ObjectIdentifier.fetchValue(releaseLotAttributes.getProductID())));
            }

            //【step7】check subLot Type
            log.debug("【step7】check subLot Type");
            List<Infos.LotTypeInfo> outRetCode = lotMethod.lotTypeSubLotTypeInfoGet(objCommon, releaseLotAttributes.getLotType());


            boolean foundFlag = false;
            int subLotTypeSize = CimArrayUtils.getSize(outRetCode.get(0).getStrSubLotTypes());
            for (int i = 0; i < subLotTypeSize; i++) {
                Infos.SubLotType subLotType = outRetCode.get(0).getStrSubLotTypes().get(i);
                if (CimStringUtils.equals(releaseLotAttributes.getSubLotType(), subLotType.getSubLotType())) {
                    log.debug("input subLotType is valid");
                    foundFlag = true;
                    break;
                }
            }

            if (!foundFlag) {
                log.error("not found subLotType");
                Validations.check(true, new OmCode(retCodeConfig.getNotFoundSubLotType(), releaseLotAttributes.getSubLotType()));
            }
        }
    }

    @Override
    public ObjectIdentifier productRequestUpdate(Infos.ObjCommon objCommon, Infos.UpdateLotAttributes updateLotAttributes) {
        RetCode<ObjectIdentifier> result = new RetCode<>();

        //【step1】get object reference of product request
        log.debug("【step1】get object reference of product request");
        CimProductRequest productRequest = baseCoreFactory.getBO(CimProductRequest.class, updateLotAttributes.getLotID());
        Validations.check(null == productRequest,retCodeConfig.getNotFoundProductRequest());

        //【step2】get object reference of request user.
        log.debug("【step2】get object reference of request user.");
        CimPerson person = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == person,new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        //【step3】get source lots of product request
        log.debug("【step3】get source lots of product request");
        List<PlanDTO.SourceLotEx> sourceLotExList = productRequest.allSourceLots();
        int size = CimArrayUtils.getSize(sourceLotExList);
        for (int i = 0; i < size; i++) {
            PlanDTO.SourceLotEx sourceLotEx = sourceLotExList.get(i);
            CimLot lot = baseCoreFactory.getBO(CimLot.class, sourceLotEx.getLotID());
            Validations.check(null == lot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(sourceLotEx.getLotID())));

            List<ObjectIdentifier> waferIDList = sourceLotEx.getWaferIDList();
            if (!CimArrayUtils.isEmpty(waferIDList)) {
                Consumer<ObjectIdentifier> updateSourceWafer = waferID -> {
                    CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, waferID);
                    Validations.check(null == wafer, new OmCode(retCodeConfig.getNotFoundWafer(), waferID.getValue()));
                    wafer.makeNotSTBAllocated();
                    wafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    wafer.setLastClaimedPerson(person);
                };
            }
            lot.setNotAllocatedQuantity();
            lot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            lot.setLastClaimedPerson(person);
        }

        //【step4】get object reference of product spec. this id must be given from input parameter.
        log.debug("【step4】get object reference of product spec. this id must be given from input parameter.");
        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, updateLotAttributes.getProductID());
        Validations.check(null == productSpecification, new OmCode(retCodeConfig.getNotFoundProductSpec(), ObjectIdentifier.fetchValue(updateLotAttributes.getProductID())));

        //【step5】get object reference of customer.
        log.debug("【step5】get object reference of customer.");
        com.fa.cim.newcore.bo.prodspec.CimCustomer customer = null;
        if (!CimStringUtils.isEmpty(updateLotAttributes.getCustomerCode())) {
            customer = productSpecificationManager.findCustomerNamed(updateLotAttributes.getCustomerCode());
            Validations.check(null == customer, new OmCode(retCodeConfigEx.getNotFoundCustomer(), updateLotAttributes.getCustomerCode()));
        }

        //【step6】get object reference of lot owner.
        log.debug("【step6】get object reference of lot owner.");
        com.fa.cim.newcore.bo.person.CimPerson lotOwner = null;
        if (!CimStringUtils.isEmpty(updateLotAttributes.getLotOwner())) {
            log.debug("the logOwner is not blank.");
            lotOwner = personManager.findPersonNamed(updateLotAttributes.getLotOwner());
            Validations.check(null == lotOwner,new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        }

        //【step7】get object reference of main process definition.
        log.debug("【step7】get object reference of main process definition.");
        boolean roudFoudFlag = false;
        CimProcessDefinition mainProcessDefinition =  baseCoreFactory.getBO(CimProcessDefinition.class, updateLotAttributes.getRouteID());

        String mfgLayer = null;
        com.fa.cim.newcore.bo.factory.CimBank startBank = null;
        com.fa.cim.newcore.bo.factory.CimBank endBank = null;
        if (null != mainProcessDefinition) {
            roudFoudFlag = true;
            log.debug("the main process definition is not null.");

            //【step8】get object reference of mfg layer.
            log.debug("【step8】get object reference of mfg layer.");
            mfgLayer = mainProcessDefinition.getMFGLayer();

            //【step9】get object reference of start bank.
            log.debug("【step9】get object reference of start bank.");
            startBank = mainProcessDefinition.getStartBank();

            //【step10】get object reference key of end bank.
            endBank = mainProcessDefinition.getEndBank();
        }

        //【step11】set information into created product request.
        log.debug("【step11】set information into created product request.");
        productRequest.setProductSpecification(productSpecification);

        //【step12】check sub lot type existence
        log.debug("【step12】check sub lot type existence");
        Outputs.LotSubLotTypeGetDetailInfoDR detailInfoDRRetCode = lotMethod.lotSubLotTypeGetDetailInfoDR(objCommon, updateLotAttributes.getSubLotType());


        //get lottype
        String tmpLotType = productRequest.getLotType();
        log.debug("the specified lot's lottype is %s", tmpLotType);
        log.debug("lottype delonging to the specified subLotType is %s", detailInfoDRRetCode.getLotType());
        Validations.check(!CimStringUtils.equals(tmpLotType, detailInfoDRRetCode.getLotType()),new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        productRequest.setSubLotType(updateLotAttributes.getSubLotType());
        productRequest.setPlanReleaseDateTime(updateLotAttributes.getPlannedStartTime());
        productRequest.setDeliveryDateTime(updateLotAttributes.getPlannedFinishTime());
        productRequest.setProductQuantity(updateLotAttributes.getProductQuantity());
        productRequest.setLastClaimedPerson(person);
        productRequest.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        productRequest.setLotOwner(lotOwner);
        productRequest.setOrderNumber(updateLotAttributes.getManufacturingOrderNumber());
        productRequest.setCustomer(customer);
        productRequest.setMainProcessDefinition(mainProcessDefinition);
        productRequest.setMFGLayer(mfgLayer);
        productRequest.setScheduleMode(updateLotAttributes.getSchedulingMode());
        productRequest.setProductMode(updateLotAttributes.getProductDefinitionMode());
        Integer priorityClass = CimStringUtils.isEmpty(updateLotAttributes.getPriorityClass()) ? 0: Integer.parseInt(updateLotAttributes.getPriorityClass());
        productRequest.setPriorityClass(CimNumberUtils.longValue(priorityClass));
        Integer extPriorirt = CimStringUtils.isEmpty(updateLotAttributes.getExternalPriority()) ? 0 : Integer.parseInt(updateLotAttributes.getExternalPriority());
        productRequest.setSchedulePriority(extPriorirt);
        productRequest.setStartBank(startBank);
        productRequest.setEndBank(endBank);
        productRequest.setLotComment(updateLotAttributes.getLotComment());

        //【step13】prepare PosSourceLotSequence structure information
        log.debug("【step13】prepare PosSourceLotSequence structure information");

        int directSourceLotSize = CimArrayUtils.getSize(updateLotAttributes.getDirectedSourceLotList());
        List<PlanDTO.SourceLotEx> sourceLotList = new ArrayList<>();
        for (int i = 0; i < directSourceLotSize; i++) {
            Infos.DirectedSourceLot directedSourceLot = updateLotAttributes.getDirectedSourceLotList().get(i);
            CimLot sourceLot = baseCoreFactory.getBO(CimLot.class, directedSourceLot.getSourceLotID());
            Validations.check(null == sourceLot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(directedSourceLot.getSourceLotID())));
            PlanDTO.SourceLotEx targetSourceLot = new PlanDTO.SourceLotEx();
            targetSourceLot.setLotID(sourceLot.getLotID());
            List<ObjectIdentifier> waferIDList = new ArrayList<>();

            int sourceProductSize = CimArrayUtils.getSize(directedSourceLot.getStrSourceProduct());
            for (int j = 0; j < sourceProductSize; j++) {
                Infos.SourceProduct sourceProduct = directedSourceLot.getStrSourceProduct().get(j);  // the Source Product = wafer

                CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, new ObjectIdentifier(sourceProduct.getSourceProduct()));
                Validations.check(null == wafer, new OmCode(retCodeConfig.getNotFoundWafer(), sourceProduct.getSourceProduct()));
                //【bear】just set the waferID, no wafer.id, the source product = wafer
                waferIDList.add(new ObjectIdentifier(wafer.getIdentifier()));
                Boolean waferAlreadySTBAllocated = wafer.isSTBAllocated();
                Validations.check(CimBooleanUtils.isTrue(waferAlreadySTBAllocated),retCodeConfig.getWaferAllocated());

                wafer.makeSTBAllocated();
                wafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                wafer.setLastClaimedPerson(person);
            }
            targetSourceLot.setWaferIDList(waferIDList);
            sourceLot.setNotAllocatedQuantity();
            sourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            sourceLot.setLastClaimedPerson(person);
            sourceLotList.add(targetSourceLot);
        }
        productRequest.setSourceLots(sourceLotList);

        //【step14】create lot Schedule and set it into updated Product Request
        log.debug("【step14】create lot Schedule and set it into updated Product Request");
        int lotSchduleSize = CimArrayUtils.getSize(updateLotAttributes.getUpdateLotScheduleList());
        if (lotSchduleSize > 0) {
            com.fa.cim.newcore.bo.planning.CimLotSchedule lotSchedule = productRequest.getLotSchedule();
            if (null == lotSchedule) {
                lotSchedule = planManager.createLotScheduleNamed(updateLotAttributes.getLotID().getValue());
            }
            Validations.check(null == lotSchedule, new OmCode(retCodeConfig.getNotFoundLotSchedule(), ""));
            productRequest.setLotSchedule(lotSchedule);

            List<com.fa.cim.newcore.bo.planning.CimLotOperationSchedule> lotOperationScheduleList = lotSchedule.allLotOperationSchedules();
            List<String> keyList = new ArrayList<>();
            for (int i = 0; i < lotSchduleSize; i++) {
                Infos.ReleaseLotSchedule releaseLotSchedule = updateLotAttributes.getUpdateLotScheduleList().get(i);
                String key = String.format("%s.%s", updateLotAttributes.getRouteID().getValue(), releaseLotSchedule.getOperationNumber());
                keyList.add(key);
            }

            //【step15】delete lot operation schedule for not existing operation
            log.debug("【step15】delete lot operation schedule for not existing operation");

            String lotOperationScheduleID = null;
            Boolean operationScheduleFoundFlag = false;
            int operationScheduleSize = CimArrayUtils.getSize(lotOperationScheduleList);
            for (int i = 0; i < operationScheduleSize; i++) {
                operationScheduleFoundFlag = false;
                lotOperationScheduleID = lotOperationScheduleList.get(i).getIdentifier();
                for (String key : keyList) {
                    if (CimStringUtils.equals(key, lotOperationScheduleID)) {
                        operationScheduleFoundFlag = true;
                        break;
                    }
                }

                if (false == operationScheduleFoundFlag) {
                    log.debug("false = operationScheduleFoundFlag, remove the operationSchedule: %s", lotOperationScheduleID);
                    lotSchedule.removeLotOperationSchedule(lotOperationScheduleList.get(i));
                }
            }

            //【step16】create lot operation schedule if doesn't exist
            log.debug("【step16】create lot operation schedule if doesn't exist");
            for (int i = 0; i < lotSchduleSize; i++) {
                Infos.ReleaseLotSchedule releaseLotSchedule = updateLotAttributes.getUpdateLotScheduleList().get(i);
                String makeUpKey = String.format("%s.%s", updateLotAttributes.getRouteID().getValue(), releaseLotSchedule.getOperationNumber());
                com.fa.cim.newcore.bo.planning.CimLotOperationSchedule lotOperationSchedule = lotSchedule.findLotOperationScheduleNamed(makeUpKey);
                if (null == lotOperationSchedule) {
                    lotOperationSchedule = lotSchedule.createLotOperationScheduleNamed(makeUpKey);
                }

                Validations.check(null == lotOperationSchedule,retCodeConfig.getNotFoundLotOperationSchedule());

                Validations.check(null == mainProcessDefinition,retCodeConfig.getNotFoundRoute());

                lotOperationSchedule.setMainProcessDefinition(mainProcessDefinition);
                lotOperationSchedule.setOperationNumber(releaseLotSchedule.getOperationNumber());
                if (!ObjectIdentifier.isEmpty(releaseLotSchedule.getEquipmentID())) {
                    CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, releaseLotSchedule.getEquipmentID());
                    Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(releaseLotSchedule.getEquipmentID())));

                    lotOperationSchedule.setPlannedMachine(equipment);
                }
                lotOperationSchedule.setPlannedStartDateTime(CimDateUtils.convertTo(releaseLotSchedule.getPlannedStartTime()));
                lotOperationSchedule.setPlannedEndDateTime(CimDateUtils.convertTo(releaseLotSchedule.getPlannedFinishTime()));
            }
        } else {
            log.debug("the lotScheduleSize = 0");
            com.fa.cim.newcore.bo.planning.CimLotSchedule lotSchedule = productRequest.getLotSchedule();
            if (null != lotSchedule) {
                //【bear】remove the lotSchedule
                lotSchedule.setObjectManager(null);
            }
            productRequest.setLotSchedule(null);
        }

        return updateLotAttributes.getLotID();
    }

    @Override
    public String productRequestProductionStateGet(Infos.ObjCommon objCommon,ObjectIdentifier lotID) {
        CimProductRequest aProductRequest = planManager.findProductRequestNamed(lotID.getValue());
        Validations.check(aProductRequest == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), lotID.getValue()));
        return aProductRequest.getProductionState();
    }

    @Override
    public void productRequestReleaseCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        log.info("【Method Entry】productRequestReleaseCancel");
        CimProductRequest aProductRequest = planManager.findProductRequestNamed(lotID.getValue());
        Validations.check(aProductRequest == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), lotID.getValue()));
        boolean isInReleaseFlag = aProductRequest.isInRelease();
        Validations.check(isInReleaseFlag, new OmCode(retCodeConfigEx.getLotAlreadySTB(), lotID.getValue()));
        List<PlanDTO.SourceLotEx> aSourceLotSequence = aProductRequest.allSourceLots();
        int nLen = CimArrayUtils.getSize(aSourceLotSequence);
        for (int i = 0; i < nLen; i++){
            PlanDTO.SourceLotEx sourceLotEx = aSourceLotSequence.get(i);
            CimLot aSourceLot = baseCoreFactory.getBO(CimLot.class, sourceLotEx.getLotID());
            List<ObjectIdentifier> waferIDList = aSourceLotSequence.get(i).getWaferIDList();
            int aProdLen = CimArrayUtils.getSize(waferIDList);
            CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
            Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
            for (int j = 0; j < aProdLen; j++){
                ObjectIdentifier waferID = waferIDList.get(j);
                CimWafer aSourceWafer = baseCoreFactory.getBO(CimWafer.class, waferID);
                aSourceWafer.makeNotSTBAllocated();
                aSourceWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                aSourceWafer.setLastClaimedPerson(aPerson);

            }
            aSourceLot.setNotAllocatedQuantity();
            aSourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aSourceLot.setLastClaimedPerson(aPerson);
        }
        // Remove Lot Schedule
        // Lot operation schedule's are removed automaticaly when lot schedule is removed in F/W.
        CimLotSchedule aLotSchedule = aProductRequest.getLotSchedule();
        if (aLotSchedule != null){
            aLotSchedule.setObjectManager(null);
        }
        planManager.removeProductRequest(aProductRequest);
        log.info("【Method Exit】productRequestReleaseCancel");
    }

    @Override
    public List<Infos.ProductIDListAttributes> productSpecificationFillInTxPCQ015DR180(Infos.ObjCommon objCommon, Params.ProductIdListInqInParams params) {

        String productCategory = params.getProductCategory();

        List<CimLotTypeSubLotTypeDO> lotTypeSublotTypeInfos= cimJpaRepository.query("SELECT  OMLOTTYPE_SUBTYPE.sub_lot_type\n" +
                "                 FROM    OMLOTTYPE,\n" +
                "                         OMLOTTYPE_SUBTYPE\n" +
                "                 WHERE   OMLOTTYPE.LOTTYPE_ID = ?\n" +
                "                 AND    OMLOTTYPE_SUBTYPE.REFKEY = OMLOTTYPE.ID", CimLotTypeSubLotTypeDO.class,  productCategory);

        List<String> candidateSubLotTypes = new ArrayList<>();
        lotTypeSublotTypeInfos.forEach(lotTypeSublotTypeInfo -> candidateSubLotTypes.add(lotTypeSublotTypeInfo.getSubLotType()));

        String userID = params.getUserID();
        String productGroupID = params.getProductGroupID();
        String productID = params.getProductID();
        String routeID = params.getRouteID();
        StringBuilder sb = new StringBuilder("SELECT OMPRODINFO.PROD_ID,OMPRODINFO.ID,OMPRODINFO.MAIN_PROCESS_ID,OMPRODINFO.MAIN_PROCESS_RKEY,OMPRODINFO.DESCRIPTION,OMPRODINFO.PRODFMLY_ID," +
                "OMPRODINFO.PRODFMLY_RKEY,OMPRODINFO.PROD_TYPE,OMPRODINFO.OWNER_ID,OMPRODINFO.OWNER_RKEY,OMPRODINFO.MFG_LAYER,OMPRODINFO.STATE, OMPRODINFO.LOT_GEN_TYPE, OMPRODINFO.RELEASE_LOT_SIZE," +
                " OMPRODINFO.YIELD_PLAN, OMPRODINFO.USAGE_LIMIT, OMPRODINFO.RECYCLE_LIMIT ");
        if (checkParam(userID)) {
            sb.append("FROM OMPRODINFO, OMUSER, OMUSER_USERGRP, OMUSERGRP WHERE ");
            sb.append(String.format("USER.USER_ID = '%s'", userID));
            sb.append("AND OMPRODINFO.SPS_OWNER_GRP = OMUSERGRP.USERGRP_ID AND USERGRP.USER_GRP_TYPE = 'Scheduler' AND ");
            sb.append("(OMUSER_USERGRP.REFKEY = OMUSER.ID AND USERGRP.USER_GRP_ID = OMUSER_USERGRP.USER_GRP_ID) ");
        } else {
            sb.append("FROM OMPRODINFO WHERE ");
        }

        if (CimStringUtils.equals(BizConstant.SP_PRODUCTCATEGORY_ALL, productCategory)) {
            if (checkParam(userID)) {
                log.info("User id is not null: {}", userID);
            } else {
                sb.append(" 1=1 ");
            }
        } else {
            if (checkParam(userID)) {
                sb.append(String.format(" AND OMPRODINFO.PROD_CAT_ID = '%s' ", productCategory));
            } else {
                sb.append(String.format(" OMPRODINFO.PROD_CAT_ID = '%s' ", productCategory));
            }
        }
        if (checkParam(productGroupID)) {
            sb.append(String.format(" AND OMPRODINFO.PRODFMLY_ID LIKE '%s'", "%"+productGroupID+"%"));
        }
        if (checkParam(productID)) {
            sb.append(String.format(" AND OMPRODINFO.PROD_ID LIKE '%s'", "%"+productID+"%"));
        }
        if (checkParam(routeID)) {
            sb.append(String.format(" AND OMPRODINFO.MAIN_PROCESS_ID LIKE '%s'", "%"+routeID+"%"));
        }
        sb.append(" ORDER BY OMPRODINFO.PROD_CAT_ID");

        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        List<Infos.ProductIDListAttributes> productIDListAttributesList = new ArrayList<>();

        // this is all in CimProductSpecificationDO entity.
        List<Object[]> list = cimJpaRepository.query(sb.toString());

        for (Object[] objects : list) {
            String specID = (String) objects[0];
            String specObj = (String) objects[1];
            String specMainPDID = (String) objects[2];
            String specMainPDObj = (String) objects[3];
            String specDescription = (String) objects[4];
            String specProductGroupID = (String) objects[5];
            String specProductGroupObj = (String) objects[6];
            String specProductType = (String) objects[7];
            String specOwnerID = (String) objects[8];
            String specOwnerObj = (String) objects[9];
            String specManufacturingLayerID = (String) objects[10];
            String specState = (String) objects[11];
            String specLotGenType = (String) objects[12];

            Infos.ProductIDListAttributes productIDListAttributes = new Infos.ProductIDListAttributes();
            productIDListAttributes.setProductID(new ObjectIdentifier(specID, specObj));
            productIDListAttributes.setProductIDDescription(specDescription);
            productIDListAttributes.setProductType(specProductType);
            productIDListAttributes.setProductGroupID(new ObjectIdentifier(specProductGroupID, specProductGroupObj));
            productIDListAttributes.setProductOwnerID(new ObjectIdentifier(specOwnerID, specOwnerObj));
            productIDListAttributes.setManufacturingLayerID(new ObjectIdentifier(specManufacturingLayerID));
            productIDListAttributes.setState(specState);
            productIDListAttributes.setLotGenType(specLotGenType);
            productIDListAttributes.setReleaseSize(CimNumberUtils.intValue((Number) objects[13]));
            productIDListAttributes.setPlanYeild(CimNumberUtils.intValue((Number) objects[14]));
            productIDListAttributes.setUsageLimit(CimNumberUtils.intValue((Number) objects[15]));
            productIDListAttributes.setRecycleLimit(CimNumberUtils.intValue((Number) objects[16]));

            productCategory = BizConstant.SP_CATEGORY_MFGLAYER;
            CimCodeDO code =cimJpaRepository.queryOne("SELECT DESCRIPTION \n" +
                    "                     FROM   OMCODE\n" +
                    "                     WHERE  CODE_ID     = ?1 AND\n" +
                    "                            CODETYPE_ID = ?2 ", CimCodeDO.class, specManufacturingLayerID, productCategory);
            if (null == code) {
                productIDListAttributes.setManufacturingLayerIDDescription("*");
            } else {
                productIDListAttributes.setManufacturingLayerIDDescription(code.getDescription());
            }

            CimProductGroupDO productGroup = cimJpaRepository.queryOne("SELECT DESCRIPTION, TECH_ID,TECH_RKEY \n" +
                    "                     FROM    OMPRODFMLY\n" +
                    "                     WHERE   PRODFMLY_ID = ? ", CimProductGroupDO.class, specProductGroupID);

            if (null == productGroup) {
                productIDListAttributes.setProductGroupIDDescription("*");
                productIDListAttributes.setTechnologyID(new ObjectIdentifier("*", "*"));
                productIDListAttributes.setTechnologyIDDescription("*");
            } else {
                String technologyID = productGroup.getTechnologyID();
                productIDListAttributes.setProductGroupIDDescription(productGroup.getDescription());
                productIDListAttributes.setTechnologyID(new ObjectIdentifier(technologyID, productGroup.getTechnologyObj()));

                CimTechnologyDO technology = cimJpaRepository.queryOne("SELECT DESCRIPTION\n" +
                        "                                             FROM   OMTECH\n" +
                        "                         WHERE  TECH_ID = ?", CimTechnologyDO.class, technologyID);
                if (null == technology) {
                    productIDListAttributes.setTechnologyIDDescription("*");
                } else {
                    productIDListAttributes.setTechnologyIDDescription(technology.getDescription());
                }
            }

            String processDefinitionLevel = BizConstant.SP_PD_FLOWLEVEL_MAIN;
            CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(" SELECT DESCRIPTION ,START_BANK_ID ,START_BANK_RKEY, VERSION_ID, ACTIVE_VER_ID, ACTIVE_VER_RKEY\n" +
                    "                     FROM     OMPRP\n" +
                    "                     WHERE    PRP_ID = ?1 \n" +
                    "                     AND      PRP_LEVEL = ?2 ", CimProcessDefinitionDO.class,
                    specMainPDID, processDefinitionLevel);
            if (null == processDefinition) {
                productIDListAttributes.setRouteID(new ObjectIdentifier(specMainPDID, specMainPDObj));
                productIDListAttributes.setRouteIDDescription("*");
                productIDListAttributes.setStartBankID(new ObjectIdentifier("*", "*"));
            } else {
                if (!CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, processDefinition.getVersionID())) {
                    productIDListAttributes.setRouteID(new ObjectIdentifier(specMainPDID, specMainPDObj));
                } else {
                    productIDListAttributes.setRouteID(new ObjectIdentifier(processDefinition.getActiveID(), processDefinition.getActiveObj()));
                }
                productIDListAttributes.setRouteIDDescription(processDefinition.getDescription());
                productIDListAttributes.setStartBankID(new ObjectIdentifier(processDefinition.getStartBankID(), processDefinition.getStartBankObj()));
            }

            CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT ID\n" +
                    "                             FROM           OMPRODINFO\n" +
                    "                             WHERE PROD_ID = ? ", CimProductSpecificationDO.class, specID);

            if (null == productSpecification) continue;
            List<CimProductSpecificationSourceDO> productSpecificationSources = cimJpaRepository.query(" SELECT     SRC_PROD_ID,SRC_PROD_RKEY\n" +
                    "                     FROM       OMPRODINFO_SRC\n" +
                    "                     WHERE REFKEY = ?", CimProductSpecificationSourceDO.class, specObj);
            if (CimObjectUtils.isEmpty(productSpecificationSources)) continue;
            List<ObjectIdentifier> sourceProductID = new ArrayList<>();
            for (CimProductSpecificationSourceDO productSpecificationSource : productSpecificationSources) {
                sourceProductID.add(new ObjectIdentifier(productSpecificationSource.getSourceProductSpecificationID(), productSpecificationSource.getSourceProductSpecificationObj()));
            }
            productIDListAttributes.setSourceProductID(sourceProductID);
            productIDListAttributes.setCandidateSubLotTypes(candidateSubLotTypes);
            productIDListAttributesList.add(productIDListAttributes);
        }
        productIDListAttributesList.sort((x,y) -> x.getProductID().getValue().compareTo(y.getProductID().getValue()));
        return productIDListAttributesList;
    }

    @Override
    public ObjectIdentifier productSpecificationStartBankGet(Infos.ObjCommon objCommon, ObjectIdentifier productID) {
        //------------------------------------------------
        // Get object reference of PosProductSpecification
        //------------------------------------------------
        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        //CimProductSpecificationDO productSpecification = productRequestSpecCore.findProductRequestSpecByProductSpecID(productID);
        Validations.check(null == productSpecification,retCodeConfig.getNotFoundProductSpec());
        //------------------------------------------------
        // Get object reference of Main PD
        //------------------------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aPD = (CimProcessDefinition) productSpecification.getProcessDefinition();
        //CimProcessDefinitionDO aPD = processDefinitionCore.findProcessDefinitionByProcessDefinitionID(productSpecification.getMainProcessDefinitionID());
        Validations.check(null == aPD,retCodeConfig.getNotFoundProcessDefinition());
        //------------------------------------------------
        // Get object reference of Start bank of Main PD
        //------------------------------------------------
        com.fa.cim.newcore.bo.factory.CimBank aPosBank = aPD.getStartBank();
        //CimBankDO aPosBank = bankCore.findByBankID(aPD.getStartBankID());
        Validations.check(null == aPosBank,retCodeConfig.getNotFoundBank());

        return ObjectIdentifier.build(aPosBank.getIdentifier(), aPosBank.getPrimaryKey());
    }

    @Override
    public ObjectIdentifier productRequestForControlLotRelease(Infos.ObjCommon objCommon, ObjectIdentifier productID, Integer waferCount, String lotType, String subLotType) {
        //-----------------------------------------------------
        // Prepare structure for productRequest_Release()
        //-----------------------------------------------------
        Infos.ReleaseLotAttributes strReleaseLotAttributes = new Infos.ReleaseLotAttributes();
        //------------------------------------------------
        // Copy input product ID to structure strReleaseLotAttributes
        //------------------------------------------------
        strReleaseLotAttributes.setProductID(productID);
        //------------------------------------------------
        // Retrieve object reference of Product specification
        //------------------------------------------------
        CimProductSpecification aProdSpec = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(null == aProdSpec,retCodeConfig.getNotFoundProductSpec());
        //------------------------------------------------
        // retrieve object reference of Process Flow
        //------------------------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessDefinition aMainPD = (CimProcessDefinition) aProdSpec.getProcessDefinition();
        Validations.check(null == aMainPD,retCodeConfig.getNotFoundRoute());
        Validations.check(!lotType.equals(aMainPD.getProcessDefinitionType()),retCodeConfig.getInvalidInputParam());
        //-------------------------------
        // calculate plan finish time
        //-------------------------------
        com.fa.cim.newcore.bo.pd.CimProcessFlow aMainPF = aMainPD.getActiveMainProcessFlow();
        Validations.check(null == aMainPF,retCodeConfig.getNotFoundProcessFlow());
        //【TODO】【TODO - NOTIMPL】- isNewlyCreated
        double remainingCycleTime = aMainPF.findRemainingCycleTimeForMain(null, null);
        //------------------------------------------------
        // retrieve object reference of Product's owner
        //------------------------------------------------
        com.fa.cim.newcore.bo.person.CimPerson person = aProdSpec.getOwnerUser();
        //-----------------------------------------------------
        // set structure
        //-----------------------------------------------------
        strReleaseLotAttributes.setProductQuantity(waferCount);
        strReleaseLotAttributes.setPlannedStartTime(objCommon.getTimeStamp().getReportTimeStamp().toString());

        strReleaseLotAttributes.setPlannedFinishTime(objCommon.getTimeStamp().getReportTimeStamp().toString());

        String prtyClass = StandardProperties.OM_CTRL_LOT_PRIORITY.getValue();
        Integer iPrtyClass = CimStringUtils.isEmpty(prtyClass) ? 0: Integer.valueOf(prtyClass);
        String wk = null;
        if(CimStringUtils.isEmpty(prtyClass) || iPrtyClass > 5 || iPrtyClass <= 0 ) {
            wk = String.format("%d", BizConstant.SP_PRIORITYCLASS_NORMAL);
        } else {
            wk = String.format("%d", iPrtyClass);
        }
        strReleaseLotAttributes.setPriorityClass(wk);
        if (null != person) {
            strReleaseLotAttributes.setLotOwner(person.getIdentifier());
        }
        ObjectIdentifier routeID = ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
        strReleaseLotAttributes.setLotType(lotType);
        strReleaseLotAttributes.setSubLotType(subLotType);
        strReleaseLotAttributes.setRouteID(routeID);
        Outputs.ObjProductRequestReleaseOut objProductRequestReleaseOut = this.productRequestRelease(objCommon, strReleaseLotAttributes);
        return objProductRequestReleaseOut.getCreateProductRequest();
    }

    @Override
    public void productExistenceCheck(Infos.ObjCommon objCommon, ObjectIdentifier productID) {
        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(null == productSpecification, new OmCode(retCodeConfig.getNotFoundProductSpec(), ObjectIdentifier.fetchValue(productID)));

        String productState = productSpecification.getState();
        if (CimStringUtils.equals(productState, BizConstant.SP_PRODUCTSPECIFICATION_STATE_OBSOLETE)
         || CimStringUtils.equals(productState, BizConstant.SP_PRODUCTSPECIFICATION_STATE_DRAFT)) {
            Validations.check(true, new OmCode(retCodeConfig.getInvalidProductStat(), productState));
        }
    }

    private boolean checkParam(String str) {
        return !CimStringUtils.isEmpty(str) && !str.equals("%");
    }

    @Override
    public ObjectIdentifier productRouteInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier productID) {
        log.info("In params ProductID: " + ObjectIdentifier.fetchValue(productID));
        CimProductSpecification productSpecification = baseCoreFactory.getBO(CimProductSpecification.class, productID);
        Validations.check(null == productSpecification, retCodeConfig.getInvalidProdId());

        ProcessDefinition processDefinition = productSpecification.getProcessDefinition();
        Validations.check(null == processDefinition, retCodeConfig.getNotFoundRoute());
        CimProcessDefinition aMainPD = (CimProcessDefinition) processDefinition;
        return ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
    }
}
