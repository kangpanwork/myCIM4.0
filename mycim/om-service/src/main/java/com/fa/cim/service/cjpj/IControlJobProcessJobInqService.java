package com.fa.cim.service.cjpj;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IControlJobProcessJobInqService {

    Results.CJPJProgressInfoInqResult sxCJPJProgressInfoInq(Infos.ObjCommon objCommon, Params.CJPJProgressInfoInqParams params);

    Results.CJPJOnlineInfoInqResult sxCJPJOnlineInfoInq(Infos.ObjCommon objCommon, Params.CJPJOnlineInfoInqInParams params);
}
