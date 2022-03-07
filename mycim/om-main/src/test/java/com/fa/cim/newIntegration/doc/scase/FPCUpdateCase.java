package com.fa.cim.newIntegration.doc.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.equipment.scase.MoveOutCase;
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/28          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/11/28 17:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FPCUpdateCase {

    @Autowired
    RetCodeConfig retCodeConfig;
    @Autowired
    private STBCase stbCase;
    @Autowired
    private DurableTestCase durableTestCase;
    @Autowired
    private LotTestCase lotTestCase;
    @Autowired
    private SystemConfigTestCase systemConfigTestCase;
    @Autowired
    private DOCTestCase docTestCase;
    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;
    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;
    @Autowired
    private EquipmentTestCase equipmentTestCase;
    @Autowired
    private StartLotsReservationCase startLotsReservationCase;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MoveInCase moveInCase;
    @Autowired
    private MoveOutCase moveOutCase;
    @Autowired
    private DataCollectionTestCase dataCollectionTestCase;

    @Data
    public static class FrontInterfaceResult {
        private Infos.LotInfo lotInfo;
        private Infos.RouteInfo routeInfo;
        private List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos; //wafers

        private List<Infos.AreaEqp> areaEquipments;
        private List<Outputs.MachineRecipe> machineRecipes;
    }

    /**
     * description:DOC Registration BY LOT（EQP and Recipe）
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/11/28 17:41
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_1() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        //1. get wafers.
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //eqp/eqp_list_by_bay/inq
        //2. get equipment. /einfo/eqp_list/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //3. get recipe. /fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(areaEqps.get(0).getEquipmentID(), machineRecipes.get(0).getMachineRecipeID());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration BY WAFER（EQP and Recipe）
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 0:24
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_2() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        //1. get wafers.
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //2. get equipment. /einfo/eqp_list/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //3. get recipe. /fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfosNew = new ArrayList<>();
        waferListInLotFamilyInfosNew.add(waferListInLotFamilyInfos.get(0));
        waferListInLotFamilyInfosNew.add(waferListInLotFamilyInfos.get(1));
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(areaEqps.get(0).getEquipmentID(), machineRecipes.get(0).getMachineRecipeID());
        docTestCase.fpcRegistrationCase(lotInfo, routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfosNew,
                docRegistrationParams);

        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(lotInfo.getLotLocationInfo().getCassetteID()).getBody();
        ObjectIdentifier childLotID = lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList().stream().filter(x -> !CimObjectUtils.equalsWithValue(x, lotInfo.getLotBasicInfo().getLotID())).findFirst().orElse(null);

        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(childLotID)).getBody();
        frontInterfaceResult.setLotInfo(lotInfoInqResult.getLotInfoList().get(0));
        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration which"Get equipment by operation"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 1:14
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_3() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        //1. get wafers
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //2. einfo/eqp_list_by_operation/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByStepInqCase(routeInfo.getStrOperationInformationList().get(0).getOperationID(), lotInfo.getLotProductInfo().getProductID());

        //fpc/machine_recipe_list_for_fpc/inq
        DOCTestCase.recipeIdListForDOCInqCaseParams recipeIdListForDOCInqCaseParams = new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_OPERATION);
        recipeIdListForDOCInqCaseParams.setLotID(lotInfo.getLotBasicInfo().getLotID());
        recipeIdListForDOCInqCaseParams.setPdID(routeInfo.getStrOperationInformationList().get(0).getOperationID());
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(recipeIdListForDOCInqCaseParams);

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        Infos.AreaEqp areaEqp = areaEqps.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getEquipmentID(), "1SRT03")).findFirst().orElse(null);
        ObjectIdentifier equipmentID = null;
        if (areaEqp != null) {
            equipmentID = areaEqp.getEquipmentID();
        }
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(equipmentID, machineRecipes.get(0).getMachineRecipeID());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration which"Get recipe by Equipment "
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 1:32
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_4() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //eqp/eqp_list_by_step/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByStepInqCase(routeInfo.getStrOperationInformationList().get(0).getOperationID(), lotInfo.getLotProductInfo().getProductID());

        //fpc/machine_recipe_list_for_fpc/inq
        DOCTestCase.recipeIdListForDOCInqCaseParams recipeIdListForDOCInqCaseParams = new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_EQUIPMENT);
        recipeIdListForDOCInqCaseParams.setLotID(lotInfo.getLotBasicInfo().getLotID());
        recipeIdListForDOCInqCaseParams.setEquipmentID(areaEqps.get(0).getEquipmentID());
        recipeIdListForDOCInqCaseParams.setPdID(routeInfo.getStrOperationInformationList().get(0).getOperationID());
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(recipeIdListForDOCInqCaseParams);

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        Infos.AreaEqp areaEqp = areaEqps.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getEquipmentID(), "1SRT03")).findFirst().orElse(null);
        ObjectIdentifier equipmentID = null;
        if (areaEqp != null) {
            equipmentID = areaEqp.getEquipmentID();
        }
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(equipmentID, machineRecipes.get(0).getMachineRecipeID());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration EQP and Recipe with Search condition
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 10:59
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_5() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //einfo/eqp_list_by_operation/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_MEASUREMENT);

        //fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(areaEqps.get(0).getEquipmentID(), machineRecipes.get(0).getMachineRecipeID());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration with "EQP restriction flag is ON "
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 13:31
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_6() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //einfo/eqp_list/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(areaEqps.get(0).getEquipmentID(), machineRecipes.get(0).getMachineRecipeID());
        docRegistrationParams.setRestrictEquipmentFlag(true);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        frontInterfaceResult.setAreaEquipments(areaEqps);
        frontInterfaceResult.setMachineRecipes(machineRecipes);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration through Get RParam by Recipe
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 17:21
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_7() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //einfo/eqp_list/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //eqp/eqp_recipe_parameter_list/inq
        ObjectIdentifier equipmentID = new ObjectIdentifier("1BKD01");
        ObjectIdentifier machineRecipeID = new ObjectIdentifier("DOC.T01.01");
        Params.EqpRecipeParameterListInq eqpRecipeParameterListInq = new Params.EqpRecipeParameterListInq();
        eqpRecipeParameterListInq.setEquipmentID(equipmentID);
        eqpRecipeParameterListInq.setLotID(lotInfo.getLotBasicInfo().getLotID());
        eqpRecipeParameterListInq.setMachineRecipeID(machineRecipeID);
        eqpRecipeParameterListInq.setPdID(routeInfo.getStrOperationInformationList().get(0).getOperationID());
        eqpRecipeParameterListInq.setRParmSearchCriteria("LogicalRecipe");
        Results.EqpRecipeParameterListInqResult eqpRecipeParameterListInqResult = equipmentTestCase.eqpRecipeParameterListInqCase(eqpRecipeParameterListInq);

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(equipmentID, machineRecipeID);
        docRegistrationParams.setRecipeParameterChangeType("RecipeParmChangeByLot");
        docRegistrationParams.setRecipeParameterInfos(eqpRecipeParameterListInqResult.getStrRecipeParameterInfoList());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        List<Infos.AreaEqp> areaEqpsNew = new ArrayList<>();
        areaEqpsNew.add(areaEqps.stream().filter(x-> CimObjectUtils.equalsWithValue(x.getEquipmentID(), equipmentID)).findFirst().orElse(null));
        frontInterfaceResult.setAreaEquipments(areaEqpsNew);

        List<Outputs.MachineRecipe> machineRecipesNew = new ArrayList<>();
        machineRecipesNew.add(machineRecipes.stream().filter(x-> CimObjectUtils.equalsWithValue(x.getMachineRecipeID(), machineRecipeID)).findFirst().orElse(null));
        frontInterfaceResult.setMachineRecipes(machineRecipesNew);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration through Get RParam by EQP
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/3 13:31
     */
    public FrontInterfaceResult fpcRegistration_ENG_4_1_8() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //einfo/eqp_list/inq
        List<Infos.AreaEqp> areaEqps = electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //fpc/machine_recipe_list_for_fpc/inq
        List<Outputs.MachineRecipe> machineRecipes = docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //eqp/eqp_recipe_parameter_list/inq
        ObjectIdentifier equipmentID = new ObjectIdentifier("1BKD01");
        ObjectIdentifier machineRecipeID = new ObjectIdentifier("DOC.T01.01");
        Params.EqpRecipeParameterListInq eqpRecipeParameterListInq = new Params.EqpRecipeParameterListInq();
        eqpRecipeParameterListInq.setEquipmentID(equipmentID);
        eqpRecipeParameterListInq.setRParmSearchCriteria("Equipment");
        Results.EqpRecipeParameterListInqResult eqpRecipeParameterListInqResult = equipmentTestCase.eqpRecipeParameterListInqCase(eqpRecipeParameterListInq);

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(equipmentID, machineRecipeID);
        docRegistrationParams.setRecipeParameterChangeType("RecipeParmChangeByWafer");
        docRegistrationParams.setRecipeParameterInfos(eqpRecipeParameterListInqResult.getStrRecipeParameterInfoList());
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);

        List<Infos.AreaEqp> areaEqpsNew = new ArrayList<>();
        areaEqpsNew.add(areaEqps.stream().filter(x-> CimObjectUtils.equalsWithValue(x.getEquipmentID(), equipmentID)).findFirst().orElse(null));
        frontInterfaceResult.setAreaEquipments(areaEqpsNew);

        List<Outputs.MachineRecipe> machineRecipesNew = new ArrayList<>();
        machineRecipesNew.add(machineRecipes.stream().filter(x-> CimObjectUtils.equalsWithValue(x.getMachineRecipeID(), machineRecipeID)).findFirst().orElse(null));
        frontInterfaceResult.setMachineRecipes(machineRecipesNew);
        return frontInterfaceResult;
    }

    /**
     * description:DOC Registration through  change DC Spec
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/12/20 13:38
     * @param  -
     * @return void
     */
    public void fpcRegistration_ENG_4_1_12() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //einfo/eqp_list/inq
        electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //fpc/machine_recipe_list_for_fpc/inq
        docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        ObjectIdentifier equipmentID = new ObjectIdentifier("1CMS01");
        ObjectIdentifier machineRecipeID = new ObjectIdentifier("THK.1THK01.01");
        ObjectIdentifier dcDefineID = new ObjectIdentifier("DC_COR_TEST.00");
        //data_collection_list/inq
        Infos.EDCConfigListInqInParm edcConfigListInqInParm = new Infos.EDCConfigListInqInParm();
        edcConfigListInqInParm.setDcDefID(dcDefineID);
        edcConfigListInqInParm.setObjectID(new ObjectIdentifier("%"));
        edcConfigListInqInParm.setObjectType("DCSpecification");
        edcConfigListInqInParm.setWhiteDefSearchCriteria("All");
        edcConfigListInqInParm.setMaxCount(9999L);
        edcConfigListInqInParm.setDcSearchCriteria("All");
        List<Infos.DataCollection> dataCollections = dataCollectionTestCase.edcConfigListInqCase(edcConfigListInqInParm);

        //dc/dc_spec_detail_info/inq
        ObjectIdentifier dcSpecID = dataCollections.get(0).getObjectID();
        Results.EDCSpecInfoInqResult edcSpecInfoInqResult = dataCollectionTestCase.edcSpecInfoInqCase(dcSpecID);
        List<Infos.DCSpecDetailInfo> strDCSpecList = edcSpecInfoInqResult.getStrDCSpecList();
        Infos.DCSpecDetailInfo dcSpecDetailInfo = strDCSpecList.get(0);
        dcSpecDetailInfo.setActionCodes_uscrn("Inhibit-Equipment");
        dcSpecDetailInfo.setScreenLimitLowerRequired(false);
        dcSpecDetailInfo.setSpecLimitUpperRequired(false);
        dcSpecDetailInfo.setSpecLimitLowerRequired(false);
        dcSpecDetailInfo.setControlLimitUpperRequired(false);
        dcSpecDetailInfo.setControlLimitLowerRequired(false);

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(equipmentID, machineRecipeID);
        docRegistrationParams.setDcDefineID(dcDefineID);
        docRegistrationParams.setDcSpecID(dcSpecID);
        docRegistrationParams.setDcSpecList(strDCSpecList);

        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration in  Reticle
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/10 13:37
     */
    public void fpcRegistration_ENG_4_1_13() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        //eqp/eqp_list_by_bay/inq
        electronicInformationTestCase.eqpListByBayInqCase(ElectronicInformationTestCase.EQUIPMENT_CATEGORY_PROCESS);

        //fpc/machine_recipe_list_for_fpc/inq
        docTestCase.recipeIdListForDOCInqCase(new DOCTestCase.recipeIdListForDOCInqCaseParams(DOCTestCase.GET_RECIPE_BY_ALL));

        //drb/reticle_list/inq
        List<Infos.FoundReticle> strFoundReticle = durableTestCase.reticleListInqCase();
        List<Infos.FoundReticle> foundReticles = new ArrayList<>();
        foundReticles.add(strFoundReticle.get(0));

        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams(new ObjectIdentifier("1FHI01_NORM"), new ObjectIdentifier("ASH.1ASH21.01"));
        docRegistrationParams.setFoundReticles(foundReticles);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0), waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration(Skip operation which is not mandatory operation)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 14:57
     */
    public void fpcRegistration_ENG_4_1_14() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0350"));

        //------------------ DOC Registration --------------------
        List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
        Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0400")).findFirst().orElse(null);
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
        docRegistrationParams.setSkipFlag(true);
        docRegistrationParams.setCorrespondingOperationInfos(correspondingOperationInfos);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration(Skip operation which is mandatory operation)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 14:57
     */
    public void fpcRegistration_ENG_4_1_15() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0200"));
        //------------------ DOC Registration --------------------
        //doc/doc_lot_info_set/req
        try {
            List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
            Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0300")).findFirst().orElse(null);
            //fpc/fpc_update/req
            DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
            docRegistrationParams.setSkipFlag(true);
            docRegistrationParams.setCorrespondingOperationInfos(correspondingOperationInfos);
            docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                    docRegistrationParams);

        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getFpcCannotSkipOperation(), ex.getCode())) {
                throw ex;
            }
        }
    }

    /**
     * description:DOC Skip to next process which has DOC Registration too
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 15:31
     */
    public void fpcRegistration_ENG_4_1_16() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
        Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0400")).findFirst().orElse(null);
        //------------------ DOC Registration --------------------
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
        docRegistrationParams.setSkipFlag(true);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration(send E-mail)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 15:38
     */
    public void fpcRegistration_ENG_4_1_17() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0350"));

        //------------------ DOC Registration --------------------
        List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
        Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0400")).findFirst().orElse(null);
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
        docRegistrationParams.setSendEmailFlag(true);
        docRegistrationParams.setCorrespondingOperationInfos(correspondingOperationInfos);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration(Onhold)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 15:39
     */
    public void fpcRegistration_ENG_4_1_18() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0350"));

        //------------------ DOC Registration --------------------
        List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
        Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0400")).findFirst().orElse(null);
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
        docRegistrationParams.setHoldLotFlag(true);
        docRegistrationParams.setCorrespondingOperationInfos(correspondingOperationInfos);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC Registration with multiple corresponding operation
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/12/20 15:04
     * @param  -
     * @return void
     */
    public void fpcRegistration_ENG_4_1_20() {
        FrontInterfaceResult frontInterfaceResult = docFrontInterface();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = frontInterfaceResult.getWaferListInLotFamilyInfos();

        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0350"));
        correspondingOperationInfos.add(new Infos.CorrespondingOperationInfo("2000.0300"));

        //------------------ DOC Registration --------------------
        List<Infos.OperationInformation> strOperationInformationList = routeInfo.getStrOperationInformationList();
        Infos.OperationInformation operationInformation = strOperationInformationList.stream().filter(x -> x.getOperationNumber().equals("2000.0400")).findFirst().orElse(null);
        //fpc/fpc_update/req
        DOCTestCase.DOCRegistrationParams docRegistrationParams = new DOCTestCase.DOCRegistrationParams();
        docRegistrationParams.setCorrespondingOperationInfos(correspondingOperationInfos);
        docTestCase.fpcRegistrationCase(frontInterfaceResult.getLotInfo(), routeInfo.getRouteID(), operationInformation, waferListInLotFamilyInfos,
                docRegistrationParams);
    }

    /**
     * description:DOC configuration  change befor DOC execute
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 16:13
     */
    public void fpcUpdate_ENG_4_1_21() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(frontInterfaceResult.getLotInfo().getLotBasicInfo().getLotID(), false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        testUtils.skip(frontInterfaceResult.getLotInfo().getLotBasicInfo().getLotID(), operationNameAttributesList.get(0).getOperationNumber());
        Infos.OperationInformation operationInformation = frontInterfaceResult.getRouteInfo().getStrOperationInformationList().stream().filter(x -> x.getOperationNumber().equals("1000.0100")).findFirst().orElse(null);
        if (operationInformation == null) {
            return;
        }
        Results.DOCLotInfoInqResult docLotInfoInqResult = docTestCase.docLotInfoInqCase(frontInterfaceResult.getLotInfo().getLotBasicInfo().getLotID(), frontInterfaceResult.getRouteInfo().getRouteID(), operationInformation.getOperationNumber());
        Infos.FPCInfo fpcInfo = docLotInfoInqResult.getFPCInfoList().get(0);
        //modify
        fpcInfo.setEquipmentID(frontInterfaceResult.getAreaEquipments().get(1).getEquipmentID()); //modify equipment
        //------------------ DOC UPDATE --------------------
        //fpc/fpc_update/req
        docTestCase.fpcUpdateCase(fpcInfo);
    }

    /**
     * description: DOC configuration  change after DOC execute
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/12/20 15:14
     * @param  -
     * @return void
     */
    public void fpcUpdate_ENG_4_1_22() {
        //1.registration
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //2.process
        List<Infos.AreaEqp> areaEquipments = frontInterfaceResult.getAreaEquipments();
        docManufacturingProcesses(lotInfo, areaEquipments.get(0).getEquipmentID());

        //3.modify
        Infos.OperationInformation operationInformation = frontInterfaceResult.getRouteInfo().getStrOperationInformationList().stream().filter(x -> x.getOperationNumber().equals("1000.0100")).findFirst().orElse(null);
        if (operationInformation == null) {
            return;
        }
        Results.DOCLotInfoInqResult docLotInfoInqResult = docTestCase.docLotInfoInqCase(lotInfo.getLotBasicInfo().getLotID(), frontInterfaceResult.getRouteInfo().getRouteID(), operationInformation.getOperationNumber());
        Infos.FPCInfo fpcInfo = docLotInfoInqResult.getFPCInfoList().get(0);
        fpcInfo.setEquipmentID(frontInterfaceResult.getAreaEquipments().get(1).getEquipmentID()); //modify equipment

        //------------------ DOC UPDATE --------------------
        //doc/doc_lot_info_set/req
        docTestCase.fpcUpdateCase(fpcInfo);
    }

    /**
     * description:delete DOC setting befor DOC execute
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/4 16:44
     */
    public void fpcDelete_ENG_4_1_24() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        Infos.OperationInformation operationInformation = frontInterfaceResult.getRouteInfo().getStrOperationInformationList().stream().filter(x -> x.getOperationNumber().equals("1000.0100")).findFirst().orElse(null);
        if (operationInformation == null) {
            return;
        }
        Results.DOCLotInfoInqResult docLotInfoInqResult = docTestCase.docLotInfoInqCase(lotInfo.getLotBasicInfo().getLotID(), routeInfo.getRouteID(), operationInformation.getOperationNumber());
        Infos.FPCInfo fpcInfo = docLotInfoInqResult.getFPCInfoList().get(0);

        testUtils.skip(lotInfo.getLotBasicInfo().getLotID(), "1000.0200");
        //------------------ DOC DELETE --------------------
        //fpc/fpc_delete/req
        docTestCase.docLotRemoveReqCase(Arrays.asList(fpcInfo.getFpcID()), fpcInfo.getLotFamilyID());
    }

    /**
     * description: delete DOC setting after DOC execute
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/12/20 15:28
     * @param  -
     * @return void
     */
    public void fpcDelete_ENG_4_1_25() {
        //1.registration
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.RouteInfo routeInfo = frontInterfaceResult.getRouteInfo();
        Infos.OperationInformation operationInformation = frontInterfaceResult.getRouteInfo().getStrOperationInformationList().stream().filter(x -> x.getOperationNumber().equals("1000.0100")).findFirst().orElse(null);
        if (operationInformation == null) {
            return;
        }
        //2.process
        List<Infos.AreaEqp> areaEquipments = frontInterfaceResult.getAreaEquipments();
        docManufacturingProcesses(lotInfo, areaEquipments.get(0).getEquipmentID());

        //3.fpcinfo
        Results.DOCLotInfoInqResult docLotInfoInqResult = docTestCase.docLotInfoInqCase(frontInterfaceResult.getLotInfo().getLotBasicInfo().getLotID(), routeInfo.getRouteID(), operationInformation.getOperationNumber());
        Infos.FPCInfo fpcInfo = docLotInfoInqResult.getFPCInfoList().get(0);

        //------------------ DOC DELETE --------------------
        //fpc/fpc_delete/req
        docTestCase.docLotRemoveReqCase(Arrays.asList(fpcInfo.getFpcID()), fpcInfo.getLotFamilyID());
    }

    /**
     * description:Execute DOC without start reserve
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/6 16:07
     */
    public void fpcExecute_ENG_4_1_26() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();

        //einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID()).getBody();
        Infos.EqpPortStatus eqpPortStatus = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0);

        //eqp/carrier_loading/rpt
        try {
            equipmentTestCase.carrierLoadingRpt(eqpInfoInqResult.getEquipmentID(), frontInterfaceResult.getLotInfo().getLotLocationInfo().getCassetteID(), eqpPortStatus.getPortID(), eqpPortStatus.getLoadPurposeType());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getFpcRequireStartReserve())) {
                throw e;
            }
        }
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Registration BY LOT（EQP and Recipe）
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/6 16:22
     */
    public void fpcExecute_ENG_4_1_27() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_1();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Registration BY Wafer（EQP and Recipe）
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:32
     */
    public void fpcExecute_ENG_4_1_28() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_2();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();
        Infos.OperationInformation operationInformation = frontInterfaceResult.getRouteInfo().getStrOperationInformationList().stream().filter(x -> x.getOperationNumber().equals("1000.0100")).findFirst().orElse(null);
        if (operationInformation == null) {
            return;
        }
        Results.DOCLotInfoInqResult docLotInfoInqResult = docTestCase.docLotInfoInqCase(frontInterfaceResult.getLotInfo().getLotBasicInfo().getLotID(), frontInterfaceResult.getRouteInfo().getRouteID(), operationInformation.getOperationNumber());
        Infos.FPCInfo fpcInfo = docLotInfoInqResult.getFPCInfoList().get(0);
        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Execute "Get equipment by operation" and "Get recipe by operation"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:33
     */
    public void fpcExecute_ENG_4_1_29() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_3();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        Infos.AreaEqp areaEqp = frontInterfaceResult.getAreaEquipments().stream().filter(x -> CimObjectUtils.equalsWithValue(x.getEquipmentID(), "1SRT03")).findFirst().orElse(null);
        if (areaEqp == null) {
            return;
        }
        ObjectIdentifier equipmentID = areaEqp.getEquipmentID();
        docManufacturingProcesses(lotInfo, equipmentID);
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Execute "Get equipment by operation" and "Get recipe by Equipment"
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:43
     */
    public void fpcExecute_ENG_4_1_30() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_4();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        Infos.AreaEqp areaEqp = frontInterfaceResult.getAreaEquipments().stream().filter(x -> CimObjectUtils.equalsWithValue(x.getEquipmentID(), "1SRT03")).findFirst().orElse(null);
        if (areaEqp == null) {
            return;
        }
        ObjectIdentifier equipmentID = areaEqp.getEquipmentID();
        docManufacturingProcesses(lotInfo, equipmentID);
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Execute EQP and Recipe with Search condition
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:43
     */
    public void fpcExecute_ENG_4_1_31() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_5();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }

    /**
     * DOC Execute（EQP and Recipe）
     * DOC Execute EQP and Recipe with Search condition
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:43
     */
    public void fpcExecute_ENG_4_1_32() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_6();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }

    /**
     * DOC Execute（Recipe Parameter）
     * DOC Execute through Get RParam by Recipe
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:43
     */
    public void fpcExecute_ENG_4_1_34() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_7();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }

    /**
     * DOC Execute（Recipe Parameter）
     * DOC Execute through Get RParam by EQP
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/9 17:43
     */
    public void fpcExecute_ENG_4_1_35() {
        FrontInterfaceResult frontInterfaceResult = fpcRegistration_ENG_4_1_8();
        Infos.LotInfo lotInfo = frontInterfaceResult.getLotInfo();

        //-----------ManufacturingProcesses------------------
        docManufacturingProcesses(lotInfo, frontInterfaceResult.getAreaEquipments().get(0).getEquipmentID());
    }


    /**
     * description：注册DOC的前置条件
    * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/12/20 10:33
     * @param  -
     * @return com.fa.cim.newIntegration.doc.scase.FPCUpdateCase.FrontInterfaceResult
     */
    private FrontInterfaceResult docFrontInterface() {
        FrontInterfaceResult frontInterfaceResult = new FrontInterfaceResult();
        //DOC page
        //1. lotstart/stb_released_lot/req
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //2. einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();

        //doc/process_flow_ope_list_with_nest/inq
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo().getRouteID()).getBody();

        //fpc/fpc_detail_info/inq
        docTestCase.docLotInfoInqCase(lotID, routeInfo.getRouteID(), routeInfo.getStrOperationInformationList().get(0).getOperationNumber());

        //add page
        //lot/wafer_list_in_lot_family_info/inq
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = lotTestCase.waferListInLotFamilyInqCase(lotInfoInqResult.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID());

        //syscfg/code_list/inq
        systemConfigTestCase.codeSelectionInqCase("Equipment Category");

        //syscfg/code_list/inq
        systemConfigTestCase.codeSelectionInqCase("EDCSpecGroup");

        frontInterfaceResult.setLotInfo(lotInfoInqResult.getLotInfoList().get(0));
        frontInterfaceResult.setRouteInfo(routeInfo);
        frontInterfaceResult.setWaferListInLotFamilyInfos(waferListInLotFamilyInfos);
        return frontInterfaceResult;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotInfo
     * @param equipmentID -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/12/9 17:45
     */
    private Response docManufacturingProcesses(Infos.LotInfo lotInfo, ObjectIdentifier equipmentID) {
        //-----------operationSkip------------------
        //lot/ope_locate/req
        ObjectIdentifier lotID = lotInfo.getLotBasicInfo().getLotID();

        equipmentTestCase.changeOperationModeToOffLine1(equipmentID);

        //-----------StartReservation---------------
        startLotsReservationCase.startLotsReserve(lotID, equipmentID);

        //-----------load---------------
        equipmentTestCase.carrierLoadingRpt(equipmentID, lotInfo.getLotLocationInfo().getCassetteID(), new ObjectIdentifier("P1"), BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        //-----------moveIn---------------
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.onlyMoveIn(Arrays.asList(lotInfo.getLotLocationInfo().getCassetteID()), equipmentID).getBody();

        //-----------moveOut---------------
        return moveOutCase.onlyMoveOut(moveInReqResult.getControlJobID(), equipmentID);
    }
}