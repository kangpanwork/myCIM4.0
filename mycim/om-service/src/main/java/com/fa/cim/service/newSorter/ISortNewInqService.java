package com.fa.cim.service.newSorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.Results;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 *
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/06/22       开发NewSorter        Jerry               create file
 *
 * @author Jerry
 * @since 2021/06/22 16:22
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortNewInqService {

    /**
     * description: 查询即将加工的ComponentJob
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param  params -
     * @return
     */
    Info.SortJobInfo sorterActionInq(Infos.ObjCommon objCommon, Params.SorterActionInqParams params,String jobStatus);

    /**
     * description:  通过equipment查询sort action code
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 7:35 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 7:35 下午
     * @param  ‐
     * @return com.fa.cim.sorter.Results.OnlineSorterActionSelectionInqResult
     */
    Results.OnlineSorterActionSelectionInqResult sxOnlineSorterActionSelectionInq(Infos.ObjCommon objCommon, Params.OnlineSorterActionSelectionInqParams params) ;

    /**
     * description: 查询sortJob列表，并使用优先级进行排序
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/10 4:10 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/10 4:10 下午
     * @param  ‐
     * @return java.util.List<com.fa.cim.dto.Infos.SortJobListAttributes>
     */
    List<Info.SortJobListAttributes> sxSJListInq(Infos.ObjCommon objCommon, Params.SJListInqParams params);

    /**
     * description: 获取sortjOb历史记录
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param  params -
     * @return
     */
    Page<Results.SortJobHistoryResult> getSortJobHistory(Infos.ObjCommon objCommon, Params.SortJobHistoryParams params);

    /**
    * description: 查询设备历史记录中存在的carrier和lot
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    *  10:22 上午 ZH Create
    *
    * @author ZH
    * @date  10:22 上午
    * @param  ‐
    * @return void
    */
    Info.CarrierAndLotHistory sxCarrierAndLotHisInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description: 扫码如果vendorLotID对比
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param  params -
     * @return
     */
    void scanbarCodeInq(Infos.ObjCommon objCommon, Params.ScanBarCodeInqParams params);
}
