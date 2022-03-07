package com.fa.cim.service.post;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:49
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPostInqService {
    Results.CimObjectListInqResult sxCimObjectListInq(Infos.ObjCommon objCommon, Infos.CimObjectListInqInParm cimObjectListInqInParm);

    Results.PostActionPageListInqResult sxPostActionListInq(Infos.ObjCommon objCommon, Params.PostActionListInqParams params);

    List<Infos.ExternalPostProcessFilterInfo> sxPostFilterListForExtInq(Infos.ObjCommon objCommon, Infos.PostFilterListForExtInqInParm parm);
}