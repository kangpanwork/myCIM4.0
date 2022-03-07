package com.fa.cim.method;

import com.fa.cim.dto.Results;

/**
 * description:
 * <p>IAMSMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/7/27/027   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/7/27/027 17:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAMSMethod {
    Results.OmsMsgInqResult OmsMsgInfoGet();
}
