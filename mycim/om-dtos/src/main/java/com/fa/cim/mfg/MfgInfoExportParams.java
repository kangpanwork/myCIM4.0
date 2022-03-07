package com.fa.cim.mfg;

import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/5/10        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/5/10 16:56
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class MfgInfoExportParams {

    private User user;

    private List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos;
}