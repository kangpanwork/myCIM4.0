package com.fa.cim.controller.interfaces.newSorter;

import com.fa.cim.common.support.Response;
import com.fa.cim.sorter.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/6/22        ********            Jerry                create file
 *
 * @author: Jerry
 * @date: 2021/6/22 15:34
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortNewInqController {
    /**
     * description: 返回当前Sorter Job下面的需要加工的Component Job的信息
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
    Response sorterActionInq(Params.SorterActionInqParams params);

    /**
     * description:  查询设备的sorter action code
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 7:14 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 7:14 下午
     * @param  ‐
     * @return com.fa.cim.common.support.Response
     */
    Response onlineSorterActionSelectionInq(Params.OnlineSorterActionSelectionInqParams params);

    /**
     * description: 获取sortjob列表
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:03 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:03 下午
     * @param  ‐
     * @return com.fa.cim.common.support.Response
     */
    Response SJListInq(Params.SJListInqParams params);

    /**
     * description: 获取sortjOb历史记录
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/26 15:58 Ly Create
     *
     * @author Ly
     * @date 2021/6/26 15:58
     * @param  ‐
     * @return
     */
    Response getSortJobHistoryInq(Params.SortJobHistoryParams params);

    /**
    * description: 查询设备历史记录中存在的carrier和lot
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/12 10:24 上午 ZH Create
    *
    * @author ZH
    * @date 2021/7/12 10:24 上午
    * @param  ‐
    * @return com.fa.cim.common.support.Response
    */
    Response carrierAndLotHisInq(Params.OnlineSorterActionSelectionInqParams params);


    /**
     * description: 获取条形码信息
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
    Response scanbarCodeInq(Params.ScanBarCodeInqParams params);

}