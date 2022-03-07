package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.lot.CimLotMaterialContainerDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.lmg.LotMonitorGroupResults;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IMonitorGroupMethod;
import com.fa.cim.newcore.bo.factory.CimStage;
import com.fa.cim.newcore.bo.factory.CimStageGroup;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/26                            Wind                create file
 *
 * @author: Wind
 * @date: 2018/12/26 13:28
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class MonitorGroupMethod implements IMonitorGroupMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private com.fa.cim.newcore.bo.product.ProductManager newProductManager;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public List<Infos.MonitorGroups> monitorGroupGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotID)));
        List<MaterialContainer> strMaterialContainers = null;
        CimMonitorGroup aControlMonitorGroup = aLot.getControlMonitorGroup();
        List<Infos.MonitorGroups> monitorGroupList = new ArrayList<>();
        List<ProductDTO.MonitoredLot> monitoredLotList = null;
        if (aControlMonitorGroup != null){
            Infos.MonitorGroups monitorGroup = new Infos.MonitorGroups();
            monitorGroup.setMonitorGroupID(ObjectIdentifier.build(aControlMonitorGroup.getIdentifier(), aControlMonitorGroup.getPrimaryKey()));
            monitorGroup.setMonitorLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
            strMaterialContainers = aLot.materialContainers();
            int len = CimArrayUtils.getSize(strMaterialContainers);
            if (len > 0){
                monitorGroup.setMonitorCassetteID(ObjectIdentifier.build(strMaterialContainers.get(0).getIdentifier(),
                        strMaterialContainers.get(0).getPrimaryKey()));
            }
            monitoredLotList = aControlMonitorGroup.allLots();
            int lotLen = CimArrayUtils.getSize(monitoredLotList);
            boolean monitoredLotExtInOtherFlag = false;
            List<Infos.MonitoredLots> strMonitoredLots = new ArrayList<>();
            monitorGroup.setStrMonitoredLots(strMonitoredLots);
            monitorGroupList.add(monitorGroup);

            if (lotLen > 0){
                String generatedFabID = aControlMonitorGroup.getGeneratedFabID();
                String currentFabID = StandardProperties.OM_SITE_ID.getValue();
                if (!CimStringUtils.isEmpty(generatedFabID) && !CimStringUtils.isEmpty(currentFabID)
                        && !CimStringUtils.equals(generatedFabID, currentFabID)){
                    monitoredLotExtInOtherFlag = true;
                }
            }
            if (!monitoredLotExtInOtherFlag){
                for (ProductDTO.MonitoredLot monitoredLot : monitoredLotList){
                    Infos.MonitoredLots tmpMonitoredLot = new Infos.MonitoredLots();
                    strMonitoredLots.add(tmpMonitoredLot);
                    tmpMonitoredLot.setLotID(monitoredLot.getLotID());
                    CimLot aMonLot = baseCoreFactory.getBO(CimLot.class, monitoredLot.getLotID());
                    Validations.check(aMonLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(monitoredLot.getLotID())));
                    strMaterialContainers = aMonLot.materialContainers();
                    len = CimArrayUtils.getSize(strMaterialContainers);
                    if (len > 0){
                        tmpMonitoredLot.setCassetteID(ObjectIdentifier.build(strMaterialContainers.get(0).getIdentifier(),
                                strMaterialContainers.get(0).getPrimaryKey()));
                    }
                }
            } else {
                log.info("monitoredLots exist in other Fab. lot info get from monitorGroup");
                // Get MonitoredLots
                Infos.InterFabMonitorGroups interFabMonitorGroups = lotMethod.lotMonitoredLotsGetDR(objCommon, monitorGroupList.get(0).getMonitorGroupID());
                List<Infos.InterFabMonitoredLots> strInterFabMonitoredLotsList = interFabMonitorGroups.getStrInterFabMonitoredLotsSeq();
                int monLotLen = CimArrayUtils.getSize(strInterFabMonitoredLotsList);
                if (monLotLen > 0){
                    for (Infos.InterFabMonitoredLots interFabMonitoredLots : strInterFabMonitoredLotsList){
                        Infos.MonitoredLots tmpMonitoredLot = new Infos.MonitoredLots();
                        tmpMonitoredLot.setLotID(interFabMonitoredLots.getLotID());
                        tmpMonitoredLot.setCassetteID(interFabMonitoredLots.getCassetteID());
                        strMonitoredLots.add(tmpMonitoredLot);
                    }
                }
            }
        }
        List<CimMonitorGroup> strPosMonitorGroupList = aLot.allMonitorGroups();
        int mgLen = CimArrayUtils.getSize(strPosMonitorGroupList);
        CimLot aMonitorLot = null;
        if (mgLen > 0){
            for (CimMonitorGroup cimMonitorGroup : strPosMonitorGroupList){
                Infos.MonitorGroups tmpMonitorGroups = new Infos.MonitorGroups();
                monitorGroupList.add(tmpMonitorGroups);
                tmpMonitorGroups.setMonitorGroupID(ObjectIdentifier.build(cimMonitorGroup.getIdentifier(), cimMonitorGroup.getPrimaryKey()));
                boolean lotExistCurrentFlag = true;
                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotBasicInfoFlag(false);
                lotInfoInqFlag.setLotControlUseInfoFlag(false);
                lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                lotInfoInqFlag.setLotOperationInfoFlag(false);
                lotInfoInqFlag.setLotOrderInfoFlag(false);
                lotInfoInqFlag.setLotControlJobInfoFlag(false);
                lotInfoInqFlag.setLotProductInfoFlag(false);
                lotInfoInqFlag.setLotRecipeInfoFlag(false);
                lotInfoInqFlag.setLotLocationInfoFlag(false);
                lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                lotInfoInqFlag.setLotWaferAttributesFlag(false);
                lotInfoInqFlag.setLotBackupInfoFlag(false);
                Infos.LotInfo lotInfoRetCode = null;
                try {
                    lotInfoRetCode = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, cimMonitorGroup.getMonitorLot().getLotID());
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode())){
                        throw e;
                    }
                    if (Validations.isEquals(retCodeConfig.getNotFoundLot(),e.getCode())){
                        lotExistCurrentFlag = false;
                    }
                }

                List<MaterialContainer> strMaterialContainers1 = null;
                if (lotExistCurrentFlag){
                    aMonitorLot = cimMonitorGroup.getMonitorLot();
                    Validations.check(aMonitorLot == null, new OmCode(retCodeConfig.getNotFoundMonitorLot(), ""));
                    tmpMonitorGroups.setMonitorLotID(new ObjectIdentifier(aMonitorLot.getIdentifier(), aMonitorLot.getPrimaryKey()));
                    strMaterialContainers1 = aMonitorLot.materialContainers();
                    int len = CimArrayUtils.getSize(strMaterialContainers1);
                    if (len > 0){
                        tmpMonitorGroups.setMonitorCassetteID(new ObjectIdentifier(strMaterialContainers1.get(0).getIdentifier(),
                                strMaterialContainers1.get(0).getPrimaryKey()));
                    }
                } else {
                    Infos.InterFabMonitorGroups interFabMonitorGroups = lotMethod.lotMonitoredLotsGetDR(objCommon,
                            monitorGroupList.get(CimArrayUtils.getSize(monitorGroupList)).getMonitorGroupID());
                    tmpMonitorGroups.setMonitorLotID(interFabMonitorGroups.getMonitorLotID());
                }
                monitoredLotList = cimMonitorGroup.allLots();
                List<Infos.MonitoredLots> strMonitoredLots = new ArrayList<>();
                tmpMonitorGroups.setStrMonitoredLots(strMonitoredLots);
                int lotLen = CimArrayUtils.getSize(monitoredLotList);
                if (lotLen > 0){
                    for (ProductDTO.MonitoredLot monitoredLot : monitoredLotList){
                        Infos.MonitoredLots tmpMonitoredLot = new Infos.MonitoredLots();
                        strMonitoredLots.add(tmpMonitoredLot);
                        tmpMonitoredLot.setLotID(monitoredLot.getLotID());
                        CimLot aMonLot2 = baseCoreFactory.getBO(CimLot.class, monitoredLot.getLotID());
                        Validations.check(aMonLot2 == null, new OmCode(retCodeConfig.getNotFoundLot(),
                                ObjectIdentifier.fetchValue(monitoredLot.getLotID())));
                        strMaterialContainers1 = aMonLot2.materialContainers();
                        int len = CimArrayUtils.getSize(strMaterialContainers1);
                        if (len > 0){
                            tmpMonitoredLot.setCassetteID(ObjectIdentifier.build(strMaterialContainers1.get(0).getIdentifier(),
                                    strMaterialContainers1.get(0).getPrimaryKey()));
                        }
                    }
                }
            }
        }
        return monitorGroupList;
    }

    @Override
    public List<Infos.MonitoredLots> monitorGroupDelete(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        RetCode<List<Infos.MonitoredLots>> result = new RetCode<>();
        List<Infos.MonitoredLots> monitoredLots = new ArrayList<>();
        Boolean lotExistCurrentFlag = true;
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(false);
        lotInfoInqFlag.setLotControlUseInfoFlag(false);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
        lotInfoInqFlag.setLotOperationInfoFlag(false);
        lotInfoInqFlag.setLotOrderInfoFlag(false);
        lotInfoInqFlag.setLotControlJobInfoFlag(false);
        lotInfoInqFlag.setLotProductInfoFlag(false);
        lotInfoInqFlag.setLotRecipeInfoFlag(false);
        lotInfoInqFlag.setLotLocationInfoFlag(false);
        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
        lotInfoInqFlag.setLotWaferAttributesFlag(false);
        lotInfoInqFlag.setLotBackupInfoFlag(false);

        Infos.LotInfo objLotDetailInfoGetDROut = null;
        try {
            objLotDetailInfoGetDROut = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, lotID);
        }catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundLot(), e.getCode())) {
                throw e;
            }
            lotExistCurrentFlag = false;
        }
        if (CimBooleanUtils.isFalse(lotExistCurrentFlag)) {
            //-----------------------//
            //   Get MonitoredLots   //
            //-----------------------//
            Infos.InterFabMonitorGroups out = lotMethod.lotMonitoredLotsGetDR(objCommon, lotID);
            if (!CimArrayUtils.isEmpty(out.getStrInterFabMonitoredLotsSeq())) {
                //--------------------------//
                //   Release monitorgroup   //
                //--------------------------//
                this.monitorGroupDeleteDR(objCommon, out.getMonitorGroupID());
            }
        } else {
            CimLot aMonitorLot = baseCoreFactory.getBO(CimLot.class, lotID);
            Validations.check(null == aMonitorLot, retCodeConfig.getNotFoundLot());

            com.fa.cim.newcore.bo.product.CimMonitorGroup aControlMonitorGroup = aMonitorLot.getControlMonitorGroup();
            if (null != aControlMonitorGroup) {
                String generatedFabID = aControlMonitorGroup.getGeneratedFabID();

                String currentFabID = StandardProperties.OM_SITE_ID.getValue();
                boolean monitoredLotExtInOtherFabFlag = false;
                if (!CimStringUtils.isEmpty(generatedFabID) && !CimStringUtils.isEmpty(currentFabID) && !CimStringUtils.equals(generatedFabID, currentFabID)) {
                    monitoredLotExtInOtherFabFlag = true;
                }
                List<ProductDTO.MonitoredLot> monitoredLotSequence = aControlMonitorGroup.allLots();
                for (int i = 0; i < CimArrayUtils.getSize(monitoredLotSequence); i++) {
                    ProductDTO.MonitoredLot monitoredLot = monitoredLotSequence.get(i);
                    Infos.MonitoredLots monitoredLo = new Infos.MonitoredLots();

                    monitoredLo.setLotID(monitoredLot.getLotID());
                    if (!monitoredLotExtInOtherFabFlag) {
                        //If monitoredLot exist in current Fab, can be used monitoredLot object
                        CimLot lot = baseCoreFactory.getBO(CimLot.class, monitoredLot.getLotID());
                        List<MaterialContainer> strMaterialContainers = lot.materialContainers();
                        if (!CimArrayUtils.isEmpty(strMaterialContainers)) {
                            monitoredLo.setCassetteID(new ObjectIdentifier(strMaterialContainers.get(0).getIdentifier(), strMaterialContainers.get(0).getPrimaryKey()));
                        }
                    }
                    monitoredLots.add(monitoredLo);

                }

                aMonitorLot.setControlMonitorGroup(null);
                if (CimBooleanUtils.isTrue(monitoredLotExtInOtherFabFlag)) {
                    //-------------------------------------//
                    //   Delete monitorGroup informaiton   //
                    //-------------------------------------//
                    this.monitorGroupDeleteDR(objCommon, lotID);
                    //------------------------------------------//
                    //   MonitorGroupRelease request to SaR     //
                    //------------------------------------------//
                    //【TODO】【TODO - NOTIMPL】- monitorGroupRelease_RequestForMultiFab
                } else {
                    newProductManager.removeMonitorGroup(aControlMonitorGroup);
                }
            } else {
                Validations.check(retCodeConfig.getNotFoundMonitorgroup());
            }
        }
        return monitoredLots;
    }


    @Override
    public void monitorGroupDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        RetCode<Object> result = new RetCode<>();
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        CimMonitorGroup monitorGroup = baseCoreFactory.getBO(CimMonitorGroup.class, lotID);
        monitorGroup.remove();

        //CimMonitorGroupDO monitorGroup = monitorGroupCore.findMonitorGroupByMonitorGroupID(lotID);
        //-----------------------//
        //   Get the SystemKey   //
        //-----------------------//
        //-------------------------------//
        //   Delete FRMONITORGRP_UDATA   //
        //-------------------------------//
        /*
        monitorGroupCore.findMonitorGroupUDatasByMonitorGroupID(monitorGroup.getId()).forEach(monitorGroupUData -> {
            monitorGroupCore.removeMonitorGroupUData(monitorGroupUData);
        });
        */
        //-----------------------------//
        //   Delete FRMONITORGRP_LOT   //
        //-----------------------------//
        /*
        monitorGroupCore.findMonitorGroupLotsByReferenceKey(monitorGroup).forEach(monitorGroupLot -> {
            monitorGroupCore.removeMonitorGroupLot(monitorGroupLot);
        });
        */
        //-------------------------//
        //   Delete #RMONITORGRP   //
        //-------------------------//
        //【TODO】【TODO - NOTIMPL】- #RMONITORGRP

        //-----------------------------//
        //   Delete FRLOT_MONITORGRP   //
        //-----------------------------//
        com.fa.cim.newcore.bo.product.CimLot monitorLot = monitorGroup.getMonitorLot();
        monitorLot.removeMonitorGroup(monitorGroup);
        /*
        cimLot.findLotmonitorGroupsByMonitorGroupID(monitorGroup.getMonitorGroupID()).forEach(lotMonitorGroup -> {
            cimLot.removeLotMonitorGroup(lotMonitorGroup);
        });
        */
        //-------------------------//
        //   Delete FRMONITORGRP   //
        //-------------------------//

        //monitorGroupCore.removeMonitorGroup(monitorGroup);
    }



    @Override
    public Outputs.ObjMonitorGroupDeleteCompOut monitorGroupDeleteComp(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Outputs.ObjMonitorGroupDeleteCompOut out = new Outputs.ObjMonitorGroupDeleteCompOut();
        List<Infos.MonitoredCompLots> monitoredCompLotsList = new ArrayList<>();

        com.fa.cim.newcore.bo.product.CimLot monitorLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        Validations.check(null == monitorLot, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));

        com.fa.cim.newcore.bo.pd.CimProcessFlowContext processFlowContextDO = monitorLot.getProcessFlowContext();
        Validations.check(null == processFlowContextDO, retCodeConfig.getNotFoundPfx());

        com.fa.cim.newcore.bo.pd.CimProcessOperation processOperationDO = processFlowContextDO.getPreviousProcessOperation();
        Validations.check(null == processOperationDO, retCodeConfig.getSucc());

        Boolean isEndOfMonitorRelationFlag = processOperationDO.isEndOfMonitorRelationFlagOn();
        Boolean isMonitorGroupReleaseRequired = processOperationDO.isMonitorGroupReleaseRequired();
        if (CimBooleanUtils.isTrue(isEndOfMonitorRelationFlag) || CimBooleanUtils.isTrue(isMonitorGroupReleaseRequired)) {
            com.fa.cim.newcore.bo.product.CimMonitorGroup controlMonitorGroup = monitorLot.getControlMonitorGroup();
            if (null != controlMonitorGroup) {
                List<ProductDTO.MonitoredLot> monitoredLotList = controlMonitorGroup.allLots();
                int size = CimArrayUtils.getSize(monitoredLotList);
                for (int i = 0; i < size; i++) {
                    ProductDTO.MonitoredLot monitoredLot = monitoredLotList.get(i);
                    Infos.MonitoredCompLots monitoredCompLots = new Infos.MonitoredCompLots();
                    monitoredCompLots.setProductLotID(monitoredLot.getLotID());
                    CimLot lotDO = baseCoreFactory.getBO(CimLot.class, monitoredLot.getLotID());
                    Validations.check(null == lotDO, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(monitoredLot.getLotID())));
                    Boolean isOnHoldFlag = lotDO.isOnHold();
                    if (CimBooleanUtils.isTrue(isOnHoldFlag)) {
                        List<ProductDTO.HoldRecord> holdRecordList = lotDO.allHoldRecords();
                        List<Infos.LotHoldReq> strLotHoldReleaseReqList = new ArrayList<>();
                        Boolean heldFlag = false;
                        int holdRecordListSize = CimArrayUtils.getSize(holdRecordList);
                        for (int j = 0; j < holdRecordListSize; j++) {
                            ProductDTO.HoldRecord holdRecord =  holdRecordList.get(j);
                            if (CimStringUtils.equals(BizConstant.SP_HOLDTYPE_WAITINGMONITORRESULTHOLD, holdRecord.getHoldType())
                                    && ObjectIdentifier.equalsWithValue(lotID, holdRecord.getRelatedLot())) {
                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(holdRecord.getHoldType());
                                lotHoldReq.setHoldReasonCodeID(holdRecord.getReasonCode());
                                lotHoldReq.setHoldUserID(holdRecord.getHoldPerson());
                                lotHoldReq.setRelatedLotID(holdRecord.getRelatedLot());
                                strLotHoldReleaseReqList.add(lotHoldReq);
                            }
                        }
                        monitoredCompLots.setStrLotHoldReleaseReqList(strLotHoldReleaseReqList);
                    }
                    monitoredCompLotsList.add(monitoredCompLots);
                }

                if (CimBooleanUtils.isTrue(isMonitorGroupReleaseRequired)) {
                    monitorLot.setControlMonitorGroup(null);
                    monitorLot.flush();
                    newProductManager.removeMonitorGroup(controlMonitorGroup);
                }

            }
        }
        out.setMonitoredCompLotsList(monitoredCompLotsList);
        return out;
    }

    public void monitorGroupCheckExistance(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        com.fa.cim.newcore.bo.product.CimMonitorGroup controlMonitorGroup = aLot.getControlMonitorGroup();
        Validations.check(controlMonitorGroup != null, new OmCode(retCodeConfig.getAlreadyExistMonitorGroup(), lotID.getValue()));
    }

    @Override
    public ObjectIdentifier monitorGroupMakeByAuto(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<ObjectIdentifier> productLotIDs) {
        //-------------------------------------------
        // Get object reference of Production Lots
        //-------------------------------------------
        List<CimLot> aProdLotSeq = new ArrayList<>();
        int nProdLotLen = CimArrayUtils.getSize(productLotIDs);
        for (int i = 0; i < nProdLotLen; i++) {
            aProdLotSeq.add(baseCoreFactory.getBO(CimLot.class, productLotIDs.get(i)));
        }
        //-------------------------------------------
        // Get object reference of Monitor lot
        //-------------------------------------------
        CimLot aMonitorLot = baseCoreFactory.getBO(CimLot.class, lotID);
        //-------------------------------------------
        // Check Monitor lot Process State
        //-------------------------------------------
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, aMonitorLot.getLotProcessState()), retCodeConfig.getInvalidLotStat());
        //-------------------------------------------
        // Prepare structure in order to call monitorGroup_Make()
        //-------------------------------------------
        List<Infos.MonRelatedProdLots> strMonRelatedProdLots = new ArrayList<>();
        //-------------------------------------------
        // Check Production lot Process State
        //-------------------------------------------
        String strProductionLotProcState = null;
        String strControlJobID =null;
        for (int i = 0; i < nProdLotLen; i++) {
            if (0 == i) {
                strProductionLotProcState = aProdLotSeq.get(i).getLotProcessState();
            } else {
                String tmpProductionLotProcState = aProdLotSeq.get(i).getLotProcessState();
                Validations.check(!CimStringUtils.equals(strProductionLotProcState, tmpProductionLotProcState), retCodeConfig.getNotSameProcstat());
            }
            com.fa.cim.newcore.bo.pd.CimProcessOperation aPosPO = null;
            if (BizConstant.SP_LOT_PROCSTATE_WAITING.equals(strProductionLotProcState)) {
                aPosPO =  aProdLotSeq.get(i).getPreviousProcessOperation();
            } else if (BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(strProductionLotProcState)) {
                aPosPO = aProdLotSeq.get(i).getProcessOperation();
            } else {
                throw new ServiceException(retCodeConfig.getInvalidProcstateForMonitorgrping());
            }
            Validations.check(null == aPosPO, retCodeConfig.getNotFoundProcessOperation());

            CimControlJob aPosCtrlJob = aPosPO.getAssignedControlJob();
            Validations.check(null == aPosCtrlJob, retCodeConfig.getNotFoundControlJob());

            if (0 == i) {
                strControlJobID = aPosCtrlJob.getIdentifier();
            } else {
                Validations.check(!CimStringUtils.equals(strControlJobID, aPosCtrlJob.getIdentifier()), retCodeConfig.getInvalidProcstateForMonitorgrping());
            }
            Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
            monRelatedProdLots.setProductLotID(productLotIDs.get(i));
            strMonRelatedProdLots.add(monRelatedProdLots);
        }
        return this.monitorGroupMake(objCommon, lotID, strMonRelatedProdLots, false).getMonitorLotId();
    }

    @Override
    public LotMonitorGroupResults.LotMonitorGroupHistoryResults monitorGroupMake
            (Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.MonRelatedProdLots> monRelatedProdLots,
             Boolean previousOperationFlag) {
        CimLot monitorLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == monitorLot,
                new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotID)));

        CimMonitorGroup aMonitorGroup = newProductManager.createMonitorGroupNamed(monitorLot.newMonitorGroupID());
        Validations.check(null == aMonitorGroup, retCodeConfig.getNotFoundMonitorgroup());

        aMonitorGroup.setMonitorLot(monitorLot);
        monitorLot.setControlMonitorGroup(aMonitorGroup);

        LotMonitorGroupResults.LotMonitorGroupHistoryResults lmgHResult =
                new LotMonitorGroupResults.LotMonitorGroupHistoryResults();
        lmgHResult.setMonitorGroupId(aMonitorGroup.getIdentifier());
        lmgHResult.setMonitorLotId(monitorLot.getLotID());
        List<LotMonitorGroupResults.MonitorGroupLotDetailsResults> lotList = new ArrayList<>(
                CimArrayUtils.getSize(monRelatedProdLots));
        lmgHResult.setLotResult(lotList);

        String fabID = StandardProperties.OM_SITE_ID.getValue();
        if (!CimStringUtils.isEmpty(fabID)) {
            log.info("#### Set generated FabID to Monitor Group ==> {}", fabID);
            aMonitorGroup.setGeneratedFabID(fabID);
        }
        for (int i = 0; i < CimArrayUtils.getSize(monRelatedProdLots); i++) {
            Infos.MonRelatedProdLots monRelatedProdLot = monRelatedProdLots.get(i);

            CimLot productLot = baseCoreFactory.getBO(CimLot.class, monRelatedProdLot.getProductLotID());
            Validations.check(null == productLot, new OmCode(retCodeConfig.getNotFoundLot(),
                    ObjectIdentifier.fetchValue(monRelatedProdLot.getProductLotID())));

            ProductDTO.MonitoredLot monitoredLot = new ProductDTO.MonitoredLot();
            monitoredLot.setLotID(new ObjectIdentifier(productLot.getIdentifier(), productLot.getPrimaryKey()));

            String operationNumber = monRelatedProdLot.getOperationNumber();
            CimProcessFlowContext processFlowContext;
            CimProcessOperation processOperation;
            if (!CimStringUtils.isEmpty(operationNumber)) {
                processFlowContext = productLot.getProcessFlowContext();
                Validations.check(null == processFlowContext, retCodeConfig.getNotFoundPfx());

                processOperation = processFlowContext.findProcessOperationForOperationNumberBefore(operationNumber);

            } else if (previousOperationFlag) {
                log.info("previousOperationFlag is TRUE");
                processFlowContext = productLot.getProcessFlowContext();
                Validations.check(null == processFlowContext, new OmCode(retCodeConfig.getNotFoundPfx(), ""));

                processOperation = processFlowContext.getPreviousProcessOperation();
            } else {
                processOperation = productLot.getProcessOperation();
            }
            Validations.check(null == processOperation,
                    new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", productLot.getIdentifier()));
            monitoredLot.setProcessOperation(processOperation.getPrimaryKey());
            aMonitorGroup.addLot(monitoredLot);

            // process flow context 可能为null
            CimProcessFlowContext pfc = monitorLot.getProcessFlowContext();
            // 设置pass count
            int passCount = Objects.isNull(pfc) ? 0 : pfc.getPassCount(processOperation);
            CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
            String processFlow = Objects.isNull(mainProcessDefinition)
                    ? BizConstant.EMPTY
                    : mainProcessDefinition.getIdentifier();
            CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
            ObjectIdentifier operationId = ObjectIdentifier.emptyIdentifier();
            if (Objects.nonNull(processDefinition)) {
                operationId = ObjectIdentifier.build(processDefinition.getIdentifier(),
                        processDefinition.getPrimaryKey());
            }
            settingLotMonitorData(lotList, productLot);

        }

        // 设置pass count
        settingLotMonitorData(lotList, monitorLot);

        return lmgHResult;
    }

    /**
     * settingLotMonitorData @{description}
     *      设置生成history的参数
     * @param lotList
     * @param lot
     */
    private void settingLotMonitorData(List<LotMonitorGroupResults.MonitorGroupLotDetailsResults> lotList, CimLot lot) {
        LotMonitorGroupResults.MonitorGroupLotDetailsResults monitorGroupLotDetailsResults
                = new LotMonitorGroupResults.MonitorGroupLotDetailsResults();
        lotList.add(monitorGroupLotDetailsResults);
        CimProcessOperation processOperation = lot.getProcessOperation();

        // step1. process flow context 可能为null
        CimProcessFlowContext pfc = lot.getProcessFlowContext();
        int passCount = Objects.isNull(pfc) ? 0 : pfc.getPassCount(processOperation);
        monitorGroupLotDetailsResults.setOperationPassCount(passCount);

        // step2. operation Id
        CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
        ObjectIdentifier operationId = ObjectIdentifier.emptyIdentifier();
        if (Objects.nonNull(processDefinition)) {
            operationId = ObjectIdentifier.build(processDefinition.getIdentifier(), processDefinition.getPrimaryKey());
        }
        monitorGroupLotDetailsResults.setOperationId(operationId);

        // step3. process flow
        CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
        String processFlow = Objects.isNull(mainProcessDefinition)
                ? BizConstant.EMPTY
                : mainProcessDefinition.getIdentifier();
        monitorGroupLotDetailsResults.setProcessFlowId(processFlow);

        // step4. operation type / name / number
        monitorGroupLotDetailsResults.setOperationType(processDefinition.getProcessDefinitionType());
        monitorGroupLotDetailsResults.setOperationName(lot.getOperationName());
        monitorGroupLotDetailsResults.setOperationNumber(lot.getOperationNumber());

        // step5. lot / lot type / sub lot type / priority / mfg layer / owner
        monitorGroupLotDetailsResults.setLotType(lot.getLotType());
        monitorGroupLotDetailsResults.setSubLotType(lot.getSubLotType());
        monitorGroupLotDetailsResults.setLotId(lot.getLotID());
        monitorGroupLotDetailsResults.setLotPriority(lot.getPriority());
        monitorGroupLotDetailsResults.setMfgLayer(lot.getMFGLayer());
        CimPerson lotOwner = lot.getLotOwner();
        if (Objects.nonNull(lotOwner)) {
            monitorGroupLotDetailsResults.setLotOwnerId(lotOwner.getIdentifier());
        }

        // step6. setting carrier
        monitorGroupLotDetailsResults.setCarrierCategory(lot.getCurrentCarrierCategory());
        CimLotMaterialContainerDO cimLotMaterialContainerExample = new CimLotMaterialContainerDO();
        cimLotMaterialContainerExample.setReferenceKey(lot.getPrimaryKey());
        CimLotMaterialContainerDO lotMaterialContainer = cimJpaRepository.findOne(Example.of(cimLotMaterialContainerExample)).orElse(null);
        if (Objects.nonNull(lotMaterialContainer)) {
            monitorGroupLotDetailsResults.setCarrierId(lotMaterialContainer.getMaterialContainerID());
        }

        // step7. hold
        monitorGroupLotDetailsResults.setHoldState(lot.getLotHoldState());

        // step8. product
        CimProductSpecification productSpecification = lot.getProductSpecification();
        if (Objects.nonNull(productSpecification)) {
            monitorGroupLotDetailsResults.setProductId(productSpecification.getIdentifier());
            monitorGroupLotDetailsResults.setProductType(productSpecification.getProductType());

            CimProductGroup productGroup = productSpecification.getProductGroup();
            if (Objects.nonNull(productGroup)) {
                monitorGroupLotDetailsResults.setProductFmlyId(productGroup.getIdentifier());
                CimTechnology technology = productGroup.getTechnology();
                if (Objects.nonNull(technology)) {
                    monitorGroupLotDetailsResults.setTechnologyId(technology.getIdentifier());
                }
            }
        }

        // step9. stage
        CimStage stage = processOperation.getStage();
        if (Objects.nonNull(stage)) {
            monitorGroupLotDetailsResults.setStageId(stage.getIdentifier());
            CimStageGroup stageGroup = stage.getStageGroup();
            if (Objects.nonNull(stageGroup)) {
                monitorGroupLotDetailsResults.setStageGroupId(stageGroup.getIdentifier());
            }
        }
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strMonitorGroupDeleteCompIn
     * @return com.fa.cim.dto.Infos.MonitorGroupDeleteCompOut
     * @exception
     * @author Ho
     * @date 2019/10/8 14:33
     */
    public Infos.MonitorGroupDeleteCompOut monitorGroupDeleteComp100(
            Infos.ObjCommon strObjCommonIn,
            Infos.MonitorGroupDeleteCompIn strMonitorGroupDeleteCompIn ){
        log.info("monitorGroupDeleteComp_100");

        List<Infos.InterFabMonitorGroupActionInfo> strInterFabMonitorGroupActionInfoSequence = strMonitorGroupDeleteCompIn.getStrInterFabMonitorGroupActionInfoSequence();
        ObjectIdentifier lotID = strMonitorGroupDeleteCompIn.getLotID();

        com.fa.cim.newcore.bo.product.CimLot aMonitorLot ;
        aMonitorLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);

        com.fa.cim.newcore.bo.pd.CimProcessFlowContext aProcessFlowContext ;
        aProcessFlowContext = aMonitorLot.getProcessFlowContext();

        Validations.check(aProcessFlowContext==null,retCodeConfig.getNotFoundPfx());

        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation;
        //BUG-2424 EDCExcutor precedes processMove before it reaches the next step
        final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(strObjCommonIn, lotID);
        if (checkConditionForPO) {
            aProcessOperation = aMonitorLot.getProcessOperation();
        } else {
            aProcessOperation = aMonitorLot.getPreviousProcessOperation();
        }

        if (aProcessOperation==null){
            log.info("monitorGroupDeleteComp");
            return new Infos.MonitorGroupDeleteCompOut();
        }

        Boolean isEndOfMonitorRelationFlag ;

        isEndOfMonitorRelationFlag = aProcessOperation.isEndOfMonitorRelationFlagOn() ;

        Boolean isMonitorGroupReleaseRequired ;

        isMonitorGroupReleaseRequired = aProcessOperation.isMonitorGroupReleaseRequired() ;

        Infos.MonitorGroupDeleteCompOut strMonitorGroupDeleteCompOut=new Infos.MonitorGroupDeleteCompOut();
        if( CimBooleanUtils.isTrue(isEndOfMonitorRelationFlag) || CimBooleanUtils.isTrue(isMonitorGroupReleaseRequired) ){
            log.info(""+ "isEndOfMonitorRelationFlag == TRUE");

            Boolean requestOtherFabFlag = FALSE;
            String currentFabID;
            currentFabID = aProcessOperation.getFabID();

            log.info(""+ "current FabID"+ currentFabID);
            if( CimStringUtils.length(currentFabID) == 0 ){
                log.info(""+ "currentFabID is null");
            }

            com.fa.cim.newcore.bo.product.CimMonitorGroup aControlMonitorGroup ;
            aControlMonitorGroup = aMonitorLot.getControlMonitorGroup();

            if( aControlMonitorGroup != null ){
                log.info(""+ "isEndOfMonitorRelationFlag == TRUE"+ "aControlMonitorGroup)!= TRUE");

                String generatedFabID;
                generatedFabID = aControlMonitorGroup.getGeneratedFabID();

                log.info(""+ "generatedFabID"+ generatedFabID);
                if( CimStringUtils.length(generatedFabID)== 0 ){

                    log.info(""+ "generatedFabID is null");
                }

                if( CimStringUtils.length(generatedFabID) != 0 && CimStringUtils.length(currentFabID) != 0 &&
                        !CimStringUtils.equals(generatedFabID, currentFabID)){
                    log.info(""+ "monitoredLot exist in other Fab");
                    requestOtherFabFlag = TRUE;
                }

                if( CimBooleanUtils.isFalse(requestOtherFabFlag) ){

                    List<ProductDTO.MonitoredLot> aMonitoredLotSequence = aControlMonitorGroup.allLots();

                    List<ProductDTO.MonitoredLot> aMonitoredLotSequenceVar = aMonitoredLotSequence;

                    int i, nLen = CimArrayUtils.getSize(aMonitoredLotSequence);
                    log.info(""+ "isEndOfMonitorRelationFlag == TRUE"+ "aControlMonitorGroup)!= TRUE"+ "aMonitoredLotSequence.length()"+ nLen);

                    List<Infos.MonitoredCompLots> strMonitoredCompLots = new ArrayList<>();
                    strMonitorGroupDeleteCompOut.setStrMonitoredCompLots(strMonitoredCompLots) ;

                    for( i=0; i<nLen; i++ ){
                        strMonitoredCompLots.add(new Infos.MonitoredCompLots());
                        strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).setProductLotID ( (aMonitoredLotSequence).get(i).getLotID() );

                        com.fa.cim.newcore.bo.product.CimLot aLot ;
                        aLot =baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,(aMonitoredLotSequence).get(i).getLotID());
                        List<MaterialContainer> aMaterialContainerSequence = null ;
                        aMaterialContainerSequence = aLot.materialContainers();

                        List<MaterialContainer> aTmpMaterialContainerSequence ;
                        aTmpMaterialContainerSequence = aMaterialContainerSequence ;

                        if( CimArrayUtils.getSize(aMaterialContainerSequence) > 0){
                            strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).setProductCassetteID(
                                    ObjectIdentifier.build((aMaterialContainerSequence).get(0).getIdentifier(),
                                            (aMaterialContainerSequence).get(0).getPrimaryKey())
                            );
                        }

                        Boolean isOnHoldFlag ;
                        isOnHoldFlag = aLot.isOnHold() ;

                        if( CimBooleanUtils.isTrue(isOnHoldFlag) ){
                            log.info(""+ "isOnHoldFlag == TRUE");

                            List<ProductDTO.HoldRecord> aHoldRecordSeq = null ;
                            List<ProductDTO.HoldRecord> aTmpHoldRecordSeq ;

                            aHoldRecordSeq = aLot.allHoldRecords() ;
                            aTmpHoldRecordSeq = aHoldRecordSeq ;

                            int countIndex = 0 , k ,nHoldRecordLen = CimArrayUtils.getSize(aHoldRecordSeq);
                            log.info(""+ "HoldRecordSeq.getLength()"+ nHoldRecordLen ) ;

                            Boolean aHeldFlag = FALSE ;

                            strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).setStrLotHoldReleaseReqList(new ArrayList<>());
                            for( k=0 ; k<nHoldRecordLen ; k++ ){
                                if( ( CimStringUtils.equals((aHoldRecordSeq).get(k).getHoldType() , BizConstant.SP_HOLDTYPE_WAITINGMONITORRESULTHOLD)) &&
                                        ( CimStringUtils.equals((aHoldRecordSeq).get(k).getRelatedLot().getValue() , lotID.getValue() )) ){
                                    strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).getStrLotHoldReleaseReqList().add(new Infos.LotHoldReq());

                                    strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).getStrLotHoldReleaseReqList().get(countIndex).setHoldType ( (aHoldRecordSeq).get(k).getHoldType() );
                                    strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).getStrLotHoldReleaseReqList().get(countIndex).setHoldReasonCodeID         ( (aHoldRecordSeq).get(k).getReasonCode() );
                                    strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).getStrLotHoldReleaseReqList().get(countIndex).setHoldUserID               ( (aHoldRecordSeq).get(k).getHoldPerson() );
                                    strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(i).getStrLotHoldReleaseReqList().get(countIndex).setRelatedLotID             ( (aHoldRecordSeq).get(k).getRelatedLot() );

                                    countIndex++ ;
                                }
                            }
                        }
                    }

                    if (isMonitorGroupReleaseRequired){
                        aMonitorLot.setControlMonitorGroup( null );

                        try{
                            newProductManager.removeMonitorGroup( aControlMonitorGroup ) ;
                        }catch( ServiceException exception ){
                            Validations.check(true,new OmCode(retCodeConfig.getNotFoundMonitorgroup(),lotID.getValue()));
                        }

                    }

                }else{

                    log.info(""+ "correspondingPO is other Fab");

                    List<ProductDTO.MonitoredLot> aMonitoredLotSequence = aControlMonitorGroup.allLots();

                    List<ProductDTO.MonitoredLot> aMonitoredLotSequenceVar = aMonitoredLotSequence;

                    ObjectIdentifier monitorGroupID=new ObjectIdentifier();
                    monitorGroupID.setValue ( aControlMonitorGroup.getIdentifier());

                    String lotType;
                    lotType = aMonitorLot.getLotType();

                    int i, nLen = CimArrayUtils.getSize(aMonitoredLotSequence);
                    log.info(""+ "isEndOfMonitorRelationFlag == TRUE"+ "aControlMonitorGroup)!= TRUE"+ "aMonitoredLotSequence.length()"+ nLen);

                    if (isMonitorGroupReleaseRequired){

                        int monLen = CimArrayUtils.getSize(strInterFabMonitorGroupActionInfoSequence);
                        Boolean findFlag = FALSE;
                        for( i=0; i<monLen; i++ ){
                            if( CimStringUtils.equals(lotID.getValue(), strInterFabMonitorGroupActionInfoSequence.get(i).getMonitoringLotID().getValue()) ){
                                log.info(""+ "this lot is already checked. getAdd() monitoredLots data");
                                findFlag = TRUE;
                                break;
                            }
                        }

                        if( CimBooleanUtils.isTrue(findFlag) ){
                            log.info(""+ "add monitoredLots data");
                            strInterFabMonitorGroupActionInfoSequence.get(i).getMonitorGroupReleaseInfo().setGroupReleaseFlag ( TRUE);
                            strInterFabMonitorGroupActionInfoSequence.get(i).getMonitorGroupReleaseInfo().setMonitorGroupID   ( monitorGroupID);
                            strInterFabMonitorGroupActionInfoSequence.get(i).getMonitorGroupReleaseInfo().setLotType          ( lotType);
                        }else{
                            log.info(""+ "create new data");
                            // strInterFabMonitorGroupActionInfoSequence.getLength()( monLen + 1 );

                            strInterFabMonitorGroupActionInfoSequence.add(new Infos.InterFabMonitorGroupActionInfo());
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).setFabID ( generatedFabID);
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).setMonitoringLotID ( lotID);
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).setMonitorGroupReleaseInfo(new Infos.MonitorGroupReleaseInfo());
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).getMonitorGroupReleaseInfo().setGroupReleaseFlag ( TRUE);
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).getMonitorGroupReleaseInfo().setMonitorGroupID   ( monitorGroupID);
                            strInterFabMonitorGroupActionInfoSequence.get(monLen).getMonitorGroupReleaseInfo().setLotType          ( lotType);
                        }

                        aMonitorLot.setControlMonitorGroup( null );

                        monitorGroupDeleteDR( strObjCommonIn,
                                monitorGroupID );
                    }
                }
            }

        }

        strMonitorGroupDeleteCompOut.setStrInterFabMonitorGroupActionInfoSequence ( strInterFabMonitorGroupActionInfoSequence);

        return strMonitorGroupDeleteCompOut;
    }

}
