package com.fa.cim.tms.service.impl;

import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ILocalTransportJobRptService;
import com.fa.cim.tms.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.beanutils.PropertyUtils;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class LocalTransportJobRptService implements ILocalTransportJobRptService {

    @Autowired
    private ITransferJobMethod transferJobMethod;

    public void sxLocalTransportJobRpt(Infos.ObjCommon objCommon, Params.LocalTransportJobReqParams localTransportJobReqParams){
        Params.LocalTransportJobReqParams tempLocalTransportJob = new Params.LocalTransportJobReqParams();
        /*--------------------------------------------*/
        /* Store Data to OTXFERREQ                     */
        /*--------------------------------------------*/
        tempLocalTransportJob.setJobID(localTransportJobReqParams.getJobID());
        List<Infos.LocalTransportJob> localTransportJobs = new ArrayList<>();
        tempLocalTransportJob.setLocalTransportJobData(localTransportJobs);
        if (CimArrayUtils.isNotEmpty(localTransportJobReqParams.getLocalTransportJobData())) {
            for (Infos.LocalTransportJob data : localTransportJobReqParams.getLocalTransportJobData()) {
                Infos.LocalTransportJob localTransportJob = new Infos.LocalTransportJob();
                localTransportJobs.add(localTransportJob);
                BeanUtils.copyProperties(data, localTransportJob);
            }
        }
        List<Infos.TransferJobInfo> transferJobInfoList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(tempLocalTransportJob.getLocalTransportJobData())) {
            localTransportJobReqParams.getLocalTransportJobData().forEach(data -> {
                Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                transferJobInfo.setCarrierID(data.getCarrierID());
                transferJobInfo.setJobID(tempLocalTransportJob.getJobID());
                transferJobInfo.setCarrierJobID(data.getCarrierJobID());
                transferJobInfo.setTransportType("");
                transferJobInfo.setZoneType("");
                transferJobInfo.setN2PurgeFlag(false);
                transferJobInfo.setFromMachineID(data.getFromMachineID());
                transferJobInfo.setFromPortID(data.getFromPortID());
                transferJobInfo.setToStockerGroup(data.getToStockerGroup());
                transferJobInfo.setToMachineID(data.getToMachineID());
                transferJobInfo.setToPortID(data.getToPortID());
                transferJobInfo.setExpectedStartTime("");
                transferJobInfo.setExpectedEndTime(data.getExpectedEndTime());
                transferJobInfo.setEstimatedStartTime("");
                transferJobInfo.setEstimatedEndTime("");
                transferJobInfo.setMandatoryFlag(false);
                transferJobInfo.setPriority(data.getPriority());
                transferJobInfo.setJobStatus("");
                transferJobInfo.setCarrierJobStatus("");
                transferJobInfo.setTimeStamp(DateUtils.getCurrentTimeStamp().toString());
                transferJobInfoList.add(transferJobInfo);
            });
        }
        log.info("【step1】 - transferJobMethod.transferJobPut");
        transferJobMethod.transferJobPut(objCommon, transferJobInfoList);
    }
}
