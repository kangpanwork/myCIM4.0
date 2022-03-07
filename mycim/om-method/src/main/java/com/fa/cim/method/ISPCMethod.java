package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>ISPCMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/6        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/11/6 18:17
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISPCMethod {
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon            objCommon
     * @param equipmentID          equipmentID
     * @param controlJobID         controlJobID
     * @param tmpStartCassetteList tmpStartCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/11/6 18:44:52
     */
    Outputs.ObjSPCMgrSendSPCCheckReqOut spcMgrSendSPCCheckReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> tmpStartCassetteList);

}
