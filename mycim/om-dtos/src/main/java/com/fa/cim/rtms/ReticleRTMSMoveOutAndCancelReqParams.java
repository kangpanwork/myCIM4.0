package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/24        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/24 16:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSMoveOutAndCancelReqParams {

    private String lotId;

    private int waferCount;

    private String opeCategory;

    private User user;
}