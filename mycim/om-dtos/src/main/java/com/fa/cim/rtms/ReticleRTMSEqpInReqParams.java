package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/22        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/7/22 14:39
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSEqpInReqParams {

    private Integer reticleId;

    private String reticleName;

    private String equipmentName;

    private String equipmentType;

    private User user;
}