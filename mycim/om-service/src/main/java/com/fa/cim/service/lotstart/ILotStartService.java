package com.fa.cim.service.lotstart;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotStartService {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/30 10:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    ObjectIdentifier sxNPWLotStartReq(Infos.ObjCommon objCommon, Params.NPWLotStartReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/19 10:58
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sxWaferAliasSetReq(Infos.ObjCommon objCommon, Params.WaferAliasSetReqParams params) ;



    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/8        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/6/8 14:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.WaferLotStartCancelReqResult sxWaferLotStartCancelReq(Infos.ObjCommon objCommon, Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams) ;


    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/7        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/7 9:45
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Results.WaferLotStartReqResult sxWaferLotStartReq(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes, String claimMemo) ;

    /**
    * description: lotStart check
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/21 9:43 Ly Create
    *
    * @author Ly
    * @date 2021/7/21 9:43
    * @param  ‐
    * @return
    */
    void lotStartCheck (Infos.ObjCommon objCommon, ObjectIdentifier productRequestID,
                        Infos.NewLotAttributes newLotAttributes, String claimMemo) ;

}