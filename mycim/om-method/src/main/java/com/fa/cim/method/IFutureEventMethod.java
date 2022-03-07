package com.fa.cim.method;

import com.fa.cim.dto.Infos;

public interface IFutureEventMethod {
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/6 10:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void experimentalLotExecEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo strExperimentalLotDetailResultInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/18 17:09
     * @param objCommon
     * @param transactionID
     * @param testMemo
     * @param experimentalLotRegistInfo -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void experimentalLotRegistEventMake(Infos.ObjCommon objCommon, String transactionID, String testMemo, com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo experimentalLotRegistInfo);

}
