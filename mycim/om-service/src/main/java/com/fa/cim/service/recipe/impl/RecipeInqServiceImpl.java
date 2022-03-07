package com.fa.cim.service.recipe.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IMachineRecipeMethod;
import com.fa.cim.method.ITCSMethod;
import com.fa.cim.method.impl.RecipeMethod;
import com.fa.cim.service.recipe.IRecipeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@OmService
@Slf4j
public class RecipeInqServiceImpl implements IRecipeInqService {
    @Autowired
    RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private RecipeMethod recipeMethod;
    /**
     * The Method is txUploadedRecipeIdListByEqpInq.
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return RetCode<Results.UploadedRecipeIdListByEqpInqResult>
     */
    @Override
    public Results.UploadedRecipeIdListByEqpInqResult sxUploadedRecipeIdListByEqpInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------------*/
        /*   Get Machine Recipe Information   */
        /*------------------------------------*/
        Results.UploadedRecipeIdListByEqpInqResult uploadedRecipeIdListByEqpInqResult = new Results.UploadedRecipeIdListByEqpInqResult();
        uploadedRecipeIdListByEqpInqResult.setEquipmentID(equipmentID);
        uploadedRecipeIdListByEqpInqResult.setStrMachineRecipeInfo(machineRecipeMethod.machineRecipeGetListByEquipment(objCommon, equipmentID));
        return uploadedRecipeIdListByEqpInqResult;
    }

    @Override
    public Results.RecipeDirectoryInqResult sxRecipeDirectoryInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        Results.RecipeDirectoryInqResult recipeDirectoryInqResult = new Results.RecipeDirectoryInqResult();
        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        log.info("【step1】: Get Eqp's Recipe Body Manage Flag...");
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        log.info("【step2】: Get Eqp's Operation Mode...");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        String onlineMode = eqpPortStatuses.get(0).getOnlineMode();
        Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), onlineMode);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        /*---------------------------------------*/
        /*   Send Recipe Upload Request to EAP   */
        /*---------------------------------------*/
        log.info("【step3】: Main Process: Send Recipe Directory Inquiry Request to EAP...");
        Inputs.SendRecipeDirectoryInqIn in = new Inputs.SendRecipeDirectoryInqIn();
        in.setObjCommonIn(objCommon);
        in.setRequestUserID(objCommon.getUser());
        in.setEquipmentID(equipmentID);
        Outputs.SendRecipeDirectoryInqOut out = (Outputs.SendRecipeDirectoryInqOut) tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeDirectoryInq, in);
        if(out != null){
            recipeDirectoryInqResult = out.getRecipeDirectoryInqResult();
        }
        return recipeDirectoryInqResult;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List<ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    @Override
    public List<ObjectIdentifier> sxAllRecipeIdListInq(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxAllRecipeIdListInq()");
        return recipeMethod.recipeRecipeIDGetDR(objCommon);
    }

    @Override
    public Results.RecipeIdListForDOCInqResult sxRecipeIdListForDOCInq(Infos.ObjCommon objCommon, Params.RecipeIdListForDOCInqParams recipeIdListForDOCInqParams) {
        /*--------------------*/
        /*   Initialization   */
        /*--------------------*/
        Results.RecipeIdListForDOCInqResult out = new Results.RecipeIdListForDOCInqResult();
        String environmentValueAll = BizConstant.SP_MCRECIPE_SEARCHCRITERIA_ALL;
        String environmentValuePd = BizConstant.SP_MCRECIPE_SEARCHCRITERIA_PD;
        String recipeSearchCriteria = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getRecipeSearchCriteria();
        ObjectIdentifier machineRecipeID = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getMachineRecipeID();
        ObjectIdentifier equipmentID = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getEquipmentID();
        ObjectIdentifier lotID = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getLotID();
        ObjectIdentifier pdID = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getPdID();
        String fpcCategory = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getFpcCategory();
        String whiteDefSearchCriteria = recipeIdListForDOCInqParams.getStrRecipeIdListForDOCInqInParm().getWhiteDefSearchCriteria();
        if (CimStringUtils.equals(environmentValueAll, recipeSearchCriteria)) {
            // 【step1】 - machineRecipe_GetListForFPCDR
            List<Outputs.MachineRecipe> getListForFPCDRList = machineRecipeMethod.machineRecipeGetListForFPCDR(objCommon, machineRecipeID, fpcCategory, whiteDefSearchCriteria);
            out.setStrMachineRecipeList(getListForFPCDRList);
        } else if (CimStringUtils.equals(environmentValuePd, recipeSearchCriteria)) {
            // 【step2】 - machineRecipe_GetListByPDForFPC
            List<Outputs.MachineRecipe> getListByPDForFPCCode = machineRecipeMethod.machineRecipeGetListByPDForFPC(objCommon, lotID, equipmentID, pdID, fpcCategory, whiteDefSearchCriteria);
            out.setStrMachineRecipeList(getListByPDForFPCCode);
        }
        return out;
    }

}
