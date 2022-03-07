package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/18         ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/7/18 18:32
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICodeMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/7/18 18:36
     * @param objCommon
     * @param categoryID
     * @param codeDataIDs -
     * @return com.fa.cim.dto.RetCode
     */
    void codeCheckExistanceDR(Infos.ObjCommon objCommon, String categoryID, List<ObjectIdentifier> codeDataIDs);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param categoryID
     * @param reasonCodeID
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.ReasonCodeAttributes>>
     * @author Ho
     * @date 2019/1/10 14:44:51
     */
    List<Infos.ReasonCodeAttributes> codeReasonDiscriptionGetDR(Infos.ObjCommon strObjCommonIn,String categoryID,ObjectIdentifier reasonCodeID);

    /**
     * description:
     * <p>codeMethod .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/11/21        ********               Lin            create file
     *
     * @author: Lin
     * @date: 2018/11/21 17:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.CodeInfo> codeListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier category);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/10/25 16:59                       Jerry               Create
     *
     * @author Jerry
     * @date 2019/10/25 16:59
     * @param objCommon
     * @param category -
     * @return java.util.List<com.fa.cim.dto.Infos.ReasonCodeAttributes>
     */

    List<Infos.ReasonCodeAttributes> codeFillInTxPLQ010DR(Infos.ObjCommon objCommon, String category);

    /**
     * description:
     * <p>codeMethod .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/11/21        ********               Nyx            create
     *
     * @author: Nyx
     * @date: 2020/4/9 16:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    CimCode convertCodeIDToCodeOr(CimCategory category, ObjectIdentifier reasonCode);

    /**
     * description: find reason code by group ids
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon - common
     * @param reasonCodeQueryParams  - group id list
     * @return attributes
     * @author YJ
     * @date 2021/1/20 0020 16:43
     */
    List<Infos.ReasonCodeAttributes> codeByGroupIdsGet(Infos.ObjCommon objCommon, Params.ReasonCodeQueryParams reasonCodeQueryParams);
}
