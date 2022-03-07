package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.lmg.LotMonitorGroupResults;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/26                             Wind               create file
 *
 * @author: Wind
 * @date: 2018/12/26 13:27
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IMonitorGroupMethod {

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/26                          Wind
      * @param objCommon
      * @param lotID
      * @return RetCode<List<Infos.MonitorGroups>>
      * @author Wind
      * @date 2018/12/26 13:32
      */
    List<Infos.MonitorGroups> monitorGroupGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/20 17:48
     * @param objCommon
     * @param lotID -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjMonitorGroupDeleteCompOut>
     */
    Outputs.ObjMonitorGroupDeleteCompOut monitorGroupDeleteComp(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    void monitorGroupDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    List<Infos.MonitoredLots> monitorGroupDelete(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/06/04                          Zack               Create file.
     * @param objCommon
     * @param lotID
     * @return RetCode<List<Infos.MonitorGroups>>
     * @author Wind
     * @date 2018/12/26 13:32
     */
    void monitorGroupCheckExistance(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    ObjectIdentifier monitorGroupMakeByAuto(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<ObjectIdentifier> productLotIDs);

    LotMonitorGroupResults.LotMonitorGroupHistoryResults monitorGroupMake(Infos.ObjCommon objCommon,
                                                                          ObjectIdentifier lotID, List<Infos.MonRelatedProdLots> monRelatedProdLots, Boolean previousOperationFlag);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strMonitorGroupDeleteCompIn
     * @return com.fa.cim.dto.Infos.MonitorGroupDeleteCompOut
     * @exception
     * @author Ho
     * @date 2019/10/8 14:33
     */
    Infos.MonitorGroupDeleteCompOut monitorGroupDeleteComp100(Infos.ObjCommon strObjCommonIn, Infos.MonitorGroupDeleteCompIn strMonitorGroupDeleteCompIn );
}
