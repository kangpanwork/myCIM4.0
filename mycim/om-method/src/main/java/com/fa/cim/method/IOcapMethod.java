package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * <p>IOcapMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2021/1/22   ********     Decade     create file
 *
 * @author: hd
 * @date: 2021/1/22 17:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOcapMethod {
    /**
     * description:
     * <p> ocapUpdateRpt
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/21      ********              hd             create file
     * 2021/7/9       ********              neyo           add recipe id inputParam & Optimize the code
     * @author: hd
     * @date: 2021/1/21 9:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void ocapUpdateRpt(Infos.ObjCommon objCommon,Params.OcapReqParams params);

    /**
     * description:ocapInfomationSave
     * <p>
     *     lotID
     *     ocapNo
     *     equimentID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/21      ********              hd             create file
     * 2021/7/9       ********              neyo           Optimize the code, remove the return
     *
     * @author: hd
     * @date: 2021/1/21 9:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void ocapInfomationSave(ObjectIdentifier lotID, String ocapNo, ObjectIdentifier equimentID);

    /**
     * description:ocapInformationGetByLotID
     * <p>
     *     lotID
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/21      ********              hd             create file
     * 2021/7/9       ********              neyo           Optimize the code
     *
     * @author: hd
     * @date: 2021/1/21 9:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Infos.OcapInfo ocapInformationGetByLotID(ObjectIdentifier lotID);

    /**
     * description:ocapCheckEquipmentAndSamplingAndRecipeExchange
     * <p>
     *     ObjectIdentifier equipmentID
     *     List<Infos.StartCassette> startCassetteList
     * change history:
     * date             defect#             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/1/21      ********              hd             create file
     * 2021/7/9       ********              neyo           rename class & add recipe check & optimize some
     * @author: hd
     * @date: 2021/1/21 9:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.StartCassette> ocapCheckEquipmentAndSamplingAndRecipeExchange(ObjectIdentifier equipmentID,
                                                                             List<Infos.StartCassette> startCassettes);


    /**
     * description: ocapHold action Excute by postProcess after SPC check
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/7/8                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/7/8 10:36
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void ocapHoldActionAfterSPCCheckByPostTaskReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

}
