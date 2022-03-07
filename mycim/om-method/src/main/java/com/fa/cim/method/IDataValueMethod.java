package com.fa.cim.method;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>IDataValueMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/10/19 11:12
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDataValueMethod {

    /**
     * Check validity of input values for spec check as follows.
     * <p>
     * 1.Omit the following data.<br/>
     * <blockquote>
     * - Required value type is "STRING".<br/>
     * - In this case, '*' mark will be assigned to specCheckResult field which is in
     *  the {@link Infos.DataCollectionItemInfo} structure of inout parameter.
     * </blockquote>
     * <p>
     * 2.Check the following data
     * <blockquote>
     * - Required value type is not "STRING", and that value can not be converted to the numeric.
     *</blockquote>
     *
     * <p>
     * 3.Get equipment's operation phase.
     *
     * <p>
     * 4.If online_phase != <b><i>SP_EQP_ONLINEMODE_ONLINEREMOTE</i></b><br/>
     * <blockquote> dataValue of itemType:<b><i>SP_DCDEF_ITEM_RAW</i></b><br/>
     * and dataCollectionType:<b><i>SP_DCDEF_MODE_MANU</i></b> must be filled.</blockquote><br/>
     * else <br/>
     * <blockquote>dataValue of itemType:<b><i>SP_DCDEF_ITEM_RAW</i></b> must be filled.</blockquote>
     *
     * <p>
     * <pre>
     * Example:
     *
     * == Input parameter's value ==
     *
     *                   case-1 case-2 case-3 case-4 case-5 case-6 case-7 case-8
     * item-nm type phase Manu   Manu   Manu   Manu   Manu  SemiUp SemiUp SemiUp
     * ------- ---- ---- ------ ------ ------ ------ ------ ------ ------ ------
     * ITEM-M1 RAW  MANU   1.0    1.0    *      1.0           1.0    1.0
     * ITEM-M2 RAW  MANU   0.5    *      *                    0.5    0.5
     * ITEM-A1 RAW  AUTO                                      0.8    0.8
     * ITEM-A2 RAW  AUTO                                      1.2
     * ITEM-A3 RAW  AUTO                                      0.9
     * ITEM-D1 DRVD DRVD
     * ITEM-D2 DRVD DRVD
     *
     * == Returned RC code ==
     *
     * case-1 : return RC_OK
     * case-2 : return RC_OK
     * case-3 : return RC_ALL_DATAVAL_ASTERISK (warning)
     * case-4 : return RC_SOME_DATAVAL_BLANK   (error)
     * case-5 : return RC_ALL_DATAVAL_BLANK    (error)
     * case-6 : return RC_OK
     * case-7 : return RC_SOME_DATAVAL_BLANK   (error)
     * case-8 : return RC_ALL_DATAVAL_BLANK    (error)
     * </pre>
     *
     * @param objCommon         objCommon
     * @param equipmentID       equipmentID
     * @param controlJobID      controlJobID
     * @param startCassetteList List of {@linkplain Infos.StartCassette}
     * @return validity check result {@linkplain Outputs.ObjDataValueCheckValidityForSpecCheckDrOut}
     * @throws ServiceException
     *         If the data business verification fails, throws {@link ServiceException}
     * @throws NumberFormatException
     *         Thrown to indicate that the application has attempted to convert a string to one of the numeric types
     * @author ZQI
     * @date 2021/7/2
     */
    Outputs.ObjDataValueCheckValidityForSpecCheckDrOut dataValueCheckValidityForSpecCheckDR(Infos.ObjCommon objCommon,
                                                                                            ObjectIdentifier equipmentID,
                                                                                            ObjectIdentifier controlJobID,
                                                                                            List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/4/12 15:35
     * @param objCommon
     * @param equipmentID
     * @param controlJob
     * @param lotID
     * @param strDCDef -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.DCDef>>
     */
    List<Infos.DataCollectionInfo> dataValueCheckValidityForSpecCheckByPJ(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJob, ObjectIdentifier lotID, List<Infos.DataCollectionInfo> strDCDef);
}
