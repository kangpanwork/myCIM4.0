package com.fa.cim.method.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.durable.CimDurableDO;
import com.fa.cim.entity.runtime.durable.CimDurableDurableGroupDO;
import com.fa.cim.entity.runtime.durablegroup.CimDurableGroupDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IConstraintMethod;
import com.fa.cim.method.IFixtureMethod;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimProcessDurableCapability;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/16       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/10/16 10:06
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FixtureMethod implements IFixtureMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IConstraintMethod entityInhibitMethod;

    @Override
    public Results.FixtureListInqResult fixtureFillInTxPDQ001DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID, ObjectIdentifier fixtureID, String fixturePartNumber, ObjectIdentifier fixtureGroupID, ObjectIdentifier fixtureCategoryID, String fixtureStatus, Long maxRetrieveCount) {
        Results.FixtureListInqResult fixtureListInqResult = new Results.FixtureListInqResult();
        int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
        long fetchLimitCount = 0;
        if (maxRetrieveCount <= 0 || maxRetrieveCount > BizConstant.SP_MAXLIMITCOUNT_FOR_LISTINQ){
            fetchLimitCount = BizConstant.SP_MAXLIMITCOUNT_FOR_LISTINQ;
        } else {
            fetchLimitCount = maxRetrieveCount;
        }
        String hFRDRBLEQP_ID = equipmentID.getValue();
        String hFRDRBLDRBL_PART_NO = fixturePartNumber;
        String hFRDRBLDRBL_ID = fixtureID.getValue();
        String hFRDRBL_DRBLGRPDRBLGRP_ID = fixtureGroupID.getValue();
        String hFRDRBLGRPDRBL_SUB_CATEGORY = fixtureCategoryID.getValue();
        // Case 2 : LotID
        if (ObjectIdentifier.isEmptyWithValue(equipmentID) && !ObjectIdentifier.isEmptyWithValue(lotID)){
            log.info("Case 2 : LotID");
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfigEx.getNoNeedFixture());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
            List<CimProcessDurableCapability> aProcessDurableCapabilities = aLogicalRecipe.allFixtureGroups();
            int nFixturePDCs = CimArrayUtils.getSize(aProcessDurableCapabilities);
            Validations.check(nFixturePDCs == 0, retCodeConfigEx.getNoNeedFixture());
            List<Infos.FoundFixture> strFoundFixture = new ArrayList<>();
            int l, count = 0;
            for (int i = 0; i < nFixturePDCs; i++){
                CimProcessDurableCapability cimProcessDurableCapability = aProcessDurableCapabilities.get(i);
                List<CimProcessDurable> strProcessDurables = cimProcessDurableCapability.allAssignedProcessDurables();
                ObjectIdentifier aFixtureGroupID = new ObjectIdentifier(cimProcessDurableCapability.getIdentifier(), cimProcessDurableCapability.getPrimaryKey());
                int PDLen = CimArrayUtils.getSize(strProcessDurables);
                for (int j = 0; j < PDLen; j++){
                    CimProcessDurable cimProcessDurable = strProcessDurables.get(j);
                    String aFixtureIdentifier = cimProcessDurable.getIdentifier();
                    for (l = 0; l < count; l++){
                        if (ObjectIdentifier.equalsWithValue(aFixtureIdentifier, strFoundFixture.get(l).getFixtureStatusInfo().getFixtureID())){
                            break;
                        }
                    }
                    if (l < count){
                        continue;
                    }
                    Infos.FoundFixture foundFixture = new Infos.FoundFixture();
                    strFoundFixture.add(foundFixture);
                    
                    Infos.FixtureStatusInfo fixtureStatusInfo = new Infos.FixtureStatusInfo();
                    foundFixture.setFixtureStatusInfo(fixtureStatusInfo);
                    fixtureStatusInfo.setFixtureID(new ObjectIdentifier(aFixtureIdentifier, cimProcessDurable.getPrimaryKey()));
                    fixtureStatusInfo.setDescription(cimProcessDurable.getProcessDurableDescription());
                    foundFixture.setFixtureCategoryID(new ObjectIdentifier(cimProcessDurableCapability.getSubCategory()));
                    foundFixture.setFixtureGroupID(aFixtureGroupID);
                    foundFixture.setFixtureGroupDescription(cimProcessDurableCapability.getProcessDurableCapabilityDescription());
                    fixtureStatusInfo.setFixturePartNumber(cimProcessDurable.getPartNumber());
                    fixtureStatusInfo.setFixtureSerialNumber(cimProcessDurable.getSerialNumber());
                    fixtureStatusInfo.setFixtureStatus(cimProcessDurable.getDurableState());
                    fixtureStatusInfo.setTransferStatus(cimProcessDurable.getTransportState());
                    Machine aMachine = cimProcessDurable.currentAssignedMachine();
                    if (CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) || CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                        fixtureStatusInfo.setEquipmentID(new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                    } else {
                        fixtureStatusInfo.setStockerID(new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                    }
                    fixtureStatusInfo.setLastClaimedTimeStamp(CimDateUtils.getTimestampAsString(cimProcessDurable.getLastClaimedTimeStamp()));
                    fixtureStatusInfo.setLastClaimedPerson(new ObjectIdentifier(cimProcessDurable.getLastClaimedPersonID()));
                    count++;
                }
            }
            Validations.check(CimArrayUtils.isEmpty(strFoundFixture), new OmCode(retCodeConfig.getNotFoundFixture(), "*****"));
            fixtureListInqResult.setStrFoundFixture(strFoundFixture);
            return fixtureListInqResult;
        }
        // Case 3 : EquipmentID, LotID
        else if (!ObjectIdentifier.isEmptyWithValue(equipmentID) && !ObjectIdentifier.isEmptyWithValue(lotID)){
            log.info("Case 3 : EquipmentID, LotID");
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
            CimProcessOperation aProcessOperation = aLot.getProcessOperation();
            Validations.check(aProcessOperation == null, retCodeConfigEx.getNoNeedFixture());
            CimProductSpecification aProductSpecification = aLot.getProductSpecification();
            CimLogicalRecipe aLogicalRecipe = aProcessOperation.findLogicalRecipeFor(aProductSpecification);
            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
            String subLotType = aLot.getSubLotType();
            CimMachineRecipe aMachineRecipe = null;
            if(searchCondition == 1){
                aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
            } else {
                aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
            }
            Validations.check(aMachineRecipe == null, retCodeConfigEx.getNoNeedFixture());
            List<CimProcessDurableCapability> aProcessDurableCapabilitySeq = aLogicalRecipe.findFixtureGroupsForSubLotType(aMachine, aMachineRecipe, subLotType);
            int PDCLen = CimArrayUtils.getSize(aProcessDurableCapabilitySeq);
            Validations.check(PDCLen == 0, retCodeConfigEx.getNoNeedFixture());
            List<Infos.FoundFixture> strFoundFixture = new ArrayList<>();
            int l, count = 0;
            for (int j = 0; j < PDCLen; j++){
                CimProcessDurableCapability cimProcessDurableCapability = aProcessDurableCapabilitySeq.get(j);
                List<CimProcessDurable> allAssignedProcessDurablesSeq = cimProcessDurableCapability.allAssignedProcessDurables();
                ObjectIdentifier aFixtureGroupID = new ObjectIdentifier(cimProcessDurableCapability.getIdentifier(), cimProcessDurableCapability.getPrimaryKey());
                int PDLen = CimArrayUtils.getSize(allAssignedProcessDurablesSeq);
                Machine aMachineInner = null;
                String aFixtureIdentifier = null;
                for (int k = 0; k < PDLen; k++){
                    CimProcessDurable cimProcessDurable = allAssignedProcessDurablesSeq.get(k);
                    aFixtureIdentifier = cimProcessDurable.getIdentifier();
                    for (l = 0; l < count; l++){
                        if (ObjectIdentifier.equalsWithValue(aFixtureIdentifier, strFoundFixture.get(l).getFixtureStatusInfo().getFixtureID())){
                            break;
                        }
                    }
                    if (l < count){
                        continue;
                    }
                    Infos.FoundFixture foundFixture = new Infos.FoundFixture();
                    strFoundFixture.add(foundFixture);

                    Infos.FixtureStatusInfo fixtureStatusInfo = new Infos.FixtureStatusInfo();
                    foundFixture.setFixtureStatusInfo(fixtureStatusInfo);
                    fixtureStatusInfo.setFixtureID(new ObjectIdentifier(aFixtureIdentifier, cimProcessDurable.getPrimaryKey()));
                    fixtureStatusInfo.setDescription(cimProcessDurable.getProcessDurableDescription());
                    foundFixture.setFixtureCategoryID(new ObjectIdentifier(cimProcessDurableCapability.getSubCategory()));
                    foundFixture.setFixtureGroupID(aFixtureGroupID);
                    foundFixture.setFixtureGroupDescription(cimProcessDurableCapability.getProcessDurableCapabilityDescription());
                    fixtureStatusInfo.setFixturePartNumber(cimProcessDurable.getPartNumber());
                    fixtureStatusInfo.setFixtureSerialNumber(cimProcessDurable.getSerialNumber());
                    fixtureStatusInfo.setFixtureStatus(cimProcessDurable.getDurableState());
                    fixtureStatusInfo.setTransferStatus(cimProcessDurable.getTransportState());
                    aMachineInner = cimProcessDurable.currentAssignedMachine();
                    ObjectIdentifier machineIDToFill = new ObjectIdentifier(aMachineInner.getIdentifier(), aMachineInner.getPrimaryKey());
                    if (CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) || CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                        fixtureStatusInfo.setEquipmentID(machineIDToFill);
                    } else {
                        fixtureStatusInfo.setStockerID(machineIDToFill);
                    }
                    fixtureStatusInfo.setLastClaimedTimeStamp(CimDateUtils.getTimestampAsString(cimProcessDurable.getLastClaimedTimeStamp()));
                    fixtureStatusInfo.setLastClaimedPerson(new ObjectIdentifier(cimProcessDurable.getLastClaimedPersonID()));
                    count++;
                }
            }
            Validations.check(CimArrayUtils.isEmpty(strFoundFixture), new OmCode(retCodeConfig.getNotFoundFixture(), "*****"));
            fixtureListInqResult.setStrFoundFixture(strFoundFixture);
            return fixtureListInqResult;
        }
        log.info("Case: LotID not Filled");
        String HV_BUFFER = "SELECT OMPDRBL.PDRBL_ID,\n" +
                "       OMPDRBL.ID,\n" +
                "       OMPDRBL.DESCRIPTION,\n" +
                "       OMPDRBL.DRBL_PART_NO,\n" +
                "       OMPDRBL.DRBL_SERIAL_NO,\n" +
                "       OMPDRBL.PDRBL_STATE,\n" +
                "       OMPDRBL.XFER_STATE,\n" +
                "       OMPDRBL.EQP_ID,\n" +
                "       OMPDRBL.EQP_RKEY,\n" +
                "       OMPDRBL.LAST_TRX_TIME,\n" +
                "       OMPDRBL.LAST_TRX_USER_ID,\n" +
                "       OMPDRBL.LAST_TRX_USER_RKEY,\n" +
                "       OMPDRBL_PDRBLGRP.PDRBL_GRP_ID,\n" +
                "       OMPDRBL_PDRBLGRP.PDRBL_GRP_RKEY,\n" +
                "       OMPDRBLGRP.DESCRIPTION,\n" +
                "       OMPDRBLGRP.PDRBL_SUB_TYPE\n" +
                "  FROM OMPDRBL, OMPDRBL_PDRBLGRP, OMPDRBLGRP\n" +
                " WHERE OMPDRBL_PDRBLGRP.DRBLGRP_ID = OMPDRBLGRP.PDRBL_GRP_ID\n" +
                "   AND OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY\n" +
                "   AND OMPDRBL.PDRBL_CATEGORY = 'Fixture'\n";
        List<Object> params = new ArrayList<>();
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)){
            HV_BUFFER += " AND OMPDRBL.EQP_ID = ? ";
            params.add(equipmentID.getValue());
        }
        if (!CimStringUtils.isEmpty(fixturePartNumber)){
            HV_BUFFER += " AND OMPDRBL.DRBL_PART_NO = ? ";
            params.add(fixturePartNumber);
        }
        if (!ObjectIdentifier.isEmptyWithValue(fixtureID)){
            HV_BUFFER += " AND OMPDRBL.PDRBL_ID LIKE ? ";
            params.add(fixtureID.getValue());
        }
        if (!ObjectIdentifier.isEmptyWithValue(fixtureCategoryID)){
            HV_BUFFER += " AND OMPDRBLGRP.PDRBL_SUB_TYPE = ? ";
            params.add(fixtureCategoryID.getValue());
        }
        if (!CimStringUtils.isEmpty(fixtureStatus)){
            HV_BUFFER += " AND OMPDRBL.PDRBL_STATE = ? ";
            params.add(fixtureStatus);
        }
        List<Object[]> queryResult = cimJpaRepository.query(HV_BUFFER, params.toArray());
        Validations.check(CimArrayUtils.isEmpty(queryResult), new OmCode(retCodeConfig.getNotFoundFixture(), "*****"));
        List<Infos.FoundFixture> strFoundFixture = new ArrayList<>();
        int count = 0;
        for (Object[] objects : queryResult){
            Infos.FoundFixture foundFixture = new Infos.FoundFixture();
            strFoundFixture.add(foundFixture);
            Infos.FixtureStatusInfo fixtureStatusInfo = new Infos.FixtureStatusInfo();
            foundFixture.setFixtureStatusInfo(fixtureStatusInfo);
            fixtureStatusInfo.setFixtureID(new ObjectIdentifier((String)objects[0], (String)objects[1]));
            fixtureStatusInfo.setDescription((String)objects[2]);
            foundFixture.setFixtureGroupID(new ObjectIdentifier((String)objects[12]));
            foundFixture.setFixtureGroupDescription((String)objects[14]);
            foundFixture.setFixtureCategoryID(new ObjectIdentifier((String)objects[15]));
            fixtureStatusInfo.setFixturePartNumber((String)objects[3]);
            fixtureStatusInfo.setFixtureSerialNumber((String)objects[4]);
            fixtureStatusInfo.setFixtureStatus((String)objects[5]);
            fixtureStatusInfo.setTransferStatus((String)objects[6]);
            if (CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) || CimStringUtils.equals(fixtureStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                fixtureStatusInfo.setEquipmentID(new ObjectIdentifier((String)objects[7], (String)objects[8]));
            } else {
                fixtureStatusInfo.setStockerID(new ObjectIdentifier((String)objects[7], (String)objects[8]));
            }
            fixtureStatusInfo.setLastClaimedTimeStamp(String.valueOf((Timestamp)objects[9]));
            fixtureStatusInfo.setLastClaimedPerson(new ObjectIdentifier((String) objects[10], (String) objects[11]));
            count++;
            if (count >= fetchLimitCount){
                break;
            }
        }
        if (!CimArrayUtils.isEmpty(strFoundFixture)){
            fixtureListInqResult.setStrFoundFixture(strFoundFixture);
        }
        return fixtureListInqResult;
    }

    @Override
    public Outputs.objFixtureUsageLimitationCheckOut fixtureUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier fixtureID) {
        Outputs.objFixtureUsageLimitationCheckOut limitationCheckOut = new Outputs.objFixtureUsageLimitationCheckOut();
        /*------------------------*/
        /*   Get Fixture Object   */
        /*------------------------*/
        CimProcessDurable aFixture = baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
        Validations.check(CimObjectUtils.isEmpty(aFixture), retCodeConfig.getNotFoundFixture());

        /*--------------------------*/
        /*   Get Usage Check Flag   */
        /*--------------------------*/
        if (CimBooleanUtils.isTrue(aFixture.isUsageCheckRequired())) {
            return null;
        }

        /*--------------------------*/
        /*   Get Usage Limitation   */
        /*--------------------------*/
        limitationCheckOut.setMaxRunTime((int) (aFixture.getDurationLimit() / (60 * 1000)));
        Long timeUsed = aFixture.getTimeUsed();
        limitationCheckOut.setStartCount(CimObjectUtils.isEmpty(timeUsed) ? 0 : timeUsed.intValue());
        Long timesUsedLimit = aFixture.getTimesUsedLimit();
        limitationCheckOut.setMaxStartCount(CimObjectUtils.isEmpty(timesUsedLimit) ? 0 : timesUsedLimit.intValue());
        Long intervalBetweenPM = aFixture.getIntervalBetweenPM();
        limitationCheckOut.setIntervalBetweenPM(CimObjectUtils.isEmpty(intervalBetweenPM) ? 0 : intervalBetweenPM.intValue());

        if (limitationCheckOut.getMaxRunTime() == 0 &&
                limitationCheckOut.getMaxStartCount() == 0 &&
                limitationCheckOut.getIntervalBetweenPM() == 0) {
            return null;
        }


        /*-------------------------------------*/
        /*   Calcurate Run Time from Last PM   */
        /*-------------------------------------*/
        Timestamp lastMaintenanceTimeStamp = aFixture.getLastMaintenanceTimeStamp();
        Long tempElapsedTime = CimDateUtils.substractTimeStamp(CimObjectUtils.isEmpty(lastMaintenanceTimeStamp) ? 0 : lastMaintenanceTimeStamp.getTime(), objCommon.getTimeStamp().getReportTimeStamp().getTime()) / 1000;
        limitationCheckOut.setRunTime(0);
        limitationCheckOut.setPassageTimeFromLastPM((int) (tempElapsedTime / 60));

        /*--------------------------*/
        /*   Set Flag Information   */
        /*--------------------------*/
        if (limitationCheckOut.getStartCount() >= limitationCheckOut.getMaxStartCount()) {
            limitationCheckOut.setUsageLimitOverFlag(true);
            limitationCheckOut.setStartCountOverFlag(true);
        }

        if (limitationCheckOut.getPassageTimeFromLastPM() >= limitationCheckOut.getIntervalBetweenPM()) {
            limitationCheckOut.setUsageLimitOverFlag(true);
            limitationCheckOut.setPmTimeOverFlag(true);
        }
        /*-------------------------*/
        /*   Create Message Text   */
        /*-------------------------*/
        if (limitationCheckOut.isUsageLimitOverFlag()) {
            String messageText = "<<< Fixture Usage Limitation Over >>> " +
                    "\n    Fixture ID            : %s " +
                    "\n    Start Count           : %s" +
                    "\n    Max Start Count       : %s" +
                    "\n    Passage from PM (min) : %s" +
                    "\n    Interval for PM (min) : %s";
            limitationCheckOut.setMessageText(String.format(messageText, limitationCheckOut.getFixtureID(), limitationCheckOut.getStartCount(), limitationCheckOut.getMaxStartCount(),
                    limitationCheckOut.getPassageTimeFromLastPM(), limitationCheckOut.getIntervalBetweenPM()));

        }

        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return limitationCheckOut;
    }

    @Override
    public void fixtureStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartFixtureInfo> startFixtureList) {
        /*----------------------------------*/
        /*   Get and Check Fixture Status   */
        /*----------------------------------*/
        int sfLength = CimArrayUtils.getSize(startFixtureList);
        Validations.check(0 == sfLength, retCodeConfig.getNotAvailableFixture());

        for (Infos.StartFixtureInfo startFixtureInfo : startFixtureList) {
            com.fa.cim.newcore.bo.durable.CimProcessDurable aFixture = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimProcessDurable.class, startFixtureInfo.getFixtureID());
            Validations.check(null == aFixture, new OmCode(retCodeConfig.getNotFoundReticle(), ObjectIdentifier.fetchValue(startFixtureInfo.getFixtureID())));
            String currentState = aFixture.getDurableState();
            if (!CimStringUtils.equals(currentState, CIMStateConst.CIM_DURABLE_AVAILABLE)
                    && !CimStringUtils.equals(currentState, CIMStateConst.CIM_DURABLE_INUSE)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidFixtureStat(), ObjectIdentifier.fetchValue(startFixtureInfo.getFixtureID()), currentState));
            }
            Machine aMachine = aFixture.currentAssignedMachine();
            Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEquipment(), "******"));
            if (!ObjectIdentifier.equalsWithValue(equipmentID, aMachine.getIdentifier())
                    || !CimStringUtils.equals(aFixture.getTransportState(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidFixtureStat(), ObjectIdentifier.fetchValue(startFixtureInfo.getFixtureID()), aFixture.getTransportState()));
            }
        }
    }

    @Override
    public void fixtureUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier fixtureID) {
        com.fa.cim.newcore.bo.durable.CimProcessDurable fixture = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimProcessDurable.class, fixtureID);
        Validations.check(fixture == null, "can not found fixture");
        Long touchCount = fixture.timesUsed();
        /*
        TODO: Fixture
        EXEC SQL SELECT  FRPRODGRP.GROSS_DIE_COUNT, FRLOT.QTY, FRDRBLGRP.DRBL_SUB_CATEGORY
         INTO   :hFRPRODGRPGROSS_DIE_COUNT, :hFRLOTQTY, :hFRDRBLGRPDRBL_SUB_CATEGORY
         FROM    FRPRODSPEC, FRPRODGRP, FRLOT, OMPDRBL, FRDRBL_DRBLGRP, FRDRBLGRP
         WHERE   FRLOT.LOT_ID = :hFRLOTLOT_ID AND FRLOT.PRODSPEC_ID = FRPRODSPEC.PRODSPEC_ID AND FRPRODGRP.PRODGRP_ID = FRPRODSPEC.PRODGRP_ID
         AND OMPDRBL.DRBL_ID = :hFRDRBLDRBL_ID AND OMPDRBL.D_THESYSTEMKEY = FRDRBL_DRBLGRP.D_THESYSTEMKEY AND FRDRBL_DRBLGRP.DRBLGRP_ID = FRDRBLGRP.DRBLGRP_ID;
         */
        fixture.setTimesUsed(++touchCount);
    }

    @Override
    public void fixtureUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier fixtureID) {
        CimProcessDurable aFixture = baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
        Long touchCount = aFixture.timesUsed();
        String sql = String.format(" SELECT OMPRODFMLY.GROSS_DIE_COUNT, OMLOT.QTY, OMPDRBLGRP.PDRBL_SUB_TYPE\n" +
                "   FROM OMPRODINFO, OMPRODFMLY, OMLOT, OMPDRBL, OMPDRBL_PDRBLGRP, OMPDRBLGRP\n" +
                "  WHERE OMLOT.LOT_ID = '%s'\n" +
                "    AND OMLOT.PROD_ID = OMPRODINFO.PROD_ID\n" +
                "    AND OMPRODFMLY.PRODFMLY_ID = OMPRODINFO.PRODFMLY_ID\n" +
                "    AND OMPDRBL.PDRBL_ID = '%s'\n" +
                "    AND OMPDRBL.ID = OMPDRBL_PDRBLGRP.refkey\n" +
                "    AND OMPDRBL_PDRBLGRP.PDRBL_GRP_ID = OMPDRBLGRP.PDRBL_GRP_ID;\n",
                ObjectIdentifier.fetchValue(lotID),
                ObjectIdentifier.fetchValue(fixtureID));
        List<Object[]> query = cimJpaRepository.query(sql);
        String hfrprodgrpgrossDieCount = null;
        String hfrlotQTY = null;
        String hfrdrblgrpdrblSubCategory = null;
        if (CimArrayUtils.isNotEmpty(query)) {
            for (Object[] objects : query) {
                hfrprodgrpgrossDieCount = objects[0].toString();
                hfrlotQTY = objects[1].toString();
                hfrdrblgrpdrblSubCategory = objects[2].toString();
            }
        }
        if ( "Prober" .equals(hfrdrblgrpdrblSubCategory)) {
            if (touchCount >= Integer.valueOf(hfrprodgrpgrossDieCount) * Integer.valueOf(hfrlotQTY)) {
                touchCount = touchCount - (Integer.valueOf(hfrprodgrpgrossDieCount) * Integer.valueOf(hfrlotQTY));
            } else {
                touchCount = 0L;
            }
        } else {
            if (touchCount >= 1d) {
                touchCount -= 1;
            } else {
                touchCount = 0L;
            }
        }
        aFixture.setTimesUsed(touchCount);
    }

    @Override
    public Results.FixtureStatusChangeRptResult fixtureFillInTxPDR001(Infos.ObjCommon objCommon,
                                                                      ObjectIdentifier fixtureID) {
        Results.FixtureStatusChangeRptResult fixtureStatusChangeRptResult = new Results.FixtureStatusChangeRptResult();
        CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
        //fixtureStatusChangeRptResult.setFixtureID(fixtureID);
        Infos.FixtureBr fixtureBRInfo = new Infos.FixtureBr();
        fixtureStatusChangeRptResult.setFixtureBRInfo(fixtureBRInfo);
        fixtureStatusChangeRptResult.getFixtureBRInfo().setDescription(cimProcessDurable.getDescription());
        fixtureStatusChangeRptResult.getFixtureBRInfo().setFixturePartNumber(cimProcessDurable.getPartNumber());
        fixtureStatusChangeRptResult.getFixtureBRInfo().setFixtureSerialNumber(cimProcessDurable.getSerialNumber());
        fixtureStatusChangeRptResult.getFixtureBRInfo().setSupplierName(cimProcessDurable.getVendorName());
        fixtureStatusChangeRptResult.getFixtureBRInfo().setUsageCheckFlag(cimProcessDurable.isUsageCheckRequired());

        List<CimProcessDurableCapability> cimProcessDurableCapabilities = cimProcessDurable.allAssignedProcessDurableCapabilities();
        if(cimProcessDurableCapabilities.size()>0){
            //PPT_SET_OBJECT_IDENTIFIER，change object的相关对象
            ObjectIdentifier objectIdentifier = new ObjectIdentifier();
            objectIdentifier.setValue(cimProcessDurableCapabilities.get(0).getIdentifier());
            objectIdentifier.setReferenceKey(cimProcessDurableCapabilities.get(0).toString());
            fixtureStatusChangeRptResult.getFixtureBRInfo().setFixtureGroupID(objectIdentifier);
            fixtureStatusChangeRptResult.getFixtureBRInfo().setFixtureGroupDescription(cimProcessDurableCapabilities.get(0).getProcessDurableCapabilityDescription());
        }

        Infos.FixtureStatusInfo fixtureStatusInfo = new Infos.FixtureStatusInfo();
        fixtureStatusChangeRptResult.setFixtureStatusInfo(fixtureStatusInfo);
        fixtureStatusChangeRptResult.getFixtureStatusInfo().setFixtureStatus(cimProcessDurable.getDurableState());
        fixtureStatusChangeRptResult.getFixtureStatusInfo().setTransferStatus(cimProcessDurable.getTransferStatus());


        Machine machine = cimProcessDurable.currentAssignedMachine();

        if(!CimObjectUtils.isEmpty(machine)){
            if(CimStringUtils.equals(fixtureStatusChangeRptResult.getFixtureStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) || CimStringUtils.equals(fixtureStatusChangeRptResult.getFixtureStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                /*fixtureStatusChangeRptResult.getFixtureStatusInfo().getEquipmentID().setValue(machine.getIdentifier());
                fixtureStatusChangeRptResult.getFixtureStatusInfo().getEquipmentID().setReferenceKey(machine.toString());*/
                fixtureStatusChangeRptResult.getFixtureStatusInfo().setEquipmentID(new ObjectIdentifier(machine.getIdentifier(),machine.toString()));
            }else{
                /*fixtureStatusChangeRptResult.getFixtureStatusInfo().getStockerID().setValue(machine.getIdentifier());
                fixtureStatusChangeRptResult.getFixtureStatusInfo().getStockerID().setReferenceKey(machine.toString());*/
                fixtureStatusChangeRptResult.getFixtureStatusInfo().setStockerID(new ObjectIdentifier(machine.getIdentifier(),machine.toString()));
            }
        }

        fixtureStatusChangeRptResult.getFixtureStatusInfo().setLastClaimedTimeStamp(cimProcessDurable.getLastClaimedTimeStamp().toString());

        ObjectIdentifier lastClaimedPerson = new ObjectIdentifier();
        fixtureStatusChangeRptResult.getFixtureStatusInfo().setLastClaimedPerson(lastClaimedPerson);
        fixtureStatusChangeRptResult.getFixtureStatusInfo().getLastClaimedPerson().setValue(cimProcessDurable.getLastClaimedPersonID());
        double duration = cimProcessDurable.durationUsed();
        double durationLimit = cimProcessDurable.getDurationLimit();

        double min = duration/(60*1000);

        Infos.FixturePm fixturePMInfo = new Infos.FixturePm();
        fixtureStatusChangeRptResult.setFixturePMInfo(fixturePMInfo);
        fixtureStatusChangeRptResult.getFixturePMInfo().setMaximumRunTime(CimObjectUtils.toString(min));
        fixtureStatusChangeRptResult.getFixturePMInfo().setOperationStartCount(cimProcessDurable.getTimeUsed());
        fixtureStatusChangeRptResult.getFixturePMInfo().setLastMaintenanceTimeStamp(CimDateUtils.getTimestampAsString(cimProcessDurable.getLastMaintenanceTimeStamp()));

        // fixtureStatusChangeRptResult.getFixturePMInfo().setLastMaintenancePerson(new ObjectIdentifier());
        //fixtureStatusChangeRptResult.getFixturePMInfo().getLastMaintenancePerson().setValue(cimProcessDurable.getLastMaintenancePersonID());
        fixtureStatusChangeRptResult.getFixturePMInfo().setLastMaintenancePerson(new ObjectIdentifier(cimProcessDurable.getLastMaintenancePersonID()));
        return fixtureStatusChangeRptResult;
    }

    @Override
    public Results.FixtureStatusInqResult fixtureFillInTxPDQ002DR(Infos.ObjCommon objCommon,
                                                                  ObjectIdentifier fixtureID) {
        Results.FixtureStatusInqResult fixtureStatusInqResult = new Results.FixtureStatusInqResult();
        fixtureStatusInqResult.setFixtureID(fixtureID);
        String       hfrdebldrblId = fixtureID.getValue();
        String sql = "SELECT   DESCRIPTION, DRBL_PART_NO, DRBL_SERIAL_NO, VENDOR_NAME, USAGE_CHECK_REQ, DRBL_STATE, TRANS_STATE, EQP_ID, EQP_OBJ, CLAIM_TIME, CLAIM_USER_ID, CLAIM_USER_OBJ, DURATION_USED, DURATION_LIMIT,TIMES_USED, TIMES_USED_LIMIT, MAINT_TIME, MAINT_USER_ID, MAINT_USER_OBJ, SHELF_POSITION_X, SHELF_POSITION_Y, SHELF_POSITION_Z,ID FROM  OMPDRBL where DRBL_ID =?1  AND DRBL_CATEGORY = 'Fixture'";
        CimDurableDO durableDO     = cimJpaRepository.queryOne(sql, CimDurableDO.class, ObjectIdentifier.fetchValue(fixtureID));

        Validations.check(CimObjectUtils.isEmpty(durableDO), "This durable is not find");
        Infos.FixtureBr fixtureBr = new Infos.FixtureBr();
        fixtureStatusInqResult.setFixtureBRInfo(fixtureBr);
        fixtureStatusInqResult.getFixtureBRInfo().setDescription(durableDO.getDescription());
        fixtureStatusInqResult.getFixtureBRInfo().setFixturePartNumber(durableDO.getDurablePartNumber());
        fixtureStatusInqResult.getFixtureBRInfo().setFixtureSerialNumber(durableDO.getDurableSerialNumber());
        fixtureStatusInqResult.getFixtureBRInfo().setSupplierName(durableDO.getVendorName());
        fixtureStatusInqResult.getFixtureBRInfo().setUsageCheckFlag(durableDO.getUsageCheckRequired());

        Infos.FixtureStatusInfo fixtureStatusInfo = new Infos.FixtureStatusInfo();
        fixtureStatusInqResult.setFixtureStatusInfo(fixtureStatusInfo);
        fixtureStatusInqResult.getFixtureStatusInfo().setFixtureStatus(durableDO.getDurableId());
        fixtureStatusInqResult.getFixtureStatusInfo().setTransferStatus(durableDO.getTransferState());

        if(CimStringUtils.equals(durableDO.getDurableId(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) || CimStringUtils.equals(durableDO.getTransferState(), CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)){
            fixtureStatusInqResult.getFixtureStatusInfo().setEquipmentID(new ObjectIdentifier(durableDO.getEquipmentID(),durableDO.getEquipmentObj()));
        }else {
            fixtureStatusInqResult.getFixtureStatusInfo().setStockerID(new ObjectIdentifier(durableDO.getEquipmentID(),durableDO.getEquipmentObj()));
        }

        fixtureStatusInqResult.getFixtureStatusInfo().setXPosition(CimObjectUtils.toString(durableDO.getShelfPositionX()));
        fixtureStatusInqResult.getFixtureStatusInfo().setYPosition(CimObjectUtils.toString(durableDO.getShelfPositionY()));
        fixtureStatusInqResult.getFixtureStatusInfo().setZPosition(CimObjectUtils.toString(durableDO.getShelfPositionZ()));

        fixtureStatusInqResult.getFixtureStatusInfo().setLastClaimedTimeStamp(CimDateUtils.getTimestampAsString(durableDO.getClaimTime()));//这个需要询问对象不一致的问题
        fixtureStatusInqResult.getFixtureStatusInfo().setLastClaimedPerson(new ObjectIdentifier(durableDO.getClaimUserID(),durableDO.getClaimUserObj()));

        double runTime = (objCommon.getTimeStamp().getReportTimeStamp().getTime()-durableDO.getMaintainTime().getTime())/1000/60;
        Infos.FixturePm fixturePMInfo  = new Infos.FixturePm();
        fixtureStatusInqResult.setFixturePMInfo(fixturePMInfo);
        fixtureStatusInqResult.getFixturePMInfo().setRunTime(CimObjectUtils.toString(runTime));

        if(Double.doubleToLongBits(durableDO.getDurationLimit())==Double.doubleToLongBits(-1)){
            durableDO.setDurationLimit(-1d);
        }else{
            durableDO.setDurationLimit(durableDO.getDurationLimit()/60/1000);
        }

        fixtureStatusInqResult.getFixturePMInfo().setMaximumRunTime(CimObjectUtils.toString(durableDO.getDurationLimit()));


        fixtureStatusInqResult.getFixturePMInfo().setOperationStartCount(durableDO.getTimesUsed().longValue());
        fixtureStatusInqResult.getFixturePMInfo().setMaximumOperationStartCount(durableDO.getTimesUsedLimit().longValue());
        fixtureStatusInqResult.getFixturePMInfo().setLastMaintenanceTimeStamp(durableDO.getMaintainTime().toString());//注意强制转换的问题
        fixtureStatusInqResult.getFixturePMInfo().setLastMaintenancePerson(new ObjectIdentifier(durableDO.getMaintainUserID(),durableDO.getMaintainUserObj()));

        fixtureStatusInqResult.getFixturePMInfo().setIntervalBetweenPM(durableDO.getIntervalBetweenPM());

        double aDuration = objCommon.getTimeStamp().getReportTimeStamp().getTime()-objCommon.getTimeStamp().getReportTimeStamp().getTime();
        long minutes  =(long)aDuration/1000/60;
        fixtureStatusInqResult.getFixturePMInfo().setPassageTimeFromLastPM(minutes);

        CimDurableDurableGroupDO cimDurableDurableGroupDO =  cimJpaRepository.queryOne("SELECT DRBLGRP_ID,DRBLGRP_OBJ FROM   FRDRBL_DRBLGRP  WHERE  REFKEY =?1", CimDurableDurableGroupDO.class, durableDO.getBankID());

        if(!CimObjectUtils.isEmpty(cimDurableDurableGroupDO)){
            fixtureStatusInqResult.getFixtureBRInfo().setFixtureGroupID(new ObjectIdentifier(cimDurableDurableGroupDO.getDurableGroupId(),cimDurableDurableGroupDO.getDurableGroupObj()));

            CimDurableGroupDO cimDurableGroupDO = cimJpaRepository.queryOne("SELECT DESCRIPTION,DRBL_SUB_CATEGORY  FROM   FRDRBLGRP WHERE  DRBLGRP_ID = ?1", CimDurableGroupDO.class, cimDurableDurableGroupDO.getDurableGroupId());
            fixtureStatusInqResult.getFixtureBRInfo().setFixtureCategoryDescription(cimDurableGroupDO.getDescription());
            fixtureStatusInqResult.getFixtureBRInfo().getFixtureCategoryID().setValue(cimDurableGroupDO.getId());
        }


        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_FIXTURE);
        entityIdentifier.setObjectID(fixtureID);
        entityInhibitAttributes.setEntities(new ArrayList<>());
        entityInhibitAttributes.getEntities().add(entityIdentifier);

        if(fixtureID.getReferenceKey().length()==0){
            CimProcessDurable aFixture = baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
            entityInhibitAttributes.getEntities().get(0).getObjectID().setReferenceKey(aFixture.getPrimaryKey());
        }


        Infos.EntityInhibitCheckForEntitiesOut entityInhibitCheckForEntitiesOut = new Infos.EntityInhibitCheckForEntitiesOut();
        entityInhibitCheckForEntitiesOut = entityInhibitMethod.constraintCheckForEntities(objCommon, entityInhibitAttributes);
        //需要注意的是源码中有关于这个的判断，但是我们系统目前直接抓出这些错误，所以可以不加判断
        int n =entityInhibitCheckForEntitiesOut.getEntityInhibitInfo().size();
        List<Infos.EntityInhibitAttributes> entityInhibitions = new ArrayList<>();
        for(int i=0;i<n;i++){
            fixtureStatusInqResult.getEntityInhibitions().add(entityInhibitCheckForEntitiesOut.getEntityInhibitInfo().get(i).getEntityInhibitAttributes());
        }
        fixtureStatusInqResult.setEntityInhibitions(entityInhibitions);
        return fixtureStatusInqResult;
    }

    @Override
    public void fixtureStateChange(
            Infos.ObjCommon strObjCommonIn, ObjectIdentifier fixtureID,
            String fixtureStatus) {
        Boolean flag = false;

        CimProcessDurable aFixture;

        aFixture = baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
        if (fixtureStatus.equals(BizConstant.CIMFW_DURABLE_AVAILABLE)) {
            flag = aFixture.isAvailable();
            if (flag) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                        BizConstant.CIMFW_DURABLE_AVAILABLE);
            } else {
                try {
                    aFixture.makeAvailable();
                } catch (CoreFrameworkException e) {
                    Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                            BizConstant.CIMFW_DURABLE_AVAILABLE);
                    return;
                }
            }
        } else if (fixtureStatus.equals(BizConstant.CIMFW_DURABLE_INUSE)) {
            flag = aFixture.isInUse();
            if (flag) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                        BizConstant.CIMFW_DURABLE_INUSE);
            } else {
                try {
                    aFixture.makeAvailable();
                } catch (CoreFrameworkException e) {
                    Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                            BizConstant.CIMFW_DURABLE_INUSE);
                }
            }
        } else if (BizConstant.CIMFW_DURABLE_NOTAVAILABLE.equals(fixtureStatus)) {
            flag = aFixture.isNotAvailable();
            if (flag) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                        BizConstant.CIMFW_DURABLE_NOTAVAILABLE);
            } else {
                try {
                    aFixture.makeNotAvailable();
                } catch (CoreFrameworkException e) {
                    Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                            BizConstant.CIMFW_DURABLE_NOTAVAILABLE);
                }
            }
        } else if (BizConstant.CIMFW_DURABLE_SCRAPPED.equals(
                fixtureStatus))//CIMFWStrCmp(fixtureStatus,CIMFW_Durable_Scrapped) == 0
        {
            flag = aFixture.isScrapped();
            if (flag) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                        BizConstant.CIMFW_DURABLE_SCRAPPED);
            } else {
                try {
                    //aFixture->makeScrapped();
                    aFixture.makeScrapped();
                } catch (CoreFrameworkException e) {
                    Validations.check(true, retCodeConfig.getInvalidStateTrans(), "*****",
                            BizConstant.CIMFW_DURABLE_SCRAPPED);
                }
            }
        } else {
            Validations.check(true, retCodeConfig.getInvalidFixtureStat(),
                    ObjectIdentifier.fetchValue(fixtureID), fixtureStatus);
        }


        aFixture.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());


        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());


        aFixture.setLastClaimedPerson(aPerson);

        aFixture.setStateChangedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
        aFixture.setStateChangedPerson(aPerson);
    }

    public void fixtureUsageInfoReset(Infos.ObjCommon strObjCommonIn, ObjectIdentifier fixtureID) {
        CimProcessDurable aFixture;
        aFixture=baseCoreFactory.getBO(CimProcessDurable.class, fixtureID);
        aFixture.setTimesUsed( 0L );

        aFixture.setDurationUsed( 0D );

        aFixture.setLastMaintenanceTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp()) ;

        CimPerson aPerson ;
        aPerson=baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());

        aFixture.setLastMaintenancePerson( aPerson ) ;

        aFixture.setLastClaimedTimeStamp( strObjCommonIn.getTimeStamp().getReportTimeStamp() ) ;

        aFixture.setLastClaimedPerson( aPerson ) ;
    }

    @Override
    public Outputs.ObjFixtureChangeTransportStateOut fixtureChangeTransportState(
            Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID, ObjectIdentifier equipmentID,
            List<Infos.XferFixture> strXferFixture) {
        Outputs.ObjFixtureChangeTransportStateOut objFixtureChangeTransportStateOut = new Outputs.ObjFixtureChangeTransportStateOut();
        CimStorageMachine aStorageMachine = null;
        CimMachine aMachine = null;
        objFixtureChangeTransportStateOut.setStockerID(stockerID);
        objFixtureChangeTransportStateOut.setEquipmentID(equipmentID);
        objFixtureChangeTransportStateOut.setStrXferFixture(strXferFixture);
        int len = strXferFixture.size();
        boolean firstEquipment = true;
        boolean firstStocker   = true;

        String recordTimeStamp;
        for (int i=0; i<len; i++)
        {
            CimProcessDurable aFixture = baseCoreFactory.getBO(CimProcessDurable.class, strXferFixture.get(i).getFixtureID());

            if(CimObjectUtils.isEmpty(strXferFixture.get(i).getTransferStatusChangeTimeStamp())){
                recordTimeStamp = CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            }
            else{
                recordTimeStamp = strXferFixture.get(i).getTransferStatusChangeTimeStamp();
            }
            /*---------------------------------------*/
            /*   Check Cassette's XferChgTimeStamp   */
            /*---------------------------------------*/
            String xferStatChgTimeStamp = CimDateUtils.getTimestampAsString(aFixture.getTransferStatusChangedTimeStamp());

            if(recordTimeStamp.compareTo(xferStatChgTimeStamp)<0){
                continue;
            }
            aFixture.setTransferStatusChangedTimeStamp(CimDateUtils.convertToOrInitialTime(recordTimeStamp));
            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                aFixture.makeEquipmentIn();
                aFixture.setShelfPositionX(0);
                aFixture.setShelfPositionY(0);
                aFixture.setShelfPositionZ(0);
            }
            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                aFixture.makeEquipmentOut();
                aFixture.setShelfPositionX(0);
                aFixture.setShelfPositionY(0);
                aFixture.setShelfPositionZ(0);
            }
            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_SHELFIN)){
                aFixture.makeShelfIn();
                aFixture.setShelfPositionX(Integer.valueOf(strXferFixture.get(i).getXPosition()));
                aFixture.setShelfPositionY(Integer.valueOf(strXferFixture.get(i).getYPosition()));
                aFixture.setShelfPositionZ(Integer.valueOf(strXferFixture.get(i).getZPosition()));
            }
            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_SHELFOUT)){
                aFixture.makeShelfOut();
                aFixture.setShelfPositionX(0);
                aFixture.setShelfPositionY(0);
                aFixture.setShelfPositionZ(0);

            }
            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN )||
                    CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                if (firstEquipment){
                    aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                    firstEquipment = false; // Use aMachine for further equipmentID
                }
                aFixture.assignToMachine(aMachine);

            }else{
                if (firstStocker)
                {
                    aStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
                    firstStocker = false; // Use aStorageMachine for further stockerID
                }
                aFixture.assignToMachine(aStorageMachine);
            }
            aFixture.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            CimPerson aPerson;
            aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());
            aFixture.setLastClaimedPerson(aPerson);

        }
        return objFixtureChangeTransportStateOut;
    }
}