package com.fa.cim.service.sort.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ISorterMethod;
import com.fa.cim.method.IWaferMethod;
import com.fa.cim.service.sort.ISortInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SortInqServiceImpl implements ISortInqService {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;


    @Autowired
    private ISorterMethod sorterMethod;

    public Results.OnlineSorterActionSelectionInqResult sxOnlineSorterActionSelectionInq(Infos.ObjCommon objCommon, Params.OnlineSorterActionSelectionInqParams params) {

        //init
        Results.OnlineSorterActionSelectionInqResult out = new Results.OnlineSorterActionSelectionInqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();

        //----------------------------------------
        //  Check Equipment ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        /*-----------------------------*/
        /*  Check And Read Action code */
        /*-----------------------------*/
        //【step2】 - waferSorter_actionList_SelectDR
        List<Infos.WaferSorterActionList> waferSorterActionListSelectDROut = waferMethod.waferSorterActionListSelectDR(objCommon,equipmentID);

        log.info("waferSorter_actionList_SelectDR() == RC_OK");
        out.setStrWaferSorterActionListSequence(waferSorterActionListSelectDROut);
        out.setEquipmentID(equipmentID);

        /*----------------------------*/
        /*       Return to Caller     */
        /*----------------------------*/
        return out;
    }

    public List<Infos.WaferSorterSlotMap> sxOnlineSorterActionStatusInq (Infos.ObjCommon objCommon, Params.OnlineSorterActionStatusInqParm params) {
        //P4000099 Start
        //----------------------------------------
        //  Check Equipment ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());
        //P4000099 End

        //-----------------------------
        //  Read a Slot Map
        //-----------------------------
        Infos.WaferSorterSlotMap strWaferSorterGetSlotMapReferenceCondition = new Infos.WaferSorterSlotMap();
        strWaferSorterGetSlotMapReferenceCondition.setPortGroup(params.getPortGroup());
        strWaferSorterGetSlotMapReferenceCondition.setEquipmentID(params.getEquipmentID());
        strWaferSorterGetSlotMapReferenceCondition.setDestinationCassetteManagedByOM(false);
        strWaferSorterGetSlotMapReferenceCondition.setOriginalCassetteManagedByOM(false);

        List<Infos.WaferSorterSlotMap> waferSorterSlotMapSelectDROut = null;
        try {
            waferSorterSlotMapSelectDROut = waferMethod.waferSorterSlotMapSelectDR(objCommon,
                    params.getRequiredData(),
                    params.getSortPattern(),
                    BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                    BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                    strWaferSorterGetSlotMapReferenceCondition);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfigEx.getNotFoundSlotMapRecord(), e.getCode())) {
                throw e;
            }
        }

        return waferSorterSlotMapSelectDROut;
    }

    public List<Infos.LotWaferMap> sxOnlineSorterScrapWaferInq(Infos.ObjCommon objCommon, Params.OnlineSorterScrapWaferInqParams params) {

        //init
        List<Infos.LotWaferMap> out = new ArrayList<>();

        List<ObjectIdentifier> cassetteIDs = params.getCassetteIDs();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        String portGroup = params.getPortGroup();
        User user = params.getUser();

        int castLen = CimArrayUtils.getSize(cassetteIDs);
        log.info("Cassette Number is :{}",castLen);
        for (int i = 0; i < castLen; i++) {
            log.info("CassetteIDs : {}",cassetteIDs.get(i).getValue());
        }

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //--------------------------------------------------------------
        //   Getting scrap flag marked record from DB that were on MMDB
        //-------------------------------------------------------------
        log.info("Try to waferSorter_scrapWafer_SelectDR");
        //【step2】 - cassette_scrapWafer_SelectDR
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);

        //------------------------------------------------------
        // Set Object returned value to Tx Value
        //------------------------------------------------------
        out = lotWaferMaps;
        // Trace
        int scrapLen = CimArrayUtils.getSize(lotWaferMaps);
        for (int i = 0; i < scrapLen; i++) {
            log.info("scrapped wafer waferID is:{}",out.get(i).getWaferID().getValue());
            log.info("scrapped wafer LotID is:{}",out.get(i).getLotID().getValue());
            log.info("scrapped wafer CassetteID is:{}",out.get(i).getCassetteID().getValue());
            log.info("scrapped wafer Psition is:{}",out.get(i).getSlotNumber());
        }
        //----------
        //   Return
        //----------
        return out;
    }

    public Results.SJInfoForAutoLotStartInqResult sxSJInfoForAutoLotStartInq(Infos.ObjCommon objCommon) {
        Results.SJInfoForAutoLotStartInqResult result = null;
        String sql = "SELECT DISTINCT OMPORT.EQP_ID\n" +
                "  FROM OMEQP, OMPORT, OMEQPOPEMODE\n" +
                " WHERE OMEQPOPEMODE.OPE_MODE_ID = OMPORT.EQP_OPE_MODE_ID\n" +
                "   AND OMEQP.EQP_ID = OMPORT.EQP_ID\n" +
                "   AND OMEQPOPEMODE.ONLINE_MODE <> 'Off-Line'\n" +
                "   AND OMEQPOPEMODE.ACCESS_MODE = 'Auto'\n" +
                "   AND DISPATCH_MODE = 'Manual'\n" +
                "   AND OMPORT.PORT_STATE IN ('LoadAvail', 'LoadReq', 'UnloadReq')\n" +
                "   AND OMEQP.EQP_CATEGORY = 'Wafer Sorter'";
        List<Object[]> query = cimJpaRepository.query(sql);

        HashMap<Integer, String> sjSizeAndEquipment = new HashMap<>();
        if (CimArrayUtils.isNotEmpty(query)) {
            for (Object[] objects : query) {
                ObjectIdentifier equipmentID = ObjectIdentifier.buildWithValue((String) objects[0]);
                Params.SJListInqParams sjListInqParams = new Params.SJListInqParams();
                sjListInqParams.setEquipmentID(equipmentID);
                List<Infos.SortJobListAttributes> sortJobListAttributes = this.sxSJListInq(objCommon, sjListInqParams);
                if (0 == CimArrayUtils.getSize(sortJobListAttributes)) {
                    //没有就是它
                    result = this.autoLotStartInfo(objCommon, equipmentID);
                    break;
                } else  {
                    //有就排序添加
                    sjSizeAndEquipment.put(CimArrayUtils.getSize(sortJobListAttributes), ObjectIdentifier.fetchValue(equipmentID));
                }
            }
        }

        if (null == result) {
            String equipment = this.mapKeySort(sjSizeAndEquipment);
            result = this.autoLotStartInfo(objCommon, ObjectIdentifier.buildWithValue(equipment));
        }

        return result;
    }

    private Results.SJInfoForAutoLotStartInqResult autoLotStartInfo(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID){
        Results.SJInfoForAutoLotStartInqResult result = new Results.SJInfoForAutoLotStartInqResult();

        result.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        List<String> portGroups = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(eqpPortStatuses); i++) {
            if (!portGroups.contains(eqpPortStatuses.get(i).getPortGroup())) {
                portGroups.add(eqpPortStatuses.get(i).getPortGroup());
            }
        }

        Boolean inPutFlag = false;
        Boolean outPutFlag = false;
        for (int i = 0; i < CimArrayUtils.getSize(portGroups); i++) {
            for (int j = 0; j < CimArrayUtils.getSize(eqpPortStatuses); j++) {
                if (!CimStringUtils.equals(portGroups.get(i), eqpPortStatuses.get(j).getPortGroup())) {
                    continue;
                }
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER,eqpPortStatuses.get(j).getLoadPurposeType())) {
                    result.setPortGroupID(eqpPortStatuses.get(j).getPortGroup());
                    if (CimStringUtils.equals(eqpPortStatuses.get(j).getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUTOUTPUT) && !inPutFlag) {
                        result.setInputPortID(ObjectIdentifier.fetchValue(eqpPortStatuses.get(j).getPortID()));
                        inPutFlag = true;
                        continue;
                    }
                    if (CimStringUtils.equals(eqpPortStatuses.get(j).getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUTOUTPUT) && !outPutFlag) {
                        result.setOutPutPortID(ObjectIdentifier.fetchValue(eqpPortStatuses.get(j).getPortID()));
                        outPutFlag = true;
                        continue;
                    }

                    if (CimStringUtils.equals(eqpPortStatuses.get(j).getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUT)) {
                        result.setInputPortID(ObjectIdentifier.fetchValue(eqpPortStatuses.get(j).getPortID()));
                        inPutFlag = true;
                    }
                    if (CimStringUtils.equals(eqpPortStatuses.get(j).getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_OUTPUT)) {
                        result.setOutPutPortID(ObjectIdentifier.fetchValue(eqpPortStatuses.get(j).getPortID()));
                        outPutFlag = true;
                    }
                }
            }
            if (inPutFlag && outPutFlag) {
                break;
            }
        }
        if (!inPutFlag && ! outPutFlag) {
            Validations.check(true,"not found input port or output port");
        }
        return result;
    }

    private static String mapKeySort (HashMap<Integer, String> labelsMap) {

        List<Map.Entry<Integer, String>> list = new ArrayList<Map.Entry<Integer, String>>(labelsMap.entrySet());
        list.sort((x,y)-> x.getKey() < y.getKey() ? -1 : (x.getKey().equals(y.getKey())) ? 0 : 1);
        String equipment = null;
        for (Map.Entry<Integer, String> mapping : list) {
            equipment = mapping.getValue();
            break;
        }
        return equipment;
    }

    public List<Infos.SortJobListAttributes> sxSJListInq(Infos.ObjCommon objCommon, Params.SJListInqParams params) {
        Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setEquipmentID(params.getEquipmentID());
        objSorterJobListGetDRIn.setCarrierID(params.getCarrierID());
        objSorterJobListGetDRIn.setCreateUser(params.getCreateUser());
        objSorterJobListGetDRIn.setLotID(params.getLotID());
        objSorterJobListGetDRIn.setSorterJob(params.getSorterJobID());
        return sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
    }

    public Results.SJStatusInqResult sxSJStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID) {
        Results.SJStatusInqResult result = new Results.SJStatusInqResult();
        //-----------------------------------
        //  Get SorterJobList information
        //-----------------------------------
        Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(sorterJobID);
        List<Infos.SortJobListAttributes> sortJobListAttributes = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);

        //--------------------
        //  Set out Result
        //--------------------

        int countLen1 = CimArrayUtils.getSize(sortJobListAttributes);
        List<Infos.SorterComponentJobList> sorterComponentJobLists = new ArrayList<>();
        log.info("sortJobListAttributes.length() : {}", countLen1);

        for (int i = 0; i < countLen1; i++ ) {
            result.setEquipmentID(sortJobListAttributes.get(i).getEquipmentID());
            result.setPortGroupID(sortJobListAttributes.get(i).getPortGroupID());
            result.setWaferIDReadFlag(sortJobListAttributes.get(i).isWaferIDReadFlag());

            int countLen2 = CimArrayUtils.getSize(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList());
            for (int j = 0; j < countLen2; j++ ) {
                Infos.SorterComponentJobList sorterComponentJobList = new Infos.SorterComponentJobList();
                sorterComponentJobList.setComponentJobID(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getSorterComponentJobID());
                sorterComponentJobList.setSorterComponentJobStatus(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getComponentSorterJobStatus());
                sorterComponentJobList.setOriginalCassetteID(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getOriginalCarrierID());
                sorterComponentJobList.setDestinationCassetteID(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getDestinationCarrierID());
                sorterComponentJobList.setRequestTimeStamp(sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getRequestTimeStamp());
                List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = sortJobListAttributes.get(i).getSorterComponentJobListAttributesList().get(j).getWaferSorterSlotMapList();
                Optional.ofNullable(waferSorterSlotMapList).ifPresent(waferSorterSlotMaps -> waferSorterSlotMaps.sort((slot1,slot2)->{
                    Long originalSlotNumber1 = CimNumberUtils.longValue(slot1.getOriginalSlotNumber());
                    Long originalSlotNumber2 = CimNumberUtils.longValue(slot2.getOriginalSlotNumber());
                    return originalSlotNumber1.compareTo(originalSlotNumber2);
                }));
                sorterComponentJobList.setWaferSorterSlotMapList(waferSorterSlotMapList);
                sorterComponentJobLists.add(sorterComponentJobList);
            }
            result.setSorterComponentJobList(sorterComponentJobLists);
        }

        return result;
    }

    @Override
    public String reqCategoryGetByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        return sorterMethod.reqCategoryGetByLot(objCommon, lotID);
    }

}
