package com.fa.cim.service.runcard;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

public interface IRunCardService {
    /**
     * description: RunCard Function Update Service
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/24 13:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxRunCardUpdateReq(Infos.ObjCommon objCommon, Params.RunCardUpdateReqParams params) ;
    /**
     * description: RunCard Function Delete Service
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/24 13:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxRunCardDeleteReq(Infos.ObjCommon objCommon, List<String> runCardIDs) ;
    /**
     * description: RunCard Function Off_Line Approval Service
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/24 13:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxRunCardApprovalReq(Infos.ObjCommon objCommon, Params.RunCardStateApprovalReqParams params) ;
    /**
     * description: RunCard Function State Change Service
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/24 13:08
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxRunCardStateChangeReq(Infos.ObjCommon objCommon, Params.RunCardStateChangeReqParams params) ;
}
