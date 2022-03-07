package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.dto.Infos;
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
 * @date: 2020/3/30 14:47
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsPriorityChangeReqParam implements TmsAdapt<Infos.PriorityChangeReq,TmsPriorityChangeReqParam> {
    private static final long serialVersionUID = 1386693598112839337L;
    private TmsUser requestUserID;
    private String jobID;
    private List<TmsPriorityInfo> priorityInfoData;

    @Override
    public Infos.PriorityChangeReq adapt() {
        Infos.PriorityChangeReq result = new Infos.PriorityChangeReq();
        if (null != this.priorityInfoData){
            List<TmsPriorityInfo> priorityInfoData = this.priorityInfoData;
            List<Infos.PriorityInfo> datas = new ArrayList<>();
            result.setPriorityInfoData(datas);
            for (TmsPriorityInfo data : priorityInfoData) {
                datas.add(data.adapt());
            }
        }
        result.setJobID(this.jobID);
        return result;
    }

    @Override
    public TmsPriorityChangeReqParam from(Infos.PriorityChangeReq obj) {
        this.jobID = obj.getJobID();
        if (null != obj.getPriorityInfoData()){
            List<Infos.PriorityInfo> priorityInfoData = obj.getPriorityInfoData();
            List<TmsPriorityInfo> datas = new ArrayList<>();
            this.priorityInfoData = datas;
            for (Infos.PriorityInfo data : priorityInfoData) {
                datas.add(new TmsPriorityInfo().from(data));
            }
        }
        return this;
    }

    @Data
    public static class TmsPriorityInfo implements TmsAdapt<Infos.PriorityInfo,TmsPriorityInfo>{
        private static final long serialVersionUID = -6050979376775126974L;
        private String carrierJobID;
        private TmsIdentifier carrierID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private Object reserve;

        @Override
        public Infos.PriorityInfo adapt() {
            Infos.PriorityInfo priorityInfo = new Infos.PriorityInfo();
            if (null != this.carrierID)
                priorityInfo.setCarrierID(this.carrierID.adapt());
            priorityInfo.setCarrierJobID(this.carrierJobID);
            priorityInfo.setExpectedStartTime(this.expectedStartTime);
            priorityInfo.setExpectedEndTime(this.expectedEndTime);
            priorityInfo.setMandatoryFlag(this.mandatoryFlag);
            priorityInfo.setPriority(this.priority);
            priorityInfo.setReserve(this.reserve);
            return priorityInfo;
        }

        @Override
        public TmsPriorityInfo from(Infos.PriorityInfo obj) {
            if (null != obj.getCarrierID())
                this.carrierID = new TmsIdentifier().from(obj.getCarrierID());
            this.carrierJobID = obj.getCarrierJobID();
            this.expectedStartTime = obj.getExpectedStartTime();
            this.expectedEndTime = obj.getExpectedEndTime();
            this.mandatoryFlag = obj.getMandatoryFlag();
            this.priority = obj.getPriority();
            this.reserve = obj.getReserve();
            return this;
        }
    }
}
