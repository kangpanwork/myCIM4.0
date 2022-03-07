package com.fa.cim.tms.status.recovery.method;

import com.fa.cim.tms.status.recovery.dto.User;
import com.fa.cim.tms.status.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.status.recovery.pojo.Infos;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/4/28        *******              jerry              create file
 *
 * @author: jerry
 * @date: 2018/4/28 17:51
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IUtilsComp {
    //public Infos.ObjCommon setObjCommon(String transactionIDEnumValue, User user);
    Infos.ObjCommon setObjCommon(TransactionIDEnum transactionIDEnum, User user);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param transactionID
     * @param user
     * @return Infos.ObjCommon
     * @author miner
     * @date 2018/7/11
     */
    Infos.ObjCommon setObjCommon(String transactionID, User user);
}
