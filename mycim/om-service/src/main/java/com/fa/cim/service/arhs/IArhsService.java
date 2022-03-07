package com.fa.cim.service.arhs;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:14
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IArhsService {
    /**
     * description: This method creates RDJ for Reticle Pod transfer. (RCJ is not created)
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/4 14:11
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxAsyncReticleXferJobCreateReq(Infos.ObjCommon objCommon, Params.AsyncReticleXferJobCreateReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/5 13:41                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/5 13:41
     * @param objCommon
     * @param params -
     * @return com.fa.cim.dto.Results.RSPXferStatusChangeRptResult
     */
    Results.RSPXferStatusChangeRptResult sxRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, Params.RSPXferStatusChangeRptParams params);

    /**
     * description:This function is internally used by ReticleActionReleaseReq.
     *             When reticle action release is failed, ReticleActionReleaseReq will call ReticleActionReleaseErrorReq to handling the error.
     *             Such as updating job status to "WaitToRelease" and record the fromEquipment, etc.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 9:48
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleActionReleaseErrorReq(Infos.ObjCommon objCommon, Params.ReticleActionReleaseErrorReqParams params);

    /**
     * description: This function is called by action release watchdog to release actions (Create reticle component job from reticle dispatch job).
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 10:55
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.ReticleXferJob sxReticleActionReleaseReq(Infos.ObjCommon objCommon, Params.ReticleActionReleaseReqParams params);

    /**
     * description:This function will cancel a reticle dispatch job.
     *             A reticle dispatch job can be canceled and deleted from system at any status.
     *             If the job is executing, OMS will notify related subsystem such as EAP, RTMS.
     *             The reticle dispatch job is canceled anyway, even if the subsystem interface is not available.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/9 11:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleDispatchJobCancelReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobCancelReqParams params);

    /**
     * description: This function retries a reticle component job which is in error or executing status.
     *              After retry, the errored component job status will be changed to waitToExecute again.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 10:40
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleComponentJobRetryReq(Infos.ObjCommon objCommon, Params.ReticleComponentJobRetryReqParams params);


    /**
     * description:This function skips a reticle component job which is in error or executing status.
     *             After retry, the errored component job status will be changed to complete and is skipped.
     *             The next reticle component job will be waitToExecute.
     *             If the current errored component job is the last one in the reticle dispatch job,
     *             the reticle dispatch job status is changed to complete and is deleted from system.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 11:34
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleComponentJobSkipReq(Infos.ObjCommon objCommon, Params.ReticleComponentJobSkipReqParams params);

    /**
     * description:This function is internally used by WhatReticleActionListReq.
     *             This function will delete the dispatch job from system.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 14:41
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleDispatchJobDeleteReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobDeleteReqParams params);

    /**
     * description:This function is internally used by WhatReticleActionListReq.
     *             This function will insert a new reticle dispatch job.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 14:52
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleDispatchJobInsertReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobInsertReqParams params);

    /**
     * description: This function requests creating retrieve job of Reticle.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/12 15:53
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleRetrieveJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleRetrieveJobCreateReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/13 15:25                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/13 15:25
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleStoreJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleStoreJobCreateReqParams params);

    /**
     * description: This function sends the command for transferring ReticlePod to ReticlePod MCS.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/16 10:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.ReticlePodXferReqResult sxReticlePodXferReq(Infos.ObjCommon objCommon, Params.ReticlePodXferReqParams params);

    /**
     * description: This function is internally used by following functions:
     *         ReticlePodUnclampReq
     *         ReticlePodUnclampRpt
     *         ReticlePodXferJobCompRpt
     *         ReticlePodXferReq
     *         ReticleRetrieveReq
     *         ReticleRetrieveRpt
     *         ReticleStoreReq
     *         ReticleStoreRpt
     *         ReticleDispatchAndComponentJobStatusChangeReq is called to change the reticle dispatch job and reticle component job status.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/16 10:35
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticleDispatchAndComponentJobStatusChangeReq(Infos.ObjCommon objCommon, Params.ReticleDispatchAndComponentJobStatusChangeReqParams params);

    /**
     * description: This function requests transfer of ReticlePod.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/16 15:44
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobCreateReqParams params);

    /**
     * description: This function receives the end report of ReticlePod transfer job.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/17 9:52
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.ReticlePodXferJobCompInfo> sxReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, Params.ReticlePodXferJobCompRptParams params);

    /**
     * description: This function receives the report of ReticlePod unclamp.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/17 16:04
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodUnclampRpt(Infos.ObjCommon objCommon, Params.ReticlePodUnclampRptParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/16 14:01                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/16 14:01
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticleXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleXferJobCreateReqParams params);

    /**
     * description: This function requests umclamp of ReticlePod.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/17 17:05
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodUnclampReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampReqParams params);
    
    /**     
     * description: This function requests transfer of ReticlePod.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/18 10:33                     Nyx                Create
     *       
     * @author Nyx
     * @date 2020/11/18 10:33
     * @param objCommon
     * @param params -
     * @return void
     */
    void sxReticlePodUnclampAndXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampAndXferJobCreateReqParams params);

    /**
     * description: This function requests that UnclampJob of ReticlePod should be created.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/23                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/23 15:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodUnclampJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampJobCreateReqParams params);

    /**
     * description: This function performs deletion/cancellation of ReticlePod transfer request.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/24 10:35
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxReticlePodXferJobDeleteReq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobDeleteReqParams params);
}
