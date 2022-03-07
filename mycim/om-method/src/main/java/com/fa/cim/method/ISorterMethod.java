package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

import java.util.List;

public interface ISorterMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon               -
     * @param objSorterJobListGetDRIn -
     * @return com.fa.cim.pojo.obj.Outputs.ObjSorterJobListGetDROut
     * @author Sun
     * @since 2018/09/18
     */
    List<Infos.SortJobListAttributes> sorterJobListGetDR(Infos.ObjCommon objCommon, Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author decade
     * @date 2021/1/29 23:36
     * @param
     * @param
     */
    String reqCategoryGetByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/16 23:36
     * @param objCommon
     * @param sorterJobID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjSorterComponentJobInfoGetDROut>
     */
    List<Infos.SorterComponentJobListAttributes> sorterComponentJobInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID);

    /**
     * description:
     * <p>Change structure type of input parameter
     * from 'pptWaferTransferSequence' to 'pptLotSequence'<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon     objCommon
     * @param waferXferList waferXferList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/8 15:11:58
     */
    Outputs.ObjSorterWaferTransferInfoRestructureOut sorterWaferTransferInfoRestructure(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/6/14 9:25
     * @param objCommon -
     * @param waferXferList -
     * @param originalLocationVerify -
     * @return
     */
    void sorterWaferTransferInfoVerify(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList, String originalLocationVerify);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/6/25 10:30
     * @param objCommon
     * @param strReticleSortInfos -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void sorterReticleTransferInfoVerify(Infos.ObjCommon objCommon, List<Infos.ReticleSortInfo> strReticleSortInfos);

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author Jerry
    * @date 2019/7/30 17:58
    * @param objCommon
    * @param sorterSorterJobStatusGetDRIn
    * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.SorterSorterJobStatusGetDROut>
    */
    Outputs.SorterSorterJobStatusGetDROut sorterSorterJobStatusGetDR(Infos.ObjCommon objCommon, Inputs.SorterSorterJobStatusGetDRIn sorterSorterJobStatusGetDRIn);

    /**
     * description:
     *       此方法没有完成，会涉及到数据的加锁
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/5 15:36
     * @param objCommon
     * @param sorterSorterJobLockDRIn
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     */
    String sorterSorterJobLockDR(Infos.ObjCommon objCommon, Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn);

    /**
     * description: Update SorterJob Status.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/5 16:03
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sorterSorterJobStatusUpdateDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID, String sorterJobStatus);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/6 15:43
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut sorterComponentJobInfoGetByComponentJobIDDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/6 16:39
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void sorterComponentJobStatusUpdateDR(Infos.ObjCommon objCommon, ObjectIdentifier componentJobID, String sorterComponentJobStatus);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/7 11:06
     * @param objCommon
     * @param sorterComponentJobDeleteDRIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void sorterComponentJobDeleteDR(Infos.ObjCommon objCommon, Inputs.SorterComponentJobDeleteDRIn sorterComponentJobDeleteDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/7 11:06
     * @param objCommon
     * @param sorterJobID
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void sorterSorterJobDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/7 11:06
     * @param objCommon
     * @param sorterLinkedJobUpdateDRIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void sorterLinkedJobUpdateDR(Infos.ObjCommon objCommon, Inputs.SorterLinkedJobUpdateDRIn sorterLinkedJobUpdateDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/7 11:12
     * @param objCommon
     * @param sorterComponentJobID
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void sorterWaferSlotmapDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/13 16:18                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/13 16:18
     * @param objCommon
     * @param equipmentID
     * @param portGroupID -
     * @return void
     */
    void sorterCheckConditionForJobCreate(Infos.ObjCommon objCommon, List<Infos.SorterComponentJobListAttributes> strSorterComponentJobListAttributesSequence, ObjectIdentifier equipmentID, String portGroupID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/13 16:27                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/13 16:27
     * @param objCommon
     * @param sorterSorterJobCreateIn -
     * @return com.fa.cim.dto.Infos.SortJobListAttributes
     */

    Infos.SortJobListAttributes sorterSorterJobCreate(Infos.ObjCommon objCommon, Inputs.SorterSorterJobCreateIn sorterSorterJobCreateIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/14 13:44                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/14 13:44
     * @param objCommon
     * @param sortJobListAttributes -
     * @return void
     */
    void sorterSorterJobInsertDR(Infos.ObjCommon objCommon, Infos.SortJobListAttributes sortJobListAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/14 13:44                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/14 13:44
     * @param objCommon
     * @param sortJobListAttributes -
     * @return void
     */
    void sorterComponentJobInsertDR(Infos.ObjCommon objCommon, Infos.SortJobListAttributes sortJobListAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/14 13:44                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/14 13:44
     * @param objCommon
     * @param sorterWaferSlotmapInsertDRIn -
     * @return void
     */
    void sorterWaferSlotmapInsertDR(Infos.ObjCommon objCommon, Inputs.SorterWaferSlotmapInsertDRIn sorterWaferSlotmapInsertDRIn);

}
