package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/24       ********              lightyh             create file
 *
 * @author lightyh
 * @since 2019/7/24 22:10
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAutoDispatchControlMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/24 22:25
     * @param objCommon -
     * @param lotAutoDispatchControlUpdateInfo -
     */
    void autoDispatchControlInfoCheck(Infos.ObjCommon objCommon, Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/24 22:27
     * @param objCommon -
     * @param autoDispatchControlInfoUpdateIn -
     */
    void autoDispatchControlInfoUpdate(Infos.ObjCommon objCommon, Inputs.AutoDispatchControlInfoUpdateIn autoDispatchControlInfoUpdateIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/7/25 15:13
     * @param objCommon -
     * @param objAutoDispatchControlInfoGetDRIn -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjAutoDispatchControlInfoGetDROut>
     */
    List<Infos.LotAutoDispatchControlInfo> autoDispatchControlInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn);

}
