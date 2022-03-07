package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.CimSlotMapDO;
import com.fa.cim.entity.nonruntime.CimSlotMapSTInfoDO;
import com.fa.cim.entity.runtime.actionrecipe.CimActionRecipeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.lot.LotUsageRecycleCountParams;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.fa.cim.newcore.standard.prdctmng.Product;
import com.fa.cim.newcore.standard.prdctmng.Wafer;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/19        ********             Bear               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author Bear
 * @since 2018/9/19 15:40
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class WaferMethod  implements IWaferMethod {

    @Autowired
    @Qualifier("ProductManagerCore")
    private ProductManager productManager;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ISorterMethod sorterMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Override
    public void waferMaterialContainerChange(Infos.ObjCommon objCommon, ObjectIdentifier newCassetteID, Infos.Wafer strWafer) {
        //【step1】retrieve  request user's object reference
        log.info("【step1】retrieve  request user's object reference");
        CimPerson aPosPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPosPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), "*****"));
        //【step2】retrieve object reference of cassette
        log.info("【step2】retrieve object reference of cassette");
        CimCassette aNewCassette = baseCoreFactory.getBO(CimCassette.class, newCassetteID);
        //【step3】check cassette status
        log.info("【step3】check cassette status");
        if (null != aNewCassette) {
            String aDurableState = aNewCassette.getDurableState();
            if (!CimStringUtils.equals(aDurableState, BizConstant.CIMFW_DURABLE_AVAILABLE)
                    && !CimStringUtils.equals(aDurableState, BizConstant.CIMFW_DURABLE_INUSE)){
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), aDurableState, newCassetteID.getValue()));
            }
        }
        //【step4】retrieve object reference of wafer.object reference must be retrieved
        log.info("【step4】retrieve object reference of wafer.object reference must be retrieved");
        CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, strWafer.getWaferID());
        Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
        //【step5】retrieve object reference of old carrier
        log.info("【step5】retrieve object reference of old carrier");
        MaterialContainer aMaterialContainer = aPosWafer.getMaterialContainer();
        //【step6】cancel relation between old carrier and lot/wafer by using this method, both of carrier-wafer
        // relation and carrier-lot relation is canceled.
        log.info("【step6】cancel relation-ship between old cassette and lot/wafer");
        CimCassette anOldCassette = null;
        if (null != aMaterialContainer) {
            anOldCassette = (CimCassette) aMaterialContainer;
            int prevSlotNumber = aPosWafer.getPosition();
            anOldCassette.removeWaferFromPosition(prevSlotNumber);
        }
        //【step7】set wafer position in carrier, by using this method, both of carrier-wafer relation,
        // and carrier-lot relation is canceled.
        log.info("【step7】set wafer position in carrier");
        if (null != aNewCassette) {
            aNewCassette.addWaferAtPosition(aPosWafer, CimNumberUtils.intValue(strWafer.getSlotNumber()));
        }
        // step7 - set wafer last claimed timestamp vs last claimed person
        aPosWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aPosWafer.setLastClaimedPerson(aPosPerson);
        if (null != anOldCassette) {
            // set old cassette last claimed timestamp
            anOldCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            anOldCassette.setLastClaimedPerson(aPosPerson);
        }
        if (null != aNewCassette) {
            // set new cassette last claimed timestamp vs last claimed person
            aNewCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aNewCassette.setLastClaimedPerson(aPosPerson);
        }
    }

    @Override
    public void waferAssignedLotChangeForSTBCancel(Infos.ObjCommon objCommon, List<Infos.NewWaferAttributes> newWaferAttributesList) {
        log.info("check input parameter. new lot of all wafers must be the same...");
        //【step1】check input parameter
        int newWaferAttrSize = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < newWaferAttrSize; i++) {
            if (i > 0 && !ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getNewLotID(), newWaferAttributesList.get(0).getNewLotID())) {
                log.info("[error]newWaferAttributesList[0].newLotID != newWaferAttributesList[i].newLotID");
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
            if (i > 0 && !ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getSourceLotID(), newWaferAttributesList.get(0).getSourceLotID())) {
                log.info("[error]newWaferAttributesList[0].sourceLotID != newWaferAttributesList[i].sourceLotID");
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
        }
        //【step2】retrieve request user's object reference
        log.info("【step2】retrieve request user's object reference");
        CimPerson aPosPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        if (null == aPosPerson) {
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        }
        //【step3】prepare object reference
        log.info("【step3】prepare object reference");
        CimLot aTargetLot = null;
        CimLot aSourceLot = null;
        List<CimWafer> aPosWaferList = new ArrayList<>();
        //【step3-1】check specified source wafer is exist or not
        for (int i = 0; i < newWaferAttrSize; i++) {
            log.info("Check specified source wafer is exist or not");
            CimWafer cimWafer = baseCoreFactory.getBO(CimWafer.class, newWaferAttributesList.get(i).getSourceWaferID());
            Validations.check(cimWafer == null, new OmCode(retCodeConfig.getNotFoundWafer(), newWaferAttributesList.get(i).getSourceWaferID().getValue()));
            aPosWaferList.add(cimWafer);
        }
        ObjectIdentifier sourceLotID = null;
        int sourceLotWaferCount = 0;
        int sourceLotProductWaferCount = 0;
        int sourceLotControlWaferCount = 0;
        //【step3-2】check specified source lot is exist or not
        for (int i = 0; i < newWaferAttrSize; i++) {
            if (0 == i) {
                sourceLotID = newWaferAttributesList.get(i).getSourceLotID();
                sourceLotWaferCount = 1;
                if (aPosWaferList.get(i).isControlWafer()) {
                    sourceLotProductWaferCount = 0;
                    sourceLotControlWaferCount = 1;
                } else {
                    sourceLotProductWaferCount = 1;
                    sourceLotControlWaferCount = 0;
                }
            } else {
                sourceLotWaferCount++;
                if (aPosWaferList.get(i).isControlWafer()) {
                    sourceLotControlWaferCount++;
                } else {
                    sourceLotProductWaferCount++;
                }
            }
        }
        //【step3-3】check specified target lot is exist or not
        log.info("【step3-3】check specified target lot is exist or not");
        aTargetLot = baseCoreFactory.getBO(CimLot.class, newWaferAttributesList.get(0).getNewLotID());
        Validations.check(aTargetLot == null, new OmCode(retCodeConfig.getNotFoundLot(), newWaferAttributesList.get(0).getNewLotID().getValue()));
        //【step4】update source lot's quantity info
        log.info("【step4】update source lot's quantity info");
        aSourceLot = baseCoreFactory.getBO(CimLot.class, sourceLotID);
        Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), sourceLotID.getValue()));
        int nSourceLotProductCount = (null == aSourceLot.getProductQuantity() ? 0 : aSourceLot.getProductQuantity());
        int nSourceLotControlCount = (null == aSourceLot.getControlQuantity() ? 0 : aSourceLot.getControlQuantity());
        int nSourceLotTotalCount = (null == aSourceLot.getQuantity() ? 0 : aSourceLot.getQuantity());
        if (sourceLotWaferCount > nSourceLotTotalCount) {
            log.info("[error]nSourceLotWaferCount > nSourceLotTotalQty");
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        aSourceLot.setProductQuantity(nSourceLotProductCount - sourceLotProductWaferCount);
        aSourceLot.setControlQuantity(nSourceLotControlCount - sourceLotControlWaferCount);
        aSourceLot.setVendorLotQuantity(nSourceLotTotalCount - sourceLotWaferCount);
        //【step5】calculate wafer's initial good die quantity
        log.info("【step5】calculate wafer's initial good die quantity");
        com.fa.cim.newcore.bo.prodspec.CimProductSpecification aProductSpecification = aTargetLot.getProductSpecification();
        Validations.check(null == aProductSpecification, retCodeConfig.getNotFoundProductSpec());
        com.fa.cim.newcore.bo.prodspec.CimProductGroup aProductGroup = aProductSpecification.getProductGroup();
        Validations.check(null == aProductGroup, new OmCode(retCodeConfig.getNotFoundProductGroup(), ""));
        int totalDiceCount = (null == aProductGroup.getGrossDieCount() ? 0 : aProductGroup.getGrossDieCount().intValue());
        double plannedYield = (null == aProductGroup.getPlannedYield() ? 0 : aProductGroup.getPlannedYield());
        double goodDiceQty = (double) totalDiceCount * ((double) plannedYield / 100);
        //【step6】check request type for generating lot
        log.info("【step6】check request type for generating lot");
        String lotType = aTargetLot.getLotType();
        //【step7】update target lot's  quantity info
        log.info("【step7】update target lot's  quantity info");
        aTargetLot.setProductQuantity(newWaferAttrSize);
        aTargetLot.setVendorLotQuantity(newWaferAttrSize);
        //【step8】get lot's inventoryState for RecycleCount Management
        log.info("【step8】get lot's inventoryState for RecycleCount Management");
        boolean onFloorFlag = aTargetLot.isOnFloor();
        //【step9】update wafer-lot relation
        log.info("【step9】update wafer-lot relation");
        for (int i = 0; i < newWaferAttrSize; i++) {
            CimWafer aPosWafer = aPosWaferList.get(i);
            //【step9-1】remove wafer from cassette
            log.info("【step9-1】remove wafer from cassette");
            MaterialContainer aMaterialContainer = aPosWafer.getMaterialContainer();
            CimCassette aPosCassette = (CimCassette) aMaterialContainer;
            Validations.check(null == aPosCassette, new OmCode(retCodeConfig.getNotFoundCassette(), "*****"));
            int nPositionOfWafer = aPosWafer.getPosition();
            aPosCassette.removeWaferFromPosition(nPositionOfWafer);
            //【step9-2】remove wafer from lot
            log.info("【step9-2】remove wafer from lot");
            aSourceLot.removeMaterial(aPosWafer);
            //【step9-3】make wafer not allocated
            log.info("【step9-3】make wafer not allocated");
            aPosWafer.makeNotSTBAllocated();
            //【step9-4】set waferID
            log.info("【step9-4】set waferID and name");
            if (!ObjectIdentifier.isEmptyWithValue(newWaferAttributesList.get(i).getNewWaferID())
                    && !ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getNewWaferID(), newWaferAttributesList.get(i).getSourceWaferID())) {
                productManager.resetWaferIdentifier(aPosWafer, newWaferAttributesList.get(i).getNewWaferID().getValue());
            }
            //【step9-5】assign new lot to wafer
            // By 'addMaterial', wafer's product specification information is changed automatically.
            log.info("【step9-5】assign new lot to wafer");
            aTargetLot.addMaterial(aPosWafer);
            //【step9-6】update wafer attribute for new lot
            log.info("【step9-6】update wafer attribute for new lot");
            if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)
                    || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                    || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)
                    || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)){
                aPosWafer.makeControlWafer();
            } else if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_RECYCLELOT)){
                aPosWafer.makeControlWafer();
                CimLot lot = (CimLot) aPosWafer.getLot();
                Integer usageLimit = lot.getUsageLimit();
                aPosWafer.setUsageCount(usageLimit);
                aPosWafer.setRecycleCount(aPosWafer.getRecycleCount() - 1);
            } else if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)
                    || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_VENDORLOT)){
                aPosWafer.makeNotControlWafer();
            }
            //【step9-7】set previous lot info into wafer
            log.info("【step9-7】set previous lot info into wafer");
            aPosWafer.setPreviousLot(aSourceLot);
            //【step9-8】set wafer's dice quantity information
            log.info("【step9-8】set wafer's dice quantity information");
            aPosWafer.setTotalDiceQuantity(totalDiceCount);
            aPosWafer.setGoodDiceQuantity((int)goodDiceQty);
            aPosWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aPosWafer.setLastClaimedPerson(aPosPerson);
            aSourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aSourceLot.setLastClaimedPerson(aPosPerson);
            aTargetLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aTargetLot.setLastClaimedPerson(aPosPerson);
            // Quantity must refresh here!!
            // Because, Quantity of wafer changes in the top.
            aTargetLot.refreshQuantity();
            //【step9-9】add wafer to cassette
            log.info("【step9-9】add wafer to cassette");
            aPosCassette.addWaferAtPosition(aPosWafer, nPositionOfWafer);
        }
    }

    @Override
    public void waferAssignedLotChange(Infos.ObjCommon objCommon, List<Infos.NewWaferAttributes> newWaferAttributesList) {
        //【step1】check input parameter. new lot of all wafers must be same.
        log.info("【step1】check input parameter. new lot of all wafers must be same.");
        int lenNewWaferAttr = newWaferAttributesList.size();
        for (int i = 1; i < lenNewWaferAttr; i++) {
            boolean check = i > 0 && !ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(0).getNewLotID(), newWaferAttributesList.get(i).getNewLotID());
            Validations.check(check, new OmCode(retCodeConfig.getInvalidInputParam()));
        }
        //【step2】retrieve request user's object reference
        log.info("【step2】retrieve request user's object reference");
        CimPerson aPosPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPosPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), "*****"));
        //【step3】check specified source wafer is exist or not
        log.info("【step3】check specified source wafer is exist or not");
        List<CimWafer> aPosWaferList = new ArrayList<>();
        for (int i = 0; i < lenNewWaferAttr; i++){
            CimWafer aPosWafer = baseCoreFactory.getBO(CimWafer.class, newWaferAttributesList.get(i).getSourceWaferID());
            Validations.check(aPosWafer == null, retCodeConfig.getNotFoundWafer());
            aPosWaferList.add(aPosWafer);
        }
        List<ObjectIdentifier> aSourceLotIDList = new ArrayList<>();
        List<Integer> sourceLotWaferCountList = new ArrayList<>();
        List<Integer> sourceLotProdWaferCountList = new ArrayList<>();
        List<Integer> sourceLotCtrlWaferCountList = new ArrayList<>();
        //【step4】check specified source lot is exist or not
        log.info("【step4】check specified source lot is exist or not...");
        int nTmpSourceLotLen = 0;
        for (int i = 0; i < lenNewWaferAttr; i++) {
            boolean bSourceLotRegFlag = false;
            if (i > 0) {
                for (int j = 0; j < nTmpSourceLotLen; j++) {
                    if (ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getSourceLotID(), aSourceLotIDList.get(j))) {
                        sourceLotWaferCountList.set(j, sourceLotWaferCountList.get(j) + 1);
                        if (aPosWaferList.get(i).isControlWafer()){
                            sourceLotCtrlWaferCountList.set(j, sourceLotCtrlWaferCountList.get(j) + 1);
                        } else {
                            sourceLotProdWaferCountList.set(j, sourceLotProdWaferCountList.get(j) + 1);
                        }
                        bSourceLotRegFlag = true;
                        break;
                    }
                }
            }
            if (bSourceLotRegFlag) {
                continue;
            }
            nTmpSourceLotLen ++;
            sourceLotWaferCountList.add(1);
            if (aPosWaferList.get(i).isControlWafer()) {
                sourceLotProdWaferCountList.add(0);
                sourceLotCtrlWaferCountList.add(1);
            } else {
                sourceLotProdWaferCountList.add(1);
                sourceLotCtrlWaferCountList.add(0);
            }
            aSourceLotIDList.add(newWaferAttributesList.get(i).getSourceLotID());
        }
        //【step5】check specified target lot is exist or not
        log.info("【step5】check specified target lot is exist or not");
        CimLot aTargetLot = baseCoreFactory.getBO(CimLot.class, newWaferAttributesList.get(0).getNewLotID());
        Validations.check(aTargetLot == null, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));
        // 【step6】update source lot's quantity info
        log.info("【step6】update source lot's quantity info");
        CimLot aSourceLot = null;
        for (int i = 0; i < nTmpSourceLotLen; i++) {
            aSourceLot = baseCoreFactory.getBO(CimLot.class, aSourceLotIDList.get(i));
            Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));

            int nSourceLotProdQty = (null == aSourceLot.getProductQuantity() ? 0 : aSourceLot.getProductQuantity());
            int nSourceLotCtrlQty = (null == aSourceLot.getControlQuantity()? 0 : aSourceLot.getControlQuantity());
            int nSourceLotTotalQty = (null == aSourceLot.getQuantity() ? 0 :  aSourceLot.getQuantity());
            Validations.check(sourceLotWaferCountList.get(i) > nSourceLotTotalQty, retCodeConfig.getInvalidInputParam());
            aSourceLot.setProductQuantity(nSourceLotProdQty - sourceLotProdWaferCountList.get(i));
            aSourceLot.setControlQuantity(nSourceLotCtrlQty - sourceLotCtrlWaferCountList.get(i));
            aSourceLot.setVendorLotQuantity(nSourceLotTotalQty - sourceLotWaferCountList.get(i));
            if (nSourceLotTotalQty == sourceLotWaferCountList.get(i)) {
                CimLotFamily aLotFamily = aSourceLot.getLotFamily();
                Validations.check(aLotFamily == null, retCodeConfig.getNotFoundLotFamily());
                boolean bSourceLotFoundInCurrentLots = false;
                boolean bSourceLotFoundInArchiveLots = false;
                String strSourceLotID = aSourceLot.getIdentifier();
                String strLotFamilyID = aSourceLot.getIdentifier();
                List<Lot> aCurrentLotList = aLotFamily.currentLots();
                List<Lot> anArchiveLotList = aLotFamily.archive();
                int nLotListLen = CimArrayUtils.getSize(aCurrentLotList);
                for (int j = 0; j < nLotListLen; j++){
                    String strListLotID = aCurrentLotList.get(j).getIdentifier();
                    if (CimStringUtils.equals(strSourceLotID, strListLotID)){
                        bSourceLotFoundInCurrentLots = true;
                        break;
                    }
                }
                nLotListLen = CimArrayUtils.getSize(anArchiveLotList);
                for (int j = 0; j < nLotListLen; j++){
                    String strListLotID = anArchiveLotList.get(j).getIdentifier();
                    if (CimStringUtils.equals(strSourceLotID, strListLotID)){
                        bSourceLotFoundInArchiveLots = true;
                        break;
                    }
                }
                Validations.check(bSourceLotFoundInCurrentLots && bSourceLotFoundInArchiveLots,
                        new OmCode(retCodeConfig.getLotLotFamilyDataInvalid(),strSourceLotID, strLotFamilyID));
                Validations.check(!bSourceLotFoundInCurrentLots && !bSourceLotFoundInArchiveLots,
                        new OmCode(retCodeConfig.getLotLotFamilyDataInvalid(), strSourceLotID, strLotFamilyID));
                if (bSourceLotFoundInCurrentLots && !bSourceLotFoundInArchiveLots) {
                    log.info("slot is in currentLots List of lot family...");
                    if (!ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(0).getNewLotID(), aSourceLotIDList.get(i))){
                        aLotFamily.archiveLot(aSourceLot);
                        aLotFamily.removeCurrentLot(aSourceLot);
                    }
                }
            }
        }
        //【step7】calculate wafer's initial good die quantity...
        log.info("【step7】calculate wafer's initial good die quantity...");
        CimProductSpecification aProductSpecification = aTargetLot.getProductSpecification();
        Validations.check(aProductSpecification == null, retCodeConfig.getNotFoundProductSpec());
        CimProductGroup aProductGroup = aProductSpecification.getProductGroup();
        Validations.check(aProductGroup == null, new OmCode(retCodeConfig.getNotFoundProductGroup(), ""));
        long totalDiceQty = aProductGroup.getGrossDieCount() == null ? 0 : aProductGroup.getGrossDieCount();
        double plannedYield = aProductGroup.getPlannedYield() == null ? 0 : aProductGroup.getPlannedYield();
        double goodDiceQty = totalDiceQty * (plannedYield / 100);
        String strLotType = aTargetLot.getLotType();
        // 【step8】update target lot's quantity info
        log.info("【step8】update target lot's quantity info");
        int nTargetLotProdQty = aTargetLot.getProductQuantity() == null ? 0 : aTargetLot.getProductQuantity();
        aTargetLot.setProductQuantity(nTargetLotProdQty + lenNewWaferAttr);
        int nTargetLotTotalQty = aTargetLot.getQuantity();
        aTargetLot.setVendorLotQuantity(nTargetLotTotalQty + lenNewWaferAttr);
        //--------------------------------------------------------
        // Get Lot's inventoryState for RecycleCount Management
        //--------------------------------------------------------
        boolean onFloorFlag = aTargetLot.isOnFloor();
        //【step9】update wafer lot relation
        log.info("【step9】update wafer lot relation");
        for (int i = 0; i < lenNewWaferAttr; i++) {
            //【step9-1】remove wafer from cassette
            CimWafer aPosWafer = aPosWaferList.get(i);
            MaterialContainer aMaterialContainer = aPosWafer.getMaterialContainer();
            CimCassette aPosCassette = (CimCassette) aMaterialContainer;
            Validations.check(aPosCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), "*****"));
            int nPositionOfWafer = aPosWafer.getPosition();
            aPosCassette.removeWaferFromPosition(nPositionOfWafer);
            //【step9-2】remove wafer from lot
            log.info("remove wafer from lot...");
            Lot tmpLot = aPosWafer.getLot();
            aSourceLot = (CimLot) tmpLot;
            Validations.check(aSourceLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ""));
            aSourceLot.removeMaterial(aPosWafer);
            //【step9-3】make wafer not allocated
            aPosWafer.makeNotSTBAllocated();
            //【step9-4】set wafer ID and Name (This section must be executed before 'lot.getadMaterial()')
            ObjectIdentifier newWaferID = newWaferAttributesList.get(i).getNewWaferID();
            ObjectIdentifier sourceWaferID = newWaferAttributesList.get(i).getSourceWaferID();
            if (!ObjectIdentifier.isEmptyWithValue(newWaferID)
                    && !ObjectIdentifier.equalsWithValue(newWaferID, sourceWaferID)) {
                productManager.resetWaferIdentifier(aPosWafer, newWaferID.getValue());
            }
            // 【step9-5】assign new lot to wafer by 'addMaterial', wafer's product specification information is changed automatically.
            log.info("assign new lot to wafer...");
            aTargetLot.addMaterial(aPosWafer);
            //【step9-6】update wafer attribute for new lot
            log.info("【step9-6】update wafer attribute for new lot");
            if (CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)
                || CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                || CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)
                || CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                aPosWafer.makeControlWafer();
            } else if (CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_RECYCLELOT)){
                aPosWafer.makeControlWafer();
                if (onFloorFlag){
                    //new requirment, only recycle lot stb will add the recycle Count
//                    aPosWafer.setRecycleCount(aPosWafer.getRecycleCount() + 1);
                }
            } else if (CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)
                        || CimStringUtils.equals(strLotType, BizConstant.SP_LOT_TYPE_VENDORLOT)) {
                aPosWafer.makeNotControlWafer();
            }
            //【step9-7】 set previous lot info wafer
            log.info("【step9-7】 set previous lot info wafer");
            aPosWafer.setPreviousLot(aSourceLot);
            //【step9-8】update wafer info
            log.info("【step9-8】update wafer info");
            // set wafer's dice quantity information
            aPosWafer.setTotalDiceQuantity((int)totalDiceQty);
            aPosWafer.setGoodDiceQuantity((int)goodDiceQty);
            // set wafer last claimed timestamp/last claimed person
            aPosWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aPosWafer.setLastClaimedPerson(aPosPerson);
            // set source lot last claimed timestamp/last claimed person
            aSourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aSourceLot.setLastClaimedPerson(aPosPerson);
            // set target lot last clamied timestamp/last clamied person
            aTargetLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aTargetLot.setLastClaimedPerson(aPosPerson);
            // quantity must refresh here. because, quantity of wafer change in the top.
            aTargetLot.refreshQuantity();
            // add wafer to cassette
            aPosCassette.addWaferAtPosition(aPosWafer, nPositionOfWafer);
            // add AliasWaferName
            aPosWafer.setAliasWaferName(newWaferAttributesList.get(i).getWaferAliasName());
        }
        //【step10】update lot finish state
        log.info("【step10】update lot finish state");
        for (int i = 0; i < nTmpSourceLotLen; i++) {
            if (ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(0).getNewLotID(), aSourceLotIDList.get(i))){
                continue;
            }
            aSourceLot = baseCoreFactory.getBO(CimLot.class, aSourceLotIDList.get(i));
            Validations.check(aSourceLot == null, aSourceLotIDList.get(i).getValue());
            Integer nSrcLotTotalQty = aSourceLot.getQuantity() == null ? 0 : aSourceLot.getQuantity();
            if (nSrcLotTotalQty > 0) {
                continue;
            }
            //【step10-1】 check for scrapped wafer existence of lot family
            log.info("【step10-1】 check for scrapped wafer existence of lot family");
            CimLotFamily aLotFamily = aSourceLot.getLotFamily();
            Validations.check(aLotFamily == null, retCodeConfig.getNotFoundLotFamily());
            List<Product> aProductList = aLotFamily.productUnitsScrapped();
            if (CimArrayUtils.isEmpty(aProductList)) {
                log.info("Scrap wafer don't exist in the lot family...");
                aSourceLot.makeEmptied();
                String envEventCreateTypeStr = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();
                int envEventCreateType = CimStringUtils.isEmpty(envEventCreateTypeStr) ? 0 : Integer.parseInt(envEventCreateTypeStr);
                if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED
                        || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                    CimProcessOperation aCurrentPO = aSourceLot.getProcessOperation();
                    if (aCurrentPO != null){
                        processMethod.poDelQueuePutDR(objCommon, aSourceLotIDList.get(i));
                    }
                }
                continue;
            }
            log.info("Scrap wafers exist in the lot family...");
            boolean changeToEmptiedFlag = false;
            List<Lot> aCurrentLotList = aLotFamily.currentLots();
            int nLotLen = CimArrayUtils.getSize(aCurrentLotList);
            for (int k = 0; k < nLotLen; k++) {
                CimLot aPosLot = (CimLot) aCurrentLotList.get(k);
                Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ""));
                log.info("Not emptied lot exists in the lot family...");
                String strLotID = aPosLot.getIdentifier();
                if (ObjectIdentifier.equalsWithValue(strLotID, aSourceLotIDList.get(i))){
                    continue;
                }
                changeToEmptiedFlag = true;
                break;
            }
            if (changeToEmptiedFlag) {
                aSourceLot.makeEmptied();
                String envEventCreateTypeStr = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();
                int envEventCreateType = CimStringUtils.isEmpty(envEventCreateTypeStr) ? 0 : Integer.parseInt(envEventCreateTypeStr);
                if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED
                        || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                    CimProcessOperation aCurrentPO = aSourceLot.getProcessOperation();
                    if (null != aCurrentPO) {
                        processMethod.poDelQueuePutDR(objCommon, aSourceLotIDList.get(i));
                    }
                }
                continue;
            }
            List<Lot> anArchiveLotList = aLotFamily.archive();
            nLotLen = CimArrayUtils.getSize(anArchiveLotList);
            for (int k = 0; k < nLotLen; k++) {
                CimLot aPosLot = (CimLot) anArchiveLotList.get(k);
                Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ""));
                String strLotID = aPosLot.getIdentifier();
                if (ObjectIdentifier.equalsWithValue(strLotID, aSourceLotIDList.get(i))){
                    continue;
                }
                int lotTotalCount = (null == aPosLot.getQuantity() ? 0 : aPosLot.getQuantity());
                String strLotState = aPosLot.getLotState();
                String strLotFinishState = aPosLot.getLotFinishedState();
                if (0 == lotTotalCount) {
                    log.info("lot total Count is 0");
                    if (CimStringUtils.equals(BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED, strLotFinishState)) {
                        log.info("lot finish state is EMPTIED...");
                        continue;
                    }
                    boolean bOtherSourceLotFlag = false;
                    for (int l = 0; l < nTmpSourceLotLen; l++) {
                        if (ObjectIdentifier.equalsWithValue(strLotID, aSourceLotIDList.get(l))) {
                            log.info("This lot is other source lot(not watched)");
                            bOtherSourceLotFlag = true;
                            break;
                        }
                    }
                    if (bOtherSourceLotFlag) {
                        log.info("OTHER source lot flag is true...");
                        continue;
                    }
                    log.info("lot finish state is SCRAPPED");
                    changeToEmptiedFlag = true;
                    break;
                } else {
                    if (CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_SHIPPED, strLotState)) {
                        log.info("lot state is SHIPPED...");
                        continue;
                    }
                    changeToEmptiedFlag = true;
                    break;
                }
            }
            if (changeToEmptiedFlag) {
                aSourceLot.makeEmptied();
                String envEventCreateTypeStr = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();
                int envEventCreateType = CimStringUtils.isEmpty(envEventCreateTypeStr) ? 0 : Integer.parseInt(envEventCreateTypeStr);
                if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED
                        || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                    CimProcessOperation aCurrentPO = aSourceLot.getProcessOperation();
                    if (null != aCurrentPO) {
                        processMethod.poDelQueuePutDR(objCommon, aSourceLotIDList.get(i));
                    }
                }
                continue;
            }
            log.info("Change to Emptied is false...");

            // step9-1 - this is loophole logic for makeScrapped().
            // if source lot isn't on route, back the lot state from archive to current once.
            // lot state become archive again in makeScrapped.
            CimProcessFlowContext aPosProcessFlowContext = aSourceLot.getProcessFlowContext();
            if (null == aPosProcessFlowContext) {
                aLotFamily.removeArchive(aSourceLot);
                aLotFamily.addLot(aSourceLot);
            }
            aSourceLot.makeScrapped();
            String envEventCreateTypeStr = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();
            int envEventCreateType = CimStringUtils.isEmpty(envEventCreateTypeStr) ? 0 : Integer.parseInt(envEventCreateTypeStr);
            if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED
                    || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                CimProcessOperation aCurrentPO = aSourceLot.getProcessOperation();
                if (null != aCurrentPO) {
                    processMethod.poDelQueuePutDR(objCommon, aSourceLotIDList.get(i));
                }
            }
        }
    }

    @Override
    public Outputs.ObjWaferLotCassetteGetOut waferLotCassetteGet(Infos.ObjCommon objCommon, ObjectIdentifier waferID) {

        Outputs.ObjWaferLotCassetteGetOut out = new Outputs.ObjWaferLotCassetteGetOut();

        CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, waferID);
        Validations.check(CimObjectUtils.isEmpty(aWafer),retCodeConfig.getNotFoundWafer());
        MaterialContainer aMaterialContainer = aWafer.getMaterialContainer();
        Validations.check(CimObjectUtils.isEmpty(aMaterialContainer),retCodeConfig.getNotFoundMaterial());
        String tmpMaterialContainer = null;
        if (!CimObjectUtils.isEmpty(aMaterialContainer)){
            tmpMaterialContainer = aMaterialContainer.getIdentifier();
        }

        Lot lot = aWafer.getLot();
        Validations.check(CimObjectUtils.isEmpty(lot),retCodeConfig.getNotFoundLot());
        CimLot cimLot = (CimLot) lot;

        // Set lotID and cassetteID to out paramter.
        log.info("Set lotID and cassetteID to out paramter");
        if (CimStringUtils.isNotEmpty(tmpMaterialContainer)){
            out.setCassetteID(new ObjectIdentifier(tmpMaterialContainer));
        }else {
            out.setCassetteID(new ObjectIdentifier(""));
        }
        if (!CimObjectUtils.isEmpty(cimLot)){
            out.setLotID(ObjectIdentifier.build(cimLot.getIdentifier(),cimLot.getPrimaryKey()));
        }

        return out;
    }

    @Override
    public void waferSorterCheckRunningJobs(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroup) {

        log.info("【Method Entry】waferSorterCheckRunningJobs()");

        log.info("in para equipmentID = {}", equipmentID.getValue());
        log.info("in para portGroupName = {}", portGroup);

        //================================================================
        // Checking Previous Job is running or not
        //================================================================
        Infos.WaferSorterSlotMap strWaferSorterSlotMap = new Infos.WaferSorterSlotMap();
        //------------------------------------------------------
        //   Setting Search Condition Value
        //   Condition
        //   sorterStatus = "Resuested"
        //   direction    = "OMS"
        //   This condition means Sort request was submitted,
        //   but TCS Responce is not received
        //------------------------------------------------------
        strWaferSorterSlotMap.setEquipmentID(equipmentID);
        strWaferSorterSlotMap.setPortGroup(portGroup);
        strWaferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_REQUESTED);
        strWaferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);

        //------------------------------------------------------
        //   Setting Not Search Condition Value
        //------------------------------------------------------
        strWaferSorterSlotMap.setDestinationSlotNumber(0L);
        strWaferSorterSlotMap.setOriginalSlotNumber(0L);
        strWaferSorterSlotMap.setRequestTime("");
        strWaferSorterSlotMap.setDestinationCassetteManagedByOM(false);
        strWaferSorterSlotMap.setOriginalCassetteManagedByOM(false);

        //------------------------------------------------------
        //   Search From Flotmap DB
        //------------------------------------------------------
        List<Infos.WaferSorterSlotMap> outWaferSorterSlopMapSelectDR = null;
        try {
            outWaferSorterSlopMapSelectDR = waferSorterSlotMapSelectDR(objCommon,
                    BizConstant.SP_SORTER_SLOTMAP_ALLDATA, "",  BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                    BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, strWaferSorterSlotMap);
            //------------------------------------------------------
            //   Other cases, waferSorter_slotMap_SelectDR returns RC_OK
            //   So there must be previous jobs
            //------------------------------------------------------
            log.info("There are previous Jobs.");
        } catch (ServiceException e) {
            //------------------------------------------------------
            //   If there's no running Jobs
            //------------------------------------------------------
            if (Validations.isEquals(retCodeConfigEx.getNotFoundSlotMapRecord(), e.getCode())) {
                log.info( "There 's no previous Jobs");
                Validations.check(retCodeConfigEx.getWaferSortPreviousJobNotFound());
            }
            //------------------------------------------------------
            //   If Any error occurs
            //------------------------------------------------------
            else{
                throw e;
            }
        }
        Validations.check(retCodeConfigEx.getWaferSortAlreadyRunning());
    }

    public List<Infos.WaferSorterSlotMap> waferSorterSlotMapSelectDR(Infos.ObjCommon objCommon,
                                                                     String requiredData, String sortPattern, String destinationCassetteManagedBySiViewFlag,
                                                                     String originalCassetteManagedBySiViewFlag, Infos.WaferSorterSlotMap strWaferSorterSlotMap) {

        List<Infos.WaferSorterSlotMap> slotMapSequence = new ArrayList<>();

        String tmpLastReqTime = null;

        log.info("【Method Entry】waferSorterSlotMapSelectDR");

        if (CimBooleanUtils.isTrue(strWaferSorterSlotMap.getDestinationCassetteManagedByOM())) {
            log.debug("DestinationCassetteManagedByOM = true");
        } else {
            log.debug("DestinationCassetteManagedByOM = false");
        }

        String strEnvNameMaximumWafersInALot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
        long upLen = CimStringUtils.isEmpty(strEnvNameMaximumWafersInALot) ? 10 : CimNumberUtils.longValue(strEnvNameMaximumWafersInALot);

        //【step1】blank check equipmentID portGroup and get
        log.debug("【step1】blank check equipmentID portGroup and get");

        long blankCheck = 0;
        if (ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getEquipmentID())) {
            log.debug("equipmentID is blank.");
            blankCheck = blankCheck + 1;
        }

        if(CimObjectUtils.isEmpty(strWaferSorterSlotMap.getPortGroup())) {
            log.debug("protGroup is blank");
            blankCheck = blankCheck + 1;
        }

        String tmpPortGroup = null;
        ObjectIdentifier tmpEquipmentID = null;
        Validations.check(blankCheck == 1, retCodeConfig.getInvalidParameter());
        if (blankCheck == 0) {
            tmpPortGroup = strWaferSorterSlotMap.getPortGroup();
            tmpEquipmentID = strWaferSorterSlotMap.getEquipmentID();
        } else if (blankCheck == 2) {
            // blankCheck = 2
            List<Object[]> ports = cimJpaRepository.query("SELECT EQP_ID, EQP_RKEY, PORT_GRP FROM OMPORT WHERE ID = (SELECT ENTITY_MGR FROM OMMATLOC WHERE RESIDING_MTRL_ID = ?1)",
                    ObjectIdentifier.fetchValue(strWaferSorterSlotMap.getDestinationCassetteID()));
            if (!CimObjectUtils.isEmpty(ports)) {
                tmpPortGroup = (String) ports.get(0)[2];
                tmpEquipmentID = new ObjectIdentifier((String) ports.get(0)[0], (String) ports.get(0)[1]);
            }
        }


        //【step2】select sql from OSSLOTMAP get letset request_time
        log.debug("【step2】select sql from OSSLOTMAP get letset request_time");
        if (CimStringUtils.equals(BizConstant.SP_SORTER_SLOTMAP_LATESTDATA, requiredData)) {
            String hsSlotMapPortGroup = tmpPortGroup;
            String hsSlotMapEqpID = tmpEquipmentID == null ? null : tmpEquipmentID.getValue();
            String sql = null;
            List<Object> query = new ArrayList<>();
            if (CimStringUtils.isEmpty(strWaferSorterSlotMap.getActionCode())){
                log.info("actionCode is null");
                sql = "SELECT  DISTINCT MAX(REQUEST_TIME) FROM  OSSLOTMAP WHERE PORT_GRP = ?1 AND EQP_ID = ?2";
                query = cimJpaRepository.queryOneColumn(sql, hsSlotMapPortGroup, hsSlotMapEqpID);
            }else {
                log.info("actionCode = {}",strWaferSorterSlotMap.getActionCode());
                String fsSlotMapActionCode = strWaferSorterSlotMap.getActionCode();
                sql = "SELECT  DISTINCT MAX(REQUEST_TIME) FROM  OSSLOTMAP  WHERE ACTION_CODE = ?1 AND PORT_GRP = ?2 AND  EQP_ID = ?3";
                query = cimJpaRepository.queryOneColumn(sql, fsSlotMapActionCode, hsSlotMapPortGroup, hsSlotMapEqpID);
            }
            String fsSlotMapRequestTimeInd = null;
            if (CimArrayUtils.getSize(query) > 0){
                fsSlotMapRequestTimeInd = String.valueOf(query.get(0));
            }
            tmpLastReqTime = fsSlotMapRequestTimeInd;
            log.info("LatestReqTime = {}",tmpLastReqTime);
            Validations.check(CimStringUtils.isEmpty(tmpLastReqTime) || CimStringUtils.equals(tmpLastReqTime,"null"), retCodeConfigEx.getNotFoundSlotMapRecord());

        }
        //【step3】select sql from OSSLOTMAP
        log.debug("【step3】select sql from OSSLOTMAP");
        String HVTMPBUFFER = null;
        String HVBUFFER =
                "SELECT     PORT_GRP, " +
                        "   EQP_ID, " +
                        "   ACTION_CODE, " +
                        "   REQUEST_TIME, " +
                        "   DIRECTION, " +
                        "   WAFER_ID, " +
                        "   LOT_ID, " +
                        "   DEST_CARRIER_ID, " +
                        "   DEST_PORT_ID, " +
                        "   DEST_CARRIER_IS_REGIST, " +
                        "   DEST_POSITION, " +
                        "   ORIG_CARRIER_ID, " +
                        "   ORIG_PORT_ID, " +
                        "   ORIG_CARRIER_IS_REGIST, " +
                        "   ORIG_POSITION, " +
                        "   USER_ID," +
                        "   REPLY_TIME, " +
                        "   SORTER_STATUS, " +
                        "   SLOT_COMPARE_STATUS, " +
                        "   MES_COMPARE_STATUS," +
                        "   ALIAS_NAME "+
                "FROM   OSSLOTMAP ";

        //-----------------------
        // SET WHERE
        //-----------------------
        HVBUFFER += "WHERE";

        //-----------------------
        // SET PORTGRP
        //-----------------------
        HVTMPBUFFER = String.format(" PORT_GRP = '%s'",tmpPortGroup);
        HVBUFFER += HVTMPBUFFER;

        //-----------------------
        // SET EQP_ID
        //-----------------------
        HVTMPBUFFER = String.format(" AND EQP_ID = '%s'", ObjectIdentifier.fetchValue(tmpEquipmentID));
        HVBUFFER += HVTMPBUFFER;

        //-----------------------
        // SET ACTION_CODE
        //-----------------------
        if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getActionCode())){
            HVTMPBUFFER = String.format(" AND ACTION_CODE = '%s'",strWaferSorterSlotMap.getActionCode());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET REQUEST_TIME
        //-----------------------
        Timestamp timestamp = null;
        if (CimStringUtils.equals(BizConstant.SP_SORTER_SLOTMAP_LATESTDATA,requiredData)){
            log.info("requiredData == SP_Sorter_SlotMap_LatestData,{}",tmpLastReqTime);
            timestamp = tmpLastReqTime == null ? null : Timestamp.valueOf(tmpLastReqTime);
            HVTMPBUFFER = " AND REQUEST_TIME = ?1 ";
            HVBUFFER += HVTMPBUFFER;
        }else {
            log.info("requiredData != SP_Sorter_SlotMap_LatestData");
            if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getRequestTime())){
                log.info("CIMFWStrLen(requestTime) != 0 {}",strWaferSorterSlotMap.getRequestTime());
                timestamp = strWaferSorterSlotMap.getRequestTime() == null ? null : Timestamp.valueOf(strWaferSorterSlotMap.getRequestTime());
                HVTMPBUFFER = " AND REQUEST_TIME = ?1";
                HVBUFFER += HVTMPBUFFER;
            }
        }

        //-----------------------
        // SET DIRECTION
        //-----------------------
        if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getDirection())){
            log.info("CIMFWStrLen(direction) != 0 {}",strWaferSorterSlotMap.getDirection());
            HVTMPBUFFER = String.format(" AND DIRECTION = '%s'",strWaferSorterSlotMap.getDirection());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET WAFER_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getWaferID())){
            log.info("CIMFWStrLen(waferID.identifier) != 0 {}",strWaferSorterSlotMap.getWaferID().getValue());
            HVTMPBUFFER = String.format(" AND WAFER_ID = '%s'",strWaferSorterSlotMap.getWaferID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET LOT_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getLotID())){
            log.info("CIMFWStrLen(lotID.identifier) != 0 {}",strWaferSorterSlotMap.getLotID().getValue());
            HVTMPBUFFER = String.format(" AND LOT_ID = '%s'",strWaferSorterSlotMap.getLotID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET DEST_CAST_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getDestinationCassetteID())){
            log.info("CIMFWStrLen(destinationCassetteID.identifier) != 0 {}",strWaferSorterSlotMap.getDestinationCassetteID().getValue());
            HVTMPBUFFER = String.format(" AND DEST_CARRIER_ID = '%s'",strWaferSorterSlotMap.getDestinationCassetteID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET DEST_PORT_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getDestinationPortID())){
            log.info("CIMFWStrLen(destinationPortID.identifier) != 0 {}",strWaferSorterSlotMap.getDestinationCassetteID().getValue());
            HVTMPBUFFER = String.format(" AND DEST_PORT_ID = '%s'",strWaferSorterSlotMap.getDestinationPortID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET DEST_M_B_SIVIEW
        //-----------------------
        if (!CimStringUtils.equals(BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,destinationCassetteManagedBySiViewFlag)){
            log.info("destinationCassetteManagedBySiViewFlag != SP_Sorter_Ignore_SiViewFlag");
            if (CimBooleanUtils.isTrue(strWaferSorterSlotMap.getDestinationCassetteManagedByOM())){
                log.info("bDestinationCassetteManagedBySiView == TRUE");
                HVBUFFER += " AND DEST_CARRIER_IS_REGIST <> 0";
            }else {
                log.info("bDestinationCassetteManagedBySiView == FALSE");
                HVBUFFER += " AND DEST_CARRIER_IS_REGIST = 0";
            }
        }

        //-----------------------
        // SET DEST_POSITION
        //-----------------------
        if (CimNumberUtils.longValue(strWaferSorterSlotMap.getDestinationSlotNumber()) != 0){
            HVTMPBUFFER = String.format(" AND DEST_POSITION = %s", CimNumberUtils.longValue(strWaferSorterSlotMap.getDestinationSlotNumber()));
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET ORG_CAST_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getOriginalCassetteID())){
            log.info("CIMFWStrLen(originalCassetteID.identifier) != 0 {}",strWaferSorterSlotMap.getOriginalCassetteID().getValue());
            HVTMPBUFFER = String.format(" AND ORIG_CARRIER_ID = '%s'",strWaferSorterSlotMap.getOriginalCassetteID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET ORG_PORT_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getOriginalPortID())){
            log.info("CIMFWStrLen(originalPortID.identifier) != 0 {}",strWaferSorterSlotMap.getOriginalPortID().getValue());
            HVTMPBUFFER = String.format(" AND ORIG_PORT_ID = '%s'",strWaferSorterSlotMap.getOriginalPortID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET ORG_M_B_SIVIEW
        //-----------------------
        if (!CimStringUtils.equals(BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,originalCassetteManagedBySiViewFlag)){
            log.info("bOriginalCassetteManagedBySiView != SP_Sorter_Ignore_SiViewFlag");
            if (CimBooleanUtils.isTrue(strWaferSorterSlotMap.getOriginalCassetteManagedByOM())){
                log.info("bOriginalCassetteManagedBySiView == TRUE");
                HVBUFFER += " AND ORIG_CARRIER_IS_REGIST <> 0";
            }else {
                log.info("bOriginalCassetteManagedBySiView == FALSE");
                HVBUFFER += " AND ORIG_CARRIER_IS_REGIST = 0";
            }
        }

        //-----------------------
        // SET ORG_POSITION
        //-----------------------
        if (CimNumberUtils.longValue(strWaferSorterSlotMap.getOriginalSlotNumber()) != 0){
            log.info("CIMFWStrLen(originalSlotNumber) != 0 {}", CimNumberUtils.longValue(strWaferSorterSlotMap.getOriginalSlotNumber()));
            HVTMPBUFFER = String.format(" AND ORIG_POSITION = %s", CimNumberUtils.longValue(strWaferSorterSlotMap.getOriginalSlotNumber()));
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET USER_ID
        //-----------------------
        if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMap.getRequestUserID())){
            log.info("CIMFWStrLen(requestUserID.identifier) != 0 {}",strWaferSorterSlotMap.getRequestUserID().getValue());
            HVTMPBUFFER = String.format(" AND USER_ID = '%s'",strWaferSorterSlotMap.getRequestUserID().getValue());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET SORTER_STATUS
        //-----------------------
        if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getSorterStatus())){
            log.info("CIMFWStrLen(sorterStatus) != 0 {}",strWaferSorterSlotMap.getSorterStatus());
            HVTMPBUFFER = String.format(" AND SORTER_STATUS = '%s'",strWaferSorterSlotMap.getSorterStatus());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET SLOTMAP_COMP
        //-----------------------
        if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getSlotMapCompareStatus())){
            log.info("CIMFWStrLen(slotMapCompareStatus) != 0 {}",strWaferSorterSlotMap.getSlotMapCompareStatus());
            HVTMPBUFFER = String.format(" AND SLOT_COMPARE_STATUS = '%s'",strWaferSorterSlotMap.getSlotMapCompareStatus());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET MM_COMP
        //-----------------------
        if (CimStringUtils.isNotEmpty(strWaferSorterSlotMap.getOmsCompareStatus())){
            log.info("CIMFWStrLen(mmCompareStatus) != 0 {}",strWaferSorterSlotMap.getOmsCompareStatus());
            HVTMPBUFFER = String.format(" AND MES_COMPARE_STATUS = '%s'",strWaferSorterSlotMap.getOmsCompareStatus());
            HVBUFFER += HVTMPBUFFER;
        }

        //-----------------------
        // SET ORDER BY
        //-----------------------
        if (CimStringUtils.isNotEmpty(sortPattern)){
            log.info("CIMFWStrLen(mmCompareStatus) != 0 {}",sortPattern);
            HVBUFFER += sortPattern;
        }else {
            log.info("CIMFWStrLen(mmCompareStatus) == 0 ORDER BY DEST_CARRIER_ID, DEST_POSITION ");
            HVBUFFER += " ORDER BY REQUEST_TIME DESC, DEST_CARRIER_ID, DEST_POSITION ";
        }
        List<Object[]> queryResult = new ArrayList<>();
        if (CimObjectUtils.isEmpty(timestamp)){
            queryResult = cimJpaRepository.query(HVBUFFER);
        }else {
            queryResult = cimJpaRepository.query(HVBUFFER,timestamp);
        }

        //---------------------------
        //     SET RETURN VALUE
        //---------------------------
        if (CimArrayUtils.getSize(queryResult) > 0){
            for (Object[] objects : queryResult) {
                Infos.WaferSorterSlotMap waferSorterSlotMap = new Infos.WaferSorterSlotMap();
                waferSorterSlotMap.setPortGroup(CimObjectUtils.toString(objects[0]));//PORTGRP
                waferSorterSlotMap.setEquipmentID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[1])));//EQP_ID
                waferSorterSlotMap.setActionCode(CimObjectUtils.toString(objects[2]));//ACTION_CODE
                waferSorterSlotMap.setRequestTime(CimObjectUtils.toString(objects[3]));//REQUEST_TIME
                waferSorterSlotMap.setDirection(CimObjectUtils.toString(objects[4]));//DIRECTION
                waferSorterSlotMap.setWaferID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[5])));//WAFER_ID
                //-----------------------------------------------------------------------
                // lotID of MM is used if lotID can't be acquired by the answer from TCS.
                //-----------------------------------------------------------------------
                if (CimObjectUtils.isEmpty(CimObjectUtils.toString(objects[6])) && !CimObjectUtils.isEmpty(CimObjectUtils.toString(objects[5]))){
                    log.info("waferSorter_SlotMap_SelectDR , DB's lotID is NULL... ");
                    //-----------------------------------------------------------
                    //   Get Lot ID from Wafer ID
                    //-----------------------------------------------------------
                    try {
                        ObjectIdentifier lotMethodWaferLotOut = waferMethod.waferLotGet(objCommon, ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[5])));
                        log.info("wafer_lot_Get() == RC_OK");
                        waferSorterSlotMap.setLotID(lotMethodWaferLotOut);
                    }catch (Exception ex){}
                }else {
                    log.info("waferSorter_SlotMap_SelectDR , DB's lotID Get OK");
                    waferSorterSlotMap.setLotID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[6])));
                }
                waferSorterSlotMap.setDestinationCassetteID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[7])));//DEST_CAST_ID
                waferSorterSlotMap.setDestinationPortID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[8])));//DEST_PORT_ID
                waferSorterSlotMap.setDestinationCassetteManagedByOM(CimBooleanUtils.getBoolean(CimObjectUtils.toString(objects[9])));//DEST_M_B_SIVIEW boolean
                if (null != objects[10]) {
                    waferSorterSlotMap.setDestinationSlotNumber(CimNumberUtils.longValue((Number) objects[10]));//DEST_POSITION  integer
                }
                waferSorterSlotMap.setOriginalCassetteID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[11])));//ORG_CAST_ID
                waferSorterSlotMap.setOriginalPortID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[12])));//ORG_PORT_ID
                waferSorterSlotMap.setOriginalCassetteManagedByOM(Boolean.valueOf(CimObjectUtils.toString(objects[13])));//ORG_M_B_SIVIEW boolean
                if (null != objects[14]) {
                    waferSorterSlotMap.setOriginalSlotNumber(CimNumberUtils.longValue((Number) objects[14]));//ORG_POSITION integer
                }
                waferSorterSlotMap.setRequestUserID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(objects[15])));//USER_ID
                waferSorterSlotMap.setReplyTime(CimObjectUtils.toString(objects[16]));//REPLY_TIME
                waferSorterSlotMap.setSorterStatus(CimObjectUtils.toString(objects[17]));//SORTER_STATUS
                waferSorterSlotMap.setSlotMapCompareStatus(CimObjectUtils.toString(objects[18]));//SLOTMAP_COMP
                waferSorterSlotMap.setOmsCompareStatus(CimObjectUtils.toString(objects[19]));//MM_COMP
                waferSorterSlotMap.setAliasName(CimObjectUtils.toString(objects[20]));//ALIAS_NAME
                slotMapSequence.add(waferSorterSlotMap);
            }
        }else {
            throw new ServiceException(retCodeConfigEx.getNotFoundSlotMapRecord());
        }

        Validations.check(CimArrayUtils.isEmpty(slotMapSequence), retCodeConfigEx.getNotFoundSlotMapRecord());

        slotMapSequence.forEach(x -> {
            if (CimStringUtils.isEmpty(x.getPortGroup())
                    || CimStringUtils.isEmpty(x.getEquipmentID().getValue())
                    || CimStringUtils.isEmpty(x.getActionCode())
                    || CimStringUtils.isEmpty(x.getRequestTime())
                    || CimStringUtils.isEmpty(x.getDirection())
                    || CimStringUtils.isEmpty(x.getDestinationCassetteID().getValue())
                    || CimStringUtils.isEmpty(x.getDestinationPortID().getValue())
                    || CimStringUtils.isEmpty(x.getRequestUserID().getValue())
                    || CimStringUtils.isEmpty(x.getSorterStatus())){
                log.info("It doesn't have a necessary item");
                slotMapSequence.clear();
            }
        });
        log.info("【Method Exit】waferSorterSlotMapSelectDR()");

        return slotMapSequence;
    }

    @Override
    public ObjectIdentifier waferLotGet(Infos.ObjCommon objCommon, ObjectIdentifier waferID) {
        log.info("【Method Entry】waferLotGet()");
        CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, waferID);
        Validations.check(aWafer == null, retCodeConfig.getNotFoundWafer());
        Lot aLot = aWafer.getLot();
        Validations.check(aLot == null, retCodeConfig.getWaferLotConnectionError());
        CimLot aPosLot = (CimLot) aLot;
        log.info("【Method Exit】waferLotGet()");
        return  new ObjectIdentifier(aPosLot.getIdentifier(), aPosLot.getPrimaryKey());
    }

    @Override
    public List<Infos.AliasWaferName> waferAliasNameGetDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> waferIDs) {
        List<Infos.AliasWaferName> aliasWaferNames = new ArrayList<>();
        for (ObjectIdentifier waferID : waferIDs) {
            CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, waferID);
            if (null != wafer) {
                aliasWaferNames.add(new Infos.AliasWaferName(new ObjectIdentifier(wafer.getIdentifier(), wafer.getPrimaryKey()), wafer.getAliasWaferName()));
            }
        }
        return aliasWaferNames;
    }

    @Override
    public void waferAssignedLotChangeForWaferSorter(Infos.ObjCommon objCommon,List<Infos.NewWaferAttributes> newWaferAttributesList) {
        log.info("【Method Entry】waferAssignedLotChangeForWaferSorter()");

        // Check input parameter. New lot of all wafers must be the same;
        int lenNewWaferAttr = CimArrayUtils.getSize(newWaferAttributesList);
        log.info( "Check input parameter. New lot of all wafers must be the same. lenNewWaferAttr = {}", lenNewWaferAttr);
        for (int i=0; i < lenNewWaferAttr; i++ ) {
            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);

            if ( i > 0 && !CimStringUtils.equals(newWaferAttributesList.get(0).getNewLotID().getValue(),
                    newWaferAttributes.getNewLotID().getValue()))
            {
                log.info( "newWaferAttributesList[0].newLotID == newWaferAttributesList[i].newLotID");
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
        }

        // Retrieve request user's object reference;
        log.info( "Retrieve request user's object reference");
        CimPerson aPosPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(aPosPerson),retCodeConfig.getNotFoundPerson());

        // Prepare object reference;
        log.info( "Prepare object reference");
        List<CimWafer> aPosWaferSeq = new ArrayList<>();
        for(int i=0; i<lenNewWaferAttr; i++) {
            // Check specified source wafer is exist or not
            log.info( "Check specified source wafer is exist or not");
            CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, newWaferAttributesList.get(i).getSourceWaferID());
            Validations.check(CimObjectUtils.isEmpty(wafer),retCodeConfig.getNotFoundWafer());
            aPosWaferSeq.add(wafer);
        }

        // Check specified source lot is exist or not
        log.info( "Check specified source lot is exist or not");

        List<ObjectIdentifier> aSourceLotIDSeq = new ArrayList<>();
        List<Integer> sourceLotWaferCountSeq = new ArrayList<>();
        List<Integer> sourceLotCtrlWaferCountSeq = new ArrayList<>();
        List<Integer> sourceLotProdWaferCountSeq = new ArrayList<>();

        int nTmpSourceLotLen = 0;
        for(int i=0; i<lenNewWaferAttr; i++) {
            Boolean bSourceLotRegFlag = false;
            if ( i > 0 ) {
                for(int j=0; j<nTmpSourceLotLen; j++) {
                    if(ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i).getSourceLotID(), aSourceLotIDSeq.get(j))) {
                        sourceLotWaferCountSeq.set(j, +1);
                        if(aPosWaferSeq.get(i).isControlWafer()){
                            sourceLotCtrlWaferCountSeq.set(j, +1);
                        } else {
                            sourceLotProdWaferCountSeq.set(j, +1);
                        }
                        bSourceLotRegFlag = true;
                        break;
                    }
                }
            }
            if ( bSourceLotRegFlag ) {
                continue;
            }

            nTmpSourceLotLen++;

            sourceLotWaferCountSeq.add(1);

            if(aPosWaferSeq.get(i).isControlWafer()) {
                sourceLotProdWaferCountSeq.add(0);
                sourceLotCtrlWaferCountSeq.add(1);
            } else {
                sourceLotProdWaferCountSeq.add(1);
                sourceLotCtrlWaferCountSeq.add(0);
            }

            aSourceLotIDSeq.add(newWaferAttributesList.get(i).getSourceLotID());
        }

        // Check specified target lot is exist or not
        log.info( "Check specified target lot is exist or not.");
        CimLot aTargetLot = baseCoreFactory.getBO(CimLot.class, newWaferAttributesList.get(0).getNewLotID());
        Validations.check(CimObjectUtils.isEmpty(aTargetLot),retCodeConfig.getNotFoundLot());

        //Update source lot's quantity info
        log.info( "Update source lot's quantity info");
        for (int i=0; i < nTmpSourceLotLen; i++ ) {
            CimLot aSourceLot = baseCoreFactory.getBO(CimLot.class, aSourceLotIDSeq.get(i));
            Validations.check(CimObjectUtils.isEmpty(aSourceLot),retCodeConfig.getNotFoundLot());
            int nSourceLotProdQty = aSourceLot.getProductQuantity();
            int nSourceLotCtrlQty = aSourceLot.getControlQuantity();
            int nSourceLotTotalQty = aSourceLot.getQuantity();

            Validations.check(sourceLotWaferCountSeq.get(i) > nSourceLotTotalQty,retCodeConfig.getInvalidInputParam());

            log.info(  "@@@@ setProductQuantity： {} @@@@", nSourceLotProdQty, sourceLotWaferCountSeq.get(i));
            aSourceLot.setProductQuantity(nSourceLotProdQty - sourceLotProdWaferCountSeq.get(i));

            log.info(  "@@@@ setControlQuantity ： {}@@@@", nSourceLotCtrlQty, sourceLotCtrlWaferCountSeq.get(i));
            aSourceLot.setControlQuantity(nSourceLotCtrlQty - sourceLotCtrlWaferCountSeq.get(i));

            log.info(  "@@@@ setVendorLotQuantity ： {}@@@@", nSourceLotTotalQty, sourceLotWaferCountSeq.get(i));
            aSourceLot.setVendorLotQuantity(nSourceLotTotalQty - sourceLotWaferCountSeq.get(i));

            if ( nSourceLotTotalQty == sourceLotWaferCountSeq.get(i)) {
                log.info( "nSourceLotTotalQty == sourceLotWaferCountSeq.get(i) : {}", sourceLotWaferCountSeq.get(i));
                CimLotFamily aLotFamily = aSourceLot.getLotFamily();
                Validations.check(CimObjectUtils.isEmpty(aLotFamily),retCodeConfig.getNotFoundLotFamily());

                Boolean bSourceLotFoundInCurrentLots = false;
                Boolean bSourceLotFoundInArchiveLots = false;
                List<Lot> aCurrentLotSeq = new ArrayList<>();
                List<Lot> anArchiveLotSeq = new ArrayList<>();
                String strSourceLotID = null;
                String strLotFamilyID = null;
                strSourceLotID =  aSourceLot.getIdentifier();
                strLotFamilyID = aLotFamily.getIdentifier();

                aCurrentLotSeq = aLotFamily.currentLots();
                anArchiveLotSeq = aLotFamily.archive();

                for (int j = 0; j < CimArrayUtils.getSize(aCurrentLotSeq); j++) {
                    String strListLotID = aCurrentLotSeq.get(j).getIdentifier();
                    if (CimStringUtils.equals(strSourceLotID, strListLotID)){
                        bSourceLotFoundInCurrentLots = true;
                        break;
                    }
                }
                for (int j = 0; j < CimArrayUtils.getSize(anArchiveLotSeq); j++) {
                    String strListLotID = anArchiveLotSeq.get(i).getIdentifier();
                    if (CimStringUtils.equals(strSourceLotID, strListLotID)){
                        bSourceLotFoundInArchiveLots = true;
                        break;
                    }
                }

                Validations.check(CimBooleanUtils.isTrue(bSourceLotFoundInCurrentLots) &&
                        CimBooleanUtils.isTrue(bSourceLotFoundInArchiveLots),retCodeConfig.getLotLotFamilyDataInvalid(),
                        strSourceLotID,strLotFamilyID);

                Validations.check(CimBooleanUtils.isFalse(bSourceLotFoundInCurrentLots) &&
                        CimBooleanUtils.isFalse(bSourceLotFoundInArchiveLots),retCodeConfig.getLotLotFamilyDataInvalid(),
                        strSourceLotID,strLotFamilyID);

                if( CimBooleanUtils.isFalse(bSourceLotFoundInCurrentLots) &&
                        CimBooleanUtils.isTrue(bSourceLotFoundInArchiveLots) )
                {
                    log.info(  "lot is in Archives List of lot Family !");
                    //Do nothing
                }
                if( CimBooleanUtils.isTrue(bSourceLotFoundInCurrentLots) &&
                        CimBooleanUtils.isFalse(bSourceLotFoundInArchiveLots) )
                {
                    log.info(  "lot is in currentLots List of lot Family !");
                    aLotFamily.archiveLot(aSourceLot);
                    aLotFamily.removeCurrentLot(aSourceLot);
                }

                aSourceLot.makeEmptied();

                String configEventCreateType = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();

                String inActiveLotEnabled = CimObjectUtils.toString(BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED);
                String enabled = CimObjectUtils.toString(BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED);
                if (CimStringUtils.equals(configEventCreateType, inActiveLotEnabled) ||
                        CimStringUtils.equals(configEventCreateType, enabled))
                {
                    CimProcessOperation aCurrentPO = aSourceLot.getProcessOperation();
                    if (aCurrentPO != null)
                    {
                        processMethod.poDelQueuePutDR(objCommon, new ObjectIdentifier(aSourceLot.getIdentifier(), aSourceLot.getPrimaryKey()));

                    }
                }
            }
        }

        //Calculate wafer's initial good die quantity;
        log.info( "Calculate wafer's initial good die quantity");
        CimProductSpecification aProductSpecification = aTargetLot.getProductSpecification();
        Validations.check(CimObjectUtils.isEmpty(aProductSpecification),retCodeConfig.getNotFoundProductSpec());

        CimProductGroup aProductGroup = aProductSpecification.getProductGroup();
        Validations.check(CimObjectUtils.isEmpty(aProductGroup),retCodeConfig.getNotFoundProductGroup());

        Long totalDiceQty =  aProductGroup.getGrossDieCount();
        Double plannedYield = aProductGroup.getPlannedYield();

        Double goodDiceQty = (Double.parseDouble(String.format("%d",totalDiceQty))) * (plannedYield / 100);

        // Update target lot's quantity info;
        log.info( "Update target lot's quantity info.");

        int nTargetLotProdQty =  aTargetLot.getProductQuantity();
        aTargetLot.setProductQuantity(nTargetLotProdQty + lenNewWaferAttr);

        int nTargetLotTotalQty = aTargetLot.getQuantity();
        aTargetLot.setVendorLotQuantity(nTargetLotTotalQty + lenNewWaferAttr);

        // Get lot's inventoryState for RecycleCount Management;
        Boolean onFloorFlag = aTargetLot.isOnFloor();

        //Check Request type for generating lot;
        log.info( "Check Request type for generating lot");
        String strLotType = aTargetLot.getLotType();

        // Update wafer-lot relation
        for (int i=0; i < lenNewWaferAttr; i++ ) {
            CimLot aSourceLot = null;
            log.info( "Update wafer-lot relation. Round : {}", i);
            MaterialContainer materialContainer = aPosWaferSeq.get(i).getMaterialContainer();
            CimCassette aPosCassette = (CimCassette) materialContainer;

            // Remove wafer from cassette
            log.info( "Remove wafer from cassette");
            int nPositionOfWafer = 0;
            if (aPosCassette == null) {
                log.info( "aPosCassette is null.");
                //Validations.check(retCodeConfig.getNotFoundCassette());
            } else {
                nPositionOfWafer = aPosWaferSeq.get(i).getPosition();
                Wafer tmpWafer = aPosCassette.removeWaferFromPosition(nPositionOfWafer);

            }

            // Remove wafer from lot;
            log.info( "Remove wafer from lot");

            Lot tmpLot = aPosWaferSeq.get(i).getLot();
            aSourceLot = (CimLot)tmpLot;
            // Initialize wafer object reference
            Validations.check(CimObjectUtils.isEmpty(aSourceLot),retCodeConfig.getNotFoundLot());

            try {
                aSourceLot.removeMaterial(aPosWaferSeq.get(i));
            } catch (ServiceException e) {
                if (CimStringUtils.equals(e.getMessage(), "Material Not Found Signal.")){
                    Validations.check(retCodeConfig.getNotFoundProductSpec());
                }
            }

            // Make wafer not allocated
            log.info( "Make wafer not allocated");
            aPosWaferSeq.get(i).makeNotSTBAllocated();

            // set wafer ID and Name
            // (This section must be executed before 'lot.getaddMaterial()'
            log.info( "set wafer ID and Name");

            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);
            if (!CimObjectUtils.isEmpty(newWaferAttributes.getNewWaferID().getValue())
                    && !CimStringUtils.equals(newWaferAttributes.getNewWaferID().getValue(),
                    newWaferAttributes.getSourceWaferID().getValue())) {
                log.info( "call!! theProductManager.getresetWaferIdentifier");
                aPosWaferSeq.get(i).setIdentifier(newWaferAttributes.getNewWaferID().getValue());
            }

            // assign new lot to wafer
            // By 'addMaterial', wafer's product specification
            // information is changed automatically .
            log.info( "assign new lot to wafer");
            aTargetLot.addMaterial(aPosWaferSeq.get(i));

            // Update wafer attribute for new lot
            log.info( "Update wafer attribute for new lot");

            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, strLotType) ||
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, strLotType) ||
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_ENGINEERINGLOT, strLotType) ||
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, strLotType)) {
                log.info( "call!! aPosWafer.getmakeControlWafer");
                aPosWaferSeq.get(i).makeControlWafer();
            } else if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_RECYCLELOT, strLotType)) {
                log.info( "strLotType == SP_Lot_Type_RecycleLot");
                aPosWaferSeq.get(i).makeControlWafer();

                if ( CimBooleanUtils.isTrue(onFloorFlag) ) {
                    log.info(  "lot is 'OnFloor'... {}", i);
                    //new requirment, only recycle lot stb will add the recycle Count
//                    aPosWaferSeq.get(i).setRecycleCount(aPosWaferSeq.get(i).getRecycleCount() + 1);
                }
            } else if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, strLotType) ||
                    CimStringUtils.equals(BizConstant.SP_LOT_TYPE_VENDORLOT, strLotType)) {
                log.info("ForWaferSorter , strLotType == 'Production' or 'Vendor'");
                aPosWaferSeq.get(i).makeControlWafer();
            }

            // set previous lot info into wafer
            log.info( "set previous lot info into wafer");
            aPosWaferSeq.get(i).setPreviousLot(aSourceLot);

            // set wafer's Dice Quantity Information
            log.info( "set wafer's Dice Quantity Information.");
            aPosWaferSeq.get(i).setTotalDiceQuantity(totalDiceQty.intValue());
            aPosWaferSeq.get(i).setGoodDiceQuantity(goodDiceQty.intValue());

            // set wafer last claimed timestamp
            aPosWaferSeq.get(i).setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

            // set wafer last claimed person
            aPosWaferSeq.get(i).setLastClaimedPerson(aPosPerson);

            // set source lot last claimed timestamp
            aSourceLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

            // set source lot last claimed person
            aSourceLot.setLastClaimedPerson(aPosPerson);

            // set target lot last claimed timestamp
            aTargetLot.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

            // set target lot last claimed person
            aTargetLot.setLastClaimedPerson(aPosPerson);

            if ( aPosCassette != null) {
                // Add wafer to cassette
                log.info( "Add wafer to cassette");
                aPosCassette.addWaferAtPosition(aPosWaferSeq.get(i),nPositionOfWafer);
            }
        }

        log.info("【Method Exit】waferAssignedLotChangeForWaferSorter()");

    }

    @Override
    public void waferSorterSorterJobCheckForOperation(Infos.ObjCommon objCommon, Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation) {
        boolean checkFailFlag = false;
        ObjectIdentifier objectIDForReturnMsg = null;
        if(CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, objWaferSorterJobCheckForOperation.getOperation())
        || CimStringUtils.equals(BizConstant.SP_OPERATION_LOADINGLOT, objWaferSorterJobCheckForOperation.getOperation())){
            /**************************************/
            /* Get cassette sorterJob information */
            /**************************************/
            List<Infos.CassetteLoadPort> cassetteLoadPortList = objWaferSorterJobCheckForOperation.getEquipmentLoadPortAttribute().getCassetteLoadPortList();
            if(CimArrayUtils.isNotEmpty(cassetteLoadPortList)){
                for (Infos.CassetteLoadPort cassetteLoadPort : cassetteLoadPortList) {
                    Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn = new Info.ObjectValidSorterJobGetIn();
                    objectValidSorterJobGetIn.setClassification(BizConstant.SP_CLASSNAME_POSCASSETTE);
                    objectValidSorterJobGetIn.setObjectID(cassetteLoadPort.getCassetteID());
                    Info.ObjObjectValidSorterJobGetOut objectValidSorterJob = objectMethod.objectValidSorterJobGet(objCommon, objectValidSorterJobGetIn);
                    boolean matchEqpAndPort =false;
                    //Check sorter job validity for input parameters
                    List<Info.SortJobListAttributes> validSorterJobList = objectValidSorterJob.getStrValidSorterJob();
                    List<Info.SortJobListAttributes> otherSorterJobList = objectValidSorterJob.getStrOtherSorterJob();
                    if(CimArrayUtils.isNotEmpty(validSorterJobList)){
                        for (Info.SortJobListAttributes sortJobListAttributes : validSorterJobList) {
                            if(!CimStringUtils.equals(sortJobListAttributes.getEquipmentID().getValue(), objWaferSorterJobCheckForOperation.getEquipmentLoadPortAttribute().getEquipmentID().getValue())){
                                objectIDForReturnMsg=ObjectIdentifier.buildWithValue(
                                        objWaferSorterJobCheckForOperation.getEquipmentLoadPortAttribute().getEquipmentID()+" device error");
                                checkFailFlag = true;
                                break;
                            }
                            List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = sortJobListAttributes.getSorterComponentJobListAttributesList();
                            if(CimArrayUtils.isNotEmpty(sorterComponentJobListAttributesList)){
                                for (Info.SorterComponentJobListAttributes sorterComponentJobListAttributes : sorterComponentJobListAttributesList) {
                                    if((CimStringUtils.equals(sorterComponentJobListAttributes.getOriginalPortID().getValue(),cassetteLoadPort.getPortID().getValue()) &&
                                            CimStringUtils.equals(sorterComponentJobListAttributes.getOriginalCarrierID().getValue(),cassetteLoadPort.getCassetteID().getValue())) ||
                                            (CimStringUtils.equals(sorterComponentJobListAttributes.getDestinationPortID().getValue(),cassetteLoadPort.getPortID().getValue()) &&
                                                    CimStringUtils.equals(sorterComponentJobListAttributes.getDestinationCarrierID().getValue(),cassetteLoadPort.getCassetteID().getValue()))){
                                        matchEqpAndPort = true;
                                        break;
                                    }
                                }
                            }
                            if(CimBooleanUtils.isFalse(matchEqpAndPort)){
                                objectIDForReturnMsg=ObjectIdentifier.buildWithValue("Carrier does not match port");
                                checkFailFlag = true;
                                break;
                            }
                        }
                    } else if (CimArrayUtils.isNotEmpty(otherSorterJobList)) {
                        objectIDForReturnMsg=ObjectIdentifier.buildWithValue("Carrier does not create a Sort Job ");
                        checkFailFlag = true;
                        break;
                    } else {
                        // Do nothing...
                    }
                    if(CimBooleanUtils.isTrue(checkFailFlag)){
                        break;
                    }
                }
            }
        } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, objWaferSorterJobCheckForOperation.getOperation())
                || CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, objWaferSorterJobCheckForOperation.getOperation())
                || CimStringUtils.equals(BizConstant.SP_OPERATION_FOR_CAST, objWaferSorterJobCheckForOperation.getOperation())) {
            //Get cassette sorterJob information
            List<ObjectIdentifier> cassetteIDList = objWaferSorterJobCheckForOperation.getCassetteIDList();
            if (CimArrayUtils.isNotEmpty(cassetteIDList)) {
                for (ObjectIdentifier cassetteID : cassetteIDList) {
                    objectIDForReturnMsg = cassetteID;

                    Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn = new Info.ObjectValidSorterJobGetIn();
                    objectValidSorterJobGetIn.setClassification(BizConstant.SP_CLASSNAME_POSCASSETTE);
                    objectValidSorterJobGetIn.setObjectID(cassetteID);
                    Info.ObjObjectValidSorterJobGetOut objectValidSorterJob = objectMethod.objectValidSorterJobGet(objCommon, objectValidSorterJobGetIn);

                    /****************************************************/
                    /*  Check sorter job validity for input parameters  */
                    /****************************************************/
                    if (CimArrayUtils.isNotEmpty(objectValidSorterJob.getStrValidSorterJob()) || CimArrayUtils.isNotEmpty(objectValidSorterJob.getStrOtherSorterJob())) {
                        checkFailFlag = true;
                        break;
                    }
                }
            }
        } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_FOR_DESTCAST, objWaferSorterJobCheckForOperation.getOperation())) {
            //Get cassette sorterJob information
            List<ObjectIdentifier> cassetteIDList = objWaferSorterJobCheckForOperation.getCassetteIDList();
            if (CimArrayUtils.isNotEmpty(cassetteIDList)) {
                for (ObjectIdentifier cassetteID : cassetteIDList) {
                    Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn = new Info.ObjectValidSorterJobGetIn();
                    objectValidSorterJobGetIn.setClassification(BizConstant.SP_CLASSNAME_POSCASSETTE);
                    objectValidSorterJobGetIn.setObjectID(cassetteID);
                    Info.ObjObjectValidSorterJobGetOut objectValidSorterJob = objectMethod.objectValidSorterJobGet(objCommon, objectValidSorterJobGetIn);
                    //Check sorter job validity for input parameters
                    List<Info.SortJobListAttributes> validSorterJobList = objectValidSorterJob.getStrValidSorterJob();
                    if (CimArrayUtils.isNotEmpty(validSorterJobList)) {
                        for (Info.SortJobListAttributes sortJobListAttributes : validSorterJobList) {
                            List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = sortJobListAttributes.getSorterComponentJobListAttributesList();
                            if (CimArrayUtils.isNotEmpty(sorterComponentJobListAttributesList)) {
                                for (Info.SorterComponentJobListAttributes sorterComponentJobListAttributes : sorterComponentJobListAttributesList) {
                                    if (CimStringUtils.equals(sorterComponentJobListAttributes.getDestinationCarrierID().getValue(), cassetteID.getValue())) {
                                        checkFailFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (CimBooleanUtils.isTrue(checkFailFlag)) {
                                break;
                            }
                        }
                    }
                    if (CimBooleanUtils.isFalse(checkFailFlag)) {
                        //Check sorter job validity for input parameters
                        List<Info.SortJobListAttributes> otherSorterJobList = objectValidSorterJob.getStrOtherSorterJob();
                        if (CimArrayUtils.isNotEmpty(otherSorterJobList)) {
                            for (Info.SortJobListAttributes sortJobListAttributes : otherSorterJobList) {
                                List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = sortJobListAttributes.getSorterComponentJobListAttributesList();
                                if (CimArrayUtils.isNotEmpty(sorterComponentJobListAttributesList)) {
                                    for (Info.SorterComponentJobListAttributes sorterComponentJobListAttributes : sorterComponentJobListAttributesList) {
                                        if (CimStringUtils.equals(sorterComponentJobListAttributes.getDestinationCarrierID().getValue(), cassetteID.getValue())) {
                                            checkFailFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (CimBooleanUtils.isTrue(checkFailFlag)) {
                                    break;
                                }
                            }
                        }
                    }
                    if (CimBooleanUtils.isTrue(checkFailFlag)) {
                        break;
                    }
                }
            }
        } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_FOR_LOT, objWaferSorterJobCheckForOperation.getOperation())) {
            //Get lot sorterJob information
            List<ObjectIdentifier> lotIDList = objWaferSorterJobCheckForOperation.getLotIDList();
            if (CimArrayUtils.isNotEmpty(lotIDList)) {
                for (ObjectIdentifier lotId : lotIDList) {
                    objectIDForReturnMsg = lotId;
                    Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn = new Info.ObjectValidSorterJobGetIn();
                    objectValidSorterJobGetIn.setClassification(BizConstant.SP_CLASSNAME_POSLOT);
                    objectValidSorterJobGetIn.setObjectID(lotId);
                    Info.ObjObjectValidSorterJobGetOut objectValidSorterJob = objectMethod.objectValidSorterJobGet(objCommon, objectValidSorterJobGetIn);
                    //Check sorter job validity for input parameters
                    if (CimArrayUtils.isNotEmpty(objectValidSorterJob.getStrOtherSorterJob()) || CimArrayUtils.isNotEmpty(objectValidSorterJob.getStrValidSorterJob())) {
                        checkFailFlag = true;
                        break;
                    }
                }
            }
        } else {
            throw new ServiceException(retCodeConfig.getSorterValidityNotChecked());
        }
        if (checkFailFlag) {
            String returnClass = null;
            if (CimStringUtils.equals(objWaferSorterJobCheckForOperation.getOperation(), BizConstant.SP_OPERATION_FOR_LOT)){
                returnClass = BizConstant.SP_OPERATION_FOR_LOT;
            } else {
                returnClass = BizConstant.SP_OPERATION_FOR_CAST;
            }
            throw new ServiceException(new OmCode(retCodeConfig.getPreventedBySorterJob(), returnClass, ObjectIdentifier.fetchValue(objectIDForReturnMsg)));
        }

    }

    @Override
    public List<Infos.WaferSorterSlotMap> waferSorterCheckConditionForAction(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String actionCode, List<Infos.WaferSorterSlotMap> waferSorterSlotMaps, String portGroup, String physicalRecipeID) {
        //---------------------
        // Initialize
        //---------------------
        int WaferSorterSlotMapSequenceLen = CimArrayUtils.getSize(waferSorterSlotMaps);

        //----------------------------
        // No Parameters are specified
        //--------------------------------------------------
        // If ActionCode is not SP_Sorter_End, then error
        //--------------------------------------------------
        Validations.check(WaferSorterSlotMapSequenceLen == 0 && !CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_END), retCodeConfig.getInvalidParameter());

        log.info("Length of waferSorterSlotMaps is : {}" , WaferSorterSlotMapSequenceLen );

        //=======================================================
        // In-Parameter check routine
        // --------------------------
        // These Parameters are verified
        //     actionCode/equipmentID/equipment category/portGroup
        //=======================================================
        //----------------------------------------------------------------------------------
        //    Check ActionCode is valid
        //    => SP_Sorter_Read/SP_Sorter_MiniRead/SP_Sorter_PositionChange/SP_Sorter_JustIn
        //       SP_Sorter_JustOut/SP_Sorter_Scrap/SP_Sorter_End is only valid
        //----------------------------------------------------------------------------------
        Validations.check (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_READ) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_START) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_MINIREAD) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_POSITIONCHANGE) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_LOTTRANSFEROFF) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_SEPARATEOFF) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_COMBINEOFF) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_JUSTIN) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_JUSTOUT) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_WAFERENDOFF) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_SCRAP) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_ADJUSTTOMM) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_ADJUSTTOSORTER) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_AUTOSORTING) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_LOTTRANSFER) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_WAFEREND) &&
                !CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_END), retCodeConfig.getInvalidActionCode());

        log.info("waferSorter_CheckConditionForAction Specified ActionCode is : {}",actionCode);

        //----------------------------------------------------------------------------------
        //    Check Equipment Category
        //    => SP_Mc_Category_WaferSorter is only valid
        //----------------------------------------------------------------------------------
        log.info("Getting Equipment Category Information ");

        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aPosMachine, retCodeConfig.getNotFoundEqp(), objCommon.getTransactionID());

        String strMachneCategory = aPosMachine.getCategory();

        log.info("Check Equipment Category");
        Validations.check( !CimStringUtils.equals(strMachneCategory,BizConstant.SP_MC_CATEGORY_WAFERSORTER),retCodeConfig.getMachineTypeNotSorter());

        //-------------------------------------------------------
        //    Check PortGroup
        //    => PortGroupame should be same as inpara
        //-------------------------------------------------------
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());

        int i;
        int j;
        int k;
        int l;
        ObjectIdentifier operationModeID = null;
        ObjectIdentifier portID = null;

        for ( i=0; i < lenPortInfo ; i++ ) {
            // ---------------------------------------------
            // Check On-Line Mode of Port
            // ---------------------------------------------
            Validations.check(i == 0 && !CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(i).getOnlineMode(),
                    BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getInvalidPortState());

            // ---------------------------------------------
            // Check In-Para Port Group is on EQP Port Group
            // ---------------------------------------------
            if(CimStringUtils.equals( eqpPortInfo.getEqpPortStatuses().get(i).getPortGroup(),portGroup)) {
                log.info("Found the portGroup : {}", portGroup);
                operationModeID =  eqpPortInfo.getEqpPortStatuses().get(i).getOperationModeID(); //PSIV00000956
                portID =  eqpPortInfo.getEqpPortStatuses().get(i).getPortID();                   //PSIV00000956
                break;
            }

            //--------------------
            // Case in Final Loop
            //--------------------
            Validations.check(i == (lenPortInfo - 1), retCodeConfigEx.getPortPortgroupUnmatch());
        }

        if(CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_WAFEREND)) {
            log.info("actionCode = SP_Sorter_AutoSorting");
            Validations.check ( !ObjectIdentifier.equalsWithValue(operationModeID, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)
                    && !ObjectIdentifier.equalsWithValue(operationModeID, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2), retCodeConfigEx.getInvalidPortOperationMode(),portID,operationModeID);

        } else {
            log.info("actionCode = {}", actionCode);
            Validations.check(ObjectIdentifier.equalsWithValue(operationModeID, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1) ||
                    ObjectIdentifier.equalsWithValue(operationModeID, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2) ||
                    ObjectIdentifier.equalsWithValue(operationModeID, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3), retCodeConfigEx.getInvalidPortOperationMode(),portID,operationModeID);
        }

        //-------------------------------------------------------
        // Check PhysicalRecipeID
        // => physicalRecipeID should be input except SP_Sorter_End
        //-------------------------------------------------------
        log.info("Check PhysicalRecipeID");
        Validations.check(CimStringUtils.isEmpty(physicalRecipeID) && !CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_END) &&
                !CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_ADJUSTTOSORTER), retCodeConfigEx.getNotFoundPhysicalRecipe());

        if(CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_WAFEREND)) {
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
            objSorterJobListGetDRIn.setEquipmentID(equipmentID);
            List<Info.SortJobListAttributes>  strSorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
            Validations.check(CimArrayUtils.isEmpty(strSorterJobListGetDROut), retCodeConfigEx.getNotFoundSorterjob());
        }


        //------------------------------------
        //   Check equipment availability
        //------------------------------------
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);

        //======================================================
        //    Each Check of Structure by ActionCode
        //======================================================
        int    lotFoundCount = 0;
        List<CimLot> aPosLotSeq = new ArrayList<>();
        List<String> cassetteIDs = new ArrayList<>();
        List<String> lotIDs = new ArrayList<>();
        Boolean waferFound = false;
        CimWafer aPosWafer;

        // --------------------------------------------------------------
        // Check All Data in Structure Sequence
        // --------------------------------------------------------------
        for( i = 0; i < WaferSorterSlotMapSequenceLen ; i++) {
            log.info("Loop Count is : {}",i);
            log.info("portGroup : {}",waferSorterSlotMaps.get(i).getPortGroup());
            log.info("equipmentID: {}",waferSorterSlotMaps.get(i).getEquipmentID());
            log.info("actionCode : {}",waferSorterSlotMaps.get(i).getActionCode());
            log.info("requestTime : {}",waferSorterSlotMaps.get(i).getRequestTime());
            log.info("direction : {}",waferSorterSlotMaps.get(i).getDirection());
            log.info("waferID : {}",waferSorterSlotMaps.get(i).getWaferID());
            log.info("lotID : {}",waferSorterSlotMaps.get(i).getLotID());
            log.info("destinationCassetteID : {}",waferSorterSlotMaps.get(i).getDestinationCassetteID());
            log.info("destinationPortID : {}",waferSorterSlotMaps.get(i).getDestinationPortID());
            if(waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM()) {
                log.info("bDestinationCassetteManagedBySiView: {}","TRUE");
            } else {
                log.info("bDestinationCassetteManagedBySiView: {}","false");
            }
            log.info("destinationSlotNumber : {}",waferSorterSlotMaps.get(i).getDestinationSlotNumber());
            log.info("originalCassetteID : {}",waferSorterSlotMaps.get(i).getOriginalCassetteID());
            log.info("originalPortID : {}",waferSorterSlotMaps.get(i).getOriginalPortID());
            if(waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM()) {
                log.info("bOriginalCassetteManagedBySiView: {}","TRUE");
            } else {
                log.info("bOriginalCassetteManagedBySiView : {}","false");
            }
            log.info("originalSlotNumber ： {}",waferSorterSlotMaps.get(i).getOriginalSlotNumber());
            log.info("requestUserID : {}",waferSorterSlotMaps.get(i).getRequestUserID());
            log.info("replyTime : {}",waferSorterSlotMaps.get(i).getReplyTime());
            log.info("sorterStatus : {}",waferSorterSlotMaps.get(i).getSorterStatus());
            log.info("slotMapCompareStatus : {}",waferSorterSlotMaps.get(i).getSlotMapCompareStatus());
            log.info("mmCompareStatus  : {}",waferSorterSlotMaps.get(i).getOmsCompareStatus());

            //=====================================================================================
            //    Common Check
            //=====================================================================================

            //--------------------------------------------------
            // Compare In-para variable and structure variable
            //--------------------------------------------------
            //---------------------------------
            // 1. PortGroup Check
            //---------------------------------
            Validations.check(!CimStringUtils.equals( waferSorterSlotMaps.get(i).getPortGroup(), portGroup ), retCodeConfigEx.getPortPortgroupUnmatch());

            log.info( "PortGroup Check is OK");
            //---------------------------------
            // 2. Equipment ID Check
            //---------------------------------
            Validations.check(!ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getEquipmentID(), equipmentID), retCodeConfigEx.getEquipmentUnmatch());
            log.info("Equipment ID Check is OK");

            //---------------------------------
            // 3. ActionCode Check
            //---------------------------------
            Validations.check(!CimStringUtils.equals( waferSorterSlotMaps.get(i).getActionCode(), actionCode ),retCodeConfigEx.getActioncodeUnmatch());
            log.info( "ActionCode Check is OK");

            //--------------------------------------------------
            // OTHER Checks
            //--------------------------------------------------
            //---------------------------------
            // 4. Direction
            //---------------------------------
            Validations.check( !CimStringUtils.equals( waferSorterSlotMaps.get(i).getDirection(), BizConstant.SP_SORTER_DIRECTION_MM),retCodeConfigEx.getInvalidDirection());
            log.info( "Direction Check is OK");

            //---------------------------------
            // 5. requestUserID Check
            //---------------------------------
            Validations.check(!ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getRequestUserID(), objCommon.getUser().getUserID()), retCodeConfigEx.getUseridUnmatch());
            log.info( "UserID Check is OK");

            //---------------------------------
            // 6. SorterStatus Check
            //---------------------------------
            Validations.check(!CimStringUtils.equals( waferSorterSlotMaps.get(i).getSorterStatus(), BizConstant.SP_SORTER_REQUESTED), retCodeConfigEx.getInvalidSorterstatus());
            log.info( "SorterStatus Check is OK");

            //---------------------------------------------------------------------------------------------
            // Destination SlotNumber Check
            //---------------------------------------------------------------------------------------------
            Boolean cassetteFound = true;
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, waferSorterSlotMaps.get(i).getDestinationCassetteID());
            if (null == aCassette) {
                cassetteFound = false;
            }
            Boolean destinationCassetteFound = cassetteFound;

            //=================================
            //    Check by ActionCode
            //=================================
            //---------------------------------------------------------------------------------------------
            // ActionCode : SP_Sorter_Read/SP_Sorter_MiniRead/SP_Sorter_End
            //---------------------------------------------------------------------------------------------
            //    Checking Items :
            //        bDestinationCassetteManagedBySiView == TRUE)
            //           destinationCassetteID must exist in MM
            //        bDestinationCassetteManagedBySiView == false)
            //           size of destinationCassetteID is bigger than 0
            //        MiniRead)
            //           destinationSlotNumber must be in 1 to theSP_EnvName_MaximumWafersInALot
            //--------------------------------------------------------------------------------------------
            if( ( CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_READ)) ||
                    ( CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_START))  ||
                    ( CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_MINIREAD))  ||
                    ( CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_END))) {
                // -------------------------------
                // Case MM Known Carriers
                // -------------------------------
                if( CimBooleanUtils.isTrue(waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM())) {
                    Validations.check(CimBooleanUtils.isFalse(destinationCassetteFound), retCodeConfigEx.getInvalidRegisteredCarrier());

                }
                // -------------------------------
                // Case MM Unknown Carriers
                // -------------------------------
                else {
                    Validations.check(CimBooleanUtils.isTrue(destinationCassetteFound) || null == waferSorterSlotMaps.get(i).getDestinationCassetteID(),
                            retCodeConfigEx.getInvalidUnregisteredCarrier());

                }
                // -------------------------------------------------------------------
                // If ActionCode is SP_Sorter_MiniRead, SlotNumber should be specified
                // -------------------------------------------------------------------
                if( CimStringUtils.equals(actionCode,BizConstant.SP_SORTER_MINIREAD)) {
                    String maximumWafersInALotStr = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
                    int maximumWafersInALot = null == maximumWafersInALotStr ? 0 : Integer.valueOf(maximumWafersInALotStr);
                    Validations.check( (0 > waferSorterSlotMaps.get(i).getDestinationSlotNumber()) ||
                            (waferSorterSlotMaps.get(i).getDestinationSlotNumber() > maximumWafersInALot), retCodeConfigEx.getInvalidSlotnumberForWaferidminiread());
                }
                log.info( "check for Read  / MiniRead / End is OK");
            }
            //-----------------------------------------------------------------------------------------
            // ActionCode : SP_Sorter_PositionChange/SP_Sorter_JustOut/SP_Sorter_JustIn/SP_Sorter_Scrap
            //-----------------------------------------------------------------------------------------
            else {
                // -------------------------------------------------------------
                // WaferID Existence Check
                //   => WaferID Should be existed
                // -------------------------------------------------------------
                aPosWafer = baseCoreFactory.getBO(CimWafer.class,waferSorterSlotMaps.get(i).getWaferID());
                Validations.check(null == aPosWafer, retCodeConfig.getNotFoundWafer(), objCommon.getTransactionID());

                // -------------------------------------------------------------
                // Getting Wafers
                // => Then Error
                // -------------------------------------------------------------
                log.info( "Checking Current Carrier Information");
                MaterialContainer aMtrlCntnr = aPosWafer.getMaterialContainer();
                CimCassette aCurCassette = (CimCassette) aMtrlCntnr;

                //--------------------------------------------------------------
                // If Carrier information is not found then Error except Just-IN
                //--------------------------------------------------------------
                log.info( "Carrier Information Copy");
                String strCurrentCassetteID;
                int nCurPosition;
                if(null == aCurCassette){
                    Validations.check( !CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTIN), retCodeConfig.getNotFoundCassette());
                    nCurPosition = 0;
                } else {
                    // -------------------------------------------------------------
                    // Getting Current Carrier Information
                    // -------------------------------------------------------------
                    strCurrentCassetteID = aCurCassette.getIdentifier();
                    cassetteIDs.add(strCurrentCassetteID );

                    // -------------------------------------------------------------
                    // Getting Current SlotPosition Information
                    // -------------------------------------------------------------
                    log.info("GetPosition Information");
                    nCurPosition = aPosWafer.getPosition();
                }

                // -------------------------------------------------------------
                // GetLotID for Future Check
                // -------------------------------------------------------------
                log.info( "Get Lot Information ");
                CimLot aTmpLot = (CimLot)aPosWafer.getLot();

                log.info( "Checking Temp Lot ");
                Validations.check(null == aTmpLot,retCodeConfig.getNotFoundLot());

                aPosLotSeq.add(aTmpLot);

                Validations.check(null == aPosLotSeq.get(i),retCodeConfig.getNotFoundLot());

                lotIDs.add(aPosLotSeq.get(i).getIdentifier());

                log.info( "LotID is {}",lotIDs.get(i));

                //---------------------------------------------------------------------------------------------
                // Destination SlotNumber Check
                //---------------------------------------------------------------------------------------------
                String maximumWafersInALotStr = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
                int maximumWafersInALot = null == maximumWafersInALotStr ? 0 : Integer.valueOf(maximumWafersInALotStr);
                Validations.check( (0 > waferSorterSlotMaps.get(i).getDestinationSlotNumber())||
                        (waferSorterSlotMaps.get(i).getDestinationSlotNumber() > maximumWafersInALot), retCodeConfigEx.getInvalidWaferPosition());

                Validations.check((CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_AUTOSORTING)||
                        CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_LOTTRANSFER)||
                        CimStringUtils.equals( actionCode , BizConstant.SP_SORTER_WAFEREND)) && ( (1 > waferSorterSlotMaps.get(i).getDestinationSlotNumber())||
                        (waferSorterSlotMaps.get(i).getDestinationSlotNumber() > maximumWafersInALot)), retCodeConfigEx.getInvalidWaferPosition());

                //---------------------------------------
                // Getting Original Cassette Information
                //---------------------------------------
                cassetteFound = true;
                Boolean originalCassetteFound = false;
                aCassette = baseCoreFactory.getBO(CimCassette.class,waferSorterSlotMaps.get(i).getOriginalCassetteID());
                if (null == aCassette) {
                    cassetteFound = false;
                }
                originalCassetteFound = cassetteFound;

                //---------------------------------------------------------------------------------------------
                // PositionChange
                //---------------------------------------------------------------------------------------------
                //    Checking Items :
                //        All bDestinationCassetteManagedBySiView/bOriginalCassetteManagedBySiView must be TRUE
                //                         | Original         |     Destination
                //             ------------+------------------+-------------------
                //             SiView Flag |     TRUE         |        TRUE
                //             Cassette    | MM Registered    |    MM Registered
                //
                //---------------------------------------------------------------------------------------------
                if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_POSITIONCHANGE) ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFEROFF)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_SEPARATEOFF)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_COMBINEOFF)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_ADJUST_TO_MM)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_AUTOSORTING)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFER)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFEREND)  ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_ADJUSTTOSORTER) ) {
                    Validations.check( !waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM()  || !destinationCassetteFound ||
                            !waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM() || !originalCassetteFound, retCodeConfigEx.getInvalidSiviewflagForWaferidposchange());
                    log.info( "check for PositionChange is OK");
                }

                //---------------------------------------------------------------------------------------------
                // JustIn
                //---------------------------------------------------------------------------------------------
                //    Checking Items :
                //        bDestinationCassetteManagedBySiView must be TRUE
                //        bOriginalCassetteManagedBySiView    must be false
                //                         | Original         |     Destination
                //             ------------+------------------+-------------------
                //             SiView Flag |     false        |        TRUE
                //             Cassette    | MM Un-Registered |    MM Registered
                //---------------------------------------------------------------------------------------------
                if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTIN)) {
                    Validations.check( !waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM()   || !destinationCassetteFound  ||  waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM() || originalCassetteFound , retCodeConfigEx.getInvalidSiviewflagForJustin());
                    log.info( "check for JustInis OK");
                }

                //---------------------------------------------------------------------------------------------
                // SP_Sorter_JustOut
                //---------------------------------------------------------------------------------------------
                //    Checking Items :
                //        bDestinationCassetteManagedBySiView must be TRUE
                //        bOriginalCassetteManagedBySiView    must be false
                //---------------------------------------------------------------------------------------------
                if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTOUT)||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFERENDOFF)) {
                    Validations.check( waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM() || destinationCassetteFound ||
                            !waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM() || !originalCassetteFound, retCodeConfigEx.getInvalidSiviewflagForJustout());
                    log.info( "check for SP_Sorter_JustOut/SP_Sorter_Scrap OK");
                }

                //---------------------------------------------------------------------------------------------
                // SP_Sorter_JustOut/SP_Sorter_Scrap
                //---------------------------------------------------------------------------------------------
                //    Checking Items :
                //        bDestinationCassetteManagedBySiView must be TRUE
                //        bOriginalCassetteManagedBySiView    must be false
                //---------------------------------------------------------------------------------------------

            }
            // ------------------------------------------------------------------------
            // Carrier/Port/Slot Duplicate Check
            // Carrier/Port/Slot should be unique because each slot can have only one wafer
            // ------------------------------------------------------------------------
            for(j=0;j<WaferSorterSlotMapSequenceLen;j++) {
                // No Check Same Data
                if( i==j ) {
                    continue;
                }
                //---------------------------------------------------------
                // Check Destination Information
                // ActionCode : ALL
                //---------------------------------------------------------
                // -------------------
                // Case PortID is Same
                // -------------------
                if(ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getDestinationPortID(),
                        waferSorterSlotMaps.get(j).getDestinationPortID())) {
                    // --------------------
                    // Case Carrier is same
                    // --------------------
                    if(ObjectIdentifier.equals(waferSorterSlotMaps.get(i).getDestinationCassetteID(),
                            waferSorterSlotMaps.get(j).getDestinationCassetteID())) {
                        // -----------------------------------
                        // If Slot Number is same then Error
                        // -----------------------------------
                        Validations.check( waferSorterSlotMaps.get(i).getDestinationSlotNumber().intValue() ==  waferSorterSlotMaps.get(j).getDestinationSlotNumber().intValue(), retCodeConfigEx.getDuplicateLocation());

                    }
                    // --------------------------------------------
                    // Carrier is not same on same port then error
                    // --------------------------------------------
                    else {
                        log.info(" waferSorter_CheckConditionForAction, Different Destination CARRIER  specified in same Port ");
                        throw new ServiceException(retCodeConfigEx.getDifferentCarrierInPort());

                    }
                }
                // -------------------------
                // Case Port is not Same
                // -------------------------
                else {
                    // -------------------------
                    // Case Carrier is not Same
                    // -------------------------
                    Validations.check(ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getDestinationCassetteID(),
                            waferSorterSlotMaps.get(j).getDestinationCassetteID()), retCodeConfigEx.getDuplicateCarrier());

                }

                //---------------------------------------------------------
                // Check Original Information
                // ActionCode : PositionChange/JustIn/JustOut/Scrap
                //---------------------------------------------------------
                if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_POSITIONCHANGE) ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFEROFF)        ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_SEPARATEOFF)        ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_COMBINEOFF)        ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTOUT)        ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFERENDOFF)        ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTIN)         ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_ADJUSTTOMM)     ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_ADJUSTTOSORTER) ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_AUTOSORTING)    ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFER)    ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFEREND)    ||
                        CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_SCRAP)) {
                    if(ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getOriginalPortID(),
                            waferSorterSlotMaps.get(j).getOriginalPortID())) {
                        // --------------------
                        // Case Carrier is same
                        // --------------------
                        if(ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getOriginalCassetteID(), waferSorterSlotMaps.get(j).getOriginalCassetteID())) {
                            // -----------------------------------
                            // If Slot Number is same then Error
                            // -----------------------------------
                            Validations.check( waferSorterSlotMaps.get(i).getOriginalSlotNumber().intValue() ==  waferSorterSlotMaps.get(j).getOriginalSlotNumber().intValue() , retCodeConfigEx.getDuplicateLocation());
                        }
                        // --------------------------------------------
                        // Carrier is not same on same port then error
                        // --------------------------------------------
                        else {
                            log.info(" waferSorter_CheckConditionForAction, Different Original CARRIER  specified in same Port ");
                            throw new ServiceException(retCodeConfigEx.getDuplicateCarrier());
                        }
                    }
                    // -------------------------
                    // Case Carrier is not Same
                    // -------------------------
                    else {
                        Validations.check(ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getOriginalCassetteID(),
                                waferSorterSlotMaps.get(j).getOriginalCassetteID()), retCodeConfigEx.getDifferentCarrierInPort());
                    }
                }
            }
        }  // End of for loop

        //=========================================================================
        // Prepare AnyData for sorter_waferTransferInfo_Verify
        // because it needs all waferdata in specified lot
        // So this routine adds waferinformation to strWaferTransferSequence
        // Invoke it only case PositionChange/JustIn/JustOut/Scrap
        //=========================================================================
        //---------------------------------------------------------------------------------------------
        // PositionChange/JustIn/JustOut/Scrap
        //---------------------------------------------------------------------------------------------
        //    Checking Items :
        //        Send Parameters to sorter_waferTransferInfo_Verify and get return code
        //---------------------------------------------------------------------------------------------
        //-----------------------------------------------------
        // Preparation for Sorter transfer buffer
        //-----------------------------------------------------
        List<Infos.WaferTransfer> strWaferTransferSequence = new ArrayList<>();

        if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_POSITIONCHANGE)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFEROFF)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_SEPARATEOFF)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_COMBINEOFF)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTOUT)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFERENDOFF)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTIN)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_AUTOSORTING)||    //D9000005
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_WAFEREND)||
                CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_SCRAP)) {
            log.info(" waferSorter_CheckConditionForAction, Check Parameters using sorter_waferTransferInfo_Verify" );

            //======================================================
            //   Copy Variable for Data check
            //======================================================
            for( i = 0; i < WaferSorterSlotMapSequenceLen ; i++) {
                //-------------------------------------
                // Copy variables for future data check
                //-------------------------------------
                Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
                waferTransfer.setWaferID(waferSorterSlotMaps.get(i).getWaferID());
                waferTransfer.setDestinationCassetteID(waferSorterSlotMaps.get(i).getDestinationCassetteID());
                waferTransfer.setBDestinationCassetteManagedByOM(waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM());
                waferTransfer.setDestinationSlotNumber(waferSorterSlotMaps.get(i).getDestinationSlotNumber().intValue());
                waferTransfer.setOriginalCassetteID(waferSorterSlotMaps.get(i).getOriginalCassetteID());
                waferTransfer.setBOriginalCassetteManagedByOM(waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM());
                waferTransfer.setOriginalSlotNumber(waferSorterSlotMaps.get(i).getOriginalSlotNumber().intValue());
                strWaferTransferSequence.add(waferTransfer);
            }

            int VerifyCount = WaferSorterSlotMapSequenceLen ;
            //-----------------------------------------------------------------------------
            // Add OTHER wafers that are on same lot to call sorter_waferTransferInfo_Verify
            //-----------------------------------------------------------------------------
            int LotLen= CimArrayUtils.getSize(lotIDs);

            for( i = 0 ; i < LotLen ; i++ ) {
                //-----------------------------------------------------------------------------
                // Getting All Wafers from LotID
                //-----------------------------------------------------------------------------
                List<Material> aMtrlSeq = aPosLotSeq.get(i).allMaterial();


                int nLotWaferLen=0;
                nLotWaferLen = CimArrayUtils.getSize(aMtrlSeq);

                log.info("Lot Count is : {}",nLotWaferLen );
                for(j=0; j<nLotWaferLen ; j++) {
                    Boolean bLotWaferFound = false;
                    String strLotWaferID = aMtrlSeq.get(j).getIdentifier();

                    int nCurPosition=0;
                    if (!CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_JUSTIN)) {
                        nCurPosition = aMtrlSeq.get(j).getPosition();
                    }

                    // ---------------------------------------------------
                    // If Wafers re not copied to strWaferTransferSequence
                    // ---------------------------------------------------
                    // ---------------------------------------------------
                    // Add Wafers that are not on verified wafer sequence
                    // ---------------------------------------------------
                    for ( k = 0 ; k < VerifyCount ; k++ ) {
                        // ---------------------------------------------------
                        // If there are same waferID in verified sequence then return
                        // ---------------------------------------------------
                        if(ObjectIdentifier.equalsWithValue(strLotWaferID, strWaferTransferSequence.get(k).getWaferID())) {
                            break;
                        }
                        // -----------------------------------------------------------------------
                        // If not found wafer then add these after check duplicate position check
                        // -----------------------------------------------------------------------
                        if( k == ( VerifyCount - 1 )) {
                            // -----------------------------------------------------------------------
                            // Check same position is existed on transfered structure
                            // -----------------------------------------------------------------------
                            log.info("Check Adding wafers are not duplicated location : {}",strLotWaferID);
                            for( l = 0; l < WaferSorterSlotMapSequenceLen ; l++) {
                                // If there are same cassette/position found in waferSorterSlotMaps
                                // then error
                                Validations.check( ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(l).getDestinationCassetteID(), cassetteIDs.get(i)) &&
                                        waferSorterSlotMaps.get(l).getDestinationSlotNumber() == nCurPosition , retCodeConfigEx.getInvalidDestWaferPosition());
                            }

                            for (Infos.WaferTransfer waferTransfer : strWaferTransferSequence) {
                                if (ObjectIdentifier.equalsWithValue(strLotWaferID, waferTransfer.getWaferID())) {
                                    continue;
                                }
                            }
                            log.info("Adding to check wafers : {}",strLotWaferID);
                            Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
                            waferTransfer.setWaferID(new ObjectIdentifier(strLotWaferID));

                            //P40A0007 Add Start
                            CimWafer aPosWaferTemp = baseCoreFactory.getBO(CimWafer.class, new ObjectIdentifier(strLotWaferID));
                            if (null != aPosWaferTemp) {
                                waferFound = true;
                            }
                            // -------------------------------------------------------------
                            // Case WaferID is not found
                            // => Then Error
                            // -------------------------------------------------------------
                            Validations.check( CimBooleanUtils.isFalse(waferFound), retCodeConfig.getNotFoundWafer());


                            // -------------------------------------------------------------
                            // Getting Wafers
                            // => Then Error
                            // -------------------------------------------------------------
                            log.info( "Checking Current Carrier Information");
                            MaterialContainer materialContainer = aPosWaferTemp.getMaterialContainer();
                            CimCassette aCurCassetteTemp = (CimCassette) materialContainer;
                            if(null != aCurCassetteTemp){
                                // -------------------------------------------------------------
                                // Getting Current Carrier Information
                                // -------------------------------------------------------------
                                log.info( "GetCarrier Information");
                                String strCurrentCassetteIDTemp = aCurCassetteTemp.getIdentifier();
                                log.info( "strCurrentCassetteIDTemp: {} ", strCurrentCassetteIDTemp);
                                waferTransfer.setDestinationCassetteID(new ObjectIdentifier(strCurrentCassetteIDTemp));
                                waferTransfer.setOriginalCassetteID(new ObjectIdentifier(strCurrentCassetteIDTemp));
                                // -------------------------------------------------------------
                                // Getting Current SlotPosition Information
                                // -------------------------------------------------------------
                                log.info( "GetPosition Information");
                                nCurPosition = aPosWaferTemp.getPosition();

                                log.info( "nCurPosition:{}", nCurPosition);
                                waferTransfer.setDestinationSlotNumber(nCurPosition);
                                waferTransfer.setOriginalSlotNumber(nCurPosition);

                                waferTransfer.setBDestinationCassetteManagedByOM(true);
                                waferTransfer.setBOriginalCassetteManagedByOM(true);
                            } else {
                                waferTransfer.setBDestinationCassetteManagedByOM(false);
                                waferTransfer.setBOriginalCassetteManagedByOM(false);
                            }

                            strWaferTransferSequence.add(waferTransfer);
                            VerifyCount++;
                        }
                        // ------------------------------------------------------
                        // When Exceed LotID x theSP_EnvName_MaximumWafersInALot
                        // ------------------------------------------------------
                        String maximumWafersInALotStr = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
                        int maximumWafersInALot = null == maximumWafersInALotStr ? 25 : Integer.valueOf(maximumWafersInALotStr);
                        Validations.check( VerifyCount >  maximumWafersInALot, retCodeConfig.getError());
                    }
                }

            }
            //---------------------------------------------------------------
            // Check Strusture data for wafer transfer (Sorter operation)
            //---------------------------------------------------------------
            log.info( " Executing sorter_waferTransferInfo_Verify" );
            sorterMethod.sorterWaferTransferInfoVerify(objCommon, strWaferTransferSequence, BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP);
            // --------------------------------------------------------------
            // If Error Occures
            // --------------------------------------------------------------
            log.info( "check for sorter_waferTransferInfo_Verify OK");
            //---------------------------------------
            // Check input parameter and
            // Server data condition
            //---------------------------------------
            cassetteMethod.cassetteCheckConditionForWaferSort(objCommon, strWaferTransferSequence, equipmentID);
            log.info( "check for cassette_CheckConditionForWaferSort OK");
        }

        log.info(" waferSorter_CheckConditionForAction , Assignning Default value for SlotMap ");

        // --------------------------------------------------------------
        // If Copy input condition structure to output Structure
        // --------------------------------------------------------------
        List<Infos.WaferSorterSlotMap> out = waferSorterSlotMaps;

        //======================================================
        //    Assign Default Data to Structure Variable for TCS's Job
        //======================================================
        for( i = 0; i < WaferSorterSlotMapSequenceLen ; i++) {
            Infos.WaferSorterSlotMap waferSorterSlotMap = out.get(i);
            //-----------------------------------------------------------------------------
            // Common Routine
            //-----------------------------------------------------------------------------
            if (CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_AUTOSORTING)||
                    CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_LOTTRANSFER)||
                    CimStringUtils.equals( actionCode, BizConstant.SP_SORTER_WAFEREND)) {
                log.info(  "actionCode == Auto Sorting");
                waferSorterSlotMap.setRequestTime(waferSorterSlotMaps.get(i).getRequestTime());
            } else {
                waferSorterSlotMap.setRequestTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
            }

            waferSorterSlotMap.setSlotMapCompareStatus(BizConstant.EMPTY);
            waferSorterSlotMap.setOmsCompareStatus(BizConstant.EMPTY);

            if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_READ) ||
                    CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_MINIREAD)) {
                waferSorterSlotMap.setWaferID(new ObjectIdentifier());
                waferSorterSlotMap.setLotID(new ObjectIdentifier());
                waferSorterSlotMap.setOriginalCassetteID(waferSorterSlotMap.getDestinationCassetteID());
                waferSorterSlotMap.setOriginalPortID(waferSorterSlotMap.getDestinationPortID());
                waferSorterSlotMap.setOriginalCassetteManagedByOM(waferSorterSlotMap.getDestinationCassetteManagedByOM());
                waferSorterSlotMap.setOriginalSlotNumber(waferSorterSlotMap.getDestinationSlotNumber());
                if ( CimStringUtils.equals(actionCode ,BizConstant.SP_SORTER_READ)) {
                    waferSorterSlotMap.setDestinationSlotNumber(0L);
                    waferSorterSlotMap.setOriginalSlotNumber(0L);
                }

            }
        }
        return out ;
    }

    @Override
    public void waferSorterSlotMapDeleteDR(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDs, String requestTimeStamp, String sorterStatus, String direction) {
        //=======================================
        // DO Action By SorterStatus
        //=======================================

        //--------------------------------------
        // Case SP_Sorter_Running
        //--------------------------------------
        //todo:test ignore the requestTime
        if (CimStringUtils.equals(sorterStatus,BizConstant.SP_SORTER_REQUESTED)) {
            CimSlotMapDO cimSlotMapExam = new CimSlotMapDO();
            cimSlotMapExam.setPortGroup(portGroup);
            cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            cimSlotMapExam.setSorterStatus(sorterStatus);
            cimSlotMapExam.setDirection(direction);
            cimJpaRepository.delete(Example.of(cimSlotMapExam));
        }
        if ( CimStringUtils.equals(sorterStatus,BizConstant.SP_SORTER_SUCCEEDED) ||
                CimStringUtils.equals(sorterStatus,BizConstant.SP_SORTER_ERRORED)  ) {
            cassetteIDs.forEach(carrierID -> {
                CimSlotMapDO cimSlotMapExam = new CimSlotMapDO();
                cimSlotMapExam.setPortGroup(portGroup);
                cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
                cimSlotMapExam.setDestCassetteID(ObjectIdentifier.fetchValue(carrierID));
                cimSlotMapExam.setDirection(direction);
                cimSlotMapExam.setSorterStatus(sorterStatus);
                cimJpaRepository.delete(Example.of(cimSlotMapExam));
            });
        }
        //--------------------------------------
        // Case SP_Sorter_AllDelete
        //--------------------------------------
        if ( CimStringUtils.equals(sorterStatus,BizConstant.SP_SORTER_ALLDELETE)) {
            CimSlotMapDO cimSlotMapExam = new CimSlotMapDO();
            cimSlotMapExam.setPortGroup(portGroup);
            cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            cimJpaRepository.delete(Example.of(cimSlotMapExam));
        }
    }

    @Override
    public void waferSorterSlotMapSTInfoDeleteDR(Infos.ObjCommon objCommon, Params.OnlineSorterRptParams onlineSorterRptParams) {
        // 参数校验
        Validations.check(onlineSorterRptParams==null,retCodeConfig.getInvalidParameter());
        Validations.check(CimArrayUtils.isEmpty(onlineSorterRptParams.getWaferSorterSlotMapList()),retCodeConfig.getInvalidParameter());

        Infos.WaferSorterSlotMap waferSorterSlotMap = onlineSorterRptParams.getWaferSorterSlotMapList().get(0);
        String portGroup = waferSorterSlotMap.getPortGroup();
        String destinationCassetteID = ObjectIdentifier.fetchValue(waferSorterSlotMap.getDestinationCassetteID());
        String equipmentID = ObjectIdentifier.fetchValue(onlineSorterRptParams.getEquipmentID());
        String actionCode=onlineSorterRptParams.getActionCode();
        Validations.check(CimStringUtils.isEmpty(portGroup)|| CimStringUtils.isEmpty(destinationCassetteID)
                || CimStringUtils.isEmpty(equipmentID)|| CimStringUtils.isEmpty(actionCode),retCodeConfig.getInvalidParameter());

        CimSlotMapSTInfoDO cimSlotMapSTInfoDO = cimJpaRepository.queryOne("SELECT * FROM OMWFSLTMAP_STINFO os WHERE PORT_GRP =? AND EQP_ID =? AND CARRIER_ID =? AND ACTION_CODE =?",
                CimSlotMapSTInfoDO.class, portGroup, equipmentID, destinationCassetteID, actionCode);
        cimJpaRepository.delete(cimSlotMapSTInfoDO);

        onlineSorterRptParams.setClaimMemo(cimSlotMapSTInfoDO.getClaimMemo());
        onlineSorterRptParams.setProductRequestID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getProductRequestID()));

        Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams=new Params.MaterialReceiveAndPrepareReqParams();
        onlineSorterRptParams.setMaterialReceiveAndPrepareReqParams(materialReceiveAndPrepareReqParams);

        materialReceiveAndPrepareReqParams.setEquipmentID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getEquipmentID()));
        materialReceiveAndPrepareReqParams.setPortGroup(cimSlotMapSTInfoDO.getPortGroup());
        materialReceiveAndPrepareReqParams.setActionCode(cimSlotMapSTInfoDO.getActionCode());
        materialReceiveAndPrepareReqParams.setCassetteID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getCassetteID()));
        materialReceiveAndPrepareReqParams.setLotType(cimSlotMapSTInfoDO.getLotType());
        materialReceiveAndPrepareReqParams.setSubLotType(cimSlotMapSTInfoDO.getSubLotType());
        materialReceiveAndPrepareReqParams.setCreatingLotID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getCreatingLotID()));
        materialReceiveAndPrepareReqParams.setVendorLotID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getVendorLotID()));
        materialReceiveAndPrepareReqParams.setVendorID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getVendorID()));
        materialReceiveAndPrepareReqParams.setProductID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getProductID()));
        materialReceiveAndPrepareReqParams.setBankID(ObjectIdentifier.buildWithValue(cimSlotMapSTInfoDO.getBankID()));
        materialReceiveAndPrepareReqParams.setClaimMemo(cimSlotMapSTInfoDO.getClaimMemo());
    }

    @Override
    public void waferSorterSlotMapInsertDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> waferSorterSlotMaps) {
        int nSlotLen = CimArrayUtils.getSize(waferSorterSlotMaps);
        for(int i = 0; i < nSlotLen; i++) {
            Validations.check(CimStringUtils.isEmpty(waferSorterSlotMaps.get(i).getPortGroup() )  ||
                    ObjectIdentifier.isEmpty(waferSorterSlotMaps.get(i).getEquipmentID()) ||
                    CimStringUtils.isEmpty(waferSorterSlotMaps.get(i).getActionCode()) ||
                    CimStringUtils.isEmpty(waferSorterSlotMaps.get(i).getRequestTime()) ||
                    CimStringUtils.isEmpty(waferSorterSlotMaps.get(i).getDirection()) ||
                    ObjectIdentifier.isEmpty(waferSorterSlotMaps.get(i).getDestinationCassetteID()) ||
                    ObjectIdentifier.isEmpty(waferSorterSlotMaps.get(i).getDestinationPortID()) ||
                    ObjectIdentifier.isEmpty(waferSorterSlotMaps.get(i).getRequestUserID()) ||
                    CimStringUtils.isEmpty(waferSorterSlotMaps.get(i).getSorterStatus()), retCodeConfig.getInvalidParameter());

        }
        for (int i = 0; i < nSlotLen; i++) {
            CimSlotMapDO slotMapDO = new CimSlotMapDO();
            slotMapDO.setPortGroup(waferSorterSlotMaps.get(i).getPortGroup());
            slotMapDO.setEquipmentID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getEquipmentID()));
            slotMapDO.setActionCode(waferSorterSlotMaps.get(i).getActionCode());
            slotMapDO.setRequestTime(Timestamp.valueOf(waferSorterSlotMaps.get(i).getRequestTime()));
            slotMapDO.setDirection(waferSorterSlotMaps.get(i).getDirection());
            slotMapDO.setWaferID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getWaferID()));
            slotMapDO.setLotID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getLotID()));
            slotMapDO.setDestCassetteID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getDestinationCassetteID()));
            slotMapDO.setDestPortID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getDestinationPortID()));
            slotMapDO.setDestMbSiview(waferSorterSlotMaps.get(i).getDestinationCassetteManagedByOM());
            slotMapDO.setDestPosition(waferSorterSlotMaps.get(i).getDestinationSlotNumber().intValue());
            slotMapDO.setOriginCassetteID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getOriginalCassetteID()));
            slotMapDO.setOriginPortID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getOriginalPortID()));
            slotMapDO.setOriginMbSiview(waferSorterSlotMaps.get(i).getOriginalCassetteManagedByOM());
            if (null != waferSorterSlotMaps.get(i).getOriginalSlotNumber()) {
                slotMapDO.setOriginPosition(CimNumberUtils.intValue(waferSorterSlotMaps.get(i).getOriginalSlotNumber()));
            }
            slotMapDO.setUserID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getRequestUserID()));
            slotMapDO.setReplyTime(new Timestamp(System.currentTimeMillis()));
            slotMapDO.setSorterStatus(waferSorterSlotMaps.get(i).getSorterStatus());
            slotMapDO.setSlopMapComp(waferSorterSlotMaps.get(i).getSlotMapCompareStatus());
            slotMapDO.setMmComp(waferSorterSlotMaps.get(i).getOmsCompareStatus());
            slotMapDO.setAliasName(waferSorterSlotMaps.get(i).getAliasName());
            cimJpaRepository.save(slotMapDO);
        }

    }

    @Override
    public void waferSorterSlotMapSTInfoInsertDR(Infos.ObjCommon objCommon, Params.OnlineSorterActionExecuteReqParams onlineSorterActionExecuteReqParams) {
        // 检验参数避免空指针
        Validations.check(onlineSorterActionExecuteReqParams==null||onlineSorterActionExecuteReqParams.getMaterialReceiveAndPrepareReqParams()==null,retCodeConfig.getInvalidParameter());
        Infos.WaferSorterSlotMap waferSorterSlotMap = onlineSorterActionExecuteReqParams.getWaferSorterSlotMapList().get(0);

        // 检验查询主键
        String portGroup = waferSorterSlotMap.getPortGroup();
        String destinationCassetteID = ObjectIdentifier.fetchValue(waferSorterSlotMap.getDestinationCassetteID());
        String equipmentID = ObjectIdentifier.fetchValue(onlineSorterActionExecuteReqParams.getEquipmentID());
        String actionCode=onlineSorterActionExecuteReqParams.getActionCode();
        Validations.check(CimStringUtils.isEmpty(portGroup)|| CimStringUtils.isEmpty(destinationCassetteID)
                || CimStringUtils.isEmpty(equipmentID)|| CimStringUtils.isEmpty(actionCode),retCodeConfig.getInvalidParameter());

        CimSlotMapSTInfoDO cimSlotMapSTInfoDO = cimJpaRepository.queryOne("SELECT * FROM OMWFSLTMAP_STINFO os WHERE PORT_GRP =? AND EQP_ID =? AND CARRIER_ID =? AND ACTION_CODE =?",
                CimSlotMapSTInfoDO.class, portGroup, equipmentID, destinationCassetteID, actionCode);
        if (cimSlotMapSTInfoDO!=null){
            cimJpaRepository.delete(cimSlotMapSTInfoDO);
        }

        Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams = onlineSorterActionExecuteReqParams.getMaterialReceiveAndPrepareReqParams();
        cimSlotMapSTInfoDO=new CimSlotMapSTInfoDO();
        cimSlotMapSTInfoDO.setPortGroup(materialReceiveAndPrepareReqParams.getPortGroup());
        cimSlotMapSTInfoDO.setEquipmentID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getEquipmentID()));
        cimSlotMapSTInfoDO.setActionCode(materialReceiveAndPrepareReqParams.getActionCode());
        cimSlotMapSTInfoDO.setCassetteID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getCassetteID()));
        cimSlotMapSTInfoDO.setLotType(materialReceiveAndPrepareReqParams.getLotType());
        cimSlotMapSTInfoDO.setSubLotType(materialReceiveAndPrepareReqParams.getSubLotType());
        cimSlotMapSTInfoDO.setCreatingLotID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getCreatingLotID()));
        cimSlotMapSTInfoDO.setVendorLotID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getVendorLotID()));
        cimSlotMapSTInfoDO.setVendorID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getVendorID()));
        cimSlotMapSTInfoDO.setProductID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getProductID()));
        cimSlotMapSTInfoDO.setBankID(ObjectIdentifier.fetchValue(materialReceiveAndPrepareReqParams.getBankID()));
        cimSlotMapSTInfoDO.setClaimMemo(materialReceiveAndPrepareReqParams.getClaimMemo());
        cimSlotMapSTInfoDO.setProductRequestID(ObjectIdentifier.fetchValue(onlineSorterActionExecuteReqParams.getProductRequestID()));
        cimJpaRepository.save(cimSlotMapSTInfoDO);
    }

    @Override
    public void waferSorterCheckAliasName(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> waferSorterSlotMapList) {
        // 检查必要参数
        if (CimArrayUtils.isEmpty(waferSorterSlotMapList)){
            return;
        }

        // 检查alias name是否全部存在,并且不重复
        Set<String> aliasNames=new HashSet<>();
        Map<String,String> waferAlias=new HashMap<>();
        waferSorterSlotMapList.forEach(waferSorterSlotMap -> {
            if (ObjectIdentifier.isEmptyWithValue(waferSorterSlotMap.getWaferID()) || CimStringUtils.isEmpty(waferSorterSlotMap.getAliasName())){
                return;
            }
            aliasNames.add(waferSorterSlotMap.getAliasName());
            waferAlias.put(ObjectIdentifier.fetchValue(waferSorterSlotMap.getWaferID()),waferSorterSlotMap.getAliasName());
        });

        if (waferAlias.isEmpty()){
            return;
        }
        Validations.check(aliasNames.size()!= CimArrayUtils.getSize(waferSorterSlotMapList),retCodeConfig.getInvalidParameter());
        // 检查存在的alias name和slotmap里面的是否一致
        Optional.ofNullable(waferAliasNameGetDR(objCommon, waferSorterSlotMapList.stream().map(
                waferSorterSlotMap -> waferSorterSlotMap.getWaferID()
        ).collect(Collectors.toList()))).ifPresent(
                        aliasWaferNames -> aliasWaferNames.forEach(
                                aliasWaferName -> Validations.check(
                                        CimStringUtils.equals(
                                                waferAlias.get(ObjectIdentifier.fetchValue(aliasWaferName.getWaferID())),
                                                aliasWaferName.getAliasWaferName()),
                                        retCodeConfig.getInvalidParameter())));

        // 检查对应slot number的alias是否和上传的一致
        Map<String,Map<Long,String>> cassetteSlotAliasMap=new HashMap<>();
        waferSorterSlotMapList.forEach(waferSorterSlotMap -> {
            if (CimBooleanUtils.isFalse(waferSorterSlotMap.getOriginalCassetteManagedByOM())||
                    CimStringUtils.isEmpty(waferSorterSlotMap.getAliasName())){
                return;
            }
            String cassetteID=ObjectIdentifier.fetchValue(waferSorterSlotMap.getOriginalCassetteID());
            Validations.check(CimStringUtils.isEmpty(cassetteID),retCodeConfig.getInvalidParameter());
            Map<Long, String> slotAliasMap = cassetteSlotAliasMap.get(cassetteID);
            if (slotAliasMap==null){
                slotAliasMap=new HashMap<>();
                cassetteSlotAliasMap.put(cassetteID,slotAliasMap);
            }
            Long destinationSlotNumber = waferSorterSlotMap.getOriginalSlotNumber();
            Validations.check(destinationSlotNumber==null,retCodeConfig.getInvalidParameter());
            if (destinationSlotNumber!=0){
                slotAliasMap.put(destinationSlotNumber,waferSorterSlotMap.getAliasName());
            }
        });
        Optional.ofNullable(cassetteSlotAliasMap.keySet()).ifPresent(cassetteIDs->{
            cassetteIDs.forEach(cassetteID->{
                List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfos = cassetteMethod.cassetteGetWaferMapDR(objCommon, ObjectIdentifier.buildWithValue(cassetteID));
                if (CimArrayUtils.isEmpty(waferMapInCassetteInfos)){
                    return;
                }
                Map<Long, String> slotAliasMap = cassetteSlotAliasMap.get(cassetteID);
                waferMapInCassetteInfos.forEach(waferMapInCassetteInfo -> {
                    String aliasWaferName = waferMapInCassetteInfo.getAliasWaferName();
                    if (CimStringUtils.isEmpty(aliasWaferName)){
                        return;
                    }
                    Integer slotNumber = waferMapInCassetteInfo.getSlotNumber();
                    if (CimNumberUtils.intValue(slotNumber)==0){
                        return;
                    }
                    String aliasName = slotAliasMap.get(slotNumber.longValue());
                    if (CimStringUtils.isEmpty(aliasName)){
                        return;
                    }
                    Validations.check(!CimStringUtils.equals(aliasWaferName,aliasName),retCodeConfigEx.getWafersorterSlotmapCompareError());
                });
            });
        });
    }

    /**
     * description:
     *
     * 此方法过时，请参考Wafer sorter method方法
     *
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 10:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    @Deprecated
    public void waferSorterCheckConditionAfterWaferIdReadDR(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID) {

        Long waferIdReadActionFlg = 0L;
        Long waferMoveActionFlg = 0L;
        String actionCode = "";
        String moveActionCode = "";

        //----------------------------------------
        //  Check Equipment ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        //--------------------------
        // SELECT SQL FROM OSSLOTMAP
        //--------------------------
        String fsPortGroup = portGroup;
        String fsEquipmentID = equipmentID.getValue();
        String fsActionCode = BizConstant.SP_SORTER_DIRECTION_TCS;

        String sql = "SELECT\n" +
                "            DISTINCT(ACTION_CODE),\n" +
                "            REQUEST_TIME\n" +
                "        FROM\n" +
                "            OSSLOTMAP\n" +
                "        WHERE\n" +
                "            EQP_ID = ?1\n" +
                "        AND\n" +
                "            PORT_GRP = ?2\n" +
                "        AND\n" +
                "            DIRECTION = ?3\n" +
                "        ORDER BY\n" +
                "            REQUEST_TIME DESC ";
        List<CimSlotMapDO> queryResult = cimJpaRepository.query(sql, CimSlotMapDO.class, fsEquipmentID, fsPortGroup, fsActionCode);
        for (CimSlotMapDO slotMapDO : queryResult) {
            actionCode = slotMapDO.getActionCode();
            if (CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_READ,actionCode)||
                    CimObjectUtils.equalsWithValue(BizConstant.SP_SORTER_START,actionCode)){
                log.info("WaferIdReadAciotnFlg is 1");
                waferIdReadActionFlg = 1L;
                break;
            }else if (CimStringUtils.equals(BizConstant.SP_SORTER_POSITIONCHANGE,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_LOTTRANSFEROFF,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_SEPARATEOFF,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_COMBINEOFF,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_JUSTIN,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_JUSTOUT,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_WAFERENDOFF,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_SCRAP,actionCode) ||
                    CimStringUtils.equals(BizConstant.SP_SORTER_ADJUSTTOMM,actionCode)){
                log.info("WaferMoveActionFlg is 1");
                moveActionCode = slotMapDO.getActionCode();
                waferMoveActionFlg = 1L;
            }else {
                log.info("other ActionCode,{}",actionCode);
            }
        }
        //------------------
        // Judgment
        //------------------
        Validations.check(waferIdReadActionFlg == 0L, retCodeConfigEx.getNotFoundWaferIdRead());
    }

    @Override
    public List<Infos.WaferSorterActionList> waferSorterActionListSelectDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {

        //init
        //--------------------------------------------------
        //    Get object reference of Equipment
        //--------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(CimObjectUtils.isEmpty(aPosMachine), retCodeConfig.getNotFoundEqp());

        //--------------------------------------------------
        // Get Equipment attributes
        //--------------------------------------------------
        String strMachineCategory = aPosMachine.getCategory();
        Validations.check(CimStringUtils.isEmpty(strMachineCategory), retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        //--------------------------------------------------
        // Check Equipment Type
        //--------------------------------------------------

        Validations.check(!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, strMachineCategory), retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        //--------------------------
        // SELECT SQL FROM OMSORTACT
        //--------------------------

        CimActionRecipeDO cimActionRecipeExam = new CimActionRecipeDO();
        cimActionRecipeExam.setEqpId(ObjectIdentifier.fetchValue(equipmentID));
        return cimJpaRepository.findAll(Example.of(cimActionRecipeExam)).stream()
                .map(cimActionRecipeDO -> {
                    Infos.WaferSorterActionList waferSorterActionList = new Infos.WaferSorterActionList();
                    waferSorterActionList.setEquipmentID(ObjectIdentifier.buildWithValue(cimActionRecipeDO.getEqpId()));
                    waferSorterActionList.setActionCode(cimActionRecipeDO.getActionCode());
                    waferSorterActionList.setPhysicalRecipeID(cimActionRecipeDO.getPhysicalRecipeId());
                    waferSorterActionList.setUserID(ObjectIdentifier.buildWithValue(cimActionRecipeDO.getUserId()));
                    waferSorterActionList.setStoreTime(CimDateUtils.convertToSpecString(cimActionRecipeDO.getStoreTime()));
                    return waferSorterActionList;
                }).collect(Collectors.toList());
    }

    @Override
    public void waferSorterActionListInsertDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterActionList> strWaferSorterActionListSequence, ObjectIdentifier equipmentID) {


        //--------------------------------------------------
        //    Get object reference of Equipment
        //--------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check (CimObjectUtils.isEmpty(aPosMachine), new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID)));


        //--------------------------------------------------
        // Get Equipment attributes
        //--------------------------------------------------
        String strMachineCategory = aPosMachine.getCategory();
        Validations.check(CimStringUtils.isEmpty(strMachineCategory), retCodeConfig.getNotFoundCategory(),"*****");

        //--------------------------------------------------
        // Check Equipment Type
        //--------------------------------------------------
        Validations.check(!CimObjectUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER,strMachineCategory), retCodeConfig.getMachineTypeNotSorter(),strMachineCategory);

        //------------------------------------
        //          RECORD DELETE
        //------------------------------------
        String fsEquipmentID = ObjectIdentifier.fetchValue(equipmentID);
        CimActionRecipeDO actionRecipe = new CimActionRecipeDO();
        actionRecipe.setEqpId(fsEquipmentID);
//        cimJpaRepository.removeNonRuntimeEntityForExample(actionRecipe);

        //------------------------------------
        //   SELECT INSERT RECORD
        //------------------------------------
        int nListLen = CimArrayUtils.getSize(strWaferSorterActionListSequence);
        List<Infos.WaferSorterActionList> tmpActListSeq = new ArrayList<>();
        int lenCnt = 0;
        for (int i = 0; i < nListLen; i++) {
            if (CimStringUtils.isNotEmpty(strWaferSorterActionListSequence.get(i).getPhysicalRecipeID())){
                log.info("physicalRecipeID != NULL");
                tmpActListSeq.add(lenCnt,strWaferSorterActionListSequence.get(i));
                lenCnt++;
            }
        }
        //------------------------------------
        //          RECORD INSERT
        //------------------------------------
        nListLen = CimArrayUtils.getSize(tmpActListSeq);
        log.info("strWaferSorterActionListSequence.length() = {}",nListLen);
        for (int i = 0; i < nListLen; i++) {
            fsEquipmentID = tmpActListSeq.get(i).getEquipmentID().getValue();
            String fsActionCode = tmpActListSeq.get(i).getActionCode();
            String fsPhysicalRecipeID = tmpActListSeq.get(i).getPhysicalRecipeID();
            String fsUserID = tmpActListSeq.get(i).getUserID().getValue();
            CimActionRecipeDO cimActionRecipeDO = new CimActionRecipeDO();
            cimActionRecipeDO.setEqpId(fsEquipmentID);
            cimActionRecipeDO.setActionCode(fsActionCode);
            cimActionRecipeDO.setPhysicalRecipeId(fsPhysicalRecipeID);
            cimActionRecipeDO.setUserId(fsUserID);
            cimActionRecipeDO.setStoreTime(new Timestamp(System.currentTimeMillis()));
//            cimJpaRepository.saveNonRuntimeEntity(cimActionRecipeDO);
        }

    }

    @Override
    public void recycleCountCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes, String lotType) {
        List<Infos.NewWaferAttributes> newWaferAttributesList = newLotAttributes.getNewWaferAttributesList();
        for (Infos.NewWaferAttributes newWaferAttributes : newWaferAttributesList) {
            ObjectIdentifier sourceWaferID = newWaferAttributes.getSourceWaferID();
            if (ObjectIdentifier.isEmpty(sourceWaferID)) {
                continue;
            }
            ObjectIdentifier sourceLotID = newWaferAttributes.getSourceLotID();
            CimLot sourceLot = baseCoreFactory.getBO(CimLot.class, sourceLotID);
            Integer recycleLimit = sourceLot.getRecycleLimit();
            Integer usageLimit = sourceLot.getUsageLimit();
            LotUsageRecycleCountParams usageRecycleCountByLot = waferMethod.getUsageRecycleCountByLot(objCommon, sourceLotID.getReferenceKey());
            Integer recycleCount = usageRecycleCountByLot.getRecycleCount();
            Integer usageCount = usageRecycleCountByLot.getUsageCount();
            if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_RECYCLELOT)) {
                Validations.check(recycleCount >= recycleLimit && recycleLimit != 0, new OmCode(retCodeConfigEx.getNpwCountOverLimit(), sourceLotID.getValue()));
            } else {
                Validations.check((recycleCount >= recycleLimit && recycleLimit != 0) || (usageCount >= usageLimit && usageLimit != 0), new OmCode(retCodeConfigEx.getNpwCountOverLimit(), sourceLotID.getValue()));
            }
        }
    }

    @Override
    public void bankInCancelCheckByUsageCount(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        String lotType = aLot.getLotType();
        if (!CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)
                && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)
                && !CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)) {
            return;
        }
        LotUsageRecycleCountParams usageRecycleCountByLot = waferMethod.getUsageRecycleCountByLot(objCommon, lotID.getReferenceKey());
        Integer usageCount = usageRecycleCountByLot.getUsageCount();
        Integer usageLimit = aLot.getUsageLimit();
        Validations.check(usageCount >= usageLimit && usageLimit != 0, new OmCode(retCodeConfigEx.getNpwCountOverLimit(), lotID.getValue()));
    }

    @Override
    public LotUsageRecycleCountParams getUsageRecycleCountByLot(Infos.ObjCommon objCommon, String lotObj) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotObj);
        List<ProductDTO.WaferInfo> allWaferInfo = aLot.getAllWaferInfo();
        List<Integer> recycleCountList = new ArrayList<>();
        List<Integer> usageCountList = new ArrayList<>();
        Integer recycleCount = 0;
        Integer usageCount = 0;
        if (CimArrayUtils.isNotEmpty(allWaferInfo)) {
            for (ProductDTO.WaferInfo waferInfo : allWaferInfo) {
                CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, waferInfo.getWaferID());
                Integer tempRecycleCount = aWafer.getRecycleCount();
                Integer tempUsageCount = aWafer.getUsageCount();
                recycleCountList.add(CimObjectUtils.isEmpty(tempRecycleCount) ? 0 : tempRecycleCount);
                usageCountList.add(CimObjectUtils.isEmpty(tempUsageCount) ? 0 : tempUsageCount);
            }
            recycleCount = Collections.max(recycleCountList);
            usageCount = Collections.max(usageCountList);
        }
        LotUsageRecycleCountParams usageRecycleCountParams = new LotUsageRecycleCountParams();
        usageRecycleCountParams.setRecycleCount(recycleCount);
        usageRecycleCountParams.setUsageCount(usageCount);
        return usageRecycleCountParams;
    }

    @Override
    public void lotRecycleWaferCountUpdate(Infos.ObjCommon objCommon, String lotObj) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotObj);
        if (!CimStringUtils.equals(aLot.getLotType(),BizConstant.SP_LOT_TYPE_RECYCLELOT)){
            return;
        }
        String sql = " SELECT OMWAFER.ID \n" +
                "FROM   OMLOT_WAFER, OMWAFER\n" +
                "WHERE  OMLOT_WAFER.REFKEY = ?1 \n" +
                "AND    OMWAFER.WAFER_ID = OMLOT_WAFER.WAFER_ID";
        List<Object> waferObjList = cimJpaRepository.queryOneColumn(sql, lotObj);
        if (CimArrayUtils.isNotEmpty(waferObjList)){
            for (Object tmpWaferObj : waferObjList){
                String waferObj = CimObjectUtils.toString(tmpWaferObj);
                CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, waferObj);
                int recycleCount = aWafer.getRecycleCount();
                aWafer.setRecycleCount(recycleCount + 1);
                aWafer.setUsageCount(0);
            }
        }
        //check if the usage count get over the limit
        LotUsageRecycleCountParams usageRecycleCountByLot = waferMethod.getUsageRecycleCountByLot(objCommon, lotObj);
        Integer usageCount = usageRecycleCountByLot.getUsageCount();
        Integer recycleCount = usageRecycleCountByLot.getRecycleCount();
        Integer usageLimit = aLot.getUsageLimit();
        Integer recycleLimit = aLot.getRecycleLimit();
        if (CimStringUtils.equals(aLot.getLotType(), BizConstant.SP_LOT_TYPE_RECYCLELOT)) {
            Validations.check(recycleCount >= recycleLimit && recycleLimit != 0, new OmCode(retCodeConfigEx.getNpwCountOverLimit(), "New Lot"));
        } else {
            Validations.check((recycleCount >= recycleLimit && recycleLimit != 0) || (usageCount >= usageLimit && usageLimit != 0), new OmCode(retCodeConfigEx.getNpwCountOverLimit(), "New Lot"));
        }
    }

    @Override
    public void waferSorterAliasNameUpdate(Infos.ObjCommon objCommon, Infos.WaferSorter waferSorter) {
        CimWafer waferBO = baseCoreFactory.getBO(CimWafer.class, waferSorter.getWaferID());
        Validations.check(waferBO == null, retCodeConfig.getNotFoundWafer());
        waferBO.setAliasWaferName(waferSorter.getAliasName());
    }


    @Override
    public void waferSorterSlotMapStatusUpdateDR(Infos.ObjCommon objCommon, Infos.WaferSorterSlotMap strWaferSorterSlotMap, String updateStatus) {
        //ignore the request time from postman
        CimSlotMapDO cimSlotMapExam = new CimSlotMapDO();
        cimSlotMapExam.setPortGroup(strWaferSorterSlotMap.getPortGroup());
        cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(strWaferSorterSlotMap.getEquipmentID()));
        cimSlotMapExam.setActionCode(strWaferSorterSlotMap.getActionCode());
        cimSlotMapExam.setDirection(strWaferSorterSlotMap.getDirection());
        cimSlotMapExam.setSorterStatus(strWaferSorterSlotMap.getSorterStatus());
        List<CimSlotMapDO> slotMapDOS = cimJpaRepository.findAll(Example.of(cimSlotMapExam));
        Validations.check(CimArrayUtils.isEmpty(slotMapDOS), retCodeConfigEx.getNotFoundSlotMapRecord());

        slotMapDOS.forEach(slotMapDO -> {
            slotMapDO.setSorterStatus(updateStatus);
            cimJpaRepository.save(slotMapDO);
        });
    }

    @Override
    public void waferSorterLotMaterialsScrap(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> waferSorterSlotMaps) {

        //----------------------------------------
        // INITIALIZATION
        //----------------------------------------
        int i = 0;
        int slotPosition = 0;
        int scrapLen = CimArrayUtils.getSize(waferSorterSlotMaps);

        for(i = 0; i < scrapLen; i++) {
            //----------------------------------------
            // Get obj PosCassette_var
            //----------------------------------------
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, waferSorterSlotMaps.get(i).getOriginalCassetteID());
            Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette(), objCommon.getTransactionID());
            //----------------------------------------
            // Get obj PosWafer_var
            //----------------------------------------
            CimWafer aWafer = baseCoreFactory.getBO(CimWafer.class, waferSorterSlotMaps.get(i).getWaferID());
            Validations.check(null == aWafer, retCodeConfig.getNotFoundWafer(), objCommon.getTransactionID());


            //----------------------------------------
            // Get slotPosition
            //----------------------------------------
            slotPosition = aWafer.getPosition();

            //----------------------------------------
            // Get obj Material_var
            //----------------------------------------
            Material aMaterial = aCassette.contentsOfPosition(slotPosition);
            CimWafer atmpWafer = (CimWafer) aMaterial;
            Validations.check(null == atmpWafer, retCodeConfig.getNotFoundWafer(), objCommon.getTransactionID());


            //----------------------------------------
            // Get WaferID to monitor existence
            //----------------------------------------

            //----------------------------------------
            // Practice removeWafer_fromPosition
            //----------------------------------------
            if (ObjectIdentifier.equalsWithValue(waferSorterSlotMaps.get(i).getWaferID(), atmpWafer.getIdentifier())) {
                Wafer tmpWafer  = aCassette.removeWaferFromPosition(slotPosition);
            }
        }
    }

    @Override
    public void waferSorterSlotMapWaferIdReadUpdateDR(Infos.ObjCommon objCommon, List<Infos.WaferSorterSlotMap> waferSorterSlotMaps) {

        String tempPortGroup  = waferSorterSlotMaps.get(0).getPortGroup();
        ObjectIdentifier tempEqpID      = waferSorterSlotMaps.get(0).getEquipmentID();
        ObjectIdentifier tempDestCastID = waferSorterSlotMaps.get(0).getDestinationCassetteID();
        ObjectIdentifier tempDestPortID = waferSorterSlotMaps.get(0).getDestinationPortID();

        int lenUpdateSeq = CimArrayUtils.getSize(waferSorterSlotMaps);
        Boolean bError_status = false;
        for(int i = 0; i < lenUpdateSeq; i++) {
            if(!CimStringUtils.equals(tempPortGroup,waferSorterSlotMaps.get(0).getPortGroup())) {
                bError_status = true;
            }

            if(!ObjectIdentifier.equalsWithValue(tempEqpID, waferSorterSlotMaps.get(0).getEquipmentID())) {
                bError_status = true;
            }

            if(!ObjectIdentifier.equalsWithValue(tempDestCastID, waferSorterSlotMaps.get(0).getDestinationCassetteID())) {
                bError_status = true;
            }

            if(!ObjectIdentifier.equalsWithValue(tempDestPortID, waferSorterSlotMaps.get(0).getDestinationPortID())) {
                bError_status = true;
            }

            Validations.check(bError_status, retCodeConfig.getInvalidParameter());

        }
        CimSlotMapDO cimSlotMapExam = new CimSlotMapDO();
        cimSlotMapExam.setActionCode(BizConstant.SP_SORTER_READ);
        cimSlotMapExam.setPortGroup(tempPortGroup);
        cimSlotMapExam.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
        cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(tempEqpID));
        List<CimSlotMapDO> slotMapDOS = cimJpaRepository.findAll(Example.of(cimSlotMapExam));
        Validations.check(CimArrayUtils.isEmpty(slotMapDOS), retCodeConfigEx.getNotFoundSlotMapRecord());

        for (CimSlotMapDO slotMapDO : slotMapDOS) {
            for (int i = 0; i < lenUpdateSeq; i++) {
                CimSlotMapDO _cimSlotMapExam = new CimSlotMapDO();
                _cimSlotMapExam.setPortGroup(tempPortGroup);
                _cimSlotMapExam.setEquipmentID(ObjectIdentifier.fetchValue(tempEqpID));
                _cimSlotMapExam.setActionCode(BizConstant.SP_SORTER_READ);
                _cimSlotMapExam.setRequestTime(slotMapDO.getRequestTime());
                _cimSlotMapExam.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
                _cimSlotMapExam.setWaferID(ObjectIdentifier.fetchValue(waferSorterSlotMaps.get(i).getWaferID()));
                Infos.WaferSorterSlotMap waferSorterSlotMap = waferSorterSlotMaps.get(i);
                cimJpaRepository.findAll(Example.of(cimSlotMapExam)).forEach(cimSlotMapDO -> {
                    cimSlotMapDO.setDestCassetteID(ObjectIdentifier.fetchValue(waferSorterSlotMap.getDestinationCassetteID()));
                    cimSlotMapDO.setDestPortID(ObjectIdentifier.fetchValue((waferSorterSlotMap.getDestinationPortID())));
                    cimSlotMapDO.setDestPosition(waferSorterSlotMap.getDestinationSlotNumber().intValue());
                    cimJpaRepository.save(slotMapDO);
                });
            }
        }

    }
}
