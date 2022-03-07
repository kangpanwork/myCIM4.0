package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.eqp.CimEquipmentContaminationDO;
import com.fa.cim.sorter.Info;

import java.util.List;

/**
 * description: contamination method
 * This file use to define the IContaminationMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/25 0025        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2020/11/25 0025 10:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IContaminationMethod {

    /**
     * description: update contamination update
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params    - params
     * @param objCommon - obj
     * @author YJ
     * @date 2020/11/24 0024 10:22
     */
    void lotContaminationUpdate(Params.LotContaminationParams params, Infos.ObjCommon objCommon);

    /**
     * description: check operation check contamination level and pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @param eqpId - eqp id
     * @author YJ
     * @date 2020/11/24 0024 13:10
     */
    void lotCheckContaminationLevelAndPrFlagStepIn(ObjectIdentifier lotId, ObjectIdentifier eqpId, String actionCode);

    /*
    * description:  check carrier exchange and sorter when sorter cj created
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/30 10:58                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/30 10:58
    * @param null -
    * @return
    */
    void lotSorterCJCreateCheck(ObjectIdentifier lotId, ObjectIdentifier eqpId, String actionCode);

    /**
     * description: check pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @param eqpId - eqp id
     * @author YJ
     * @date 2020/11/27 0027 10:31
     */
    void lotCheckPrFlagStepIn(ObjectIdentifier lotId, ObjectIdentifier eqpId);

    /**
     * description: check pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @author YJ
     * @date 2020/11/27 0027 10:31
     */
    void lotCheckPrFlagStepIn(ObjectIdentifier lotId);

    /**
     * description: check operation check contamination level and pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotIds - lot id
     * @param eqpId  - lot id
     * @author YJ
     * @date 2020/11/25 0025 9:56
     */
    void lotCheckContaminationLevelAndPrFlagStepIn(List<ObjectIdentifier> lotIds, ObjectIdentifier eqpId, String actionCode);

    /**
     * description: check contamination level  after lot move out
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId     - lot id
     * @param objCommon - objCommon
     * @author YJ
     * @date 2020/11/24 0024 14:57
     */
    void lotCheckContaminationLevelStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description: check contamination level  after lot move out
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId     - lot id
     * @param objCommon - objCommon
     * @author YJ
     * @date 2020/11/24 0024 14:57
     */
    boolean lotCheckContaminationLevelForHold(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description: check contamination level and pr flag hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId     - lot id
     * @param objCommon - objCommon
     * @author YJ
     * @date 2020/11/24 0024 14:57
     */
    void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId);

    /**
     * description: check contamination level and pr flag hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId     - lot id
     * @param objCommon - objCommon
     * @param eqpId     - eqp id
     * @author YJ
     * @date 2020/11/27 0027 19:33
     */
    void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, ObjectIdentifier lotId, ObjectIdentifier eqpId);

    /**
     * description: check contamination level and pr flag hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param lotIds    - lot ids
     * @param eqpId     - eqp id
     * @author YJ
     * @date 2020/11/27 0027 19:42
     */
    void lotCheckContaminationLevelAndPrFlagStepOut(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIds, ObjectIdentifier eqpId);


    /**
     * description: check contamination level  after lot move out
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotIds    - lot id list
     * @param objCommon - objCommon
     * @author YJ
     * @date 2020/11/24 0024 14:57
     */
    void lotCheckContaminationLevelStepOut(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIds);

    /**
     * description: set step defined contamination level and pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotId - lot id
     * @author YJ
     * @date 2020/11/24 0024 15:51
     */
    void lotContaminationLevelAndPrFlagSet(ObjectIdentifier lotId);

    void lotReqCastCategorySetForPP(ObjectIdentifier lotId);


    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/25/025 11:14
     */
    void carrierProductUsageTypeCheck(ObjectIdentifier productID, ObjectIdentifier lotID, ObjectIdentifier CastID);

    void stbCheck(ObjectIdentifier cassetteID,ObjectIdentifier productRequestID);

    /**
     * description: partial rework cancel , check contamination level and pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param parentLotId - parent lot id
     * @param childLotId  - child lot id
     * @author YJ
     * @date 2020/11/26 0026 10:53
     */
    void lotPartialReworkCancelContaminationCheck(Infos.ObjCommon objCommon, ObjectIdentifier parentLotId, ObjectIdentifier childLotId);

    /**
     * description: check child and parent lot the contamination level and pr flag whether match, if the not match error
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param parentLotId - parent lot id
     * @param childLotId  - child lot id
     */
    void lotCheckContaminationLevelAndPrFlagMatchError(Infos.ObjCommon objCommon, ObjectIdentifier parentLotId, ObjectIdentifier childLotId);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/26/026 17:11
     */
    void inheritContaminationFlagFromParentLot(ObjectIdentifier parentLotId, ObjectIdentifier childLotId);

    /**
     * description: check wafer slot map change , lot contamination level whether match
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon     - common
     * @param waferTransfer - wafer info
     * @author YJ
     * @date 2020/11/27 0027 13:16
     */
    void lotWaferSlotMapChangeCheck(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer);

    /*
    * description: wafer slot change check under qiandao mode
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/15 11:13                      Decade                Create
    *
    * @author Decade
    * @date 2021/6/15 11:13
    * @param null -
    * @return
    */
    void lotOffRouteSorterCheckForQiandao(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer);

    void lotWaferCarrierExchangeChangeCheck(Infos.ObjCommon objCommon, Infos.WaferTransfer waferTransfer, String actionCode);

    /*
    * description:  carrier exchange check by operation carrier category under qiandao mode
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/11 15:34                      Decade                Create
    *
    * @author Decade
    * @date 2021/6/11 15:34
    * @param null -
    * @return
    */
    void carrierExchangeCheckQiandaoMode(Infos.ObjCommon objCommon, ObjectIdentifier castID, ObjectIdentifier LotId, String actionCode);
    
    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/12/2/002 10:16
     */

    void contaminationLvlCheckAmongLots(List<Params.ContaminationAllLotCheckParams> checkParamsList);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/13 10:57                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/13 10:57
    * @param null -
    * @return
    */
    void normalMoveInWaferBondingUsageCheck(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotList);

    void contaminationSorterCheck(Info.ComponentJob componentJob,String productRequestID,ObjectIdentifier equipmentID, ObjectIdentifier controlJJobId);

    /*
    * description:  contamination sorter check under qiandao mode
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/11 19:37                      Decade                Create
    *
    * @author Decade
    * @date 2021/6/11 19:37
    * @param null -
    * @return
    */
    void contaminationSorterCheckForQiandao(Info.ComponentJob componentJob,String productRequestID, ObjectIdentifier eqID);

    void carrierCategoryCheckAmongLotAndCarrier(ObjectIdentifier castID, ObjectIdentifier LotId);

    /*
     * description:  get eqp contamination setting by eqpID
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/7 20:00                      Decade                Create
     *
     * @author Decade
     * @date 2021/6/7 20:00
     * @param null -
     * @return
     */
    List<CimEquipmentContaminationDO> getEqpContaminationByEqp(ObjectIdentifier eqpID);

    /*
     * description:  check if match the contamination carrier exchange table
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/7 20:01                      Decade                Create
     *
     * @author Decade
     * @date 2021/6/7 20:01
     * @param null -
     * @return
     */
    boolean contaminationExchangeMatchCheck(ObjectIdentifier lotID, String sourceCate, String desCate);

    /*
     * description:  check if match the contamination sorter table
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/7 20:01                      Decade                Create
     *
     * @author Decade
     * @date 2021/6/7 20:01
     * @param null -
     * @return
     */
    boolean contaminationSorterMatchCheck(String EqpID, String sourceCate, String desCate);

    /*
     * description:  check if match the contamination operation move table
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/7 20:01                      Decade                Create
     *
     * @author Decade
     * @date 2021/6/7 20:01
     * @param null -
     * @return
     */
    boolean contaminationOperationMatchCheck(String currentCate, String desCate);

    /*
    * description:  check the recipe's active chamber's contamination flag match the lot's contimination flag or not
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/19 13:48                      Decade                Create
    *
    * @author Decade
    * @date 2021/6/19 13:48
    * @param null -
    * @return
    */
    void recipeContaminationCheck(ObjectIdentifier aLogicalRecipe, ObjectIdentifier aMachineRecipe, ObjectIdentifier aLot, ObjectIdentifier eqpID);

}