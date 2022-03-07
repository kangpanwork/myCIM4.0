package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/15          ********            Nyx                create file
 *
 * @author Nyx
 * @date 2019/10/15 17:33
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBinSummaryMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/10/15 17:34
     * @param objCommon -
     * @param lotID -
     * @param testTypeID -
     * @param value -
     */
    void binSummaryBinRptCountSetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String testTypeID, String value);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @since 2019/10/16 15:46
     * @param objCommon -
     * @param lotID -
     * @param testTypeID -
     * @return java.util.List<com.fa.cim.dto.Infos.WaferBinSummary>
     */
    List<Infos.WaferBinSummary> binSummaryGetByTestTypeDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier testTypeID);
}