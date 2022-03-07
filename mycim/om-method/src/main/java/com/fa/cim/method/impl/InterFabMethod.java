package com.fa.cim.method.impl;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimLotTransferPlanDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IInterFabMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/22          ********            light                create file
 *
 * @author: light
 * @date: 2019/11/22 10:22
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class InterFabMethod implements IInterFabMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfig retCodeConfig;

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/3/27 10:10
     * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjInterFabXferPlanListGetDROut interFabXferPlanListGetDR(Infos.ObjCommon objCommon, Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn) {

        //【joseph】we don't do,so we skip this source code.
        Outputs.ObjInterFabXferPlanListGetDROut out = new Outputs.ObjInterFabXferPlanListGetDROut();
        ObjectIdentifier lotID = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getLotID();
        String originalFabID = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getOriginalFabID();
        ObjectIdentifier originalRouteID = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getOriginalRouteID();
        String originalOpeNumber = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getOriginalOpeNumber();
        String destinationFabID = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getDestinationFabID();
        String xferType = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getXferType();
        ObjectIdentifier modifierUserID = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getModifierUserID();
        String modifiedTime = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getModifiedTime();
        String state = objInterFabXferPlanListGetDRIn.getStrInterFabLotXferPlanInfo().getState();
        log.debug("InParam {}", JSONObject.toJSONString(objInterFabXferPlanListGetDRIn));
        //Set current FabID
        String currentFabID = StandardProperties.OM_SITE_ID.getValue();

        log.debug("SP_FAB_ID {}",currentFabID);
        out.setCurrentFabID(currentFabID);

        String HV_TMPBUFFER;
        String HV_BUFFER =
                "SELECT     LOT_ID, " +
                        "   LOT_RKEY, " +
                        "   SEQ_NO, " +
                        "   ORIG_FAB_ID, " +
                        "   ORIG_MAIN_PROCESS_ID, " +
                        "   ORIG_MAIN_PROCESS_RKEY," +
                        "   ORIG_OPE_NO, " +
                        "   DEST_FAB_ID, " +
                        "   DEST_MAIN_PROCESS_ID, " +
                        "   DEST_MAIN_PROCESS_RKEY, " +
                        "   DEST_OPE_NO, " +
                        "   CONTAINER_TYPE, " +
                        "   DESCRIPTION, " +
                        "   MODIFY_USER_ID, " +
                        "   MODIFY_USER_RKEY, " +
                        "   LAST_MODIFY_TIME, " +
                        "   XFER_PLAN_STATE, " +
                        "   STATE_MODIFY_TIME  " +
                        "FROM   OSIFBXFERPLAN";
        boolean bFirstCondition = true;
        //LOT_ID
        if (!CimObjectUtils.isEmpty(lotID.getValue())){
            log.debug("lotID {} ",lotID.getValue());
            HV_TMPBUFFER = String.format(" WHERE LOT_ID LIKE '%s'",lotID.getValue());
            HV_BUFFER += HV_TMPBUFFER;
            bFirstCondition = false;
        }
        //ORIGINAL_FAB_ID
        if (!CimObjectUtils.isEmpty(originalFabID)){
            log.debug("originalFabID {} ",originalFabID);
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER +=" AND";
            }
            HV_TMPBUFFER = String.format(" ORIG_FAB_ID = '%s'",originalFabID);
            HV_BUFFER += HV_TMPBUFFER;
        }
        //ORIGINAL_ROUTE_ID
        if (!ObjectIdentifier.isEmpty(originalRouteID)){
            log.debug("originalFabID {}",originalRouteID.getValue());
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else{
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" ORIG_MAIN_PROCESS_ID LIKE '%s'",originalRouteID.getValue());
            HV_BUFFER += HV_TMPBUFFER;
        }
        // ORIG_OPE_NO
        if (!CimObjectUtils.isEmpty(originalOpeNumber)){
            log.debug("originalOpeNumber {}",originalOpeNumber);
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" ORIG_OPE_NO = '%s'",originalOpeNumber);
            HV_BUFFER += HV_TMPBUFFER;
        }
        //DESTINATION_FAB_ID
        if (!CimObjectUtils.isEmpty(destinationFabID)){
            log.debug("destinationFabID {}",destinationFabID);
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" DEST_FAB_ID = '%s'",destinationFabID);
            HV_BUFFER += HV_TMPBUFFER;
        }
        //XFER_TYPE
        if (!CimObjectUtils.isEmpty(xferType)){
            log.debug("xferType {}",xferType);
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" CONTAINER_TYPE = '%s'",xferType);
            HV_BUFFER += HV_TMPBUFFER;
        }
        //MODIFIER_ID
        if (!ObjectIdentifier.isEmpty(modifierUserID)){
            log.debug("modifierUserID {}",modifierUserID.getValue());
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" MODIFY_USER_ID = '%s'",modifierUserID.getValue());
            HV_BUFFER += HV_TMPBUFFER;
        }
        //STATE
        if (!CimObjectUtils.isEmpty(state)){
            log.debug("state {}",state);
            if (bFirstCondition){
                bFirstCondition = false;
                HV_BUFFER += " WHERE";
            }else {
                HV_BUFFER += " AND";
            }
            HV_TMPBUFFER = String.format(" XFER_PLAN_STATE = '%s'",state);
            HV_BUFFER += HV_TMPBUFFER;
        }
        HV_BUFFER += " ORDER BY LOT_ID, SEQ_NO";
        //HV_BUFFER += " FOR READ ONLY ";
        log.debug("HV_BUFFER {}",HV_BUFFER);
        //Judge and Convert SQL with Escape Sequence
        boolean bConvertFlag = false;
        String originalSQL = HV_BUFFER;
        log.debug("covertedSQL {}",HV_BUFFER);
        List<CimLotTransferPlanDO> query = cimJpaRepository.query(originalSQL, CimLotTransferPlanDO.class);
        int fetchLimitCount =1000;
        int t_len = 500;
        int count = 0;
        log.debug("t_len Default {}",t_len);
        List<Infos.InterFabLotXferPlanInfo> strPlanInfoSeq = new ArrayList<>();
        for (CimLotTransferPlanDO data : query) {
            log.info("FETCH DATA ------------------ {}",count);
            log.info("OSIFBXFERPLAN: {}",JSONObject.toJSONString(data));
            Infos.InterFabLotXferPlanInfo info = new Infos.InterFabLotXferPlanInfo();
            info.setLotID(ObjectIdentifier.build(data.getLotId(),data.getLotObj()));
            info.setSeqNo(data.getSeqNo());
            info.setOriginalFabID(data.getOriginalFabId());
            info.setOriginalRouteID(ObjectIdentifier.build(data.getOriginalRouteId(),data.getOriginalRouteObj()));
            info.setOriginalOpeNumber(data.getOriginalOpeNo());
            info.setDestinationFabID(data.getDestinationFabId());
            info.setDestinationRouteID(ObjectIdentifier.build(data.getDestinationRouteId(),data.getDestinationRouteObj()));
            info.setDestinationOpeNumber(data.getDestinationOpeNo());
            info.setXferType(data.getXferType());
            info.setDescription(data.getDescription());
            info.setModifierUserID(ObjectIdentifier.build(data.getModifierId(),data.getModifierObj()));
            info.setModifiedTime(data.getModifiedTimestamp().toString());
            info.setState(data.getState());
            info.setStateUpdateTime(data.getStateUpdateTimestamp().toString());
            strPlanInfoSeq.add(info);
            count++;
            Validations.check ( count > fetchLimitCount, retCodeConfig.getFoundInfoLimitOver());
        }
        log.info("Count {}",count);
        out.setStrInterFabLotXferPlanInfoSeq(strPlanInfoSeq);
        if (count == 0){
            throw new ServiceException(retCodeConfig.getInterfabNotFoundXferPlan(),out);
        }
        //confirm the code return OK
        return out;
    }
}
