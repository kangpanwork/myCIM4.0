package com.fa.cim.service.bank.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.lottype.CimLotTypeDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 14:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class BankServiceImpl implements IBankService {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private ILotMethod lotMethod;
    @Autowired
    private IEventMethod eventMethod;
    @Autowired
    private IBankMethod bankMethod;
    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IMonitorGroupMethod monitorGroupMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param materialReceiveAndPrepareReqParams -
     * @param objCommon                          -
     * @return ObjectIdentifier
     * @author Sun
     * @since 10/26/2018 3:21 PM
     */
    public ObjectIdentifier sxMaterialReceiveAndPrepareReq(Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxMaterialReceiveAndPrepareReq()");
        ObjectIdentifier equipmentID = materialReceiveAndPrepareReqParams.getEquipmentID();

        log.info("In Param's equipmentID = {}", equipmentID.getValue());
        log.info("In Param's portGroup = {}", materialReceiveAndPrepareReqParams.getPortGroup());
        ObjectIdentifier cassetteID = materialReceiveAndPrepareReqParams.getCassetteID();
        log.info("In Param's cassetteID = {}", cassetteID);
        log.info("In Param's lotType = {}", materialReceiveAndPrepareReqParams.getLotType());
        log.info("In Param's subLotType = {}", materialReceiveAndPrepareReqParams.getSubLotType());
        log.info("In Param's creatingLotID = {}", materialReceiveAndPrepareReqParams.getCreatingLotID().getValue());
        log.info("In Param's vendorLotID = {}", materialReceiveAndPrepareReqParams.getVendorLotID().getValue());
        log.info("In Param's vendorID = {}", materialReceiveAndPrepareReqParams.getVendorID().getValue());
        ObjectIdentifier productID = materialReceiveAndPrepareReqParams.getProductID();
        log.info("In Param's productID = {}", productID.getValue());
        log.info("In Param's bankID = {}", materialReceiveAndPrepareReqParams.getBankID().getValue());
        log.info("In Param's claimMemo = {} ", materialReceiveAndPrepareReqParams.getClaimMemo());

        //【Step-1】Check eqp ID is SORTER or
        log.info("Check Transaction ID and eqp Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        //【Step-3-1】 Lock eqp Main Object
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.VENDOR_LOT_RECEIVE_AND_PREPARE_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        if (!objLockModeOut.getLockMode().equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            // Lock Equipment Main Object
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                    objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
            // 【Step-3-2】check if the cassetteID is a FOUP
            log.info("calling cassetteGetStatusDR()");
            int retCode = 0;
            try {
                Outputs.ObjCassetteStatusOut objCassetteStatusOutRetCode = cassetteMethod.cassetteGetStatusDR(objCommon, cassetteID);
            } catch (ServiceException e) {
                retCode = e.getCode();
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                    throw e;
                }
            }
            if (retCode == 0) {
                // Lock Equipment LoadCassette Element (Write)
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, Collections.singletonList(cassetteID.getValue())));
            } else {
                // FOSB, do nothing
                log.info("cassette_getStatusDR() = RC_NOT_FOUND_CASSETTE");
            }
        }
        // FOUP
        log.info("cassetteGetStatusDR executed success.");
        // 【Step-4】Checking Previous Job is running or not
        try {
            waferMethod.waferSorterCheckRunningJobs(objCommon, equipmentID, materialReceiveAndPrepareReqParams.getPortGroup());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfigEx.getWaferSortAlreadyRunning(), e.getCode())) {
                log.error("SorterJob is already running");
                throw e;
            } else if (!Validations.isEquals(retCodeConfigEx.getWaferSortPreviousJobNotFound(), e.getCode())) {
                throw e;
            }
        }


        // 【Step-5】Checking lottype is Vendor or not
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, materialReceiveAndPrepareReqParams.getLotType()),
                retCodeConfig.getInvalidLotType(), materialReceiveAndPrepareReqParams.getLotType(), "*****");

        //check if product usage match the carrier usage
        contaminationMethod.carrierProductUsageTypeCheck(productID, ObjectIdentifier.emptyIdentifier(), cassetteID);

        // 【Step-6】Get SLOTMAP DATA When Requested
        Infos.WaferSorterSlotMap strReferenceCondition = new Infos.WaferSorterSlotMap();
        String requiredData = BizConstant.SP_SORTER_SLOTMAP_LATESTDATA;

        strReferenceCondition.setPortGroup(materialReceiveAndPrepareReqParams.getPortGroup());
        strReferenceCondition.setEquipmentID(materialReceiveAndPrepareReqParams.getEquipmentID());
        strReferenceCondition.setActionCode(CimStringUtils.isEmpty(materialReceiveAndPrepareReqParams.getActionCode()) ? BizConstant.SP_SORTER_READ : materialReceiveAndPrepareReqParams.getActionCode());
        strReferenceCondition.setRequestTime(null);
        strReferenceCondition.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
        strReferenceCondition.setWaferID(null);
        strReferenceCondition.setLotID(null);
        strReferenceCondition.setDestinationCassetteID(cassetteID);
        strReferenceCondition.setDestinationPortID(null);
        strReferenceCondition.setDestinationCassetteManagedByOM(false);
        strReferenceCondition.setDestinationSlotNumber(0L);
        strReferenceCondition.setOriginalCassetteID(null);
        strReferenceCondition.setDestinationPortID(null);
        strReferenceCondition.setOriginalCassetteManagedByOM(false);
        strReferenceCondition.setOriginalSlotNumber(0L);
        strReferenceCondition.setRequestUserID(null);
        strReferenceCondition.setReplyTime("");
        strReferenceCondition.setSorterStatus(BizConstant.SP_SORTER_SUCCEEDED);
        strReferenceCondition.setSlotMapCompareStatus("");
        strReferenceCondition.setOmsCompareStatus("");

        //waferMethod.waferSorterSlotMapSelectDR();
        List<Infos.WaferSorterSlotMap> outWaferSorterSlotMapSelectDR = waferMethod.waferSorterSlotMapSelectDR(objCommon, requiredData, "",
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, strReferenceCondition);


        // 【Step-7】 Check whether the same wafer ID is
        //  cyclically reported from wafer sorter or not
        String waferID = BizConstant.EMPTY;
        Boolean bSameWaferIDReported = false;
        int nReadWaferLen = CimArrayUtils.getSize(outWaferSorterSlotMapSelectDR);
        for (int i = 0; i < nReadWaferLen - 1; i++) {
            Infos.WaferSorterSlotMap iWaferSorterSlotMap = outWaferSorterSlotMapSelectDR.get(i);
            for (int j = i + 1; j < nReadWaferLen; j++) {
                Infos.WaferSorterSlotMap jWaferSorterSlotMap = outWaferSorterSlotMapSelectDR.get(j);

                if (!ObjectIdentifier.isEmptyWithValue(iWaferSorterSlotMap.getWaferID()))
                    if (iWaferSorterSlotMap.getWaferID().getValue().compareTo(jWaferSorterSlotMap.getWaferID().getValue()) == 0) {
                        bSameWaferIDReported = true;
                        waferID = iWaferSorterSlotMap.getWaferID().getValue();
                        break;
                    }
            }
            if (CimBooleanUtils.isTrue(bSameWaferIDReported)) {
                break;
            }
        }
        Validations.check(CimBooleanUtils.isTrue(bSameWaferIDReported),
                retCodeConfig.getDuplicateWafer(), waferID);
        //【Step-8】Set Slot Map Return Data
        List<Infos.WaferTransfer> waferTransferList = new ArrayList<>();
        log.info("nReadWaferLen = {}", nReadWaferLen);

        for (int i = 0; i < nReadWaferLen; i++) {
            log.info("Loop Count = {}", i);
            Infos.WaferSorterSlotMap iWaferSorterSlotMap = outWaferSorterSlotMapSelectDR.get(i);

            Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
            waferTransfer.setWaferID(iWaferSorterSlotMap.getWaferID());
            waferTransfer.setDestinationCassetteID(iWaferSorterSlotMap.getDestinationCassetteID());
            waferTransfer.setBDestinationCassetteManagedByOM(iWaferSorterSlotMap.getDestinationCassetteManagedByOM());
            waferTransfer.setDestinationSlotNumber(Integer.parseInt(iWaferSorterSlotMap.getDestinationSlotNumber().toString()));
            waferTransfer.setOriginalCassetteID(iWaferSorterSlotMap.getOriginalCassetteID());
            waferTransfer.setBOriginalCassetteManagedByOM(iWaferSorterSlotMap.getOriginalCassetteManagedByOM());
            waferTransfer.setOriginalSlotNumber(CimNumberUtils.intValue(iWaferSorterSlotMap.getOriginalSlotNumber()));

            log.info("WaferID  = {}", waferTransfer.getWaferID().getValue());
            log.info("destinationCassetteID = {}", waferTransfer.getDestinationCassetteID().getValue());
            log.info("bDestinationCassetteManagedByOM = {}", waferTransfer.getBDestinationCassetteManagedByOM());
            log.info("destinationSlotNumber = {}", waferTransfer.getDestinationSlotNumber());
            log.info("bDestinationCassetteManagedByOM = {}", waferTransfer.getOriginalCassetteID().getValue());
            log.info("bOriginalCassetteManagedByOM = {}", waferTransfer.getBOriginalCassetteManagedByOM());
            log.info("originalSlotNumber  = {}", waferTransfer.getOriginalSlotNumber());

            waferTransferList.add(waferTransfer);
        }

        // 【Step-9】Copy input parameter and use tmpNewLotAttributes inside of this method
        Infos.NewLotAttributes tmpNewLotAttributes = new Infos.NewLotAttributes();

        List<Infos.NewWaferAttributes> strNewWaferAttributesList = new ArrayList<>();
        tmpNewLotAttributes.setNewWaferAttributesList(strNewWaferAttributesList);

        tmpNewLotAttributes.setCassetteID(cassetteID);

        int unknownWaferCnt = 0;
        Boolean bFindWaferNull = false;
        Boolean bFindWaferFill = false;

        for (int i = 0; i < nReadWaferLen; i++) {
            Infos.WaferTransfer iWaferTransfer = waferTransferList.get(i);

            //【Step-9-1】All wafer is NULL or Filled Check
            if (CimObjectUtils.isEmpty(iWaferTransfer.getWaferID().getValue())) {
                bFindWaferNull = true;
            } else {
                bFindWaferFill = true;
            }
            Validations.check(CimBooleanUtils.isTrue(bFindWaferFill) && CimBooleanUtils.isTrue(bFindWaferNull), retCodeConfigEx.getInvalidWaferIdReadResultForVLRP());

            //【Step-9-2】Get lot ID from wafer ID  (0.0.6)
            ObjectIdentifier waferLotRetCode = null;
            try {
                waferLotRetCode = waferMethod.waferLotGet(objCommon, iWaferTransfer.getWaferID());
            } catch (ServiceException e) {
                Infos.NewWaferAttributes strNewWaferAttributes = new Infos.NewWaferAttributes();

                strNewWaferAttributes.setNewLotID(materialReceiveAndPrepareReqParams.getCreatingLotID());
                strNewWaferAttributes.setNewWaferID(iWaferTransfer.getWaferID());
                strNewWaferAttributes.setNewSlotNumber(iWaferTransfer.getDestinationSlotNumber());
                strNewWaferAttributes.setSourceLotID(materialReceiveAndPrepareReqParams.getCreatingLotID());

                log.info("waferLotGet() return {}.", waferLotRetCode);
                log.info("Unknown wafer Count = {}", unknownWaferCnt + 1);
                log.info("creatingLotID  = {}", strNewWaferAttributes.getNewLotID().getValue());
                log.info("newWaferID = {}", strNewWaferAttributes.getNewWaferID().getValue());
                log.info("newSlotNumber = {}", strNewWaferAttributes.getNewSlotNumber());
                log.info("sourceLotID = {}", strNewWaferAttributes.getSourceLotID().getValue());

                strNewWaferAttributesList.add(strNewWaferAttributes);
            }
        }

        //【Step-10】Check Unknown wafer Exist
        unknownWaferCnt = CimArrayUtils.getSize(strNewWaferAttributesList);
        Validations.check(unknownWaferCnt == 0, retCodeConfigEx.getNoNeedWaferPreparation());
        //【Step-11】Check eqp availability
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);
        //【Step-12】Create vendorLot's PosLot data
        Params.VendorLotReceiveParams vendorLotReceiveParams = new Params.VendorLotReceiveParams();
        vendorLotReceiveParams.setBankID(materialReceiveAndPrepareReqParams.getBankID());
        vendorLotReceiveParams.setProductID(productID);
        vendorLotReceiveParams.setLotID(materialReceiveAndPrepareReqParams.getCreatingLotID().getValue());
        vendorLotReceiveParams.setSubLotType(materialReceiveAndPrepareReqParams.getSubLotType());
        vendorLotReceiveParams.setProductWaferCount(unknownWaferCnt);
        vendorLotReceiveParams.setVendorLotID(materialReceiveAndPrepareReqParams.getVendorLotID().getValue());
        vendorLotReceiveParams.setVendorID(materialReceiveAndPrepareReqParams.getVendorID().getValue());
        Results.VendorLotReceiveReqResult makeVendorLotRetCode;
        try {
            makeVendorLotRetCode = lotMethod.lotMakeVendorLot(objCommon, vendorLotReceiveParams);

            //【Step-13】 History, Output data
            Inputs.VendorLotEventMakeParams vendorLotEventMakeParams = new Inputs.VendorLotEventMakeParams();
            vendorLotEventMakeParams.setTransactionID(TransactionIDEnum.VENDOR_LOT_RECEIVE_AND_PREPARE_REQ.toString());
            vendorLotEventMakeParams.setLotID(new ObjectIdentifier(makeVendorLotRetCode.getCreatedLotID()));
            vendorLotEventMakeParams.setClaimQuantity(unknownWaferCnt);
            vendorLotEventMakeParams.setClaimMemo(materialReceiveAndPrepareReqParams.getClaimMemo());
            eventMethod.vendorLotEventMake(objCommon, vendorLotEventMakeParams);

        } catch (ServiceException e) {
            log.info("lotMakeVendorLot return {}.", e.getCode());
            if (Validations.isEquals(retCodeConfig.getDuplicateLot(), e.getCode())) {
                Results.VendorLotReceiveReqResult vendorLotReceiveReqResult = new Results.VendorLotReceiveReqResult();
                vendorLotReceiveReqResult.setCreatedLotID(materialReceiveAndPrepareReqParams.getCreatingLotID().getValue());
                makeVendorLotRetCode = vendorLotReceiveReqResult;
            } else {
                throw e;
            }
        }

        log.info("vendorLotEventMake lotID = {}.", makeVendorLotRetCode.getCreatedLotID());

        for (int i = 0; i < unknownWaferCnt; i++) {
            Infos.NewWaferAttributes strNewWaferAttributes = tmpNewLotAttributes.getNewWaferAttributesList().get(i);
            strNewWaferAttributes.getSourceLotID().setValue(makeVendorLotRetCode.getCreatedLotID());
        }

        //【Step-14】Object Lock for cassette
        Boolean bCasstteIsFOSB = false;

        log.info("objectLock START:cassetteID = {}", tmpNewLotAttributes.getCassetteID().getValue());
        try {
            objectLockMethod.objectLock(objCommon, CimCassette.class, tmpNewLotAttributes.getCassetteID());
        } catch (ServiceException e) {
            log.info("objectLock return {}", e.getCode());
            if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                bCasstteIsFOSB = true;
                log.info("rc == RC_NOT_FOUND_CASSETTE:SET bCasstteIsFOSB = TRUE");

                // Check for cassette is FOSB And All wafer is NULL Or
                Validations.check((CimBooleanUtils.isTrue(bFindWaferNull)) && (CimBooleanUtils.isFalse(bFindWaferFill)), retCodeConfigEx.getInvalidWaferIdReadResultForVLRP());
            } else {
                throw e;
            }
        }
        //【Step-15】Check SorterJob existence
        Infos.EquipmentLoadPortAttribute dummyEquipmentPortAttribute = new Infos.EquipmentLoadPortAttribute();
        List<ObjectIdentifier> dummyLotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(tmpNewLotAttributes.getCassetteID());

        Inputs.ObjWaferSorterJobCheckForOperation inputSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();

        inputSorterJobCheckForOperation.setEquipmentLoadPortAttribute(dummyEquipmentPortAttribute);
        inputSorterJobCheckForOperation.setCassetteIDList(cassetteIDs);
        inputSorterJobCheckForOperation.setLotIDList(dummyLotIDs);
        inputSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_DESTCAST);

        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, inputSorterJobCheckForOperation);

        // 【Step-16】Object Lock for source lot
        for (int i = 0; i < unknownWaferCnt; i++) {
            Infos.NewWaferAttributes strNewWaferAttributes = tmpNewLotAttributes.getNewWaferAttributesList().get(i);
            log.info("objectLock START:sourceLotID = {}.", strNewWaferAttributes.getSourceLotID().getValue());
            objectLockMethod.objectLock(objCommon, CimLot.class, strNewWaferAttributes.getSourceLotID());
        }

        //【Step-17】 Prepare Product Request for New Vendor lot
        Outputs.ObjProductRequestForVendorLotReleaseOut objProductRequestForVendorLotReleaseOut = productMethod.productRequestForVendorLotRelease(objCommon, materialReceiveAndPrepareReqParams.getBankID(),
                tmpNewLotAttributes.getNewWaferAttributesList().get(0).getSourceLotID(),
                unknownWaferCnt,
                materialReceiveAndPrepareReqParams.getLotType(),
                materialReceiveAndPrepareReqParams.getSubLotType());

        //【Step-18】Copy generated New lot ID to structure
        for (int i = 0; i < unknownWaferCnt; i++) {
            Infos.NewWaferAttributes strNewWaferAttributes = tmpNewLotAttributes.getNewWaferAttributesList().get(i);

            strNewWaferAttributes.setNewLotID(objProductRequestForVendorLotReleaseOut.getCreateProductRequestID());
            log.info("newLotID = {}", objProductRequestForVendorLotReleaseOut.getCreateProductRequestID());
        }

        // 【Step-19】Check input parameter
        if (CimBooleanUtils.isFalse(bCasstteIsFOSB)) {
            Outputs.ObjLotParameterForLotGenerationCheckOut lotGenerationCheckOutRetCode = lotMethod.lotParameterForLotGenerationCheck(objCommon,
                    materialReceiveAndPrepareReqParams.getBankID(), tmpNewLotAttributes);

            //【Step-20】lotWaferIDGenerate;
            if (CimBooleanUtils.isTrue(lotGenerationCheckOutRetCode.isWaferIDAssignRequiredFlag())) {
                log.info("isWaferIDAssignRequiredFlag == TRUE");
                Outputs.ObjLotWaferIDGenerateOut lotWaferIDGenerateOutRetCode = lotMethod.lotWaferIDGenerate(objCommon, tmpNewLotAttributes);
                tmpNewLotAttributes = lotWaferIDGenerateOutRetCode.getNewLotAttributes();
            }
        }

        // 【Step-21】Prepare wafer of source lot
        log.info("unknownWaferCnt = {}", unknownWaferCnt);
        for (int i = 0; i < unknownWaferCnt; i++) {
            Infos.NewWaferAttributes strNewWaferAttributes = tmpNewLotAttributes.getNewWaferAttributesList().get(i);

            log.info("tmpNewLotAttributes.strNewWaferAttributes Loop Count {}", i);
            log.info("tmpNewLotAttributes.strNewWaferAttributes sourceWaferID = {}", strNewWaferAttributes.getSourceWaferID());
            if (ObjectIdentifier.isEmpty(strNewWaferAttributes.getSourceWaferID())) {
                log.info("sourceWaferID is null");
                //【Step-1】lotWaferCreate
                Outputs.ObjLotWaferCreateOut lotWaferCreateOutRetCode;
                try {
                    lotWaferCreateOutRetCode = lotMethod.lotWaferCreate(objCommon, strNewWaferAttributes.getSourceLotID(), strNewWaferAttributes.getNewWaferID().getValue());
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getDuplicateWafer(), e.getCode())) {
                        log.info("lotWaferCreate() return : wafer Dupulicate = {}", e.getCode());
                        continue;
                    } else {
                        throw e;
                    }
                }

                if (lotWaferCreateOutRetCode != null) {
                    strNewWaferAttributes.setSourceWaferID(lotWaferCreateOutRetCode.getNewWaferID());

                    log.info("newWaferID = {}", lotWaferCreateOutRetCode.getNewWaferID());
                }

                //【Step-1】waferMaterialContainerChange
                Infos.Wafer strWafer = new Infos.Wafer();
                if (lotWaferCreateOutRetCode != null) strWafer.setWaferID(lotWaferCreateOutRetCode.getNewWaferID());
                strWafer.setSlotNumber(strNewWaferAttributes.getNewSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, tmpNewLotAttributes.getCassetteID(), strWafer);
            }
        }

        //【Step-22】Case of cassette is not FOSB
        ObjectIdentifier tmpCreatedLotID;
        if (CimBooleanUtils.isFalse(bCasstteIsFOSB)) {
            //【Step-1】Check source lot's condition
            bankMethod.bankLotPreparationCheck(objCommon,
                    materialReceiveAndPrepareReqParams.getBankID(),
                    materialReceiveAndPrepareReqParams.getLotType(),
                    materialReceiveAndPrepareReqParams.getSubLotType(),
                    tmpNewLotAttributes);

            //【Step-2】lot Preparation
            tmpCreatedLotID = bankMethod.bankLotPreparation(objCommon, objProductRequestForVendorLotReleaseOut.getCreateProductRequestID(), materialReceiveAndPrepareReqParams.getBankID(), tmpNewLotAttributes);
        } else {
            // 【Step-1】Check source lot's condition
            bankMethod.bankLotPreparationCheckForWaferSorter(objCommon,
                    materialReceiveAndPrepareReqParams.getBankID(),
                    materialReceiveAndPrepareReqParams.getLotType(),
                    materialReceiveAndPrepareReqParams.getSubLotType(),
                    tmpNewLotAttributes);


            //【Step-2】lot Preparation
            tmpCreatedLotID = bankMethod.bankLotPreparationForWaferSorter(objCommon,
                    objProductRequestForVendorLotReleaseOut.getCreateProductRequestID(),
                    materialReceiveAndPrepareReqParams.getBankID(),
                    tmpNewLotAttributes);
        }
        //clean contamination level
        Params.LotContaminationParams lotContaminationParams = new Params.LotContaminationParams();
        lotContaminationParams.setLotId(tmpCreatedLotID);
        lotContaminationParams.setContaminationLevel(null);
        lotContaminationParams.setPrFlag(0);
        contaminationMethod.lotContaminationUpdate(lotContaminationParams,objCommon);

        //【Step-23】Copy created lot ID to output structure

        //【Step-24】Copy Created New lot Object Identifier in order to create History Data
        for (int i = 0; i < unknownWaferCnt; i++) {
            tmpNewLotAttributes.getNewWaferAttributesList().get(i).setNewLotID(tmpCreatedLotID);
        }

        if (CimBooleanUtils.isFalse(bCasstteIsFOSB)) {
            // Update cassette multi lot type;
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, tmpNewLotAttributes.getCassetteID());

            //Maintain Loaded cassette on Eqp Information;
            log.info("strCassette_transferState_Get_out.transferState is 'EI'...");
            List<ObjectIdentifier> cassetteIDs2 = new ArrayList<>();
            cassetteIDs2.add(tmpNewLotAttributes.getCassetteID());
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDs2);
        }

        //【Step-25】Make History
        log.info("【Step-25】Make History");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, tmpCreatedLotID);
        eventMethod.lotWaferMoveEventMake(objCommon, tmpNewLotAttributes, TransactionIDEnum.VENDOR_LOT_PREPARATION_AND_PREPARE_REQ.getValue(), materialReceiveAndPrepareReqParams.getClaimMemo());
        //Return to Caller;
        log.info("【Method Exit】sxMaterialReceiveAndPrepareReq()");

        return tmpCreatedLotID;
    }

    public Results.VendorLotReturnReqResult sxVendorLotReturnReq(Infos.ObjCommon objCommon, Params.VendorLotReturnParams vendorLotReturnParams) {
        //step1 - Check lot status
        log.debug("[step-1] Check lot status");
        Results.VendorLotReturnReqResult vendorLotReturnReqObj = new Results.VendorLotReturnReqResult();
        vendorLotReturnReqObj.setVendorLotID(ObjectIdentifier.fetchValue(vendorLotReturnParams.getLotID()));
        String lotState = lotMethod.lotStateGet(objCommon, vendorLotReturnParams.getLotID());

        //step2 - Get theInventoryState
        log.debug("[step-2] Get theInventoryState");
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, vendorLotReturnParams.getLotID());

        //step3 - Get theFinishedState
        log.debug("[step-3] Get theFinishedState");
        String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, vendorLotReturnParams.getLotID());

        //step4 - Get theHoldState
        log.debug("[step-4] Get theHoldState");
        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, vendorLotReturnParams.getLotID());

        //step5 -  Check the all statuses
        log.debug("[step-5]  Check the all statuses");
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotState), retCodeConfig.getInvalidLotStat(), lotState);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_INVENTORY_STATE_INBANK, lotInventoryState), retCodeConfig.getInvalidLotInventoryStat(),
                ObjectIdentifier.fetchValue(vendorLotReturnParams.getLotID()), lotInventoryState);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED, lotFinishedState), retCodeConfig.getInvalidLotFinishStat(), lotFinishedState);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, lotHoldState), retCodeConfig.getInvalidLotHoldStat(), ObjectIdentifier.fetchValue(vendorLotReturnParams.getLotID()), lotHoldState);

        //step6 -  Get lottype
        log.debug("[step-6]  Get lottype");
        String lotType = lotMethod.lotTypeGet(objCommon, vendorLotReturnParams.getLotID());
        /*======================================================*/
        /* Check the LotType                                    */
        /*======================================================*/
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, lotType), retCodeConfig.getInvalidLotType(), lotType, ObjectIdentifier.fetchValue(vendorLotReturnParams.getLotID()));

        //step7 - Subtract requested count from current volume
        log.debug("[step-7]  Subtract requested count from current volume");
        Results.LotQuantitySubtractResult lotQuantitySubtractResult = lotMethod.lotQuantitySubtract(objCommon, vendorLotReturnParams);
        vendorLotReturnReqObj.setProductWaferCount(lotQuantitySubtractResult.getProductCount());

        Results.LotFillInTxBKC007Result lotFillInTxBKC007Obj = lotMethod.lotFillInTxBKC007(objCommon, vendorLotReturnParams.getLotID());

        vendorLotReturnReqObj.setLotID(lotFillInTxBKC007Obj.getLotID());
        vendorLotReturnReqObj.setProductWaferCount(lotFillInTxBKC007Obj.getProductWaferCount());
        vendorLotReturnReqObj.setVendorLotID(lotFillInTxBKC007Obj.getVendorLotID());
        vendorLotReturnReqObj.setProductID(lotFillInTxBKC007Obj.getProductID());
        vendorLotReturnReqObj.setVendorID(lotFillInTxBKC007Obj.getVendorID());
        vendorLotReturnReqObj.setBankID(lotFillInTxBKC007Obj.getBankID());

        /*------------------------------------------------------*/
        /*    Operation History creation                        */
        /*------------------------------------------------------*/
        log.debug("[step-8] Operation History creation");
        Inputs.VendorLotEventMakeParams params = new Inputs.VendorLotEventMakeParams();
        params.setTransactionID(TransactionIDEnum.VEND_LOT_RETURN_REQ.getValue());
        params.setLotID(vendorLotReturnParams.getLotID());
        params.setVendorLotID(lotQuantitySubtractResult.getVendorLotID());
        params.setClaimQuantity(vendorLotReturnParams.getProductWaferCount());
        params.setClaimMemo(vendorLotReturnParams.getClaimMemo());
        eventMethod.vendorLotEventMake(objCommon, params);
        return vendorLotReturnReqObj;
    }


    public Results.VendorLotReceiveReqResult sxVendorLotReceiveReq(Infos.ObjCommon objCommon, Params.VendorLotReceiveParams vendorLotReceiveParams) {
        log.debug("[step-1] create Vendor lot");
        Results.VendorLotReceiveReqResult vendorLotReceiveReqResult = lotMethod.lotMakeVendorLot(objCommon, vendorLotReceiveParams);
        //step2 -  History, Output data
        log.debug("[step-2] History, Output datat");
        Inputs.VendorLotEventMakeParams vendorLotEventMakeParams = new Inputs.VendorLotEventMakeParams();
        vendorLotEventMakeParams.setVendorLotID(vendorLotReceiveParams.getVendorLotID());
        vendorLotEventMakeParams.setLotID(new ObjectIdentifier(vendorLotReceiveReqResult.getCreatedLotID()));
        vendorLotEventMakeParams.setTransactionID(TransactionIDEnum.VEND_LOT_RECEIVE_REQ.getValue());
        vendorLotEventMakeParams.setClaimMemo(vendorLotReceiveParams.getClaimMemo());
        vendorLotEventMakeParams.setClaimQuantity(vendorLotReceiveParams.getProductWaferCount());
        eventMethod.vendorLotEventMake(objCommon, vendorLotEventMakeParams);
        return vendorLotReceiveReqResult;
    }


    public List<String> getProductID(String str) {
        String sql = "SELECT PROD_ID FROM OMPRODINFO  ";
        if (!CimStringUtils.isEmpty(str)) {
            sql += " WHERE PROD_ID LIKE '" + str + "%'";
        }
        List<CimProductSpecificationDO> sqlResult = cimJpaRepository.query(sql, CimProductSpecificationDO.class);

        return sqlResult.stream().map(CimProductSpecificationDO::getProductSpecID).collect(Collectors.toList());
    }

    public List<String> getRaw(String str) {
        String sql = ("SELECT PROD_ID FROM OMPRODINFO  WHERE PROD_CAT_ID = 'Raw'");
        if (!CimStringUtils.isEmpty(str)) {
            sql += " AND PROD_ID LIKE '" + str + "%'";
        }
        List<CimProductSpecificationDO> sqlResult = cimJpaRepository.query(sql, CimProductSpecificationDO.class);

        return sqlResult.stream().map(CimProductSpecificationDO::getProductSpecID).collect(Collectors.toList());
    }


    public List<Infos.ReturnCodeInfo> sxUnshipReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams unshipReqParams) { //nyx-modify

        List<Infos.ReturnCodeInfo> returnCodeInfos = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = unshipReqParams.getLotIDs();
        for (int i = 0; i < CimArrayUtils.getSize(lotIDs); i++) {
            ObjectIdentifier lotID = lotIDs.get(i);
            Infos.ReturnCodeInfo returnCodeInfo = new Infos.ReturnCodeInfo();
            returnCodeInfo.setLotID(lotID);

            String lotState = lotMethod.lotStateGet(objCommon, lotID);
            // step1 - done:theLotState condition check(line:61)
            if (!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_SHIPPED, lotState)) {
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step2 - done:Check lot duration(line:89 -lot_CheckDurationForOperation.dr)
            //lot_CheckDurationForOperation
            Boolean operationFlag = lotMethod.lotCheckDurationForOperation(objCommon, lotID);
            Validations.check(CimBooleanUtils.isFalse(operationFlag), retCodeConfig.getReservedByDeletionProgram());

            // step3 - done:theLotState change to FINISHED(line:124 -lot_state_CancelShipped.cpp)
            try {
                lotMethod.lotStateCancelShipped(objCommon, lotID);
            } catch (ServiceException e) {
                returnCodeInfo.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step4 Make History (line:201 -lotBankMoveEvent_Make.cpp)
            Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
            lotBankMoveEventMakeParams.setLotID(lotID);
            lotBankMoveEventMakeParams.setClaimMemo(unshipReqParams.getClaimMemo());
            lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.SHIP_CANCEL_REQ.getValue());
            try {
                eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
            } catch (ServiceException e) {
                returnCodeInfo.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                returnCodeInfos.add(returnCodeInfo);
            }
        }

        if (!CimArrayUtils.isEmpty(returnCodeInfos)) {
            throw new ServiceException(returnCodeInfos.get(0).getReturnCode(), returnCodeInfos);
//            for (Infos.ReturnCodeInfo returnCodeInfo : returnCodeInfos) {
//                throw new ServiceException(returnCodeInfo.getReturnCode(), returnCodeInfos);
//            }
        }
        return returnCodeInfos;
    }

    public List<Infos.ReturnCodeInfo> sxShipReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams shipReqParams) { //nyx-modify
        log.info("objCommon = {}, BankMoveReqParams = {}", objCommon, shipReqParams);
        List<Infos.ReturnCodeInfo> returnCodeInfos = new ArrayList<>();

        List<ObjectIdentifier> lotIDs = shipReqParams.getLotIDs();
        ObjectIdentifier bankID = shipReqParams.getBankID();

        for (ObjectIdentifier lotID : lotIDs) {
            Infos.ReturnCodeInfo returnCodeInfo = new Infos.ReturnCodeInfo();
            returnCodeInfo.setLotID(lotID);

            // step1 - done:Check Input parameter(line:72 -lot_detailInfo_GetDR__160.dr)
            Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
            lotInfoInqFlag.setLotBasicInfoFlag(true);
            Infos.LotInfo lotInfoRetCode;
            try {
                lotInfoRetCode = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqFlag, lotID);
            } catch (ServiceException e) {
                log.error("getLotDetailInfo() != ok");
                returnCodeInfo.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            Infos.LotBasicInfo lotBasicInfo = lotInfoRetCode.getLotBasicInfo();
            if (CimObjectUtils.isEmpty(lotBasicInfo) || !ObjectIdentifier.equalsWithValue(bankID, lotBasicInfo.getBankID())) {
                log.error("InPara's bankID and Lot's bankID are different.");
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidBankId(), ObjectIdentifier.fetchValue(bankID)));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step2 - done:theLotState condition check (line:112 -lot_state_Get.cpp)
            String lotState = lotMethod.lotStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotState)) {
                log.error("LotState != FINISHED");
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step3 - done:theFinishedState condition check(line:141 -lot_finishedState_Get.cpp)
            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED, lotFinishedState)) {
                log.error("LotFinishedState != COMPLETED");
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinishedState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step4 - done:theInventoryState condition check(line:165 -lot_inventoryState_Get.cpp)
            String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, lotInventoryState)) {
                log.error("LotInventoryState != InBank");
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidLotInventoryStat(), lotInventoryState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step5 - done:theHoldState condition check(line:189 -lot_holdState_Get.cpp)
            String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
            if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState)) {
                log.error("LotHoldState == ONHOLD");
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInvalidLotHoldStat(), ObjectIdentifier.fetchValue(lotID), lotHoldState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            // step6 - done:theLotState change to SHIPPED (line:212 -lot_state_ChangeShipped.cpp)
            try {
                lotMethod.lotStateChangeShipped(objCommon, lotID);
            } catch (ServiceException e) {
                returnCodeInfo.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }


            // step7 Make History (line:201 -lotBankMoveEvent_Make.cpp)
            Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
            lotBankMoveEventMakeParams.setLotID(lotID);
            lotBankMoveEventMakeParams.setClaimMemo(shipReqParams.getClaimMemo());
            lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.SHIP_REQ.getValue());
            try {
                eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
            } catch (ServiceException e) {
                returnCodeInfo.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                returnCodeInfos.add(returnCodeInfo);
            }

        }
        if (!CimArrayUtils.isEmpty(returnCodeInfos)) {
            throw new ServiceException(returnCodeInfos.get(0).getReturnCode(), returnCodeInfos.get(0).getLotID());
//            for (Infos.ReturnCodeInfo returnCodeInfo : returnCodeInfos) {
//                throw new ServiceException(returnCodeInfo.getReturnCode(), returnCodeInfo.getLotID());
//            }
        }
        return returnCodeInfos;
    }

    public void sxNonProdBankStoreReq(Infos.ObjCommon objCommon, Params.NonProdBankStoreReqParams params) {
        ObjectIdentifier lotID = params.getLotID();
        log.debug("[step0]Get lot / cassette connection");
        ObjectIdentifier cassetteID = null;
        try {
            cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.error(e.getCode() + e.getMessage());
            if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                throw e;
            }
        }

        log.debug("[step1]Lock objects to be updated");
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        log.debug("[step2]Skip cassette lock to increase parallel availability under PostProcess parallel execution");
        log.trace("strParallelPostProcFlag : {}",strParallelPostProcFlag);
        if (CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
            //PostProcess sequential execution
            objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        }
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        log.trace("lotInterFabXferState : {}",lotInterFabXferState);
        if (!CimStringUtils.equals(lotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            lotIDList.add(lotID);
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDList);

            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
            log.trace("lotInPostProcessFlagOut.getInPostProcessFlagOfLot() : {}",lotInPostProcessFlagOut.getInPostProcessFlagOfLot());
            if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
                List<ObjectIdentifier> userGroupListGetDR = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int nCnt;
                for (nCnt = 0; nCnt < CimArrayUtils.getSize(userGroupListGetDR); nCnt++) {
                    log.trace("userGroupListGetDR.get(nCnt) : {};",userGroupListGetDR.get(nCnt));
                }
                Validations.check(nCnt == CimArrayUtils.getSize(userGroupListGetDR), new OmCode(retCodeConfig.getLotInPostProcess(), ObjectIdentifier.fetchValue(lotID)));
            }
        }

        log.debug("[step3]Check for flow batch");
        try {
            lotMethod.lotFlowBatchIDGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.error(e.getCode() + e.getMessage());
            if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
            } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                throw new ServiceException(retCodeConfig.getLotFlowBatchIdFilled());
            } else {
                throw e;
            }
        }
        log.debug("[step4]Check for controljob");
        ObjectIdentifier objLotControlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, lotID);
        Validations.check(!ObjectIdentifier.isEmptyWithValue(objLotControlJobIDGetOut), new OmCode(
                retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(lotID), ObjectIdentifier.fetchValue(objLotControlJobIDGetOut)));


        log.debug("[step5]Check for monitorgroup ");
        //monitorGroup_GetDR
        List<Infos.MonitorGroups> monitorGroupsList = monitorGroupMethod.monitorGroupGetDR(objCommon, lotID);
        Validations.check(!CimArrayUtils.isEmpty(monitorGroupsList), new OmCode(retCodeConfig.getAlreadyExistMonitorGroup(), ObjectIdentifier.fetchValue(lotID)));

        log.debug("[step6]Call txHoldLotReq ");
        ObjectIdentifier currentRouteID = lotMethod.lotCurrentRouteIDGet(objCommon, lotID);
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_BANKHOLD);
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_NONPROBANKHOLD));
        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
        lotHoldReq.setRouteID(currentRouteID);
        lotHoldReq.setOperationNumber(BizConstant.SP_LOTCUSTOMIZE_DUMMY_OPERATIONNUMBER);
        lotHoldReq.setClaimMemo(params.getClaimMemo());
        List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
        lotHoldReqList.add(lotHoldReq);
        lotService.sxHoldLotReq(objCommon, lotID, lotHoldReqList);

        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
        Validations.check(!CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR), new OmCode(
                retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(lotID), lotInventoryState));

        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
        Validations.check(!CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD), new OmCode(
                retCodeConfig.getInvalidLotHoldStat(), ObjectIdentifier.fetchValue(lotID), lotHoldState));



        log.debug("[step7]Change bank of the lot");
        lotMethod.lotNonProBankIn(objCommon, lotID, params.getBankID());

        log.debug("[step8]Make History");
        Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
        lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.NON_PRO_BANK_IN_REQ.getValue());
        lotBankMoveEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotBankMoveEventMakeParams.setLotID(params.getLotID());
        eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
    }

    public void sxNonProdBankReleaseReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, boolean holdCheckFlag) {
        //com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        //Validations.check(StringUtils.isEmpty(lot), retCodeConfig.getNotFoundLot());

        log.debug("[step0]Get lot / cassette connection");
        int retCode = 0;
        ObjectIdentifier lotCassetteOut = null;
        try {
            lotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotID);
        } catch (ServiceException e) {
            log.error(e.getCode() + e.getMessage());
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                throw e;
            }
        }
        log.debug("[step1]Lock objects to be updated");
        log.trace("retCode : {}",retCode);
        if (retCode != retCodeConfig.getNotFoundCst().getCode()) {
            log.debug("add lock for lotCassetteOut : {}",lotCassetteOut);
            objectLockMethod.objectLock(objCommon, CimCassette.class, lotCassetteOut);
        }
        log.debug("add lock for lotID : {}",lotID);
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        log.debug("[step2]Check whether lot has hold record or not");
        List<Infos.LotHoldListAttributes> lotHoldListAttributesList = new ArrayList<>();
        try {
            lotHoldListAttributesList = lotInqService.sxHoldLotListInq(objCommon, lotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                throw e;
            }
        }
        for (Infos.LotHoldListAttributes lotHoldListAttributes : lotHoldListAttributesList) {
            log.debug("[step3]If this lot is held by NonProBankHold reason, then call hold release request");
            log.trace("lotHoldListAttributes.getHoldType() : {}",lotHoldListAttributes.getHoldType());
            if (CimStringUtils.equals(BizConstant.SP_HOLDTYPE_BANKHOLD, lotHoldListAttributes.getHoldType())
                    && CimStringUtils.equals(BizConstant.SP_REASON_NONPROBANKHOLD, ObjectIdentifier.fetchValue(lotHoldListAttributes.getReasonCodeID()))) {

                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
                lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
                lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
                lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
                lotHoldReq.setRouteID(lotHoldListAttributes.getResponsibleRouteID());
                lotHoldReq.setOperationNumber(lotHoldListAttributes.getResponsibleOperationNumber());
                lotHoldReq.setRelatedLotID(lotHoldListAttributes.getRelatedLotID());
                lotHoldReq.setClaimMemo(lotHoldListAttributes.getClaimMemo());
                List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
                lotHoldReqList.add(lotHoldReq);
                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                holdLotReleaseReqParams.setLotID(lotID);
                holdLotReleaseReqParams.setReleaseReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE));
                holdLotReleaseReqParams.setHoldReqList(lotHoldReqList);
                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                break;
            }
        }

        for (Infos.LotHoldListAttributes lotHoldListAttributes1 : lotHoldListAttributesList) {
            log.trace("lotHoldListAttributes1.getReasonCodeID() : {}",lotHoldListAttributes1.getReasonCodeID());
            if (ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_BACKUPOPERATION_HOLD, lotHoldListAttributes1.getReasonCodeID())) {
                String transactionID = objCommon.getTransactionID();
                log.trace("transactionID : {}",transactionID);
                if (TransactionIDEnum.equals(TransactionIDEnum.TXBOC006, transactionID) && TransactionIDEnum.equals(TransactionIDEnum.TXBOC009, transactionID)) {
                    throw new ServiceException(retCodeConfig.getCannotNonOroBankOutWithBohr());
                }
            }
        }

        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
        log.trace("lotInventoryState : {}",lotInventoryState);
        if (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK, lotInventoryState)) {
            if (holdCheckFlag) {
                return;
            }
            Validations.check(retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(lotID), lotInventoryState);
        }

        if (holdCheckFlag) {
            String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
            log.trace("lotHoldState : {}",lotHoldState);
            if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState)) {
                return;
            }
        }
        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferState),
                new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), ObjectIdentifier.fetchValue(lotID), lotInterFabXferState));

        log.debug("[step5]Change bank of the lot");
        String nonProBankOut = lotMethod.lotNonProBankOut(objCommon, lotID);


        log.debug("[step6]History, Output data");
        Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
        lotBankMoveEventMakeParams.setLotID(lotID);
        lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.NON_PRO_BANK_OUT_REQ.getValue());
        lotBankMoveEventMakeParams.setClaimMemo("");
        eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/24       DSN201804241831     bear               use runFlag to skip some step which we could not care about, as Object Lock Mode, sorterJob, make History
     *
     * @param materialPrepareReqParams - vendor lot preparation require params
     * @param objCommon                - common object
     * @return com.fa.cim.dto.result.RetCode<MaterialPrepareReqResult>
     * @author Bear
     * @since 2018/4/18
     */
    public Results.MaterialPrepareReqResult sxMaterialPrepareReq(Infos.ObjCommon objCommon,
                                                                 Params.MaterialPrepareReqParams materialPrepareReqParams) {
        Results.MaterialPrepareReqResult materialPrepareReqResult = new Results.MaterialPrepareReqResult();
        Infos.NewLotAttributes tmpNewLotAttributes = materialPrepareReqParams.getNewLotAttributes();
        ObjectIdentifier bankID = materialPrepareReqParams.getBankID();
        String lotType = materialPrepareReqParams.getLotType();
        String subLotType = materialPrepareReqParams.getSubLotType();
        //【step1】 getCassetteLocationInfo
        log.debug("[step-1] cassette_LocationInfo_GetDR");
        Infos.LotLocationInfo cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(tmpNewLotAttributes.getCassetteID());
        // step2
        log.debug("[step-2] Get required equipment lock mode");
        log.trace("StringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) : {}", CimStringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN));
        if (CimStringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
            // Get required equipment lock mode
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(cassetteLocationInfo.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.VENDOR_LOT_PREPARATION_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            log.trace("!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE) :{}", !lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE));
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // Lock Equipment Main Object
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                // Lock Equipment LoadCassette Element (Write)
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                        Collections.singletonList(tmpNewLotAttributes.getCassetteID().getValue())));
            }
        }
        // step3 - lock cassette
        log.debug("[step-3] lock cassette");
        objectLockMethod.objectLock(objCommon, CimCassette.class, tmpNewLotAttributes.getCassetteID());
        // step4 - object lock for source lot
        for (Infos.NewWaferAttributes waferAttributes : materialPrepareReqParams.getNewLotAttributes().getNewWaferAttributesList()) {
            objectLockMethod.objectLock(objCommon, CimLot.class, waferAttributes.getSourceLotID());
        }
        //step5 - check sorter job existence(cassetteIDs)
        log.debug("[step-5] check sorter job existence");
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(tmpNewLotAttributes.getCassetteID());
        objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_DESTCAST);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);

        //step5 - check sorter job existence(lotIDs)
        log.debug("[step-5] check sorter job existence");
        Inputs.ObjWaferSorterJobCheckForOperation checkForLotId  = new Inputs.ObjWaferSorterJobCheckForOperation();
        Set<ObjectIdentifier> lotIDSet = new HashSet<>();
        tmpNewLotAttributes.getNewWaferAttributesList().forEach(lotAttribute->lotIDSet.add(lotAttribute.getSourceLotID()));
        checkForLotId.setLotIDList(new ArrayList<>(lotIDSet));
        checkForLotId.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, checkForLotId);

        //【step6】prepare product request for new vendor lot
        log.debug("[step-6] prepare product request for new vendor lot");
        Outputs.ObjProductRequestForVendorLotReleaseOut objProductRequestForVendorLotReleaseOut = productMethod.productRequestForVendorLotRelease(objCommon,
                bankID, materialPrepareReqParams.getNewLotAttributes().getNewWaferAttributesList().get(0).getSourceLotID(),
                materialPrepareReqParams.getNewLotAttributes().getNewWaferAttributesList().size(), lotType, subLotType);
        // copy generated New lot ID to structure
        log.debug("[step-7] copy generated New lot ID to structure");
        List<Infos.NewWaferAttributes> newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        int nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < nLen; i++) {
            ObjectIdentifier newLotID = objProductRequestForVendorLotReleaseOut.getCreateProductRequestID();
            tmpNewLotAttributes.getNewWaferAttributesList().get(i).setNewLotID(newLotID);
        }
        //【step8】check input parameter
        log.debug("[step-8] check input parameter");
        Outputs.ObjLotParameterForLotGenerationCheckOut checkOut = lotMethod.lotParameterForLotGenerationCheck(objCommon, bankID, tmpNewLotAttributes);
        //【step9】generate lot WaferID
        log.debug("[step-9] generate lot WaferID");
        log.trace("checkOut.isWaferIDAssignRequiredFlag() : {}", checkOut.isWaferIDAssignRequiredFlag());
        if (checkOut.isWaferIDAssignRequiredFlag()) {
            Outputs.ObjLotWaferIDGenerateOut generateOut = lotMethod.lotWaferIDGenerate(objCommon, tmpNewLotAttributes);
            tmpNewLotAttributes = generateOut.getNewLotAttributes();
        }
        List<Infos.NewWaferAttributes> newWaferAttributesList2 = tmpNewLotAttributes.getNewWaferAttributesList();
        nLen = CimArrayUtils.getSize(newWaferAttributesList2);
        for (int i = 0; i < nLen; i++) {
            log.trace("ObjectUtils.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getNewWaferID())\n" +
                            "                    && !ObjectUtils.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID()) : {}",
                    ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getNewWaferID())
                            && !ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID()));

            if (ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getNewWaferID())
                    && !ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID())) {
                ObjectIdentifier sourceWaferID = tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID();
                tmpNewLotAttributes.getNewWaferAttributesList().get(i).setNewWaferID(sourceWaferID);
            }
        }
        //【step10】prepare wafer of source lot
        log.debug("[step-10] prepare wafer of source lot");
        List<Infos.NewWaferAttributes> newWaferAttributesList3 = tmpNewLotAttributes.getNewWaferAttributesList();
        nLen = CimArrayUtils.getSize(newWaferAttributesList3);
        for (int i = 0; i < nLen; i++) {
            log.trace("ObjectUtils.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID()) : {]", ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID()));
            if (ObjectIdentifier.isEmptyWithValue(tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID())) {
                Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList3.get(i);
                //【step10-1】 create lot-wafer relationship
                log.debug("[step-10-1] create lot-wafer relationship");
                Outputs.ObjLotWaferCreateOut objLotWaferCreateOut = lotMethod.lotWaferCreate(objCommon, newWaferAttributes.getSourceLotID(), newWaferAttributes.getNewWaferID().getValue());
                // 【step10-2】change wafer cassette
                log.debug("[step-10-2] change wafer cassette");
                newWaferAttributes.setSourceWaferID(objLotWaferCreateOut.getNewWaferID());
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setSlotNumber(newWaferAttributes.getNewSlotNumber());
                strWafer.setWaferID(objLotWaferCreateOut.getNewWaferID());
                waferMethod.waferMaterialContainerChange(objCommon, tmpNewLotAttributes.getCassetteID(), strWafer);

                //CHECK THE CAST AND PRODUCT USAGE TYPE
                ObjectIdentifier sourceLotID = tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceLotID();
                contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(), sourceLotID, tmpNewLotAttributes.getCassetteID());
            }
        }
        //【step11】check source lot's condition
        log.debug("[step-11]check source lot's condition");
        bankMethod.bankLotPreparationCheck(objCommon, bankID, lotType, subLotType, tmpNewLotAttributes);


        //【step12】lot preparation
        log.debug("[step-12] lot preparation");
        ObjectIdentifier objBankLotPreparationOut = bankMethod.bankLotPreparation(objCommon, objProductRequestForVendorLotReleaseOut.getCreateProductRequestID(), bankID, tmpNewLotAttributes);
        log.debug("[step-13] copy created lot id to output structure and  copy created new lot object ID in order to create history data");
        // copy created lot id to output structure
        materialPrepareReqResult.setLotID(objBankLotPreparationOut);
        // copy created new lot object ID in order to create history data
        for (int i = 0; i < nLen; i++) {
            tmpNewLotAttributes.getNewWaferAttributesList().get(i).setNewLotID(objBankLotPreparationOut);
        }
        //【step14】update cassette multiple lot type
        log.debug("[step-14] update cassette multiple lot type");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, tmpNewLotAttributes.getCassetteID());
        // step14 - maintain loaded cassette on eqp information
        log.debug("[step-14] maintain loaded cassette on eqp information");
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(tmpNewLotAttributes.getCassetteID());
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDList);
        // step15 - make history
        log.debug("[step-15] make history");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, objBankLotPreparationOut);
        eventMethod.lotWaferMoveEventMake(objCommon, materialPrepareReqParams.getNewLotAttributes(), TransactionIDEnum.VENDOR_LOT_PREPARATION_REQ.getValue(), materialPrepareReqParams.getClaimMemo());
        return materialPrepareReqResult;
    }

    /**
     * description: get required eqp lock mode
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon       -
     * @param equipmentID     -
     * @param className       -
     * @param functionCateory -
     * @return com.fa.cim.pojo.obj.Outputs.ObjLockModeOut
     * @author Bear
     * @since 2018/4/11
     */
    private Outputs.ObjLockModeOut getRequiredEquipmentLockMode(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String className, String functionCateory) {
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(className);
        objLockModeIn.setFunctionCategory(functionCateory);
        objLockModeIn.setUserDataUpdateFlag(false);
        return objectMethod.objectLockModeGet(objCommon, objLockModeIn);
    }

    /**
     * description:
     * find all lot Types from FRLOTTYPE
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return java.util.List
     * @author Bear
     * @since 2018/4/26
     */
    public List<CimLotTypeDO> getLotType() {
        return cimJpaRepository.findAll(CimLotTypeDO.class);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                      - common object
     * @param materialPrepareCancelReqParams -
     * @return Results.MaterialPrepareCancelReqResult
     * @author Bear
     * @since 2018/6/29
     */
    public Results.MaterialPrepareCancelReqResult sxMaterialPrepareCancelReq(Infos.ObjCommon objCommon, Params.MaterialPrepareCancelReqParams materialPrepareCancelReqParams) {
        //【step1】input parameter
        log.debug("[step-1] input parameter");
        ObjectIdentifier preparationCancelledLotID = materialPrepareCancelReqParams.getPreparationCancelledLotID();
        List<Infos.NewVendorLotInfo> inputVendorLotInfoList = materialPrepareCancelReqParams.getNewVendorLotInfoList();

        //【step2】check input preparation cancelled sourceLotID
        log.debug("[step-2] check input preparation cancelled sourceLotID");
        Validations.check(ObjectIdentifier.isEmpty(preparationCancelledLotID) || CimArrayUtils.isEmpty(inputVendorLotInfoList), retCodeConfig.getInvalidInputParam());
        log.debug("in-param [preparationCancelledLotID]: {}", preparationCancelledLotID);

        //【step3】check SP_LOT_PREPARECANCEL

        /*
        String preparationCancelEnv = environmentVariableManager.getValue(EnvConst.SP_LOT_PREPARECANCEL);
        Validations.check(!StringUtils.equals(BizConstant.SP_LOT_PREPARECANCEL_ON, preparationCancelEnv), retCodeConfig.getLotPrepareCancelOff());
        */

        //【step4】check input parameter
        log.debug("[step-4] check input parameter");
        Set<String> tempSet = new HashSet<>();
        for (Infos.NewVendorLotInfo newVendorLotInfo : inputVendorLotInfoList) {
            Validations.check(CimStringUtils.isEmpty(newVendorLotInfo.getSubLotType()), retCodeConfig.getInvalidInputParam());
            log.info("in-param [originalVendorLotID]: {}", newVendorLotInfo.getOriginalVendorLotID());
            log.info("in-param [subLotType]: {}", newVendorLotInfo.getSubLotType());

            // step4-1 check if duplicate input data exist
            log.debug("[step-4-1] check if duplicate input data exist");
            int originalSize = tempSet.size();
            tempSet.add(newVendorLotInfo.getOriginalVendorLotID());
            Validations.check(originalSize == tempSet.size(), retCodeConfig.getDuplicateValuesInInput());
        }

        //--------------------------------------------------------------------------
        //【step5】object lock for lot preparation cancelled lot
        //--------------------------------------------------------------------------
        log.debug("[step-5] object lock for lot preparation cancelled lot");
        objectLockMethod.objectLock(objCommon, CimLot.class, preparationCancelledLotID);

        //【step6】get lot preparation cancel information
        log.debug("[step-6] get lot preparation cancel information");
        Outputs.ObjLotPreparationCancelInfoGetDROut lotPreparationCancelInfoOut = lotMethod.lotPreparationCancelInfoGetDR(objCommon, preparationCancelledLotID);


        //【step7】set collected preparation cancel information to temporary structure
        log.debug("[step-7] set collected preparation cancel information to temporary structure...");
        Infos.PreparationCancelledLotInfo tmpPreparationCancelledLotInfo = lotPreparationCancelInfoOut.getPreparationCancelledLotInfo();
        List<Infos.NewVendorLotInfo> tmpNewVendorLotInfoList = lotPreparationCancelInfoOut.getNewVendorLotInfoList();
        List<Infos.PreparationCancelledWaferInfo> tmpPreparationCancelledWaferInfoList = lotPreparationCancelInfoOut.getPreparationCancelledWaferInfoList();

        //【step8】check if lot isn't in a cassette
        log.debug("[step-8] check if lot isn't in cassette...");
        Validations.check(ObjectIdentifier.isNotEmpty(tmpPreparationCancelledLotInfo.getCassetteID()), new OmCode(retCodeConfig.getLotInCassette(), preparationCancelledLotID.getValue()));

        //【step9】check if preparation cancelled wafers's vendor lots info exist
        log.debug("[step-9] check if preparation cancelled wafers's vendor lots info exist...");

        // set up hash set for duplication checking
        Set<String> tmpSet = new HashSet<>();
        for (Infos.NewVendorLotInfo newVendorLotInfo : tmpNewVendorLotInfoList) {
            log.trace("!StringUtils.isEmpty(newVendorLotInfo.getOriginalVendorLotID()) : {}", !CimStringUtils.isEmpty(newVendorLotInfo.getOriginalVendorLotID()));
            if (!CimStringUtils.isEmpty(newVendorLotInfo.getOriginalVendorLotID())) {
                tmpSet.add(newVendorLotInfo.getOriginalVendorLotID());
            }
        }
        for (Infos.PreparationCancelledWaferInfo preparationCancelledWaferInfo : tmpPreparationCancelledWaferInfoList) {
            Validations.check(ObjectIdentifier.isEmpty(preparationCancelledWaferInfo.getOriginalVendorLotID()), new OmCode(retCodeConfig.getWaferNoOriginalVendorLotInfo(), preparationCancelledWaferInfo.getWaferID().getValue()));

            Validations.check(preparationCancelledWaferInfo.getStbCount() > 0, new OmCode(retCodeConfig.getWaferStbCountMoreThanOne(), preparationCancelledWaferInfo.getWaferID().getValue(), CimObjectUtils.toString(preparationCancelledWaferInfo.getStbCount())));

            //【bear】the preparationCancelledWaferInfo.getOriginalVendorLotID() is ObjectIdentifier.
            boolean vendorInfoFoundFlag = tmpSet.contains(preparationCancelledWaferInfo.getOriginalVendorLotID().getValue());

            // Original vendor lot info for wafer doesn't exist in tmpList, throw exception
            Validations.check(!vendorInfoFoundFlag, new OmCode(retCodeConfig.getOriginalVendorLotInfoNotExist(), preparationCancelledWaferInfo.getOriginalVendorLotID().getValue()));
        }

        //【step10】reset tmpNewVendorLotInfoSeq to call lot_preparationCancel_Check
        log.debug("[step-9] reset tmpNewVendorLotInfoSeq to call lot_preparationCancel_Check");
        Map<String, Infos.NewVendorLotInfo> tmpMap = new HashMap<>();
        for (Infos.NewVendorLotInfo newVendorLotInfo : inputVendorLotInfoList) {
            tmpMap.put(newVendorLotInfo.getOriginalVendorLotID(), newVendorLotInfo);
        }
        for (Infos.NewVendorLotInfo newVendorLotInfo : tmpNewVendorLotInfoList) {
            boolean lotFoundFlag = false;
            String originalVendorLotID = newVendorLotInfo.getOriginalVendorLotID();
            log.trace("tmpMap.containsKey(originalVendorLotID) : {}", tmpMap.containsKey(originalVendorLotID));
            if (tmpMap.containsKey(originalVendorLotID)) {
                newVendorLotInfo.setSubLotType(tmpMap.get(originalVendorLotID).getSubLotType());
                lotFoundFlag = true;
                int newVendorLotWaferCount = 0;
                for (Infos.PreparationCancelledWaferInfo preparationCancelledWaferInfo : tmpPreparationCancelledWaferInfoList) {
                    log.trace("!StringUtils.isEmpty(originalVendorLotID) && ObjectUtils.equalsWithValue(preparationCancelledWaferInfo.getOriginalVendorLotID(), originalVendorLotID) : {}",
                            !CimStringUtils.isEmpty(originalVendorLotID) && ObjectIdentifier.equalsWithValue(preparationCancelledWaferInfo.getOriginalVendorLotID(), originalVendorLotID));

                    if (!CimStringUtils.isEmpty(originalVendorLotID) && ObjectIdentifier.equalsWithValue(preparationCancelledWaferInfo.getOriginalVendorLotID(), originalVendorLotID)) {
                        log.info("This wafer has original vendor lot info. Wafer ID = {}", preparationCancelledWaferInfo.getWaferID().getValue());
                        newVendorLotWaferCount++;
                    }
                }
                newVendorLotInfo.setWaferCount(newVendorLotWaferCount);
            }
            Validations.check(!lotFoundFlag, new OmCode(retCodeConfig.getOriginalVendorLotInfoNotInput(), newVendorLotInfo.getOriginalVendorLotID()));
        }

        //【step11】check lot preparation cancel condition for lot, wafer, bank
        log.debug("[step-11] check lot preparation cancel condition for lot, wafer, bank");
        Inputs.ObjLotPreparationCancelCheckIn objLotPreparationCancelCheckIn = new Inputs.ObjLotPreparationCancelCheckIn();
        objLotPreparationCancelCheckIn.setPreparationCancelledLoID(preparationCancelledLotID);
        objLotPreparationCancelCheckIn.setBankID(tmpPreparationCancelledLotInfo.getBankID());
        objLotPreparationCancelCheckIn.setNewVendorLotInfoList(tmpNewVendorLotInfoList);
        objLotPreparationCancelCheckIn.setPreparationCancelledWaferInfoList(tmpPreparationCancelledWaferInfoList);
        lotMethod.lotPreparationCancelCheck(objCommon, objLotPreparationCancelCheckIn);

        //【step12】lot preparation cancel
        log.debug("[step-12] lot preparation cancel");
        List<Infos.ReceivedLotInfo> objLotPreparationCancelOut = lotMethod.lotPreparationCancel(objCommon, objLotPreparationCancelCheckIn);


        //【step13】copy created lot information to output structure
        log.debug("[step-13] copy created lot information to output structure");
        Results.MaterialPrepareCancelReqResult materialPrepareCancelReqResult = new Results.MaterialPrepareCancelReqResult();
        materialPrepareCancelReqResult.setReceivedLotInfoList(objLotPreparationCancelOut);
        materialPrepareCancelReqResult.setBankID(tmpPreparationCancelledLotInfo.getBankID());
        materialPrepareCancelReqResult.setProductID(tmpNewVendorLotInfoList.get(0).getProductID());// just get the get(0).getProductID()
        //--------------------------------------------------------------------------------------------------------------
        //【step14】prepare structure for making history
        // ######### Attention !! ##########
        //     To reuse the existing LotWaferMoveEvent, lot preparation cancel history information is set to strNewLotAttributesHstry in reverse.
        //     ex)
        //        strNewWaferAttributes[].sourceWaferID = waferID before lot preparation cancel
        //        strNewWaferAttributes[].newWaferID    = waferID before lot preparation cancel
        //        strNewWaferAttributes[].sourceLotID   = LotID after lot preparation cancel
        //        strNewWaferAttributes[].newLotID      = LotID before lot preparation cancel
        //--------------------------------------------------------------------------------------------------------------
        int newVendorLotLen = CimArrayUtils.getSize(objLotPreparationCancelOut);
        int cancelledWaferLen = CimArrayUtils.getSize(tmpPreparationCancelledWaferInfoList);
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        for (int i = 0; i < newVendorLotLen; i++) {
            for (int j = 0; j < cancelledWaferLen; j++) {
                log.trace("ObjectUtils.equalsWithValue(objLotPreparationCancelOut.get(i).getOriginalVendorLotID(), tmpPreparationCancelledWaferInfoList.get(j).getOriginalVendorLotID()): {}",
                        ObjectIdentifier.equalsWithValue(objLotPreparationCancelOut.get(i).getOriginalVendorLotID(), tmpPreparationCancelledWaferInfoList.get(j).getOriginalVendorLotID()));

                if (ObjectIdentifier.equalsWithValue(objLotPreparationCancelOut.get(i).getOriginalVendorLotID(), tmpPreparationCancelledWaferInfoList.get(j).getOriginalVendorLotID())) {
                    Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                    newWaferAttributesList.add(newWaferAttributes);
                    newWaferAttributes.setSourceWaferID(tmpPreparationCancelledWaferInfoList.get(j).getWaferID());
                    newWaferAttributes.setSourceLotID(objLotPreparationCancelOut.get(i).getNewLotID());
                    newWaferAttributes.setNewWaferID(tmpPreparationCancelledWaferInfoList.get(j).getWaferID());
                    newWaferAttributes.setNewLotID(preparationCancelledLotID);
                }
            }
        }

        //  Make History
        log.debug("Make History");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, preparationCancelledLotID);
        int createdLotsLen = CimArrayUtils.getSize(objLotPreparationCancelOut);
        for (int i = 0; i < createdLotsLen; i++) {
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, objLotPreparationCancelOut.get(i).getNewLotID());
        }
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes, TransactionIDEnum.LOT_PREPARATION_CANCEL_REQ.getValue(), materialPrepareCancelReqParams.getClaimMemo());

        // step15- delete wafer
        log.debug("[step-15] delete wafer");
        List<ObjectIdentifier> deleteWaferIDList = new ArrayList<>();
        for (Infos.PreparationCancelledWaferInfo preparationCancelledWaferInfo : tmpPreparationCancelledWaferInfoList) {
            log.trace("!StringUtils.isEmpty(preparationCancelledWaferInfo.getWaferID()) : {}", !ObjectIdentifier.isEmpty(preparationCancelledWaferInfo.getWaferID()));
            if (!ObjectIdentifier.isEmpty(preparationCancelledWaferInfo.getWaferID())) {
                deleteWaferIDList.add(preparationCancelledWaferInfo.getWaferID());
            }
        }
        lotMethod.lotWafersDelete(objCommon, preparationCancelledLotID, deleteWaferIDList);

        return materialPrepareCancelReqResult;
    }

    public List<Infos.HoldHistory> sxHoldLotReleaseInBankReq(Infos.ObjCommon objCommon, Integer i, Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams) {
        List<Infos.HoldHistory> holdLotReleaseInBankReqResult;

        log.debug("step 1 - Check Condition");
        if (!CimArrayUtils.isEmpty(holdLotReleaseInBankReqParams.getLotIDs()) && i >= holdLotReleaseInBankReqParams.getLotIDs().size()) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        log.debug("step 2 - lotStateGet");
        log.debug("get state of lot {}",holdLotReleaseInBankReqParams.getLotIDs().get(i));
        String lotStateGet = lotMethod.lotStateGet(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i));
        log.trace("lot state is {}",lotStateGet);
        if (!CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, lotStateGet)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotStat(), lotStateGet));
        }

        log.debug("step3 - lotFinishedStateGet");
        log.debug("get lot finished state of lot {}",holdLotReleaseInBankReqParams.getLotIDs().get(i));
        String finishedStateGet = lotMethod.lotFinishedStateGet(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i));
        log.trace("log finished state is {}",finishedStateGet);
        if (!CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_COMPLETED, finishedStateGet)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotFinishStat(), finishedStateGet));
        }

        log.debug("step4 - lotHoldStateGet");
        log.debug("get lot hold state of lot {}",holdLotReleaseInBankReqParams.getLotIDs().get(i));
        String holdStateGetOut = lotMethod.lotHoldStateGet(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i));
        log.trace("lot hold state is {}",holdStateGetOut);
        if (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD, holdStateGetOut)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotNotHeld(), ObjectIdentifier.fetchValue(holdLotReleaseInBankReqParams.getLotIDs().get(i))));
        }

        log.debug("step5 - lotInventoryStateGet");
        log.debug("get lot inventory state of lot {}",holdLotReleaseInBankReqParams.getLotIDs().get(i));
        String lotInventoryStateGet = lotMethod.lotInventoryStateGet(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i));
        log.trace("lot inventory state is {}",lotInventoryStateGet);
        if (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotInventoryStateGet)) {
            throw new ServiceException(retCodeConfig.getLotNotInBank());
        }
        /*-----------------------------------------------------------*/
        /*   Check PosCode                                           */
        /*-----------------------------------------------------------*/
        log.trace("reason code is {}",holdLotReleaseInBankReqParams.getReasonCodeID());
        if (ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLDRELEASE, holdLotReleaseInBankReqParams.getReasonCodeID())) {
            throw new ServiceException(retCodeConfig.getCannotHoldreleaseWithNpbr());
        }
        //--- check for reason code existence ---//
        List<ObjectIdentifier> list = new ArrayList<>();
        list.add(holdLotReleaseInBankReqParams.getReasonCodeID());
        log.debug("check the exist of {}",list);
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_BANKHOLDRELEASE, list);

        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        log.debug("get lot interfab xfer state of lot {}",holdLotReleaseInBankReqParams.getLotIDs().get(i));
        String lotInterFabXferStateGetResult = lotMethod.lotInterFabXferStateGet(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i));
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        log.trace("lot interfab xfer state is {}",lotInterFabXferStateGetResult);
        if (CimStringUtils.equals(lotInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
            throw new ServiceException(retCodeConfig.getInterfabInvalidLotXferstateForReq());

        }
        log.debug("step  -  Change State");
        holdLotReleaseInBankReqResult = lotMethod.lotBankHoldRelease(objCommon, holdLotReleaseInBankReqParams.getLotIDs().get(i), holdLotReleaseInBankReqParams.getReasonCodeID(), holdLotReleaseInBankReqParams.getClaimMemo());

        log.debug("step  - Make History");
        Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
        lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_RELEASE_BANK_LOT_REQ.getValue());
        lotHoldEventMakeParams.setLotID(holdLotReleaseInBankReqParams.getLotIDs().get(i));
        lotHoldEventMakeParams.setHoldHistoryList(holdLotReleaseInBankReqResult);
        eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);

        log.debug("// step  - Return");
        return holdLotReleaseInBankReqResult;
    }

    public List<Infos.ReturnCodeInfo> sxBankMoveReq(Infos.ObjCommon objCommon, Params.BankMoveReqParams bankMoveReqParams) { //nyx-modify
        List<ObjectIdentifier> lotIDs = bankMoveReqParams.getLotIDs();
        ObjectIdentifier toBankID = bankMoveReqParams.getBankID();
        List<Infos.ReturnCodeInfo> returnCodeInfos = new ArrayList<>();
        for (ObjectIdentifier lotID : lotIDs) {
            log.debug("get lot inventory state of lot {}",lotID);
            String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
            Infos.ReturnCodeInfo returnCodeInfo = new Infos.ReturnCodeInfo();
            log.trace("lot inventory state is {}",lotInventoryState);
            if (CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                log.debug("get lot hold state of lot {}",lotID);
                String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
                Validations.check(CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD),
                        new OmCode(retCodeConfig.getInvalidLotHoldStat(), ObjectIdentifier.fetchValue(lotID), lotHoldState));

                log.debug("get lot state of lot {}",lotID);
                String lotState = lotMethod.lotStateGet(objCommon, lotID);
                Validations.check(CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_SHIPPED), new OmCode(retCodeConfig.getInvalidLotStat(), lotState));

                log.debug("get lot finished state of lot {}",lotID);
                String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
                if (CimStringUtils.equals(lotFinishedState, CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED)
                        || CimStringUtils.equals(lotFinishedState, CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinishedState));
                }
            } else if (CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK)) {
                log.debug("check that bank {} prod bank is true",toBankID);
                bankMethod.bankCheckNonProBank(objCommon, toBankID);
            } else {
                Validations.check(retCodeConfig.getInvalidLotInventoryStat(),ObjectIdentifier.fetchValue(lotID),lotInventoryState);
            }

            log.debug("step2 - done:Check lot interFabXferState (lotInterFabXferStateGet)");
            String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
            log.trace("lot interfab xfer state is {}",lotInterFabXferState);
            if (CimStringUtils.equals(lotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                returnCodeInfo.setReturnCode(new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), ObjectIdentifier.fetchValue(lotID), lotInterFabXferState));
                returnCodeInfos.add(returnCodeInfo);
                continue;
            }

            log.debug("step3 - move bank (lotBankMove)");
            lotMethod.lotBankMove(objCommon, lotID, toBankID);

            log.debug("step4 - Make History");
            Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
            lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.BANK_MOVE_REQ.getValue());
            lotBankMoveEventMakeParams.setClaimMemo(bankMoveReqParams.getClaimMemo());
            lotBankMoveEventMakeParams.setLotID(lotID);
            log.debug("add event");
            eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
        }

        Validations.check(!CimObjectUtils.isEmpty(returnCodeInfos), retCodeConfig.getSomeRequestsFailed());
        return returnCodeInfos;
    }


    public Results.HoldLotInBankReqResult sxHoldLotInBankReqReq(Infos.ObjCommon objCommon, Integer i, Params.HoldLotInBankReqParams holdLotInBankReqParams) {

        Validations.check(!CimArrayUtils.isEmpty(holdLotInBankReqParams.getLotIDs()) && i >= CimArrayUtils.getSize(holdLotInBankReqParams.getLotIDs()), retCodeConfig.getInvalidParameter());
        log.debug("get state of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        String lotState = lotMethod.lotStateGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));
        log.debug("get finished state of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));
        log.debug("get hold state of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));
        log.debug("get inventory state of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));
        log.debug("get route id of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        ObjectIdentifier routeID = lotMethod.lotRouteIdGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));

        log.debug("step1 -  Check Condition");
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotState), retCodeConfig.getInvalidLotStat(), lotState);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED, lotFinishedState), retCodeConfig.getInvalidLotFinishStat(), lotFinishedState);
        Validations.check(CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState), retCodeConfig.getLotAlreadyHold(), holdLotInBankReqParams.getLotIDs().get(i));
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotInventoryState), retCodeConfig.getLotNotInBank(), holdLotInBankReqParams.getLotIDs().get(i));
        Validations.check(ObjectIdentifier.isEmptyWithValue(routeID), retCodeConfig.getNotFoundMainRoute());

        log.debug("step2 -  Check CimCode");
        Validations.check(ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_NONPROBANKHOLD, holdLotInBankReqParams.getReasonCodeID()), retCodeConfig.getCannotHoldWithNpbh());
        //--- check for reason code existence ---//
        List<ObjectIdentifier> list = new ArrayList<>();
        list.add(holdLotInBankReqParams.getReasonCodeID());
        log.debug("check code is exist");
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_BANKHOLD, list);

        log.debug("step 3 - Check lot interFabXferState");
        String lotInterFabXferStateGetResult = lotMethod.lotInterFabXferStateGet(objCommon, holdLotInBankReqParams.getLotIDs().get(i));

        log.debug("step 4 - Transferring");
        log.trace("lot {} interfab xfer state is {}",holdLotInBankReqParams.getLotIDs().get(i),lotInterFabXferStateGetResult);
        if (CimStringUtils.equals(lotInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), holdLotInBankReqParams.getLotIDs().get(i).getValue()));
        }
        log.debug("step 5 - Change State");
        log.debug("get bank hold of lot {}",holdLotInBankReqParams.getLotIDs().get(i));
        Results.LotBankHoldResult lotBankHoldResult = lotMethod.lotBankHold(objCommon, holdLotInBankReqParams.getLotIDs().get(i), holdLotInBankReqParams.getReasonCodeID(), holdLotInBankReqParams.getClaimMemo());
        log.debug("step 6 - Make History");
        Inputs.LotHoldEventMakeParams lotHoldEventMakeParams = new Inputs.LotHoldEventMakeParams();
        lotHoldEventMakeParams.setLotID(holdLotInBankReqParams.getLotIDs().get(i));
        lotHoldEventMakeParams.setTransactionID(TransactionIDEnum.HOLD_BANK_LOT_REQ.getValue());
        lotHoldEventMakeParams.setHoldHistoryList(lotBankHoldResult.getHoldHistoryList());
        log.debug("add event");
        eventMethod.lotHoldEventMake(objCommon, lotHoldEventMakeParams);

        Results.HoldLotInBankReqResult holdLotInBankReqResult = new Results.HoldLotInBankReqResult();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(holdLotInBankReqParams.getLotIDs().get(i));
        holdLotInBankReqResult.setLotList(lotIDs);
        log.debug("step 7 - return");
        return holdLotInBankReqResult;
    }

    @Override
    public void sxBankInCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo) {

        log.debug("【step0】Get lot / cassette connection ");
        ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);

        log.debug("【step1】Lock objects to be updated");
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        String lotStateGet = lotMethod.lotStateGet(objCommon, lotID);
        log.debug("【step2】check lot state {}",lotStateGet);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, lotStateGet), retCodeConfig.getInvalidLotStat(), lotStateGet);

        String lotHoldStateGet = lotMethod.lotHoldStateGet(objCommon, lotID);
        log.debug("【step3】check lot hold state {}",lotHoldStateGet);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD, lotHoldStateGet), retCodeConfig.getInvalidLotHoldStat(), lotID, lotHoldStateGet);

        String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
        log.debug("【step4】check lot process state {}",lotProcessStateGet);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSED, lotProcessStateGet), retCodeConfig.getInvalidLotProcessState(), lotID, lotProcessStateGet);

        String lotInventoryStateGet = lotMethod.lotInventoryStateGet(objCommon, lotID);
        log.debug("【step5】check lot inventory state {}",lotInventoryStateGet);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, lotInventoryStateGet),
                retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(lotID), lotInventoryStateGet);

        String lotFinishedStateGet = lotMethod.lotFinishedStateGet(objCommon, lotID);
        log.debug("【step6】check lot finished state {}",lotFinishedStateGet);
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED, lotFinishedStateGet),
                retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(lotID), lotFinishedStateGet);

        log.debug("【step7】Check lot Condition");
        lotMethod.lotCheckBankInCancel(objCommon, lotID);

        log.debug("【step8】Check lot interFabXferState");
        String lotInterFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

        log.debug("【step9】check if the lot is npw lot and reach the usage limit");
        waferMethod.bankInCancelCheckByUsageCount(objCommon, lotID);

        log.trace("lotInterFabXferStateGet : {}",lotInterFabXferStateGet);
        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferStateGet),
                retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID, lotInterFabXferStateGet);
        log.debug("【step10】Change State");
        lotMethod.lotBankInCancel(objCommon, lotID);

        log.debug("【step11】Update cassette's MultiLotType");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

        log.debug("【step12】Make History");
        Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
        lotBankMoveEventMakeParams.setLotID(lotID);
        lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.BANK_IN_CANCEL_REQ.getValue());
        lotBankMoveEventMakeParams.setClaimMemo(claimMemo);
        eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
    }

    @Override
    public List<Infos.BankInLotResult> sxBankInReq(Infos.ObjCommon objCommon, int seqIndex, List<ObjectIdentifier> lotIDs, String claimMemo) {
        log.info("lotIDs = {}", lotIDs);
        Validations.check(CimObjectUtils.isEmpty(lotIDs) || seqIndex > lotIDs.size() || seqIndex < 0, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier lotID = lotIDs.get(seqIndex);

        List<Infos.BankInLotResult> bankInLotResults = new ArrayList<>();
        log.debug("【step0】Get lot / cassette connection");
        ObjectIdentifier cassetteID = null;
        try {
            cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }
        boolean bParallelPostProcFlag = false;
        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        if (CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
            bParallelPostProcFlag = true;
        }
        log.debug("【step1】Lock objects to be updated");
        log.debug("Skip cassette lock to increase parallel availability  under PostProcess parallel execution ");
        if (!bParallelPostProcFlag) {
            objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        }
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        if (bParallelPostProcFlag) {
            log.debug("Write Lock MonitorGroup to keep data consistency ");
            List<Infos.MonitorGroups> monitorGroups = monitorGroupMethod.monitorGroupGetDR(objCommon, lotID);
            int monGrpLen = CimArrayUtils.getSize(monitorGroups);
            for (int i = 0; i < monGrpLen; i++) {
                objectLockMethod.objectLock(objCommon, CimMonitorGroup.class, monitorGroups.get(i).getMonitorGroupID());
            }
        }
        try {
            lotMethod.lotRemoveFromMonitorGroup(objCommon, lotID);
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        log.debug("【step2】 Check Condition");
        try {
            String lotStateGet = lotMethod.lotStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(lotStateGet, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(retCodeConfig.getInvalidLotStat(), lotStateGet)));
                return bankInLotResults;
            }
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        try {
            String lotStateGet = lotMethod.lotHoldStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(lotStateGet, CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(retCodeConfig.getInvalidLotHoldStat(), lotID.getValue(), lotStateGet)));
                return bankInLotResults;
            }
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        try {
            String lotStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(lotStateGet, BizConstant.SP_LOT_PROCSTATE_WAITING)) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(retCodeConfig.getInvalidLotProcessState(), lotID.getValue(), lotStateGet)));
                return bankInLotResults;
            }
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        try {
            String lotStateGet = lotMethod.lotInventoryStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(lotStateGet, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR)) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(retCodeConfig.getInvalidLotInventoryStat(), lotID.getValue(), lotStateGet)));
                return bankInLotResults;
            }
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        try {
            processMethod.processCheckBankIn(objCommon, lotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getBankinOperation(), e.getCode())) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
                return bankInLotResults;
            }
        }

        log.debug("【step3】Check lot interFabXferState");
        try {
            String lotStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
            if (CimStringUtils.equals(lotStateGet, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID.getValue(), lotStateGet)));
                return bankInLotResults;
            }
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        log.debug("【step4】Change State ");
        try {
            lotMethod.lotBankIn(objCommon, lotID);
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        log.debug("【step5】Update cassette's MultiLotType ");
        try {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        log.debug("【step6】Make History ");
        Inputs.LotBankMoveEventMakeParams lotBankMoveEventMakeParams = new Inputs.LotBankMoveEventMakeParams();
        lotBankMoveEventMakeParams.setClaimMemo(claimMemo);
        lotBankMoveEventMakeParams.setTransactionID(TransactionIDEnum.BANK_IN_REQ.getValue());
        lotBankMoveEventMakeParams.setLotID(lotID);
        try {
            eventMethod.lotBankMoveEventMake(objCommon, lotBankMoveEventMakeParams);
        } catch (ServiceException e) {
            bankInLotResults.add(new Infos.BankInLotResult(lotID, new OmCode(e.getCode(), e.getMessage())));
            return bankInLotResults;
        }

        return bankInLotResults;
    }

    @Override
    public boolean sxBankInByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo) {
        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidInputParam());

        /*---------------------------------------------------------------*/
        /*   Auto-Bank-In Procedure                                      */
        /*---------------------------------------------------------------*/
        String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);

        if (!CimStringUtils.equals(lotHoldState, BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {

            // check condition
            boolean checkFlag = lotMethod.lotCheckConditionForAutoBankIn(objCommon, lotID);

            if (checkFlag) {
                /*-----------------------------------------*/
                /*   Call txBankInReq() for Auto-Bank-In   */
                /*-----------------------------------------*/
                sxBankInReq(objCommon, 0, Collections.singletonList(lotID), claimMemo);
                return true;
            }
        }

        return false;
    }
}
