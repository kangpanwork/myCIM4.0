package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/14       ********             lightyh             create file
 *
 * @author lightyh
 * @since 2019/10/14 16:56
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAPCMethod {
    
    /**
     * description: This object function check the parameter use APC I/F or not.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/10/14 16:58
     * @param objCommon -
     * @param strStartCassetteList -
     * @return boolean
     */
    boolean apcInterfaceFlagCheck(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 9:18
     * @param strObjCommonIn
     * @param controlJobID
     * @param strAPCRunTimeCapabilityResponse -
     * @return void
     */
    void apcRuntimeCapabilityRegistDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier controlJobID, List<Infos.APCRunTimeCapabilityResponse>  strAPCRunTimeCapabilityResponse );

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 9:18
     * @param objCommon
     * @param controlJobID -
     * @return void
     */
    void apcRuntimeCapabilityDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 13:41
     * @param objCommon
     * @param equipmentID
     * @param strAPCBaseCassetteList
     * @param strAPCRunTimeCapabilityResponse
     * @param finalBoolean -
     * @return java.util.List<com.fa.cim.dto.Infos.APCRecipeParameterResponse>
     */
    List<Infos.APCRecipeParameterResponse> APCMgrSendRecipeParameterRequest(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.ApcBaseCassette> strAPCBaseCassetteList, List<Infos.APCRunTimeCapabilityResponse> strAPCRunTimeCapabilityResponse, boolean finalBoolean);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/8 15:59
     * @param objCommon
     * @param equipmentID
     * @param portGroupID
     * @param controlJobID
     * @param strOrgStartCassette -
     * @return java.util.List<com.fa.cim.dto.Infos.StartCassette>
     */
    List<Infos.StartCassette> APCMgrSendRecipeParamInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strOrgStartCassette,String operation);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 17:22
     * @param objCommon
     * @param equipmentID
     * @param strAPCBaseCassetteList -
     * @return java.util.List<com.fa.cim.dto.Infos.APCRunTimeCapabilityResponse>
     */
    List<Infos.APCRunTimeCapabilityResponse> APCMgrSendAPCRunTimeCapabilityRequestDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.ApcBaseCassette> strAPCBaseCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/6 16:27
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param controlJobStatus
     * @param strAPCBaseCassetteList -
     * @return void
     */
    void APCMgrSendControlJobInformationDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, String controlJobStatus, List<Infos.ApcBaseCassette> strAPCBaseCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 10:19
     * @param objCommon
     * @param equipmentID -
     * @return java.util.List<com.fa.cim.dto.Infos.APCIf>
     */
    List<Infos.APCIf> APCIFListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 12:51
     * @param objCommon
     * @param operation
     * @param apcIf -
     * @return void
     */
    void APCIFPointInsertDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 15:57
     * @param objCommon
     * @param operation
     * @param apcIf -
     * @return void
     */
    void APCIFPointUpdateDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 16:22
     * @param objCommon
     * @param operation
     * @param apcIf -
     * @return void
     */
    void APCIFPointDeleteDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/11 10:30
     * @param in -
     * @return java.lang.String
     */
    String sendToApcServer(Inputs.ApcIn in);

 }