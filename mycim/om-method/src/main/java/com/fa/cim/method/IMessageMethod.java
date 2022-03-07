package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/6       ********              lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/8/6 22:29
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IMessageMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/8/6 22:33
     * @param objCommon
     * @param subSystemID
     * @param messageCode -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjSystemMessageDefinitionMessageIDGetByMessageCodeOut>
     */
    ObjectIdentifier systemMessageDefinitionMessageIDGetByMessageCode(Infos.ObjCommon objCommon, String subSystemID, String messageCode);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/8/6 22:36
     * @param objCommon
     * @param messageID
     * @param lotID
     * @param lotStatus
     * @param equipmentID
     * @param routeID
     * @param operationNumber
     * @param reasonCode
     * @param messageText -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     */
    void messageDistributionMgrPutMessage(Infos.ObjCommon objCommon, ObjectIdentifier messageID, ObjectIdentifier lotID, String lotStatus,
                                                     ObjectIdentifier equipmentID, ObjectIdentifier routeID, String operationNumber, String reasonCode, String messageText);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strSystemMessageRequestForMultiFabIn
     * @return void
     * @exception
     * @author Ho
     * @date 2019/9/26 11:28
     */
    void systemMessageRequestForMultiFab( Infos.ObjCommon strObjCommonIn,
                                                 Infos.SystemMessageRequestForMultiFabIn strSystemMessageRequestForMultiFabIn);
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @since 2019/10/19 13:28
     * @param objCommon -
     * @param messageQueuePutIn -
     */
    void messageQueuePut(Infos.ObjCommon objCommon, Inputs.MessageQueuePutIn messageQueuePutIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:19
     * @param objCommon
     * @param strSLMMsgQueueRecord -
     * @return void
     */
    void slmMessageQueuePutDR(Infos.ObjCommon objCommon, Infos.StrSLMMsgQueueRecord strSLMMsgQueueRecord);

    /**
     * description:Task-461 carrierOutMessageEventMake
     *                      1.存入Event, Sentinel 去处理Event
     * change history:
     * date             defect#             person             comments
     * ----------------------------------------------------------------------------------------------------------------
     * 2021/8/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/8/3 13:41
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void carrierOutMessageEventMake(Infos.ObjCommon objCommon,
                                    ObjectIdentifier carrierID,
                                    ObjectIdentifier equipmentID);

}
