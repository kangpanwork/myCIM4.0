package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/23        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/23 10:59
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSInspectionRequestReqParams {

    private String reticleName;

    private String inspectionType;

    private String des;

    private User user;

}