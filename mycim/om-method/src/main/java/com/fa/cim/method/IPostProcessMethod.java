package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.jpa.SearchCondition;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/29       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2019/4/29 17:42
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPostProcessMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param key
     * @param prevSeqNo
     * @param seqNoList
     * @param status
     * @param syncFlag
     * @param targetType
     * @param objectID   -
     * @return com.fa.cim.common.support.RetCode<java.util.List   <   com.fa.cim.dto.Infos.PostProcessActionInfo>>
     * @author Nyx
     * @date 2019/4/29 18:04
     */
    List<Infos.PostProcessActionInfo> postProcessQueueGetDR(Infos.ObjCommon objCommon, String key, Long prevSeqNo, List<Long> seqNoList, String status, Integer syncFlag, String targetType, ObjectIdentifier objectID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/8 15:38
     * @param objCommon
     * @param dKey -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void postProcessAdditionalInfoDeleteDR(Infos.ObjCommon objCommon, String dKey);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/28 10:01
     * @param objCommon
     * @param postProcessAdditionalInfoList -
     * @return void
     */
    void postProcessAdditionalInfoInsertDR(Infos.ObjCommon objCommon, List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/28 10:41
     * @param objCommon
     * @param postProcessAdditionalInfoList -
     * @return void
     */
    void postProcessAdditionalInfoInsertUpdateDR(Infos.ObjCommon objCommon, List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/9 16:05
     * @param objCommon
     * @param actionCode
     * @param strPostProcessActionInfoSeq -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.PostProcessActionInfo>>
     */
    List<Infos.PostProcessActionInfo> postProcessQueueUpdateDR(Infos.ObjCommon objCommon, String actionCode, List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/11 16:47
     * @param objCommon
     * @param txID
     * @param patternID
     * @param key
     * @param seqNo
     * @param processRegistrationParm
     * @param claimMemo -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.PostProcessQueueMakeOut>
     */
    Outputs.PostProcessQueueMakeOut postProcessQueueMake(Infos.ObjCommon objCommon, String txID, String patternID, String key, Integer seqNo, Infos.PostProcessRegistrationParam processRegistrationParm, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/11 17:26
     * @param objCommon
     * @param txID
     * @param patternID
     * @param strSearchInfoSeq -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.PostProcessConfigInfo>
     */
    List<Infos.PostProcessConfigInfo> postProcessConfigGetPatternInfoDR(Infos.ObjCommon objCommon, String txID, String patternID, List<Infos.HashedInfo> strSearchInfoSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/31 13:41
     * @param objCommon
     * @param strCollectionInfoSeq
     * @param strSearchInfoSeq -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.HashedInfo>>
     */
    List<Infos.HashedInfo> postProcessPatternSearchConditionGet(Infos.ObjCommon objCommon, List<String> strCollectionInfoSeq, List<Infos.HashedInfo> strSearchInfoSeq);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/13 17:13
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjPostProcessQueListDROut postProcessQueueListDR(Infos.ObjCommon objCommon, Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn);

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/3 17:06                     Aoki               Create
    *
    * @author Aoki
    * @date 2021/6/3 17:06
    * @param
    * @return com.fa.cim.dto.Outputs.ObjPostProcessQueListDROut
    *
    */
    Outputs.ObjPostProcessPageQueListDROut postProcessQueueListDR(Infos.ObjCommon objCommon, Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn, SearchCondition searchConditions);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/8/1 13:56
     * @param objCommon
     * @param postProcessQueueParallelExecCheckIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjPostProcessQueueParallelExecCheckOut>
     */
    Outputs.ObjPostProcessQueueParallelExecCheckOut postProcessQueueParallelExecCheck(Infos.ObjCommon objCommon, Inputs.PostProcessQueueParallelExecCheckIn postProcessQueueParallelExecCheckIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/27 10:31
     * @param objCommon
     * @param carrierID -
     * @return boolean
     */
    boolean postProcessLastFlagForCarrierGetDR(Infos.ObjCommon objCommon, ObjectIdentifier carrierID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/30 9:50
     * @param objCommon
     * @param dKey
     * @param seqNo -
     * @return java.util.List<com.fa.cim.dto.Infos.PostProcessAdditionalInfo>
     */
    List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoGetDR(Infos.ObjCommon objCommon, String dKey, long seqNo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/30 16:39
     * @param objCommon
     * @param key -
     * @return java.util.List<java.lang.String>
     */
    List<String> postProcessRelatedQueueKeyGetDR(Infos.ObjCommon objCommon, String key);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 15:00
     * @param objCommon
     * @param objectType
     * @param objectID -
     * @return void
     */
    void postProcessFilterRegistCheckDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 15:08
     * @param objCommon
     * @param objectType
     * @param objectID -
     * @return void
     */
    void postProcessFilterInsertDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 15:17
     * @param objCommon
     * @param externalPostProcessFilterInfos -
     * @return void
     */
    void postProcessFilterDeleteDR(Infos.ObjCommon objCommon, List<Infos.ExternalPostProcessFilterInfo> externalPostProcessFilterInfos);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/2/16 18:44
     * @param objCommon
     * @param objectType
     * @param objectID
     * @param userID -
     * @return java.util.List<com.fa.cim.dto.Infos.ExternalPostProcessFilterInfo>
     */
    List<Infos.ExternalPostProcessFilterInfo> postProcessFilterGetDR(Infos.ObjCommon objCommon, String objectType, ObjectIdentifier objectID, ObjectIdentifier userID);

    /**
     * Make Post Process Queue information for Durable.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/30 13:21
     */
    Outputs.DurablePostProcessQueueMakeOut durablePostProcessQueueMake(Infos .ObjCommon objCommon, Inputs.DurablePostProcessQueueMakeIn paramIn);
}
