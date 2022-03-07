package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * This file use to define the IRouteMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/19 11:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRouteMethod {
    
    /**     
     * description:
     *  method:route_productList_GetDR
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/10/19 11:20
     * @param objCommon - 
     * @param routeID -  
     * @return com.fa.cim.pojo.Outputs.ObjRouteProductListGetDRout
     */
    List<ObjectIdentifier> routeProductListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier routeID);

    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/3/8 17:33
     * @param objCommon - 
     * @param routeID -
     * @param operationNumber -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.ObjectIdentifier>
     */
    ObjectIdentifier routeOperationOperationIDGet(Infos.ObjCommon objCommon, ObjectIdentifier routeID, String operationNumber);

    /**
     * This function inquires parts definition of specified route and BOM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/22 15:53
     */
    List<Infos.BOMPartsDefInProcess> routeBOMPartsDefinitionGetDR(Infos.ObjCommon objCommon, ObjectIdentifier bomID, ObjectIdentifier routeID);

    /**
     * This function inquires connected sub route of specified route.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/22 15:56
     */
    List<Infos.OperationInfo> routeConnectedSubRouteGetDR(Infos.ObjCommon objCommon, ObjectIdentifier mainRouteID);
}