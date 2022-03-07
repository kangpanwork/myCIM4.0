package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.bank.CimBankDO;
import com.fa.cim.entity.runtime.bank.CimBankDestBankDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.IBankMethod;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.ILotFamilyMethod;
import com.fa.cim.method.IWaferMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.planning.PlanDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.prdctspc.ProductSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 10:31
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class BankMethod  implements IBankMethod {

    @Autowired
    private ProductManager productManager;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    @Qualifier("ProductManagerCore")
    private ProductManager productManagerNew;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public Results.BankListInqResult bankGetByProductionBankDR(Infos.ObjCommon objCommon, Params.BankListInqParams bankListInqParams) {
            //【step1】prepare the input params
        log.debug("【step1】check the input params");
        String inqBank = bankListInqParams.getInqBank();
        SearchCondition searchCondition = bankListInqParams.getSearchCondition();
        String bankID = "%";
        if (ObjectIdentifier.isNotEmptyWithValue(bankListInqParams.getBankID())) {
            bankID = bankListInqParams.getBankID().getValue();
        }

        String sql = "  SELECT    BANK_ID, \n" +
                "                      ID, \n" +
                "                      DESCRIPTION, \n" +
                "                      RCV_FLAG, \n" +
                "                      SHIP_FLAG, \n" +
                "                      LOT_START_FLAG, \n" +
                "                      BANK_IN_FLAG, \n" +
                "                      PROD_TYPE, \n" +
                "                      ASSIGN_WAFER_ID_FLAG, \n" +
                "                      PROD_BANK__FLAG, \n" +
                "                      RECYCLE_BANK_FLAG, \n" +
                "                      NPW_BANK_FLAG\n" +
                "                      FROM OMBANK   ";
        Page<CimBankDO> bankInfo = null;
        //【step2】filter by inquire bank type
        log.debug("【step2】filter by inquire bank type");
        switch (inqBank) {
            case "B":
                bankInfo = cimJpaRepository.query(sql+" WHERE BANK_ID LIKE ?1 ", CimBankDO.class, searchCondition,  bankID);
                break;
            case "N":
                bankInfo = cimJpaRepository.query(sql+" WHERE OMBANK.PROD_BANK__FLAG = 0 AND BANK_ID LIKE ?1 ", CimBankDO.class, searchCondition,  bankID);
                break;
            case "P":
                bankInfo = cimJpaRepository.query(sql+" WHERE OMBANK.PROD_BANK__FLAG <> 0 AND BANK_ID LIKE ?1 ", CimBankDO.class,  searchCondition, bankID);
                break;
            default:
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }


        //【step3】filter by function category - myCIM4.0 Request
        log.debug("【step3】filter by function category");
        TransactionIDEnum transactionIDEnum = TransactionIDEnum.get(bankListInqParams.getUser().getFunctionID());
       /* if (null != transactionIDEnum) {
            switch (transactionIDEnum) {
                case VEND_LOT_RECEIVE_REQ:
                case VEND_LOT_RETURN_REQ:
                    bank.setReceiveBank(true);
                    //bank.setRecycleBank(false);
                    break;
                case SHIP_REQ:
                case SHIP_CANCEL_REQ:
                    bank.setShipBank(true);
                    //bank.setRecycleBank(false);
                    break;
                case STB_RELEASED_LOT_REQ:
                case STB_CANCEL_INFO_INQ:
                    bank.setShipBank(true);
                    //bank.setRecycleBank(false);
                    break;
                case BANK_MOVE_REQ:
                case HOLD_BANK_LOT_REQ:
                case HOLD_RELEASE_BANK_LOT_REQ:
                    // have no restrictions
                default:
            }
        }*/
        List<Infos.BankAttributes> bankAttributesList = new ArrayList<>();
        if (null != bankInfo && CimArrayUtils.isNotEmpty(bankInfo.getContent())) {
            for (CimBankDO bankDO: bankInfo.getContent()) {
                Infos.BankAttributes bankAttributes = new Infos.BankAttributes();
                bankAttributes.setBankID(new ObjectIdentifier(bankDO.getBankID(), bankDO.getId()));
                bankAttributes.setBankInBankFlag(CimBooleanUtils.isTrue(bankDO.getBankInBank()));
                bankAttributes.setBankName(bankDO.getDescription());
                bankAttributes.setControlWaferBankFlag(CimBooleanUtils.isTrue(bankDO.getControlWaferBank()));
                bankAttributes.setProductBankFlag(CimBooleanUtils.isTrue(bankDO.getProductBank()));
                bankAttributes.setProductType(bankDO.getProductType());
                bankAttributes.setReceiveBankFlag(CimBooleanUtils.isTrue(bankDO.getReceiveBank()));
                bankAttributes.setShipBankFlag(CimBooleanUtils.isTrue(bankDO.getShipBank()));
                bankAttributes.setRecyclenBankFlag(CimBooleanUtils.isTrue(bankDO.getRecycleBank()));
                bankAttributes.setStbBankFlag(CimBooleanUtils.isTrue(bankDO.getStbBank()));
                bankAttributes.setWaferIdGenerateBankFlag(CimBooleanUtils.isTrue(bankDO.getWaferIDAssignRequest()));
                String mysql = "select * from OMBANK_DESTBANK where REFKEY = ?1";
                List<CimBankDestBankDO> query = cimJpaRepository.query(mysql, CimBankDestBankDO.class, bankDO.getId());
                List<ObjectIdentifier> nextBankID = new ArrayList<>();
                if (CimArrayUtils.isNotEmpty(query)) {
                    for(CimBankDestBankDO bankDestBankDO: query) {
                        nextBankID.add(new ObjectIdentifier(bankDestBankDO.getDestBankID(), bankDestBankDO.getDestBankObj()));
                    }
                }
                bankAttributes.setNextBankID(nextBankID);
                bankAttributesList.add(bankAttributes);
            }
        }

        //【step4】get banks which match the condition
        log.debug("【step4】get banks which match the condition");
        Results.BankListInqResult bankListInqResult = new Results.BankListInqResult();
        bankListInqResult.setBankAttributes(CimPageUtils.convertListToPage(bankAttributesList, searchCondition.getPage(), searchCondition.getSize()));
        return bankListInqResult;
    }

    @Override
    public void bankLotSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes) {
        // Get Object reference of Product Request
        CimProductRequest aProdReq = baseCoreFactory.getBO(CimProductRequest.class, productRequestID);
        Validations.check(aProdReq == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), productRequestID.getValue()));
        // Get Object reference of bank
        com.fa.cim.newcore.bo.factory.CimBank aStartBank = aProdReq.getStartBank();
        Validations.check(aStartBank == null, new OmCode(retCodeConfig.getNotFoundBank(), ""));
        String strStartBankID = aStartBank.getIdentifier();
        // Check bank is STB Allowed or not
        Boolean bSTBBankFlag = aStartBank.isSTBBank();
        Validations.check(!bSTBBankFlag, new OmCode(retCodeConfig.getInvalidBankStb(), strStartBankID));
        // Check Reqest type for generating lot and bank attribute combination
        String strLotType = aProdReq.getLotType();
        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, strLotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_ENGINEERINGLOT, strLotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, strLotType)) {
            boolean bPrdBank = aStartBank.isProductionBank();
            Validations.check(!bPrdBank, new OmCode(retCodeConfig.getInvalidBankData(), productRequestID.getValue(), strStartBankID));
        } else if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, strLotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, strLotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_RECYCLELOT, strLotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, strLotType)) {
            boolean bCtrlBank = aStartBank.isControlWaferBank();
            Validations.check(!bCtrlBank, new OmCode(retCodeConfig.getInvalidBankData(), productRequestID.getValue(), strStartBankID));
        } else {
            throw new ServiceException(retCodeConfig.getNotFoundLotType());
        }
        // Retrieve lot product's source product sequence
        ProductSpecification aProdSpec = aProdReq.getProductSpecification();
        CimProductSpecification aPosProdSpec = (CimProductSpecification) aProdSpec;
        Validations.check(aPosProdSpec == null, retCodeConfig.getNotFoundProductSpec());
        List<CimProductSpecification> pSourceProdList = aPosProdSpec.allSourceProductSpecifications();
        List<String> sourceProdList = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(pSourceProdList)){
            for (CimProductSpecification cimProductSpecification : pSourceProdList){
                sourceProdList.add(cimProductSpecification.getIdentifier());
            }
        }
        if (StandardProperties.OM_CARRIER_CHK_FOR_LOT_START.isTrue()) {
            // Check carrier has controlJob or not
            CimCassette aPosCassette = baseCoreFactory.getBO(CimCassette.class, newLotAttributes.getCassetteID());
            Validations.check(aPosCassette == null, retCodeConfig.getNotFoundCassette());
            com.fa.cim.newcore.bo.product.CimControlJob aPosControlJob = aPosCassette.getControlJob();
            if (aPosControlJob != null){
                ObjectIdentifier ctrlJobID = new ObjectIdentifier(aPosControlJob.getIdentifier(), aPosControlJob.getPrimaryKey());
                throw new ServiceException(new OmCode(retCodeConfig.getStbCassetteHasControlJob(), newLotAttributes.getCassetteID().getValue(), ctrlJobID.getValue()));
            }
            //------------------------------------------
            // Check carrier is on equipment or not
            //------------------------------------------
            String strCassetteXferState = aPosCassette.getTransportState();
            Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, strCassetteXferState),
                    new OmCode(retCodeConfig.getInvalidCassetteTransferState(),strCassetteXferState,newLotAttributes.getCassetteID().getValue()));
        }
        // Check source lot condition
        List<Infos.NewWaferAttributes> newWaferAttributesList = newLotAttributes.getNewWaferAttributesList();
        int lenNewLotAttr = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < lenNewLotAttr; i++) {
            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);
            if (i > 0) {
                if (ObjectIdentifier.equalsWithValue(newWaferAttributes.getSourceLotID(), newWaferAttributesList.get(i - 1).getSourceLotID())) {
                    continue;
                }
            }
            com.fa.cim.newcore.bo.product.CimLot aSourceLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, newWaferAttributesList.get(i).getSourceLotID());
            Validations.check(aSourceLot == null, retCodeConfig.getNotFoundLot());
            // Check source lot Finished Status
            String strLotFinishedState = aSourceLot.getLotFinishedState();
            if (!CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_COMPLETED, strLotFinishedState)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotFinishStat(), strLotFinishedState));
            }
            // Check source lot Status
            String strLotState = aSourceLot.getLotState();
            if (!CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, strLotState)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotStat(), strLotState));
            }
            // Check source lot Hold Status
            String strLotHoldState = aSourceLot.getLotHoldState();
            if (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD, strLotHoldState)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotHoldStat(), newWaferAttributesList.get(i).getSourceLotID().getValue()));
            }
            // Check source lot Inventory Status
            String strLotInventoryState = aSourceLot.getLotInventoryState();
            if (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, strLotInventoryState)) {
                Validations.check(retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(newWaferAttributesList.get(i).getSourceLotID()),
                        strLotInventoryState);
            }
            // Check source lot control use state
            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, strLotType)
                    || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, strLotType)
                    || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_RECYCLELOT, strLotType)
                    || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, strLotType)) {
                String strLotControlUseState = aSourceLot.getControlUseState();
                if (!CimStringUtils.equals(BizConstant.SP_LOT_CONTROLUSESTATE_WAITUSE, strLotControlUseState)
                        && !CimStringUtils.equals(BizConstant.SP_LOT_CONTROLUSESTATE_WAITRECYCLE, strLotControlUseState)) {
                    String tmpStr = String.format("%s or %s", BizConstant.SP_LOT_CONTROLUSESTATE_WAITUSE, BizConstant.SP_LOT_CONTROLUSESTATE_WAITRECYCLE);
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotControlUseStat(), newWaferAttributesList.get(i).getSourceLotID().getValue(), tmpStr));
                }
            }
            // Check source lot bank
            com.fa.cim.newcore.bo.factory.CimBank aSourceLotBank = aSourceLot.getBank();
            Validations.check(aSourceLotBank == null, new OmCode(retCodeConfig.getNotFoundBank(), ""));
            String strSourceLotBankID = aSourceLotBank.getIdentifier();
            if (!CimStringUtils.equals(strSourceLotBankID, strStartBankID)) {
                throw new ServiceException(new OmCode(retCodeConfig.getNotMatchStartBank(), newWaferAttributesList.get(i).getSourceLotID().getValue(), productRequestID.getValue()));
            }
            // Check source lot Product Spec
            CimProductSpecification aSourceLotProdSpec = aSourceLot.getProductSpecification();
            Validations.check(aSourceLotProdSpec == null, retCodeConfig.getNotFoundProductSpec());
            String strSourceLotProduct = aSourceLotProdSpec.getIdentifier();
            boolean bSourceProductMatchFlag  = false;
            if (!CimObjectUtils.isEmpty(sourceProdList)){
                for (String sourceProd : sourceProdList){
                    if (CimStringUtils.equals(sourceProd, strSourceLotProduct)) {
                        bSourceProductMatchFlag = true;
                        break;
                    }
                }
            }
            Validations.check(!bSourceProductMatchFlag, new OmCode(retCodeConfig.getInvalidSourceLotProduct(), newWaferAttributesList.get(i).getSourceLotID().getValue(), strSourceLotProduct));
        }

        //------------------------------------------
        // 当ActionCode=waferStart时创建sortJob跳过此验证
        //------------------------------------------
        if(!TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue().equals(objCommon.getTransactionID())){
            //------------------------------------------
            // Retrieve product request's source lot
            //------------------------------------------
            List<PlanDTO.SourceLotEx> aSourceLots = aProdReq.allSourceLots();
            //------------------------------------------
            // Check source lot Wafer Contents
            //------------------------------------------
            int lenSrcLot = CimArrayUtils.getSize(aSourceLots);
            lenNewLotAttr = CimArrayUtils.getSize(newLotAttributes.getNewWaferAttributesList());
            if (lenSrcLot > 0) {
                for (int i = 0; i < lenSrcLot; i++) {
                    int lenSrcLotPrd = CimArrayUtils.getSize(aSourceLots.get(i).getWaferIDList());
                    for (int j = 0; j < lenSrcLotPrd; j++) {
                        boolean foundFlag = false;
                        for (int k = 0; k < lenNewLotAttr; k++) {
                            if (ObjectIdentifier.equalsWithValue(aSourceLots.get(i).getWaferIDList().get(j), newLotAttributes.getNewWaferAttributesList().get(k).getSourceWaferID())) {
                                foundFlag = true;
                                break;
                            }
                        }
                        Validations.check(!foundFlag, new OmCode(retCodeConfig.getNotFoundSourceWafer(), aSourceLots.get(i).getWaferIDList().get(j).getValue()));
                    }
                }
            } else {
                for (int i = 0; i < lenNewLotAttr; i++) {
                    com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, newLotAttributes.getNewWaferAttributesList().get(i).getSourceWaferID());
                    boolean bWaferFoundFlag = aPosWafer.isSTBAllocated();
                    String strWaferID = aPosWafer.getIdentifier();
                    Validations.check(bWaferFoundFlag, new OmCode(retCodeConfig.getWaferAllocated(), strWaferID));
                    // Check wafer is in target cassette or not
                    MaterialContainer aMaterialContainer = aPosWafer.getMaterialContainer();
                    String tmpMaterialContainer = null;
                    if (aMaterialContainer != null){
                        tmpMaterialContainer = aMaterialContainer.getIdentifier();
                    }
                    Validations.check(!ObjectIdentifier.equalsWithValue(newLotAttributes.getCassetteID(), tmpMaterialContainer), retCodeConfig.getWaferNotPrepared());
                }
            }
        }


    }

    @Override
    public void bankLotPreparationCheck(Infos.ObjCommon objCommon, ObjectIdentifier bankID, String lotType, String subLotType, Infos.NewLotAttributes newLotAttributes) {
        com.fa.cim.newcore.bo.factory.CimBank aBank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(aBank == null, new OmCode(retCodeConfig.getNotFoundBank(), bankID.getValue()));
        //【step1】check request type for generating lot
        log.info("【step1】check request type for generating lot");
        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_ENGINEERINGLOT, lotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, lotType)) {
            boolean bPrdBank = aBank.isProductionBank();
            Validations.check(!bPrdBank, new OmCode(retCodeConfig.getInvalidBankData(),"",bankID.getValue()));
        } else if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotType)
                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_RECYCLELOT, lotType)) {
            boolean bCtrlWaferBank = aBank.isControlWaferBank();
            Validations.check(!bCtrlWaferBank, new OmCode(retCodeConfig.getInvalidBankData(), "", bankID.getValue()));
        } else {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        /**************************************************************************************************************/
        log.info("【step2】check carrier has controlJob or not");
        com.fa.cim.newcore.bo.durable.CimCassette aPosCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, newLotAttributes.getCassetteID());
        Validations.check(aPosCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), newLotAttributes.getCassetteID().getValue()));
        com.fa.cim.newcore.bo.product.CimControlJob aPosControlJob = aPosCassette.getControlJob();
        if (aPosControlJob != null){
            ObjectIdentifier ctrlJobID = new ObjectIdentifier(aPosControlJob.getIdentifier(), aPosCassette.getPrimaryKey());
            throw new ServiceException(new OmCode(retCodeConfig.getStbCassetteHasControlJob(), newLotAttributes.getCassetteID().getValue(), ctrlJobID.getValue()));
        }
        //【step3】check source wafer loaded equipments
        log.info("【step3】check source wafer loaded equipments");
        String tmpCarrierID = aPosCassette.getIdentifier();
        Machine aMachine = aPosCassette.currentAssignedMachine();
        CimMachine aPosMachine = null;
        if (aMachine != null){
            boolean isStorageBool = aMachine.isStorageMachine();
            if (!isStorageBool){
                aPosMachine = (CimMachine)aMachine;
            }
        }
        if (aPosMachine != null){
            //--------------------------------------------------
            // Get Equipment attributes
            //--------------------------------------------------
            String strMachineCategory = aPosMachine.getCategory();
            //--------------------------------------------------
            // Check Equipment Type
            //--------------------------------------------------
            if (!CimStringUtils.equals(strMachineCategory, BizConstant.SP_MC_CATEGORY_WAFERSORTER)){
                //------------------------------------------
                // Check carrier is on equipment or not
                //------------------------------------------
                String strCassetteXferState = aPosCassette.getTransportState();
                Validations.check(CimStringUtils.equals(strCassetteXferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN),
                        new OmCode(retCodeConfig.getInvalidCassetteTransferState(), strCassetteXferState, newLotAttributes.getCassetteID().getValue()));
            }
        }
        //【step4】check source lot condition
        log.info("【step4】check source lot condition");
        String strSourceLotProduct = null;
        List<Infos.NewWaferAttributes> newWaferAttributesList = newLotAttributes.getNewWaferAttributesList();
        int lenLotAttr = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < lenLotAttr; i++) {
            if (i > 0){
                if (ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getSourceLotID(), newWaferAttributesList.get(i - 1).getSourceLotID())){
                    continue;
                }
            }
            //get source lot
            log.info("get source lot...");
            com.fa.cim.newcore.bo.product.CimLot aPosLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, newWaferAttributesList.get(i).getSourceLotID());
            Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), newWaferAttributesList.get(i).getSourceLotID().getValue()));
            String strlotID = aPosLot.getIdentifier();
            //【step4-1】check source lot hold status
            log.info("【step4-1】check source lot hold status");
            String strLotHoldState = aPosLot.getLotHoldState();
            Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD, strLotHoldState),
                    new OmCode(retCodeConfig.getInvalidLotHoldStat(), strlotID, strLotHoldState));
            //【step4-2】check source lot inventory status
            log.info("【step4-2】check source lot inventory status...");
            String strLotInventoryState = aPosLot.getLotInventoryState();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, strLotInventoryState),
                    retCodeConfig.getInvalidLotInventoryStat(), strlotID, strLotInventoryState);
            //【step4-3】check source lot type
            log.info("【step4-3】check source lot type...");
            String strLotType = aPosLot.getLotType();
            Validations.check(!CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_VENDORLOT),
                    new OmCode(retCodeConfig.getInvalidLotType(), strLotType, strlotID));
            // 【step4-4】check source lot bank
            log.info("【step4-4】check source lot bank...");
            com.fa.cim.newcore.bo.factory.CimBank aPosBank = aPosLot.getBank();
            Validations.check(aPosBank == null, new OmCode(retCodeConfig.getNotFoundBank(), ""));
            String strLotBankID = aPosBank.getIdentifier();
            Validations.check(!ObjectIdentifier.equalsWithValue(strLotBankID, bankID), new OmCode(retCodeConfig.getLotBankDifferent(), strLotBankID, bankID.getValue()));
            //【step4-5】check source lot product spec
            log.info("【step4-5】check source lot product spec...");
            CimProductSpecification aPosProdSpec = aPosLot.getProductSpecification();
            Validations.check(aPosProdSpec == null, retCodeConfig.getNotFoundProductSpec());
            String tmpSourceLotProduct = aPosProdSpec.getIdentifier();
            if (CimStringUtils.isEmpty(strSourceLotProduct)){
                strSourceLotProduct = tmpSourceLotProduct;
            } else {
                Validations.check(!CimStringUtils.equals(strSourceLotProduct, tmpSourceLotProduct),
                new OmCode(retCodeConfig.getInvalidSourceLotCombination()));
            }
        }
        //【step5】check source lot wafer contents
        log.info("【step5】check source lot wafer contents...");
        for (Infos.NewWaferAttributes newWaferAttributes : newLotAttributes.getNewWaferAttributesList()) {
            // check requested wafer existance and
            // STB Allocated Flag of each wafer
            com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, newWaferAttributes.getSourceWaferID());
            Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
            //【step5-1】check wafer is in target cassette or not
            log.info("【step5-1】check wafer is in target cassette or not");
            boolean bWaferFoundFlag = aPosWafer.isSTBAllocated();
            Validations.check(bWaferFoundFlag, new OmCode(retCodeConfig.getWaferAllocated(), aPosWafer.getIdentifier()));
            // Check wafer is in target cassette or not
            MaterialContainer aMaterialContainer = aPosWafer.getMaterialContainer();
            String tmpMaterialContainer = null;
            if (aMaterialContainer != null){
                tmpMaterialContainer = aMaterialContainer.getIdentifier();
            }
            Validations.check(!ObjectIdentifier.equalsWithValue(tmpMaterialContainer, newLotAttributes.getCassetteID()), retCodeConfig.getWaferNotPrepared());
        }
    }

    @Override
    public ObjectIdentifier bankLotPreparation(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID
            , ObjectIdentifier bankID, Infos.NewLotAttributes newLotAttributes) {
        Validations.check(CimObjectUtils.isEmpty(newLotAttributes.getNewWaferAttributesList()), retCodeConfig.getInvalidInputParam());
        Outputs.ObjBankLotPreparationOut objBankLotPreparationOut = new Outputs.ObjBankLotPreparationOut();
        ObjectIdentifier sourceLotID = newLotAttributes.getNewWaferAttributesList().get(0).getSourceLotID();
        com.fa.cim.newcore.bo.product.CimLot aSourceLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, sourceLotID);
        Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), sourceLotID.getValue()));
        //【step1】get total count of vendor lot's wafer
        log.info("【step1】get total count of vendor lot's wafer");
        int productCount = newLotAttributes.getNewWaferAttributesList().size();
        com.fa.cim.newcore.bo.factory.CimBank aPosBank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(aPosBank == null, new OmCode(retCodeConfig.getNotFoundBank(), bankID.getValue()));
        // 【step2】get product request object
        log.info("【step2】get product request object");
        CimProductRequest prdReq = baseCoreFactory.getBO(CimProductRequest.class, productRequestID);
        Validations.check(prdReq == null, new OmCode(retCodeConfig.getNotFoundProductRequest(), productRequestID.getValue()));
        CimProductSpecification aProduct = (CimProductSpecification) prdReq.getProductSpecification();
        //【step3】create new lot
        log.info("【step3】create new lot");
        com.fa.cim.newcore.bo.product.CimLot newLot = productManager.createLotUsing(prdReq);
        Validations.check(newLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ""));
        //【step4】adjust split number if need.
        log.info("【step4】adjust split number if need.");
        com.fa.cim.newcore.bo.product.CimLotFamily aLotFamily = newLot.getLotFamily();
        Validations.check(aLotFamily == null, retCodeConfig.getNotFoundLotFamily());
        boolean notInheritedFlag = aLotFamily.isNewlyCreated();
        if (notInheritedFlag) {
            ObjectIdentifier lotFamilyID = new ObjectIdentifier(aLotFamily.getIdentifier(), aLotFamily.getPrimaryKey());
            // Eric-BUG-6382
            /*String duplicationAllowable = environmentVariableManager.getValue(EnvConst.OM_DUPLICATE_FAMILY_LOT_ENABLE);
            if ("0".equals(duplicationAllowable)) {
                log.info(String.format("the duplicationAllowable is '%s'", duplicationAllowable));
                //【step5】check whether lot family duplicated
                log.info("check whether lot family duplicated");
                lotFamilyMethod.lotFamilyDuplicationCheckDR(objCommon, lotFamilyID);
            }*/
            // Eric-BUG-6382
            //【step6】adjust lot family split number
            log.info("【step6】adjust lot family split number");
            lotFamilyMethod.lotFamilySplitNoAdjust(objCommon, lotFamilyID);
        }
        // prepare output structure
        ObjectIdentifier createLotID = new ObjectIdentifier(newLot.getIdentifier(), newLot.getPrimaryKey());
        objBankLotPreparationOut.setCreatedLotID(createLotID);
        //【step7】set lot info
        log.info("【step7】set lot info");
        String vendLotID = aSourceLot.getVendorLot();
        String vend = aSourceLot.getVendor();
        newLot.setAllStatesForVendorLot();
        newLot.setBank(aPosBank);
        newLot.refreshQuantity();
        newLot.setVendorLot(vendLotID);
        newLot.setVendor(vend);
        newLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        newLot.setLastClaimedPerson(aPerson);
        newLot.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        newLot.setStateChangedPerson(aPerson);
        newLot.setInventoryStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        newLot.setInventoryStateChangedPerson(aPerson);
        String lotType = newLot.getLotType();
        newLot.makeWaitUse();
        //set npw use limit for lot
        newLot.setRecycleLimit(aProduct.getRecycleLimit());
        newLot.setUsageLimit(aProduct.getUsageLimit());
        //【step8】wafer assigned lot change
        log.info("【step8】wafer assigned lot change");
        Infos.NewLotAttributes tempLotAttributes = newLotAttributes;
        for (int i = 0; i < productCount; i++) {
            tempLotAttributes.getNewWaferAttributesList().get(i).setNewLotID(objBankLotPreparationOut.getCreatedLotID());
        }
        waferMethod.waferAssignedLotChange(objCommon, tempLotAttributes.getNewWaferAttributesList());
        return createLotID;
    }

    @Override
    public void bankLotPreparationCheckForWaferSorter(Infos.ObjCommon objCommon, ObjectIdentifier bankID,
                                                                 String lotType, String subLotType, Infos.NewLotAttributes strNewLotAttributes) {
        log.info("【Method Entry】bankLotPreparationCheckForWaferSorter()");

        //【Step-1】Get Object reference of PosBank;
        log.info("Get Object reference of PosBank");
        com.fa.cim.newcore.bo.factory.CimBank bank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(CimObjectUtils.isEmpty(bank),retCodeConfig.getNotFoundBank());

        //【Step-2】Check Request type for generating lot;
        log.info("Check Request type for generating lot");

        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotType) ||
                CimStringUtils.equals(BizConstant.SP_LOT_TYPE_ENGINEERINGLOT, lotType) ||
                CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, lotType)) {
            log.info("lotType == LOT_TYPE_PRODUCTION_LOT or LOT_TYPE_ENGINEERING_LOT or LOT_TYPE_VENDOR_LOT");

            Boolean bPrdBank = bank.isProductionBank();
            Validations.check(CimBooleanUtils.isFalse(bPrdBank),retCodeConfig.getInvalidBankData(),"",bankID);
        } else if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotType) ||
                CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType) ||
                CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotType) ||
                CimStringUtils.equals(BizConstant.SP_LOT_TYPE_RECYCLELOT, lotType)) {
            log.info("lotType = LOT_TYPE_PRODUCTION_MONITOR_LOT or LOT_TYPE_EQUIPMENT_MONITOR_LOT or LOT_TYPE_DUMMY_LOT or LOT_TYPE_RECYCLE_LOT");

            Boolean bCtrlWaferBank = bank.isControlWaferBank();
            Validations.check(CimBooleanUtils.isFalse(bCtrlWaferBank),retCodeConfig.getInvalidBankData(),"",bankID);
        } else {
            log.info("Else!!");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //【Step-3】Check source lot condition;
        log.info("Check source lot condition");
        String strSourceLotProduct = BizConstant.EMPTY;
        int lenLotAttr = CimArrayUtils.getSize(strNewLotAttributes.getNewWaferAttributesList());
        for (int i = 0; i < lenLotAttr; i++) {
            Infos.NewWaferAttributes newWaferAttributes = strNewLotAttributes.getNewWaferAttributesList().get(i);
            if (i > 0) {
                log.info("Check source lot condition. Round {}", i);

                Infos.NewWaferAttributes preNewWaferAttributes = strNewLotAttributes.getNewWaferAttributesList().get(i - 1);
                if (CimStringUtils.equals(preNewWaferAttributes.getSourceLotID().getValue(), newWaferAttributes.getSourceLotID().getValue())) {
                    continue;
                }
            }
            com.fa.cim.newcore.bo.product.CimLot aPosLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, newWaferAttributes.getSourceLotID());
            Validations.check(CimObjectUtils.isEmpty(aPosLot),retCodeConfig.getNotFoundLot());

            //Check source lot Hold Status;
            log.info("Check source lot Hold Status");
            String lotHoldState = aPosLot.getLotHoldState();
            Validations.check(CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState),
                    retCodeConfig.getInvalidLotHoldStat(),aPosLot.getIdentifier(),lotHoldState);
            //Check source lot Inventory Status;
            log.info("Check source lot Inventory Status");
            String lotInventoryState = aPosLot.getLotInventoryState();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_INBANK, lotInventoryState),
                    retCodeConfig.getInvalidLotInventoryStat(),aPosLot.getIdentifier(),lotInventoryState);

            //Check source lot Type;
            log.info("Check source lot Type");
            String strLotType = aPosLot.getLotType();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, strLotType),
                    retCodeConfig.getInvalidLotType(),aPosLot.getIdentifier(),strLotType);

            //Check source lot bank
            log.info("Check source lot bank");
            com.fa.cim.newcore.bo.factory.CimBank lotBank = aPosLot.getBank();
            Validations.check(CimObjectUtils.isEmpty(lotBank),retCodeConfig.getNotFoundBank());

            String lotBankID = lotBank.getIdentifier();
            Validations.check(!CimStringUtils.equals(lotBankID, bankID.getValue()),
                    retCodeConfig.getLotBankDifferent(),lotBankID);
            //Check source lot Product Spec;
            log.info("Check source lot Product Spec");
            CimProductSpecification aProductSpec = aPosLot.getProductSpecification();
            Validations.check(CimObjectUtils.isEmpty(aProductSpec),retCodeConfig.getNotFoundProductSpec());

            String tmpSourceLotProduct = aProductSpec.getIdentifier();
            log.info("#### length of strSourceLotProduct = {} ....", strSourceLotProduct.length());

            if (CimObjectUtils.isEmpty(strSourceLotProduct)) {
                log.info("strSourceLotProduct is empty string.");
                strSourceLotProduct = tmpSourceLotProduct;
            } else {
                log.info("strSourceLotProduct is not empty string.");
                log.info("#### strSourceLotProduct = {} ...", strSourceLotProduct);
                log.info("#### tmpSourceLotProduct = {} ...", tmpSourceLotProduct);
                Validations.check(!CimStringUtils.equals(strSourceLotProduct, tmpSourceLotProduct),retCodeConfig.getInvalidSourceLotCombination());
            }
        }

        //------------------------------------------
        //【Step-4】Check source lot wafer Contents
        //------------------------------------------
        log.info("Check source lot wafer Contents : lenLotAttr = {}", lenLotAttr);
        for (int i = 0; i < lenLotAttr; i++) {
            // check requested wafer existence and
            // STB Allocated Flag of each wafer
            Infos.NewWaferAttributes newWaferAttributes = strNewLotAttributes.getNewWaferAttributesList().get(i);
            com.fa.cim.newcore.bo.product.CimWafer aPosWafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, newWaferAttributes.getSourceWaferID());
            Validations.check(CimObjectUtils.isEmpty(aPosWafer),retCodeConfig.getNotFoundWafer());
            Boolean bWaferFoundFlag = aPosWafer.isSTBAllocated();
            Validations.check(CimBooleanUtils.isTrue(bWaferFoundFlag),retCodeConfig.getWaferAllocated(),
                    aPosWafer.getIdentifier());
        }
        log.info("【Method Exit】bankLotPreparationCheckForWaferSorter()");
    }

    @Override
    public ObjectIdentifier bankLotPreparationForWaferSorter(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, ObjectIdentifier bankID, Infos.NewLotAttributes strNewLotAttributes) {

        log.info("【Method Entry】bankLotPreparationForWaferSorter()");
        int newWaferAttrsLen = CimArrayUtils.getSize(strNewLotAttributes.getNewWaferAttributesList());
        Validations.check(newWaferAttrsLen == 0,retCodeConfig.getInvalidInputParam());

        CimLot aSourceLot = baseCoreFactory.getBO(CimLot.class, strNewLotAttributes.getNewWaferAttributesList().get(0).getSourceLotID());
        Validations.check(CimObjectUtils.isEmpty(aSourceLot),retCodeConfig.getNotFoundLot());

        //【Step-1】Get total count of Vendor lot's wafer;
        log.info("Get total count of Vendor lot's wafer : {}", newWaferAttrsLen);

        com.fa.cim.newcore.bo.factory.CimBank aPosBank = baseCoreFactory.getBO(com.fa.cim.newcore.bo.factory.CimBank.class, bankID);
        Validations.check(CimObjectUtils.isEmpty(aPosBank),retCodeConfig.getNotFoundBank());

        //【Step-2】 Get Product Request object reference;
        CimProductRequest prdReq = baseCoreFactory.getBO(CimProductRequest.class, productRequestID);
        Validations.check(CimObjectUtils.isEmpty(prdReq),retCodeConfig.getNotFoundProductRequest());

        //【Step-3】 Create new lot;
        log.info("Create new lot");
        CimLot newLot = null;

        newLot = productManagerNew.createLotUsing(prdReq);
        Validations.check(CimObjectUtils.isEmpty(newLot),retCodeConfig.getNotFoundLot());

        //【Step-4】Adjust split number if need;
        CimLotFamily lotFamily = newLot.getLotFamily();
        Validations.check(CimObjectUtils.isEmpty(lotFamily),retCodeConfig.getNotFoundLotFamily());

        Boolean notInheritedFlag = lotFamily.isNewlyCreated();
        log.info("notInheritedFlag = {}", notInheritedFlag);
        if (CimBooleanUtils.isTrue(notInheritedFlag)) {
            ObjectIdentifier lotFamilyID = new ObjectIdentifier("", "");
            if (lotFamily != null) {
                lotFamilyID.setReferenceKey(lotFamily.getIdentifier());
                lotFamilyID.setValue(lotFamily.getPrimaryKey());
            }

            // Eric-BUG-6382
            /*String duplicationAllowableFlag = environmentVariableManager.getValue(EnvConst.OM_DUPLICATE_FAMILY_LOT_ENABLE);
            if (ObjectUtils.equalsWithValue("0", duplicationAllowableFlag)) {
                lotFamilyMethod.lotFamilyDuplicationCheckDR(objCommon, lotFamilyID);
            }*/
            // Eric-BUG-6382

            Outputs.ObjLotFamilySplitNoAdjustOut objLotFamilySplitNoAdjustOut = lotFamilyMethod.lotFamilySplitNoAdjust(objCommon, lotFamilyID);

        }

        //------------------------
        //【Step-5】Prepare output structure
        //------------------------
        log.info("Prepare output structure.");
        ObjectIdentifier createdLotID = new ObjectIdentifier("", "");
        if (newLot != null) {
            createdLotID.setValue(newLot.getIdentifier());
            createdLotID.setReferenceKey(newLot.getPrimaryKey());
        }

        //【Step-6】retrieve vendor lot information
        log.info("retrieve vendor lot information");
        String vendLotID = aSourceLot.getVendorLot();
        String vend = aSourceLot.getVendor();

        // 【Step-7】Set lot info
        log.info("Set lot info");
        newLot.setAllStatesForVendorLot();
        newLot.setBank(aPosBank);
        newLot.refreshQuantity();
        newLot.setVendorLot(vendLotID);
        newLot.setVendor(vend);
        newLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        com.fa.cim.newcore.bo.person.CimPerson aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(aPerson),retCodeConfig.getNotFoundPerson());

        newLot.setLastClaimedPerson(aPerson);
        newLot.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        newLot.setStateChangedPerson(aPerson);
        newLot.setInventoryStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        newLot.setInventoryStateChangedPerson(aPerson);

        //set npw use limit for lot
        CimProductSpecification aProduct = (CimProductSpecification) prdReq.getProductSpecification();
        newLot.setRecycleLimit(aProduct.getRecycleLimit());
        newLot.setUsageLimit(aProduct.getUsageLimit());

        String lotType = newLot.getLotType();
        newLot.makeWaitUse();

        //【Step-8】 wafer assigned lot change
        log.info("wafer assigned lot change");
        Infos.NewLotAttributes tmpNewLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        tmpNewLotAttributes.setCassetteID(strNewLotAttributes.getCassetteID());
        for (int i = 0; i < newWaferAttrsLen; i++) {
            Infos.NewWaferAttributes referNewWaferAttrs = strNewLotAttributes.getNewWaferAttributesList().get(i);

            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber(referNewWaferAttrs.getNewSlotNumber());
            newWaferAttributes.setNewLotID(createdLotID);
            newWaferAttributes.setNewWaferID(referNewWaferAttrs.getNewWaferID());
            newWaferAttributes.setSourceLotID(referNewWaferAttrs.getSourceLotID());
            newWaferAttributes.setSourceWaferID(referNewWaferAttrs.getSourceWaferID());
            newWaferAttributesList.add(newWaferAttributes);
        }
        tmpNewLotAttributes.setNewWaferAttributesList(newWaferAttributesList);

        waferMethod.waferAssignedLotChangeForWaferSorter(objCommon, tmpNewLotAttributes.getNewWaferAttributesList());
        log.info("【Method Exit】bankLotPreparationForWaferSorter()");
        return createdLotID;
    }

    public List<Infos.WhatNextStandbyAttributes> bankGetLotListByQueryDR(Infos.ObjCommon objCommon, ObjectIdentifier bankID) {
        log.info("【Method Entry】bankGetLotListByQueryDR()");

        //【Step-1-1】 GetLotListByQueryDR;
        String lotsQuerySql = "select * from OMLOT where OMLOT.BANK_ID  = '%s' " +
                " and OMLOT.LOT_INV_STATE = '%s'" +
                " and OMLOT.LOT_FINISHED_STATE = '%s' " +
                " and OMLOT.LOT_HOLD_STATE ='%s' " +
                " and OMLOT.NPW_USE_STATE = '%s' " +
                " ORDER BY LOT_ID ";
        String lotsQuerySqlFormat = String.format(lotsQuerySql, bankID.getValue(),
                BizConstant.SP_LOT_INVENTORYSTATE_INBANK,
                CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED,
                CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD,
                BizConstant.SP_LOT_CONTROLUSESTATE_WAITUSE);

        List<CimLotDO> lotList = cimJpaRepository.query(lotsQuerySqlFormat, CimLotDO.class);

        int count = CimArrayUtils.getSize(lotList);
        List<Infos.WhatNextStandbyAttributes> whatNextStandbyAttributesList = new ArrayList<>();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                CimLotDO lot = lotList.get(i);

                String cassettesQuerySql = "SELECT  B.CARRIER_ID, " +
                        "                           B.ID, " +
                        "                           B.XFER_STATE, " +
                        "                           B.EQP_ID, " +
                        "                           B.EQP_RKEY, " +
                        "                           B.CARRIER_CATEGORY " +
                        "                   FROM    OMLOT_CARRIER A, OMCARRIER B " +
                        "                   WHERE   A.REFKEY  = '%s' " +
                        "                   AND     B.CARRIER_ID = A.CARRIER_ID  ";
                String cassettesQuerySqlFormat = String.format(cassettesQuerySql, lot.getId());
                Outputs.CassetteInformationOut cassetteInformationOut = new Outputs.CassetteInformationOut();

                Object[] objects = cimJpaRepository.queryOne(cassettesQuerySqlFormat);
                if (objects == null) {
                    continue;
                }
                cassetteInformationOut.setCassetteID(String.valueOf(objects[0]));
                cassetteInformationOut.setCassetteObj(String.valueOf(objects[1]));
                cassetteInformationOut.setTransState(String.valueOf(objects[2]));
                cassetteInformationOut.setEquipmentID(String.valueOf(objects[3]));
                cassetteInformationOut.setEquipmentObj(String.valueOf(objects[4]));
                cassetteInformationOut.setCassetteCategory(String.valueOf(objects[5]));


                //【Step-1-2】  Get MultiLotType;
                ObjectIdentifier cassetteID =  new ObjectIdentifier(cassetteInformationOut.getCassetteID(), cassetteInformationOut.getCassetteObj());
                ObjectIdentifier equipmentID = new ObjectIdentifier(cassetteInformationOut.getEquipmentID(), cassetteInformationOut.getEquipmentObj());
                ObjectIdentifier productID = new ObjectIdentifier(lot.getProductSpecificationID(), lot.getProductSpecificationObj());
                ObjectIdentifier lotID = new ObjectIdentifier(lot.getLotID(), lot.getId());
                String outMultiLotType = cassetteMethod.cassetteMultiLotTypeGet(objCommon, cassetteID);

                //【Step-1-3】  Output Result Structure;
                Infos.WhatNextStandbyAttributes whatNextStandbyAttributes = new Infos.WhatNextStandbyAttributes();

                whatNextStandbyAttributes.setLotID(lotID);
                whatNextStandbyAttributes.setLotType(lot.getLotType());
                whatNextStandbyAttributes.setMultiLotType(outMultiLotType);
                whatNextStandbyAttributes.setLotStatus(lot.getLotFinishedState());
                whatNextStandbyAttributes.setBankID(new ObjectIdentifier(lot.getBankID(), lot.getBankObj()));
                whatNextStandbyAttributes.setProductID(productID);
                whatNextStandbyAttributes.setLastClaimedTimeStamp(lot.getLastClamiedTimeStamp().toString());
                whatNextStandbyAttributes.setTotalWaferCount(Long.parseLong(String.format("%d", lot.getWaferCount())));
                whatNextStandbyAttributes.setControlUseState(lot.getControlUseState());
                if (null != lot.getUsedCount()) {
                    whatNextStandbyAttributes.setUsedCount(Long.parseLong(String.format("%d", lot.getUsedCount())));
                }
                whatNextStandbyAttributes.setRequiredCassetteCategory(lot.getRequiredCassetteCategory());
                whatNextStandbyAttributes.setCassetteID(cassetteID);
                whatNextStandbyAttributes.setTransferStatus(cassetteInformationOut.getTransState());
                whatNextStandbyAttributes.setCassetteCategory(cassetteInformationOut.getCassetteCategory());

                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteInformationOut.getTransState())
                 || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, cassetteInformationOut.getTransState())) {
                    log.info("cassetteInformationOut.getTransState() == 'EI' /'EO'");
                    whatNextStandbyAttributes.setEquipmentID(equipmentID);
                } else {
                    log.info("cassetteInformationOut.getTransState() != 'EI' / 'EO'");
                    whatNextStandbyAttributes.setStockerID(equipmentID);
                }
                whatNextStandbyAttributesList.add(whatNextStandbyAttributes);
                log.info("bankGetLotListByQueryDR(): lotID = {},i = {}", lot.getLotID(), i);
            }
        }

        log.info("【Method Exit】bankGetLotListByQueryDR()");
        return whatNextStandbyAttributesList;
    }

    @Override
    public void bankCheckNonProBank(Infos.ObjCommon objCommon, ObjectIdentifier bankID) {
        CimBank bank = baseCoreFactory.getBO(CimBank.class, bankID);
        Validations.check(null == bank, new OmCode(retCodeConfig.getNotFoundBank(), ObjectIdentifier.fetchValue(bankID)));
        Validations.check(CimBooleanUtils.isTrue(bank.isProductionBank()), retCodeConfig.getNotNonProductBank());
    }
}
