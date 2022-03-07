package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import com.fa.cim.idp.tms.adaptor.common.TmsIdentifier;
import com.fa.cim.idp.tms.adaptor.common.TmsUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/30                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/3/30 10:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsTransportJobCancelReqParam implements TmsAdapt<Inputs.SendTransportJobCancelReqIn,TmsTransportJobCancelReqParam> {
    private static final long serialVersionUID = 6780900165010394641L;
    private TmsUser                                                     requestUserID;
    private String                                                      jobID;
    private List<TmsCarrierJob>                                         carrierJobData;

    @Override
    public Inputs.SendTransportJobCancelReqIn adapt() {
        Inputs.SendTransportJobCancelReqIn result = new Inputs.SendTransportJobCancelReqIn();
        if (null != this.requestUserID){
            result.setUser(this.requestUserID.adapt());
        }
        result.setTranJobCancelReq(new Infos.TranJobCancelReq());
        result.getTranJobCancelReq().setJobID(this.jobID);
        if (null != this.carrierJobData){
            List<Infos.CarrierJob> carrierJobData = new ArrayList<>();
            result.getTranJobCancelReq().setCarrierJobData(carrierJobData);
            for (TmsCarrierJob data : this.getCarrierJobData()) {
                Infos.CarrierJob carrierJob = new Infos.CarrierJob();
                carrierJob.setCarrierID(data.getCarrierID().adapt());
                carrierJob.setCarrierJobID(data.getCarrierJobID());
                carrierJobData.add(carrierJob);
            }
        }
        return result;
    }

    @Override
    public TmsTransportJobCancelReqParam from(Inputs.SendTransportJobCancelReqIn obj) {
        this.requestUserID = new TmsUser().from(obj.getUser());
        if (null != obj.getTranJobCancelReq()){
            this.jobID = obj.getTranJobCancelReq().getJobID();
            List<TmsCarrierJob> carrierJobs = new ArrayList<>();
            this.setCarrierJobData(carrierJobs);
            for (Infos.CarrierJob data : obj.getTranJobCancelReq().getCarrierJobData()) {
                TmsCarrierJob tmsCarrierJob = new TmsCarrierJob();
                carrierJobs.add(tmsCarrierJob);
                tmsCarrierJob.setCarrierID(new TmsIdentifier().from(data.getCarrierID()));
                tmsCarrierJob.setCarrierJobID(data.getCarrierJobID());
            }
        }
        return this;
    }

    @Data
    public static class TmsCarrierJob implements TmsAdapt<Infos.CarrierJob,TmsCarrierJob>{
        private static final long serialVersionUID = 4437985021716144306L;
        private String                                   carrierJobID;
        private TmsIdentifier                            carrierID;

        @Override
        public Infos.CarrierJob adapt() {
            Infos.CarrierJob carrierJob = new Infos.CarrierJob();
            carrierJob.setCarrierJobID(this.carrierJobID);
            if (null != this.carrierID)
                carrierJob.setCarrierID(this.carrierID.adapt());
            return carrierJob;
        }

        @Override
        public TmsCarrierJob from(Infos.CarrierJob obj) {
            this.carrierJobID = obj.getCarrierJobID();
            if (null != obj.getCarrierID())
                this.carrierID = new TmsIdentifier().from(obj.getCarrierID());
            return this;
        }
    }

}
