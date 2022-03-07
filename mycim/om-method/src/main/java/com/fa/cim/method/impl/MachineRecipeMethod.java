package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IMachineRecipeMethod;
import com.fa.cim.method.IPortMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimDurableProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;


/**
 * description:
 * <p>MachineRecipeMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2018/12/6         ********             ZQI               create file
 *
 * @author: ZQI
 * @date: 2018/12/6 13:33
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class MachineRecipeMethod  implements IMachineRecipeMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    @Qualifier("RecipeManagerCore")
    private com.fa.cim.newcore.bo.recipe.RecipeManager recipeManager;

    @Autowired
    private com.fa.cim.newcore.bo.pd.ProcessDefinitionManager newProcessDefinitionManager;


    @Override
    public List<Outputs.MachineRecipe> machineRecipeGetListForFPCDR(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID, String fpcCategory, String whiteDefSearchCriteria) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Validations.check(0 == CimStringUtils.length(machineRecipeID.getValue()), retCodeConfig.getNotFoundCode());
        String HV_BUFFER = "";
        String HV_TMPBUFFER = "";
        String searchCondition = "";
        HV_BUFFER += "SELECT RECIPE_ID, ID, DESCRIPTION, TEMP_MODE_FLAG, DOC_CATEGORY FROM OMRCP WHERE ";
        // The Recipe that has an active version of ## is excluded.
        searchCondition += String.format("VERSION_ID <> '%s' AND",BizConstant.SP_ACTIVE_VERSION);
        HV_BUFFER += searchCondition;
        // machineRecipeID
        HV_TMPBUFFER += String.format(" RECIPE_ID LIKE '%s'",machineRecipeID.getValue());
        // whiteDefSearchCriteria
        if (CimStringUtils.equals(whiteDefSearchCriteria,BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)){
            HV_TMPBUFFER += String.format(" AND TEMP_MODE_FLAG = %s","1");
        }
        else if (CimStringUtils.equals(whiteDefSearchCriteria,BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)){
            HV_TMPBUFFER += String.format(" AND TEMP_MODE_FLAG = %s","0");
        }
        else {
            throw new ServiceException(retCodeConfig.getNotFoundCode());
        }
        // FPCCategory
        if (0 != CimStringUtils.length(fpcCategory)){
            HV_TMPBUFFER += String.format(" AND (DOC_CATEGORY = '%s' OR DOC_CATEGORY = '')",fpcCategory);
        }
        HV_BUFFER += HV_TMPBUFFER;

        //-------------------
        // SQL PREPARE
        //-------------------
        List<CimMachineRecipeDO> cimMachineRecipeDOList = cimJpaRepository.query(HV_BUFFER, CimMachineRecipeDO.class);
        int nMachineRecipeCnt = 0;
        int t_len = 100;
        List<Outputs.MachineRecipe> strMachineRecipeList = new ArrayList<>();
        for (CimMachineRecipeDO cimMachineRecipeDO : cimMachineRecipeDOList) {
            if (nMachineRecipeCnt >= t_len){
                t_len += 100;
            }
            Outputs.MachineRecipe strMachineRecipe = new Outputs.MachineRecipe();
            strMachineRecipe.setMachineRecipeID(new ObjectIdentifier(cimMachineRecipeDO.getRecipeID()));
            strMachineRecipe.setDescription(cimMachineRecipeDO.getDescription());
            strMachineRecipe.setFpcCategory(cimMachineRecipeDO.getProcessCategory());
            strMachineRecipe.setWhiteDefFlag(cimMachineRecipeDO.getWhiteFlag());
            strMachineRecipeList.add(strMachineRecipe);
            nMachineRecipeCnt++;
        }
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/

        return strMachineRecipeList;

    }

    @Override
    public List<Outputs.MachineRecipe> machineRecipeGetListByPDForFPC(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier pdID, String fpcCategory, String whiteDefSearchCriteria) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        //Outputs.MachineRecipe out = new Outputs.MachineRecipe();
        String strSearchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        long searchCondition = CimStringUtils.isEmpty(strSearchCondition) ? 0 : Long.parseLong(strSearchCondition);

        /*--------------------*/
        /*   Get Lot Object   */
        /*--------------------*/
        log.debug("【step1】 Get Lot Object   ");
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        Validations.check(lot == null, retCodeConfig.getNotFoundCode());
        /*---------------------------------------*/
        /*   Get Process Definition Object       */
        /*---------------------------------------*/
        log.debug("【step2】Get Process Definition Object  ");
        CimProcessDefinition processDefinition = newProcessDefinitionManager.findProcessDefinitionNamed(pdID.getValue());
        Validations.check(processDefinition == null, retCodeConfig.getNotFoundProcessDefinition());
        /*---------------------------------------*/
        /*   Get Product Spec Object             */
        /*---------------------------------------*/
        log.debug("【step3】 Get Product Spec Object  ");
        CimProductSpecification productSpecification = lot.getProductSpecification();
        Validations.check(productSpecification == null, retCodeConfig.getNotFoundProcessDefinition());
        //Get Sub Lot Type
        log.debug("【step4】 Get Sub Lot Type  ");
        String subLotType = lot.getSubLotType();
        Validations.check(CimStringUtils.isEmpty(subLotType), retCodeConfig.getNotFoundSubLotType(), subLotType);
        /*---------------------------------------*/
        /*   Get Logical Recipe Object           */
        /*---------------------------------------*/
        log.debug("【step5】 Get Logical Recipe Object  ");
        com.fa.cim.newcore.bo.recipe.CimLogicalRecipe logicalRecipe = processDefinition.findLogicalRecipeFor(productSpecification);
        Validations.check(logicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
        /*---------------------------------------*/
        /* Get Machine Objects                   */
        /*---------------------------------------*/
        log.debug("【step6】 Get Machine Objects    ");
        List<CimMachine> machines = new ArrayList<>();
        if (ObjectIdentifier.isEmpty(equipmentID)) {
            machines = processDefinition.findMachinesFor(productSpecification);
            Validations.check(null == machines || machines.size() == 0, retCodeConfig.getNotFoundMachine());
        } else {
            CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
            machines.add(equipment);
        }
        int nMachineLen = machines.size();
        /*---------------------------------------*/
        /* Get Machine Recipe Informations       */
        /*---------------------------------------*/
        log.debug("【step7】 Get Machine Recipe Informations    ");
        List<Outputs.MachineRecipe> machineRecipeList = new ArrayList<>();
        int nMachineRecipeCnt = 0;
        int size = machineRecipeList.size();
        for (int nMachineCnt = 0; nMachineCnt < nMachineLen; nMachineCnt++) {
            /*---------------------------------------*/
            /* Get Machine Recipe Object             */
            /*---------------------------------------*/
            log.debug("【step8】 Get Machine Recipe Object     ");
            com.fa.cim.newcore.bo.recipe.CimMachineRecipe aMachineRecipe = null;
            if (searchCondition == 1) {
                aMachineRecipe = logicalRecipe.findMachineRecipeFor(lot, machines.get(nMachineCnt));
            } else {
                aMachineRecipe = logicalRecipe.findMachineRecipeForSubLotType(machines.get(nMachineCnt), subLotType);
            }
            if (null != aMachineRecipe) {
                String processCategory = aMachineRecipe.getFPCCategory();
                Boolean whiteFlag = aMachineRecipe.isWhiteFlagOn();
                //FPCCategory Check
                if (!CimObjectUtils.isEmpty(fpcCategory)) {
                    if (!CimStringUtils.equals(fpcCategory, processCategory)) {
                        continue;
                    }
                }
                //WhiteDef Flag Check
                if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
                    if (!whiteFlag) {
                        continue;
                    }
                } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {
                    if (whiteFlag) {
                        continue;
                    }
                } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL)) {

                } else {
                    throw new ServiceException(retCodeConfig.getInvalidParameter());
                }
                // Get Machine Recipe ID
                ObjectIdentifier machineRecipeId = aMachineRecipe.getMachineRecipeID();
                boolean foundFlag = false;
                for (int i = 0; i < nMachineRecipeCnt; i++) {
                    if (CimStringUtils.equals(machineRecipeId.getValue(), machineRecipeList.get(i).getMachineRecipeID().getValue())) {
                        foundFlag = true;
                    }
                }
                if (!foundFlag) {
                    Outputs.MachineRecipe machineRecipe = new Outputs.MachineRecipe();
                    machineRecipe.setMachineRecipeID(machineRecipeId);
                    machineRecipe.setDescription(aMachineRecipe.getDescription());
                    machineRecipe.setFpcCategory(aMachineRecipe.getFPCCategory());
                    machineRecipe.setWhiteDefFlag(aMachineRecipe.isWhiteFlagOn());
                    machineRecipeList.add(machineRecipe);
                    nMachineRecipeCnt++;
                }
            }
        }
        return machineRecipeList;
    }

    @Override
    public void machineRecipeDownloadingInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID) {
        CimMachineRecipe aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, machineRecipeID);
        Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
        /*---------------------------------*/
        /*   Set Downloading Information   */
        /*---------------------------------*/
        RecipeDTO.DownloadedMachine downloadedMachine = new RecipeDTO.DownloadedMachine();
        downloadedMachine.setMachine(equipmentID);
        downloadedMachine.setUser(objCommon.getUser().getUserID());
        downloadedMachine.setLastTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        aMachineRecipe.addDownloadedMachine(downloadedMachine);
    }

    @Override
    public List<Infos.MachineRecipeInfo> machineRecipeGetListByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        List<Infos.MachineRecipeInfo> machineRecipeInfos = new ArrayList<>();
        /*------------------------*/
        /*   Get Machine Object   */
        /*------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));

        /*--------------------------------------*/
        /*   Get Machine Recipe For Equipment and set Machine Recipe Information  */
        /*--------------------------------------*/
        List<CimMachineRecipe> aMachineRecipeSeq = recipeManager.allMachineRecipesFor(aMachine);
        if (!CimArrayUtils.isEmpty(aMachineRecipeSeq)){
            for (CimMachineRecipe aMachineRecipe : aMachineRecipeSeq) {
                Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                Infos.MachineRecipeInfo machineRecipeInfo = new Infos.MachineRecipeInfo();
                machineRecipeInfos.add(machineRecipeInfo);
                machineRecipeInfo.setMachineRecipeID(new ObjectIdentifier(aMachineRecipe.getIdentifier(), aMachineRecipe.getPrimaryKey()));
                machineRecipeInfo.setPhysicalRecipeID(aMachineRecipe.getPhysicalRecipeId());
                machineRecipeInfo.setFileLocation(aMachineRecipe.getRecipeBodyFileLocation());
                machineRecipeInfo.setFileName(aMachineRecipe.getRecipeBodyFileName());
                machineRecipeInfo.setFormatFlag(CimBooleanUtils.isTrue(aMachineRecipe.isFormatFlagOn()));
                CimMachine anUploadMachine = aMachineRecipe.getLastUploadedMachine();
                machineRecipeInfo.setUploadEquipmentID(anUploadMachine == null ? null : new ObjectIdentifier(anUploadMachine.getIdentifier(), anUploadMachine.getPrimaryKey()));
                machineRecipeInfo.setLastUploadUser(new ObjectIdentifier(aMachineRecipe.getLastUploadPersonID()));
                machineRecipeInfo.setLastUploadTimeStamp(aMachineRecipe.getLastUploadTimeStamp());
                machineRecipeInfo.setLastDeleteUser(new ObjectIdentifier(aMachineRecipe.getLastDeletePersonID()));
                machineRecipeInfo.setLastDeleteTimeStamp(aMachineRecipe.getLastDeleteTimeStamp());
                /*--------------------------*/
                /*   Download Information   */
                /*--------------------------*/
                List<RecipeDTO.DownloadedMachine> downloadedMachineSeq = aMachineRecipe.allDownloadedMachines();
                List<Infos.RecipeDownloadEquipmentInfo> recipeDownloadEquipmentInfos = new ArrayList<>();
                downloadedMachineSeq.forEach(x -> recipeDownloadEquipmentInfos.add(new Infos.RecipeDownloadEquipmentInfo(x.getMachine(), x.getUser(), x.getLastTimeStamp())));
                machineRecipeInfo.setStrRecipeDownloadEquipmentInfo(recipeDownloadEquipmentInfos);
            }
        }
        return machineRecipeInfos;
    }

    @Override
    public void machineRecipeDeletionInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID) {
        /*-------------------------------*/
        /*   Get Machine Recipe Object   */
        /*-------------------------------*/
        CimMachineRecipe aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, machineRecipeID);
        Validations.check(aMachineRecipe == null, new OmCode(retCodeConfig.getNotFoundMachineRecipe(), machineRecipeID.getValue()));
        /*-----------------------*/
        /*   Get Person Object   */
        /*-----------------------*/
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), ObjectIdentifier.fetchValue(objCommon.getUser().getUserID())));
        /*------------------------------*/
        /*   Set Deletion Information   */
        /*------------------------------*/
        /*=== Delete Person ===*/
        aMachineRecipe.setLastDeletePerson(aPerson);
        /*=== Delete TimeStamp ===*/
        aMachineRecipe.setLastDeleteTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public void machineRecipeUploadingInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID, String fileName, boolean formatFlag) {
        /*------------------------*/
        /*   Get Machine Object   */
        /*------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        Validations.check(aMachine == null, retCodeConfig.getNotFoundEquipment());
        /*-------------------------------*/
        /*   Get Machine Recipe Object   */
        /*-------------------------------*/
        CimMachineRecipe aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class,machineRecipeID);
        Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());

        /*-----------------------*/
        /*   Get Person Object   */
        /*-----------------------*/
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class,objCommon.getUser().getUserID());
        Validations.check(aPerson == null, retCodeConfig.getNotFoundPerson());

        /*-------------------------------*/
        /*   Set Uploading Information   */
        /*-------------------------------*/
        /*=== File Name ===*/
        aMachineRecipe.setRecipeBodyFileName(fileName);

        /*=== Format Flag ===*/
        if (formatFlag) {
            aMachineRecipe.makeFormatFlagOn();
        } else {
            aMachineRecipe.makeFormatFlagOff();
        }

        /*=== Upload Machine ===*/
        aMachineRecipe.setLastUploadedMachine(aMachine);

        /*=== Upload Person ===*/
        aMachineRecipe.setLastUploadPerson(aPerson);

        /*=== Upload TimeStamp ===*/
        aMachineRecipe.setLastUploadTimeStamp( objCommon.getTimeStamp().getReportTimeStamp());
    }

    @Override
    public List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagement(Infos.ObjCommon objCommon, Inputs.ObjEquipmentRecipeGetListForRecipeBodyManagementIn objEquipmentRecipeGetListForRecipeBodyManagementIn) {
        ObjectIdentifier equipmentID = objEquipmentRecipeGetListForRecipeBodyManagementIn.getEquipmentID();
        List<Infos.StartCassette> strStartCassetteList = objEquipmentRecipeGetListForRecipeBodyManagementIn.getStartCassetteList();
        int startCassetteLen = CimArrayUtils.getSize(strStartCassetteList);
        List<Infos.RecipeBodyManagement> strRecipeBodyManagementList = new ArrayList<>();
        Validations.check(startCassetteLen == 0, retCodeConfig.getInvalidParameter());
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (!CimObjectUtils.isEmpty(searchCondition)){
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        //--------------------------------
        //   Get Equipment's OnlineMode
        //--------------------------------
        Outputs.ObjPortResourceCurrentOperationModeGetOut objPortResourceCurrentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentID, strStartCassetteList.get(0).getLoadPortID());
        String onlineMode = objPortResourceCurrentOperationModeGetOut.getOperationMode().getOnlineMode();
        if (!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
            //-----------------------------------------
            //   Get Equipment's Recipe Manage Flag
            //-----------------------------------------
            boolean recipeBodyManageFlag = equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID);
            if (recipeBodyManageFlag){
                //------------------------------------------------------
                //   Get Machine Recipe List for Recipe Body Management
                //------------------------------------------------------
                int machineRcpCnt = 0;
                for (int i = 0; i < startCassetteLen; i++){
                    List<Infos.LotInCassette> lotInCassetteList = strStartCassetteList.get(i).getLotInCassetteList();
                    int lotInCassetteLength = CimArrayUtils.getSize(lotInCassetteList);
                    for (int j = 0; j < lotInCassetteLength; j++){
                        boolean operationStartFlag = lotInCassetteList.get(j).getMoveInFlag();
                        if (!operationStartFlag){
                            continue;
                        }
                        //------------------------------------------------
                        //   GetMachine Recipe's RecipeBodyConfirm Flag
                        //------------------------------------------------
                        com.fa.cim.newcore.bo.recipe.CimMachineRecipe aMachineRecipe = null;
                        if (ObjectIdentifier.isEmptyWithValue(lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID())){
                            //===== get Lot Object =====//
                            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassetteList.get(j).getLotID());
                            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotInCassetteList.get(j).getLotID().getValue()));
                            //===== get LotType =====//
                            String subLotType = aLot.getSubLotType();
                            //===== get ProdSpec =====//
                            CimProductSpecification aProdSpec = aLot.getProductSpecification();
                            Validations.check(aProdSpec == null, retCodeConfig.getNotFoundProductSpec());
                            //===== get PO Object =====//
                            com.fa.cim.newcore.bo.pd.CimProcessOperation aPO = aLot.getProcessOperation();
                            Validations.check(aPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "*****", lotInCassetteList.get(j).getLotID().getValue()));
                            //===== get LogicalRecipe Object =====//
                            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aPO.findLogicalRecipeFor(aProdSpec);
                            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
                            //===== get Machine Object =====//
                            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
                            //===== get MachineRecipe Object =====//
                            if (searchCondition == 1){
                                aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                            } else {
                                aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                            }
                            Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                        } else {
                            aMachineRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimMachineRecipe.class, lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID());
                            Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                        }
                        //------------------------------------
                        // Get Recipe Body Management Info.
                        //------------------------------------
                        String tmpMachineRecipeID = aMachineRecipe.getIdentifier();
                        String physicalRecipeID = aMachineRecipe.getPhysicalRecipeId();
                        String fileLocation = aMachineRecipe.getRecipeBodyFileLocation();
                        String fileName = aMachineRecipe.getRecipeBodyFileName();
                        boolean formatFlag = aMachineRecipe.isFormatFlagOn();
                        boolean forceDownLoadFlag = aMachineRecipe.isForceDownloadFlagOn();
                        boolean recipeBodyConfirmFlag = aMachineRecipe.isRecipeBodyConfirmFlagOn();
                        boolean conditionalDownLoadFlag = aMachineRecipe.isConditionalDownloadFlagOn();
                        if (forceDownLoadFlag || recipeBodyConfirmFlag){
                            // Add to machineRecipeID list.
                            boolean foundFlag = false;
                            for (int k = 0; k < machineRcpCnt; k++){
                                if (ObjectIdentifier.equalsWithValue(strRecipeBodyManagementList.get(k).getMachineRecipeId(), tmpMachineRecipeID)){
                                    foundFlag = true;
                                    break;
                                }
                            }
                            if (!foundFlag){
                                // Add
                                Infos.RecipeBodyManagement recipeBodyManagement = new Infos.RecipeBodyManagement();
                                strRecipeBodyManagementList.add(recipeBodyManagement);
                                recipeBodyManagement.setMachineRecipeId(new ObjectIdentifier(tmpMachineRecipeID));
                                recipeBodyManagement.setPhysicalRecipeId(physicalRecipeID);
                                recipeBodyManagement.setFileLocation(fileLocation);
                                recipeBodyManagement.setFileName(fileName);
                                recipeBodyManagement.setFormatFlag(formatFlag);
                                recipeBodyManagement.setForceDownLoadFlag(forceDownLoadFlag);
                                recipeBodyManagement.setRecipeBodyConfirmFlag(recipeBodyConfirmFlag);
                                recipeBodyManagement.setConditionalDownLoadFlag(conditionalDownLoadFlag);
                                machineRcpCnt++;
                            }
                        }
                    }
                }
            }
        }
        return strRecipeBodyManagementList;
    }

    @Override
    public List<ObjectIdentifier> machineRecipeAllEquipmentGetDR(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipe) {
        List<ObjectIdentifier> out = new ArrayList<>();
        /*--------------------------------*/
        /*   Get Equipment from OMRCP    */
        /*--------------------------------*/
        List<Object[]> query = cimJpaRepository.query("SELECT OMRCP_EQP.EQP_ID,\n" +
                "                        OMRCP_EQP.EQP_RKEY\n" +
                "                 FROM   OMRCP, OMRCP_EQP\n" +
                "                 WHERE  OMRCP.RECIPE_ID = ?1\n" +
                "                 AND    OMRCP.ID = OMRCP_EQP.REFKEY ", ObjectIdentifier.fetchValue(machineRecipe));
        if (CimArrayUtils.isNotEmpty(query)) {
            for (Object[] objects : query) {
                out.add(ObjectIdentifier.build(objects[0].toString(), objects[1].toString()));
            }
        }

        return out;
    }

    @Override
    public List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagement(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, List<Infos.StartCassette> startCassettes) {
        List<Infos.RecipeBodyManagement> recipeBodyManagements = new ArrayList<>();
        Validations.check(CimArrayUtils.isEmpty(startCassettes), retCodeConfig.getInvalidParameter());
        int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();

        //   Get eqp's OnlineMode
        Outputs.ObjPortResourceCurrentOperationModeGetOut objPortResourceCurrentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentId, startCassettes.get(0).getLoadPortID());
        String onlineMode = objPortResourceCurrentOperationModeGetOut.getOperationMode().getOnlineMode();
        if (!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            //-----------------------------------------
            //   Get Equipment's Recipe Manage Flag
            //-----------------------------------------
            boolean recipeBodyManageFlag = equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentId);
            if (recipeBodyManageFlag) {
                //   Get Machine Recipe List for Recipe Body Management
                for (Infos.StartCassette startCassette : startCassettes) {
                    List<Infos.LotInCassette> lotInCassettes = startCassette.getLotInCassetteList();
                    for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                        if (!lotInCassette.getMoveInFlag()) {
                            continue;
                        }
                        //------------------------------------------------
                        //   GetMachine Recipe's RecipeBodyConfirm Flag
                        //------------------------------------------------
                        CimMachineRecipe aMachineRecipe = null;
                        if (ObjectIdentifier.isEmptyWithValue(lotInCassette.getStartRecipe().getMachineRecipeID())) {
                            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                            Validations.check(null == aLot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(lotInCassette.getLotID())));
                            String subLotType = aLot.getSubLotType();
                            CimProductSpecification aProdSpec = aLot.getProductSpecification();
                            Validations.check(aProdSpec == null, retCodeConfig.getNotFoundProductSpec());
                            //===== get PO Object =====//
                            CimProcessOperation aPO = aLot.getProcessOperation();
                            Validations.check(aPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "*****", lotInCassette.getLotID().getValue()));
                            // get logicalrecipe Object
                            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aPO.findLogicalRecipeFor(aProdSpec);
                            Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
                            //===== get Machine Object =====//
                            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentId);
                            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentId.getValue()));
                            if (searchCondition == 1) {
                                aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                            } else {
                                aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                            }
                            Validations.check(null == aMachineRecipe, retCodeConfig.getNotFoundMachineRecipe());
                        } else {
                            aMachineRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimMachineRecipe.class, lotInCassette.getStartRecipe().getMachineRecipeID());
                            Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                        }
                        // Get Recipe Body Management Info.
                        String tmpMachineRecipeID = aMachineRecipe.getIdentifier();
                        String physicalRecipeID = aMachineRecipe.getPhysicalRecipeId();
                        String fileLocation = aMachineRecipe.getRecipeBodyFileLocation();
                        String fileName = aMachineRecipe.getRecipeBodyFileName();
                        boolean formatFlag = aMachineRecipe.isFormatFlagOn();
                        boolean forceDownLoadFlag = aMachineRecipe.isForceDownloadFlagOn();
                        boolean recipeBodyConfirmFlag = aMachineRecipe.isRecipeBodyConfirmFlagOn();
                        boolean conditionalDownLoadFlag = aMachineRecipe.isConditionalDownloadFlagOn();
                        if (forceDownLoadFlag || recipeBodyConfirmFlag) {
                            // Add to machineRecipeID list.
                            boolean foundFlag = false;
                            for (Infos.RecipeBodyManagement recipeBodyManagement : recipeBodyManagements) {
                                if (ObjectIdentifier.equalsWithValue(recipeBodyManagement.getMachineRecipeId(), tmpMachineRecipeID)) {
                                    foundFlag = true;
                                    break;
                                }
                            }
                            if (!foundFlag) {
                                Infos.RecipeBodyManagement recipeBodyManagement = new Infos.RecipeBodyManagement();
                                recipeBodyManagement.setMachineRecipeId(new ObjectIdentifier(tmpMachineRecipeID));
                                recipeBodyManagement.setPhysicalRecipeId(physicalRecipeID);
                                recipeBodyManagement.setFileLocation(fileLocation);
                                recipeBodyManagement.setFileName(fileName);
                                recipeBodyManagement.setFormatFlag(formatFlag);
                                recipeBodyManagement.setForceDownLoadFlag(forceDownLoadFlag);
                                recipeBodyManagement.setRecipeBodyConfirmFlag(recipeBodyConfirmFlag);
                                recipeBodyManagement.setConditionalDownLoadFlag(conditionalDownLoadFlag);
                                recipeBodyManagements.add(recipeBodyManagement);
                            }
                        }
                    }
                }
            }
        }
        return recipeBodyManagements;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strMachineRecipeGetListForRecipeBodyManagementForDurablein
     * @return java.util.List<com.fa.cim.dto.Infos.RecipeBodyManagement>
     * @exception
     * @author ho
     * @date 2020/7/3 13:01
     */
    public List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagementForDurable(
            Infos.ObjCommon                                                 strObjCommonIn,
            Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn strMachineRecipeGetListForRecipeBodyManagementForDurablein ) {
        List<Infos.RecipeBodyManagement>        strMachineRecipeGetListForRecipeBodyManagementForDurableout;

        log.info( "PPTManager_i::machineRecipe_GetListForRecipeBodyManagementForDurable" );

        Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn strInParm = strMachineRecipeGetListForRecipeBodyManagementForDurablein;

        // Result structure
        int machineRcpCnt = 0;
        int nMax1 = BizConstant.SP_CAPACITY_INCREMENT_10;
        List<Infos.RecipeBodyManagement> strRecipeBodyManagementSeq;
        strRecipeBodyManagementSeq=new ArrayList<>(nMax1);

        //--------------------------------
        //   Get Equipment's OnlineMode
        //--------------------------------
        String onlineMode;
        String strEquipmentonlineModeGetout;
        strEquipmentonlineModeGetout = equipmentMethod.equipmentOnlineModeGet(
                strObjCommonIn,
                strInParm.getEquipmentID() );
        onlineMode = strEquipmentonlineModeGetout;

        if ( !CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE) ) {
            log.info("{}", "onlineMode != SP_Eqp_OnlineMode_Offline");

            boolean strEquipmentrecipeBodyManageFlagGetout;
            strEquipmentrecipeBodyManageFlagGetout = equipmentMethod.equipmentRecipeBodyManageFlagGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID() );

            boolean recipeBodyManageFlag = CimBooleanUtils.isTrue(strEquipmentrecipeBodyManageFlagGetout);
            if ( recipeBodyManageFlag ) {
                log.info("{}", "recipeBodyManageFlag == TRUE");

                //------------------------------------------------------
                //   Get Machine Recipe List for Recipe Body Management
                //------------------------------------------------------
                List<Infos.DurableStartRecipe> strDurableStartRecipes = strInParm.getStrDurableStartRecipes();
                int recipeLen = CimArrayUtils.getSize(strDurableStartRecipes);
                if(recipeLen <= 0 || CimStringUtils.length(ObjectIdentifier.fetchValue(strDurableStartRecipes.get(0).getMachineRecipeId())) == 0) {
                    log.info("{}", "strDurableStartRecipes.length == 0 or strDurableStartRecipes[0].machineRecipeID is blank ");

                    int durableLen = CimArrayUtils.getSize(strInParm.getStrStartDurables());
                    CimDurableProcessOperation durablePOObj = null;
                    CimMachine equipmentObj;
                    equipmentObj=baseCoreFactory.getBO(CimMachine.class,
                            strMachineRecipeGetListForRecipeBodyManagementForDurablein.getEquipmentID());

                    if( durableLen > 0 ) {
                        log.info("{}", "durableLen > 0");
                        int durableSeq = 0;
                        if ( CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE) ) {
                            log.info("{}", "durableCategory is Cassette");
                            CimCassette aCassette;
                            aCassette=baseCoreFactory.getBO(CimCassette.class,
                                    strInParm.getStrStartDurables().get(durableSeq).getDurableId());

                            durablePOObj = aCassette.getDurableProcessOperation();

                        } else if ( CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD) ) {
                            log.info("{}", "durableCategory is ReticlePod");
                            CimReticlePod aReticlePod;
                            aReticlePod=baseCoreFactory.getBO(CimReticlePod.class,
                                    strInParm.getStrStartDurables().get(durableSeq).getDurableId());

                            durablePOObj = aReticlePod.getDurableProcessOperation();

                        } else if ( CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE) ) {
                            log.info("{}", "durableCategory is Reticle");
                            CimProcessDurable aReticle;
                            aReticle=baseCoreFactory.getBO(CimProcessDurable.class,
                                    strInParm.getStrStartDurables().get(durableSeq).getDurableId());

                            durablePOObj = aReticle.getDurableProcessOperation();
                        }

                        Validations.check(durablePOObj==null,retCodeConfig.getNotFoundDurablePo(),
                                ObjectIdentifier.fetchValue(strInParm.getStrStartDurables().get(durableSeq).getDurableId()));

                        CimLogicalRecipe logicalRecipeObj;
                        CimProductSpecification nilObj = null;
                        logicalRecipeObj = durablePOObj.findLogicalRecipeFor(nilObj);

                        if(logicalRecipeObj==null) {
                            Validations.check(true,retCodeConfig.getNotFoundLogicalRecipe());
                        }

                        CimMachineRecipe aMachineRecipe;
                        String subLotType = "";
                        aMachineRecipe = logicalRecipeObj.findMachineRecipeForSubLotType(equipmentObj, subLotType);

                        Validations.check(aMachineRecipe==null,retCodeConfig.getNotFoundMachineRecipe());

                        // strDurableStartRecipes.length(1);
                        strDurableStartRecipes.get(0).setMachineRecipeId(
                                ObjectIdentifier.build(aMachineRecipe.getIdentifier(),aMachineRecipe.getPrimaryKey()));
                    }
                }

                for (int iCnt1 = 0; iCnt1 < CimArrayUtils.getSize(strDurableStartRecipes); iCnt1++ ) {
                    log.info("{} {}", "loop to strDurableStartRecipes.length()", iCnt1);
                    CimMachineRecipe aMachineRecipe;
                    aMachineRecipe=baseCoreFactory.getBO(CimMachineRecipe.class,
                            strDurableStartRecipes.get(iCnt1).getMachineRecipeId());

                    //------------------------------------
                    // Get Recipe Body Management Info.
                    //------------------------------------
                    log.info("{}", "Get Recipe Body Management Info.");
                    String tmpMachineRecipeID;
                    String physicalRecipeID;
                    String fileLocation;
                    String fileName;
                    boolean formatFlag              = false;
                    boolean forceDownLoadFlag       = false;
                    boolean recipeBodyConfirmFlag   = false;
                    boolean conditionalDownLoadFlag = false;
                    tmpMachineRecipeID = "";
                    physicalRecipeID   = "";
                    fileLocation       = "";
                    fileName           = "";

                    tmpMachineRecipeID = aMachineRecipe.getIdentifier();

                    physicalRecipeID = aMachineRecipe.getPhysicalRecipeId();

                    fileLocation = aMachineRecipe.getRecipeBodyFileLocation();

                    fileName = aMachineRecipe.getRecipeBodyFileName();

                    formatFlag = CimBooleanUtils.isTrue(aMachineRecipe.isFormatFlagOn());

                    forceDownLoadFlag = CimBooleanUtils.isTrue(aMachineRecipe.isForceDownloadFlagOn());

                    recipeBodyConfirmFlag = CimBooleanUtils.isTrue(aMachineRecipe.isRecipeBodyConfirmFlagOn());

                    conditionalDownLoadFlag = CimBooleanUtils.isTrue(aMachineRecipe.isConditionalDownloadFlagOn());


                    log.info("{} {}", " Machine Recipe ID          ", tmpMachineRecipeID);
                    log.info("{} {}", " Physical Recipe ID         ", physicalRecipeID);
                    log.info("{} {}", " File Location              ", fileLocation);
                    log.info("{} {}", " File Name                  ", fileName);
                    log.info("{} {}", " Format Flag                ", (formatFlag?"True":"False"));
                    log.info("{} {}", " Force Down Load Flag       ", (forceDownLoadFlag?"True":"False"));
                    log.info("{} {}", " Recipe Body Confirm Flag   ", (recipeBodyConfirmFlag?"True":"False"));
                    log.info("{} {}", " Conditional Down Load Flag ", (conditionalDownLoadFlag?"True":"False"));

                    log.info("{}", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    log.info("{}", "Only Machine Recipe needs support are colledted.");
                    if ( forceDownLoadFlag || recipeBodyConfirmFlag ) {
                        log.info("{}", "TRUE == forceDownLoadFlag || TRUE == recipeBodyConfirmFlag");
                        // Add to machineRecipeID list.
                        boolean foundFlag = false;
                        for ( int iCnt2=0; iCnt2 < machineRcpCnt; iCnt2++ ) {
                            log.info("{} {}", "loop to machineRcpCnt", iCnt2);
                            if ( CimStringUtils.equals(ObjectIdentifier.fetchValue(strRecipeBodyManagementSeq.get(iCnt2).getMachineRecipeId()), tmpMachineRecipeID) ) {
                                log.info("{}", "strRecipeBodyManagementSeq[iCnt2].machineRecipeID.identifier == tmpMachineRecipeID");
                                foundFlag = true;
                                break;
                            }
                        }
                        if ( !foundFlag ) {
                            log.info("{}", "FALSE == foundFlag");

                            log.info("{} {}", "Add to machineRecipeID list", tmpMachineRecipeID, machineRcpCnt);

                            log.info("{} {}", "# now count and now max is", machineRcpCnt, nMax1);
                            if ( machineRcpCnt >= nMax1 ) {
                                log.info("{}", "machineRcpCnt>=nMax1, increase the sequence by 10.");
                                nMax1 += BizConstant.SP_CAPACITY_INCREMENT_10;
//                                strRecipeBodyManagementSeq.length(nMax1);
                                log.info("{} {}", "# nMax1", nMax1);
                            }

                            strRecipeBodyManagementSeq.add(new Infos.RecipeBodyManagement());
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setMachineRecipeId               (ObjectIdentifier.buildWithValue(tmpMachineRecipeID));
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setPhysicalRecipeId              (physicalRecipeID);
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setFileLocation                  (fileLocation);
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setFileName                      (fileName);
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setFormatFlag                    (formatFlag);
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setForceDownLoadFlag             (forceDownLoadFlag);
                            strRecipeBodyManagementSeq.get(machineRcpCnt).setRecipeBodyConfirmFlag         (recipeBodyConfirmFlag);
                            strRecipeBodyManagementSeq.get(machineRcpCnt++).setConditionalDownLoadFlag     (conditionalDownLoadFlag);
                        }
                    }
                }
            }
        }

        //strRecipeBodyManagementSeq.length(machineRcpCnt);
        strMachineRecipeGetListForRecipeBodyManagementForDurableout = strRecipeBodyManagementSeq;

        //--------------------//
        //  Return to Caller  //
        //--------------------//
        log.info( "PPTManager_i::machineRecipe_GetListForRecipeBodyManagementForDurable" );
        return strMachineRecipeGetListForRecipeBodyManagementForDurableout;
    }
}
