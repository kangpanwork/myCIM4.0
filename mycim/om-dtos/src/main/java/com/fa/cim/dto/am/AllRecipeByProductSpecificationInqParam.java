package com.fa.cim.dto.am;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description: lot Sampling Rule Check param
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/10 0010        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2020/12/10 0010 13:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class AllRecipeByProductSpecificationInqParam {

    private User user;
    private ObjectIdentifier productID;
    private ObjectIdentifier equipmentID;
    private ObjectIdentifier chamberID;
    private String claimMemo;

}