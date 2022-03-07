package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

/**
 * description:
 * This file use to define the ITransMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/18        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/18 11:23
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransMethod {
    /**     
     * description:
     *  method:transAccessControlCheckDR
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/18 13:41
     * @param objCommon -  
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjTransAccessControlCheckOut>
     */
    Outputs.ObjTransAccessControlCheckOut transAccessControlCheckDR(Infos.ObjCommon objCommon);
}