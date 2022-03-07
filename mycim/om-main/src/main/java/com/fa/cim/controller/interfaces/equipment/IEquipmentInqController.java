package com.fa.cim.controller.interfaces.equipment;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfoInqParams;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 11:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentInqController {
    /**
     * description:
     * <p>TxChamberStatusSelectionInq .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/21        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/9/21 14:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response chamberStatusSelectionInq(Params.ChamberStatusSelectionInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-05                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-05 18:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response equipmentModeSelectionInq(Params.EquipmentModeSelectionInqParams params);

    /**
     * description:
     * EqpStatusSelectionInqController .
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/1        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/8/1 15:37
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpStatusSelectionInq(Params.EqpStatusSelectionInqParams params);

    /**
     * description:TxEqpEAPInfoInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpEAPInfoInq(Params.EqpEAPInfoInqParams eqpEAPInfoInqParams);

    /**
     * description:TxEqpMemoAddReq
     * <p>This function inquires the eqp notes that are registered by TxEqpMemoAddReq.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19                            Wind              create file
     *
     * @author: Wind
     * @date: 2018/10/19 14:47
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpMemoInfoInq(Params.EqpMemoInfoInqParams eqpMemoInfoInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/27                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/27 17:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpRecipeParameterListInq(Params.EqpRecipeParameterListInq params);

    /**
     * description:
     * <p>EqpRecipeSelectionInqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/11/27        ********               Lin            create file
     *
     * @author: Lin
     * @date: 2018/11/27 11:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpRecipeSelectionInq(Params.EqpRecipeSelectionInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/10        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/8/10 13:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response LotsMoveInInfoInq(Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/21        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/11/21 13:44
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response LotsMoveInInfoForIBInq(Params.LotsMoveInInfoForIBInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-13                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-13 19:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpInfoForIBInq(Params.EqpInfoForIBInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-07-09                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-07-09 10:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpInfoInq(Params.EqpInfoInqParams eqpInfoInqParams);

    /**
     * description: TxEqpListByStepInq
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/13 10:15:19
     */
    Response eqpListByStepInq(Params.EqpListByStepInqParm parm);

    /**
     * description:TxEqpListByBayInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-13                                  ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-13 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpListByBayInq(Params.EqpListByBayInqInParm eqpListByBayInqInParm);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-11-13                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-11-13 18:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allEqpListByBayInq(Params.EquipmentIDListParams params);

    /**
     * description:TxBayListInq
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-09-18                           ho             create file
     * <p>
     * return:
     *
     * @author ho
     * @date: 2018-09-18 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response bayListInq(Params.BayListInqParams bayListInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/13                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/13 10:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpBufferInfoInq(Params.EqpBufferInfoInqInParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/3/16 15:02
     * @param  -
     * @return com.fa.cim.common.support.Response
     */
    Response spcCheckInfoInq(Params.SpcCheckInfoInqParams spcCheckInfoInqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018-10-25                                  Paladin             create file
     * <p>
     * return:
     *
     * @author Paladin
     * @date: 2018-10-25 10:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response generalEqpInfoInq(Params.CommonEqpInfoParam params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/4 12:28                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/4 12:28
    * @param null -
    * @return
    */
    Response IBFurnaceEQPBatchInfoInq(IBFurnaceEQPBatchInfoInqParams params);
}