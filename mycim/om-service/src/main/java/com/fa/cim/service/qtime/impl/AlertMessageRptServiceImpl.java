package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IMessageMethod;
import com.fa.cim.service.qtime.IAlertMessageRptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/6       ********              lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/8/6 18:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class AlertMessageRptServiceImpl implements IAlertMessageRptService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Override
    public Results.AlertMessageRptResult sxAlertMessageRpt(Infos.ObjCommon objCommon, Params.AlertMessageRptParams alertMessageRptParams){
        Results.AlertMessageRptResult alertMessageRptResult = null;
        /*------------------------------------------------------------------------*/
        /*  Check In-Param                                                        */
        /*                                                                        */
        /*  If EqpID set to InParam then EqpID is used.                           */
        /*  And if StockerID set to InParam then StockerID is used.               */
        /*  However, Setting both of IDs is not allowed.                          */
        /*------------------------------------------------------------------------*/
        ObjectIdentifier tmpEqpOrStkID  = null;
        if (!ObjectIdentifier.isEmptyWithValue(alertMessageRptParams.getEquipmentID()) && !ObjectIdentifier.isEmptyWithValue(alertMessageRptParams.getStockerID())){
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        } else if (!ObjectIdentifier.isEmptyWithValue(alertMessageRptParams.getEquipmentID())){
            tmpEqpOrStkID = alertMessageRptParams.getEquipmentID();
        } else if (!ObjectIdentifier.isEmptyWithValue(alertMessageRptParams.getStockerID())){
            tmpEqpOrStkID = alertMessageRptParams.getStockerID();
        }
        /*------------------------*/
        /*   Make / Send E-Mail   */
        /*------------------------*/
        if (alertMessageRptParams.isNotifyFlag()){
            ObjectIdentifier systemMessageDefinitionMessageIDGetByMessageCodeOut = messageMethod.systemMessageDefinitionMessageIDGetByMessageCode(objCommon, alertMessageRptParams.getSubSystemID(), alertMessageRptParams.getSystemMessageCode());
                /*--------------------------------------*/             //0.01 (R11)
                /*   Make Email Message and Put         */             //0.01 (R11)
                /*                                      */             //0.01 (R11)
                /*   messageID       (Mandatory)        */             //0.01 (R11)
                /*   lotID           *1                 */             //0.01 (R11)
                /*   lotStatus                          */             //0.01 (R11)
                /*   equipmentID     *1                 */             //0.01 (R11)
                /*   routeID         *1                 */             //0.01 (R11)
                /*   operationNumber                    */             //0.01 (R11)
                /*   messageText     (Option)           */             //0.01 (R11)
                /*                                      */             //0.01 (R11)
                /*   At least one of lotID, equipmentID */             //0.01 (R11)
                /*   or routeID must be specified.      */             //0.01 (R11)
                /*   If lotID is filled, lotStatus will */             //0.01 (R11)
                /*   be also filled.                    */             //0.01 (R11)
                /*   If routeID is filled, operationNo  */             //0.01 (R11)
                /*   will be also filled.               */             //0.01 (R11)
                /*                                      */             //0.01 (R11)
                /*   About *1, equipmentID, stockerID,  */             //0.01 (R11)
                /*   or AGVID (one of them) must be set */             //0.01 (R11)
                /*   as equipmentID.                    */             //0.01 (R11)
                /*--------------------------------------*/             //0.01 (R11)
                messageMethod.messageDistributionMgrPutMessage(objCommon, systemMessageDefinitionMessageIDGetByMessageCodeOut,
                        alertMessageRptParams.getLotID(), alertMessageRptParams.getLotStatus(), tmpEqpOrStkID, alertMessageRptParams.getRouteID(), alertMessageRptParams.getOperationNumber(),
                        "****", alertMessageRptParams.getSystemMessageText());


        }
        /*---------------------------------*/
        /*   Create System Message Event   */
        /*---------------------------------*/
        Inputs.SystemMessageEventMakeIn systemMessageEventMakeIn = new Inputs.SystemMessageEventMakeIn();
        BeanUtils.copyProperties(alertMessageRptParams, systemMessageEventMakeIn);
        systemMessageEventMakeIn.setTransactionID(TransactionIDEnum.SYSTEM_MSG_RPT.getValue());
        eventMethod.systemMessageEventMake(objCommon, systemMessageEventMakeIn);
        return alertMessageRptResult;
    }
}
