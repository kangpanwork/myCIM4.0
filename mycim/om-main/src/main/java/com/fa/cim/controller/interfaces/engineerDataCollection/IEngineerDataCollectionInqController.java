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
 * @date: 2019/7/30 14:13
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEngineerDataCollectionInqController {
    /**
     * description:TxEDCSpecCheckActionResultInq
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
    Response edcSpecCheckActionResultInq(Params.EDCSpecCheckActionResultInqInParms edcSpecCheckActionResultInqInParms);

    /**
     * description:
     * <p>This function returns List of Data Item that fulfills specified condition.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author PlayBoy
     * @date 2018/11/21 10:39:32
     */
    Response edcDataItemListByKeyInq(Params.EDCDataItemListByKeyInqParams params);

    /**
     * description:
     * <p>This function returns List of DC DefinitionID or DC SpecificationID that fulfill specified condition.</p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author ZQI
     * @date 2018/12/11 14:29:41
     */
    Response edcConfigListInq(Params.EDCConfigListInqParams params);

    /**
     * description:
     * <p>This function returns the measurement items of DataCollection.<br/>
     * The information, which belonged to the lot at the time of reservation, is also returned.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params params
     * @return Response
     * @author PlayBoy
     * @date 2018/10/12 10:35:26
     */
    Response edcDataItemWithTransitDataInq(Params.EDCDataItemWithTransitDataInqParams params);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param specCheckResultInqInParms
     * @return
     * @author Ho
     * @date 2018/9/26 13:58:11
     */
    Response specCheckResultInq(Params.SpecCheckResultInqInParms specCheckResultInqInParms);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param edcPlanInfoInqParms
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2018/10/16 13:40:27
     */
    Response edcPlanInfoInq(Params.EDCPlanInfoInqParms edcPlanInfoInqParms);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param edcSpecInfoInqParms
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2018/10/16 13:40:27
     */
    Response edcSpecInfoInq(Params.EDCSpecInfoInqParms edcSpecInfoInqParms);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/8/15 14:56
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response edcDataShowForUpdateInq(Params.EDCDataShowForUpdateInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author: Sun
     * @date: 12/10/2018 4:00 PM
     */
    Response edcDataItemListByCJInq(Params.EDCDataItemListByCJInqParams params);
}