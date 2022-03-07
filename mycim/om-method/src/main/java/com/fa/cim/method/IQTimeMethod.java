package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.newcore.bo.product.CimLot;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * IQTimeMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/8        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/8 18:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IQTimeMethod {

    /**
     * description:
     * Re-Start qtime check by Watch-Dog for the start cenceled operation.
     * In case of canceled operation is target operation of qtime restriction,
     * watchRequiredFlag of the qtime restriction is changed to True.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param startCassetteList
     * @return RetCode<List < Infos.QTimeActionRegisterInfo>>
     * @author PlayBoy
     * @date 2018/8/8
     */
    List<Infos.QTimeActionRegisterInfo> qtimeReSetByOpeStartCancel(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * This function check whether the lot's current operation is inside qtime section or not.
     * method: qTime_CheckConditionForReplaceTarget
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Bear
     * @date 2018/10/29 17:07
     */
    void qTimeCheckConditionForReplaceTarget(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/24 9:57
     */
    void checkMMQTime(CimLot lot);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/24 10:09
     */
    void checkMMQTime(CimLot lot, boolean deleteReq);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/24 10:09
     */
    void checkMMQTime(ObjectIdentifier lotID,boolean deleteReq);



    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strExpiredQrestTime_lotList_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.ExpiredQrestTimeLotListGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/23 10:40
     */
    Infos.ExpiredQrestTimeLotListGetDROut expiredQrestTimeLotListGetDR(Infos.ObjCommon strObjCommonIn,
                                                                                Infos.ExpiredQrestTimeLotListGetDRIn strExpiredQrestTime_lotList_GetDR_in);

    /**
     * description:
     * <p>Get candidate Q-Time definitions information.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                           Wind
     *
     * @param objCommon
     * @param qtimeDefinitionSelectionInqIn
     * @return RetCode<Outputs.ObjQTimeCandidateListGetOut>
     * @author Wind
     * @date 2018/10/30 13:03
     */
    List<Infos.QrestTimeInfo> qtimeCandidateListGet(Infos.ObjCommon objCommon, Inputs.QtimeDefinitionSelectionInqIn qtimeDefinitionSelectionInqIn);

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                          Wind
     *
     * @param objCommon
     * @param objQtimeCandidateListInRouteGetDRIn
     * @return RetCode<Outputs.ObjQTimeCandidateListInRouteGetDROut>
     * @author Wind
     * @date 2018/10/30 16:13
     */
    List<Infos.QrestTimeInfo> qtimeCandidateListInRouteGetDR(Infos.ObjCommon objCommon, Inputs.ObjQtimeCandidateListInRouteGetDRIn objQtimeCandidateListInRouteGetDRIn);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strStartCassette
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/11/1 14:00:51
     */
    void qtimeStopByOpeStart(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassette);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param strQtimeInfoSeq -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Outputs.QrestTimeAction>>
     * @author Jerry
     * @date 2018/11/13 14:48
     */
    List<Outputs.QrestTimeAction> lotQtimeInfoSortByAction(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.QtimeInfo> strQtimeInfoSeq);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param qtimeListInqInfo -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Outputs.QrestLotInfo>>
     * @author Jerry
     * @date 2018/11/9 13:46
     */
    List<Outputs.QrestLotInfo> qTimeLotInfoGetDR(Infos.ObjCommon objCommon, Infos.QtimeListInqInfo qtimeListInqInfo);

    /**       
    * description:  
    * change history:  
    * date             defect             person             comments  
    * ---------------------------------------------------------------------------------------------------------------------  
    * 2021/6/3 11:22                     Aoki               Create  
    *         
    * @author Aoki  
    * @date 2021/6/3 11:22  
    * @param   
    * @return java.util.List<com.fa.cim.dto.Outputs.QrestLotInfo>
    *  
    */
    Page<Outputs.QrestLotInfo> qTimeLotInfoGetDR(Infos.ObjCommon objCommon, Infos.QtimePageListInqInfo qtimePageListInqInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param qTimeType -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Outputs.QrestLotInfo>>
     * @author Jerry
     * @date 2018/11/13 14:48
     */
    List<Outputs.QrestLotInfo> qTimeLotListGetDR(Infos.ObjCommon objCommon, String qTimeType);
    
    /**       
    * description:  
    * change history:  
    * date             defect             person             comments  
    * ---------------------------------------------------------------------------------------------------------------------  
    * 2021/6/3 11:25                     Aoki               Create  
    *         
    * @author Aoki  
    * @date 2021/6/3 11:25  
    * @param   
    * @return java.util.List<com.fa.cim.dto.Outputs.QrestLotInfo>
    *  
    */

    Page<Outputs.QrestLotInfo> qTimeLotListGetDR(Infos.ObjCommon objCommon, String qTimeType, SearchCondition searchCondition);

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
     * @date 11/7/2018 10:06 AM
     */
    void qTimeCheckForMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

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
     * @date 11/7/2018 3:53 PM
     */
    void qTimeInfoMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param lotID     -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/8/2018 10:17 AM
     */
    void qTimeTargetOpeCancelReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param routeID
     * @param operationNumber
     * @param qrestTimeAction -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2018/11/13 14:50
     */
    void qtimeQrestTimeFlagMaint(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNumber, Outputs.QrestTimeAction qrestTimeAction);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params    -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/14/2018 11:25 AM
     */
    void qTimeInfoUpdate(Infos.ObjCommon objCommon, Params.QtimerReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        -
     * @param consistencyCheck -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/16/2018 10:07 AM
     */
    void qTimeActionConsistencyCheck(Infos.ObjCommon objCommon, Inputs.ObjQTimeActionConsistencyCheck consistencyCheck);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/4 15:26
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjQtimeAllClearByRouteChangeOut>
     */
    Outputs.ObjQtimeAllClearByRouteChangeOut qtimeAllClearByRouteChange(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/12 13:59
     * @param objCommon
     * @param originalQTime -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjQtimeOriginalInformationGetOut>
     */
    Outputs.ObjQtimeOriginalInformationGetOut qtimeOriginalInformationGet(Infos.ObjCommon objCommon, String originalQTime);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param startCassetteList -
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.QTimeActionRegisterInfo>>
     * @author Sun
     * @date 12/20/2018 1:51 PM
     */
    List<Infos.QTimeActionRegisterInfo> qtimeSetClearByOpeComp(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/26                          Wind
      * @param objCommon
      * @param lotID
      * @return RetCode<Outputs.ObjQtimeSetClearByOperationCompout>
      * @author Wind
      * @date 2018/12/26 15:27
      */
    Outputs.ObjQtimeSetClearByOperationCompOut qtimeSetClearByOperationComp(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 14:35
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.dto.Outputs.QtimeClearByOpeStartCancelOut
     */
    Outputs.QtimeClearByOpeStartCancelOut qtimeClearByOpeStartCancel(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQTime_triggerOpe_Replace_in
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/6/18 15:56
     */
    void qTimeTriggerOpeReplace(Infos.ObjCommon strObjCommonIn,
                                           Inputs.QTimeTriggerOpeReplaceIn strQTime_triggerOpe_Replace_in);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 14:33
     * @param strObjCommonIn
     * @param qtimeSetByPJCompIn -
     * @return void
     */
    void qtimeSetByPJComp(Infos.ObjCommon strObjCommonIn, Inputs.QtimeSetByPJCompIn qtimeSetByPJCompIn);

    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 14:32
     * @param objCommon
     * @param qTimeTriggerOpeReplaceByPJCompIn -
     * @return void
     */
    void qTimeTriggerOpeReplaceByPJComp(Infos.ObjCommon objCommon, Inputs.QTimeTriggerOpeReplaceByPJCompIn qTimeTriggerOpeReplaceByPJCompIn);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQtimeLotSetClearByOperationCompIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.QtimeLotSetClearByOperationCompOut>
     * @exception
     * @author Ho
     * @date 2019/8/27 14:55
     */
    Infos.QtimeLotSetClearByOperationCompOut qtimeLotSetClearByOperationComp(
            Infos.ObjCommon strObjCommonIn,
            Infos.QtimeLotSetClearByOperationCompIn strQtimeLotSetClearByOperationCompIn );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strLot_qtimeInfo_GetForClearByProcessOperation_in
     * @return com.fa.cim.dto.Infos.LotQtimeInfoGetForClearByProcessOperationOut
     * @exception
     * @author Ho
     * @date 2019/8/28 13:30
     */
    Infos.LotQtimeInfoGetForClearByProcessOperationOut lotQtimeInfoGetForClearByProcessOperation(
            Infos.ObjCommon strObjCommonIn,
            Infos.LotQtimeInfoGetForClearByProcessOperationIn strLot_qtimeInfo_GetForClearByProcessOperation_in );

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strQTimeTargetOpeReplaceIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/9/19 17:11
     */
    void qTimeTargetOpeReplace(
            Infos.ObjCommon strObjCommonIn,
            Infos.QTimeTargetOpeReplaceIn strQTimeTargetOpeReplaceIn );

    /**
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/11 15:10
     * @param objCommon
     * @param lotID
     * @param allClearFlag -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjLotQtimeInfoGetForClearOut>
     */
    Outputs.ObjLotQtimeInfoGetForClearOut lotQtimeInfoGetForClear(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Boolean allClearFlag);
}
