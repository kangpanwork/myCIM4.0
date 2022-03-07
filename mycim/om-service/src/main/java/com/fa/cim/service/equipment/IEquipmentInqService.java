package com.fa.cim.service.equipment;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfo;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfoInqParams;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.layoutrecipe.LayoutRecipeResults;

import java.util.List;

/**
 * description:
 * <p>IEquipmentInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/9/009 15:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentInqService {

    List<Infos.CandidateChamberStatusInfo> sxChamberStatusSelectionInq(Infos.ObjCommon objCommon,
                                                                       ObjectIdentifier equipmentID);

    Results.EquipmentModeSelectionInqResult sxEquipmentModeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String modeChangeType, List<ObjectIdentifier> portIDs);

    /**
     * description: According to the identity of the device all state or device swappable state query
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/20 14:35                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/20 14:35
     * @param objCommon
     * @param equipmentID
     * @param allInquiryFlag -
     * @return com.fa.cim.dto.Results.EqpStatusSelectionInqResult
     */
    Results.EqpStatusSelectionInqResult sxEqpStatusSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                                boolean allInquiryFlag);

    /**
     * description: send EAP info and return info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/22 15:25                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/22 15:25
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.Results.EqpEAPInfoInqResult
     */
    Results.EqpEAPInfoInqResult sxEqpEAPInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description: get the equipment's note information through the equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 9:38                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 9:38
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.Results.EqpMemoInfoInqResult
     */
    Results.EqpMemoInfoInqResult sxEqpMemoInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description: get equipment's recipe parameter
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 9:44                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 9:44
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.EqpRecipeParameterListInqResult
     */
    Results.EqpRecipeParameterListInqResult sxEqpRecipeParameterListInq(Infos.ObjCommon objCommon, Params.EqpRecipeParameterListInq params);

    /**
     * description: get physical recipe by equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 9:48                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 9:48
     * @param objCommon
     * @param equipmentID -
     * @return com.fa.cim.dto.Results.EqpRecipeSelectionInqResult
     */
    Results.EqpRecipeSelectionInqResult sxEqpRecipeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description: get lot move in info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 10:39                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 10:39
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.LotsMoveInInfoInqResult
     */
    Results.LotsMoveInInfoInqResult sxLotsMoveInInfoInq(Infos.ObjCommon objCommon, Params.LotsMoveInInfoInqParams params);

    Results.LotsMoveInInfoInqResult sxLotsMoveInInfoForIBInq(Infos.ObjCommon objCommon, Params.LotsMoveInInfoForIBInqParams params);

    Results.EqpInfoForIBInqResult sxEqpInfoForIBInq(Infos.ObjCommon objCommon, Params.EqpInfoForIBInqParams params);

    /**
     * description: get equipment info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 14:10                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 14:10
     * @param objCommon
     * @param eqpInfoInqParams -
     * @return com.fa.cim.dto.Results.EqpInfoInqResult
     */
    Results.EqpInfoInqResult sxEqpInfoInq(Infos.ObjCommon objCommon, Params.EqpInfoInqParams eqpInfoInqParams);

    /**
     * description: get equipment list by step
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 14:10                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 14:10
     * @param objCommon
     * @param strEqpListByStepInqInParm -
     * @return java.util.List<com.fa.cim.dto.Infos.AreaEqp>
     */
    List<Infos.AreaEqp> sxEqpListByStepInq(Infos.ObjCommon objCommon, Params.EqpListByStepInqParm strEqpListByStepInqInParm);

    /**
     * description: get information about the stocker and state of the equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/22 14:25                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/22 14:25
     * @param objCommon
     * @param paramIn -
     * @return com.fa.cim.dto.Results.EqpListByBayInqResult
     */
    Results.EqpListByBayInqResult sxEqpListByBayInq(Infos.ObjCommon objCommon, Params.EqpListByBayInqInParm paramIn);

    /**
     * description: get equipment id list
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 14:13                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 14:13
     * @param objCommon -
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> sxAllEqpListByBayInq(Infos.ObjCommon objCommon);

    /**
     * description: get area
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 14:15                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 14:15
     * @param objCommon
     * @param userID -
     * @return java.util.List<com.fa.cim.dto.Infos.WorkArea>
     */
    List<Infos.WorkArea> sxBayListInq(Infos.ObjCommon objCommon, ObjectIdentifier userID);

    /**
     * description: get equipment buffer info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 16:58                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 16:58
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.EqpBufferInfoInqResult
     */
    Results.EqpBufferInfoInqResult sxEqpBufferInfoInq(Infos.ObjCommon objCommon, Params.EqpBufferInfoInqInParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 17:09                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 17:09
     * @param spcCheckInfoInqParams -
     * @return com.fa.cim.dto.Results.SpcCheckInfoInqResult
     */
    Results.SpcCheckInfoInqResult spcCheckInfoInq(Params.SpcCheckInfoInqParams spcCheckInfoInqParams);

    /**
     * description: get general equipment info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 17:08                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 17:08
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.GeneralEqpInfoInqResult
     */
    Results.GeneralEqpInfoInqResult sxGeneralEqpInfoInq(Infos.ObjCommon objCommon, Params.CommonEqpInfoParam params);

    /**
     * description: find last processed cj info
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/25 17:10                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/25 17:10
     * @param objCommon
     * @param params -
     * @return com.fa.cim.eqp.IBFurnaceEQPBatchInfo
     */
    IBFurnaceEQPBatchInfo sxIBFurnaceEQPBatchInfoInq(Infos.ObjCommon objCommon, IBFurnaceEQPBatchInfoInqParams params);

    /**
     * description: equipment furnace search
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentFurnaceSearchParams - eqp search params
     * @param objCommon - obj common
     * @return result
     * @author YJ
     * @date 2021/3/2 0002 16:03
     */
    List<LayoutRecipeResults.EquipmentFurnaceResult> sxEquipmentFurnaceSearchInq(Infos.ObjCommon objCommon, LayoutRecipeParams.EquipmentFurnaceSearchParams equipmentFurnaceSearchParams);
}
