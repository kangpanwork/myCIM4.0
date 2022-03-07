package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * IOperationMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/30        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/30 22:46
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOperationMethod {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strOperationHistoryFillInTxPLQ008DRIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.OperationHistoryFillInTxPLQ008DROut>
     * @exception
     * @author Ho
     * @date 2019/4/25 14:25
     */
    Infos.OperationHistoryFillInTxPLQ008DROut operationHistoryFillInTxPLQ008DR(Infos.ObjCommon strObjCommonIn, Infos.OperationHistoryFillInTxPLQ008DRIn strOperationHistoryFillInTxPLQ008DRIn);

    /**
     * description:
     * <p>Get cassetteID / lotID by each load purpose type.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return ObjOperationStartLotCountByLoadPurposeTypeOut
     * @author PlayBoy
     * @date 2018/7/30
     */
    Outputs.ObjOperationStartLotCountByLoadPurposeTypeOut operationStartLotLotCountGetByLoadPurposeType(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p>Get operation's pdType for Virtual Operation.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   objCommon
     * @param operationID operationID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/7 11:27:37
     */
    Outputs.ObjOperationPdTypeGetOut operationPdTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier operationID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/26 13:07
     * @param objCommon
     * @param strStartCassette -
     * @return java.util.List<com.fa.cim.dto.Infos.StartCassette>
     */
    List<Infos.StartCassette> operationStartCassetteSet(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     *             check current operation step and next operation 'step' carrier category is same and send to EAP carrierExchangeFlag
     *             1.same not send and different send
     *             1.1 A - B: change
     *             1.2 A - null: no change
     *             1.3 null - B:
     *                  - 1.3.1: check product reserve carrier category is same as B, is same no change , if different change
     *                  - 1.3.1.1: if product reserve carrier category is decided to change check if have empty carrier
     *                  - 1.3.1.2: if have empty carrier check empty carrier cate gory is same as B,is different no change, if same change.
     *             1.4 null - null no change
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/10                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/1/10 17:15
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Boolean getAndCheckBackSideCleanCarrierExchangeByFlowStep(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);
}
