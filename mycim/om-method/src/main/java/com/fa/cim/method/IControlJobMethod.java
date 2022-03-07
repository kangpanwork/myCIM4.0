package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * IControlJobMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/16        ********             PlayBoy               create file
 *
 * @author PlayBoy
 * @since 2018/7/16 18:43
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IControlJobMethod {

    /**
     * controlJob_ProcessOperationList_GetDR
     * @author ho
     * @param objCommon
     * @param objControlJobProcessOperationListGetDRIn
     * @return
     */
    List<Infos.ProcessOperationLot> controlJobProcessOperationListGetDR(Infos.ObjCommon objCommon, Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2018/8/10 18:15
     * @param objCommonIn
     * @param controlJobID -
     * @return java.util.List<com.fa.cim.pojo.Infos.ControlJobCassette>
     */
    List<Infos.ControlJobCassette> controlJobContainedLotGet(Infos.ObjCommon objCommonIn, ObjectIdentifier controlJobID);

    /**
     * controlJob_lotList_Get
     * @author Ho
     * @param objCommon
     * @param controlJobID
     * @return
     */
    List<Infos.ControlJobLot> controlJobLotListGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     *  controlJob_lotIDList_GetDR
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param controlJobID controlJobID
     * @return List
     * @author PlayBoy
     * @since 2018/7/31
     */
    List<ObjectIdentifier> controlJobLotIDListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * Get Started lot information which is specified with controlJob ID
     *
     * @param objCommon        objCommon
     * @param controlJobID     control job id
     * @param edcItemsNeedFlag edcItemsNeedFlag 是否需要获取EDC item信息，为了EDC的性能优化，
     *                         添加此参数，因为当EDC item过多的时候，该方法的性能会变差
     * @return StartReserveInformation {@link Outputs.ObjControlJobStartReserveInformationOut}
     * @author zqi
     */
    Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationGet(Infos.ObjCommon objCommon,
                                                                                         ObjectIdentifier controlJobID,
                                                                                         boolean edcItemsNeedFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param controlJobID     controlJobID
     * @param controlJobStatus controlJobStatus
     * @return controljob
     * @author PlayBoy
     * @since 2018/9/4
     */
    void controlJobStatusChange(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, String controlJobStatus);

    /**
     * description:
     * <p>
     * This function deletes controljob and deletes a relation of controljob from eqp and cassette and lot.<br/>
     * "controlJobAction" of inpermeter are :<br/>
     * SP_ControlJobAction_Type_delete                       (deleted from eqp and cassette and lot.)<br/>
     * SP_ControlJobAction_Type_delete_From_EQP              (deleted from eqp.)<br/>
     * SP_ControlJobAction_Type_delete_From_LotAndCassette   (deleted from cassette and lot.)<br/>
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param controlJobID     controlJobID
     * @param controlJobAction controlJobAction
     * @return RetCode
     * @author PlayBoy
     * @since 2018/9/5
     */
    void controlJobDelete(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, String controlJobAction);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author PlayBoy
     * @since 2018/9/5 16:45
     * @param objCommon
     * @param equipmentID
     * @param portGroup
     * @param startCassetteList -
     * @return com.fa.cim.pojo.Outputs.ObjControlJobCreateOut
     */
    ObjectIdentifier controlJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroup, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2018/9/27 16:45
     * @param objCommon
     * @param controlJobID -
     * @return com.fa.cim.pojo.Outputs.ObjControlJobAttributeInfoGetOut
     */
    Outputs.ControlJobAttributeInfo controlJobAttributeInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @since 2018/9/27 16:45
     * @param objCommon
     * @param cassetteIDs -
     * @return com.fa.cim.dto.RetCode
     */
    void controlJobRelatedInfoUpdate(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDs);

    /**
     * description:
     * <p>If empty cassette was in input cassette sequence, and if that cassette
     * attendended to Control Job, then this method delete cassette from Control Job<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param cassetteIDList cassetteIDList
     * @return RetCode
     * @author PlayBoy
     * @since 2018/10/8 17:40:39
     */
    void controlJobEmptyCassetteInfoDelete(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon      objCommon
     * @param cassetteIDList cassetteIDList
     * @return RetCode
     * @author PlayBoy
     * @since 2018/10/11 17:52:17
     */
    void controlJobCassetteInfoDelete(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    -
     * @param controlJobID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjControlJobStatusGetOut>
     * @author Sun
     * @since 12/10/2018 4:23 PM
     */
    Outputs.ObjControlJobStatusGetOut controlJobStatusGet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/7/13 16:13
     * @param objCommon
     * @param controlJobID
     * @param processJobs -
     * @return void
     */
    void controlJobProcessWafersSet(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.ProcessJob> processJobs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/29 16:36
     * @param objCommon
     * @param controlJobStartLotWaferInfoGetIn -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjControlJobStartLotWaferInfoGetOut>
     */
    Infos.ControlJobInformation controlJobStartLotWaferInfoGet(Infos.ObjCommon objCommon, Inputs.ControlJobStartLotWaferInfoGetIn controlJobStartLotWaferInfoGetIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/8/22 13:57
     * @param objCommon
     * @param controlJobInfo
     * @param partialOpeCompLotSeqForOpeComp -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void controlJobUpdateForPartialOpeComp(Infos.ObjCommon objCommon, Infos.ControlJobInformation controlJobInfo, List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeComp);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/8/22 14:17
     * @param objCommon
     * @param controlJobID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjControlJobRelatedReticlesGetDROut>
     */
    List<ObjectIdentifier> controlJobRelatedReticlesGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 17:57
     * @param objCommon
     * @param controlJobID -
     * @return java.util.List<com.fa.cim.dto.Infos.APCRunTimeCapabilityResponse>
     */
    List<Infos.APCRunTimeCapabilityResponse> controlJobAPCRunTimeCapabilityGetDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID);

    /**
     * description:
     * <p>controlJob_portGroup_Set</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/4/30/030 13:41
     */
    void controlJobPortGroupSet(Infos.ObjCommon objCommon,ObjectIdentifier controlJobID,ObjectIdentifier portGroupID);

}
