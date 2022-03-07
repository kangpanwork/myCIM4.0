package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the ILotMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 10:29
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface IBankMethod {
    /**
     * description:
     *      get bank info by production bank
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param bankListInqParams -
     * @return com.fa.cim.pojo.obj.Outputs.ObjBankGetByProductionBankDROut
     * @author Bear
     * @since 2018/6/7
     */
    Results.BankListInqResult bankGetByProductionBankDR(Infos.ObjCommon objCommon, Params.BankListInqParams
            bankListInqParams);

    /**
     * description:
     * method:bank_lotSTB_Check
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common object
     * @param productRequestID - productRequestID
     * @param newLotAttributes - new lot Attributes
     * @author NYX
     * @since 2018/5/7
     */
    void bankLotSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes);


    /**
     * description:
     * Check preparation lot type and bank property combination
     * Check source lot's hold state (might not be ONHOLD)
     * Check source lot's inventory state (might be InBank)
     * Check source lot's lot type (might be 'Vendor lot')
     * Check source lot's bank ID (might be the same as input bank ID)
     * Check source lot's product specification ID.(all source lots' product ID must be the same)
     * Check source wafer is allocated for STB or not
     * Check carrier has control job or not
     * Check carrier is on eqp or not
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        -
     * @param bankID           -
     * @param lotType          -
     * @param subLotType       -
     * @param newLotAttributes -
     * @author Bear
     * @since 2018/4/26
     */
    void bankLotPreparationCheck(Infos.ObjCommon objCommon, ObjectIdentifier bankID, String lotType, String subLotType, Infos.NewLotAttributes newLotAttributes);

    /**
     * description:
     * Cancel relation between current carrier - current lot - current wafers
     * Create lot and wafer information with input parameters
     * Create new relation between lot and wafer
     * if assignID is filled, re-assign wafer ID
     * Set created sourceLotID to createdLotID of out parameter.
     * Relation between created lot and carrier is not created by this method.
     * 1st source lot's vendor lot ID and vendor ID is taken over to new lot.
     * Created lot should have the following status
     * (1) productrequest
     * - Planning State     : Planned
     * - Production State   : Completed
     * (2) lot
     * - lot State          : Finished
     * - Production State   : InProduction
     * - Hold State         : NotOnHold
     * - Finished State     : Completed
     * - Process State      : Processed
     * - Inventory State    : InBank
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        -
     * @param productRequestID -
     * @param bankID           -
     * @param newLotAttributes -
     * @return com.fa.cim.pojo.obj.RetCode<Outputs.ObjBankLotPreparationOut>
     * @author Bear
     * @since 2018/4/26
     */
    ObjectIdentifier bankLotPreparation(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, ObjectIdentifier bankID, Infos.NewLotAttributes newLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           -
     * @param bankID              -
     * @param lotType             -
     * @param subLotType          -
     * @param strNewLotAttributes -
     * @author Sun
     * @since 10/25/2018 1:47 PM
     */
    void bankLotPreparationCheckForWaferSorter(Infos.ObjCommon objCommon, ObjectIdentifier bankID, String lotType, String subLotType, Infos.NewLotAttributes strNewLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon           -
     * @param productRequestID    -
     * @param bankID              -
     * @param strNewLotAttributes -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.ObjectIdentifier>
     * @author Sun
     * @since 10/25/2018 2:59 PM
     */
    ObjectIdentifier bankLotPreparationForWaferSorter(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, ObjectIdentifier bankID, Infos.NewLotAttributes strNewLotAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param bankID    -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.WhatNextNPWStandbyLotInqResult>
     * @author Sun
     * @since 10/29/2018 10:42 AM
     */
    List<Infos.WhatNextStandbyAttributes> bankGetLotListByQueryDR(Infos.ObjCommon objCommon, ObjectIdentifier bankID);

    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2019/10/25
     * @param objCommon -
 * @param bankID -
     */
    void bankCheckNonProBank(Infos.ObjCommon objCommon, ObjectIdentifier bankID);
}
