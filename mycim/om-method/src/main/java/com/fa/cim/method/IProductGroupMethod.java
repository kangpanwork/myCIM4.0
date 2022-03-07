package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.common.support.RetCode;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/4                             Wind               create file
 *
 * @author: Wind
 * @date: 2018/12/4 22:11
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProductGroupMethod {
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/4                          Wind
     * @param objCommon -
     * @return RetCode<List<Infos.ProductGroupIDListAttributes>>
     * @author Wind
     * @since 2018/12/4 22:14
     */
    List<Infos.ProductGroupIDListAttributes> productGroupListAttributesGetDR(Infos.ObjCommon objCommon);
}
