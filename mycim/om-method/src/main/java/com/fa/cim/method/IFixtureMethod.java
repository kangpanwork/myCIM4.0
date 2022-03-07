package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/14       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/10/14 16:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IFixtureMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/16 10:06
     * @param objCommon
     * @param equipmentID
     * @param lotID
     * @param fixtureID
     * @param fixturePartNumber
     * @param fixtureGroupID
     * @param fixtureCategoryID
     * @param fixtureStatus
     * @param maxRetrieveCount -
     * @return java.util.List<com.fa.cim.dto.Results.FixtureListInqResult>
     */
    Results.FixtureListInqResult fixtureFillInTxPDQ001DR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID,
                                                               ObjectIdentifier fixtureID, String fixturePartNumber, ObjectIdentifier fixtureGroupID,
                                                               ObjectIdentifier fixtureCategoryID, String fixtureStatus, Long maxRetrieveCount);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/17 13:21
     * @param objCommon
     * @param fixtureID -
     * @return com.fa.cim.dto.Outputs.objFixtureUsageLimitationCheckOut
     */
    Outputs.objFixtureUsageLimitationCheckOut fixtureUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier fixtureID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param startFixtureList
     * @return
     * @author PlayBoy
     * @date 2018/7/30
     */
    void fixtureStateCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartFixtureInfo> startFixtureList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param fixtureID
     * @return
     * @author PlayBoy
     * @date 2018/7/30
     */
    void fixtureUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier fixtureID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param lotID     lotID
     * @param fixtureID fixtureID
     * @return RetCode<Object>
     * @author PlayBoy
     * @date 2018/8/8
     */
    void fixtureUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier fixtureID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry_Huang
     * @date 2020/11/06 10:06
     * @param objCommon
     * @param fixtureID
     * @return java.util.List<com.fa.cim.dto.Results.FixtureListInqResult>
     */
    Results.FixtureStatusInqResult fixtureFillInTxPDQ002DR(Infos.ObjCommon objCommon, ObjectIdentifier fixtureID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param fixtureID
     * @return void
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/12 13:00
     */
    void fixtureStateChange(Infos.ObjCommon strObjCommonIn, ObjectIdentifier fixtureID,String fixtureStatus);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry_Huang
     * @date 2020/11/11
     * @param objCommon
     * @param fixtureID
     * @return java.util.List<com.fa.cim.dto.Results.FixtureListInqResult>
     */
    Results.FixtureStatusChangeRptResult fixtureFillInTxPDR001(Infos.ObjCommon objCommon, ObjectIdentifier fixtureID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param fixtureID
     * @return void
     * @exception
     * @author ho
     * @date 2020/11/11 18:00
     */
    void fixtureUsageInfoReset(Infos.ObjCommon strObjCommonIn, ObjectIdentifier fixtureID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @param equipmentID
     * @param strXferFixture
     * @return void
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/13 13:00
     */
    Outputs.ObjFixtureChangeTransportStateOut fixtureChangeTransportState(Infos.ObjCommon strObjCommonIn,ObjectIdentifier stockerID,ObjectIdentifier equipmentID,List<Infos.XferFixture> strXferFixture);

}
