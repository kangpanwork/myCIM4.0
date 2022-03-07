package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>ISpecCheckMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/10/19 12:27
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISpecCheckMethod {

    /**
     * description:
     * <p>Do spec check based on the following rules.<br/>
     * If OM_EDC_SCREEN_LIMIT_OVER_NO_CALC is not 0, spec check is done before data calculation.
     * Then, if spec check result of raw data is '5' or '6', derived and delta calculations are done not using the data.
     * If OM_EDC_SCREEN_LIMIT_OVER_NO_CALC is not 0, data calculation and spec check are done after spec check.
     * At that time, if spec check result is '5' or '6', derived and delta calculations are done not using the data.
     * </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentID
     * @param controlJobID
     * @param startCassetteList
     * @return RetCode
     * @author Wind
     * @date 2018/10/19 12:38:45
     * @deprecated
     * @see ISpecCheckMethod#specCheckFillInDR(Infos.ObjCommon, ObjectIdentifier, ObjectIdentifier, List)
     */
    @Deprecated
    Results.SpecCheckReqResult specCheckFillInTxDCC001DR(Infos.ObjCommon objCommon,
                                                         ObjectIdentifier equipmentID,
                                                         ObjectIdentifier controlJobID,
                                                         List<Infos.StartCassette> startCassetteList);

    /**
     * Do spec check based on the following rules.
     *
     * <p>If <b><i>OM_EDC_SCREEN_LIMIT_OVER_NO_CALC</i></b> is not 0, data calculation and spec check are done after spec check.
     * At that time, if spec check result is '5' or '6', derived and delta calculations are done not using the data.
     *
     * <p>The Spec Check Process includes the following 3 functions:
     * <pre>
     *      1. Data Calculation
     *      2. Spec Check
     *      3. Decision of Final State and Action
     * </pre><br>
     *
     *
     * <p><b>1. Data Calculation</b>
     * <br>-------------------
     *
     * <p>If calculationRequiredFlag is TRUE, this calculation must be executed.
     *
     * <p>All Data Items (=dataCollectionItemNam) in the {@link Infos.DataCollectionItemInfo} structure can be classified
     * to one of the following types:
     * <pre>
     *
     *              Item Type          Calculation Type
     *              ---------  --------------------------------
     *     [Case1]  RAW        RAW
     *     [Case2]  DERIVED    DELTA
     *     [Case3]  DERIVED    MEAN / STANDARDDEVIATION / RANGE
     *    </pre>
     *
     * <p>As for Data Items classified to [Case1], dataValue aren't needed to convert.
     * But, dataValue must be calculated for Data Items classified to [Case2] or [Case3]
     * by the follwoing way:<br>
     *
     * <p>[Case2]
     * <pre>
     * 1) Get the collected data when the corresponding previous Measurement Operation has.
     *    (PO.theLotCollectedData which PreviousOperationNumber has. )
     *
     * 2) Calculate Delta Value based on each calculationExpresion
     *
     *    Sample of calculationExpresion
     *    ------------------------------
     *       #.item.wafer.site, item.wafer.site : Pre data - Post data
     *       item.wafer.site, #.item.wafer.site : Post data - Pre data
     *       ( Prefix '#' shows Item of previous Measurement Operation. )
     * </pre>
     *
     * <p>[Case3]
     * <pre>
     * 1) Calculate Mean or Standard deviation or Range
     *
     *    Sample of calculationExpresion
     *    ------------------------------
     *       item.wafer.site, item.wafer.site, item.wafer.site, ...
     *       ( All items in this expresion are included in the current DCDef. )
     * </pre>
     * <br><br>
     *
     * <p><b>2. Spec Check</b>
     * <br>-------------
     * <p>If specCheckRequiredFlag is TRUE, this check must be executed.
     *
     * <ui>
     * <li>[Check0] - No Check</li>
     *
     * <pre>If the first byte of dataValue is '*', '*' must be set in the first byte of
     *       specCheckResult by each dataValue, and the Spec Check isn't executed.
     *    </pre>
     * <li>[Check1] ... Upper Screen Limit</li>
     *
     * <pre>If screenLimitUpperRequired is TRUE, the following check is executed:
     *
     *       If screenUpperLimit < dataValue,
     *         - Set '5' in specCheckResult
     *         - Set actionCodes_uscrn in actionCode
     * </pre>
     * <li>[Check2] ... Lower Screen Limit</li>
     *
     * <pre>If screenLimitLowerRequired is TRUE, the following check is executed:
     *
     *       If screenLowerLimit > dataValue,
     *         - Set '6' in specCheckResult
     *         - Set actionCodes_lscrn in actionCode
     * </pre>
     * <li>[Check3] ... Upper Spec Limit</li>
     *
     * <pre>If specLimitUpperRequired is TRUE, the following check is executed:
     *
     *       If specUpperLimit < dataValue,
     *         - Set '3' in specCheckResult
     *         - Set actionCodes_usl in actionCode
     * </pre>
     * <li>[Check4] ... Lower Spec Limit</li>
     *
     * <pre>If specLimitLowerRequired is TRUE, the following check is executed:
     *
     *       If specLowerLimit > dataValue,
     *         - Set '4' in specCheckResult
     *         - Set actionCodes_lsl in actionCode
     * </pre>
     * <li>[Check5] ... Upper Control Limit</li>
     *
     * <pre>If controlLimitUpperRequired is TRUE, the following check is executed:
     *
     *       If controlUpperLimit < dataValue,
     *         - Set '1' in specCheckResult
     *         - Set actionCodes_ucl in actionCode
     * </pre>
     * <li>[Check6] ... Lower Control Limit</li>
     *
     * <pre>If controlLimitLowerRequired is TRUE, the following check is executed:
     *
     *       If controlLowerLimit > dataValue,
     *         - Set '2' in specCheckResult
     *         - Set actionCodes_lcl in actionCode
     * </pre>
     * </ui>
     *
     * <p>If the dataValue aren't classified to the above [Check0]-[Check6], set '0'
     * in specCheckResult.
     * <br><br>
     *
     *
     * <p><b>3. Decision of Final State and Action</b>
     * <br>-------------------------------------
     *
     * <ui>
     * <li>The worst state ('0'-'6')is finally set in specCheckResult as lot.</li>
     * </ui>
     * <br><br>
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param controlJobID      controlJobID
     * @param startCassetteList StartCassette like List
     * @author ZQI
     * @date 2021/7/7
     */
    Results.SpecCheckReqResult specCheckFillInDR(Infos.ObjCommon objCommon,
                                                         ObjectIdentifier equipmentID,
                                                         ObjectIdentifier controlJobID,
                                                         List<Infos.StartCassette> startCassetteList);

}
