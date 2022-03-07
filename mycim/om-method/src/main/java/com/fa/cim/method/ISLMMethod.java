package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * <p>ISLMMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/29 15:10         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/29 15:10
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface ISLMMethod {

    /**
     * Perform check for SLM operations.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/29 15:14
     */
    void slmCheckConditionForOperation(Infos.ObjCommon objCommon,
                                       ObjectIdentifier equipmentID,
                                       String portGroupID,
                                       ObjectIdentifier controlJobID,
                                       List<Infos.StartCassette> strStartCassette,
                                       List<Infos.MtrlOutSpec> strMtrlOutSpecSeq,
                                       String operation);

    /**
     * description:
     * <p>SLM_CheckConditionForCassetteReserve</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/5/6/006 16:12
     */
    Results.SLMCheckConditionForCassetteReserveResult slmCheckConditionForCassetteReserve(Infos.ObjCommon objCommon,
                                                                                          ObjectIdentifier equipmentID,
                                                                                          ObjectIdentifier lotID,
                                                                                          ObjectIdentifier controlJobID,
                                                                                          List<Infos.SlmSlotMap> dstMapSeq,
                                                                                          List<Infos.SlmSlotMap> srcMapSeq);
    
    /**
     * description:
     * <p>SLM_CheckConditionForPortReserve</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/5/7/007 17:33
     */
    Results.SLMCheckConditionForPortReserveResult slmCheckConditionForPortReserve(Infos.ObjCommon objCommon,
                                                                                      String actionCode,
                                                                                      ObjectIdentifier equipmentID,
                                                                                      ObjectIdentifier cassetteID,
                                                                                      ObjectIdentifier destPortID);
    
    /**
     * description:
     * <p>SLM_materialOutSpec_CombinationCheck</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/5/9/009 16:17
     */
    void slmMaterialOutSpecCombinationCheck(Infos.ObjCommon objCommon, Infos.EqpContainerPositionInfo eqpContainerPositionInfo);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/5/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/5/25 17:43
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.MtrlOutSpec> slmStartReserveInfoForDeliveryMake(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> startCassetteList);
}
