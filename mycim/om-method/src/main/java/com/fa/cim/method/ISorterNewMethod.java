package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.bo.sorter.CimSorterJob;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.Results;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ISorterNewMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 11:20 上午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 11:20 上午
     * @param 11:20 上午
     * @return void
     */
    void sorterCheckConditionForJobCreate(Infos.ObjCommon objCommon, List<Info.ComponentJob> strSorterComponentJobListAttributesSequence,
                                          ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID);

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
     * @param
     * @return com.fa.cim.dto.Infos.SortJobListAttributes
     */
    Params.SJCreateReqParams sorterJobCreate(Infos.ObjCommon objCommon, Params.SJCreateReqParams jobCreateIn);

    /**
     * description: sorterJobInsertDR
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/29 10:57 Ly Create
     *
     * @author Ly
     * @date 2021/6/29 10:57
     * @param  ‐
     * @return
     */
    void sorterJobInsertDR(Infos.ObjCommon objCommon, Params.SJCreateReqParams sortJobListAttributes);


    /**
     * description:
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/25 15:41 Ly Create
     *
     * @author Ly
     * @date 2021/6/25 15:41
     * @param  ‐
     * @return
     */
    Boolean checkVendorLotID(String vendorLotID);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:25
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Info.SortActionAttributes> waferSorterActionListSelectDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);
    /**
     * description: 根据eqpid,carrier ,portId 查询ComponentJob
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param  params -jotStatus
     * @return
     */
    Info.SortJobInfo  sorterActionInq(Infos.ObjCommon objCommon, Params.SorterActionInqParams params,String jotStatus );



    /**
     * description: 获取sortjob列表
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:01 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:01 下午
     * @param  ‐
     * @return java.util.List<com.fa.cim.sorter.Info.SortJobListAttributes>
     */
    List<Info.SortJobListAttributes> sorterJobListGetDR(Infos.ObjCommon objCommon, Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn);

    /**
     * description: new sorter: 对设备的sort action 进行增删改
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 17:05
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void waferSorterActionListInsertDR(Infos.ObjCommon objCommon, List<Info.SortActionAttributes> strWaferSorterActionListSequence, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 3:39 下午                 jerry              Create
     * @author jerry
     * @date 2021/6/30 3:39 下午
     * @param objCommon
     * @param sortJobInfo
     * @return void
     */
    void waferSorterCheckConditionForAction(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 5:04 下午                 jerry              Create
     * @author jerry
     * @date 2021/6/30 5:04 下午
     * @param objCommon
     * @return void
     */
    void sorterWaferTransferInfoVerify(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo, String action);

    void cassetteCheckConditionForWaferSort(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo);

    Results.ObjSorterWaferTransferInfoRestructureOut sorterWaferTransferInfoRestructure(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo);


    /**
     * description: sortJobID 获取SortJobPostAct
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    Info.SortJobPostAct getPostAct(String sortJobID);

    /**
     * description: 根据sortJobId查询OHSORTJOB_COMP_SLOTMAP历史记录
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    List<Info.SlotmapHisRecord> getSorterjobSlotmapHisRecord(String sortJobId);

    /**
     * description: 根据当前SortJob和componentJob 获取slotMapList
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    Info.SortJobInfo getSortJobInfoResultBySortJob(Infos.ObjCommon objCommon,
                                                            ObjectIdentifier sortJob, String componentJob);


    /**
     * description:获取历史记录
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/30 13:15 Ly Create
     *
     * @author Ly
     * @date 2021/6/30 13:15
     * @param  ‐
     * @return
     */
    Page<Results.SortJobHistoryResult> getSortJobHistory(Infos.ObjCommon objCommon, Params.SortJobHistoryParams params);

    /**
     * description: 此方法没有完成，会涉及到数据的加锁
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/23 9:48 上午 ZH Create
     *
     * @author ZH
     * @date 2021/6/23 9:48 上午
     * @param  ‐
     * @return java.lang.String
     */
    String sorterSorterJobLockDR(Infos.ObjCommon objCommon, Info.SorterSorterJobLockDRIn sorterSorterJobLockDRIn);

    /**
     * description:
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/23 9:58 上午 ZH Create
     *
     * @author ZH
     * @date 2021/6/23 9:58 上午
     * @param  ‐
     * @return com.fa.cim.sorter.Results.ObjSorterComponentJobInfoGetByComponentJobIDDROut
     */
    Info.ObjSorterComponentJobInfoGetByComponentJobIDDROut sorterComponentJobInfoGetByComponentJobIDDR(Infos.ObjCommon objCommon, ObjectIdentifier sorterComponentJobID);

    /**
     * description: 修改sortJob/componentJob的顺序
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/23 9:53 上午 ZH Create
     *
     * @author ZH
     * @date 2021/6/23 9:53 上午
     * @param  ‐
     * @return void
     */
    void sorterLinkedJobUpdateDR(Infos.ObjCommon objCommon, Info.SorterLinkedJobUpdateDRIn orterLinkedJobUpdateDRIn);

    /**
     * description: 修改sortJob的状态
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/23 1:42 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/23 1:42 下午
     * @param  ‐
     * @return void
     */
    void sorterAndCompnentJobStatusUpdateDR(Infos.ObjCommon objCommon, Info.SorterComponentJobType sorterComponentJobType);

    /**
     * description: 删除sortJob/componentJob/slotMap
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/28 4:52 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/28 4:52 下午
     * @param  ‐
     * @return void
     */
    void sorterJobInfoDeleteDR(Infos.ObjCommon objCommon, Info.SorterComponentJobDeleteDRIn sjDeleteParam);

    /**
     * description: 根据componentJobID获取sortJob的BO对象
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/29 1:34 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/29 1:34 下午
     * @param  ‐
     * @return com.fa.cim.newcore.bo.sorter.CimSorterJob
     */
    CimSorterJob getCimSortJobByComponentJobID(String componentJobID);

    /**
     * description: 根据lotId，aliasName检查他们之间关系
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 10:58 Ly Create
     *
     * @author Ly
     * @date 2021/6/22 10:58
     * @param  ‐
     * @return
     */
    Info.LotAliasName checkLotAndAliasNameRelationship(ObjectIdentifier lotId, String aliasName);


    /**
     * description: actionCode=waferStart，获取数量为空的vendorlotID
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/22 9:50 Ly Create
     *
     * @author Ly
     * @date 2021/6/22 9:50
     * @param  ‐
     * @return
     */
    List<String> getVendorlotIDs(ObjectIdentifier sortJobId);


    /**
    * description:获取ComponentJob历史记录详情
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/1 13:44 Ly Create
    *
    * @author Ly
    * @date 2021/7/1 13:44
    * @param  ‐
    * @return
    */
    List<Info.SorterComponentJobListAttributes>getComponentJobHistoryRecordReListByRefkey(Infos.ObjCommon objCommon,String refkey);

    /**
     * description:做waferStart时更新OMSORTJOB_COMP_SLOTMAP表中AliasName
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/7/7 11:23 Ly Create
     *
     * @author Ly
     * @date 2021/7/7 11:23
     * @param  ‐
     * @return
     */
    void setAliasNameForComponentJobID(List<String> aliasNames,String componentJobID,String sortJobId );

    /**
    * description: 获取FOUP的category
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/7 15:38 Ly Create
    *
    * @author Ly
    * @date 2021/7/7 15:38
    * @param  ‐
    * @return
    */
    String getCarrierCategory(String carrierID);

    /**
    * description:生成FOSB ID
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/7 15:50 Ly Create
    *
    * @author Ly
    * @date 2021/7/7 15:50
    * @param  ‐
    * @return
    */
    String generateFosbID();

    /**
     * description: updateLotFilpStatus
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/7/8 10:56 Ly Create
     *
     * @author Ly
     * @date 2021/7/8 10:56
     * @param  ‐
     * @return
     */
    void updateLotFilpStatus(String lotID);

    /**
     * description: updateLotFilpStatus
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/7/8 10:56 Ly Create
     *
     * @author Ly
     * @date 2021/7/8 10:56
     * @param  ‐
     * @return
     */
    void updateRelationFoupFlag(ObjectIdentifier srcCarrierID, ObjectIdentifier desCarrierID);


    /**
    * description: 查询最近的一次sort操作记录
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/9 1:29 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/9 1:29 下午
    * @param  ‐
    * @return com.fa.cim.sorter.Info.SlotMapHistory
    */
    Info.SortJobInfo getRecentlyHistory(Infos.ObjCommon objCommon, Params.SJListInqParams params, String actionCode);

    /**
    * description: 获取设备历史记录中存在的lot
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/12 10:29 上午 ZH Create
    *
    * @author ZH
    * @date 2021/7/12 10:29 上午
    * @param  ‐
    * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
    */
    List<ObjectIdentifier> getLotIDByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
    * description: 获取设备历史记录中存在的carrier
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/12 1:49 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/12 1:49 下午
    * @param  ‐
    * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
    */
    List<ObjectIdentifier> getCarrierIDByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
    * description: 修改sortJob的postAct和slotMap表中的lot信息
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/17 10:03 上午 ZH Create
    *
    * @author ZH
    * @date 2021/7/17 10:03 上午
    * @param  ‐
    * @return void
    */
    void updatePostActAndSoltMapLot(ObjectIdentifier sortJobID, ObjectIdentifier childLotID);

    /**
    * description:
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/22 3:57 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/22 3:57 下午
    * @param  ‐
    * @return void
    */
    void cassetteCheckConditionForExchange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList);

    /**
    * description: 修改SorterJob下单个ComponentJob信息
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/5 4:53 下午 ZH Create
    *
    * @author ZH
    * @date 2021/8/5 4:53 下午
    * @param  ‐
    * @return void
    */
    void updateComponentJob(Infos.ObjCommon objCommon, Info.ComponentJob componentJob);


    /**
    * description: 根据sj查询eqpID
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/19 10:48 Ly Create
    *
    * @author Ly
    * @date 2021/8/19 10:48
    * @param  ‐
    * @return
    */
    ObjectIdentifier getEquipmentIDBySortJobID(Infos.ObjCommon objCommon,ObjectIdentifier sortJobID);
}
