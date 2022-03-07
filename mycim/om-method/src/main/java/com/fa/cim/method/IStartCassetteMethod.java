package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/17       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/12/17 16:12
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IStartCassetteMethod {

    /**
     * description:startCassette_processJobExecFlag_Set__090
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2018/12/17 16:19
     * @param objCommon
     * @param startCassettes
     * @param equipmentID -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjStartCassetteProcessJobExecFlagSetOut>
     */
    Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSet(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes, ObjectIdentifier equipmentID);
}
