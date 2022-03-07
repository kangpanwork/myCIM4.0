package com.fa.cim.service.parts.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.service.parts.IBOMPartsInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 16:43
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class BOMPartsInqService implements IBOMPartsInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IRouteMethod routeMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Override
    public Results.BOMPartsDefinitionInqResult sxBOMPartsDefinitionInq(Infos.ObjCommon objCommon, Params.BOMPartsDefinitionInqInParams bomPartsDefinitionInqInParams) {
        Validations.check(null == bomPartsDefinitionInqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier productID = bomPartsDefinitionInqInParams.getProductID();
        ObjectIdentifier lotID = bomPartsDefinitionInqInParams.getLotID();
        log.info("InParam [lotID]    : " + ObjectIdentifier.fetchValue(lotID));
        log.info("InParam [productID]: " + ObjectIdentifier.fetchValue(productID));

        Results.BOMPartsDefinitionInqResult retVal = null;

        if (!ObjectIdentifier.isEmptyWithValue(lotID)) {
            Outputs.ObjLotProductIDGetOut objLotProductIDGetOut = lotMethod.lotProductIDGet(objCommon, lotID);
            if (null != objLotProductIDGetOut) {
                productID = objLotProductIDGetOut.getProductID();
            }
        }

        //------------------------------------------------------
        // Get product BOM information
        //------------------------------------------------------
        Outputs.ObjProductBOMInfoGetOut productBOMInfoGetOut = productMethod.productBOMInfoGet(objCommon, productID);

        ObjectIdentifier bomID = productBOMInfoGetOut.getBomID();
        //------------------------------------------------------
        // Get product route information
        //------------------------------------------------------
        ObjectIdentifier mainRouteID = productMethod.productRouteInfoGet(objCommon, productID);

        //------------------------------------------------------
        // Get BOM parts definition for main route
        //------------------------------------------------------
        List<Infos.BOMPartsDefInProcess> bomPartsDefInProcessesForMainRoute = routeMethod.routeBOMPartsDefinitionGetDR(objCommon, bomID, mainRouteID);

        //----- Return Data -------
        retVal = new Results.BOMPartsDefinitionInqResult();
        retVal.setStrBOMPartsDefInProcessSeq(bomPartsDefInProcessesForMainRoute);

        //------------------------------------------------------
        // Get sub route information for main route
        //------------------------------------------------------
        Set<String> routeIDSeq = new HashSet<>();
        routeIDSeq.add(mainRouteID.getValue());
        // todo
        List<Infos.OperationInfo> strMainOpeSeq = routeMethod.routeConnectedSubRouteGetDR(objCommon, mainRouteID);
        Optional.ofNullable(strMainOpeSeq).ifPresent(mainOpeSeq -> {
            for (Infos.OperationInfo mainOperation : mainOpeSeq) {
                log.info("--------------------Main Operation round");
                Optional.ofNullable(mainOperation.getConnectedRouteList()).ifPresent(mainOperations -> {
                    for (Infos.ConnectedRoute operationForSub : mainOperations) {
                        log.info("--------------------Sub Route round");
                        if (routeIDSeq.contains(operationForSub.getRouteID().getValue())) {
                            continue;
                        }
                        routeIDSeq.add(operationForSub.getRouteID().getValue());
                        ObjectIdentifier subRouteID = operationForSub.getRouteID();

                        //------------------------------------------------------
                        // Get BOM parts difinition for sub route
                        //------------------------------------------------------
                        List<Infos.BOMPartsDefInProcess> bomPartsDefInProcessesForSubRoute = routeMethod.routeBOMPartsDefinitionGetDR(objCommon, bomID, subRouteID);
                        bomPartsDefInProcessesForMainRoute.addAll(bomPartsDefInProcessesForSubRoute);

                        //------------------------------------------------------
                        // Get sub route information for sub route
                        //------------------------------------------------------
                        List<Infos.OperationInfo> operationInfosForSub = routeMethod.routeConnectedSubRouteGetDR(objCommon, subRouteID);
                        Optional.ofNullable(operationInfosForSub).ifPresent(subOpeSeq -> {
                            for (Infos.OperationInfo subOperation : subOpeSeq) {
                                log.info("--------------------Sub Operation round");
                                Optional.ofNullable(subOperation.getConnectedRouteList()).ifPresent(subOperations -> {
                                    for (Infos.ConnectedRoute operationForBranch : subOperations) {
                                        log.info("--------------------Branch Route round");
                                        if (routeIDSeq.contains(operationForBranch.getRouteID().getValue())) {
                                            continue;
                                        }
                                        ObjectIdentifier branchRouteID = operationForBranch.getRouteID();
                                        routeIDSeq.add(branchRouteID.getValue());
                                        //------------------------------------------------------
                                        // Get BOM parts definition for branch route
                                        //------------------------------------------------------
                                        List<Infos.BOMPartsDefInProcess> bomPartsDefInProcessesForBranchRoute = routeMethod.routeBOMPartsDefinitionGetDR(objCommon, bomID, branchRouteID);
                                        //----- Return Data -------
                                        bomPartsDefInProcessesForMainRoute.addAll(bomPartsDefInProcessesForBranchRoute);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        Validations.check(CimArrayUtils.getSize(bomPartsDefInProcessesForMainRoute) == 0, retCodeConfig.getBomNotDefined());
        retVal.setBomID(bomID);
        retVal.setProductID(productID);
        retVal.setBomDescription(productBOMInfoGetOut.getBomDescription());
        return retVal;
    }


    @Override
    public List<Infos.LotListAttributes> sxBOMPartsLotListForProcessInq(Infos.ObjCommon objCommon, Params.BOMPartsLotListForProcessInqInParams bomPartsLotListForProcessInqInParams) {
        Validations.check(null == bomPartsLotListForProcessInqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier productID = bomPartsLotListForProcessInqInParams.getProductID();
        ObjectIdentifier operationID = bomPartsLotListForProcessInqInParams.getOperationID();
        ObjectIdentifier lotID = bomPartsLotListForProcessInqInParams.getLotID();
        ObjectIdentifier routeID = bomPartsLotListForProcessInqInParams.getRouteID();
        String operationNumber = bomPartsLotListForProcessInqInParams.getOperationNumber();
        log.info("InParam [lotID]          :" + ObjectIdentifier.fetchValue(lotID));
        log.info("InParam [productID]      :" + ObjectIdentifier.fetchValue(productID));
        log.info("InParam [routeID]        :" + ObjectIdentifier.fetchValue(routeID));
        log.info("InParam [operationNumber]:" + operationNumber);
        log.info("InParam [operationID]    :" + ObjectIdentifier.fetchValue(operationID));

        if(!ObjectIdentifier.isEmptyWithValue(lotID)) {
            //***********************************************/
            //*    Call lot_productID_Get                   */
            //***********************************************/
            Outputs.ObjLotProductIDGetOut objLotProductIDGetOut = lotMethod.lotProductIDGet(objCommon, lotID);
            if(null != objLotProductIDGetOut) {
                productID = objLotProductIDGetOut.getProductID();
            }
        }

        if(!ObjectIdentifier.isEmptyWithValue(routeID) && CimStringUtils.isNotEmpty(operationNumber)) {
            //***********************************************/
            //*    Call routeOperation_operationID_Get      */
            //***********************************************/
            ObjectIdentifier objectIdentifier = routeMethod.routeOperationOperationIDGet(objCommon, routeID, operationNumber);
            if(null != objectIdentifier) {
                operationID = objectIdentifier;
            }
        }

        //***********************************************/
        //*    Call process_BOMPartsInfo_GetDR          */
        //***********************************************/
        List<Infos.BOMPartsInfo> bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, productID, operationID);

        List<ObjectIdentifier> productIDs = new ArrayList<>();
        Optional.ofNullable(bomPartsInfos).ifPresent(list -> list.forEach(data -> productIDs.add(data.getPartID())));

        //***********************************************/
        //*    Call process_dispatchEquipments_GetDR    */
        //***********************************************/
        List<ObjectIdentifier> equipmentIDs = processMethod.processDispatchEquipmentsGetDR(objCommon, productID, operationID);

        //***********************************************/
        //*    Call equipments_productLotList_GetDR     */
        //***********************************************/
        return equipmentMethod.equipmentsProductLotListGetDR(objCommon, equipmentIDs, productIDs);
    }
}