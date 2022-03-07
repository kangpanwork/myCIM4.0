package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/3        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/3 11:06
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSStockerInReqParams {

    private Integer reticleId;

    private String reticleName;

    private String stockerName;

    private User user;
}