package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * This file use to define the ILotFamilyMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/19        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/19 16:33
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotFamilyMethod {
    /**
     * description:
     * In this method, check lot Family ID and set Split Number according to its suffix.
     * Ex)
     * lot Family ID : "xxxxx.01" then Split Number is set as "1"
     * : "xxxxx.02" then Split Number is set as "2"
     * : "xxxxx.A0" then Split Number is set as "100"
     * : "xxxxx.C5" then Split Number is set as "125"
     * In Siview Standard, split digit is supported to 2 byte. It means that only 2 byte digit is considered as split number.
     * Therefore, split number isn't calculated correctly from following lot Family ID.
     * Ex)
     * lot Family ID  : "xxxxx.011"  then Split Number is set as "1"
     * : "xxxxx.A05"  then Split Number is set as "100"
     * : "xxxxx.0A1"  then Split Number is set as "0"
     * : "xxxxx.009"  then Split Number is set as "0"
     * Even if lot Family suffix includes prohibited character, the Split Number is set according to the suffix number.
     * Ex)
     * startmmxx.ksh : export SP_PROHIBIT_CHARACTER=OI
     * lot Family ID : "xxxxx.I5" then Split Number is set as "185" --> Next Split lot ID  : "xxxxx.J0" because "xxxxxx.I6" is prohibitted.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotFamilyID -
     * @return com.fa.cim.pojo.obj.Outputs.ObjLotFamilySplitNoAdjustOut
     * @author jerry
     * @date 2018/4/27
     */
    Outputs.ObjLotFamilySplitNoAdjustOut lotFamilySplitNoAdjust(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID);

    /**
     * description:
     * By this method, check lot Family ID duplication according to following rule.
     * <p>
     * The naming rule is "HH9999999.BBC"      (ex. P10000001.00W)
     * "HH9999999" is separated by last period.
     * HH      : Heading characters are defined by sub lot type in SM
     * 9999999 : 0000001 - 9999999  (Last sequence number is kept by lot type)
     * BB      : Split digit  (00 - Z9)
     * C       : Reserved for System. It represents a manufacturing layer.
     * Duplication check is done for "HH9999999" of LotFamilyID,
     * Ex)
     * Existing lot family : "lot-A.00"  ===> "lot-A.xx" and "lot-A" will be rejected.
     * Existing lot family : "lot-A"       ===> "lot-A.xx" will be rejected.
     * Existing lot family : "lot-A.S.00" ===> "lot-A.S.xx" and "lot-A.S" will be rejected. ("lot-A.xx" is OK)
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotFamilyID -
     * @return com.fa.cim.dto.result.LotFamilyDuplicationCheckDRResult
     * @author Bear
     * @date 2018/6/25
     */
    void lotFamilyDuplicationCheckDR(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/10/10 10:51
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ScrapHistories>>
     */
    List<Infos.ScrapHistories> lotFamilyFillInTxDFQ003DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 2018/10/11 10:00
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<Results.LotFamilyInqResult>
     */
    Results.LotFamilyInqResult lotFamilyFillInTxTRQ007DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:29
     * @param objCommon
     * @param parentLotID
     * @param childLotID -
     * @return void
     */
    void lotFamilyCheckMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param parentLotID -
     * @param childLotID  -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/6/2018 1:48 PM
     */
    void lotFamilyCheckReworkCancel(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/4/4 16:20
     * @param objCommon
     * @param lotFamilyID -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.common.support.ObjectIdentifier>>
     */
    List<ObjectIdentifier> lotFamilyAllLotsGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID);
}
