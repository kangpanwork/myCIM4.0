package com.fa.cim.controller.interfaces.engineerDataCollection;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 14:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEngineerDataCollectionController {
    /**
     * description:
     * <p>This function performs SPEC Check and SPC Check, and performs action to the entities specified based on the check result.
     * The actions are Entity Inhibition and Hold.
     * If the target lot belongs to Monitor Group and it is the representative lot,
     * the Monitor Group and Hold of Monitored lot are released according to a setup of the previous operation of lot.
     * And then, if the monitored lot's operation which moved by Hold release is measurement, measurement operation is passed.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author PlayBoy
     * @date 2018/11/1 13:31:48
     */
    Response edcWithSpecCheckActionReq(Params.EDCWithSpecCheckActionReqParams params);

    /**
     * description:
     * <p>This function checks the propriety of the data based on the reported measured value.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author PlayBoy
     * @date 2018/10/18 10:25:41
     */
    Response specCheckReq(Params.SpecCheckReqParams params);

    /**
     * description:
     * <p>This function stores the measured data, which was reported from the eqp, as a temporal data.
     * Usually, this API is issued from the EAP.
     * Also, after a lot is processed on the eqp, the measured data that has been stored temporally will be updated together with the measurement result and be turned to the permanent data.<br/>
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author PlayBoy
     * @date 2018/10/29 18:28:34
     */
    Response edcTransitDataRpt(Params.EDCTransitDataRptParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/29        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/6/19 17:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response spcCheckReq(Params.SPCCheckReqParams spcCheckReqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/2/28 10:47
     * @param spcDoActionReqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response spcDoActionReq(Params.SPCDoActionReqParams spcDoActionReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/12/5 16:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response edcWithSpecCheckActionByPJReq(Params.EDCWithSpecCheckActionByPJReqParams edcWithSpecCheckActionByPJReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/28        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/28 9:40
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response edcByPJRpt(Params.EDCByPJRptInParms params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strDChubDataSendCompleteRptInParam
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author Ho
     * @date 2019/8/6 17:04
     */
    public Response dchubDataSendCompleteRpt(Params.DChubDataSendCompleteRptInParam strDChubDataSendCompleteRptInParam );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strEDCDataUpdateForLotReqInParm
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author Ho
     * @date 2019/8/20 13:14
     */
    public Response edcDataUpdateForLotReq( Params.EDCDataUpdateForLotReqInParm strEDCDataUpdateForLotReqInParm );


    /**
     * description: This function performs SPEC Check and SPC Check, and performs action to the entities specified based on the check result.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/18                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/1/18 13:47
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response edcWithSpecCheckActionByPostTaskReq(Params.EDCWithSpecCheckActionByPostTaskReqParams params);
}