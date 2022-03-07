package com.fa.cim.service.apc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.service.apc.IAPCInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 12:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class APCInqServiceImpl implements IAPCInqService {
    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;

    public List<Infos.APCIf> sxAPCInterfaceListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID){
        return apcMethod.APCIFListGetDR(objCommon, equipmentID);
    }

    public List<Infos.APCRunTimeCapabilityResponse> sxAPCRunTimeCapabilityInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette, boolean sendTxFlag){
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponseList = null;
        if (sendTxFlag){
            /*-------------------------------------*/
            /*   Call cassette_APCInformation_GetDR*/
            /*-------------------------------------*/
            List<Infos.ApcBaseCassette> apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon, equipmentID, strStartCassette);
            /*--------------------------------------------------------*/
            /*   Call APCMgr_SendAPCRunTimeCapabilityRequestDR        */
            /*--------------------------------------------------------*/
            List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponses = apcMethod.APCMgrSendAPCRunTimeCapabilityRequestDR(objCommon, equipmentID, apcBaseCassettes);

            /*--------------------------------------------------------------------------------------*/
            /*   Lot and Wafer Combination Check                                                    */
            /*    - if APC returned lotID is not in strStartCassette, return NG                     */
            /*    - if APC returned waferID is not in strStartCassette's waferID of lot, return NG  */
            /*--------------------------------------------------------------------------------------*/
            /*----------------------------------------------*/
            /*   For loop strAPCRunTimeCapabilityResponse   */
            /*----------------------------------------------*/
            int apc_resLen = CimArrayUtils.getSize(apcRunTimeCapabilityResponses);
            int startCasLen = CimArrayUtils.getSize(strStartCassette);
            int apc_i= 0;
            for (apc_i = 0; apc_i < apc_resLen; apc_i++){
                List<Infos.APCRunTimeCapability> strAPCRunTimeCapability = apcRunTimeCapabilityResponses.get(apc_i).getStrAPCRunTimeCapability();
                int apc_capaLen = CimArrayUtils.getSize(strAPCRunTimeCapability);
                for (int apc_j = 0; apc_j < apc_capaLen; apc_j++){
                    List<Infos.APCLotWaferCollection> strAPCLotWaferCollection = strAPCRunTimeCapability.get(apc_j).getStrAPCLotWaferCollection();
                    int apc_lotLen = CimArrayUtils.getSize(strAPCLotWaferCollection);
                    Validations.check(apc_lotLen == 0, retCodeConfigEx.getApcReturnInvalidParam());
                    for (int apc_k = 0; apc_k < apc_lotLen; apc_k++){
                        /*------------------------------*/
                        /*   For loop strStartCassette  */
                        /*------------------------------*/
                        boolean bLotExistFlag = false;
                        for (int sc_i = 0; sc_i < startCasLen; sc_i++){
                            List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(sc_i).getLotInCassetteList();
                            int startLotLen = CimArrayUtils.getSize(lotInCassetteList);
                            /*--------------------------*/
                            /*   Lot existence check    */
                            /*--------------------------*/
                            for (int sc_j = 0; sc_j < startLotLen; sc_j++){
                                if (ObjectIdentifier.equalsWithValue(strAPCLotWaferCollection.get(apc_k).getLotID(), lotInCassetteList.get(sc_j).getLotID())){
                                    bLotExistFlag = true;
                                    List<String> waferIDs = strAPCLotWaferCollection.get(apc_k).getWaferID();
                                    int apc_waferLen = CimArrayUtils.getSize(waferIDs);
                                    Validations.check(apc_waferLen == 0, retCodeConfigEx.getApcReturnInvalidParam());
                                    for (int apc_l = 0; apc_l < apc_waferLen; apc_l++){
                                        /*------------------------------*/
                                        /*   Wafer existence check      */
                                        /*------------------------------*/
                                        boolean bWaferExistFlag = false;
                                        List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(sc_j).getLotWaferList();
                                        int startWaferLen = CimArrayUtils.getSize(lotWaferList);
                                        for (int sc_k = 0; sc_k < startWaferLen; sc_k++){
                                            if (ObjectIdentifier.equalsWithValue(waferIDs.get(apc_l), lotWaferList.get(sc_k).getWaferID())){
                                                bWaferExistFlag = true;
                                                break;
                                            }
                                        }
                                        Validations.check(!bWaferExistFlag, retCodeConfigEx.getApcReturnInvalidParam());
                                    }
                                    break;
                                }
                            }
                            if (bLotExistFlag) {
                                break;
                            }
                        }
                        Validations.check(!bLotExistFlag, retCodeConfigEx.getApcReturnInvalidParam());
                    }
                }
            }
            /*------------------------------------------------------------------------------------------------------*/
            /*   Check whether APC returned lot is OpeStartLot or not                                               */
            /*      If APC returned lotID is not an object for OpeStart, it should be ignore.                       */
            /*      To check OpeStartLot or not, strStartCassette[].strLotInCassette[].opeStartFlag can be used.    */
            /*      If not, strAPCRunTimeCapabilityResponse[].strAPCRunTimeCapability[].strAPCLotWaferCollection[]  */
            /*      must be arranged. (remove non-start lot's information from sequence)                            */
            /*      If all lots in strAPCLotWaferCollection sequence are non-start lot, strAPCLotWaferCollection's  */
            /*      length must be set to 0.                                                                        */
            /*      By this, garbage lot information is not set to FSRUNCATA_LOT.                                   */
            /*------------------------------------------------------------------------------------------------------*/
            /*----------------------------------------------*/
            /*   For loop strAPCRunTimeCapabilityResponse   */
            /*----------------------------------------------*/
            apc_resLen = CimArrayUtils.getSize(apcRunTimeCapabilityResponses);
            startCasLen = CimArrayUtils.getSize(strStartCassette);
            List<Infos.APCLotWaferCollection> tmpAPCLotWaferCollectionList = new ArrayList<>();
            int res_k;
            for( apc_i = 0; apc_i < apc_resLen; apc_i++ ) {
                List<Infos.APCRunTimeCapability> strAPCRunTimeCapability = apcRunTimeCapabilityResponses.get(apc_i).getStrAPCRunTimeCapability();
                int apc_capaLen = CimArrayUtils.getSize(strAPCRunTimeCapability);
                for( int apc_j = 0; apc_j < apc_capaLen; apc_j++ ){
                    List<Infos.APCLotWaferCollection> strAPCLotWaferCollection = strAPCRunTimeCapability.get(apc_j).getStrAPCLotWaferCollection();
                    int apc_lotLen = CimArrayUtils.getSize(strAPCLotWaferCollection);
                    res_k = 0;
                    for(int apc_k = 0; apc_k < apc_lotLen; apc_k++ ){
                        boolean bOpeStartFlag = false;
                        /*------------------------------*/
                        /*   For loop strStartCassette  */
                        /*------------------------------*/
                        for(int sc_i = 0; sc_i < startCasLen; sc_i++){
                            List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(sc_i).getLotInCassetteList();
                            int startLotLen = CimArrayUtils.getSize(lotInCassetteList);
                            /*------------------------------*/
                            /*   operationStartFlag check   */
                            /*------------------------------*/
                            for(int sc_j = 0; sc_j < startLotLen; sc_j++) {
                                if(ObjectIdentifier.equalsWithValue(strAPCLotWaferCollection.get(apc_k).getLotID(), lotInCassetteList.get(sc_j).getLotID())) {
                                    if(lotInCassetteList.get(sc_j).getMoveInFlag()) {
                                        tmpAPCLotWaferCollectionList.add(strAPCLotWaferCollection.get(apc_k));
                                        res_k++;
                                        bOpeStartFlag = true;
                                        break;
                                    }
                                }
                            }
                            if(bOpeStartFlag) {
                                break;
                            }
                        }
                    }
                    strAPCRunTimeCapability.get(apc_j).setStrAPCLotWaferCollection(tmpAPCLotWaferCollectionList);
                }
            }
            apcRunTimeCapabilityResponseList = apcRunTimeCapabilityResponses;
        } else {
            /*-----------------------------------------------------*/
            /*   Call controlJob_APCRunTimeCapability_GetDR        */
            /*-----------------------------------------------------*/
            apcRunTimeCapabilityResponseList = controlJobMethod.controlJobAPCRunTimeCapabilityGetDR(objCommon, controlJobID);
        }
        return apcRunTimeCapabilityResponseList;
    }

    public List<Infos.StartCassette> sxAPCRecipeParameterAdjustInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, List<Infos.APCRunTimeCapabilityResponse> strAPCRunTimeCapabilityResponse, boolean finalBoolean){
        /*-------------------------------------*/
        /*   call cassette_ForAPC_GetDR        */
        /*-------------------------------------*/
        List<Infos.ApcBaseCassette> apcBaseCassetteList = cassetteMethod.cassetteAPCInformationGetDR(objCommon, equipmentID, strStartCassette);
        /*---------------------------------------------------*/
        /*   Call APCMgr_SendRecipeParameterRequest          */
        /*---------------------------------------------------*/
        List<Infos.APCRecipeParameterResponse> apcRecipeParameterResponses = null;
        try {
            apcRecipeParameterResponses = apcMethod.APCMgrSendRecipeParameterRequest(objCommon, equipmentID, apcBaseCassetteList, strAPCRunTimeCapabilityResponse, finalBoolean);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())) {
                e.setData(strStartCassette);
            }
            throw e;
        }
        /*---------------------------------------------*/
        /*   Call lot_APCRecipeParameterRespose        */
        /*---------------------------------------------*/
        return lotMethod.lotAPCRecipeParameterResponse(objCommon, strStartCassette, apcRecipeParameterResponses);
    }

    public Results.EntityListInqResult sxEntityListInq(Infos.ObjCommon objCommon, String entityClass, String searchKeyName, String searchKeyValue, String option){
        Results.EntityListInqResult entityListInqResult = new Results.EntityListInqResult();
        entityListInqResult.setEntityClass(entityClass);
        // -----------------------------------------------------------------------------------------------------------------
        //   Initialize
        // -----------------------------------------------------------------------------------------------------------------
        if (CimStringUtils.equals(entityClass, BizConstant.SP_APC_ENTITYCLASS_EQUIPMENTTYPE)){
            List<Infos.EntityValue> entityValueList = equipmentMethod.equipmentTypeListGetDR(objCommon);
            entityListInqResult.setEntityValueList(entityValueList);
        } else if(CimStringUtils.equals(entityClass, BizConstant.SP_APC_ENTITYCLASS_EQUIPMENT)){
            List<Infos.EntityValue> entityValueList = equipmentMethod.equipmentListGetDR(objCommon, searchKeyName, searchKeyValue, option);
            entityListInqResult.setEntityValueList(entityValueList);
        } else {
            log.info("entityClass error. Entity_list_GetDR obj not called.");
        }
        return entityListInqResult;
    }
}
