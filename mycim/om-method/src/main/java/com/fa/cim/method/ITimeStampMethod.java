package com.fa.cim.method;

import com.fa.cim.dto.Infos;

/**
 * description:
 * This file use to define the ITCSMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/10        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/4/10 17:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITimeStampMethod {

    /**
     * description: need to move common code in the future.
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param currentTimeStamp
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     * @param milliseconds
     * @return RetCode<String>
     * @author Sun
     * @date 2018/10/13 18:11
     */
    @Deprecated
    String timeStampDoCalculationold(Infos.ObjCommon objCommon, String currentTimeStamp,
                                              long days, long hours, long minutes, long seconds, long milliseconds);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 9:44
     * @param objCommon
     * @param currentTimeStamp
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     * @param milliseconds -
     * @return com.fa.cim.common.support.RetCode<java.lang.String>
     */
    String timeStampDoCalculation(Infos.ObjCommon objCommon, String currentTimeStamp,
                                           int days, int hours, int minutes, int seconds, int milliseconds);
}
