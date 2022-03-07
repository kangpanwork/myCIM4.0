package com.fa.cim.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/2/27        ********              Decade               create file
 * * @author: Nyx
 *
 * @date: 2021/2/27 17:40
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class LotStbUsageRecycleLimitParams {

    public Boolean newLotFlag;

    private ObjectIdentifier sourceLotID;

}