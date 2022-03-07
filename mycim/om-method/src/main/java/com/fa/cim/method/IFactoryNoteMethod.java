package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/29       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/11/29 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFactoryNoteMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/11/29 10:22
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EboardInfoInqResult>
     */
    Results.EboardInfoInqResult factoryNoteFillInTxPLQ001DR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/2 10:53
     * @param objCommon
     * @param noticeTitle
     * @param noticeDescription -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void factoryNoteMake(Infos.ObjCommon objCommon, String noticeTitle, String noticeDescription);
}
