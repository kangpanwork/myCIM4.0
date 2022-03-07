package com.fa.cim.eqp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/4        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/3/4 12:30
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class IBFurnaceEQPBatchInfoInqParams {

    private User user;

    private ObjectIdentifier eqpID;
}